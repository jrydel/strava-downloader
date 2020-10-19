package cz.jr.trailtour.scheduler.mysql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jiří Rýdel on 6/21/20, 1:45 PM
 */
public class MysqlUtils {

    private static final Logger LOG = LogManager.getLogger(MysqlUtils.class);

    public static int execute(Connection connection, String sql, Object[] params) throws SQLException {
        LOG.debug("[{}] {}", sql, params);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            initParams(statement, params);
            return statement.executeUpdate();
        }
    }

    public static void initParams(PreparedStatement statement, Object... params) throws SQLException {
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(1 + i, params[i]);
            }
        }
    }

    public static String generateUpsert(String table, Param[] params) {
        List<String> columnSet = new ArrayList<>();
        List<String> valueSet = new ArrayList<>();
        List<String> updateSet = new ArrayList<>();

        for (Param param : params) {
            if (UpdateParam.class.isAssignableFrom(param.getClass())) {
                updateSet.add(param.getColumn() + " = " + param.getValue());
            } else {
                columnSet.add(param.getColumn());
                valueSet.add(param.getValue());
                if (param instanceof UpsertParam) {
                    UpsertParam upsertParam = (UpsertParam) param;
                    updateSet.add(upsertParam.getColumn() + " = " + upsertParam.getUpsertValue());
                }
            }
        }

        return "INSERT INTO " + table + " (" + String.join(", ", columnSet) + ") VALUES (" +
                String.join(", ", valueSet) + " ) " +
                (updateSet.isEmpty() ? "" : "ON DUPLICATE KEY UPDATE " + String.join(", ", updateSet));
    }
}
