package com.mymealserver.domain.reaction;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReactionReader {

  private final ReactionRepository reactionRepository;

  public Reaction findByMealId(Long mealId) {
    return reactionRepository.findByMealId(mealId).orElse(null);
  }

  public List<Reaction> findAllByMealIds(List<Long> mealIds) {
    return reactionRepository.findAllByMealIdIn(mealIds);
  }

  public Map<Long, Reaction> findByMealIdsAsMap(List<Long> mealIds) {
    List<Reaction> reactions = findAllByMealIds(mealIds);
    return reactions.stream()
        .collect(
            java.util.stream.Collectors.toMap(
                Reaction::getMealId,
                reaction -> reaction,
                (existing, replacement) -> existing // Keep first if duplicate mealId
                ));
  }

  public boolean existsByMealId(Long mealId) {
    return reactionRepository.existsByMealId(mealId);
  }
}
