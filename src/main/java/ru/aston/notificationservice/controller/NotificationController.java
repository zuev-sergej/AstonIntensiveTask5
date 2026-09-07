package ru.aston.notificationservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.aston.notificationservice.dto.NotificationRequestDto;
import ru.aston.notificationservice.service.NotificationService;

@Controller
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<Void> sendNotification(
            @Valid
            @RequestBody NotificationRequestDto request
    ) {
        notificationService.sendNotification(
                request.email(),
                request.operation()
        );

        return ResponseEntity.noContent().build();
    }
}
