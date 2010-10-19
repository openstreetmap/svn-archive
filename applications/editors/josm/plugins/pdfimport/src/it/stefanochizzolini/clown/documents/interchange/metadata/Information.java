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

package it.stefanochizzolini.clown.documents.interchange.metadata;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.objects.PdfAtomicObject;
import it.stefanochizzolini.clown.objects.PdfDate;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfTextString;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.util.Date;

/**
  Document information [PDF:1.6:10.2.1].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.6
*/
public class Information
  extends PdfObjectWrapper<PdfDictionary>
{
  // <class>
  // <dynamic>
  // <constructors>
  public Information(
    Document context
    )
  {
    super(
      context.getFile(),
      new PdfDictionary()
      );

    try
    {
      Package package_ = getClass().getPackage();
      setProducer(
        package_.getSpecificationTitle() + " "
          + package_.getSpecificationVersion()
        );
    }
    catch(Exception e)
    {/* NOOP (noncritical exception). */}
  }

  /**
    For internal use only.
  */
  public Information(
    PdfDirectObject baseObject
    )
  {
    super(
      baseObject,
      null // NO container (baseObject MUST be an indirect object [PDF:1.6:3.4.4]).
      );
  }
  // </constructors>

  // <interface>
  // <public>
  public String getAuthor(
    )
  {return (String)this.<PdfTextString>getEntry(PdfName.Author);}

  @Override
  public Information clone(
    Document context
    )
  {throw new NotImplementedException();}

  public Date getCreationDate(
    )
  {return (Date)this.<PdfDate>getEntry(PdfName.CreationDate);}

  public String getCreator(
    )
  {return (String)this.<PdfTextString>getEntry(PdfName.Creator);}

  public String getKeywords(
    )
  {return (String)this.<PdfTextString>getEntry(PdfName.Keywords);}

  public Date getModificationDate(
    )
  {return (Date)this.<PdfDate>getEntry(PdfName.ModDate);}

  public String getProducer(
    )
  {return (String)this.<PdfTextString>getEntry(PdfName.Producer);}

  public String getSubject(
    )
  {return (String)this.<PdfTextString>getEntry(PdfName.Subject);}

  public String getTitle(
    )
  {return (String)this.<PdfTextString>getEntry(PdfName.Title);}

  public void setAuthor(
    String value
    )
  {this.<PdfTextString>setEntry(PdfName.Author,value,PdfTextString.class);}

  public void setCreationDate(
    Date value
    )
  {this.<PdfDate>setEntry(PdfName.CreationDate,value,PdfDate.class);}

  public void setCreator(
    String value
    )
  {this.<PdfTextString>setEntry(PdfName.Creator,value,PdfTextString.class);}

  public void setKeywords(
    String value
    )
  {this.<PdfTextString>setEntry(PdfName.Keywords,value,PdfTextString.class);}

  public void setModificationDate(
    Date value
    )
  {this.<PdfDate>setEntry(PdfName.ModDate,value,PdfDate.class);}

  public void setProducer(
    String value
    )
  {this.<PdfTextString>setEntry(PdfName.Producer,value,PdfTextString.class);}

  public void setSubject(
    String value
    )
  {this.<PdfTextString>setEntry(PdfName.Subject,value,PdfTextString.class);}

  public void setTitle(
    String value
    )
  {this.<PdfTextString>setEntry(PdfName.Title,value,PdfTextString.class);}
  // </public>

  // <protected>
  @SuppressWarnings("unchecked")
  protected <TPdf extends PdfAtomicObject<?>> Object getEntry(
    PdfName key
    )
  {
    TPdf entry = (TPdf)File.resolve(getBaseDataObject().get(key));
    if(entry == null)
      return null;

    return entry.getValue();
  }

  @SuppressWarnings("unchecked")
  protected <TPdf extends PdfAtomicObject<?>> void setEntry(
    PdfName key,
    Object value,
    Class<TPdf> entryType // This Class<TPdf> parameter is an ugly workaround to the horrific generics type erasure that precludes full reflection over parameterized types.
    )
  {
    TPdf entry = (TPdf)File.resolve(getBaseDataObject().get(key));
    if(entry == null)
    {
      try
      {
        getBaseDataObject().put(
          key,
          entry = entryType.newInstance()
          );
      }
      catch(Exception e)
      {
        // Propagate the exception!
        throw new RuntimeException(e);
      }
    }

    entry.setValue(value);
  }
  // </protected>
  // </interface>
  // </class>
}