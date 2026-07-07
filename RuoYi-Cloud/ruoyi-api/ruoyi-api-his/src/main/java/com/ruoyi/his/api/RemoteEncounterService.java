package com.ruoyi.his.api;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.his.api.factory.RemoteEncounterFallbackFactory;

/**
 * Encounter remote service.
 */
@FeignClient(contextId = "hisRemoteEncounterService", value = ServiceNameConstants.EMR_SERVICE, fallbackFactory = RemoteEncounterFallbackFactory.class)
public interface RemoteEncounterService
{
    /**
     * Create or update an encounter after check-in.
     */
    @PostMapping("/encounter/inner/check-in")
    public R<Boolean> syncFromCheckIn(@RequestBody Map<String, Object> data, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
