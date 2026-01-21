package com.kob.botrunningsystem.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable) // 必须禁用，否则 POST 请求必报 403
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. 允许所有的转发（微服务内部跳转常见）
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()

                        // 2. 针对匹配系统的接口进行 IP 限制
                        // 注意：由于你的 Controller 路径带了末尾斜杠，这里也要对应上，或者用 /**
                        .requestMatchers("/bot/add/").access(
                                new WebExpressionAuthorizationManager("hasIpAddress('127.0.0.1') or hasIpAddress('::1')")
                        )

                        // 3. 其他所有请求（包括你直接访问 localhost:8081）都会被拒绝
                        // 这符合微服务安全原则：只暴露必要的接口
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}