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

package it.stefanochizzolini.clown.documents.contents;

import it.stefanochizzolini.clown.documents.contents.colorSpaces.Color;
import it.stefanochizzolini.clown.documents.contents.colorSpaces.ColorSpace;
import it.stefanochizzolini.clown.documents.contents.colorSpaces.DeviceGrayColor;
import it.stefanochizzolini.clown.documents.contents.colorSpaces.DeviceGrayColorSpace;
import it.stefanochizzolini.clown.documents.contents.fonts.Font;
import it.stefanochizzolini.clown.documents.contents.objects.CompositeObject;
import it.stefanochizzolini.clown.documents.contents.objects.ContainerObject;
import it.stefanochizzolini.clown.documents.contents.objects.ContentObject;
import it.stefanochizzolini.clown.documents.contents.objects.InlineImage;
import it.stefanochizzolini.clown.documents.contents.objects.ShowText;
import it.stefanochizzolini.clown.documents.contents.objects.Text;
import it.stefanochizzolini.clown.documents.contents.objects.XObject;
import it.stefanochizzolini.clown.objects.PdfName;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
  Content objects scanner.
  <h3>Remarks</h3>
  <p>It wraps the content objects collection ({@link Contents}) to scan its graphics state
  through a forward cursor.</p>
  <p>Scanning is performed at an arbitrary depth, according to the content objects nesting:
  each depth level corresponds to a scan level so that at any time it's possible
  to seamlessly navigate across the levels (see {@link #getParentLevel() },
  {@link #getChildLevel() })</p>

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.4
  @version 0.0.8
*/
public final class ContentScanner
{
  // <class>
  // <classes>
  /**
    Graphics state [PDF:1.6:4.3].
  */
  public static final class GraphicsState
    implements Cloneable
  {
    // <class>
    // <static>
    // <interface>
    // <public>
    /**
      Concatenates the given coordinate matrices.
    */
    public static double[] concat(
      double[] matrix1,
      double[] matrix2
      )
    {
      double[] result = new double[6];
      result[0] = matrix1[0]*matrix2[0] + matrix1[1]*matrix2[2]; // a.
      result[1] = matrix1[0]*matrix2[1] + matrix1[1]*matrix2[3]; // b.
      result[2] = matrix1[2]*matrix2[0] + matrix1[3]*matrix2[2]; // c.
      result[3] = matrix1[2]*matrix2[1] + matrix1[3]*matrix2[3]; // d.
      result[4] = matrix1[4]*matrix2[0] + matrix1[5]*matrix2[2] + 1*matrix2[4]; // e.
      result[5] = matrix1[4]*matrix2[1] + matrix1[5]*matrix2[3] + 1*matrix2[5]; // f.

      return result;
    }
    // </public>
    // </interface>
    // </static>

    // <dynamic>
    // <fields>
    /**
      Current character spacing [PDF:1.6:5.2.1].
    */
    public double charSpace;
    /**
      Current transformation matrix.
    */
    public double[] ctm;
    /**
      Current color for nonstroking operations [PDF:1.6:4.5.1].
    */
    public Color fillColor;
    /**
      Current color space for nonstroking operations [PDF:1.6:4.5.1].
    */
    public ColorSpace fillColorSpace;
    /**
      Current font [PDF:1.6:5.2].
    */
    public Font font;
    /**
      Current font size [PDF:1.6:5.2].
    */
    public double fontSize;
    /**
      Current leading [PDF:1.6:5.2.4].
    */
    public double lead;
    /**
      Current line cap style [PDF:1.6:4.3.2].
    */
    public LineCapEnum lineCap;
    /**
      Current line dash pattern [PDF:1.6:4.3.2].
    */
    public LineDash lineDash;
    /**
      Current line join style [PDF:1.6:4.3.2].
    */
    public LineJoinEnum lineJoin;
    /**
      Current line width [PDF:1.6:4.3.2].
    */
    public double lineWidth;
    /**
      Current miter limit [PDF:1.6:4.3.2].
    */
    public double miterLimit;
    /**
      Current text rendering mode [PDF:1.6:5.2.5].
    */
    public TextRenderModeEnum renderMode;
    /**
      Current text rise [PDF:1.6:5.2.6].
    */
    public double rise;
    /**
      Current horizontal scaling [PDF:1.6:5.2.3].
    */
    public double scale;
    /**
      Current color for stroking operations [PDF:1.6:4.5.1].
    */
    public Color strokeColor;
    /**
      Current color space for stroking operations [PDF:1.6:4.5.1].
    */
    public ColorSpace strokeColorSpace;
    /**
      Text line matrix [PDF:1.6:5.3].
    */
    public double[] tlm;
    /**
      Text matrix [PDF:1.6:5.3].
    */
    public double[] tm;
    /**
      Current word spacing [PDF:1.6:5.2.2].
    */
    public double wordSpace;

    private ContentScanner scanner;
    // </fields>

    // <constructors>
    private GraphicsState(
      ContentScanner scanner
      )
    {
      this.scanner = scanner;
      initialize();
    }
    // </constructors>

    // <interface>
    // <public>
    /**
      Gets a deep copy of the graphics state object.
    */
    @Override
    public GraphicsState clone(
      )
    {
      GraphicsState clone;

      // Shallow copy.
      try
      {clone = (GraphicsState)super.clone();}
      catch(CloneNotSupportedException e)
      {throw new RuntimeException(e); /* NOTE: It should never happen. */}

      // Deep copy.
      /* NOTE: Mutable objects are to be cloned. */

      return clone;
    }

    public void copyTo(
      GraphicsState state
      )
    {//TODO:cache fields!!!
      ContentScanner stateScanner = state.scanner;
      for(Field field : GraphicsState.class.getDeclaredFields())
      {
        try
        {field.set(state,field.get(this));}
        catch (IllegalAccessException e)
        {throw new RuntimeException(e);}
      }
      state.scanner = stateScanner;
//TODO:temporary hack (define TextState for textual parameters!)...
      if(!(state.scanner.getParent() instanceof Text))
      {
        state.tlm = new double[]{1,0,0,1,0,0};
        state.tm = new double[]{1,0,0,1,0,0};
      }
    }

    /**
      Gets the scanner associated to this state.
    */
    public ContentScanner getScanner(
      )
    {return scanner;}

    /**
      Resolves the given text-space point to its equivalent device-space one [PDF:1.6:5.3.3].
    */
    public Point2D textToDeviceSpace(
      Point2D point
      )
    {
      /*
        NOTE: The text rendering matrix (trm) is obtained from the concatenation
        of the text parameters (fontSize, scale, rise) matrix, the text matrix (tm) and the CTM.
      */
      double[] trm = concat(tm,ctm);

      return new Point2D.Double(
        trm[0] * point.getX() + trm[2] * point.getY() + trm[4],
        scanner.getContentContext().getBox().getHeight() - (trm[1] * point.getX() + trm[3] * point.getY() + trm[5])
        );
    }

    /**
      Resolves the given user-space point to its equivalent device-space one [PDF:1.6:4.2.3].
    */
    public Point2D userToDeviceSpace(
      Point2D point
      )
    {
      return new Point2D.Double(
        ctm[0] * point.getX() + ctm[2] * point.getY() + ctm[4],
        ctm[1] * point.getX() + ctm[3] * point.getY() + ctm[5]
        );
    }
    // </public>

    // <private>
    private GraphicsState clone(
      ContentScanner scanner
      )
    {
      GraphicsState state = clone();
      state.scanner = scanner;

      return state;
    }

    private void initialize(
      )
    {
      charSpace = 0;
      ctm = new double[]{1,0,0,1,0,0};
      fillColor = DeviceGrayColor.Default;
      fillColorSpace = DeviceGrayColorSpace.Default;
      font = null;
      fontSize = 0;
      lead = 0;
      lineCap = LineCapEnum.Butt;
      lineDash = new LineDash();
      lineJoin = LineJoinEnum.Miter;
      lineWidth = 1;
      miterLimit = 10;
      renderMode = TextRenderModeEnum.Fill;
      rise = 0;
      scale = 100;
      strokeColor = DeviceGrayColor.Default;
      strokeColorSpace = DeviceGrayColorSpace.Default;
      tlm = new double[]{1,0,0,1,0,0};
      tm = new double[]{1,0,0,1,0,0};
      wordSpace = 0;
    }
    // </private>
    // </interface>
    // </dynamic>
    // </class>
  }

  /**
    Object information.
    <h3>Remarks</h3>
    <p>This class provides derivative (higher-level) information
    about the currently scanned object.</p>
  */
  public static abstract class GraphicsObjectWrapper<TDataObject extends ContentObject>
  {
    // <static>
    @SuppressWarnings("unchecked")
    private static GraphicsObjectWrapper get(
      ContentScanner scanner
      )
    {
      ContentObject object = scanner.getCurrent();
      if(object instanceof ShowText)
        return new TextStringWrapper(scanner);
      else if(object instanceof Text)
        return new TextWrapper(scanner);
      else if(object instanceof XObject)
        return new XObjectWrapper(scanner);
      else if(object instanceof InlineImage)
        return new InlineImageWrapper(scanner);
      else
        return null;
    }
    // </static>

    // <dynamic>
    // <fields>
    protected Rectangle2D box;

    private TDataObject baseDataObject;
    // </fields>

    // <constructors>
    protected GraphicsObjectWrapper(
      TDataObject baseDataObject
      )
    {this.baseDataObject = baseDataObject;}
    // </constructors>

    // <interface>
    // <public>
    /**
      Gets the underlying data object.
    */
    public TDataObject getBaseDataObject(
      )
    {return baseDataObject;}

    /**
      Gets the object's bounding box.
    */
    public Rectangle2D getBox(
      )
    {return box;}
    // </public>
    // </interface>
    // </dynamic>
  }

  /**
    Inline image information.
  */
  public static final class InlineImageWrapper
    extends GraphicsObjectWrapper<InlineImage>
  {
    private InlineImageWrapper(
      ContentScanner scanner
      )
    {
      super((InlineImage)scanner.getCurrent());

      double[] ctm = scanner.getState().ctm;
      this.box = new Rectangle2D.Double(
        ctm[4],
        scanner.getContentContext().getBox().getHeight() - ctm[5],
        ctm[0],
        Math.abs(ctm[3])
        );
    }

    /**
      Gets the inline image.
    */
    public InlineImage getInlineImage(
      )
    {return getBaseDataObject();}
  }

  /**
    Text information.
  */
  public static final class TextWrapper
    extends GraphicsObjectWrapper<Text>
  {
    private List<TextStringWrapper> textStrings;

    private TextWrapper(
      ContentScanner scanner
      )
    {
      super((Text)scanner.getCurrent());

      textStrings = new ArrayList<TextStringWrapper>();
      extract(scanner.getChildLevel());
    }
    
    @Override
    public Rectangle2D getBox(
      )
    {
      if(box == null)
      {
        for(TextStringWrapper textString : textStrings)
        {
          if(box == null)
          {box = (Rectangle2D)textString.getBox().clone();}
          else
          {box.add(textString.getBox());}
        }
      }
      return box;
    }

    /**
      Gets the text strings.
    */
    public List<TextStringWrapper> getTextStrings(
      )
    {return textStrings;}

    private void extract(
      ContentScanner level
      )
    {
      if(level == null)
        return;

      while(level.moveNext())
      {
        ContentObject content = level.getCurrent();
        if(content instanceof ShowText)
        {textStrings.add((TextStringWrapper)level.getCurrentWrapper());}
        else if(content instanceof ContainerObject)
        {extract(level.getChildLevel());}
      }
    }
  }

  /**
    Text string information.
  */
  public static final class TextStringWrapper
    extends GraphicsObjectWrapper<ShowText>
    implements ITextString
  {
    private TextStyle style;
    private List<TextChar> textChars;

    TextStringWrapper(
      ContentScanner scanner
      )
    {
      super((ShowText)scanner.getCurrent());

      textChars = new ArrayList<TextChar>();
      {
        GraphicsState state = scanner.getState();
        style = new TextStyle(
          state.font,
          state.fontSize,
          state.renderMode,
          state.strokeColor,
          state.strokeColorSpace,
          state.fillColor,
          state.fillColorSpace
          );
        getBaseDataObject().scan(
          state,
          new ShowText.IScanner()
          {
            public void scanChar(
              char textChar,
              Rectangle2D textCharBox
              )
            {
              textChars.add(
                new TextChar(
                  textChar,
                  textCharBox,
                  style,
                  false
                  )
                );
            }
            public void scanFont(
              double fontSize
              )
            {style.fontSize = fontSize;}
          }
          );
      }
    }

    @Override
    public Rectangle2D getBox(
      )
    {
      if(box == null)
      {
        for(TextChar textChar : textChars)
        {
          if(box == null)
          {box = (Rectangle2D)textChar.box.clone();}
          else
          {box.add(textChar.box);}
        }
      }
      return box;
    }

    /**
      Gets the text style.
     */
    public TextStyle getStyle(
      )
    {return style;}

    public String getText(
      )
    {
      StringBuilder textBuilder = new StringBuilder();
      for(TextChar textChar : textChars)
      {textBuilder.append(textChar);}
      return textBuilder.toString();
    }

    public List<TextChar> getTextChars(
      )
    {return textChars;}
  }

  /**
    External object information.
  */
  public static final class XObjectWrapper
    extends GraphicsObjectWrapper<XObject>
  {
    private PdfName name;
    private it.stefanochizzolini.clown.documents.contents.xObjects.XObject xObject;

    private XObjectWrapper(
      ContentScanner scanner
      )
    {
      super((XObject)scanner.getCurrent());

      IContentContext context = scanner.getContentContext();
      double[] ctm = scanner.getState().ctm;
      this.box = new Rectangle2D.Double(
        ctm[4],
        context.getBox().getHeight() - ctm[5],
        ctm[0],
        Math.abs(ctm[3])
        );
      this.name = getBaseDataObject().getName();
      this.xObject = context.getResources().getXObjects().get(name);
    }

    /**
      Gets the corresponding resource key.
    */
    public PdfName getName(
      )
    {return name;}

    /**
      Gets the external object.
    */
    public it.stefanochizzolini.clown.documents.contents.xObjects.XObject getXObject(
      )
    {return xObject;}
  }
  // </classes>

  // <static>
  // <fields>
  private static final int StartIndex = -1;
  // </fields>
  // </static>

  // <dynamic>
  // <fields>
  /**
    Child level.
  */
  private ContentScanner childLevel;
  /**
    Content objects collection.
  */
  private Contents contents;
  /**
    Current object index at this level.
  */
  private int index;
  /**
    Object collection at this level.
  */
  private List<ContentObject> objects;
  /**
    Parent level.
  */
  private ContentScanner parentLevel;
  /**
    Current graphics state.
  */
  private GraphicsState state;
  // </fields>

  // <constructors>
  /**
    @param contents Content objects collection to scan.
  */
  public ContentScanner(
    Contents contents
    )
  {
    this.parentLevel = null;
    this.objects = this.contents = contents;

    moveStart();
  }

  /**
    @param contentContext Content context containing the content objects collection to scan.
  */
  public ContentScanner(
    IContentContext contentContext
    )
  {this(contentContext.getContents());}

  /**
    @param parentLevel Parent scan level.
  */
  private ContentScanner(
    ContentScanner parentLevel
    )
  {
    this.parentLevel = parentLevel;
    this.contents = parentLevel.contents;
    this.objects = ((CompositeObject)parentLevel.getCurrent()).getObjects();

    moveStart();
  }
  // </constructors>

  // <interface>
  // <public>
  /**
    Gets the current child scan level.

    @see #getParentLevel()
  */
  public ContentScanner getChildLevel(
    )
  {return childLevel;}

  /**
    Gets the content context associated to the content objects collection.
  */
  public IContentContext getContentContext(
    )
  {return contents.getContentContext();}

  /**
    Gets the content objects collection this scanner is inspecting.
  */
  public Contents getContents(
    )
  {return contents;}

  /**
    Gets the current content object.

    @see #getIndex()
    @see #getParent()
  */
  public ContentObject getCurrent(
    )
  {
    try
    {return objects.get(index);}
    catch(Exception e)
    {return null;}
  }

  /**
    Gets the current content object's information.

    @see #getCurrent()
  */
  @SuppressWarnings("unchecked")
  public GraphicsObjectWrapper getCurrentWrapper(
    )
  {return GraphicsObjectWrapper.get(this);}

  /**
    Gets the current position.

    @see #getCurrent()
  */
  public int getIndex(
    )
  {return index;}

  /**
    Gets the current parent object.

    @see #getCurrent()
  */
  public CompositeObject getParent(
    )
  {return (parentLevel == null ? null : (CompositeObject)parentLevel.getCurrent());}

  /**
    Gets the parent scan level.

    @see #getChildLevel()
  */
  public ContentScanner getParentLevel(
    )
  {return parentLevel;}

  /**
    Gets the current graphics state applied to the current content object.
    <h3>Remarks</h3>
    <p>The returned object of this method is fundamental for any content manipulation
    as it represents the actual constraints that affect the current content object
    rendering.</p>
  */
  public GraphicsState getState(
    )
  {return state;}

  /**
    Inserts a content object at the current position.
  */
  public void insert(
    ContentObject object
    )
  {
    if(index == -1)
    {index = 0;}

    objects.add(index,object);
    refresh();
  }

  /**
    Inserts content objects at the current position.
    <h3>Remarks</h3>
    <p>After insertion complete, lastly-inserted content object is at the current position.</p>
  */
  public void insert(
    Collection<? extends ContentObject> objects
    )
  {
    int index = 0;
    int size = objects.size();
    for(ContentObject object : objects)
    {
      insert(object);

      if(++index < size)
      {moveNext();}
    }
  }

  /**
    Moves to the object at the given position.

    @since 0.0.8
    @param index New position.
    @return Whether the object was successfully reached.
  */
  public boolean move(
    int index
    )
  {
    if(this.index > index)
    {moveStart();}
    while(this.index < index
      && moveNext())
    {}
    return getCurrent() != null;
  }

  /**
    Moves after the last object.

    @since 0.0.8
  */
  public void moveEnd(
    )
  {moveLast(); moveNext();}

  /**
    Moves to the first object.

    @since 0.0.5
    @return Whether the first object was successfully reached.
  */
  public boolean moveFirst(
    )
  {moveStart(); return moveNext();}

  /**
    Moves to the last object.

    @since 0.0.5
    @return Whether the last object was successfully reached.
  */
  public boolean moveLast(
    )
  {
    int lastIndex = objects.size()-1;
    while(index < lastIndex)
    {moveNext();}
    return getCurrent() != null;
  }

  /**
    Moves to the next object.

    @return Whether the next object was successfully reached.
  */
  public boolean moveNext(
    )
  {
    // Updating the current graphics state...
    ContentObject currentObject = getCurrent();
    if(currentObject != null)
    {currentObject.applyTo(state);}

    // Moving to the next object...
    if(index < objects.size())
    {
      index++;
      refresh();
    }

    return getCurrent() != null;
  }

  /**
    Moves before the first object.

    @since 0.0.8
  */
  public void moveStart(
    )
  {
    index = StartIndex;
    if(state == null)
    {
      if(parentLevel == null)
      {state = new GraphicsState(this);}
      else
      {state = parentLevel.state.clone(this);}
    }
    else
    {
      if(parentLevel == null)
      {state.initialize();}
      else
      {parentLevel.state.copyTo(state);}
    }
    refresh();
  }

  /**
    Removes the content object at the current position.

    @return Removed object.
  */
  public ContentObject remove(
    )
  {
    ContentObject removedObject = objects.remove(index);
    refresh();

    return removedObject;
  }

  /**
    Replaces the content object at the current position.

    @return Replaced object.
  */
  public ContentObject setCurrent(
    ContentObject value
    )
  {
    ContentObject replacedObject = objects.set(index,value);
    refresh();

    return replacedObject;
  }
  // </public>

  // <private>
  /**
    Synchronizes the scanner state.
  */
  private void refresh(
    )
  {
    if(getCurrent() instanceof CompositeObject)
    {childLevel = new ContentScanner(this);}
    else
    {childLevel = null;}
  }
  // </private>
  // </interface>
  // </dynamic>
  // </class>
}