package com.mymealserver.domain.reaction;

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
