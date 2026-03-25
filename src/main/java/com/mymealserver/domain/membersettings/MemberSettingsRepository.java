package com.mymealserver.domain.membersettings;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberSettingsRepository extends JpaRepository<MemberSettings, Long> {

    Optional<MemberSettings> findByMemberId(Long memberId);
}
