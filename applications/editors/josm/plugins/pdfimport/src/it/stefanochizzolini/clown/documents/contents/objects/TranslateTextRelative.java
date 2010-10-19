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
import it.stefanochizzolini.clown.objects.IPdfNumber;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfReal;

import java.util.Arrays;
import java.util.List;

/**
  'Move to the start of the next line, offset from the start of the current line' operation
  [PDF:1.6:5.2].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.4
  @version 0.0.8
*/
public final class TranslateTextRelative
  extends Operation
{
  // <class>
  // <static>
  // <fields>
  /**
    No side effect.
  */
  public static final String SimpleOperator = "Td";
  /**
    Lead parameter setting.
  */
  public static final String LeadOperator = "TD";
  // </fields>
  // </static>

  // <dynamic>
  // <constructors>
  public TranslateTextRelative(
    double offsetX,
    double offsetY
    )
  {this(offsetX,offsetY,false);}

  public TranslateTextRelative(
    double offsetX,
    double offsetY,
    boolean leadSet
    )
  {
    super(
      leadSet ? LeadOperator : SimpleOperator,
      new PdfReal(offsetX),
      new PdfReal(offsetY)
      );
  }

  public TranslateTextRelative(
    String operator,
    List<PdfDirectObject> operands
    )
  {super(operator,operands);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public void applyTo(
    ContentScanner.GraphicsState state
    )
  {
    state.tm = ContentScanner.GraphicsState.concat(
      new double[]{1,0,0,1,getOffsetX(),getOffsetY()},
      state.tlm
      );
    state.tlm = Arrays.copyOf(state.tm,state.tm.length);
    if(isLeadSet())
    {state.lead = getOffsetY();}
  }

  public double getOffsetX(
    )
  {return ((IPdfNumber)operands.get(0)).getNumberValue();}

  public double getOffsetY(
    )
  {return ((IPdfNumber)operands.get(1)).getNumberValue();}

  /**
    Gets whether this operation, as a side effect, sets the leading parameter in the text state.

    @since 0.0.8
  */
  public boolean isLeadSet(
    )
  {return operator.equals(LeadOperator);}

  /**
    @see #isLeadSet()
    @since 0.0.8
  */
  public void setLeadSet(
    boolean value
    )
  {operator = (value ? LeadOperator : SimpleOperator);}

  /**
    @since 0.0.6
  */
  public void setOffsetX(
    double value
    )
  {((IPdfNumber)operands.get(0)).setNumberValue(value);}

  /**
    @since 0.0.6
  */
  public void setOffsetY(
    double value
    )
  {((IPdfNumber)operands.get(1)).setNumberValue(value);}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}