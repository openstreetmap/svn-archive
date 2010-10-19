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

package it.stefanochizzolini.clown.documents.contents;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.util.NotImplementedException;

/**
  Resources collection [PDF:1.6:3.7.2].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
*/
public class Resources
  extends PdfObjectWrapper<PdfDictionary>
{
  // <class>
  // <dynamic>
  // <constructors>
  public Resources(
    Document context
    )
  {
    super(
      context.getFile(),
      new PdfDictionary()
      );
  }

  /**
    For internal use only.
  */
  public Resources(
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
  public Resources clone(
    Document context
    )
  {throw new NotImplementedException();}

  public ColorSpaceResources getColorSpaces(
    )
  {
    /*
      NOTE: ColorSpace entry may be undefined [PDF:1.6:3.7.2].
    */
    PdfDirectObject colorSpaces = getBaseDataObject().get(PdfName.ColorSpace);
    if(colorSpaces == null)
      return null;

    return new ColorSpaceResources(
      colorSpaces,
      getContainer()
      );
  }

  public ExtGStateResources getExtGStates(
    )
  {
    /*
      NOTE: ExtGState entry may be undefined [PDF:1.6:3.7.2].
    */
    PdfDirectObject extGStates = getBaseDataObject().get(PdfName.ExtGState);
    if(extGStates == null)
      return null;

    return new ExtGStateResources(
      extGStates,
      getContainer()
      );
  }

  public FontResources getFonts(
    )
  {
    /*
      NOTE: Font entry may be undefined [PDF:1.6:3.7.2].
    */
    PdfDirectObject fonts = getBaseDataObject().get(PdfName.Font);
    if(fonts == null)
      return null;

    return new FontResources(
      fonts,
      getContainer()
      );
  }

  public XObjectResources getXObjects(
    )
  {
    /*
      NOTE: XObject entry may be undefined [PDF:1.6:3.7.2].
    */
    PdfDirectObject xObjects = getBaseDataObject().get(PdfName.XObject);
    if(xObjects == null)
      return null;

    return new XObjectResources(
      xObjects,
      getContainer()
      );
  }

  public void setColorSpaces(
    ColorSpaceResources value
    )
  {getBaseDataObject().put(PdfName.ColorSpace,value.getBaseObject());}

  public void setExtGStates(
    ExtGStateResources value
    )
  {getBaseDataObject().put(PdfName.ExtGState,value.getBaseObject());}

  public void setFonts(
    FontResources value
    )
  {getBaseDataObject().put(PdfName.Font,value.getBaseObject());}

  public void setXObjects(
    XObjectResources value
    )
  {getBaseDataObject().put(PdfName.XObject,value.getBaseObject());}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}