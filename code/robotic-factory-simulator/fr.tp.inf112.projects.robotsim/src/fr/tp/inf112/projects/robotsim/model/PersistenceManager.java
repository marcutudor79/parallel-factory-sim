package fr.tp.inf112.projects.robotsim.model;

/* Java related packages */
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/* Robotsim related packages */
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.model.impl.AbstractCanvasPersistenceManager;

public class PersistenceManager extends AbstractCanvasPersistenceManager {

    /* Used to connect to the persistence server */
    private PersistenceClient persistenceClient = null;

    Logger LOGGER = Logger.getLogger(PersistenceManager.class.getName());

    public PersistenceManager(final CanvasChooser canvasChooser, String netAddr, int port) {
        super(canvasChooser);

        this.persistenceClient = new PersistenceClient(netAddr, port);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Canvas read(final String canvasId) {
        Canvas canvas = null;

        /* read canvas from the server using the persistenceClient */
        try {
            canvas = this.persistenceClient.retrieveCanvas(canvasId);
        } catch (IOException e) {
            LOGGER.severe("Failed to retrieve canvas " + canvasId + " from server");
        }

        return canvas;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persist(Canvas canvasModel) {
        /* save the canvas to the server using persistenceClient */
        try {
            this.persistenceClient.saveCanvas(canvasModel);
        } catch (Exception e) {
            LOGGER.severe("Failed to persist canvas " + canvasModel.getId() + " to server");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override // TODO: Make it call a server
    public boolean delete(final Canvas canvasModel)
    throws IOException {
        final File canvasFile = new File(canvasModel.getId());

        return canvasFile.delete();
    }
}
