package fr.tp.inf112.projects.robotsim.app;

/* Robotsim related pacakages */
import fr.tp.inf112.projects.canvas.controller.CanvasViewerController;
import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasPersistenceManager;
import fr.tp.inf112.projects.robotsim.model.Factory;

/* Java related packages */
import java.net.http.*;

public class RemoteSimulatorController extends SimulatorController {

    /* Used to connect to the microservice of simulation */
    private HttpClient httpClient;

	public RemoteSimulatorController(final CanvasPersistenceManager persistenceManager) {
        super(null, persistenceManager);
        this.httpClient = HttpClient.newHttpClient();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startAnimation() {
		factoryModel.startSimulation();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stopAnimation() {
		factoryModel.stopSimulation();
	}
}
