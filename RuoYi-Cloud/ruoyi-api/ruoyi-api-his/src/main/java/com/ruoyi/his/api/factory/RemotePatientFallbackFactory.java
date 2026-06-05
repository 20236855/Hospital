package com.ruoyi.his.api.factory;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.his.api.RemotePatientService;

/**
 * 患者远程服务降级处理
 */
@Component
public class RemotePatientFallbackFactory implements FallbackFactory<RemotePatientService>
{
    private static final Logger log = LoggerFactory.getLogger(RemotePatientFallbackFactory.class);

    @Override
    public RemotePatientService create(Throwable throwable)
    {
        log.error("Patient service call failed: {}", throwable.getMessage());
        return new RemotePatientService()
        {
            @Override
            public R<Map<String, Object>> getPatientInfo(Long patientId, String source)
            {
                return R.fail("Get patient failed: " + throwable.getMessage());
            }

            @Override
            public R<Map<String, Object>> getPatientByUserId(Long userId, String source)
            {
                return R.fail("Get patient by userId failed: " + throwable.getMessage());
            }
        };
    }
}
