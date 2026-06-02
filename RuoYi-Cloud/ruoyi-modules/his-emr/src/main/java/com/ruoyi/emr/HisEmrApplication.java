package com.ruoyi.emr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;

/**
 * 病历管理模块启动类
 *
 * @author ruoyi
 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
public class HisEmrApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(HisEmrApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  病历管理模块启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
