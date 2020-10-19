package cz.jr.trailtour.scheduler;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MysqlUtils {

    private static final Logger LOG = LogManager.getLogger(MysqlUtils.class);

    public static <T> T select(HikariDataSource dataSource, String sql, Object[] params, ResultSetHandler<T> handler) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return select(connection, sql, params, handler);
        }
    }

    public static <T> T select(Connection connection, String sql, Object[] params, ResultSetHandler<T> handler) throws SQLException {
        LOG.debug("[{}] {}", sql, params);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            initParams(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                return handler.handle(rs);
            }
        }
    }

    public static <T> T selectObject(HikariDataSource dataSource, String sql, Object[] params, ResultSetHandler<T> handler) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return selectObject(connection, sql, params, handler);
        }
    }

    public static <T> T selectObject(Connection connection, String sql, Object[] params, ResultSetHandler<T> handler) throws SQLException {
        LOG.debug("[{}] {}", sql, params);
        return select(connection, sql, params, rs -> {
            if (rs.next()) {
                return handler.handle(rs);
            } else {
                return null;
            }
        });
    }

    public static <T> List<T> selectList(HikariDataSource dataSource, String sql, Object[] params, ResultSetHandler<T> handler) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return selectList(connection, sql, params, handler);
        }
    }

    public static <T> List<T> selectList(Connection connection, String sql, Object[] params, ResultSetHandler<T> handler) throws SQLException {
        LOG.debug("[{}] {}", sql, params);
        return select(connection, sql, params, rs -> {
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                T entity = handler.handle(rs);
                result.add(entity);
            }
            return result;
        });
    }

    public static int execute(HikariDataSource dataSource, String sql, Object[] params) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return execute(connection, sql, params);
        }
    }

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

    public interface ResultSetHandler<T> {

        T handle(ResultSet rs) throws SQLException;
    }

    public static class DefaultResultSetHandler implements ResultSetHandler<Map<String, Object>> {

        @Override
        public Map<String, Object> handle(ResultSet rs) throws SQLException {
            ResultSetMetaData rsmd = rs.getMetaData();
            Map<String, Object> map = new LinkedHashMap<>();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                String name = rsmd.getColumnLabel(i);
                map.put(name, rs.getObject(i));
            }
            return map;
        }
    }

}
