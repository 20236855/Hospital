---
name: medical-vision-ai-workflow
description: Use when implementing or repairing medical image recognition workflows in this RuoYi Hospital project, including image/CT upload, local Python lesion annotation services, selecting the current image or slice, Doubao Ark multimodal AI diagnosis dialogs, structured result display, backend proxy controllers, permissions, and image token-limit handling.
---

# Medical Vision AI Workflow

Use this skill when adding a new medical recognition page or fixing an existing one, such as CT metal artifact/hemorrhage, skin mole recognition, lung CT, ultrasound, pathology, or similar image-based checks.

## Repository Map

- Frontend check pages: `RuoYi-Cloud-Vue3/src/views/hisexam/check/**/index.vue`
- Frontend APIs: `RuoYi-Cloud-Vue3/src/api/hisexam/*Analysis.js`
- Backend controllers: `RuoYi-Cloud/ruoyi-modules/his-exam/src/main/java/com/ruoyi/hisexam/controller/*AnalysisController.java`
- Backend AI services: `RuoYi-Cloud/ruoyi-modules/his-exam/src/main/java/com/ruoyi/hisexam/service/impl/*AiDiagnosisServiceImpl.java`
- Local Python analyzers: `RuoYi-Cloud/ruoyi-modules/his-exam/scripts/*_service.py`
- Doubao API key: `D:/RuoYi-Code/Hospital/ApiKey.txt`

## Target Flow

Build this workflow unless the user explicitly asks for a different UX:

1. User uploads a study/image.
2. Frontend immediately calls the local analysis endpoint. Do not require a second “AI识别分析” button after upload.
3. Backend proxies the file to the local Python service, usually `http://127.0.0.1:<port>/api/analyze`.
4. Frontend displays annotation/detection results in the “检测结果” area near the image.
5. Doctor clicks “选择该图片” or “选择当前切片”.
6. Frontend opens an AI dialog with image preview, question textarea, send button, and structured AI response cards.
7. Frontend sends the selected image/slice plus the doctor question and compact local detection result to the backend AI endpoint.
8. Backend calls Doubao Ark Responses API with `input_image` + `input_text`.
9. Frontend displays `answer`, findings, confidence/risk, doctor advice, and fills the diagnosis report form.

## Frontend Checklist

### Upload and Local Analysis

- Keep original preview URL for display with `URL.createObjectURL(file)`.
- After setting the file, call analysis immediately:

```js
function setImageFile(file) {
  imageFile.value = file
  imageUrl.value = URL.createObjectURL(file)
  result.value = null
  chatMessages.value = []
  analyzeImage()
}
```

- Remove extra manual “AI识别分析” buttons if upload should auto-analyze.
- Show loading state inside the “检测结果” panel while the local Python analyzer runs.
- Keep “重新上传” near the image preview.

### Selection and AI Dialog

- Put “选择该图片” in the detection result panel for single-image pages.
- Put “选择当前切片” in each relevant annotation panel for slice-based pages.
- Disable the selection button while local analysis is running.
- Dialog should show:
  - selected image/slice preview
  - selected source metadata, such as image name, slice number, Z index, or detection type
  - doctor question textarea with a sensible default
  - structured assistant response, not raw JSON

### Image Size Control

Never send full phone-resolution images directly to Doubao. Compress before sending:

```js
function imageFileToBase64(file, maxSide = 1024, quality = 0.82) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const image = new Image()
      image.onload = () => {
        const scale = Math.min(1, maxSide / Math.max(image.width, image.height))
        const canvas = document.createElement('canvas')
        canvas.width = Math.max(1, Math.round(image.width * scale))
        canvas.height = Math.max(1, Math.round(image.height * scale))
        canvas.getContext('2d').drawImage(image, 0, 0, canvas.width, canvas.height)
        resolve(canvas.toDataURL('image/jpeg', quality))
      }
      image.onerror = reject
      image.src = reader.result
    }
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}
```

CT slices returned by local services are usually already small enough, but still avoid sending complete studies or large base64 arrays.

### Structured Display

Prefer rendering AI output as cards:

- Direct answer: `answer || suggestion || reply || rawText`
- Classification/conclusion: benign/suspicious/malignant/unknown or normal/uncertain etc.
- Findings list: name, description, severity, confidence if present
- Doctor advice: ordered list
- Report patch: prefill diagnosis form fields from `report`

Normalize confidence:

```js
function formatConfidence(value) {
  const n = Number(value)
  if (!Number.isFinite(n)) return '-'
  return `${Math.round(n <= 1 ? n * 100 : n)}%`
}
```

## Backend Checklist

### Controller

- Route pattern: `/xxx-analysis/analyze`, `/xxx-analysis/health`, `/xxx-analysis/ai-diagnosis`.
- For check-page endpoints, either ensure the logged-in role has the permission or do not add unrelated `@RequiresPermissions`. A common failure is:
  - `权限码校验失败 'hisexam:skin:list'`
- Local analysis endpoint should proxy `MultipartFile file` to the Python service using `multipart/form-data`.
- Include returned Python result plus `fileName` and patient/apply metadata if available.

### Local Python Analyzer

- Expose:
  - `GET /api/health`
  - `POST /api/analyze` with multipart field `file`
- Return compact fields that frontend can render:
  - booleans/counts/confidence/threshold
  - boxes or mask metadata
  - optional `annotatedImage` base64 for display
- Do not pass `annotatedImage` back into Doubao prompt text.

### Doubao Ark Multimodal Service

Use Responses API:

- URL: `https://ark.cn-beijing.volces.com/api/v3/responses`
- Model currently used here: `doubao-seed-2-0-pro-260215`
- Read API key from `D:/RuoYi-Code/Hospital/ApiKey.txt` first; environment fallback is okay.
- Request body must use `input`, not chat `messages`, when sending images.

Request shape:

```java
body.put("model", MODEL_ID);
message.put("role", "user");
content.add(Map.of("type", "input_image", "image_url", normalizeImageUrl(imageBase64)));
content.add(Map.of("type", "input_text", "text", buildPrompt(request)));
message.put("content", content);
body.put("input", List.of(message));
```

Extract response text from either:

- top-level `output_text`
- or `output[*].content[*].text`
- or `output[*].content[*].output_text`

Parse JSON from the returned text. If parsing fails, return a fallback object with `reply`/`answer` and empty arrays.

### Prompt Requirements

Prompt must demand pure JSON and include a schema. Keep it modality-specific:

- Skin: `classification`, `confidence`, `answer`, `suggestion`, `findings`, `doctorAdvice`, `recommendations`, `report`.
- CT: `findings`, `answer`, `suggestion`, `features`, `report`.

Always include:

- selected image/slice context
- patient/apply metadata if useful
- compact local detection result
- doctor question
- medical safety disclaimer

## Common Failures

- **403 / permission code failed**: remove inappropriate `@RequiresPermissions` or add the exact permission to the role/menu.
- **400 token limit**: compress image client-side; remove base64 fields such as `annotatedImage` from prompt context.
- **401 unauthorized**: confirm `ApiKey.txt` is read by the running backend process; restart service after changing key logic.
- **Python service unavailable**: start the script on the expected port and confirm `/api/health`.
- **AI returns raw JSON in UI**: render structured fields instead of dumping `JSON.stringify`.
- **Diagnosis form not updated**: map `report` fields back into local form state after each AI response.

## Validation Pass

Before finishing:

1. Search for stale manual buttons such as “AI识别分析” if the intended flow is upload-triggered.
2. Search for stale permission annotations on new check endpoints.
3. Confirm the Python script path and controller port match.
4. Trigger one upload if the local service is running.
5. Trigger one AI dialog request with a compressed/selected image.
6. Check that detection result, AI response cards, and diagnosis form are all updated.

