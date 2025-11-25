/**
 * Represents a notifier for changes in the factory model.
*
* @author Marculescu Tudor
*/
package fr.tp.inf112.projects.robotsim.model;

import fr.tp.inf112.projects.canvas.controller.Observer;
import java.util.List;

public interface FactoryModelChangedNotifier {

    /**
     *  Notifies all registered observers about changes in the factory model.
     */
    void notifyObservers();

    /**
     *  Adds observers to the notifier.
     */
    boolean addObserver(Observer observer);

    /**
     *  Removes observers from the notifier.
     */
    boolean removeObserver(Observer observer);

    /**
     * Retrieves the list of registered observers.
     */
    List<Observer> getObservers();
}
