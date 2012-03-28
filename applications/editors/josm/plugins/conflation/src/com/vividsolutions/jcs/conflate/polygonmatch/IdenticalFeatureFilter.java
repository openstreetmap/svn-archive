package com.vividsolutions.jcs.conflate.polygonmatch;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;

/**
 * Filters out matches where features are identical.
 */
public class IdenticalFeatureFilter implements FeatureMatcher {
    
  /**
   * Filters out matches where features are identical.
   * @param target the Feature which is the target
   * @param candidates a Matches object created by another FeatureMatcher
   * @return the candidates that aren't identical to the target
   */
    @Override
  public Matches match(Feature target, FeatureCollection candidates) {
    Matches survivors = new Matches(candidates.getFeatureSchema());
    Matches allMatches = (Matches) candidates;
    for (int i = 0; i < allMatches.size(); i++) {
      if (!allMatches.getFeature(i).equals(target)) {
        survivors.add(allMatches.getFeature(i), allMatches.getScore(i));
      }
    }
    return survivors;
  }
}