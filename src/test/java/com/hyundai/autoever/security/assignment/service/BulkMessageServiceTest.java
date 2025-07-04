package com.hyundai.autoever.security.assignment.service;

import com.hyundai.autoever.security.assignment.common.dto.ApiResponse;
import com.hyundai.autoever.security.assignment.component.RateLimiter;
import com.hyundai.autoever.security.assignment.domain.dto.request.MessageSendRequestDto;
import com.hyundai.autoever.security.assignment.domain.dto.response.MessageSendResponseDto;
import com.hyundai.autoever.security.assignment.domain.entity.User;
import com.hyundai.autoever.security.assignment.repository.UserRepository;
import com.hyundai.autoever.security.assignment.util.AgeCalculator;
import com.hyundai.autoever.security.assignment.enums.AgeGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BulkMessageService 테스트")
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
        private MessageService bulkMessageService;

        private User user1, user2, user3;
        private MessageSendRequestDto requestDto;

        @BeforeEach
        void setUp() {
                // 테스트용 사용자 데이터 설정
                user1 = User.builder()
                                .id(1L)
                                .name("김철수")
                                .residentNumber("9001011234567")
                                .phoneNumber("010-1234-5678")
                                .build();

                user2 = User.builder()
                                .id(2L)
                                .name("이영희")
                                .residentNumber("9505152345678")
                                .phoneNumber("010-2345-6789")
                                .build();

                user3 = User.builder()
                                .id(3L)
                                .name("박민수")
                                .residentNumber("8803033456789")
                                .phoneNumber("010-3456-7890")
                                .build();

                requestDto = MessageSendRequestDto.builder()
                                .ageGroup(AgeGroup.TWENTIES)
                                .customMessage("안녕하세요! 특별한 혜택을 확인해보세요.")
                                .build();
        }

        @Test
        @DisplayName("성공적인 메시지 발송 - 카카오톡 우선 발송")
        void sendMessageByAgeGroup_Success_KakaoTalkFirst() {
                // Given
                List<User> users = Arrays.asList(user1, user2);
                Page<User> userPage = new PageImpl<>(users);

                when(userRepository.findAll(any(Pageable.class)))
                                .thenReturn(userPage)
                                .thenReturn(new PageImpl<>(Collections.emptyList()));
                when(ageCalculator.getAgeGroupFromResidentNumber(anyString())).thenReturn(AgeGroup.TWENTIES);
                when(rateLimiter.isKakaoTalkAllowed()).thenReturn(true);
                when(kakaoTalkService.sendMessage(anyString(), anyString())).thenReturn(Mono.just(true));

                // When
                Mono<ApiResponse<MessageSendResponseDto>> result = bulkMessageService.sendMessageByAgeGroup(requestDto);

                // Then
                StepVerifier.create(result)
                                .expectNextMatches(response -> {
                                        return response.getData().getTotalUsers() == 2 &&
                                                        response.getData().getSuccessCount() == 2 &&
                                                        response.getData().getFailureCount() == 0 &&
                                                        response.getData().getAgeGroup()
                                                                        .equals(AgeGroup.TWENTIES.getCode())
                                                        &&
                                                        response.getData().getKakaoTalkCount() == 2 &&
                                                        response.getData().getSmsCount() == 0;
                                })
                                .verifyComplete();

                verify(kakaoTalkService, times(2)).sendMessage(anyString(), anyString());
                verify(smsService, never()).sendMessage(anyString(), anyString());
        }

        @Test
        @DisplayName("카카오톡 실패 시 SMS로 폴백")
        void sendMessageByAgeGroup_KakaoTalkFailure_SmsFallback() {
                // Given
                List<User> users = Arrays.asList(user1, user2);
                Page<User> userPage = new PageImpl<>(users);

                when(userRepository.findAll(any(Pageable.class)))
                                .thenReturn(userPage)
                                .thenReturn(new PageImpl<>(Collections.emptyList()));
                when(ageCalculator.getAgeGroupFromResidentNumber(anyString())).thenReturn(AgeGroup.TWENTIES);
                when(rateLimiter.isKakaoTalkAllowed()).thenReturn(true);
                when(kakaoTalkService.sendMessage(anyString(), anyString())).thenReturn(Mono.just(false));
                when(rateLimiter.isSmsAllowed()).thenReturn(true);
                when(smsService.sendMessage(anyString(), anyString())).thenReturn(Mono.just(true));

                // When
                Mono<ApiResponse<MessageSendResponseDto>> result = bulkMessageService.sendMessageByAgeGroup(requestDto);

                // Then
                StepVerifier.create(result)
                                .expectNextMatches(response -> {
                                        return response.getData().getTotalUsers() == 2 &&
                                                        response.getData().getSuccessCount() == 2 &&
                                                        response.getData().getFailureCount() == 0 &&
                                                        response.getData().getKakaoTalkCount() == 0 &&
                                                        response.getData().getSmsCount() == 2;
                                })
                                .verifyComplete();

                verify(kakaoTalkService, times(2)).sendMessage(anyString(), anyString());
                verify(smsService, times(2)).sendMessage(anyString(), anyString());
        }

        @Test
        @DisplayName("카카오톡 속도 제한 시 SMS로 발송")
        void sendMessageByAgeGroup_KakaoTalkRateLimited_SmsOnly() {
                // Given
                List<User> users = Arrays.asList(user1, user2);
                Page<User> userPage = new PageImpl<>(users);

                when(userRepository.findAll(any(Pageable.class)))
                                .thenReturn(userPage)
                                .thenReturn(new PageImpl<>(Collections.emptyList()));
                when(ageCalculator.getAgeGroupFromResidentNumber(anyString())).thenReturn(AgeGroup.TWENTIES);
                when(rateLimiter.isKakaoTalkAllowed()).thenReturn(false);
                when(rateLimiter.isSmsAllowed()).thenReturn(true);
                when(smsService.sendMessage(anyString(), anyString())).thenReturn(Mono.just(true));

                // When
                Mono<ApiResponse<MessageSendResponseDto>> result = bulkMessageService.sendMessageByAgeGroup(requestDto);

                // Then
                StepVerifier.create(result)
                                .expectNextMatches(response -> {
                                        return response.getData().getTotalUsers() == 2 &&
                                                        response.getData().getSuccessCount() == 2 &&
                                                        response.getData().getKakaoTalkCount() == 0 &&
                                                        response.getData().getSmsCount() == 2;
                                })
                                .verifyComplete();

                verify(kakaoTalkService, never()).sendMessage(anyString(), anyString());
                verify(smsService, times(2)).sendMessage(anyString(), anyString());
        }

        @Test
        @DisplayName("모든 메시지 발송 실패")
        void sendMessageByAgeGroup_AllMessagesFailed() {
                // Given
                List<User> users = Arrays.asList(user1, user2);
                Page<User> userPage = new PageImpl<>(users);

                when(userRepository.findAll(any(Pageable.class)))
                                .thenReturn(userPage)
                                .thenReturn(new PageImpl<>(Collections.emptyList()));
                when(ageCalculator.getAgeGroupFromResidentNumber(anyString())).thenReturn(AgeGroup.TWENTIES);
                when(rateLimiter.isKakaoTalkAllowed()).thenReturn(true);
                when(kakaoTalkService.sendMessage(anyString(), anyString())).thenReturn(Mono.just(false));
                when(rateLimiter.isSmsAllowed()).thenReturn(true);
                when(smsService.sendMessage(anyString(), anyString())).thenReturn(Mono.just(false));

                // When
                Mono<ApiResponse<MessageSendResponseDto>> result = bulkMessageService.sendMessageByAgeGroup(requestDto);

                // Then
                StepVerifier.create(result)
                                .expectNextMatches(response -> {
                                        return response.getData().getTotalUsers() == 2 &&
                                                        response.getData().getSuccessCount() == 0 &&
                                                        response.getData().getFailureCount() == 2 &&
                                                        response.getData().getKakaoTalkCount() == 0 &&
                                                        response.getData().getSmsCount() == 0;
                                })
                                .verifyComplete();
        }

        @Test
        @DisplayName("카카오톡 예외 발생 시 SMS로 폴백")
        void sendMessageByAgeGroup_KakaoTalkException_SmsFallback() {
                // Given
                List<User> users = Arrays.asList(user1);
                Page<User> userPage = new PageImpl<>(users);

                when(userRepository.findAll(any(Pageable.class)))
                                .thenReturn(userPage)
                                .thenReturn(new PageImpl<>(Collections.emptyList()));
                when(ageCalculator.getAgeGroupFromResidentNumber(anyString())).thenReturn(AgeGroup.TWENTIES);
                when(rateLimiter.isKakaoTalkAllowed()).thenReturn(true);
                when(kakaoTalkService.sendMessage(anyString(), anyString()))
                                .thenReturn(Mono.error(new RuntimeException("카카오톡 API 오류")));
                when(rateLimiter.isSmsAllowed()).thenReturn(true);
                when(smsService.sendMessage(anyString(), anyString())).thenReturn(Mono.just(true));

                // When
                Mono<ApiResponse<MessageSendResponseDto>> result = bulkMessageService.sendMessageByAgeGroup(requestDto);

                // Then
                StepVerifier.create(result)
                                .expectNextMatches(response -> {
                                        return response.getData().getTotalUsers() == 1 &&
                                                        response.getData().getSuccessCount() == 1 &&
                                                        response.getData().getFailureCount() == 0 &&
                                                        response.getData().getKakaoTalkCount() == 0 &&
                                                        response.getData().getSmsCount() == 1;
                                })
                                .verifyComplete();
        }

        @Test
        @DisplayName("사용자가 없는 경우")
        void sendMessageByAgeGroup_NoUsers() {
                // Given
                Page<User> emptyPage = new PageImpl<>(Collections.emptyList());

                when(userRepository.findAll(any(Pageable.class)))
                                .thenReturn(emptyPage)
                                .thenReturn(new PageImpl<>(Collections.emptyList()));

                // When
                Mono<ApiResponse<MessageSendResponseDto>> result = bulkMessageService.sendMessageByAgeGroup(requestDto);

                // Then
                StepVerifier.create(result)
                                .expectNextMatches(response -> {
                                        return response.getData().getTotalUsers() == 0 &&
                                                        response.getData().getSuccessCount() == 0 &&
                                                        response.getData().getFailureCount() == 0 &&
                                                        response.getData().getAgeGroup()
                                                                        .equals(AgeGroup.TWENTIES.getCode());
                                })
                                .verifyComplete();

                verify(kakaoTalkService, never()).sendMessage(anyString(), anyString());
                verify(smsService, never()).sendMessage(anyString(), anyString());
        }

        @Test
        @DisplayName("연령대 필터링 테스트")
        void sendMessageByAgeGroup_AgeGroupFiltering() {
                // Given
                List<User> allUsers = Arrays.asList(user1, user2, user3);
                Page<User> userPage = new PageImpl<>(allUsers);

                when(userRepository.findAll(any(Pageable.class)))
                                .thenReturn(userPage)
                                .thenReturn(new PageImpl<>(Collections.emptyList()));
                when(ageCalculator.getAgeGroupFromResidentNumber("9001011234567")).thenReturn(AgeGroup.THIRTIES);
                when(ageCalculator.getAgeGroupFromResidentNumber("9505152345678")).thenReturn(AgeGroup.TWENTIES);
                when(ageCalculator.getAgeGroupFromResidentNumber("8803033456789")).thenReturn(AgeGroup.THIRTIES);
                when(rateLimiter.isKakaoTalkAllowed()).thenReturn(true);
                when(kakaoTalkService.sendMessage(anyString(), anyString())).thenReturn(Mono.just(true));

                // When
                Mono<ApiResponse<MessageSendResponseDto>> result = bulkMessageService.sendMessageByAgeGroup(requestDto);

                // Then
                StepVerifier.create(result)
                                .expectNextMatches(response -> {
                                        return response.getData().getTotalUsers() == 1 &&
                                                        response.getData().getSuccessCount() == 1 &&
                                                        response.getData().getAgeGroup()
                                                                        .equals(AgeGroup.TWENTIES.getCode());
                                })
                                .verifyComplete();

                // 20대 사용자(이영희)에게만 메시지가 발송되었는지 확인
                verify(kakaoTalkService, times(1)).sendMessage(eq("010-2345-6789"), contains("이영희"));
        }

        @Test
        @DisplayName("커스텀 메시지가 포함된 메시지 생성")
        void sendMessageByAgeGroup_CustomMessageIncluded() {
                // Given
                List<User> users = Arrays.asList(user1);
                Page<User> userPage = new PageImpl<>(users);

                when(userRepository.findAll(any(Pageable.class)))
                                .thenReturn(userPage)
                                .thenReturn(new PageImpl<>(Collections.emptyList()));
                when(ageCalculator.getAgeGroupFromResidentNumber(anyString())).thenReturn(AgeGroup.TWENTIES);
                when(rateLimiter.isKakaoTalkAllowed()).thenReturn(true);
                when(kakaoTalkService.sendMessage(anyString(), anyString())).thenReturn(Mono.just(true));

                // When
                Mono<ApiResponse<MessageSendResponseDto>> result = bulkMessageService.sendMessageByAgeGroup(requestDto);

                // Then
                StepVerifier.create(result)
                                .expectNextMatches(response -> response.getData().getSuccessCount() == 1)
                                .verifyComplete();

                // 커스텀 메시지가 포함된 메시지가 발송되었는지 확인
                verify(kakaoTalkService, times(1)).sendMessage(
                                eq("010-1234-5678"),
                                contains("안녕하세요! 특별한 혜택을 확인해보세요."));
        }

        @Test
        @DisplayName("빈 커스텀 메시지 처리")
        void sendMessageByAgeGroup_EmptyCustomMessage() {
                // Given
                MessageSendRequestDto emptyMessageRequest = MessageSendRequestDto.builder()
                                .ageGroup(AgeGroup.TWENTIES)
                                .customMessage("")
                                .build();

                List<User> users = Arrays.asList(user1);
                Page<User> userPage = new PageImpl<>(users);

                when(userRepository.findAll(any(Pageable.class)))
                                .thenReturn(userPage)
                                .thenReturn(new PageImpl<>(Collections.emptyList()));
                when(ageCalculator.getAgeGroupFromResidentNumber(anyString())).thenReturn(AgeGroup.TWENTIES);
                when(rateLimiter.isKakaoTalkAllowed()).thenReturn(true);
                when(kakaoTalkService.sendMessage(anyString(), anyString())).thenReturn(Mono.just(true));

                // When
                Mono<ApiResponse<MessageSendResponseDto>> result = bulkMessageService
                                .sendMessageByAgeGroup(emptyMessageRequest);

                // Then
                StepVerifier.create(result)
                                .expectNextMatches(response -> response.getData().getSuccessCount() == 1)
                                .verifyComplete();

                // 기본 메시지만 포함된 메시지가 발송되었는지 확인
                verify(kakaoTalkService, times(1)).sendMessage(
                                eq("010-1234-5678"),
                                eq("김철수님, 안녕하세요. 현대 오토에버입니다."));
        }

        @Test
        @DisplayName("SMS 속도 제한 시 발송 실패")
        void sendMessageByAgeGroup_SmsRateLimited_Failure() {
                // Given
                List<User> users = Arrays.asList(user1);
                Page<User> userPage = new PageImpl<>(users);

                when(userRepository.findAll(any(Pageable.class)))
                                .thenReturn(userPage)
                                .thenReturn(new PageImpl<>(Collections.emptyList()));
                when(ageCalculator.getAgeGroupFromResidentNumber(anyString())).thenReturn(AgeGroup.TWENTIES);
                when(rateLimiter.isKakaoTalkAllowed()).thenReturn(false);
                when(rateLimiter.isSmsAllowed()).thenReturn(false);

                // When
                Mono<ApiResponse<MessageSendResponseDto>> result = bulkMessageService.sendMessageByAgeGroup(requestDto);

                // Then
                StepVerifier.create(result)
                                .expectNextMatches(response -> {
                                        return response.getData().getTotalUsers() == 1 &&
                                                        response.getData().getSuccessCount() == 0 &&
                                                        response.getData().getFailureCount() == 1;
                                })
                                .verifyComplete();

                verify(kakaoTalkService, never()).sendMessage(anyString(), anyString());
                verify(smsService, never()).sendMessage(anyString(), anyString());
        }

        @Test
        @DisplayName("진행 상황 조회 테스트")
        void getSimpleProgress_Success() {
                // When
                Mono<String> result = bulkMessageService.getSimpleProgress();

                // Then
                StepVerifier.create(result)
                                .expectNext("메시지 발송이 진행 중입니다. 로그를 확인해주세요.")
                                .verifyComplete();
        }

        @Test
        @DisplayName("여러 페이지의 사용자 처리")
        void sendMessageByAgeGroup_MultiplePages() {
                // Given
                List<User> page1Users = Arrays.asList(user1, user2);
                List<User> page2Users = Arrays.asList(user3);
                Page<User> page1 = new PageImpl<>(page1Users, PageRequest.of(0, 100), 3);
                Page<User> page2 = new PageImpl<>(page2Users, PageRequest.of(1, 100), 3);
                Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(2, 100), 3);

                when(userRepository.findAll(PageRequest.of(0, 100))).thenReturn(page1);
                when(userRepository.findAll(PageRequest.of(1, 100))).thenReturn(page2);
                when(userRepository.findAll(PageRequest.of(2, 100))).thenReturn(emptyPage);
                when(ageCalculator.getAgeGroupFromResidentNumber(anyString())).thenReturn(AgeGroup.TWENTIES);
                when(rateLimiter.isKakaoTalkAllowed()).thenReturn(true);
                when(kakaoTalkService.sendMessage(anyString(), anyString())).thenReturn(Mono.just(true));

                // When
                Mono<ApiResponse<MessageSendResponseDto>> result = bulkMessageService.sendMessageByAgeGroup(requestDto);

                // Then
                StepVerifier.create(result)
                                .expectNextMatches(response -> {
                                        return response.getData().getTotalUsers() == 3 &&
                                                        response.getData().getSuccessCount() == 3 &&
                                                        response.getData().getFailureCount() == 0;
                                })
                                .verifyComplete();

                verify(kakaoTalkService, times(3)).sendMessage(anyString(), anyString());
        }
}