/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor;

/**
 *
 * @author marcin
 */
public class ComponentTransactionAdapterPostgreSql implements TransactionParticipantIF {


    public boolean commitTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean abortTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean startTransaction(DBConnectionData dbcd, ComponentTransaction ct) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
