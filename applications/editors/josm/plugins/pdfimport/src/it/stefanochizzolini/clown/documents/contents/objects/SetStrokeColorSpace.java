/*
  Copyright 2007-2010 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.documents.contents.objects;

import it.stefanochizzolini.clown.documents.contents.ContentScanner;
import it.stefanochizzolini.clown.documents.contents.colorSpaces.DeviceCMYKColorSpace;
import it.stefanochizzolini.clown.documents.contents.colorSpaces.DeviceGrayColorSpace;
import it.stefanochizzolini.clown.documents.contents.colorSpaces.DeviceRGBColorSpace;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfName;

import java.util.List;

/**
  'Set the current color space to use for stroking operations' operation [PDF:1.6:4.5.7].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.4
  @version 0.0.8
*/
public final class SetStrokeColorSpace
  extends Operation
{
  // <class>
  // <static>
  // <fields>
  public static final String Operator = "CS";
  // </fields>
  // </static>

  // <dynamic>
  // <constructors>
  public SetStrokeColorSpace(
    PdfName name
    )
  {super(Operator,name);}

  public SetStrokeColorSpace(
    List<PdfDirectObject> operands
    )
  {super(Operator,operands);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public void applyTo(
    ContentScanner.GraphicsState state
    )
  {
    /*
      NOTE: The names DeviceGray, DeviceRGB, DeviceCMYK, and Pattern always identify
      the corresponding color spaces directly; they never refer to resources in the
      ColorSpace subdictionary [PDF:1.6:4.5.7].
    */
    PdfName name = getName();
    if(name.equals(PdfName.DeviceGray))
    {state.strokeColorSpace = DeviceGrayColorSpace.Default;}
    else if(name.equals(PdfName.DeviceRGB))
    {state.strokeColorSpace = DeviceRGBColorSpace.Default;}
    else if(name.equals(PdfName.DeviceCMYK))
    {state.strokeColorSpace = DeviceCMYKColorSpace.Default;}
//TODO:special color spaces[PDF:1.6:4.5.5]!!!
//       else if(name.equals(PdfName.Pattern))
//       {state.strokeColorSpace = Pattern.Default;}
    else
    {state.strokeColorSpace = state.getScanner().getContentContext().getResources().getColorSpaces().get(name);}
//TODO:eliminate when full support to color spaces!!!
if(state.strokeColorSpace != null)
{
          /*
            NOTE: The operation also sets the current stroking color
            to its initial value, which depends on the color space [PDF:1.6:4.5.7].
          */
          state.strokeColor = state.strokeColorSpace.getDefaultColor();
}
  }

  public PdfName getName(
    )
  {return (PdfName)operands.get(0);}

  /**
    @since 0.0.6
  */
  public void setName(
    PdfName value
    )
  {operands.set(0,value);}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}