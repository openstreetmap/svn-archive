/*
  Copyright 2010 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

import it.stefanochizzolini.clown.bytes.Buffer;
import it.stefanochizzolini.clown.bytes.IInputStream;
import it.stefanochizzolini.clown.objects.PdfDataObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfStream;
import it.stefanochizzolini.clown.util.ByteArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

/**
  Character map [PDF:1.6:5.6.4].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.8
  @version 0.0.8
*/
final class CMap
{
  // <static>
  // <interface>
  /**
    Gets the character map extracted from the given data.

    @param stream Character map data.
  */
  public static Map<ByteArray,Integer> get(
    IInputStream stream
    )
  {
    CMapParser parser = new CMapParser(stream);
    return parser.parse();
  }

  /**
    Gets the character map extracted from the given encoding object.
  
    @param encodingObject Encoding object.
  */
  public static Map<ByteArray,Integer> get(
    PdfDataObject encodingObject
    )
  {
    if(encodingObject == null)
      return null;
    
    if(encodingObject instanceof PdfName) // Predefined CMap.
      return get((PdfName)encodingObject);
    else if(encodingObject instanceof PdfStream) // Embedded CMap file.
      return get((PdfStream)encodingObject);
    else
      throw new UnsupportedOperationException("Unknown encoding object type: " + encodingObject.getClass().getSimpleName());
  }

  /**
    Gets the character map extracted from the given data.

    @param stream Character map data.
  */
  public static Map<ByteArray,Integer> get(
    PdfStream stream
    )
  {return get(stream.getBody());}

  /**
    Gets the character map corresponding to the given name.

    @param name Predefined character map name.
    @return null, in case no name matching occurs.
  */
  public static Map<ByteArray,Integer> get(
    PdfName name
    )
  {return get((String)name.getValue());}

  /**
    Gets the character map corresponding to the given name.

    @param name Predefined character map name.
    @return null, in case no name matching occurs.
  */
  public static Map<ByteArray,Integer> get(
    String name
    )
  {
    Map<ByteArray,Integer> cmap = null;
    {
      BufferedReader cmapStream = null;
      try
      {
        cmapStream = new BufferedReader(
          new InputStreamReader(
            CMap.class.getResourceAsStream("/fonts/cmap/" + name)
            )
          );
        cmap = get(new Buffer(cmapStream));
      }
      catch(Exception e)
      { /* Ignore. */ }
      finally
      {
        try
        {
          if(cmapStream != null)
          {cmapStream.close();}
        }
        catch(Exception e)
        { /* Ignore. */ }
      }
    }
    return cmap;
  }
  // </interface>
  // </static>

  // <constructors>
  private CMap(
    )
  {}
  // </constructors>
}