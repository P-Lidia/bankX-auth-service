package com.itgirls.auth.kafka;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class KafkaTopicManager {

    private AdminClient adminClient;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.topics.user-registration-events}")
    private String userEventsTopic;

    @Value("${app.kafka.topics.dead-letter-queue}")
    private String dlqTopic;

    @Value("${app.kafka.topics.user-reset-events}")
    private String resetPasswordTopic;

    //Инициализирует AdminClient и создаёт необходимые топики при старте приложения
    @PostConstruct
    public void init() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        this.adminClient = AdminClient.create(props);
        createDefaultTopics();
    }

    //Создаёт предопределённые топики из конфигурации.
    //По умолчанию создаёт топики с 3 партициями и фактором репликации 3.
    private void createDefaultTopics() {
        List<String> topics = new ArrayList<>(Arrays.asList(userEventsTopic, dlqTopic, resetPasswordTopic));
        for (String topic : topics) {
            createTopicIfNotExists(topic, 3, (short) 1);
        }
    }

    //Создаёт топик, если он не существует
    private void createTopicIfNotExists(String topicName, int numPartitions, short replicationFactor) {
        try {
            if (topicExists(topicName)) {
                log.warn("Topic '{}' already exists,", topicName);
                return;
            }
            NewTopic newTopic = new NewTopic(topicName, numPartitions, replicationFactor);
            adminClient.createTopics(Collections.singleton(newTopic)).all().get();
            log.info("Topic '{}' created successfully (Partitions: {}, Replication: {})",
                    topicName, numPartitions, replicationFactor);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while creating topic '{}'", topicName, e);
        } catch (ExecutionException e) {
            log.error("Execution error while creating topic '{}'", topicName, e);
        }
    }

    //Проверяет существование топика
    private boolean topicExists(String topicName) throws ExecutionException, InterruptedException {
        return adminClient.listTopics().names().get().contains(topicName);
    }

    //Закрывает AdminClient при уничтожении бина.
    @PreDestroy
    public void close() {
        if (adminClient != null) {
            adminClient.close();
            log.info("Kafka AdminClient closed");
        }
    }
}
