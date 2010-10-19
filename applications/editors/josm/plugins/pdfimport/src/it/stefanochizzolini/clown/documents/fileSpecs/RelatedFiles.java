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

package it.stefanochizzolini.clown.documents.fileSpecs;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfTextString;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
  Embedded files referenced by another one (dependencies) [PDF:1.6:3.10.3].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.7
  @version 0.0.7
*/
public class RelatedFiles
  extends PdfObjectWrapper<PdfArray>
  implements Map<String,EmbeddedFile>
{
  // <class>
  // <classes>
  private class Entry
    implements Map.Entry<String,EmbeddedFile>
  {
    // <class>
    // <dynamic>
    // <fields>
    private final String key;
    private final EmbeddedFile value;
    // </fields>

    // <constructors>
    private Entry(
      String key,
      EmbeddedFile value
      )
    {
      this.key = key;
      this.value = value;
    }
    // </constructors>

    // <interface>
    // <public>
    // <Map.Entry>
    public String getKey(
      )
    {return key;}

    public EmbeddedFile getValue(
      )
    {return value;}

    public EmbeddedFile setValue(
      EmbeddedFile value
      )
    {throw new UnsupportedOperationException();}
    // </Map.Entry>
    // </public>
    // </interface>
    // </dynamic>
    // </class>
  }
  // </classes>

  // <dynamic>
  // <constructors>
  public RelatedFiles(
    Document context
    )
  {
    super(
      context.getFile(),
      new PdfArray()
      );
  }

  RelatedFiles(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {super(baseObject,container);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public RelatedFiles clone(
    Document context
    )
  {throw new NotImplementedException();}

  // <Map>
  public void clear(
    )
  {getBaseDataObject().clear();}

  public boolean containsKey(
    Object key
    )
  {
    PdfArray itemPairs = getBaseDataObject();
    for(
      int index = 0,
        length = itemPairs.size();
      index < length;
      index += 2
      )
    {
      if(((PdfTextString)itemPairs.get(index)).getValue().equals(key))
        return true;
    }

    return false;
  }

  public boolean containsValue(
    Object value
    )
  {
    PdfArray itemPairs = getBaseDataObject();
    for(
      int index = 1,
        length = itemPairs.size();
      index < length;
      index += 2
      )
    {
      if(itemPairs.get(index).equals(value))
        return true;
    }

    return false;
  }

  public Set<Map.Entry<String,EmbeddedFile>> entrySet(
    )
  {
    HashSet<Map.Entry<String,EmbeddedFile>> entrySet = new HashSet<Map.Entry<String,EmbeddedFile>>();
    PdfArray itemPairs = getBaseDataObject();
    for(
      int index = 0,
        length = itemPairs.size();
      index < length;
      index += 2
      )
    {
      entrySet.add(
        new Entry(
          (String)((PdfTextString)itemPairs.get(index)).getValue(),
          new EmbeddedFile(itemPairs.get(index+1))
          )
        );
    }

    return entrySet;
  }

  public boolean equals(
    PdfDirectObject object
    )
  {throw new NotImplementedException();}

  public EmbeddedFile get(
    Object key
    )
  {
    PdfArray itemPairs = getBaseDataObject();
    for(
      int index = 0,
        length = itemPairs.size();
      index < length;
      index += 2
      )
    {
      if(((PdfTextString)itemPairs.get(index)).getValue().equals(key))
        return new EmbeddedFile(itemPairs.get(index+1));
    }

    return null;
  }

  public int hashCode(
    )
  {throw new NotImplementedException();}

  public boolean isEmpty(
    )
  {return (getBaseDataObject().size() == 0);}

  public Set<String> keySet(
    )
  {
    HashSet<String> keySet = new HashSet<String>();
    PdfArray itemPairs = getBaseDataObject();
    for(
      int index = 0,
        length = itemPairs.size();
      index < length;
      index += 2
      )
    {
      keySet.add(
        (String)((PdfTextString)itemPairs.get(index)).getValue()
        );
    }

    return keySet;
  }

  public EmbeddedFile put(
    String key,
    EmbeddedFile value
    )
  {
    PdfArray itemPairs = getBaseDataObject();
    for(
      int index = 0,
        length = itemPairs.size();
      index < length;
      index += 2
      )
    {
      // Already existing entry?
      if(((PdfTextString)itemPairs.get(index)).getValue().equals(key))
      {
        EmbeddedFile oldEmbeddedFile = new EmbeddedFile(itemPairs.get(index+1));

        itemPairs.set(index+1,value.getBaseObject());

        return oldEmbeddedFile;
      }
    }

    // New entry.
    itemPairs.add(new PdfTextString(key));
    itemPairs.add(value.getBaseObject());

    return null;
  }

  public void putAll(
    Map<? extends String,? extends EmbeddedFile> entries
    )
  {throw new NotImplementedException();}

  public EmbeddedFile remove(
    Object key
    )
  {
    PdfArray itemPairs = getBaseDataObject();
    for(
      int index = 0,
        length = itemPairs.size();
      index < length;
      index += 2
      )
    {
      if(((PdfTextString)itemPairs.get(index)).getValue().equals(key))
      {
        itemPairs.remove(index); // Key removed.

        return new EmbeddedFile(itemPairs.remove(index)); // Value removed.
      }
    }

    return null;
  }

  public int size(
    )
  {return getBaseDataObject().size();}

  public Collection<EmbeddedFile> values(
    )
  {
    List<EmbeddedFile> values = new ArrayList<EmbeddedFile>();
    PdfArray itemPairs = getBaseDataObject();
    for(
      int index = 1,
        length = itemPairs.size();
      index < length;
      index += 2
      )
    {
      values.add(
        new EmbeddedFile(itemPairs.get(index))
        );
    }

    return values;
  }
  // </Map>
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}