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

package it.stefanochizzolini.clown.documents.interaction.navigation.page;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.objects.IPdfNumber;
import it.stefanochizzolini.clown.objects.PdfAtomicObject;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfInteger;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfReal;
import it.stefanochizzolini.clown.util.NotImplementedException;

/**
  Visual transition to use when moving to a page during a presentation [PDF:1.6:8.3.3].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.7
  @version 0.0.8
*/
public class Transition
  extends PdfObjectWrapper<PdfDictionary>
{
  // <class>
  // <classes>
  /**
    Transition direction [PDF:1.6:8.3.3].
  */
  public enum DirectionEnum
  {
    // <class>
    // <static>
    // <fields>
    /**
      Left to right.
    */
    LeftToRight(new PdfInteger(0)),
    /**
      Bottom to top.
    */
    BottomToTop(new PdfInteger(90)),
    /**
      Right to left.
    */
    RightToLeft(new PdfInteger(180)),
    /**
      Top to bottom.
    */
    TopToBottom(new PdfInteger(270)),
    /**
      Top-left to bottom-right.
    */
    TopLeftToBottomRight(new PdfInteger(315)),
    /**
      None.
    */
    None(PdfName.None);
    // </fields>

    // <interface>
    // <public>
    /**
      Gets the direction corresponding to the given value.
    */
    public static DirectionEnum get(
      PdfAtomicObject<?> value
      )
    {
      for(DirectionEnum direction : DirectionEnum.values())
      {
        if(direction.getCode().equals(value))
          return direction;
      }
      return null;
    }
    // </public>
    // </interface>
    // </static>

    // <dynamic>
    // <fields>
    private final PdfAtomicObject<?> code;
    // </fields>

    // <constructors>
    private DirectionEnum(
      PdfAtomicObject<?> code
      )
    {this.code = code;}
    // </constructors>

    // <interface>
    // <public>
    public PdfAtomicObject<?> getCode(
      )
    {return code;}
    // </public>
    // </interface>
    // </dynamic>
    // </class>
  }

  /**
    Transition orientation [PDF:1.6:8.3.3].
  */
  public enum OrientationEnum
  {
    // <class>
    // <static>
    // <fields>
    /**
      Horizontal.
    */
    Horizontal(PdfName.H),
    /**
      Vertical.
    */
    Vertical(PdfName.V);
    // </fields>

    // <interface>
    // <public>
    /**
      Gets the orientation corresponding to the given value.
    */
    public static OrientationEnum get(
      PdfName value
      )
    {
      for(OrientationEnum orientation : OrientationEnum.values())
      {
        if(orientation.getCode().equals(value))
          return orientation;
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
    private OrientationEnum(
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

  /**
    Transition direction on page [PDF:1.6:8.3.3].
  */
  public enum PageDirectionEnum
  {
    // <class>
    // <static>
    // <fields>
    /**
      Inward (from the edges of the page).
    */
    Inward(PdfName.I),
    /**
      Outward (from the center of the page).
    */
    Outward(PdfName.O);
    // </fields>

    // <interface>
    // <public>
    /**
      Gets the page direction corresponding to the given value.
    */
    public static PageDirectionEnum get(
      PdfName value
      )
    {
      for(PageDirectionEnum direction : PageDirectionEnum.values())
      {
        if(direction.getCode().equals(value))
          return direction;
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
    private PageDirectionEnum(
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

  /**
    Transition style [PDF:1.6:8.3.3].
  */
  public enum StyleEnum
  {
    // <class>
    // <static>
    // <fields>
    /**
      Two lines sweep across the screen, revealing the page.
    */
    Split(PdfName.Split),
    /**
      Multiple lines sweep across the screen, revealing the page.
    */
    Blinds(PdfName.Blinds),
    /**
      A rectangular box sweeps between the edges of the page and the center.
    */
    Box(PdfName.Box),
    /**
      A single line sweeps across the screen from one edge to the other.
    */
    Wipe(PdfName.Wipe),
    /**
      The old page dissolves gradually.
    */
    Dissolve(PdfName.Dissolve),
    /**
      The old page dissolves gradually sweeping across the page in a wide band
      moving from one side of the screen to the other.
    */
    Glitter(PdfName.Glitter),
    /**
      No transition.
    */
    Replace(PdfName.R),
    /**
      Changes are flown across the screen.
    */
    Fly(PdfName.Fly),
    /**
      The page slides in, pushing away the old one.
    */
    Push(PdfName.Push),
    /**
      The page slides on to the screen, covering the old one.
    */
    Cover(PdfName.Cover),
    /**
      The old page slides off the screen, uncovering the new one.
    */
    Uncover(PdfName.Uncover),
    /**
      The new page reveals gradually.
    */
    Fade(PdfName.Fade);
    // </fields>

    // <interface>
    // <public>
    /**
      Gets the style corresponding to the given value.
    */
    public static StyleEnum get(
      PdfName value
      )
    {
      for(StyleEnum style : StyleEnum.values())
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
    private StyleEnum(
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
  /**
    Creates a new action within the given document context.
  */
  public Transition(
    Document context
    )
  {
    super(
      context.getFile(),
      new PdfDictionary(
        new PdfName[]{PdfName.Type},
        new PdfDirectObject[]{PdfName.Trans}
        )
      );
  }

  public Transition(
    Document context,
    StyleEnum style
    )
  {
    this(
      context,
      style,
      null,
      null,
      null,
      null,
      null
      );
  }

  public Transition(
    Document context,
    StyleEnum style,
    Double duration
    )
  {
    this(
      context,
      style,
      duration,
      null,
      null,
      null,
      null
      );
  }

  public Transition(
    Document context,
    StyleEnum style,
    Double duration,
    OrientationEnum orientation,
    PageDirectionEnum pageDirection,
    DirectionEnum direction,
    Double scale
    )
  {
    this(context);

    setStyle(style);
    setDuration(duration);
    setOrientation(orientation);
    setPageDirection(pageDirection);
    setDirection(direction);
    setScale(scale);
  }

  public Transition(
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
  public Transition clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the transition direction.
  */
  public DirectionEnum getDirection(
    )
  {
    PdfAtomicObject<?> directionObject = (PdfAtomicObject<?>)getBaseDataObject().get(PdfName.Di);
    if(directionObject == null)
      return DirectionEnum.LeftToRight;

    return DirectionEnum.get(directionObject);
  }

  /**
    Gets the duration of the transition effect, in seconds.
  */
  public Double getDuration(
    )
  {
    IPdfNumber durationObject = (IPdfNumber)getBaseDataObject().get(PdfName.D);
    if(durationObject == null)
      return new Double(1);

    return durationObject.getNumberValue();
  }

  /**
    Gets the transition orientation.
  */
  public OrientationEnum getOrientation(
    )
  {
    PdfName orientationObject = (PdfName)getBaseDataObject().get(PdfName.Dm);
    if(orientationObject == null)
      return OrientationEnum.Horizontal;

    return OrientationEnum.get(orientationObject);
  }

  /**
    Gets the transition direction on page.
  */
  public PageDirectionEnum getPageDirection(
    )
  {
    PdfName pageDirectionObject = (PdfName)getBaseDataObject().get(PdfName.M);
    if(pageDirectionObject == null)
      return PageDirectionEnum.Inward;

    return PageDirectionEnum.get(pageDirectionObject);
  }

  /**
    Gets the scale at which the changes are drawn.
  */
  public Double getScale(
    )
  {
    IPdfNumber scaleObject = (IPdfNumber)getBaseDataObject().get(PdfName.SS);
    if(scaleObject == null)
      return new Double(1);

    return scaleObject.getNumberValue();
  }

  /**
    Gets the transition style.
  */
  public StyleEnum getStyle(
    )
  {
    PdfName styleObject = (PdfName)getBaseDataObject().get(PdfName.S);
    if(styleObject == null)
      return StyleEnum.Replace;

    return StyleEnum.get(styleObject);
  }

  /**
    @see #getDirection()
  */
  public void setDirection(
    DirectionEnum value
    )
  {
    if(value == null)
    {getBaseDataObject().remove(PdfName.Di);}
    else
    {getBaseDataObject().put(PdfName.Di,value.getCode());}
  }

  /**
    @see #getDuration()
  */
  public void setDuration(
    Double value
    )
  {
    if(value == null)
    {getBaseDataObject().remove(PdfName.D);}
    else
    {getBaseDataObject().put(PdfName.D,new PdfReal(value));}
  }

  /**
    @see #getOrientation()
  */
  public void setOrientation(
    OrientationEnum value
    )
  {
    if(value == null)
    {getBaseDataObject().remove(PdfName.Dm);}
    else
    {getBaseDataObject().put(PdfName.Dm,value.getCode());}
  }

  /**
    @see #getPageDirection()
  */
  public void setPageDirection(
    PageDirectionEnum value
    )
  {
    if(value == null)
    {getBaseDataObject().remove(PdfName.M);}
    else
    {getBaseDataObject().put(PdfName.M,value.getCode());}
  }

  /**
    @see #getScale()
  */
  public void setScale(
    Double value
    )
  {
    if(value == null)
    {getBaseDataObject().remove(PdfName.SS);}
    else
    {getBaseDataObject().put(PdfName.SS,new PdfReal(value));}
  }

  /**
    @see #getStyle()
  */
  public void setStyle(
    StyleEnum value
    )
  {
    if(value == null)
    {getBaseDataObject().remove(PdfName.S);}
    else
    {getBaseDataObject().put(PdfName.S,value.getCode());}
  }
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}