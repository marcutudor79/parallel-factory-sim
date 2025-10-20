package fr.tp.inf112.projects.robotsim.app;

import fr.tp.inf112.projects.robotsim.model.PersistenceServer;

public class SimulatorPersistenceServer {
    public static void main(String[] args) {
        final int port = 55555; // default port

        PersistenceServer server = new PersistenceServer(port);
        new Thread(server).start();
    }
}
