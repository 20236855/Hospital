package com.ruoyi.his.api.factory;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.his.api.RemoteEncounterService;

/**
 * Encounter remote service fallback.
 */
@Component
public class RemoteEncounterFallbackFactory implements FallbackFactory<RemoteEncounterService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteEncounterFallbackFactory.class);

    @Override
    public RemoteEncounterService create(Throwable throwable)
    {
        log.error("Encounter service call failed: {}", throwable.getMessage());
        return new RemoteEncounterService()
        {
            @Override
            public R<Boolean> syncFromCheckIn(Map<String, Object> data, String source)
            {
                return R.fail("Sync encounter from check-in failed: " + throwable.getMessage());
            }
        };
    }
}
