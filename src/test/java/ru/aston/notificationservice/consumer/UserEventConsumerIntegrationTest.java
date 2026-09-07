package ru.aston.notificationservice.consumer;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetup;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(UserEventConsumerIntegrationTest.KafkaProducerTestConfig.class)
@EmbeddedKafka(
        partitions = 1,
        topics = "user-events",
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class UserEventConsumerIntegrationTest {

    private static final String TOPIC = "user-events";

    @RegisterExtension
    static GreenMailExtension greenMail =
            new GreenMailExtension(
                    new ServerSetup(3026, "localhost", ServerSetup.PROTOCOL_SMTP)
            );
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @DynamicPropertySource
    static void configureMail(DynamicPropertyRegistry registry) {

        registry.add(
                "spring.mail.host",
                () -> "localhost"
        );

        registry.add(
                "spring.mail.port",
                () -> 3026
        );

        registry.add(
                "spring.mail.properties.mail.smtp.auth",
                () -> false
        );

        registry.add(
                "spring.mail.properties.mail.smtp.starttls.enable",
                () -> false
        );
    }

    @BeforeEach
    void setUp() throws com.icegreen.greenmail.store.FolderException {
        greenMail.purgeEmailFromAllMailboxes();
    }

    @Test
    void shouldConsumeCreatedEventAndSendEmail() throws Exception {

        String json = """
                {
                    "id": 1,
                    "email": "test@mail.ru",
                    "operation": "CREATED"
                }
                """;

        kafkaTemplate.send(TOPIC, "test@mail.ru", json).get();

        boolean received = greenMail.waitForIncomingEmail(10_000, 1);

        assertTrue(received);

        var messages = greenMail.getReceivedMessages();

        assertEquals(1, messages.length);

        assertEquals("test@mail.ru", messages[0].getAllRecipients()[0].toString());
    }

    @Test
    void shouldConsumerDeletedEventAndSendEmail() throws Exception {
        String json = """
                {
                    "id": 1,
                    "email": "test@mail.ru",
                    "operation": "DELETED"
                }
                """;

        kafkaTemplate.send(TOPIC, "test@mail.ru", json).get();

        boolean received = greenMail.waitForIncomingEmail(10000, 1);

        assertTrue(received);

        var messages = greenMail.getReceivedMessages();

        assertEquals(1, messages.length);

        assertEquals("test@mail.ru", messages[0].getAllRecipients()[0].toString());
    }

    @TestConfiguration
    static class KafkaProducerTestConfig {

        @Bean
        ProducerFactory<String, String> producerFactory(
                EmbeddedKafkaBroker embeddedKafka) {

            Map<String, Object> props =
                    KafkaTestUtils.producerProps(embeddedKafka);

            return new DefaultKafkaProducerFactory<>(
                    props,
                    new StringSerializer(),
                    new StringSerializer()
            );
        }

        @Bean
        KafkaTemplate<String, String> kafkaTemplate(
                ProducerFactory<String, String> producerFactory) {

            return new KafkaTemplate<>(producerFactory);
        }
    }
}
