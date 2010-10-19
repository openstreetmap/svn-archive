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

package it.stefanochizzolini.clown.documents.interaction.actions;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfDataObject;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
  Chained actions [PDF:1.6:8.5.1].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.7
*/
public class ChainedActions
  extends PdfObjectWrapper<PdfDataObject>
  implements List<Action>
{
  /*
    NOTE: Chained actions may be either singular or multiple (within an array).
    This implementation hides such a complexity to the user, smoothly exposing
    just the most general case (array) yet preserving its internal state.
  */
  // <class>
  // <dynamic>
  // <fields>
  /**
    Parent action.
  */
  private Action parent;
  // </fields>

  // <constructors>
  ChainedActions(
    PdfDirectObject baseObject,
    PdfIndirectObject container,
    Action parent
    )
  {
    super(
      baseObject,
      container
      );

    this.parent = parent;
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public ChainedActions clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the parent action.
  */
  public Action getParent(
    )
  {return parent;}

  // <List>
  public void add(
    int index,
    Action value
    )
  {ensureArray().add(index,value.getBaseObject());}

  public boolean addAll(
    int index,
    Collection<? extends Action> values
    )
  {
    PdfArray items = ensureArray();
    for(Action value : values)
    {items.add(index++,value.getBaseObject());}

    return true;
  }

  public Action get(
    int index
    )
  {
    PdfDataObject baseDataObject = getBaseDataObject();
    if(baseDataObject instanceof PdfDictionary) // Single action.
    {
      if(index != 0)
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: 1");

      return Action.wrap(getBaseObject(),getContainer());
    }

    return Action.wrap(((PdfArray)baseDataObject).get(index),getContainer());
  }

  public int indexOf(
    Object value
    )
  {
    if(!(value instanceof Action))
      return -1;

    PdfDataObject baseDataObject = getBaseDataObject();
    if(baseDataObject instanceof PdfDictionary) // Single action.
    {
      if(!((Action)value).getBaseObject().equals(getBaseObject()))
        return -1;

      return 0;
    }

    return ((PdfArray)baseDataObject).indexOf(((Action)value).getBaseObject());
  }

  public int lastIndexOf(
    Object value
    )
  {
    /*
      NOTE: Actions are expected not to be duplicated.
    */
    return indexOf(value);
  }

  public ListIterator<Action> listIterator(
    )
  {throw new NotImplementedException();}

  public ListIterator<Action> listIterator(
    int index
    )
  {throw new NotImplementedException();}

  public Action remove(
    int index
    )
  {
    PdfDirectObject actionObject = ensureArray().remove(index);

    return Action.wrap(actionObject,getContainer());
  }

  public Action set(
    int index,
    Action value
    )
  {
    return Action.wrap(
      ensureArray().set(index,value.getBaseObject()),
      getContainer()
      );
  }

  public List<Action> subList(
    int fromIndex,
    int toIndex
    )
  {throw new NotImplementedException();}

  // <Collection>
  public boolean add(
    Action value
    )
  {return ensureArray().add(value.getBaseObject());}

  public boolean addAll(
    Collection<? extends Action> values
    )
  {
    for(Action value : values)
    {add(value);}

    return true;
  }

  public void clear(
    )
  {ensureArray().clear();}

  public boolean contains(
    Object value
    )
  {
    if(!(value instanceof Action))
      return false;

    PdfDataObject baseDataObject = getBaseDataObject();
    if(baseDataObject instanceof PdfDictionary) // Single action.
      return ((Action)value).getBaseObject().equals(getBaseObject());

    return ((PdfArray)baseDataObject).contains(((Action)value).getBaseObject());
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
  {
    PdfDataObject baseDataObject = getBaseDataObject();
    if(baseDataObject instanceof PdfDictionary) // Single action.
      return false;

    return ((PdfArray)baseDataObject).isEmpty();
  }

  public boolean remove(
    Object value
    )
  {
    if(!(value instanceof Action))
      return false;

    return ensureArray().remove(((Action)value).getBaseObject());
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
  {
    PdfDataObject baseDataObject = getBaseDataObject();
    if(baseDataObject instanceof PdfDictionary) // Single action.
      return 1;

    return ((PdfArray)baseDataObject).size();
  }

  public Object[] toArray(
    )
  {return toArray(new Action[0]);}

  @SuppressWarnings("unchecked")
  public <T> T[] toArray(
    T[] values
    )
  {
    PdfDataObject baseDataObject = getBaseDataObject();
    if(baseDataObject instanceof PdfDictionary) // Single action.
    {
      if(values.length == 0)
      {values = (T[])new Object[1];}

      values[0] = (T)Action.wrap(getBaseObject(),getContainer());
    }
    else // Array.
    {
      PdfArray actionObjects = (PdfArray)baseDataObject;
      if(values.length < actionObjects.size())
      {values = (T[])new Object[actionObjects.size()];}

      PdfIndirectObject container = getContainer();
      for(
        int index = 0,
          length = actionObjects.size();
        index < length;
        index++
        )
      {values[index] = (T)Action.wrap(actionObjects.get(index),container);}
    }
    return values;
  }

  // <Iterable>
  public Iterator<Action> iterator(
    )
  {
    return new Iterator<Action>()
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

      public Action next(
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

  // <private>
  private PdfArray ensureArray(
    )
  {
    PdfDataObject baseDataObject = getBaseDataObject();
    if(baseDataObject instanceof PdfDictionary) // Single action.
    {
      PdfArray actionsArray = new PdfArray();
      actionsArray.add(getBaseObject());
      setBaseObject(actionsArray);
      parent.getBaseDataObject().put(PdfName.Next,actionsArray);

      baseDataObject = actionsArray;
    }

    return (PdfArray)baseDataObject;
  }
  // </private>
  // </interface>
  // </dynamic>
  // </class>
}