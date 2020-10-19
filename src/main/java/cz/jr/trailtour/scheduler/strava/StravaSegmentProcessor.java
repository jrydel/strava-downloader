package cz.jr.trailtour.scheduler.strava;

import cz.jr.trailtour.scheduler.entites.Data;
import cz.jr.trailtour.scheduler.entites.Gender;
import cz.jr.trailtour.scheduler.trailtour.entities.TrailtourStage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Created by Jiří Rýdel on 5/1/20, 7:09 PM
 */
public class StravaSegmentProcessor {

    private final Logger LOG = LogManager.getLogger(StravaSegmentProcessor.class);

    private final LocalDate dateFrom;
    private final LocalDate dateTo;
    private final StravaDownloader stravaDownloader;
    private final StravaParser stravaParser;

    public StravaSegmentProcessor(LocalDate dateFrom, LocalDate dateTo, StravaDownloader stravaDownloader, StravaParser stravaParser) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.stravaDownloader = stravaDownloader;
        this.stravaParser = stravaParser;
    }

    public void process(TrailtourStage stage, Data data) {
        for (Gender gender : Gender.values()) {
            int page = 1;
            while (true) {
                try {
                    String url = stage.getStravaUrl() + "/leaderboard?filter=overall&gender=" + gender.toString() + "&page=" + page++ + "&per_page=100&partial=true";
                    Document document = stravaDownloader.get(url);
                    data.setStravaRequestCount(1 + data.getStravaRequestCount());
                    try {
                        stravaParser.parseSegment(document, stage, data, dateFrom, dateTo);
                    } catch (IndexOutOfBoundsException | NoSegmentDataException e) {
                        break;
                    }
                } catch (InterruptedException | IOException e) {
                    LOG.error("Error.", e);
                }
            }
        }
    }
}
