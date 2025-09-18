package com.itgirls.auth.kafka.producer;

import com.itgirls.auth.dto.UserEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${app.kafka.topics.user-registration-events}")
    private String userRegistrationTopic;
    @Value("${app.kafka.topics.dead-letter-queue}")
    private String dlqTopic;
    @Value("${app.kafka.topics.user-reset-events}")
    private String resetPasswordTopic;


    //sending event to userRegistrationTopic
    public void sendRegistrationEvent(String key, UserEventDto userEventDto) {
        try {
            kafkaTemplate.send(userRegistrationTopic, key, userEventDto);
            log.info("Standard producer sent to {}: key={}, value={}", userRegistrationTopic, key, userEventDto);
        } catch (Exception e) {
            sendToDlq(key, userEventDto, e.getMessage());
            log.error("Error sending to {}, sending to DLQ: {}", userRegistrationTopic, e.getMessage());
        }
    }

    //sending event to resetPasswordTopic
    public void sendResetPasswordEvent(String key, UserEventDto userEventDto) {
        try {
            kafkaTemplate.send(resetPasswordTopic, key, userEventDto);
            log.info("Standard producer sent to {}: key={}, value={}", resetPasswordTopic, key, userEventDto);
        } catch (Exception e) {
            sendToDlq(key, userEventDto, e.getMessage());
            log.error("Error sending to {}, sending to DLQ: {}", resetPasswordTopic, e.getMessage());
        }
    }

    //Отправка сообщения в Dead Letter Queue при ошибке
    private void sendToDlq(String key, UserEventDto userEventDto, String error) {
        try {
            String dlqMessage = String.format("Original message: %s, Error: %s", userEventDto.toString(), error);
            kafkaTemplate.send(dlqTopic, key, dlqMessage);
            log.info("Sent to DLQ {}: key={}, value={}", dlqTopic, key, dlqMessage);
        } catch (Exception e) {
            log.error("Failed to send to DLQ {}: {}", dlqTopic, e.getMessage());
        }
    }
}
