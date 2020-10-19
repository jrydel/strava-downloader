package cz.jr.trailtour.scheduler;

import cz.jr.trailtour.scheduler.entites.*;
import cz.jr.trailtour.scheduler.trailtour.entities.TrailtourStage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Jiří Rýdel on 5/25/20, 11:44 AM
 */
public class PointsProcessor {

    public void process(List<TrailtourStage> stageList, Data data) {
        for (TrailtourStage stage : stageList) {
            for (Athlete athlete : data.getAthleteMap().values()) {
                Points trailtourPoints = athlete.getTrailtourPointsMap().get(stage.getNumber());
                Activity activity = athlete.getActivityMap().get(stage.getNumber());

                // nema TT body ani aktivitu
                if (trailtourPoints == null && activity == null) {
                    continue;
                }

                Points points = athlete.getPointsMap().computeIfAbsent(stage.getNumber(), k -> new Points());
                if (trailtourPoints != null) {
                    points.setPoints(trailtourPoints.getPoints());
                    points.setTime(trailtourPoints.getTime());
                } else {
                    points.setTime(activity.getTime());
                }
            }
            for (Club club : data.getClubMap().values()) {
                Points trailtourPoints = club.getTrailtourPointsMap().get(stage.getNumber());

                // ma TT body
                if (trailtourPoints != null) {
                    Points points = club.getPointsMap().computeIfAbsent(stage.getNumber(), k -> new Points());
                    points.setPosition(trailtourPoints.getPosition());
                    points.setPoints(trailtourPoints.getPoints());
                }
            }
        }

        // zavodnici - etapy
        {
            for (Gender gender : Gender.values()) {
                for (TrailtourStage stage : stageList) {
                    computePoints(
                            data.getAthleteMap().values().stream()
                                    .filter(athlete -> athlete.getGender().equals(gender))
                                    .filter(athlete -> athlete.getPointsMap().containsKey(stage.getNumber()))
                                    .collect(Collectors.toList()),
                            stage.getNumber()
                    );
                    computePosition(
                            data.getAthleteMap().values().stream()
                                    .filter(athlete -> athlete.getGender().equals(gender))
                                    .filter(athlete -> athlete.getPointsMap().containsKey(stage.getNumber()))
                                    .map(athlete -> athlete.getPointsMap().get(stage.getNumber()))
                                    .sorted(Comparator.comparing(Points::getPoints).reversed())
                                    .collect(Collectors.toList())
                    );
                }
            }
        }
        // zavodnici - celkove
        {
            computeLadderPoints(data.getAthleteMap().values());
            for (Gender gender : Gender.values()) {
                computePosition(
                        data.getAthleteMap().values().stream()
                                .filter(athlete -> athlete.getGender().equals(gender))
                                .map(Athlete::getPoints)
                                .sorted(Comparator.comparingDouble(Points::getPoints).reversed())
                                .collect(Collectors.toList())
                );
            }
        }

        // kluby - etapy
        {
            for (TrailtourStage stage : stageList) {
                for (Club club : data.getClubMap().values()) {
                    double sum = data.getAthleteMap().values().stream()
                            .filter(athlete -> club.getName().equals(athlete.getClub()))
                            .filter(athlete -> athlete.getPointsMap().containsKey(stage.getNumber()))
                            .mapToDouble(athlete -> athlete.getPointsMap().get(stage.getNumber()).getPoints())
                            .sum();
                    club.getPointsMap().computeIfAbsent(stage.getNumber(), k -> new Points()).setPoints(sum);
                }

                computePosition(
                        data.getClubMap().values().stream()
                                .filter(club -> club.getPointsMap().containsKey(stage.getNumber()))
                                .map(club -> club.getPointsMap().get(stage.getNumber()))
                                .sorted(Comparator.comparing(Points::getPoints).reversed())
                                .collect(Collectors.toList())
                );
            }
        }

        // kluby - celkove
        {
            for (Club club : data.getClubMap().values()) {
                double sum = club.getPointsMap().values().stream()
                        .mapToDouble(Points::getPoints).sum();
                club.getPoints().setPoints(sum);
            }
            computePosition(
                    data.getClubMap().values().stream()
                            .map(Club::getPoints)
                            .sorted(Comparator.comparingDouble(Points::getPoints).reversed())
                            .collect(Collectors.toList())
            );
        }
    }

    private void computePoints(Collection<Athlete> collection, int stageNumber) {
        List<Athlete> temp = collection.stream().sorted(Comparator.comparingInt(a -> a.getPointsMap().get(stageNumber).getTime()))
                .limit(3).collect(Collectors.toList());
        boolean recomputeAll = temp.stream().anyMatch(athlete -> athlete.getPointsMap().get(stageNumber).getPoints() == null);
        double average = temp.stream().mapToInt(athlete -> athlete.getPointsMap().get(stageNumber).getTime()).average().orElse(0D);
        for (Athlete athlete : collection) {
            Points points = athlete.getPointsMap().get(stageNumber);
            if (!recomputeAll && points.getPoints() != null) {
                continue;
            }

            double result = 100 * (2.5 - (athlete.getPointsMap().get(stageNumber).getTime() / average));
            result = result < 0 ? 0D : result;
            points.setPoints(result);
        }
    }

    private void computeLadderPoints(Collection<Athlete> collection) {
        for (Athlete athlete : collection) {
            if (athlete.getPointsMap().isEmpty()) {
                athlete.getPoints().setPoints(0D);
            } else {
                double sum = athlete.getPointsMap().values().stream().mapToDouble(Points::getPoints).sum();
                athlete.getPoints().setPoints(sum);
            }
        }
    }

    private void computePosition(List<Points> list) {
        Points previous = null;
        for (int i = 0; i < list.size(); i++) {
            Points current = list.get(i);
            if (previous != null && Objects.equals(previous.getPoints(), current.getPoints())) {
                current.setPosition(previous.getPosition());
            } else {
                current.setPosition(1 + i);
            }
            previous = current;
        }
    }
}
