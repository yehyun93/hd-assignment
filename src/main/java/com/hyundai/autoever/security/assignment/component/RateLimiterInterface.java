package com.hyundai.autoever.security.assignment.component;

public interface RateLimiterInterface {
    boolean isKakaoTalkAllowed();
    boolean isSmsAllowed();
}
