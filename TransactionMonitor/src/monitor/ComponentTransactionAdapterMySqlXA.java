/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package monitor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class ComponentTransactionAdapterMySqlXA implements TransactionParticipantIF {

    Connection connection;
    DBConnectionData dbcd;

    public boolean init(DBConnectionData dbcd) {
        this.dbcd = dbcd;

        try {
            String driver1 = "com.mysql.jdbc.Driver";
            Class.forName(driver1);
            this.connection = DriverManager.getConnection(this.dbcd.driver, this.dbcd.user, this.dbcd.password);

        } catch (Exception sqle) {
        }
        return false;
    }

    public boolean startTransaction() {

        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean commitTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean abortTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
