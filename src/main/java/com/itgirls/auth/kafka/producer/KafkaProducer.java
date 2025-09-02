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

    private KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${app.kafka.topics.user-events}")
    private String userEventsTopic;

    @Value("${app.kafka.topics.dead-letter-queue}")
    private String dlqTopic;

    public void sendEvent(String key, UserEventDto userEventDto) {
        try {
            kafkaTemplate.send(userEventsTopic, key, userEventDto);
            log.info("Standard producer sent to {}: key={}, value={}", userEventsTopic, key, userEventDto);
        } catch (Exception e) {
            log.error("Error sending to {}, sending to DLQ: {}", userEventsTopic, e.getMessage());
        }
    }

    //Отправка сообщения в Dead Letter Queue при ошибке
    private void sendToDlq(String key, UserEventDto userEventDto, String error) {
        try {
            String dlqMessage = String.format("Original message: %s, Error: %s", userEventDto.toString(), error);
            kafkaTemplate.send(dlqTopic, key, dlqMessage);
            log.info("Sent to DLQ {}: key={}, value={}", dlqTopic, key, userEventDto);
        } catch (Exception e) {
            log.error("Failed to send to DLQ {}: {}", dlqTopic, e.getMessage());
        }
    }
}
