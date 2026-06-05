package com.ruoyi.his.api;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.his.api.factory.RemotePatientFallbackFactory;

/**
 * 患者远程服务
 */
@FeignClient(contextId = "hisRemotePatientService", value = ServiceNameConstants.PATIENT_SERVICE, fallbackFactory = RemotePatientFallbackFactory.class)
public interface RemotePatientService
{
    /**
     * 根据患者ID查询患者信息
     */
    @GetMapping("/patient/inner/{patientId}")
    public R<Map<String, Object>> getPatientInfo(@PathVariable("patientId") Long patientId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 根据系统用户ID查询患者信息
     */
    @GetMapping("/patient/getByUserId/{userId}")
    public R<Map<String, Object>> getPatientByUserId(@PathVariable("userId") Long userId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
