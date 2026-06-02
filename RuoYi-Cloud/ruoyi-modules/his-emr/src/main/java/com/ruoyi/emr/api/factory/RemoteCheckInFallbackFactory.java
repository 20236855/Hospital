package com.ruoyi.emr.api.factory;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.emr.api.RemoteCheckInService;

/**
 * CheckIn remote service fallback.
 */
@Component
public class RemoteCheckInFallbackFactory implements FallbackFactory<RemoteCheckInService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteCheckInFallbackFactory.class);

    @Override
    public RemoteCheckInService create(Throwable throwable)
    {
        log.error("CheckIn service call failed: {}", throwable.getMessage());
        return new RemoteCheckInService()
        {
            @Override
            public R<Map<String, Object>> getCheckInInfo(Long registerId, String source)
            {
                return R.fail("Get checkIn failed: " + throwable.getMessage());
            }
        };
    }
}
