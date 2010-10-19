/*
  Copyright 2006-2010 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

import it.stefanochizzolini.clown.bytes.FileInputStream;
import it.stefanochizzolini.clown.bytes.IInputStream;
import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.objects.IPdfNumber;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfInteger;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfReference;
import it.stefanochizzolini.clown.objects.PdfStream;
import it.stefanochizzolini.clown.util.BiMap;
import it.stefanochizzolini.clown.util.ByteArray;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.io.ByteArrayOutputStream;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.Map;

/**
  Abstract font [PDF:1.6:5.4].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
*/
public abstract class Font
  extends PdfObjectWrapper<PdfDictionary>
{
  // <class>
  // <classes>
  /**
    Font descriptor flags [PDF:1.6:5.7.1].
  */
  public enum FlagsEnum
  {
    // <class>
    // <static>
    // <fields>
    /**
      All glyphs have the same width.
    */
    FixedPitch(0x1),
    /**
      Glyphs have serifs.
    */
    Serif(0x2),
    /**
      Font contains glyphs outside the Adobe standard Latin character set.
    */
    Symbolic(0x4),
    /**
      Glyphs resemble cursive handwriting.
    */
    Script(0x8),
    /**
      Font uses the Adobe standard Latin character set.
    */
    Nonsymbolic(0x20),
    /**
      Glyphs have dominant vertical strokes that are slanted.
    */
    Italic(0x40),
    /**
      Font contains no lowercase letters.
    */
    AllCap(0x10000),
    /**
      Font contains both uppercase and lowercase letters.
    */
    SmallCap(0x20000),
    /**
      Thicken bold glyphs at small text sizes.
    */
    ForceBold(0x40000);
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
  // <interface>
  // <public>
  /**
   * Creates the representation of a font.
   */
  public static Font get(
    Document context,
    String path
    )
  {
    try
    {
      return get(
        context,
        new FileInputStream(
          new java.io.RandomAccessFile(path,"r")
          )
        );
    }
    catch(Exception e)
    {throw new RuntimeException(e);}
  }

  /**
   * Creates the representation of a font.
   */
  public static Font get(
    Document context,
    java.io.File file
    )
  {return get(context,file.getPath());}

  /**
   * Creates the representation of a font.
   */
  public static Font get(
    Document context,
    IInputStream fontData
    )
  {
    if(OpenFontParser.isOpenFont(fontData))
      return CompositeFont.get(context,fontData);
    else
      throw new NotImplementedException();
   }

  /**
    Gets the scaling factor to be applied to unscaled metrics to get actual measures.
  */
  public static final double getScalingFactor(
    double size
    )
  {return (0.001 * size);}

  /**
    Wraps a font reference into a font object.

    @param reference Reference to a font object.
    @return Font object associated to the reference.
  */
  public static final Font wrap(
    PdfReference reference
    )
  {
    if(reference == null)
      return null;

    {
      // Has the font been already instantiated?
      /*
        NOTE: Font structures are reified as complex objects, both IO- and CPU-intensive to load.
        So, it's convenient to retrieve them from a common cache whenever possible.
      */
      Hashtable<PdfReference,Object> cache = reference.getIndirectObject().getFile().getDocument().cache;
      if(cache.containsKey(reference))
        return (Font)cache.get(reference);
    }

    PdfDictionary fontDictionary = (PdfDictionary)reference.getDataObject();
    PdfName fontType = (PdfName)fontDictionary.get(PdfName.Subtype);
    if(fontType == null)
      throw new RuntimeException("Font type undefined (reference: " + reference + ")");

    if(fontType.equals(PdfName.Type1)) // Type 1.
    {
      if(!fontDictionary.containsKey(PdfName.FontDescriptor)) // Standard Type 1.
        return new StandardType1Font(reference);
      else // Custom Type 1.
      {
        PdfDictionary fontDescriptor = (PdfDictionary)fontDictionary.resolve(PdfName.FontDescriptor);
        if(fontDescriptor.containsKey(PdfName.FontFile3)
            && ((PdfName)((PdfStream)fontDescriptor.resolve(PdfName.FontFile3)).getHeader().resolve(PdfName.Subtype)).equals(PdfName.OpenType)) // OpenFont/CFF.
          throw new NotImplementedException();
        else // Non-OpenFont Type 1.
          return new Type1Font(reference);
      }
    }
    else if(fontType.equals(PdfName.TrueType)) // TrueType.
      return new TrueTypeFont(reference);
    else if(fontType.equals(PdfName.Type0)) // OpenFont.
    {
      PdfDictionary cidFontDictionary = (PdfDictionary)((PdfArray)fontDictionary.resolve(PdfName.DescendantFonts)).resolve(0);
      PdfName cidFontType = (PdfName)cidFontDictionary.get(PdfName.Subtype);
      if(cidFontType.equals(PdfName.CIDFontType0)) // OpenFont/CFF.
        return new Type0Font(reference);
      else if(cidFontType.equals(PdfName.CIDFontType2)) // OpenFont/TrueType.
        return new Type2Font(reference);
      else
        throw new NotImplementedException("Type 0 subtype " + cidFontType + " not supported yet.");
    }
    else if(fontType.equals(PdfName.Type3)) // Type 3.
      return new Type3Font(reference);
    else if(fontType.equals(PdfName.MMType1)) // MMType1.
      return new MMType1Font(reference);
    else // Unknown.
      throw new UnsupportedOperationException("Unknown font type: " + fontType + " (reference: " + reference + ")");
  }
  // </public>
  // </interface>
  // </static>

  // <dynamic>
  // <fields>
  /*
    NOTE: In order to avoid nomenclature ambiguities, these terms are used consistently within the code:
    * unicode: character encoded according to the Unicode standard;
    * character code: codepoint corresponding to a character expressed inside a string object of a content stream;
    * glyph index: identifier of the graphical representation of a character.
  */
  /**
    Unicodes by character code.
    <h3>Note</h3>
    <p>When this map is populated, {@link #symbolic} variable shall accordingly be set.</p>
  */
  protected BiMap<ByteArray,Integer> codes;
  /**
    Default glyph width.
  */
  protected int defaultGlyphWidth;
  /**
    Glyph indexes by unicode.
  */
  protected Map<Integer,Integer> glyphIndexes;
  /**
    Glyph kernings by (left-right) glyph index pairs.
  */
  protected Map<Integer,Integer> glyphKernings;
  /**
    Glyph widths by glyph index.
  */
  protected Map<Integer,Integer> glyphWidths;
  /**
    Whether the font encoding is custom (that is non-Unicode).
  */
  protected boolean symbolic = true;

  /**
    Maximum character code byte size.
  */
  private int charCodeMaxLength = 0;
  // </fields>

  // <constructors>
  /**
    Creates a new font structure within the given document context.
  */
  protected Font(
    Document context
    )
  {
    super(
      context.getFile(),
      new PdfDictionary(
        new PdfName[]{PdfName.Type},
        new PdfDirectObject[]{PdfName.Font}
        )
      );
    initialize();
  }

  /**
    Loads an existing font structure.
  */
  protected Font(
    PdfDirectObject baseObject
    )
  {
    super(
      baseObject,
      null // NO container. NOTE: this is a simplification (the spec [PDF:1.6] doesn't apparently prescribe the use of an indirect object for font dictionary, whilst the general practice is as such. If an exception occurs, you'll need to specify the proper container).
      );
    initialize();
    load();
  }
  // </constructors>

  // <interface>
  // <public>
  /**
    Gets the text from the given internal representation.

    @param code Internal representation to decode.
    @since 0.0.6
  */
  public final String decode(
    byte[] code
    )
  {
    StringBuilder textBuilder = new StringBuilder();
    {
      byte[][] codeBuffers = new byte[charCodeMaxLength+1][];
      for(
        int codeBufferIndex = 0;
        codeBufferIndex <= charCodeMaxLength;
        codeBufferIndex++
        )
      {codeBuffers[codeBufferIndex] = new byte[codeBufferIndex];}
      int position = 0;
      int codeLength = code.length;
      int codeBufferSize = 1;
      while(position < codeLength)
      {
        byte[] codeBuffer = codeBuffers[codeBufferSize];
        System.arraycopy(code,position,codeBuffer,0,codeBufferSize);
        Integer textChar = codes.get(new ByteArray(codeBuffer));
        if(textChar == null)
        {
          if(codeBufferSize < charCodeMaxLength)
          {
            codeBufferSize++;
            continue;
          }
          /*
            NOTE: In case no valid code entry is found, a default space is resiliantely
            applied instead of throwing an exception.
            This is potentially risky as failing to determine the actual code length
            may result in a "code shifting" which could affect following characters.
           */
          textChar = (int)' ';
        }
        textBuilder.append((char)(int)textChar);
        position += codeBufferSize;
        codeBufferSize = 1;
      }
    }
    return textBuilder.toString();
  }

  /**
    Gets the internal representation of the given text.

    @param text Text to encode.
    @since 0.0.6
  */
  public final byte[] encode(
    String text
    )
  {
    ByteArrayOutputStream encodedStream = new ByteArrayOutputStream();
    try
    {
      for(char textChar : text.toCharArray())
      {
        byte[] charCode = codes.getKey((int)textChar).data;
        encodedStream.write(charCode);
      }
      encodedStream.close();
    }
    catch(Exception exception)
    {throw new RuntimeException(exception);}

    return encodedStream.toByteArray();
  }

  public final boolean equals(
    Font object
    )
  {
    return (getType().equals(object.getType())
      && getName().equals(object.getName()));
  }

  /**
    Gets the unscaled vertical offset from the baseline to the ascender line (ascent).
    The value is a positive number.
  */
  public double getAscent(
    )
  {return ((IPdfNumber)getDescriptor().get(PdfName.Ascent)).getNumberValue();}

  /**
    Gets the vertical offset from the baseline to the ascender line (ascent),
    scaled to the given font size.
    The value is a positive number.

    @param size Font size.
  */
  public final double getAscent(
    double size
    )
  {return getAscent() * getScalingFactor(size);}

  /**
    Gets the unscaled vertical offset from the baseline to the descender line (descent).
    The value is a negative number.
  */
  public double getDescent(
    )
  {return ((IPdfNumber)getDescriptor().get(PdfName.Descent)).getNumberValue();}

  /**
    Gets the vertical offset from the baseline to the descender line (descent), scaled to the given font size.
    The value is a negative number.

    @param size Font size.
  */
  public final double getDescent(
    double size
    )
  {return getDescent() * getScalingFactor(size);}

  /**
    Gets the font descriptor flags.
  */
  public EnumSet<FlagsEnum> getFlags(
    )
  {
    PdfInteger flagsObject = (PdfInteger)File.resolve(
      getDescriptor().get(PdfName.Flags)
      );
    if(flagsObject == null)
      return EnumSet.noneOf(FlagsEnum.class);

    return FlagsEnum.toEnumSet(flagsObject.getRawValue());
  }

  /**
    Gets the unscaled height of the given character.

    @param textChar Character whose height has to be calculated.
  */
  public final double getHeight(
    char textChar
    )
  {return getLineHeight();}

  /**
    Gets the height of the given character, scaled to the given font size.

    @param textChar Character whose height has to be calculated.
    @param size Font size.
  */
  public final double getHeight(
    char textChar,
    double size
    )
  {return getHeight(textChar) * getScalingFactor(size);}

  /**
    Gets the unscaled height of the given text.

    @param text Text whose height has to be calculated.
  */
  public final double getHeight(
    String text
    )
  {return getLineHeight();}

  /**
    Gets the height of the given text, scaled to the given font size.

    @param text Text whose height has to be calculated.
    @param size Font size.
  */
  public final double getHeight(
    String text,
    double size
    )
  {return getHeight(text) * getScalingFactor(size);}

  /**
    Gets the width (kerning inclusive) of the given text, scaled to the given font size.

    @param text Text whose width has to be calculated.
    @param size Font size.
  */
  public final double getKernedWidth(
    String text,
    double size
    )
  {return (getWidth(text) + getKerning(text)) * getScalingFactor(size);}

  /**
    Gets the unscaled kerning width between two given characters.

    @param textChar1 Left character.
    @param textChar2 Right character,
  */
  public final int getKerning(
    char textChar1,
    char textChar2
    )
  {
    try
    {
      return glyphKernings.get(
        glyphIndexes.get((int)textChar1) << 16 // Left-hand glyph index.
          + glyphIndexes.get((int)textChar2) // Right-hand glyph index.
        );
    }
    catch(Exception e)
    {return 0;}
  }

  /**
    Gets the unscaled kerning width inside the given text.

    @param text Text whose kerning has to be calculated.
  */
  public final int getKerning(
    String text
    )
  {
    int kerning = 0;
    // Are kerning pairs available?
    if(glyphKernings != null)
    {
      char textChars[] = text.toCharArray();
      for(
        int index = 0,
          length = text.length() - 1;
        index < length;
        index++
        )
      {
        kerning += getKerning(
          textChars[index],
          textChars[index + 1]
          );
      }
    }
    return kerning;
  }

  /**
    Gets the kerning width inside the given text, scaled to the given font size.

    @param text Text whose kerning has to be calculated.
    @param size Font size.
  */
  public final double getKerning(
    String text,
    double size
    )
  {return getKerning(text) * getScalingFactor(size);}

  /**
    Gets the unscaled line height.
  */
  public double getLineHeight(
    )
  {return getAscent() - getDescent();}

  /**
    Gets the line height, scaled to the given font size.

    @param size Font size.
  */
  public final double getLineHeight(
    double size
    )
  {return getLineHeight() * getScalingFactor(size);}

  /**
    Gets the PostScript name of the font.
  */
  public final String getName(
    )
  {return ((PdfName)getBaseDataObject().get(PdfName.BaseFont)).toString();}

  /**
    Gets the font type.
  */
  public final String getType(
    )
  {return ((PdfName)getBaseDataObject().get(PdfName.Subtype)).toString();}

  /**
    Gets the unscaled width of the given character.

    @param textChar Character whose width has to be calculated.
  */
  public int getWidth(
    char textChar
    )
  {
    Integer glyphWidth = glyphWidths.get(glyphIndexes.get((int)textChar));
    if(glyphWidth == null)
      return defaultGlyphWidth;
    else
      return glyphWidth;
  }

  /**
    Gets the width of the given character, scaled to the given font size.

    @param textChar Character whose height has to be calculated.
    @param size Font size.
  */
  public final double getWidth(
    char textChar,
    double size
    )
  {return getWidth(textChar) * getScalingFactor(size);}

  /**
    Gets the unscaled width (kerning exclusive) of the given text.

    @param text Text whose width has to be calculated.
  */
  public int getWidth(
    String text
    )
  {
    int width = 0;
    for(char textChar : text.toCharArray())
    {width += getWidth(textChar);}

    return width;
  }

  /**
    Gets the width (kerning exclusive) of the given text, scaled to the given font size.

    @param text Text whose width has to be calculated.
    @param size Font size.
  */
  public final double getWidth(
    String text,
    double size
    )
  {return getWidth(text) * getScalingFactor(size);}

  /**
    Gets whether the font encoding is custom (that is non-Unicode).
  */
  public boolean isSymbolic(
    )
  {return symbolic;}
  // </public>

  // <protected>
  /**
    Gets the font descriptor.
  */
  protected abstract PdfDictionary getDescriptor(
    );

  /**
    Loads font information from existing PDF font structure.
  */
  protected void load(
    )
  {
    if(getBaseDataObject().containsKey(PdfName.ToUnicode)) // To-Unicode explicit mapping.
    {
      PdfStream toUnicodeStream = (PdfStream)getBaseDataObject().resolve(PdfName.ToUnicode);
      CMapParser parser = new CMapParser(toUnicodeStream.getBody());
      codes = new BiMap<ByteArray,Integer>(parser.parse());
      symbolic = false;
    }

    onLoad();

    // Maximum character code length.
    for(ByteArray charCode : codes.keySet())
    {
      if(charCode.data.length > charCodeMaxLength)
      {charCodeMaxLength = charCode.data.length;}
    }
  }

  /**
    Notifies the loading of font information from an existing PDF font structure.
   */
  protected abstract void onLoad(
    );
  // </protected>

  // <private>
  private void initialize(
    )
  {
    // Put the newly instantiated font into the common cache!
    /*
      NOTE: Font structures are reified as complex objects, both IO- and CPU-intensive to load.
      So, it's convenient to put them into a common cache for later reuse.
    */
    getDocument().cache.put((PdfReference)getBaseObject(),this);
  }
  // </private>
  // </interface>
  // </dynamic>
  // </class>
}