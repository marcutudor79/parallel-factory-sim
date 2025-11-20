package com.example.controller;

/* Spring related packages */
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/* Java related packages */
import java.util.logging.*;

/* Robotsim related packages */
import fr.tp.inf112.projects.robotsim.model.*;

@RestController
@RequestMapping("/simulation")
public class SimulatorController {

    private SimulationService service;
    private static final Logger logger = Logger.getLogger(SimulatorController.class.getName());

	public SimulatorController(SimulationService service) {
        this.service = service;
    }

    /*  Start simulating a factory model as identified by its ID.
        a. The service will call the persistence web server that you created in a previous
           exercise to read the factory model from it.
        b. It will store this model in a list of models being currently simulated by the
           simulation server.
        c. It will start the simulation of this factory model.
        d. It will return true if everything went well and false otherwise */
    @PostMapping("/start/{id}")
    public ResponseEntity<?> start(@PathVariable String id) {
        boolean response = false;
        logger.info("Received request to start simulation for model ID: " + id);

        /* Sanity check the received ID */
        if (id == null || id.isEmpty()) {
            logger.warning("Invalid model ID received: " + id);
            return ResponseEntity.ok(response);
        }

        /* Trigger a start of the simulation for a specific id */
        response = service.startSimulation(id);
        logger.info("Return start status for model ID " + id + ": " + response);

        return ResponseEntity.ok(response);
    }

    /* Retrieve a factory model currently being simulated as identified by its ID passed as a
       parameter. This method will be used later by the factory viewer to obtain the
       simulated model at a given period to be displayed by the viewer showing the fresh model. */
    @GetMapping("/retrieve/{id}")
    public ResponseEntity<?> retrieve(@PathVariable String id) {
        boolean response = false;
        Factory factory  = null;

        /* Trigger a retrieve of the simulation for a specific id */
        logger.info("Received request to retrieve simulation for model ID: " + id);
        factory = service.getSimulatedModel(id);

        if (factory == null) {
            logger.info("No factory found for model ID: " + id);
            return ResponseEntity.ok(response);
        }

        logger.info("Returning factory for model ID: " + id);
        return ResponseEntity.ok(factory);
    }

    /* Stop the simulation of a robotic factory model as identified by its ID passed as parameter.
     */
    @PostMapping("/stop/{id}")
    public ResponseEntity<?> stop(@PathVariable String id) {
        boolean response = false;

        /* Trigger a stop of the simulation for a specific id */
        logger.info("Received request to stop simulation for model ID: " + id);
        response = service.stopSimulation(id);
        logger.info("Simulation stop status for model ID " + id + ": " + response);

        return ResponseEntity.ok(response);
    }
}
