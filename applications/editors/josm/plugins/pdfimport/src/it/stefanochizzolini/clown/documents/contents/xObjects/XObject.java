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

package it.stefanochizzolini.clown.documents.contents.xObjects;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfReference;
import it.stefanochizzolini.clown.objects.PdfStream;

import java.awt.geom.Dimension2D;

/**
  Abstract external object [PDF:1.6:4.7].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.5
*/
public abstract class XObject
  extends PdfObjectWrapper<PdfStream>
{
  // <class>
  // <static>
  // <interface>
  // <public>
  /**
    Wraps an external object reference into an external object.
    @param reference Reference to an external object.
    @return External object associated to the reference.
  */
  public static XObject wrap(
    PdfReference reference
    )
  {
    /*
      NOTE: This is a factory method for any xobject-derived object.
    */
    if(reference == null)
      return null;

    PdfName subtype = (PdfName)((PdfStream)reference.getDataObject()).getHeader().get(PdfName.Subtype);
    if(subtype.equals(PdfName.Form))
      return new FormXObject(reference);
    else if(subtype.equals(PdfName.Image))
      return new ImageXObject(reference);
    else
      return null;
  }
  // </public>
  // </interface>
  // </static>

  // <dynamic>
  // <constructors>
  /**
    Creates a new external object inside the document.
  */
  protected XObject(
    Document context
    )
  {
    this(
      context,
      new PdfStream()
      );
  }

  /**
    Creates a new external object inside the document.
  */
  protected XObject(
    Document context,
    PdfStream baseDataObject
    )
  {
    super(
      context.getFile(),
      baseDataObject
      );

    baseDataObject.getHeader().put(PdfName.Type,PdfName.XObject);
  }

  /**
    Instantiates an existing external object.
  */
  protected XObject(
    PdfDirectObject baseObject
    )
  {
    super(
      baseObject,
      null // NO container (baseObject is (by definition) a PDF stream, so it MUST be an indirect object [PDF:1.6:3.2.7]).
      );
  }
  // </constructors>

  // <interface>
  // <public>
  /**
    Gets the mapping from external-object space to user space.

    @since 0.0.5
  */
  public abstract double[] getMatrix(
    );

  /**
    Gets the external object size.

    @since 0.0.5
  */
  public abstract Dimension2D getSize(
    );

  /**
    Sets the external object size.

    @since 0.0.5
  */
  public abstract void setSize(
    Dimension2D value
    );
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}