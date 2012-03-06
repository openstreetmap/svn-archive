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
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.NilExpression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.expression.Subtract;

/**
 * Abstract implementation of FilterVisitor that simply walks the data structure.
 * <p>
 * This class implements the full FilterVisitor interface and will visit every Filter member of a
 * Filter object. This class performs no actions and is not intended to be used directly, instead
 * extend it and overide the methods for the Filter type you are interested in. Remember to call the
 * super method if you want to ensure that the entire filter tree is still visited.
 * 
 * <pre><code>
 * FilterVisitor allFids = new DefaultFilterVisitor(){
 *     public Object visit( Id filter, Object data ) {
 *         Set set = (Set) data;
 *         set.addAll(filter.getIDs());
 *         return set;
 *     }
 * };
 * Set set = (Set) myFilter.accept(allFids, new HashSet());
 * </code></pre>
 * 
 * @author Jody
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/filter/visitor/DefaultFilterVisitor.java $
 */
public abstract class DefaultFilterVisitor implements FilterVisitor, ExpressionVisitor {

    public DefaultFilterVisitor() {
    }

    public Object visit( ExcludeFilter filter, Object data ) {
        return data;
    }

    public Object visit( IncludeFilter filter, Object data ) {
        return data;
    }

    public Object visit( And filter, Object data ) {
        if (filter.getChildren() != null) {
            for( Filter child : filter.getChildren()) {
                data = child.accept(this, data);
            }
        }
        return data;
    }

    public Object visit( Id filter, Object data ) {
        return data;
    }

    public Object visit( Not filter, Object data ) {
        Filter child = filter.getFilter();
        if (child != null) {
            data = child.accept(this, data);
        }
        return data;
    }

    public Object visit( Or filter, Object data ) {
        if (filter.getChildren() != null) {
            for( Filter child : filter.getChildren()) {
                data = child.accept(this, data);
            }
        }
        return data;
    }

    public Object visit( NilExpression expression, Object data ) {        
        return data;
    }

    public Object visit( Add expression, Object data ) {
        data = expression.getExpression1().accept( this, data);
        data = expression.getExpression2().accept( this, data);
        return data;
    }

    public Object visit( Divide expression, Object data ) {
        data = expression.getExpression1().accept( this, data);
        data = expression.getExpression2().accept( this, data);        
        return data;
    }

    public Object visit( Function expression, Object data ) {
        if( expression.getParameters() != null ){
            for( Expression parameter : expression.getParameters() ){
                data =  parameter.accept( this, data);
            }
        }
        return data;
    }

    public Object visit( Literal expression, Object data ) {        
        return data;
    }

    public Object visit( Multiply expression, Object data ) {
        data = expression.getExpression1().accept( this, data);
        data = expression.getExpression2().accept( this, data);                
        return data;
    }

    public Object visit( PropertyName expression, Object data ) {
        return data;
    }

    public Object visit( Subtract expression, Object data ) {
        data = expression.getExpression1().accept( this, data);
        data = expression.getExpression2().accept( this, data);                
        return data;
    }

}
