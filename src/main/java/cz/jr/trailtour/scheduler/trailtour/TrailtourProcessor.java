package cz.jr.trailtour.scheduler.trailtour;

import cz.jr.trailtour.scheduler.HttpUtils;
import cz.jr.trailtour.scheduler.entites.Data;
import cz.jr.trailtour.scheduler.trailtour.entities.TrailtourStage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

/**
 * Created by Jiří Rýdel on 5/3/20, 12:32 PM
 */
public class TrailtourProcessor {

    private static final Logger LOG = LogManager.getLogger(TrailtourProcessor.class);

    private final TrailtourParser parser = new TrailtourParser();

    public void process(List<TrailtourStage> stageList, Data data, String ladderUrl) {
        try {
            Document ladderDocument = Jsoup.parse(HttpUtils.downloadPage(ladderUrl));
            parser.parseLadder(ladderDocument, data);
        } catch (InterruptedException | IOException e) {
            LOG.error("Error.", e);
        }

        for (TrailtourStage stage : stageList) {
            try {
                Document stageDocument = Jsoup.parse(HttpUtils.downloadPage(stage.getTrailtourUrl()));
                parser.parseStage(stageDocument, stage, data);
                processStageData(stage, data);
            } catch (InterruptedException | IOException e) {
                LOG.error("Error.", e);
            }
        }
    }

    private void processStageData(TrailtourStage stage, Data basicData) throws IOException, InterruptedException {
        if (stage.getTrailtourUrl() == null) {
            return;
        }
        String dataUrl = "https://www.strava.com/stream/segments/" + stage.getStravaUrl().replace("https://www.strava.com/segments/", "") + "?streams%5B%5D=latlng&streams%5B%5D=distance&streams%5B%5D=altitude";
        String data = HttpUtils.downloadPage(dataUrl);
        basicData.setStravaRequestCount(1 + basicData.getStravaRequestCount());
        stage.setStravaData(data);
    }
}
