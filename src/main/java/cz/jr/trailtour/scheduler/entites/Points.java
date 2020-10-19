package cz.jr.trailtour.scheduler.entites;

/**
 * Created by Jiří Rýdel on 6/20/20, 7:15 PM
 */
public class Points {

    private Integer position;
    private Double points;
    private Integer time;

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Double getPoints() {
        return points;
    }

    public void setPoints(Double points) {
        this.points = points;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }
}
