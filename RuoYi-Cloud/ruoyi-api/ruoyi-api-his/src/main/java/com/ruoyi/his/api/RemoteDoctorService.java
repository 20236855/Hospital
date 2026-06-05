package com.ruoyi.his.api;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.his.api.factory.RemoteDoctorFallbackFactory;

/**
 * 医生远程服务
 */
@FeignClient(contextId = "hisRemoteDoctorService", value = ServiceNameConstants.DOCTOR_SERVICE, fallbackFactory = RemoteDoctorFallbackFactory.class)
public interface RemoteDoctorService
{
    /**
     * 根据系统用户ID查询医生信息
     */
    @GetMapping("/doctor/getByUserId/{userId}")
    public R<Map<String, Object>> getDoctorByUserId(@PathVariable("userId") Long userId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
