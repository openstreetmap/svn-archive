package com.vividsolutions.jcs.conflate.polygonmatch;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.task.TaskMonitor;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

class DisambiguationMatch implements Comparable {
    private Feature target;
    private Feature candidate;
    private double score;
    public double getScore() {
        return score;
    }

    public Feature getCandidate() {
        return candidate;
    }

    public Feature getTarget() {
        return target;
    }

    public DisambiguationMatch(Feature target, Feature candidate, double score) {
        this.target = target;
        this.candidate = candidate;
        this.score = score;
    }
    @Override
    public int compareTo(Object o) {
        DisambiguationMatch other = (DisambiguationMatch) o;
        //Highest scores first. [Jon Aquino]
        if (score > other.score) { return -1; }
        if (score < other.score) { return 1; }
        if (target.compareTo(other.target) != 0) { return target.compareTo(other.target); } 
        if (candidate.compareTo(other.candidate) != 0) { return candidate.compareTo(other.candidate); }
        Assert.shouldNeverReachHere("Unexpected duplicate match?"); 
        return -1;
    }
    public static SortedSet createDisambiguationMatches(Map targetToMatchesMap, TaskMonitor monitor) {
        TreeSet set = new TreeSet();
        monitor.report("Sorting scores");
        int k = 0;
        for (Iterator i = targetToMatchesMap.keySet().iterator(); i.hasNext();) {
            Feature target = (Feature) i.next();
            Matches matches = (Matches) targetToMatchesMap.get(target);
            monitor.report(++k, targetToMatchesMap.keySet().size(), "features");
            for (int j = 0; j < matches.size(); j++) {
                set.add(new DisambiguationMatch(target, matches.getFeature(j), matches.getScore(j)));
            }
        }
        return set;
    }        
}