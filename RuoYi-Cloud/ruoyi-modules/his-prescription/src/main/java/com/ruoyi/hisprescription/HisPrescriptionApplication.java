package com.ruoyi.hisprescription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;

/**
 * 处方服务模块
 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
public class HisPrescriptionApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(HisPrescriptionApplication.class, args);

        System.out.println("(♥◠‿◠)ﾉﾞ  his-prescription 启动成功 ლ(´ڡ`ლ)ﾞ");
    }
}
