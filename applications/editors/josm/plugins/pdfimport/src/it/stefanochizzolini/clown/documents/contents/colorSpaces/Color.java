/*
  Copyright 2006-2008 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.documents.contents.colorSpaces;

/**
  Color value [PDF:1.6:4.5.1].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.6
*/
public abstract class Color
{
  // <class>
  // <static>
  // <interface>
  // <protected>
  /**
    Gets the normalized value of a color component [PDF:1.6:4.5.1].
    @param value Color component value to normalize.
    @return Normalized color component value.
  */
  /*
    NOTE: Further developments may result in a color-space family-specific
    implementation of this method; currently this implementation focuses on
    device colors only.
  */
  protected static double normalizeComponent(
    double value
    )
  {
    if(value < 0)
      return 0d;
    else if(value > 1)
      return 1d;

    return value;
  }
  // </protected>
  // </interface>
  // </static>

  // <dynamic>
  // <fields>
  private ColorSpace colorSpace;
  // </fields>

  // <constructors>
  protected Color(
    ColorSpace colorSpace
    )
  {this.colorSpace = colorSpace;}
  // </constructors>

  // <interface>
  // <internal>
  public ColorSpace getColorSpace(
    )
  {return colorSpace;}

  public abstract double[] getComponents(
    );
  // </internal>
  // </interface>
  // </dynamic>
  // </class>
}