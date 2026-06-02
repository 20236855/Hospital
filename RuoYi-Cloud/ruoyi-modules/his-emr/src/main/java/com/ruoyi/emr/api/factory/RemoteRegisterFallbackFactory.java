package com.ruoyi.emr.api.factory;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.emr.api.RemoteRegisterService;

/**
 * Register remote service fallback.
 */
@Component
public class RemoteRegisterFallbackFactory implements FallbackFactory<RemoteRegisterService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteRegisterFallbackFactory.class);

    @Override
    public RemoteRegisterService create(Throwable throwable)
    {
        log.error("Register service call failed: {}", throwable.getMessage());
        return new RemoteRegisterService()
        {
            @Override
            public R<Map<String, Object>> getRegisterInfo(Long registerId, String source)
            {
                return R.fail("Get register failed: " + throwable.getMessage());
            }
        };
    }
}
