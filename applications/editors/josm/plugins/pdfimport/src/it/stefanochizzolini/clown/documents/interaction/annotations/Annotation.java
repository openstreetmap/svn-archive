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

package it.stefanochizzolini.clown.documents.interaction.annotations;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.documents.Page;
import it.stefanochizzolini.clown.documents.interaction.actions.Action;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfDate;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfInteger;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfReference;
import it.stefanochizzolini.clown.objects.PdfTextString;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.awt.geom.Rectangle2D;
import java.util.Date;
import java.util.EnumSet;

/**
  Annotation [PDF:1.6:8.4].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.7
*/
public class Annotation
  extends PdfObjectWrapper<PdfDictionary>
{
  // <class>
  // <classes>
  /**
    Field flags [PDF:1.6:8.4.2].
  */
  public enum FlagsEnum
  {
    // <class>
    // <static>
    // <fields>
    /**
      Hide the annotation, both on screen and on print,
      if it does not belong to one of the standard annotation types
      and no annotation handler is available.
    */
    Invisible(0x1),
    /**
      Hide the annotation, both on screen and on print
      (regardless of its annotation type or whether an annotation handler is available).
    */
    Hidden(0x2),
    /**
      Print the annotation when the page is printed.
    */
    Print(0x4),
    /**
      Do not scale the annotation's appearance to match the magnification of the page.
    */
    NoZoom(0x8),
    /**
      Do not rotate the annotation's appearance to match the rotation of the page.
    */
    NoRotate(0x10),
    /**
      Hide the annotation on the screen.
    */
    NoView(0x20),
    /**
      Do not allow the annotation to interact with the user.
    */
    ReadOnly(0x40),
    /**
      Do not allow the annotation to be deleted or its properties to be modified by the user.
    */
    Locked(0x80),
    /**
      Invert the interpretation of the NoView flag.
    */
    ToggleNoView(0x100);
    // </fields>

    // <interface>
    // <public>
    /**
      Converts an enumeration set into its corresponding bit mask representation.
    */
    public static int toInt(
      EnumSet<FlagsEnum> flags
      )
    {
      int flagsMask = 0;
      for(FlagsEnum flag : flags)
      {flagsMask |= flag.getCode();}

      return flagsMask;
    }

    /**
      Converts a bit mask into its corresponding enumeration representation.
    */
    public static EnumSet<FlagsEnum> toEnumSet(
      int flagsMask
      )
    {
      EnumSet<FlagsEnum> flags = EnumSet.noneOf(FlagsEnum.class);
      for(FlagsEnum flag : FlagsEnum.values())
      {
        if((flagsMask & flag.getCode()) > 0)
        {flags.add(flag);}
      }

      return flags;
    }
    // </public>
    // </interface>
    // </static>

    // <dynamic>
    // <fields>
    /**
      <h3>Remarks</h3>
      <p>Bitwise code MUST be explicitly distinct from the ordinal position of the enum constant
      as they don't coincide.</p>
    */
    private final int code;
    // </fields>

    // <constructors>
    private FlagsEnum(
      int code
      )
    {this.code = code;}
    // </constructors>

    // <interface>
    // <public>
    public int getCode(
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
  // </fields>

  // <interface>
  // <public>
  /**
    Wraps an annotation reference into an annotation object.

    @param reference Reference to an annotation object.
    @return Annotation object associated to the reference.
  */
  public static final Annotation wrap(
    PdfReference reference
    )
  {return wrap(reference,null);}

  /**
    Wraps an annotation base object into an annotation object.

    @param baseObject Annotation base object.
    @param container Annotation base object container.
    @return Annotation object associated to the base object.
  */
  public static final Annotation wrap(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {
    /*
      NOTE: This is a factory method for any annotation-derived object.
    */
    if(baseObject == null)
      return null;

    PdfDictionary dataObject = (PdfDictionary)File.resolve(baseObject);
    if(!dataObject.get(PdfName.Type).equals(PdfName.Annot))
      return null;

    PdfName annotationType = (PdfName)dataObject.get(PdfName.Subtype);
    if(annotationType.equals(PdfName.Text))
      return new Note(baseObject,container);
    else if(annotationType.equals(PdfName.Link))
      return new Link(baseObject,container);
    else if(annotationType.equals(PdfName.FreeText))
      return new CalloutNote(baseObject,container);
    else if(annotationType.equals(PdfName.Line))
      return new Line(baseObject,container);
    else if(annotationType.equals(PdfName.Square))
      return new Rectangle(baseObject,container);
    else if(annotationType.equals(PdfName.Circle))
      return new Ellipse(baseObject,container);
    else if(annotationType.equals(PdfName.Polygon))
      return new Polygon(baseObject,container);
    else if(annotationType.equals(PdfName.PolyLine))
      return new Polyline(baseObject,container);
    else if(annotationType.equals(PdfName.Highlight)
      || annotationType.equals(PdfName.Underline)
      || annotationType.equals(PdfName.Squiggly)
      || annotationType.equals(PdfName.StrikeOut))
      return new TextMarkup(baseObject,container);
    else if(annotationType.equals(PdfName.Stamp))
      return new RubberStamp(baseObject,container);
    else if(annotationType.equals(PdfName.Caret))
      return new Caret(baseObject,container);
    else if(annotationType.equals(PdfName.Ink))
      return new Scribble(baseObject,container);
    else if(annotationType.equals(PdfName.Popup))
      return new Popup(baseObject,container);
    else if(annotationType.equals(PdfName.FileAttachment))
      return new FileAttachment(baseObject,container);
    else if(annotationType.equals(PdfName.Sound))
      return new SoundAnnotation(baseObject,container);
    else if(annotationType.equals(PdfName.Movie))
      return new MovieAnnotation(baseObject,container);
    else if(annotationType.equals(PdfName.Widget))
      return new Widget(baseObject,container);
//TODO
//     else if(annotationType.equals(PdfName.Screen)) return new Screen(baseObject,container);
//     else if(annotationType.equals(PdfName.PrinterMark)) return new PrinterMark(baseObject,container);
//     else if(annotationType.equals(PdfName.TrapNet)) return new TrapNet(baseObject,container);
//     else if(annotationType.equals(PdfName.Watermark)) return new Watermark(baseObject,container);
//     else if(annotationType.equals(PdfName.3DAnnotation)) return new 3DAnnotation(baseObject,container);
    else // Other annotation type.
      return new Annotation(baseObject,container);
  }
  // </public>
  // </interface>
  // </static>

  // <dynamic>
  // <constructors>
  protected Annotation(
    Document context,
    PdfName subtype,
    Rectangle2D box,
    Page page
    )
  {
    super(
      context.getFile(),
      new PdfDictionary(
        new PdfName[]
        {
          PdfName.Type,
          PdfName.Subtype,
          PdfName.P,
          PdfName.Border
        },
        new PdfDirectObject[]
        {
          PdfName.Annot,
          subtype,
          page.getBaseObject(),
          new PdfArray(new PdfDirectObject[]{new PdfInteger(0),new PdfInteger(0),new PdfInteger(0)}) // NOTE: Hide border by default.
        }
        )
      );

    {
      setBox(box);

      PdfArray pageAnnotsObject = (PdfArray)File.resolve(page.getBaseDataObject().get(PdfName.Annots));
      if(pageAnnotsObject == null)
      {page.getBaseDataObject().put(PdfName.Annots,pageAnnotsObject = new PdfArray());}
      pageAnnotsObject.add(getBaseObject());
    }
  }

  protected Annotation(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {super(baseObject,container);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public Annotation clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the action to be performed when the annotation is activated.

    @since 0.0.8
  */
  public Action getAction(
    )
  {
    /*
      NOTE: 'A' entry may be undefined.
    */
    PdfDirectObject actionObject = getBaseDataObject().get(PdfName.A);
    if(actionObject == null)
      return null;

    return Action.wrap(actionObject,getContainer());
  }

  /**
    Gets the annotation's behavior in response to various trigger events.
  */
  public AnnotationActions getActions(
    )
  {
    PdfDirectObject actionsObject = getBaseDataObject().get(PdfName.AA);
    if(actionsObject == null)
      return null;

    return new AnnotationActions(this,actionsObject,getContainer());
  }

  /**
    Gets the appearance specifying how the annotation is presented visually on the page.
  */
  public Appearance getAppearance(
    )
  {
    /*
      NOTE: 'AP' entry may be undefined.
    */
    PdfDirectObject appearanceObject = getBaseDataObject().get(PdfName.AP);
    if(appearanceObject == null)
      return null;

    return new Appearance(appearanceObject,getContainer());
  }

  /**
    Gets the border style.
  */
  public Border getBorder(
    )
  {
    /*
      NOTE: 'BS' entry may be undefined.
    */
    PdfDirectObject borderObject = getBaseDataObject().get(PdfName.BS);
    if(borderObject == null)
      return null;

    return new Border(borderObject,getContainer());
  }

  /**
    Gets the annotation rectangle.
  */
  public Rectangle2D getBox(
    )
  {
    /*
      NOTE: 'Rect' entry MUST be defined.
    */
    it.stefanochizzolini.clown.objects.Rectangle box = new it.stefanochizzolini.clown.objects.Rectangle(getBaseDataObject().get(PdfName.Rect));
    return new Rectangle2D.Double(
      box.getX(),
      getPageHeight() - box.getY(),
      box.getWidth(),
      box.getHeight()
      );
  }

  /**
    Gets the annotation flags.
  */
  public EnumSet<FlagsEnum> getFlags(
    )
  {
    PdfInteger flagsObject = (PdfInteger)getBaseDataObject().get(PdfName.F);
    if(flagsObject == null)
      return EnumSet.noneOf(FlagsEnum.class);

    return FlagsEnum.toEnumSet(flagsObject.getRawValue());
  }

  /**
    Gets the date and time when the annotation was most recently modified.
  */
  public Date getModificationDate(
    )
  {
    /*
      NOTE: 'M' entry may be undefined.
    */
    PdfDirectObject modificationDateObject = getBaseDataObject().get(PdfName.M);
    if(modificationDateObject == null
      || !(modificationDateObject instanceof PdfDate)) // NOTE: Non-well-formed dates are ignored.
      return null;

    return (Date)((PdfDate)modificationDateObject).getValue();
  }

  /**
    Gets the annotation name.
    <h3>Remarks</h3>
    <p>The annotation name uniquely identifies the annotation among all the annotations on its page.</p>
  */
  public String getName(
    )
  {
    /*
      NOTE: 'NM' entry may be undefined.
    */
    PdfTextString nameObject = (PdfTextString)getBaseDataObject().get(PdfName.NM);
    if(nameObject == null)
      return null;

    return (String)nameObject.getValue();
  }

  /**
    Gets the associated page.
  */
  public Page getPage(
    )
  {
    /*
      NOTE: 'P' entry may be undefined.
    */
    PdfDirectObject pageObject = getBaseDataObject().get(PdfName.P);
    if(pageObject == null)
      return null;

    return new Page(pageObject);
  }

  /**
    Gets the annotation text.
    <h3>Remarks</h3>
    <p>Depending on the annotation type, the text may be either directly displayed
    or (in case of non-textual annotations) used as alternate description.</p>
  */
  public String getText(
    )
  {
    /*
      NOTE: 'Contents' entry may be undefined.
    */
    PdfTextString textObject = (PdfTextString)getBaseDataObject().get(PdfName.Contents);
    if(textObject == null)
      return null;

    return (String)textObject.getValue();
  }

  /**
    Gets whether to print the annotation when the page is printed.
  */
  public boolean isPrintable(
    )
  {return getFlags().contains(FlagsEnum.Print);}

  /**
    Gets whether the annotation is visible.
  */
  public boolean isVisible(
    )
  {return !getFlags().contains(FlagsEnum.Hidden);}

  /**
    @see #getAction()
  */
  public void setAction(
    Action value
    )
  {
    if(value == null)
    {getBaseDataObject().remove(PdfName.A);}
    else
    {getBaseDataObject().put(PdfName.A,value.getBaseObject());}
  }

  /**
    @see #getActions()
  */
  public void setActions(
    AnnotationActions value
    )
  {
    if(value == null)
    {getBaseDataObject().remove(PdfName.AA);}
    else
    {getBaseDataObject().put(PdfName.AA,value.getBaseObject());}
  }

  /**
    @see #getAppearance()
  */
  public void setAppearance(
    Appearance value
    )
  {
    if(value == null)
    {getBaseDataObject().remove(PdfName.AP);}
    else
    {getBaseDataObject().put(PdfName.AP, value.getBaseObject());}
  }

  /**
    @see #getBorder()
  */
  public void setBorder(
    Border value
    )
  {
    if(value == null)
    {getBaseDataObject().remove(PdfName.BS);}
    else
    {
      getBaseDataObject().put(PdfName.BS, value.getBaseObject());
      getBaseDataObject().remove(PdfName.Border);
    }
  }

  /**
    @see #getBox()
  */
  public void setBox(
    Rectangle2D value
    )
  {
    getBaseDataObject().put(
      PdfName.Rect,
      new it.stefanochizzolini.clown.objects.Rectangle(
        value.getX(),
        getPageHeight() - value.getY(),
        value.getWidth(),
        value.getHeight()
        ).getBaseDataObject()
      );
  }

  /**
    @see #getFlags()
  */
  public void setFlags(
    EnumSet<FlagsEnum> value
    )
  {getBaseDataObject().put(PdfName.F, new PdfInteger(FlagsEnum.toInt(value)));}

  /**
    @see #getModificationDate()
  */
  public void setModificationDate(
    Date value
    )
  {getBaseDataObject().put(PdfName.M, new PdfDate(value));}

  /**
    @see #getName()
  */
  public void setName(
    String value
    )
  {getBaseDataObject().put(PdfName.NM, new PdfTextString(value));}

  /**
    @see #getPage()
  */
  public void setPage(
    Page value
    )
  {getBaseDataObject().put(PdfName.P, value.getBaseObject());}

  /**
    @see #isPrintable()
  */
  public void setPrintable(
    boolean value
    )
  {
    EnumSet<FlagsEnum> flags = getFlags();
    if(value)
    {flags.add(FlagsEnum.Print);}
    else
    {flags.remove(FlagsEnum.Print);}
    setFlags(flags);
  }

  /**
    @see #getText()
  */
  public void setText(
    String value
    )
  {
    if(value == null)
    {getBaseDataObject().remove(PdfName.Contents);}
    else
    {getBaseDataObject().put(PdfName.Contents, new PdfTextString(value));}
  }

  /**
    @see #isVisible()
  */
  public void setVisible(
    boolean value
    )
  {
    EnumSet<FlagsEnum> flags = getFlags();
    if(value)
    {flags.remove(FlagsEnum.Hidden);}
    else
    {flags.add(FlagsEnum.Hidden);}
    setFlags(flags);
  }
  // </public>

  // <private>
  private double getPageHeight(
    )
  {
    Page page = getPage();
    return (page != null
        ? page.getBox().getHeight()
        : getDocument().getSize().getHeight());
  }
  // </private>
  // </interface>
  // </dynamic>
  // </class>
}