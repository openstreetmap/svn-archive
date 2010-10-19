/*
  Copyright 2007-2009 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.tools;

import it.stefanochizzolini.clown.documents.Page;
import it.stefanochizzolini.clown.documents.contents.Contents;
import it.stefanochizzolini.clown.documents.contents.ContentScanner;
import it.stefanochizzolini.clown.documents.contents.composition.PrimitiveFilter;
import it.stefanochizzolini.clown.documents.contents.objects.RestoreGraphicsState;
import it.stefanochizzolini.clown.documents.contents.objects.SaveGraphicsState;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfDataObject;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfReference;
import it.stefanochizzolini.clown.objects.PdfStream;

/**
  Tool for content insertion into existing pages.

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
*/
public class PageStamper
{
  // <class>
  // <dynamic>
  // <fields>
  private Page page;

  private PrimitiveFilter background;
  private PrimitiveFilter foreground;
  // </fields>

  // <constructors>
  public PageStamper(
    )
  {this(null);}

  public PageStamper(
    Page page
    )
  {setPage(page);}
  // </constructors>

  // <interface>
  // <public>
  public void flush(
    )
  {
    // Ensuring that there's room for the new content chunks inside the page's content stream...
    /*
      NOTE: This specialized stamper is optimized for content insertion without modifying
      existing content representations, leveraging the peculiar feature of page structures
      to express their content streams as arrays of data streams.
    */
    PdfArray streams;
    {
      PdfDirectObject contentsObject = page.getBaseDataObject().get(PdfName.Contents);
      PdfDataObject contentsDataObject = File.resolve(contentsObject);
      // Single data stream?
      if(contentsDataObject instanceof PdfStream)
      {
        /*
          NOTE: Content stream MUST be expressed as an array of data streams in order to host
          background- and foreground-stamped contents.
        */
        streams = new PdfArray();
        streams.add(contentsObject);
        page.getBaseDataObject().put(PdfName.Contents,streams);

        page.update(); // Fundamental to override original page contents collection.
      }
      else
      {
        streams = (PdfArray)contentsDataObject;

        if(!File.update(contentsObject))
        {page.update();} // Fundamental to override original page contents collection.
      }
    }

    // Background.
    // Serialize the content!
    background.flush();
    // Insert the serialized content into the page's content stream!
    streams.add(
      0,
      (PdfReference)background.getScanner().getContents().getBaseObject()
      );

    // Foreground.
    // Serialize the content!
    foreground.flush();
    // Append the serialized content into the page's content stream!
    streams.add(
      (PdfReference)foreground.getScanner().getContents().getBaseObject()
      );
  }

  public PrimitiveFilter getBackground(
    )
  {return background;}

  public PrimitiveFilter getForeground(
    )
  {return foreground;}

  public Page getPage(
    )
  {return page;}

  public void setPage(
    Page value
    )
  {
    page = value;
    if(page == null)
    {
      background = null;
      foreground = null;
    }
    else
    {
      // Background.
      background = createFilter();
      // Open the background local state!
      background.add(SaveGraphicsState.Value);
      // Close the background local state!
      background.add(RestoreGraphicsState.Value);
      // Open the middleground local state!
      background.add(SaveGraphicsState.Value);
      // Move into the background!
      background.getScanner().move(1);

      // Foregrond.
      foreground = createFilter();
      // Close the middleground local state!
      foreground.add(RestoreGraphicsState.Value);
    }
  }
  // </public>

  // <private>
  private PrimitiveFilter createFilter(
    )
  {
    PdfReference reference = page.getFile().register(new PdfStream());

    return new PrimitiveFilter(
      new ContentScanner(
        new Contents(
          reference,
          reference.getIndirectObject(),
          page
          )
        )
      );
  }
  // </private>
  // </interface>
  // </dynamic>
  // </class>
}