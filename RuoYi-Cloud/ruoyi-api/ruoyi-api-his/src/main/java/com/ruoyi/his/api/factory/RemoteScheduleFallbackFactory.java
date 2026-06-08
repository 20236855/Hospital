package com.ruoyi.his.api.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.his.api.RemoteScheduleService;

/**
 * 排班远程服务降级处理
 */
@Component
public class RemoteScheduleFallbackFactory implements FallbackFactory<RemoteScheduleService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteScheduleFallbackFactory.class);

    @Override
    public RemoteScheduleService create(Throwable throwable)
    {
        log.error("Schedule service call failed: {}", throwable.getMessage());
        return new RemoteScheduleService()
        {
            @Override
            public R<Boolean> bookSchedule(Long scheduleId, String source)
            {
                return R.fail("Book schedule failed: " + throwable.getMessage());
            }

            @Override
            public R<Boolean> cancelSchedule(Long scheduleId, String source)
            {
                return R.fail("Cancel schedule failed: " + throwable.getMessage());
            }

            @Override
            public R<Object> getScheduleInfo(Long scheduleId, String source)
            {
                return R.fail("Get schedule info failed: " + throwable.getMessage());
            }
        };
    }
}
