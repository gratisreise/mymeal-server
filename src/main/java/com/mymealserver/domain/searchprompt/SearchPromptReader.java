package com.mymealserver.domain.searchprompt;

import com.mymealserver.common.enums.PromptType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchPromptReader {

    private final SearchPromptRepository searchPromptRepository;

    public Optional<SearchPrompt> findActiveByType(PromptType promptType) {
        return searchPromptRepository.findByPromptTypeAndIsActiveTrue(promptType);
    }

    public Optional<SearchPrompt> findByType(PromptType promptType) {
        return searchPromptRepository.findByPromptType(promptType);
    }
}
