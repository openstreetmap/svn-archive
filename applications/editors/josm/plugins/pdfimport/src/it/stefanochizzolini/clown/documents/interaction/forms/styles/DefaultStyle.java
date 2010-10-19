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

package it.stefanochizzolini.clown.documents.interaction.forms.styles;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.documents.contents.colorSpaces.DeviceRGBColor;
import it.stefanochizzolini.clown.documents.contents.composition.AlignmentXEnum;
import it.stefanochizzolini.clown.documents.contents.composition.AlignmentYEnum;
import it.stefanochizzolini.clown.documents.contents.composition.BlockFilter;
import it.stefanochizzolini.clown.documents.contents.composition.PrimitiveFilter;
import it.stefanochizzolini.clown.documents.contents.fonts.StandardType1Font;
import it.stefanochizzolini.clown.documents.contents.xObjects.FormXObject;
import it.stefanochizzolini.clown.documents.interaction.annotations.Appearance;
import it.stefanochizzolini.clown.documents.interaction.annotations.AppearanceStates;
import it.stefanochizzolini.clown.documents.interaction.annotations.DualWidget;
import it.stefanochizzolini.clown.documents.interaction.annotations.Widget;
import it.stefanochizzolini.clown.documents.interaction.forms.CheckBox;
import it.stefanochizzolini.clown.documents.interaction.forms.ChoiceItem;
import it.stefanochizzolini.clown.documents.interaction.forms.ComboBox;
import it.stefanochizzolini.clown.documents.interaction.forms.Field;
import it.stefanochizzolini.clown.documents.interaction.forms.ListBox;
import it.stefanochizzolini.clown.documents.interaction.forms.PushButton;
import it.stefanochizzolini.clown.documents.interaction.forms.RadioButton;
import it.stefanochizzolini.clown.documents.interaction.forms.TextField;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfInteger;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfReal;
import it.stefanochizzolini.clown.objects.PdfString;
import it.stefanochizzolini.clown.util.math.geom.Dimension;

import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
  Default field appearance style.

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.7
*/
public class DefaultStyle
  extends FieldStyle
{
  // <dynamic>
  // <constructors>
  public DefaultStyle(
    )
  {
    setBackColor(new DeviceRGBColor(.9,.9,.9));
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public void apply(
    Field field
    )
  {
    if(field instanceof PushButton)
    {apply((PushButton)field);}
    else if(field instanceof CheckBox)
    {apply((CheckBox)field);}
    else if(field instanceof TextField)
    {apply((TextField)field);}
    else if(field instanceof ComboBox)
    {apply((ComboBox)field);}
    else if(field instanceof ListBox)
    {apply((ListBox)field);}
    else if(field instanceof RadioButton)
    {apply((RadioButton)field);}
  }

  private void apply(
    CheckBox field
    )
  {
    Document document = field.getDocument();
    for(Widget widget : field.getWidgets())
    {
      {
        PdfDictionary widgetDataObject = widget.getBaseDataObject();
        widgetDataObject.put(
          PdfName.DA,
          new PdfString("/ZaDb 0 Tf 0 0 0 rg")
          );
        widgetDataObject.put(
          PdfName.MK,
          new PdfDictionary(
            new PdfName[]
            {
              PdfName.BG,
              PdfName.BC,
              PdfName.CA
            },
            new PdfDirectObject[]
            {
              new PdfArray(new PdfDirectObject[]{new PdfReal(0.9412),new PdfReal(0.9412),new PdfReal(0.9412)}),
              new PdfArray(new PdfDirectObject[]{new PdfInteger(0),new PdfInteger(0),new PdfInteger(0)}),
              new PdfString("4")
            }
            )
          );
        widgetDataObject.put(
          PdfName.BS,
          new PdfDictionary(
            new PdfName[]
            {
              PdfName.W,
              PdfName.S
            },
            new PdfDirectObject[]
            {
              new PdfReal(0.8),
              PdfName.S
            }
            )
          );
        widgetDataObject.put(
          PdfName.H,
          PdfName.P
          );
      }

      Appearance appearance = widget.getAppearance();
      if(appearance == null)
      {widget.setAppearance(appearance = new Appearance(document));}

      AppearanceStates normalAppearance = appearance.getNormal();
      FormXObject onState = new FormXObject(document);
      normalAppearance.put(PdfName.Yes,onState);

//TODO:verify!!!
//   appearance.getRollover().put(PdfName.Yes,onState);
//   appearance.getDown().put(PdfName.Yes,onState);
//   appearance.getRollover().put(PdfName.Off,offState);
//   appearance.getDown().put(PdfName.Off,offState);

      Rectangle2D widgetBox = widget.getBox();
      Dimension2D size = new Dimension(widgetBox.getWidth(),widgetBox.getHeight());
      Rectangle2D frame = new Rectangle2D.Double(0,0,size.getWidth(),size.getHeight());
      {
        onState.setSize(size);

        PrimitiveFilter builder = new PrimitiveFilter(onState);

        builder.beginLocalState();
        builder.setFillColor(getBackColor());
        builder.setStrokeColor(getForeColor());
        builder.drawRectangle(frame);
        builder.fillStroke();
        builder.end();

        BlockFilter blockFilter = new BlockFilter(builder);
        blockFilter.begin(frame,AlignmentXEnum.Center,AlignmentYEnum.Middle);
        builder.setFillColor(getForeColor());
        builder.setFont(
          new StandardType1Font(
            document,
            StandardType1Font.FamilyEnum.ZapfDingbats,
            true,
            false
            ),
          size.getHeight() * 0.8
          );
        blockFilter.showText(new String(new char[]{getCheckSymbol()}));
        blockFilter.end();

        builder.flush();
      }

      FormXObject offState = new FormXObject(document);
      normalAppearance.put(PdfName.Off,offState);
      {
        offState.setSize(size);

        PrimitiveFilter builder = new PrimitiveFilter(offState);

        builder.beginLocalState();
        builder.setFillColor(getBackColor());
        builder.setStrokeColor(getForeColor());
        builder.drawRectangle(frame);
        builder.fillStroke();
        builder.end();

        builder.flush();
      }
    }
  }

  private void apply(
    RadioButton field
    )
  {
    Document document = field.getDocument();
    for(Widget widget : field.getWidgets())
    {
      {
        PdfDictionary widgetDataObject = widget.getBaseDataObject();
        widgetDataObject.put(
          PdfName.DA,
          new PdfString("/ZaDb 0 Tf 0 0 0 rg")
          );
        widgetDataObject.put(
          PdfName.MK,
          new PdfDictionary(
            new PdfName[]
            {
              PdfName.BG,
              PdfName.BC,
              PdfName.CA
            },
            new PdfDirectObject[]
            {
              new PdfArray(new PdfDirectObject[]{new PdfReal(0.9412),new PdfReal(0.9412),new PdfReal(0.9412)}),
              new PdfArray(new PdfDirectObject[]{new PdfInteger(0),new PdfInteger(0),new PdfInteger(0)}),
              new PdfString("l")
            }
            )
          );
        widgetDataObject.put(
          PdfName.BS,
          new PdfDictionary(
            new PdfName[]
            {
              PdfName.W,
              PdfName.S
            },
            new PdfDirectObject[]
            {
              new PdfReal(0.8),
              PdfName.S
            }
            )
          );
        widgetDataObject.put(
          PdfName.H,
          PdfName.P
          );
      }

      Appearance appearance = widget.getAppearance();
      if(appearance == null)
      {widget.setAppearance(appearance = new Appearance(document));}

      AppearanceStates normalAppearance = appearance.getNormal();
      FormXObject onState = normalAppearance.get(new PdfName(((DualWidget)widget).getWidgetName()));

//TODO:verify!!!
//   appearance.getRollover().put(new PdfName(...),onState);
//   appearance.getDown().put(new PdfName(...),onState);
//   appearance.getRollover().put(PdfName.Off,offState);
//   appearance.getDown().put(PdfName.Off,offState);

      Rectangle2D widgetBox = widget.getBox();
      Dimension2D size = new Dimension(widgetBox.getWidth(),widgetBox.getHeight());
      Rectangle2D frame = new Rectangle2D.Double(1,1,size.getWidth()-2,size.getHeight()-2);
      {
        onState.setSize(size);

        PrimitiveFilter builder = new PrimitiveFilter(onState);

        builder.beginLocalState();
        builder.setFillColor(getBackColor());
        builder.setStrokeColor(getForeColor());
        builder.drawEllipse(frame);
        builder.fillStroke();
        builder.end();

        BlockFilter blockFilter = new BlockFilter(builder);
        blockFilter.begin(frame,AlignmentXEnum.Center,AlignmentYEnum.Middle);
        builder.setFillColor(getForeColor());
        builder.setFont(
          new StandardType1Font(
            document,
            StandardType1Font.FamilyEnum.ZapfDingbats,
            true,
            false
            ),
          size.getHeight() * 0.8
          );
        blockFilter.showText(new String(new char[]{getRadioSymbol()}));
        blockFilter.end();

        builder.flush();
      }

      FormXObject offState = new FormXObject(document);
      normalAppearance.put(PdfName.Off,offState);
      {
        offState.setSize(size);

        PrimitiveFilter builder = new PrimitiveFilter(offState);

        builder.beginLocalState();
        builder.setFillColor(getBackColor());
        builder.setStrokeColor(getForeColor());
        builder.drawEllipse(frame);
        builder.fillStroke();
        builder.end();

        builder.flush();
      }
    }
  }

  private void apply(
    PushButton field
    )
  {
    Document document = field.getDocument();
    Widget widget = field.getWidgets().get(0);

    Appearance appearance = widget.getAppearance();
    if(appearance == null)
    {widget.setAppearance(appearance = new Appearance(document));}

    FormXObject normalAppearanceState = new FormXObject(document);
    {
      Rectangle2D widgetBox = widget.getBox();
      Dimension2D size = new Dimension(widgetBox.getWidth(),widgetBox.getHeight());
      normalAppearanceState.setSize(size);
      PrimitiveFilter builder = new PrimitiveFilter(normalAppearanceState);

      builder.beginLocalState();
      double lineWidth = 1;
      builder.setLineWidth(lineWidth);
      builder.setFillColor(getBackColor());
      builder.setStrokeColor(getForeColor());
      Rectangle2D frame = new Rectangle2D.Double(lineWidth/2,lineWidth/2,size.getWidth()-lineWidth,size.getHeight()-lineWidth);
      builder.drawRectangle(frame,5);
      builder.fillStroke();
      builder.end();

      String caption = (String)field.getValue();
      if(caption != null)
      {
        BlockFilter blockFilter = new BlockFilter(builder);
        blockFilter.begin(frame,AlignmentXEnum.Center,AlignmentYEnum.Middle);
        builder.setFillColor(getForeColor());
        builder.setFont(
          new StandardType1Font(
            document,
            StandardType1Font.FamilyEnum.Helvetica,
            true,
            false
            ),
          size.getHeight() * 0.5
          );
        blockFilter.showText(caption);
        blockFilter.end();
      }

      builder.flush();
    }
    appearance.getNormal().put(null,normalAppearanceState);
  }

  private void apply(
    TextField field
    )
  {
    Document document = field.getDocument();
    Widget widget = field.getWidgets().get(0);

    Appearance appearance = widget.getAppearance();
    if(appearance == null)
    {widget.setAppearance(appearance = new Appearance(document));}

    widget.getBaseDataObject().put(
      PdfName.DA,
      new PdfString("/Helv " + getFontSize() + " Tf 0 0 0 rg")
      );

    FormXObject normalAppearanceState = new FormXObject(document);
    {
      Rectangle2D widgetBox = widget.getBox();
      Dimension2D size = new Dimension(widgetBox.getWidth(),widgetBox.getHeight());
      normalAppearanceState.setSize(size);
      PrimitiveFilter builder = new PrimitiveFilter(normalAppearanceState);

      builder.beginLocalState();
      double lineWidth = 1;
      builder.setLineWidth(lineWidth);
      builder.setFillColor(getBackColor());
      builder.setStrokeColor(getForeColor());
      Rectangle2D frame = new Rectangle2D.Double(lineWidth/2,lineWidth/2,size.getWidth()-lineWidth,size.getHeight()-lineWidth);
      builder.drawRectangle(frame,5);
      builder.fillStroke();
      builder.end();

      builder.beginMarkedContent(PdfName.Tx);
      builder.setFont(
        new StandardType1Font(
          document,
          StandardType1Font.FamilyEnum.Helvetica,
          false,
          false
          ),
        getFontSize()
        );
      builder.showText(
        (String)field.getValue(),
        new Point2D.Double(0,size.getHeight()/2),
        AlignmentXEnum.Left,
        AlignmentYEnum.Middle,
        0
        );
      builder.end();

      builder.flush();
    }
    appearance.getNormal().put(null,normalAppearanceState);
  }

  private void apply(
    ComboBox field
    )
  {
    Document document = field.getDocument();
    Widget widget = field.getWidgets().get(0);

    Appearance appearance = widget.getAppearance();
    if(appearance == null)
    {widget.setAppearance(appearance = new Appearance(document));}

    widget.getBaseDataObject().put(
      PdfName.DA,
      new PdfString("/Helv " + getFontSize() + " Tf 0 0 0 rg")
      );

    FormXObject normalAppearanceState = new FormXObject(document);
    {
      Rectangle2D widgetBox = widget.getBox();
      Dimension2D size = new Dimension(widgetBox.getWidth(),widgetBox.getHeight());
      normalAppearanceState.setSize(size);
      PrimitiveFilter builder = new PrimitiveFilter(normalAppearanceState);

      builder.beginLocalState();
      double lineWidth = 1;
      builder.setLineWidth(lineWidth);
      builder.setFillColor(getBackColor());
      builder.setStrokeColor(getForeColor());
      Rectangle2D frame = new Rectangle2D.Double(lineWidth/2,lineWidth/2,size.getWidth()-lineWidth,size.getHeight()-lineWidth);
      builder.drawRectangle(frame,5);
      builder.fillStroke();
      builder.end();

      builder.beginMarkedContent(PdfName.Tx);
      builder.setFont(
        new StandardType1Font(
          document,
          StandardType1Font.FamilyEnum.Helvetica,
          false,
          false
          ),
        getFontSize()
        );
      builder.showText(
        (String)field.getValue(),
        new Point2D.Double(0,size.getHeight()/2),
        AlignmentXEnum.Left,
        AlignmentYEnum.Middle,
        0
        );
      builder.end();

      builder.flush();
    }
    appearance.getNormal().put(null,normalAppearanceState);
  }

  private void apply(
    ListBox field
    )
  {
    Document document = field.getDocument();
    Widget widget = field.getWidgets().get(0);

    Appearance appearance = widget.getAppearance();
    if(appearance == null)
    {widget.setAppearance(appearance = new Appearance(document));}

    {
      PdfDictionary widgetDataObject = widget.getBaseDataObject();
      widgetDataObject.put(
        PdfName.DA,
        new PdfString("/Helv " + getFontSize() + " Tf 0 0 0 rg")
        );
      widgetDataObject.put(
        PdfName.MK,
        new PdfDictionary(
          new PdfName[]
          {
            PdfName.BG,
            PdfName.BC
          },
          new PdfDirectObject[]
          {
            new PdfArray(new PdfDirectObject[]{new PdfReal(.9),new PdfReal(.9),new PdfReal(.9)}),
            new PdfArray(new PdfDirectObject[]{new PdfInteger(0),new PdfInteger(0),new PdfInteger(0)})
          }
          )
        );
    }

    FormXObject normalAppearanceState = new FormXObject(document);
    {
      Rectangle2D widgetBox = widget.getBox();
      Dimension2D size = new Dimension(widgetBox.getWidth(),widgetBox.getHeight());
      normalAppearanceState.setSize(size);
      PrimitiveFilter builder = new PrimitiveFilter(normalAppearanceState);

      builder.beginLocalState();
      double lineWidth = 1;
      builder.setLineWidth(lineWidth);
      builder.setFillColor(getBackColor());
      builder.setStrokeColor(getForeColor());
      Rectangle2D frame = new Rectangle2D.Double(lineWidth/2,lineWidth/2,size.getWidth()-lineWidth,size.getHeight()-lineWidth);
      builder.drawRectangle(frame,5);
      builder.fillStroke();
      builder.end();

      builder.beginLocalState();
      builder.drawRectangle(frame,5);
      builder.clip(); // Ensures that the visible content is clipped within the rounded frame.

      builder.beginMarkedContent(PdfName.Tx);
      builder.setFont(
        new StandardType1Font(
          document,
          StandardType1Font.FamilyEnum.Helvetica,
          false,
          false
          ),
        getFontSize()
        );
      double y = 3;
      for(ChoiceItem item : field.getItems())
      {
        builder.showText(
          item.getText(),
          new Point2D.Double(0,y)
          );
        y += getFontSize() * 1.175;
        if(y > size.getHeight())
          break;
      }
      builder.end();
      builder.end();

      builder.flush();
    }
    appearance.getNormal().put(null,normalAppearanceState);
  }
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}