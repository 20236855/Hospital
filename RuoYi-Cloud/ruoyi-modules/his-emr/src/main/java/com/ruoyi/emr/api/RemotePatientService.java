package com.ruoyi.emr.api;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.emr.api.factory.RemotePatientFallbackFactory;

/**
 * Patient remote service.
 */
@FeignClient(contextId = "remotePatientService", value = ServiceNameConstants.PATIENT_SERVICE, fallbackFactory = RemotePatientFallbackFactory.class)
public interface RemotePatientService
{
    @GetMapping("/patient/inner/{patientId}")
    public R<Map<String, Object>> getPatientInfo(@PathVariable("patientId") Long patientId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
