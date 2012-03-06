/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.feature;


/**
 * Factory for creating instances of the Attribute family of classes.
 * 
 * @author Andrea Aime
 * 
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/feature/ValidatingFeatureFactoryImpl.java $
 * @version $Id: ValidatingFeatureFactoryImpl.java 37298 2011-05-25 05:16:15Z mbedward $
 */
public class ValidatingFeatureFactoryImpl extends AbstractFeatureFactoryImpl { // NO_UCD
    
    public ValidatingFeatureFactoryImpl() {
        validating = true;
    }
 }

