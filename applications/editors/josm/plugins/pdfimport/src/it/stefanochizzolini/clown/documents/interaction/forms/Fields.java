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

package it.stefanochizzolini.clown.documents.interaction.forms;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfReference;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.Set;

/**
  Interactive form fields [PDF:1.6:8.6.1].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.7
  @since 0.0.7
*/
public class Fields
  extends PdfObjectWrapper<PdfArray>
  implements Map<String,Field>
{
  // <class>
  // <dynamic>
  // <constructors>
  public Fields(
    Document context
    )
  {
    super(
      context.getFile(),
      new PdfArray()
      );
  }

  Fields(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {super(baseObject,container);}
  // </constructors>

  // <interface>
  // <public>
  public boolean add(
    Field value
    )
  {
    getBaseDataObject().add(value.getBaseObject());

    return true;
  }

  @Override
  public Fields clone(
    Document context
    )
  {throw new NotImplementedException();}

  // <Map>
  public void clear(
    )
  {
    //TODO:verify whether to recursively unregister the fields!!!
    getBaseDataObject().clear();
  }

  public boolean containsKey(
    Object key
    )
  {throw new NotImplementedException();}

  public boolean containsValue(
    Object value
    )
  {throw new NotImplementedException();}

  public Set<Map.Entry<String,Field>> entrySet(
    )
  {throw new NotImplementedException();}

  public boolean equals(
    PdfDirectObject object
    )
  {throw new NotImplementedException();}

  public Field get(
    Object key
    )
  {throw new NotImplementedException();}

  public int hashCode(
    )
  {throw new NotImplementedException();}

  public boolean isEmpty(
    )
  {
    throw new NotImplementedException();
  //TODO:verify whether the fields collection is empty!!!
//     return getBaseDataObject().isEmpty();
  }

  public Set<String> keySet(
    )
  {
    throw new NotImplementedException();
  //TODO: retrieve all the full names (keys)!!!
//     return getBaseDataObject().keySet();
  }

  public Field put(
    String key,
    Field value
    )
  {
    throw new NotImplementedException();
/*
TODO:put the field into the correct position, based on the full name (key)!!!
  return Field.wrap(
      getBaseDataObject().put(key,value.getBaseObject())
      );*/
  }

  public void putAll(
    Map<? extends String,? extends Field> entries
    )
  {throw new NotImplementedException();}

  public Field remove(
    Object key
    )
  {
    throw new NotImplementedException();
/*
TODO:search through the full name (key)!
    return Field.wrap(
      getBaseDataObject().remove(key)
      );*/
  }

  public int size(
    )
  {return getBaseDataObject().size();}

  public Collection<Field> values(
    )
  {
    List<Field> values = new ArrayList<Field>();
    retrieveValues(getBaseDataObject(), values);

    return values;
  }
  // </Map>
  // </public>

  // <private>
  private void retrieveValues(
    PdfArray fieldObjects,
    List<Field> values
    )
  {
    for(
      PdfDirectObject fieldObject : fieldObjects
      )
    {
      PdfReference fieldReference = (PdfReference)fieldObject;
      PdfArray kidReferences = (PdfArray)File.resolve(
        ((PdfDictionary)fieldReference.getDataObject()).get(PdfName.Kids)
        );
      PdfDictionary kidObject;
      if(kidReferences == null)
      {kidObject = null;}
      else
      {kidObject = (PdfDictionary)((PdfReference)kidReferences.get(0)).getDataObject();}
      // Terminal field?
      if(kidObject == null // Merged single widget annotation.
        || (!kidObject.containsKey(PdfName.FT) // Multiple widget annotations.
          && kidObject.containsKey(PdfName.Subtype)
          && kidObject.get(PdfName.Subtype).equals(PdfName.Widget)))
      {values.add(Field.wrap(fieldReference));}
      else // Non-terminal field.
      {retrieveValues(kidReferences, values);}
    }
  }
  // </private>
  // </interface>
  // </dynamic>
  // </class>
}