/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.feature.visitor;

import org.opengis.feature.FeatureVisitor;

/**
 * A visitor which performs a calculation on a FeatureCollection. A FeatureCalc
 * will not modify the features visited.
 *
 * @author Cory Horner, Refractions
 *
 * @see FeatureVisitor
 * @since 2.2.M2
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/feature/visitor/FeatureCalc.java $
 */
public interface FeatureCalc extends FeatureVisitor {
}
