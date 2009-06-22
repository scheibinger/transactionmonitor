/*
 * Klasa zawierająca logikę całej transakcji rozproszonej. Deleguje logikę
 * podtransakcji bezpośrednio do obiektów klas ComponentTransaction. Podczas
 * zarządzania podtransakcjami używa obiektu klasy TransactionManager.
 */
package monitor;


import java.util.Hashtable;
import java.util.Vector;
import monitor.MyLogger.MyLogger;
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
		MyLogger.log("Początek transakcji.");
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
        if(allOK) {
			MyLogger.log("Transakcja zakończyła się sukcesem.");
			commitTransaction();
		}
        else {
			MyLogger.log("Transakcja zakończyła się porażką.");
			abortTransaction();
		}

		MyLogger.log("Koniec transakcji.");
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
        this.adapterList.clear();
        this.componentTransactionList.clear();
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

        // MySql Ares6 XA
        String driver4 = "com.mysql.jdbc.Driver";
        String url4 = "jdbc:mysql://sql.ares-system.nazwa.pl:3305/ares-system_6";
        String user4 = "ares-system_6";
        String password4 = "TEsttest1";
        String desc4 = "MySql Ares6 XA";
        String dbType4 = "MySql";
        String protocolType4 = "XA";
        this.addDbConnection(driver4, url4, user4, password4, desc4, dbType4, protocolType4);

        // MySql Ares6 XA Localhost
        String driver9 = "com.mysql.jdbc.Driver";
        String url9 = "jdbc:mysql://localhost:3306/ares-system_6";
        String user9 = "ares-system_6";
        String password9 = "TEsttest1";
        String desc9 = "MySql Ares6 XA Localhost";
        String dbType9 = "MySql";
        String protocolType9 = "XA";
        this.addDbConnection(driver9, url9, user9, password9, desc9, dbType9, protocolType9);

        // MySql Ares6
        String driver1 = "com.mysql.jdbc.Driver";
        String url1 = "jdbc:mysql://sql.ares-system.nazwa.pl:3305/ares-system_6";
        String user1 = "ares-system_6";
        String password1 = "TEsttest1";
        String desc1 = "MySql Ares6";
        String dbType1 = "MySql";
        String protocolType1 = ""; 
        this.addDbConnection(driver1, url1, user1, password1, desc1, dbType1, protocolType1);

        // MySql Ares6 Localhost
        String driver10 = "com.mysql.jdbc.Driver";
        String url10 = "jdbc:mysql://localhost:3306/ares-system_6";
        String user10 = "ares-system_6";
        String password10 = "TEsttest1";
        String desc10 = "MySql Ares6 Localhost";
        String dbType10 = "MySql";
        String protocolType10 = "";
        this.addDbConnection(driver10, url10, user10, password10, desc10, dbType10, protocolType10);

        // MySql Ares7
        String driver2 = "com.mysql.jdbc.Driver";
        String url2 = "jdbc:mysql://sql.ares-system.nazwa.pl:3305/ares-system_7";
        String user2 = "ares-system_7";
        String password2 = "TEsttest1";
        String desc2 = "MySql Ares7";
        String dbType2 = "MySql";
        String protocolType2 = "";
        this.addDbConnection(driver2, url2, user2, password2, desc2, dbType2, protocolType2);

        // Baza MySql Ares7 Localhost
        String driver8 = "com.mysql.jdbc.Driver";
        String url8 = "jdbc:mysql://localhost:3306/ares-system_7";
        String user8 = "ares-system_7";
        String password8 = "TEsttest1";
        String desc8 = "MySql Ares7 Localhost";
        String dbType8 = "MySql";
        String protocolType8 = "";
        this.addDbConnection(driver8, url8, user8, password8, desc8, dbType8, protocolType8);

        // PostgreSql Ares8
        /*String driver3 = "org.postgresql.Driver";
        String url3 = "jdbc:postgresql://sql.ares-system.nazwa.pl:5433/ares-system_8";
        String user3 = "ares-system_8";
        String password3 = "TEsttest1";
        String desc3 = "PostgreSql Ares8";
        String dbType3 = "PostgreSql";
        String protocolType3 = "";
        this.addDbConnection(driver3, url3, user3, password3,desc3,dbType3, protocolType3);
        */

		// PostgreSql Ares8 XA
        String driver6 = "org.postgresql.Driver";
        String url6 = "jdbc:postgresql://sql.ares-system.nazwa.pl:5433/ares-system_8";
        String user6 = "ares-system_8";
        String password6 = "TEsttest1";
        String desc6 = "PostgreSql Ares8 XA";
        String dbType6 = "PostgreSql";
        String protocolType6 = "XA";
        this.addDbConnection(driver6, url6, user6, password6,desc6,dbType6, protocolType6);

        // PostgreSql Ares8 XA Localhost
        String driver11 = "org.postgresql.Driver";
        String url11 = "jdbc:postgresql://localhost:5432/ares-system_8";
        String user11 = "ares-system_8";
        String password11 = "TEsttest1";
        String desc11 = "PostgreSql Ares8 XA Localhost";
        String dbType11 = "PostgreSql";
        String protocolType11 = "XA";
        this.addDbConnection(driver11, url11, user11, password11,desc11,dbType11, protocolType11);

        // MySql Ares7 XA
        String driver5 = "com.mysql.jdbc.Driver";
        String url5 = "jdbc:mysql://sql.ares-system.nazwa.pl:3305/ares-system_7";
        String user5 = "ares-system_7";
        String password5 = "TEsttest1";
        String desc5 = "MySql Ares7 XA";
        String dbType5 = "MySql";
        String protocolType5 = "XA";
        this.addDbConnection(driver5, url5, user5, password5, desc5, dbType5, protocolType5);

        // MySql Ares7 XA Localhost
        String driver7 = "com.mysql.jdbc.Driver";
        String url7 = "jdbc:mysql://localhost:3306/ares-system_7";
        String user7 = "ares-system_7";
        String password7 = "TEsttest1";
        String desc7 = "MySql Ares7 XA Localhost";
        String dbType7 = "MySql";
        String protocolType7 = "XA";
        this.addDbConnection(driver7, url7, user7, password7, desc7, dbType7, protocolType7);
    }
}
