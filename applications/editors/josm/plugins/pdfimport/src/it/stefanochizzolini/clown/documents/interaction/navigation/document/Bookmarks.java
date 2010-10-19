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
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.objects.IPdfNumber;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfInteger;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfReference;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
  Collection of bookmarks [PDF:1.6:8.2.2].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
*/
public class Bookmarks
  extends PdfObjectWrapper<PdfDictionary>
  implements List<Bookmark>
{
  // <class>
  // <dynamic>
  // <constructors>
  public Bookmarks(
    Document context
    )
  {
    super(
      context.getFile(),
      new PdfDictionary(
        new PdfName[]
        {
          PdfName.Type,
          PdfName.Count
        },
        new PdfDirectObject[]
        {
          PdfName.Outlines,
          new PdfInteger(0)
        }
        )
      );
  }

  /**
    For internal use only.
  */
  public Bookmarks(
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
  public Bookmarks clone(
    Document context
    )
  {throw new NotImplementedException();}

  // <List>
  public void add(
    int index,
    Bookmark bookmark
    )
  {throw new NotImplementedException();}

  public boolean addAll(
    int index,
    Collection<? extends Bookmark> bookmarks
    )
  {throw new NotImplementedException();}

  public Bookmark get(
    int index
    )
  {
    PdfReference bookmarkObject = (PdfReference)getBaseDataObject().get(PdfName.First);
    while(index > 0)
    {
      bookmarkObject = (PdfReference)((PdfDictionary)File.resolve(bookmarkObject)).get(PdfName.Next);
      // Did we go past the collection range?
      if(bookmarkObject == null)
        throw new IndexOutOfBoundsException();

      index--;
    }

    return new Bookmark(bookmarkObject);
  }

  public int indexOf(
    Object bookmark
    )
  {throw new NotImplementedException();}

  public int lastIndexOf(
    Object bookmark
    )
  {return indexOf(bookmark);}

  public ListIterator<Bookmark> listIterator(
    )
  {throw new NotImplementedException();}

  public ListIterator<Bookmark> listIterator(
    int index
    )
  {throw new NotImplementedException();}

  public Bookmark remove(
    int index
    )
  {throw new NotImplementedException();}

  public Bookmark set(
    int index,
    Bookmark bookmark
    )
  {throw new NotImplementedException();}

  public List<Bookmark> subList(
    int fromIndex,
    int toIndex
    )
  {throw new NotImplementedException();}

  // <Collection>
  public boolean add(
    Bookmark bookmark
    )
  {
    /*
      NOTE: Bookmarks imported from alien PDF files MUST be cloned
      before being added.
    */
    bookmark.getBaseDataObject().put(PdfName.Parent,getBaseObject());

    PdfInteger countObject = ensureCountObject();
    // Is it the first bookmark?
    if(countObject.getRawValue() == 0) // First bookmark.
    {
      getBaseDataObject().put(PdfName.First,bookmark.getBaseObject());
      getBaseDataObject().put(PdfName.Last,bookmark.getBaseObject());

      ((IPdfNumber)countObject).translateNumberValue(+1);
    }
    else // Non-first bookmark.
    {
      PdfReference oldLastBookmarkReference = (PdfReference)getBaseDataObject().get(PdfName.Last);
      getBaseDataObject().put(PdfName.Last,bookmark.getBaseObject()); // Added bookmark is the last in the collection...
      ((PdfDictionary)File.resolve(oldLastBookmarkReference)).put(PdfName.Next,bookmark.getBaseObject()); // ...and the next of the previously-last bookmark.
      bookmark.getBaseDataObject().put(PdfName.Prev,oldLastBookmarkReference);

      /*
        NOTE: The Count entry is a relative number (whose sign represents
        the node open state).
      */
      ((IPdfNumber)countObject).translateNumberValue(Math.signum(countObject.getRawValue()));
    }

    return true;
  }

  public boolean addAll(
    Collection<? extends Bookmark> bookmarks
    )
  {throw new NotImplementedException();}

  public void clear(
    )
  {throw new NotImplementedException();}

  public boolean contains(
    Object bookmark
    )
  {throw new NotImplementedException();}

  public boolean containsAll(
    Collection<?> bookmarks
    )
  {throw new NotImplementedException();}

  public boolean equals(
    Object object
    )
  {throw new NotImplementedException();}

  public int hashCode(
    )
  {throw new NotImplementedException();}

  public boolean isEmpty(
    )
  {throw new NotImplementedException();}

  public boolean remove(
    Object bookmark
    )
  {throw new NotImplementedException();}

  public boolean removeAll(
    Collection<?> bookmarks
    )
  {throw new NotImplementedException();}

  public boolean retainAll(
    Collection<?> bookmarks
    )
  {throw new NotImplementedException();}

  public int size(
    )
  {
    /*
      NOTE: The Count entry may be absent [PDF:1.6:8.2.2].
    */
    PdfInteger countObject = (PdfInteger)getBaseDataObject().get(PdfName.Count);
    if(countObject == null)
      return 0;

    return countObject.getRawValue();
  }

  public Bookmark[] toArray(
    )
  {throw new NotImplementedException();}

  public <T> T[] toArray(
    T[] values
    )
  {throw new NotImplementedException();}

  // <Iterable>
  public Iterator<Bookmark> iterator(
    )
  {
    return new Iterator<Bookmark>()
    {
      // <class>
      // <dynamic>
      // <fields>
      /**
        Current bookmark.
      */
      private PdfDirectObject currentBookmarkObject = null;
      /**
        Next bookmark.
      */
      private PdfDirectObject nextBookmarkObject = getBaseDataObject().get(PdfName.First);
      // </fields>

      // <interface>
      // <public>
      // <Iterator>
      public boolean hasNext(
        )
      {return (nextBookmarkObject != null);}

      public Bookmark next(
        )
      {
        if(!hasNext())
          throw new NoSuchElementException();

        currentBookmarkObject = nextBookmarkObject;
        nextBookmarkObject = ((PdfDictionary)File.resolve(currentBookmarkObject)).get(PdfName.Next);

        return new Bookmark(currentBookmarkObject);
      }

      public void remove(
        )
      {throw new UnsupportedOperationException();}
      // </Iterator>
      // </public>
      // </interface>
      // </dynamic>
      // </class>
    };
  }
  // </Iterable>
  // </Collection>
  // </List>
  // </public>

  // <protected>
  /**
    Gets the count object, forcing its creation if it doesn't exist.
  */
  protected PdfInteger ensureCountObject(
    )
  {
    /*
      NOTE: The Count entry may be absent [PDF:1.6:8.2.2].
    */
    PdfInteger countObject = (PdfInteger)getBaseDataObject().get(PdfName.Count);
    if(countObject == null)
      getBaseDataObject().put(PdfName.Count,countObject = new PdfInteger(0));

    return countObject;
  }
  // </protected>
  // </interface>
  // </dynamic>
  // </class>
}