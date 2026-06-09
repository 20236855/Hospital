package com.ruoyi.his.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.his.api.factory.RemoteScheduleFallbackFactory;

@FeignClient(contextId = "hisRemoteScheduleService", value = ServiceNameConstants.DOCTOR_SERVICE, fallbackFactory = RemoteScheduleFallbackFactory.class)
public interface RemoteScheduleService
{
    @PostMapping("/schedule/book/{scheduleId}")
    R<Boolean> bookSchedule(@PathVariable("scheduleId") Long scheduleId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PostMapping("/schedule/cancel/{scheduleId}")
    R<Boolean> cancelSchedule(@PathVariable("scheduleId") Long scheduleId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/schedule/info/{scheduleId}")
    R<Object> getScheduleInfo(@PathVariable("scheduleId") Long scheduleId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
