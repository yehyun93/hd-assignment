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
import com.hyundai.autoever.security.assignment.util.AddressExtractUtil;
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
      throw new DuplicateAccountException();
    }

    if (userRepository.existsByResidentNumber(requestDto.getResidentNumber())) {
      throw new DuplicateResidentNumberException();
    }
  }

  public UserLoginResponseDto login(UserLoginRequestDto requestDto) {
    User user = userRepository.findByUserId(requestDto.getUserId())
        .orElseThrow(() -> new UserNotFoundException());

    if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
      throw new InvalidPasswordException();
    }

    String token = jwtUtil.generateToken(user.getUserId());
    return UserLoginResponseDto.builder()
        .token(token)
        .userId(user.getUserId())
        .name(user.getName())
        .build();
  }

  public UserDetailResponseDto getMyInfo(String userId) {
    User user = userRepository.findByUserId(userId).orElseThrow(UserNotFoundException::new);

    return UserDetailResponseDto.builder()
        .id(user.getId())
        .userId(user.getUserId())
        .name(user.getName())
        .residentNumber(user.getResidentNumber())
        .phoneNumber(user.getPhoneNumber())
        .address(AddressExtractUtil.extractAdministrativeDistrict(user.getAddress()))
        .build();
  }
}