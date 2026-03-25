package com.mymealserver.domain.meallog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MealLogRepository extends JpaRepository<MealLog, Long> {

    MealLog findByMealId(Long mealId);
    List<MealLog> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE meal_log SET embedding = CAST(:embedding AS vector), "
        + "embedding_created_at = NOW() "
        + "WHERE id = :id", nativeQuery = true)
    void updateEmbedding(Long id, String embedding);

    @Query(value = """
        SELECT id, meal_id, member_id, meal_summary, reaction_summary, combined_summary,
               embedding, embedding_created_at, created_at, updated_at, deleted_at,
               1 - (embedding <=> CAST(:queryVector AS vector)) AS similarity
        FROM meal_logs
        WHERE embedding IS NOT NULL
          AND deleted_at IS NULL
          AND member_id = :memberId
        ORDER BY embedding <=> CAST(:queryVector AS vector)
        LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> findSimilarByEmbedding(
            @Param("queryVector") String queryVector,
            @Param("memberId") Long memberId,
            @Param("limit") int limit
    );

    long countByMemberIdAndEmbeddingIsNotNull(Long memberId);

    @Query(value = """
            SELECT ml.*
            FROM meal_logs ml
            WHERE ml.member_id = :memberId
              AND ml.embedding IS NOT NULL
              AND ml.deleted_at IS NULL
            ORDER BY ml.embedding <=> CAST(:queryVector AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<MealLog> findSimilarMealsByVector(
            @Param("memberId") Long memberId,
            @Param("queryVector") String queryVector,
            @Param("limit") int limit
    );
}
