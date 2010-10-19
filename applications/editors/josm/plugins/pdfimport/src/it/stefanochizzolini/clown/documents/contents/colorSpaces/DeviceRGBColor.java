/*
  Copyright 2006-2009 Stefano Chizzolini. http://clown.stefanochizzolini.it

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
  Device Red-Green-Blue color value [PDF:1.6:4.5.3].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
*/
public class DeviceRGBColor
  extends DeviceColor
{
  // <class>
  // <static>
  // <fields>
  public static final DeviceRGBColor Black = new DeviceRGBColor(0,0,0);
  public static final DeviceRGBColor White = new DeviceRGBColor(1,1,1);

  public static final DeviceRGBColor Default = Black;
  // </fields>
  // </static>

  // <dynamic>
  // <fields>
  private double blueComponent;
  private double greenComponent;
  private double redComponent;
  // </fields>

  // <constructors>
  public DeviceRGBColor(
    double redComponent,
    double greenComponent,
    double blueComponent
    )
  {
    super(DeviceRGBColorSpace.Default);

    setRedComponent(redComponent);
    setGreenComponent(greenComponent);
    setBlueComponent(blueComponent);
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public boolean equals(
    Object object
    )
  {
    if(!(object instanceof DeviceRGBColor))
      return false;

    DeviceRGBColor other = (DeviceRGBColor)object;

    return other.redComponent == this.redComponent
      && other.greenComponent == this.greenComponent
      && other.blueComponent == this.blueComponent;
  }

  /**
    Gets the blue component.
  */
  public double getBlueComponent(
    )
  {return blueComponent;}

  @Override
  public double[] getComponents(
    )
  {return new double[]{redComponent,greenComponent,blueComponent};}

  /**
    Gets the green component.
  */
  public double getGreenComponent(
    )
  {return greenComponent;}

  /**
    Gets the red component.
  */
  public double getRedComponent(
    )
  {return redComponent;}

  @Override
  public int hashCode(
    )
  {return new Double(redComponent).hashCode() ^ new Double(greenComponent).hashCode() ^ new Double(blueComponent).hashCode();}

  /**
    Sets the blue component.
  */
  public void setBlueComponent(
    double value
    )
  {blueComponent = normalizeComponent(value);}

  /**
    Sets the green component.
  */
  public void setGreenComponent(
    double value
    )
  {greenComponent = normalizeComponent(value);}

  /**
    Sets the red component.
  */
  public void setRedComponent(
    double value
    )
  {redComponent = normalizeComponent(value);}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}