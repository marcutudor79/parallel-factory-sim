package fr.tp.inf112.projects.robotsim.model;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.tp.inf112.projects.canvas.model.Figure;
import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.canvas.model.Shape;

public abstract class Component implements Figure, Serializable {

	private static final long serialVersionUID = -5960950869184030220L;

	private String id;

    @JsonBackReference // manage bi-directional references during serialization
	private final Factory factory;

	private final PositionedShape positionedShape;

	private final String name;

    /* Used by Jackson serialization */
    public Component()
    {
        super();

        // initialize final/transient fields so Jackson can use the no-arg constructor
        this.factory = null;
        this.positionedShape = null;
        this.name = null;
    }

	protected Component(final Factory factory,
						final PositionedShape shape,
						final String name) {
		this.factory = factory;
		this.positionedShape = shape;
		this.name = name;

		if (factory != null) {
			factory.addComponent(this);
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public PositionedShape getPositionedShape() {
		return positionedShape;
	}

    @JsonIgnore // derived from positionedShape, no need to serialize
	public Position getPosition() {
		return getPositionedShape().getPosition();
	}

	protected Factory getFactory() {
		return factory;
	}

	@Override
    @JsonIgnore // derived from positionedShape
	public int getxCoordinate() {
		return getPositionedShape().getxCoordinate();
	}

	protected boolean setxCoordinate(int xCoordinate) {
		if ( getPositionedShape().setxCoordinate( xCoordinate ) ) {
			notifyObservers();

			return true;
		}

		return false;
	}

	@Override
    @JsonIgnore // derived from positionedShape
	public int getyCoordinate() {
		return getPositionedShape().getyCoordinate();
	}

	protected boolean setyCoordinate(final int yCoordinate) {
		if (getPositionedShape().setyCoordinate(yCoordinate) ) {
			notifyObservers();

			return true;
		}

		return false;
	}

	protected void notifyObservers() {
		getFactory().notifyObservers();
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [name=" + name + " xCoordinate=" + getxCoordinate() + ", yCoordinate=" + getyCoordinate()
				+ ", shape=" + getPositionedShape();
	}

    @JsonIgnore // derived from positionedShape
	public int getWidth() {
		return getPositionedShape().getWidth();
	}

    @JsonIgnore // derived from positionedShape
	public int getHeight() {
		return getPositionedShape().getHeight();
	}

	public boolean behave() {
		return false;
	}

    @JsonIgnore // Ignore default value
	public boolean isMobile() {
		return false;
	}

	public boolean overlays(final Component component) {
		return overlays(component.getPositionedShape());
	}

	public boolean overlays(final PositionedShape shape) {
		return getPositionedShape().overlays(shape);
	}

	public boolean canBeOverlayed(final PositionedShape shape) {
		return false;
	}

	@Override
    @JsonIgnore // default value
	public Style getStyle() {
		return ComponentStyle.DEFAULT;
	}

	@Override
    @JsonGetter("positionedShape")
	public Shape getShape() {
		return getPositionedShape();
	}

    @JsonIgnore // derived from factory and transient; double trouble
	public boolean isSimulationStarted() {
		return getFactory().isSimulationStarted();
	}
}
