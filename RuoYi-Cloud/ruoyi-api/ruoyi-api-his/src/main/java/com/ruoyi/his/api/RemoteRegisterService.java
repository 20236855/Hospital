package com.ruoyi.his.api;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.his.api.factory.RemoteRegisterFallbackFactory;

/**
 * Register remote service.
 */
@FeignClient(contextId = "hisRemoteRegisterService", value = ServiceNameConstants.REGISTER_SERVICE, fallbackFactory = RemoteRegisterFallbackFactory.class)
public interface RemoteRegisterService
{
    /**
     * Get register detail for internal callers.
     */
    @GetMapping("/register/inner/{registerId}")
    public R<Map<String, Object>> getRegisterInfo(@PathVariable("registerId") Long registerId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * Get register payment info.
     */
    @GetMapping("/register/inner/pay-info/{registerId}")
    public R<Map<String, Object>> getPayInfo(@PathVariable("registerId") Long registerId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * Mark register as paid.
     */
    @PutMapping("/register/inner/pay/{registerId}")
    public R<Boolean> markPaid(@PathVariable("registerId") Long registerId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * Create an online register record for internal AI agent callers.
     */
    @PostMapping("/register/inner/agent")
    public R<Map<String, Object>> createAgentRegister(@RequestBody Map<String, Object> register,
                                                      @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
