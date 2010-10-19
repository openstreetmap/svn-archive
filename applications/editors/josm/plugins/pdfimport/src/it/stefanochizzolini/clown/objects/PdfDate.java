/*
  Copyright 2006-2008 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
  PDF date object [PDF:1.6:3.8.3].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.6
*/
public class PdfDate
  extends PdfString
{
  // <class>
  // <static>
  // <fields>
  protected static final SimpleDateFormat formatter;
  // </fields>

  // <constructors>
  static
  {formatter = new SimpleDateFormat("yyyyMMddHHmmssZ");}
  // </constructors>

  // <interface>
  // <public>
  /**
    Converts a PDF-formatted date value to a date value.
  */
  public static Date toDate(
    String value
    )
  {
    //TODO:IMPL this code is quite ugly... is there a more elegant solution (regex)?
    // Normalize datetime value.
    // Cut leading "D:" tag!
    value = value.substring(2);
    int length = value.length();
    switch(length)
    {
      case 8: // Date only.
        value += "000000+0000";
        break;
      case 14: // Datetime without timezone.
        value += "+0000";
        break;
      case 15: // Datetime at UT timezone ("Z" tag).
        value = value.substring(0,length-1) + "+0000";
        break;
      case 17: // Datetime at non-UT timezone without minutes.
        value += "00";
        break;
      case 21: // Datetime at non-UT full timezone ("'mm'" PDF timezone-minutes format).
        value = value.substring(0,length-1).replace("\'","");
        break;
    }

    try
    {return formatter.parse(value);}
    catch(Exception e)
    {throw new RuntimeException(e);}
  }
  // </public>
  // </interface>
  // </static>

  // <dynamic>
  // <constructors>
  public PdfDate(
    )
  {}

  public PdfDate(
    Date value
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
    PdfDate clone = (PdfDate)super.clone();

    // Deep copy.
    /* NOTE: No mutable object to be cloned. */

    return clone;
  }

  @Override
  public Object getValue(
    )
  {
    try
    {return toDate(new String(getRawValue(),"ISO-8859-1"));}
    catch(Exception e)
    {throw new RuntimeException(e); /* (should NEVER happen). */}
  }

  @Override
  public void setValue(
    Object value
    )
  {
    byte[] valueBytes;
    try
    {valueBytes = formatter.format(value).getBytes("ISO-8859-1");}
    catch(Exception e)
    {throw new RuntimeException(e);}

    byte[] buffer = new byte[23];
    buffer[0] = 68; buffer[1] = 58;
    System.arraycopy(valueBytes,0,buffer,2,17);
    buffer[19] = 39;
    System.arraycopy(valueBytes,17,buffer,20,2);
    buffer[22] = 39;

    setRawValue(buffer);
  }
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}