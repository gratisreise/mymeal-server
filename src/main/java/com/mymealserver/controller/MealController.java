package com.mymealserver.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/meals")
@RequiredArgsConstructor
@Tag(name = "Meals", description = "식사 관리")
public class MealController {

    // TODO: 식사 관리 API 구현
}
