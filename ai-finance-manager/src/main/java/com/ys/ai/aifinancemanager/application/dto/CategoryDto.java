package com.ys.ai.aifinancemanager.application.dto;

import com.ys.ai.aifinancemanager.domain.entity.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

  private Integer idCategory;

  private String description;

  private CategoryType type;
}

