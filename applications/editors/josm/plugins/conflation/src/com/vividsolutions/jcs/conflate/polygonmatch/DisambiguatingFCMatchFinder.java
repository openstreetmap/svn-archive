package com.vividsolutions.jcs.conflate.polygonmatch;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.task.TaskMonitor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
/**
 * Enforces a one-to-one relationship between target features and
 * matched candidate features, in the returned result set.
 * "Aggressive" because 2nd, 3rd, 4th, etc. best
 * matches are tried if the 1st, 2nd, 3rd, etc. match is "taken" by another 
 * feature.
 */
public class DisambiguatingFCMatchFinder implements FCMatchFinder {
    private FCMatchFinder matchFinder;
    public DisambiguatingFCMatchFinder(FCMatchFinder matchFinder) {
        this.matchFinder = matchFinder;
    }
    @Override
    public Map match(
        FeatureCollection targetFC,
        FeatureCollection candidateFC,
        TaskMonitor monitor) {
        ArrayList targets = new ArrayList();
        ArrayList candidates = new ArrayList();
        ArrayList scores = new ArrayList();
        SortedSet matchSet = DisambiguationMatch.createDisambiguationMatches(matchFinder.match(targetFC, candidateFC, monitor), monitor);
        monitor.report("Discarding inferior matches");
        int j = 0;
        for (Iterator i = matchSet.iterator(); i.hasNext();) {
            DisambiguationMatch match = (DisambiguationMatch) i.next();
            monitor.report(++j, matchSet.size(), "matches");
            if (targets.contains(match.getTarget()) || candidates.contains(match.getCandidate())) {
                continue;
            }
            targets.add(match.getTarget());
            candidates.add(match.getCandidate());
            scores.add(new Double(match.getScore()));
        }
        //Re-add filtered-out targets, but with zero-score matches [Jon Aquino]
        Map targetToMatchesMap =
            AreaFilterFCMatchFinder.blankTargetToMatchesMap(
                targetFC.getFeatures(),
                candidateFC.getFeatureSchema());
        for (int i = 0; i < targets.size(); i++) {
            Matches matches = new Matches(candidateFC.getFeatureSchema());
            matches.add((Feature)candidates.get(i), ((Double)scores.get(i)).doubleValue());
            targetToMatchesMap.put(targets.get(i), matches);
        }
        return targetToMatchesMap;
    }
}