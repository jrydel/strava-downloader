package cz.jr.stravadownloader;

import cz.jr.trailtour.scheduler.entites.Data;
import cz.jr.trailtour.scheduler.trailtour.entities.TrailtourStage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Created by Jiří Rýdel on 5/4/20, 12:34 PM
 */
public class StravaProcessor {

    private static final Logger LOG = LogManager.getLogger(StravaProcessor.class);

    public void process(LocalDate dateFrom, LocalDate dateTo, List<TrailtourStage> stageList, Data data) {
        StravaDownloader stravaDownloader = new StravaDownloader();
        StravaParser stravaParser = new StravaParser();
        StravaSegmentProcessor stravaSegmentProcessor = new StravaSegmentProcessor(dateFrom, dateTo, stravaDownloader, stravaParser);
        try {
            stravaDownloader.logIn();
            for (TrailtourStage stage : stageList) {
                try {
                    stravaSegmentProcessor.process(stage, data);
                } catch (Throwable t) {
                    // chyby na segmentech
                    LOG.error("Error processing segment {}", stage.getStravaUrl(), t);
                }
            }
        } catch (IOException | InterruptedException e) {
            LOG.error("Error.", e);
        } finally {
            try {
                stravaDownloader.logOut();
            } catch (IOException | InterruptedException e) {
                LOG.error("Error.", e);
            }
        }
    }
}
