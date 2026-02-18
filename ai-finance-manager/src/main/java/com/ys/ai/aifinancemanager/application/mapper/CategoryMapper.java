package com.ys.ai.aifinancemanager.application.mapper;

import com.ys.ai.aifinancemanager.application.dto.CategoryDto;
import com.ys.ai.aifinancemanager.domain.entity.Category;
import org.mapstruct.Mapper;

@Mapper
public interface CategoryMapper {

  CategoryDto toDto(Category category);

  Category toEntity(CategoryDto categoryDto);
}

