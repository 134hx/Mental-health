package com.example.xinli.config;

import com.example.xinli.util.LoginInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final LoginInterceptor loginInterceptor;

    @Value("${web.upload.path}")
    private String uploadRootPath;

    public WebConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/error",
                        "/upload/**",
                        "/video/**",
                        "/vo/**",
                        "/user/checkPhone",
                        "/user/checkAccount",
                        "/user/resetPassword",
                        "/scale/list",
                        "/"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ★ 关键：两个 location 不一样！
        // 头像 URL 是 /upload/avatar/xxx → location 指 upload 根
        String uploadLoc = Paths.get(uploadRootPath).toUri().toString();

        // 视频 URL 是 /video/xxx → location 指 upload/video 子目录
        String videoLoc = Paths.get(uploadRootPath, "video").toUri().toString();

        System.out.println("====uploadLoc=[" + uploadLoc + "]");
        System.out.println("====videoLoc=["  + videoLoc  + "]");

        registry.addResourceHandler("/upload/**").addResourceLocations(uploadLoc);
        registry.addResourceHandler("/video/**").addResourceLocations(videoLoc);
        registry.addResourceHandler("/vo/**").addResourceLocations("classpath:/static/vo/");
    }
}