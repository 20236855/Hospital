package com.ruoyi.his.api.factory;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.his.api.RemotePrescriptionService;

/**
 * Prescription remote service fallback.
 */
@Component
public class RemotePrescriptionFallbackFactory implements FallbackFactory<RemotePrescriptionService>
{
    private static final Logger log = LoggerFactory.getLogger(RemotePrescriptionFallbackFactory.class);

    @Override
    public RemotePrescriptionService create(Throwable throwable)
    {
        log.error("Prescription service call failed: {}", throwable.getMessage());
        return new RemotePrescriptionService()
        {
            @Override
            public R<Map<String, Object>> getPrescriptionPayInfo(Long prescriptionId, String source)
            {
                return R.fail("Get prescription pay info failed: " + throwable.getMessage());
            }

            @Override
            public R<Boolean> markPrescriptionPaid(Long prescriptionId, String source)
            {
                return R.fail("Mark prescription paid failed: " + throwable.getMessage());
            }
        };
    }
}
