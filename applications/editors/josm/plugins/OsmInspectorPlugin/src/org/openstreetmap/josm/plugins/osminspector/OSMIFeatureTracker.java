package org.openstreetmap.josm.plugins.osminspector;

import java.util.HashMap;
import java.util.Iterator;

import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class OSMIFeatureTracker {

	private HashMap<Long, SimpleFeature> hashFeatures;
	private MemoryFeatureCollection features;

	public OSMIFeatureTracker(
			FeatureCollection<SimpleFeatureType, SimpleFeature> featuresIn) {
		hashFeatures = new HashMap<>();
		features = new MemoryFeatureCollection(featuresIn.getSchema());

		for (Iterator<?> it = features.iterator(); it.hasNext();) {
			SimpleFeature element = (SimpleFeature) it.next();
			try {
				Long ID = (Long.parseLong((String) element
						.getAttribute("problem_id")));

				if (!hashFeatures.containsKey(ID)) {
					hashFeatures.put(ID, element);
					features.add(element);
				}
			} catch (NumberFormatException e) {
				continue;
			}
		}

		try (FeatureIterator<SimpleFeature> it = featuresIn.features()) {
		    while (it.hasNext()) {
		        features.add(it.next());
		    }
		}
	}

	public boolean mergeFeatures(
			FeatureCollection<SimpleFeatureType, SimpleFeature> newFeatures) {
	    try (FeatureIterator<SimpleFeature> it = newFeatures.features()) {
	        while (it.hasNext()) {
    			SimpleFeature element = (SimpleFeature) it.next();
    			try {
    				Long ID = (Long.parseLong((String) element
    						.getAttribute("problem_id")));
    
    				if (!hashFeatures.containsKey(ID)) {
    					hashFeatures.put(ID, element);
    					features.add(element);
    				}
    			} catch (NumberFormatException e) {
    				continue;
    			}
    		}
	    }

		return true;
	}

	public HashMap<Long, SimpleFeature> getFeatureHash() {
		return hashFeatures;
	}

	public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatures() {
		return features;
	}
}
