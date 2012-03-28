

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

import com.vividsolutions.jts.util.Assert;

/**
 * Generic model for a bar graph.
 */
public class Histogram {

  //<<TODO:DESIGN>> Add functions to make Histograms n-dimensional. But leave
  //the original 1D API intact, for the majority of users who need just 1D
  //[Jon Aquino]

  /**
   * Creates a Histogram with the given number of bins.
   * @param binCount the number of bins
   */
  public Histogram(int binCount) {
    bins = new double[binCount];
  }

  private double[] bins;

  /**
   * Adds a score to the ith bin.
   * @param i 0, 1, 2, ...
   * @param score the amount by which the ith bin will be incremented
   */
  public void addToBinScore(int i, double score) {
    bins[i] += score;
  }

  /**
   * Returns the score for the ith bin.
   * @param i 0, 1, 2, ...
   * @return the score for the ith bin
   */
  public double getBinScore(int i) {
    return bins[i];
  }

  /**
   * Adds the scores from all the bins.
   * @return the sum of the scores in each bin. If the sum is 1, this
   * histogram is said to be "normalized".
   */
  public double getTotalScore() {
    double total = 0;
    for (int i = 0; i < getBinCount(); i++) {
      total += getBinScore(i);
    }
    return total;
  }

  /**
   * Returns the number of bins.
   * @return the number of bins that make up this Histogram
   */
  public int getBinCount() {
    return bins.length;
  }

  /**
   * Returns the symmetric difference between this Histogram and another
   * Histogram.
   * @param other the Histogram with which the symmetric difference will be
   * computed
   * @return the sum of the symmetric differences between corresponding
   * bins. The symmetric difference between two corresponding bins is
   * simply the absolute value of the difference.
   */
  public double symDiff(Histogram other) {
    Assert.isTrue(getBinCount() == other.getBinCount());
    double symDiff = 0;
    for (int i = 0; i < getBinCount(); i++) {
      symDiff += Math.abs(getBinScore(i) - other.getBinScore(i));
    }
    return symDiff;
  }

  /**
   * Adds another Histogram's bin scores to this Histogram's bin scores.
   * The number of bins must be the same in both Histograms.
   * @param other the Histogram whose scores will be added to this Histogram
   */
  public void add(Histogram other) {
    Assert.isTrue(getBinCount() == other.getBinCount());
    for (int i = 0; i < getBinCount(); i++) {
      addToBinScore(i, other.getBinScore(i));
    }
  }

}
