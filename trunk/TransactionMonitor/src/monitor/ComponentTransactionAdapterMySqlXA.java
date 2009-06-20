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
 * @author ASUS
 */
public class ComponentTransactionAdapterMySqlXA implements TransactionParticipantIF {

    Connection connection;
    DBConnectionData dbcd;
    String key;
    boolean success = true;

    public boolean startTransaction(DBConnectionData dbcd, ComponentTransaction ct) {
        this.key = String.valueOf(this.hashCode());

        this.dbcd = dbcd;
        try {
            this.connection = DriverManager.getConnection(this.dbcd.driver, this.dbcd.user, this.dbcd.password);
        } catch (SQLException ex) {
            Logger.getLogger(ComponentTransactionAdapterMySqlXA.class.getName()).log(Level.SEVERE, null, ex);
        }


        Statement stmt;
        try {
            stmt = connection.createStatement();

            this.success &= stmt.execute("xa start " + this.key);
            for (Operation oper : ct) {
                this.success &= oper.execute(stmt);
                if (this.success) break;
            }
            this.success &= stmt.execute("xa end " + this.key);
            this.success &= stmt.execute("xa prepare " + this.key);
        } catch (SQLException ex) {
            Logger.getLogger(ComponentTransactionAdapterMySqlXA.class.getName()).log(Level.SEVERE, null, ex);
        }
        return this.success;
    }


    public boolean commitTransaction() {
        try {
            return connection.createStatement().execute("xa prepare " + this.key);
        } catch (SQLException ex) {
            Logger.getLogger(ComponentTransactionAdapterMySqlXA.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean abortTransaction() {
        try {
            return connection.createStatement().execute("xa prepare " + this.key);
        } catch (SQLException ex) {
            Logger.getLogger(ComponentTransactionAdapterMySqlXA.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
