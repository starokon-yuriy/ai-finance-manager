package com.ys.ai.aifinancemanager.application.mapper;

import com.ys.ai.aifinancemanager.application.dto.CategoryDto;
import com.ys.ai.aifinancemanager.domain.entity.Category;
import com.ys.ai.aifinancemanager.domain.entity.CategoryType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class CategoryMapperTest {

  private final CategoryMapper categoryMapper = Mappers.getMapper(CategoryMapper.class);

  @Test
  void toDto_shouldMapCategoryEntityToDtoCorrectly() {
    // Given
    Category category = Category.builder()
        .idCategory(1)
        .description("Salary")
        .type(CategoryType.INCOMES)
        .build();

    // When
    CategoryDto result = categoryMapper.toDto(category);

    // Then
    assertNotNull(result);
    assertEquals(category.getIdCategory(), result.getIdCategory());
    assertEquals(category.getDescription(), result.getDescription());
    assertEquals(category.getType(), result.getType());
  }

  @Test
  void toDto_shouldHandleNullValues() {
    // Given
    Category category = Category.builder()
        .idCategory(null)
        .description(null)
        .type(null)
        .build();

    // When
    CategoryDto result = categoryMapper.toDto(category);

    // Then
    assertNotNull(result);
    assertNull(result.getIdCategory());
    assertNull(result.getDescription());
    assertNull(result.getType());
  }

  @Test
  void toDto_shouldReturnNullWhenInputIsNull() {
    // When
    CategoryDto result = categoryMapper.toDto(null);

    // Then
    assertNull(result);
  }

  @Test
  void toEntity_shouldMapCategoryDtoToEntityCorrectly() {
    // Given
    CategoryDto dto = CategoryDto.builder()
        .idCategory(2)
        .description("Food")
        .type(CategoryType.EXPENSES)
        .build();

    // When
    Category result = categoryMapper.toEntity(dto);

    // Then
    assertNotNull(result);
    assertEquals(dto.getIdCategory(), result.getIdCategory());
    assertEquals(dto.getDescription(), result.getDescription());
    assertEquals(dto.getType(), result.getType());
  }

  @Test
  void toEntity_shouldHandleNullValues() {
    // Given
    CategoryDto dto = CategoryDto.builder()
        .idCategory(null)
        .description(null)
        .type(null)
        .build();

    // When
    Category result = categoryMapper.toEntity(dto);

    // Then
    assertNotNull(result);
    assertNull(result.getIdCategory());
    assertNull(result.getDescription());
    assertNull(result.getType());
  }

  @Test
  void toEntity_shouldReturnNullWhenInputIsNull() {
    // When
    Category result = categoryMapper.toEntity(null);

    // Then
    assertNull(result);
  }

  @Test
  void toDto_shouldMapExpensesCategoryCorrectly() {
    // Given
    Category category = Category.builder()
        .idCategory(3)
        .description("Transportation")
        .type(CategoryType.EXPENSES)
        .build();

    // When
    CategoryDto result = categoryMapper.toDto(category);

    // Then
    assertNotNull(result);
    assertEquals(3, result.getIdCategory());
    assertEquals("Transportation", result.getDescription());
    assertEquals(CategoryType.EXPENSES, result.getType());
  }

  @Test
  void toDto_shouldMapIncomesCategoryCorrectly() {
    // Given
    Category category = Category.builder()
        .idCategory(4)
        .description("Freelance")
        .type(CategoryType.INCOMES)
        .build();

    // When
    CategoryDto result = categoryMapper.toDto(category);

    // Then
    assertNotNull(result);
    assertEquals(4, result.getIdCategory());
    assertEquals("Freelance", result.getDescription());
    assertEquals(CategoryType.INCOMES, result.getType());
  }
}

