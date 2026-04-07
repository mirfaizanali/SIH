package com.placement.portal.config;

import com.placement.portal.security.AuditLoggingFilter;
import com.placement.portal.security.RateLimitingFilter;
import com.placement.portal.security.jwt.JwtAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Prevents Spring Boot from auto-registering security filters as top-level
 * servlet filters. These filters are already added to the Spring Security
 * filter chain in SecurityConfig; registering them twice would cause them
 * to run before the CorsFilter, breaking CORS preflight requests.
 */
@Configuration
public class FilterRegistrationConfig {

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
            JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setEnabled(false);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitFilterRegistration(
            RateLimitingFilter filter) {
        FilterRegistrationBean<RateLimitingFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setEnabled(false);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<AuditLoggingFilter> auditFilterRegistration(
            AuditLoggingFilter filter) {
        FilterRegistrationBean<AuditLoggingFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setEnabled(false);
        return bean;
    }
}
