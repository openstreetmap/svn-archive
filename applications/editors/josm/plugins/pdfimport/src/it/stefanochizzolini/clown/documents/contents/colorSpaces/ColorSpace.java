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
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfDataObject;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfReference;

/**
  Color space [PDF:1.6:4.5].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.4
*/
public abstract class ColorSpace
  extends PdfObjectWrapper<PdfDirectObject>
{
  // <class>
  // <static>
  // <interface>
  // <public>
  /**
    Wraps a color space reference into a color space object.
    @param reference Reference to a color space object.
    @return Color space object associated to the reference.
  */
  public static ColorSpace wrap(
    PdfReference reference
    )
  {return wrap(reference,null);}

  /**
    Wraps a color space base object into a color space object.
    @param baseObject Base object of a color space object.
    @param container Indirect object possibly containing the color space
    base object.
    @return Color space object associated to the base object.
  */
  public static ColorSpace wrap(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {
    /*
      NOTE: This is a factory method for any ColorSpace-derived object.
    */
    if(baseObject == null)
      return null;

    PdfName name;
    // Get the data object corresponding to the color space!
    PdfDataObject baseDataObject = File.resolve(baseObject);
    // Is it an array?
    /*
      NOTE: [PDF:1.6:4.5.2] A color space is defined by an array object whose
      first element is a name object identifying the color space family. For
      families that do not require parameters, the color space CAN be specified
      simply by the family name itself instead of an array.
    */
    if(baseDataObject instanceof PdfArray) // PdfArray.
    {name = (PdfName)((PdfArray)baseDataObject).get(0);}
    else // PdfName (by definition).
    {name = (PdfName)baseDataObject;}

    if(name.equals(PdfName.DeviceRGB))
      return new DeviceRGBColorSpace(baseObject,container);
    else if(name.equals(PdfName.DeviceCMYK))
      return new DeviceCMYKColorSpace(baseObject,container);
    else if(name.equals(PdfName.DeviceGray))
      return new DeviceGrayColorSpace(baseObject,container);
    else if(name.equals(PdfName.CalRGB))
      return new CalRGBColorSpace(baseObject,container);
    else if(name.equals(PdfName.CalGray))
      return new CalGrayColorSpace(baseObject,container);
    else if(name.equals(PdfName.Lab))
      return new LabColorSpace(baseObject,container);
    else if(name.equals(PdfName.ICCBased))
      return new ICCBasedColorSpace(baseObject,container);
    else
      return null; // Should never happen.
  }
  // </public>
  // </interface>
  // </static>

  // <dynamic>
  // <constructors>
  protected ColorSpace(
    Document context,
    PdfDirectObject baseDataObject
    )
  {
    super(
      context.getFile(),
      baseDataObject
      );
  }

  protected ColorSpace(
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
  // <internal>
  public abstract Color getColor(
    PdfDirectObject[] components
    );

  public abstract Color getDefaultColor(
    );
  // </internal>
  // </interface>
  // </dynamic>
  // </class>
}