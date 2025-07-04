package com.hyundai.autoever.security.assignment.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.hyundai.autoever.security.assignment.domain.entity.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;



@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("UserRepository 테스트")
class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .userId("testuser")
            .password("encodedPassword")
            .name("홍길동")
            .residentNumber("1234567890123")
            .phoneNumber("01012345678")
            .address("서울시 강남구")
            .build();
    }
    
    @Test
    @DisplayName("사용자 ID로 존재 여부 확인")
    void existsByUserId() {
        // Given
        userRepository.save(testUser);
        
        // When
        boolean exists = userRepository.existsByUserId("testuser");
        boolean notExists = userRepository.existsByUserId("nonexistent");
        
        // Then
        assertTrue(exists);
        assertFalse(notExists);
    }
    
    @Test
    @DisplayName("주민등록번호로 존재 여부 확인")
    void existsByResidentNumber() {
        // Given
        userRepository.save(testUser);
        
        // When
        boolean exists = userRepository.existsByResidentNumber("1234567890123");
        boolean notExists = userRepository.existsByResidentNumber("9876543210987");
        
        // Then
        assertTrue(exists);
        assertFalse(notExists);
    }
    
    @Test
    @DisplayName("사용자 ID로 조회")
    void findByUserId() {
        // Given
        userRepository.save(testUser);
        
        // When
        var found = userRepository.findByUserId("testuser");
        
        // Then
        assertTrue(found.isPresent());
        assertEquals("홍길동", found.get().getName());
    }
}
