/*
  Copyright 2008 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.documents.fileSpecs;

import it.stefanochizzolini.clown.bytes.Buffer;
import it.stefanochizzolini.clown.bytes.FileInputStream;
import it.stefanochizzolini.clown.bytes.IInputStream;
import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfStream;
import it.stefanochizzolini.clown.util.NotImplementedException;

/**
  Embedded file [PDF:1.6:3.10.3].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.7
  @version 0.0.7
*/
public class EmbeddedFile
  extends PdfObjectWrapper<PdfStream>
{
  // <class>
  // <static>
  // <interface>
  // <public>
  public static EmbeddedFile get(
    Document context,
    String path
    )
  {
    try
    {
      return new EmbeddedFile(
        context,
        new FileInputStream(
          new java.io.RandomAccessFile(path,"r")
          )
        );
    }
    catch(Exception e)
    {throw new RuntimeException(e);}
  }

  public static EmbeddedFile get(
    Document context,
    java.io.File file
    )
  {return get(context,file.getPath());}
  // </public>
  // </interface>
  // </static>

  // <dynamic>
  // <constructors>
  /**
    Creates a new embedded file inside the document.
  */
  public EmbeddedFile(
    Document context,
    IInputStream stream
    )
  {
    super(
      context.getFile(),
      new PdfStream(
        new PdfDictionary(
          new PdfName[]{PdfName.Type},
          new PdfDirectObject[]{PdfName.EmbeddedFile}
          ),
        new Buffer(stream.toByteArray())
        )
      );
  }

  /**
    Instantiates an existing embedded file.
  */
  public EmbeddedFile(
    PdfDirectObject baseObject
    )
  {
    super(
      baseObject,
      null // NO container (baseObject is (by definition) a PDF stream, so it MUST be an indirect object [PDF:1.6:3.2.7]).
      );
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public EmbeddedFile clone(
    Document context
    )
  {throw new NotImplementedException();}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}