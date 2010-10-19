/*
  Copyright 2008-2010 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.documents.contents.objects;

import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfName;

/**
  'Begin marked-content sequence' operation [PDF:1.6:10.5].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.7
  @version 0.0.8
*/
public final class BeginMarkedContent
  extends Operation
{
  // <class>
  // <static>
  // <fields>
  public static final String PropertyListOperator = "BDC";
  public static final String SimpleOperator = "BMC";
  // </fields>
  // </static>

  // <dynamic>
  // <constructors>
  public BeginMarkedContent(
    PdfName tag
    )
  {
    super(
      SimpleOperator,
      tag
      );
  }

  public BeginMarkedContent(
    PdfName tag,
    PdfDirectObject properties
    )
  {
    super(
      PropertyListOperator,
      tag, properties
      );
  }
  // </constructors>

  // <interface>
  // <public>
  /**
    Gets the private information meaningful to the program (application or plugin extension)
    creating the marked content.
  */
  public PdfDirectObject getProperties(
    )
  {return operands.get(1);}

  /**
    Gets the role or significance of the marked content.
  */
  public PdfName getTag(
    )
  {return (PdfName)operands.get(0);}

  public void setProperties(
    PdfDirectObject value
    )
  {
    if(value == null)
    {
      operator = SimpleOperator;
      if(operands.size() > 1)
      {operands.remove(1);}
    }
    else
    {
      operator = PropertyListOperator;
      if(operands.size() > 1)
      {operands.set(1,value);}
      else
      {operands.add(value);}
    }
  }

  public void setTag(
    PdfName value
    )
  {operands.set(0,value);}
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}