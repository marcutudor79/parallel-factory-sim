package fr.tp.inf112.projects.robotsim.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;
import fr.tp.inf112.projects.canvas.model.Canvas;

import java.lang.Runnable;

public class PersistenceServer implements Runnable {
    private final int port;
    private volatile boolean running = true;
    private ServerSocket serverSocket;
    private FactoryPersistenceManager FactoryPersistenceManager;

    Logger LOGGER = Logger.getLogger(PersistenceServer.class.getName());

    public PersistenceServer(final int port) {
        this.port = port;
        this.FactoryPersistenceManager = new FactoryPersistenceManager(null);
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void run() {
        // open the server socket
        LOGGER.info("Starting PersistenceServer on port " + port + "...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            this.serverSocket = serverSocket;
            while (running) {
                try {
                    LOGGER.info("Waiting for client connection...");
                    Socket client = serverSocket.accept();
                    // handle each connection in its own short-lived thread
                    LOGGER.info("Client connected from " + client.getInetAddress().toString() + ":" + client.getPort());
                    new Thread(() -> handleClient(client)).start();
                } catch (IOException e) {
                    if (running) e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(final Socket client) {
        try (Socket             clientSocket = client;
             ObjectInputStream  objInStream  = new ObjectInputStream (new BufferedInputStream (clientSocket.getInputStream()));
             ObjectOutputStream objOutStream = new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
        ) {
            Object obj = null;

            try {
                obj = objInStream.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            LOGGER.info("Received object of type " + obj.getClass().getName());

            if (obj instanceof String)
            {
                final String canvasId = (String) obj;
                final Canvas canvas   = FactoryPersistenceManager.read(canvasId);
                LOGGER.info("Sending canvas with id " + canvasId + " to client...");
                objOutStream.writeObject(canvas);
                objOutStream.flush();
                LOGGER.info("Canvas with id " + canvasId + " sent to client.");
            }

            else if (obj instanceof Canvas)
            {
                LOGGER.info("Persisting received canvas...");
                final Canvas canvas = (Canvas) obj;
                FactoryPersistenceManager.persist(canvas);
                LOGGER.info("Canvas with id " + canvas.getId() + " persisted.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
