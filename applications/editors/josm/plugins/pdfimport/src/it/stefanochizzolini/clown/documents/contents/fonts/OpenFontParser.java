/*
  Copyright 2009-2010 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.documents.contents.fonts;

import it.stefanochizzolini.clown.bytes.IInputStream;

import java.util.Hashtable;
import java.util.Map;

/**
  Open Font Format parser [OFF:2009].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.8
  @version 0.0.8
*/
final class OpenFontParser
{
  // <class>
  // <classes>
  /**
    Font metrics.
  */
  static final class FontMetrics
  {
    /**
      Whether the encoding is custom (symbolic font).
    */
    public boolean isCustomEncoding;//TODO:verify whether it can be replaced by the 'symbolic' variable!!!
    /**
      Unit normalization coefficient.
    */
    public float unitNorm;
    /*
      Font Header ('head' table).
    */
    public int flags; // USHORT.
    public int unitsPerEm; // USHORT.
    public short xMin;
    public short yMin;
    public short xMax;
    public short yMax;
    public int macStyle; // USHORT.
    /*
      Horizontal Header ('hhea' table).
    */
    public short ascender;
    public short descender;
    public short lineGap;
    public int advanceWidthMax; // UFWORD.
    public short minLeftSideBearing;
    public short minRightSideBearing;
    public short xMaxExtent;
    public short caretSlopeRise;
    public short caretSlopeRun;
    public int numberOfHMetrics; // USHORT.
    /*
      OS/2 table ('OS/2' table).
    */
    public short sTypoAscender;
    public short sTypoDescender;
    public short sTypoLineGap;
    public short sxHeight;
    public short sCapHeight;
    /*
      PostScript table ('post' table).
    */
    public float italicAngle;
    public short underlinePosition;
    public short underlineThickness;
    public boolean isFixedPitch;
  }

  /**
    Outline format.
  */
  enum OutlineFormatEnum
  {
    /**
      TrueType format outlines.
    */
    TrueType,
    /**
      Compact Font Format outlines.
    */
    CFF;
  }
  // </classes>

  // <static>
  // <fields>
  private static final String Encoding_Latin1 = "ISO-8859-1";
  private static final String Encoding_Unicode = "UTF-16";

  private static final int MicrosoftLanguage_UsEnglish = 0x409;
  private static final int NameID_FontPostscriptName = 6;

  private static final int PlatformID_Unicode = 0;
  private static final int PlatformID_Macintosh = 1;
  private static final int PlatformID_Microsoft = 3;
  // </fields>

  // <interface>
  // <public>
  /**
   * Gets whether the given data represents a valid Open Font.
   */
  public static boolean isOpenFont(
    IInputStream fontData
    )
  {
    long position = fontData.getPosition();
    try
    {
      fontData.setPosition(0);
      getOutlineFormat(fontData.readInt()); // NOTE: An exception is expected to be thrown in case of wrong file format.
      return true;
    }
    catch(Exception e)
    {return false;}
    finally
    {fontData.setPosition(position);}
  }
  // </public>

  // <private>
  private static OutlineFormatEnum getOutlineFormat(
    int versionCode
    )
  {
    // Which font file format ('sfnt') version?
    switch(versionCode)
    {
      case(0x00010000): // TrueType (standard/Windows).
      case(0x74727565): // TrueType (legacy/Apple).
        return OutlineFormatEnum.TrueType;
      case(0x4F54544F): // CFF (Type 1).
        return OutlineFormatEnum.CFF;
      default:
        throw new UnsupportedOperationException("Unknown OpenFont format version.");
    }
  }
  // </private>
  // </interface>
  // </static>

  // <dynamic>
  // <fields>
  public FontMetrics metrics;

  public String fontName;
  public OutlineFormatEnum outlineFormat;
  /**
    Whether glyphs are indexed by custom (non-Unicode) encoding.
  */
  public boolean symbolic;

  public Map<Integer,Integer> glyphIndexes;
  public Map<Integer,Integer> glyphKernings;
  public Map<Integer,Integer> glyphWidths;

  public IInputStream fontData;

  private Map<String,Integer> tableOffsets;
  // </fields>

  // <constructors>
  OpenFontParser(
    IInputStream fontData
    )
  {
    this.fontData = fontData;

    load();
  }
  // </constructors>

  // <interface>
  // <private>
  /**
    Loads the font data.
  */
  private void load(
    )
  {
    metrics = new FontMetrics();

    try
    {
      // 1. Offset Table.
      fontData.seek(0);
      // Get the outline format!
      this.outlineFormat = getOutlineFormat(fontData.readInt());
      // Get the number of tables!
      int tableCount = fontData.readUnsignedShort();
      this.tableOffsets = new Hashtable<String,Integer>(tableCount);

      // 2. Table Directory.
      // Skip to the beginning of the table directory!
      fontData.skip(6);
      // Collecting the table offsets...
      for(
        int index = 0;
        index < tableCount;
        index++
        )
      {
        // Get the table tag!
        String tag = readString_ascii(4);
        // Skip to the table offset!
        fontData.skip(4);
        // Get the table offset!
        int offset = fontData.readInt();
        // Collect the table offset!
        tableOffsets.put(tag,offset);

        // Skip to the next entry!
        fontData.skip(4);
      }

      this.fontName = load_getName(NameID_FontPostscriptName);

      // Loading font metrics...
      load_tables();
      load_cMap();
      load_glyphWidths();
      load_glyphKerning();
    }
    catch(Exception e)
    {throw new RuntimeException(e);}
  }

  /**
    Loads the character to glyph index mapping table.
  */
  private void load_cMap(
    ) throws FontFileFormatException
  {
    /*
      NOTE: A 'cmap' table may contain one or more subtables that represent multiple encodings
      intended for use on different platforms (such as Mac OS and Windows).
      Each subtable is identified by the two numbers, such as (3,1), that represent a combination
      of a platform ID and a platform-specific encoding ID, respectively.
      A symbolic font (used to display glyphs that do not use standard encodings, i.e. neither
      MacRomanEncoding nor WinAnsiEncoding) program's "cmap" table should contain a (1,0) subtable.
      It may also contain a (3,0) subtable; if present, this subtable should map from character
      codes in the range 0xF000 to 0xF0FF by prepending the single-byte codes in the (1,0) subtable
      with 0xF0 and mapping to the corresponding glyph descriptions.
    */
    // Character To Glyph Index Mapping Table ('cmap' table).
    // Retrieve the location info!
    Integer tableOffset = tableOffsets.get("cmap");
    if(tableOffset == null)
      throw new FontFileFormatException("'cmap' table does NOT exist.");

    int cmap10Offset = 0;
    int cmap31Offset = 0;
    try
    {
      // Header.
      // Go to the number of tables!
      fontData.seek(tableOffset + 2);
      int tableCount = fontData.readUnsignedShort();

      // Encoding entries.
      for(
        int tableIndex = 0;
        tableIndex < tableCount;
        tableIndex++
        )
      {
        // Platform ID.
        int platformID = fontData.readUnsignedShort();
        // Encoding ID.
        int encodingID = fontData.readUnsignedShort();
        // Subtable offset.
        int offset = fontData.readInt();
        switch(platformID)
        {
          case PlatformID_Macintosh:
            switch(encodingID)
            {
              case 0: // Symbolic font.
                cmap10Offset = offset;
                break;
            }
            break;
          case PlatformID_Microsoft:
            switch(encodingID)
            {
              case 0: // Symbolic font.
                break;
              case 1: // Nonsymbolic font.
                cmap31Offset = offset;
                break;
            }
            break;
        }
      }
    }
    catch(Exception e)
    {throw new RuntimeException(e);}

    /*
      NOTE: Symbolic fonts use specific (non-standard, i.e. neither Unicode nor
      platform-standard) font encodings.
    */
    if(cmap31Offset > 0) // Nonsymbolic.
    {
      metrics.isCustomEncoding = false;
      // Go to the beginning of the subtable!
      fontData.seek(tableOffset + cmap31Offset);
    }
    else if(cmap10Offset > 0) // Symbolic.
    {
      metrics.isCustomEncoding = true;
      // Go to the beginning of the subtable!
      fontData.seek(tableOffset + cmap10Offset);
    }
    else
      throw new FontFileFormatException("CMAP table unavailable.");

    int format;
    try
    {format = fontData.readUnsignedShort();}
    catch(Exception e)
    {throw new FontFileFormatException("Cmap table format not found.",e,fontData.getPosition());}
    // Which cmap table format?
    switch(format)
    {
      case 0: // Byte encoding table.
        load_cMap_format0(); break;
      case 4: // Segment mapping to delta values.
        load_cMap_format4(); break;
      case 6: // Trimmed table mapping.
        load_cMap_format6(); break;
      default:
        throw new FontFileFormatException("Cmap table format " + format + " NOT supported.");
    }
  }

  /**
    Loads format-0 cmap subtable (Byte encoding table, that is Apple standard
    character-to-glyph index mapping table).
  */
  private void load_cMap_format0(
    ) throws FontFileFormatException
  {
    /*
      NOTE: This is a simple 1-to-1 mapping of character codes to glyph indices.
      The glyph collection is limited to 256 entries.
    */
    symbolic = true;
    glyphIndexes = new Hashtable<Integer,Integer>(256);

    try
    {
      // Skip to the mapping array!
      fontData.skip(4);
      // Glyph index array.
      // Iterating through the glyph indexes...
      for(
        int code = 0;
        code < 256;
        code++
        )
      {
        glyphIndexes.put(
          code, // Character code.
          fontData.readUnsignedByte() // Glyph index.
          );
      }
    }
    catch(Exception e)
    {throw new FontFileFormatException("Cmap data retrieval failed.",e,fontData.getPosition());}
  }

  /**
    Loads format-4 cmap subtable (Segment mapping to delta values, that is Microsoft standard
    character to glyph index mapping table for fonts that support Unicode ranges other than the
    range [U+D800 - U+DFFF] (defined as Surrogates Area, in Unicode v 3.0)).
  */
  private void load_cMap_format4(
    ) throws FontFileFormatException
  {
    /*
      NOTE: This format is used when the character codes for the characters represented by a font
      fall into several contiguous ranges, possibly with holes in some or all of the ranges (i.e.
      some of the codes in a range may not have a representation in the font).
      The format-dependent data is divided into three parts, which must occur in the following
      order:
        1. A header gives parameters for an optimized search of the segment list;
        2. Four parallel arrays (end characters, start characters, deltas and range offsets)
        describe the segments (one segment for each contiguous range of codes);
        3. A variable-length array of glyph IDs.
    */
    symbolic = false;
    try
    {
      // 1. Header.
      // Get the table length!
      int tableLength = fontData.readUnsignedShort(); // USHORT.

      // Skip to the segment count!
      fontData.skip(2);
      // Get the segment count!
      int segmentCount = fontData.readUnsignedShort() / 2;

      // 2. Arrays describing the segments.
      // Skip to the array of end character code for each segment!
      fontData.skip(6);
      // End character code for each segment.
      int[] endCodes = new int[segmentCount]; // USHORT.
      for(
        int index = 0;
        index < segmentCount;
        index++
        )
      {endCodes[index] = fontData.readUnsignedShort();}

      // Skip to the array of start character code for each segment!
      fontData.skip(2);
      // Start character code for each segment.
      int[] startCodes = new int[segmentCount]; // USHORT.
      for(
        int index = 0;
        index < segmentCount;
        index++
        )
      {startCodes[index] = fontData.readUnsignedShort();}

      // Delta for all character codes in segment.
      short[] deltas = new short[segmentCount];
      for(
        int index = 0;
        index < segmentCount;
        index++
        )
      {deltas[index] = fontData.readShort();}

      // Offsets into glyph index array.
      int[] rangeOffsets = new int[segmentCount]; // USHORT.
      for(
        int index = 0;
        index < segmentCount;
        index++
        )
      {rangeOffsets[index] = fontData.readUnsignedShort();}

      // 3. Glyph ID array.
      /*
        NOTE: There's no explicit field defining the array length;
        it must be inferred from the space left by the known fields.
      */
      int glyphIndexCount = tableLength / 2 // Number of 16-bit words inside the table.
        - 8 // Number of single-word header fields (8 fields: format, length, language, segCountX2, searchRange, entrySelector, rangeShift, reservedPad).
        - segmentCount * 4; // Number of single-word items in the arrays describing the segments (4 arrays of segmentCount items).
      int[] glyphIds = new int[glyphIndexCount]; // USHORT.
      for(
        int index = 0;
        index < glyphIds.length;
        index++
        )
      {glyphIds[index] = fontData.readUnsignedShort();}

      glyphIndexes = new Hashtable<Integer,Integer>(glyphIndexCount);
      // Iterating through the segments...
      for(
        int segmentIndex = 0;
        segmentIndex < segmentCount;
        segmentIndex++
        )
      {
        int endCode = endCodes[segmentIndex];
        // Is it NOT the last end character code?
        /*
          NOTE: The final segment's endCode MUST be 0xFFFF. This segment need not (but MAY)
          contain any valid mappings (it can just map the single character code 0xFFFF to
          missing glyph). However, the segment MUST be present.
        */
        if(endCode < 0xFFFF)
        {endCode++;}
        // Iterating inside the current segment...
        for(
          int code = startCodes[segmentIndex];
          code < endCode;
          code++
          )
        {
          int glyphIndex;
          // Doesn't the mapping of character codes rely on glyph ID?
          if(rangeOffsets[segmentIndex] == 0) // No glyph-ID reliance.
          {
            /*
              NOTE: If the range offset is 0, the delta value is added directly to the character
              code to get the corresponding glyph index. The delta arithmetic is modulo 65536.
            */
            glyphIndex = (code + deltas[segmentIndex]) & 0xFFFF;
          }
          else // Glyph-ID reliance.
          {
            /*
              NOTE: If the range offset is NOT 0, the mapping of character codes relies on glyph ID.
              The character code offset from start code is added to the range offset. This sum is
              used as an offset from the current location within range offset itself to index out
              the correct glyph ID. This obscure indexing trick (sic!) works because glyph ID
              immediately follows range offset in the font file. The C expression that yields the
              address to the glyph ID is:
                *(rangeOffsets[segmentIndex]/2
                + (code - startCodes[segmentIndex])
                + &idRangeOffset[segmentIndex])
              As Java language semantics don't deal directly with pointers, we have to further
              exploit such a trick reasoning with 16-bit displacements in order to yield an index
              instead of an address (sooo-good!).
            */
            // Retrieve the glyph index!
            int glyphIdIndex = rangeOffsets[segmentIndex] / 2 // 16-bit word range offset.
              + (code - startCodes[segmentIndex]) // Character code offset from start code.
              - (segmentCount - segmentIndex); // Physical offset between the offsets into glyph index array and the glyph index array.

            /*
              NOTE: The delta value is added to the glyph ID to get the corresponding glyph index.
              The delta arithmetic is modulo 65536.
            */
            glyphIndex = (glyphIds[glyphIdIndex] + deltas[segmentIndex]) & 0xFFFF;
          }

          glyphIndexes.put(
            code, // Character code.
            glyphIndex // Glyph index.
            );
        }
      }
    }
    catch(Exception e)
    {throw new FontFileFormatException("Cmap data retrieval failed.",e,fontData.getPosition());}
  }

  /**
    Loads format-6 cmap subtable (Trimmed table mapping).
  */
  private void load_cMap_format6(
    ) throws FontFileFormatException
  {
    symbolic = true;
    try
    {
      // Skip to the first character code!
      fontData.skip(4);
      int firstCode = fontData.readUnsignedShort();
      int codeCount = fontData.readUnsignedShort();
      glyphIndexes = new Hashtable<Integer,Integer>(codeCount);
      for(
        int code = firstCode,
          lastCode = firstCode + codeCount;
        code < lastCode;
        code++
        )
      {
        glyphIndexes.put(
          code, // Character code.
          fontData.readUnsignedShort() // Glyph index.
          );
      }
    }
    catch(Exception e)
    {throw new FontFileFormatException("Cmap data retrieval failed.",e,fontData.getPosition());}
  }

  /**
    Gets a name.

    @param id Name identifier.
  */
  private String load_getName(
    int id
    ) throws FontFileFormatException
  {
    // Naming Table ('name' table).
    Integer tableOffset = tableOffsets.get("name");
    if(tableOffset == null)
      throw new FontFileFormatException("'name' table does NOT exist.");

    try
    {
      // Go to the number of name records!
      fontData.seek(tableOffset + 2);

      int recordCount = fontData.readUnsignedShort(); // USHORT.
      int storageOffset = fontData.readUnsignedShort(); // USHORT.
      // Iterating through the name records...
      for(
        int recordIndex = 0;
        recordIndex < recordCount;
        recordIndex++
        )
      {
        int platformID = fontData.readUnsignedShort(); // USHORT.
        // Is it the default platform?
        if(platformID == PlatformID_Microsoft)
        {
          fontData.skip(2);
          int languageID = fontData.readUnsignedShort(); // USHORT.
          // Is it the default language?
          if(languageID == MicrosoftLanguage_UsEnglish)
          {
            int nameID = fontData.readUnsignedShort(); // USHORT.
            // Does the name ID equal the searched one?
            if(nameID == id)
            {
              int length = fontData.readUnsignedShort(); // USHORT.
              int offset = fontData.readUnsignedShort(); // USHORT.

              // Go to the name string!
              fontData.seek(tableOffset + storageOffset + offset);

              return readString(length,platformID);
            }
            else
            {fontData.skip(4);}
          }
          else
          {fontData.skip(6);}
        }
        else
        {fontData.skip(10);}
      }

      return null; // Not found.
    }
    catch(Exception e)
    {throw new FontFileFormatException("Name '" + id + "' not found.",e,fontData.getPosition());}
  }

  /**
    Loads the glyph kerning.
  */
  private void load_glyphKerning(
    ) throws FontFileFormatException
  {
    // Kerning ('kern' table).
    Integer tableOffset = tableOffsets.get("kern");
    if(tableOffset == null)
      return;

    try
    {
      // Go to the table count!
      fontData.seek(tableOffset + 2);
      int subtableCount = fontData.readUnsignedShort(); // USHORT.

      glyphKernings = new Hashtable<Integer,Integer>();
      int subtableOffset = (int)fontData.getPosition();
      // Iterating through the subtables...
      for(
        int subtableIndex = 0;
        subtableIndex < subtableCount;
        subtableIndex++
        )
      {
        // Go to the subtable length!
        fontData.seek(subtableOffset + 2);
        // Get the subtable length!
        int length = fontData.readUnsignedShort(); // USHORT.

        // Get the type of information contained in the subtable!
        int coverage = fontData.readUnsignedShort(); // USHORT.
        // Is it a format-0 subtable?
        /*
          NOTE: coverage bits 8-15 (format of the subtable) MUST be all zeros
          (representing format 0).
        */
        //
        if((coverage & 0xff00) == 0x0000)
        {
          int pairCount = fontData.readUnsignedShort(); // USHORT.

          // Skip to the beginning of the list!
          fontData.skip(6);
          // List of kerning pairs and values.
          for(
            int pairIndex = 0;
            pairIndex < pairCount;
            pairIndex++
            )
          {
            // Get the glyph index pair (left-hand and right-hand)!
            int pair = fontData.readInt(); // USHORT USHORT.
            // Get the normalized kerning value!
            int value = (int)(fontData.readShort() * metrics.unitNorm);

            glyphKernings.put(pair,value);
          }
        }

        subtableOffset += length;
      }
    }
    catch(Exception e)
    {throw new FontFileFormatException("Glyph kernings retrieval failed.",e,fontData.getPosition());}
  }

  /**
    Loads the glyph widths.
  */
  private void load_glyphWidths(
    ) throws FontFileFormatException
  {
    // Horizontal Metrics ('hmtx' table).
    Integer tableOffset = tableOffsets.get("hmtx");
    if(tableOffset == null)
      throw new FontFileFormatException("'hmtx' table does NOT exist.");

    try
    {
      // Go to the glyph horizontal-metrics entries!
      fontData.seek(tableOffset);
      glyphWidths = new Hashtable<Integer,Integer>(metrics.numberOfHMetrics);
      for(
        int index = 0;
        index < metrics.numberOfHMetrics;
        index++
        )
      {
        // Get the glyph width!
        glyphWidths.put(index,(int)(fontData.readUnsignedShort() * metrics.unitNorm));
        // Skip the left side bearing!
        fontData.skip(2);
      }
    }
    catch(Exception e)
    {throw new FontFileFormatException("Glyph widths retrieval failed.",e,fontData.getPosition());}
  }

  /**
    Loads general tables.
  */
  private void load_tables(
    ) throws FontFileFormatException
  {
    // Font Header ('head' table).
    Integer tableOffset = tableOffsets.get("head");
    if(tableOffset == null)
      throw new FontFileFormatException("'head' table does NOT exist.");

    try
    {
      // Go to the font flags!
      fontData.seek(tableOffset + 16);
      metrics.flags = fontData.readUnsignedShort();
      metrics.unitsPerEm = fontData.readUnsignedShort();
      metrics.unitNorm = 1000f / metrics.unitsPerEm;
      // Go to the bounding box limits!
      fontData.skip(16);
      metrics.xMin = fontData.readShort();
      metrics.yMin = fontData.readShort();
      metrics.xMax = fontData.readShort();
      metrics.yMax = fontData.readShort();
      metrics.macStyle = fontData.readUnsignedShort();
    }
    catch(Exception e)
    {throw new FontFileFormatException("'head' metrics retrieval failed.",e,fontData.getPosition());}

    // Font Header ('OS/2' table).
    tableOffset = tableOffsets.get("OS/2");
    if(tableOffset != null)
    {
      try
      {
        fontData.seek(tableOffset);
        int version = fontData.readUnsignedShort();
        // Go to the ascender!
        fontData.skip(66);
        metrics.sTypoAscender = fontData.readShort();
        metrics.sTypoDescender = fontData.readShort();
        metrics.sTypoLineGap = fontData.readShort();
        if(version >= 2)
        {
          fontData.skip(12);
          metrics.sxHeight = fontData.readShort();
          metrics.sCapHeight = fontData.readShort();
        }
        else
        {
          /*
            NOTE: These are just rule-of-thumb values,
            in case the xHeight and CapHeight fields aren't available.
          */
          metrics.sxHeight = (short)(.5 * metrics.unitsPerEm);
          metrics.sCapHeight = (short)(.7 * metrics.unitsPerEm);
        }
      }
      catch(Exception e)
      {throw new FontFileFormatException("'OS/2' metrics retrieval failed.",e,fontData.getPosition());}
    }

    // Horizontal Header ('hhea' table).
    tableOffset = tableOffsets.get("hhea");
    if(tableOffset == null)
      throw new FontFileFormatException("'hhea' table does NOT exist.");

    try
    {
      // Go to the ascender!
      fontData.seek(tableOffset + 4);
      metrics.ascender = fontData.readShort();
      metrics.descender = fontData.readShort();
      metrics.lineGap = fontData.readShort();
      metrics.advanceWidthMax = fontData.readUnsignedShort();
      metrics.minLeftSideBearing = fontData.readShort();
      metrics.minRightSideBearing = fontData.readShort();
      metrics.xMaxExtent = fontData.readShort();
      metrics.caretSlopeRise = fontData.readShort();
      metrics.caretSlopeRun = fontData.readShort();
      // Go to the horizontal metrics count!
      fontData.skip(12);
      metrics.numberOfHMetrics = fontData.readUnsignedShort();
    }
    catch(Exception e)
    {throw new FontFileFormatException("'hhea' metrics retrieval failed.",e,fontData.getPosition());}

    // PostScript ('post' table).
    tableOffset = tableOffsets.get("post");
    if(tableOffset == null)
      throw new FontFileFormatException("'post' table does NOT exist.");

    try
    {
      // Go to the italic angle!
      fontData.seek(tableOffset + 4);
      metrics.italicAngle =
        fontData.readShort() // Fixed-point mantissa (16 bits).
        + (float)fontData.readUnsignedShort() / 16384; // Fixed-point fraction (16 bits).
      metrics.underlinePosition = fontData.readShort();
      metrics.underlineThickness = fontData.readShort();
      metrics.isFixedPitch = (fontData.readInt() != 0);
    }
    catch(Exception e)
    {throw new FontFileFormatException("'post' metrics retrieval failed.",e,fontData.getPosition());}
  }

  /**
    Reads a string.
  */
  private String readString(
    int length,
    int platformID
    )
  {
    // Which platform?
    switch(platformID)
    {
      case PlatformID_Unicode:
      case PlatformID_Microsoft:
        return readString_unicode(length);
      default:
        return readString_ascii(length);
    }
  }

  /**
    Reads a string from the font file using the extended ASCII encoding.
  */
  private String readString_ascii(
    int length
    )
  {
    /*
      NOTE: Extended ASCII (Latin1) is a single-byte encoding
      (I know you already knew that!).
    */
    try
    {
      byte[] data = new byte[length];
      fontData.read(data);

      return new String(data,Encoding_Latin1);
    }
    catch(Exception e)
    {throw new RuntimeException(e);}
  }

  /**
    Reads a string from the font file using the Unicode encoding.
  */
  private String readString_unicode(
    int length
    )
  {
    /*
      NOTE: Unicode is a double-byte encoding
      (I know you already knew that!).
    */
    try
    {
      byte[] data = new byte[length];
      fontData.read(data);

      return new String(data,Encoding_Unicode);
    }
    catch(Exception e)
    {throw new RuntimeException(e);}
  }
  // </private>
  // </interface>
  // </dynamic>
  // </class>
}