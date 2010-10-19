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
  Freehand "scribble" composed of one or more disjoint paths [PDF:1.6:8.4.5].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.7
*/
public class Scribble
  extends Annotation
{
  // <class>
  // <dynamic>
  // <constructors>
  public Scribble(
    Page page,
    Rectangle2D box,
    List<List<Point2D>> paths
    )
  {
    super(
      page.getDocument(),
      PdfName.Ink,
      box,
      page
      );

    setPaths(paths);
  }

  public Scribble(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {super(baseObject,container);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public Scribble clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the coordinates of each path.
  */
  public List<List<Point2D>> getPaths(
    )
  {
    /*
      NOTE: 'InkList' entry MUST be present.
    */
    PdfArray pathsObject = (PdfArray)getBaseDataObject().get(PdfName.InkList);
    List<List<Point2D>> paths = new ArrayList<List<Point2D>>();
    double pageHeight = getPage().getBox().getHeight();
    for(
      int pathIndex = 0,
        pathLength = pathsObject.size();
      pathIndex < pathLength;
      pathIndex++
      )
    {
      PdfArray pathObject = (PdfArray)pathsObject.get(pathIndex);
      List<Point2D> path = new ArrayList<Point2D>();
      for(
        int pointIndex = 0,
          pointLength = pathObject.size();
        pointIndex < pointLength;
        pointIndex += 2
        )
      {
        path.add(
          new Point2D.Double(
            ((IPdfNumber)pathObject.get(pointIndex)).getNumberValue(),
            pageHeight - ((IPdfNumber)pathObject.get(pointIndex+1)).getNumberValue()
            )
          );
      }
      paths.add(path);
    }

    return paths;
  }

  /**
    @see #getPaths()
  */
  public void setPaths(
    List<List<Point2D>> value
    )
  {
    PdfArray pathsObject = new PdfArray();
    double pageHeight = getPage().getBox().getHeight();
    for(List<Point2D> path : value)
    {
      PdfArray pathObject = new PdfArray();
      for(Point2D point : path)
      {
        pathObject.add(new PdfReal(point.getX())); // x.
        pathObject.add(new PdfReal(pageHeight-point.getY())); // y.
      }
      pathsObject.add(pathObject);
    }

    getBaseDataObject().put(PdfName.InkList,pathsObject);
  }
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}