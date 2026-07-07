# notice-mcp

独立的线上问诊风险告知 MCP 服务。它用最小 MCP stdio 协议实现两个能力：

- Resource：读取线上问诊风险告知与免责协议。
- Tool：保存用户已阅读确认日志到 SQLite 数据库。

## 运行

```powershell
python .\notice-mcp\server.py
```

默认数据库位置：

```text
notice-mcp/data/notice_mcp.sqlite3
```

可通过环境变量改位置：

```powershell
$env:NOTICE_MCP_DB='D:\tmp\notice_mcp.sqlite3'
python .\notice-mcp\server.py
```

## MCP 配置示例

```json
{
  "mcpServers": {
    "notice-mcp": {
      "command": "python",
      "args": ["D:/RuoYi-Code/Hospital/notice-mcp/server.py"],
      "env": {
        "NOTICE_MCP_DB": "D:/RuoYi-Code/Hospital/notice-mcp/data/notice_mcp.sqlite3"
      }
    }
  }
}
```

## 资源

- `notice://online-consultation/disclaimer`
- `notice://online-consultation/high-risk-guide`

Agent 在发起问诊前读取 `notice://online-consultation/disclaimer`，展示给用户确认。

## 工具

### save_notice_confirmation

保存用户确认日志。

示例参数：

```json
{
  "user_id": "1001",
  "patient_id": "2001",
  "session_id": "session_20260706",
  "notice_version": "2026-07-06.v1",
  "confirmed": true,
  "high_risk_acknowledged": true,
  "source": "patient-app",
  "ip": "127.0.0.1",
  "user_agent": "RuoYi-Patient",
  "symptoms_text": "头晕一天，无胸痛",
  "metadata": {
    "scene": "before_chat"
  }
}
```

### check_high_risk_symptoms

按关键词做轻量高危症状粗筛。命中时应强制引导急诊，不进入普通线上咨询。

示例参数：

```json
{
  "text": "突然胸痛，喘不上气"
}
```

## 前端接入建议

1. 进入问诊页时，由 Agent 通过 MCP Resource 读取协议文本。
2. 前端弹窗展示协议，必须勾选确认后才启用输入框和快捷问题。
3. 点击确认后调用 MCP Tool `save_notice_confirmation` 记录日志。
4. 每次用户提交症状前，可调用 `check_high_risk_symptoms` 粗筛；命中时展示急诊引导并阻止普通咨询。

如果 Web 前端不能直接连 stdio MCP，可在现有后端增加一个很薄的桥接接口，由后端 Agent 或 MCP client 代调本服务。
