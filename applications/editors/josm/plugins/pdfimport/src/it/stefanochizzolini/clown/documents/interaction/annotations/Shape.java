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

package it.stefanochizzolini.clown.documents.interaction.annotations;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.documents.Page;
import it.stefanochizzolini.clown.documents.contents.colorSpaces.DeviceRGBColor;
import it.stefanochizzolini.clown.objects.IPdfNumber;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfReal;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.awt.geom.Rectangle2D;

/**
  Abstract shape annotation.

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.7
  @version 0.0.7
*/
public abstract class Shape
  extends Annotation
{
  // <class>
  // <dynamic>
  // <constructors>
  protected Shape(
    Page page,
    Rectangle2D box,
    PdfName subtype
    )
  {
    super(
      page.getDocument(),
      subtype,
      box,
      page
      );
  }

  protected Shape(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {super(baseObject,container);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public Shape clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the color with which to fill the interior of the annotation's shape.
  */
  public DeviceRGBColor getFillColor(
    )
  {
    /*
      NOTE: 'IC' entry may be undefined.
    */
    PdfArray fillColorObject = (PdfArray)getBaseDataObject().get(PdfName.IC);
    if(fillColorObject == null)
      return null;

    return new DeviceRGBColor(
      ((IPdfNumber)fillColorObject.get(0)).getNumberValue(),
      ((IPdfNumber)fillColorObject.get(1)).getNumberValue(),
      ((IPdfNumber)fillColorObject.get(2)).getNumberValue()
      );
  }

  /**
    @see #getFillColor()
  */
  public void setFillColor(
     DeviceRGBColor value
    )
  {
    getBaseDataObject().put(
      PdfName.IC,
      new PdfArray(
        new PdfDirectObject[]
        {
          new PdfReal(value.getRedComponent()),
          new PdfReal(value.getGreenComponent()),
          new PdfReal(value.getBlueComponent())
        }
        )
      );
  }
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}