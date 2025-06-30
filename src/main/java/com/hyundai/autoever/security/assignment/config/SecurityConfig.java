package com.hyundai.autoever.security.assignment.config;

import com.hyundai.autoever.security.assignment.util.JwtUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtUtil jwtUtil;
  private final CustomUserDetailsService customUserDetailsService;

  public SecurityConfig(JwtUtil jwtUtil, CustomUserDetailsService customUserDetailsService) {
    this.jwtUtil = jwtUtil;
    this.customUserDetailsService = customUserDetailsService;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean("inMemoryUserDetailsService")
  @Primary
  public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    String rawPassword = "1212";
    String encodedPassword = passwordEncoder.encode(rawPassword);
    UserDetails admin = User.builder()
        .username("admin")
        .password(encodedPassword)
        .roles("ADMIN")
        .build();
    InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager(admin);
    try {
      manager.loadUserByUsername("admin");
    } catch (Exception e) {
      // 예외 무시
    }
    return manager;
  }

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter() {
    return new JwtAuthenticationFilter(jwtUtil, customUserDetailsService);
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
      @Qualifier("inMemoryUserDetailsService") UserDetailsService userDetailsService)
      throws Exception {
    http
        .authorizeHttpRequests(auth -> {
          auth.requestMatchers("/h2-console/**").permitAll()
              .requestMatchers("/users/register", "/users/login").permitAll()
              .requestMatchers("/admin/**").hasRole("ADMIN")
              .requestMatchers("/users/me").authenticated()
              .anyRequest().permitAll();
        })
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> {
          session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        })
        .httpBasic(basic -> {
        })
        .userDetailsService(userDetailsService)
        .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
        .headers((headerConfig) -> headerConfig
            .frameOptions((frameOptionsConfig -> frameOptionsConfig.sameOrigin())));
    return http.build();
  }
}