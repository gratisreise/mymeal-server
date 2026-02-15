package com.mymealserver.domain.reaction;

import com.mymealserver.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReactionReader {

    private final ReactionRepository reactionRepository;

}
