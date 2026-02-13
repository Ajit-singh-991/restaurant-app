package com.restaurant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.menu-dir:uploads/menu}")
    private String menuUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path menuPath = Paths.get(menuUploadDir).toAbsolutePath().normalize();
        String location = "file:" + menuPath + "/";
        registry.addResourceHandler("/images/menu/**")
                .addResourceLocations(location);
    }
}
