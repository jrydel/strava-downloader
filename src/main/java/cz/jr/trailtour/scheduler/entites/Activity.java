package cz.jr.trailtour.scheduler.entites;

import java.time.LocalDate;

/**
 * Created by Jiří Rýdel on 5/18/20, 1:35 PM
 */
public class Activity {

    private long id;
    private LocalDate date;
    private int position;
    private int time;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
