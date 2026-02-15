package com.mymealserver.dto.auth;

import com.mymealserver.entity.enums.WithdrawalReason;
import jakarta.validation.constraints.NotNull;

public record WithdrawRequest(

        @NotNull(message = "탈퇴 사유는 필수입니다.")
        WithdrawalReason reason,

        String reasonDetail
) {
}
