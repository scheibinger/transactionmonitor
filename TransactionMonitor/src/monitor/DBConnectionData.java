/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package monitor;

/**
 *
 * @author ASUS
 */
public class DBConnectionData {

    String desc;
    String driver;
    String url;
    String user;
    String password;
    String dbType;
    String protocolType;

    public DBConnectionData( String driver, String url, String user, String password, String desc, String dbType, String protocolType) {
        this.desc = desc;
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
        this.dbType = dbType;
        this.protocolType = protocolType;
    }






    public String getDesc() {
        return desc;
    }

    public String getDbType() {
        return dbType;
    }

    public String getProtocolType() {
        return protocolType;
    }


    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getAdapterName(){
        return this.getDbType() + this.getProtocolType();
    }
}
