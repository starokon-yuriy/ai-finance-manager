package com.ys.ai.aifinancemanager.configuration;

import com.ys.ai.aifinancemanager.application.mapper.CategoryMapper;
import com.ys.ai.aifinancemanager.application.mapper.TransactionMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class that registers MapStruct mappers as Spring beans.
 */
@Configuration
public class MapperConfiguration {

  @Bean
  public CategoryMapper categoryMapper() {
    return Mappers.getMapper(CategoryMapper.class);
  }

  @Bean
  public TransactionMapper transactionMapper() {
    return Mappers.getMapper(TransactionMapper.class);
  }
}
