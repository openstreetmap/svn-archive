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

package it.stefanochizzolini.clown.documents.contents.objects;

import it.stefanochizzolini.clown.documents.contents.ContentScanner;
import it.stefanochizzolini.clown.documents.contents.IContentContext;
import it.stefanochizzolini.clown.documents.contents.fonts.Font;
import it.stefanochizzolini.clown.objects.PdfDirectObject;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;

/**
  Abstract 'show a text string' operation [PDF:1.6:5.3.2].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.8
  @version 0.0.8
*/
public abstract class ShowText
  extends Operation
{
  // <class>
  // <interfaces>
  public interface IScanner
  {
    /**
      Notifies the scanner about the font.

      @param fontSize Scaled font size.
    */
    void scanFont(
      double fontSize
      );

    /**
      Notifies the scanner about a text character.

      @param textChar Scanned character.
      @param textCharBox Bounding box of the scanned character.
    */
    void scanChar(
      char textChar,
      Rectangle2D textCharBox
      );
  }
  // </interfaces>

  // <dynamic>
  // <constructors>
  protected ShowText(
    String operator
    )
  {super(operator);}

  protected ShowText(
    String operator,
    PdfDirectObject... operands
    )
  {super(operator,operands);}

  protected ShowText(
    String operator,
    List<PdfDirectObject> operands
    )
  {super(operator,operands);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public void applyTo(
    ContentScanner.GraphicsState state
    )
  {
	  //TODO: fix this
	  //scan(state,null);
	  }

  /**
    Gets the encoded text.
    <h3>Remarks</h3>
    <p>Text is expressed in native encoding: to resolve it to Unicode, pass it
    to the decode method of the corresponding font.</p>
  */
  public abstract byte[] getText(
    );

  /**
    Gets the encoded text elements along with their adjustments.
    <h3>Remarks</h3>
    <p>Text is expressed in native encoding: to resolve it to Unicode, pass it
    to the decode method of the corresponding font.</p>

    @return Each element can be either a byte array or a number:
      if it's a byte array (encoded text), the operator shows text glyphs;
      if it's a number (glyph adjustment), the operator inversely adjusts the next glyph position by that amount
      (that is: a positive value reduces the distance between consecutive glyphs).
  */
  public List<Object> getValue(
    )
  {return Arrays.asList((Object)getText());}

  /**
    Executes scanning on this operation.

    @param state Graphics state context.
    @param textScanner Scanner to be notified about text contents.
      In case it's null, the operation is applied to the graphics state context.
  */
  public void scan(
    ContentScanner.GraphicsState state,
    IScanner textScanner
    )
  {
    /*
      TODO: I really dislike this solution -- it's a temporary hack until the event-driven
      parsing mechanism is implemented...
     */
    /*
      TODO: support to vertical writing mode.
    */

    IContentContext context = state.getScanner().getContentContext();
    double contextHeight = context.getBox().getHeight();
    Font font = state.font;
    double fontSize = state.fontSize;
    double scale = state.scale / 100;
    double scaledFactor = Font.getScalingFactor(fontSize) * scale;
    double wordSpace = state.wordSpace * scale;
    double charSpace = state.charSpace * scale;
    double[] tm = state.tm;
    double[] ctm = state.ctm;
    boolean fontScanned = false;
    if(this instanceof ShowTextToNextLine)
    {
      ShowTextToNextLine showTextToNextLine = (ShowTextToNextLine)this;
      Double newWordSpace = showTextToNextLine.getWordSpace();
      if(newWordSpace != null)
      {
        if(textScanner == null)
        {state.wordSpace = newWordSpace;}
        wordSpace = newWordSpace * scale;
      }
      Double newCharSpace = showTextToNextLine.getCharSpace();
      if(newCharSpace != null)
      {
        if(textScanner == null)
        {state.charSpace = newCharSpace;}
        charSpace = newCharSpace * scale;
      }
      tm = ContentScanner.GraphicsState.concat(
        new double[]{1,0,0,1,0,state.lead},
        state.tlm
        );
    }
    List<Object> textElements = getValue();
    for(Object textElement : textElements)
    {
      if(textElement instanceof byte[]) // Text string.
      {
        String textString = font.decode((byte[])textElement);
        for(char textChar : textString.toCharArray())
        {
          /*
            NOTE: The text rendering matrix is recomputed before each glyph is painted
            during a text-showing operation.
          */
          double[] trm = ContentScanner.GraphicsState.concat(tm,ctm);

          double charWidth = font.getWidth(textChar) * scaledFactor;
          double charHeight = font.getHeight(textChar,fontSize);

          if(textScanner != null)
          {
            if(!fontScanned)
            {
              fontScanned = true;
              textScanner.scanFont(fontSize * tm[3]);
            }

	          double scaledCharWidth = charWidth * tm[0];
	          double scaledCharHeight = charHeight * tm[3];
            Rectangle2D charBox = new Rectangle2D.Double(
              trm[4],
              contextHeight - trm[5] - font.getAscent(fontSize) * tm[3],
              scaledCharWidth,
              scaledCharHeight
              );
            textScanner.scanChar(textChar,charBox);
          }

          /*
            NOTE: After the glyph is painted, the text matrix is updated
            according to the glyph displacement and any applicable spacing parameter.
          */
          tm = ContentScanner.GraphicsState.concat(
            new double[]
            {
              1,0,0,1,
              charWidth
	              + charSpace
	              + (textChar == ' ' ? wordSpace : 0),
              0
            },
            tm
            );
        }
      }
      else // Text position adjustment.
      {
        tm = ContentScanner.GraphicsState.concat(
          new double[]
          {
            1,0,0,1,
            -((Number)textElement).doubleValue() * scaledFactor,
            0
          },
          tm
          );
      }
    }

    if(textScanner == null)
    {
      state.tm = tm;

      if(this instanceof ShowTextToNextLine)
      {state.tlm = Arrays.copyOf(state.tm,state.tm.length);}
    }
  }

  /**
    @see #getText()
  */
  public abstract void setText(
    byte[] value
    );

  /**
    @see #getValue()
  */
  public void setValue(
    List<Object> value
    )
  {setText((byte[])value.get(0));}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}