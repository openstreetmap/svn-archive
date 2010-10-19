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

package it.stefanochizzolini.clown.documents.interaction.actions;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.documents.interaction.annotations.Annotation;
import it.stefanochizzolini.clown.documents.interaction.forms.Field;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfBoolean;
import it.stefanochizzolini.clown.objects.PdfDataObject;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfReference;
import it.stefanochizzolini.clown.objects.PdfTextString;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.util.ArrayList;
import java.util.Collection;

/**
  'Toggle the visibility of one or more annotations on the screen' action [PDF:1.6:8.5.3].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.7
  @since 0.0.7
*/
public class ToggleVisibility
  extends Action
{
  // <class>
  // <dynamic>
  // <constructors>
  /**
    Creates a new action within the given document context.
  */
  public ToggleVisibility(
    Document context,
    Collection<PdfObjectWrapper<?>> objects,
    boolean visible
    )
  {
    super(
      context,
      PdfName.Hide
      );

    setObjects(objects);
    setVisible(visible);
  }

  ToggleVisibility(
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
  public ToggleVisibility clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the annotations (or associated form fields) to be affected.
  */
  public Collection<PdfObjectWrapper<?>> getObjects(
    )
  {
    ArrayList<PdfObjectWrapper<?>> objects = new ArrayList<PdfObjectWrapper<?>>();

    /*
      NOTE: 'T' entry MUST exist.
    */
    PdfDirectObject objectsObject = getBaseDataObject().get(PdfName.T);
    fillObjects(objectsObject,objects);

    return objects;
  }

  /**
    Gets whether to show the annotations.
  */
  public boolean isVisible(
    )
  {
    /*
      NOTE: 'H' entry may be undefined.
    */
    PdfBoolean hideObject = (PdfBoolean)getBaseDataObject().get(PdfName.H);
    if(hideObject == null)
      return false;

    return !((Boolean)hideObject.getValue()).booleanValue();
  }

  /**
    @see #getObjects()
  */
  public void setObjects(
    Collection<PdfObjectWrapper<?>> value
    )
  {
    PdfArray objectsDataObject = new PdfArray();
    for(PdfObjectWrapper<?> item : value)
    {
      if(item instanceof Annotation)
        objectsDataObject.add(
          item.getBaseObject()
          );
      else if(item instanceof Field)
        objectsDataObject.add(
          new PdfTextString(((Field)item).getFullName())
          );
      else
        throw new IllegalArgumentException(
          "Invalid 'Hide' action target type (" + item.getClass().getName() + ").\n"
            + "It MUST be either an annotation or a form field."
          );
    }
    getBaseDataObject().put(PdfName.T, objectsDataObject);
  }

  /**
    @see #isVisible()
  */
  public void setVisible(
    boolean value
    )
  {getBaseDataObject().put(PdfName.H, new PdfBoolean(!value));}
  // </public>

  // <private>
  private void fillObjects(
    PdfDataObject objectObject,
    Collection<PdfObjectWrapper<?>> objects
    )
  {
    PdfDataObject objectDataObject = File.resolve(objectObject);
    if(objectDataObject instanceof PdfArray) // Multiple objects.
    {
      for(PdfDirectObject itemObject : (PdfArray)objectDataObject)
      {fillObjects(itemObject,objects);}
    }
    else // Single object.
    {
      if(objectDataObject instanceof PdfDictionary) // Annotation.
        objects.add(
          Annotation.wrap((PdfReference)objectObject)
          );
      else if(objectDataObject instanceof PdfTextString) // Form field (associated to widget annotations).
        objects.add(
          getDocument().getForm().getFields().get(
            (String)((PdfTextString)objectDataObject).getValue()
            )
          );
      else // Invalid object type.
        throw new RuntimeException(
          "Invalid 'Hide' action target type (" + objectDataObject.getClass().getName() + ").\n"
            + "It should be either an annotation or a form field."
          );
    }
  }
  // </private>
  // </interface>
  // </dynamic>
  // </class>
}