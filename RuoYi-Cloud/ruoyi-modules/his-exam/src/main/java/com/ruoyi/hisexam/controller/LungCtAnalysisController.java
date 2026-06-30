package com.ruoyi.hisexam.controller;

import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.hisexam.domain.CtImageSlice;
import com.ruoyi.hisexam.domain.ExamApply;
import com.ruoyi.hisexam.service.ICtImageSliceService;
import com.ruoyi.hisexam.service.IExamApplyService;

/**
 * 肺部CT分析Controller
 * 检验医生流程：上传CT → AI分割 → 查看切片 → 保存影像入库
 * 诊断报告归门诊医生，此处不存诊断结论
 *
 * @author ruoyi
 * @date 2026-06-26
 */
@RestController
@RequestMapping("/lung-ct")
public class LungCtAnalysisController extends BaseController {

    @Value("${lung.ct.service.url:http://127.0.0.1:5004}")
    private String lungCtServiceUrl;

    /** CT切片文件存储根目录（默认在picture/ct_slices下，与ruoyi-file共用存储空间） */
    @Value("${lung.ct.slice.dir:C:/Users/Admin/Desktop/实训/hosipital/picture/ct_slices}")
    private String sliceDir;

    /** 对外访问URL前缀（与CtSliceResourceConfig映射一致） */
    @Value("${lung.ct.slice.url-prefix:/ct-slices}")
    private String sliceUrlPrefix;

    @Autowired
    private ICtImageSliceService ctImageSliceService;

    @Autowired
    private IExamApplyService examApplyService;

    private final RestTemplate restTemplate = new RestTemplate();

    // ==================== CT分析 ====================

    @GetMapping("/health")
    public AjaxResult health() {
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.getForEntity(
                lungCtServiceUrl + "/api/health", Map.class);
            Map<String, Object> result = new HashMap<>();
            result.put("available", response.getStatusCode().is2xxSuccessful());
            result.put("python", response.getBody());
            return success(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("available", false);
            result.put("message", "肺部CT分析服务未启动: " + lungCtServiceUrl);
            return success(result);
        }
    }

    /**
     * 上传CT文件，执行CT值分割分析
     */
    @PostMapping("/analyze")
    public AjaxResult analyzeLungCt(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "params", required = false, defaultValue = "{}") String params) {

        if (file == null || file.isEmpty()) return error("请选择肺部CT文件");

        String lower = file.getOriginalFilename() != null
            ? file.getOriginalFilename().toLowerCase() : "";
        if (!(lower.endsWith(".zip") || lower.endsWith(".mhd") || lower.endsWith(".nii")
                || lower.endsWith(".nii.gz") || lower.endsWith(".dcm"))) {
            return error("不支持的文件格式，请上传 DICOM(zip)、MHD 或 NIfTI");
        }
        if (file.getSize() > 500 * 1024 * 1024) return error("文件大小不能超过500MB");

        try {
            String targetUrl = lungCtServiceUrl + "/api/analyze";
            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override public String getFilename() { return file.getOriginalFilename(); }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);
            body.add("params", params);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, new HttpHeaders());

            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.exchange(targetUrl, HttpMethod.POST, requestEntity, Map.class);
            if (response.getBody() == null) return error("分析服务返回为空");
            return success(response.getBody());

        } catch (org.springframework.web.client.ResourceAccessException e) {
            logger.error("无法连接服务: {}", e.getMessage());
            return error("肺部CT分析服务未启动，请执行: python lung_ct_segment.py --port 5004");
        } catch (Exception e) {
            logger.error("CT分析失败", e);
            return error("分析失败: " + e.getMessage());
        }
    }

    // ==================== 保存切片（Base64→磁盘→URL入库） ====================

    /**
     * 检验医生保存CT切片影像到数据库
     * 前端传来Base64，后端写磁盘，库中存文件URL
     * 不存诊断报告——诊断归门诊医生
     */
    @PostMapping("/save")
    public AjaxResult saveSlices(@RequestBody Map<String, Object> saveData) {
        try {
            // 解析参数
            Long registerId = saveData.get("applyId") != null
                ? Long.valueOf(saveData.get("applyId").toString()) : null;
            Long techId = saveData.get("techId") != null
                ? Long.valueOf(saveData.get("techId").toString()) : null;
            String checkType = (String) saveData.getOrDefault("checkType", "LUNG_CT");
            Long patientId = saveData.get("patientId") != null
                ? Long.valueOf(saveData.get("patientId").toString()) : null;
            Long doctorId = saveData.get("doctorId") != null
                ? Long.valueOf(saveData.get("doctorId").toString()) : null;
            String fileName = (String) saveData.get("fileName");

            if (registerId == null) return error("挂号ID不能为空");

            // 查找或创建 exam_apply 记录
            Long applyId = findOrCreateApply(registerId, techId, patientId, doctorId, checkType, fileName);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> slices = (List<Map<String, Object>>) saveData.get("slices");
            if (slices == null || slices.isEmpty()) return error("没有可保存的切片数据");

            // 创建该申请单的存储目录  upload/ct_slices/{applyId}/
            Path applyDir = Paths.get(sliceDir, String.valueOf(applyId));
            Files.createDirectories(applyDir);

            // 先清空旧数据（磁盘文件 + 数据库记录）
            deleteSliceFiles(applyDir);
            ctImageSliceService.deleteCtImageSliceByApplyId(applyId);

            // 逐片写入磁盘 + 入库
            List<CtImageSlice> entities = new ArrayList<>();
            for (int i = 0; i < slices.size(); i++) {
                Map<String, Object> s = slices.get(i);

                // 写标注图到磁盘
                String annotatedB64 = (String) s.get("sliceImage");
                String imagePath = null;
                if (annotatedB64 != null && !annotatedB64.isEmpty()) {
                    String fname = "slice_" + i + ".png";
                    byte[] bytes = Base64.getDecoder().decode(annotatedB64);
                    Files.write(applyDir.resolve(fname), bytes);
                    imagePath = sliceUrlPrefix + "/" + applyId + "/" + fname;
                }

                // 写原始图到磁盘
                String rawB64 = (String) s.get("rawImage");
                String rawPath = null;
                if (rawB64 != null && !rawB64.isEmpty()) {
                    String fname = "raw_" + i + ".png";
                    byte[] bytes = Base64.getDecoder().decode(rawB64);
                    Files.write(applyDir.resolve(fname), bytes);
                    rawPath = sliceUrlPrefix + "/" + applyId + "/" + fname;
                }

                CtImageSlice entity = new CtImageSlice();
                entity.setApplyId(applyId);
                entity.setCheckType(checkType);
                entity.setPatientId(patientId);
                entity.setDoctorId(doctorId);
                entity.setSliceIndex(i);
                entity.setSliceZ(s.get("sliceZ") != null
                    ? Integer.valueOf(s.get("sliceZ").toString()) : i);
                entity.setImagePath(imagePath);
                entity.setRawImagePath(rawPath);
                entity.setFileName(fileName);
                entity.setRemark((String) saveData.get("remark"));

                if (s.get("huMin") != null) entity.setHuMin(Double.valueOf(s.get("huMin").toString()));
                if (s.get("huMax") != null) entity.setHuMax(Double.valueOf(s.get("huMax").toString()));
                if (s.get("huMean") != null) entity.setHuMean(Double.valueOf(s.get("huMean").toString()));
                if (s.get("sliceThickness") != null) entity.setSliceThickness(Double.valueOf(s.get("sliceThickness").toString()));
                if (s.get("pixelSpacingX") != null) entity.setPixelSpacingX(Double.valueOf(s.get("pixelSpacingX").toString()));
                if (s.get("pixelSpacingY") != null) entity.setPixelSpacingY(Double.valueOf(s.get("pixelSpacingY").toString()));

                // AI分割/检测结果 — 整体数据存在第一条切片中
                if (i == 0) {
                    if (saveData.get("segmentationData") != null)
                        entity.setSegmentationData(saveData.get("segmentationData").toString());
                    if (saveData.get("detectionResult") != null)
                        entity.setDetectionResult(saveData.get("detectionResult").toString());
                }

                entities.add(entity);
            }

            ctImageSliceService.batchInsertCtImageSlice(entities);

            Map<String, Object> result = new HashMap<>();
            result.put("savedCount", entities.size());
            result.put("applyId", applyId);
            result.put("imageDir", sliceUrlPrefix + "/" + applyId + "/");
            return success(result);

        } catch (Exception e) {
            logger.error("保存CT切片失败", e);
            return error("保存失败: " + e.getMessage());
        }
    }

    private void deleteSliceFiles(Path dir) {
        try {
            if (Files.exists(dir)) {
                File[] files = dir.toFile().listFiles();
                if (files != null) for (File f : files) f.delete();
            }
        } catch (Exception ignored) {}
    }

    /**
     * 查找或创建 exam_apply 记录（检验医生保存时若无申请单则自动创建）
     */
    private Long findOrCreateApply(Long registerId, Long techId,
            Long patientId, Long doctorId, String checkType, String fileName) {
        // 查找已存在的申请单（同挂号+同医技项目）
        ExamApply query = new ExamApply();
        query.setRegisterId(registerId);
        query.setTechId(techId);
        query.setApplyType("CHECK");
        List<ExamApply> existing = examApplyService.selectExamApplyList(query);
        if (existing != null && !existing.isEmpty()) {
            return existing.get(0).getApplyId();
        }

        // 不存在则自动创建
        ExamApply apply = new ExamApply();
        apply.setRegisterId(registerId);
        apply.setTechId(techId);
        apply.setPatientId(patientId);
        apply.setDoctorId(doctorId);
        apply.setApplyType("CHECK");
        apply.setApplyPosition("肺部CT");
        apply.setApplyInfo("检验医生执行肺部CT检查，由" + fileName + "生成");
        apply.setApplyStatus("COMPLETED");
        apply.setPayStatus("paid");
        apply.setExamTime(new Date());
        examApplyService.insertExamApply(apply);
        return apply.getApplyId();
    }

    // ==================== 查询 ====================

    @GetMapping("/slices/{applyId}")
    public AjaxResult getSlicesByApplyId(@PathVariable Long applyId) {
        List<CtImageSlice> slices = ctImageSliceService.selectCtImageSliceByApplyId(applyId);
        List<Map<String, Object>> result = slices.stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("sliceId", s.getSliceId());
            m.put("applyId", s.getApplyId());
            m.put("checkType", s.getCheckType());
            m.put("sliceIndex", s.getSliceIndex());
            m.put("sliceZ", s.getSliceZ());
            m.put("imagePath", s.getImagePath());
            m.put("rawImagePath", s.getRawImagePath());
            m.put("huMin", s.getHuMin());
            m.put("huMax", s.getHuMax());
            m.put("huMean", s.getHuMean());
            m.put("sliceThickness", s.getSliceThickness());
            m.put("pixelSpacingX", s.getPixelSpacingX());
            m.put("pixelSpacingY", s.getPixelSpacingY());
            m.put("segmentationData", s.getSegmentationData());
            m.put("detectionResult", s.getDetectionResult());
            m.put("fileName", s.getFileName());
            m.put("remark", s.getRemark());
            m.put("createTime", s.getCreateTime());
            return m;
        }).collect(Collectors.toList());
        return success(result);
    }

    @DeleteMapping("/slices/{applyId}")
    public AjaxResult deleteSlicesByApplyId(@PathVariable Long applyId) {
        // 同时清理磁盘文件
        Path applyDir = Paths.get(sliceDir, String.valueOf(applyId));
        deleteSliceFiles(applyDir);
        try { Files.deleteIfExists(applyDir); } catch (Exception ignored) {}
        int rows = ctImageSliceService.deleteCtImageSliceByApplyId(applyId);
        return success("已清理 " + rows + " 条记录");
    }
}
