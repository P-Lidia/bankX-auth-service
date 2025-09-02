package com.itgirls.auth.config;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

//Used with application.yaml (bootstrap-servers, retries, acks). Class has a priority
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers; //brocker address

    @Value("${app.kafka.retry.max-attempts}")
    private int retryMaxAttempts; //max number of retries

    private Map<String, Object> getBaseProducerConfigs() {
        Map<String, Object> configProps = new HashMap<>();

        //Адреса брокеров Kafka
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        //Сериализаторы для ключей и значений. Преобразуют строки в байты для отправки сообщений
        //Рекомендация: StringSerializer для строк; JsonSerializer/AvroSerializer для сложных объектов
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Режим подтверждения доставк
        //Влияние: all — ждёт подтверждения от всех ISR (надёжность); 1 — только от лидера (производительность); 0 — без подтверждения (максимальная скорость)
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");

        // Количество попыток повтора при ошибке доставки. Повторяет отправку при временных сбоях
        // Рекомендация: 3 для демо (из application.yaml); 5-10 для продакшена;
        configProps.put(ProducerConfig.RETRIES_CONFIG, retryMaxAttempts);

        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);

        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 5);

        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);

        //Тип компрессии. snappy — баланс скорости и компрессии; lz4 — быстрее; gzip — выше компрессия; none — без компрессии
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);

        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);

        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);

        return configProps;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(getBaseProducerConfigs());
    }

    //KafkaTemplate для стандартного продюсера. Используется для отправки сообщений в топики (например, order-events).
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
