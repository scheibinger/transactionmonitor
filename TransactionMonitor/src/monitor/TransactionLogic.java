/*
 * Klasa zawierająca logikę całej transakcji rozproszonej. Deleguje logikę
 * podtransakcji bezpośrednio do obiektów klas ComponentTransaction. Podczas
 * zarządzania podtransakcjami używa obiektu klasy TransactionManager.
 */
package monitor;

import java.lang.reflect.Array;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author gofer
 */
public class TransactionLogic {

    private Hashtable componentTransactionList = null;
    private Vector dbConnectionList = null;
    private static TransactionLogic instance = null;

    protected TransactionLogic() {
        dbConnectionList = new Vector();
    }
    ;

    public static TransactionLogic getInstance() {
        if (instance == null) {
            instance = new TransactionLogic();
        }
        return instance;
    }

    public boolean openTransaction() {
        this.componentTransactionList = new Hashtable();
        return false;
    }
    ;

    public boolean startTransaction() {
        return false;
    }
    ;

    public boolean commitTransaction() {
        return false;
    }
    ;

    public boolean abortTransaction() {
        return false;
    }
    ;

    public boolean addAtomicTransaction() {
        return false;
    }
    ;

    public boolean restartTransaction() {
        return false;
    }
    ;
    public boolean addDbConnection(String driver,String url, String user, String password){
        DBConnectionData tmpConn=new DBConnectionData(driver, url, user, password);
        dbConnectionList.add(tmpConn);
        return false;
    }
    ;
}
