package com.ruoyi.his.api;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.his.api.factory.RemoteExamFallbackFactory;

/**
 * Exam remote service.
 */
@FeignClient(contextId = "hisRemoteExamService", value = ServiceNameConstants.EXAM_SERVICE, fallbackFactory = RemoteExamFallbackFactory.class)
public interface RemoteExamService
{
    /**
     * Get unpaid exam payment info by register id.
     */
    @GetMapping("/apply/inner/pay-info/register/{registerId}")
    public R<Map<String, Object>> getExamPayInfo(@PathVariable("registerId") Long registerId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * Mark unpaid exam applies under register as paid.
     */
    @PutMapping("/apply/inner/pay/register/{registerId}")
    public R<Boolean> markRegisterExamPaid(@PathVariable("registerId") Long registerId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
