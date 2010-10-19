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

import it.stefanochizzolini.clown.objects.IPdfNumber;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfReal;
import it.stefanochizzolini.clown.objects.PdfString;

import java.util.List;

/**
  'Move to the next line and show a text string' operation [PDF:1.6:5.3.2].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.8
  @version 0.0.8
*/
public final class ShowTextToNextLine
  extends ShowText
{
  // <class>
  // <static>
  // <fields>
  /**
    Specifies no text state parameter
    (just uses the current settings).
  */
  public static final String SimpleOperator = "'";
  /**
    Specifies the word spacing and the character spacing
    (setting the corresponding parameters in the text state).
  */
  public static final String SpaceOperator = "''";
  // </fields>
  // </static>

  // <dynamic>
  // <constructors>
  /**
    @param text Text encoded using current font's encoding.
  */
  public ShowTextToNextLine(
    byte[] text
    )
  {
    super(
      SimpleOperator,
      new PdfString(text)
      );
  }

  /**
    @param text Text encoded using current font's encoding.
    @param wordSpace Word spacing.
    @param charSpace Character spacing.
  */
  public ShowTextToNextLine(
    byte[] text,
    double wordSpace,
    double charSpace
    )
  {
    super(
      SpaceOperator,
      new PdfReal(wordSpace),
      new PdfReal(charSpace),
      new PdfString(text)
      );
  }

  public ShowTextToNextLine(
    String operator,
    List<PdfDirectObject> operands
    )
  {super(operator,operands);}
  // </constructors>

  // <interface>
  // <public>
  /**
    Gets the character spacing.
  */
  public Double getCharSpace(
    )
  {
    if(operator.equals(SimpleOperator))
      return null;

    return ((IPdfNumber)operands.get(1)).getNumberValue();
  }

  @Override
  public byte[] getText(
    )
  {
    return ((PdfString)operands.get(
      operator.equals(SimpleOperator) ? 0 : 2
      )).getRawValue();
  }

  /**
    Gets the word spacing.
  */
  public Double getWordSpace(
    )
  {
    if(operator.equals(SimpleOperator))
      return null;

    return ((IPdfNumber)operands.get(0)).getNumberValue();
  }

  /**
    @see #getCharSpace()
  */
  public void setCharSpace(
    Double value
    )
  {
    ensureSpaceOperation();

    ((IPdfNumber)operands.get(1)).setNumberValue(value);
  }

  @Override
  public void setText(
    byte[] value
    )
  {
    ((PdfString)operands.get(
      operator.equals(SimpleOperator) ? 0 : 2
      )).setRawValue(value);
  }

  /**
    @see #getWordSpace()
  */
  public void setWordSpace(
    Double value
    )
  {
    ensureSpaceOperation();

    ((IPdfNumber)operands.get(0)).setNumberValue(value);
  }
  // </public>

  // <private>
  private void ensureSpaceOperation(
    )
  {
    if(operator.equals(SimpleOperator))
    {
      operator = SpaceOperator;
      operands.add(0,new PdfReal(0));
      operands.add(1,new PdfReal(0));
    }
  }
  // </private>
  // </interface>
  // </dynamic>
  // </class>
}