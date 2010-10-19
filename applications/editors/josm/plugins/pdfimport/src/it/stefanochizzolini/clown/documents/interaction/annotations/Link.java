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

package it.stefanochizzolini.clown.documents.interaction.annotations;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.documents.Page;
import it.stefanochizzolini.clown.documents.interaction.ILink;
import it.stefanochizzolini.clown.documents.interaction.actions.Action;
import it.stefanochizzolini.clown.documents.interaction.navigation.document.Destination;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.awt.geom.Rectangle2D;

/**
  Link annotation [PDF:1.6:8.4.5].
  <p>It represents either a hypertext link to a destination elsewhere in the document
  or an action to be performed.</p>

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.7
*/
public class Link
  extends Annotation
  implements ILink
{
  // <class>
  // <dynamic>
  // <constructors>
  public Link(
    Page page,
    Rectangle2D box,
    Destination destination
    )
  {
    this(page,box);

    setDestination(destination);
  }

  public Link(
    Page page,
    Rectangle2D box,
    Action action
    )
  {
    this(page,box);

    AnnotationActions actions = new AnnotationActions(this);
    actions.setOnActivate(action);
    setActions(actions);
  }

  public Link(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {super(baseObject,container);}

  private Link(
    Page page,
    Rectangle2D box
    )
  {
    super(
      page.getDocument(),
      PdfName.Link,
      box,
      page
      );
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public Link clone(
    Document context
    )
  {throw new NotImplementedException();}

  // <ILink>
  public Destination getDestination(
    )
  {
    /*
      NOTE: 'Dest' entry may be undefined.
    */
    PdfDirectObject destinationObject = getBaseDataObject().get(PdfName.Dest);
    if(destinationObject == null)
      return null;

    return Document.resolveName(
      Destination.class,
      destinationObject,
      getContainer()
      );
  }

  public PdfObjectWrapper<?> getTarget(
    )
  {
    if(getBaseDataObject().containsKey(PdfName.Dest))
      return getDestination();
    else if(getBaseDataObject().containsKey(PdfName.A))
      return getAction();
    else
      return null;
  }

  @Override
  public void setAction(
    Action value
    )
  {
    /*
      NOTE: This entry is not permitted in link annotations if a 'Dest' entry is present.
    */
    if(getBaseDataObject().containsKey(PdfName.Dest)
      && value != null)
    {getBaseDataObject().remove(PdfName.Dest);}
    
    super.setAction(value);
  }
  
  /**
    @see #getDestination()
  */
  public void setDestination(
    Destination value
    )
  {
    if(value == null)
    {getBaseDataObject().remove(PdfName.Dest);}
    else
    {
      /*
        NOTE: This entry is not permitted in link annotations if an 'A' entry is present.
      */
      if(getBaseDataObject().containsKey(PdfName.A))
      {getBaseDataObject().remove(PdfName.A);}

      getBaseDataObject().put(PdfName.Dest,value.getNamedBaseObject());
    }
  }

  public void setTarget(
    PdfObjectWrapper<?> value
    )
  {
    if(value instanceof Destination)
    {setDestination((Destination)value);}
    else if(value instanceof Action)
    {setAction((Action)value);}
    else
      throw new IllegalArgumentException("It MUST be either a Destination or an Action.");
  }
  // </ILink>
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}