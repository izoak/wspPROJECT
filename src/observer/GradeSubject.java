package observer;

/**
 * Subject interface for the Observer pattern.
 * Mark class implements this to notify observers when grades change.
 */
public interface GradeSubject {
    void addObserver(GradeObserver observer);
    void removeObserver(GradeObserver observer);
    void notifyObservers();
}
