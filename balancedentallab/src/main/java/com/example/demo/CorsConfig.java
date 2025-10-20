package com.example.demo;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

  @Bean
  public FilterRegistrationBean<CorsFilter> corsFilter() {
    // 1) 建 CORS 規則
    CorsConfiguration config = new CorsConfiguration();
    // 前端實際的來源「要精準列出」
    config.setAllowedOrigins(List.of(
        "http://127.0.0.1:5500",
        "http://127.0.0.1:8080",
        "http://localhost:5500",
        "http://localhost:3000",
        "http://localhost:8080"
    ));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setExposedHeaders(List.of("*"));
    config.setAllowCredentials(true); // 你前端沒帶 cookie，就維持 false
    config.setMaxAge(3600L);

    // 2) 套用到路徑（建議 /api/**）
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);

    // 3) 用 Filter 搶在最高優先序處理 preflight
    FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
    bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return bean;
  }
}
