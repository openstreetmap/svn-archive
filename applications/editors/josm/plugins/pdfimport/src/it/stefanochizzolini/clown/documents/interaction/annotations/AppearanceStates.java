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

package it.stefanochizzolini.clown.documents.interaction.annotations;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.documents.contents.xObjects.FormXObject;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.objects.PdfDataObject;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfStream;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
  Appearance states [PDF:1.6:8.4.4].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.7
  @version 0.0.7
*/
public class AppearanceStates
  extends PdfObjectWrapper<PdfDataObject>
  implements Map<PdfName,FormXObject>
{
  // <class>
  // <classes>
  private class Entry
    implements Map.Entry<PdfName,FormXObject>
  {
    // <class>
    // <dynamic>
    // <fields>
    private final PdfName key;
    private final FormXObject value;
    // </fields>

    // <constructors>
    private Entry(
      PdfName key,
      FormXObject value
      )
    {
      this.key = key;
      this.value = value;
    }
    // </constructors>

    // <interface>
    // <public>
    // <Map.Entry>
    public PdfName getKey(
      )
    {return key;}

    public FormXObject getValue(
      )
    {return value;}

    public FormXObject setValue(
      FormXObject value
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
  // <fields>
  private Appearance appearance;

  private PdfName statesKey;
  // </fields>

  // <constructors>
  AppearanceStates(
    PdfName statesKey,
    PdfIndirectObject container,
    Appearance appearance
    )
  {
    super(
      appearance.getBaseDataObject().get(statesKey),
      container
      );

    this.appearance = appearance;
    this.statesKey = statesKey;
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public AppearanceStates clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the appearance associated to these states.
  */
  public Appearance getAppearance(
    )
  {return appearance;}

//TODO
  /**
    Gets the key associated to a given value.
  */
//   public PdfName getKey(
//     FormXObject value
//     )
//   {return getBaseDataObject().getKey(value.getBaseObject());}

  // <Map>
  public void clear(
    )
  {ensureDictionary().clear();}

  public boolean containsKey(
    Object key
    )
  {
    PdfDataObject baseDataObject = getBaseDataObject();
    if(baseDataObject == null) // No state.
      return false;
    if(baseDataObject instanceof PdfStream) // Single state.
      return (key == null);
    // Multiple state.
    return ((PdfDictionary)baseDataObject).containsKey(key);
  }

  public boolean containsValue(
    Object value
    )
  {
    if(!(value instanceof FormXObject))
      return false;

    PdfDataObject baseDataObject = getBaseDataObject();
    if(baseDataObject == null) // No state.
      return false;
    if(baseDataObject instanceof PdfStream) // Single state.
      return ((FormXObject)value).getBaseObject().equals(getBaseObject());
    // Multiple state.
    return ((PdfDictionary)baseDataObject).containsValue(
      ((FormXObject)value).getBaseObject()
      );
  }

  public Set<Map.Entry<PdfName,FormXObject>> entrySet(
    )
  {
    HashSet<Map.Entry<PdfName,FormXObject>> entrySet = new HashSet<Map.Entry<PdfName,FormXObject>>();

    PdfDataObject baseDataObject = getBaseDataObject();
    if(baseDataObject == null) // No state.
    { /* NOOP. */ }
    else if(baseDataObject instanceof PdfStream) // Single state.
    {
      entrySet.add(
        new Entry(
          null,
          new FormXObject(getBaseObject())
          )
        );
    }
    else // Multiple state.
    {
      for(Map.Entry<PdfName,PdfDirectObject> entry : ((PdfDictionary)baseDataObject).entrySet())
      {
        entrySet.add(
          new Entry(
            entry.getKey(),
            new FormXObject(entry.getValue())
            )
          );
      }
    }

    return entrySet;
  }

  public boolean equals(
    PdfDirectObject object
    )
  {throw new NotImplementedException();}

  public FormXObject get(
    Object key
    )
  {
    PdfDataObject baseDataObject = getBaseDataObject();
    if(baseDataObject == null) // No state.
      return null;

    if(key == null)
    {
      if(baseDataObject instanceof PdfStream) // Single state.
        return new FormXObject(getBaseObject());
      // Multiple state, but invalid key.
      return null;
    }
    // Multiple state.
    return new FormXObject(((PdfDictionary)baseDataObject).get(key));
  }

  public int hashCode(
    )
  {throw new NotImplementedException();}

  public boolean isEmpty(
    )
  {
    PdfDataObject baseDataObject = getBaseDataObject();
    if(baseDataObject == null) // No state.
      return true;
    if(baseDataObject instanceof PdfStream) // Single state.
      return false;
    // Multiple state.
    return ((PdfDictionary)baseDataObject).isEmpty();
  }

  public Set<PdfName> keySet(
    )
  {throw new NotImplementedException();}

  public FormXObject put(
    PdfName key,
    FormXObject value
    )
  {
    PdfDirectObject previousValue;
    if(key == null) // Single state.
    {
      setBaseObject(value.getBaseObject());
      previousValue = appearance.getBaseDataObject().put(statesKey,getBaseObject());
    }
    else // Multiple state.
    {previousValue = ensureDictionary().put(key,value.getBaseObject());}

    if(File.resolve(previousValue) instanceof PdfStream)
      return new FormXObject(previousValue);

    return null;
  }

  public void putAll(
    Map<? extends PdfName,? extends FormXObject> entries
    )
  {throw new NotImplementedException();}

  public FormXObject remove(
    Object key
    )
  {
    PdfDataObject baseDataObject = getBaseDataObject();
    if(baseDataObject == null) // No state.
      return null;

    PdfDirectObject previousValue;
    if(baseDataObject instanceof PdfStream) // Single state.
    {
      if(key == null)
      {
        setBaseObject(null);
        previousValue = appearance.getBaseDataObject().remove(statesKey);
      }
      else // Invalid key.
      {previousValue = null;}
    }
    else // Multiple state.
    {previousValue = ((PdfDictionary)baseDataObject).remove(key);}

    if(File.resolve(previousValue) instanceof PdfStream)
      return new FormXObject(previousValue);

    return null;
  }

  public int size(
    )
  {
    PdfDataObject baseDataObject = getBaseDataObject();
    if(baseDataObject == null) // No state.
      return 0;
    if(baseDataObject instanceof PdfStream) // Single state.
      return 1;
    // Multiple state.
    return ((PdfDictionary)baseDataObject).size();
  }

  public Collection<FormXObject> values(
    )
  {throw new NotImplementedException();}
  // </Map>
  // </public>

  // <private>
  private PdfDictionary ensureDictionary(
    )
  {
    PdfDataObject baseDataObject = getBaseDataObject();
    if(!(baseDataObject instanceof PdfDictionary))
    {
      /*
        NOTE: Single states are erased as they have no valid key
        to be consistently integrated within the dictionary.
      */
      setBaseObject((PdfDirectObject)(baseDataObject = new PdfDictionary()));
      appearance.getBaseDataObject().put(statesKey,(PdfDictionary)baseDataObject);
    }

    return (PdfDictionary)baseDataObject;
  }
  // </private>
  // </interface>
  // </dynamic>
  // </class>
}