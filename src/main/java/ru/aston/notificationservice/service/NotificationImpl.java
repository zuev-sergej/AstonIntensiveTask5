package ru.aston.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.aston.notificationservice.kafka.UserOperation;

@Service
@RequiredArgsConstructor
public class NotificationImpl implements NotificationService {

    private final JavaMailSender mailSender;

    @Override
    public void sendNotification(String email, UserOperation operation) {
        String text = switch (operation) {
            case CREATED -> "Здравствуйте! Ваш аккаунт на сайте " + email + " был успешно создан.";
            case DELETED -> "Здравствуйте! Ваш аккаунт был удалён.";
        };

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);

        message.setSubject("Notification from User Service");

        message.setText(text);

        mailSender.send(message);
    }
}
