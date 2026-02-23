package com.mymealserver.domain.MemberSettings;

import com.mymealserver.domain.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberSettingsWriter {

    private final MemberSettingsRepository memberSettingsRepository;

    @Transactional
    public MemberSettings save(MemberSettings settings) {
        return memberSettingsRepository.save(settings);
    }

    @Transactional
    public MemberSettings createDefault(Member member) {
        MemberSettings settings = MemberSettings.createDefault(member);
        return memberSettingsRepository.save(settings);
    }

    @Transactional
    public MemberSettings updateFcmToken(Long memberId, String fcmToken) {
        MemberSettings settings = memberSettingsRepository.findByMemberId(memberId)
                .orElse(null);

        if (settings != null) {
            settings.updateFcmToken(fcmToken);
            return memberSettingsRepository.save(settings);
        } else {
            MemberSettings newSettings = MemberSettings.builder()
                    .memberId(memberId)
                    .recommendationEnabled(true)
                    .reactionReminderEnabled(true)
                    .mealReminderEnabled(true)
                    .fcmToken(fcmToken)
                    .build();
            return memberSettingsRepository.save(newSettings);
        }
    }
}
