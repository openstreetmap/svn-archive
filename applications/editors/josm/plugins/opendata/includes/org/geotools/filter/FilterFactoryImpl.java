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
 *
 *
 * Created on 24 October 2002, 16:16
 */
package org.geotools.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.factory.Hints;
import org.geotools.filter.capability.FunctionNameImpl;
import org.geotools.filter.expression.AddImpl;
import org.geotools.filter.expression.DivideImpl;
import org.geotools.filter.expression.MultiplyImpl;
import org.geotools.filter.expression.SubtractImpl;
import org.geotools.filter.identity.FeatureIdImpl;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
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
import org.xml.sax.helpers.NamespaceSupport;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Implementation of the FilterFactory, generates the filter implementations in
 * defaultcore.
 *
 * @author Ian Turton, CCG
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/filter/FilterFactoryImpl.java $
 * @version $Id: FilterFactoryImpl.java 37298 2011-05-25 05:16:15Z mbedward $
 */
public class FilterFactoryImpl implements FilterFactory {
        
    private FunctionFinder functionFinder;

    /**
     * Creates a new instance of FilterFactoryImpl
     */
    public FilterFactoryImpl() {
        this( null );
    }
    public FilterFactoryImpl( Hints hints ){
        functionFinder = new FunctionFinder( null );
    }

    public FeatureId featureId(String id) {
        return new FeatureIdImpl( id );
    } 
        
    public And and(Filter f, Filter g ) {
        List/*<Filter>*/ list = new ArrayList/*<Filter>*/( 2 );
        list.add( f );
        list.add( g );
        return new AndImpl( this, list );
    }
    
    public And and(List/*<Filter>*/ filters) {
        return new AndImpl( this, filters );
    }
    
    public Or or(Filter f, Filter g) {
        List/*<Filter>*/ list = new ArrayList/*<Filter>*/( 2 );
        list.add( f );
        list.add( g );
        return new OrImpl( this, list );
    }    

    public Or or(List/*<Filter>*/ filters) {
        return new OrImpl( this, filters );
    }
    
    /** Java 5 type narrowing used to advertise explicit implementation for chaining */
    public Not /*NotImpl*/ not(Filter filter) {
        return new NotImpl( this, filter );
    }
    
    public Id id( Set id ){
        return new FidFilterImpl( id );
    }
    
    public PropertyName property(String name) {
        return new AttributeExpressionImpl(name);
    }

    public Add add(Expression expr1, Expression expr2) {
        return new AddImpl(expr1,expr2);
    }

    public Divide divide(Expression expr1, Expression expr2) {
        return new DivideImpl(expr1,expr2);
    }

    public Multiply multiply(Expression expr1, Expression expr2) {
        return new MultiplyImpl(expr1,expr2);
    }

    public Subtract subtract(Expression expr1, Expression expr2) {
        return new SubtractImpl(expr1,expr2);
    }

    public Function function(String name, Expression[] args) {
        Function function = functionFinder.findFunction( name, Arrays.asList(args) );
        return function;
    }
    
    public Literal literal(Object obj) {
        try {
            return new LiteralExpressionImpl(obj);
        } 
        catch (IllegalFilterException e) {
            new IllegalArgumentException().initCause(e);
        }
        
        return null;
    }

    /**
     * Creates a BBox Expression from an envelope.
     *
     * @param env the envelope to use for this bounding box.
     *
     * @return The newly created BBoxExpression.
     *
     * @throws IllegalFilterException if there were creation problems.
     */
    public BBoxExpression createBBoxExpression(Envelope env)
        throws IllegalFilterException {
        return new BBoxExpressionImpl(env);
    }

    public Map getImplementationHints() {
            return Collections.EMPTY_MAP;
    }

    public org.geotools.filter.Filter and( org.geotools.filter.Filter filter1, org.geotools.filter.Filter filter2 ) {
        return (org.geotools.filter.Filter) and( (Filter) filter1, (Filter) filter2 );         
    } 

    public org.geotools.filter.Filter not( org.geotools.filter.Filter filter ) {
        return (org.geotools.filter.Filter) not( (Filter) filter );
    }

    public org.geotools.filter.Filter or( org.geotools.filter.Filter filter1, org.geotools.filter.Filter filter2 ) {
        return (org.geotools.filter.Filter) or( (Filter) filter1, (Filter) filter2 );
    }
    
    public PropertyName property( String name, NamespaceSupport namespaceContext ) {
        if (namespaceContext == null) {
            return property(name);
        }
        return new AttributeExpressionImpl(name, namespaceContext );
    }
    
    public FunctionName functionName(String name, int nargs) {
        return new FunctionNameImpl( name, nargs );
    }
}
