package cz.jr.trailtour.scheduler.trailtour.entities;

import cz.jr.trailtour.scheduler.entites.Country;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jiří Rýdel on 5/2/20, 7:27 PM
 */
public class TrailtourAthlete {

    private Long id;
    private String name;
    private Gender gender;
    private Map<Country, TrailtourPoints> pointsLadderMap = new HashMap<>();
    private Map<Long, TrailtourPoints> pointsStageMap = new HashMap<>();

    public TrailtourAthlete() {
    }

    public TrailtourAthlete(Long id, String name, Gender gender) {
        this.id = id;
        this.name = name;
        this.gender = gender;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Map<Country, TrailtourPoints> getPointsLadderMap() {
        return pointsLadderMap;
    }

    public void setPointsLadderMap(Map<Country, TrailtourPoints> pointsLadderMap) {
        this.pointsLadderMap = pointsLadderMap;
    }

    public Map<Long, TrailtourPoints> getPointsStageMap() {
        return pointsStageMap;
    }

    public void setPointsStageMap(Map<Long, TrailtourPoints> pointsStageMap) {
        this.pointsStageMap = pointsStageMap;
    }

    public boolean isRegistered(Country country) {
        TrailtourPoints trailtourPoints = pointsLadderMap.get(country);
        return trailtourPoints != null && trailtourPoints.getPointsTrailtour() != null;
    }
}
