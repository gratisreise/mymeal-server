package com.mymealserver.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient googleChatClient(GoogleGenAiChatModel googleGenAiChatModel){
        return ChatClient.create(googleGenAiChatModel);
    }

}