package com.mymealserver.api.meal.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record MealRetakePhotoRequest(

        @NotNull(message = "{validation.meal.photo.notnull}")
        MultipartFile photo
) {
}
