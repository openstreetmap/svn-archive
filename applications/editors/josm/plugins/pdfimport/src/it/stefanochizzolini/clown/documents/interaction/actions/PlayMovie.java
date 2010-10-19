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

package it.stefanochizzolini.clown.documents.interaction.actions;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.documents.interaction.annotations.MovieAnnotation;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.util.NotImplementedException;

/**
  'Play a movie' action [PDF:1.6:8.5.3].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.7
  @since 0.0.7
*/
public class PlayMovie
  extends Action
{
  // <class>
  // <dynamic>
  // <constructors>
  /**
    Creates a new action within the given document context.
  */
  public PlayMovie(
    Document context,
    MovieAnnotation movie
    )
  {
    super(
      context,
      PdfName.Movie
      );

    setMovie(movie);
  }

  PlayMovie(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {super(baseObject,container);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public PlayMovie clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the movie to be played.
  */
  public MovieAnnotation getMovie(
    )
  {
    /*
      NOTE: Either 'Annotation' or 'T' entry MUST exist.
    */
    PdfDirectObject annotationObject = getBaseDataObject().get(PdfName.Annotation);
    if(annotationObject == null)
    {
      annotationObject = getBaseDataObject().get(PdfName.T);

      throw new NotImplementedException("No by-title movie annotation support currently: we have to implement a hook to the page of the referenced movie to get it from its annotations collection.");
    }

    return new MovieAnnotation(annotationObject,getContainer());
  }

  /**
    @see #getMovie()
  */
  public void setMovie(
    MovieAnnotation value
    )
  {
    if(value == null)
      throw new IllegalArgumentException("Movie MUST be defined.");

    getBaseDataObject().put(PdfName.Annotation,value.getBaseObject());
  }
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}