package com.mymealserver.reaction.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/meals/{mealId}/reactions")
@RequiredArgsConstructor
@Tag(name = "Reactions", description = "식후 반응")
public class ReactionController {

    // TODO: 식후 반응 API 구현
}
