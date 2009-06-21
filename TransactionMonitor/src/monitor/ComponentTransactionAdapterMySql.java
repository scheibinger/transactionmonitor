/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import monitor.queryobject.QueryObject;

/**
 *
 * @author marcin
 */
public class ComponentTransactionAdapterMySql  implements TransactionParticipantIF{

    Connection connection;
    DBConnectionData dbcd;
    String key;
    ComponentTransaction ct;
    boolean success = true;

    public boolean commitTransaction() {

        Statement stmt;
        try {
            stmt = connection.createStatement();
            for (Operation oper : ct){
                QueryObject qo = oper.getQueryObject();
                if (!qo.getQueryType().equals(QueryObject.INSERT)){
                    stmt.execute("DROP TABLE "+qo.getTableName()+"_temp;");
                }
            }
            stmt.executeUpdate("UNLOCK TABLES");
        } catch (SQLException ex) {
            this.success = false;
            Logger.getLogger(ComponentTransactionAdapterMySql.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    public boolean abortTransaction() {
        Statement stmt;
        try {
            stmt = connection.createStatement();
            for (Operation oper : ct) {
                if (oper.success){
   //                 if (oper.getQueryObject().equals(QueryObject.UPDATE))
    //                stmt.execute("UNLOCK TABLES");
                    Vector rollback = oper.getQueryObject().getRollbackQueries();
                    for (int i=0; i< rollback.size(); i++)
                    stmt.execute((String)rollback.get(i));
                }
            }
  //          stmt.executeUpdate("UNLOCK TABLES");
        } catch (SQLException ex) {
            this.success = false;
            Logger.getLogger(ComponentTransactionAdapterMySql.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    public boolean startTransaction(DBConnectionData dbcd, ComponentTransaction ct) {
        this.ct = ct;
        this.key = String.valueOf(this.hashCode());
        this.success = true;
        this.dbcd = dbcd;
        try {
            Class.forName(this.dbcd.getDriver());
            this.connection = DriverManager.getConnection(this.dbcd.url, this.dbcd.user, this.dbcd.password);
        } catch (Exception ex) {
            Logger.getLogger(ComponentTransactionAdapterMySql.class.getName()).log(Level.SEVERE, null, ex);
        }

        Statement stmt;
        try {
            stmt = connection.createStatement();

            for (Operation oper : ct) {
                oper.execute(stmt);
            }

        } catch (SQLException ex) {
            this.success = false;
            Logger.getLogger(ComponentTransactionAdapterMySql.class.getName()).log(Level.SEVERE, null, ex);
        }

        return this.success;
    }

}
