package com.ruoyi.patient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;

/**
 * 患者管理模块启动类
 *
 * @author ruoyi
 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
public class HisPatientApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(HisPatientApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  患者管理模块启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
