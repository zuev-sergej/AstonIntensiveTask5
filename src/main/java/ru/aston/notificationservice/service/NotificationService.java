package ru.aston.notificationservice.service;

import ru.aston.notificationservice.kafka.UserOperation;

public interface NotificationService {

    void sendNotification(String email, UserOperation operation);
}
