package com.mymealserver.domain.memberwithdrawal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberWithdrawalReader {

  private final MemberWithdrawalRepository memberWithdrawalRepository;

  public MemberWithdrawal findById(Long id) {
    return memberWithdrawalRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("MemberWithdrawal not found"));
  }
}
