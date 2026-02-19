package com.mymealserver.repository;

import com.mymealserver.entity.MealLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealLogRepository extends JpaRepository<MealLog, Long> {

    /**
     * Meal ID로 MealLog 조회
     */
    MealLog findByMealId(Long mealId);

    /**
     * Member ID로 모든 MealLog 조회 (최신순)
     */
    List<MealLog> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    /**
     * 벡터 유사도 검색 (pgvector)
     * cosine distance 기반 유사 식사 검색
     * 1 - (embedding <=> query_vector) = similarity
     */
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

    /**
     * 임베딩이 있는 MealLog 수 조회 (member별)
     */
    long countByMemberIdAndEmbeddingIsNotNull(Long memberId);
}
