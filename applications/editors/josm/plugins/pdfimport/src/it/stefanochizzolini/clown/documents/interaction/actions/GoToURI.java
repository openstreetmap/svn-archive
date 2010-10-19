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
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfString;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.net.URI;

/**
  'Cause a URI (Uniform Resource Identifier) to be resolved' action [PDF:1.6:8.5.3].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.7
  @version 0.0.8
*/
public class GoToURI
  extends Action
  implements IGoToAction
{
  // <class>
  // <dynamic>
  // <constructors>
  /**
    Creates a new action within the given document context.
  */
  public GoToURI(
    Document context,
    URI uri
    )
  {
    super(
      context,
      PdfName.URI
      );
    setURI(uri);
  }

  GoToURI(
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
  public GoToURI clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the uniform resource identifier to resolve [RFC 2396].
  */
  public URI getURI(
    )
  {
    /*
      NOTE: 'URI' entry MUST exist.
    */
    try
    {
      return new URI(
        (String)((PdfString)getBaseDataObject().get(PdfName.URI)).getValue()
        );
    }
    catch(Exception exception)
    {throw new RuntimeException(exception);}
  }

  /**
    @see #getURI()
  */
  public void setURI(
    URI value
    )
  {getBaseDataObject().put(PdfName.URI,new PdfString(value.toString()));}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}