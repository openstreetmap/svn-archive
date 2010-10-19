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
import it.stefanochizzolini.clown.documents.contents.colorSpaces.DeviceRGBColor;
import it.stefanochizzolini.clown.objects.IPdfNumber;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfBoolean;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfReal;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
  Line annotation [PDF:1.6:8.4.5].
  <p>It displays displays a single straight line on the page.
  When opened, it displays a pop-up window containing the text of the associated note.</p>

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.7
*/
public class Line
  extends Annotation
{
  // <class>
  // <classes>
  /**
    Line ending style [PDF:1.6:8.4.5].
  */
  public enum LineEndStyleEnum
  {
    // <class>
    // <static>
    // <fields>
    /**
      Square.
    */
    Square(PdfName.Square),
    /**
      Circle.
    */
    Circle(PdfName.Circle),
    /**
      Diamond.
    */
    Diamond(PdfName.Diamond),
    /**
      Open arrow.
    */
    OpenArrow(PdfName.OpenArrow),
    /**
      Closed arrow.
    */
    ClosedArrow(PdfName.ClosedArrow),
    /**
      None.
    */
    None(PdfName.None),
    /**
      Butt.
    */
    Butt(PdfName.Butt),
    /**
      Reverse open arrow.
    */
    ReverseOpenArrow(PdfName.ROpenArrow),
    /**
      Reverse closed arrow.
    */
    ReverseClosedArrow(PdfName.RClosedArrow),
    /**
      Slash.
    */
    Slash(PdfName.Slash);
    // </fields>

    // <interface>
    // <public>
    /**
      Gets the line ending style corresponding to the given value.
    */
    public static LineEndStyleEnum get(
      PdfName value
      )
    {
      for(LineEndStyleEnum style : LineEndStyleEnum.values())
      {
        if(style.getCode().equals(value))
          return style;
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
    private LineEndStyleEnum(
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

  // <static>
  // <fields>
  private static final double DefaultLeaderLineExtensionLength = 0;
  private static final double DefaultLeaderLineLength = 0;
  private static final LineEndStyleEnum DefaultLineEndStyle = LineEndStyleEnum.None;
  // </fields>
  // </static>

  // <dynamic>
  // <constructors>
  public Line(
    Page page,
    Point2D startPoint,
    Point2D endPoint
    )
  {
    super(
      page.getDocument(),
      PdfName.Line,
      new Rectangle2D.Double(
        startPoint.getX(),
        startPoint.getY(),
        endPoint.getX()-startPoint.getX(),
        endPoint.getY()-startPoint.getY()
        ),
      page
      );

    getBaseDataObject().put(
      PdfName.L,
      new PdfArray(new PdfDirectObject[]{new PdfReal(0),new PdfReal(0),new PdfReal(0),new PdfReal(0)})
      );
    setStartPoint(startPoint);
    setEndPoint(endPoint);
  }

  public Line(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {super(baseObject,container);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public Line clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the ending coordinates.
  */
  public Point2D getEndPoint(
    )
  {
    /*
      NOTE: 'L' entry MUST be defined.
    */
    PdfArray coordinatesObject = (PdfArray)getBaseDataObject().get(PdfName.L);

    return new Point2D.Double(
      ((IPdfNumber)coordinatesObject.get(2)).getNumberValue(),
      ((IPdfNumber)coordinatesObject.get(3)).getNumberValue()
      );
  }

  /**
    Gets the style of the ending line ending.
  */
  public LineEndStyleEnum getEndStyle(
    )
  {
    /*
      NOTE: 'LE' entry may be undefined.
    */
    PdfArray endstylesObject = (PdfArray)getBaseDataObject().get(PdfName.LE);
    if(endstylesObject == null)
      return DefaultLineEndStyle;

    return LineEndStyleEnum.get((PdfName)endstylesObject.get(1));
  }

  /**
    Gets the color with which to fill the interior of the annotation's line endings.
  */
  public DeviceRGBColor getFillColor(
    )
  {
    /*
      NOTE: 'IC' entry may be undefined.
    */
    PdfArray fillColorObject = (PdfArray)getBaseDataObject().get(PdfName.IC);
    if(fillColorObject == null)
      return null;

    return new DeviceRGBColor(
      ((IPdfNumber)fillColorObject.get(0)).getNumberValue(),
      ((IPdfNumber)fillColorObject.get(1)).getNumberValue(),
      ((IPdfNumber)fillColorObject.get(2)).getNumberValue()
      );
  }

  /**
    Gets the length of leader line extensions that extend
    in the opposite direction from the leader lines.
  */
  public double getLeaderLineExtensionLength(
    )
  {
    /*
      NOTE: 'LLE' entry may be undefined.
    */
    IPdfNumber leaderLineExtensionLengthObject = (IPdfNumber)getBaseDataObject().get(PdfName.LLE);
    if(leaderLineExtensionLengthObject == null)
      return DefaultLeaderLineExtensionLength;

    return leaderLineExtensionLengthObject.getNumberValue();
  }

  /**
    Gets the length of leader lines that extend from each endpoint
    of the line perpendicular to the line itself.
    <p>A positive value means that the leader lines appear in the direction
    that is clockwise when traversing the line from its starting point
    to its ending point; a negative value indicates the opposite direction.</p>
  */
  public double getLeaderLineLength(
    )
  {
    /*
      NOTE: 'LL' entry may be undefined.
    */
    IPdfNumber leaderLineLengthObject = (IPdfNumber)getBaseDataObject().get(PdfName.LL);
    if(leaderLineLengthObject == null)
      return DefaultLeaderLineLength;

    return -leaderLineLengthObject.getNumberValue();
  }

  /**
    Gets the starting coordinates.
  */
  public Point2D getStartPoint(
    )
  {
    /*
      NOTE: 'L' entry MUST be defined.
    */
    PdfArray coordinatesObject = (PdfArray)getBaseDataObject().get(PdfName.L);

    return new Point2D.Double(
      ((IPdfNumber)coordinatesObject.get(0)).getNumberValue(),
      ((IPdfNumber)coordinatesObject.get(1)).getNumberValue()
      );
  }

  /**
    Gets the style of the starting line ending.
  */
  public LineEndStyleEnum getStartStyle(
    )
  {
    /*
      NOTE: 'LE' entry may be undefined.
    */
    PdfArray endstylesObject = (PdfArray)getBaseDataObject().get(PdfName.LE);
    if(endstylesObject == null)
      return DefaultLineEndStyle;

    return LineEndStyleEnum.get((PdfName)endstylesObject.get(0));
  }

  /**
    Gets whether the contents should be shown as a caption.
  */
  public boolean isCaptionVisible(
    )
  {
    /*
      NOTE: 'Cap' entry may be undefined.
    */
    PdfBoolean captionVisibleObject = (PdfBoolean)getBaseDataObject().get(PdfName.Cap);
    if(captionVisibleObject == null)
      return false;

    return ((Boolean)captionVisibleObject.getValue()).booleanValue();
  }

  /**
    @see #isCaptionVisible()
  */
  public void setCaptionVisible(
    boolean value
    )
  {getBaseDataObject().put(PdfName.Cap,new PdfBoolean(value));}

  /**
    @see #getEndPoint()
  */
  public void setEndPoint(
    Point2D value
    )
  {
    PdfArray coordinatesObject = (PdfArray)getBaseDataObject().get(PdfName.L);
    coordinatesObject.set(2,new PdfReal(value.getX()));
    coordinatesObject.set(3,new PdfReal(getPage().getBox().getHeight()-value.getY()));
  }

  /**
    @see #getEndStyle()
  */
  public void setEndStyle(
    LineEndStyleEnum value
    )
  {ensureLineEndStylesObject().set(1,value.getCode());}

  /**
    @see #getFillColor()
  */
  public void setFillColor(
     DeviceRGBColor value
    )
  {
    getBaseDataObject().put(
      PdfName.IC,
      new PdfArray(
        new PdfDirectObject[]
        {
          new PdfReal(value.getRedComponent()),
          new PdfReal(value.getGreenComponent()),
          new PdfReal(value.getBlueComponent())
        }
        )
      );
  }

  /**
    @see #getLeaderLineExtensionLength()
  */
  public void setLeaderLineExtensionLength(
    double value
    )
  {
    getBaseDataObject().put(PdfName.LLE,new PdfReal(value));
    /*
      NOTE: If leader line extension entry is present, leader line MUST be too.
    */
    if(!getBaseDataObject().containsKey(PdfName.LL))
    {setLeaderLineLength(DefaultLeaderLineLength);}
  }

  /**
    @see #getLeaderLineLength()
  */
  public void setLeaderLineLength(
    double value
    )
  {getBaseDataObject().put(PdfName.LL,new PdfReal(-value));}

  /**
    @see #getStartPoint()
  */
  public void setStartPoint(
    Point2D value
    )
  {
    PdfArray coordinatesObject = (PdfArray)getBaseDataObject().get(PdfName.L);
    coordinatesObject.set(0,new PdfReal(value.getX()));
    coordinatesObject.set(1,new PdfReal(getPage().getBox().getHeight()-value.getY()));
  }

  /**
    @see #getStartStyle()
  */
  public void setStartStyle(
    LineEndStyleEnum value
    )
  {ensureLineEndStylesObject().set(0,value.getCode());}
  // </public>

  // <private>
  private PdfArray ensureLineEndStylesObject(
    )
  {
    PdfArray endStylesObject = (PdfArray)getBaseDataObject().get(PdfName.LE);
    if(endStylesObject == null)
    {
      getBaseDataObject().put(
        PdfName.LE,
        endStylesObject = new PdfArray(
          new PdfDirectObject[]
          {
            DefaultLineEndStyle.getCode(),
            DefaultLineEndStyle.getCode()
          }
          )
        );
    }

    return endStylesObject;
  }
  // </private>
  // </interface>
  // </dynamic>
  // </class>
}