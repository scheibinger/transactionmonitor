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
    String type;
    String driver;
    String url;
    String user;
    String password;

    public DBConnectionData(String driver, String url, String user, String password, String desc, String type) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
        this.desc = desc;
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public String getType() {
        return type;
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
}
