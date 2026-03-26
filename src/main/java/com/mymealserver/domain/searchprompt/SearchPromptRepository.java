package com.mymealserver.domain.searchprompt;

import com.mymealserver.common.enums.PromptType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SearchPromptRepository extends JpaRepository<SearchPrompt, Long> {

  Optional<SearchPrompt> findByPromptTypeAndIsActiveTrue(PromptType promptType);

  Optional<SearchPrompt> findByPromptType(PromptType promptType);

  @Transactional
  @Modifying
  @Query(
      value =
          "UPDATE search_prompt SET embedding = CAST(:embedding AS vector), "
              + "embedding_created_at = NOW() "
              + "WHERE id = :id",
      nativeQuery = true)
  void updateEmbedding(Long id, String embedding);
}
