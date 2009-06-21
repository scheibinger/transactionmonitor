/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor;

/**
 *
 * @author marcin
 */
public class ComponentTransactionAdapterFactory {

    public static TransactionParticipantIF CreateAdapter(String type) {
        if(type.equals("MySql")){
            return new ComponentTransactionAdapterMySql();
        }
        else if(type.equals("MySqlXA")){
            return new ComponentTransactionAdapterMySqlXA();
        }
        else if(type.equals("PostgreSql")){
            return new ComponentTransactionAdapterPostgreSql();
        }
		else if(type.equals("PostgreSqlXA")){
            return new ComponentTransactionAdapterPostgreSqlXA();
        }
		else throw new UnsupportedOperationException("Not supported yet.");

    }

}
