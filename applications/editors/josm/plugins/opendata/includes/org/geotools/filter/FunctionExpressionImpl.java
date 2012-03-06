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
package org.geotools.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;

/**
 * Abstract class for a function expression implementation
 *
 * @author James Macgill, PSU
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/filter/FunctionExpressionImpl.java $
 */
public abstract class FunctionExpressionImpl
    extends org.geotools.filter.DefaultExpression implements FunctionExpression {
	
	/** function name **/
	protected String name;

	/** function params **/
	protected List params;
	
    protected Literal fallback;
	
    protected FunctionExpressionImpl(String name ){
        this( name, null );
    }
    /**
     * Creates a new instance of FunctionExpression
     */
    protected FunctionExpressionImpl(String name, Literal fallback) {
        this.name = name;
        this.fallback = fallback;
        params = new ArrayList();
    }

     /**
     * Gets the type of this expression.
     *
     * @return the short representation of a function expression.
     */
    public short getType() {
        return FUNCTION;
    }

    /**
     * Gets the name of this function.
     *
     * @return the name of the function.
     * 
     */
    public String getName() {
    	return name;
    }

    /**
     * Returns the function parameters.
     */
    public List getParameters() {
    	return params;
    }
    
    /**
     * Sets the function parameters.
     */
    public void setParameters(List params) {
        if(params == null){
            throw new NullPointerException("params can't be null");
        }
        final int argCount = getArgCount();
        final int paramsSize = params.size();
        if(argCount > 0 && argCount != paramsSize){
            throw new IllegalArgumentException("Function "+name+" expected " + argCount + 
                    " arguments, got " + paramsSize);
        }
    	this.params = new ArrayList(params);
    }
    
    /**
     * Gets the number of arguments that are set.
     *
     * @return the number of args.
     */
    public abstract int getArgCount();

    /**
     * @see org.opengis.filter.expression.Expression#accept(ExpressionVisitor, Object)
     */
    public Object accept(ExpressionVisitor visitor, Object extraData) {
    	return visitor.visit(this, extraData);
    }
    
    /**
     * Returns the implementation hints. The default implementation returns an empty map.
     */
    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }
    
    /**
     * Creates a String representation of this Function with
     * the function name and the arguments. The String created
     * should be good for most subclasses
     */
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(getName());
        sb.append("(");
        List params = getParameters();
        if(params != null){
            org.opengis.filter.expression.Expression exp;
            for(Iterator it = params.iterator(); it.hasNext();){
                exp = (Expression) it.next();
                sb.append("[");
                sb.append(exp);
                sb.append("]");
                if(it.hasNext()){
                    sb.append(", ");
                }
            }
        }
        sb.append(")");
        return sb.toString();
    }
        
    public boolean equals(Object obj) {
    	if( obj == null || !(obj instanceof Function)){
    		return false;
    	}
    	Function other = (Function) obj;
    	if( ( getName() == null && other.getName() != null ) ||
    	    (getName() != null && !getName().equalsIgnoreCase( other.getName() ))){
    		return false;
    	}
    	if( getParameters() == null && other.getClass() != null ){
    		return false;
    	}
    	return getParameters() != null && getParameters().equals( other.getParameters() );
    }
}
