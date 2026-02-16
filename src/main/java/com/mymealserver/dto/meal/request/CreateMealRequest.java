package com.mymealserver.dto.meal;

import com.mymealserver.entity.enums.MealType;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

public record CreateMealRequest(

        @NotNull(message = "{validation.meal.photo.notnull}")
        MultipartFile photo,

        @NotNull(message = "{validation.meal.mealType.notnull}")
        MealType mealType,

        LocalDateTime mealTime,

        String memo
) {
}
