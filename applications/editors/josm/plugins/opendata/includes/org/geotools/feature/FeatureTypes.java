/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.factory.FactoryRegistryException;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Utility methods for working against the FeatureType interface.
 * <p>
 * Many methods from DataUtilities should be refractored here.
 * </p>
 * <p>
 * Responsibilities:
 * <ul>
 * <li>Schema construction from String spec
 * <li>Schema Force CRS
 * </ul>
 *
 * @author Jody Garnett, Refractions Research
 * @since 2.1.M3
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/feature/FeatureTypes.java $
 */
public class FeatureTypes {

	/** the default namespace for feature types */
	//public static final URI = GMLSchema.NAMESPACE;
	public static final URI DEFAULT_NAMESPACE;
	static {
		URI uri;
		try {
			uri = new URI( "http://www.opengis.net/gml" );
		}
		catch (URISyntaxException e) {
			uri = null;	//will never happen
		}
		DEFAULT_NAMESPACE = uri;
	}

	/** abstract base type for all feature types */
    public final static SimpleFeatureType ABSTRACT_FEATURE_TYPE;
    static {
        SimpleFeatureType featureType = null;
        try {
            featureType = FeatureTypes.newFeatureType(null, "Feature",new URI("http://www.opengis.net/gml"), true);
        }
        catch(Exception e ) {
            //shold not happen
        }
        ABSTRACT_FEATURE_TYPE = featureType;
    }

    /**
     * Forces the specified CRS on all geometry attributes
     * @param schema the original schema
     * @param crs the forced crs
     * @return
     * @throws SchemaException
     */
    public static SimpleFeatureType transform( SimpleFeatureType schema, CoordinateReferenceSystem crs )
        throws SchemaException {
        return transform(schema, crs, false);
    }

    /**
     * Forces the specified CRS on geometry attributes (all or some, depends on the parameters).
     * @param schema the original schema
     * @param crs the forced crs
     * @param forceOnlyMissing if true, will force the specified crs only on the attributes that
     *        do miss one
     * @return
     * @throws SchemaException
     */
    public static SimpleFeatureType transform( SimpleFeatureType schema, CoordinateReferenceSystem crs, boolean forceOnlyMissing)
            throws SchemaException {
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName(schema.getTypeName());
        tb.setNamespaceURI( schema.getName().getNamespaceURI() );
        tb.setAbstract(schema.isAbstract());

        for( int i = 0; i < schema.getAttributeCount(); i++ ) {
            AttributeDescriptor attributeType = schema.getDescriptor(i);
            if (attributeType instanceof GeometryDescriptor) {
                GeometryDescriptor geometryType = (GeometryDescriptor) attributeType;

                tb.descriptor( geometryType );
                if ( !forceOnlyMissing || geometryType.getCoordinateReferenceSystem() == null ) {
                    tb.crs( crs );
                }

                tb.add( geometryType.getLocalName(), geometryType.getType().getBinding() );
            } else {
                tb.add(attributeType);
            }
        }
        if (schema.getGeometryDescriptor() != null) {
            tb.setDefaultGeometry(schema.getGeometryDescriptor().getLocalName());
        }

        tb.setSuperType((SimpleFeatureType) schema.getSuper());

        return tb.buildFeatureType();
    }

    /**
     * The most specific way to create a new FeatureType.
     *
     * @param types The AttributeTypes to create the FeatureType with.
     * @param name The typeName of the FeatureType. Required, may not be null.
     * @param ns The namespace of the FeatureType. Optional, may be null.
     * @param isAbstract True if this created type should be abstract.
     * @param superTypes A Collection of types the FeatureType will inherit from. Currently, all
     *        types inherit from feature in the opengis namespace.
     * @return A new FeatureType created from the given arguments.
     * @throws FactoryRegistryException If there are problems creating a factory.
     * @throws SchemaException If the AttributeTypes provided are invalid in some way.
     */
    public static SimpleFeatureType newFeatureType( AttributeDescriptor[] types, String name, URI ns,
            boolean isAbstract, SimpleFeatureType[] superTypes ) throws FactoryRegistryException,
            SchemaException {
        return newFeatureType(types, name, ns, isAbstract, superTypes, null);
    }

    /**
     * The most specific way to create a new FeatureType.
     *
     * @param types The AttributeTypes to create the FeatureType with.
     * @param name The typeName of the FeatureType. Required, may not be null.
     * @param ns The namespace of the FeatureType. Optional, may be null.
     * @param isAbstract True if this created type should be abstract.
     * @param superTypes A Collection of types the FeatureType will inherit from. Currently, all
     *        types inherit from feature in the opengis namespace.
     * @return A new FeatureType created from the given arguments.
     * @throws FactoryRegistryException If there are problems creating a factory.
     * @throws SchemaException If the AttributeTypes provided are invalid in some way.
     */
    public static SimpleFeatureType newFeatureType( AttributeDescriptor[] types, String name, URI ns,
            boolean isAbstract, SimpleFeatureType[] superTypes, AttributeDescriptor defaultGeometry )
            throws FactoryRegistryException, SchemaException {
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();

        tb.setName(name);
        tb.setNamespaceURI(ns);
        tb.setAbstract(isAbstract);
        if(types != null) {
            tb.addAll(types);
        }

        if ( defaultGeometry != null ) {
            //make sure that the default geometry was one of the types specified
            boolean add = true;
            for ( int i = 0; i < types.length; i++ ) {
                if (types[i] == defaultGeometry) {
                    add = false;
                    break;
                }
            }
            if ( add ) {
                tb.add(defaultGeometry);
            }
            tb.setDefaultGeometry(defaultGeometry.getLocalName());
        }
        if ( superTypes != null && superTypes.length > 0) {
            if ( superTypes.length > 1 ) {
                throw new SchemaException("Can only specify a single super type");
            }
            tb.setSuperType(superTypes[0]);

        }
        else {
            //use the default super type
            tb.setSuperType(ABSTRACT_FEATURE_TYPE);
        }
        return tb.buildFeatureType();
    }

    /**
     * The most specific way to create a new FeatureType.
     *
     * @param types The AttributeTypes to create the FeatureType with.
     * @param name The typeName of the FeatureType. Required, may not be null.
     * @param ns The namespace of the FeatureType. Optional, may be null.
     * @param isAbstract True if this created type should be abstract.
     * @param superTypes A Collection of types the FeatureType will inherit from. Currently, all
     *        types inherit from feature in the opengis namespace.
     * @return A new FeatureType created from the given arguments.
     * @throws FactoryRegistryException If there are problems creating a factory.
     * @throws SchemaException If the AttributeTypes provided are invalid in some way.
     */
    public static SimpleFeatureType newFeatureType( AttributeDescriptor[] types, String name, URI ns,
            boolean isAbstract, SimpleFeatureType[] superTypes, GeometryDescriptor   defaultGeometry )
            throws FactoryRegistryException, SchemaException {
        return newFeatureType(types,name,ns,isAbstract,superTypes,(AttributeDescriptor)defaultGeometry);
    }

    /**
     * Create a new FeatureType with the given AttributeTypes. A short cut for calling
     * <code>newFeatureType(types,name,ns,isAbstract,null)</code>.
     *
     * @param types The AttributeTypes to create the FeatureType with.
     * @param name The typeName of the FeatureType. Required, may not be null.
     * @param ns The namespace of the FeatureType. Optional, may be null.
     * @param isAbstract True if this created type should be abstract.
     * @return A new FeatureType created from the given arguments.
     * @throws FactoryRegistryException If there are problems creating a factory.
     * @throws SchemaException If the AttributeTypes provided are invalid in some way.
     */
    public static SimpleFeatureType newFeatureType( AttributeDescriptor[] types, String name, URI ns,
            boolean isAbstract ) throws FactoryRegistryException, SchemaException {
        return newFeatureType(types, name, ns, isAbstract, null);
    }

    /**
     * Walks up the type hierarchy of the feature returning all super types of the specified feature
     * type. The search terminates when a non-FeatureType or null is found. The original featureType
     * is not included as an ancestor, only its strict ancestors.
     */
    public static List<FeatureType> getAncestors(FeatureType featureType) {
        List<FeatureType> ancestors = new ArrayList<FeatureType>();
        while (featureType.getSuper() instanceof FeatureType) {
            FeatureType superType = (FeatureType) featureType.getSuper();
            ancestors.add(superType);
            featureType = superType;
        }
        return ancestors;
    }

    /** Exact equality based on typeNames, namespace, attributes and ancestors, including the user maps contents */
    public static boolean equalsExact( SimpleFeatureType typeA, SimpleFeatureType typeB ) {
        return equals(typeA, typeB, true);
    }
    
    /** Exact equality based on typeNames, namespace, attributes and ancestors */
    static boolean equals( SimpleFeatureType typeA, SimpleFeatureType typeB, boolean compareUserMaps) {
        if (typeA == typeB)
            return true;

        if (typeA == null || typeB == null) {
            return false;
        }
        
        if(compareUserMaps) {
            if(!equals(typeA.getUserData(), typeB.getUserData()))
                return false;
        }
        
        return equalsId(typeA, typeB)
                && equals(typeA.getAttributeDescriptors(), typeB.getAttributeDescriptors(), compareUserMaps) &&
                equalsAncestors( typeA, typeB );
    }
    
    static boolean equals( List attributesA, List attributesB, boolean compareUserMaps) {
        return equals(
            (AttributeDescriptor[]) attributesA.toArray(new AttributeDescriptor[attributesA.size()]),
            (AttributeDescriptor[]) attributesB.toArray(new AttributeDescriptor[attributesB.size()]), 
            compareUserMaps);
    }

    static boolean equals( AttributeDescriptor attributesA[], AttributeDescriptor attributesB[], boolean compareUserMaps ) {
        if (attributesA.length != attributesB.length)
            return false;

        for( int i = 0, length = attributesA.length; i < length; i++ ) {
            if (!equals(attributesA[i], attributesB[i], compareUserMaps))
                return false;
        }
        return true;
    }
    /**
     * This method depends on the correct implementation of FeatureType equals
     * <p>
     * We may need to write an implementation that can detect cycles,
     * </p>
     *
     * @param typeA
     * @param typeB
     */
    public static boolean equalsAncestors( SimpleFeatureType typeA, SimpleFeatureType typeB ) {
        return ancestors( typeA ).equals( ancestors(typeB) );
    }

    public static Set ancestors( SimpleFeatureType featureType ) {
        if (featureType == null || getAncestors(featureType).isEmpty()) {
            return Collections.EMPTY_SET;
        }
        return new HashSet(getAncestors(featureType));
    }
    
    static boolean equals( AttributeDescriptor a, AttributeDescriptor b, boolean compareUserMaps) {
        if(a == b)
            return true;
        
        if(a == null)
            return true;
        
        if(!a.equals(b))
            return false;
        
        if(compareUserMaps) {
            if(!equals(a.getUserData(), b.getUserData()))
                return false;
            if(!equals(a.getType().getUserData(), b.getType().getUserData()))
                return false;
        }
        
        return true;
            
    }
    
    /**
     * Tolerant map comparison. Two maps are considered to be equal if they express the
     * same content. So for example two null maps are equal, but also a null and an 
     * empty one are
     */
    static boolean equals(Map a, Map b) {
        if(a == b)
            return true;
        
        // null == null handled above
        if(a == null || b == null)
            return false;
        
        return a.equals(b);
    }
    
    /** Quick check of namespace and typename */
    public static boolean equalsId( SimpleFeatureType typeA, SimpleFeatureType typeB ) {
        if (typeA == typeB)
            return true;

        if (typeA == null || typeB == null) {
            return false;
        }

        String typeNameA = typeA.getTypeName();
        String typeNameB = typeB.getTypeName();
        if (typeNameA == null && typeNameB != null)
            return false;
        else if (!typeNameA.equals(typeNameB))
            return false;

        String namespaceA = typeA.getName().getNamespaceURI();
        String namespaceB = typeB.getName().getNamespaceURI();
        if(namespaceA == null && namespaceB == null)
            return true;
        
        if (namespaceA == null && namespaceB != null)
            return false;
        else if (!namespaceA.equals(namespaceB))
            return false;

        return true;
    }

}
