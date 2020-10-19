package cz.jr.trailtour.scheduler.trailtour;

import cz.jr.trailtour.scheduler.HttpUtils;
import cz.jr.trailtour.scheduler.entites.*;
import cz.jr.trailtour.scheduler.trailtour.entities.TrailtourStage;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


/**
 * Created by Jiří Rýdel on 5/2/20, 6:46 PM
 */
public class TrailtourParser {

    private static final String EMPTY = "---";

    public void parseLadder(Document document, Data data) {
        Elements elements = document.select("table.table");
        parseLadderClubs(elements.get(2), data);
        parseLadderAthletes(elements.get(0), Gender.F, data);
        parseLadderAthletes(elements.get(1), Gender.M, data);
    }

    private void parseLadderAthletes(Element element, Gender gender, Data data) {
        Elements trElements = element.select("tr");
        for (int i = 1; i < trElements.size(); i++) {
            Element trElement = trElements.get(i);
            Elements td = trElement.select("td");
            String position = td.get(0).text();
            String id = td.select("a").attr("href").replace("https://www.strava.com/athletes/", "");
            String name = td.get(1).text();
            String club = td.get(2).text();
            String points = td.get(3).text();

            long athleteId = Long.parseLong(id);
            Athlete athlete = data.getAthleteMap().computeIfAbsent(athleteId, k -> new Athlete(athleteId, gender, name, EMPTY.equals(club) ? null : club));
            athlete.getTrailtourPoints().setPosition(parsePosition(position));
            athlete.getTrailtourPoints().setPoints(parsePoints(points));
        }
    }

    private void parseLadderClubs(Element element, Data data) {
        Elements trElements = element.select("tr");
        for (int i = 1; i < trElements.size(); i++) {
            Element trElement = trElements.get(i);
            Elements td = trElement.select("td");

            String position = td.get(0).text();
            String name = td.get(1).text();
            String points = td.get(2).text();

            Club club = data.getClubMap().computeIfAbsent(name.toLowerCase(), k -> new Club(name));
            club.getTrailtourPoints().setPosition(parsePosition(position));
            club.getTrailtourPoints().setPoints(parsePoints(points));
        }
    }

    public void parseStage(Document document, TrailtourStage stage, Data data) throws IOException, InterruptedException {
        String stravaUrl = document.select("a.btn.btn-warning.btn-sm").attr("href");

        // strava ma redirecty
        stravaUrl = HttpUtils.getFinalUrl(stravaUrl);
        stage.setStravaUrl(stravaUrl);

        String before = "var lnd = JAK.mel(\"a\", {href:\"";
        String str = document.select("script").html();
        String mapyczUrl = str.substring(str.indexOf(before) + before.length(), str.indexOf("\", target:\"_blank\", innerHTML:lnt});"));
        stage.setMapyczUrl(mapyczUrl);

        Elements elements = document.select("table.table");
        if (elements == null || elements.isEmpty()) {
            return;
        }

        parseStageAthletes(elements.get(0), stage.getNumber(), data);
        parseStageAthletes(elements.get(1), stage.getNumber(), data);
        parseStageClubs(elements.get(2), stage.getNumber(), data);
    }

    public void parseStageAthletes(Element element, int stageNumber, Data data) {
        Elements trElements = element.select("tr");
        for (int i = 1; i < trElements.size(); i++) {
            Element trElement = trElements.get(i);
            Elements tdElements = trElement.select("td");

            String position = tdElements.get(0).text();

            Element athleteElement = tdElements.get(1);
            String stravaUrl = athleteElement.select("a").attr("href");
            long athleteId = Long.parseLong(stravaUrl.replace("https://www.strava.com/athletes/", ""));

            Element timeElement = tdElements.get(3);
            String time = timeElement.text();

            Element pointsElement = tdElements.get(4);
            String points = pointsElement.text();


            Points pointsData = data.getAthleteMap().get(athleteId).getTrailtourPointsMap().computeIfAbsent(stageNumber, k -> new Points());
            pointsData.setPosition(parsePosition(position));
            pointsData.setPoints(parsePoints(points));
            pointsData.setTime(parseTime(time));
        }
    }

    public void parseStageClubs(Element element, int stageNumber, Data data) {
        Elements trElements = element.select("tr");
        for (int i = 1; i < trElements.size(); i++) {
            Element trElement = trElements.get(i);
            Elements tdElements = trElement.select("td");

            String position = tdElements.get(0).text();
            String name = tdElements.get(1).text();
            String points = tdElements.get(2).text();

            Points pointsData = data.getClubMap().get(name.toLowerCase()).getTrailtourPointsMap().computeIfAbsent(stageNumber, k -> new Points());
            pointsData.setPosition(parsePosition(position));
            pointsData.setPoints(parsePoints(points));
        }
    }

    private Integer parseTime(String test) {
        if (test == null || EMPTY.equals(test)) {
            return null;
        }
        String[] split = test.split(":");
        if (split.length == 1) {
            return Integer.parseInt(split[0]);
        } else if (split.length == 2) {
            return Integer.parseInt(split[0]) * 60 + Integer.parseInt(split[1]);
        } else {
            return Integer.parseInt(split[0]) * 3600 + Integer.parseInt(split[1]) * 60 + Integer.parseInt(split[2]);
        }
    }

    private Integer parsePosition(String test) {
        if (test == null || EMPTY.equals(test)) {
            return null;
        }
        try {
            return Integer.parseInt(test);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Double parsePoints(String test) {
        if (test == null || EMPTY.equals(test)) {
            return null;
        }
        try {
            return Double.parseDouble(test);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
