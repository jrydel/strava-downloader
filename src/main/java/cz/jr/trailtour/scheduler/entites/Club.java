package cz.jr.trailtour.scheduler.entites;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jiří Rýdel on 5/4/20, 1:56 PM
 */
public class Club {

    private final String name;

    private final Map<Integer, Points> trailtourPointsMap = new HashMap<>();
    private final Map<Integer, Points> pointsMap = new HashMap<>();

    private final Points points = new Points();
    private final Points trailtourPoints = new Points();

    public Club(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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
