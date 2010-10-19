/*
  Copyright 2006 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.bytes.filters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
  zlib/deflate [RFC:1950,1951] filter [PDF:1.6:3.3.3].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.2
  @since 0.0.2
*/
public class FlateFilter
  extends Filter
{
  // <class>
  // <dynamic>
  // <constructors>
  FlateFilter(
    )
  {}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public byte[] decode(
    byte[] data,
    int offset,
    int length
    )
  {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try
    {
      InflaterInputStream inputFilter = new InflaterInputStream(
        new ByteArrayInputStream(data,offset,length)
        );

      transform(inputFilter,outputStream);
    }
    catch(IOException e)
    {throw new RuntimeException("Decoding failed.",e);}

    return outputStream.toByteArray();
  }

  @Override
  public byte[] encode(
    byte[] data,
    int offset,
    int length
    )
  {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try
    {
      DeflaterOutputStream outputFilter = new DeflaterOutputStream(outputStream);
      ByteArrayInputStream inputStream = new ByteArrayInputStream(data,offset,length);

      transform(inputStream,outputFilter);
    }
    catch(IOException e)
    {throw new RuntimeException("Encoding failed.",e);}

    return outputStream.toByteArray();
  }
  // </public>

  // <private>
  private void transform(
    InputStream input,
    OutputStream output
    )
    throws IOException
  {
    byte[] buffer = new byte[8192]; int bufferLength;
    while((bufferLength = input.read(buffer, 0, buffer.length)) != -1)
    {output.write(buffer, 0, bufferLength);}

    input.close(); output.close();
  }
  // </private>
  // </interface>
  // </dynamic>
  // </class>
}