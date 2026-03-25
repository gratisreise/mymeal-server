package com.mymealserver.external.batch;


import com.mymealserver.api.recommendation.service.AiAnalysisService;
import com.mymealserver.api.recommendation.service.RagPromptBuilder;
import com.mymealserver.api.recommendation.service.RecommendationResult;
import com.mymealserver.api.recommendation.service.RecommendationResult.MealRecommendation;
import com.mymealserver.common.enums.MealType;
import com.mymealserver.common.enums.PromptType;
import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.domain.meallog.MealLog;
import com.mymealserver.domain.meallog.MealLogReader;
import com.mymealserver.domain.member.Member;
import com.mymealserver.domain.membersettings.MemberSettings;
import com.mymealserver.domain.membersettings.MemberSettingsReader;
import com.mymealserver.domain.recommendation.Recommendation;
import com.mymealserver.domain.recommendation.RecommendationReader;
import com.mymealserver.domain.searchprompt.SearchPrompt;
import com.mymealserver.domain.searchprompt.SearchPromptReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationProcessor implements ItemProcessor<Member, List<Recommendation>> {

    private final MealLogReader mealLogReader;
    private final SearchPromptReader searchPromptReader;
    private final AiAnalysisService aiAnalysisService;
    private final MemberSettingsReader memberSettingsReader;
    private final RecommendationReader recommendationReader;

    @Override
    public List<Recommendation> process(Member member) {
        // 1. 회원 설정 조회
        MemberSettings settings = memberSettingsReader.findByMemberIdOrNull(member.getId());

        if (settings == null) {
            return null;
        }

        // 2. 식사 시간 설정 여부 확인
        if (isMealTimeSetted(settings)) {
            return null;
        }

        // 3. 중복 추천 확인
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        if (hasAnyTodayRecommendation(member.getId(), todayStart, todayEnd)) {
            return null;
        }

        // 4. 쿼리 벡터 조회
        String queryVector = getQueryVector();

        // 5. 벡터 검색
        List<MealLog> goodMeals = mealLogReader.findSimilarMealsByVector(member.getId(), queryVector, 3);

        // 6. RAG 프롬프트 구성
        String prompt = RagPromptBuilder.buildRecommendationPrompt(goodMeals);

        // 7. AI 호출
        RecommendationResult result = aiAnalysisService.generateRecommendations(prompt);

        // 8. 추천 엔티티 생성
        List<Recommendation> recommendations = new ArrayList<>();
        LocalDateTime today = LocalDate.now().atStartOfDay();

        // 아침
        if (result.breakfast() != null && settings.getBreakfastTime() != null) {
            LocalDateTime breakfastTime = today.with(settings.getBreakfastTime());
            recommendations.add(createRecommendation(
                member.getId(),
                result.breakfast(),
                MealType.BREAKFAST,
                breakfastTime
            ));
        }

        // 점심
        if (result.lunch() != null && settings.getLunchTime() != null) {
            LocalDateTime lunchTime = today.with(settings.getLunchTime());
            recommendations.add(createRecommendation(
                member.getId(),
                result.lunch(),
                MealType.LUNCH,
                lunchTime
            ));
        }

        // 저녁
        if (result.dinner() != null && settings.getDinnerTime() != null) {
            LocalDateTime dinnerTime = today.with(settings.getDinnerTime());
            recommendations.add(createRecommendation(
                member.getId(),
                result.dinner(),
                MealType.DINNER,
                dinnerTime
            ));
        }

        return recommendations.isEmpty() ? null : recommendations;
    }

    private static boolean isMealTimeSetted(MemberSettings settings) {
        return settings.getBreakfastTime() == null &&
            settings.getLunchTime() == null &&
            settings.getDinnerTime() == null;
    }

    private Recommendation createRecommendation(
            Long memberId,
            MealRecommendation mealRec,
            MealType mealType,
            LocalDateTime scheduledTime
    ) {
        String mealName = mealRec.mealName();
        String reason = mealRec.reason();
        String nutritionTips = mealRec.nutritionTips();

        // 푸시 메시지 구성
        String pushMessage = String.format("🍽️ %s 추천: %s\n\n%s",
                mealType.getDescription(),
                mealName,
                reason
        );

        // 메뉴 상세 JSON 구성
        String menuDetails = String.format(
                "{\"mealName\":\"%s\",\"reason\":\"%s\",\"nutritionTips\":\"%s\"}",
                mealName, reason, nutritionTips
        );

        return Recommendation.builder()
                .memberId(memberId)
                .mealType(mealType)
                .scheduledTime(scheduledTime)
                .menuDetails(menuDetails)
                .pushMessage(pushMessage)
                .isSent(false)
                .build();
    }


    // == private ==

    private boolean hasAnyTodayRecommendation(Long memberId, LocalDateTime start, LocalDateTime end) {
        return recommendationReader.existsRecommendationInRange(memberId, MealType.BREAKFAST, start, end) ||
               recommendationReader.existsRecommendationInRange(memberId, MealType.LUNCH, start, end) ||
               recommendationReader.existsRecommendationInRange(memberId, MealType.DINNER, start, end);
    }


    private String getQueryVector() {
        SearchPrompt searchPrompt = searchPromptReader.findActiveByType(PromptType.POSITIVE_SEARCH)
                .filter(sp -> sp.getEmbedding() != null)
                .orElseThrow(() -> new BusinessException(ErrorCode.SEARCH_PROMPT_NOT_FOUND));
        return searchPrompt.getEmbedding();
    }
}
