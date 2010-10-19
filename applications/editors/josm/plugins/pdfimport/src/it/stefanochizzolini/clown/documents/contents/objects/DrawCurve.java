/*
  Copyright 2008-2010 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

import java.awt.geom.Point2D;
import java.util.List;

/**
  'Append a cubic Bezier curve to the current path' operation
  [PDF:1.6:4.4.1].
  <p>Such curves are defined by four points:
  the two endpoints (the current point and the final point)
  and two control points (the first control point, associated to the current point,
  and the second control point, associated to the final point).</p>

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.7
  @version 0.0.8
*/
public final class DrawCurve
  extends Operation
{
  // <class>
  // <static>
  // <fields>
  /**
    Specifies only the second control point
    (the first control point coincides with initial point of the curve).
  */
  public static final String FinalOperator = "v";
  /**
    Specifies both control points explicitly.
  */
  public static final String FullOperator = "c";
  /**
    Specifies only the first control point
    (the second control point coincides with final point of the curve).
  */
  public static final String InitialOperator = "y";
  // </fields>
  // </static>

  // <dynamic>
  // <constructors>
  /**
    Creates a fully-explicit curve.

    @param point Final endpoint.
    @param control1 First control point.
    @param control2 Second control point.
  */
  public DrawCurve(
    Point2D point,
    Point2D control1,
    Point2D control2
    )
  {
    this(
      point.getX(),
      point.getY(),
      control1.getX(),
      control1.getY(),
      control2.getX(),
      control2.getY()
      );
  }

  /**
    Creates a fully-explicit curve.
  */
  public DrawCurve(
    double pointX,
    double pointY,
    double control1X,
    double control1Y,
    double control2X,
    double control2Y
    )
  {
    super(
      FullOperator,
      new PdfReal(control1X),
      new PdfReal(control1Y),
      new PdfReal(control2X),
      new PdfReal(control2Y),
      new PdfReal(pointX),
      new PdfReal(pointY)
      );
  }

  /**
    Creates a partially-explicit curve.

    @param point Final endpoint.
    @param control Explicit control point.
    @param operator Operator (either <code>InitialOperator</code> or <code>FinalOperator</code>). It defines how to interpret the <code>control</code> parameter.
  */
  public DrawCurve(
    Point2D point,
    Point2D control,
    String operator
    )
  {
    super(
      operator.equals(InitialOperator) ? InitialOperator : FinalOperator,
      new PdfReal(control.getX()),
      new PdfReal(control.getY()),
      new PdfReal(point.getX()),
      new PdfReal(point.getY())
      );
  }

  public DrawCurve(
    String operator,
    List<PdfDirectObject> operands
    )
  {super(operator,operands);}
  // </constructors>

  // <interface>
  // <public>
  /**
    Gets the first control point.
  */
  public Point2D getControl1(
    )
  {
    if(operator.equals(FinalOperator))
      return null;

    return new Point2D.Double(
      ((IPdfNumber)operands.get(0)).getNumberValue(),
      ((IPdfNumber)operands.get(1)).getNumberValue()
      );
  }

  /**
    Gets the second control point.
  */
  public Point2D getControl2(
    )
  {
    if(operator.equals(InitialOperator))
      return null;
    if(operator.equals(FinalOperator))
      return new Point2D.Double(
        ((IPdfNumber)operands.get(0)).getNumberValue(),
        ((IPdfNumber)operands.get(1)).getNumberValue()
        );
    // Full operator.
    return new Point2D.Double(
      ((IPdfNumber)operands.get(2)).getNumberValue(),
      ((IPdfNumber)operands.get(3)).getNumberValue()
      );
  }

  /**
    Gets the final endpoint.
  */
  public Point2D getPoint(
    )
  {
    return new Point2D.Double(
      ((IPdfNumber)operands.get(0)).getNumberValue(),
      ((IPdfNumber)operands.get(1)).getNumberValue()
      );
  }

  /**
    @see #getControl1()
  */
  public void setControl1(
    Point2D value
    )
  {
    if(operator.equals(FinalOperator))
    {operator = FullOperator;}

    ((IPdfNumber)operands.get(0)).setNumberValue(value.getX());
    ((IPdfNumber)operands.get(1)).setNumberValue(value.getY());
  }

  /**
    @see #getControl2()
  */
  public void setControl2(
    Point2D value
    )
  {
    if(operator.equals(InitialOperator))
    {operator = FullOperator;}

    if(operator.equals(FinalOperator))
    {
      ((IPdfNumber)operands.get(0)).setNumberValue(value.getX());
      ((IPdfNumber)operands.get(1)).setNumberValue(value.getY());
    }
    else // Full operator.
    {
      ((IPdfNumber)operands.get(2)).setNumberValue(value.getX());
      ((IPdfNumber)operands.get(3)).setNumberValue(value.getY());
    }
  }

  /**
    @see #getPoint()
  */
  public void setPoint(
    Point2D value
    )
  {
    ((IPdfNumber)operands.get(0)).setNumberValue(value.getX());
    ((IPdfNumber)operands.get(1)).setNumberValue(value.getY());
  }
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}