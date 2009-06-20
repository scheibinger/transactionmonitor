/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package monitor;

import java.sql.SQLException;
import java.sql.Statement;
import monitor.queryobject.QueryObject;

/**
 * Klasa przechowująca dane pojedynczej operacji.
 *
 * @author ASUS
 */
public class Operation {

    public QueryObject query;
    public DBConnectionData dbcd;
    boolean success = true;

    public Operation() {
    }

    public Operation(QueryObject qo) {
        this.query = qo;
    }

    public Operation(QueryObject qo, DBConnectionData dbcd) {
        this.query = qo;
        this.dbcd = dbcd;
    }

    public void setQueryObject(QueryObject qo) {
    }

    public QueryObject getQueryObject() {
        return query;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean execute(Statement stmt) throws SQLException {
        this.success = stmt.execute(this.query.getQuery());
        return this.success;
    }
}
