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

/**
 * 排班远程服务
 */
@FeignClient(contextId = "hisRemoteScheduleService", value = ServiceNameConstants.DOCTOR_SERVICE, fallbackFactory = RemoteScheduleFallbackFactory.class)
public interface RemoteScheduleService
{
    /**
     * 预约排班
     */
    @PostMapping("/schedule/book/{scheduleId}")
    public R<Boolean> bookSchedule(@PathVariable("scheduleId") Long scheduleId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 取消排班预约
     */
    @PostMapping("/schedule/cancel/{scheduleId}")
    public R<Boolean> cancelSchedule(@PathVariable("scheduleId") Long scheduleId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 获取排班信息
     */
    @GetMapping("/schedule/info/{scheduleId}")
    public R<Object> getScheduleInfo(@PathVariable("scheduleId") Long scheduleId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
