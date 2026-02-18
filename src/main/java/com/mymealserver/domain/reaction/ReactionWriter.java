package com.mymealserver.domain.reaction;

import com.mymealserver.entity.Reaction;
import com.mymealserver.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReactionWriter {

    private final ReactionRepository reactionRepository;

    @Transactional
    public Reaction save(Reaction reaction) {
        return reactionRepository.save(reaction);
    }
}
