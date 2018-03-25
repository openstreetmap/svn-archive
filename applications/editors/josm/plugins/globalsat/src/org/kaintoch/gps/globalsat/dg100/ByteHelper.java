/**
 * @author Stefan Kaintoch
 * @version $Id: ByteHelper.java 3 2007-10-30 19:40:04Z ramack $
 */
package org.kaintoch.gps.globalsat.dg100;

/**
 * @author skaintoch
 *
 */
public class ByteHelper
{

    /**
     * Copy one byte array into another
     * @param src the array to copy data from
     * @param startS start index in source array
     * @param len number of entries to copy
     * @param dest the array to copy into
     * @param startD start index in target array
     * @return the index pointer behind the last target array copied
     */
    public static int copyByteArr2ByteArr(byte src[], int startS, int len, byte dest[], int startD)
    {
        for (int ii = 0 ;
            ii < len && startD < dest.length && startS < src.length && startD >= 0 && startS >= 0;
            ++ii, ++startD, ++startS)
        {
            dest[startD] = src[startS];
        }
        return startD;
    }

    /**
     * Convert byte to unsigned integer
     * @param byt byte value to convert
     * @return unsigned integer
     */
    public static int byte2IntUnsigned(byte byt)
    {
        return ((byt >= 0) ? byt : 256 + byt);
    }

    /**
     * Convert 4 bit into hexadecimal character
     * @param nibble value to convert (only last 4 bit used)
     * @return character in range of 0 to F
     */
    private static char nibble2Char(int nibble)
    {
        char chr = '*';
        nibble = nibble & 0xF;
        switch (nibble)
        {
        case 0: chr = '0'; break;
        case 1: chr = '1'; break;
        case 2: chr = '2'; break;
        case 3: chr = '3'; break;
        case 4: chr = '4'; break;
        case 5: chr = '5'; break;
        case 6: chr = '6'; break;
        case 7: chr = '7'; break;
        case 8: chr = '8'; break;
        case 9: chr = '9'; break;
        case 10: chr = 'A'; break;
        case 11: chr = 'B'; break;
        case 12: chr = 'C'; break;
        case 13: chr = 'D'; break;
        case 14: chr = 'E'; break;
        case 15: chr = 'F'; break;
        }
        return chr;
    }

    /**
     * Convert byte to unsigned hexadecimal string
     * @param byt byte value to convert
     * @return 2 character string representing the last 8 bit
     */
    public static String byte2StringUnsigned(byte byt)
    {
        StringBuffer buf = new StringBuffer(4);
        int bb = byte2IntUnsigned(byt);
        //buf.append("0x");
        buf.append(nibble2Char(bb / 16));
        buf.append(nibble2Char(bb % 16));
        return buf.toString();
    }

    /**
     * Convert byte array to unsigned hexadecimal string
     * @param byt array of bytes to convert
     * @return hexadecimal string representing the byte array
     */
    public static String byteArray2String(byte byt[])
    {
        return byteArray2String(byt, byt.length);
    }

    /**
     * Convert byte array to unsigned hexadecimal string
     * @param byt array of bytes to convert
     * @param cnt number of entries to convert
     * @return hexadecimal string representing the byte array
     */
    public static String byteArray2String(byte byt[], int cnt)
    {
        StringBuffer buf = new StringBuffer(cnt * 5);
        for (int ii = 0 ; ii < cnt && ii < byt.length ; ++ii)
        {
            if (ii > 0) {buf.append(" ");}
            buf.append(byte2StringUnsigned(byt[ii]));
        }
        return buf.toString();
    }

}
