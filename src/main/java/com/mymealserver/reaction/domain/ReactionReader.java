package com.mymealserver.reaction.domain;

import com.mymealserver.entity.Reaction;
import com.mymealserver.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReactionReader {

    private final ReactionRepository reactionRepository;

    public boolean existsByMealId(Long mealId) {
        return reactionRepository.existsByMealId(mealId);
    }

    public Optional<Reaction> findByMealId(Long mealId) {
        return reactionRepository.findByMealId(mealId);
    }
}
