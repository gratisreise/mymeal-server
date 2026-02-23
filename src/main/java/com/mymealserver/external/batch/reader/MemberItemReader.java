package com.mymealserver.external.batch.reader;

import com.mymealserver.domain.member.Member;
import com.mymealserver.domain.memberSettings.MemberSettings;
import com.mymealserver.domain.member.MemberRepository;
import com.mymealserver.domain.memberSettings.MemberSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.Iterator;

@Slf4j
@RequiredArgsConstructor
public class MemberItemReader implements ItemReader<Member> {

    private final MemberRepository memberRepository;
    private final MemberSettingsRepository memberSettingsRepository;

    private Iterator<Member> memberIterator;

    @Override
    public Member read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (memberIterator == null) {
            log.info("Initializing member reader for recommendation generation");
            // Fetch members who have recommendation enabled
            memberIterator = memberRepository.findAll().stream()
                    .filter(member -> {
                        MemberSettings settings = memberSettingsRepository.findByMemberId(member.getId()).orElse(null);
                        return settings != null && Boolean.TRUE.equals(settings.getRecommendationEnabled());
                    })
                    .iterator();
            log.info("Found {} members with recommendation enabled", memberRepository.findAll().size());
        }

        if (memberIterator.hasNext()) {
            Member member = memberIterator.next();
            log.debug("Reading member for recommendation processing: memberId={}", member.getId());
            return member;
        }

        log.info("No more members to process for recommendations");
        return null;
    }
}
