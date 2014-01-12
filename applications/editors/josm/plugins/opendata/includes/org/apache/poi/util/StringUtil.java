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

import java.io.UnsupportedEncodingException;
import java.text.FieldPosition;
import java.text.NumberFormat;

import org.apache.poi.hssf.record.RecordInputStream;
/**
 *  Title: String Utility Description: Collection of string handling utilities<p/>
 *
 * Note - none of the methods in this class deals with {@link org.apache.poi.hssf.record.ContinueRecord}s.  For such
 * functionality, consider using {@link RecordInputStream
} *
 *
 *@author     Andrew C. Oliver
 *@author     Sergei Kozello (sergeikozello at mail.ru)
 *@author     Toshiaki Kamoshida (kamoshida.toshiaki at future dot co dot jp)
 */
public class StringUtil {
	private static final String ENCODING_ISO_8859_1 = "ISO-8859-1";

	private StringUtil() {
		// no instances of this class
	}



	public static String readCompressedUnicode(LittleEndianInput in, int nChars) {
		char[] buf = new char[nChars];
		for (int i = 0; i < buf.length; i++) {
			buf[i] = (char) in.readUByte();
		}
		return new String(buf);
	}
	/**
	 * InputStream <tt>in</tt> is expected to contain:
	 * <ol>
	 * <li>ushort nChars</li>
	 * <li>byte is16BitFlag</li>
	 * <li>byte[]/char[] characterData</li>
	 * </ol>
	 * For this encoding, the is16BitFlag is always present even if nChars==0.
	 * 
	 * This structure is also known as a XLUnicodeString.
	 */
	public static String readUnicodeString(LittleEndianInput in) {

		int nChars = in.readUShort();
		byte flag = in.readByte();
		if ((flag & 0x01) == 0) {
			return readCompressedUnicode(in, nChars);
		}
		return readUnicodeLE(in, nChars);
	}
	/**
	 * InputStream <tt>in</tt> is expected to contain:
	 * <ol>
	 * <li>byte is16BitFlag</li>
	 * <li>byte[]/char[] characterData</li>
	 * </ol>
	 * For this encoding, the is16BitFlag is always present even if nChars==0.
	 * <br/>
	 * This method should be used when the nChars field is <em>not</em> stored
	 * as a ushort immediately before the is16BitFlag. Otherwise, {@link
	 * #readUnicodeString(LittleEndianInput)} can be used.
	 */
	public static String readUnicodeString(LittleEndianInput in, int nChars) {
		byte is16Bit = in.readByte();
		if ((is16Bit & 0x01) == 0) {
			return readCompressedUnicode(in, nChars);
		}
		return readUnicodeLE(in, nChars);
	}

	/**
	 * OutputStream <tt>out</tt> will get:
	 * <ol>
	 * <li>ushort nChars</li>
	 * <li>byte is16BitFlag</li>
	 * <li>byte[]/char[] characterData</li>
	 * </ol>
	 * For this encoding, the is16BitFlag is always present even if nChars==0.
	 */
	public static void writeUnicodeString(LittleEndianOutput out, String value) {

		int nChars = value.length();
		out.writeShort(nChars);
		boolean is16Bit = hasMultibyte(value);
		out.writeByte(is16Bit ? 0x01 : 0x00);
		if (is16Bit) {
			putUnicodeLE(value, out);
		} else {
			putCompressedUnicode(value, out);
		}
	}
	/**
	 * OutputStream <tt>out</tt> will get:
	 * <ol>
	 * <li>byte is16BitFlag</li>
	 * <li>byte[]/char[] characterData</li>
	 * </ol>
	 * For this encoding, the is16BitFlag is always present even if nChars==0.
	 * <br/>
	 * This method should be used when the nChars field is <em>not</em> stored
	 * as a ushort immediately before the is16BitFlag. Otherwise, {@link
	 * #writeUnicodeString(LittleEndianOutput, String)} can be used.
	 */
	public static void writeUnicodeStringFlagAndData(LittleEndianOutput out, String value) {
		boolean is16Bit = hasMultibyte(value);
		out.writeByte(is16Bit ? 0x01 : 0x00);
		if (is16Bit) {
			putUnicodeLE(value, out);
		} else {
			putCompressedUnicode(value, out);
		}
	}

	/**
	 * @return the number of bytes that would be written by {@link #writeUnicodeString(LittleEndianOutput, String)}
	 */
	public static int getEncodedSize(String value) {
		int result = 2 + 1;
		result += value.length() * (StringUtil.hasMultibyte(value) ? 2 : 1);
		return result;
	}

	public static void putCompressedUnicode(String input, LittleEndianOutput out) {
		byte[] bytes;
		try {
			bytes = input.getBytes(ENCODING_ISO_8859_1);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		out.write(bytes);
	}

	public static void putUnicodeLE(String input, LittleEndianOutput out) {
		byte[] bytes;
		try {
			bytes = input.getBytes("UTF-16LE");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		out.write(bytes);
	}

	public static String readUnicodeLE(LittleEndianInput in, int nChars) {
		char[] buf = new char[nChars];
		for (int i = 0; i < buf.length; i++) {
			buf[i] = (char) in.readUShort();
		}
		return new String(buf);
	}

	/**
	 *  Apply printf() like formatting to a string.
	 *  Primarily used for logging.
	 * @param  message  the string with embedded formatting info
	 *                 eg. "This is a test %2.2"
	 * @param  params   array of values to format into the string
	 * @return          The formatted string
	 */
	public static String format(String message, Object[] params) {
		int currentParamNumber = 0;
		StringBuffer formattedMessage = new StringBuffer();
		for (int i = 0; i < message.length(); i++) {
			if (message.charAt(i) == '%') {
				if (currentParamNumber >= params.length) {
					formattedMessage.append("?missing data?");
				} else if (
					(params[currentParamNumber] instanceof Number)
						&& (i + 1 < message.length())) {
					i
						+= matchOptionalFormatting(
							(Number) params[currentParamNumber++],
							message.substring(i + 1),
							formattedMessage);
				} else {
					formattedMessage.append(
						params[currentParamNumber++].toString());
				}
			} else {
				if ((message.charAt(i) == '\\')
					&& (i + 1 < message.length())
					&& (message.charAt(i + 1) == '%')) {
					formattedMessage.append('%');
					i++;
				} else {
					formattedMessage.append(message.charAt(i));
				}
			}
		}
		return formattedMessage.toString();
	}


	private static int matchOptionalFormatting(
		Number number,
		String formatting,
		StringBuffer outputTo) {
		NumberFormat numberFormat = NumberFormat.getInstance();
		if ((0 < formatting.length())
			&& Character.isDigit(formatting.charAt(0))) {
			numberFormat.setMinimumIntegerDigits(
				Integer.parseInt(formatting.charAt(0) + ""));
			if ((2 < formatting.length())
				&& (formatting.charAt(1) == '.')
				&& Character.isDigit(formatting.charAt(2))) {
				numberFormat.setMaximumFractionDigits(
					Integer.parseInt(formatting.charAt(2) + ""));
				numberFormat.format(number, outputTo, new FieldPosition(0));
				return 3;
			}
			numberFormat.format(number, outputTo, new FieldPosition(0));
			return 1;
		} else if (
			(0 < formatting.length()) && (formatting.charAt(0) == '.')) {
			if ((1 < formatting.length())
				&& Character.isDigit(formatting.charAt(1))) {
				numberFormat.setMaximumFractionDigits(
					Integer.parseInt(formatting.charAt(1) + ""));
				numberFormat.format(number, outputTo, new FieldPosition(0));
				return 2;
			}
		}
		numberFormat.format(number, outputTo, new FieldPosition(0));
		return 1;
	}


	/**
	 * check the parameter has multibyte character
	 *
	 * @param value string to check
	 * @return boolean result true:string has at least one multibyte character
	 */
	public static boolean hasMultibyte(String value) {
		if (value == null)
			return false;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c > 0xFF) {
				return true;
			}
		}
		return false;
	}

	
}
