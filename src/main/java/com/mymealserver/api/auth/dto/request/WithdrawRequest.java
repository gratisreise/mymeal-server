package com.mymealserver.api.auth.dto.request;

import com.mymealserver.common.enums.WithdrawalReason;
import com.mymealserver.domain.memberwithdrawal.MemberWithdrawal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/** 회원 탈퇴 요청 DTO */
@Builder
public record WithdrawRequest(
    @NotBlank(message = "{validation.withdraw.reason.notblank}") String reason,
    @Size(max = 500, message = "{validation.withdraw.reasonDetail.size.max}") String reasonDetail) {
  public MemberWithdrawal toEntity(Long memberId) {
    WithdrawalReason withdrawalReason = WithdrawalReason.fromString(reason);

    return MemberWithdrawal.builder()
        .memberId(memberId)
        .reason(withdrawalReason)
        .reasonDetail(reasonDetail)
        .build();
  }
}
