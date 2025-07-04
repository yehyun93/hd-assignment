package com.hyundai.autoever.security.assignment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
public class KakaoTalkService {

  @Value("${kakao.api.url:http://localhost:8081}")
  private String kakaoApiUrl;

  @Value("${kakao.api.username:autoever}")
  private String username;

  @Value("${kakao.api.password:1234}")
  private String password;

  @Value("${kakao.api.test.failure-rate:10}")
  private int failureRate;

  @Value("${kakao.api.test.enabled:true}")
  private boolean testModeEnabled;

  private final Random random = new Random();
  private final WebClient webClient = WebClient.builder()
      .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
      .build();

  /**
   * 카카오톡 메시지를 발송합니다.
   * 테스트 모드에서는 설정된 실패율에 따라 인위적 실패 발생
   */
  public Mono<Boolean> sendMessage(String phone, String message) {
    // 테스트 모드에서만 인위적 실패 적용
    if (testModeEnabled && random.nextInt(100) < failureRate) {
      log.debug("카카오톡 테스트 실패 시뮬레이션 (실패율: {}%)", failureRate);
      return Mono.just(false);
    }

    Map<String, String> requestBody = Map.of(
        "phone", phone,
        "message", message);
        
    return webClient.post()
        .uri(kakaoApiUrl + "/kakaotalk-messages")
        .header(HttpHeaders.AUTHORIZATION, createBasicAuth(username, password))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(requestBody)
        .retrieve()
        .toBodilessEntity()
        .timeout(Duration.ofSeconds(5))
        .map(response -> response.getStatusCode().is2xxSuccessful())
        .onErrorResume(WebClientResponseException.class, ex -> {
          if (ex.getStatusCode().is5xxServerError()) {
            log.error("카카오톡 API 서버 오류 - 상태코드: {}", ex.getStatusCode().value());
          } else if (ex.getStatusCode().is4xxClientError()) {
            log.warn("카카오톡 API 클라이언트 오류 - 상태코드: {}", ex.getStatusCode().value());
          }
          return Mono.just(false);
        })
        .onErrorResume(Exception.class, error -> {
          log.error("카카오톡 API 연결 오류: {}", error.getMessage());
          return Mono.just(false);
        });
  }

  private String createBasicAuth(String username, String password) {
    String credentials = username + ":" + password;
    String encodedCredentials = Base64.getEncoder()
        .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    return "Basic " + encodedCredentials;
  }
}