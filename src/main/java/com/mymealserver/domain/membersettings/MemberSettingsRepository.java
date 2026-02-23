package com.mymealserver.domain.membersettings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberSettingsRepository extends JpaRepository<MemberSettings, Long> {

    Optional<MemberSettings> findByMemberId(Long memberId);
}
