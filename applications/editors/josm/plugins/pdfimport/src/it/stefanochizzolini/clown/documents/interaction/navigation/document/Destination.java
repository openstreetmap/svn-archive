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

package it.stefanochizzolini.clown.documents.interaction.navigation.document;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfInteger;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfNamedObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfReal;
import it.stefanochizzolini.clown.objects.PdfReference;
import it.stefanochizzolini.clown.objects.PdfString;
import it.stefanochizzolini.clown.util.NotImplementedException;

/**
  Interaction target [PDF:1.6:8.2.1].

  <h3>Remarks</h3>
  <p>It represents a particular view of a document, consisting of the following items:</p>
  <ul>
    <li>the page of the document to be displayed;</li>
    <li>the location of the document window on that page;</li>
    <li>the magnification (zoom) factor to use when displaying the page.</li>
  </ul>

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
*/
public abstract class Destination
  extends PdfNamedObjectWrapper<PdfArray>
{
  // <class>
  // <classes>
  /**
    Destination mode [PDF:1.6:8.2.1].
  */
  public enum ModeEnum
  {
    /**
      Display the page at the given upper-left position,
      applying the given magnification.

      <p>View parameters:</p>
      <ol>
        <li>left coordinate</li>
        <li>top coordinate</li>
        <li>zoom</li>
      </ol>
    */
    XYZ,
    /**
      Display the page with its contents magnified just enough to fit
      the entire page within the window both horizontally and vertically.

      <p>No view parameters.</p>
    */
    Fit,
    /**
      Display the page with the vertical coordinate <code>top</code> positioned
      at the top edge of the window and the contents of the page magnified
      just enough to fit the entire width of the page within the window.

      <p>View parameters:</p>
      <ol>
        <li>top coordinate</li>
      </ol>
    */
    FitHorizontal,
    /**
      Display the page with the horizontal coordinate <code>left</code> positioned
      at the left edge of the window and the contents of the page magnified
      just enough to fit the entire height of the page within the window.

      <p>View parameters:</p>
      <ol>
        <li>left coordinate</li>
      </ol>
    */
    FitVertical,
    /**
      Display the page with its contents magnified just enough to fit
      the rectangle specified by the given coordinates entirely
      within the window both horizontally and vertically.

      <p>View parameters:</p>
      <ol>
        <li>left coordinate</li>
        <li>bottom coordinate</li>
        <li>right coordinate</li>
        <li>top coordinate</li>
      </ol>
    */
    FitRectangle,
    /**
      Display the page with its contents magnified just enough to fit
      its bounding box entirely within the window both horizontally and vertically.

      <p>No view parameters.</p>
    */
    FitBoundingBox,
    /**
      Display the page with the vertical coordinate <code>top</code> positioned
      at the top edge of the window and the contents of the page magnified
      just enough to fit the entire width of its bounding box within the window.

      <p>View parameters:</p>
      <ol>
        <li>top coordinate</li>
      </ol>
    */
    FitBoundingBoxHorizontal,
    /**
      Display the page with the horizontal coordinate <code>left</code> positioned
      at the left edge of the window and the contents of the page magnified
      just enough to fit the entire height of its bounding box within the window.

      <p>View parameters:</p>
      <ol>
        <li>left coordinate</li>
      </ol>
    */
    FitBoundingBoxVertical
  }
  // </classes>

  // <static>
  // <interface>
  // <public>
  /**
    Wraps a destination base object into a destination object.

    @param baseObject Destination base object.
    @param container Destination base object container.
    @param name Destination name.
    @return Destination object associated to the base object.
  */
  public static final Destination wrap(
    PdfDirectObject baseObject,
    PdfIndirectObject container,
    PdfString name
    )
  {
    /*
      NOTE: This is a factory method for any destination-derived object.
    */
    if(baseObject == null)
      return null;

    PdfArray dataObject = (PdfArray)File.resolve(baseObject);
    PdfDirectObject pageObject = dataObject.get(0);
    if(pageObject instanceof PdfReference)
      return new LocalDestination(baseObject,container,name);
    else if(pageObject instanceof PdfInteger)
      return new RemoteDestination(baseObject,container,name);
    else
      throw new IllegalArgumentException("'baseObject' parameter doesn't represent a valid destination object.");
  }
  // </public>
  // </interface>
  // </static>

  // <dynamic>
  // <constructors>
  /**
    Creates a new destination within the given document context.

    @param context Document context.
    @param pageObject Page reference. It may be either an actual page reference (PdfReference)
      or a page index (PdfInteger).
    @param mode Destination mode.
    @param viewParams View parameters. Their actual composition depends on the <code>mode</code> value
      (see ModeEnum for more info).
  */
  protected Destination(
    Document context,
    PdfDirectObject pageObject,
    ModeEnum mode,
    Double[] viewParams
    )
  {
    super(
      context.getFile(),
      new PdfArray()
      );

    PdfArray destinationObject = getBaseDataObject();

    destinationObject.add(pageObject);

    switch(mode)
    {
      case Fit:
        destinationObject.add(PdfName.Fit);
        break;
      case FitBoundingBox:
        destinationObject.add(PdfName.FitB);
        break;
      case FitBoundingBoxHorizontal:
        destinationObject.add(PdfName.FitBH);
        destinationObject.add(PdfReal.get(viewParams[0]));
        break;
      case FitBoundingBoxVertical:
        destinationObject.add(PdfName.FitBV);
        destinationObject.add(PdfReal.get(viewParams[0]));
        break;
      case FitHorizontal:
        destinationObject.add(PdfName.FitH);
        destinationObject.add(PdfReal.get(viewParams[0]));
        break;
      case FitRectangle:
        destinationObject.add(PdfName.FitR);
        destinationObject.add(PdfReal.get(viewParams[0]));
        destinationObject.add(PdfReal.get(viewParams[1]));
        destinationObject.add(PdfReal.get(viewParams[2]));
        destinationObject.add(PdfReal.get(viewParams[3]));
        break;
      case FitVertical:
        destinationObject.add(PdfName.FitV);
        destinationObject.add(PdfReal.get(viewParams[0]));
        break;
      case XYZ:
        destinationObject.add(PdfName.XYZ);
        destinationObject.add(PdfReal.get(viewParams[0]));
        destinationObject.add(PdfReal.get(viewParams[1]));
        destinationObject.add(PdfReal.get(viewParams[2]));
        break;
    }
  }

  protected Destination(
    PdfDirectObject baseObject,
    PdfIndirectObject container,
    PdfString name
    )
  {
    super(
      baseObject,
      container,
      name
      );
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public Destination clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the destination mode.
  */
  public ModeEnum getMode(
    )
  {
    PdfName modeObject = (PdfName)getBaseDataObject().get(1);
    if(modeObject.equals(PdfName.FitB))
      return ModeEnum.FitBoundingBox;
    else if(modeObject.equals(PdfName.FitBH))
      return ModeEnum.FitBoundingBoxHorizontal;
    else if(modeObject.equals(PdfName.FitBV))
      return ModeEnum.FitBoundingBoxVertical;
    else if(modeObject.equals(PdfName.FitH))
      return ModeEnum.FitHorizontal;
    else if(modeObject.equals(PdfName.FitR))
      return ModeEnum.FitRectangle;
    else if(modeObject.equals(PdfName.FitV))
      return ModeEnum.FitVertical;
    else if(modeObject.equals(PdfName.XYZ))
      return ModeEnum.XYZ;
    else
      return ModeEnum.Fit;
  }

  /**
    Gets the target page reference.
  */
  public abstract Object getPageRef(
    );
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}