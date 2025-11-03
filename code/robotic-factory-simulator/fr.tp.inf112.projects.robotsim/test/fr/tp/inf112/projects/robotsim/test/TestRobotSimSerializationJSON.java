package fr.tp.inf112.projects.robotsim.test;

/* Java related packages */
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.logging.Logger;

/* JUnit related packages */
import org.junit.jupiter.api.Test;

/* Jackson related packages */
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.core.JsonProcessingException;

/* RobotSim related packages */
import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;
import fr.tp.inf112.projects.robotsim.model.Area;
import fr.tp.inf112.projects.robotsim.model.Component;
import fr.tp.inf112.projects.robotsim.model.Door;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.Machine;
import fr.tp.inf112.projects.robotsim.model.Room;

public class TestRobotSimSerializationJSON {

	private final ObjectMapper objectMapper;
    private final static Logger LOGGER = Logger.getLogger(TestRobotSimSerializationJSON.class.getName());

	public TestRobotSimSerializationJSON() {
		this.objectMapper = new ObjectMapper();

        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
            .allowIfSubType(PositionedShape.class.getPackageName())
            .allowIfSubType(Component.class.getPackageName())
            .allowIfSubType(BasicVertex.class.getPackageName())
            .allowIfSubType(ArrayList.class.getName())
            .allowIfSubType(LinkedHashSet.class.getName())
            .build();

        this.objectMapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);
	}

	@Test
	public void testSerialization()
	throws JsonProcessingException {

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

	    final String factoryAsJsonString = objectMapper.writeValueAsString(factory);
	    LOGGER.info(factoryAsJsonString);
	    final Factory roundTrip = objectMapper.readValue(factoryAsJsonString, Factory.class);
        LOGGER.info(roundTrip.toString());
	}
}
