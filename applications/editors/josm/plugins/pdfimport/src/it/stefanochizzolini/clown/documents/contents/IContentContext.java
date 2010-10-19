/*
  Copyright 2006-2009 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

import java.awt.geom.Rectangle2D;

/**
  Content stream context.

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
*/
public interface IContentContext
  extends IContentEntity
{
  /**
    Gets the bounding box associated with the object, either explicitly (directly
    associated to the object) or (if not explicitly available) implicitly (inherited
    from a higher level object).
    <h3>Remarks</h3>
    <p>The box represents the physical-coordinates frame expressed in default
    user-space units. This frame is crucial for the proper positioning of graphic
    elements on the canvas, as the spec [PDF:1.6:4.2] states that the
    coordinate system is positively-oriented, although the typographic coordinates
    would be conveniently expressed as negatively-oriented.</p>
  */
  Rectangle2D getBox(
    );

  /**
    Gets the contents collection representing the content stream.
    @since 0.0.5
  */
  Contents getContents(
    );

  /**
    Gets the resources associated with the object, either explicitly (directly
    associated to the object) or (if not explicitly available) implicitly (inherited
    from a higher-level object).
    <h3>Remarks</h3>
    <p>The implementing class MUST ensure that the returned object isn't null.</p>
  */
  Resources getResources(
    );
}