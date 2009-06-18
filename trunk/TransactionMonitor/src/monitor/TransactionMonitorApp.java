/*
 * Główna klasa zawierająca aplikację klienta
 *
 * TransactionMonitorApp.java
 */
package monitor;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class TransactionMonitorApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        show(new TransactionMonitorView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of TransactionMonitorApp
     */
    public static TransactionMonitorApp getApplication() {
        return Application.getInstance(TransactionMonitorApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(TransactionMonitorApp.class, args);
        TransactionLogic.getInstance();

    }

    public void loadDefaultDbConnections() {
        // Baza1 MySQL 5
        String driver1 = "com.mysql.jdbc.Driver";
        String url1 = "jdbc:mysql://sql.ares-system.nazwa.pl:3305/ares-system_6";
        String user1 = "ares-system_6";
        String password1 = "TEsttest1";
        TransactionLogic.getInstance().addDbConnection(driver1, url1, user1, password1);

        // Baza2 MySQL 5
        String driver2 = "com.mysql.jdbc.Driver";
        String url2 = "jdbc:mysql://sql.ares-system.nazwa.pl:3305/ares-system_7";
        String user2 = "ares-system_7";
        String password2 = "TEsttest1";
        TransactionLogic.getInstance().addDbConnection(driver2, url2, user2, password2);

        // Baza3 PostgreSQL 8.3
        String driver3 = "org.postgresql.Driver";
        String url3 = "jdbc:postgresql://sql.ares-system.nazwa.pl:5433/ares-system_8";
        String user3 = "ares-system_8";
        String password3 = "TEsttest1";
        TransactionLogic.getInstance().addDbConnection(driver3, url3, user3, password3);
    }
}
