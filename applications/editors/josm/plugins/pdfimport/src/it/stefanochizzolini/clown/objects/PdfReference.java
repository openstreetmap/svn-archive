/*
  Copyright 2006-2009 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

import it.stefanochizzolini.clown.bytes.IOutputStream;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.tokens.Parser;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.text.DecimalFormat;

/**
  PDF indirect reference object [PDF:1.6:3.2.9].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
*/
public class PdfReference
  extends PdfDirectObject
  implements IPdfIndirectObject
{
  // <class>
  // <static>
  // <fields>
  private static final DecimalFormat xRefGenerationFormatter;
  private static final DecimalFormat xRefOffsetFormatter;
  // </fields>

  // <constructors>
  static
  {
    xRefGenerationFormatter = new DecimalFormat("00000");
    xRefOffsetFormatter = new DecimalFormat("0000000000");
  }
  // </constructors>
  // </static>

  // <dynamic>
  // <fields>
  private PdfIndirectObject indirectObject;

  private int generationNumber;
  private int objectNumber;

  private File file;
  // </fields>

  // <constructors>
  PdfReference(
    PdfIndirectObject indirectObject,
    int objectNumber,
    int generationNumber
    )
  {
    this.indirectObject = indirectObject;

    this.objectNumber = objectNumber;
    this.generationNumber = generationNumber;
  }

  /**
    For internal use only.

    <p>This is a necessary hack because indirect objects are unreachable on parsing bootstrap
    (see File(IInputStream) constructor).</p>
  */
  public PdfReference(
    Parser.Reference reference,
    File file
    )
  {
    this.objectNumber = reference.getObjectNumber();
    this.generationNumber = reference.getGenerationNumber();

    this.file = file;
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public int compareTo(
    PdfDirectObject object
    )
  {throw new NotImplementedException();}

  @Override
  public boolean equals(
    Object object
    )
  {
    if(!(object instanceof PdfReference))
      return false;

    return (getID().equals(((PdfReference)object).getID()));
  }

  public String getCrossReference(
    long offset
    )
  {
    return (xRefOffsetFormatter.format(offset) + " "
        + xRefGenerationFormatter.format(generationNumber) + " "
        + getIndirectObject().getUsage()
        + "\r\n");
  }

  public int getGenerationNumber(
    )
  {return generationNumber;}

  public String getID(
    )
  {return (objectNumber + " " + generationNumber);}

  public String getIndirectReference(
    )
  {return (getID() + " R");}

  public int getObjectNumber(
    )
  {return objectNumber;}

  @Override
  public int hashCode(
    )
  {return getIndirectObject().hashCode();}

  @Override
  public String toString(
    )
  {return getIndirectReference();}

  @Override
  public void writeTo(
    IOutputStream stream
    )
  {stream.write(getIndirectReference());}

  // <IPdfIndirectObject>
  @Override
  public Object clone(
    File context
    )
  {return ((PdfIndirectObject)getIndirectObject().clone(context)).getReference();}

  public void delete(
    )
  {getIndirectObject().delete();}

  public PdfDataObject getDataObject(
    )
  {return getIndirectObject().getDataObject();}

  public PdfIndirectObject getIndirectObject(
    )
  {
    if(indirectObject == null)
    {indirectObject = file.getIndirectObjects().get(objectNumber);}

    return indirectObject;
  }

  public PdfReference getReference(
    )
  {return this;}

  public void setDataObject(
    PdfDataObject value
    )
  {getIndirectObject().setDataObject(value);}
  // </IPdfIndirectObject>
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}