package ru.aston.notificationservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.aston.notificationservice.kafka.UserOperation;

public record NotificationRequestDto(
        @NotBlank
        @Email
        String email,

        @NotNull
        UserOperation operation
) {
}
