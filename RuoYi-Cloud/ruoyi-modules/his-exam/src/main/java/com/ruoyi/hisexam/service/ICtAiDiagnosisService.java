package com.ruoyi.hisexam.service;

import java.util.Map;

/**
 * CT AI diagnosis service.
 */
public interface ICtAiDiagnosisService
{
    Map<String, Object> diagnose(Map<String, Object> request);
}
