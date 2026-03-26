package com.mymealserver.domain.searchprompt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchPromptWriter {

  private final SearchPromptRepository searchPromptRepository;

  @Transactional
  public SearchPrompt save(SearchPrompt searchPrompt) {
    return searchPromptRepository.save(searchPrompt);
  }

  @Transactional
  public void delete(SearchPrompt searchPrompt) {
    searchPrompt.softDelete();
    searchPromptRepository.save(searchPrompt);
  }
}
