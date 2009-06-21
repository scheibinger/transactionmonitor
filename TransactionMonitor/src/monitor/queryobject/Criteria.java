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
    public static final String GT = " > ";
    public static final String LT = " < ";
    public static final String EQ = " = ";
    public static final String LIKE = " LIKE ";
    public static final String AND = " AND ";
    public static final String OR = " OR ";
    public static final String IS = " IS ";
    public static final String ISNOT = " IS NOT ";
    private String sqlOperator = null;
    private String field = null;
    private String value=null;
    private Criteria left = null;
    private Criteria right = null;

    public Criteria(String field, String operator, String value){
        this.sqlOperator = operator;
        this.field = field;
        this.value = value;
    }
    public Criteria (Criteria left, String sqlOperator,  Criteria right){
        this.sqlOperator = sqlOperator;
        this.left = left;
        this.right = right;
    }

    public static Criteria equals(String field, String value){
        return new Criteria(Criteria.EQ, field, value);
    }
    public static Criteria like(String field, String value){
        return new Criteria(Criteria.LIKE, field, value);
    }
    public static Criteria ilike(String field, String value){
        return null;//Criteria.like();
    }
    public static Criteria lowerThen(String field, String value){
        return new Criteria(Criteria.LT, field, value);
    }

    public static Criteria greaterThen(String field, String value){
        return new Criteria(Criteria.GT,field,value);
    }
    public static Criteria and(Criteria c1, Criteria c2){
        return new Criteria (c1,Criteria.AND,  c2);
    }
    public static Criteria or(Criteria c1, Criteria c2){
        return new Criteria ( c1,Criteria.OR, c2);
    }
    //GETTERS & SETTERS
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
