package com.hyundai.autoever.security.assignment.util;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 테스트용 Rate Limiter - 항상 허용
 */
@Component
@Primary
@Profile("test")
public class MockRateLimiter implements RateLimiterInterface {

    @Override
    public boolean isKakaoTalkAllowed() {
        return true;
    }

    @Override
    public boolean isSmsAllowed() {
        return true;
    }
}
