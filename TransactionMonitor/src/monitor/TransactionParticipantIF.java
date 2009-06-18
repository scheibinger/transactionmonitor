/*
 * Dzięki temu interfejsowi obiekt klasy TransactionManager może manipulować
 obiektami klas ComponentTransaction pomimo różnych implementacji danych klasach.
 */

package monitor;

/**
 *
 * @author gofer
 */
public interface TransactionParticipantIF {
    public boolean startTransaction();
    public boolean commitTransaction();
    public boolean abortTransaction();
}
