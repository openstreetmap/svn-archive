/*
  Copyright 2009-2010 Stefano Chizzolini. http://clown.stefanochizzolini.it

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
import it.stefanochizzolini.clown.documents.contents.ExtGState;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfName;

import java.util.List;

/**
  'Set the specified graphics state parameters' operation [PDF:1.6:4.3.3].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.8
  @version 0.0.8
*/
public final class SetExtGState
  extends Operation
{
  // <class>
  // <static>
  // <fields>
  public static final String Operator = "gs";
  // </fields>
  // </static>

  // <dynamic>
  // <constructors>
  public SetExtGState(
    PdfName name
    )
  {super(Operator,name);}

  public SetExtGState(
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
    ExtGState extGState = state.getScanner().getContentContext().getResources().getExtGStates().get(getName());
    for(PdfName parameterName : extGState.getBaseDataObject().keySet())
    {
      if(parameterName.equals(PdfName.Font))
      {
        state.font = extGState.getFont();
        state.fontSize = extGState.getFontSize();
      }
      else if(parameterName.equals(PdfName.LC))
      {state.lineCap = extGState.getLineCap();}
      else if(parameterName.equals(PdfName.D))
      {state.lineDash = extGState.getLineDash();}
      else if(parameterName.equals(PdfName.LJ))
      {state.lineJoin = extGState.getLineJoin();}
      else if(parameterName.equals(PdfName.LW))
      {state.lineWidth = extGState.getLineWidth();}
      else if(parameterName.equals(PdfName.ML))
      {state.miterLimit = extGState.getMiterLimit();}
      //TODO:extend supported parameters!!!
    }
  }

  public PdfName getName(
    )
  {return (PdfName)operands.get(0);}

  public void setName(
    PdfName value
    )
  {operands.set(0,value);}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}