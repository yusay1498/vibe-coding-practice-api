package com.yusay.user.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * セキュリティ設定
 * 
 * セキュリティ対策:
 * - パスワードはBCryptでハッシュ化
 * - 全件削除エンドポイントにはADMINロール必須
 * - 破壊的操作には確認ヘッダー必須
 * 
 * 開発・検証環境用設定:
 * - CSRF保護を無効化（本番環境では必ず有効化すること）
 * - Basic認証を使用（本番環境ではOAuth2等の利用を推奨）
 * 
 * 本番環境での推奨事項:
 * 1. CSRF保護を有効化する
 * 2. OAuth2やJWTベースの認証に移行する
 * 3. HTTPSを必須とする
 * 4. レート制限を実装する
 * 5. 監査ログを有効化する
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // 全件削除エンドポイントは管理者ロール必須
                .requestMatchers(HttpMethod.DELETE, "/users").hasRole("ADMIN")
                .anyRequest().permitAll()
            )
            // 未認証アクセス時に401を返すためBasic認証を有効化
            .httpBasic(httpBasic -> {})
            // 開発・検証用にCSRF保護を無効化
            // 本番環境では必ず有効化すること
            .csrf(csrf -> csrf.disable());
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
