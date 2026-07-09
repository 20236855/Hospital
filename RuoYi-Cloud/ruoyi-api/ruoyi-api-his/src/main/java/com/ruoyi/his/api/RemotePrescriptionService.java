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
import com.ruoyi.his.api.factory.RemotePrescriptionFallbackFactory;

/**
 * Prescription remote service.
 */
@FeignClient(contextId = "hisRemotePrescriptionService", value = ServiceNameConstants.PRESCRIPTION_SERVICE, fallbackFactory = RemotePrescriptionFallbackFactory.class)
public interface RemotePrescriptionService
{
    /**
     * Get unpaid prescription payment info by prescription id.
     */
    @GetMapping("/prescription/inner/pay-info/{prescriptionId}")
    public R<Map<String, Object>> getPrescriptionPayInfo(@PathVariable("prescriptionId") Long prescriptionId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * Mark unpaid prescription as paid.
     */
    @PutMapping("/prescription/inner/pay/{prescriptionId}")
    public R<Boolean> markPrescriptionPaid(@PathVariable("prescriptionId") Long prescriptionId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
