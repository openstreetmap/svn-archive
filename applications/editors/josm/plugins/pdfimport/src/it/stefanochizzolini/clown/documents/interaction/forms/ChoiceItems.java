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

package it.stefanochizzolini.clown.documents.interaction.forms;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
  Field options [PDF:1.6:8.6.3].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.7
  @version 0.0.8
*/
public class ChoiceItems
  extends PdfObjectWrapper<PdfArray>
  implements List<ChoiceItem>
{
  // <class>
  // <dynamic>
  // <fields>
  // </fields>

  // <constructors>
  public ChoiceItems(
    Document context
    )
  {
    super(
      context.getFile(),
      new PdfArray()
      );
  }

  public ChoiceItems(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {super(baseObject,container);}
  // </constructors>

  // <interface>
  // <public>
  public ChoiceItem add(
    String value
    )
  {
    ChoiceItem item = new ChoiceItem(value);
    add(item);

    return item;
  }

  public ChoiceItem add(
    int index,
    String value
    )
  {
    ChoiceItem item = new ChoiceItem(value);
    add(index, item);

    return item;
  }

  @Override
  public ChoiceItems clone(
    Document context
    )
  {throw new NotImplementedException();}

  // <List>
  public void add(
    int index,
    ChoiceItem value
    )
  {
    getBaseDataObject().add(index,value.getBaseObject());
    value.setItems(this);
  }

  public boolean addAll(
    int index,
    Collection<? extends ChoiceItem> values
    )
  {
    for(ChoiceItem value : values)
    {add(index++,value);}

    return true;
  }

  public ChoiceItem get(
    int index
    )
  {return new ChoiceItem(getBaseDataObject().get(index),getContainer(),this);}

  public int indexOf(
    Object value
    )
  {
    if(!(value instanceof ChoiceItem))
      return -1;

    return getBaseDataObject().indexOf(((ChoiceItem)value).getBaseObject());
  }

  public int lastIndexOf(
    Object value
    )
  {
    /*
      NOTE: Items are expected not to be duplicate.
    */
    return indexOf(value);
  }

  public ListIterator<ChoiceItem> listIterator(
    )
  {throw new NotImplementedException();}

  public ListIterator<ChoiceItem> listIterator(
    int index
    )
  {throw new NotImplementedException();}

  public ChoiceItem remove(
    int index
    )
  {
    PdfDirectObject itemObject = getBaseDataObject().remove(index);

    return new ChoiceItem(itemObject,getContainer(),null);
  }

  public ChoiceItem set(
    int index,
    ChoiceItem value
    )
  {
    PdfDirectObject oldValueObject = getBaseDataObject().set(index,value.getBaseObject());
    value.setItems(this);

    return new ChoiceItem(
      oldValueObject,
      getContainer(),
      null
      );
  }

  public List<ChoiceItem> subList(
    int fromIndex,
    int toIndex
    )
  {throw new NotImplementedException();}

  // <Collection>
  public boolean add(
    ChoiceItem value
    )
  {
    getBaseDataObject().add(value.getBaseObject());
    value.setItems(this);

    return true;
  }

  public boolean addAll(
    Collection<? extends ChoiceItem> values
    )
  {
    for(ChoiceItem value : values)
    {add(value);}

    return true;
  }

  public void clear(
    )
  {getBaseDataObject().clear();}

  public boolean contains(
    Object value
    )
  {
    if(!(value instanceof ChoiceItem))
      return false;

    return getBaseDataObject().contains(((ChoiceItem)value).getBaseObject());
  }

  public boolean containsAll(
    Collection<?> values
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
  {return getBaseDataObject().isEmpty();}

  public boolean remove(
    Object value
    )
  {
    if(!(value instanceof ChoiceItem))
      return false;

    return getBaseDataObject().remove(((ChoiceItem)value).getBaseObject());
  }

  public boolean removeAll(
    Collection<?> values
    )
  {throw new NotImplementedException();}

  public boolean retainAll(
    Collection<?> values
    )
  {throw new NotImplementedException();}

  public int size(
    )
  {return getBaseDataObject().size();}

  public Object[] toArray(
    )
  {return toArray(new ChoiceItem[0]);}

  @SuppressWarnings("unchecked")
  public <T> T[] toArray(
    T[] values
    )
  {
    PdfArray itemObjects = (PdfArray)getBaseDataObject();
    if(values.length < itemObjects.size())
    {values = (T[])new Object[itemObjects.size()];}

    PdfIndirectObject container = getContainer();
    for(
      int index = 0,
        length = itemObjects.size();
      index < length;
      index++
      )
    {values[index] = (T)new ChoiceItem(itemObjects.get(index),container,this);}

    return values;
  }

  // <Iterable>
  public Iterator<ChoiceItem> iterator(
    )
  {
    return new Iterator<ChoiceItem>()
    {
      // <class>
      // <dynamic>
      // <fields>
      /**
        Index of the next item.
      */
      private int index = 0;
      /**
        Collection size.
      */
      private int size = size();
      // </fields>

      // <interface>
      // <public>
      // <Iterator>
      public boolean hasNext(
        )
      {return (index < size);}

      public ChoiceItem next(
        )
      {
        if(!hasNext())
          throw new NoSuchElementException();

        return get(index++);
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
  // </interface>
  // </dynamic>
  // </class>
}
