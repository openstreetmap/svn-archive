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
import it.stefanochizzolini.clown.objects.PdfInteger;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfReal;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
  Free text annotation [PDF:1.6:8.4.5].
  <p>It displays text directly on the page. Unlike an ordinary text annotation,
  a free text annotation has no open or closed state;
  instead of being displayed in a pop-up window, the text is always visible.</p>

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.7
*/
public class CalloutNote
  extends Annotation
{
  // <class>
  // <classes>
  /**
    Callout line [PDF:1.6:8.4.5].
  */
  public static class LineObject
    extends PdfObjectWrapper<PdfArray>
  {
    // <class>
    // <dynamic>
    // <fields>
    private Page page;
    // </fields>

    // <constructors>
    public LineObject(
      Page page,
      Point2D start,
      Point2D end
      )
    {
      super(
        page.getFile(),
        new PdfArray()
        );

      this.page = page;

      PdfArray baseDataObject = getBaseDataObject();
      double pageHeight = page.getBox().getHeight();
      baseDataObject.add(new PdfReal(start.getX()));
      baseDataObject.add(new PdfReal(pageHeight - start.getY()));
      baseDataObject.add(new PdfReal(end.getX()));
      baseDataObject.add(new PdfReal(pageHeight - end.getY()));
    }

    public LineObject(
      Page page,
      Point2D start,
      Point2D knee,
      Point2D end
      )
    {
      super(
        page.getFile(),
        new PdfArray()
        );

      this.page = page;

      PdfArray baseDataObject = getBaseDataObject();
      double pageHeight = page.getBox().getHeight();
      baseDataObject.add(new PdfReal(start.getX()));
      baseDataObject.add(new PdfReal(pageHeight - start.getY()));
      baseDataObject.add(new PdfReal(knee.getX()));
      baseDataObject.add(new PdfReal(pageHeight - knee.getY()));
      baseDataObject.add(new PdfReal(end.getX()));
      baseDataObject.add(new PdfReal(pageHeight - end.getY()));
    }

    private LineObject(
      PdfDirectObject baseObject,
      PdfIndirectObject container
      )
    {super(baseObject,container);}
    // </constructors>

    // <interface>
    // <public>
    @Override
    public LineObject clone(
      Document context
      )
    {throw new NotImplementedException();}

    public Point2D getEnd(
      )
    {
      PdfArray coordinates = getBaseDataObject();
      if(coordinates.size() < 6)
        return new Point2D.Double(
          ((IPdfNumber)coordinates.get(2)).getNumberValue(),
          page.getBox().getHeight() - ((IPdfNumber)coordinates.get(3)).getNumberValue()
          );
      else
        return new Point2D.Double(
          ((IPdfNumber)coordinates.get(4)).getNumberValue(),
          page.getBox().getHeight() - ((IPdfNumber)coordinates.get(5)).getNumberValue()
          );
    }

    public Point2D getKnee(
      )
    {
      PdfArray coordinates = getBaseDataObject();
      if(coordinates.size() < 6)
        return null;

      return new Point2D.Double(
        ((IPdfNumber)coordinates.get(2)).getNumberValue(),
        page.getBox().getHeight() - ((IPdfNumber)coordinates.get(3)).getNumberValue()
        );
    }

    public Point2D getStart(
      )
    {
      PdfArray coordinates = getBaseDataObject();

      return new Point2D.Double(
        ((IPdfNumber)coordinates.get(0)).getNumberValue(),
        page.getBox().getHeight() - ((IPdfNumber)coordinates.get(1)).getNumberValue()
        );
    }
    // </public>
    // </interface>
    // </dynamic>
    // </class>
  }

  /**
    Text justification [PDF:1.6:8.4.5].
  */
  public enum JustificationEnum
  {
    // <class>
    // <static>
    // <fields>
    /**
      Left.
    */
    Left(new PdfInteger(0)),
    /**
      Center.
    */
    Center(new PdfInteger(1)),
    /**
      Right.
    */
    Right(new PdfInteger(2));
    // </fields>

    // <interface>
    // <public>
    /**
      Gets the highlighting mode corresponding to the given value.
    */
    public static JustificationEnum get(
      PdfInteger value
      )
    {
      for(JustificationEnum justification : JustificationEnum.values())
      {
        if(justification.getCode().equals(value))
          return justification;
      }
      return null;
    }
    // </public>
    // </interface>
    // </static>

    // <dynamic>
    // <fields>
    private final PdfInteger code;
    // </fields>

    // <constructors>
    private JustificationEnum(
      PdfInteger code
      )
    {this.code = code;}
    // </constructors>

    // <interface>
    // <public>
    public PdfInteger getCode(
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
  public CalloutNote(
    Page page,
    Rectangle2D box,
    String text
    )
  {
    super(
      page.getDocument(),
      PdfName.FreeText,
      box,
      page
      );

    setText(text);
  }

  public CalloutNote(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {super(baseObject,container);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public CalloutNote clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the justification to be used in displaying the annotation's text.
  */
  public JustificationEnum getJustification(
    )
  {
    /*
      NOTE: 'Q' entry may be undefined.
    */
    PdfInteger justificationObject = (PdfInteger)getBaseDataObject().get(PdfName.Q);
    if(justificationObject == null)
      return JustificationEnum.Left;

    return JustificationEnum.get(justificationObject);
  }

  /**
    Gets the callout line attached to the free text annotation.
  */
  public LineObject getLine(
    )
  {
    /*
      NOTE: 'CL' entry may be undefined.
    */
    PdfArray calloutLineObject = (PdfArray)getBaseDataObject().get(PdfName.CL);
    if(calloutLineObject == null)
      return null;

    return new LineObject(calloutLineObject,getContainer());
  }

  /**
    @see #getJustification()
  */
  public void setJustification(
    JustificationEnum value
    )
  {getBaseDataObject().put(PdfName.Q, value.getCode());}

  /**
    @see #getLine()
  */
  public void setLine(
    LineObject value
    )
  {getBaseDataObject().put(PdfName.CL,value.getBaseObject());}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}