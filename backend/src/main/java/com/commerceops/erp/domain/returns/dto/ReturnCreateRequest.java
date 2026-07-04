package com.commerceops.erp.domain.returns.dto;

import com.commerceops.erp.domain.returns.enums.ReturnReason;
import jakarta.validation.constraints.NotNull;

public record ReturnCreateRequest(
        @NotNull ReturnReason reason,
        String reasonDetail
) {}
