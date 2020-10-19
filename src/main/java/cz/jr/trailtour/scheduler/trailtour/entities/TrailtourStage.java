package cz.jr.trailtour.scheduler.trailtour.entities;

/**
 * Created by Jiří Rýdel on 5/4/20, 10:47 AM
 */
public class TrailtourStage {

    private final int number;
    private final String trailtourUrl;
    private String mapyczUrl;
    private String stravaData;
    private String stravaUrl;

    public TrailtourStage(int number, String trailtourUrl) {
        this.number = number;
        this.trailtourUrl = trailtourUrl;
    }

    public int getNumber() {
        return number;
    }

    public String getTrailtourUrl() {
        return trailtourUrl;
    }

    public String getMapyczUrl() {
        return mapyczUrl;
    }

    public void setMapyczUrl(String mapyczUrl) {
        this.mapyczUrl = mapyczUrl;
    }

    public String getStravaData() {
        return stravaData;
    }

    public void setStravaData(String stravaData) {
        this.stravaData = stravaData;
    }

    public String getStravaUrl() {
        return stravaUrl;
    }

    public void setStravaUrl(String stravaUrl) {
        this.stravaUrl = stravaUrl;
    }
}
