/*
  Copyright 2007-2010 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

import it.stefanochizzolini.clown.bytes.IBuffer;
import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.documents.contents.objects.ContentObject;
import it.stefanochizzolini.clown.documents.contents.tokens.Parser;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfDataObject;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfReference;
import it.stefanochizzolini.clown.objects.PdfStream;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
  Content objects collection.

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.4
*/
public class Contents
  extends PdfObjectWrapper<PdfDataObject>
  implements List<ContentObject>
{
  // <class>
  // <dynamic>
  // <fields>
  private List<ContentObject> items;

  private IContentContext contentContext;
  // </fields>

  // <constructors>
  /**
    For internal use only.
  */
  public Contents(
    PdfDirectObject baseObject,
    PdfIndirectObject container,
    IContentContext contentContext
    )
  {
    super(
      baseObject,
      container
      );

    this.contentContext = contentContext;

    load();
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public Contents clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Serializes the contents into the content stream.
  */
  public void flush(
    )
  {
    PdfStream stream;
    PdfDataObject baseDataObject = getBaseDataObject();
    // Are contents just a single stream object?
    if(baseDataObject instanceof PdfStream) // Single stream.
    {stream = (PdfStream)baseDataObject;}
    else // Array of streams.
    {
      PdfArray streams = (PdfArray)baseDataObject;
      // No stream available?
      if(streams.size() == 0) // No stream.
      {
        // Add first stream!
        stream = new PdfStream();
        streams.add( // Inserts the new stream into the content stream.
          getFile().register(stream) // Inserts the new stream into the file.
          );
      }
      else // Streams exist.
      {
        // Eliminating exceeding streams...
        /*
          NOTE: Applications that consume or produce PDF files are not required to preserve
          the existing structure of the Contents array [PDF:1.6:3.6.2].
        */
        while(streams.size() > 1)
        {
          getFile().unregister( // Removes the exceeding stream from the file.
            (PdfReference)streams.remove(1) // Removes the exceeding stream from the content stream.
            );
        }

        PdfReference streamReference = (PdfReference)streams.get(0);
        File.update(streamReference); // Updates the existing stream into the file.
        stream = (PdfStream)streamReference.getDataObject();
      }
    }

    // Get the stream buffer!
    IBuffer buffer = stream.getBody();
    // Delete old contents from the stream buffer!
    buffer.setLength(0);
    // Serializing the new contents into the stream buffer...
    for(ContentObject item : items)
    {item.writeTo(buffer);}

    // Update the content stream container!
    update();
  }

  public IContentContext getContentContext(
    )
  {return contentContext;}

  // <List>
  public void add(
    int index,
    ContentObject content
    )
  {items.add(index,content);}

  public boolean addAll(
    int index,
    Collection<? extends ContentObject> contents
    )
  {return items.addAll(index,contents);}

  public ContentObject get(
    int index
    )
  {return items.get(index);}

  public int indexOf(
    Object content
    )
  {return items.indexOf(content);}

  public int lastIndexOf(
    Object content
    )
  {return items.lastIndexOf(content);}

  public ListIterator<ContentObject> listIterator(
    )
  {return items.listIterator();}

  public ListIterator<ContentObject> listIterator(
    int index
    )
  {return items.listIterator(index);}

  public ContentObject remove(
    int index
    )
  {return items.remove(index);}

  public ContentObject set(
    int index,
    ContentObject content
    )
  {return items.set(index,content);}

  public List<ContentObject> subList(
    int fromIndex,
    int toIndex
    )
  {return items.subList(fromIndex,toIndex);}

  // <Collection>
  public boolean add(
    ContentObject content
    )
  {return items.add(content);}

  public boolean addAll(
    Collection<? extends ContentObject> contents
    )
  {return items.addAll(contents);}

  public void clear(
    )
  {items.clear();}

  public boolean contains(
    Object content
    )
  {return items.contains(content);}

  public boolean containsAll(
    Collection<?> contents
    )
  {return items.containsAll(contents);}

  public boolean equals(
    Object object
    )
  {throw new NotImplementedException();}

  public int hashCode(
    )
  {throw new NotImplementedException();}

  public boolean isEmpty(
    )
  {return items.isEmpty();}

  public boolean remove(
    Object content
    )
  {return items.remove(content);}

  public boolean removeAll(
    Collection<?> contents
    )
  {return items.removeAll(contents);}

  public boolean retainAll(
    Collection<?> contents
    )
  {return items.retainAll(contents);}

  public int size(
    )
  {return items.size();}

  public Object[] toArray(
    )
  {return items.toArray();}

  public <T> T[] toArray(
    T[] contents
    )
  {return items.toArray(contents);}

  // <Iterable>
  public Iterator<ContentObject> iterator(
    )
  {return items.iterator();}
  // </Iterable>
  // </Collection>
  // </List>
  // </public>

  // <private>
  private void load(
    )
  {
    final Parser parser = new Parser(getBaseDataObject());
    try
    {items = parser.parseContentObjects();}
    catch(Exception e)
    {throw new RuntimeException(e);}
  }
  // </private>
  // </interface>
  // </dynamic>
  // </class>
}