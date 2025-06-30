package com.hyundai.autoever.security.assignment.service;

import com.hyundai.autoever.security.assignment.domain.dto.request.UserLoginRequestDto;
import com.hyundai.autoever.security.assignment.domain.dto.request.UserRegisterRequestDto;
import com.hyundai.autoever.security.assignment.domain.dto.response.UserDetailResponseDto;
import com.hyundai.autoever.security.assignment.domain.dto.response.UserLoginResponseDto;
import com.hyundai.autoever.security.assignment.domain.dto.response.UserRegisterResponseDto;
import com.hyundai.autoever.security.assignment.domain.entity.User;
import com.hyundai.autoever.security.assignment.exception.DuplicateAccountException;
import com.hyundai.autoever.security.assignment.exception.DuplicateResidentNumberException;
import com.hyundai.autoever.security.assignment.exception.InvalidPasswordException;
import com.hyundai.autoever.security.assignment.exception.UserNotFoundException;
import com.hyundai.autoever.security.assignment.repository.UserRepository;
import com.hyundai.autoever.security.assignment.util.JwtUtil;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  public UserRegisterResponseDto register(UserRegisterRequestDto requestDto) {
    validateDuplicates(requestDto);

    User user = User.builder()
        .userId(requestDto.getUserId())
        .password(passwordEncoder.encode(requestDto.getPassword()))
        .name(requestDto.getName())
        .residentNumber(requestDto.getResidentNumber())
        .phoneNumber(requestDto.getPhoneNumber())
        .address(requestDto.getAddress())
        .build();

    User savedUser = userRepository.save(user);

    return UserRegisterResponseDto.builder()
        .userId(savedUser.getUserId())
        .name(savedUser.getName())
        .build();
  }

  public List<User> getUsers() {
    return userRepository.findAll();
  }

  private void validateDuplicates(UserRegisterRequestDto requestDto) {
    if (userRepository.existsByUserId(requestDto.getUserId())) {
      throw new DuplicateAccountException("이미 존재하는 계정입니다.");
    }

    if (userRepository.existsByResidentNumber(requestDto.getResidentNumber())) {
      throw new DuplicateResidentNumberException("이미 존재하는 주민등록번호입니다.");
    }
  }

  public UserLoginResponseDto login(UserLoginRequestDto requestDto) {
    User user = userRepository.findByUserId(requestDto.getUserId())
        .orElseThrow(() -> new UserNotFoundException("존재하지 않는 계정입니다."));

    if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
      throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
    }

    String token = jwtUtil.generateToken(user.getUserId());
    return UserLoginResponseDto.builder()
        .token(token)
        .userId(user.getUserId())
        .name(user.getName())
        .build();
  }

  public UserDetailResponseDto getMyInfo(String userId) {
    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));

    return UserDetailResponseDto.builder()
        .id(user.getId())
        .userId(user.getUserId())
        .name(user.getName())
        .residentNumber(user.getResidentNumber())
        .phoneNumber(user.getPhoneNumber())
        .address(extractAdministrativeDistrict(user.getAddress()))
        .build();
  }

  /**
   * 주소에서 최대 행정구역 단위만 추출합니다.
   * 공백으로 split하여 첫 번째 요소를 반환합니다.
   * 예: "서울특별시 강남구 테헤란로 123" -> "서울특별시"
   */
  private String extractAdministrativeDistrict(String fullAddress) {
    if (fullAddress == null || fullAddress.trim().isEmpty()) {
      return "";
    }

    String[] addressParts = fullAddress.trim().split("\\s+");
    return addressParts.length > 0 ? addressParts[0] : fullAddress.trim();
  }
}