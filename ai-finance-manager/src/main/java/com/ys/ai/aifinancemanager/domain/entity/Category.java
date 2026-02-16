package com.ys.ai.aifinancemanager.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CATEGORY")
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID_CATEGORY")
  private Integer idCategory;

  @Column(name = "DESCRIPTION", nullable = false, unique = true)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "TYPE", nullable = false, length = 20)
  private CategoryType type;
}

