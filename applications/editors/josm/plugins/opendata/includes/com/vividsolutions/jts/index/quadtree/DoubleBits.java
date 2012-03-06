
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jts.index.quadtree;

/**
 * DoubleBits manipulates Double numbers
 * by using bit manipulation and bit-field extraction.
 * For some operations (such as determining the exponent)
 * this is more accurate than using mathematical operations
 * (which suffer from round-off error).
 * <p>
 * The algorithms and constants in this class
 * apply only to IEEE-754 double-precision floating point format.
 *
 * @version 1.7
 */
public class DoubleBits {

  public static final int EXPONENT_BIAS = 1023;

  public static double powerOf2(int exp)
  {
    if (exp > 1023 || exp < -1022)
      throw new IllegalArgumentException("Exponent out of bounds");
    long expBias = exp + EXPONENT_BIAS;
    long bits = expBias << 52;
    return Double.longBitsToDouble(bits);
  }

  public static int exponent(double d)
  {
    DoubleBits db = new DoubleBits(d);
    return db.getExponent();
  }

  private double x;
  private long xBits;

  public DoubleBits(double x)
  {
    this.x = x;
    xBits = Double.doubleToLongBits(x);
  }

  /**
   * Determines the exponent for the number
   */
  public int biasedExponent()
  {
    int signExp = (int) (xBits >> 52);
    int exp = signExp & 0x07ff;
    return exp;
  }

  /**
   * Determines the exponent for the number
   */
  public int getExponent()
  {
    return biasedExponent() - EXPONENT_BIAS;
  }

  /**
   * A representation of the Double bits formatted for easy readability
   */
  public String toString()
  {
    String numStr = Long.toBinaryString(xBits);
    // 64 zeroes!
    String zero64 = "0000000000000000000000000000000000000000000000000000000000000000";
    String padStr =  zero64 + numStr;
    String bitStr = padStr.substring(padStr.length() - 64);
    String str = bitStr.substring(0, 1) + "  "
        + bitStr.substring(1, 12) + "(" + getExponent() + ") "
        + bitStr.substring(12)
        + " [ " + x + " ]";
    return str;
  }
}
