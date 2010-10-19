/*
  Copyright 2008-2009 Stefano Chizzolini. http://clown.stefanochizzolini.it

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
import it.stefanochizzolini.clown.objects.IPdfNumber;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfReal;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
  Abstract vertexed shape annotation.

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.7
*/
public abstract class VertexShape
  extends Shape
{
  // <class>
  // <dynamic>
  // <constructors>
  protected VertexShape(
    Page page,
    Rectangle2D box,
    PdfName subtype
    )
  {
    super(
      page,
      box,
      subtype
      );
  }

  protected VertexShape(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {super(baseObject,container);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public VertexShape clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the coordinates of each vertex.
  */
  public List<Point2D> getVertices(
    )
  {
    /*
      NOTE: 'Vertices' entry MUST be present.
    */
    PdfArray verticesObject = (PdfArray)getBaseDataObject().get(PdfName.Vertices);
    List<Point2D> vertices = new ArrayList<Point2D>();
    double pageHeight = getPage().getBox().getHeight();
    for(
      int index = 0,
        length = verticesObject.size();
      index < length;
      index += 2
      )
    {
      double x = ((IPdfNumber)verticesObject.get(index)).getNumberValue();
      double y = pageHeight - ((IPdfNumber)verticesObject.get(index+1)).getNumberValue();
      vertices.add(
        new Point2D.Double(x,y)
        );
    }

    return vertices;
  }

  /**
    @see #getVertices()
  */
  public void setVertices(
    List<Point2D> value
    )
  {
    PdfArray verticesObject = new PdfArray();
    double pageHeight = getPage().getBox().getHeight();
    for(Point2D vertex : value)
    {
      verticesObject.add(new PdfReal(vertex.getX())); // x.
      verticesObject.add(new PdfReal(pageHeight-vertex.getY())); // y.
    }

    getBaseDataObject().put(PdfName.Vertices,verticesObject);
  }
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}