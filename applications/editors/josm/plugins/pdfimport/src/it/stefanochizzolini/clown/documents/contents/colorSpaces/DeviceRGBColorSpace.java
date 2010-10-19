/*
  Copyright 2006-2007 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.documents.contents.colorSpaces;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.objects.IPdfNumber;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.util.NotImplementedException;

/**
  Device Red-Green-Blue color space [PDF:1.6:4.5.3].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.4
*/
public class DeviceRGBColorSpace
  extends DeviceColorSpace
{
  // <class>
  // <static>
  // <fields>
  /*
    NOTE: It may be specified directly (i.e. without being defined in the ColorSpace subdictionary
    of the contextual resource dictionary) [PDF:1.6:4.5.7].
  */
  public static final DeviceRGBColorSpace Default = new DeviceRGBColorSpace(null,null);
  // </fields>
  // </static>

  // <dynamic>
  // <constructors>
  public DeviceRGBColorSpace(
    Document context
    )
  {
    super(
      context,
      PdfName.DeviceRGB
      );
  }

  DeviceRGBColorSpace(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {
    super(
      baseObject,
      container
      );
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public DeviceRGBColorSpace clone(
    Document context
    )
  {throw new NotImplementedException();}

  @Override
  public Color getColor(
    PdfDirectObject[] components
    )
  {
    return new DeviceRGBColor(
      ((IPdfNumber)components[0]).getNumberValue(),
      ((IPdfNumber)components[1]).getNumberValue(),
      ((IPdfNumber)components[2]).getNumberValue()
      );
  }

  @Override
  public Color getDefaultColor(
    )
  {return DeviceRGBColor.Default;}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}