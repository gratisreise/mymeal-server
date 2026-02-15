package com.mymealserver.domain.reaction;

import com.mymealserver.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReactionWriter {

    private final ReactionRepository reactionRepository;

}
