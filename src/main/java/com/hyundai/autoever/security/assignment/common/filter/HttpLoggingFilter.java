package com.hyundai.autoever.security.assignment.common.filter;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.slf4j.MDC;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(1)
public class HttpLoggingFilter implements Filter {
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
          throws IOException, ServletException {
      
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      String traceId = UUID.randomUUID().toString().substring(0, 8);

      MDC.put("traceId", traceId);
      log.info("[{}][{}][{}][{}]{}", 
          LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")),
          traceId,
          getClientIP(httpRequest),
          httpRequest.getMethod(), 
          httpRequest.getRequestURI()
      );

      try {
        chain.doFilter(request, response);
      } finally {
          MDC.remove("traceId");
      }
    }

    private String getClientIP(HttpServletRequest request) {
      String ip = request.getHeader("X-Forwarded-For");
      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
          ip = request.getHeader("Proxy-Client-IP");
      }
      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
          ip = request.getHeader("X-Real-IP");
      }
      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
          ip = request.getRemoteAddr();
      }
      return ip;
  }
}
