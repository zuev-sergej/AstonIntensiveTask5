package ru.aston.notificationservice.controller;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.aston.notificationservice.dto.NotificationRequestDto;
import ru.aston.notificationservice.kafka.UserOperation;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class NotificationControllerIntegrationTest {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(new ServerSetup(3026, null, ServerSetup.PROTOCOL_SMTP));
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configureMail(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> "localhost");

        registry.add("spring.mail.port", () -> 3026);

        registry.add("spring.mail.properties.mail.smtp.auth", () -> "false");
    }

    @Test
    void shouldSendNotificationTroughRestApi() throws Exception {
        NotificationRequestDto requestDto = new NotificationRequestDto("test@mail.ru", UserOperation.CREATED);

        mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))).andExpect(status().isNoContent());

        assertTrue(greenMail.waitForIncomingEmail(5000, 1));

        MimeMessage message = greenMail.getReceivedMessages()[0];

        assertEquals("Создание аккаунта", message.getSubject());

        assertEquals("Здравствуйте! Ваш аккаунт на сайте " + requestDto.email() + " был успешно создан.", message.getContent());
    }
}
