package com.ruoyi.his.api.factory;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.his.api.RemoteRegisterService;

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

            @Override
            public R<Map<String, Object>> getPayInfo(Long registerId, String source)
            {
                return R.fail("Get register pay info failed: " + throwable.getMessage());
            }

            @Override
            public R<Boolean> markPaid(Long registerId, String source)
            {
                return R.fail("Mark register paid failed: " + throwable.getMessage());
            }

            @Override
            public R<Map<String, Object>> createAgentRegister(Map<String, Object> register, String source)
            {
                return R.fail("Create agent register failed: " + throwable.getMessage());
            }
        };
    }
}
