/*
 * Klasa implementująca wzorzec QueryObject
 * Wzorca QueryObject używamy w naszym projekcie w celu przechowywania
 parametrów zapytań sql, a także generowania kodu sql na podstawie danych parametrów.
 */

package monitor.queryobject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *I * @author gofer
 */
public class QueryObject {
    public static final String INSERT = "INSERT";
    public static final String DELETE = "DELETE";
    public static final String UPDATE = "UPDATE";
    public static final String SELECT = "SELECT";

    private List criteria = new ArrayList();
    private List parameters = new ArrayList();

    private String tableName = null;
    private String queryType = null;
    private String query = null;
    public QueryObject(String queryType, String tableName, List criteria, List params){
        this.tableName = tableName;
        this.queryType = queryType;
        this.criteria = criteria;
        this.parameters = params;
    }

    public List execute(String driver, String user, String passwd, String query){
        query = "";
        ResultSet rs = null;
        try {
        String driver1 = "com.mysql.jdbc.Driver";
        Class.forName(driver1);
        Connection connection = DriverManager.getConnection(driver, user, passwd);
        Statement statement = connection.createStatement();
         rs = statement.executeQuery(query);
        } catch (Exception sqle){

        }
        return null;
    }

    public void addCriteria(Criteria cr){
        this.criteria.add(cr);
    }
    public void addParameter(QueryParameter param){
        this.parameters.add(param);
    }
    private String getQueryFromCriteria(Criteria cr){
        String q = "";
        String operator = cr.getSqlOperator();
        if (operator.equals(" AND ") || operator.equals(" OR ")){
            q+="("+getQueryFromCriteria(cr.getLeft())+operator+getQueryFromCriteria(cr.getRight())+")";
        } else {
            q+="("+cr.getField()+operator+cr.getValue()+")";
        }
        return q;
    }
    public String generateQuery() throws Exception{
       String query = "";
        if (queryType.equals(QueryObject.INSERT)){
           String query1 = "INSERT INTO "+tableName+" (";
           String query2 = " VALUES (";
           if (parameters != null){
               for (int i=0; i<parameters.size(); i++){
                   QueryParameter param = (QueryParameter) parameters.get(i);
                   query1+=param.getField()+",";
                   query2+=param.getValue()+",";
               }
               query1 = query1.substring(0, query1.length()-1);
               query2 = query2.substring(0, query2.length()-1);
               query1 += ")";
               query2 += ")";
               query += query1+query2;
           } else {
               throw new Exception("Nie podano parametrow dla zapytania");
           }
       } else if (queryType.equals(QueryObject.DELETE)){
            query += "DELETE FROM "+tableName;
       } else if (queryType.equals(QueryObject.UPDATE)){
           query += "UPDATE "+tableName+" SET ";
           for (int i=0; i<parameters.size(); i++){
               QueryParameter param = (QueryParameter) parameters.get(i);
               query+=param.getField()+"="+param.getValue()+",";
           }
           query = query.substring(0, query.length()-1);
       } else if (queryType.equals(QueryObject.SELECT)){
           query += "SELECT * FROM "+tableName;
       }
        if (criteria != null && !criteria.isEmpty() && !queryType.equals(QueryObject.INSERT)){
            query += " WHERE ";
            for (int i=0; i<this.criteria.size(); i++){
                Criteria cr = (Criteria) criteria.get(i);
                query += getQueryFromCriteria(cr);
                if (i!=criteria.size()-1){
                query += Criteria.AND;
                }
            }
        }  
        return (query+";");
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    public String getQuery(){
        if (query == null){
            try {
                return generateQuery();
            } catch (Exception e){
                System.out.println("Wystapil blad podczas generowania zapytania: "+e.getMessage());
            }
        }
        return query;     
    }
 }
