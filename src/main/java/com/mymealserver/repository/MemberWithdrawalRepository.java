package com.mymealserver.repository;

import com.mymealserver.entity.MemberWithdrawal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberWithdrawalRepository extends JpaRepository<MemberWithdrawal, Long> {
}
