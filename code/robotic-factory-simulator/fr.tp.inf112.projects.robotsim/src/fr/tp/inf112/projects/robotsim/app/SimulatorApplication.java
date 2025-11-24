package fr.tp.inf112.projects.robotsim.app;

import java.awt.Component;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;
import fr.tp.inf112.projects.canvas.view.CanvasViewer;
import fr.tp.inf112.projects.robotsim.model.Area;
import fr.tp.inf112.projects.robotsim.model.Battery;
import fr.tp.inf112.projects.robotsim.model.ChargingStation;
import fr.tp.inf112.projects.robotsim.model.Conveyor;
import fr.tp.inf112.projects.robotsim.model.Door;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.HardcodedFileCanvasChooser;
import fr.tp.inf112.projects.robotsim.model.Machine;
import fr.tp.inf112.projects.robotsim.model.Robot;
import fr.tp.inf112.projects.robotsim.model.Room;
import fr.tp.inf112.projects.robotsim.model.path.CustomDijkstraFactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.path.FactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.path.JGraphTDijkstraFactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.shapes.BasicPolygonShape;
import fr.tp.inf112.projects.robotsim.model.shapes.CircularShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;
import fr.tp.inf112.projects.robotsim.model.PersistenceManager;

public class SimulatorApplication {

    public static void main(String[] args) {

        /* Instantiate logger */
        final Logger LOGGER = Logger.getLogger(SimulatorApplication.class.getName());
        LOGGER.info("Starting the robot simulator..");
        LOGGER.config("With parameters " + Arrays.toString(args) + ".");

        /* Create factory with rooms model */
        final Factory factory  = new Factory(200, 200, "Simple Test Puck Factory");

        /* Create room1 */
        final Room room1       = new Room(factory, new RectangularShape(20, 20,
                                          75, 75), "Production Room 1");
                                 new Door(room1, Room.WALL.BOTTOM, 10, 20, true,
                                          "Entrance");
        final Area area1       = new Area(room1, new RectangularShape(35, 35,
                                          50, 50), "Production Area 1");
        final Machine machine1 = new Machine(area1, new RectangularShape(50, 50,
                                          15, 15), "Machine 1");

        /* Create room2 */
        final Room room2       = new Room(factory, new RectangularShape( 120, 22,
                                          75, 75 ), "Production Room 2");
                                 new Door(room2, Room.WALL.LEFT, 10, 20, true,
                                          "Entrance");
        final Area area2       = new Area(room2, new RectangularShape( 135, 35,
                                          50, 50 ), "Production Area 1");
        final Machine machine2 = new Machine(area2, new RectangularShape( 150, 50,
                                          15, 15 ), "Machine 1");

        /* Create conveyor belt in the factory */
        final int baselineSize = 3;
        final int xCoordinate = 10;
        final int yCoordinate = 165;
        final int width =  10;
        final int height = 30;
        final BasicPolygonShape conveyorShape = new BasicPolygonShape();
        conveyorShape.addVertex(new BasicVertex(xCoordinate, yCoordinate));
        conveyorShape.addVertex(new BasicVertex(xCoordinate + width, yCoordinate));
        conveyorShape.addVertex(new BasicVertex(xCoordinate + width, yCoordinate + height - baselineSize));
        conveyorShape.addVertex(new BasicVertex(xCoordinate + width + baselineSize, yCoordinate + height - baselineSize));
        conveyorShape.addVertex(new BasicVertex(xCoordinate + width + baselineSize, yCoordinate + height));
        conveyorShape.addVertex(new BasicVertex(xCoordinate - baselineSize, yCoordinate + height));
        conveyorShape.addVertex(new BasicVertex(xCoordinate - baselineSize, yCoordinate + height - baselineSize));
        conveyorShape.addVertex(new BasicVertex(xCoordinate, yCoordinate + height - baselineSize));

        /* Create a charging room */
        final Room chargingRoom               = new Room(factory, new RectangularShape(125,
                                                         125, 50, 50), "Charging Room");
                                                new Door(chargingRoom, Room.WALL.RIGHT, 10,
                                                         20, false, "Entrance");
        final ChargingStation chargingStation = new ChargingStation(factory, new RectangularShape(150,
                                                         145, 15, 15), "Charging Station");

        /* Create Djisktra path finder to be used by robots */
        final FactoryPathFinder jgraphPahtFinder = new JGraphTDijkstraFactoryPathFinder(factory, 5);

        /* Create robots */
        final Robot robot1 = new Robot(factory, jgraphPahtFinder, new CircularShape(5, 5, 2),
                                       new Battery(10), "Robot 1");
        robot1.addTargetComponent(machine1);
        robot1.addTargetComponent(machine2);
        robot1.addTargetComponent(new Conveyor(factory, conveyorShape, "Conveyor 1"));
        robot1.addTargetComponent(chargingStation);

        final FactoryPathFinder customPathFinder = new CustomDijkstraFactoryPathFinder(factory, 5);
        final Robot robot2 = new Robot(factory, customPathFinder, new CircularShape(45, 5, 2), new Battery(10), "Robot 2");
        robot2.addTargetComponent(chargingStation);
        robot2.addTargetComponent(machine1);
        robot2.addTargetComponent(machine2);
        robot2.addTargetComponent(new Conveyor(factory, conveyorShape, "Conveyor 1"));

        /* Invoke library for canvas */
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                /* Create canvas chooser */
                final HardcodedFileCanvasChooser canvasChooser =
                new HardcodedFileCanvasChooser("factory", "Puck Factory");

                final RemoteSimulatorController controller =
                new RemoteSimulatorController(new PersistenceManager(canvasChooser, "localhost", 55555), "localhost", "8080", "Puck_Factory_1762554691881.factory");

                /* Uncomment to use local simulator controller */
                /*
                final SimulatorController controller = new SimulatorController(
                new PersistenceManager(canvasChooser, "localhost", 55555));
                */

                /* Add persitence manager here */
                final Component factoryViewer = new CanvasViewer(controller);

                /* This is where the menu is set to the factory viewer */
                canvasChooser.setViewer(factoryViewer);

                // start polling (e.g., every 500ms)
                controller.startRemotePolling(0, 500);
            }
        });

    }
}
