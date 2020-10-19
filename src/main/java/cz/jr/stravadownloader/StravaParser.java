package cz.jr.stravadownloader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.jr.trailtour.scheduler.entites.Activity;
import cz.jr.trailtour.scheduler.entites.Athlete;
import cz.jr.trailtour.scheduler.entites.Data;
import cz.jr.trailtour.scheduler.trailtour.entities.TrailtourStage;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created by Jiří Rýdel on 4/14/20, 11:47 AM
 */
public class StravaParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void parseSegment(Document document, TrailtourStage stage, Data data, LocalDate dateFrom, LocalDate dateTo) throws JsonProcessingException, NoSegmentDataException {
        Element test = document.select("div#results").select("tbody").select("td").first();
        if (test != null && "No results found".equals(test.text())) {
            throw new NoSegmentDataException();
        }

        Element tableElement = document.select("#results > table > tbody").first();
        if ("No results found".equals(tableElement.text())) {
            throw new NoSegmentDataException();
        }

        for (Element element : tableElement.select("tr")) {
            Element resultElement = element.select("td[data-tracking-element=leaderboard_effort]").first();
            if (resultElement == null) {
                continue;
            }

            String date = resultElement.text();
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("MMM d, yyyy"));

            if (localDate.isBefore(dateFrom) || localDate.isAfter(dateTo)) {
                continue;
            }

            //{"athlete_id":27058130,"activity_id":2029260372,"segment_effort_id":80735082841,"rank":1}
            String json = resultElement.attr("data-tracking-properties");
            JsonNode root = objectMapper.readTree(json);

            long athleteId = root.get("athlete_id").asLong();
            long activityId = root.get("activity_id").asLong();
            int position = root.get("rank").asInt();
            String time = element.selectFirst("td.last-child").text();

            Athlete athlete = data.getAthleteMap().get(athleteId);
            if (athlete != null) {
                Activity activity = new Activity();
                activity.setId(activityId);
                activity.setPosition(position);
                activity.setDate(LocalDate.parse(date, DateTimeFormatter.ofPattern("MMM d, yyyy")));
                activity.setTime(parseTime(time));
                athlete.getActivityMap().putIfAbsent(stage.getNumber(), activity);
            }
        }
    }

    private int parseTime(String time) {
        String[] split = time.split(":");
        if (split.length == 1) {
            return Integer.parseInt(split[0]);
        } else if (split.length == 2) {
            return Integer.parseInt(split[0]) * 60 + Integer.parseInt(split[1]);
        } else {
            return Integer.parseInt(split[0]) * 3600 + Integer.parseInt(split[1]) * 60 + Integer.parseInt(split[2]);
        }
    }
}
