package com.mymealserver.external.batch;

import com.mymealserver.domain.member.Member;
import com.mymealserver.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberItemReader implements ItemStreamReader<Member> {

    private static final int PAGE_SIZE = 10;
    private static final String CURRENT_PAGE_KEY = "member.reader.current.page";
    private static final String CURRENT_INDEX_KEY = "member.reader.current.index";

    private final MemberRepository memberRepository;

    private Page<Member> memberPage;
    private int currentPage = 0;
    private int currentIndex = 0;

    @Override
    public void open(ExecutionContext executionContext) {
        // 실행 컨텍스트에서 상태 복원
        if (executionContext.containsKey(CURRENT_PAGE_KEY)) {
            currentPage = executionContext.getInt(CURRENT_PAGE_KEY);
        }
        if (executionContext.containsKey(CURRENT_INDEX_KEY)) {
            currentIndex = executionContext.getInt(CURRENT_INDEX_KEY);
        }
        loadPage(currentPage);
    }

    @Override
    public void update(ExecutionContext executionContext) {
        // 현재 상태를 실행 컨텍스트에 저장
        executionContext.putInt(CURRENT_PAGE_KEY, currentPage);
        executionContext.putInt(CURRENT_INDEX_KEY, currentIndex);
    }

    @Override
    public void close() {
        memberPage = null;
        currentPage = 0;
        currentIndex = 0;
    }

    @Override
    public Member read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        // 현재 페이지를 모두 읽었으면 다음 페이지 로드
        while (memberPage == null || currentIndex >= memberPage.getContent().size()) {
            if (memberPage != null && !memberPage.hasNext()) {
                return null;
            }
            loadPage(currentPage + 1);
            currentPage++;
            currentIndex = 0;
        }

        Member member = memberPage.getContent().get(currentIndex);
        currentIndex++;
        return member;
    }

    private void loadPage(int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        memberPage = memberRepository.findMembersWithRecommendationEnabled(pageable);
    }
}
