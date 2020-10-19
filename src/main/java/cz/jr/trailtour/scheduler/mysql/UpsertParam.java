package cz.jr.trailtour.scheduler.mysql;

/**
 * Created by Jiří Rýdel on 2/17/20, 1:55 PM
 */
public class UpsertParam extends Param {

    protected final String upsertValue;

    public UpsertParam(String column) {
        super(column, "?");
        this.upsertValue = "VALUES(" + column + ")";
    }

    public UpsertParam(String column, String value) {
        super(column, value);
        this.upsertValue = "VALUES(" + column + ")";
    }

    public UpsertParam(String column, String value, String upsertValue) {
        super(column, value);
        this.upsertValue = upsertValue;
    }

    public String getUpsertValue() {
        return upsertValue;
    }
}
