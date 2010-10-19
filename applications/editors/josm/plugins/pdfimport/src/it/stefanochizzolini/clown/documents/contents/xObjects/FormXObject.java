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

package it.stefanochizzolini.clown.documents.contents.xObjects;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.documents.contents.Contents;
import it.stefanochizzolini.clown.documents.contents.IContentContext;
import it.stefanochizzolini.clown.documents.contents.Resources;
import it.stefanochizzolini.clown.documents.contents.composition.PrimitiveFilter;
import it.stefanochizzolini.clown.documents.contents.objects.ContentObject;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.objects.IPdfNumber;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfReference;
import it.stefanochizzolini.clown.objects.Rectangle;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

/**
  Form external object [PDF:1.6:4.9].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
*/
public class FormXObject
  extends XObject
  implements IContentContext
{
  // <class>
  // <dynamic>
  // <constructors>
  /**
    Creates a new form within the given document context, using default resources.
  */
  public FormXObject(
    Document context
    )
  {this(context,null);}

  /**
    Creates a new form within the given document context, using custom resources.

    @since 0.0.5
  */
  public FormXObject(
    Document context,
    Resources resources
    )
  {
    super(context);

    PdfDictionary header = getBaseDataObject().getHeader();
    header.put(PdfName.Subtype,PdfName.Form);
    header.put(PdfName.BBox,new Rectangle(0,0,0,0).getBaseDataObject());

    // No resources collection?
    /* NOTE: Resources collection is mandatory. */
    if(resources == null)
    {resources = new Resources(context);}
    header.put(PdfName.Resources,resources.getBaseObject());
  }

  /**
    For internal use only.
  */
  public FormXObject(
    PdfDirectObject baseObject
    )
  {super(baseObject);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public FormXObject clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    @since 0.0.5
  */
  @Override
  public double[] getMatrix(
    )
  {
    /*
      NOTE: Form-space-to-user-space matrix is identity [1 0 0 1 0 0] by default,
      but may be adjusted by setting the Matrix entry in the form dictionary [PDF:1.6:4.9].
    */
    PdfArray matrix = (PdfArray)File.resolve(
      getBaseDataObject().getHeader().get(PdfName.Matrix)
      );
    if(matrix == null)
      return new double[]
        {
          1, // a.
          0, // b.
          0, // c.
          1, // d.
          0, // e.
          0 // f.
        };

    return new double[]
      {
        ((IPdfNumber)matrix.get(0)).getNumberValue(), // a.
        ((IPdfNumber)matrix.get(1)).getNumberValue(), // b.
        ((IPdfNumber)matrix.get(2)).getNumberValue(), // c.
        ((IPdfNumber)matrix.get(3)).getNumberValue(), // d.
        ((IPdfNumber)matrix.get(4)).getNumberValue(), // e.
        ((IPdfNumber)matrix.get(5)).getNumberValue() // f.
      };
  }

  /**
    Gets the form size.
  */
  @Override
  public Dimension2D getSize(
    )
  {
    PdfArray box = (PdfArray)File.resolve(
      getBaseDataObject().getHeader().get(PdfName.BBox)
      );

    return new Dimension(
      (int)((IPdfNumber)box.get(2)).getNumberValue(),
      (int)((IPdfNumber)box.get(3)).getNumberValue()
      );
  }

  /**
    Sets the resources associated to the form.
  */
  public void setResources(
    Resources value
    )
  {
    getBaseDataObject().getHeader().put(
      PdfName.Resources,
      value.getBaseObject()
      );
  }

  /**
    Sets the form size.
  */
  @Override
  public void setSize(
    Dimension2D value
    )
  {
    PdfDirectObject box = getBaseDataObject().getHeader().get(PdfName.BBox);

    PdfArray boxObject = (PdfArray)File.resolve(box);
    ((IPdfNumber)boxObject.get(2)).setNumberValue(value.getWidth());
    ((IPdfNumber)boxObject.get(3)).setNumberValue(value.getHeight());

    File.update(box);
  }

  // <IContentContext>
  public Rectangle2D getBox(
    )
  {
    PdfArray box = (PdfArray)File.resolve(
      getBaseDataObject().getHeader().get(PdfName.BBox) // NOTE: Required [PDF:1.6:4.9.1].
      );

    return new Rectangle2D.Double(
      ((IPdfNumber)box.get(0)).getNumberValue(),
      ((IPdfNumber)box.get(1)).getNumberValue(),
      ((IPdfNumber)box.get(2)).getNumberValue(),
      ((IPdfNumber)box.get(3)).getNumberValue()
      );
  }

  /**
    Gets the content stream associated to the form.
  */
  public Contents getContents(
    )
  {
    return new Contents(
      getBaseObject(),
      ((PdfReference)getBaseObject()).getIndirectObject(),
      this
      );
  }

  /**
    Gets the resources associated to the form.
  */
  public Resources getResources(
    )
  {
    return new Resources(
      getBaseDataObject().getHeader().get(PdfName.Resources),
      ((PdfReference)getBaseObject()).getIndirectObject()
      );
  }

  // <IContentEntity>
  /**
    @since 0.0.6
  */
  public ContentObject toInlineObject(
    PrimitiveFilter context
    )
  {throw new NotImplementedException();}

  /**
    @since 0.0.5
  */
  public XObject toXObject(
    Document context
    )
  {throw new NotImplementedException();}
  // </IContentEntity>
  // </IContentContext>
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}