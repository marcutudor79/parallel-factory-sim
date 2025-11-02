package fr.tp.inf112.projects.robotsim.model;

/* Java related packages */
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Logger;

/* Robotsim related packages */
import fr.tp.inf112.projects.canvas.model.Canvas;

public class PersistenceClient {

    private int           port          = -1;
    private InetAddress   netAddr       = null;
    private SocketAddress socketAddr    = null;

    Logger LOGGER = Logger.getLogger(PersistenceManager.class.getName());

    /* Constructor for the PersistenceClient
     *
     *  @param netAddr The network address of the server
     *  @param port    The port number of the server
     */
    public PersistenceClient(String netAddr, int port) {

        // setup the port number
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Port number must be between 0 and 65535.");
        }
        this.port = port;

        // try to get the InetAddress from the provided string
        try {
            this.netAddr = InetAddress.getByName(netAddr);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // setup the socket address
        this.socketAddr = new InetSocketAddress(netAddr, port);
    }

    /*  Close an open socket
     *
     *  @param socket The socket to close
     */
    private void closeSocket(Socket socket)
    {
        try {
            socket.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Retrieve a canvas from the server by id
     *
     * @param canvasId The id of the canvas to retrieve
     * @return The canvas object retrieved from the server
     */
    public Canvas retrieveCanvas(final String canvasId)
    throws IOException {
        OutputStream outStream    = null;
        Socket       serverSocket = new Socket();
        Canvas       canvas       = null;

        // Should be working since the socket is connected in the constructor
        LOGGER.info("Connecting to server " + this.netAddr.toString() + " on port " + this.port + "...");
        serverSocket.connect(this.socketAddr, 1000);
        outStream = serverSocket.getOutputStream();
        LOGGER.info("Connected to server.");

        // send the canvasId to the server
        LOGGER.info("Requesting canvas with id " + canvasId + "...");
        final ObjectOutputStream objOutStream = new ObjectOutputStream(outStream);
        objOutStream.writeObject(canvasId);
        objOutStream.flush();

        // connect to the input stream of the socket
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(serverSocket.getInputStream()));

        // try to read the object
        try {
            canvas = (Canvas) ois.readObject();
        }
        catch (ClassNotFoundException e) {
            closeSocket(serverSocket);
            throw new RuntimeException(e);
        }
        LOGGER.info("Canvas with id " + canvasId + " received.");

        closeSocket(serverSocket);
        return canvas;
    }

    /*
     *  Retrieve a Factory from the server by id
     *
     *  @param factoryId The id of the factory to retrieve
     */
    public Factory retrieveFactory(final String factoryId)
    throws IOException {
        Canvas canvas = retrieveCanvas(factoryId);
        if (canvas instanceof Factory) {
            return (Factory) canvas;
        } else {
            LOGGER.warning("Retrieved canvas is not a Factory.");
            return null;
        }
    }

    /* Save a canvas to the server
     *
     * @param canvasModel The canvas model to save
     * @return true if the canvas was saved successfully, false otherwise
     */
    public boolean saveCanvas(Canvas canvasModel) {
        try (Socket serverSocket = new Socket()) {
            LOGGER.info("Connecting to server " + this.netAddr.toString() + " on port " + this.port + "...");
            serverSocket.connect(this.socketAddr, 1000);
            LOGGER.info("Connected to server.");

            try (OutputStream outStream = serverSocket.getOutputStream();
                ObjectOutputStream objOutputStream = new ObjectOutputStream(outStream)) {

                LOGGER.info("Sending canvas with id " + canvasModel.getId() + " to server...");
                objOutputStream.writeObject(canvasModel);
                objOutputStream.flush();
                LOGGER.info("Canvas with id " + canvasModel.getId() + " sent to server.");
                return true;
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to save canvas " + canvasModel.getId() + " to server");
            return false;
        }
    }
}
