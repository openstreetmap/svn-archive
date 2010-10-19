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
import it.stefanochizzolini.clown.documents.contents.LineDash;
import it.stefanochizzolini.clown.objects.IPdfNumber;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfReal;

import java.util.List;

/**
  'Set the line dash pattern' operation [PDF:1.6:4.3.3].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.4
  @version 0.0.8
*/
public final class SetLineDash
  extends Operation
{
  // <class>
  // <static>
  // <fields>
  public static final String Operator = "d";
  // </fields>
  // </static>

  // <dynamic>
  // <constructors>
  public SetLineDash(
    double phase,
    double unitsOn,
    double unitsOff
    )
  {
    super(
      Operator,
      new PdfArray(
        new PdfReal(unitsOn),
        new PdfReal(unitsOff)
        ),
      new PdfReal(phase)
      );
  }

  public SetLineDash(
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
  {state.lineDash = getValue();}

  public LineDash getValue(
    )
  {
    // 1. Dash array.
    PdfArray baseDashArray = (PdfArray)operands.get(0);
    double[] dashArray = new double[baseDashArray.size()];
    for(
      int index = 0,
        length = dashArray.length;
      index < length;
      index++
      )
    {dashArray[index] = ((IPdfNumber)baseDashArray.get(index)).getNumberValue();}
    // 2. Dash phase.
    double dashPhase = ((IPdfNumber)operands.get(1)).getNumberValue();

    return new LineDash(dashArray,dashPhase);
  }

  /**
    @since 0.0.6
  */
  public void setValue(
    LineDash value
    )
  {
    // 1. Dash array.
    double[] dashArray = value.getDashArray();
    PdfArray baseDashArray = new PdfArray(dashArray.length);
    for(
      int index = 0,
        length = dashArray.length;
      index < length;
      index++
      )
    {baseDashArray.set(index,new PdfReal(dashArray[index]));}
    operands.set(0,baseDashArray);

    // 2. Dash phase.
    ((IPdfNumber)operands.get(1)).setNumberValue(value.getDashPhase());
  }
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}