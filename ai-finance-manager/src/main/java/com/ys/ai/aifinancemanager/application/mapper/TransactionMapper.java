package com.ys.ai.aifinancemanager.application.mapper;

import com.ys.ai.aifinancemanager.application.dto.TransactionDto;
import com.ys.ai.aifinancemanager.domain.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(uses = {CategoryMapper.class})
public interface TransactionMapper {

  TransactionDto toDto(Transaction transaction);

  @Mapping(target = "category", ignore = true)
  Transaction toEntity(TransactionDto transactionDto);

  List<TransactionDto> toDtoList(List<Transaction> transactions);

}

