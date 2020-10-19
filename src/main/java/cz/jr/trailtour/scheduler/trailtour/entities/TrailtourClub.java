package cz.jr.trailtour.scheduler.trailtour.entities;

import cz.jr.trailtour.scheduler.entites.Country;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jiří Rýdel on 5/2/20, 7:27 PM
 */
public class TrailtourClub {

    private String name;
    private final Map<Country, TrailtourPoints> pointsMap = new HashMap<>();
    private final Map<Long, TrailtourPoints> pointsStageMap = new HashMap<>();

    public TrailtourClub() {
    }

    public TrailtourClub(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Country, TrailtourPoints> getPointsMap() {
        return pointsMap;
    }

    public Map<Long, TrailtourPoints> getPointsStageMap() {
        return pointsStageMap;
    }
}
