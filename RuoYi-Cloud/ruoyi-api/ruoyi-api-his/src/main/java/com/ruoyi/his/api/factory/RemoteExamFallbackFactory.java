package com.ruoyi.his.api.factory;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.his.api.RemoteExamService;

/**
 * Exam remote service fallback.
 */
@Component
public class RemoteExamFallbackFactory implements FallbackFactory<RemoteExamService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteExamFallbackFactory.class);

    @Override
    public RemoteExamService create(Throwable throwable)
    {
        log.error("Exam service call failed: {}", throwable.getMessage());
        return new RemoteExamService()
        {
            @Override
            public R<Map<String, Object>> getExamPayInfo(Long registerId, String source)
            {
                return R.fail("Get exam pay info failed: " + throwable.getMessage());
            }

            @Override
            public R<Boolean> markRegisterExamPaid(Long registerId, String source)
            {
                return R.fail("Mark exam paid failed: " + throwable.getMessage());
            }
        };
    }
}
