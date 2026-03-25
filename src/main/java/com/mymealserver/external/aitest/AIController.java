package com.mymealserver.external.ai;


import com.mymealserver.domain.meallog.MealLog;
import com.mymealserver.domain.meallog.MealLogReader;
import com.mymealserver.domain.meallog.MealLogRepository;
import com.mymealserver.domain.searchprompt.SearchPrompt;
import com.mymealserver.domain.searchprompt.SearchPromptRepository;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class AIController {
    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final MealLogRepository mealLogRepository;
    private final MealLogReader mealLogReader;
    private final SearchPromptRepository searchPromptRepository;

    @GetMapping("/chat")
    public String testChat(@RequestParam String text){
        return chatClient
            .prompt()
            .user(text)
            .call()
            .content();
    }

    @GetMapping("/embed")
    public void testEmbed(){
        Long id = 1L;
        SearchPrompt searchPrompt = searchPromptRepository.findById(id)
            .orElseGet(null);
        if(searchPrompt == null) log.info("컷!!!!");
        float[] embedded = embeddingModel.embed(searchPrompt.getPromptText());
        searchPromptRepository.updateEmbedding(id, Arrays.toString(embedded));
    }



}
