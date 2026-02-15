package com.mymealserver.dto.meal;

import com.mymealserver.entity.enums.MealType;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

public record CreateMealRequest(

        @NotNull(message = "식사 사진은 필수입니다.")
        MultipartFile photo,

        @NotNull(message = "식사 유형은 필수입니다.")
        MealType mealType,

        LocalDateTime mealTime,

        String memo
) {
}
