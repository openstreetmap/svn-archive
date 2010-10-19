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

package it.stefanochizzolini.clown.documents.interaction.actions;

import it.stefanochizzolini.clown.bytes.IBuffer;
import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.objects.PdfDataObject;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfStream;
import it.stefanochizzolini.clown.objects.PdfTextString;
import it.stefanochizzolini.clown.util.NotImplementedException;

/**
  'Cause a script to be compiled and executed by the JavaScript interpreter' action [PDF:1.6:8.6.4].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.7
*/
public class JavaScript
  extends Action
{
  // <class>
  // <dynamic>
  // <constructors>
  /**
    Creates a new action within the given document context.
  */
  public JavaScript(
    Document context,
    String script
    )
  {
    super(
      context,
      PdfName.JavaScript
      );

    getBaseDataObject().put(PdfName.JS,new PdfTextString(script));
  }

  JavaScript(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {
    super(
      baseObject,
      container
      );
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public JavaScript clone(
    Document context
    )
  {throw new NotImplementedException();}

  public String getScript(
    )
  {
    /*
      NOTE: 'JS' entry MUST be defined.
    */
    PdfDataObject scriptObject = getBaseDataObject().get(PdfName.JS);
    if(scriptObject instanceof PdfTextString)
    {return (String)((PdfTextString)scriptObject).getValue();}
    else
    {
      IBuffer scriptBuffer = ((PdfStream)scriptObject).getBody();
      return scriptBuffer.getString(0,(int)scriptBuffer.getLength());
    }
  }

  /**
    @see #getScript()
  */
  public void setScript(
    String value
    )
  {
    /*
      NOTE: 'JS' entry MUST be defined.
    */
    PdfDataObject scriptObject = getBaseDataObject().get(PdfName.JS);
    if(scriptObject instanceof PdfTextString)
    {((PdfTextString)scriptObject).setValue(value);}
    else
    {
      IBuffer scriptBuffer = ((PdfStream)scriptObject).getBody();
      scriptBuffer.setLength(0);
      scriptBuffer.append(value);
    }
  }
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}