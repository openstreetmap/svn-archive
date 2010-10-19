/*
  Copyright 2007-2008 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.documents.contents;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.documents.contents.composition.PrimitiveFilter;
import it.stefanochizzolini.clown.documents.contents.objects.ContentObject;
import it.stefanochizzolini.clown.documents.contents.xObjects.XObject;

/**
  Generic content entity.
  <p>It provides common ways to convert any content into content stream objects.</p>

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.6
  @since 0.0.5
*/
public interface IContentEntity
{
  /**
    Converts the entity to its equivalent inline (dependent) object representation [PDF:1.6:4.8.6].

    <h3>Remarks</h3>
    <p>This method creates and shows an inline object within the target content context,
    returning it.</p>
    <p>Due to its direct-content nature (opposite to the indirect-content nature of
    external objects (see {@link #toXObject(Document)})), the resulting object should be shown
    only one time in order not to wastefully duplicate its data.</p>

    @param context Target content context.
    @return The inline object representing the entity.
    @since 0.0.6
  */
  ContentObject toInlineObject(
    PrimitiveFilter context
    );

  /**
    Converts the entity to its equivalent external (independent) object representation [PDF:1.6:4.7].

    <h3>Remarks</h3>
    <p>This method creates an external object within the target document, returning it. To show it
    in a content context (for example: a page), then it must be applied in an appropriate manner (see
    {@link it.stefanochizzolini.clown.documents.contents.composition.PrimitiveFilter PrimitiveFilter} object).</p>

    @param context Target document.
    @return The external object representing the entity.
    @since 0.0.5
  */
  XObject toXObject(
    Document context
    );
}