package fr.tp.inf112.projects.robotsim.app;

import fr.tp.inf112.projects.canvas.controller.CanvasViewerController;
import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasPersistenceManager;
import fr.tp.inf112.projects.robotsim.model.Factory;

public class SimulatorController implements CanvasViewerController {

    private Factory factoryModel;

    private final CanvasPersistenceManager persistenceManager;

    // keep observers even before a Factory exists
    private final java.util.List<Observer> controllerObservers = new java.util.concurrent.CopyOnWriteArrayList<>();

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(SimulatorController.class.getName());

    public SimulatorController(final CanvasPersistenceManager persistenceManager) {
        this(null, persistenceManager);
    }

    public SimulatorController(final Factory factoryModel,
							   final CanvasPersistenceManager persistenceManager) {
        this.factoryModel = factoryModel;
        this.persistenceManager = persistenceManager;
    }

    private void installObserversOnFactory(final Factory f) {
        if (f == null) return;
        for (final Observer o : controllerObservers) {
            if (!f.getNotifier().getObservers().contains(o)) {
                f.addObserver(o); // safe now (notifier guaranteed non-null)
            }
        }
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public boolean addObserver(final Observer observer) {
        if (observer == null) return false;
        controllerObservers.add(observer);
        if (factoryModel != null) {
            return factoryModel.addObserver(observer);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public boolean removeObserver(final Observer observer) {
        controllerObservers.remove(observer);
        if (factoryModel != null) {
            return factoryModel.removeObserver(observer);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public void setCanvas(final Canvas canvasModel) {
        LOGGER.fine("setCanvas called; EDT=" + javax.swing.SwingUtilities.isEventDispatchThread());
        // remove observers from old model
        if (factoryModel != null) {
            for (final Observer o : controllerObservers) {
                factoryModel.removeObserver(o);
            }
        }

        factoryModel = (Factory) canvasModel;

        // install controller observers on new factory
        installObserversOnFactory(factoryModel);

        if (factoryModel != null) {
            LOGGER.fine("Notifying observers after setCanvas");
            factoryModel.notifyObservers();
        }
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public Canvas getCanvas() {
        return factoryModel;
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

    /**
     * {@inheritDoc}
     */
	@Override
	public boolean isAnimationRunning() {
		return factoryModel != null && factoryModel.isSimulationStarted();
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public CanvasPersistenceManager getPersistenceManager() {
		return persistenceManager;
	}
}
