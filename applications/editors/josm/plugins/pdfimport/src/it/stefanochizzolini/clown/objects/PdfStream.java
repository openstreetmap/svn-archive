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

package it.stefanochizzolini.clown.objects;

import it.stefanochizzolini.clown.bytes.Buffer;
import it.stefanochizzolini.clown.bytes.IBuffer;
import it.stefanochizzolini.clown.bytes.IOutputStream;
import it.stefanochizzolini.clown.bytes.filters.Filter;
import it.stefanochizzolini.clown.files.File;
import java.util.Iterator;

/**
  PDF stream object [PDF:1.6:3.2.7].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.6
*/
public class PdfStream
  extends PdfDataObject
{
  // <class>
  // <dynamic>
  // <fields>
  private IBuffer body;
  private PdfDictionary header;
  // </fields>

  // <constructors>
  public PdfStream(
    )
  {
    this(
      new PdfDictionary(),
      new Buffer()
      );
  }

  public PdfStream(
    PdfDictionary header
    )
  {
    this(
      header,
      new Buffer()
      );
  }

  public PdfStream(
    IBuffer body
    )
  {
    this(
      new PdfDictionary(),
      body
      );
  }

  public PdfStream(
    PdfDictionary header,
    IBuffer body
    )
  {
    this.header = header;
    this.body = body;
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public Object clone(
    File context
    )
  {
    PdfStream clone = new PdfStream(
      (PdfDictionary)header.clone(context),
      body.clone()
      );

    return clone;
  }

  /**
    Gets the decoded stream body.
  */
  public IBuffer getBody(
    )
  {
    /*
      NOTE: Encoding filters are removed by default because they belong to a lower layer
      (token layer), so that it's appropriate and consistent to transparently keep the object layer
      unaware of such a facility.
    */
    return getBody(true);
  }

  /**
    Gets the stream body.
    @param decode Defines whether the body has to be decoded.
  */
  public IBuffer getBody(
    boolean decode
    )
  {
    if(decode)
    {
      // Get 'Filter' entry!
      /*
        NOTE: Such an entry defines possible encodings applied to the stream.
      */
      PdfDirectObject filterObj = header.get(PdfName.Filter);
      // Is the stream encoded?
      if(filterObj != null)
      {
        /*
          NOTE: If the stream is encoded, we must decode it before continuing.
        */
        // TODO:IMPL 'DecodeParms' entry management!!!
        PdfDataObject filterDataObj = File.resolve(filterObj);
        if(filterDataObj instanceof PdfName) // PdfName.
        {body.decode(Filter.get((PdfName)filterDataObj));}
        else // MUST be PdfArray.
        {
          // [FIX:0.0.5:1:SC] It failed to deal with filter chains.
          Iterator<PdfDirectObject> filterObjIterator = ((PdfArray)filterDataObj).iterator();
          while(filterObjIterator.hasNext())
          {body.decode(Filter.get((PdfName)File.resolve(filterObjIterator.next())));}
        }

        // Update 'Filter' entry!
        header.put(PdfName.Filter,null); // The stream is free from encodings.
      }
    }

    return body;
  }

  public PdfDictionary getHeader(
    )
  {return header;}

  @Override
  public void writeTo(
    IOutputStream stream
    )
  {
    boolean unencodedBody;
    byte[] bodyData;
    int bodyLength;

    // 1. Header.
    // Encoding.
    /*
      NOTE: As the contract establishes that a stream instance should be kept
      free from encodings in order to be editable, encoding is NOT applied to
      the actual online stream, but to its serialized representation only.
      That is, as encoding is just a serialization practise, it is excluded from
      alive, instanced streams.
    */
    PdfDirectObject filterObj = header.get(PdfName.Filter);
    // Is the body free from encodings?
    if(filterObj == null) // Unencoded body.
    {
      /*
        NOTE: As online representation is unencoded,
        header entries related to the encoded stream body are temporary
        (instrumental to the current serialization process).
      */
      unencodedBody = true;
    
      // Set the filter to apply!
      filterObj = PdfName.FlateDecode; // zlib/deflate filter.
      // Get encoded body data applying the filter to the stream!
      bodyData = body.encode(Filter.get((PdfName)filterObj));
      // Set encoded length!
      bodyLength = bodyData.length;
      // Update 'Filter' entry!
      header.put(PdfName.Filter,filterObj);
    }
    else // Encoded body.
    {
      unencodedBody = false;

      // Get encoded body data!
      bodyData = body.toByteArray();
      // Set encoded length!
      bodyLength = (int)body.getLength();
    }
    // Set encoded length!
    header.put(PdfName.Length,new PdfInteger(bodyLength));

    header.writeTo(stream);

    // Is the body free from encodings?
    if(unencodedBody)
    {
      // Restore actual header entries!
      ((PdfInteger)header.get(PdfName.Length)).setValue((int)body.getLength());
      header.put(PdfName.Filter,null);
    }

    // 2. Body.
    // [FIX:1909707:AH] Invalid end-of-line marker for stream objects.
    stream.write("\nstream\n");
    stream.write(bodyData);
    stream.write("\nendstream");
  }
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}