package com.hyundai.autoever.security.assignment.controller;

import com.hyundai.autoever.security.assignment.domain.dto.request.UserLoginRequestDto;
import com.hyundai.autoever.security.assignment.domain.dto.request.UserRegisterRequestDto;
import com.hyundai.autoever.security.assignment.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

        @Autowired
        private WebApplicationContext webApplicationContext;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ObjectMapper objectMapper;

        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                                .apply(SecurityMockMvcConfigurers.springSecurity())
                                .build();
                userRepository.deleteAll();
        }

        @Test
        @DisplayName("정상적인 회원가입 요청 시 성공 응답을 반환한다")
        void registerSuccess() throws Exception {
                // given
                UserRegisterRequestDto requestDto = UserRegisterRequestDto.builder()
                                .userId("testuser")
                                .password("password123")
                                .name("홍길동")
                                .residentNumber("1234567890123")
                                .phoneNumber("01012345678")
                                .address("서울특별시 강남구")
                                .build();

                // when & then
                mockMvc.perform(post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.userId").value("testuser"))
                                .andExpect(jsonPath("$.data.name").value("홍길동"));
        }

        @Test
        @DisplayName("중복된 계정으로 회원가입 시 409 에러를 반환한다")
        void registerDuplicateAccount() throws Exception {
                // given
                UserRegisterRequestDto firstRequest = UserRegisterRequestDto.builder()
                                .userId("testuser")
                                .password("password123")
                                .name("홍길동")
                                .residentNumber("1234567890123")
                                .phoneNumber("01012345678")
                                .address("서울특별시 강남구")
                                .build();

                UserRegisterRequestDto duplicateRequest = UserRegisterRequestDto.builder()
                                .userId("testuser") // 중복된 계정
                                .password("password456")
                                .name("김철수")
                                .residentNumber("9876543210987")
                                .phoneNumber("01087654321")
                                .address("부산광역시 해운대구")
                                .build();

                // 첫 번째 회원가입
                mockMvc.perform(post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(firstRequest)))
                                .andExpect(status().isOk());

                // 중복 계정으로 회원가입 시도
                mockMvc.perform(post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(duplicateRequest)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.code").value("C409"))
                                .andExpect(jsonPath("$.message").value("이미 존재하는 계정입니다."));
        }

        @Test
        @DisplayName("중복된 주민등록번호로 회원가입 시 409 에러를 반환한다")
        void registerDuplicateResidentNumber() throws Exception {
                // given
                UserRegisterRequestDto firstRequest = UserRegisterRequestDto.builder()
                                .userId("testuser1")
                                .password("password123")
                                .name("홍길동")
                                .residentNumber("1234567890123")
                                .phoneNumber("01012345678")
                                .address("서울특별시 강남구")
                                .build();

                UserRegisterRequestDto duplicateRequest = UserRegisterRequestDto.builder()
                                .userId("testuser2")
                                .password("password456")
                                .name("김철수")
                                .residentNumber("1234567890123") // 중복된 주민등록번호
                                .phoneNumber("01087654321")
                                .address("부산광역시 해운대구")
                                .build();

                // 첫 번째 회원가입
                mockMvc.perform(post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(firstRequest)))
                                .andExpect(status().isOk());

                // 중복 주민등록번호로 회원가입 시도
                mockMvc.perform(post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(duplicateRequest)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.code").value("C409"))
                                .andExpect(jsonPath("$.message").value("이미 존재하는 주민등록번호입니다."));
        }

        @Test
        @DisplayName("잘못된 형식의 주민등록번호로 회원가입 시 400 에러를 반환한다")
        void registerInvalidResidentNumber() throws Exception {
                // given
                UserRegisterRequestDto requestDto = UserRegisterRequestDto.builder()
                                .userId("testuser")
                                .password("password123")
                                .name("홍길동")
                                .residentNumber("123456789012") // 12자리 (잘못된 형식)
                                .phoneNumber("01012345678")
                                .address("서울특별시 강남구")
                                .build();

                // when & then
                mockMvc.perform(post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.code").value("C400"));
        }

        @Test
        @DisplayName("잘못된 형식의 핸드폰번호로 회원가입 시 400 에러를 반환한다")
        void registerInvalidPhoneNumber() throws Exception {
                // given
                UserRegisterRequestDto requestDto = UserRegisterRequestDto.builder()
                                .userId("testuser")
                                .password("password123")
                                .name("홍길동")
                                .residentNumber("1234567890123")
                                .phoneNumber("0101234567") // 10자리 (잘못된 형식)
                                .address("서울특별시 강남구")
                                .build();

                // when & then
                mockMvc.perform(post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.code").value("C400"));
        }

        @Test
        @DisplayName("필수 필드가 누락된 경우 400 에러를 반환한다")
        void registerMissingRequiredFields() throws Exception {
                // given
                UserRegisterRequestDto requestDto = UserRegisterRequestDto.builder()
                                .userId("testuser")
                                .name("홍길동")
                                .residentNumber("1234567890123")
                                .phoneNumber("01012345678")
                                .address("서울특별시 강남구")
                                .build();

                // when & then
                mockMvc.perform(post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.code").value("C400"));
        }

        @Test
        @DisplayName("정상적인 로그인 요청 시 JWT 토큰을 반환한다")
        void loginSuccess() throws Exception {
                // given
                UserRegisterRequestDto registerRequest = UserRegisterRequestDto.builder()
                                .userId("testuser")
                                .password("password123")
                                .name("홍길동")
                                .residentNumber("1234567890123")
                                .phoneNumber("01012345678")
                                .address("서울특별시 강남구")
                                .build();

                UserLoginRequestDto loginRequest = UserLoginRequestDto.builder()
                                .userId("testuser")
                                .password("password123")
                                .build();

                // 회원가입
                mockMvc.perform(post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk());

                // 로그인
                mockMvc.perform(post("/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.token").exists());
        }

        @Test
        @DisplayName("JWT 토큰으로 사용자 상세정보 조회 시 성공한다")
        void getMyInfoSuccess() throws Exception {
                // given
                UserRegisterRequestDto registerRequest = UserRegisterRequestDto.builder()
                                .userId("testuser")
                                .password("password123")
                                .name("홍길동")
                                .residentNumber("1234567890123")
                                .phoneNumber("01012345678")
                                .address("서울특별시 강남구")
                                .build();

                UserLoginRequestDto loginRequest = UserLoginRequestDto.builder()
                                .userId("testuser")
                                .password("password123")
                                .build();

                // 회원가입
                mockMvc.perform(post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk());

                // 로그인하여 토큰 획득
                String response = mockMvc.perform(post("/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                String token = objectMapper.readTree(response).get("data").get("token").asText();

                // 토큰으로 사용자 정보 조회
                mockMvc.perform(get("/users/me")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.userId").value("testuser"))
                                .andExpect(jsonPath("$.data.name").value("홍길동"))
                                .andExpect(jsonPath("$.data.residentNumber").value("1234567890123"))
                                .andExpect(jsonPath("$.data.phoneNumber").value("01012345678"))
                                .andExpect(jsonPath("$.data.address").value("서울특별시"));
        }

        @Test
        @DisplayName("JWT 토큰 없이 사용자 상세정보 조회 시 401 에러를 반환한다")
        void getMyInfoWithoutToken() throws Exception {
                mockMvc.perform(get("/users/me"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.code").value("C401"))
                                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
        }

        @Test
        @DisplayName("잘못된 JWT 토큰으로 사용자 상세정보 조회 시 401 에러를 반환한다")
        void getMyInfoWithInvalidToken() throws Exception {
                mockMvc.perform(get("/users/me")
                                .header("Authorization", "Bearer invalid-token"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.code").value("C401"))
                                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
        }
}