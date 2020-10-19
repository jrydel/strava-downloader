package cz.jr.trailtour.scheduler;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Created by Jiří Rýdel on 5/2/20, 7:15 PM
 */
public class Main {

    private static final Logger LOG = LogManager.getLogger(Main.class);

    public static void main(String[] args) {

        final LocalDate dateFrom = LocalDate.of(2020, 5, 1);
        final LocalDate dateTo = LocalDate.of(2020, 10, 22);

        final HikariDataSource dataSource = new HikariDataSource(createConfig());
        try (dataSource) {
            Processor processor = new Processor(dataSource, dateFrom, dateTo);
            processor.process("trailtour", "http://www.trailtour.cz/2020/poradi/poradi-cz/");
//            processor.process("trailtour_sk", "http://www.trailtour.cz/2020/poradi/poradi-sk/");
        } catch (SQLException e) {
            LOG.error("Error.", e);
        }
    }

    static HikariConfig createConfig() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://37.205.12.14:3306");
        hikariConfig.setUsername("admin");
        hikariConfig.setPassword("klekanice17081992");
        hikariConfig.setMaximumPoolSize(8);
        hikariConfig.setMinimumIdle(8);
        hikariConfig.setPoolName("trailtour-scheduler");
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
        hikariConfig.addDataSourceProperty("useLocalTransactionState", "true");
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
        hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
        hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
        hikariConfig.addDataSourceProperty("maintainTimeStats", "false");
        hikariConfig.addDataSourceProperty("characterEncoding", "UTF-8");
        hikariConfig.addDataSourceProperty("useUnicode", "true");
        hikariConfig.addDataSourceProperty("useSSL", "false");
        hikariConfig.addDataSourceProperty("allowPublicKeyRetrieval", "true");
        hikariConfig.addDataSourceProperty("serverTimezone", "Europe/Prague");

        return new HikariDataSource(hikariConfig);
    }
}
