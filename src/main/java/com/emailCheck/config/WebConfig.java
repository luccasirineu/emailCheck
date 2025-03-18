package com.emailCheck.config;

import com.emailCheck.interceptor.RequestLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RequestLimitInterceptor requestLimitInterceptor;

    public WebConfig(RequestLimitInterceptor requestLimitInterceptor) {
        this.requestLimitInterceptor = requestLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLimitInterceptor).addPathPatterns("/**");
    }
}