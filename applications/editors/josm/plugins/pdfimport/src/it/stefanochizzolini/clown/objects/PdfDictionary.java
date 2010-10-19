/*
  Copyright 2006-2009 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
  PDF dictionary object [PDF:1.6:3.2.6].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.0
*/
public class PdfDictionary
  extends PdfDirectObject
  implements Map<PdfName,PdfDirectObject>
{
  // <class>
  // <dynamic>
  // <fields>
  private HashMap<PdfName,PdfDirectObject> entries;
  // </fields>

  // <constructors>
  public PdfDictionary(
    )
  {entries = new HashMap<PdfName,PdfDirectObject>();}

  public PdfDictionary(
    int capacity
    )
  {entries = new HashMap<PdfName,PdfDirectObject>(capacity);}

  public PdfDictionary(
    PdfName[] keys,
    PdfDirectObject[] values
    )
  {
    this(values.length);

    for(
      int index = 0;
      index < values.length;
      index++
      )
    {
      put(
        keys[index],
        values[index]
        );
    }
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
    PdfDictionary clone = new PdfDictionary(entries.size());

    for(
      Map.Entry<PdfName,PdfDirectObject> entry : entries.entrySet()
      )
    {
      clone.put(
        entry.getKey(),
        (PdfDirectObject)entry.getValue().clone(context)
        );
    }

    return clone;
  }

  @Override
  public int compareTo(
    PdfDirectObject obj
    )
  {throw new NotImplementedException();}

  /**
    Gets the key associated to a given value.
  */
  public PdfName getKey(
    PdfDirectObject value
    )
  {
    /*
      NOTE: Current PdfDictionary implementation doesn't support bidirectional
      maps, to say that the only currently-available way to retrieve a key from a
      value is to iterate the whole map (really poor performance!).
      NOTE: Complex high-level matches are not verified (too expensive!), to say that
      if the searched high-level object (font, xobject, colorspace etc.) has a
      PdfReference base object while some high-level objects in the
      collection have other direct type (PdfName, for example) base objects, they
      won't match in any case (even if they represent the SAME high-level object --
      but that should be a rare case...).
    */
    for(
      Map.Entry<PdfName,PdfDirectObject> entry : entries.entrySet()
      )
    {
      if(entry.getValue().equals(value))
        return entry.getKey(); // Found.
    }

    return null; // Not found.
  }

  /**
    Gets the dereferenced value corresponding to the given key.
    <h3>Remarks</h3>
    <p>This method takes care to resolve the value returned by {@link #get(Object)}.</p>

    @param key Key whose associated value is to be returned.
    @return null, if the map contains no mapping for this key.

    @since 0.0.8
   */
  public PdfDataObject resolve(
    Object key
    )
  {return File.resolve(get(key));}

  @Override
  public String toString(
    )
  {
    StringBuilder buffer = new StringBuilder();

    // Begin.
    buffer.append("<< ");

    // Entries.
    for(
      Map.Entry<PdfName,PdfDirectObject> entry : entries.entrySet()
      )
    {
      // Entry...
      // ...key.
      buffer.append(entry.getKey().toString()); buffer.append(" ");

      // ...value.
      buffer.append(PdfDirectObject.toString(entry.getValue())); buffer.append(" ");
    }

    // End.
    buffer.append(">>");

    return buffer.toString();
  }

  @Override
  public void writeTo(
    IOutputStream stream
    )
  {
    // Begin.
    stream.write("<< ");

    // Entries.
    for(
      Map.Entry<PdfName,PdfDirectObject> entry : entries.entrySet()
      )
    {
      // Entry...
      // ...key.
      entry.getKey().writeTo(stream); stream.write(" ");

      // ...value.
      PdfDirectObject.writeTo(stream,entry.getValue()); stream.write(" ");
    }

    // End.
    stream.write(">>");
  }

  // <Map>
  public void clear(
    )
  {entries.clear();}

  public boolean containsKey(
    Object key
    )
  {return entries.containsKey(key);}

  public boolean containsValue(
    Object value
    )
  {return entries.containsValue(value);}

  public Set<Map.Entry<PdfName,PdfDirectObject>> entrySet(
    )
  {return entries.entrySet();}

  public boolean equals(
    Object object
    )
  {
    return (object instanceof PdfDictionary
      && this.entrySet().equals(((PdfDictionary)object).entrySet()));
  }

  public PdfDirectObject get(
    Object key
    )
  {return entries.get(key);}

  public int hashCode(
    )
  {return entries.hashCode();}

  public boolean isEmpty(
    )
  {return entries.isEmpty();}

  public Set<PdfName> keySet(
    )
  {return entries.keySet();}

  public PdfDirectObject put(
    PdfName key,
    PdfDirectObject value
    )
  {return entries.put(key,value);}

  public void putAll(
    Map<? extends PdfName,? extends PdfDirectObject> entries
    )
  {this.entries.putAll(entries);}

  public PdfDirectObject remove(
    Object key
    )
  {return entries.remove(key);}

  public int size(
    )
  {return entries.size();}

  public Collection<PdfDirectObject> values(
    )
  {return entries.values();}
  // </Map>
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}