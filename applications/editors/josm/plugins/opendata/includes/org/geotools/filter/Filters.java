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
package org.geotools.filter;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.expression.Expression;

/**
 * Utility class for working with Filters & Expression.
 * <p>
 * To get the full benifit you will need to create an instanceof
 * this Object (supports your own custom FilterFactory!). Additional
 * methods to help create expressions are available.
 * </p>
 * <p>
 * Example use:
 * <pre><code>
 * Filters filters = new Filters( factory );
 * filters.duplicate( origional );
 * </code></pre>
 * The above example creates a copy of the provided Filter,
 * the factory provided will be used when creating the duplicated
 * content.
 * </p>
 * <h3>Expression</h3>
 * <p>
 * Expressions form an interesting little semi scripting languge,
 * intended for queries.  A interesting Feature of Filter as a language
 * is that it is not strongly typed. This utility class many helper
 * methods that ease the transition from Strongly typed Java to the more
 * relaxed setting of Expression where most everything can be a string.
 * </p>
 * <pre><code>
 * double sum = Filters.number( Object ) + Filters.number( Object );
 * </code></pre>
 * The above example will support the conversion of many things into a format
 * suitable for addition - the complete list is something like:
 * <ul>
 * <li>Any instance of Number
 * <li>"1234" - aka Integer
 * <li>"#FFF" - aka Integer 
 * <li>"123.0" - aka Double
 * </ul>
 * A few things (like Geometry and "ABC") will not be considered addative.
 * </p>
 * In general the scope of these functions should be similar to that
 * allowed by the XML Atomic Types, aka those that can be seperated by
 * whitespace to form a list.
 * </p>
 * <p>
 * We do our best to be forgiving, any Java class which takes a String as
 * a constructor can be tried, and toString() assumed to be the inverse. This
 * lets many things (like URL and Date) function without modification.
 * </p>
 * 
 * @author Jody Garnett, Refractions Research
 * @since GeoTools 2.2.M3
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/filter/Filters.java $
 */
public class Filters {
	
	public Filters(){
		this( CommonFactoryFinder.getFilterFactory2(null) );
	}	
	public Filters( org.opengis.filter.FilterFactory2 factory ){
	}

      
    /**
     * Treat provided value as a Number, used for math opperations.
     * <p>
     * This function allows for the non stongly typed Math Opperations
     * favoured by the Expression standard.
     * </p>
     * <p>
     * Able to hanle:
     * <ul>
     * <li>null - to NaN
     * <li>Number
     * <li>String - valid Integer and Double encodings
     * </ul>
     * 
     * </p>
     * @param value
     * @return double or Double.NaN;
     * @throws IllegalArgumentException For non numerical among us -- like Geometry 
     */
    public static double number(Object value) {
    	if( value == null ) return Double.NaN;
    	if( value instanceof Number ){
    		Number number = (Number) value;
    		return number.doubleValue();
    	}
    	if( value instanceof String ){
    		String text = (String) value;
    		try {
				Number number = (Number) gets( text, Number.class );
				return number.doubleValue();
			} catch (Throwable e) {
				throw new IllegalArgumentException("Unable to decode '"+text+"' as a number" );				
			}    		
    	}
    	if( value instanceof Expression ){
    		throw new IllegalArgumentException("Cannot deal with un evaulated Expression");
    	}
    	throw new IllegalArgumentException("Unable to evaulate "+value.getClass()+" in a numeric context");
    }
    
    /**
     * Used to upcovnert a "Text Value" into the provided TYPE.
     * <p>
     * Used to tread softly on the Java typing system, because
     * Filter/Expression is not strongly typed. Values in in
     * Expression land are often not the the real Java Objects
     * we wish they were - it is reall a small, lax, query
     * language and Java objects need a but of help getting
     * through.
     * <p>
     * </p>
     * A couple notes:
     * <ul>
     * <li>Usual trick of reflection for a Constructors that
     *     supports a String parameter is used as a last ditch effort.
     *     </li>
     * <li>will do its best to turn Object into the indicated Class
     * <li>will be used for ordering literals against attribute values
     *     are calculated at runtime (like Date.)
     * </ul>
     * Remember Strong typing is for whimps who know what they are
     * doing ahead of time. Real programmers let their program
     * learn at runtime... :-)
     * </p>
     * 
     * @param text
     * @param TYPE
     * @throws open set of Throwable reflection for TYPE( String ) 
     */
    public static Object gets( String text, Class TYPE ) throws Throwable {
    	if( text == null ) return null;
    	if( TYPE == String.class ) return text;
    	if( TYPE == Integer.class ) {
    		return Integer.decode( text );    		
    	}
    	if( TYPE == Double.class ){
    		return Double.valueOf( text );
    	}
    	if( TYPE == Number.class ){
    		try {
    			return Double.valueOf( text );
    		}
    		catch( NumberFormatException ignore ){
    		}
    		return Integer.decode( text );    		
    	}
    	if( TYPE == Color.class ){
    		return new Color( Integer.decode( text ).intValue() );
    	}    	
    	try {
			Constructor create = TYPE.getConstructor( new Class[]{String.class});
			return create.newInstance( new Object[]{ text } );
		} catch (SecurityException e) {
			// hates you
		} catch (NoSuchMethodException e) {
			// nope
		} catch (IllegalArgumentException e) {
			// should not occur
		} catch (InstantiationException e) {
			// should not occur, perhaps the class was abstract?
			// eg. Number.class is a bad idea
		} catch (IllegalAccessException e) {
			// hates you
		} catch (InvocationTargetException e) {
			// should of worked but we got a real problem,
			// an actual problem
			throw e.getCause();
		}    	
    	return null;
    }
}

