package com.mymealserver.api.notification.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BatchReadRequest(
    @NotEmpty(message = "{validation.notification.ids.notempty}")
        @Size(max = 100, message = "{validation.notification.ids.size.max}")
        List<Long> ids) {}
