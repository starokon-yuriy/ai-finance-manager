package com.ys.ai.aifinancemanager.domain.repository;

import com.ys.ai.aifinancemanager.domain.entity.Category;
import com.ys.ai.aifinancemanager.domain.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

  List<Category> findByType(CategoryType type);
}

