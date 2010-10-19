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

package it.stefanochizzolini.clown.documents;

import it.stefanochizzolini.clown.documents.interaction.actions.Action;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.util.NotImplementedException;

/**
  Page actions [PDF:1.6:8.5.2].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.7
  @since 0.0.7
*/
public class PageActions
  extends PdfObjectWrapper<PdfDictionary>
{
  // <class>
  // <dynamic>
  // <constructors>
  public PageActions(
    Document context
    )
  {
    super(
      context.getFile(),
      new PdfDictionary()
      );
  }

  public PageActions(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {super(baseObject,container);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public PageActions clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the action to be performed when the page is closed.
  */
  public Action getOnClose(
    )
  {
    /*
      NOTE: 'C' entry may be undefined.
    */
    PdfDirectObject onCloseObject = getBaseDataObject().get(PdfName.C);
    if(onCloseObject == null)
      return null;

    return Action.wrap(onCloseObject,getContainer());
  }

  /**
    Gets the action to be performed when the page is opened.
  */
  public Action getOnOpen(
    )
  {
    /*
      NOTE: 'O' entry may be undefined.
    */
    PdfDirectObject onOpenObject = getBaseDataObject().get(PdfName.O);
    if(onOpenObject == null)
      return null;

    return Action.wrap(onOpenObject,getContainer());
  }

  /**
    @see #getOnClose()
  */
  public void setOnClose(
    Action value
    )
  {getBaseDataObject().put(PdfName.C, value.getBaseObject());}

  /**
    @see #getOnOpen()
  */
  public void setOnOpen(
    Action value
    )
  {getBaseDataObject().put(PdfName.O, value.getBaseObject());}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}