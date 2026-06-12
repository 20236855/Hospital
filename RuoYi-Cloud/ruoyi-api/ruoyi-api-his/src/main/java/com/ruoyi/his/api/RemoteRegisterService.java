package com.ruoyi.his.api;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.his.api.factory.RemoteRegisterFallbackFactory;

/**
 * 挂号远程服务
 */
@FeignClient(contextId = "hisRemoteRegisterService", value = ServiceNameConstants.REGISTER_SERVICE, fallbackFactory = RemoteRegisterFallbackFactory.class)
public interface RemoteRegisterService
{
    /**
     * 获取挂号支付信息
     */
    @GetMapping("/register/inner/pay-info/{registerId}")
    public R<Map<String, Object>> getPayInfo(@PathVariable("registerId") Long registerId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 标记挂号单已支付
     */
    @PutMapping("/register/inner/pay/{registerId}")
    public R<Boolean> markPaid(@PathVariable("registerId") Long registerId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
