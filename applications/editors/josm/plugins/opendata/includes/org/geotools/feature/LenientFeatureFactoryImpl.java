package org.geotools.feature;

public class LenientFeatureFactoryImpl extends AbstractFeatureFactoryImpl { // NO_UCD
    public LenientFeatureFactoryImpl() {
        validating = false;
    }
}
