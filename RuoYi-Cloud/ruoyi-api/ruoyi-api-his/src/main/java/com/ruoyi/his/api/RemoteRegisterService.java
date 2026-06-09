package com.ruoyi.his.api;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.his.api.factory.RemoteRegisterFallbackFactory;

@FeignClient(contextId = "hisRemoteRegisterService", value = ServiceNameConstants.REGISTER_SERVICE, fallbackFactory = RemoteRegisterFallbackFactory.class)
public interface RemoteRegisterService
{
    @GetMapping("/register/inner/{registerId}")
    public R<Map<String, Object>> getRegisterInfo(@PathVariable("registerId") Long registerId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
