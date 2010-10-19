/*
  Copyright 2008-2009 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.documents.interaction.annotations;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.documents.Page;
import it.stefanochizzolini.clown.objects.IPdfNumber;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfReal;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
  Text markup annotation [PDF:1.6:8.4.5].
  <p>It displays highlights, underlines, strikeouts, or jagged ("squiggly") underlines
  in the text of a document.</p>

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.7
*/
public class TextMarkup
  extends Annotation
{
  // <class>
  // <classes>
  /**
    Markup type [PDF:1.6:8.4.5].
  */
  public enum MarkupTypeEnum
  {
    // <class>
    // <static>
    // <fields>
    /**
      Highlight.
    */
    Highlight(PdfName.Highlight),
    /**
      Squiggly.
    */
    Squiggly(PdfName.Squiggly),
    /**
      StrikeOut.
    */
    StrikeOut(PdfName.StrikeOut),
    /**
      Underline.
    */
    Underline(PdfName.Underline);
    // </fields>

    // <interface>
    // <public>
    /**
      Gets the markup type corresponding to the given value.
    */
    public static MarkupTypeEnum get(
      PdfName value
      )
    {
      for(MarkupTypeEnum markupType : MarkupTypeEnum.values())
      {
        if(markupType.getCode().equals(value))
          return markupType;
      }
      return null;
    }
    // </public>
    // </interface>
    // </static>

    // <dynamic>
    // <fields>
    private final PdfName code;
    // </fields>

    // <constructors>
    private MarkupTypeEnum(
      PdfName code
      )
    {this.code = code;}
    // </constructors>

    // <interface>
    // <public>
    public PdfName getCode(
      )
    {return code;}
    // </public>
    // </interface>
    // </dynamic>
    // </class>
  }
  // </classes>

  // <dynamic>
  // <constructors>
  public TextMarkup(
    Page page,
    Rectangle2D box,
    MarkupTypeEnum type
    )
  {
    super(
      page.getDocument(),
      type.getCode(),
      box,
      page
      );
  }

  public TextMarkup(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {super(baseObject,container);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public TextMarkup clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the quadrilaterals encompassing a word or group of contiguous words
    in the text underlying the annotation.
  */
  public List<Rectangle2D> getBoxes(
    )
  {
    /*
      NOTE: 'QuadPoints' entry MUST be present.
    */
    PdfArray quadPointsObject = (PdfArray)getBaseDataObject().get(PdfName.QuadPoints);
    List<Rectangle2D> boxes = new ArrayList<Rectangle2D>();
    double pageHeight = getPage().getBox().getHeight();
    for(
      int index = 0,
        length = quadPointsObject.size();
      index < length;
      index += 8
      )
    {
      double x = ((IPdfNumber)quadPointsObject.get(index+6)).getNumberValue();
      double y = pageHeight - ((IPdfNumber)quadPointsObject.get(index+7)).getNumberValue();
      double width = ((IPdfNumber)quadPointsObject.get(index+2)).getNumberValue() - ((IPdfNumber)quadPointsObject.get(index)).getNumberValue();
      double height = ((IPdfNumber)quadPointsObject.get(index+3)).getNumberValue() - ((IPdfNumber)quadPointsObject.get(index+1)).getNumberValue();
      boxes.add(
        new Rectangle2D.Double(x,y,width,height)
        );
    }

    return boxes;
  }

  /**
    Gets the markup type.
  */
  public MarkupTypeEnum getMarkupType(
    )
  {
    /*
      NOTE: 'Subtype' entry MUST be present.
    */
    return MarkupTypeEnum.get((PdfName)getBaseDataObject().get(PdfName.Subtype));
  }

  /**
    @see #getBoxes()
  */
  public void setBoxes(
    List<Rectangle2D> value
    )
  {
    PdfArray quadPointsObject = new PdfArray();
    double pageHeight = getPage().getBox().getHeight();
    for(Rectangle2D box : value)
    {
      quadPointsObject.add(new PdfReal(box.getX())); // x1.
      quadPointsObject.add(new PdfReal(pageHeight-(box.getY()+box.getHeight()))); // y1.
      quadPointsObject.add(new PdfReal(box.getX()+box.getWidth())); // x2.
      quadPointsObject.add(quadPointsObject.get(1)); // y2.
      quadPointsObject.add(quadPointsObject.get(2)); // x3.
      quadPointsObject.add(new PdfReal(pageHeight-box.getY())); // y3.
      quadPointsObject.add(quadPointsObject.get(0)); // x4.
      quadPointsObject.add(quadPointsObject.get(5)); // y4.
    }

    getBaseDataObject().put(PdfName.QuadPoints,quadPointsObject);
  }

  /**
    @see #getMarkupType()
  */
  public void setMarkupType(
    MarkupTypeEnum value
    )
  {getBaseDataObject().put(PdfName.Subtype, value.getCode());}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}