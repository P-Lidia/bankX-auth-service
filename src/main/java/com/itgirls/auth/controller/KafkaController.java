package com.itgirls.auth.controller;

import com.itgirls.auth.dto.UserEventDto;
import com.itgirls.auth.kafka.producer.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KafkaController {

    private final KafkaProducer kafkaProducer;

    @PostMapping("/publish")
    public ResponseEntity<String> sendStandardUser(@RequestBody UserEventDto userEventDto) {
        kafkaProducer.sendEvent(userEventDto.getId(), userEventDto);
        return ResponseEntity.ok("User sent via standard producer");
    }
}
