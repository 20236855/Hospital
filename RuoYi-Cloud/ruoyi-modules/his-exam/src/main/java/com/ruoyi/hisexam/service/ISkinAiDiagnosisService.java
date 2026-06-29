package com.ruoyi.hisexam.service;

import java.util.Map;

/**
 * 皮肤 AI 诊断服务接口
 */
public interface ISkinAiDiagnosisService
{
    /**
     * 基于检测结果 + 医生问题进行 AI 诊断
     */
    Map<String, Object> diagnose(Map<String, Object> request);
}
