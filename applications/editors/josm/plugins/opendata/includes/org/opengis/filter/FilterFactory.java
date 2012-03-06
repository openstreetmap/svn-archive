/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2005 Open Geospatial Consortium Inc.
 *    
 *    All Rights Reserved. http://www.opengis.org/legal/
 */
package org.opengis.filter;

import java.util.List;
import java.util.Set;

import org.opengis.filter.capability.FunctionName;
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.expression.Subtract;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.identity.Identifier;


/**
 * Interface whose methods allow the caller to create instances of the various
 * {@link Filter} and {@link Expression} subclasses.
 * <p>
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/opengis/src/main/java/org/opengis/filter/FilterFactory.java $
 * @version <A HREF="http://www.opengis.org/docs/02-059.pdf">Implementation specification 1.0</A>
 * @author Chris Dillard (SYS Technologies)
 * @since GeoAPI 2.0
 */
public interface FilterFactory {
        /**
         * The FilterCapabilities data structure is used to describe the abilities of
         * this FilterFactory, it includes restrictions on the available spatial operations,
         * scalar operations, lists the supported functions, and describes what geometry
         * literals are understood.
         * @return FilterCapabilities describing the abilities of this FilterFactory
         */
        // FilterCapabilities getCapabilities();
        
////////////////////////////////////////////////////////////////////////////////
//
//  IDENTIFIERS
//
////////////////////////////////////////////////////////////////////////////////
    /** Creates a new feautre id from a string */
    FeatureId featureId(String id);

////////////////////////////////////////////////////////////////////////////////
//
//  FILTERS
//
////////////////////////////////////////////////////////////////////////////////

    /** {@code AND} filter between two filters. */
    And and(Filter f, Filter g);

    /** {@code AND} filter between a list of filters. */
    And and(List<Filter> f);

    /** {@code OR} filter between two filters. */
    Or or(Filter f, Filter g);

    /** {@code OR} filter between a list of filters. */
    Or or (List<Filter> f);

    /** Reverses the logical value of a filter. */
    Not not(Filter f);

    /** Passes only for objects that have one of the IDs given to this object. */
    Id id(Set<? extends Identifier> ids);

    /** Retrieves the value of a {@linkplain org.opengis.feature.Feature feature}'s property. */
    PropertyName property(String name);

////////////////////////////////////////////////////////////////////////////////
//
//  EXPRESSIONS
//
////////////////////////////////////////////////////////////////////////////////

    /** Computes the numeric addition of the first and second operand. */
    Add       add(Expression expr1, Expression expr2);

    /** Computes the numeric quotient resulting from dividing the first operand by the second. */
    Divide    divide(Expression expr1, Expression expr2);

    /** Computes the numeric product of their first and second operand. */
    Multiply  multiply(Expression expr1, Expression expr2);

    /** Computes the numeric difference between the first and second operand. */
    Subtract  subtract(Expression expr1, Expression expr2);

    /** Call into some implementation-specific function. */
    Function  function(String name, Expression ... args);

    /** A constant, literal value that can be used in expressions. */
    Literal  literal(Object obj);

    ////////////////////////////////////////////////////////////////////////////////
    //
    //  CAPABILITIES
    //
    ////////////////////////////////////////////////////////////////////////////////
    /** function name */
    FunctionName functionName(String name, int nargs);
}
