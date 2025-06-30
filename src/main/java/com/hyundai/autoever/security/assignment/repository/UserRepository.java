package com.hyundai.autoever.security.assignment.repository;

import com.hyundai.autoever.security.assignment.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  boolean existsByUserId(String userId);

  boolean existsByResidentNumber(String residentNumber);

  Optional<User> findByUserId(String userId);
}