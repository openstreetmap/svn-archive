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

package it.stefanochizzolini.clown.documents.interaction.navigation.document;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfInteger;
import it.stefanochizzolini.clown.objects.PdfString;
import it.stefanochizzolini.clown.util.NotImplementedException;

/**
  Remote interaction target [PDF:1.6:8.2.1].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.7
*/
public class RemoteDestination
  extends Destination
{
  // <class>
  // <dynamic>
  // <constructors>
  public RemoteDestination(
    Document context,
    int pageIndex
    )
  {
    this(
      context,
      pageIndex,
      ModeEnum.Fit,
      null
      );
  }

  public RemoteDestination(
    Document context,
    int pageIndex,
    ModeEnum mode,
    Double[] viewParams
    )
  {
    super(
      context,
      new PdfInteger(pageIndex),
      mode,
      viewParams
      );
  }

  public RemoteDestination(
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
  public RemoteDestination clone(
    Document context
    )
  {throw new NotImplementedException();}

  public int getPageIndex(
    )
  {return ((PdfInteger)getBaseDataObject().get(0)).getRawValue();}

  @Override
  public Object getPageRef(
    )
  {return getPageIndex();}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}