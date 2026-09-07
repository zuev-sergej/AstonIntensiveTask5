package ru.aston.notificationservice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.aston.notificationservice.service.NotificationService;

@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "user-events",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(UserEvent event) {

        notificationService.sendNotification(
                event.email(),
                event.operation()
        );
    }
}
