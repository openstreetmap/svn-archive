/*
   Copyright (C) 2002 MySQL AB

      This program is free software; you can redistribute it and/or modify
      it under the terms of the GNU General Public License as published by
      the Free Software Foundation; either version 2 of the License, or
      (at your option) any later version.

      This program is distributed in the hope that it will be useful,
      but WITHOUT ANY WARRANTY; without even the implied warranty of
      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
      GNU General Public License for more details.

      You should have received a copy of the GNU General Public License
      along with this program; if not, write to the Free Software
      Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 */
package com.mysql.jdbc;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Various utility methods for converting to/from byte arrays in the platform
 * encoding
 *
 * @author Mark Matthews
 */
public class StringUtils {
    private static final int BYTE_RANGE = (1 + Byte.MAX_VALUE) - Byte.MIN_VALUE;
    private static byte[] allBytes = new byte[BYTE_RANGE];
    private static char[] byteToChars = new char[BYTE_RANGE];

    static {
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
            allBytes[i - Byte.MIN_VALUE] = (byte) i;
        }

        String allBytesString = new String(allBytes, 0,
                Byte.MAX_VALUE - Byte.MIN_VALUE);

        int allBytesStringLen = allBytesString.length();

        for (int i = 0;
                (i < (Byte.MAX_VALUE - Byte.MIN_VALUE))
                && (i < allBytesStringLen); i++) {
            byteToChars[i] = allBytesString.charAt(i);
        }
    }

    /**
     * Returns the byte[] representation of the given string using given
     * encoding.
     *
     * @param s the string to convert
     * @param encoding the character encoding to use
     *
     * @return byte[] representation of the string
     *
     * @throws UnsupportedEncodingException if an encoding unsupported by the
     *         JVM is supplied.
     */
    public static final byte[] getBytes(String s, String encoding, boolean parserKnowsUnicode)
        throws UnsupportedEncodingException {
        SingleByteCharsetConverter converter = SingleByteCharsetConverter
            .getInstance(encoding);

        return getBytes(s, converter, encoding, parserKnowsUnicode);
    }

    /**
     * Returns the byte[] representation of the given string (re)using the
     * given charset converter, and the given encoding.
     *
     * @param s the string to convert
     * @param converter the converter to reuse
     * @param encoding the character encoding to use
     *
     * @return byte[] representation of the string
     *
     * @throws UnsupportedEncodingException if an encoding unsupported by the
     *         JVM is supplied.
     */
    public static final byte[] getBytes(String s,
        SingleByteCharsetConverter converter, String encoding, boolean parserKnowsUnicode)
        throws UnsupportedEncodingException {
        byte[] b = null;

        if (converter != null) {
            b = converter.toBytes(s);
        } else if (encoding == null) {
            b = s.getBytes();
        } else {
            b = s.getBytes(encoding);

            if (!parserKnowsUnicode && (encoding.equalsIgnoreCase("SJIS")
                    || encoding.equalsIgnoreCase("BIG5")
                    || encoding.equalsIgnoreCase("GBK"))) {
                b = escapeEasternUnicodeByteStream(b, s, 0, s.length());
            }
        }

        return b;
    }

    /**
     * DOCUMENT ME!
     *
     * @param s DOCUMENT ME!
     * @param converter DOCUMENT ME!
     * @param encoding DOCUMENT ME!
     * @param offset DOCUMENT ME!
     * @param length DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws UnsupportedEncodingException DOCUMENT ME!
     */
    public static final byte[] getBytes(String s,
        SingleByteCharsetConverter converter, String encoding, int offset,
        int length,
		boolean parserKnowsUnicode) throws UnsupportedEncodingException {
        byte[] b = null;

        if (converter != null) {
            b = converter.toBytes(s, offset, length);
        } else if (encoding == null) {
            byte[] temp = s.getBytes();

            b = new byte[length];
            System.arraycopy(temp, offset, b, 0, length);
        } else {
            byte[] temp = s.getBytes(encoding);

            b = new byte[length];
            System.arraycopy(temp, offset, b, 0, length);

            if (!parserKnowsUnicode && (encoding.equalsIgnoreCase("SJIS")
                    || encoding.equalsIgnoreCase("BIG5")
                    || encoding.equalsIgnoreCase("GBK"))) {
                b = escapeEasternUnicodeByteStream(b, s, offset, length);
            }
        }

        return b;
    }

    /**
     * Dumps the given bytes to STDOUT as a hex dump (up to length bytes).
     *
     * @param byteBuffer the data to print as hex
     * @param length the number of bytes to print
     */
    public static final void dumpAsHex(byte[] byteBuffer, int length) {
        int p = 0;
        int rows = length / 8;

        for (int i = 0; i < rows; i++) {
            int ptemp = p;

            for (int j = 0; j < 8; j++) {
                String hexVal = Integer.toHexString((int) byteBuffer[ptemp]
                        & 0xff);

                if (hexVal.length() == 1) {
                    hexVal = "0" + hexVal;
                }

                System.out.print(hexVal + " ");
                ptemp++;
            }

            System.out.print("    ");

            for (int j = 0; j < 8; j++) {
                if ((byteBuffer[p] > 32) && (byteBuffer[p] < 127)) {
                    System.out.print((char) byteBuffer[p] + " ");
                } else {
                    System.out.print(". ");
                }

                p++;
            }

            System.out.println();
        }

        int n = 0;

        for (int i = p; i < length; i++) {
            String hexVal = Integer.toHexString((int) byteBuffer[i] & 0xff);

            if (hexVal.length() == 1) {
                hexVal = "0" + hexVal;
            }

            System.out.print(hexVal + " ");
            n++;
        }

        for (int i = n; i < 8; i++) {
            System.out.print("   ");
        }

        System.out.print("    ");

        for (int i = p; i < length; i++) {
            if ((byteBuffer[i] > 32) && (byteBuffer[i] < 127)) {
                System.out.print((char) byteBuffer[i] + " ");
            } else {
                System.out.print(". ");
            }
        }

        System.out.println();
    }

    /**
     * Returns the bytes as an ASCII String.
     *
     * @param buffer the bytes representing the string
     *
     * @return The ASCII String.
     */
    public static final String toAsciiString(byte[] buffer) {
        return toAsciiString(buffer, 0, buffer.length);
    }

    /**
     * Returns the bytes as an ASCII String.
     *
     * @param buffer the bytes to convert
     * @param startPos the position to start converting
     * @param length the length of the string to convert
     *
     * @return the ASCII string
     */
    public static final String toAsciiString(byte[] buffer, int startPos,
        int length) {
        char[] charArray = new char[length];
        int readpoint = startPos;

        for (int i = 0; i < length; i++) {
            charArray[i] = (char) buffer[readpoint];
            readpoint++;
        }

        return new String(charArray);
    }

    /**
     * Unfortunately, SJIS has 0x5c as a high byte in some of its double-byte
     * characters, so we need to escape it.
     *
     * @param origBytes the original bytes in SJIS format
     * @param origString the string that had .getBytes() called on it
     * @param offset where to start converting from
     * @param length how many characters to convert.
     *
     * @return byte[] with 0x5c escaped
     */
    public static byte[] escapeEasternUnicodeByteStream(byte[] origBytes,
        String origString, int offset, int length) {
        if ((origBytes == null) || (origBytes.length == 0)) {
            return origBytes;
        }

        int bytesLen = origBytes.length;
        int bufIndex = 0;
        int strIndex = 0;

        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream(bytesLen);

        while (true) {
            if (origString.charAt(strIndex) == '\\') {
                // write it out as-is
            	bytesOut.write(origBytes[bufIndex++]);
            	//bytesOut.write(origBytes[bufIndex++]);
            } else {
                // Grab the first byte
                int loByte = (int) origBytes[bufIndex];

                if (loByte < 0) {
                    loByte += 256; // adjust for signedness/wrap-around
                }

                // We always write the first byte
                bytesOut.write(loByte);

                //
                // The codepage characters in question exist between
                // 0x81-0x9F and 0xE0-0xFC...
                //
                // See:
                //
                // http://www.microsoft.com/GLOBALDEV/Reference/dbcs/932.htm
                //
                // Problematic characters in GBK
                //
                // U+905C : CJK UNIFIED IDEOGRAPH
                //
                // Problematic characters in Big5
                //
                // B9F0 = U+5C62 : CJK UNIFIED IDEOGRAPH
                //
                if (loByte >= 0x80) {
                    if (bufIndex < (bytesLen - 1)) {
                        int hiByte = (int) origBytes[bufIndex + 1];

                        if (hiByte < 0) {
                            hiByte += 256; // adjust for signedness/wrap-around
                        }

                        // write the high byte here, and increment the index
                        // for the high byte
                        bytesOut.write(hiByte);
                        bufIndex++;

                        // escape 0x5c if necessary
                        if (hiByte == 0x5C) {
                            bytesOut.write(hiByte);
                        }
                    }
                } else if (loByte == 0x5c) {
                    if (bufIndex < (bytesLen - 1)) {
                        int hiByte = (int) origBytes[bufIndex + 1];

                        if (hiByte < 0) {
                            hiByte += 256; // adjust for signedness/wrap-around
                        }

                        if (hiByte == 0x62) {
                            // we need to escape the 0x5c
                            bytesOut.write(0x5c);
                            bytesOut.write(0x62);
                            bufIndex++;
                        }
                    }
                }

				bufIndex++;

                
            }

			if (bufIndex >= bytesLen) {
				// we're done
				break;
			}
			
            strIndex++;
        }

        return bytesOut.toByteArray();
    }

    /**
     * Returns the first non whitespace char, converted to upper case
     *
     * @param searchIn the string to search in
     *
     * @return the first non-whitespace character, upper cased.
     */
    public static char firstNonWsCharUc(String searchIn) {
        if (searchIn == null) {
            return 0;
        }

        int length = searchIn.length();

        for (int i = 0; i < length; i++) {
            char c = searchIn.charAt(i);

            if (!Character.isWhitespace(c)) {
                return Character.toUpperCase(c);
            }
        }

        return 0;
    }

    /**
     * Splits stringToSplit into a list, using the given delimitter
     *
     * @param stringToSplit the string to split
     * @param delimitter the string to split on
     * @param trim should the split strings be whitespace trimmed?
     *
     * @return the list of strings, split by delimitter
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static final List split(String stringToSplit, String delimitter,
        boolean trim) {
        if (stringToSplit == null) {
            return new ArrayList();
        }

        if (delimitter == null) {
            throw new IllegalArgumentException();
        }

        StringTokenizer tokenizer = new StringTokenizer(stringToSplit,
                delimitter, false);

        List splitTokens = new ArrayList(tokenizer.countTokens());

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();

            if (trim) {
                token = token.trim();
            }

            splitTokens.add(token);
        }

        return splitTokens;
    }

    /**
     * Determines whether or not the string 'searchIn' contains the string
     * 'searchFor', dis-regarding case. Shorthand for a
     * String.regionMatch(...)
     *
     * @param searchIn the string to search in
     * @param searchFor the string to search for
     *
     * @return whether searchIn starts with searchFor, ignoring case
     */
    public static boolean startsWithIgnoreCase(String searchIn, String searchFor) {
        return startsWithIgnoreCase(searchIn, 0, searchFor);
    }

    /**
     * Determines whether or not the string 'searchIn' contains the string
     * 'searchFor', dis-regarding case starting at 'startAt' Shorthand for a
     * String.regionMatch(...)
     *
     * @param searchIn the string to search in
     * @param startAt the position to start at
     * @param searchFor the string to search for
     *
     * @return whether searchIn starts with searchFor, ignoring case
     */
    public static boolean startsWithIgnoreCase(String searchIn, int startAt,
        String searchFor) {
        return searchIn.regionMatches(true, 0, searchFor, startAt,
            searchFor.length());
    }

    /**
     * Determines whether or not the sting 'searchIn' contains the string
     * 'searchFor', di-regarding case and leading whitespace
     *
     * @param searchIn the string to search in
     * @param searchFor the string to search for
     *
     * @return true if the string starts with 'searchFor' ignoring whitespace
     */
    public static boolean startsWithIgnoreCaseAndWs(String searchIn,
        String searchFor) {
        int beginPos = 0;

        int inLength = searchIn.length();

        for (beginPos = 0; beginPos < inLength; beginPos++) {
            if (!Character.isWhitespace(searchIn.charAt(beginPos))) {
                break;
            }
        }

        return startsWithIgnoreCase(searchIn, beginPos, searchFor);
    }
}
