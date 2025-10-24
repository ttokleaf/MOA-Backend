package com.moa.moa_backend.transaction.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionType {
    INCOME("수입"),
    EXPENSE("지출"),
    SAVINGS("저축");

    private final String description;
}

