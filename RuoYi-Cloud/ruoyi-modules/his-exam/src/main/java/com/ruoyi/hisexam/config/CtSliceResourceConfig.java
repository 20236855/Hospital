package com.ruoyi.hisexam.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CT切片静态资源映射
 * 将 /ct-slices/** URL 映射到本地磁盘目录
 * 注意：如果 ruoyi-file 模块已配置 /statics/** 映射，
 * 且 sliceDir 在 picture/ct_slices 下，则也可通过 /statics/ct_slices/** 访问
 */
@Configuration
public class CtSliceResourceConfig implements WebMvcConfigurer {

    @Value("${lung.ct.slice.dir:./upload/ct_slices}")
    private String sliceDir;

    @Value("${lung.ct.slice.url-prefix:/ct-slices}")
    private String urlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确保路径以 / 结尾
        String dir = sliceDir.replace('\\', '/');
        if (!dir.endsWith("/")) dir += "/";
        String prefix = urlPrefix.startsWith("/") ? urlPrefix : "/" + urlPrefix;

        registry.addResourceHandler(prefix + "/**")
                .addResourceLocations("file:" + dir);
    }
}
