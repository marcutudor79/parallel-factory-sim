package fr.tp.inf112.projects.robotsim.app;

/* Robotsim related pacakages */
import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasPersistenceManager;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.Component;
/* Java related packages */
import java.net.http.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.*;
import javax.swing.SwingUtilities;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

public class RemoteSimulatorController extends SimulatorController {

    private final ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor();
    /* Used to connect to the microservice of simulation */
    private HttpClient httpClient;
    private String     remoteAddr;
    private String     remotePort;
    URI startSimulationURI     = null;
    URI stopSimulationURI      = null;
    URI retrieveSimulationURI  = null;

    private static final Logger LOGGER = Logger.getLogger(RemoteSimulatorController.class.getName());

	public RemoteSimulatorController(final CanvasPersistenceManager persistenceManager, String remoteAddr, String remotePort, String startFactoryId) {
        super(null, persistenceManager);
        this.httpClient     = HttpClient.newHttpClient();
        this.remoteAddr     = remoteAddr;
        this.remotePort     = remotePort;
        this.startSimulationURI = URI.create("http://" + remoteAddr + ":" + remotePort +
                                             "/simulation/start/" + startFactoryId);
        this.stopSimulationURI  = URI.create("http://" + remoteAddr + ":" + remotePort +
                                             "/simulation/stop/" + startFactoryId);
        this.retrieveSimulationURI = URI.create("http://" + remoteAddr + ":" + remotePort +
                                                "/simulation/retrieve/" + startFactoryId);

        /* Start the simulation on the remote server before fetching the JSON */
        startAnimation();
    }

    /**
     *  The method to get the factory model being currently simulated
     *  on the remote simulation server.
     *
     */
    @Override
    public Factory getCanvas() {
        Canvas c = getFactory();
        return (c instanceof Factory) ? (Factory) c : null;
    }

    /**
     * Method to retrieve the factory model from the remote simulation server.
     *
     * @return The factory model retrieved from the remote simulation server.
     */
    private Factory getFactory() {
        LOGGER.info("Building request to get the factory from the remote server: " + remoteAddr + ":" + remotePort);
        HttpRequest getReqRetrieveFact = HttpRequest.newBuilder()
                .uri(retrieveSimulationURI)
                .GET()
                .build();

        LOGGER.info("Sending request to get the factory from the remote server: " + remoteAddr + ":" + remotePort);
        try {
            HttpResponse<String> resp = httpClient.send(getReqRetrieveFact, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) {
                LOGGER.severe("Remote server returned status: " + resp.statusCode());
                return null;
            }

            String body = resp.body();
            if (body == null || body.isBlank()) {
                LOGGER.warning("Empty response body from remote server");
                return null;
            }

            // strip common log-prefix if present (e.g. "INFO: [ ...")
            int firstBracket = body.indexOf('[');
            if (firstBracket > 0) {
                body = body.substring(firstBracket);
            }

            LOGGER.info("Received JSON: " + body);

            // Build a type validator similar to your test so polymorphic type info is allowed
            PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                    .allowIfSubType(PositionedShape.class.getPackageName())
                    .allowIfSubType(Component.class.getPackageName())
                    .allowIfSubType("fr.tp.inf112.projects.canvas.model.impl")
                    .allowIfSubType("java.util")
                    .build();

            ObjectMapper mapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

            Factory factory = mapper.readValue(body, Factory.class);
            LOGGER.info("Successfully parsed factory from remote server");
            return factory;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get/parse factory from remote server", e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
    */
    @Override
    public void setCanvas(final Canvas canvasModel) {

        final List<Observer> observers = getCanvas().getObservers();

        super.setCanvas(canvasModel);

        for (final Observer observer : observers) {
            getCanvas().addObserver(observer);
        }

        getCanvas().notifyObservers();
    }

	/**
     * Method to send the request to start the factory animation on a specified remote simulation
     * server. Using the remoteAddr and remotePort provided at construction time.
     *
	 * {@inheritDoc}
	 */
	@Override
	public void startAnimation() {
        LOGGER.info("Building request to start animation on the remote server: " + remoteAddr + ":" + remotePort);
        HttpRequest postReqStartAnim = HttpRequest.newBuilder()
                .uri(startSimulationURI)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        LOGGER.info("Sending request to start animation on the remote server: " + remoteAddr + ":" + remotePort);
         /* Send the request to start the animation on the remote server */
        try {
            httpClient.send(postReqStartAnim, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.severe("Failed to send request to start animation on the remote server: " + remoteAddr + ":" + remotePort);
            return;
        }
        LOGGER.info("Successfully sent request to start animation on the remote server: " + remoteAddr + ":" + remotePort);
    }

	/**
     * Remote method to send the request to stop the factory animation on a specified remote server.
     * Using the remoteAddr and remotePort provided at construction time.
     *
	 * {@inheritDoc}
	 */
	@Override
	public void stopAnimation() {
        LOGGER.info("Building request to stop animation on the remote server: " + remoteAddr + ":" + remotePort);
        HttpRequest postReqStopAnim = HttpRequest.newBuilder()
                .uri(stopSimulationURI)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        LOGGER.info("Sending request to stop animation on the remote server: " + remoteAddr + ":" + remotePort);
         /* Send the request to stop the animation on the remote server */
        try {
            httpClient.send(postReqStopAnim, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.severe("Failed to send request to stop animation on the remote server: " + remoteAddr + ":" + remotePort);
            return;
        }
        LOGGER.info("Successfully sent request to stop animation on the remote server: " + remoteAddr + ":" + remotePort);
	}

    public void startRemotePolling(long initialDelayMs, long periodMs) {
        poller.scheduleAtFixedRate(() -> {
            try {
                final Factory remote = getFactory(); // your existing method that does HTTP + Jackson
                if (remote != null) {
                    // update controller model on EDT so Swing components see changes safely
                    SwingUtilities.invokeLater(() -> setFactoryModel(remote));
                }
            } catch (final Throwable t) {
                LOGGER.log(Level.WARNING, "Remote polling failed", t);
            }
        }, initialDelayMs, periodMs, TimeUnit.MILLISECONDS);
    }

    public void stopRemotePolling() {
        poller.shutdownNow();
    }

    // call this to replace the canvas model and notify any viewers
    public synchronized void setFactoryModel(final Factory factory) {
        LOGGER.fine("setFactoryModel called; EDT=" + javax.swing.SwingUtilities.isEventDispatchThread() + " factory=" + (factory != null));
        // Delegate to base class which handles observer transfer and notification
        super.setCanvas(factory);
    }
}
