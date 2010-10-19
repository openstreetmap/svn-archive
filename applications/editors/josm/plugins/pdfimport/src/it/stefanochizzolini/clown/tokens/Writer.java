/*
  Copyright 2006-2009 Stefano Chizzolini. http://clown.stefanochizzolini.it

  Contributors:
    * Stefano Chizzolini (original code developer, http://www.stefanochizzolini.it)
    * Haakan Aakerberg (bugfix contributor):
      - [FIX:0.0.4:5]

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

package it.stefanochizzolini.clown.tokens;

import it.stefanochizzolini.clown.bytes.IOutputStream;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.files.IndirectObjects;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfInteger;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfReference;

import java.util.Map;

/**
  PDF file writer.

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
*/
public class Writer
{
  // <class>
  // <static>
  // <fields>
  private static final byte[] HeaderBinaryHint = new byte[]{(byte)'%',(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)'\r'}; // NOTE: Arbitrary binary characters (code >= 128) for ensuring proper behavior of file transfer applications [PDF:1.6:3.4.1].
  // </fields>
  // </static>

  // <dynamic>
  // <fields>
  private File file;
  private IOutputStream stream;
  // </fields>

  // <constructors>
  /**
    For internal use only.
  */
  public Writer(
    IOutputStream stream,
    File file
    )
  {
    this.stream = stream;
    this.file = file;
  }
  // </constructors>

  // <interface>
  public IOutputStream getStream(
    )
  {return stream;}

  /**
    Serializes the PDF file compactly [PDF:1.6:3.4].
  */
  public void writeStandard(
    )
  {
    StringBuilder xrefBuilder = new StringBuilder();
    int xrefSize = file.getIndirectObjects().size();

    // Header [PDF:1.6:3.4.1].
    {
      stream.write("%PDF-" + file.getDocument().getVersion() + "\r"); // NOTE: Document version represents the actual (possibly-overridden) file version.
      stream.write(HeaderBinaryHint);
    }

    // Body [PDF:1.6:3.4.2].
    {
      /*
        NOTE: A compact xref table comprises just one section composed by just one subsection.
        NOTE: As xref-table free entries MUST be arrayed as a linked list,
        it's needed to cache intermingled in-use entries in order to properly render
        the object number of the next free entry inside the previous one.
      */
      StringBuilder xrefInUseBlockBuilder = new StringBuilder();
      IndirectObjects indirectObjects = file.getIndirectObjects();
      PdfReference freeReference = indirectObjects.get(0).getReference(); // Initialized to the first free entry.
      for(
        int index = 1;
        index < xrefSize;
        index++
        )
      {
        PdfIndirectObject indirectObject = indirectObjects.get(index);
        // Is the object entry in use?
        if(indirectObject.isInUse()) // In-use entry.
        {
          // Indirect object.
          // Append to the xref table its xref!
          xrefInUseBlockBuilder.append(
            indirectObject.getReference().getCrossReference(
              stream.getLength()
              )
            );
          // Serialize its content!
          indirectObject.writeTo(stream);
        }
        else // Free entry.
        {
          // Flush current xref-table cache!
          xrefBuilder.append(
            freeReference.getCrossReference(index)
              + xrefInUseBlockBuilder.toString()
            );
          // Initialize next xref-table subsection!
          xrefInUseBlockBuilder.setLength(0);
          freeReference = indirectObject.getReference();
        }
      }
      // Flush current xref-table cache!
      xrefBuilder.append(
        freeReference.getCrossReference(0)
          + xrefInUseBlockBuilder.toString()
        );
    }

    // XRef table (unique section) [PDF:1.6:3.4.3]...
    long startxref = stream.getLength();
    {
      // ...header.
      stream.write(
        "xref\r"
          + "0 " + xrefSize + "\r"
        );
      // ...body.
      stream.write(xrefBuilder.toString());
    }

    // Trailer [PDF:1.6:3.4.4]...
    {
      // ...header.
      stream.write("trailer\r");
      // ...body.
      // Update the counter!
      PdfDictionary trailer = file.getTrailer();
      trailer.put(PdfName.Size,new PdfInteger(xrefSize));
      trailer.remove(PdfName.Prev); // [FIX:0.0.4:5] It (wrongly) kept the 'Prev' entry of multiple-section xref tables.
      // Serialize the contents!
      trailer.writeTo(stream);
      // ...tail.
      stream.write(
        "\r"
          + "startxref\r"
          + startxref + "\r"
          + "%%EOF"
        );
    }
  }

  /**
    Serializes the PDF file as incremental update [PDF:1.6:3.4.5].
  */
  public void writeIncremental(
    )
  {
    StringBuilder xrefBuilder = new StringBuilder();
    int xrefSize = file.getIndirectObjects().size();
    Parser parser = file.getReader().getParser();

    // Original content.
    stream.write(parser.getStream());

    // Body update.
    {
      /*
        NOTE: incremental xref table comprises multiple sections each one composed by multiple
        subsections.
      */
      // Insert modified indirect objects.
      StringBuilder xrefSubBuilder = new StringBuilder(); // xref-table subsection builder.
      int xrefSubCount = 0; // xref-table subsection counter.
      int prevKey = 0; // Previous-entry object number.
      for(
        Map.Entry<Integer,PdfIndirectObject> indirectObjectEntry
          : file.getIndirectObjects().getModifiedObjects().entrySet()
        )
      {
        // Is the object in the current subsection?
        /*
          NOTE: to belong to the current subsection, the object entry MUST be contiguous with the
          previous (condition 1) or the iteration has to have been just started (condition 2).
       */
        if(indirectObjectEntry.getKey() - prevKey == 1
          || prevKey == 0) // Current subsection continues.
        {
          xrefSubCount++;
        }
        else // Current subsection terminates.
        {
          // Flush current xref-table subsection!
          xrefBuilder.append(
            (prevKey - xrefSubCount + 1) + " " + xrefSubCount + "\r"
              + xrefSubBuilder.toString()
            );
          // Initialize next xref-table subsection!
          xrefSubBuilder.setLength(0);
          xrefSubCount = 1;
        }

        prevKey = indirectObjectEntry.getKey();

        // Modified indirect object.
        if(indirectObjectEntry.getValue().isInUse()) // In-use entry.
        {
          // Append to the current xref-table subsection its xref!
          xrefSubBuilder.append(
            indirectObjectEntry.getValue().getReference().getCrossReference(
              stream.getLength()
              )
            );
          // Serialize its content!
          indirectObjectEntry.getValue().writeTo(stream);
        }
        else // Free entry.
        {
          // Append to the current xref-table subsection its xref!
          /*
            NOTE: We purposely neglect the linked list of free entries
            (see IndirectObjects.remove(int)),
            so that this entry links directly back to object number 0,
            having a generation number of 65535 (not reusable) [PDF:1.6:3.4.3].
          */
          xrefSubBuilder.append(
            indirectObjectEntry.getValue().getReference().getCrossReference(0)
            );
        }
      }
      // Flush current xref-table subsection!
      xrefBuilder.append(
        (prevKey - xrefSubCount + 1) + " " + xrefSubCount + "\r"
          + xrefSubBuilder.toString()
        );
    }

    // XRef-table last section...
    long startxref = stream.getLength();
    {
      // ...header.
      stream.write("xref\r");
      // ...body.
      stream.write(xrefBuilder.toString());
    }

    // Updated trailer...
    try
    {
      // ...header.
      stream.write("trailer\r");
      // ...body.
      // Update the entries!
      PdfDictionary trailer = file.getTrailer();
      trailer.put(PdfName.Size,new PdfInteger(xrefSize));
      trailer.put(PdfName.Prev,new PdfInteger((int)parser.retrieveXRefOffset()));
      // Serialize the contents!
      trailer.writeTo(stream);
      // ...tail.
      stream.write(
        "\r"
          + "startxref\r"
          + startxref + "\r"
          + "%%EOF"
        );
    }
    catch(Exception e)
    {
      // Propagate the exception!
      throw new RuntimeException(e);
    }
  }
  // </interface>
  // </dynamic>
  // </class>
}