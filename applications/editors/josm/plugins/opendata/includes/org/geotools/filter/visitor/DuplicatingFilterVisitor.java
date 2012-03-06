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
package org.geotools.filter.visitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.opengis.filter.And;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
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
 * Used to duplication Filters and/or Expressions - returned object is a copy.
 * <p>
 * Extra data can be used to provide a {@link FilterFactory2} but this is NOT required.
 * This class is thread safe.
 * </ul>
 * @author Jesse
 *
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/filter/visitor/DuplicatingFilterVisitor.java $
 */
public class DuplicatingFilterVisitor implements FilterVisitor, ExpressionVisitor {

	protected final FilterFactory2 ff;

	public DuplicatingFilterVisitor() {
		this(CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints()));
		
	}
	
	public DuplicatingFilterVisitor(FilterFactory2 factory) {
		this.ff = factory;
	}
	
	protected FilterFactory2 getFactory(Object extraData) {
		if( extraData instanceof FilterFactory2)
			return (FilterFactory2) extraData;
		return ff;
	}

	public Object visit(ExcludeFilter filter, Object extraData) {
		return filter;
	}


	public Object visit(IncludeFilter filter, Object extraData) {
		return filter;
	}
	
	/**
	 * Null safe expression cloning
	 * @param expression
	 * @param extraData
	 * @return
	 */
	Expression visit(Expression expression, Object extraData) {
	    if(expression == null)
	        return null;
	    return (Expression) expression.accept(this, extraData);
	}

	public Object visit(And filter, Object extraData) {
		List children = filter.getChildren();
		List newChildren = new ArrayList();
		for (Iterator iter = children.iterator(); iter.hasNext();) {
			Filter child = (Filter) iter.next();
			if( child!=null )
				newChildren.add(child.accept(this, extraData));
		}
		return getFactory(extraData).and(newChildren);
	}

	public Object visit(Id filter, Object extraData) {
		return getFactory(extraData).id(filter.getIdentifiers());
	}

	public Object visit(Not filter, Object extraData) {
		return getFactory(extraData).not((Filter) filter.getFilter().accept(this, extraData));
	}

	public Object visit(Or filter, Object extraData) {
		List children = filter.getChildren();
		List newChildren = new ArrayList();
		for (Iterator iter = children.iterator(); iter.hasNext();) {
			Filter child = (Filter) iter.next();
			if( child!=null )
				newChildren.add(child.accept(this, extraData));
		}
		return getFactory(extraData).or(newChildren);
	}

	public Object visit(NilExpression expression, Object extraData) {
		return expression;
	}

	public Object visit(Add expression, Object extraData) {
	    Expression expr1= visit(expression.getExpression1(), extraData);
	    Expression expr2= visit(expression.getExpression2(), extraData);
		return getFactory(extraData).add(expr1, expr2);
	}

	public Object visit(Divide expression, Object extraData) {
	    Expression expr1= visit(expression.getExpression1(), extraData);
        Expression expr2= visit(expression.getExpression2(), extraData);
		return getFactory(extraData).divide(expr1, expr2);
	}

	public Object visit(Function expression, Object extraData) {
		List old = expression.getParameters();
		Expression[] args = new Expression[old.size()];
		int i = 0;
		for (Iterator iter = old.iterator(); iter.hasNext(); i++) {
			Expression exp = (Expression) iter.next();
			args[i]= visit(exp, extraData);
		}
		return getFactory(extraData).function(expression.getName(), args);
	}

	public Object visit(Literal expression, Object extraData) {
		return getFactory(extraData).literal(expression.getValue());
	}

	public Object visit(Multiply expression, Object extraData) {
	    Expression expr1= visit(expression.getExpression1(), extraData);
        Expression expr2= visit(expression.getExpression2(), extraData);
		return getFactory(extraData).multiply(expr1, expr2);
	}

	public Object visit(PropertyName expression, Object extraData) {
		return getFactory(extraData).property(expression.getPropertyName(), expression.getNamespaceContext());
	}

	public Object visit(Subtract expression, Object extraData) {
	    Expression expr1= visit(expression.getExpression1(), extraData);
        Expression expr2= visit(expression.getExpression2(), extraData);
		return getFactory(extraData).subtract(expr1, expr2);
	}

}
