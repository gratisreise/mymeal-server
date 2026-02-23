package com.mymealserver.domain.MemberWithdrawal;

import com.mymealserver.entity.Member;
import com.mymealserver.entity.MemberWithdrawal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberWithdrawalReader {

    private final MemberWithdrawalRepository memberWithdrawalRepository;

    public MemberWithdrawal findById(Long id) {
        return memberWithdrawalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MemberWithdrawal not found"));
    }

    public List<MemberWithdrawal> findByMember(Member member) {
        return memberWithdrawalRepository.findByMember(member);
    }
}
