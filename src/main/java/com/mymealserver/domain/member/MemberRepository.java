package com.mymealserver.domain.member;

import com.mymealserver.common.enums.ProviderType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmailAndDeletedAtIsNull(String email);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    Optional<Member> findByProviderAndProviderIdAndDeletedAtIsNull(ProviderType provider, String providerId);

    @Query("""
        SELECT m FROM Member m
        JOIN MemberSettings ms ON ms.memberId = m.id
        WHERE ms.recommendationEnabled = true
        AND m.deletedAt IS NULL
        AND m.isActive = true
        ORDER BY m.id ASC
        """)
    Page<Member> findMembersWithRecommendationEnabled(Pageable pageable);
}
