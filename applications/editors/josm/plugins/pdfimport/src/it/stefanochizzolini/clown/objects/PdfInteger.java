/*
  Copyright 2006-2009 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

import it.stefanochizzolini.clown.bytes.IOutputStream;
import it.stefanochizzolini.clown.files.File;

/**
  PDF integer number object [PDF:1.6:3.2.2].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
*/
public class PdfInteger
  extends PdfAtomicObject<Integer>
  implements IPdfNumber
{
  // <class>
  // <dynamic>
  // <constructors>
  public PdfInteger(
    int value
    )
  {setRawValue(value);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public Object clone(
    File context
    )
  {
    // Shallow copy.
    PdfInteger clone = (PdfInteger)super.clone();

    // Deep copy.
    /* NOTE: No mutable object to be cloned. */

    return clone;
  }

  @Override
  public int compareTo(
    PdfDirectObject obj
    )
  {return PdfNumber.compare(this,obj);}

  @Override
  public void writeTo(
    IOutputStream stream
    )
  {stream.write(toPdf(getRawValue()));}

  // <IPdfNumber>
  public double getNumberValue(
    )
  {return getRawValue();}

  public void setNumberValue(
    double value
    )
  {setRawValue((int)value);}

  public void translateNumberValue(
    double value
    )
  {setRawValue(getRawValue() + (int)value);}
  // </IPdfNumber>
  // </public>

  // <private>
  private String toPdf(
    int value
    )
  {return Integer.toString(value);}
  // </private>
  // </interface>
  // </dynamic>
  // </class>
}