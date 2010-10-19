/*
  Copyright 2007 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.util.math.geom;

import java.awt.geom.Dimension2D;

/**
  Double-precision 2D dimension.

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
*/
public final class Dimension
  extends Dimension2D
{
  // <class>
  // <dynamic>
  // <fields>
  private double height;
  private double width;
  // </fields>

  // <constructors>
  public Dimension(
    double width,
    double height
    )
  {
    this.width = width;
    this.height = height;
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public double getHeight(
    )
  {return height;}

  @Override
  public double getWidth(
    )
  {return width;}

  @Override
  public void setSize(
    double width,
    double height
    )
  {
    this.width = width;
    this.height = height;
  }
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}