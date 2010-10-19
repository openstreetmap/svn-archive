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

package it.stefanochizzolini.clown.documents.contents.composition;

import it.stefanochizzolini.clown.documents.Page;
import it.stefanochizzolini.clown.documents.contents.ContentScanner;
import it.stefanochizzolini.clown.documents.contents.FontResources;
import it.stefanochizzolini.clown.documents.contents.IContentContext;
import it.stefanochizzolini.clown.documents.contents.LineCapEnum;
import it.stefanochizzolini.clown.documents.contents.LineJoinEnum;
import it.stefanochizzolini.clown.documents.contents.Resources;
import it.stefanochizzolini.clown.documents.contents.TextRenderModeEnum;
import it.stefanochizzolini.clown.documents.contents.XObjectResources;
import it.stefanochizzolini.clown.documents.contents.colorSpaces.Color;
import it.stefanochizzolini.clown.documents.contents.colorSpaces.ColorSpace;
import it.stefanochizzolini.clown.documents.contents.colorSpaces.DeviceCMYKColorSpace;
import it.stefanochizzolini.clown.documents.contents.colorSpaces.DeviceGrayColorSpace;
import it.stefanochizzolini.clown.documents.contents.colorSpaces.DeviceRGBColorSpace;
import it.stefanochizzolini.clown.documents.contents.fonts.Font;
import it.stefanochizzolini.clown.documents.contents.objects.BeginMarkedContent;
import it.stefanochizzolini.clown.documents.contents.objects.BeginSubpath;
import it.stefanochizzolini.clown.documents.contents.objects.CloseSubpath;
import it.stefanochizzolini.clown.documents.contents.objects.CompositeObject;
import it.stefanochizzolini.clown.documents.contents.objects.ContentObject;
import it.stefanochizzolini.clown.documents.contents.objects.DrawCurve;
import it.stefanochizzolini.clown.documents.contents.objects.DrawLine;
import it.stefanochizzolini.clown.documents.contents.objects.DrawRectangle;
import it.stefanochizzolini.clown.documents.contents.objects.EndPathNoOp;
import it.stefanochizzolini.clown.documents.contents.objects.Fill;
import it.stefanochizzolini.clown.documents.contents.objects.FillStroke;
import it.stefanochizzolini.clown.documents.contents.objects.LocalGraphicsState;
import it.stefanochizzolini.clown.documents.contents.objects.MarkedContent;
import it.stefanochizzolini.clown.documents.contents.objects.ModifyCTM;
import it.stefanochizzolini.clown.documents.contents.objects.ModifyClipPath;
import it.stefanochizzolini.clown.documents.contents.objects.PaintXObject;
import it.stefanochizzolini.clown.documents.contents.objects.SetCharSpace;
import it.stefanochizzolini.clown.documents.contents.objects.SetFillColor;
import it.stefanochizzolini.clown.documents.contents.objects.SetFillColorSpace;
import it.stefanochizzolini.clown.documents.contents.objects.SetFont;
import it.stefanochizzolini.clown.documents.contents.objects.SetLineCap;
import it.stefanochizzolini.clown.documents.contents.objects.SetLineDash;
import it.stefanochizzolini.clown.documents.contents.objects.SetLineJoin;
import it.stefanochizzolini.clown.documents.contents.objects.SetLineWidth;
import it.stefanochizzolini.clown.documents.contents.objects.SetMiterLimit;
import it.stefanochizzolini.clown.documents.contents.objects.SetStrokeColor;
import it.stefanochizzolini.clown.documents.contents.objects.SetStrokeColorSpace;
import it.stefanochizzolini.clown.documents.contents.objects.SetTextLead;
import it.stefanochizzolini.clown.documents.contents.objects.SetTextMatrix;
import it.stefanochizzolini.clown.documents.contents.objects.SetTextRenderMode;
import it.stefanochizzolini.clown.documents.contents.objects.SetTextRise;
import it.stefanochizzolini.clown.documents.contents.objects.SetTextScale;
import it.stefanochizzolini.clown.documents.contents.objects.SetWordSpace;
import it.stefanochizzolini.clown.documents.contents.objects.ShowSimpleText;
import it.stefanochizzolini.clown.documents.contents.objects.Stroke;
import it.stefanochizzolini.clown.documents.contents.objects.Text;
import it.stefanochizzolini.clown.documents.contents.objects.TranslateTextRelative;
import it.stefanochizzolini.clown.documents.contents.objects.TranslateTextToNextLine;
import it.stefanochizzolini.clown.documents.contents.xObjects.XObject;
import it.stefanochizzolini.clown.documents.interaction.actions.Action;
import it.stefanochizzolini.clown.documents.interaction.annotations.Link;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

/**
  Content stream primitive filter.
  <p>It provides the basic (primitive) operations described by the PDF specification
  for graphics content composition.</p>
  <h3>Remarks</h3>
  <p>This class leverages the object-oriented content stream modelling infrastructure,
  which encompasses 1st-level content stream objects (operations),
  2nd-level content stream objects (graphics objects) and full graphics state support.</p>

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.4
  @version 0.0.8
*/
public class PrimitiveFilter
{
  // <class>
  // <dynamic>
  // <fields>
  private ContentScanner scanner;
  // </fields>

  // <constructors>
  public PrimitiveFilter(
    ContentScanner scanner
    )
  {setScanner(scanner);}

  public PrimitiveFilter(
    IContentContext context
    )
  {
    this(
      new ContentScanner(context.getContents())
      );
  }
  // </constructors>

  // <interface>
  // <public>
  /**
    Adds a content object.

    @return The added content object.
  */
  public ContentObject add(
    ContentObject object
    )
  {
    scanner.insert(object);
    scanner.moveNext();

    return object;
  }

  /**
    Applies a transformation to the coordinate system from user space to device space [PDF:1.6:4.3.3].
    <h3>Remarks</h3>
    <p>The transformation is applied to the current transformation matrix (CTM) by concatenation,
    i.e. it doesn't replace it.</p>

    @see #setMatrix(double,double,double,double,double,double)

    @param a Item 0,0 of the matrix.
    @param b Item 0,1 of the matrix.
    @param c Item 1,0 of the matrix.
    @param d Item 1,1 of the matrix.
    @param e Item 2,0 of the matrix.
    @param f Item 2,1 of the matrix.
  */
  public void applyMatrix(
    double a,
    double b,
    double c,
    double d,
    double e,
    double f
    )
  {add(new ModifyCTM(a,b,c,d,e,f));}

  /**
    Adds a composite object beginning it.

    @see #end()

    @return The added composite object.
  */
  public CompositeObject begin(
    CompositeObject object
    )
  {
    // Insert the new object at the current level!
    scanner.insert(object);
    // The new object's children level is the new current level!
    scanner = scanner.getChildLevel();

    return object;
  }

  /**
    Begins a new nested graphics state context [PDF:1.6:4.3.1].

    @see #end()

    @return The added local graphics state object.
  */
  public LocalGraphicsState beginLocalState(
    )
  {return (LocalGraphicsState)begin(new LocalGraphicsState());}

  /**
    Begins a new marked-content sequence [PDF:1.6:10.5].

    @see #end()

    @return The added marked-content sequence.
  */
  public MarkedContent beginMarkedContent(
    PdfName tag
    )
  {
    return (MarkedContent)begin(
      new MarkedContent(
        new BeginMarkedContent(tag)
        )
      );
  }

  /**
    Modifies the current clipping path by intersecting it with the current path [PDF:1.6:4.4.1].
    <h3>Remarks</h3>
    <p>It can be validly called only just before painting the current path.</p>
  */
  public void clip(
    )
  {
    add(ModifyClipPath.Value);
    add(EndPathNoOp.Value);
  }

  /**
    Closes the current subpath by appending a straight line segment
    from the current point to the starting point of the subpath [PDF:1.6:4.4.1].
  */
  public void closePath(
    )
  {add(CloseSubpath.Value);}

  /**
    Draws a circular arc.

    @see #stroke()
    @since 0.0.7

    @param location Arc location.
    @param startAngle Starting angle.
    @param endAngle Ending angle.
  */
  public void drawArc(
    RectangularShape location,
    double startAngle,
    double endAngle
    )
  {drawArc(location,startAngle,endAngle,0,1);}

  /**
    Draws an arc.

    @see #stroke()
    @since 0.0.7

    @param location Arc location.
    @param startAngle Starting angle.
    @param endAngle Ending angle.
    @param branchWidth Distance between the spiral branches. '0' value degrades to a circular arc.
    @param branchRatio Linear coefficient applied to the branch width. '1' value degrades to a constant branch width.
  */
  public void drawArc(
    RectangularShape location,
    double startAngle,
    double endAngle,
    double branchWidth,
    double branchRatio
    )
  {drawArc(location,startAngle,endAngle,branchWidth,branchRatio,true);}

  /**
    Draws a cubic Bezier curve from the current point [PDF:1.6:4.4.1].

    @see #stroke()
    @since 0.0.7

    @param endPoint Ending point.
    @param startControl Starting control point.
    @param endControl Ending control point.
  */
  public void drawCurve(
    Point2D endPoint,
    Point2D startControl,
    Point2D endControl
    )
  {
    double contextHeight = scanner.getContentContext().getBox().getHeight();
    add(
      new DrawCurve(
        endPoint.getX(),
        contextHeight - endPoint.getY(),
        startControl.getX(),
        contextHeight - startControl.getY(),
        endControl.getX(),
        contextHeight - endControl.getY()
        )
      );
  }

  /**
    Draws a cubic Bezier curve [PDF:1.6:4.4.1].

    @see #stroke()
    @since 0.0.7

    @param startPoint Starting point.
    @param endPoint Ending point.
    @param startControl Starting control point.
    @param endControl Ending control point.
  */
  public void drawCurve(
    Point2D startPoint,
    Point2D endPoint,
    Point2D startControl,
    Point2D endControl
    )
  {
    beginSubpath(startPoint);
    drawCurve(endPoint,startControl,endControl);
  }

  /**
    Draws an ellipse.
    @since 0.0.7

    @see #fill()
    @see #fillStroke()
    @see #stroke()

    @param location Ellipse location.
  */
  public void drawEllipse(
    RectangularShape location
    )
  {drawArc(location,0,360);}

  /**
    Draws a line from the current point [PDF:1.6:4.4.1].

    @see #stroke()
    @since 0.0.7

    @param endPoint Ending point.
  */
  public void drawLine(
    Point2D endPoint
    )
  {
    add(
      new DrawLine(
        endPoint.getX(),
        scanner.getContentContext().getBox().getHeight() - endPoint.getY()
        )
      );
  }

  /**
    Draws a line [PDF:1.6:4.4.1].

    @see #stroke()
    @since 0.0.7

    @param startPoint Starting point.
    @param endPoint Ending point.
  */
  public void drawLine(
    Point2D startPoint,
    Point2D endPoint
    )
  {
    beginSubpath(startPoint);
    drawLine(endPoint);
  }

  /**
    Draws a polygon.
    <h3>Remarks</h3>
    <p>A polygon is the same as a multiple line except that it's a closed path.</p>

    @see #fill()
    @see #fillStroke()
    @see #stroke()
    @since 0.0.7

    @param points Points.
  */
  public void drawPolygon(
    Point2D[] points
    )
  {
    drawPolyline(points);
    closePath();
  }

  /**
    Draws a multiple line.

    @see #stroke()
    @since 0.0.7

    @param points Points.
  */
  public void drawPolyline(
    Point2D[] points
    )
  {
    beginSubpath(points[0]);
    for(
      int index = 1,
        length = points.length;
      index < length;
      index++
      )
    {drawLine(points[index]);}
  }

  /**
    Draws a rectangle [PDF:1.6:4.4.1].

    @see #fill()
    @see #fillStroke()
    @see #stroke()

    @param location Rectangle location.
  */
  public void drawRectangle(
    RectangularShape location
    )
  {drawRectangle(location,0);}

  /**
    Draws a rounded rectangle.

    @see #fill()
    @see #fillStroke()
    @see #stroke()

    @param location Rectangle location.
    @param radius Vertex radius, '0' value degrades to squared vertices.
  */
  public void drawRectangle(
    RectangularShape location,
    double radius
    )
  {
    if(radius == 0)
    {
      add(
        new DrawRectangle(
          location.getX(),
          scanner.getContentContext().getBox().getHeight() - location.getY() - location.getHeight(),
          location.getWidth(),
          location.getHeight()
          )
        );
    }
    else
    {
      final double endRadians = Math.PI * 2;
      final double quadrantRadians = Math.PI / 2;
      double radians = 0;
      while(radians < endRadians)
      {
        double radians2 = radians + quadrantRadians;
        int sin2 = (int)Math.sin(radians2);
        int cos2 = (int)Math.cos(radians2);
        double x1 = 0, x2 = 0, y1 = 0, y2 = 0;
        double xArc = 0, yArc = 0;
        if(cos2 == 0)
        {
          if(sin2 == 1)
          {
            x1 = x2 = location.getX() + location.getWidth();
            y1 = location.getY() + location.getHeight() - radius;
            y2 = location.getY() + radius;

            xArc =- radius * 2;
            yArc =- radius;

            beginSubpath(new Point2D.Double(x1,y1));
          }
          else
          {
            x1 = x2 = location.getX();
            y1 = location.getY() + radius;
            y2 = location.getY() + location.getHeight() - radius;

            yArc =- radius;
          }
        }
        else if(cos2 == 1)
        {
          x1 = location.getX() + radius;
          x2 = location.getX() + location.getWidth() - radius;
          y1 = y2 = location.getY() + location.getHeight();

          xArc =- radius;
          yArc =- radius*2;
        }
        else if(cos2 == -1)
        {
          x1 = location.getX() + location.getWidth() - radius;
          x2 = location.getX() + radius;
          y1 = y2 = location.getY();

          xArc=-radius;
        }
        drawLine(
          new Point2D.Double(x2,y2)
          );
        drawArc(
          new Rectangle2D.Double(x2+xArc, y2+yArc, radius*2, radius*2),
          Math.toDegrees(radians),
          Math.toDegrees(radians2),
          0,
          1,
          false
          );

        radians = radians2;
      }
    }
  }

  /**
    Draws a spiral.

    @see #stroke()
    @since 0.0.7

    @param center Spiral center.
    @param startAngle Starting angle.
    @param endAngle Ending angle.
    @param branchWidth Distance between the spiral branches.
    @param branchRatio Linear coefficient applied to the branch width.
  */
  public void drawSpiral(
    Point2D center,
    double startAngle,
    double endAngle,
    double branchWidth,
    double branchRatio
    )
  {
    drawArc(
      new Rectangle2D.Double(center.getX(),center.getY(),0.0001,0.0001),
      startAngle,
      endAngle,
      branchWidth,
      branchRatio
      );
  }

  /**
    Ends the current (innermostly-nested) composite object.

    @see #begin(CompositeObject)
  */
  public void end(
    )
  {
    scanner = scanner.getParentLevel();
    scanner.moveNext();
  }

  /**
    Fills the path using the current color [PDF:1.6:4.4.2].

    @see #setFillColor(Color)
  */
  public void fill(
    )
  {add(Fill.Value);}

  /**
    Fills and then strokes the path using the current colors [PDF:1.6:4.4.2].

    @see #setFillColor(Color)
    @see #setStrokeColor(Color)
  */
  public void fillStroke(
    )
  {add(FillStroke.Value);}

  /**
    Serializes the contents into the content stream.
  */
  public void flush(
    )
  {scanner.getContents().flush();}

  /**
    Gets the content stream scanner.
  */
  public ContentScanner getScanner(
    )
  {return scanner;}

  /**
    Gets the current graphics state [PDF:1.6:4.3].
  */
  public ContentScanner.GraphicsState getState(
    )
  {return scanner.getState();}

  /**
    Applies a rotation to the coordinate system from user space to device space [PDF:1.6:4.2.2].

    @see #applyMatrix(double,double,double,double,double,double)

    @param angle Rotational counterclockwise angle.
  */
  public void rotate(
    double angle
    )
  {
    double rad = angle * Math.PI / 180;
    double cos = Math.cos(rad);
    double sin = Math.sin(rad);

    applyMatrix(cos, sin, -sin, cos, 0, 0);
  }

  /**
    Applies a rotation to the coordinate system from user space to device space [PDF:1.6:4.2.2].

    @see #applyMatrix(double,double,double,double,double,double)

    @param angle Rotational counterclockwise angle.
    @param origin Rotational pivot point; it becomes the new coordinates origin.
  */
  public void rotate(
    double angle,
    Point2D origin
    )
  {
    // Center to the new origin!
    translate(
      origin.getX(),
      scanner.getContentContext().getBox().getHeight() - origin.getY()
      );
    // Rotate on the new origin!
    rotate(angle);
    // Restore the standard vertical coordinates system!
    translate(
      0,
      -scanner.getContentContext().getBox().getHeight()
      );
  }

  /**
    Applies a scaling to the coordinate system from user space to device space [PDF:1.6:4.2.2].

    @see #applyMatrix(double,double,double,double,double,double)

    @param ratioX Horizontal scaling ratio.
    @param ratioY Vertical scaling ratio.
  */
  public void scale(
    double ratioX,
    double ratioY
    )
  {applyMatrix(ratioX, 0, 0, ratioY, 0, 0);}

  /**
    Sets the character spacing parameter [PDF:1.6:5.2.1].
  */
  public void setCharSpace(
    double value
    )
  {add(new SetCharSpace(value));}

  /**
    Sets the nonstroking color value [PDF:1.6:4.5.7].

    @see #setStrokeColor(Color)
  */
  public void setFillColor(
    Color value
    )
  {
    if(scanner.getState().fillColorSpace != value.getColorSpace())
    {
      // Set filling color space!
      add(
        new SetFillColorSpace(
          getColorSpaceName(
            value.getColorSpace()
            )
          )
        );
    }

    add(new SetFillColor(value));
  }

  /**
    Sets the font [PDF:1.6:5.2].

    @param name Resource identifier of the font.
    @param size Scaling factor (points).
  */
  public void setFont(
    PdfName name,
    double size
    )
  {
    // Doesn't the font exist in the context resources?
    if(!scanner.getContentContext().getResources().getFonts().containsKey(name))
      throw new IllegalArgumentException("No font resource associated to the given argument (name:'name'; value:'" + name + "';)");

    add(new SetFont(name,size));
  }

  /**
    Sets the font [PDF:1.6:5.2].
    <h3>Remarks</h3>
    <p>The <code>value</code> is checked for presence in the current resource
    dictionary: if it isn't available, it's automatically added. If you need to
    avoid such a behavior, use {@link #setFont(PdfName,double) setFont(PdfName,double)}.</p>

    @param value Font.
    @param size Scaling factor (points).
  */
  public void setFont(
    Font value,
    double size
    )
  {setFont(getFontName(value),size);}

  /**
    Sets the text horizontal scaling [PDF:1.6:5.2.3].
  */
  public void setTextScale(
    double value
    )
  {add(new SetTextScale(value));}

  /**
    Sets the text leading [PDF:1.6:5.2.4].
  */
  public void setTextLead(
    double value
    )
  {add(new SetTextLead(value));}

  /**
    Sets the line cap style [PDF:1.6:4.3.2].
  */
  public void setLineCap(
    LineCapEnum value
    )
  {add(new SetLineCap(value));}

  /**
    Sets the line dash pattern [PDF:1.6:4.3.2].

    @param phase Distance into the dash pattern at which to start the dash.
    @param unitsOn Length of evenly alternating dashes and gaps.
  */
  public void setLineDash(
    int phase,
    int unitsOn
    )
  {setLineDash(phase,unitsOn,unitsOn);}

  /**
    Sets the line dash pattern [PDF:1.6:4.3.2].

    @param phase Distance into the dash pattern at which to start the dash.
    @param unitsOn Length of dashes.
    @param unitsOff Length of gaps.
  */
  public void setLineDash(
    int phase,
    int unitsOn,
    int unitsOff
    )
  {add(new SetLineDash(phase,unitsOn,unitsOff));}

  /**
    Sets the line join style [PDF:1.6:4.3.2].
  */
  public void setLineJoin(
    LineJoinEnum value
    )
  {add(new SetLineJoin(value));}

  /**
    Sets the line width [PDF:1.6:4.3.2].
  */
  public void setLineWidth(
    double value
    )
  {add(new SetLineWidth(value));}

  /**
    Sets the transformation of the coordinate system from user space to device space [PDF:1.6:4.3.3].
    <h3>Remarks</h3>
    <p>The transformation replaces the current transformation matrix (CTM).</p>

    @see #applyMatrix(double,double,double,double,double,double)

    @param a Item 0,0 of the matrix.
    @param b Item 0,1 of the matrix.
    @param c Item 1,0 of the matrix.
    @param d Item 1,1 of the matrix.
    @param e Item 2,0 of the matrix.
    @param f Item 2,1 of the matrix.
  */
  public void setMatrix(
    double a,
    double b,
    double c,
    double d,
    double e,
    double f
    )
  {
    // Reset the CTM!
    add(
      ModifyCTM.getResetCTM(
        scanner.getState().ctm
        )
      );
    // Apply the transformation!
    add(new ModifyCTM(a,b,c,d,e,f));
  }

  /**
    Sets the miter limit [PDF:1.6:4.3.2].
  */
  public void setMiterLimit(
    double value
    )
  {add(new SetMiterLimit(value));}

  /**
    @see #getScanner()
  */
  public void setScanner(
    ContentScanner value
    )
  {scanner = value;}

  /**
    Sets the stroking color value [PDF:1.6:4.5.7].

    @see #setFillColor(Color)
  */
  public void setStrokeColor(
    Color value
    )
  {
    if(scanner.getState().strokeColorSpace != value.getColorSpace())
    {
      // Set stroking color space!
      add(
        new SetStrokeColorSpace(
          getColorSpaceName(
            value.getColorSpace()
            )
          )
        );
    }

    add(new SetStrokeColor(value));
  }

  /**
    Sets the text rendering mode [PDF:1.6:5.2.5].
  */
  public void setTextRenderMode(
    TextRenderModeEnum value
    )
  {add(new SetTextRenderMode(value));}

  /**
    Sets the text rise [PDF:1.6:5.2.6].
  */
  public void setTextRise(
    double value
    )
  {add(new SetTextRise(value));}

  /**
    Sets the word spacing [PDF:1.6:5.2.2].
  */
  public void setWordSpace(
    double value
    )
  {add(new SetWordSpace(value));}

  /**
    Shows the specified text on the page at the current location [PDF:1.6:5.3.2].

    @param value Text to show.
    @return Bounding box vertices in default user space units.
  */
  public Point2D[] showText(
    String value
    )
  {
    return showText(
      value,
      new Point2D.Double(0,0)
      );
  }

  /**
    Shows the link associated to the specified text on the page at the current location.

    @since 0.0.7

    @param value Text to show.
    @param action Action to apply when the link is activated.
    @return Link.
  */
  public Link showText(
    String value,
    Action action
    )
  {
    return showText(
      value,
      new Point2D.Double(0,0),
      action
      );
  }

  /**
    Shows the specified text on the page at the specified location [PDF:1.6:5.3.2].

    @param value Text to show.
    @param location Position at which showing the text.
    @return Bounding box vertices in default user space units.
  */
  public Point2D[] showText(
    String value,
    Point2D location
    )
  {
    return showText(
      value,
      location,
      AlignmentXEnum.Left,
      AlignmentYEnum.Top,
      0
      );
  }

  /**
    Shows the link associated to the specified text on the page at the specified location.

    @since 0.0.7

    @param value Text to show.
    @param location Position at which showing the text.
    @param action Action to apply when the link is activated.
    @return Link.
  */
  public Link showText(
    String value,
    Point2D location,
    Action action
    )
  {
    return showText(
      value,
      location,
      AlignmentXEnum.Left,
      AlignmentYEnum.Top,
      0,
      action
      );
  }

  /**
    Shows the specified text on the page at the specified location [PDF:1.6:5.3.2].

    @param value Text to show.
    @param location Anchor position at which showing the text.
    @param alignmentX Horizontal alignment.
    @param alignmentY Vertical alignment.
    @param rotation Rotational counterclockwise angle.
    @return Bounding box vertices in default user space units.
  */
  public Point2D[] showText(
    String value,
    Point2D location,
    AlignmentXEnum alignmentX,
    AlignmentYEnum alignmentY,
    double rotation
    )
  {
    ContentScanner.GraphicsState state = scanner.getState();
    Font font = state.font;
    double fontSize = state.fontSize;
    double x = location.getX();
    double y = location.getY();
    double width = font.getKernedWidth(value,fontSize);
    double height = font.getLineHeight(fontSize);
    double descent = font.getDescent(fontSize);

    Point2D[] frame = new Point2D[4];

    if(alignmentX == AlignmentXEnum.Left
      && alignmentY == AlignmentYEnum.Top)
    {
      beginText();
      try
      {
        if(rotation == 0)
        {
          translateText(
            x,
            scanner.getContentContext().getBox().getHeight() - y - font.getAscent(fontSize)
            );
        }
        else
        {
          double rad = rotation * Math.PI / 180.0;
          double cos = Math.cos(rad);
          double sin = Math.sin(rad);

          setTextMatrix(
            cos,
            sin,
            -sin,
            cos,
            x,
            scanner.getContentContext().getBox().getHeight() - y - font.getAscent(fontSize)
            );
        }

        state = scanner.getState();
        frame[0] = state.textToDeviceSpace(new Point2D.Double(0,descent));
        frame[1] = state.textToDeviceSpace(new Point2D.Double(width,descent));
        frame[2] = state.textToDeviceSpace(new Point2D.Double(width,height+descent));
        frame[3] = state.textToDeviceSpace(new Point2D.Double(0,height+descent));

        // Add the text!
        add(new ShowSimpleText(font.encode(value)));
      }
      catch(Exception e)
      {throw new RuntimeException("Failed to show text.", e);}
      finally
      {end(); /* Ends the text object. */}
    }
    else
    {
      beginLocalState();
      try
      {
        // Coordinates transformation.
        double cos, sin;
        if(rotation == 0)
        {
          cos = 1;
          sin = 0;
        }
        else
        {
          double rad = rotation * Math.PI / 180.0;
          cos = Math.cos(rad);
          sin = Math.sin(rad);
        }
        // Apply the transformation!
        applyMatrix(
          cos,
          sin,
          -sin,
          cos,
          x,
          scanner.getContentContext().getBox().getHeight() - y
          );

        beginText();
        try
        {
          // Text coordinates adjustment.
          switch(alignmentX)
          {
            case Left:
              x = 0;
              break;
            case Right:
              x = -width;
              break;
            case Center:
            case Justify:
              x = -width / 2;
              break;
          }
          switch(alignmentY)
          {
            case Top:
              y = -font.getAscent(fontSize);
              break;
            case Bottom:
              y = height - font.getAscent(fontSize);
              break;
            case Middle:
              y = height / 2 - font.getAscent(fontSize);
              break;
          }
          // Apply the text coordinates adjustment!
          translateText(x,y);

          state = scanner.getState();
          frame[0] = state.textToDeviceSpace(new Point2D.Double(0,descent));
          frame[1] = state.textToDeviceSpace(new Point2D.Double(width,descent));
          frame[2] = state.textToDeviceSpace(new Point2D.Double(width,height+descent));
          frame[3] = state.textToDeviceSpace(new Point2D.Double(0,height+descent));

          // Add the text!
          add(new ShowSimpleText(font.encode(value)));
        }
        catch(Exception e)
        {throw new RuntimeException("Failed to show text.", e);}
        finally
        {end(); /* Ends the text object. */}
      }
      catch(Exception e)
      {throw new RuntimeException("Failed to show text.", e);}
      finally
      {end(); /* Ends the local state. */}
    }

    return frame;
  }

  /**
    Shows the link associated to the specified text on the page at the specified location.

    @since 0.0.7

    @param value Text to show.
    @param location Anchor position at which showing the text.
    @param alignmentX Horizontal alignment.
    @param alignmentY Vertical alignment.
    @param rotation Rotational counterclockwise angle.
    @param action Action to apply when the link is activated.
    @return Link.
  */
  public Link showText(
    String value,
    Point2D location,
    AlignmentXEnum alignmentX,
    AlignmentYEnum alignmentY,
    double rotation,
    Action action
    )
  {
    Point2D[] textFrame = showText(
      value,
      location,
      alignmentX,
      alignmentY,
      rotation
      );

    IContentContext contentContext = scanner.getContentContext();
    if(!(contentContext instanceof Page))
      throw new RuntimeException("Link can be shown only on page contexts.");

    Page page = (Page)contentContext;
    Rectangle2D linkBox = new Rectangle2D.Double(textFrame[0].getX(),textFrame[0].getY(),0,0);
    for(
      int index = 1,
        length = textFrame.length;
      index < length;
      index++
      )
    {linkBox.add(textFrame[index]);}

    return new Link(
      page,
      linkBox,
      action
      );
  }

  /**
    Shows the specified external object [PDF:1.6:4.7].

    @param name Resource identifier of the external object.
  */
  public void showXObject(
    PdfName name
    )
  {add(new PaintXObject(name));}

  /**
    Shows the specified external object [PDF:1.6:4.7].
    <h3>Remarks</h3>
    <p>The <code>value</code> is checked for presence in the current resource
    dictionary: if it isn't available, it's automatically added. If you need to
    avoid such a behavior, use {@link #showXObject(PdfName) #showXObject(PdfName)}.</p>

    @since 0.0.5

    @param value External object.
  */
  public void showXObject(
    XObject value
    )
  {showXObject(getXObjectName(value));}

  /**
    Shows the specified external object at the specified position [PDF:1.6:4.7].

    @param name Resource identifier of the external object.
    @param location Position at which showing the external object.
  */
  public void showXObject(
    PdfName name,
    Point2D location
    )
  {
    showXObject(
      name,
      location,
      new Dimension(0,0)
      );
  }

  /**
    Shows the specified external object at the specified position [PDF:1.6:4.7].
    <h3>Remarks</h3>
    <p>The <code>value</code> is checked for presence in the current resource
    dictionary: if it isn't available, it's automatically added. If you need to
    avoid such a behavior, use {@link #showXObject(PdfName,Point2D) #showXObject(PdfName,Point2D)}.</p>

    @since 0.0.5

    @param value External object.
    @param location Position at which showing the external object.
  */
  public void showXObject(
    XObject value,
    Point2D location
    )
  {
    showXObject(
      getXObjectName(value),
      location
      );
  }

  /**
    Shows the specified external object at the specified position [PDF:1.6:4.7].

    @since 0.0.5

    @param name Resource identifier of the external object.
    @param location Position at which showing the external object.
    @param size Size of the external object.
  */
  public void showXObject(
    PdfName name,
    Point2D location,
    Dimension2D size
    )
  {
    showXObject(
      name,
      location,
      size,
      AlignmentXEnum.Left,
      AlignmentYEnum.Top,
      0
      );
  }

  /**
    Shows the specified external object at the specified position [PDF:1.6:4.7].
    <h3>Remarks</h3>
    <p>The <code>value</code> is checked for presence in the current resource
    dictionary: if it isn't available, it's automatically added. If you need to
    avoid such a behavior, use {@link #showXObject(PdfName,Point2D,Dimension2D) #showXObject(PdfName,Point2D,Dimension2D)}.</p>

    @since 0.0.5

    @param value External object.
    @param location Position at which showing the external object.
    @param size Size of the external object.
  */
  public void showXObject(
    XObject value,
    Point2D location,
    Dimension2D size
    )
  {
    showXObject(
      getXObjectName(value),
      location,
      size
      );
  }

  /**
    Shows the specified external object at the specified position [PDF:1.6:4.7].

    @param name Resource identifier of the external object.
    @param location Position at which showing the external object.
    @param size Size of the external object.
    @param alignmentX Horizontal alignment.
    @param alignmentY Vertical alignment.
    @param rotation Rotational counterclockwise angle.
  */
  public void showXObject(
    PdfName name,
    Point2D location,
    Dimension2D size,
    AlignmentXEnum alignmentX,
    AlignmentYEnum alignmentY,
    double rotation
    )
  {
    XObject xObject = scanner.getContentContext().getResources().getXObjects().get(name);

    // Adjusting default dimensions...
    /*
      NOTE: Zero-valued dimensions represent default proportional dimensions.
    */
    Dimension2D xObjectSize = xObject.getSize();
    if(size.getWidth() == 0)
    {
      if(size.getHeight() == 0)
      {size.setSize(xObjectSize);}
      else
      {size.setSize(size.getHeight() * xObjectSize.getWidth() / xObjectSize.getHeight(),size.getHeight());}
    }
    else if(size.getHeight() == 0)
    {size.setSize(size.getWidth(),size.getWidth() * xObjectSize.getHeight() / xObjectSize.getWidth());}

    // Scaling.
    double[] matrix = xObject.getMatrix();
    double scaleX, scaleY;
    scaleX = size.getWidth() / (xObjectSize.getWidth() * matrix[0]);
    scaleY = size.getHeight() / (xObjectSize.getHeight() * matrix[3]);

    // Alignment.
    double locationOffsetX, locationOffsetY;
    switch(alignmentX)
    {
      case Left: locationOffsetX = 0; break;
      case Right: locationOffsetX = size.getWidth(); break;
      case Center:
      case Justify:
      default: locationOffsetX = size.getWidth() / 2; break;
    }
    switch(alignmentY)
    {
      case Top: locationOffsetY = size.getHeight(); break;
      case Bottom: locationOffsetY = 0; break;
      case Middle:
      default: locationOffsetY = size.getHeight() / 2; break;
    }

    beginLocalState();
    try
    {
      translate(
        location.getX(),
        scanner.getContentContext().getBox().getHeight() - location.getY()
        );
      if(rotation != 0)
      {rotate(rotation);}
      applyMatrix(
        scaleX, 0, 0,
        scaleY,
        -locationOffsetX,
        -locationOffsetY
        );
      showXObject(name);
    }
    catch (Exception e)
    {throw new RuntimeException("Failed to show the xobject.",e);}
    finally
    {end(); /* Ends the local state. */}
  }

  /**
    Shows the specified external object at the specified position [PDF:1.6:4.7].
    <h3>Remarks</h3>
    <p>The <code>value</code> is checked for presence in the current resource
    dictionary: if it isn't available, it's automatically added. If you need to
    avoid such a behavior, use {@link #showXObject(PdfName,Point2D,Dimension2D,AlignmentXEnum,AlignmentYEnum,double) #showXObject(PdfName,Point2D,Dimension2D,AlignmentXEnum,AlignmentYEnum,double)}.</p>

    @since 0.0.5

    @param value External object.
    @param location Position at which showing the external object.
    @param size Size of the external object.
    @param alignmentX Horizontal alignment.
    @param alignmentY Vertical alignment.
    @param rotation Rotational counterclockwise angle.
  */
  public void showXObject(
    XObject value,
    Point2D location,
    Dimension2D size,
    AlignmentXEnum alignmentX,
    AlignmentYEnum alignmentY,
    double rotation
    )
  {
    showXObject(
      getXObjectName(value),
      location,
      size,
      alignmentX,
      alignmentY,
      rotation
      );
  }

  /**
    Strokes the path using the current color [PDF:1.6:4.4.2].

    @see #setStrokeColor(Color)
  */
  public void stroke(
    )
  {add(Stroke.Value);}

  /**
    Applies a translation to the coordinate system from user space
    to device space [PDF:1.6:4.2.2].

    @see #applyMatrix(double,double,double,double,double,double)

    @param distanceX Horizontal distance.
    @param distanceY Vertical distance.
  */
  public void translate(
    double distanceX,
    double distanceY
    )
  {applyMatrix(1, 0, 0, 1, distanceX, distanceY);}
  // </public>

  // <private>
  /**
    Begins a subpath [PDF:1.6:4.4.1].

    @param startPoint Starting point.
  */
  private void beginSubpath(
    Point2D startPoint
    )
  {
    add(
      new BeginSubpath(
        startPoint.getX(),
        scanner.getContentContext().getBox().getHeight() - startPoint.getY()
        )
      );
  }

  /**
    Begins a text object [PDF:1.6:5.3].

    @see #end()
  */
  private Text beginText(
    )
  {return (Text)begin(new Text());}

  //TODO: drawArc MUST seamlessly manage already-begun paths.
  private void drawArc(
    RectangularShape location,
    double startAngle,
    double endAngle,
    double branchWidth,
    double branchRatio,
    boolean beginPath
    )
  {
    /*
      NOTE: Strictly speaking, arc drawing is NOT a PDF primitive;
      it leverages the cubic bezier curve operator (thanks to
      G. Adam Stanislav, whose article was greatly inspirational:
      see http://www.whizkidtech.redprince.net/bezier/circle/).
    */

    if(startAngle > endAngle)
    {
      double swap = startAngle;
      startAngle = endAngle;
      endAngle = swap;
    }

    double radiusX = location.getWidth() / 2;
    double radiusY = location.getHeight() / 2;

    final Point2D center = new Point2D.Double(
      location.getX() + radiusX,
      location.getY() + radiusY
      );

    double radians1 = Math.toRadians(startAngle);
    Point2D point1 = new Point2D.Double(
      center.getX() + Math.cos(radians1) * radiusX,
      center.getY() - Math.sin(radians1) * radiusY
      );

    if(beginPath)
    {beginSubpath(point1);}

    final double endRadians = Math.toRadians(endAngle);
    final double quadrantRadians = Math.PI / 2;
    double radians2 = Math.min(
      radians1 + quadrantRadians - radians1 % quadrantRadians,
      endRadians
      );
    final double kappa = 0.5522847498;
    while(true)
    {
      double segmentX = radiusX * kappa;
      double segmentY = radiusY * kappa;

      // Endpoint 2.
      Point2D point2 = new Point2D.Double(
        center.getX() + Math.cos(radians2) * radiusX,
        center.getY() - Math.sin(radians2) * radiusY
        );

      // Control point 1.
      double tangentialRadians1 = Math.atan(
        -(Math.pow(radiusY,2) * (point1.getX()-center.getX()))
          / (Math.pow(radiusX,2) * (point1.getY()-center.getY()))
        );
      double segment1 = (
        segmentY * (1 - Math.abs(Math.sin(radians1)))
          + segmentX * (1 - Math.abs(Math.cos(radians1)))
        ) * (radians2-radians1) / quadrantRadians; // TODO: control segment calculation is still not so accurate as it should -- verify how to improve it!!!
      Point2D control1 = new Point2D.Double(
        point1.getX() + Math.abs(Math.cos(tangentialRadians1) * segment1) * Math.signum(-Math.sin(radians1)),
        point1.getY() + Math.abs(Math.sin(tangentialRadians1) * segment1) * Math.signum(-Math.cos(radians1))
        );

      // Control point 2.
      double tangentialRadians2 = Math.atan(
        -(Math.pow(radiusY,2) * (point2.getX()-center.getX()))
          / (Math.pow(radiusX,2) * (point2.getY()-center.getY()))
        );
      double segment2 = (
        segmentY * (1 - Math.abs(Math.sin(radians2)))
          + segmentX * (1 - Math.abs(Math.cos(radians2)))
        ) * (radians2-radians1) / quadrantRadians; // TODO: control segment calculation is still not so accurate as it should -- verify how to improve it!!!
      Point2D control2 = new Point2D.Double(
        point2.getX() + Math.abs(Math.cos(tangentialRadians2) * segment2) * Math.signum(Math.sin(radians2)),
        point2.getY() + Math.abs(Math.sin(tangentialRadians2) * segment2) * Math.signum(Math.cos(radians2))
        );

      // Draw the current quadrant curve!
      drawCurve(
        point2,
        control1,
        control2
        );

      // Last arc quadrant?
      if(radians2 == endRadians)
        break;

      // Preparing the next quadrant iteration...
      point1 = point2;
      radians1 = radians2;
      radians2 += quadrantRadians;
      if(radians2 > endRadians)
      {radians2 = endRadians;}

      double quadrantRatio = (radians2 - radians1) / quadrantRadians;
      radiusX += branchWidth * quadrantRatio;
      radiusY += branchWidth * quadrantRatio;

      branchWidth *= branchRatio;
    }
  }

  private PdfName getFontName(
    Font value
    )
  {
    // Ensuring that the font exists within the context resources...
    Resources resources = scanner.getContentContext().getResources();
    FontResources fonts = resources.getFonts();
    // No font resources collection?
    if(fonts == null)
    {
      // Create the font resources collection!
      fonts = new FontResources(scanner.getContents().getDocument());
      resources.setFonts(fonts); resources.update();
    }
    // Get the key associated to the font!
    PdfName name = fonts.getBaseDataObject().getKey(value.getBaseObject());
    // No key found?
    if(name == null)
    {
      // Insert the font within the resources!
      int fontIndex = fonts.size();
      do
      {name = new PdfName(String.valueOf(++fontIndex));}
      while(fonts.containsKey(name));
      fonts.put(name,value); fonts.update();
    }

    return name;
  }

  private PdfName getXObjectName(
    XObject value
    )
  {
    // Ensuring that the external object exists within the context resources...
    Resources resources = scanner.getContentContext().getResources();
    XObjectResources xObjects = resources.getXObjects();
    // No external object resources collection?
    if(xObjects == null)
    {
      // Create the external object resources collection!
      xObjects = new XObjectResources(scanner.getContents().getDocument());
      resources.setXObjects(xObjects); resources.update();
    }
    // Get the key associated to the external object!
    PdfName name = xObjects.getBaseDataObject().getKey(value.getBaseObject());
    // No key found?
    if(name == null)
    {
      // Insert the external object within the resources!
      int xObjectIndex = xObjects.size();
      do
      {name = new PdfName(String.valueOf(++xObjectIndex));}
      while(xObjects.containsKey(name));
      xObjects.put(name,value); xObjects.update();
    }

    return name;
  }

  private PdfName getColorSpaceName(
    ColorSpace value
    )
  {
    if(value instanceof DeviceGrayColorSpace)
    {return PdfName.DeviceGray;}
    else if(value instanceof DeviceRGBColorSpace)
    {return PdfName.DeviceRGB;}
    else if(value instanceof DeviceCMYKColorSpace)
    {return PdfName.DeviceCMYK;}
    else
      throw new NotImplementedException("colorSpace MUST be converted to its associated name; you need to implement a method in PdfDictionary that, given a PdfDirectObject, returns its associated key.");
  }

  /**
    Applies a rotation to the coordinate system from text space to user space [PDF:1.6:4.2.2].

    @param angle Rotational counterclockwise angle.
  */
  @SuppressWarnings("unused")
  private void rotateText(
    double angle
    )
  {
    double rad = angle * Math.PI / 180;
    double cos = Math.cos(rad);
    double sin = Math.sin(rad);

    setTextMatrix(cos, sin, -sin, cos, 0, 0);
  }

  /**
    Applies a scaling to the coordinate system from text space to user space
    [PDF:1.6:4.2.2].
    @param ratioX Horizontal scaling ratio.
    @param ratioY Vertical scaling ratio.
  */
  @SuppressWarnings("unused")
  private void scaleText(
    double ratioX,
    double ratioY
    )
  {setTextMatrix(ratioX, 0, 0, ratioY, 0, 0);}

  /**
    Sets the transformation of the coordinate system from text space to user space [PDF:1.6:5.3.1].
    <h3>Remarks</h3>
    <p>The transformation replaces the current text matrix.</p>

    @param a Item 0,0 of the matrix.
    @param b Item 0,1 of the matrix.
    @param c Item 1,0 of the matrix.
    @param d Item 1,1 of the matrix.
    @param e Item 2,0 of the matrix.
    @param f Item 2,1 of the matrix.
  */
  private void setTextMatrix(
    double a,
    double b,
    double c,
    double d,
    double e,
    double f
    )
  {add(new SetTextMatrix(a,b,c,d,e,f));}

  /**
    Applies a translation to the coordinate system from text space
    to user space [PDF:1.6:4.2.2].

    @param distanceX Horizontal distance.
    @param distanceY Vertical distance.
  */
  private void translateText(
    double distanceX,
    double distanceY
    )
  {setTextMatrix(1, 0, 0, 1, distanceX, distanceY);}

  /**
    Applies a translation to the coordinate system from text space to user space,
    relative to the start of the current line [PDF:1.6:5.3.1].

    @param offsetX Horizontal offset.
    @param offsetY Vertical offset.
  */
  @SuppressWarnings("unused")
  private void translateTextRelative(
    double offsetX,
    double offsetY
    )
  {
    add(
      new TranslateTextRelative(
        offsetX,
        -offsetY
        )
      );
  }

  /**
    Applies a translation to the coordinate system from text space to user space,
    moving to the start of the next line [PDF:1.6:5.3.1].
  */
  @SuppressWarnings("unused")
  private void translateTextToNextLine(
    )
  {add(TranslateTextToNextLine.Value);}
  // </private>
  // </interface>
  // </dynamic>
  // </class>
}