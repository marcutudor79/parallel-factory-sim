package com.example.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.Component;
import fr.tp.inf112.projects.robotsim.model.Factory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

@Configuration
public class SimulationRegisterModuleConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(PositionedShape.class.getPackageName())
                .allowIfSubType(Component.class.getPackageName())
                .allowIfSubType(ArrayList.class.getName())
                .allowIfSubType(LinkedHashSet.class.getName())
                .allowIfSubType("fr.tp.inf112.projects.canvas.model.impl.BasicVertex")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS); // ensure false/0 are written
        return mapper;
    }

    @Bean
    ProducerFactory<String, Factory> producerFactory(){
        final Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                   "localhost:9092");

        final JsonSerializer<Factory> factorySerializer = new
            JsonSerializer<>(objectMapper());

        return new DefaultKafkaProducerFactory<>(config,
                                                 new StringSerializer(),
                                                 factorySerializer);
    }

    @Bean
    @Primary
    KafkaTemplate<String, Factory> kafkaTemplate(){
        return new KafkaTemplate<>(producerFactory());
    }
}