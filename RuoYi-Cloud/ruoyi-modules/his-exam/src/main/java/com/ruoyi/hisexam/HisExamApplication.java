package com.ruoyi.hisexam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;

/**
 * 检查检验模块
 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
public class HisExamApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(HisExamApplication.class, args);

        System.out.println("(♥◠‿◠)ﾉﾞ  his-exam 启动成功 ლ(´ڡ`ლ)ﾞ");
    }
}
