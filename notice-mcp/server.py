#!/usr/bin/env python3
"""Minimal MCP stdio server for online consultation notice records.

This implementation intentionally avoids third-party dependencies. It speaks
the JSON-RPC framing used by MCP over stdio and stores confirmations in SQLite.
"""

from __future__ import annotations

import json
import os
import sqlite3
import sys
import traceback
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parent
NOTICE_FILE = ROOT / "notices" / "online_consultation_disclaimer.md"
DB_PATH = Path(os.environ.get("NOTICE_MCP_DB", ROOT / "data" / "notice_mcp.sqlite3"))
NOTICE_URI = "notice://online-consultation/disclaimer"
HIGH_RISK_URI = "notice://online-consultation/high-risk-guide"
NOTICE_VERSION = "2026-07-06.v1"

HIGH_RISK_KEYWORDS = {
    "chest_pain": ["胸痛", "胸闷", "心前区疼痛", "压榨痛"],
    "stroke": ["口角歪斜", "肢体无力", "说话不清", "偏瘫", "突然视物模糊"],
    "breathing": ["呼吸困难", "喘不上气", "窒息", "气促"],
    "bleeding": ["大出血", "呕血", "便血", "咯血", "血流不止"],
    "consciousness": ["昏迷", "意识不清", "抽搐", "惊厥"],
    "trauma": ["严重外伤", "车祸", "高处坠落", "骨折"],
    "pregnancy": ["孕期腹痛", "阴道流血", "胎动减少"],
    "anaphylaxis": ["喉头水肿", "全身皮疹", "过敏性休克"],
}


def utc_now() -> str:
    return datetime.now(timezone.utc).isoformat(timespec="seconds")


def ensure_db() -> None:
    DB_PATH.parent.mkdir(parents=True, exist_ok=True)
    with sqlite3.connect(DB_PATH) as conn:
        conn.execute(
            """
            create table if not exists notice_confirmation_log (
                id integer primary key autoincrement,
                user_id text,
                patient_id text,
                session_id text,
                notice_version text not null,
                confirmed integer not null,
                high_risk_acknowledged integer not null default 0,
                source text,
                ip text,
                user_agent text,
                symptoms_text text,
                metadata_json text,
                created_at text not null
            )
            """
        )
        conn.execute(
            "create index if not exists idx_notice_confirmation_user "
            "on notice_confirmation_log(user_id, patient_id, session_id)"
        )


def read_message() -> dict[str, Any] | None:
    headers: dict[str, str] = {}
    while True:
        line = sys.stdin.buffer.readline()
        if line == b"":
            return None
        line = line.decode("ascii", errors="ignore").strip()
        if line == "":
            break
        if ":" in line:
            key, value = line.split(":", 1)
            headers[key.lower()] = value.strip()

    length = int(headers.get("content-length", "0"))
    if length <= 0:
        return None
    body = sys.stdin.buffer.read(length)
    return json.loads(body.decode("utf-8"))


def write_message(payload: dict[str, Any]) -> None:
    body = json.dumps(payload, ensure_ascii=False, separators=(",", ":")).encode("utf-8")
    sys.stdout.buffer.write(f"Content-Length: {len(body)}\r\n\r\n".encode("ascii"))
    sys.stdout.buffer.write(body)
    sys.stdout.buffer.flush()


def result_response(request_id: Any, result: Any) -> dict[str, Any]:
    return {"jsonrpc": "2.0", "id": request_id, "result": result}


def error_response(request_id: Any, code: int, message: str, data: Any = None) -> dict[str, Any]:
    error: dict[str, Any] = {"code": code, "message": message}
    if data is not None:
        error["data"] = data
    return {"jsonrpc": "2.0", "id": request_id, "error": error}


def text_content(text: str) -> list[dict[str, str]]:
    return [{"type": "text", "text": text}]


def load_notice_text() -> str:
    return NOTICE_FILE.read_text(encoding="utf-8")


def high_risk_guide() -> str:
    return (
        "高危症状急诊引导规则\n\n"
        "如用户描述胸痛、卒中样表现、呼吸困难、大出血、意识障碍、严重外伤、"
        "孕期异常出血/腹痛、严重过敏等情况，不继续普通线上问诊流程。"
        "应立即提示：线上咨询不能替代急诊处置，请马上拨打 120 或前往最近急诊科。"
    )


def list_resources() -> dict[str, Any]:
    return {
        "resources": [
            {
                "uri": NOTICE_URI,
                "name": "线上问诊风险告知与免责协议",
                "description": "对话前展示给用户确认的线上咨询免责声明。",
                "mimeType": "text/markdown",
            },
            {
                "uri": HIGH_RISK_URI,
                "name": "高危症状急诊引导规则",
                "description": "Agent 在识别高危症状时使用的强制急诊引导说明。",
                "mimeType": "text/markdown",
            },
        ]
    }


def read_resource(uri: str) -> dict[str, Any]:
    if uri == NOTICE_URI:
        text = load_notice_text()
    elif uri == HIGH_RISK_URI:
        text = high_risk_guide()
    else:
        raise ValueError(f"unknown resource uri: {uri}")
    return {"contents": [{"uri": uri, "mimeType": "text/markdown", "text": text}]}


def list_tools() -> dict[str, Any]:
    return {
        "tools": [
            {
                "name": "save_notice_confirmation",
                "description": "保存用户已阅读并确认线上问诊免责协议的日志。",
                "inputSchema": {
                    "type": "object",
                    "properties": {
                        "user_id": {"type": "string"},
                        "patient_id": {"type": "string"},
                        "session_id": {"type": "string"},
                        "notice_version": {"type": "string", "default": NOTICE_VERSION},
                        "confirmed": {"type": "boolean", "default": True},
                        "high_risk_acknowledged": {"type": "boolean", "default": False},
                        "source": {"type": "string", "description": "patient-app, doctor-app, agent, etc."},
                        "ip": {"type": "string"},
                        "user_agent": {"type": "string"},
                        "symptoms_text": {"type": "string"},
                        "metadata": {"type": "object"},
                    },
                    "required": ["confirmed"],
                },
            },
            {
                "name": "check_high_risk_symptoms",
                "description": "按关键词粗筛高危症状；命中时应强制引导急诊。",
                "inputSchema": {
                    "type": "object",
                    "properties": {
                        "text": {"type": "string", "description": "用户症状描述"},
                    },
                    "required": ["text"],
                },
            },
        ]
    }


def save_notice_confirmation(args: dict[str, Any]) -> dict[str, Any]:
    ensure_db()
    notice_version = str(args.get("notice_version") or NOTICE_VERSION)
    confirmed = 1 if bool(args.get("confirmed", True)) else 0
    high_risk_acknowledged = 1 if bool(args.get("high_risk_acknowledged", False)) else 0
    created_at = utc_now()

    with sqlite3.connect(DB_PATH) as conn:
        cursor = conn.execute(
            """
            insert into notice_confirmation_log (
                user_id, patient_id, session_id, notice_version, confirmed,
                high_risk_acknowledged, source, ip, user_agent, symptoms_text,
                metadata_json, created_at
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                nullable_str(args.get("user_id")),
                nullable_str(args.get("patient_id")),
                nullable_str(args.get("session_id")),
                notice_version,
                confirmed,
                high_risk_acknowledged,
                nullable_str(args.get("source")),
                nullable_str(args.get("ip")),
                nullable_str(args.get("user_agent")),
                nullable_str(args.get("symptoms_text")),
                json.dumps(args.get("metadata") or {}, ensure_ascii=False),
                created_at,
            ),
        )
        log_id = cursor.lastrowid

    return {
        "ok": True,
        "id": log_id,
        "notice_version": notice_version,
        "db_path": str(DB_PATH),
        "created_at": created_at,
    }


def nullable_str(value: Any) -> str | None:
    if value is None:
        return None
    value = str(value).strip()
    return value or None


def check_high_risk_symptoms(args: dict[str, Any]) -> dict[str, Any]:
    text = str(args.get("text") or "")
    matches: list[dict[str, str]] = []
    for category, keywords in HIGH_RISK_KEYWORDS.items():
        for keyword in keywords:
            if keyword in text:
                matches.append({"category": category, "keyword": keyword})
    return {
        "emergency": bool(matches),
        "matches": matches,
        "guidance": (
            "检测到可能的高危症状。请立即拨打 120 或前往最近急诊科，线上咨询不能替代急诊处置。"
            if matches
            else "未命中内置高危关键词；仍需根据实际症状和医生判断决定是否急诊。"
        ),
    }


def call_tool(name: str, args: dict[str, Any]) -> dict[str, Any]:
    if name == "save_notice_confirmation":
        data = save_notice_confirmation(args)
    elif name == "check_high_risk_symptoms":
        data = check_high_risk_symptoms(args)
    else:
        raise ValueError(f"unknown tool: {name}")
    return {"content": text_content(json.dumps(data, ensure_ascii=False)), "structuredContent": data}


def handle(request: dict[str, Any]) -> dict[str, Any] | None:
    request_id = request.get("id")
    method = request.get("method")
    params = request.get("params") or {}

    if request_id is None:
        return None

    try:
        if method == "initialize":
            return result_response(
                request_id,
                {
                    "protocolVersion": "2024-11-05",
                    "capabilities": {"resources": {}, "tools": {}},
                    "serverInfo": {"name": "notice-mcp", "version": "0.1.0"},
                },
            )
        if method == "ping":
            return result_response(request_id, {})
        if method == "resources/list":
            return result_response(request_id, list_resources())
        if method == "resources/read":
            return result_response(request_id, read_resource(params.get("uri", "")))
        if method == "tools/list":
            return result_response(request_id, list_tools())
        if method == "tools/call":
            return result_response(
                request_id,
                call_tool(str(params.get("name") or ""), params.get("arguments") or {}),
            )
        return error_response(request_id, -32601, f"method not found: {method}")
    except Exception as exc:
        return error_response(request_id, -32000, str(exc), traceback.format_exc())


def main() -> int:
    ensure_db()
    while True:
        request = read_message()
        if request is None:
            return 0
        response = handle(request)
        if response is not None:
            write_message(response)


if __name__ == "__main__":
    raise SystemExit(main())
