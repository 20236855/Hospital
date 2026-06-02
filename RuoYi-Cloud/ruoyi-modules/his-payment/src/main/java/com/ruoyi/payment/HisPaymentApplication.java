package com.ruoyi.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;

/**
 * 支付管理模块启动类
 *
 * @author ruoyi
 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
public class HisPaymentApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(HisPaymentApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  支付管理模块启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
