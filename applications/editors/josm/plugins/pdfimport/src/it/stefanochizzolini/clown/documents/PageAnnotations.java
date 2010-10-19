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

package it.stefanochizzolini.clown.documents;

import it.stefanochizzolini.clown.documents.interaction.annotations.Annotation;
import it.stefanochizzolini.clown.objects.PdfArray;
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
  Page annotations [PDF:1.6:3.6.2].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.7
*/
public class PageAnnotations
  extends PdfObjectWrapper<PdfArray>
  implements List<Annotation>
{
  // <class>
  // <dynamic>
  // <fields>
  private Page page;
  // </fields>

  // <constructors>
  PageAnnotations(
    PdfDirectObject baseObject,
    PdfIndirectObject container,
    Page page
    )
  {
    super(
      baseObject,
      container
      );

    this.page = page;
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public PageAnnotations clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the page associated to these annotations.
  */
  public Page getPage(
    )
  {return page;}

  // <List>
  public void add(
    int index,
    Annotation value
    )
  {getBaseDataObject().add(index,value.getBaseObject());}

  public boolean addAll(
    int index,
    Collection<? extends Annotation> values
    )
  {
    PdfArray items = getBaseDataObject();
    for(Annotation value : values)
    {items.add(index++,value.getBaseObject());}

    return true;
  }

  public Annotation get(
    int index
    )
  {return Annotation.wrap(getBaseDataObject().get(index),getContainer());}

  public int indexOf(
    Object value
    )
  {
    if(!(value instanceof Annotation))
      return -1;

    return getBaseDataObject().indexOf(((Annotation)value).getBaseObject());
  }

  public int lastIndexOf(
    Object value
    )
  {
    /*
      NOTE: Annotations are expected not to be duplicate.
    */
    return indexOf(value);
  }

  public ListIterator<Annotation> listIterator(
    )
  {throw new NotImplementedException();}

  public ListIterator<Annotation> listIterator(
    int index
    )
  {throw new NotImplementedException();}

  public Annotation remove(
    int index
    )
  {
    PdfDirectObject annotationObject = getBaseDataObject().remove(index);
    return Annotation.wrap(annotationObject,getContainer());
  }

  public Annotation set(
    int index,
    Annotation value
    )
  {return Annotation.wrap(getBaseDataObject().set(index,value.getBaseObject()),getContainer());}

  public List<Annotation> subList(
    int fromIndex,
    int toIndex
    )
  {throw new NotImplementedException();}

  // <Collection>
  public boolean add(
    Annotation value
    )
  {
    // Assign the annotation to the page!
    value.getBaseDataObject().put(PdfName.P,page.getBaseObject());

    return getBaseDataObject().add(value.getBaseObject());
  }

  public boolean addAll(
    Collection<? extends Annotation> values
    )
  {
    for(Annotation value : values)
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
    if(!(value instanceof Annotation))
      return false;

    return getBaseDataObject().contains(((Annotation)value).getBaseObject());
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
  {return getBaseDataObject().remove(((Annotation)value).getBaseObject());}

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
  {return toArray(new Annotation[0]);}

  @SuppressWarnings("unchecked")
  public <T> T[] toArray(
    T[] values
    )
  {
    PdfArray annotationObjects = getBaseDataObject();
    if(values.length < annotationObjects.size())
    {values = (T[])new Object[annotationObjects.size()];}

    PdfIndirectObject container = getContainer();
    for(
      int index = 0,
        length = annotationObjects.size();
      index < length;
      index++
      )
    {values[index] = (T)Annotation.wrap(annotationObjects.get(index),container);}
    return values;
  }

  // <Iterable>
  public Iterator<Annotation> iterator(
    )
  {
    return new Iterator<Annotation>()
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

      public Annotation next(
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