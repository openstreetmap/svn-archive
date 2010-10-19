/*
  Copyright 2007-2009 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.documents.fileSpecs.FileSpec;
import it.stefanochizzolini.clown.documents.interaction.navigation.document.Destination;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfString;
import it.stefanochizzolini.clown.util.NotImplementedException;

/**
  Name dictionary [PDF:1.6:3.6.3].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.7
  @since 0.0.4
*/
public class Names
  extends PdfObjectWrapper<PdfDictionary>
{
  // <class>
  // <dynamic>
  // <constructors>
  public Names(
    Document context
    )
  {
    super(
      context.getFile(),
      new PdfDictionary()
      );
  }

  /**
    For internal use only.
  */
  public Names(
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
  public Names clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the map of name strings to destinations.
  */
  public NamedDestinations getDestinations(
    )
  {
    /*
      NOTE: 'Dests' entry may be undefined.
    */
    PdfDirectObject destinations = getBaseDataObject().get(PdfName.Dests);
    if(destinations == null)
      return null;

    return new NamedDestinations(
      destinations,
      getContainer()
      );
  }

  /**
    Gets the map of name strings to embedded files.
  */
  public NamedEmbeddedFiles getEmbeddedFiles(
    )
  {
    /*
      NOTE: 'EmbeddedFiles' entry may be undefined.
    */
    PdfDirectObject embeddedFiles = getBaseDataObject().get(PdfName.EmbeddedFiles);
    if(embeddedFiles == null)
      return null;

    return new NamedEmbeddedFiles(
      embeddedFiles,
      getContainer()
      );
  }

  @SuppressWarnings("unchecked")
  public <T extends PdfObjectWrapper> T resolve(
    Class<T> type,
    PdfString name
    )
  {
    if(Destination.class.isAssignableFrom(type))
      return (T)getDestinations().get(name);
    if(FileSpec.class.isAssignableFrom(type))
      return (T)getEmbeddedFiles().get(name);
    throw new UnsupportedOperationException("Named type '" + type.getName() + "' is not supported.");
  }

  /**
    Sets the map of name strings to destinations.
  */
  public void setDestinations(
    NamedDestinations value
    )
  {getBaseDataObject().put(PdfName.Dests,value.getBaseObject());}

  /**
    @see #getEmbeddedFiles()
  */
  public void setEmbeddedFiles(
    NamedEmbeddedFiles value
    )
  {getBaseDataObject().put(PdfName.EmbeddedFiles,value.getBaseObject());}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}