package com.example.controller;

import fr.tp.inf112.projects.robotsim.model.FactoryModelChangedNotifier;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.robotsim.model.Factory;
import org.springframework.messaging.Message;
import org.springframework.kafka.support.SendResult;
import java.util.List;
import java.util.Map;

public class KafkaFactoryModelChangeNotifier implements FactoryModelChangedNotifier {

    /* Store a factory model inside the notifier */
    private Factory factoryModel;
    private String baseId;
    private KafkaTemplate<String, Factory> simulationEventTemplate;
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(KafkaFactoryModelChangeNotifier.class.getName());

    public KafkaFactoryModelChangeNotifier(Factory factoryModel, KafkaTemplate<String, Factory> simulationEventTemplate) {
        this.factoryModel = factoryModel;

        /* Create a Kafka topic to which changes in factory are published */
        try (AdminClient admin = AdminClient.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"))) {
            String rawId = factoryModel.getId();
            this.baseId = rawId.substring(rawId.lastIndexOf('/') + 1).replaceFirst("\\.factory$", ""); // Puck_Factory_1764007783815

            admin.createTopics(java.util.List.of(new NewTopic("simulation-" + this.baseId, 1, (short)1)))
            .all().get();
        } catch (Exception e) {
            LOGGER.severe("Failed to create Kafka topic for simulation-" + this.baseId + ": " + e.getMessage());
        }

        /* Store the Kafka template used to publish events */
        this.simulationEventTemplate = simulationEventTemplate;
    }

    @Override
    public boolean addObserver(Observer observer) {
        return false;
        // Implementation to add observer via Kafka
    }

    @Override
    public boolean removeObserver(Observer observer) {
        return false;
        // Implementation to remove observer via Kafka
    }

    @Override
    public List<Observer> getObservers() {
        return null;
    }

    @Override
    public void notifyObservers() {
        /* Create a message to be send to the kafka broker */
        final Message<Factory> factoryMessage = MessageBuilder.withPayload(factoryModel)
            .setHeader(KafkaHeaders.TOPIC, "simulation-" + this.baseId)
            .build();

        /* Create completable future variable and register callback
           for when the broker responds */
        final CompletableFuture<SendResult<String, Factory>> sendResult =
            simulationEventTemplate.send(factoryMessage);

        /* Check if broker returned an error */
        sendResult.whenComplete((result, ex) -> {
            if (ex != null) {
                LOGGER.severe("Failed to notify observers via Kafka: " + ex.getMessage());
                throw new RuntimeException(ex);
            }
        });
    }

}
