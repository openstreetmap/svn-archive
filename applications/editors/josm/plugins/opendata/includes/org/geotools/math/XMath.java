/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2001-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.math;

import static org.geotools.resources.XMath.next;
import static org.geotools.resources.XMath.previous;


/**
 * Simple mathematical functions in addition to the ones provided in {@link Math}.
 *
 * @since 2.5
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/metadata/src/main/java/org/geotools/math/XMath.java $
 * @version $Id: XMath.java 37298 2011-05-25 05:16:15Z mbedward $
 * @author Martin Desruisseaux (IRD)
 */
public final class XMath {

    /**
     * Do not allow instantiation of this class.
     */
    private XMath() {
    }

    /**
     * Tries to remove at least {@code n} fraction digits in the decimal representation of
     * the specified value. This method tries small changes to {@code value}, by adding or
     * substracting up to {@code maxULP} (Unit in the Last Place). If there is no small
     * change that remove at least {@code n} fraction digits, then the value is returned
     * unchanged. This method is used for hiding rounding errors, like in conversions from
     * radians to degrees.
     * <P>
     * Example:
     * {@code XMath.trimLastDecimalDigits(-61.500000000000014, 12, 4)} returns {@code -61.5}.
     *
     * @param  value The value to fix.
     * @param  maxULP The maximal change allowed in ULPs (Unit in the Last Place).
     *         A typical value is 4.
     * @param  n The minimum amount of fraction digits.
     * @return The trimmed value, or the unchanged {@code value} if there is no small change
     *         that remove at least {@code n} fraction digits.
     */
    public static double trimDecimalFractionDigits(final double value, final int maxULP, int n) {
        double lower = value;
        double upper = value;
        n = countDecimalFractionDigits(value) - n;
        if (n > 0) {
            for (int i=0; i<maxULP; i++) {
                if (countDecimalFractionDigits(lower = previous(lower)) <= n) return lower;
                if (countDecimalFractionDigits(upper = next    (upper)) <= n) return upper;
            }
        }
        return value;
    }

    /**
     * Counts the fraction digits in the string representation of the specified value. This method
     * is equivalent to a calling <code>{@linkplain Double#toString(double) Double.toString}(value)</code>
     * and counting the number of digits after the decimal separator.
     *
     * @param value The value for which to count the fraction digits.
     * @return The number of fraction digits.
     */
    public static int countDecimalFractionDigits(final double value) {
        final String asText = Double.toString(value);
        final int exp = asText.indexOf('E');
        int upper, power;
        if (exp >= 0) {
            upper = exp;
            power = Integer.parseInt(asText.substring(exp+1));
        } else {
            upper = asText.length();
            power = 0;
        }
        while ((asText.charAt(--upper)) == '0');
        return Math.max(upper - asText.indexOf('.') - power, 0);
    }
}
