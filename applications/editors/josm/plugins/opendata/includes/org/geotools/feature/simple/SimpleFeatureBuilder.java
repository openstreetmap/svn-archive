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

import java.rmi.server.UID;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataUtilities;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.type.Types;
import org.geotools.util.Converters;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;

/**
 * A builder for features.
 * <p>
 * Simple Usage:
 * <code>
 *  <pre>
 *  //type of features we would like to build ( assume schema = (geom:Point,name:String) )
 *  SimpleFeatureType featureType = ...  
 * 
 *   //create the builder
 *  SimpleFeatureBuilder builder = new SimpleFeatureBuilder();
 *  
 *  //set the type of created features
 *  builder.setType( featureType );
 *  
 *  //add the attributes
 *  builder.add( new Point( 0 , 0 ) );
 *  builder.add( "theName" );
 *  
 *  //build the feature
 *  SimpleFeature feature = builder.buildFeature( "fid" );
 *  </pre>
 * </code>
 * </p>
 * <p>
 * This builder builds a feature by maintaining state. Each call to {@link #add(Object)}
 * creates a new attribute for the feature and stores it locally. When using the 
 * add method to add attributes to the feature, values added must be added in the 
 * same order as the attributes as defined by the feature type. The methods 
 * {@link #set(String, Object)} and {@link #set(int, Object)} are used to add 
 * attributes out of order.
 * </p>
 * <p>
 * Each time the builder builds a feature with a call to {@link #buildFeature(String)}
 * the internal state is reset. 
 * </p>
 * <p>
 * This builder can be used to copy features as well. The following code sample
 * demonstrates:
 * <code>
 * <pre>
 *  //original feature
 *  SimpleFeature original = ...;
 * 
 *  //create and initialize the builder
 *  SimpleFeatureBuilder builder = new SimpleFeatureBuilder();
 *  builder.init(original);
 * 
 *  //create the new feature
 *  SimpleFeature copy = builder.buildFeature( original.getID() );
 * 
 *  </pre>
 * </code>
 * </p>
 * <p>
 * The builder also provides a number of static "short-hand" methods which can 
 * be used when its not ideal to instantiate a new builder, thought this will
 * trigger some extra object allocations. In time critical code sections it's
 * better to instantiate the builder once and use it to build all the required
 * features.
 * <code>
 *   <pre>
 *   SimpleFeatureType type = ..;
 *   Object[] values = ...;
 *   
 *   //build a new feature
 *   SimpleFeature feature = SimpleFeatureBuilder.build( type, values, "fid" );
 *   
 *   ...
 *   
 *   SimpleFeature original = ...;
 *   
 *   //copy the feature
 *   SimpleFeature feature = SimpleFeatureBuilder.copy( original );
 *   </pre>
 * </code>
 * </p>
 * <p>
 * This class is not thread safe nor should instances be shared across multiple 
 * threads.
 * </p>
 *  
 * @author Justin Deoliveira
 * @author Jody Garnett
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/feature/simple/SimpleFeatureBuilder.java $
 */
public class SimpleFeatureBuilder {
    
    /** the feature type */
    SimpleFeatureType featureType;
    
    /** the feature factory */
    FeatureFactory factory;
    
    /** the attribute name to index index */
    Map<String, Integer> index;

    /** the values */
    //List<Object> values;
    Object[] values;
    
    /** pointer for next attribute */
    int next;
    
    Map<Object, Object>[] userData;
    
    Map<Object, Object> featureUserData;
    
    boolean validating;
    
    public SimpleFeatureBuilder(SimpleFeatureType featureType) {
        this(featureType, CommonFactoryFinder.getFeatureFactory(null));
    }
    
    public SimpleFeatureBuilder(SimpleFeatureType featureType, FeatureFactory factory) {
        this.featureType = featureType;
        this.factory = factory;

        if(featureType instanceof SimpleFeatureTypeImpl) {
            index = ((SimpleFeatureTypeImpl) featureType).index;
        } else {
            this.index = SimpleFeatureTypeImpl.buildIndex(featureType);
        }
        reset();
    }
    
    public void reset() {
        values = new Object[featureType.getAttributeCount()];
        next = 0;
        userData = null;
        featureUserData = null;
    }
    
    /**
     * Returns the simple feature type used by this builder as a feature template
     * @return
     */
    public SimpleFeatureType getFeatureType() {
        return featureType;
    }
    
    /**
     * Initialize the builder with the provided feature.
     * <p>
     * This method adds all the attributes from the provided feature. It is 
     * useful when copying a feature. 
     * </p>
     */
    public void init( SimpleFeature feature ) {
        reset();
        
        // optimize the case in which we just build
        if(feature instanceof SimpleFeatureImpl) {
            SimpleFeatureImpl impl = (SimpleFeatureImpl) feature;
            System.arraycopy(impl.values, 0, values, 0, impl.values.length);
        } else {
            for (Object value : feature.getAttributes()) {
                add(value);
            }
        }
    }
    
    

    /**
     * Adds an attribute.
     * <p>
     * This method should be called repeatedly for the number of attributes as
     * specified by the type of the feature.
     * </p>
     */
    public void add(Object value) {
        set(next, value);
        next++;
    }

    /**
     * Adds a list of attributes.
     */
    public void addAll(List<Object> values) {
        for (int i = 0; i < values.size(); i++) {
            add(values.get(i));
        }
    }

    /**
     * Adds an array of attributes.
     */
    public void addAll(Object[] values) {
       addAll(Arrays.asList(values));
    }

    /**
     * Adds an attribute value by name.
     * <p>
     * This method can be used to add attribute values out of order.
     * </p>
     * 
     * @param name
     *            The name of the attribute.
     * @param value
     *            The value of the attribute.
     * 
     * @throws IllegalArgumentException
     *             If no such attribute with teh specified name exists.
     */
    public void set(Name name, Object value) {
        set(name.getLocalPart(), value);
    }

    /**
     * Adds an attribute value by name.
     * <p>
     * This method can be used to add attribute values out of order.
     * </p>
     * 
     * @param name
     *            The name of the attribute.
     * @param value
     *            The value of the attribute.
     * 
     * @throws IllegalArgumentException
     *             If no such attribute with teh specified name exists.
     */
    public void set(String name, Object value) {
        int index = featureType.indexOf(name);
        if (index == -1) {
            throw new IllegalArgumentException("No such attribute:" + name);
        }
        set(index, value);
    }

    /**
     * Adds an attribute value by index. *
     * <p>
     * This method can be used to add attribute values out of order.
     * </p>
     * 
     * @param index
     *            The index of the attribute.
     * @param value
     *            The value of the attribute.
     */
    public void set(int index, Object value) {
        if(index >= values.length)
            throw new ArrayIndexOutOfBoundsException("Can handle " 
                    + values.length + " attributes only, index is " + index);
        
        AttributeDescriptor descriptor = featureType.getDescriptor(index);
        values[index] = convert(value, descriptor);
        if(validating)
            Types.validate(descriptor, values[index]);
    }

    private Object convert(Object value, AttributeDescriptor descriptor) {
        //make sure the type of the value and the binding of the type match up
        if ( value != null ) {
            Class<?> target = descriptor.getType().getBinding(); 
            Object converted = Converters.convert(value, target);
            if(converted != null)
                value = converted;
        } else {
            //if the content is null and the descriptor says isNillable is false, 
            // then set the default value
            if (!descriptor.isNillable()) {
                value = descriptor.getDefaultValue();
                if ( value == null ) {
                    //no default value, try to generate one
                    value = DataUtilities.defaultValue(descriptor.getType().getBinding());
                }
            }
        }
        return value;
    }

    /**
     * Builds the feature.
     * <p>
     * The specified <tt>id</tt> may be <code>null</code>. In this case an
     * id will be generated internally by the builder.
     * </p>
     * <p>
     * After this method returns, all internal builder state is reset.
     * </p>
     * 
     * @param id
     *            The id of the feature, or <code>null</code>.
     * 
     * @return The new feature.
     */
    public SimpleFeature buildFeature(String id) {
        // ensure id
        if (id == null) {
            id = SimpleFeatureBuilder.createDefaultFeatureId();
        }

        Object[] values = this.values;
        Map<Object,Object>[] userData = this.userData;
        Map<Object,Object> featureUserData = this.featureUserData;
        reset();
        SimpleFeature sf = factory.createSimpleFeature(values, featureType, id);
        
        // handle the per attribute user data
        if(userData != null) {
            for (int i = 0; i < userData.length; i++) {
                if(userData[i] != null) {
                    sf.getProperty(featureType.getDescriptor(i).getName()).getUserData().putAll(userData[i]);
                }
            }
        }
        
        // handle the feature wide user data
        if(featureUserData != null) {
            sf.getUserData().putAll(featureUserData);
        }
        
        return sf;
    }
    
    /**
     * Internal method for creating feature id's when none is specified.
     */
    public static String createDefaultFeatureId() {
          // According to GML and XML schema standards, FID is a XML ID
        // (http://www.w3.org/TR/xmlschema-2/#ID), whose acceptable values are those that match an
        // NCNAME production (http://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName):
        // NCName ::= (Letter | '_') (NCNameChar)* /* An XML Name, minus the ":" */
        // NCNameChar ::= Letter | Digit | '.' | '-' | '_' | CombiningChar | Extender
        // We have to fix the generated UID replacing all non word chars with an _ (it seems
        // they area all ":")
        //return "fid-" + NON_WORD_PATTERN.matcher(new UID().toString()).replaceAll("_");
        // optimization, since the UID toString uses only ":" and converts long and integers
        // to strings for the rest, so the only non word character is really ":"
        return "fid-" + new UID().toString().replace(':', '_');
    }
    
    /**
     * Static method to build a new feature.
     * <p>
     * If multiple features need to be created, this method should not be used
     * and instead an instance should be instantiated directly.
     * </p>
     * <p>
     * This method is a short-hand convenience which creates a builder instance
     * internally and adds all the specified attributes.
     * </p>
     * @param type SimpleFeatureType defining the structure for the created feature
     * @param values Attribute values, must be in the order defined by SimpleFeatureType
     * @param id FeatureID for the generated feature, use null to allow one to be supplied for you
     */
    public static SimpleFeature build( SimpleFeatureType type, Object[] values, String id ) {
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
        builder.addAll(values);
        return builder.buildFeature(id);
    }
    
    /**
     * * Static method to build a new feature.
     * <p>
     * If multiple features need to be created, this method should not be used
     * and instead an instance should be instantiated directly.
     * </p>
     * @param type SimpleFeatureType defining the structure for the created feature
     * @param values Attribute values, must be in the order defined by SimpleFeatureType
     * @param id FeatureID for the generated feature, use null to allow one to be supplied for you
     */
    public static SimpleFeature build( SimpleFeatureType type, List<Object> values, String id ) {
        return build( type, values.toArray(), id );
    }
    
    /**
     * Copy an existing feature (the values are reused so be careful with mutable values).
     * <p>
     * If multiple features need to be copied, this method should not be used
     * and instead an instance should be instantiated directly.
     * </p>
     * <p>
     * This method is a short-hand convenience which creates a builder instance
     * and initializes it with the attributes from the specified feature.
     * </p>
     */
    public static SimpleFeature copy(SimpleFeature original) {
        if( original == null ) return null;
        
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(original.getFeatureType());
        builder.init(original); // this is a shallow copy
        return builder.buildFeature(original.getID());
    }
}
