package com.mymealserver.domain.MemberSettings;

import com.mymealserver.domain.base.BaseEntity;
import com.mymealserver.domain.member.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSettings extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long memberId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean recommendationEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean reactionReminderEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean mealReminderEnabled = true;

    @Column
    private LocalTime breakfastTime;

    @Column
    private LocalTime lunchTime;

    @Column
    private LocalTime dinnerTime;

    @Column(length = 512)
    private String fcmToken;

    public void updateFcmToken(String token) {
        this.fcmToken = token;
    }

    public void disableRecommendation() {
        this.recommendationEnabled = false;
    }

    public void enableRecommendation() {
        this.recommendationEnabled = true;
    }

    public void disableReactionReminder() {
        this.reactionReminderEnabled = false;
    }

    public void enableReactionReminder() {
        this.reactionReminderEnabled = true;
    }

    public void disableMealReminder() {
        this.mealReminderEnabled = false;
    }

    public void enableMealReminder() {
        this.mealReminderEnabled = true;
    }

    public void setBreakfastTime(LocalTime breakfastTime) {
        this.breakfastTime = breakfastTime;
    }

    public void setLunchTime(LocalTime lunchTime) {
        this.lunchTime = lunchTime;
    }

    public void setDinnerTime(LocalTime dinnerTime) {
        this.dinnerTime = dinnerTime;
    }

    public static MemberSettings createDefault(Member member) {
        return MemberSettings.builder()
                .memberId(member.getId())
                .recommendationEnabled(true)
                .reactionReminderEnabled(true)
                .mealReminderEnabled(true)
                .build();
    }
}
