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
package org.geotools.feature.type;

import java.util.List;

import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureTypeFactory;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

/**
 * This implementation is capable of creating a good default implementation of
 * the Types used in the feature model.
 * <p>
 * The implementation focus here is on corretness rather then efficiency or even
 * strict error messages. The code serves as a good example, but is not
 * optimized for any particular use.
 * </p>
 * 
 * @author Jody Garnett
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/feature/type/FeatureTypeFactoryImpl.java $
 */
public class FeatureTypeFactoryImpl implements FeatureTypeFactory {

	/** Rely on setter injection */
	public FeatureTypeFactoryImpl() {
	}

	public AttributeDescriptor createAttributeDescriptor(AttributeType type,
		Name name, int minOccurs, int maxOccurs, boolean isNillable, Object defaultValue) {
		
		return new AttributeDescriptorImpl(type, name, minOccurs, maxOccurs, isNillable,defaultValue);
	}

	public GeometryDescriptor createGeometryDescriptor(GeometryType type,
	        Name name, int minOccurs, int maxOccurs, boolean isNillable,
	        Object defaultValue) {
	    return new GeometryDescriptorImpl(type, name, minOccurs, maxOccurs, isNillable, defaultValue);
	}
	
	public AttributeType createAttributeType(Name name, Class binding,
			boolean isIdentifiable, boolean isAbstract, List restrictions,
			AttributeType superType, InternationalString description) {

		return new AttributeTypeImpl(name, binding, isIdentifiable, isAbstract,
				restrictions, superType, description);
	}

	public GeometryType createGeometryType(Name name, Class binding,
			CoordinateReferenceSystem crs, boolean isIdentifiable,
			boolean isAbstract, List restrictions, AttributeType superType,
			InternationalString description) {

		return new GeometryTypeImpl(name, binding, crs, isIdentifiable,
				isAbstract, restrictions, superType, description);
	}

	public SimpleFeatureType createSimpleFeatureType(Name name,
	        List<AttributeDescriptor> schema,
	        GeometryDescriptor defaultGeometry, boolean isAbstract,
	        List<Filter> restrictions, AttributeType superType,
	        InternationalString description) {
	    
	    return new SimpleFeatureTypeImpl(name,schema,defaultGeometry,isAbstract,
            restrictions,superType,description);
	}
}
