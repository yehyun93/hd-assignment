package com.hyundai.autoever.security.assignment.service;

import com.hyundai.autoever.security.assignment.domain.dto.request.MessageSendRequestDto;
import com.hyundai.autoever.security.assignment.domain.dto.response.MessageSendResponseDto;
import com.hyundai.autoever.security.assignment.domain.entity.User;
import com.hyundai.autoever.security.assignment.repository.UserRepository;
import com.hyundai.autoever.security.assignment.util.AgeCalculator;
import com.hyundai.autoever.security.assignment.util.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional
@RequiredArgsConstructor
public class BulkMessageService {

  private final UserRepository userRepository;
  private final KakaoTalkService kakaoTalkService;
  private final SmsService smsService;
  private final RateLimiter rateLimiter;
  private final AgeCalculator ageCalculator;

  private static final int BATCH_SIZE = 100;
  private static final int CONCURRENT_LIMIT = 5;

  private final AtomicInteger kakaoTalkCount = new AtomicInteger(0);
  private final AtomicInteger smsCount = new AtomicInteger(0);

  public Mono<MessageSendResponseDto> sendMessageByAgeGroup(MessageSendRequestDto requestDto) {
    String ageGroup = requestDto.getAgeGroup();
    String customMessage = requestDto.getCustomMessage();

    AtomicInteger totalProcessed = new AtomicInteger(0);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failureCount = new AtomicInteger(0);
    kakaoTalkCount.set(0);
    smsCount.set(0);

    return Flux.range(0, Integer.MAX_VALUE)
        .concatMap(pageNum -> loadUserPage(pageNum, ageGroup))
        .takeWhile(users -> !users.isEmpty())
        .flatMapIterable(users -> users)
        .flatMap(user -> sendMessageWithFallback(user, customMessage)
            .doOnNext(success -> {
              totalProcessed.incrementAndGet();
              if (success) {
                successCount.incrementAndGet();
              } else {
                failureCount.incrementAndGet();
              }
            }),
            CONCURRENT_LIMIT)
        .then(Mono.fromCallable(() -> {
          int total = totalProcessed.get();
          int success = successCount.get();
          int failure = failureCount.get();
          int kakaoCount = kakaoTalkCount.get();
          int smsCountValue = smsCount.get();
          Double kakaoRate = total > 0 ? (kakaoCount * 100.0) / total : 0.0;
          Double smsRate = total > 0 ? (smsCountValue * 100.0) / total : 0.0;
          return MessageSendResponseDto.builder()
              .message("연령대별 메시지 발송이 완료되었습니다.")
              .totalUsers(total)
              .successCount(success)
              .failureCount(failure)
              .ageGroup(ageGroup)
              .kakaoTalkCount(kakaoCount)
              .smsCount(smsCountValue)
              .kakaoTalkRate(kakaoRate)
              .smsRate(smsRate)
              .build();
        }));
  }

  private Mono<List<User>> loadUserPage(int pageNum, String ageGroup) {
    return Mono.fromCallable(() -> {
      Pageable pageable = PageRequest.of(pageNum, BATCH_SIZE);
      Page<User> page = userRepository.findAll(pageable);
      if (page.isEmpty()) {
        return List.<User>of();
      }
      List<User> filteredUsers = page.getContent().stream()
          .filter(user -> {
            try {
              String userAgeGroup = ageCalculator.getAgeGroupFromResidentNumber(user.getResidentNumber());
              return userAgeGroup.equals(ageGroup);
            } catch (Exception e) {
              return false;
            }
          })
          .toList();
      return filteredUsers;
    })
        .subscribeOn(Schedulers.boundedElastic())
        .timeout(Duration.ofSeconds(10));
  }

  private Mono<Boolean> sendMessageWithFallback(User user, String customMessage) {
    String message = createMessage(user.getName(), customMessage);
    String phone = user.getPhoneNumber();
    if (rateLimiter.isKakaoTalkAllowed()) {
      return kakaoTalkService.sendMessage(phone, message)
          .flatMap(success -> {
            if (success) {
              kakaoTalkCount.incrementAndGet();
              return Mono.just(true);
            } else {
              return sendSmsMessage(phone, message);
            }
          })
          .onErrorResume(error -> sendSmsMessage(phone, message));
    } else {
      return sendSmsMessage(phone, message);
    }
  }

  private Mono<Boolean> sendSmsMessage(String phone, String message) {
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