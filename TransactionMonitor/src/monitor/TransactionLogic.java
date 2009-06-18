/*
 * Klasa zawierająca logikę całej transakcji rozproszonej. Deleguje logikę
 * podtransakcji bezpośrednio do obiektów klas ComponentTransaction. Podczas
 * zarządzania podtransakcjami używa obiektu klasy TransactionManager.
 */
package monitor;

import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author gofer
 */
public class TransactionLogic {

    private Hashtable componentTransactionList = null;
    private Vector dbConnectionList = new Vector(10);

    public Vector getDbConnectionList() {
        return dbConnectionList;
    }
    private static TransactionLogic instance = null;

    protected TransactionLogic() {
        loadDefaultDbConnections();
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

    public boolean addDbConnection(String driver, String url, String user, String password, String desc, String type) {
        DBConnectionData tmpConn = new DBConnectionData(driver, url, user, password, desc, type);
        try {
            this.dbConnectionList.add(tmpConn);
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }
    ;

    public void loadDefaultDbConnections() {
        // Baza1 MySQL 5
        String driver1 = "com.mysql.jdbc.Driver";
        String url1 = "jdbc:mysql://sql.ares-system.nazwa.pl:3305/ares-system_6";
        String user1 = "ares-system_6";
        String password1 = "TEsttest1";
        String desc1 = "MySql Ares6";
        String type1 = "";
        this.addDbConnection(driver1, url1, user1, password1, desc1, type1);

        // Baza2 MySQL 5
        String driver2 = "com.mysql.jdbc.Driver";
        String url2 = "jdbc:mysql://sql.ares-system.nazwa.pl:3305/ares-system_7";
        String user2 = "ares-system_7";
        String password2 = "TEsttest1";
        String desc2 = "MySql Ares7";
        String type2 = "";
        this.addDbConnection(driver2, url2, user2, password2, desc2, type2);

        // Baza3 PostgreSQL 8.3
        String driver3 = "org.postgresql.Driver";
        String url3 = "jdbc:postgresql://sql.ares-system.nazwa.pl:5433/ares-system_8";
        String user3 = "ares-system_8";
        String password3 = "TEsttest1";
        String desc3 = "PostgreSql Ares8";
        String type3 = "";
        this.addDbConnection(driver3, url3, user3, password3,desc3,type3);

        // Baza1 MySQL 5
        String driver4 = "com.mysql.jdbc.Driver";
        String url4 = "jdbc:mysql://sql.ares-system.nazwa.pl:3305/ares-system_6";
        String user4 = "ares-system_6";
        String password4 = "TEsttest1";
        String desc4 = "MySql Ares6 XA";
        String type4 = "XA";
        this.addDbConnection(driver4, url4, user4, password4, desc4, type4);

        // Baza2 MySQL 5
        String driver5 = "com.mysql.jdbc.Driver";
        String url5 = "jdbc:mysql://sql.ares-system.nazwa.pl:3305/ares-system_7";
        String user5 = "ares-system_7";
        String password5 = "TEsttest1";
        String desc5 = "MySql Ares7 XA";
        String type5 = "XA";
        this.addDbConnection(driver5, url5, user5, password5, desc5, type5);
    }
}
