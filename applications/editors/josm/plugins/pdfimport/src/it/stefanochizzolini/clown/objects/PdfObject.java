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

import it.stefanochizzolini.clown.bytes.IOutputStream;
import it.stefanochizzolini.clown.files.File;

/**
  Abstract PDF object.

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.6
*/
public abstract class PdfObject
  implements Cloneable
{
  // <class>
  // <dynamic>
  // <constructors>
  public PdfObject(
    )
  {}
  // </constructors>

  // <interface>
  // <public>
  /**
    Gets the clone of the object, registered inside the given file context.
  */
  public abstract Object clone(
    File context
    );

  /**
    Serializes the object to the given stream.
  */
  public abstract void writeTo(
    IOutputStream stream
    );
  // </public>

  // <protected>
  @Override
  protected Object clone(
    )
  {
    try
    {return super.clone();}
    catch(CloneNotSupportedException e)
    {throw new RuntimeException("Unable to clone.",e);}
  }
  // </protected>
  // </interface>
  // </dynamic>
  // </class>
}