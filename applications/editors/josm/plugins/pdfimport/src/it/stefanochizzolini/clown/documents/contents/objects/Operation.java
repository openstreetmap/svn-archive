/*
  Copyright 2006-2010 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

import it.stefanochizzolini.clown.bytes.IOutputStream;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfName;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
  Content stream instruction [PDF:1.6:3.7.1].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.2
  @version 0.0.8
*/
public abstract class Operation
  extends ContentObject
{
  // <class>
  // <static>
  // <interface>
  // <public>
  /**
    Gets a specific operation.

    @param operator Operator.
    @param operands List of operands.
    @return Operation associated to the operator.
    @since 0.0.4
  */
  public static Operation get(
    String operator,
    List<PdfDirectObject> operands
    )
  {
    /*
      NOTE: This is a factory method for any operation-derived object.
    */
    if(operator == null)
      return null;

    if(operator.equals(SaveGraphicsState.Operator))
      return SaveGraphicsState.Value;
    else if(operator.equals(SetFont.Operator))
      return new SetFont(operands);
    else if(operator.equals(SetStrokeColor.Operator))
      return new SetStrokeColor(operands);
    else if(operator.equals(SetStrokeColorSpace.Operator))
      return new SetStrokeColorSpace(operands);
    else if(operator.equals(SetFillColor.Operator))
      return new SetFillColor(operands);
    else if(operator.equals(SetFillColorSpace.Operator))
      return new SetFillColorSpace(operands);
    else if(operator.equals(RestoreGraphicsState.Operator))
      return RestoreGraphicsState.Value;
    else if(operator.equals(BeginSubpath.Operator))
      return new BeginSubpath(operands);
    else if(operator.equals(CloseSubpath.Operator))
      return CloseSubpath.Value;
    else if(operator.equals(CloseStroke.Operator))
      return CloseStroke.Value;
    else if(operator.equals(Fill.Operator)
      || operator.equals(Fill.ObsoleteOperator))
      return Fill.Value;
    else if(operator.equals(FillEvenOdd.Operator))
      return FillEvenOdd.Value;
    else if(operator.equals(Stroke.Operator))
      return Stroke.Value;
    else if(operator.equals(FillStroke.Operator))
      return FillStroke.Value;
    else if(operator.equals(FillStrokeEvenOdd.Operator))
      return FillStrokeEvenOdd.Value;
    else if(operator.equals(CloseFillStroke.Operator))
      return CloseFillStroke.Value;
    else if(operator.equals(CloseFillStrokeEvenOdd.Operator))
      return CloseFillStrokeEvenOdd.Value;
    else if(operator.equals(EndPathNoOp.Operator))
      return EndPathNoOp.Value;
    else if(operator.equals(ModifyClipPath.Operator))
      return ModifyClipPath.Value;
    else if(operator.equals(ModifyClipPathEvenOdd.Operator))
      return ModifyClipPathEvenOdd.Value;
    else if(operator.equals(TranslateTextToNextLine.Operator))
      return TranslateTextToNextLine.Value;
    else if(operator.equals(ShowSimpleText.Operator))
      return new ShowSimpleText(operands);
    else if(operator.equals(ShowTextToNextLine.SimpleOperator)
      || operator.equals(ShowTextToNextLine.SpaceOperator))
      return new ShowTextToNextLine(operator,operands);
    else if(operator.equals(ShowAdjustedText.Operator))
      return new ShowAdjustedText(operands);
    else if(operator.equals(TranslateTextRelative.SimpleOperator)
      || operator.equals(TranslateTextRelative.LeadOperator))
      return new TranslateTextRelative(operator,operands);
    else if(operator.equals(SetTextMatrix.Operator))
      return new SetTextMatrix(operands);
    else if(operator.equals(ModifyCTM.Operator))
      return new ModifyCTM(operands);
    else if(operator.equals(PaintXObject.Operator))
      return new PaintXObject(operands);
    else if(operator.equals(PaintShading.Operator))
      return new PaintShading(operands);
    else if(operator.equals(SetCharSpace.Operator))
      return new SetCharSpace(operands);
    else if(operator.equals(SetLineCap.Operator))
      return new SetLineCap(operands);
    else if(operator.equals(SetLineDash.Operator))
      return new SetLineDash(operands);
    else if(operator.equals(SetLineJoin.Operator))
      return new SetLineJoin(operands);
    else if(operator.equals(SetLineWidth.Operator))
      return new SetLineWidth(operands);
    else if(operator.equals(SetMiterLimit.Operator))
      return new SetMiterLimit(operands);
    else if(operator.equals(SetTextLead.Operator))
      return new SetTextLead(operands);
    else if(operator.equals(SetTextRise.Operator))
      return new SetTextRise(operands);
    else if(operator.equals(SetTextScale.Operator))
      return new SetTextScale(operands);
    else if(operator.equals(SetTextRenderMode.Operator))
      return new SetTextRenderMode(operands);
    else if(operator.equals(SetWordSpace.Operator))
      return new SetWordSpace(operands);
    else if(operator.equals(DrawLine.Operator))
      return new DrawLine(operands);
    else if(operator.equals(DrawRectangle.Operator))
      return new DrawRectangle(operands);
    else if(operator.equals(DrawCurve.FinalOperator)
      || operator.equals(DrawCurve.FullOperator)
      || operator.equals(DrawCurve.InitialOperator))
      return new DrawCurve(operator,operands);
    else if(operator.equals(EndInlineImage.Operator))
      return EndInlineImage.Value;
    else if(operator.equals(BeginText.Operator))
      return BeginText.Value;
    else if(operator.equals(EndText.Operator))
      return EndText.Value;
    else if(operator.equals(BeginMarkedContent.SimpleOperator))
      return new BeginMarkedContent((PdfName)operands.get(0));
    else if(operator.equals(BeginMarkedContent.PropertyListOperator))
      return new BeginMarkedContent((PdfName)operands.get(0),operands.get(1));
    else if(operator.equals(EndMarkedContent.Operator))
      return EndMarkedContent.Value;
    else if(operator.equals(BeginInlineImage.Operator))
      return BeginInlineImage.Value;
    else if(operator.equals(EndInlineImage.Operator))
      return EndInlineImage.Value;
    else if(operator.equals(SetExtGState.Operator))
      return new SetExtGState(operands);
    else // No explicit operation implementation available.
      return new GenericOperation(operator,operands);
  }
  // </public>
  // </interface>
  // </static>

  // <dynamic>
  // <fields>
  protected String operator;
  protected List<PdfDirectObject> operands;
  // </fields>

  // <constructors>
  protected Operation(
    String operator
    )
  {this.operator = operator;}

  protected Operation(
    String operator,
    PdfDirectObject operand
    )
  {
    this.operator = operator;
    this.operands = new ArrayList<PdfDirectObject>(2);
    this.operands.add(operand);
  }

  protected Operation(
    String operator,
    PdfDirectObject... operands
    )
  {
    this.operator = operator;
    this.operands = new ArrayList<PdfDirectObject>(2);
    for(PdfDirectObject operand : operands)
    {this.operands.add(operand);}
  }

  protected Operation(
    String operator,
    List<PdfDirectObject> operands
    )
  {
    this.operator = operator;
    this.operands = operands;
  }
  // </constructors>

  // <interface>
  // <public>
  public String getOperator(
    )
  {return operator;}

  public List<PdfDirectObject> getOperands(
    )
  {return operands;}

  @Override
  public String toString(
    )
  {
    return "{"
      + operator + " "
      + (operands == null ? "" : operands.toString())
      + "}";
  }

  @Override
  public void writeTo(
    IOutputStream stream
    )
  {
    if(operands != null)
    {
      for(PdfDirectObject operand : operands)
      {operand.writeTo(stream); stream.write(" ");}
    }
    stream.write(operator); stream.write("\n");
  }
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}