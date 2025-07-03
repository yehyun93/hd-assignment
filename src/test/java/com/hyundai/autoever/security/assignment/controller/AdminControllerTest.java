package com.hyundai.autoever.security.assignment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyundai.autoever.security.assignment.domain.dto.request.UserRegisterRequestDto;
import com.hyundai.autoever.security.assignment.domain.dto.request.UserUpdateRequestDto;
import com.hyundai.autoever.security.assignment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminControllerTest {

        @Autowired
        private WebApplicationContext webApplicationContext;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ObjectMapper objectMapper;

        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders
                                .webAppContextSetup(webApplicationContext)
                                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
                                                .springSecurity())
                                .build();
                userRepository.deleteAll();
        }

        @Test
        @WithMockUser(username = "admin", roles = { "ADMIN" })
        @DisplayName("관리자 - 사용자 목록 조회 성공 (간소화된 Pagination)")
        void getUsersSuccess() throws Exception {
                // given
                UserRegisterRequestDto user1 = UserRegisterRequestDto.builder()
                                .userId("user1")
                                .password("password1")
                                .name("홍길동")
                                .residentNumber("1234567890123")
                                .phoneNumber("01012345678")
                                .address("서울특별시 강남구")
                                .build();

                UserRegisterRequestDto user2 = UserRegisterRequestDto.builder()
                                .userId("user2")
                                .password("password2")
                                .name("김철수")
                                .residentNumber("9876543210987")
                                .phoneNumber("01087654321")
                                .address("부산광역시 해운대구")
                                .build();

                // 회원가입
                mockMvc.perform(post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user1)))
                                .andExpect(status().isOk());

                mockMvc.perform(post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user2)))
                                .andExpect(status().isOk());

                // when & then
                mockMvc.perform(get("/admin/users"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.content").isArray())
                                .andExpect(jsonPath("$.data.content.length()").value(2))
                                .andExpect(jsonPath("$.data.pagination.currentPage").value(1))
                                .andExpect(jsonPath("$.data.pagination.totalPages").value(1))
                                .andExpect(jsonPath("$.data.pagination.totalElements").value(2))
                                .andExpect(jsonPath("$.data.pagination.pageSize").value(20))
                                .andExpect(jsonPath("$.data.pagination.hasNext").value(false))
                                .andExpect(jsonPath("$.data.pagination.hasPrevious").value(false));
        }

        @Test
        @WithMockUser(username = "admin", roles = { "ADMIN" })
        @DisplayName("관리자 - 단일 사용자 조회 성공")
        void getUserSuccess() throws Exception {
                // given
                UserRegisterRequestDto user = UserRegisterRequestDto.builder()
                                .userId("user1")
                                .password("password1")
                                .name("홍길동")
                                .residentNumber("1234567890123")
                                .phoneNumber("01012345678")
                                .address("서울특별시 강남구")
                                .build();
                mockMvc.perform(post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user)))
                                .andExpect(status().isOk());
                // when & then
                mockMvc.perform(get("/admin/users/user1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.userId").value("user1"))
                                .andExpect(jsonPath("$.data.name").value("홍길동"));
        }

        @Test
        @WithMockUser(username = "admin", roles = { "ADMIN" })
        @DisplayName("관리자 - 사용자 정보 수정 성공")
        void updateUserSuccess() throws Exception {
                // given
                UserRegisterRequestDto user = UserRegisterRequestDto.builder()
                                .userId("user1")
                                .password("password1")
                                .name("홍길동")
                                .residentNumber("1234567890123")
                                .phoneNumber("01012345678")
                                .address("서울특별시 강남구")
                                .build();
                mockMvc.perform(post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user)))
                                .andExpect(status().isOk());
                UserUpdateRequestDto updateDto = UserUpdateRequestDto.builder()
                                .password("newpassword")
                                .address("경기도 수원시")
                                .build();
                // when & then
                mockMvc.perform(put("/admin/users/user1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.address").value("경기도 수원시"));
        }

        @Test
        @WithMockUser(username = "admin", roles = { "ADMIN" })
        @DisplayName("관리자 - 사용자 삭제 성공")
        void deleteUserSuccess() throws Exception {
                // given
                UserRegisterRequestDto user = UserRegisterRequestDto.builder()
                                .userId("user1")
                                .password("password1")
                                .name("홍길동")
                                .residentNumber("1234567890123")
                                .phoneNumber("01012345678")
                                .address("서울특별시 강남구")
                                .build();
                mockMvc.perform(post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user)))
                                .andExpect(status().isOk());
                // when & then
                mockMvc.perform(delete("/admin/users/user1"))
                                .andExpect(status().isOk());
                // 삭제 후 조회 시 404
                mockMvc.perform(get("/admin/users/user1"))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("인증 없이 관리자 API 접근 시 401 에러 반환")
        void getUsersUnauthorized() throws Exception {
                mockMvc.perform(get("/admin/users"))
                                .andDo(print())
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "admin", roles = { "ADMIN" })
        @DisplayName("존재하지 않는 사용자 조회 시 404 에러 반환")
        void getUserNotFound() throws Exception {
                mockMvc.perform(get("/admin/users/notexist"))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "admin", roles = { "ADMIN" })
        @DisplayName("존재하지 않는 사용자 수정 시 404 에러 반환")
        void updateUserNotFound() throws Exception {
                UserUpdateRequestDto updateDto = UserUpdateRequestDto.builder()
                                .password("newpassword")
                                .address("경기도 수원시")
                                .build();
                mockMvc.perform(put("/admin/users/notexist")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "admin", roles = { "ADMIN" })
        @DisplayName("존재하지 않는 사용자 삭제 시 404 에러 반환")
        void deleteUserNotFound() throws Exception {
                mockMvc.perform(delete("/admin/users/notexist"))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "admin", roles = { "ADMIN" })
        @DisplayName("잘못된 입력(빈 주소)으로 사용자 수정 시 200이지만 주소는 변경되지 않음")
        void updateUserWithInvalidInput() throws Exception {
                // given
                UserRegisterRequestDto user = UserRegisterRequestDto.builder()
                                .userId("user1")
                                .password("password1")
                                .name("홍길동")
                                .residentNumber("1234567890123")
                                .phoneNumber("01012345678")
                                .address("서울특별시 강남구")
                                .build();
                mockMvc.perform(post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user)))
                                .andExpect(status().isOk());
                UserUpdateRequestDto updateDto = UserUpdateRequestDto.builder()
                                .address("") // 빈 주소
                                .build();
                // when & then
                mockMvc.perform(put("/admin/users/user1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.address").value("서울특별시 강남구"));
        }
}