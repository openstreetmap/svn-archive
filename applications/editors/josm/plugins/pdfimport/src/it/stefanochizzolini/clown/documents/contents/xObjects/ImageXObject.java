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

package it.stefanochizzolini.clown.documents.contents.xObjects;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfInteger;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfStream;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;

/**
  Image external object [PDF:1.6:4.8.4].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.6
*/
public class ImageXObject
  extends XObject
{
  // <class>
  // <dynamic>
  // <constructors>
  public ImageXObject(
    Document context,
    PdfStream baseDataObject
    )
  {
    /*
      NOTE: It's caller responsability to adequately populate the stream
      header and body in order to instantiate a valid object; header entries like
      'Width', 'Height', 'ColorSpace', 'BitsPerComponent' MUST be defined
      appropriately.
    */

    super(
      context,
      baseDataObject
      );

    baseDataObject.getHeader().put(PdfName.Subtype,PdfName.Image);
  }

  /**
    For internal use only.
  */
  public ImageXObject(
    PdfDirectObject baseObject
    )
  {super(baseObject);}
  // </constructors>

  // <interface>
  // <public>
  /**
    Gets the number of bits per color component.
  */
  public int getBitsPerComponent(
    )
  {return ((PdfInteger)getBaseDataObject().getHeader().get(PdfName.BitsPerComponent)).getRawValue();}

  @Override
  public ImageXObject clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the color space in which samples are specified.
  */
  public String getColorSpace(
    )
  {return ((PdfName)getBaseDataObject().getHeader().get(PdfName.ColorSpace)).getRawValue();}

  /**
    @since 0.0.5
  */
  @Override
  public double[] getMatrix(
    )
  {
    Dimension2D size = getSize();

    /*
      NOTE: Image-space-to-user-space matrix is [1/w 0 0 1/h 0 0],
      where w and h are the width and height of the image in samples [PDF:1.6:4.8.3].
    */
    return new double[]
      {
        1 / size.getWidth(), // a.
        0, // b.
        0, // c.
        1 / size.getHeight(), // d.
        0, // e.
        0 // f.
      };
  }

  /**
    Gets the size of the image (in samples).

    @since 0.0.5
  */
  @Override
  public Dimension2D getSize(
    )
  {
    PdfDictionary header = getBaseDataObject().getHeader();

    return new Dimension(
      ((PdfInteger)header.get(PdfName.Width)).getRawValue(),
      ((PdfInteger)header.get(PdfName.Height)).getRawValue()
      );
  }

  /**
    @since 0.0.5
  */
  @Override
  public void setSize(
    Dimension2D value
    )
  {throw new UnsupportedOperationException();}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}