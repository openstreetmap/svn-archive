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
package org.geotools.feature.type;

import org.geotools.feature.IllegalAttributeException;
import org.opengis.feature.Attribute;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.filter.Filter;


/**
 * This is a set of utility methods used when <b>implementing</b> types.
 * <p>
 * This set of classes captures the all important how does it work questions,
 * particularly with respect to super types.
 * </p>
 * FIXME: These methods need a Q&A check to confirm correct use of Super TODO:
 * Cannot tell the difference in intent from FeatureTypes
 * 
 * @author Jody Garnett, LISAsoft
 * @author Justin Deoliveira, The Open Planning Project
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/feature/type/Types.java $
 */
public class Types {
    /**
     * Validates content against an attribute.
     * 
     * @param attribute
     *            The attribute.
     * @param attributeContent
     *            Content of attribute (often attribute.getValue() 
     * 
     * @throws IllegalAttributeException
     *             In the event that content violates any restrictions specified
     *             by the attribute.
     */
    public static void validate(Attribute attribute, Object attributeContent)
            throws IllegalAttributeException {

        validate(attribute.getType(), attribute, attributeContent, false);
    }
    
    /**
     * 
     * @param type AttributeType (often attribute.getType() )
     * @param attribute Attribute being tested
     * @param attributeContent Content of the attribute (often attribute.getValue() )
     * @param isSuper True if super type is being checked
     * @throws IllegalAttributeException
     */
    protected static void validate(AttributeType type, Attribute attribute,
            Object attributeContent, boolean isSuper) throws IllegalAttributeException {

        if (type == null) {
            throw new IllegalAttributeException("null type");
        }

        if (attributeContent == null) {
            if (!attribute.isNillable()) {
                throw new IllegalAttributeException(type.getName() + " not nillable");
            }
            return;
        }

        if (type.isIdentified() && attribute.getIdentifier() == null) {
            throw new NullPointerException(type.getName() + " is identified, null id not accepted");
        }

        if (!isSuper) {

            // JD: This is an issue with how the xml simpel type hierarchy
            // maps to our current Java Type hiearchy, the two are inconsitent.
            // For instance, xs:integer, and xs:int, the later extend the
            // former, but their associated java bindings, (BigDecimal, and
            // Integer)
            // dont.
            Class clazz = attributeContent.getClass();
            Class binding = type.getBinding();
            if (binding != null && binding != clazz && !binding.isAssignableFrom(clazz)) {
                throw new IllegalAttributeException(clazz.getName()
                        + " is not an acceptable class for " + type.getName()
                        + " as it is not assignable from " + binding);
            }
        }

        if (type.getRestrictions() != null) {
            for (Filter f : type.getRestrictions()) {
                if (!f.evaluate(attribute)) {
                    throw new IllegalAttributeException("Attribute instance (" + attribute.getIdentifier() + ")"
                            + "fails to pass filter: " + f);
                }
            }
        }

        // move up the chain,
        if (type.getSuper() != null) {
            validate(type.getSuper(), attribute, attributeContent, true);
        }
    }

    /**
     * Ensure that attributeContent is a good value for descriptor.
     */
    public static void validate(AttributeDescriptor descriptor,
            Object value) throws IllegalAttributeException {

        if (descriptor == null) {
            throw new NullPointerException("Attribute descriptor required for validation");
        }
        
        if (value == null) {
            if (!descriptor.isNillable()) {
                throw new IllegalArgumentException(descriptor.getName() + " requires a non null value");
            }           
        } else {
            validate( descriptor.getType(), value, false );
        }
    }
        
    protected static void validate(final AttributeType type, final Object value, boolean isSuper) throws IllegalAttributeException {
        if (!isSuper) {
            // JD: This is an issue with how the xml simpel type hierarchy
            // maps to our current Java Type hiearchy, the two are inconsitent.
            // For instance, xs:integer, and xs:int, the later extend the
            // former, but their associated java bindings, (BigDecimal, and
            // Integer)
            // dont.
            Class clazz = value.getClass();
            Class binding = type.getBinding();
            if (binding != null && !binding.isAssignableFrom(clazz)) {
                throw new IllegalAttributeException(clazz.getName()
                        + " is not an acceptable class for " + type.getName()
                        + " as it is not assignable from " + binding);
            }
        }

        if (type.getRestrictions() != null && type.getRestrictions().size() > 0) {
            for (Filter filter : type.getRestrictions()) {
                if (!filter.evaluate(value)) {
                    throw new IllegalAttributeException( type.getName() + " restriction "+ filter + " not met by: " + value);
                }
            }
        }

        // move up the chain,
        if (type.getSuper() != null) {
            validate(type.getSuper(), value, true );
        }
    }
}
