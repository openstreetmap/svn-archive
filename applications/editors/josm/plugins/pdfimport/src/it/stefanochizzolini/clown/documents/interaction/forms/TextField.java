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
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfInteger;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfTextString;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.util.EnumSet;

/**
  Text field [PDF:1.6:8.6.3].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.7
  @since 0.0.7
*/
public class TextField
  extends Field
{
  // <class>
  // <dynamic>
  // <constructors>
  /**
    Creates a new text field within the given document context.
  */
  public TextField(
    String name,
    Widget widget,
    String value
    )
  {
    super(
      PdfName.Tx,
      name,
      widget
      );

    setValue(value);
  }

  public TextField(
    PdfDirectObject baseObject
    )
  {super(baseObject);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public TextField clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the maximum length of the field's text, in characters.
  */
  public int getMaxLength(
    )
  {
    PdfInteger maxLengthObject = (PdfInteger)File.resolve(
      getInheritableAttribute(PdfName.MaxLen)
      );
    if(maxLengthObject == null)
      return Integer.MAX_VALUE;//TODO:verify!!!

    return maxLengthObject.getRawValue();
  }

  /**
    Gets whether the field can contain multiple lines of text.
  */
  public boolean isMultiline(
    )
  {return getFlags().contains(FlagsEnum.Multiline);}

  /**
    Gets whether the field is intended for entering a secure password.
  */
  public boolean isPassword(
    )
  {return getFlags().contains(FlagsEnum.Password);}

  /**
    Gets whether text entered in the field is spell-checked.
  */
  public boolean isSpellChecked(
    )
  {return !getFlags().contains(FlagsEnum.DoNotSpellCheck);}

  /**
    @see #getMaxLength()
  */
  public void setMaxLength(
    int value
    )
  {throw new NotImplementedException();}

  /**
    @see #isMultiline()
  */
  public void setMultiline(
    boolean value
    )
  {
    EnumSet<FlagsEnum> flags = getFlags();
    if(value)
    {flags.add(FlagsEnum.Multiline);}
    else
    {flags.remove(FlagsEnum.Multiline);}
    setFlags(flags);
  }

  /**
    @see #isPassword()
  */
  public void setPassword(
    boolean value
    )
  {
    EnumSet<FlagsEnum> flags = getFlags();
    if(value)
    {flags.add(FlagsEnum.Password);}
    else
    {flags.remove(FlagsEnum.Password);}
    setFlags(flags);
  }

  /**
    @see #isSpellChecked()
  */
  public void setSpellChecked(
    boolean value
    )
  {
    EnumSet<FlagsEnum> flags = getFlags();
    if(value)
    {flags.remove(FlagsEnum.DoNotSpellCheck);}
    else
    {flags.add(FlagsEnum.DoNotSpellCheck);}
    setFlags(flags);
  }

  @Override
  public void setValue(
    Object value
    )
  {
    getBaseDataObject().put(PdfName.V,new PdfTextString((String)value));
    getBaseDataObject().put(PdfName.DV,new PdfTextString((String)value));
  }
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}