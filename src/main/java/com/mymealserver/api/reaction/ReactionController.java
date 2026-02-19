package com.mymealserver.api.reaction.controller;

import com.mymealserver.api.reaction.dto.request.ReactionRequest;
import com.mymealserver.api.reaction.dto.response.ReactionResponse;
import com.mymealserver.api.reaction.service.ReactionService;
import com.mymealserver.common.annotation.AuthenticatedMember;
import com.mymealserver.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/meals/{mealId}/reactions")
@RequiredArgsConstructor
@Tag(name = "Reactions", description = "식후 반응 관리")
public class ReactionController {

    private final ReactionService reactionService;

    @PostMapping
    @Operation(summary = "식후 반응 생성", description = "특정 식사에 대한 식후 반응을 기록합니다")
    public ResponseEntity<SuccessResponse<ReactionResponse>> createReaction(
            @Parameter(description = "식사 ID", required = true)
            @PathVariable Long mealId,

            @Parameter(description = "인증된 회원 ID", hidden = true)
            @AuthenticatedMember Long memberId,

            @Valid @RequestBody ReactionRequest request
    ) {
        log.info("createReaction called - memberId: {}, mealId: {}", memberId, mealId);

        ReactionResponse response = reactionService.createReaction(memberId, mealId, request);
        return SuccessResponse.toCreated(response);
    }

    @GetMapping
    @Operation(summary = "식후 반응 조회", description = "특정 식사에 대한 식후 반응을 조회합니다")
    public ResponseEntity<SuccessResponse<ReactionResponse>> getReaction(
            @Parameter(description = "식사 ID", required = true)
            @PathVariable Long mealId,

            @Parameter(description = "인증된 회원 ID", hidden = true)
            @AuthenticatedMember Long memberId
    ) {
        log.info("getReaction called - memberId: {}, mealId: {}", memberId, mealId);

        ReactionResponse response = reactionService.getReactionByMealId(memberId, mealId);
        return SuccessResponse.toOk(response);
    }

    @PutMapping
    @Operation(summary = "식후 반응 수정", description = "특정 식사에 대한 식후 반응을 수정합니다")
    public ResponseEntity<SuccessResponse<ReactionResponse>> updateReaction(
            @Parameter(description = "식사 ID", required = true)
            @PathVariable Long mealId,

            @Parameter(description = "인증된 회원 ID", hidden = true)
            @AuthenticatedMember Long memberId,

            @Valid @RequestBody ReactionRequest request
    ) {
        log.info("updateReaction called - memberId: {}, mealId: {}", memberId, mealId);

        ReactionResponse response = reactionService.updateReaction(memberId, mealId, request);
        return SuccessResponse.toOk(response);
    }
}
