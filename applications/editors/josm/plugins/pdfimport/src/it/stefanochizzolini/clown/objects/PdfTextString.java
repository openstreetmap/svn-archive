/*
  Copyright 2008 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.objects;

import it.stefanochizzolini.clown.files.File;

/**
  PDF text string object [PDF:1.6:3.8.1].
  <h3>Remarks</h3>
  <p>Text strings are meaningful only as part of the document hierarchy;
  they cannot appear within content streams.
  They represent information that is intended to be human-readable.</p>

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.7
  @since 0.0.6
*/
public class PdfTextString
  extends PdfString
{
  /*
    NOTE: Text strings are string objects encoded in either PDFDocEncoding
    or Unicode character encoding.
    PDFDocEncoding is a superset of the ISO Latin 1 encoding [PDF:1.6:D].
    Unicode is described in the Unicode Standard by the Unicode Consortium [UCS:4].
  */
  // <static>
  // <fields>
  private static final String ISO88591Encoding = "ISO-8859-1";
  private static final String UTF16BEEncoding = "UTF-16BE";
  // </fields>
  // </static>

  // <dynamic>
  // <fields>
  private String encoding;
  // </fields>

  // <constructors>
  public PdfTextString(
    )
  {}

  public PdfTextString(
    byte[] rawValue
    )
  {setRawValue(rawValue);}

  public PdfTextString(
    String value
    )
  {setValue(value);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public Object clone(
    File context
    )
  {
    // Shallow copy.
    PdfTextString clone = (PdfTextString)super.clone();

    // Deep copy.
    /* NOTE: No mutable object to be cloned. */

    return clone;
  }

  public String getEncoding(
    )
  {return encoding;}

  @Override
  public Object getValue(
    )
  {
    try
    {
      byte[] valueBytes = getRawValue();
      byte[] buffer;
      if(encoding == UTF16BEEncoding)
      {
        // Excluding UTF marker...
        buffer = new byte[valueBytes.length - 2];
        System.arraycopy(valueBytes,2,buffer,0,buffer.length);
      }
      else
      {buffer = valueBytes;}

      return new String(buffer,encoding);
    }
    catch(Exception e)
    {throw new RuntimeException(e); /* (should NEVER happen). */}
  }

  @Override
  public void setValue(
    Object value
    )
  {
    try
    {
      // Prepending UTF marker...
      byte[] valueBytes = ((String)value).getBytes(UTF16BEEncoding);
      byte[] buffer = new byte[valueBytes.length + 2];
      buffer[0] = (byte)254; buffer[1] = (byte)255;
      System.arraycopy(valueBytes,0,buffer,2,valueBytes.length);

      setRawValue(buffer);
    }
    catch(Exception e)
    {throw new RuntimeException(e);}
  }

  @Override
  public void setRawValue(
    byte[] value
    )
  {
    if(value.length > 2
      && value[0] == (byte)254
      && value[1] == (byte)255) // Multi-byte (Unicode).
    {encoding = UTF16BEEncoding;}
    else // Single byte (PDFDocEncoding).
    {encoding = ISO88591Encoding;}

    super.setRawValue(value);
  }

  @Override
  public String toString(
    )
  {return (String)getValue();}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}