package fr.tp.inf112.projects.robotsim.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.model.impl.AbstractCanvasPersistenceManager;
import java.net.ServerSocket;
// added for socket connection
import java.net.SocketAddress;
import java.util.logging.Logger;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.Socket;


public class PersistenceManager extends AbstractCanvasPersistenceManager {

    // added for the connection with the server
    private int           port          = -1;
    private InetAddress   netAddr       = null;
    private SocketAddress socketAddr    = null;

    Logger LOGGER = Logger.getLogger(PersistenceManager.class.getName());

    public PersistenceManager(final CanvasChooser canvasChooser, String netAddr, int port) {
        super(canvasChooser);

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

    /*  Function used to close an open socket
     *
     */
    private void closeSocket(Socket socket)
    {
        try {
            socket.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Canvas read(final String canvasId)
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void persist(Canvas canvasModel)
    throws IOException {
        OutputStream outStream    = null;
        Socket       serverSocket = new Socket();

        // Should be working since the socket is connected in the constructor
        LOGGER.info("Connecting to server " + this.netAddr.toString() + " on port " + this.port + "...");
        serverSocket.connect(this.socketAddr, 1000);
        outStream = serverSocket.getOutputStream();
        LOGGER.info("Connected to server.");

        // send the canvas to the server
        LOGGER.info("Sending canvas with id " + canvasModel.getId() + " to server...");
        final ObjectOutputStream objOutputStream = new ObjectOutputStream(outStream);
        objOutputStream.writeObject(canvasModel);
        objOutputStream.flush();
        LOGGER.info("Canvas with id " + canvasModel.getId() + " sent to server.");
        closeSocket(serverSocket);
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
