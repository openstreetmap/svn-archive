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
package org.geotools.referencing.wkt;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;


/**
 * Base class for <cite>Well Know Text</cite> (WKT) parser.
 *
 * @since 2.0
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/referencing/wkt/AbstractParser.java $
 * @version $Id: AbstractParser.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Remi Eve
 * @author Martin Desruisseaux (IRD)
 *
 * @see <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html">Well Know Text specification</A>
 * @see <A HREF="http://gdal.velocet.ca/~warmerda/wktproblems.html">OGC WKT Coordinate System Issues</A>
 */
public abstract class AbstractParser {
    /**
     * Set to {@code true} if parsing of number in scientific notation is allowed.
     * The way to achieve that is currently a hack, because {@link NumberFormat} has no
     * API for managing that as of J2SE 1.5.
     *
     * @todo See if a future version of J2SE allows us to get ride of this ugly hack.
     */
    private static final boolean SCIENTIFIC_NOTATION = true;

    /**
     * The symbols to use for parsing WKT.
     */
    final Symbols symbols;

    /**
     * The object to use for parsing numbers.
     */
    private final NumberFormat numberFormat;

    /**
     * Constructs a parser using the specified set of symbols.
     *
     * @param symbols The set of symbols to use.
     */
    public AbstractParser(final Symbols symbols) {
        this.symbols      = symbols;
        this.numberFormat = (NumberFormat) symbols.numberFormat.clone();
        if (SCIENTIFIC_NOTATION && numberFormat instanceof DecimalFormat) {
            final DecimalFormat numberFormat = (DecimalFormat) this.numberFormat;
            String pattern = numberFormat.toPattern();
            if (pattern.indexOf("E0") < 0) {
                final int split = pattern.indexOf(';');
                if (split >= 0) {
                    pattern = pattern.substring(0, split) + "E0" + pattern.substring(split);
                }
                pattern += "E0";
                numberFormat.applyPattern(pattern);
            }
        }
    }

    /**
     * Parses a <cite>Well Know Text</cite> (WKT).
     *
     * @param  text The text to be parsed.
     * @return The object.
     * @throws ParseException if the string can't be parsed.
     */
    public final Object parseObject(final String text) throws ParseException {
        final Element element = getTree(text, new ParsePosition(0));
        final Object object = parse(element);
        element.close();
        return object;
    }

    /**
     * Parse the number at the given position.
     */
    final Number parseNumber(String text, final ParsePosition position) {
        if (SCIENTIFIC_NOTATION) {
            /*
             * HACK: DecimalFormat.parse(...) do not understand lower case 'e' for scientific
             *       notation. It understand upper case 'E' only. Performs the replacement...
             */
            final int base = position.getIndex();
            Number number = numberFormat.parse(text, position);
            if (number != null) {
                int i = position.getIndex();
                if (i<text.length() && text.charAt(i)=='e') {
                    final StringBuilder buffer = new StringBuilder(text);
                    buffer.setCharAt(i, 'E');
                    text = buffer.toString();
                    position.setIndex(base);
                    number = numberFormat.parse(text, position);
                }
            }
            return number;
        } else {
            return numberFormat.parse(text, position);
        }
    }

    /**
     * Parses the next element in the specified <cite>Well Know Text</cite> (WKT) tree.
     *
     * @param  element The element to be parsed.
     * @return The object.
     * @throws ParseException if the element can't be parsed.
     */
    protected abstract Object parse(final Element element) throws ParseException;

    /**
     * Returns a tree of {@link Element} for the specified text.
     *
     * @param  text       The text to parse.
     * @param  position   In input, the position where to start parsing from.
     *                    In output, the first character after the separator.
     * @return The tree of elements to parse.
     * @throws ParseException If an parsing error occured while creating the tree.
     */
    protected final Element getTree(final String text, final ParsePosition position)
            throws ParseException
    {
        return new Element(new Element(this, text, position));
    }
}
