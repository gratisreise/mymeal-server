package com.mymealserver.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record WithdrawRequest(

        @NotBlank(message = "탈퇴 사유는 필수 항목입니다.")
        String reason,

        @Size(max = 500, message = "탈퇴 사유 상세는 500자 이내로 입력해야 합니다.")
        String reasonDetail

) {
}
