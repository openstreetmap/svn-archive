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

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.files.File;

/**
  Base high-level representation of a PDF object.
  All specialized objects (e.g. Document, Page, Pages, ContentStream...) inherit from it.
  <h3>Remarks</h3>
  <p>Somebody may wonder why I didn't directly make specialized objects inherit from
  their low-level counterparts (e.g. Page extends PdfDictionary, ContentStream
  extends PdfStream and so on): that could have been much smoother anyway, you argue.
  Yeah, I could agree if there was a plain one-to-one mapping between primitive PDF
  types and specialized instances, but (you know!) reality is not so polished as
  theory: the 'Content' entry of Page dictionaries may be a simple reference to a
  PdfStream or a PdfArray of references to PdfStream-s, Pages collections may be
  spread across a B-tree instead of a flat PdfArray etc. So: <i>in order to hide all
  these annoying inner workings, I chose to adopt a composition pattern instead of
  the apparently-reasonable (but actually awkward!) inheritance pattern</i>.
  Nonetheless, users are always enabled to navigate through the low-level structure
  accessing the {@link #getBaseDataObject()} method.</p>

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.7
*/
public abstract class PdfObjectWrapper<TDataObject extends PdfDataObject>
{
  // <class>
  // <dynamic>
  // <fields>
  private TDataObject baseDataObject;
  private PdfDirectObject baseObject;
  private PdfIndirectObject container;
  // </fields>

  // <constructors>
  protected PdfObjectWrapper(
    File context,
    TDataObject baseDataObject
    )
  {
    this(
      context.register(baseDataObject),
      null
      );
  }

  /**
    @param baseObject Base PDF object. MUST be a {@link PdfReference PdfReference}
    everytime available.
    @param container Indirect object containing the base object.
  */
  protected PdfObjectWrapper(
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {
    setBaseObject(baseObject);
    setContainer(container);
  }
  // </constructors>

  // <interface>
  // <public>
  /**
    Gets a clone of the object, registered inside the given document context.

    @param context Which document the clone has to be registered in.
  */
  public abstract Object clone(
    Document context
    );

  /**
    Removes the object from its document context.
    <h3>Remarks</h3>
    <p>The object is no more usable after this method returns.</p>

    @return Whether the object was actually decontextualized (only indirect objects can be
    decontextualized).
  */
  public boolean delete(
    )
  {
    // Is the object indirect?
    if(baseObject instanceof PdfReference) // Indirect object.
    {
      ((PdfReference)baseObject).delete();
      return true;
    }
    else // Direct object.
    {return false;}
  }

  /**
    Gets the underlying data object.
  */
  public TDataObject getBaseDataObject(
    )
  {return baseDataObject;}

  /**
    Gets the underlying reference object, if available;
    otherwise, behaves like {@link #getBaseDataObject() getBaseDataObject()}.
  */
  public PdfDirectObject getBaseObject(
    )
  {return baseObject;}

  /**
    Gets the indirect object containing the base object.
    <h3>Remarks</h3>
    <p>It's used for update purposes.</p>
  */
  public PdfIndirectObject getContainer(
    )
  {return container;}

  /**
    Gets the document context.
  */
  public Document getDocument(
    )
  {return container.getFile().getDocument();}

  /**
    Gets the file context.
  */
  public File getFile(
    )
  {return container.getFile();}

  /**
    Manually update the underlying indirect object.
  */
  public void update(
    )
  {container.update();}
  // </public>

  // <protected>
  @SuppressWarnings("unchecked")
  protected void setBaseObject(
    PdfDirectObject value
    )
  {
    baseObject = value;
    baseDataObject = (TDataObject)File.resolve(baseObject);
  }
  // </protected>

  // <internal>
  /**
    For internal use only.
  */
  public void setContainer(
    PdfIndirectObject value
    )
  {
    // Is base object indirect (self-contained)?
    if(baseObject instanceof PdfReference) // Base object is indirect (self-contained).
    {container = ((PdfReference)baseObject).getIndirectObject();}
    else // Base object is direct (contained).
    {container = value;}
  }
  // </internal>
  // </interface>
  // </dynamic>
  // </class>
}