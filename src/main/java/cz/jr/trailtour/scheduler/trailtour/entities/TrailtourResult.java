package cz.jr.trailtour.scheduler.trailtour.entities;

/**
 * Created by Jiří Rýdel on 5/2/20, 7:27 PM
 */
public class TrailtourResult {

    private TrailtourAthlete athlete;
    private Integer time;
    private Double points;

    public TrailtourAthlete getAthlete() {
        return athlete;
    }

    public void setAthlete(TrailtourAthlete athlete) {
        this.athlete = athlete;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Double getPoints() {
        return points;
    }

    public void setPoints(Double points) {
        this.points = points;
    }
}
