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
import it.stefanochizzolini.clown.tokens.Parser;
import it.stefanochizzolini.clown.tokens.XRefEntry;

/**
  PDF indirect object [PDF:1.6:3.2.9].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.7
*/
public class PdfIndirectObject
  extends PdfObject
  implements IPdfIndirectObject
{
  // <class>
  // <static>
  // <fields>
  private static final String UsageFree = "f";
  private static final String UsageInUse = "n";
  // </fields>
  // </static>

  // <dynamic>
  // <fields>
  private PdfDataObject dataObject;
  private File file;
  private boolean original;
  private PdfReference reference;
  private XRefEntry xrefEntry;
  // </fields>

  // <constructors>
  /**
    For internal use only.

    @param file Associated file.
    @param dataObject Data object associated to the indirect object.
      It MUST be null if the indirect object is original (i.e. coming from an existing file) or
      free.
      It MUST be NOT null if the indirect object is new and in-use.
    @param xrefEntry Cross-reference entry associated to the indirect object.
      If the indirect object is new, its offset field MUST be set to 0 (zero).
  */
  public PdfIndirectObject(
    File file,
    PdfDataObject dataObject,
    XRefEntry xrefEntry
    )
  {
    this.file = file;
    this.dataObject = dataObject;
    this.xrefEntry = xrefEntry;

    this.original = (xrefEntry.getOffset() != 0);
    this.reference = new PdfReference(
      this,
      xrefEntry.getNumber(),
      xrefEntry.getGeneration()
      );
  }
  // </constructors>

  // <interface>
  // <public>
  public File getFile(
    )
  {return file;}

  @Override
  public int hashCode(
    )
  {
    /*
      NOTE: Uniqueness should be achieved XORring the (local) reference hashcode
      with the (global) file hashcode.
      NOTE: DO NOT directly invoke reference.hashCode() method here as
      it would trigger an infinite loop, as it conversely relies on this method.
    */
    return reference.getID().hashCode() ^ file.hashCode();
  }

  public boolean isInUse(
    )
  {return (xrefEntry.getUsage() == XRefEntry.UsageEnum.InUse);}

  public boolean isOriginal(
    )
  {return original;}

  public void update(
    )
  {
    if(original)
    {
      /*
        NOTE: It's expected that dropOriginal() is invoked by IndirectObjects set() method;
        such an action is delegated because clients may invoke directly set() method, skipping
        this method.
      */
      file.getIndirectObjects().update(this);
    }
  }

  @Override
  public void writeTo(
    IOutputStream stream
    )
  {
    // Header.
    stream.write(reference.getID() + " obj\n");

    // Body.
    getDataObject().writeTo(stream);

    // Tail.
    stream.write("\nendobj\n");
  }

  // <IPdfIndirectObject>
  @Override
  public Object clone(
    File context
    )
  {return context.getIndirectObjects().addExternal(this);}

  public void delete(
    )
  {
    if(file != null)
    {
      /*
        NOTE: It's expected that dropFile() is invoked by IndirectObjects remove() method;
        such an action is delegated because clients may invoke directly remove() method,
        skipping this method.
      */
      file.getIndirectObjects().remove(xrefEntry.getNumber());
    }
  }

  public PdfDataObject getDataObject(
    )
  {
    if(dataObject == null)
    {
      /*
        NOTE: indirect data object is null in 2 cases:
        1) when the entry is free (no data object at all);
        2) when the indirect object hasn't been initialized yet
          because it comes from a parsed reference (late-bound data object).
        In case 1 data object MUST keep itself null,
        while in case 2 data object MUST be initialized.
      */

      // Is the entry free [case 1]?
      // NOTE: Free entries have NO indirect data object associated with.
      if(xrefEntry.getUsage() == XRefEntry.UsageEnum.Free)
        return null;

      // In-use entry (late-bound data object [case 2]).
      try
      {
        Parser parser = file.getReader().getParser();
        // Retrieve the associated data object among the original objects!
        parser.seek(xrefEntry.getOffset());
        // Skip indirect-object header!
        parser.moveNext(4);
        // Get the indirect data object!
        dataObject = parser.parsePdfObject();
      }
      catch(Exception e)
      {throw new RuntimeException(e);}
    }

    return dataObject;
  }

  public PdfIndirectObject getIndirectObject(
    )
  {return this;}

  public PdfReference getReference(
    )
  {return reference;}

  public void setDataObject(
    PdfDataObject value
    )
  {
    if(xrefEntry.getGeneration() == XRefEntry.GenerationUnreusable)
      throw new RuntimeException("Unreusable entry.");

    dataObject = value;
    xrefEntry.setUsage(XRefEntry.UsageEnum.InUse);
    update();
  }
  // </IPdfIndirectObject>
  // </public>

  // <internal>
  /**
    For internal use only.
  */
  public void dropFile(
    )
  {file = null;}

  /**
    For internal use only.
  */
  public void dropOriginal(
    )
  {original = false;}

  String getUsage(
    )
  {
    switch(xrefEntry.getUsage())
    {
      case Free:
        return UsageFree;
      case InUse:
        return UsageInUse;
      default: // Should NEVER happen.
        throw new RuntimeException("Invalid xref usage value.");
    }
  }
  // </internal>
  // </interface>
  // </dynamic>
  // </class>
}