package com.yusay.user.api.presentation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * セキュリティ設定
 * 注意: 現在は開発・検証用として認証とCSRF保護を無効化しています。
 * 本番環境では必ず適切な認証・認可とCSRF保護を有効化してください。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            // 開発・検証用にCSRF保護を無効化
            // 本番環境では必ず有効化すること
            .csrf(csrf -> csrf.disable());
        
        return http.build();
    }
}
