

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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A FeatureCollection that stores the "score" of each Feature.  The score is
 * a number between 0.0 and 1.0 that indicates the confidence of a match.
 */
public class Matches implements FeatureCollection, Cloneable {

    /**
     * Creates a Matches object.
     * @param schema metadata applicable to the features that will be stored in
     * this Matches object
     */
    public Matches(FeatureSchema schema) {
        dataset = new FeatureDataset(schema);
    }

    @Override
    protected Object clone() {
        Matches clone = new Matches(dataset.getFeatureSchema());
        for (int i = 0; i < size(); i++) {
            clone.add(getFeature(i), getScore(i));
        }
        return clone;
    }

    /**
     * Creates a Matches object, initialized with the given Feature's.
     * @param schema metadata applicable to the features that will be stored in
     * this Matches object
     * @param features added to the Matches, each with the max score (1.0)
     */
    public Matches(FeatureSchema schema, List features) {
        this(schema);
        for (Iterator i = features.iterator(); i.hasNext();) {
            Feature match = (Feature) i.next();
            add(match, 1);
        }
    }

    private FeatureDataset dataset;
    private ArrayList scores = new ArrayList();

    /**
     * This method is not supported, because added features need to be associated
     * with a score. Use #add(Feature, double) instead.
     * @param feature a feature to add as a match
     * @see #add(Feature, double)
     */
    public void add(Feature feature) {
        throw new UnsupportedOperationException("Use #add(feature, score) instead");
    }

    /**
     * This method is not supported, because added features need to be associated
     * with a score. Use #add(Feature, double) instead.
     */
    public void addAll(Collection features) {
        throw new UnsupportedOperationException("Use #add(feature, score) instead");
    }

    /**
     * This method is not supported, because added features need to be associated
     * with a score. Use #add(Feature, double) instead.
     * @param feature a feature to add as a match
     * @see #add(Feature, double)
     */
    public void add(int index, Feature feature) {
        throw new UnsupportedOperationException("Use #add(feature, score) instead");
    }

    /**
     * This method is not supported, because Matches should not normally need to
     * have matches removed.
     */
    public Collection remove(Envelope envelope) {
        //If we decide to implement this, remember to remove the corresponding
        //score. [Jon Aquino]
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported, because Matches should not normally need to
     * have matches removed.
     */
    public void clear() {
        //If we decide to implement this, remember to remove the corresponding
        //score. [Jon Aquino]
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported, because Matches should not normally need to
     * have matches removed.
     */
    public void removeAll(Collection features) {
        //If we decide to implement this, remember to remove the corresponding
        //score. [Jon Aquino]
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported, because Matches should not normally need to
     * have matches removed.
     * @param feature a feature to remove
     */
    public void remove(Feature feature) {
        //If we decide to implement this, remember to remove the corresponding
        //score. [Jon Aquino]
        throw new UnsupportedOperationException();
    }
    /**
     * Adds a match. Features with zero-scores are ignored.
     * @param feature a feature to add as a match
     * @param score the confidence of the match, ranging from 0 to 1
     */
    public void add(Feature feature, double score) {
        Assert.isTrue(0 <= score && score <= 1, "Score = " + score);
        if (score == 0) {
            return;
        }
        scores.add(new Double(score));
        dataset.add(feature);
        if (score > topScore) {
            topScore = score;
            topMatch = feature;
        }
    }

    private Feature topMatch;
    private double topScore = 0;

    public double getTopScore() {
        return topScore;
    }

    /**
     * @return the feature with the highest score
     */
    public Feature getTopMatch() {
        return topMatch;
    }

    /**
     * Returns the score of the ith feature
     * @param i 0, 1, 2, ...
     * @return the confidence of the ith match
     */
    public double getScore(int i) {
        return ((Double) scores.get(i)).doubleValue();
    }

    public FeatureSchema getFeatureSchema() {
        return dataset.getFeatureSchema();
    }

    public Envelope getEnvelope() {
        return dataset.getEnvelope();
    }

    public int size() {
        return dataset.size();
    }

    public boolean isEmpty() {
        return dataset.isEmpty();
    }

    public Feature getFeature(int index) {
        return dataset.getFeature(index);
    }

    public List getFeatures() {
        return dataset.getFeatures();
    }

    public Iterator iterator() {
        return dataset.iterator();
    }

    public List query(Envelope envelope) {
        return dataset.query(envelope);
    }
}
