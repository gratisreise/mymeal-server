package com.mymealserver.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record WithdrawRequest(

        @NotBlank(message = "{validation.withdraw.reason.notblank}")
        String reason,

        @Size(max = 500, message = "{validation.withdraw.reasonDetail.size.max}")
        String reasonDetail

) {
}
