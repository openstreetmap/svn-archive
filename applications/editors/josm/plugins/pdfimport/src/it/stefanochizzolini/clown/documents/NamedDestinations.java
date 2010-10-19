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

import it.stefanochizzolini.clown.documents.interaction.navigation.document.Destination;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.objects.NameTree;
import it.stefanochizzolini.clown.objects.PdfDataObject;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfReference;
import it.stefanochizzolini.clown.objects.PdfString;
import it.stefanochizzolini.clown.util.NotImplementedException;

/**
  Named destinations [PDF:1.6:3.6.3].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.4
*/
public class NamedDestinations
  extends NameTree<Destination>
{
  // <class>
  // <dynamic>
  // <constructors>
  public NamedDestinations(
    Document context
    )
  {super(context);}

  public NamedDestinations(
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
  public NamedDestinations clone(
    Document context
    )
  {throw new NotImplementedException();}
  // </public>

  // <protected>
  @Override
  protected Destination wrap(
    PdfDirectObject baseObject,
    PdfIndirectObject container,
    PdfString name
    )
  {
    /*
      NOTE: A named destination may be either an array defining the destination,
      or a dictionary with a D entry whose value is such an array [PDF:1.6:8.2.1].
    */
    PdfDirectObject destinationObject;
    {
      PdfDataObject baseDataObject = File.resolve(baseObject);
      if(baseDataObject instanceof PdfDictionary)
      {destinationObject = ((PdfDictionary)baseDataObject).get(PdfName.D);}
      else
      {destinationObject = baseObject;}
    }

    return Destination.wrap(
      destinationObject,
      baseObject instanceof PdfReference
        ? ((PdfReference)baseObject).getIndirectObject()
        : container,
      name
      );
  }
  // </protected>
  // </interface>
  // </dynamic>
  // </class>
}