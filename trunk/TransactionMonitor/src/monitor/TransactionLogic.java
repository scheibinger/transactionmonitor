/*
 * Klasa zawierająca logikę całej transakcji rozproszonej. Deleguje logikę
 * podtransakcji bezpośrednio do obiektów klas ComponentTransaction. Podczas
 * zarządzania podtransakcjami używa obiektu klasy TransactionManager.
 */

package monitor;



/**
 *
 * @author gofer
 */
public class TransactionLogic {
    private static TransactionLogic instance = null;
    protected TransactionLogic(){};

    public static TransactionLogic getInstance(){
        if(instance == null){
            instance = new TransactionLogic();
        }
        return instance;
    }
}
