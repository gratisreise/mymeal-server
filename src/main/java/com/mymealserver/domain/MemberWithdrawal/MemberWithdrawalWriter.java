package com.mymealserver.domain.MemberWithdrawal;

import com.mymealserver.entity.MemberWithdrawal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberWithdrawalWriter {

    private final MemberWithdrawalRepository memberWithdrawalRepository;

    @Transactional
    public MemberWithdrawal save(MemberWithdrawal memberWithdrawal) {
        return memberWithdrawalRepository.save(memberWithdrawal);
    }
}
