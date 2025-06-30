package com.hyundai.autoever.security.assignment.controller;

import com.hyundai.autoever.security.assignment.domain.dto.request.MessageSendRequestDto;
import com.hyundai.autoever.security.assignment.domain.dto.response.MessageSendResponseDto;
import com.hyundai.autoever.security.assignment.service.BulkMessageService;
import com.hyundai.autoever.security.assignment.service.AdminService;
import com.hyundai.autoever.security.assignment.domain.dto.request.UserUpdateRequestDto;
import com.hyundai.autoever.security.assignment.domain.dto.response.UserListResponseDto;
import com.hyundai.autoever.security.assignment.domain.dto.response.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

        private final BulkMessageService bulkMessageService;
        private final AdminService adminService;

        // 사용자 목록 조회
        @GetMapping("/users")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Page<UserListResponseDto>> getUsers(Pageable pageable) {
                Page<UserListResponseDto> users = adminService.getUsers(pageable);
                return ResponseEntity.ok(users);
        }

        // 단일 사용자 조회
        @GetMapping("/users/{userId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<UserResponseDto> getUser(@PathVariable String userId) {
                UserResponseDto user = adminService.getUser(userId);
                return ResponseEntity.ok(user);
        }

        // 사용자 정보 수정
        @PutMapping("/users/{userId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<UserResponseDto> updateUser(
                        @PathVariable String userId,
                        @RequestBody UserUpdateRequestDto requestDto) {
                UserResponseDto updatedUser = adminService.updateUser(userId, requestDto);
                return ResponseEntity.ok(updatedUser);
        }

        // 사용자 삭제
        @DeleteMapping("/users/{userId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
                adminService.deleteUser(userId);
                return ResponseEntity.noContent().build();
        }

        /**
         * 연령대별 대용량 메시지 발송 API
         * POST /admin/messages/send-by-age-group
         */
        @PostMapping("/messages/send-by-age-group")
        @PreAuthorize("hasRole('ADMIN')")
        public Mono<ResponseEntity<MessageSendResponseDto>> sendBulkMessageByAgeGroup(
                        @RequestBody MessageSendRequestDto requestDto) {

                return bulkMessageService.sendMessageByAgeGroup(requestDto)
                                .map(ResponseEntity::ok)
                                .onErrorResume(e -> {
                                        return Mono.just(ResponseEntity.status(500).build());
                                });
        }

        /**
         * 메시지 발송 진행률 조회 API
         * GET /admin/messages/progress
         */
        @GetMapping("/messages/progress")
        @PreAuthorize("hasRole('ADMIN')")
        public Mono<ResponseEntity<String>> getProgressStatus() {
                return bulkMessageService.getSimpleProgress()
                                .map(ResponseEntity::ok)
                                .onErrorReturn(ResponseEntity.status(500).build());
        }
}
