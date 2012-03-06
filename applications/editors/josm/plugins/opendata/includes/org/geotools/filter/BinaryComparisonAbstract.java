/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.filter;

import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.expression.Expression;

/**
 * Abstract implemention for binary filters.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/filter/BinaryComparisonAbstract.java $
 */
public abstract class BinaryComparisonAbstract extends AbstractFilter 
	implements BinaryComparisonOperator {

	protected Expression expression1;
	protected Expression expression2;

	boolean matchingCase;
	
	protected BinaryComparisonAbstract(org.opengis.filter.FilterFactory factory, Expression expression1, Expression expression2 ) {
		this(factory,expression1,expression2,true);
	}
	
	protected BinaryComparisonAbstract(org.opengis.filter.FilterFactory factory, Expression expression1, Expression expression2, boolean matchingCase ) {
		super(factory);
		this.expression1 = expression1;
		this.expression2 = expression2;		
		this.matchingCase = matchingCase;
	} 
	
	public Expression getExpression1() {
		return expression1;
	}

	public void setExpression1(Expression expression) {
		this.expression1 = expression;
	}
	
	public Expression getExpression2() {
		return expression2;
	}
	
	public void setExpression2(Expression expression) {
		this.expression2 = expression;
	}
}
