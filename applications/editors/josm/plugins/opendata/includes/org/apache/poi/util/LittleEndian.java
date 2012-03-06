/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.util;


/**
 *  a utility class for handling little-endian numbers, which the 80x86 world is
 *  replete with. The methods are all static, and input/output is from/to byte
 *  arrays, or from InputStreams.
 *
 *@author     Marc Johnson (mjohnson at apache dot org)
 *@author     Andrew Oliver (acoliver at apache dot org)
 */
public class LittleEndian implements LittleEndianConsts {

    private LittleEndian() {
        // no instances of this class
    }

    /**
     *  get a short value from a byte array
     *
     *@param  data    the byte array
     *@param  offset  a starting offset into the byte array
     *@return         the short (16-bit) value
     */
    public static short getShort(byte[] data, int offset) {
        int b0 = data[offset] & 0xFF;
        int b1 = data[offset+1] & 0xFF;
        return (short) ((b1 << 8) + (b0 << 0));
    }


    /**
     *  get an unsigned short value from a byte array
     *
     *@param  data    the byte array
     *@param  offset  a starting offset into the byte array
     *@return         the unsigned short (16-bit) value in an integer
     */
    public static int getUShort(byte[] data, int offset) {
        int b0 = data[offset] & 0xFF;
        int b1 = data[offset+1] & 0xFF;
        return (b1 << 8) + (b0 << 0);
    }


    /**
     *  get an unsigned short value from the beginning of a byte array
     *
     *@param  data  the byte array
     *@return       the unsigned short (16-bit) value in an int
     */
    public static int getUShort(byte[] data) {
        return getUShort(data, 0);
    }

    /**
     *  get an int value from a byte array
     *
     *@param  data    the byte array
     *@param  offset  a starting offset into the byte array
     *@return         the int (32-bit) value
     */
    public static int getInt(byte[] data, int offset) {
        int i=offset;
        int b0 = data[i++] & 0xFF;
        int b1 = data[i++] & 0xFF;
        int b2 = data[i++] & 0xFF;
        int b3 = data[i++] & 0xFF;
        return (b3 << 24) + (b2 << 16) + (b1 << 8) + (b0 << 0);
    }




    /**
     *  get an unsigned int value from a byte array
     *
     *@param  data    the byte array
     *@param  offset  a starting offset into the byte array
     *@return         the unsigned int (32-bit) value in a long
     */
    public static long getUInt(byte[] data, int offset) {
        long retNum = getInt(data, offset);
        return retNum & 0x00FFFFFFFF;
    }


    /**
     *  get a long value from a byte array
     *
     *@param  data    the byte array
     *@param  offset  a starting offset into the byte array
     *@return         the long (64-bit) value
     */
    public static long getLong(byte[] data, int offset) {
        long result = 0;
		
		for (int j = offset + LONG_SIZE - 1; j >= offset; j--) {
		    result <<= 8;
		    result |= 0xff & data[j];
		}
		return result;
    }

    /**
     *  get a double value from a byte array, reads it in little endian format
     *  then converts the resulting revolting IEEE 754 (curse them) floating
     *  point number to a happy java double
     *
     *@param  data    the byte array
     *@param  offset  a starting offset into the byte array
     *@return         the double (64-bit) value
     */
    public static double getDouble(byte[] data, int offset) {
        return Double.longBitsToDouble(getLong(data, offset));
    }

    
    /**
     *  put a short value into a byte array
     *
     *@param  data    the byte array
     *@param  offset  a starting offset into the byte array
     *@param  value   the short (16-bit) value
     */
    public static void putShort(byte[] data, int offset, short value) {
        int i = offset;
        data[i++] = (byte)((value >>>  0) & 0xFF);
        data[i++] = (byte)((value >>>  8) & 0xFF);
    }

    /**
     *  put a short value into beginning of a byte array
     *
     *@param  data   the byte array
     *@param  value  the short (16-bit) value
     */
    public static void putShort(byte[] data, short value) {
        putShort(data, 0, value);
    }


    /**
     *  put an int value into a byte array
     *
     *@param  data    the byte array
     *@param  offset  a starting offset into the byte array
     *@param  value   the int (32-bit) value
     */
    public static void putInt(byte[] data, int offset, int value) {
        int i = offset;
        data[i++] = (byte)((value >>>  0) & 0xFF);
        data[i++] = (byte)((value >>>  8) & 0xFF);
        data[i++] = (byte)((value >>> 16) & 0xFF);
        data[i++] = (byte)((value >>> 24) & 0xFF);
    }



    /**
     *  put a long value into a byte array
     *
     *@param  data    the byte array
     *@param  offset  a starting offset into the byte array
     *@param  value   the long (64-bit) value
     */
    public static void putLong(byte[] data, int offset, long value) {
        int limit = LONG_SIZE + offset;
        long v = value;
        
        for (int j = offset; j < limit; j++) {
            data[j] = (byte) (v & 0xFF);
            v >>= 8;
        }
    }


    /**
     *  put a double value into a byte array
     *
     *@param  data    the byte array
     *@param  offset  a starting offset into the byte array
     *@param  value   the double (64-bit) value
     */
    public static void putDouble(byte[] data, int offset, double value) {
        putLong(data, offset, Double.doubleToLongBits(value));
    }
}
