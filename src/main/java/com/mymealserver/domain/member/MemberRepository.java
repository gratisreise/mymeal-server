package com.mymealserver.domain.member;

import com.mymealserver.common.enums.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmailAndDeletedAtIsNull(String email);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    Optional<Member> findByProviderAndProviderIdAndDeletedAtIsNull(ProviderType provider, String providerId);
}
