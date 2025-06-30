package com.hyundai.autoever.security.assignment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SmsService {

  @Value("${sms.api.url:http://localhost:8082}")
  private String smsApiUrl;

  @Value("${sms.api.username:autoever}")
  private String username;

  @Value("${sms.api.password:5678}")
  private String password;

  private final WebClient webClient = WebClient.builder()
      .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
      .build();

  public Mono<Boolean> sendMessage(String phone, String message) {
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("message", message);
    return webClient.post()
        .uri(uriBuilder -> uriBuilder
            .scheme("http")
            .host("localhost")
            .port(8082)
            .path("/sms")
            .queryParam("phone", phone)
            .build())
        .header(HttpHeaders.AUTHORIZATION, createBasicAuth(username, password))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        .body(BodyInserters.fromFormData(formData))
        .retrieve()
        .onStatus(status -> status.is4xxClientError(), response -> response.bodyToMono(String.class)
            .then(Mono.error(new RuntimeException("SMS API 인증 실패: " + response.statusCode()))))
        .bodyToMono(Map.class)
        .timeout(Duration.ofSeconds(5))
        .map(response -> response != null && "OK".equals(response.get("result")))
        .onErrorResume(error -> Mono.just(false));
  }

  private String createBasicAuth(String username, String password) {
    String credentials = username + ":" + password;
    String encodedCredentials = Base64.getEncoder()
        .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    return "Basic " + encodedCredentials;
  }
}