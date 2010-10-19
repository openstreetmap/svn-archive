/*
  Copyright 2008-2010 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.documents.interaction.actions;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.documents.fileSpecs.FileSpec;
import it.stefanochizzolini.clown.documents.interaction.navigation.document.RemoteDestination;
import it.stefanochizzolini.clown.objects.PdfBoolean;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.util.EnumSet;

/**
  'Change the view to a specified destination in another PDF file' action [PDF:1.6:8.5.3].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.7
*/
public class GoToRemote
  extends GoToNonLocal<RemoteDestination>
{
  // <class>
  // <dynamic>
  // <constructors>
  /**
    Creates a new action within the given document context.
  */
  public GoToRemote(
    Document context,
    FileSpec fileSpec,
    RemoteDestination destination
    )
  {
    super(
      context,
      PdfName.GoToR,
      fileSpec,
      destination
      );
  }

  GoToRemote(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {
    super(
      baseObject,
      container
      );
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public GoToRemote clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the action options.
  */
  public EnumSet<OptionsEnum> getOptions(
    )
  {
    EnumSet<OptionsEnum> options = EnumSet.noneOf(OptionsEnum.class);

    PdfDirectObject optionObject = getBaseDataObject().get(PdfName.NewWindow);
    if(optionObject != null
      && ((Boolean)((PdfBoolean)optionObject).getValue()).booleanValue())
    {options.add(OptionsEnum.NewWindow);}

    return options;
  }

  @Override
  public void setFileSpec(FileSpec value) {
    if(value == null)
      throw new IllegalArgumentException("FileSpec cannot be null.");

    super.setFileSpec(value);
  }

  /**
    @see #getOptions()
  */
  public void setOptions(
    EnumSet<OptionsEnum> value
    )
  {
    if(value.contains(OptionsEnum.NewWindow))
    {getBaseDataObject().put(PdfName.NewWindow,PdfBoolean.True);}
    else if(value.contains(OptionsEnum.SameWindow))
    {getBaseDataObject().put(PdfName.NewWindow,PdfBoolean.False);}
    else
    {getBaseDataObject().remove(PdfName.NewWindow);} // NOTE: Forcing the absence of this entry ensures that the viewer application should behave in accordance with the current user preference.
  }
  // </public>
  
  // <protected>
  @Override
  protected Class<RemoteDestination> getDestinationClass() {
    return RemoteDestination.class;
  }
  // </protected>
  // </interface>
  // </dynamic>
  // </class>
}