

/*
 * The JCS Conflation Suite (JCS) is a library of Java classes that
 * can be used to build automated or semi-automated conflation solutions.
 *
 * Copyright (C) 2003 Vivid Solutions
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package com.vividsolutions.jcs.conflate.polygonmatch;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import java.util.Iterator;

/**
 * Base class of FeatureMatchers that compare the target to each candidate
 * in turn -- the comparisons only use one candidate at a time.
 */
public abstract class IndependentCandidateMatcher implements FeatureMatcher {

  public IndependentCandidateMatcher() {
  }

    @Override
  public Matches match(Feature target, FeatureCollection candidates) {
    Matches matches = new Matches(candidates.getFeatureSchema());
    for (Iterator i = candidates.iterator(); i.hasNext(); ) {
      Feature candidate = (Feature) i.next();
      double score = match(target.getGeometry(), candidate.getGeometry());
      if (score > 0) { matches.add(candidate, score); }
    }
    return matches;
  }

  /**
   * Compares the target to the candidate feature. Called for each candidate
   * feature by #match(Feature, FeatureCollection).
   * @param target the feature to match
   * @param candidate the feature to compare with the target
   * @return a score from 0 to 1 indicating how well the candidate matches the
   * target
   */
  public abstract double match(Geometry target, Geometry candidate);
}
