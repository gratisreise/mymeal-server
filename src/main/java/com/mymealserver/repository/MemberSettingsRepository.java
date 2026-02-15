package com.mymealserver.repository;

import com.mymealserver.entity.MemberSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberSettingsRepository extends JpaRepository<MemberSettings, Long> {

    Optional<MemberSettings> findByMemberId(Long memberId);
}
