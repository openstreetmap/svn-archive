

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

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Composes several FeatureMatchers into one. Candidate features are whittled
 * down by applying each FeatureMatcher.
 * <P>
 * Note: Only the last FeatureMatcher's scores are preserved; the other scores
 * are lost. However, this behaviour should be acceptable for most situations
 * Typically you use the Chained Matcher to do some initial filtering before
 * the "real" matching. The scores from this initial filtering are usually
 * ignored (they're usually just 1 or 0, as in the case of WindowFilter).
 */
public class ChainMatcher implements FeatureMatcher {

  /**
   * Creates a ChainMatcher composed of the given matchers.
   * @param matchers the matchers to link together
   */
  public ChainMatcher(FeatureMatcher[] matchers) {
        this.matchers.addAll(Arrays.asList(matchers));
  }

  private ArrayList matchers = new ArrayList();

  /**
   * Applies the FeatureMatchers, in sequence, to the list of candidates.
   * @param target the feature to match
   * @param candidates the features to search for matches
   * @return the candidates surviving all the FeatureMatchers. The scores are
   * those returned by the last FeatureMatcher.
   */
    @Override
  public Matches match(Feature target, FeatureCollection candidates) {
    Matches survivors = new Matches(
        candidates.getFeatureSchema(), candidates.getFeatures());
    for (Iterator i = matchers.iterator(); i.hasNext(); ) {
      FeatureMatcher matcher = (FeatureMatcher) i.next();
      survivors = matcher.match(target, survivors);
    }
    return survivors;
  }
}
