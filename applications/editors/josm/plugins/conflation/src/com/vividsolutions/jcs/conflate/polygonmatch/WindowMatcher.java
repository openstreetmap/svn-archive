

/*
 * The Java Conflation Suite (JCS) is a library of Java classes that
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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.geom.EnvelopeUtil;

/**
 * Quickly filters out shapes that lie outside a given distance from the feature's
 * envelope.
 */
public class WindowMatcher implements FeatureMatcher {

  /**
   * Creates a new WindowMatcher, with envelope buffering.
   * @param buffer for each feature, the window will be the envelope extended on each
   * side by this amount
   */
  public WindowMatcher(double buffer) {
    setBuffer(buffer);
  }

  /**
   * Creates a WindowMatcher, with no envelope buffering.
   */
  public WindowMatcher() {}

  /**
   * Sets the amount by which to buffer the envelope
   * @param buffer for each feature, the window will be the envelope extended on each
   * side by this amount
   */
  public void setBuffer(double buffer) { this.buffer = buffer; }

  private double buffer;
  /**
   * Quickly filters out shapes that lie outside a given distance from the feature's
   * envelope.
   * @param target the feature to match
   * @param candidates the features to search for matches
   * @return the candidates with envelopes intersecting the window. Each will
   * have a score of 1.
   */
    @Override
  public Matches match(Feature target, FeatureCollection candidates) {
    Envelope window = new Envelope(target.getGeometry().getEnvelopeInternal());
    window = EnvelopeUtil.expand(window, buffer);
    return new Matches(candidates.getFeatureSchema(), candidates.query(window));
  }
}
