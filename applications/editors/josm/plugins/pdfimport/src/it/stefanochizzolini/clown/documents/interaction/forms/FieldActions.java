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
import it.stefanochizzolini.clown.documents.interaction.actions.Action;
import it.stefanochizzolini.clown.documents.interaction.actions.JavaScript;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.util.NotImplementedException;

/**
  Form field actions [PDF:1.6:8.5.2].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.7
  @since 0.0.7
*/
public class FieldActions
  extends PdfObjectWrapper<PdfDictionary>
{
  // <class>
  // <dynamic>
  // <constructors>
  public FieldActions(
    Document context
    )
  {
    super(
      context.getFile(),
      new PdfDictionary()
      );
  }

  public FieldActions(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {super(baseObject,container);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public FieldActions clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets a JavaScript action to be performed to recalculate the value
    of this field when that of another field changes.
  */
  public JavaScript getOnCalculate(
    )
  {
    /*
      NOTE: 'C' entry may be undefined.
    */
    PdfDirectObject onCalculateObject = getBaseDataObject().get(PdfName.C);
    if(onCalculateObject == null)
      return null;

    return (JavaScript)Action.wrap(onCalculateObject,getContainer());
  }

  /**
    Gets a JavaScript action to be performed when the user types a keystroke
    into a text field or combo box or modifies the selection in a scrollable list box.
  */
  public JavaScript getOnChange(
    )
  {
    /*
      NOTE: 'K' entry may be undefined.
    */
    PdfDirectObject onChangeObject = getBaseDataObject().get(PdfName.K);
    if(onChangeObject == null)
      return null;

    return (JavaScript)Action.wrap(onChangeObject,getContainer());
  }

  /**
    Gets a JavaScript action to be performed before the field is formatted
    to display its current value.
    <p>This action can modify the field's value before formatting.</p>
  */
  public JavaScript getOnFormat(
    )
  {
    /*
      NOTE: 'F' entry may be undefined.
    */
    PdfDirectObject onFormatObject = getBaseDataObject().get(PdfName.F);
    if(onFormatObject == null)
      return null;

    return (JavaScript)Action.wrap(onFormatObject,getContainer());
  }

  /**
    Gets a JavaScript action to be performed when the field's value is changed.
    This action can check the new value for validity.
  */
  public JavaScript getOnValidate(
    )
  {
    /*
      NOTE: 'V' entry may be undefined.
    */
    PdfDirectObject onValidateObject = getBaseDataObject().get(PdfName.V);
    if(onValidateObject == null)
      return null;

    return (JavaScript)Action.wrap(onValidateObject,getContainer());
  }

  /**
    @see #getOnCalculate()
  */
  public void setOnCalculate(
    JavaScript value
    )
  {getBaseDataObject().put(PdfName.C, value.getBaseObject());}

  /**
    @see #getOnChange()
  */
  public void setOnChange(
    JavaScript value
    )
  {getBaseDataObject().put(PdfName.K, value.getBaseObject());}

  /**
    @see #getOnFormat()
  */
  public void setOnFormat(
    JavaScript value
    )
  {getBaseDataObject().put(PdfName.F, value.getBaseObject());}

  /**
    @see #getOnValidate()
  */
  public void setOnValidate(
    JavaScript value
    )
  {getBaseDataObject().put(PdfName.V, value.getBaseObject());}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}