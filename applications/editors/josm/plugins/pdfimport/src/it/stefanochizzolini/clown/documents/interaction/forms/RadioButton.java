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
import it.stefanochizzolini.clown.documents.interaction.annotations.DualWidget;
import it.stefanochizzolini.clown.documents.interaction.annotations.Widget;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.util.EnumSet;

/**
  Radio button field [PDF:1.6:8.6.3].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.7
  @since 0.0.7
*/
public class RadioButton
  extends ButtonField
{
  // <class>
  // <dynamic>
  // <constructors>
  /**
    Creates a new radiobutton within the given document context.
  */
  public RadioButton(
    String name,
    DualWidget[] widgets,
    String value
    )
  {
    super(
      name,
      widgets[0]
      );

    EnumSet<FlagsEnum> flags = getFlags(); flags.add(FlagsEnum.Radio); flags.add(FlagsEnum.NoToggleToOff); setFlags(flags);

    FieldWidgets fieldWidgets = getWidgets();
    for(
      int index = 1,
        length = widgets.length;
      index < length;
      index++
      )
    {fieldWidgets.add(widgets[index]);}

    setValue(value);
  }

  public RadioButton(
    PdfDirectObject baseObject
    )
  {super(baseObject);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public RadioButton clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets whether all the field buttons can be deselected at the same time.
  */
  public boolean isToggleable(
    )
  {return !getFlags().contains(FlagsEnum.NoToggleToOff);}

  /**
    @see #isToggleable()
  */
  public void setToggleable(
    boolean value
    )
  {
    EnumSet<FlagsEnum> flags = getFlags();
    if(value)
    {flags.remove(FlagsEnum.NoToggleToOff);}
    else
    {flags.add(FlagsEnum.NoToggleToOff);}
    setFlags(flags);
  }

  @Override
  public void setValue(
    Object value
    )
  {
    PdfName selectedWidgetName = new PdfName((String)value);
    boolean selected = false;
    // Selecting the current appearance state for each widget...
    for(Widget widget : getWidgets())
    {
      PdfName currentState;
      if(((DualWidget)widget).getWidgetName().equals(value)) // Selected state.
      {
        selected = true;
        currentState = selectedWidgetName;
      }
      else // Unselected state.
      {currentState = PdfName.Off;}

      widget.getBaseDataObject().put(PdfName.AS,currentState);
    }
    // Select the current widget!
    getBaseDataObject().put(PdfName.V, selected ? selectedWidgetName : null);
  }
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}