package com.mymealserver.api.reaction;

import com.mymealserver.api.reaction.dto.request.ReactionRequest;
import com.mymealserver.api.reaction.dto.response.ReactionResponse;
import com.mymealserver.api.reaction.service.ReactionService;
import com.mymealserver.common.annotation.CurrentMember;
import com.mymealserver.common.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/meals/{mealId}/reactions")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;

    @PostMapping
    public ResponseEntity<SuccessResponse<ReactionResponse>> createReaction(
            @CurrentMember Long memberId,
            @PathVariable Long mealId,
            @Valid @RequestBody ReactionRequest request
    ) {
        return SuccessResponse.toCreated(reactionService.createReaction(memberId, mealId, request));
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<ReactionResponse>> getReaction(
            @PathVariable Long mealId,
            @CurrentMember Long memberId
    ) {
        return SuccessResponse.toOk(reactionService.getReactionByMealId(memberId, mealId));
    }

    @PutMapping
    public ResponseEntity<SuccessResponse<ReactionResponse>> updateReaction(
            @PathVariable Long mealId,
            @CurrentMember Long memberId,
            @Valid @RequestBody ReactionRequest request
    ) {
        return SuccessResponse.toOk(reactionService.updateReaction(memberId, mealId, request));
    }
}
