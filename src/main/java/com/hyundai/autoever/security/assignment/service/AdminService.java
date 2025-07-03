package com.hyundai.autoever.security.assignment.service;

import com.hyundai.autoever.security.assignment.domain.dto.request.UserUpdateRequestDto;
import com.hyundai.autoever.security.assignment.domain.dto.response.PaginationResponse;
import com.hyundai.autoever.security.assignment.domain.dto.response.UserListResponseDto;
import com.hyundai.autoever.security.assignment.domain.dto.response.UserResponseDto;
import com.hyundai.autoever.security.assignment.domain.entity.User;
import com.hyundai.autoever.security.assignment.exception.UserNotFoundException;
import com.hyundai.autoever.security.assignment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public PaginationResponse<UserListResponseDto> getUsers(Pageable pageable) {
    return PaginationResponse.from(userRepository.findAll(pageable).map(this::convertToUserListResponseDto));
  }

  public UserResponseDto getUser(String userId) {
    User user = userRepository.findByUserId(userId).orElseThrow(UserNotFoundException::new);
    return convertToUserResponseDto(user);
  }

  public UserResponseDto updateUser(String userId, UserUpdateRequestDto requestDto) {
    User user = userRepository.findByUserId(userId)
        .orElseThrow(UserNotFoundException::new);

    if (requestDto.getPassword() != null && !requestDto.getPassword().isEmpty()) {
      user = user.toBuilder()
          .password(passwordEncoder.encode(requestDto.getPassword()))
          .build();
    }

    if (requestDto.getAddress() != null && !requestDto.getAddress().isEmpty()) {
      user = user.toBuilder()
          .address(requestDto.getAddress())
          .build();
    }

    User updatedUser = userRepository.save(user);
    return convertToUserResponseDto(updatedUser);
  }

  public void deleteUser(String userId) {
    User user = userRepository.findByUserId(userId).orElseThrow(UserNotFoundException::new);
    userRepository.delete(user);
  }

  private UserListResponseDto convertToUserListResponseDto(User user) {
    return UserListResponseDto.builder()
        .id(user.getId())
        .userId(user.getUserId())
        .name(user.getName())
        .phoneNumber(user.getPhoneNumber())
        .address(user.getAddress())
        .build();
  }

  private UserResponseDto convertToUserResponseDto(User user) {
    return UserResponseDto.builder()
        .id(user.getId())
        .userId(user.getUserId())
        .name(user.getName())
        .residentNumber(user.getResidentNumber())
        .phoneNumber(user.getPhoneNumber())
        .address(user.getAddress())
        .build();
  }
}