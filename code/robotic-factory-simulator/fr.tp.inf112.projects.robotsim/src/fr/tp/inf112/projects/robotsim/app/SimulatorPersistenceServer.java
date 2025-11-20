package fr.tp.inf112.projects.robotsim.app;

import fr.tp.inf112.projects.robotsim.model.PersistenceServer;

public class SimulatorPersistenceServer {
    public static void main(String[] args) {
        final int port       = 55555; // default port
        final String baseDir = "./../robotsim.persistance"; // default base directory

        PersistenceServer server = new PersistenceServer(port, baseDir);
        new Thread(server).start();
    }
}
