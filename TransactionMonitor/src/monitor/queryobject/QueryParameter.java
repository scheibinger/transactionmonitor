/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor.queryobject;

/**
 *
 * @author Kubutek
 */
public class QueryParameter {

    private String field;
    private String value;

    public QueryParameter(String field, String value){
        this.field = field;
        this.value = value;
    }
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
