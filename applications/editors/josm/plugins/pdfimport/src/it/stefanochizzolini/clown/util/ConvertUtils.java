/*
  Copyright 2009 Stefano Chizzolini. http://clown.stefanochizzolini.it

  Contributors:
    * Stefano Chizzolini (original code developer, http://www.stefanochizzolini.it)

  This file should be part of the source code distribution of "PDF Clown library"
  (the Program): see the accompanying README files for more info.

  This Program is free software; you can redistribute it and/or modify it under the terms
  of the GNU Lesser General Public License as published by the Free Software Foundation;
  either version 3 of the License, or (at your option) any later version.

  This Program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY,
  either expressed or implied; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the License for more details.

  You should have received a copy of the GNU Lesser General Public License along with this
  Program (see README files); if not, go to the GNU website (http://www.gnu.org/licenses/).

  Redistribution and use, with or without modification, are permitted provided that such
  redistributions retain the above copyright notice, license and disclaimer, along with
  this list of conditions.
*/

package it.stefanochizzolini.clown.util;

import java.nio.ByteOrder;

/**
  Data convertion utility.
  <h3>Remarks</h3>
  <p>This class is a specialized adaptation from the original <a href="http://commons.apache.org/codec/">
  Apache Commons Codec</a> project, licensed under the <a href="http://www.apache.org/licenses/LICENSE-2.0">
  Apache License, Version 2.0</a>.</p>

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.8
*/
public class ConvertUtils
{
  // <class>
  // <static>
  // <fields>
  private static final char[] HexDigits = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
  // </fields>

  // <interface>
  // <public>
  public static String byteArrayToHex(
    byte[] data
    )
  {
    int dataLength = data.length;
    char[] out = new char[dataLength * 2];
    for(
      int dataIndex = 0,
        outIndex = 0;
      dataIndex < dataLength;
      dataIndex++
      )
    {
      out[outIndex++] = HexDigits[(0xF0 & data[dataIndex]) >>> 4];
      out[outIndex++] = HexDigits[0x0F & data[dataIndex]];
    }

    return new String(out);
  }

  public static int byteArrayToInt(
    byte[] data
    )
  {return byteArrayToInt(data,0,ByteOrder.BIG_ENDIAN);}

  public static int byteArrayToInt(
    byte[] data,
    int index,
    ByteOrder byteOrder
    )
  {return byteArrayToNumber(data,index,4,byteOrder);}

  public static int byteArrayToNumber(
    byte[] data,
    int index,
    int length,
    ByteOrder byteOrder
    )
  {
    int value = 0;
    length = Math.min(length,data.length-index);
    for(
      int i = index,
        endIndex = index+length;
      i < endIndex;
      i++
      )
    {value |= (data[i] & 0xff) << 8 * (byteOrder == ByteOrder.LITTLE_ENDIAN ? i-index : endIndex-i-1);}

    return value;
  }

  public static byte[] hexToByteArray(
    String data
    )
  {
    char[] dataChars = data.toCharArray();
    int dataLength = dataChars.length;
    if((dataLength % 2) != 0)
      throw new RuntimeException("Odd number of characters.");

    byte[] out = new byte[dataLength / 2];
    for(
      int outIndex = 0,
        dataIndex = 0;
      dataIndex < dataLength;
      outIndex++
      )
    {
      out[outIndex] = (byte)((
        toHexDigit(dataChars[dataIndex++]) << 4
          | toHexDigit(dataChars[dataIndex++])
          ) & 0xFF);
    }

    return out;
  }

  public static byte[] intToByteArray(
    int data
    )
  {return new byte[]{(byte)(data >> 24), (byte)(data >> 16), (byte)(data >> 8), (byte)data};}
  // </public>

  // <private>
  private static int toHexDigit(
    char dataChar
    )
  {
    int digit = Character.digit(dataChar, 16);
    if(digit == -1)
      throw new RuntimeException("Illegal hexadecimal character " + dataChar);

    return digit;
  }
  // </private>
  // </interface>
  // </static>
  // </class>
}