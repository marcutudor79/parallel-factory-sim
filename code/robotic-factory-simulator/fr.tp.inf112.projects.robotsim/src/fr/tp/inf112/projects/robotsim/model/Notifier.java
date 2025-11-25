package fr.tp.inf112.projects.robotsim.model;

import java.util.List;
import java.util.ArrayList;

import fr.tp.inf112.projects.canvas.controller.Observer;

public class Notifier implements FactoryModelChangedNotifier {

    private final transient List<Observer> observers = new ArrayList<>();

    /* Used for deserialization */
    public Notifier() {
        // empty constructor
    }

    /* Get the list of observers */
    public List<Observer> getObservers() {
		return observers;
	}

    @Override
    public boolean addObserver(final Observer observer) {
        if (observer == null) return false;
        return observers.add(observer);
    }

    @Override
    public boolean removeObserver(final Observer observer) {
        if (observer == null) return false;
        return observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.modelChanged();
        }
    }
}
