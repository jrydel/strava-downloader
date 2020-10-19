package cz.jr.trailtour.scheduler;

import com.zaxxer.hikari.HikariDataSource;
import cz.jr.trailtour.scheduler.entites.*;
import cz.jr.trailtour.scheduler.strava.StravaProcessor;
import cz.jr.trailtour.scheduler.trailtour.TrailtourProcessor;
import cz.jr.trailtour.scheduler.trailtour.entities.TrailtourStage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Created by Jiří Rýdel on 5/4/20, 3:06 PM
 */
public class Processor {

    private final HikariDataSource dataSource;
    private final LocalDate dateFrom;
    private final LocalDate dateTo;

    public Processor(HikariDataSource dataSource, LocalDate dateFrom, LocalDate dateTo) {
        this.dataSource = dataSource;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    public void process(String database, String ladderUrl) throws SQLException {
        Data data = new Data();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);

        List<TrailtourStage> stageList = MysqlUtils.selectList(dataSource, "SELECT number, trailtour_url FROM " + database + ".stage", new Object[]{}, rs -> new TrailtourStage(rs.getInt("number"), rs.getString("trailtour_url")));

        new TrailtourProcessor().process(stageList, data, ladderUrl);
        saveTrailtourData(database, stageList, data);

        new StravaProcessor().process(dateFrom, dateTo, stageList, data);
        saveStravaData(database, data, now);

        new PointsProcessor().process(stageList, data);
        saveData(database, data, stageList, now);
    }

    private void saveTrailtourData(String database, List<TrailtourStage> stageList, Data data) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            // etapy
            try (PreparedStatement statement = connection.prepareStatement("UPDATE " + database + ".stage SET strava_url = ?, strava_data = ?, mapycz_url = ?, status = ? WHERE number = ?")) {
                for (TrailtourStage stage : stageList) {
                    statement.setString(1, stage.getStravaUrl());
                    statement.setString(2, stage.getStravaData());
                    statement.setString(3, stage.getMapyczUrl());
                    statement.setString(4, "enabled");
                    statement.setInt(5, stage.getNumber());
                    statement.addBatch();
                }
                statement.executeBatch();
            }
            // kluby
            try (PreparedStatement statement = connection.prepareStatement("UPDATE " + database + ".club SET status = ?")) {
                statement.setString(1, "disabled");
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + database + ".club (name, status) VALUES (?, ?) ON DUPLICATE KEY UPDATE status = VALUES(status)")) {
                for (Club club : data.getClubMap().values()) {
                    statement.setString(1, club.getName());
                    statement.setString(2, "enabled");
                    statement.addBatch();
                }
                statement.executeBatch();
            }
            // zavodnici
            try (PreparedStatement statement = connection.prepareStatement("UPDATE " + database + ".athlete SET status = ?")) {
                statement.setString(1, "disabled");
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + database + ".athlete (id, name, club_name, gender, status) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name), club_name = VALUES(club_name), gender = VALUES(gender), status = VALUES(status)")) {
                for (Athlete athlete : data.getAthleteMap().values()) {
                    statement.setLong(1, athlete.getId());
                    statement.setString(2, athlete.getName());
                    statement.setString(3, athlete.getClub());
                    statement.setString(4, athlete.getGender().toString());
                    statement.setString(5, "enabled");
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        }
    }

    private void saveStravaData(String database, Data data, LocalDateTime now) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            // strava requesty
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + database + ".strava_requests (timestamp, count) VALUES (?, ?) ON DUPLICATE KEY UPDATE count = count + VALUES(count)")) {
                statement.setTimestamp(1, java.sql.Timestamp.valueOf(now));
                statement.setInt(2, data.getStravaRequestCount());
                statement.executeUpdate();
            }
            // aktivity
            try (PreparedStatement statement = connection.prepareStatement("UPDATE " + database + ".activity SET status = ?")) {
                statement.setString(1, "disabled");
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + database + ".activity (id, stage_number, athlete_id, date, position, time, status) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE id = VALUES(id), date = VALUES(date), position = VALUES(position), time = VALUES(time), status = VALUES(status)")) {
                int index = 0;
                for (Athlete athlete : data.getAthleteMap().values()) {
                    for (Map.Entry<Integer, Activity> entry : athlete.getActivityMap().entrySet()) {
                        Activity activity = entry.getValue();
                        statement.setLong(1, activity.getId());
                        statement.setInt(2, entry.getKey());
                        statement.setLong(3, athlete.getId());
                        statement.setDate(4, java.sql.Date.valueOf(activity.getDate()));
                        statement.setInt(5, activity.getPosition());
                        statement.setInt(6, activity.getTime());
                        statement.setString(7, "enabled");
                        statement.addBatch();

                        if (++index % 100 == 0) {
                            statement.executeBatch();
                        }
                    }
                }
                statement.executeBatch();
            }
        }
    }

    private void saveData(String database, Data data, List<TrailtourStage> stageList, LocalDateTime now) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            // zavodnici celkove body
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + database + ".athlete_ladder (athlete_id, timestamp, position, points, trailtour_position, trailtour_points) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE points = VALUES(points), position = VALUES(position), trailtour_position = VALUES(trailtour_position), trailtour_points = VALUES(trailtour_points)")) {
                int index = 0;
                for (Athlete athlete : data.getAthleteMap().values()) {
                    Points points = athlete.getPoints();
                    Points trailtourPoints = athlete.getTrailtourPoints();

                    statement.setLong(1, athlete.getId());
                    statement.setTimestamp(2, java.sql.Timestamp.valueOf(now));
                    statement.setObject(3, points.getPosition());
                    statement.setObject(4, points.getPoints());
                    statement.setObject(5, trailtourPoints.getPosition());
                    statement.setObject(6, trailtourPoints.getPoints());

                    statement.addBatch();

                    if (++index % 100 == 0) {
                        statement.executeBatch();
                    }
                }
                statement.executeBatch();
            }
            // zavodnici etapove body
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + database + ".athlete_result (athlete_id, stage_number, timestamp, position, points, trailtour_position, trailtour_points, trailtour_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE points = VALUES(points), position = VALUES(position), trailtour_position = VALUES(trailtour_position), trailtour_points = VALUES(trailtour_points), trailtour_time = VALUES(trailtour_time)")) {
                int index = 0;
                for (Athlete athlete : data.getAthleteMap().values()) {
                    for (TrailtourStage stage : stageList) {
                        Points points = athlete.getPointsMap().get(stage.getNumber());
                        Points trailtourPoints = athlete.getTrailtourPointsMap().get(stage.getNumber());

                        if (points == null && trailtourPoints == null) {
                            continue;
                        }

                        statement.setLong(1, athlete.getId());
                        statement.setInt(2, stage.getNumber());
                        statement.setTimestamp(3, java.sql.Timestamp.valueOf(now));
                        statement.setObject(4, points != null ? points.getPosition() : null);
                        statement.setObject(5, points != null ? points.getPoints() : null);
                        statement.setObject(6, trailtourPoints != null ? trailtourPoints.getPosition() : null);
                        statement.setObject(7, trailtourPoints != null ? trailtourPoints.getPoints() : null);
                        statement.setObject(8, trailtourPoints != null ? trailtourPoints.getTime() : null);

                        statement.addBatch();

                        if (++index % 100 == 0) {
                            statement.executeBatch();
                        }
                    }
                }
                statement.executeBatch();
            }
            // kluby celkove body
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + database + ".club_ladder (club_name, timestamp, position, points, trailtour_position, trailtour_points) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE points = VALUES(points), position = VALUES(position), trailtour_position = VALUES(trailtour_position), trailtour_points = VALUES(trailtour_points)")) {
                int index = 0;
                for (Club club : data.getClubMap().values()) {
                    Points points = club.getPoints();
                    Points trailtourPoints = club.getTrailtourPoints();

                    statement.setString(1, club.getName());
                    statement.setTimestamp(2, java.sql.Timestamp.valueOf(now));
                    statement.setObject(3, points.getPosition());
                    statement.setObject(4, points.getPoints());
                    statement.setObject(5, trailtourPoints.getPosition());
                    statement.setObject(6, trailtourPoints.getPoints());
                    statement.addBatch();

                    if (++index % 100 == 0) {
                        statement.executeBatch();
                    }
                }
                statement.executeBatch();
            }
            // kluby etapove body
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + database + ".club_result (club_name, stage_number, timestamp, position, points, trailtour_position, trailtour_points) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE points = VALUES(points), position = VALUES(position), trailtour_position = VALUES(trailtour_position), trailtour_points = VALUES(trailtour_points)")) {
                int index = 0;
                for (Club club : data.getClubMap().values()) {
                    for (TrailtourStage stage : stageList) {
                        Points points = club.getPointsMap().get(stage.getNumber());
                        Points trailtourPoints = club.getTrailtourPointsMap().get(stage.getNumber());

                        if (points == null && trailtourPoints == null) {
                            continue;
                        }

                        statement.setString(1, club.getName());
                        statement.setInt(2, stage.getNumber());
                        statement.setTimestamp(3, java.sql.Timestamp.valueOf(now));
                        statement.setObject(4, points != null ? points.getPosition() : null);
                        statement.setObject(5, points != null ? points.getPoints() : null);
                        statement.setObject(6, trailtourPoints != null ? trailtourPoints.getPosition() : null);
                        statement.setObject(7, trailtourPoints != null ? trailtourPoints.getPoints() : null);
                        statement.addBatch();

                        if (++index % 100 == 0) {
                            statement.executeBatch();
                        }
                    }
                }
                statement.executeBatch();
            }
            // pohled
            try (PreparedStatement statement = connection.prepareStatement("CREATE OR REPLACE VIEW " + database + ".athlete_data AS SELECT " +
                    "a.number AS stage_number, " +
                    "a.name AS stage_name, " +
                    "b.id AS activity_id, " +
                    "b.time AS activity_time, " +
                    "b.date AS activity_date, " +
                    "c.id AS athlete_id, " +
                    "c.name AS athlete_name, " +
                    "c.gender AS athlete_gender, " +
                    "d.id AS club_id, " +
                    "d.name AS club_name, " +
                    "e.position AS position, " +
                    "e.points AS points, " +
                    "e.trailtour_position AS trailtour_position, " +
                    "e.trailtour_points AS trailtour_points, " +
                    "e.trailtour_time AS trailtour_time " +
                    "FROM " + database + ".stage a " +
                    "LEFT JOIN " + database + ".activity b ON b.stage_number = a.number AND b.created = (SELECT MAX(x.created) FROM " + database + ".activity x WHERE x.stage_number = b.stage_number AND x.athlete_id = b.athlete_id) AND b.status = ?" +
                    "JOIN " + database + ".athlete c ON c.id = b.athlete_id " +
                    "LEFT JOIN " + database + ".club d ON d.name = c.club_name AND d.status = ?" +
                    "LEFT JOIN " + database + ".athlete_result e ON e.athlete_id = c.id AND e.stage_number = a.number AND e.timestamp = ? " +
                    "WHERE a.status = ? AND c.status = ?")) {
                statement.setString(1, "enabled");
                statement.setString(2, "enabled");
                statement.setTimestamp(3, java.sql.Timestamp.valueOf(now));
                statement.setString(4, "enabled");
                statement.setString(5, "enabled");
                statement.executeUpdate();
            }
        }
    }
}
