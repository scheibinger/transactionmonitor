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

/**
 *
 * @author Administrator
 */
public class ComponentTransactionAdapterPostgreSqlXA implements TransactionParticipantIF {


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

            stmt.execute("BEGIN");

            for (Operation oper : ct) {
                oper.executeXA(stmt);
            }
            stmt.execute("PREPARE TRANSACTION '" + this.key + "'");
        } catch (SQLException ex) {
            this.success = false;
            Logger.getLogger(ComponentTransactionAdapterMySqlXA.class.getName()).log(Level.SEVERE, null, ex);
        }
        return this.success;
    }


    public boolean commitTransaction() {
        try {
            return connection.createStatement().execute("COMMIT PREPARED '" + this.key + "'");
        } catch (SQLException ex) {
            Logger.getLogger(ComponentTransactionAdapterMySqlXA.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean abortTransaction() {
        try {
            return connection.createStatement().execute("ROLLBACK PREPARED '" + this.key + "'");
        } catch (SQLException ex) {
            Logger.getLogger(ComponentTransactionAdapterMySqlXA.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }


}
