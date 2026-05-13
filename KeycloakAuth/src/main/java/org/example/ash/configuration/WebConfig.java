package org.example.ash.configuration;

import org.example.ash.interceptor.UsernameHeaderInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UsernameHeaderInterceptor usernameHeaderInterceptor;

    public WebConfig(UsernameHeaderInterceptor usernameHeaderInterceptor) {
        this.usernameHeaderInterceptor = usernameHeaderInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(usernameHeaderInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/health");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
