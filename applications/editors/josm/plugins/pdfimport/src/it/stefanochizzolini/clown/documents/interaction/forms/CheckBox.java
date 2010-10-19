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
import it.stefanochizzolini.clown.documents.interaction.annotations.Widget;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.util.NotImplementedException;

/**
  Check box field [PDF:1.6:8.6.3].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8.1
  @since 0.0.7
*/
public class CheckBox
  extends ButtonField
{
  // <class>
  // <dynamic>
  // <constructors>
  /**
    Creates a new checkbox within the given document context.
  */
  public CheckBox(
    String name,
    Widget widget,
    boolean checked
    )
  {
    super(
      name,
      widget
      );

    setChecked(checked);
  }

  public CheckBox(
    PdfDirectObject baseObject
    )
  {super(baseObject);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public CheckBox clone(
    Document context
    )
  {throw new NotImplementedException();}

  public boolean isChecked(
    )
  {
    PdfName value = (PdfName)getBaseDataObject().get(PdfName.V);

    return !(value == null
      || value.equals(PdfName.Off));
  }

  public void setChecked(
    boolean value
    )
  {
    PdfName baseValue = (value ? PdfName.Yes : PdfName.Off);
    getBaseDataObject().put(PdfName.V,baseValue);
    getWidgets().get(0).getBaseDataObject().put(PdfName.AS,baseValue);
  }

  @Override
  public void setValue(
    Object value
    )
  {setChecked(!value.equals("Off"));}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}