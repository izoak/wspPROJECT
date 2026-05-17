package observer;

public interface GradeSubject {
    void addObserver(GradeObserver observer);
    void removeObserver(GradeObserver observer);
    void notifyObservers();
}
