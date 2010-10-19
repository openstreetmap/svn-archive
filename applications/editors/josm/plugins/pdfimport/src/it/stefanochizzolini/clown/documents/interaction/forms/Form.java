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

package it.stefanochizzolini.clown.documents.interaction.forms;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.documents.contents.Resources;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.util.NotImplementedException;

/**
  Interactive form (AcroForm) [PDF:1.6:8.6.1].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.7
  @since 0.0.7
*/
public class Form
  extends PdfObjectWrapper<PdfDictionary>
{
  // <class>
  // <dynamic>
  // <constructors>
  public Form(
    Document context
    )
  {
    super(
      context.getFile(),
      new PdfDictionary(
        new PdfName[]
        {PdfName.Fields},
        new PdfDirectObject[]
        {new PdfArray()}
        )
      );
  }

  public Form(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {super(baseObject,container);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public Form clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the fields collection.
  */
  public Fields getFields(
    )
  {return new Fields(getBaseDataObject().get(PdfName.Fields), getContainer());}

  /**
    Gets the default resources used by fields.
  */
  public Resources getResources(
    )
  {
    /*
      NOTE: Resources entry may be undefined.
    */
    PdfDirectObject resourcesObject = getBaseDataObject().get(PdfName.DR);
    if(resourcesObject == null)
      return null;

    return new Resources(resourcesObject, getContainer());
  }

  /**
    Sets the fields collection.

    @see #getFields()
  */
  public void setFields(
    Fields value
    )
  {getBaseDataObject().put(PdfName.Fields, value.getBaseObject());}

  /**
    Sets the default resources used by fields.

    @see #getResources()
  */
  public void setResources(
    Resources value
    )
  {getBaseDataObject().put(PdfName.DR, value.getBaseObject());}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}