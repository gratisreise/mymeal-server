package com.mymealserver.api.auth.dto.request;

import com.mymealserver.entity.MemberWithdrawal;
import com.mymealserver.entity.enums.WithdrawalReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * 회원 탈퇴 요청 DTO
 */
@Builder
public record WithdrawRequest(

        @NotBlank(message = "{validation.withdraw.reason.notblank}")
        String reason,

        @Size(max = 500, message = "{validation.withdraw.reasonDetail.size.max}")
        String reasonDetail
) {
    /**
     * MemberWithdrawal 엔티티로 변환
     *
     * @param memberId 탈퇴 회원 ID
     * @return MemberWithdrawal 엔티티
     */
    public MemberWithdrawal toEntity(Long memberId) {
        WithdrawalReason withdrawalReason = WithdrawalReason.fromString(reason);

        return MemberWithdrawal.builder()
                .memberId(memberId)
                .reason(withdrawalReason)
                .reasonDetail(reasonDetail)
                .build();
    }
}
