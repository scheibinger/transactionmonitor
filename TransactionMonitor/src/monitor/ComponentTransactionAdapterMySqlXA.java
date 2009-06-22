/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package monitor;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import monitor.MyLogger.MyLogger;

/**
 *
 * @author ASUS
 */
public class ComponentTransactionAdapterMySqlXA implements TransactionParticipantIF {

    Connection connection;
    DBConnectionData dbcd;
    String key;
    boolean success = true;

    public boolean startTransaction(DBConnectionData dbcd, ComponentTransaction ct) {
        this.key = String.valueOf(this.hashCode());
        this.success = true;
        this.dbcd = dbcd;
        try {

            Class.forName(this.dbcd.getDriver());
            this.connection = DriverManager.getConnection(this.dbcd.url, this.dbcd.user, this.dbcd.password);
        } catch (Exception ex) {
            Logger.getLogger(ComponentTransactionAdapterMySqlXA.class.getName()).log(Level.SEVERE, null, ex);
        }


        Statement stmt;
        try {
            stmt = connection.createStatement();

            stmt.execute("xa start \"" + this.key + '"');

            for (Operation oper : ct) {
                oper.executeXA(stmt);

            }
            stmt.execute("xa end \"" + this.key + '"');
            stmt.execute("xa prepare \"" + this.key + '"');
            MyLogger.log("Podtransakcja "+dbcd.getDesc()+" powiodła się!");
        } catch (SQLException ex) {
            this.success = false;
            MyLogger.log("Podtransakcja "+dbcd.getDesc()+" nie powiodła się!");
            Logger.getLogger(ComponentTransactionAdapterMySqlXA.class.getName()).log(Level.SEVERE, null, ex);
        }
        return this.success;
    }


    public boolean commitTransaction() {
        try {
            MyLogger.log("Commit podtransakcji "+dbcd.getDesc());
            return connection.createStatement().execute("xa commit \"" + this.key + '"');
        } catch (SQLException ex) {
            Logger.getLogger(ComponentTransactionAdapterMySqlXA.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean abortTransaction() {
        try {
            MyLogger.log("Rollback podtransakcji "+dbcd.getDesc());
            return connection.createStatement().execute("xa rollback \"" + this.key + '"');
        } catch (SQLException ex) {
            Logger.getLogger(ComponentTransactionAdapterMySqlXA.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }


    public boolean getStatus() {
            return this.dbcd.status;
    }

}
