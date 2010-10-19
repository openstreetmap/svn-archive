/*
  Copyright 2010 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;

/**
  PDF rectangle object [PDF:1.6:3.8.4].
  <h3>Remarks</h3>
  <p>Rectangles are described by two diagonally-opposite corners.</p>
  <p>Corner pairs which don't respect the canonical form (lower-left and upper-right)
  are automatically normalized to provide a consistent representation.</p>
  <p>Coordinates are expressed within the PDF coordinate space (lower-left origin
  and positively-oriented axes).</p>

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.8
  @version 0.0.8
*/
public class Rectangle
  extends PdfObjectWrapper<PdfArray>
{
  // <class>
  // <static>
  // <interface>
  // <private>
  private static PdfArray normalize(
    PdfArray rectangle
    )
  {
    if(((IPdfNumber)rectangle.get(0)).getNumberValue() > ((IPdfNumber)rectangle.get(2)).getNumberValue())
    {
      PdfDirectObject leftCoordinate = rectangle.get(2);
      rectangle.set(2, rectangle.get(0));
      rectangle.set(0, leftCoordinate);
    }
    if(((IPdfNumber)rectangle.get(1)).getNumberValue() > ((IPdfNumber)rectangle.get(3)).getNumberValue())
    {
      PdfDirectObject bottomCoordinate = rectangle.get(3);
      rectangle.set(3, rectangle.get(1));
      rectangle.set(1, bottomCoordinate);
    }
    return rectangle;
  }
  // </private>
  // </interface>
  // </static>

  // <dynamic>
  // <constructors>
  public Rectangle(
    RectangularShape rectangle
    )
  {
    this(
      rectangle.getMinX(),
      rectangle.getMaxY(),
      rectangle.getWidth(),
      rectangle.getHeight()
      );
  }

  public Rectangle(
    Point2D lowerLeft,
    Point2D upperRight
    )
  {
    this(
      lowerLeft.getX(),
      upperRight.getY(),
      upperRight.getX()-lowerLeft.getX(),
      upperRight.getY()-lowerLeft.getY()
      );
  }

  public Rectangle(
    double left,
    double top,
    double width,
    double height
    )
  {
    this(
      new PdfArray(
        new PdfDirectObject[]
        {
          new PdfReal(left),
          new PdfReal(top-height),
          new PdfReal(left+width),
          new PdfReal(top)
        }
        )
      );
  }
  //TODO:integrate with the container update infrastructure (see other PdfObjectWrapper subclass implementations)!!
  public Rectangle(
    PdfDirectObject baseObject
    )
  {super(normalize((PdfArray)File.resolve(baseObject)), null);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public Rectangle clone(
    Document context
    )
  {throw new NotImplementedException();}

  public double getBottom(
    )
  {return ((IPdfNumber)getBaseDataObject().get(1)).getNumberValue();}

  public double getHeight(
    )
  {return getTop() - getBottom();}

  public double getLeft(
    )
  {return getX();}

  public double getRight(
    )
  {return ((IPdfNumber)getBaseDataObject().get(2)).getNumberValue();}

  public double getTop(
    )
  {return getY();}

  public double getWidth(
    )
  {return getRight() - getLeft();}

  public double getX(
    )
  {return ((IPdfNumber)getBaseDataObject().get(0)).getNumberValue();}

  public double getY(
    )
  {return ((IPdfNumber)getBaseDataObject().get(3)).getNumberValue();}

  public void setBottom(
    double value
    )
  {((IPdfNumber)getBaseDataObject().get(1)).setNumberValue(value);}

  public void setHeight(
    double value
    )
  {setBottom(getTop() - value);}

  public void setLeft(
    double value
    )
  {setX(value);}

  public void setRight(
    double value
    )
  {((IPdfNumber)getBaseDataObject().get(2)).setNumberValue(value);}

  public void setTop(
    double value
    )
  {setY(value);}

  public void setWidth(
    double value
    )
  {setRight(getLeft() + value);}

  public void setX(
    double value
    )
  {((IPdfNumber)getBaseDataObject().get(0)).setNumberValue(value);}

  public void setY(
    double value
    )
  {((IPdfNumber)getBaseDataObject().get(3)).setNumberValue(value);}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}