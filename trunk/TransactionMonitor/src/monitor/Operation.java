/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package monitor;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
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

    public boolean executeXA(Statement stmt) throws SQLException {
        stmt.execute(this.query.getQuery());
        return this.success;
    }
    public boolean execute(Statement stmt ) throws SQLException {
        String queryType = this.query.getQueryType();
        String queryContent = this.query.getQuery();
        //lockowanie tabeli
        try {
      //  stmt.executeUpdate("LOCK TABLES "+query.getTableName()+" WRITE");
        if (queryType.equals(QueryObject.INSERT)){

            stmt.execute(queryContent,Statement.RETURN_GENERATED_KEYS);

            //pobranie primary keys wstawionych rekordow
            Vector keysVector = new Vector();
            ResultSet keys  = stmt.getGeneratedKeys();
            if (keys != null){
                while (keys.next()){
                    keysVector.add(keys.getString(1));
                }
            }
            //pobranie nazwy kolumny ktora jest primary key
            DatabaseMetaData dbmb = stmt.getConnection().getMetaData();
            ResultSet primKey = dbmb.getPrimaryKeys(null, null, query.getTableName());
            primKey.first();
            String primKeyColName = primKey.getString("COLUMN_NAME");

            //generowanie zapytania kompensujacego
            query.generateInsertRollbackQuery(primKeyColName, keysVector);
        } else {
            //tworzenie tabeli tymczasowej z usuwanymi,zmienianymi rekordami - START
            QueryObject selectQuery = new QueryObject(QueryObject.SELECT,query.getTableName(),
                                                      query.getCriteria(),null);
            //sprawdzenie czy sa rekordy spelniajace podane kryteria
            stmt.execute(selectQuery.getQuery());
            ResultSet srs = stmt.getResultSet();
            if (srs != null){
                stmt.execute("CREATE TABLE "+query.getTableName()+"_temp AS "+selectQuery.getQuery());
            }
            //tworzenie tabeli tymczasowej - KONIEC

            //wykonanie wlasciwego zapytania
            stmt.execute(query.getQuery());

            //generowanie zapytania kompensujacego
            if (queryType.equals(QueryObject.DELETE)){
                query.generateDeleteRollbackQuery();
            } else if (queryType.equals(QueryObject.UPDATE)){
                query.generateUpdateRollbackQuery();
            }


        }
        } catch(SQLException sqle){
            this.success = false;
            throw sqle;
        }
        return this.success;
    }
}
