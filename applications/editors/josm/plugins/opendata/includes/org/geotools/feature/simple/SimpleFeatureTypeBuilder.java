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
package org.geotools.feature.simple;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.NameImpl;
import org.geotools.feature.type.BasicFeatureTypes;
import org.geotools.feature.type.FeatureTypeFactoryImpl;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureTypeFactory;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.Schema;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

import com.vividsolutions.jts.geom.Geometry;

/**
 * A builder for simple feature types.
 * <p>
 * Simple Usage:
 * <pre>
 *  <code>
 *  //create the builder
 *  SimpleTypeBuilder builder = new SimpleTypeBuilder();
 *  
 *  //set global state
 *  builder.setName( "testType" );
 *  builder.setNamespaceURI( "http://www.geotools.org/" );
 *  builder.setSRS( "EPSG:4326" );
 *  
 *  //add attributes
 *  builder.add( "intProperty", Integer.class );
 *  builder.add( "stringProperty", String.class );
 *  builder.add( "pointProperty", Point.class );
 *  
 *  //add attribute setting per attribute state
 *  builder.minOccurs(0).maxOccurs(2).nillable(false).add("doubleProperty",Double.class);
 *  
 *  //build the type
 *  SimpleFeatureType featureType = builder.buildFeatureType();
 *  </code>
 * </pre>
 * </p>
 * This builder builds type by maintaining state. Two types of state are maintained:
 * <i>Global Type State</i> and <i>Per Attribute State</i>. Methods which set
 * global state are named <code>set&lt;property>()</code>. Methods which set per attribute 
 * state are named <code>&lt;property>()</code>. Furthermore calls to per attribute 
 * </p>
 * <p>
 * Global state is reset after a call to {@link #buildFeatureType()}. Per 
 * attribute state is reset after a call to {@link #add}.
 * </p>
 * <p>
 * A default geometry for the feature type can be specified explictly via 
 * {@link #setDefaultGeometry(String)}. However if one is not set the first
 * geometric attribute ({@link GeometryType}) added will be resulting default.
 * So if only specifying a single geometry for the type there is no need to 
 * call the method. However if specifying multiple geometries then it is good
 * practice to specify the name of the default geometry type. For instance:
 * <code>
 * 	<pre>
 *  builder.add( "pointProperty", Point.class );
 *  builder.add( "lineProperty", LineString.class );
 *  builder.add( "polygonProperty", Polygon.class );
 *  
 *  builder.setDefaultGeometry( "lineProperty" );
 * 	</pre>
 * </code>
 * </p>
 * 
 * @author Justin Deolivera
 * @author Jody Garnett
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/feature/simple/SimpleFeatureTypeBuilder.java $
 */
public class SimpleFeatureTypeBuilder {
	/**
	 * factories
	 */
	protected FeatureTypeFactory factory;

	/**
	 * Map of java class bound to properties types.
	 */
	protected Map/* <Class,AttributeType> */bindings;
	
	// Global state for the feature type
	//
	/**
	 * Naming: local name
	 */
	protected String local;

	/**
	 * Naming: uri indicating scope
	 */
	protected String uri;

	/**
	 * Description of type.
	 */
	protected InternationalString description;

	/**
	 * List of attributes.
	 */
	protected List<AttributeDescriptor> attributes;

	/**
	 * Additional restrictions on the type.
	 */
	protected List<Filter> restrictions;

	/** 
	 * Name of the default geometry to use 
	 */
	protected String defaultGeometry;

	/** 
	 * coordinate reference system of the type 
	 */
	protected CoordinateReferenceSystem crs;

	/**
	 * flag controlling if the type is abstract.
	 */
	protected boolean isAbstract = false;
	
	/**
	 * the parent type.
	 */
	protected SimpleFeatureType superType;
	
	/**
	 * attribute builder
	 */
	protected AttributeTypeBuilder attributeBuilder;
	
	/**
	 * Constructs the builder.
	 */
	public SimpleFeatureTypeBuilder() {
		this( new FeatureTypeFactoryImpl() );
	}
	
	/**
	 * Constructs the builder specifying the factory for creating feature and 
	 * feature collection types.
	 */
	public SimpleFeatureTypeBuilder(FeatureTypeFactory factory) {
		this.factory = factory;
		
		attributeBuilder = new AttributeTypeBuilder();
		setBindings( new SimpleSchema() );
		reset();
	}
		
	// Builder methods
	//
	/**
	 * Initializes the builder with state from a pre-existing feature type.
	 */
	public void init(SimpleFeatureType type) {
		init();
		if (type == null)
			return;

		uri = type.getName().getNamespaceURI();
		local = type.getName().getLocalPart();
		description = type.getDescription();
		restrictions = null;
		restrictions().addAll(type.getRestrictions());

		attributes = null;
		attributes().addAll(type.getAttributeDescriptors());
		
		isAbstract = type.isAbstract();
		superType = (SimpleFeatureType) type.getSuper();
	}

	/**
	 * Clears the running list of attributes. 
	 */
	protected void init() {
		attributes = null;
	}
	
	/**
	 * Completely resets all builder state.
	 *
	 */
	protected void reset() {
		uri = BasicFeatureTypes.DEFAULT_NAMESPACE;
		local = null;
		description = null;
		restrictions = null;
		attributes = null;
		crs = null;
		isAbstract = false;
		superType = BasicFeatureTypes.FEATURE;
	}
	
	/**
	 * Set the namespace uri of the built type.
	 */
	public void setNamespaceURI(String namespaceURI) {
		this.uri = namespaceURI;
	}
	public void setNamespaceURI(URI namespaceURI) {
	    if ( namespaceURI != null ) {
	        setNamespaceURI( namespaceURI.toString() );
	    }
	    else {
	        setNamespaceURI( (String) null );
	    }
	}
	
	/**
	 * Sets the name of the built type.
	 */
	public void setName(String name) {
		this.local = name;
	}
		
	/**
	 * Sets the local name and namespace uri of the built type.
	 */
	public void setName(Name name) {
	    setName( name.getLocalPart() );
	    setNamespaceURI( name.getNamespaceURI() );
	}
		
	/**
	 * Sets the name of the default geometry attribute of the built type.
	 */
	public void setDefaultGeometry(String defaultGeometryName) {
		this.defaultGeometry = defaultGeometryName;
	}
		
	/**
	 * Sets the flag controlling if the resulting type is abstract.
	 */
	public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }
	
	/**
	 * Sets the super type of the built type.
	 */
	public void setSuperType(SimpleFeatureType superType) {
        this.superType = superType;
    }
	
	/**
	 * Specifies an attribute type binding.
	 * <p>
	 * This method is used to associate an attribute type with a java class. 
	 * The class is retreived from <code>type.getBinding()</code>. When the
	 * {@link #add(String, Class)} method is used to add an attribute to the 
	 * type being built, this binding is used to locate the attribute type.
	 * </p>
	 * 
	 * @param type The attribute type.
	 */
	public void addBinding(AttributeType type) {
		bindings().put(type.getBinding(), type);
	}
	
	/**
	 * Specifies a number of attribute type bindings.
	 * 
	 * @param schema The schema containing the attribute types.
	 * 
	 * @see {@link #addBinding(AttributeType)}.
	 */
	public void addBindings( Schema schema ) {
		for (Iterator itr = schema.values().iterator(); itr.hasNext();) {
			AttributeType type = (AttributeType) itr.next();
			addBinding(type);
		}
	}
	
	/**
	 * Specifies a number of attribute type bindings clearing out all existing
	 * bindings.
	 * 
	 * @param schema The schema contianing attribute types.
	 * 
	 * @see {@link #addBinding(AttributeType)}.
	 */
	public void setBindings( Schema schema ) {
		bindings().clear();
		addBindings( schema );
	}
		
	// per attribute methods
	//
	/**
	 * Sets the minOccurs of the next attribute added to the feature type.
	 * <p>
	 * This value is reset after a call to {@link #add(String, Class)}
	 * </p>
	 */
	public SimpleFeatureTypeBuilder minOccurs( int minOccurs ) {
		attributeBuilder.setMinOccurs(minOccurs);
		return this;
	}
	/**
	 * Sets the maxOccurs of the next attribute added to the feature type.
	 * <p>
	 * This value is reset after a call to {@link #add(String, Class)}
	 * </p>
	 */
	public SimpleFeatureTypeBuilder maxOccurs( int maxOccurs ) {
		attributeBuilder.setMaxOccurs(maxOccurs);
		return this;
	}
	/**
	 * Sets the nullability of the next attribute added to the feature type.
	 * <p>
	 * This value is reset after a call to {@link #add(String, Class)}
	 * </p>
	 */
	public SimpleFeatureTypeBuilder nillable( boolean isNillable ) {
		attributeBuilder.setNillable(isNillable);
		return this;
	}
	
	/**
	 * Sets the default value of the next attribute added to the feature type.
	 * <p>
	 * This value is reset after a call to {@link #add(String, Class)}
	 * </p>
	 */
	public SimpleFeatureTypeBuilder defaultValue( Object defaultValue ) {
		attributeBuilder.setDefaultValue( defaultValue );
		return this;
	}
	/**
	 * Sets the crs of the next attribute added to the feature type.
	 * <p>
	 * This only applies if the attribute added is geometric.
	 * </p>
	 * <p>
	 * This value is reset after a call to {@link #add(String, Class)}
	 * </p>
	 */
	public SimpleFeatureTypeBuilder crs( CoordinateReferenceSystem crs ) {
		attributeBuilder.setCRS(crs);
		return this;
	}
		
	/**
	 * Sets all the attribute specific state from a single descriptor.
	 * <p>
	 * This method is convenience for:
	 * <code>
	 * builder.minOccurs( descriptor.getMinOccurs() ).maxOccurs( descriptor.getMaxOccurs() )
	 *     .nillable( descriptor.isNillable() )...
	 * </code>
	 * </p>
	 */
	public SimpleFeatureTypeBuilder descriptor( AttributeDescriptor descriptor ) {
	    minOccurs( descriptor.getMinOccurs() );
	    maxOccurs( descriptor.getMaxOccurs() );
	    nillable( descriptor.isNillable() );
	    //namespaceURI( descriptor.getName().getNamespaceURI() );
	    defaultValue( descriptor.getDefaultValue() );
	    
	    if ( descriptor instanceof GeometryDescriptor ) {
	        crs( ( (GeometryDescriptor) descriptor).getCoordinateReferenceSystem() );
	    }
	    
	    return this;
	}
	
	/**
	 * Adds a new attribute w/ provided name and class.
	 * 
	 * <p>
	 * The provided class is used to locate an attribute type binding previously 
	 * specified by {@link #addBinding(AttributeType)},{@link #addBindings(Schema)}, 
	 * or {@link #setBindings(Schema)}. 
	 * </p>
	 * <p>
	 * If not such binding exists then an attribute type is created on the fly.
	 * </p>
	 * @param name The name of the attribute.
	 * @param bind The class the attribute is bound to.
	 * 
	 */
	public void add(String name, Class binding) {

	    AttributeDescriptor descriptor = null;
	    
	    attributeBuilder.setBinding(binding);
        attributeBuilder.setName(name);
        
		//check if this is the name of the default geomtry, in that case we 
		// better make it a geometry type
		//also check for jts geometry, if we ever actually get to a point where a
        // feature can be backed by another geometry model (like iso), we need 
        // to remove this check
        //
        if ( ( defaultGeometry != null && defaultGeometry.equals( name ) ) 
            || Geometry.class.isAssignableFrom(binding) ) {
		
            //if no crs was set, set to the global
            if ( !attributeBuilder.isCRSSet() ) {
                attributeBuilder.setCRS(crs);
            }
            
            GeometryType type = attributeBuilder.buildGeometryType();
            descriptor = attributeBuilder.buildDescriptor(name, type);
		}
        else {
            AttributeType type = attributeBuilder.buildType();
            descriptor = attributeBuilder.buildDescriptor(name, type );
        }
		
        
		attributes().add(descriptor);
	}
	
	/**
	 * Adds a descriptor directly to the builder.
	 * <p>
	 * Use of this method is discouraged. Consider using {@link #add(String, Class)}. 
	 * </p>
	 */
	public void add( AttributeDescriptor descriptor ) {
	    attributes().add(descriptor);
	}
		
    /**
     * Adds a list of descriptors directly to the builder.
     * <p>
     * Use of this method is discouraged. Consider using {@link #add(String, Class)}.
     * </p>
     */
    public void addAll( List<AttributeDescriptor>  descriptors ) {
        if(descriptors != null)
            for ( AttributeDescriptor ad : descriptors ) {
                add( ad );
            }
    }
	/**
     * Adds an array of descriptors directly to the builder.
     * <p>
     * Use of this method is discouraged. Consider using {@link #add(String, Class)}.
     * </p>
     */
	public void addAll( AttributeDescriptor[] descriptors ) {
            if (descriptors != null) {
	        for ( AttributeDescriptor ad : descriptors ) {
	            add( ad );
                }
	    }
	}
		
	/**
	 * Builds a feature type from compiled state.
	 * <p>
	 * After the type is built the running list of attributes is cleared.
	 * </p>
	 * @return The built feature type.
	 */
	public SimpleFeatureType buildFeatureType() {
	    GeometryDescriptor defGeom = null;
		
		//was a default geometry set?
		if ( this.defaultGeometry != null ) {
			List<AttributeDescriptor> atts = attributes();
			for ( int i = 0; i < atts.size(); i++) {
				AttributeDescriptor att = atts.get(i);
				if ( this.defaultGeometry.equals( att.getName().getLocalPart() ) ) {
					//ensure the attribute is a geometry attribute
					if ( !(att instanceof GeometryDescriptor ) ) {
						attributeBuilder.init( att );
						attributeBuilder.setCRS(crs);
						GeometryType type = attributeBuilder.buildGeometryType();						
						att = attributeBuilder.buildDescriptor(att.getName(),type);
						atts.set( i, att );
					}
					defGeom = (GeometryDescriptor)att;
					break;
				}
			}
			
			if (defGeom == null) {
			    String msg = "'" + this.defaultGeometry + " specified as default" +
		    		" but could find no such attribute.";
			    throw new IllegalArgumentException( msg );
			}
		}
		
		if ( defGeom == null ) {
			//none was set by name, look for first geometric type
			for ( AttributeDescriptor att : attributes() ) {
				if ( att instanceof GeometryDescriptor ) {
					defGeom = (GeometryDescriptor) att;
					break;
				}
			}
		}
		
		SimpleFeatureType built = factory.createSimpleFeatureType(
			name(), attributes(), defGeom, isAbstract,
			restrictions(), superType, description);
		
		init();
		return built;
	}
	
	// Internal api available for subclasses to override
	
	/**
	 * Creates a new list instance, this default impelementation returns {@link ArrayList}.
	 */
	protected List newList() {
		return new ArrayList();
	}
	
	/**
	 * Creates a new map instance, this default implementation returns {@link HashMap}
	 */
	protected Map newMap() {
		return new HashMap();
	}
		
	// Helper methods, 
	//
	/**
	 * Naming: Accessor which returns type name as follows:
	 * <ol>
	 * <li>If <code>typeName</code> has been set, its value is returned.
	 * <li>If <code>name</code> has been set, it + <code>namespaceURI</code>
	 * are returned.
	 * </ol>
	 * 
	 */
	protected Name name() {
		if (local == null)
			return null;
		
		return new NameImpl(uri, local);
	}

	/**
	 * Accessor for attributes.
	 */
	protected List<AttributeDescriptor> attributes() {
		if (attributes == null) {
			attributes = newList();
		}
		return attributes;
	}
	/**
	 * Accessor for restrictions.
	 */
	protected List<Filter> restrictions(){
		if (restrictions == null) {
			restrictions = newList();
		}
		return restrictions;		
	}
	/**
	 * Accessor for bindings.
	 */
	protected Map bindings() {
		if (bindings == null) {
			bindings = newMap();
		}
		return bindings;
	}
		
	/**
	 * Create a SimpleFeatureType containing just the descriptors indicated.
	 * @param original SimpleFeatureType
	 * @param types name of types to include in result
	 * @return SimpleFeatureType containing just the types indicated by name
	 */
	public static SimpleFeatureType retype( SimpleFeatureType original, String[] types ) {
	    SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
	    
	    //initialize the builder
	    b.init( original );
	    
	    //clear the attributes
	    b.attributes().clear();
	    
	    //add attributes in order
	    for ( int i = 0; i < types.length; i++ ) {
	        b.add( original.getDescriptor( types[i] ) );
	    }
	    
	    return b.buildFeatureType();
	}
	
}
