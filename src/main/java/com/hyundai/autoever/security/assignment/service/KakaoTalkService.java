package com.hyundai.autoever.security.assignment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Random;

@Service
public class KakaoTalkService {

  @Value("${kakao.api.url:http://localhost:8081}")
  private String kakaoApiUrl;

  @Value("${kakao.api.username:autoever}")
  private String username;

  @Value("${kakao.api.password:1234}")
  private String password;

  private final Random random = new Random();
  private final WebClient webClient = WebClient.builder()
      .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
      .build();

  /**
   * 카카오톡 메시지를 발송합니다.
   * POST http://kakao-mock:8081/kakaotalk-messages
   */
  public Mono<Boolean> sendMessage(String phone, String message) {
    if (random.nextInt(10) == 0) {
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
        .onErrorResume(error -> Mono.just(false));
  }

  /**
   * Basic Auth 헤더를 생성합니다.
   */
  private String createBasicAuth(String username, String password) {
    String credentials = username + ":" + password;
    String encodedCredentials = Base64.getEncoder()
        .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    return "Basic " + encodedCredentials;
  }
}