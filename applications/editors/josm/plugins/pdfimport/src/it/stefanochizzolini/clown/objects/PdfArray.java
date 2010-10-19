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

package it.stefanochizzolini.clown.objects;

import it.stefanochizzolini.clown.bytes.IOutputStream;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
  PDF array object [PDF:1.6:3.2.5].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.0
  @version 0.0.8
*/
public class PdfArray
  extends PdfDirectObject
  implements List<PdfDirectObject>
{
  // <class>
  // <dynamic>
  // <fields>
  private ArrayList<PdfDirectObject> items;
  // </fields>

  // <constructors>
  public PdfArray(
    )
  {items = new ArrayList<PdfDirectObject>();}

  public PdfArray(
    int capacity
    )
  {items = new ArrayList<PdfDirectObject>(capacity);}

  public PdfArray(
    PdfDirectObject... items
    )
  {
    this(items.length);
    for(PdfDirectObject item : items)
    {this.items.add(item);}
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public Object clone(
    File context
    )
  {
    //TODO:IMPL redefine to support real cloning (current implementation is prone to object slicing hazard)!!!
    PdfArray clone = new PdfArray(items.size());

    for(PdfDirectObject item : items)
    {clone.add((PdfDirectObject)item.clone(context));}

    return clone;
  }

  @Override
  public int compareTo(
    PdfDirectObject obj
    )
  {throw new NotImplementedException();}

  /**
    Gets the dereferenced value corresponding to the given index.
    <h3>Remarks</h3>
    <p>This method takes care to resolve the value returned by {@link #get(int)}.</p>

    @param index Index of element to return.

    @since 0.0.8
   */
  public PdfDataObject resolve(
    int index
    )
  {return File.resolve(get(index));}

  @Override
  public String toString(
    )
  {
    StringBuilder buffer = new StringBuilder();

    // Begin.
    buffer.append("[ ");

    // Elements.
    for(PdfDirectObject item : items)
    {buffer.append(PdfDirectObject.toString(item)); buffer.append(" ");}

    // End.
    buffer.append("]");

    return buffer.toString();
  }

  @Override
  public void writeTo(
    IOutputStream stream
    )
  {
    // Begin.
    stream.write("[ ");

    // Items.
    for(PdfDirectObject item : items)
    {PdfDirectObject.writeTo(stream,item); stream.write(" ");}

    // End.
    stream.write("]");
  }

  // <List>
  public void add(
    int index,
    PdfDirectObject object
    )
  {items.add(index,object);}

  public boolean addAll(
    int index,
    Collection<? extends PdfDirectObject> objects
    )
  {return items.addAll(index,objects);}

  public PdfDirectObject get(
    int index
    )
  {return items.get(index);}

  public int indexOf(
    Object object
    )
  {return items.indexOf(object);}

  public int lastIndexOf(
    Object object
    )
  {return items.lastIndexOf(object);}

  public ListIterator<PdfDirectObject> listIterator(
    )
  {return items.listIterator();}

  public ListIterator<PdfDirectObject> listIterator(
    int index
    )
  {return items.listIterator(index);}

  public PdfDirectObject remove(
    int index
    )
  {return items.remove(index);}

  public PdfDirectObject set(
    int index,
    PdfDirectObject object
    )
  {return items.set(index,object);}

  public List<PdfDirectObject> subList(
    int fromIndex,
    int toIndex
    )
  {return items.subList(fromIndex,toIndex);}

  // <Collection>
  public boolean add(
    PdfDirectObject object
    )
  {return items.add(object);}

  public boolean addAll(
    Collection<? extends PdfDirectObject> objects
    )
  {return items.addAll(objects);}

  public void clear(
    )
  {items.clear();}

  public boolean contains(
    Object object
    )
  {return items.contains(object);}

  public boolean containsAll(
    Collection<?> objects
    )
  {return items.containsAll(objects);}

  public boolean equals(
    PdfDirectObject object
    )
  {throw new NotImplementedException();}

  public int hashCode(
    )
  {return items.hashCode();}

  public boolean isEmpty(
    )
  {return items.isEmpty();}

  public boolean remove(
    Object object
    )
  {return items.remove(object);}

  public boolean removeAll(
    Collection<?> objects
    )
  {return items.removeAll(objects);}

  public boolean retainAll(
    Collection<?> objects
    )
  {return items.retainAll(objects);}

  public int size(
    )
  {return items.size();}

  public PdfDirectObject[] toArray(
    )
  {return (PdfDirectObject[])items.toArray();}

  public <T> T[] toArray(
    T[] objects
    )
  {return (T[])items.toArray(objects);}

  // <Iterable>
  public Iterator<PdfDirectObject> iterator(
    )
  {return items.iterator();}
  // </Iterable>
  // </Collection>
  // </List>
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}