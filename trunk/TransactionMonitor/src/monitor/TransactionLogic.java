/*
 * Klasa zawierająca logikę całej transakcji rozproszonej. Deleguje logikę
 * podtransakcji bezpośrednio do obiektów klas ComponentTransaction. Podczas
 * zarządzania podtransakcjami używa obiektu klasy TransactionManager.
 */
package monitor;


import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import monitor.queryobject.QueryObject;

/**
 *
 * @author gofer
 */
public class TransactionLogic {

    private Hashtable<Object, ComponentTransaction> componentTransactionList = null;
    private Vector<DBConnectionData> dbConnectionList = new Vector<DBConnectionData>();
    private Vector<TransactionParticipantIF> adapterList = new Vector<TransactionParticipantIF>();
    private static TransactionLogic instance = null;



    public Vector getDbConnectionList() {
        return dbConnectionList;
    }

    protected TransactionLogic() {
        loadDefaultDbConnections();
    }

    public static TransactionLogic getInstance() {
        if (instance == null) {
            instance = new TransactionLogic();
        }
        return instance;
    }

    public boolean openTransaction() {
        this.componentTransactionList = new Hashtable<Object, ComponentTransaction>();
        return false;
    }


    /**
     * Funkcja tworzy wszystkie adaptery a następnie wywołujemy start transakcji dla każdego z nich.
     * W zależności od wyników wszystkie transakcją są akceptowane bądź wycofywane.
     * @return boolean Czy globalna transakcja się udała.
     */
    public boolean startTransaction() {
        boolean allOK = true;
        for(DBConnectionData dbcd :dbConnectionList){
            boolean isOK = true;
            if(componentTransactionList.containsKey(dbcd)){
                TransactionParticipantIF adapter = ComponentTransactionAdapterFactory.CreateAdapter(dbcd.getAdapterName());
                isOK = adapter.startTransaction(dbcd,componentTransactionList.get(dbcd));
                adapterList.add(adapter);
            }

            allOK &= isOK ;
            if (!allOK)
                break;
        }
        if(allOK) commitTransaction();
        else abortTransaction();

        return allOK;
    }

    public boolean commitTransaction() {
        for (TransactionParticipantIF adapter : adapterList) {
            adapter.commitTransaction();
        }
        return true;
    }

    public boolean abortTransaction(){
        for (TransactionParticipantIF adapter : adapterList) {
            adapter.abortTransaction();
        }
        return true;
    }

    public boolean addAtomicTransaction(QueryObject qo,DBConnectionData dbcd) {
        
        ComponentTransaction ct = null;
        if(componentTransactionList.containsKey(dbcd)){
            ct = componentTransactionList.get(dbcd);
        }
        else {
            ct = new ComponentTransaction();
            componentTransactionList.put(dbcd, ct);
        }
        ct.addElement(new Operation(qo,dbcd));

        return true;
    }

    public boolean restartTransaction() {
        return false;
    }

    public boolean addDbConnection(String driver, String url, String user, String password, String desc, String dbType, String protocolType) {
        DBConnectionData tmpConn = new DBConnectionData(driver, url, user, password, desc, dbType, protocolType );
        try {
            this.dbConnectionList.add(tmpConn);
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    public void loadDefaultDbConnections() {

                // Baza1 MySQL 5
        String driver4 = "com.mysql.jdbc.Driver";
        String url4 = "jdbc:mysql://sql.ares-system.nazwa.pl:3305/ares-system_6";
        String user4 = "ares-system_6";
        String password4 = "TEsttest1";
        String desc4 = "MySql Ares6 XA";
        String dbType4 = "MySql";
        String protocolType4 = "XA";
        this.addDbConnection(driver4, url4, user4, password4, desc4, dbType4, protocolType4);
        // Baza1 MySQL 5
        String driver1 = "com.mysql.jdbc.Driver";
        String url1 = "jdbc:mysql://sql.ares-system.nazwa.pl:3305/ares-system_6";
        String user1 = "ares-system_6";
        String password1 = "TEsttest1";
        String desc1 = "MySql Ares6";
        String dbType1 = "MySql";
        String protocolType1 = ""; 
        this.addDbConnection(driver1, url1, user1, password1, desc1, dbType1, protocolType1);

        // Baza2 MySQL 5
        String driver2 = "com.mysql.jdbc.Driver";
        String url2 = "jdbc:mysql://sql.ares-system.nazwa.pl:3305/ares-system_7";
        String user2 = "ares-system_7";
        String password2 = "TEsttest1";
        String desc2 = "MySql Ares7";
        String dbType2 = "MySql";
        String protocolType2 = "";
        this.addDbConnection(driver2, url2, user2, password2, desc2, dbType2, protocolType2);

        // Baza3 PostgreSQL 8.3
        String driver3 = "org.postgresql.Driver";
        String url3 = "jdbc:postgresql://sql.ares-system.nazwa.pl:5433/ares-system_8";
        String user3 = "ares-system_8";
        String password3 = "TEsttest1";
        String desc3 = "PostgreSql Ares8";
        String dbType3 = "PostgreSql";
        String protocolType3 = "";
        this.addDbConnection(driver3, url3, user3, password3,desc3,dbType3, protocolType3);



        // Baza2 MySQL 5
        String driver5 = "com.mysql.jdbc.Driver";
        String url5 = "jdbc:mysql://sql.ares-system.nazwa.pl:3305/ares-system_7";
        String user5 = "ares-system_7";
        String password5 = "TEsttest1";
        String desc5 = "MySql Ares7 XA";
        String dbType5 = "MySql";
        String protocolType5 = "XA";
        this.addDbConnection(driver5, url5, user5, password5, desc5, dbType5, protocolType5);
    }
}
