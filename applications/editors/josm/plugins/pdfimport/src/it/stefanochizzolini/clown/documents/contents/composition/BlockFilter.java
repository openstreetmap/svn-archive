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

import it.stefanochizzolini.clown.bytes.IOutputStream;
import it.stefanochizzolini.clown.documents.contents.ContentScanner;
import it.stefanochizzolini.clown.documents.contents.fonts.Font;
import it.stefanochizzolini.clown.documents.contents.objects.ContainerObject;
import it.stefanochizzolini.clown.documents.contents.objects.ContentObject;
import it.stefanochizzolini.clown.documents.contents.objects.LocalGraphicsState;
import it.stefanochizzolini.clown.documents.contents.objects.ModifyCTM;
import it.stefanochizzolini.clown.documents.contents.objects.Operation;
import it.stefanochizzolini.clown.documents.contents.objects.SetWordSpace;

import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
  Content block filter.
  <p>It provides content positioning functionalities for page typesetting.</p>

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.3
*/
/*
  TODO: Manage all the graphics parameters (especially
  those text-related, like horizontal scaling etc.) using ContentScanner -- see PDF:1.6:5.2-3!!!
*/
public final class BlockFilter
{
  // <class>
  // <classes>
  private static final class ContentPlaceholder
    extends Operation
  {
    public List<ContentObject> objects = new ArrayList<ContentObject>(2);

    public ContentPlaceholder(
      )
    {super(null);}

    @SuppressWarnings("unused")
    public List<ContentObject> getObjects(
      )
    {return objects;}

    @Override
    public void writeTo(
      IOutputStream stream
      )
    {
      for(ContentObject object : objects)
      {object.writeTo(stream);}
    }
  }

  private static final class Row
  {
    /**
      Row's objects.
    */
    public ArrayList<RowObject> objects = new ArrayList<RowObject>();
    /**
      Number of space characters.
    */
    public int spaceCount = 0;
    /**
      Row's graphics objects container.
    */
    @SuppressWarnings("unused")
    public ContentPlaceholder container;

    public double height;
    /**
      Vertical location relative to the block frame.
    */
    public double y;
    public double width;

    Row(
      ContentPlaceholder container,
      double y
      )
    {
      this.container = container;
      this.y = y;
    }
  }

  private static final class RowObject
  {
    /**
      Row object's graphics objects container.
    */
    public ContainerObject container;

    public double height;
    @SuppressWarnings("unused")
    public double width;

    public int spaceCount;

    RowObject(
      ContainerObject container,
      double height,
      double width,
      int spaceCount
      )
    {
      this.container = container;
      this.height = height;
      this.width = width;
      this.spaceCount = spaceCount;
    }
  }
  // </classes>

  // <dynamic>
  /*
    NOTE: In order to provide fine-grained alignment,
    there are 2 postproduction state levels:
      1- row level (see endRow());
      2- block level (see end()).

    NOTE: Graphics instructions' layout follows this scheme (XS-BNF syntax):
      block = { beginLocalState translation parameters rows endLocalState }
      beginLocalState { "q\r" }
      translation = { "1 0 0 1 " number ' ' number "cm\r" }
      parameters = { ... } // Graphics state parameters.
      rows = { row* }
      row = { object* }
      object = { parameters beginLocalState translation content endLocalState }
      content = { ... } // Text, image (and so on) showing operators.
      endLocalState = { "Q\r" }
    NOTE: all the graphics state parameters within a block are block-level or row-object ones,
    i.e. they can't be represented inside row's local state, in order to allow parameter reuse
    within the same block.
  */
  // <fields>
  private PrimitiveFilter context;
  private ContentScanner scanner;

  private AlignmentXEnum alignmentX;
  private AlignmentYEnum alignmentY;
  private boolean hyphenation;

  /** Available area where to render the block contents inside. */
  private Rectangle2D frame;
  /** Actual area occupied by the block contents. */
  private Rectangle2D.Double boundBox;

  private Row currentRow;
  private boolean rowEnded;

  private LocalGraphicsState container;
  // </fields>

  // <constructors>
  public BlockFilter(
    PrimitiveFilter context
    )
  {
    this.context = context;

    this.scanner = context.getScanner();
  }
  // </constructors>

  // <interface>
  // <public>
  /**
    Begins a content block.
    @param frame Block boundaries.
    @param alignmentX Horizontal alignment.
    @param alignmentY Vertical alignment.
  */
  public void begin(
    Rectangle2D frame,
    AlignmentXEnum alignmentX,
    AlignmentYEnum alignmentY
    )
  {
    this.frame = frame;
    this.alignmentX = alignmentX;
    this.alignmentY = alignmentY;

    // Open the block local state!
    /*
      NOTE: This device allows a fine-grained control over the block representation.
      It MUST be coupled with a closing statement on block end.
    */
    container = context.beginLocalState();

    boundBox = new Rectangle2D.Double(
      frame.getX(),
      frame.getY(),
      frame.getWidth(),
      0
      );

    beginRow();
  }

  /**
    Ends the content block.
  */
  public void end(
    )
  {
    // End last row!
    endRow(true);

    // Block translation.
    container.getObjects().add(
      0,
      new ModifyCTM(
        1, 0, 0, 1,
        boundBox.x, // Horizontal translation.
        -boundBox.y // Vertical translation.
        )
      );

    // Close the block local state!
    context.end();
  }

  /**
    Gets the actual area occupied by the block contents.
  */
  public Rectangle2D getBoundBox(
    )
  {return boundBox;}

  /**
    Gets the content scanner.
  */
  public ContentScanner getScanner(
    )
  {return scanner;}

  /**
    Gets the context of the block filter.
  */
  public PrimitiveFilter getContext(
    )
  {return context;}

  /**
    Gets the available area where to render the block contents inside.
  */
  public Rectangle2D getFrame(
    )
  {return frame;}

  /**
    Gets whether the hyphenation algorithm has to be applied.
  */
  public boolean isHyphenation(
    )
  {return hyphenation;}

  /**
    Sets whether the hyphenation algorithm has to be applied.
  */
  public void setHyphenation(
    boolean value
    )
  {hyphenation = value;}

  /**
    Ends current paragraph.
  */
  public void showBreak(
    )
  {
    endRow(true);
    beginRow();
  }

  /**
    Ends current paragraph, specifying the offset of the next one.
    <h3>Remarks</h3>
    <p>This functionality allows higher-level features such as paragraph indentation and margin.</p>
    @param offset Relative location of the next paragraph.
  */
  public void showBreak(
    Dimension2D offset
    )
  {
    showBreak();

    currentRow.y += offset.getHeight();
    currentRow.width = offset.getWidth();
  }

  /**
    Ends current paragraph, specifying the alignment of the next one.
    <h3>Remarks</h3>
    <p>This functionality allows higher-level features such as paragraph indentation and margin.</p>
    @param alignmentX Horizontal alignment.
  */
  public void showBreak(
    AlignmentXEnum alignmentX
    )
  {
    showBreak();

    this.alignmentX = alignmentX;
  }

  /**
    Ends current paragraph, specifying the offset and alignment of the next one.
    <h3>Remarks</h3>
    <p>This functionality allows higher-level features such as paragraph indentation and margin.</p>
    @param offset Relative location of the next paragraph.
    @param alignmentX Horizontal alignment.
  */
  public void showBreak(
    Dimension2D offset,
    AlignmentXEnum alignmentX
    )
  {
    showBreak(offset);

    this.alignmentX = alignmentX;
  }

  /**
    Shows text.
    @return Last shown character index.
  */
  public int showText(
    String text
    )
  {
    if(currentRow == null
      || text == null)
      return 0;

    ContentScanner.GraphicsState state = context.getState();
    Font font = state.font;
    double fontSize = state.fontSize;
    double lineHeight = font.getLineHeight(fontSize);

    TextFitter textFitter = new TextFitter(
      text,
      0,
      font,
      fontSize,
      hyphenation
      );
    int textLength = text.length();
    int index = 0;

textShowing:
    while(true)
    {
      // Beginning of current row?
      if(currentRow.width == 0)
      {
        // Removing leading spaces...
        while(true)
        {
          // Did we reach the text end?
          if(index == textLength)
            break textShowing;

          if(text.charAt(index) != ' ')
            break;

          index++;
        }
      }

      // Text height: exceeds current row's height?
      if(lineHeight > currentRow.height)
      {
        // Text height: exceeds block's remaining vertical space?
        if(lineHeight > frame.getHeight() - currentRow.y) // Text exceeds.
        {
          // Terminate the current row!
          endRow(false);
          break textShowing;
        }
        else // Text doesn't exceed.
        {
          // Adapt current row's height!
          currentRow.height = lineHeight;
        }
      }

      // Does the text fit?
      if(textFitter.fit(
        index,
        frame.getWidth() - currentRow.width // Remaining row width.
        ))
      {
        // Get the fitting text!
        String textChunk = textFitter.getFittedText();
        double textChunkWidth = textFitter.getFittedWidth();
        Point2D textChunkLocation = new Point2D.Double(
          currentRow.width,
          currentRow.y
          );

        // Render the fitting text:
        // - open the row object's local state!
        /*
          NOTE: This device allows a fine-grained control over the row object's representation.
          It MUST be coupled with a closing statement on row object's end.
        */
        RowObject object = new RowObject(
          context.beginLocalState(),
          lineHeight,
          textChunkWidth,
          countOccurrence(' ',textChunk)
          );
        currentRow.objects.add(object);
        currentRow.spaceCount += object.spaceCount;
        // - show the text chunk!
        context.showText(
          textChunk,
          textChunkLocation
          );
        // - close the row object's local state!
        context.end();

        // Update ancillary parameters:
        // - update row width!
        currentRow.width += textChunkWidth;
        // - update cursor position!
        index = textFitter.getEndIndex();
      }

      // Evaluating trailing text...
trailParsing:
      while(true)
      {
        // Did we reach the text end?
        if(index == textLength)
          break textShowing;

        switch(text.charAt(index))
        {
          case '\r':
            break;
          case '\n':
            // New paragraph!
            index++;
            showBreak();
            break trailParsing;
          default:
            // New row (within the same paragraph)!
            endRow(false); beginRow();
            break trailParsing;
        }

        index++;
      }
    }

    return index;
  }
  // </public>

  // <private>
  /**
    Begins a content row.
  */
  private void beginRow(
    )
  {
    rowEnded = false;

    currentRow = new Row(
      (ContentPlaceholder)context.add(new ContentPlaceholder()),
      boundBox.height
      );
  }

  private int countOccurrence(
    char value,
    String text
    )
  {
    int count = 0;
    int fromIndex = 0;
    do
    {
      int foundIndex = text.indexOf(value,fromIndex);
      if(foundIndex == -1)
        return count;

      count++;

      fromIndex = foundIndex + 1;
    }
    while(true);
  }

  /**
    Ends the content row.
    @param broken Indicates whether this is the end of a paragraph.
  */
  private void endRow(
    boolean broken
    )
  {
    if(rowEnded)
      return;

    rowEnded = true;

    double objectXOffsets[] = new double[currentRow.objects.size()]; // Horizontal object displacements.
    double wordSpace = 0; // Exceeding space among words.
    double rowXOffset = 0; // Horizontal row offset.

    List<RowObject> objects = currentRow.objects;

    // Horizontal alignment.
    AlignmentXEnum alignmentX = this.alignmentX;
    switch(alignmentX)
    {
      case Left:
      {break;}
      case Right:
        rowXOffset = frame.getWidth() - currentRow.width;
        break;
      case Center:
        rowXOffset = (frame.getWidth() - currentRow.width) / 2;
        break;
      case Justify:
        // Are there NO spaces?
        if(currentRow.spaceCount == 0
          || broken) // NO spaces.
        {
          /* NOTE: This situation equals a simple left alignment. */
          alignmentX = AlignmentXEnum.Left;
        }
        else // Spaces exist.
        {
          // Calculate the exceeding spacing among the words!
          wordSpace = (frame.getWidth() - currentRow.width) / currentRow.spaceCount;
          // Define the horizontal offsets for justified alignment.
          for(
            int index = 1,
              count = objects.size();
            index < count;
            index++
            )
          {
            /*
              NOTE: The offset represents the horizontal justification gap inserted
              at the left side of each object.
            */
            objectXOffsets[index] = objectXOffsets[index - 1] + objects.get(index - 1).spaceCount * wordSpace;
          }
        }
        break;
    }

    SetWordSpace wordSpaceOperation = new SetWordSpace(wordSpace);

    // Vertical alignment and translation.
    for(
      int index = objects.size() - 1;
      index >= 0;
      index--
      )
    {
      RowObject object = objects.get(index);

      // Vertical alignment.
      double objectYOffset = 0;
//TODO:IMPL image support!!!
//       switch(object.type)
//       {
//         case Text:
          objectYOffset = -(currentRow.height - object.height); // Linebase-anchored vertical alignment.
//           break;
//         case Image:
//           objectYOffset = -(currentRow.height - object.height) / 2; // Centered vertical alignment.
//           break;
//       }

      List<ContentObject> containedGraphics = object.container.getObjects();
      // Word spacing.
      containedGraphics.add(0,wordSpaceOperation);
      // Translation.
      containedGraphics.add(
        0,
        new ModifyCTM(
          1, 0, 0, 1,
          objectXOffsets[index] + rowXOffset, // Horizontal alignment.
          objectYOffset // Vertical alignment.
          )
        );
    }

    // Update the actual block height!
    boundBox.height = currentRow.y + currentRow.height;

    // Update the actual block vertical location!
    double xOffset;
    switch(alignmentY)
    {
      case Bottom:
        xOffset = frame.getHeight() - boundBox.height;
        break;
      case Middle:
        xOffset = (frame.getHeight() - boundBox.height) / 2;
        break;
      case Top:
      default:
        xOffset = 0;
        break;
    }
    boundBox.y = frame.getY() + xOffset;

    // Discard the current row!
    currentRow = null;
  }
  // </private>
  // </interface>
  // </dynamic>
  // </class>
}