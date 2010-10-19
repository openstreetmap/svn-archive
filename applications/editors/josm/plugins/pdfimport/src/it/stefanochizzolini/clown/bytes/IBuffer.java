/*
  Copyright 2006-2010 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.bytes;

import it.stefanochizzolini.clown.bytes.filters.Filter;

/**
  Buffer interface.
  <p>Its pivotal concept is the array index.</p>

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
*/
public interface IBuffer
  extends IInputStream,
    IOutputStream
{
  /**
    Appends a byte to the buffer.
    @param data Byte to copy.
  */
  void append(
    byte data
    );

  /**
    Appends a byte array to the buffer.
    @param data Byte array to copy.
  */
  void append(
    byte[] data
    );

  /**
    Appends a byte range to the buffer.
    @param data Byte array from which the byte range has to be copied.
    @param offset Location in the byte array at which copying begins.
    @param length Number of bytes to copy.
  */
  void append(
    byte[] data,
    int offset,
    int length
    );

  /**
    Appends a string to the buffer.
    @param data String to copy.
  */
  void append(
    String data
    );

  /**
    Appends an IInputStream to the buffer.
    @param data Source data to copy.
  */
  void append(
    IInputStream data
    );

  /**
    Gets a clone of the buffer.
    @return Deep copy of the buffer.
  */
  IBuffer clone(
    );

  /**
    Applies the specified filter to decode the buffer.
    @param filter Filter to use for decoding the buffer.
  */
  void decode(
    Filter filter
    );

  /**
    Deletes a byte chunk from the buffer.
    @param index Location at which deletion has to begin.
    @param length Number of bytes to delete.
  */
  void delete(
    int index,
    int length
    );

  /**
    Applies the specified filter to encode the buffer.
    @param filter Filter to use for encoding the buffer.
    @return Encoded buffer.
  */
  byte[] encode(
    Filter filter
    );

  /**
    Gets the byte at a specified location.
    @param index A location in the buffer.
    @return Byte at the specified location.
  */
  int getByte(
    int index
    );

    /**
    Gets the byte range beginning at a specified location.
    @param index Location at which the byte range has to begin.
    @param length Number of bytes to copy.
    @return Byte range beginning at the specified location.
  */
  byte[] getByteArray(
    int index,
    int length
    );

  /**
    Gets the string beginning at a specified location.
    @param index Location at which the string has to begin.
    @param length Number of bytes to convert.
    @return String beginning at the specified location.
  */
  String getString(
    int index,
    int length
    );

  /**
    Gets the allocated buffer size.
    @return Allocated buffer size.
  */
  int getCapacity(
    );

  /**
    Inserts a byte array into the buffer.
    @param index Location at which the byte array has to be inserted.
    @param data Byte array to insert.
  */
  void insert(
    int index,
    byte[] data
    );

  /**
    Inserts a byte range into the buffer.
    @param index Location at which the byte range has to be inserted.
    @param data Byte array from which the byte range has to be copied.
    @param offset Location in the byte array at which copying begins.
    @param length Number of bytes to copy.
  */
  void insert(
    int index,
    byte[] data,
    int offset,
    int length
    );

  /**
    Inserts a string into the buffer.
    @param index Location at which the string has to be inserted.
    @param data String to insert.
  */
  void insert(
    int index,
    String data
    );

  /**
    Inserts an IInputStream into the buffer.
    @param index Location at which the IInputStream has to be inserted.
    @param data Source data to copy.
  */
  void insert(
    int index,
    IInputStream data
    );

  /**
    Replaces the buffer contents with a byte array.
    @param index Location at which the byte array has to be copied.
    @param data Byte array to copy.
  */
  void replace(
    int index,
    byte[] data
    );

  /**
    Replaces the buffer contents with a byte range.
    @param index Location at which the byte range has to be copied.
    @param data Byte array from which the byte range has to be copied.
    @param offset Location in the byte array at which copying begins.
    @param length Number of bytes to copy.
  */
  void replace(
    int index,
    byte[] data,
    int offset,
    int length
    );

  /**
    Replaces the buffer contents with a string.
    @param index Location at which the string has to be copied.
    @param data String to copy.
  */
  void replace(
    int index,
    String data
    );

  /**
    Replaces the buffer contents with an IInputStream.
    @param index Location at which the IInputStream has to be copied.
    @param data Source data to copy.
  */
  void replace(
    int index,
    IInputStream data
    );

  /**
    Sets the used buffer size.
    @param value New length.
  */
  void setLength(
    int value
    );

  /**
    Writes the buffer data to a stream.
    @param stream Target stream.
  */
  void writeTo(
    IOutputStream stream
    );
}