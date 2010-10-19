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
  Device Cyan-Magenta-Yellow-Key color value [PDF:1.6:4.5.3].
  <h3>Remarks</h3>
  <p>The 'Key' component is renamed 'Black' to avoid semantic ambiguities.</p>

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.4
*/
public class DeviceCMYKColor
  extends DeviceColor
{
  // <class>
  // <static>
  // <fields>
  public static final DeviceCMYKColor Black = new DeviceCMYKColor(0,0,0,1);
  public static final DeviceCMYKColor White = new DeviceCMYKColor(0,0,0,0);

  public static final DeviceCMYKColor Default = Black;
  // </fields>
  // </static>

  // <dynamic>
  // <fields>
  private double blackComponent;
  private double cyanComponent;
  private double magentaComponent;
  private double yellowComponent;
  // </fields>

  // <constructors>
  public DeviceCMYKColor(
    double cyanComponent,
    double magentaComponent,
    double yellowComponent,
    double blackComponent
    )
  {
    super(DeviceCMYKColorSpace.Default);

    setCyanComponent(cyanComponent);
    setMagentaComponent(magentaComponent);
    setYellowComponent(yellowComponent);
    setBlackComponent(blackComponent);
  }
  // </constructors>

  // <interface>
  // <public>
  /**
    Gets the black (key) component.
  */
  public double getBlackComponent(
    )
  {return blackComponent;}

  @Override
  public double[] getComponents(
    )
  {return new double[]{cyanComponent,magentaComponent,yellowComponent,blackComponent};}

  /**
    Gets the cyan component.
  */
  public double getCyanComponent(
    )
  {return cyanComponent;}

  /**
    Gets the magenta component.
  */
  public double getMagentaComponent(
    )
  {return magentaComponent;}

  /**
    Gets the yellow component.
  */
  public double getYellowComponent(
    )
  {return yellowComponent;}

  /**
    Sets the black (key) component.
  */
  public void setBlackComponent(
    double value
    )
  {blackComponent = normalizeComponent(value);}

  /**
    Sets the cyan component.
  */
  public void setCyanComponent(
    double value
    )
  {cyanComponent = normalizeComponent(value);}

  /**
    Sets the magenta component.
  */
  public void setMagentaComponent(
    double value
    )
  {magentaComponent = normalizeComponent(value);}

  /**
    Sets the yellow component.
  */
  public void setYellowComponent(
    double value
    )
  {yellowComponent = normalizeComponent(value);}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}