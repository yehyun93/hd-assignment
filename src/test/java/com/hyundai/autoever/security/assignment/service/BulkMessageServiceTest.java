package com.hyundai.autoever.security.assignment.service;

import com.hyundai.autoever.security.assignment.domain.dto.request.MessageSendRequestDto;
import com.hyundai.autoever.security.assignment.domain.dto.response.MessageSendResponseDto;
import com.hyundai.autoever.security.assignment.domain.entity.User;
import com.hyundai.autoever.security.assignment.repository.UserRepository;
import com.hyundai.autoever.security.assignment.util.AgeCalculator;
import com.hyundai.autoever.security.assignment.util.RateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

class BulkMessageServiceTest {
  @Mock
  private UserRepository userRepository;
  @Mock
  private KakaoTalkService kakaoTalkService;
  @Mock
  private SmsService smsService;
  @Mock
  private RateLimiter rateLimiter;
  @Mock
  private AgeCalculator ageCalculator;

  @InjectMocks
  private BulkMessageService bulkMessageService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    bulkMessageService = new BulkMessageService(userRepository, kakaoTalkService, smsService, rateLimiter,
        ageCalculator);
  }

  @Test
  @DisplayName("카카오톡 성공 케이스")
  void sendMessageByAgeGroup_kakaoSuccess() {
    // given
    User user = User.builder()
        .userId("user1")
        .name("홍길동")
        .phoneNumber("01012345678")
        .residentNumber("900101-1234567")
        .build();
    List<User> users = List.of(user);
    Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 100), 1);
    given(userRepository.findAll(any(PageRequest.class)))
        .willReturn(userPage)
        .willReturn(new PageImpl<>(List.of(), PageRequest.of(1, 100), 0));
    given(ageCalculator.getAgeGroupFromResidentNumber(anyString())).willReturn("20대");
    given(rateLimiter.isKakaoTalkAllowed()).willReturn(true);
    given(kakaoTalkService.sendMessage(anyString(), anyString())).willReturn(Mono.just(true));

    MessageSendRequestDto requestDto = MessageSendRequestDto.builder()
        .ageGroup("20대")
        .customMessage("테스트 메시지")
        .build();

    // when
    MessageSendResponseDto result = bulkMessageService.sendMessageByAgeGroup(requestDto).block();

    // then
    assertThat(result).isNotNull();
    assertThat(result.getSuccessCount()).isEqualTo(1);
    assertThat(result.getFailureCount()).isEqualTo(0);
    assertThat(result.getKakaoTalkCount()).isEqualTo(1);
    assertThat(result.getSmsCount()).isEqualTo(0);
  }

  @Test
  @DisplayName("카카오톡 실패 후 SMS 성공 케이스")
  void sendMessageByAgeGroup_kakaoFail_smsSuccess() {
    // given
    User user = User.builder()
        .userId("user2")
        .name("이몽룡")
        .phoneNumber("01087654321")
        .residentNumber("900101-1234567")
        .build();
    List<User> users = List.of(user);
    Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 100), 1);
    given(userRepository.findAll(any(PageRequest.class)))
        .willReturn(userPage)
        .willReturn(new PageImpl<>(List.of(), PageRequest.of(1, 100), 0));
    given(ageCalculator.getAgeGroupFromResidentNumber(anyString())).willReturn("20대");
    given(rateLimiter.isKakaoTalkAllowed()).willReturn(true);
    given(kakaoTalkService.sendMessage(anyString(), anyString())).willReturn(Mono.just(false));
    given(rateLimiter.isSmsAllowed()).willReturn(true);
    given(smsService.sendMessage(anyString(), anyString())).willReturn(Mono.just(true));

    MessageSendRequestDto requestDto = MessageSendRequestDto.builder()
        .ageGroup("20대")
        .customMessage("테스트 메시지")
        .build();

    // when
    MessageSendResponseDto result = bulkMessageService.sendMessageByAgeGroup(requestDto).block();

    // then
    assertThat(result).isNotNull();
    assertThat(result.getSuccessCount()).isEqualTo(1);
    assertThat(result.getFailureCount()).isEqualTo(0);
    assertThat(result.getKakaoTalkCount()).isEqualTo(0);
    assertThat(result.getSmsCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("카카오톡, SMS 모두 실패 케이스")
  void sendMessageByAgeGroup_allFail() {
    // given
    User user = User.builder()
        .userId("user3")
        .name("성춘향")
        .phoneNumber("01011112222")
        .residentNumber("900101-1234567")
        .build();
    List<User> users = List.of(user);
    Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 100), 1);
    given(userRepository.findAll(any(PageRequest.class)))
        .willReturn(userPage)
        .willReturn(new PageImpl<>(List.of(), PageRequest.of(1, 100), 0));
    given(ageCalculator.getAgeGroupFromResidentNumber(anyString())).willReturn("20대");
    given(rateLimiter.isKakaoTalkAllowed()).willReturn(true);
    given(kakaoTalkService.sendMessage(anyString(), anyString())).willReturn(Mono.just(false));
    given(rateLimiter.isSmsAllowed()).willReturn(true);
    given(smsService.sendMessage(anyString(), anyString())).willReturn(Mono.just(false));

    MessageSendRequestDto requestDto = MessageSendRequestDto.builder()
        .ageGroup("20대")
        .customMessage("테스트 메시지")
        .build();

    // when
    MessageSendResponseDto result = bulkMessageService.sendMessageByAgeGroup(requestDto).block();

    // then
    assertThat(result).isNotNull();
    assertThat(result.getSuccessCount()).isEqualTo(0);
    assertThat(result.getFailureCount()).isEqualTo(1);
    assertThat(result.getKakaoTalkCount()).isEqualTo(0);
    assertThat(result.getSmsCount()).isEqualTo(0);
  }

  @Test
  @DisplayName("카카오톡 RateLimit, SMS 성공 케이스")
  void sendMessageByAgeGroup_kakaoRateLimit_smsSuccess() {
    // given
    User user = User.builder()
        .userId("user4")
        .name("변학도")
        .phoneNumber("01033334444")
        .residentNumber("900101-1234567")
        .build();
    List<User> users = List.of(user);
    Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 100), 1);
    given(userRepository.findAll(any(PageRequest.class)))
        .willReturn(userPage)
        .willReturn(new PageImpl<>(List.of(), PageRequest.of(1, 100), 0));
    given(ageCalculator.getAgeGroupFromResidentNumber(anyString())).willReturn("20대");
    given(rateLimiter.isKakaoTalkAllowed()).willReturn(false);
    given(rateLimiter.isSmsAllowed()).willReturn(true);
    given(smsService.sendMessage(anyString(), anyString())).willReturn(Mono.just(true));

    MessageSendRequestDto requestDto = MessageSendRequestDto.builder()
        .ageGroup("20대")
        .customMessage("테스트 메시지")
        .build();

    // when
    MessageSendResponseDto result = bulkMessageService.sendMessageByAgeGroup(requestDto).block();

    // then
    assertThat(result).isNotNull();
    assertThat(result.getSuccessCount()).isEqualTo(1);
    assertThat(result.getFailureCount()).isEqualTo(0);
    assertThat(result.getKakaoTalkCount()).isEqualTo(0);
    assertThat(result.getSmsCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("SMS RateLimit으로 모두 실패 케이스")
  void sendMessageByAgeGroup_smsRateLimit_allFail() {
    // given
    User user = User.builder()
        .userId("user5")
        .name("방자")
        .phoneNumber("01055556666")
        .residentNumber("900101-1234567")
        .build();
    List<User> users = List.of(user);
    Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 100), 1);
    given(userRepository.findAll(any(PageRequest.class)))
        .willReturn(userPage)
        .willReturn(new PageImpl<>(List.of(), PageRequest.of(1, 100), 0));
    given(ageCalculator.getAgeGroupFromResidentNumber(anyString())).willReturn("20대");
    given(rateLimiter.isKakaoTalkAllowed()).willReturn(false);
    given(rateLimiter.isSmsAllowed()).willReturn(false);

    MessageSendRequestDto requestDto = MessageSendRequestDto.builder()
        .ageGroup("20대")
        .customMessage("테스트 메시지")
        .build();

    // when
    MessageSendResponseDto result = bulkMessageService.sendMessageByAgeGroup(requestDto).block();

    // then
    assertThat(result).isNotNull();
    assertThat(result.getSuccessCount()).isEqualTo(0);
    assertThat(result.getFailureCount()).isEqualTo(1);
    assertThat(result.getKakaoTalkCount()).isEqualTo(0);
    assertThat(result.getSmsCount()).isEqualTo(0);
  }
}