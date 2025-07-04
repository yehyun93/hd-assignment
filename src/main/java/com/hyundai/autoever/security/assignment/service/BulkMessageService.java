package com.hyundai.autoever.security.assignment.service;

import com.hyundai.autoever.security.assignment.domain.dto.request.MessageSendRequestDto;
import com.hyundai.autoever.security.assignment.domain.dto.response.MessageSendResponseDto;
import com.hyundai.autoever.security.assignment.domain.entity.User;
import com.hyundai.autoever.security.assignment.enums.AgeGroup;
import com.hyundai.autoever.security.assignment.repository.UserRepository;
import com.hyundai.autoever.security.assignment.util.AgeCalculator;
import com.hyundai.autoever.security.assignment.util.RateLimiterInterface;
import com.hyundai.autoever.security.assignment.domain.dto.response.ApiResponse;
import com.hyundai.autoever.security.assignment.enums.ApiResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BulkMessageService {

  private final UserRepository userRepository;
  private final KakaoTalkService kakaoTalkService;
  private final SmsService smsService;
  private final RateLimiterInterface rateLimiter;
  private final AgeCalculator ageCalculator;

  private static final int BATCH_SIZE = 100;
  private static final int CONCURRENT_LIMIT = 2;

  // 글로벌 카운터 제거 - 각 요청마다 로컬 카운터 사용

  public Mono<ApiResponse<MessageSendResponseDto>> sendMessageByAgeGroup(MessageSendRequestDto requestDto) {
    AgeGroup ageGroup = requestDto.getAgeGroup();
    String customMessage = requestDto.getCustomMessage();

    log.info("연령대별 메시지 발송 시작 - 연령대: {}, 커스텀 메시지: {}", requestDto.getAgeGroup(), requestDto.getCustomMessage());

    // 각 요청마다 로컬 카운터 생성
    AtomicInteger totalProcessed = new AtomicInteger(0);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failureCount = new AtomicInteger(0);
    AtomicInteger kakaoTalkCount = new AtomicInteger(0);
    AtomicInteger smsCount = new AtomicInteger(0);

    Instant startTime = Instant.now();
    return Flux.range(0, Integer.MAX_VALUE)
        .concatMap(pageNum -> loadUserPage(pageNum, ageGroup))
        .takeWhile(users -> !users.isEmpty())
        .flatMapIterable(users -> users)
        .flatMap(user -> sendMessageWithFallback(user, customMessage, kakaoTalkCount, smsCount)
            .doOnNext(success -> {
              int processed = totalProcessed.incrementAndGet();
              if (success) {
                successCount.incrementAndGet();
              } else {
                failureCount.incrementAndGet();
              }

              if (processed % 1000 == 0) {
                log.info("진행률 - 처리: {}명, 성공: {}명", processed, successCount.get());
              }
            }),
            CONCURRENT_LIMIT)
        .then(Mono.fromCallable(() -> {
          Duration duration = Duration.between(startTime, Instant.now());

          int total = totalProcessed.get();
          int success = successCount.get();
          int failure = failureCount.get();
          int kakaoCount = kakaoTalkCount.get();
          int smsCountValue = smsCount.get();

          log.info("메시지 발송 완료 - 총 {}명, 성공 {}명, 실패 {}명, 소요시간 {}초", total, success, failure, duration.getSeconds());
          log.info("채널별 - 카카오톡: {}명, SMS: {}명", kakaoTalkCount.get(), smsCount.get());

          MessageSendResponseDto responseDto = MessageSendResponseDto.builder()
              .totalUsers(total)
              .successCount(success)
              .failureCount(failure)
              .ageGroup(ageGroup.getCode())
              .kakaoTalkCount(kakaoCount)
              .smsCount(smsCountValue)
              .build();
          return ApiResponse.success(responseDto);
        }))
        .onErrorResume(error -> {
          log.error("메시지 발송 중 오류 발생: {}", error.getMessage());
          return Mono.just(ApiResponse.error(ApiResponseCode.MESSAGE_SEND_ERROR, error.getMessage()));
        });
  }

  private Mono<List<User>> loadUserPage(int pageNum, AgeGroup ageGroup) {

    log.info("연령대별 사용자 조회 시작 - 연령대: {}", ageGroup);
    return Mono.fromCallable(() -> {
      Pageable pageable = PageRequest.of(pageNum, BATCH_SIZE);
      Page<User> page = userRepository.findAll(pageable);
      log.info("연령대별 사용자 조회 완료 - 연령대: {}, 페이지: {}, 사용자 수: {}", ageGroup, pageNum, page.getContent().size());
      if (page.isEmpty()) {
        return List.<User>of();
      }
      List<User> filteredUsers = page.getContent().stream()
          .filter(user -> {
            try {
              AgeGroup userAgeGroup = ageCalculator.getAgeGroupFromResidentNumber(user.getResidentNumber());
              return userAgeGroup == ageGroup;
            } catch (Exception e) {
              e.printStackTrace();
              log.warn("연령 계산 실패 - 사용자ID: {}", user.getUserId());
              return false;
            }
          })
          .toList();
      return filteredUsers;
    })
        .subscribeOn(Schedulers.boundedElastic())
        .timeout(Duration.ofSeconds(30));
  }

  private Mono<Boolean> sendMessageWithFallback(User user, String customMessage, AtomicInteger kakaoTalkCount, AtomicInteger smsCount) {
    String message = createMessage(user.getName(), customMessage);
    String phone = user.getPhoneNumber();
    if (rateLimiter.isKakaoTalkAllowed()) {
      return kakaoTalkService.sendMessage(phone, message)
          .flatMap(success -> {
            if (success) {
              kakaoTalkCount.incrementAndGet();
              return Mono.just(true);
            } else {
              return sendSmsMessage(phone, message, smsCount);
            }
          })
          .onErrorResume(error -> sendSmsMessage(phone, message, smsCount));
    } else {
      return sendSmsMessage(phone, message, smsCount);
    }
  }

  private Mono<Boolean> sendSmsMessage(String phone, String message, AtomicInteger smsCount) {
    if (rateLimiter.isSmsAllowed()) {
      return smsService.sendMessage(phone, message)
          .doOnNext(success -> {
            if (success) {
              smsCount.incrementAndGet();
            }
          })
          .onErrorReturn(false);
    } else {
      return Mono.just(false);
    }
  }

  private String createMessage(String userName, String customMessage) {
    StringBuilder message = new StringBuilder();
    message.append(userName).append("님, 안녕하세요. 현대 오토에버입니다.");
    if (customMessage != null && !customMessage.trim().isEmpty()) {
      message.append("\n").append(customMessage);
    }
    return message.toString();
  }

  public Mono<String> getSimpleProgress() {
    return Mono.just("메시지 발송이 진행 중입니다. 로그를 확인해주세요.");
  }
}