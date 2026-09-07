package ru.aston.notificationservice.kafka;

public record UserEvent(
        Long id,
        String email,
        UserOperation operation
) {
}
