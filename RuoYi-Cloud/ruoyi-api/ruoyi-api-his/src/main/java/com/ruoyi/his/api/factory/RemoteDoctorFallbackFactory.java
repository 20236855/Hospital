package com.ruoyi.his.api.factory;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.his.api.RemoteDoctorService;

/**
 * 医生远程服务降级处理
 */
@Component
public class RemoteDoctorFallbackFactory implements FallbackFactory<RemoteDoctorService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteDoctorFallbackFactory.class);

    @Override
    public RemoteDoctorService create(Throwable throwable)
    {
        log.error("Doctor service call failed: {}", throwable.getMessage());
        return new RemoteDoctorService()
        {
            @Override
            public R<Map<String, Object>> getDoctorByUserId(Long userId, String source)
            {
                return R.fail("Get doctor failed: " + throwable.getMessage());
            }
        };
    }
}
