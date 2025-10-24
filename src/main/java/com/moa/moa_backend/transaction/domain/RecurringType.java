package com.moa.moa_backend.transaction.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecurringType {
    NONE("반복 없음"),
    DAILY("매일"),
    WEEKLY("매주"),
    MONTHLY("매월"),
    YEARLY("매년");

    private final String description;
}

