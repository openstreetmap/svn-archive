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

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.objects.IPdfNumber;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfInteger;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.util.ByteArray;

import java.util.Hashtable;
import java.util.Map;

/**
  Simple font [PDF:1.6:5.5].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.8
  @version 0.0.8
*/
public abstract class SimpleFont
  extends Font
{
  // <constructors>
  protected SimpleFont(
    Document context
    )
  {super(context);}

  protected SimpleFont(
    PdfDirectObject baseObject
    )
  {super(baseObject);}
  // </constructors>

  @Override
  protected PdfDictionary getDescriptor(
    )
  {return (PdfDictionary)getBaseDataObject().resolve(PdfName.FontDescriptor);}

  protected abstract void loadEncoding();
  
  /**
    Loads the encoding differences into the given collection.
    
    @param encodingDictionary Encoding dictionary.
    @param codes Encoding to alter applying differences.
   */
  protected void loadEncodingDifferences(
    PdfDictionary encodingDictionary,
    Map<ByteArray,Integer> codes
    )
  {
    PdfArray differenceObjects = (PdfArray)encodingDictionary.resolve(PdfName.Differences);
    if(differenceObjects == null)
      return;
  
    /*
      NOTE: Each code is the first index in a sequence of character codes to be changed.
      The first character name after the code becomes the name corresponding to that code.
      Subsequent names replace consecutive code indices until the next code appears
      in the array or the array ends.
    */
    byte[] charCodeData = new byte[1];
    for(PdfDirectObject differenceObject : differenceObjects)
    {
      if(differenceObject instanceof PdfInteger)
      {charCodeData[0] = (byte)(((Integer)((PdfInteger)differenceObject).getValue()) & 0xFF);}
      else // NOTE: MUST be PdfName.
      {
        ByteArray charCode = new ByteArray(charCodeData);
        String charName = (String)((PdfName)differenceObject).getValue();
        if(charName.equals(".notdef"))
        {codes.remove(charCode);}
        else
        {
          try
          {codes.put(charCode,GlyphMapping.nameToCode(charName));}
          catch (Exception e)
          {codes.put(charCode,(int)charCodeData[0]);} // NOTE: This is an extreme remedy to non-standard character name lookups.
        }
        charCodeData[0]++;
      }
    }
  }

  @Override
  protected void onLoad(
    )
  {
    loadEncoding();

    // Glyph widths.
    if(glyphWidths == null)
    {
      glyphWidths = new Hashtable<Integer,Integer>();
      PdfArray glyphWidthObjects = (PdfArray)getBaseDataObject().resolve(PdfName.Widths);
      if(glyphWidthObjects != null)
      {
        ByteArray charCode = new ByteArray(
          new byte[]
          {(byte)(int)((PdfInteger)getBaseDataObject().get(PdfName.FirstChar)).getRawValue()}
          );
        for(PdfDirectObject glyphWidthObject : glyphWidthObjects)
        {
          int glyphWidth = ((PdfInteger)glyphWidthObject).getRawValue();
          if(glyphWidth > 0)
          {
            Integer code = codes.get(charCode);
            if(code != null)
            {
              glyphWidths.put(
                glyphIndexes.get(code),
                glyphWidth
                );
            }
          }
          charCode.data[0]++;
        }
      }
    }
    // Default glyph width.
    {
      PdfDictionary descriptor = getDescriptor();
      if(descriptor != null)
      {
        IPdfNumber defaultGlyphWidthObject = (IPdfNumber)descriptor.get(PdfName.MissingWidth);
        defaultGlyphWidth = (defaultGlyphWidthObject == null ? 0 : (int)Math.round(defaultGlyphWidthObject.getNumberValue()));
      }
    }
  }
}