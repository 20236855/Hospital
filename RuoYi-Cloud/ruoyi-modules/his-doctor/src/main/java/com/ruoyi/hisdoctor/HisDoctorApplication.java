package com.ruoyi.hisdoctor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;

/**
 * 医生服务模块
 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
public class HisDoctorApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(HisDoctorApplication.class, args);

        System.out.println("(♥◠‿◠)ﾉﾞ  his-doctor 启动成功 ლ(´ڡ`ლ)ﾞ");
    }
}