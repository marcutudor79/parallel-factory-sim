package com.example.controller;

/* Spring related packets */
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/* Java related packets */
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
/* RobotSim related packets */
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.PersistenceClient;

/**
 * Fetches canvas models from the persistence web server and manages active Factory simulations.
 */
@Service
public class SimulationService {
    private static final Logger logger = Logger.getLogger(SimulationService.class.getName());
    private final ConcurrentMap<String, Factory> activeSimulations = new ConcurrentHashMap<>();
    private PersistenceClient persistenceClient = null;

    public SimulationService(@Value("${persistence.addr}") String persistanceAddr, @Value("${persistence.port}") int persistancePort) {
        // ensure trailing slash for simple concatenation
        this.persistenceClient = new PersistenceClient(persistanceAddr, persistancePort);
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
            Factory factory = persistenceClient.retrieveFactory(id);
            if (factory == null) {
                logger.warning("No factory retrieved from persistence server for model ID: " + id);
                return false;
            }
            logger.info("Successfully fetched model ID: " + id + " from persistence server.");

            /* Do not block the springboot app */
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    factory.startSimulation();
                }
            });

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

        /* If already in the map of active simulations, return it */
        if (activeSimulations.containsKey(id)) {
            logger.info("Retrieving simulated model for ID: " + id);
            return activeSimulations.get(id);
        }

        logger.info("Model ID: " + id + " not loaded. Fetching from persistence server.");
        try {
            Factory factory = persistenceClient.retrieveFactory(id);
            if (factory == null) {
                logger.warning("No factory retrieved from persistence server for model ID: " + id);
                return null;
            }
            logger.info("Successfully fetched model ID: " + id + " from persistence server.");

            activeSimulations.put(id, factory);
            return factory;

        } catch (Exception e) {
            logger.severe("Failed to fetch simulation for " + id + ": " + e.getMessage());
            return null;
        }
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
