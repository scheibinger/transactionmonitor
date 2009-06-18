/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor;

import monitor.queryobject.QueryObject;

/**
 *
 * @author Kubutek
 */
public class OperationDataObject {

    private QueryObject queryObject;
    private String dbURL;
    private String dbUser;
    private String dbPassword;
    private String dbType;

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getDbURL() {
        return dbURL;
    }

    public void setDbURL(String dbURL) {
        this.dbURL = dbURL;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public QueryObject getQueryObject() {
        return queryObject;
    }

    public void setQueryObject(QueryObject queryObject) {
        this.queryObject = queryObject;
    }



}
