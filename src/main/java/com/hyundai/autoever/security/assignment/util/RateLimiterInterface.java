package com.hyundai.autoever.security.assignment.util;

public interface RateLimiterInterface {
    boolean isKakaoTalkAllowed();
    boolean isSmsAllowed();
}
