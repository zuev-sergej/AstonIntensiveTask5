package ru.aston.notificationservice.service;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ru.aston.notificationservice.kafka.UserOperation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class NotificationServiceIntegrationTest {

    @RegisterExtension
    static GreenMailExtension greenMail =
            new GreenMailExtension(new ServerSetup(3025, null, "smtp"));
    @Autowired
    private NotificationService notificationService;

    @DynamicPropertySource
    static void configureMail(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> "localhost");

        registry.add("spring.mail.port", () -> 3025);

        registry.add("spring.mail.properties.mail.smtp.starttls.enable", () -> "false");
    }

    @Test
    void shouldSendCreatedNotification() throws Exception {
        String email = "test@mail.ru";

        notificationService.sendNotification(email, UserOperation.CREATED);

        assertTrue(greenMail.waitForIncomingEmail(5000, 1));

        MimeMessage message = greenMail.getReceivedMessages()[0];

        assertEquals("Создание аккаунта", message.getSubject());

        assertEquals("Здравствуйте! Ваш аккаунт на сайте " + email + " был успешно создан.", message.getContent());
    }

    @Test
    void shouldSendDeletedNotification() throws Exception {
        String email = "test@mail.ru";

        notificationService.sendNotification(email, UserOperation.DELETED);

        assertTrue(greenMail.waitForIncomingEmail(5000, 1));

        MimeMessage message = greenMail.getReceivedMessages()[0];

        assertEquals(email, message.getRecipients(Message.RecipientType.TO)[0].toString());

        assertEquals("Здравствуйте! Ваш аккаунт был удалён.", message.getContent());
    }
}
