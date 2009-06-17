/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor.queryobject;

/**
 *
 * @author Kubutek
 */
public class Criteria {
    private static final String GT = " > ";
    private static final String LT = " < ";
    private static final String EQ = " = ";
    private static final String LIKE = " LIKE ";
    private static final String AND = " AND ";
    private static final String OR = " OR ";
    private String sqlOperator = null;
    private String field = null;
    private Object value=null;
    private Criteria left = null;
    private Criteria right = null;

    public Criteria(String operator, String field, Object value){
        this.sqlOperator = operator;
        this.field = field;
        this.value = value;
    }
    public Criteria (String sqlOperator, Criteria left, Criteria right){
        this.sqlOperator = sqlOperator;
        this.left = left;
        this.right = right;
    }

    public static Criteria equals(String field, Object value){
        return new Criteria(Criteria.EQ, field, value);
    }
    public static Criteria like(String field, String value){
        return new Criteria(Criteria.LIKE, field, value);
    }
    public static Criteria ilike(String field, String value){
        return null;//Criteria.like();
    }
    public static Criteria lowerThen(String field, Object value){
        return new Criteria(Criteria.LT, field, value);
    }

    public static Criteria greaterThen(String field, Object value){
        return new Criteria(Criteria.GT,field,value);
    }
    public static Criteria and(Criteria c1, Criteria c2){
        return new Criteria (Criteria.AND, c1, c2);
    }
    public static Criteria or(Criteria c1, Criteria c2){
        return new Criteria (Criteria.OR, c1, c2);
    }
    //GETTERS & SETTERS
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getSqlOperator() {
        return sqlOperator;
    }

    public void setSqlOperator(String sqlOperator) {
        this.sqlOperator = sqlOperator;
    }

    public Criteria getLeft() {
        return left;
    }

    public void setLeft(Criteria left) {
        this.left = left;
    }

    public Criteria getRight() {
        return right;
    }

    public void setRight(Criteria right) {
        this.right = right;
    }


}
