package ru.aston.notificationservice.kafka;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, UserEvent> consumerFactory(
            KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());


        JacksonJsonDeserializer<UserEvent> valueDeserializer =
                new JacksonJsonDeserializer<>(UserEvent.class)
                        .trustedPackages("*")
                        .ignoreTypeHeaders();

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserEvent>
    kafkaListenerContainerFactory(
            ConsumerFactory<String, UserEvent> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, UserEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        return factory;
    }
}
