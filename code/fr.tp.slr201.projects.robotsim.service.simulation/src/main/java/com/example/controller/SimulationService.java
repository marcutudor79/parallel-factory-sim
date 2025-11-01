package com.example.controller;

/* Spring related packets */
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/* Java related packets */
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/* RobotSim related packets */
import fr.tp.inf112.projects.robotsim.model.Factory;

/**
 * Fetches canvas models from the persistence web server and manages active Factory simulations.
 */
@Service
public class SimulationService {
    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String persistenceBaseUrl;
    private static final Logger logger = Logger.getLogger(SimulationService.class.getName());

    /* Map of active factory simulation models */
    private final ConcurrentMap<String, Factory> activeSimulations = new ConcurrentHashMap<>();

    public SimulationService(String persistenceBaseUrl) {
        /* Hardcoded port for the persistence server */
        this.persistenceBaseUrl = persistenceBaseUrl;
    }

    /*
     * Start simulation for model identified by id.
     * Returns true if model was fetched and simulation started, false otherwise.
     */
    public boolean startSimulation(String id) {
        logger.info("Trying to start simulation for model ID: " + id);

        /* if already loaded, start / restart the simulation */
        if (activeSimulations.containsKey(id)) {
            logger.info("Model ID: " + id + " already loaded. Attempting to start simulation.");
            try {
                activeSimulations.get(id).startSimulation();
                logger.info("Simulation for model ID: " + id + " started successfully.");
                return true;
            } catch (Exception e) {
                logger.severe("Failed to start simulation for model ID: " + id + ". Exception: " + e.getMessage());
                return false;
            }
        }

        logger.info("Model ID: " + id + " not loaded. Fetching from persistence server.");
        try {
            /* GET http://<persistence>/{id} */
            String url = persistenceBaseUrl + id;
            ResponseEntity<Object> resp = rest.getForEntity(url, Object.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                logger.warning("Failed to fetch model ID: " + id + " from persistence server. Status: " + resp.getStatusCode());
                return false;
            }

            /* Get the payload from the body and try to convert to Factory */
            Object payload = resp.getBody();
            Factory factory = (payload instanceof Factory)
                    ? (Factory) payload
                    : mapper.convertValue(payload, Factory.class);
            if (factory == null) {
                logger.warning("Failed to convert payload to Factory for model ID: " + id);
                return false;
            }
            logger.info("Successfully fetched model ID: " + id + " from persistence server.");

            /* Add factory to map of id, factories */
            factory.startSimulation();
            activeSimulations.put(id, factory);
            logger.info("Simulation for model ID: " + id + " started successfully after fetching.");
            return true;

        } catch (Exception e) {
            logger.severe("Failed to start simulation for " + id + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieve the currently simulated Canvas (Factory) by id, or null if not found.
     */
    public Factory getSimulatedModel(String id) {
        logger.info("Retrieving simulated model for ID: " + id);
        return activeSimulations.get(id);
    }

    /**
     * Stop simulation for given id. Returns true if stopped, false if not found or error.
     */
    public boolean stopSimulation(String id) {
        logger.info("Attempting to stop simulation for model ID: " + id);
        Factory factory = activeSimulations.get(id);
        if (factory == null) {
            logger.warning("Factory model ID: " + id + " not found among active simulations.");
            return false;
        }
        try {
            logger.info("Factory model ID: " + id + " found. Stopping simulation.");
            factory.stopSimulation();
            logger.info("Simulation for model ID: " + id + " stopped successfully.");
            activeSimulations.remove(id);
            return true;
        } catch (Exception e) {
            logger.severe("Failed to stop simulation for model ID: " + id + ". Exception: " + e.getMessage());
            return false;
        }
    }
}
