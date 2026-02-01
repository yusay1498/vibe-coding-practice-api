package com.yusay.user.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * 時刻関連の設定
 */
@Configuration
public class ClockConfig {

    /**
     * システムクロックのBean
     * テスト時にモックやスタブに差し替え可能
     * 
     * @return システムデフォルトタイムゾーンのClock
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
