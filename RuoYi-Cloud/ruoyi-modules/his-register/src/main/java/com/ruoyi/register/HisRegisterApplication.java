package com.ruoyi.register;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;

/**
 * 挂号管理模块启动类
 *
 * @author ruoyi
 */
@EnableCustomConfig
@EnableRyFeignClients
@EnableScheduling
@SpringBootApplication
public class HisRegisterApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(HisRegisterApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  挂号管理模块启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
