"""
knowledge-base 入库脚本：把 resources/policy/registration/faq 下的 .md
切分 → embedding → 写入本地 Chroma 服务。

用法：
  1) 安装依赖： pip install chromadb requests pyyaml
  2) 配置环境变量(用 API embedding，推荐)：
       set EMBEDDING_API_KEY=你的key
       set EMBEDDING_BASE_URL=https://api.siliconflow.cn/v1
       set EMBEDDING_MODEL=BAAI/bge-large-zh-v1.5
     若未设置 EMBEDDING_API_KEY，则自动回退到本地 sentence-transformers(需 pip install sentence-transformers)。
  3) 运行： python scripts/build_index.py
     脚本直接用 PersistentClient 写磁盘(./chroma 目录)，无需 Chroma 服务在线。
     之后启动 chroma run --path ./chroma 提供 HTTP 查询(供 Java 侧 ChromaRetriever 调用)。
"""
import os
import glob
import yaml
import requests
from chromadb import PersistentClient

# ---------- 配置(环境变量可覆盖) ----------
KB_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CHROMA_PATH = os.getenv("CHROMA_PATH", os.path.join(KB_ROOT, "chroma"))
COLLECTION = os.getenv("KB_COLLECTION", "hospital_kb")

EMBEDDING_API_KEY = os.getenv("EMBEDDING_API_KEY", "")
EMBEDDING_KEY_FILE = os.getenv("EMBEDDING_KEY_FILE", "")
EMBEDDING_BASE_URL = os.getenv("EMBEDDING_BASE_URL", "https://api.siliconflow.cn/v1")
EMBEDDING_MODEL = os.getenv("EMBEDDING_MODEL", "BAAI/bge-large-zh-v1.5")
# 默认从项目根目录的 embedding.txt 读取 key（与 DeepSeekApiKey.txt 同套路）
DEFAULT_KEY_PATH = os.path.abspath(os.path.join(KB_ROOT, "..", "embedding.txt"))


def resolve_embedding_key():
    if EMBEDDING_API_KEY:
        return EMBEDDING_API_KEY
    candidates = []
    if EMBEDDING_KEY_FILE:
        candidates.append(EMBEDDING_KEY_FILE)
    candidates.append(DEFAULT_KEY_PATH)
    candidates.append(os.path.abspath(os.path.join(KB_ROOT, "embedding.txt")))
    for c in candidates:
        if os.path.isfile(c):
            with open(c, encoding="utf-8") as f:
                return f.read().strip()
    return None


def get_embeddings(texts):
    """OpenAI 兼容 embedding（SiliconFlow）；无 key 时回退本地 sentence-transformers。"""
    key = resolve_embedding_key()
    if key:
        url = EMBEDDING_BASE_URL.rstrip("/") + "/embeddings"
        resp = requests.post(
            url,
            headers={"Authorization": f"Bearer {key}"},
            json={"model": EMBEDDING_MODEL, "input": texts},
            timeout=60,
        )
        resp.raise_for_status()
        data = resp.json()["data"]
        return [d["embedding"] for d in sorted(data, key=lambda x: x["index"])]

    # 本地回退
    from sentence_transformers import SentenceTransformer
    model = SentenceTransformer(EMBEDDING_MODEL)
    return model.encode(texts, normalize_embeddings=True).tolist()


def parse_frontmatter(text):
    if text.startswith("---"):
        end = text.find("\n---", 3)
        if end != -1:
            fm = yaml.safe_load(text[3:end]) or {}
            body = text[end + 4:].strip()
            return fm, body
    return {}, text


def split_by_heading(md):
    """按 ## 标题切块，每块带小标题，作为 Chroma 切块边界。"""
    chunks = []
    cur_title, cur_lines = "", []
    for line in md.splitlines():
        if line.startswith("## "):
            if cur_lines:
                chunks.append((cur_title, "\n".join(cur_lines).strip()))
            cur_title = line[3:].strip()
            cur_lines = []
        else:
            cur_lines.append(line)
    if cur_lines:
        chunks.append((cur_title, "\n".join(cur_lines).strip()))
    return chunks


def main():
    client = PersistentClient(path=CHROMA_PATH)
    col = client.get_or_create_collection(COLLECTION)

    docs, metas, ids, emb_texts = [], [], [], []
    for fp in glob.glob(os.path.join(KB_ROOT, "**", "*.md"), recursive=True):
        rel = os.path.relpath(fp, KB_ROOT)
        if "scripts" in rel or rel == "README.md":
            continue
        text = open(fp, encoding="utf-8").read()
        fm, body = parse_frontmatter(text)
        tags = fm.get("tags", [])
        tags_str = ",".join(tags) if isinstance(tags, list) else str(tags)
        for i, (h, c) in enumerate(split_by_heading(body)):
            if not c:
                continue
            docs.append(c)
            metas.append({
                "source": rel,
                "heading": h,
                "title": str(fm.get("title", "")),
                "category": str(fm.get("category", "")),
                "department": str(fm.get("department", "")),
                "tags": tags_str,
            })
            ids.append(f"{rel}#{i}")
            emb_texts.append(c)

    if not docs:
        print("未发现任何 .md 知识文件，已退出。")
        return

    print(f"正在对 {len(docs)} 个知识块做 embedding ...")
    embs = get_embeddings(emb_texts)
    col.upsert(documents=docs, embeddings=embs, metadatas=metas, ids=ids)
    print(f"完成：已写入/更新 {len(docs)} 个知识块到 Chroma collection '{COLLECTION}'")


if __name__ == "__main__":
    main()
