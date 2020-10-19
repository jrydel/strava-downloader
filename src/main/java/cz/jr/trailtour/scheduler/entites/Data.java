package cz.jr.trailtour.scheduler.entites;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jiří Rýdel on 5/4/20, 2:10 PM
 */
public class Data {

    private Map<Long, Athlete> athleteMap = new HashMap<>();
    private Map<String, Club> clubMap = new HashMap<>();
    private int stravaRequestCount = 0;

    public Map<Long, Athlete> getAthleteMap() {
        return athleteMap;
    }

    public void setAthleteMap(Map<Long, Athlete> athleteMap) {
        this.athleteMap = athleteMap;
    }

    public Map<String, Club> getClubMap() {
        return clubMap;
    }

    public void setClubMap(Map<String, Club> clubMap) {
        this.clubMap = clubMap;
    }

    public int getStravaRequestCount() {
        return stravaRequestCount;
    }

    public void setStravaRequestCount(int stravaRequestCount) {
        this.stravaRequestCount = stravaRequestCount;
    }
}
