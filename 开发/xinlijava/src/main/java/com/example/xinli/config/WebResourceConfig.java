package com.example.xinli.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebResourceConfig implements WebMvcConfigurer {

    @Value("${web.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 核心：访问 /upload/** 映射到本地磁盘 D:/XinLi/upload/
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:///" + uploadPath + "/");

        // 保留spring默认静态文件夹
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/META-INF/resources/",
                        "classpath:/resources/",
                        "classpath:/static/",
                        "classpath:/public/");
    }
}

