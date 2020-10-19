package cz.jr.trailtour.scheduler.trailtour.entities;

import java.time.LocalDate;

/**
 * Created by Jiří Rýdel on 5/4/20, 10:56 AM
 */
public class TrailtourPoints {

    private Double points;
    private Double pointsTrailtour;
    private Long activityId;
    private Integer position;
    private LocalDate date;
    private Integer time;

    public TrailtourPoints() {
    }

    public TrailtourPoints(Double points, Double pointsTrailtour) {
        this.points = points;
        this.pointsTrailtour = pointsTrailtour;
    }

    public Double getPoints() {
        return points;
    }

    public void setPoints(Double points) {
        this.points = points;
    }

    public Double getPointsTrailtour() {
        return pointsTrailtour;
    }

    public void setPointsTrailtour(Double pointsTrailtour) {
        this.pointsTrailtour = pointsTrailtour;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }
}
