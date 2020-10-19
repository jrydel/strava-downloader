package cz.jr.trailtour.scheduler.mysql;

/**
 * Created by Jiří Rýdel on 2/17/20, 1:49 PM
 */
public class Param {

    protected final String column;
    protected final String value;

    public Param(String column) {
        this.column = column;
        this.value = "?";
    }

    public Param(String column, String value) {
        this.column = column;
        this.value = value;
    }

    public String getColumn() {
        return column;
    }

    public String getValue() {
        return value;
    }
}
