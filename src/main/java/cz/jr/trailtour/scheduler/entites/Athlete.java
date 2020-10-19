package cz.jr.trailtour.scheduler.entites;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jiří Rýdel on 5/1/20, 6:36 PM
 */
public class Athlete {

    private final long id;
    private final Gender gender;
    private final String name;
    private final String club;

    private final Map<Integer, Activity> activityMap = new HashMap<>();

    private final Map<Integer, Points> trailtourPointsMap = new HashMap<>();
    private final Map<Integer, Points> pointsMap = new HashMap<>();

    private final Points points = new Points();
    private final Points trailtourPoints = new Points();

    public Athlete(long id, Gender gender, String name, String club) {
        this.id = id;
        this.gender = gender;
        this.name = name;
        this.club = club;
    }

    public long getId() {
        return id;
    }

    public Gender getGender() {
        return gender;
    }

    public String getName() {
        return name;
    }

    public String getClub() {
        return club;
    }

    public Map<Integer, Activity> getActivityMap() {
        return activityMap;
    }

    public Map<Integer, Points> getTrailtourPointsMap() {
        return trailtourPointsMap;
    }

    public Map<Integer, Points> getPointsMap() {
        return pointsMap;
    }

    public Points getPoints() {
        return points;
    }

    public Points getTrailtourPoints() {
        return trailtourPoints;
    }
}
