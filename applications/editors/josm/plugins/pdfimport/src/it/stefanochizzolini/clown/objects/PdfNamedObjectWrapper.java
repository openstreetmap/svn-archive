/*
  Copyright 2009 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

import it.stefanochizzolini.clown.files.File;

/**
  High-level representation of a strongly-typed PDF object
  that can be referenced also through a name. When such a name exists, the object
  is called 'named object', otherwise 'unnamed object'.
  <h3>Remarks</h3>
  <p>Some categories of objects in a PDF file can be also referred to by name
  rather than by object reference. The correspondence between names and objects
  is established by the document's name dictionary [PDF:1.6:3.6.3].</p>
  <p>The name's purpose is to provide a further level of referential abstraction
  especially for references across diverse PDF documents.</p>

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.8
*/
public abstract class PdfNamedObjectWrapper<TDataObject extends PdfDataObject>
  extends PdfObjectWrapper<TDataObject>
{
  // <class>
  // <dynamic>
  // <fields>
  private PdfString name;
  // </fields>

  // <constructors>
  protected PdfNamedObjectWrapper(
    File context,
    TDataObject baseDataObject
    )
  {
    this(
      context.register(baseDataObject),
      null,
      null
      );
  }

  /**
    @param baseObject Base PDF object. MUST be a {@link PdfReference}
    everytime available.
    @param container Indirect object containing the base object.
    @param name Object name.
  */
  protected PdfNamedObjectWrapper(
    PdfDirectObject baseObject,
    PdfIndirectObject container,
    PdfString name
    )
  {
    super(
      baseObject,
      container
      );
    this.name = name;
  }
  // </constructors>

  // <interface>
  // <public>
  /**
    Gets the object name.
  */
  public PdfString getName(
    )
  {return name;}

  /**
    Gets the object name, if available;
    otherwise, behaves like {@link PdfObjectWrapper#getBaseObject() getBaseObject()}.
  */
  public PdfDirectObject getNamedBaseObject(
    )
  {
    if(name != null)
      return name;
    else
      return getBaseObject();
  }
  // </public>

  // <internal>
  /**
    @see #getName()
  */
  void setName(
    PdfString value
    )
  {name = value;}
  // </internal>
  // </interface>
  // </dynamic>
  // </class>
}