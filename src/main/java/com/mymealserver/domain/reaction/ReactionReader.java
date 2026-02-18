package com.mymealserver.domain.reaction;

import com.mymealserver.entity.Reaction;
import com.mymealserver.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
