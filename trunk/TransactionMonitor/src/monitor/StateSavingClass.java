/*
 * Klasy te odpowiedzialne są za
 zapisywanie i przywracanie stanu bazy
 danych z przed rozpoczęcia transakcji rozproszonej.
 */

package monitor;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import monitor.queryobject.QueryObject;

/**
 *
 * @author gofer
 */
public class StateSavingClass {

    public StateSavingClass(){

    }
    public void saveState(QueryObject qo){
        if (qo.getQueryType().equals(QueryObject.INSERT)){

        } else {
           // query
        }
        String query = qo.getQuery();
        // ******INSERT //////////////
       //query = "INSERT INTO pensja VALUES (10, 5, 125630);";
     ////
        query = "UPDATE pensja SET pen_prc_id=20 WHERE pen_id=10;";
        ResultSet rs = null;
        try {
        String driver1 = "com.mysql.jdbc.Driver";
        String url1 = "jdbc:mysql://sql.ares-system.nazwa.pl:3305/ares-system_7";
        String user1 = "ares-system_7";
        String password1 = "TEsttest1";
        Class.forName(driver1);
        Connection connection = DriverManager.getConnection(url1 , user1, password1);
        Statement statement = connection.createStatement(ResultSet.CONCUR_UPDATABLE, ResultSet.TYPE_FORWARD_ONLY);
        
        //  INSERT
        statement.execute(query,Statement.RETURN_GENERATED_KEYS);
        ResultSet keys = statement.getGeneratedKeys();
        if (keys != null){
            while (keys.next()){
                   System.out.println(keys.getInt(1));
            }
        }
        /// DELETE
        statement.execute("INSERT INTO pensja (pen_id,pen_prc_id,pen_kwota) VALUES (10,20,30);");
        statement.execute("SELECT * FROM pensja WHERE pen_id=10;");
        rs = statement.getResultSet();
        if (rs != null){
            statement.execute("CREATE TABLE pensja_temp AS SELECT * FROM pensja WHERE pen_id=10;");
        }
    //    while (rs.next()){
    //        System.out.println(rs.getInt("pen_id")+" "+ rs.getInt("pen_kwota"));
    //    }
        Connection connection2 = DriverManager.getConnection(url1 , user1, password1);
        Statement statement2 = connection2.createStatement();
        statement2.execute("DELETE FROM pensja WHERE pen_id=10;");
    //    rs.first();
        statement.execute("INSERT pensja SELECT * FROM pensja_temp;");

        statement.executeQuery("SELECT * FROM pensja WHERE pen_id=10;");
        rs = statement.getResultSet();
        while (rs.next()){
            System.out.println(rs.getInt("pen_id")+" "+ rs.getInt("pen_kwota"));
        }
//////////delete - koniec

//////////////////update



 ////////////////update - koniec
         } catch (Exception sqle){
            System.out.println("blad "+sqle.getMessage());
        }
    }

}
