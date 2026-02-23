package com.mymealserver.common.test.fixtures;

import com.mymealserver.domain.reaction.Reaction;
import com.mymealserver.common.enums.GradeType;

/**
 * Test fixture for Reaction entities
 * Provides reusable Reaction instances for testing
 */
public class ReactionFixture {

    /**
     * Creates a default positive reaction (GOOD)
     */
    public static Reaction createDefaultReaction() {
        return Reaction.builder()
                .id(1L)
                .mealId(1L)
                .digestionLevel((short) 5)
                .fullnessLevel((short) 4)
                .energyLevel((short) 5)
                .overallScore(4.7)
                .grade(GradeType.GOOD)
                .build();
    }

    /**
     * Creates a good reaction (high scores)
     */
    public static Reaction createGoodReaction(Long mealId) {
        return Reaction.builder()
                .id(1L)
                .mealId(mealId)
                .digestionLevel((short) 5)
                .fullnessLevel((short) 5)
                .energyLevel((short) 5)
                .overallScore(5.0)
                .grade(GradeType.GOOD)
                .build();
    }

    /**
     * Creates a normal reaction (medium scores)
     */
    public static Reaction createNormalReaction(Long mealId) {
        return Reaction.builder()
                .id(2L)
                .mealId(mealId)
                .digestionLevel((short) 3)
                .fullnessLevel((short) 3)
                .energyLevel((short) 3)
                .overallScore(3.0)
                .grade(GradeType.NORMAL)
                .build();
    }

    /**
     * Creates a bad reaction (low scores)
     */
    public static Reaction createBadReaction(Long mealId) {
        return Reaction.builder()
                .id(3L)
                .mealId(mealId)
                .digestionLevel((short) 1)
                .fullnessLevel((short) 2)
                .energyLevel((short) 1)
                .overallScore(1.3)
                .grade(GradeType.BAD)
                .build();
    }

    /**
     * Creates a reaction with custom fields
     */
    public static Reaction createCustomReaction(
            Long id,
            Long mealId,
            short digestionLevel,
            short fullnessLevel,
            short energyLevel,
            double overallScore,
            GradeType grade
    ) {
        return Reaction.builder()
                .id(id)
                .mealId(mealId)
                .digestionLevel(digestionLevel)
                .fullnessLevel(fullnessLevel)
                .energyLevel(energyLevel)
                .overallScore(overallScore)
                .grade(grade)
                .build();
    }

    /**
     * Creates a reaction with minimum valid scores
     */
    public static Reaction createMinScoreReaction(Long mealId) {
        return Reaction.builder()
                .id(4L)
                .mealId(mealId)
                .digestionLevel((short) 1)
                .fullnessLevel((short) 1)
                .energyLevel((short) 1)
                .overallScore(1.0)
                .grade(GradeType.BAD)
                .build();
    }

    /**
     * Creates a reaction with maximum valid scores
     */
    public static Reaction createMaxScoreReaction(Long mealId) {
        return Reaction.builder()
                .id(5L)
                .mealId(mealId)
                .digestionLevel((short) 5)
                .fullnessLevel((short) 5)
                .energyLevel((short) 5)
                .overallScore(5.0)
                .grade(GradeType.GOOD)
                .build();
    }
}
