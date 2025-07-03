package com.hyundai.autoever.security.assignment.controller;

import com.hyundai.autoever.security.assignment.domain.dto.request.MessageSendRequestDto;
import com.hyundai.autoever.security.assignment.domain.dto.response.MessageSendResponseDto;
import com.hyundai.autoever.security.assignment.domain.dto.response.PaginationResponse;
import com.hyundai.autoever.security.assignment.service.BulkMessageService;
import com.hyundai.autoever.security.assignment.service.AdminService;
import com.hyundai.autoever.security.assignment.domain.dto.request.UserUpdateRequestDto;
import com.hyundai.autoever.security.assignment.domain.dto.response.UserListResponseDto;
import com.hyundai.autoever.security.assignment.domain.dto.response.UserResponseDto;
import com.hyundai.autoever.security.assignment.domain.dto.response.ApiResponse;
import com.hyundai.autoever.security.assignment.enums.ApiResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        @GetMapping("/users")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<PaginationResponse<UserListResponseDto>>> getUsers(Pageable pageable) {
                PaginationResponse<UserListResponseDto> users = adminService.getUsers(pageable);
                return ResponseEntity.ok(ApiResponse.success(users));
        }

        @GetMapping("/users/{userId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<UserResponseDto>> getUser(@PathVariable String userId) {
                UserResponseDto user = adminService.getUser(userId);
                return ResponseEntity.ok(ApiResponse.success(user));
        }

        @PutMapping("/users/{userId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(@PathVariable String userId,
                        @RequestBody UserUpdateRequestDto requestDto) {
                UserResponseDto updatedUser = adminService.updateUser(userId, requestDto);
                return ResponseEntity.ok(ApiResponse.success(updatedUser));
        }

        @DeleteMapping("/users/{userId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String userId) {
                adminService.deleteUser(userId);
                return ResponseEntity.ok(ApiResponse.success());
        }

        @PostMapping("/messages/send-by-age-group")
        @PreAuthorize("hasRole('ADMIN')")
        public Mono<ResponseEntity<ApiResponse<MessageSendResponseDto>>> sendBulkMessageByAgeGroup(
                        @RequestBody MessageSendRequestDto requestDto) {
                return bulkMessageService.sendMessageByAgeGroup(requestDto)
                                .map(ResponseEntity::ok)
                                .onErrorResume(e -> Mono.just(
                                                ResponseEntity.status(500).body(ApiResponse.error(
                                                                ApiResponseCode.MESSAGE_SEND_ERROR, e.getMessage()))));
        }

        @GetMapping("/messages/progress")
        @PreAuthorize("hasRole('ADMIN')")
        public Mono<ResponseEntity<ApiResponse<String>>> getProgressStatus() {
                return bulkMessageService.getSimpleProgress()
                                .map(res -> ResponseEntity.ok(ApiResponse.success(res)))
                                .onErrorReturn(ResponseEntity.status(500)
                                                .body(ApiResponse.error(ApiResponseCode.INTERNAL_ERROR)));
        }
}
