/*
  Copyright 2006-2010 Stefano Chizzolini. http://clown.stefanochizzolini.it

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
import it.stefanochizzolini.clown.documents.interaction.ILink;
import it.stefanochizzolini.clown.documents.interaction.actions.Action;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfInteger;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfReference;
import it.stefanochizzolini.clown.objects.PdfTextString;
import it.stefanochizzolini.clown.util.NotImplementedException;

/**
  Outline item [PDF:1.6:8.2.2].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
*/
public class Bookmark
  extends PdfObjectWrapper<PdfDictionary>
  implements ILink
{
  // <class>
  // <dynamic>
  // <constructors>
  public Bookmark(
    Document context,
    String title
    )
  {
    super(
      context.getFile(),
      new PdfDictionary()
      );
    setTitle(title);
  }

  public Bookmark(
    Document context,
    String title,
    LocalDestination destination
    )
  {
    this(context,title);
    setDestination(destination);
  }

  public Bookmark(
    Document context,
    String title,
    Action action
    )
  {
    this(context,title);
    setAction(action);
  }
  
  Bookmark(
    PdfDirectObject baseObject
    )
  {
    super(
      baseObject,
      null // NO container (bookmark MUST be an indirect object [PDF:1.6:8.2.2]).
      );
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public Bookmark clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the child bookmarks.
  */
  public Bookmarks getBookmarks(
    )
  {return new Bookmarks(getBaseObject());}

  /**
    Gets the parent bookmark.
  */
  public Bookmark getParent(
    )
  {
    PdfReference reference = (PdfReference)getBaseDataObject().get(PdfName.Parent);
    // Is its parent a bookmark?
    /*
      NOTE: the Title entry can be used as a flag to distinguish bookmark
      (outline item) dictionaries from outline (root) dictionaries.
    */
    if(((PdfDictionary)File.resolve(reference)).containsKey(PdfName.Title)) // Bookmark.
      return new Bookmark(reference);
    else // Outline root.
      return null; // NO parent bookmark.
  }

  /**
    Gets the text to be displayed for this bookmark.
  */
  public String getTitle(
    )
  {return (String)((PdfTextString)getBaseDataObject().get(PdfName.Title)).getValue();}

  /**
    Gets whether this bookmark's children are displayed.
  */
  public boolean isExpanded(
    )
  {
    PdfInteger countObject = (PdfInteger)getBaseDataObject().get(PdfName.Count);

    return (countObject == null
        || countObject.getRawValue() >= 0);
  }

  /**
    @see #isExpanded()
  */
  public void setExpanded(
    boolean value
    )
  {
    PdfInteger countObject = (PdfInteger)getBaseDataObject().get(PdfName.Count);
    if(countObject == null)
      return;

    /*
      NOTE: Non-negative Count entry means open, negative Count entry means closed [PDF:1.6:8.2.2].
    */
    countObject.setRawValue((value ? 1 : -1) * Math.abs(countObject.getRawValue()));
  }

  /**
    @see #getTitle()
  */
  public void setTitle(
    String value
    )
  {getBaseDataObject().put(PdfName.Title,new PdfTextString(value));}
  
  // <ILink>
  public Action getAction(
    )
  {
    /*
      NOTE: 'A' entry may be undefined.
    */
    PdfDirectObject actionObject = getBaseDataObject().get(PdfName.A);
    if(actionObject == null)
      return null;
  
    return Action.wrap(actionObject,getContainer());
  }

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
      LocalDestination.class,
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

  public void setAction(
    Action value
    )
  {
    if(value == null)
    {getBaseDataObject().remove(PdfName.A);}
    else
    {
      /*
        NOTE: This entry is not permitted in bookmarks if a 'Dest' entry already exists.
      */
      if(getBaseDataObject().containsKey(PdfName.Dest))
      {getBaseDataObject().remove(PdfName.Dest);}
      
      getBaseDataObject().put(PdfName.A,value.getBaseObject());
    }
  }
  
  public void setDestination(
    Destination value
    )
  {
    if(value == null)
    {getBaseDataObject().remove(PdfName.Dest);}
    else
    {
      /*
        NOTE: This entry is not permitted in bookmarks if an 'A' entry already exists.
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