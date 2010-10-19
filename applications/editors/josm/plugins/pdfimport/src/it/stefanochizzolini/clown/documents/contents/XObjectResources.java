/*
  Copyright 2006 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.documents.contents.xObjects.XObject;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfReference;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
  External object resources collection [PDF:1.6:3.7.2].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.7
*/
public class XObjectResources
  extends PdfObjectWrapper<PdfDictionary>
  implements Map<PdfName,XObject>
{
  // <class>
  // <dynamic>
  // <constructors>
  public XObjectResources(
    Document context
    )
  {
    super(
      context.getFile(),
      new PdfDictionary()
      );
  }

  XObjectResources(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {
    super(
      baseObject,
      container
      );
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public XObjectResources clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the key associated to a given value.
  */
  public PdfName getKey(
    XObject value
    )
  {return getBaseDataObject().getKey(value.getBaseObject());}

  // <Map>
  public void clear(
    )
  {getBaseDataObject().clear();}

  public boolean containsKey(
    Object key
    )
  {return getBaseDataObject().containsKey(key);}

  public boolean containsValue(
    Object value
    )
  {return getBaseDataObject().containsValue(((XObject)value).getBaseObject());}

  public Set<Map.Entry<PdfName,XObject>> entrySet(
    )
  {throw new NotImplementedException();}

  public boolean equals(
    PdfDirectObject object
    )
  {throw new NotImplementedException();}

  public XObject get(
    Object key
    )
  {
    return XObject.wrap(
      (PdfReference)getBaseDataObject().get(key)
      );
  }

  public int hashCode(
    )
  {throw new NotImplementedException();}

  public boolean isEmpty(
    )
  {return getBaseDataObject().isEmpty();}

  public Set<PdfName> keySet(
    )
  {return getBaseDataObject().keySet();}

  public XObject put(
    PdfName key,
    XObject value
    )
  {
    return XObject.wrap(
      (PdfReference)getBaseDataObject().put(key,value.getBaseObject())
      );
  }

  public void putAll(
    Map<? extends PdfName,? extends XObject> entries
    )
  {throw new NotImplementedException();}

  public XObject remove(
    Object key
    )
  {
    return XObject.wrap(
      (PdfReference)getBaseDataObject().remove(key)
      );
  }

  public int size(
    )
  {return getBaseDataObject().size();}

  public Collection<XObject> values(
    )
  {
    // Get the raw objects!
    Collection<PdfDirectObject> objects = getBaseDataObject().values();
    // Get room for the corresponding Font collection!
    Collection<XObject> values = new ArrayList<XObject>(objects.size());
    // Populating the collection...
    for(PdfDirectObject object : objects)
    {values.add(XObject.wrap((PdfReference)object));}

    return values;
  }
  // </Map>
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}