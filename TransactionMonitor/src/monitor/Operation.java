/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor;

import monitor.queryobject.QueryObject;

/**
 * Klasa przechowująca dane pojedynczej operacji.
 *
 * @author ASUS
 */
public class Operation {
    public QueryObject query;
    public DBConnectionData dbcd;

    public Operation(){};
    public Operation(QueryObject qo, DBConnectionData dbcd){
        this.query = qo;
        this.dbcd = dbcd;
    };

    public void setQueryObject(QueryObject qo){};
    public QueryObject getQueryObject(){return query;};
}
