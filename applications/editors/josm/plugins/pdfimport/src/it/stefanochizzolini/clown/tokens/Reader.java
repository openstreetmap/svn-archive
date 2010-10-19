/*
  Copyright 2006-2008 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.tokens;

import it.stefanochizzolini.clown.bytes.IInputStream;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfInteger;
import it.stefanochizzolini.clown.objects.PdfName;

import java.io.Closeable;
import java.io.IOException;

/**
  PDF file reader.

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.7
*/
public class Reader
  implements Closeable
{
  // <class>
  // <dynamic>
  // <fields>
  private Parser parser;
  // </fields>

  // <constructors>
  /**
    For internal use only.
  */
  public Reader(
    IInputStream stream,
    File file
    )
  {
    this.parser = new Parser(stream,file);
  }
  // </constructors>

  // <interface>
  // <public>
  public int hashCode(
    )
  {return parser.hashCode();}

  public Parser getParser(
    )
  {return parser;}

  public PdfDictionary readTrailer(
    ) throws FileFormatException
  {
    // Get the offset of the last xref-table section!
    long xrefOffset = parser.retrieveXRefOffset();
    // Go to the start of the last xref-table section!
    parser.seek(xrefOffset); parser.moveNext();
    if(!parser.getToken().equals("xref"))
      throw new FileFormatException("'xref' keyword not found.",parser.getPosition());

    // Searching the start of the last trailer...
    while(true)
    {
      parser.moveNext();
      if(parser.getTokenType() == TokenTypeEnum.Keyword)
        break;
      parser.moveNext();
      int count = (Integer)parser.getToken();
      parser.skip(count * 20);
    }
    if(!parser.getToken().equals("trailer"))
      throw new FileFormatException("'trailer' keyword not found.",parser.getPosition());

    // Get the last trailer!
    parser.moveNext();
    return (PdfDictionary)parser.parsePdfObject();
  }

  /**
    Retrieves the xref-table.
    @return The xref-table entries array.
  */
  public XRefEntry[] readXRefTable(
    PdfDictionary trailer
    ) throws FileFormatException
  {
    // 1. XRef-table.
    // Get the xref-table size!
    PdfInteger xrefTableSize = (PdfInteger)trailer.get(PdfName.Size);
    // Allocate the xref-table array!
    XRefEntry[] xrefEntries = new XRefEntry[xrefTableSize.getRawValue()];

    // 2. Last xref-table section.
    // Move to the start of the last xref-table section!
    parser.seek(parser.retrieveXRefOffset());
    // Parse the last xref-table section!
    readXRefSection(xrefEntries);

    // 3. Previous xref-table sections.
    while(true)
    {
      // 1. Previous xref-table section.
      // Get the previous xref-table section offset!
      PdfInteger prevXRefOffset = (PdfInteger)trailer.get(PdfName.Prev);
      if(prevXRefOffset == null)
        break;
      // Move to the start of the previous xref-table section!
      parser.seek(prevXRefOffset.getRawValue());
      // Parse the previous xref-table section!
      readXRefSection(xrefEntries);

      // 2. Previous trailer.
      // Skip 'trailer' keyword!
      parser.moveNext();
      // Get the previous trailer!
      trailer = (PdfDictionary)parser.parsePdfObject();
    }

    return xrefEntries;
  }

  public String readVersion(
    ) throws FileFormatException
  {
    return parser.retrieveVersion();
  }

  // <Closeable>
  public void close(
    ) throws IOException
  {
    if(parser != null)
    {
      parser.close();
      parser = null;
    }
  }
  // </Closeable>
  // </public>

  // <protected>
  @Override
  protected void finalize(
    ) throws Throwable
  {
    try
    {close();}
    finally
    {super.finalize();}
  }

  protected void readXRefSection(
    XRefEntry[] xrefEntries
    ) throws FileFormatException
  {
    // Reach the start of the xref-table section!
    parser.moveNext();
    if(!((String)parser.getToken()).equals("xref"))
      throw new FileFormatException("'xref' keyword not found.",parser.getPosition());

    // Loop sequentially across the subsections inside the current xref-table section.
    while(true)
    {
      /*
        NOTE: Each iteration of this loop block represents the scanning
        of one subsection.
        We get its bounds (first and last object numbers within its range)
        and then collect its entries.
      */
      // 1. First object number.
      parser.moveNext();
      // Have we reached the end of the xref-table section?
      if((parser.getTokenType() == TokenTypeEnum.Keyword)
          && ((String)parser.getToken()).equals("trailer"))
        break;
      // Is the current token type different from the expected one?
      if(parser.getTokenType() != TokenTypeEnum.Integer)
        throw new FileFormatException("Neither object number of the first object in this xref subsection nor end of xref section found.",parser.getPosition());
      // Get the object number of the first object in this xref-table subsection!
      int startObjectNumber = (Integer)parser.getToken();

      // 2. Last object number.
      parser.moveNext();
      if(parser.getTokenType() != TokenTypeEnum.Integer)
        throw new FileFormatException("Number of entries in this xref subsection not found.",parser.getPosition());
      // Get the object number of the last object in this xref-table subsection!
      int endObjectNumber = (Integer)parser.getToken() + startObjectNumber;

      // 3. xref-table subsection entries.
      for(
        int index = startObjectNumber;
        index < endObjectNumber;
        index++
        )
      {
        // Is the entry undefined?
        if(xrefEntries[index] == null) // Undefined entry.
        {
          // 1. Get the indirect object offset!
          parser.moveNext();
          int offset = (Integer)parser.getToken();
          // 2. Get the object generation number!
          parser.moveNext();
          int generation = (Integer)parser.getToken();
          // 3. Get the usage tag!
          parser.moveNext();
          String usageToken = (String)parser.getToken();
          XRefEntry.UsageEnum usage;
          if(usageToken.equals("n"))
            usage = XRefEntry.UsageEnum.InUse;
          else if(usageToken.equals("f"))
            usage = XRefEntry.UsageEnum.Free;
          else
            throw new FileFormatException("Invalid xref entry.",parser.getPosition());

          // 4. Entry initialization.
          xrefEntries[index] = new XRefEntry(
            index,
            generation,
            offset,
            usage
            );
        }
        else // Already-defined entry.
        {
          // Skip to the next entry!
          parser.moveNext(3);
        }
      }
    }
  }
  // </protected>
  // </interface>
  // </dynamic>
  // </class>
}