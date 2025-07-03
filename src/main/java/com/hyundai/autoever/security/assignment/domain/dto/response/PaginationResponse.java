package com.hyundai.autoever.security.assignment.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationResponse<T> {
  private List<T> content;
  private PaginationInfo pagination;

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PaginationInfo {
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
  }

  public static <T> PaginationResponse<T> from(Page<T> page) {
    return PaginationResponse.<T>builder()
        .content(page.getContent())
        .pagination(PaginationInfo.builder()
            .currentPage(page.getNumber() + 1)
            .totalPages(page.getTotalPages())
            .totalElements(page.getTotalElements())
            .pageSize(page.getSize())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build())
        .build();
  }

}