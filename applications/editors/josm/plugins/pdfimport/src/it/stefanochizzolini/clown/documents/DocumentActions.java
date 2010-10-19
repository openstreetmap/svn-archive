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

package it.stefanochizzolini.clown.documents;

import it.stefanochizzolini.clown.documents.interaction.actions.Action;
import it.stefanochizzolini.clown.documents.interaction.navigation.document.Destination;
import it.stefanochizzolini.clown.documents.interaction.navigation.document.LocalDestination;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.util.NotImplementedException;

/**
  Document actions [PDF:1.6:8.5.2].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.7
*/
public class DocumentActions
  extends PdfObjectWrapper<PdfDictionary>
{
  // <class>
  // <dynamic>
  // <constructors>
  public DocumentActions(
    Document context
    )
  {
    super(
      context.getFile(),
      new PdfDictionary()
      );
  }

  public DocumentActions(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {super(baseObject,container);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public DocumentActions clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the action to be performed after printing the document.
  */
  public Action getAfterPrint(
    )
  {
    /*
      NOTE: 'DP' entry may be undefined.
    */
    PdfDirectObject afterPrintObject = getBaseDataObject().get(PdfName.DP);
    if(afterPrintObject == null)
      return null;

    return Action.wrap(afterPrintObject,getContainer());
  }

  /**
    Gets the action to be performed after saving the document.
  */
  public Action getAfterSave(
    )
  {
    /*
      NOTE: 'DS' entry may be undefined.
    */
    PdfDirectObject afterSaveObject = getBaseDataObject().get(PdfName.DS);
    if(afterSaveObject == null)
      return null;

    return Action.wrap(afterSaveObject,getContainer());
  }

  /**
    Gets the action to be performed before printing the document.
  */
  public Action getBeforePrint(
    )
  {
    /*
      NOTE: 'WP' entry may be undefined.
    */
    PdfDirectObject beforePrintObject = getBaseDataObject().get(PdfName.WP);
    if(beforePrintObject == null)
      return null;

    return Action.wrap(beforePrintObject,getContainer());
  }

  /**
    Gets the action to be performed before saving the document.
  */
  public Action getBeforeSave(
    )
  {
    /*
      NOTE: 'WS' entry may be undefined.
    */
    PdfDirectObject beforeSaveObject = getBaseDataObject().get(PdfName.WS);
    if(beforeSaveObject == null)
      return null;

    return Action.wrap(beforeSaveObject,getContainer());
  }

  /**
    Gets the action to be performed before closing the document.
  */
  public Action getOnClose(
    )
  {
    /*
      NOTE: 'DC' entry may be undefined.
    */
    PdfDirectObject onCloseObject = getBaseDataObject().get(PdfName.DC);
    if(onCloseObject == null)
      return null;

    return Action.wrap(onCloseObject,getContainer());
  }

  /**
    Gets the destination to be displayed or the action to be performed
    after opening the document.
  */
  public PdfObjectWrapper<?> getOnOpen(
    )
  {
    /*
      NOTE: 'OpenAction' entry may be undefined.
    */
    PdfDirectObject onOpenObject = getDocument().getBaseDataObject().get(PdfName.OpenAction);
    if(onOpenObject == null)
      return null;

    if(onOpenObject instanceof PdfDictionary) // Action (dictionary).
      return Action.wrap(onOpenObject,getContainer());
    else // Destination (array).
      return Destination.wrap(onOpenObject,getContainer(),null);
  }

  /**
    @see #getAfterPrint()
  */
  public void setAfterPrint(
    Action value
    )
  {getBaseDataObject().put(PdfName.DP, value.getBaseObject());}

  /**
    @see #getAfterSave()
  */
  public void setAfterSave(
    Action value
    )
  {getBaseDataObject().put(PdfName.DS, value.getBaseObject());}

  /**
    @see #getBeforePrint()
  */
  public void setBeforePrint(
    Action value
    )
  {getBaseDataObject().put(PdfName.WP, value.getBaseObject());}

  /**
    @see #getBeforeSave()
  */
  public void setBeforeSave(
    Action value
    )
  {getBaseDataObject().put(PdfName.WS, value.getBaseObject());}

  /**
    @see #getOnClose()
  */
  public void setOnClose(
    Action value
    )
  {getBaseDataObject().put(PdfName.DC, value.getBaseObject());}

  /**
    @see #getOnOpen()
  */
  public void setOnOpen(
    PdfObjectWrapper<?> value
    )
  {
    if(!(value instanceof Action
      || value instanceof LocalDestination))
      throw new IllegalArgumentException("Value MUST be either an Action or a LocalDestination.");

    Document document = getDocument();
    document.getBaseDataObject().put(PdfName.OpenAction, value.getBaseObject());
    document.update(); // Ensures that the document's object modification is preserved.
  }
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}