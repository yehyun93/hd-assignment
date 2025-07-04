package com.hyundai.autoever.security.assignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyundai.autoever.security.assignment.domain.dto.request.MessageSendRequestDto;
import com.hyundai.autoever.security.assignment.domain.dto.request.UserLoginRequestDto;
import com.hyundai.autoever.security.assignment.domain.dto.request.UserRegisterRequestDto;
import com.hyundai.autoever.security.assignment.domain.dto.request.UserUpdateRequestDto;
import com.hyundai.autoever.security.assignment.enums.AgeGroup;
import com.hyundai.autoever.security.assignment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("통합 테스트")
public class IntegrationTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }
  
  @Test
  @DisplayName("사용자 전체 flow - 회원가입 → 로그인 → 내 정보 조회")
  void userTotalFlow() throws Exception {
      // 1. 회원가입
      String uniqueUserId = "user_" + UUID.randomUUID().toString().substring(0, 8);
      UserRegisterRequestDto registerRequest = UserRegisterRequestDto.builder()
              .userId(uniqueUserId)
              .password("password123!")
              .name("홍길동")
              .residentNumber("9001011234567")
              .phoneNumber("01012345678")
              .address("서울특별시 강남구 테헤란로 123")
              .build();

      mockMvc.perform(post("/users/register")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(registerRequest)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.success").value(true))
              .andExpect(jsonPath("$.data.userId").value(uniqueUserId))
              .andReturn()
              .getResponse()
              .getContentAsString();

      // 2. 로그인
      UserLoginRequestDto loginRequest = UserLoginRequestDto.builder()
              .userId(uniqueUserId)
              .password("password123!")
              .build();

      String loginResponse = mockMvc.perform(post("/users/login")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(loginRequest)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.success").value(true))
              .andExpect(jsonPath("$.data.token").exists())
              .andReturn()
              .getResponse()
              .getContentAsString();

      String token = objectMapper.readTree(loginResponse).get("data").get("token").asText();

      // 3. 내 정보 조회 (JWT 토큰 사용)
      mockMvc.perform(get("/users/me")
                      .header("Authorization", "Bearer " + token))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.success").value(true))
              .andExpect(jsonPath("$.data.userId").value(uniqueUserId))
              .andExpect(jsonPath("$.data.name").value("홍길동"))
              .andExpect(jsonPath("$.data.address").value("서울특별시")); // 주소는 광역시/도만 반환

      // 4. 잘못된 토큰으로 접근 시도
      mockMvc.perform(get("/users/me")
                      .header("Authorization", "Bearer invalid-token"))
              .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("관리자 전체 플로우 - 회원 관리 CRUD")
  void adminTotalFlow() throws Exception {
      // 1. 사용자 3명 생성
      for (int i = 1; i <= 3; i++) {
          UserRegisterRequestDto user = UserRegisterRequestDto.builder()
                  .userId("testuser" + i)
                  .password("password" + i)
                  .name("테스트유저" + i)
                  .residentNumber("900101123456" + i)
                  .phoneNumber("0101234567" + i)
                  .address("서울시 강남구 " + i + "번지")
                  .build();

          mockMvc.perform(post("/users/register")
                          .contentType(MediaType.APPLICATION_JSON)
                          .content(objectMapper.writeValueAsString(user)))
                  .andExpect(status().isOk());
      }

      // 2. 관리자로 전체 사용자 조회 (페이징)
      mockMvc.perform(get("/admin/users")
                      .with(httpBasic("admin", "1212"))
                      .param("page", "0")
                      .param("size", "2"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.data.content.length()").value(2))
              .andExpect(jsonPath("$.data.pagination.totalElements").value(3))
              .andExpect(jsonPath("$.data.pagination.totalPages").value(2));

      // 3. 특정 사용자 조회
      mockMvc.perform(get("/admin/users/testuser1")
                      .with(httpBasic("admin", "1212")))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.data.userId").value("testuser1"))
              .andExpect(jsonPath("$.data.name").value("테스트유저1"));

      // 4. 사용자 정보 수정
      UserUpdateRequestDto updateRequest = UserUpdateRequestDto.builder()
              .password("newPassword123!")
              .address("부산광역시 해운대구 신도시로 100")
              .build();

      mockMvc.perform(put("/admin/users/testuser1")
                      .with(httpBasic("admin", "1212"))
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(updateRequest)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.data.address").value("부산광역시 해운대구 신도시로 100"));

      // 5. 사용자 삭제
      mockMvc.perform(delete("/admin/users/testuser2")
                      .with(httpBasic("admin", "1212")))
              .andExpect(status().isOk());

      // 6. 삭제된 사용자 조회 시 404
      mockMvc.perform(get("/admin/users/testuser2")
                      .with(httpBasic("admin", "1212")))
              .andExpect(status().isNotFound());

      // 7. 잘못된 관리자 인증으로 접근 시도
      mockMvc.perform(get("/admin/users")
                      .with(httpBasic("admin", "wrongpassword")))
              .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("대량 메시지 발송 플로우")
    void bulkMessageSendFlow() throws Exception {
      // 1. 다양한 연령대의 사용자 생성
      createUserWithAge("user_20s", "0001013234567"); // 20대
      createUserWithAge("user_30s", "9501011234568"); // 30대
      createUserWithAge("user_40s", "8501012234569"); // 40대

      // 2. 20대 대상 메시지 발송
      MessageSendRequestDto messageRequest = MessageSendRequestDto.builder()
              .ageGroup(AgeGroup.TWENTIES)
              .customMessage("20대 고객 대상 메세지 발송")
              .build();

      MvcResult mvcResult = mockMvc.perform(post("/admin/messages/send")
              .with(httpBasic("admin", "1212"))
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(messageRequest)))
              .andExpect(request().asyncStarted())
              .andReturn();

      mockMvc.perform(asyncDispatch(mvcResult))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.success").value(true))
              .andExpect(jsonPath("$.data.ageGroup").value("TWENTIES"));
    }

  private void createUserWithAge(String userId, String residentNumber) throws Exception {
    UserRegisterRequestDto user = UserRegisterRequestDto.builder()
            .userId(userId)
            .password("password123")
            .name("테스트유저")
            .residentNumber(residentNumber)
            .phoneNumber("01012345678")
            .address("서울시 강남구")
            .build();

    mockMvc.perform(post("/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isOk());
  }
}
