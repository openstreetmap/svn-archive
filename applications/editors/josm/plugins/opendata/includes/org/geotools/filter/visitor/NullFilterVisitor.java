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
package org.geotools.filter.visitor;

import org.opengis.filter.And;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.NilExpression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.expression.Subtract;

/**
 * Abstract implementation of FilterVisitor simple returns the provided data.
 * <p>
 * This class can be used as is as a placeholder that does nothing:<pre><code>
 * Integer one = (Integer) filter.accepts( NullFilterVisitor.NULL_VISITOR, 1 );
 * </code></pre>
 * 
 * The class can also be used as an alternative to DefaultFilterVisitor if
 * you want to only walk part of the data structure:
 * <pre><code>
 * FilterVisitor allFids = new NullFilterVisitor(){
 *     public Object visit( Id filter, Object data ) {
 *         if( data == null) return null;
 *         Set set = (Set) data;
 *         set.addAll(filter.getIDs());
 *         return set;
 *     }
 * };
 * Set set = (Set) myFilter.accept(allFids, new HashSet());
 * Set set2 = (Set) myFilter.accept(allFids, null ); // set2 will be null
 * </code></pre>
 * The base class provides implementations for:
 * <ul>
 * <li>walking And, Or, and Not data structures, returning null at any point will exit early
 * <li>a default implementation for every other construct that will return the provided data
 * </ul>
 * 
 * @author Jody Garnett (Refractions Research)
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/filter/visitor/NullFilterVisitor.java $
 */
public abstract class NullFilterVisitor implements FilterVisitor, ExpressionVisitor {
    public NullFilterVisitor() {        
    }

    public Object visit( ExcludeFilter filter, Object data ) {
        return data;
    }

    public Object visit( IncludeFilter filter, Object data ) {
        return data;
    }

    public Object visit( And filter, Object data ) {
        if( data == null ) return null;
        if (filter.getChildren() != null) {
            for( Filter child : filter.getChildren() ) {
                data = child.accept(this, data);
                if( data == null ) return null;
            }
        }
        return data;
    }

    public Object visit( Id filter, Object data ) {
        return data;
    }

    public Object visit( Not filter, Object data ) {
        if( data == null ) return data;

        Filter child = filter.getFilter();
        if ( child != null) {
            data = child.accept(this, data);
        }
        return data;
    }

    public Object visit( Or filter, Object data ) {
        if( data == null ) return null;
        if (filter.getChildren() != null) {
            for( Filter child : filter.getChildren() ) {
                data = child.accept(this, data);
                if( data == null ) return null;
            }
        }
        return data;
    }

    public Object visitNullFilter( Object data ) {
        return data;
    }

    public Object visit( NilExpression expression, Object data ) {        
        return null;
    }

    public Object visit( Add expression, Object data ) {
        return data;
    }

    public Object visit( Divide expression, Object data ) {
        return data;
    }

    public Object visit( Function expression, Object data ) {
        return data;
    }

    public Object visit( Literal expression, Object data ) {        
        return data;
    }

    public Object visit( Multiply expression, Object data ) {
        return data;
    }

    public Object visit( PropertyName expression, Object data ) {
        return data;
    }

    public Object visit( Subtract expression, Object data ) {
        return data;
    }
}
