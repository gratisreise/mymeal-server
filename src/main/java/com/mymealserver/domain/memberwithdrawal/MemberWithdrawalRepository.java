package com.mymealserver.domain.memberwithdrawal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberWithdrawalRepository extends JpaRepository<MemberWithdrawal, Long> {}
