/*
  Copyright 2009 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.documents.contents;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.documents.contents.fonts.Font;
import it.stefanochizzolini.clown.objects.IPdfNumber;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfInteger;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfReference;
import it.stefanochizzolini.clown.util.NotImplementedException;

/**
  Graphics state parameter dictionary [PDF:1.6:4.3.4].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.8
*/
public class ExtGState
  extends PdfObjectWrapper<PdfDictionary>
{
  // <class>
  // <dynamic>
  // <constructors>
  public ExtGState(
    Document context,
    PdfDictionary baseDataObject
    )
  {
    super(
      context.getFile(),
      baseDataObject
      );
  }

  ExtGState(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {
    super(
      baseObject,
      container
      );
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public ExtGState clone(
    Document context
    )
  {throw new NotImplementedException();}

  public Font getFont(
    )
  {
    PdfArray fontObject = (PdfArray)getBaseDataObject().get(PdfName.Font);
    if(fontObject == null)
      return null;

    return Font.wrap(
      (PdfReference)fontObject.get(0)
      );
  }

  public Double getFontSize(
    )
  {
    PdfArray fontObject = (PdfArray)getBaseDataObject().get(PdfName.Font);
    if(fontObject == null)
      return null;

    return new Double(
      ((IPdfNumber)fontObject.get(1)).getNumberValue()
      );
  }

  public LineCapEnum getLineCap(
    )
  {
    PdfInteger lineCapObject = (PdfInteger)getBaseDataObject().get(PdfName.LC);
    if(lineCapObject == null)
      return null;

    return LineCapEnum.valueOf(lineCapObject.getRawValue());
  }

  public LineDash getLineDash(
    )
  {
    PdfArray lineDashObject = (PdfArray)getBaseDataObject().get(PdfName.D);
    if(lineDashObject == null)
      return null;

    // 1. Dash array.
    PdfArray baseDashArray = (PdfArray)lineDashObject.get(0);
    double[] dashArray = new double[baseDashArray.size()];
    for(
      int index = 0,
        length = dashArray.length;
      index < length;
      index++
      )
    {dashArray[index] = ((IPdfNumber)baseDashArray.get(index)).getNumberValue();}
    // 2. Dash phase.
    double dashPhase = ((IPdfNumber)lineDashObject.get(1)).getNumberValue();

    return new LineDash(dashArray,dashPhase);
  }

  public LineJoinEnum getLineJoin(
    )
  {
    PdfInteger lineJoinObject = (PdfInteger)getBaseDataObject().get(PdfName.LJ);
    if(lineJoinObject == null)
      return null;

    return LineJoinEnum.valueOf(lineJoinObject.getRawValue());
  }

  public Double getLineWidth(
    )
  {
    IPdfNumber lineWidthObject = (IPdfNumber)getBaseDataObject().get(PdfName.LW);
    if(lineWidthObject == null)
      return null;

    return new Double(lineWidthObject.getNumberValue());
  }

  public Double getMiterLimit(
    )
  {
    IPdfNumber miterLimitObject = (IPdfNumber)getBaseDataObject().get(PdfName.ML);
    if(miterLimitObject == null)
      return null;

    return new Double(miterLimitObject.getNumberValue());
  }
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}