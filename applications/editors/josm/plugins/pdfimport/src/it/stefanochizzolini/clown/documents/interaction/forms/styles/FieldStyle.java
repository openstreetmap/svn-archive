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

package it.stefanochizzolini.clown.documents.interaction.forms.styles;

import it.stefanochizzolini.clown.documents.contents.colorSpaces.Color;
import it.stefanochizzolini.clown.documents.contents.colorSpaces.DeviceRGBColor;
import it.stefanochizzolini.clown.documents.interaction.forms.Field;

/**
  Abstract field appearance style.
  <p>It automates the definition of field appearance, applying a common look.</p>

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.7
  @since 0.0.7
*/
public abstract class FieldStyle
{
  // <dynamic>
  // <fields>
  private Color backColor = DeviceRGBColor.White;
  private char checkSymbol = (char)52;
  private double fontSize = 10;
  private Color foreColor = DeviceRGBColor.Black;
  private char radioSymbol = (char)108;
  // </fields>

  // <constructors>
  protected FieldStyle(
    )
  {}
  // </constructors>

  // <interface>
  // <public>
  public abstract void apply(
    Field field
    );

  public Color getBackColor(
    )
  {return backColor;}

  public char getCheckSymbol(
    )
  {return checkSymbol;}

  public double getFontSize(
    )
  {return fontSize;}

  public Color getForeColor(
    )
  {return foreColor;}

  public char getRadioSymbol(
    )
  {return radioSymbol;}

  public void setBackColor(
    Color value
    )
  {backColor = value;}

  public void setCheckSymbol(
    char value
    )
  {checkSymbol = value;}

  public void setFontSize(
    double value
    )
  {fontSize = value;}

  public void setForeColor(
    Color value
    )
  {foreColor = value;}

  public void setRadioSymbol(
    char value
    )
  {radioSymbol = value;}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}