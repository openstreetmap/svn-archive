/*
  Copyright 2008-2009 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfNamedObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfReference;
import it.stefanochizzolini.clown.objects.PdfString;
import it.stefanochizzolini.clown.objects.PdfTextString;
import it.stefanochizzolini.clown.util.NotImplementedException;

/**
  Reference to the contents of another file (file specification) [PDF:1.6:3.10.2].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.7
  @version 0.0.8
*/
public class FileSpec
  extends PdfNamedObjectWrapper<PdfDictionary>
{
  // <class>
  // <dynamic>
  // <constructors>
  public FileSpec(
    Document context
    )
  {
    super(
      context.getFile(),
      new PdfDictionary(
        new PdfName[]
        {PdfName.Type},
        new PdfDirectObject[]
        {PdfName.Filespec}
        )
      );
  }

  public FileSpec(
    EmbeddedFile embeddedFile,
    String filename
    )
  {
    this(embeddedFile.getDocument());

    setFilename(filename);
    setEmbeddedFile(embeddedFile);
  }

  public FileSpec(
    PdfDirectObject baseObject,
    PdfIndirectObject container,
    PdfString name
    )
  {super(baseObject,container,name);}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public FileSpec clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the related files.
  */
  public RelatedFiles getDependencies(
    )
  {return getDependencies(PdfName.F);}

  /**
    Gets the description of the file.
  */
  public String getDescription(
    )
  {
    /*
      NOTE: 'Desc' entry may be undefined.
    */
    PdfTextString descriptionObject = (PdfTextString)getBaseDataObject().get(PdfName.Desc);
    if(descriptionObject == null)
      return null;

    return (String)descriptionObject.getValue();
  }

  /**
    Gets the embedded file.
  */
  public EmbeddedFile getEmbeddedFile(
    )
  {return getEmbeddedFile(PdfName.F);}

  /**
    Gets the Mac OS-specific related files.
  */
  public RelatedFiles getMacDependencies(
    )
  {return getDependencies(PdfName.Mac);}

  /**
    Gets the Mac OS-specific embedded file.
  */
  public EmbeddedFile getMacEmbeddedFile(
    )
  {return getEmbeddedFile(PdfName.Mac);}

  /**
    Gets the Mac OS-specific file name.
  */
  public String getMacFilename(
    )
  {return getFilename(PdfName.Mac);}

  /**
    Gets the file name.
  */
  public String getFilename(
    )
  {return getFilename(PdfName.F);}

  /**
    Gets the Unix-specific related files.
  */
  public RelatedFiles getUnixDependencies(
    )
  {return getDependencies(PdfName.Unix);}

  /**
    Gets the Unix-specific embedded file.
  */
  public EmbeddedFile getUnixEmbeddedFile(
    )
  {return getEmbeddedFile(PdfName.Unix);}

  /**
    Gets the Unix-specific file name.
  */
  public String getUnixFilename(
    )
  {return getFilename(PdfName.Unix);}

  /**
    Gets the Windows-specific related files.
  */
  public RelatedFiles getWinDependencies(
    )
  {return getDependencies(PdfName.DOS);}

  /**
    Gets the Windows-specific embedded file.
  */
  public EmbeddedFile getWinEmbeddedFile(
    )
  {return getEmbeddedFile(PdfName.DOS);}

  /**
    Gets the Windows-specific file name.
  */
  public String getWinFilename(
    )
  {return getFilename(PdfName.DOS);}

  /**
    @see #getDescription()
  */
  public void setDescription(
    String value
    )
  {getBaseDataObject().put(PdfName.Desc, new PdfTextString(value));}

  /**
    @see #getDependencies()
  */
  public void setDependencies(
    RelatedFiles value
    )
  {setDependencies(PdfName.F,value);}

  /**
    @see #getEmbeddedFile()
  */
  public void setEmbeddedFile(
    EmbeddedFile value
    )
  {setEmbeddedFile(PdfName.F,value);}

  /**
    @see #getMacDependencies()
  */
  public void setMacDependencies(
    RelatedFiles value
    )
  {setDependencies(PdfName.Mac,value);}

  /**
    @see #getMacEmbeddedFile()
  */
  public void setMacEmbeddedFile(
    EmbeddedFile value
    )
  {setEmbeddedFile(PdfName.Mac,value);}

  /**
    @see #getMacFilename()
  */
  public void setMacFilename(
    String value
    )
  {setFilename(PdfName.Mac,value);}

  /**
    @see #getFilename()
  */
  public void setFilename(
    String value
    )
  {setFilename(PdfName.F,value);}

  /**
    @see #getUnixDependencies()
  */
  public void setUnixDependencies(
    RelatedFiles value
    )
  {setDependencies(PdfName.Unix,value);}

  /**
    @see #getUnixEmbeddedFile()
  */
  public void setUnixEmbeddedFile(
    EmbeddedFile value
    )
  {setEmbeddedFile(PdfName.Unix,value);}

  /**
    @see #getUnixFilename()
  */
  public void setUnixFilename(
    String value
    )
  {setFilename(PdfName.Unix,value);}

  /**
    @see #getWinDependencies()
  */
  public void setWinDependencies(
    RelatedFiles value
    )
  {setDependencies(PdfName.DOS,value);}

  /**
    @see #getWinEmbeddedFile()
  */
  public void setWinEmbeddedFile(
    EmbeddedFile value
    )
  {setEmbeddedFile(PdfName.DOS,value);}

  /**
    @see #getWinFilename()
  */
  public void setWinFilename(
    String value
    )
  {setFilename(PdfName.DOS,value);}
  // </public>

  // <private>
  /**
    Gets the related files associated to the given key.
  */
  private RelatedFiles getDependencies(
    PdfName key
    )
  {
    /*
      NOTE: 'RF' entry may be undefined.
    */
    PdfDictionary dependenciesObject = (PdfDictionary)getBaseDataObject().get(PdfName.RF);
    if(dependenciesObject == null)
      return null;

    /*
      NOTE: key entry may be undefined.
    */
    PdfReference dependencyFilesObject = (PdfReference)dependenciesObject.get(key);
    if(dependencyFilesObject == null)
      return null;

    return new RelatedFiles(dependencyFilesObject,getContainer());
  }

  /**
    Gets the embedded file associated to the given key.
  */
  private EmbeddedFile getEmbeddedFile(
    PdfName key
    )
  {
    /*
      NOTE: 'EF' entry may be undefined.
    */
    PdfDictionary embeddedFilesObject = (PdfDictionary)getBaseDataObject().get(PdfName.EF);
    if(embeddedFilesObject == null)
      return null;

    /*
      NOTE: key entry may be undefined.
    */
    PdfReference embeddedFileObject = (PdfReference)embeddedFilesObject.get(key);
    if(embeddedFileObject == null)
      return null;

    return new EmbeddedFile(embeddedFileObject);
  }

  /**
    Gets the file name associated to the given key.
  */
  private String getFilename(
    PdfName key
    )
  {
    /*
      NOTE: key entry may be undefined.
    */
    PdfString nameObject = (PdfString)getBaseDataObject().get(key);
    if(nameObject == null)
      return null;

    return (String)nameObject.getValue();
  }

  /**
    @see #getDependencies(PdfName)
  */
  private void setDependencies(
    PdfName key,
    RelatedFiles value
    )
  {
    /*
      NOTE: 'RF' entry may be undefined.
    */
    PdfDictionary dependenciesObject = (PdfDictionary)getBaseDataObject().get(PdfName.RF);
    if(dependenciesObject == null)
    {
      dependenciesObject = new PdfDictionary();
      getBaseDataObject().put(PdfName.RF,dependenciesObject);
    }

    dependenciesObject.put(key,value.getBaseObject());
  }

  /**
    @see #getEmbeddedFile(PdfName)
  */
  private void setEmbeddedFile(
    PdfName key,
    EmbeddedFile value
    )
  {
    /*
      NOTE: 'EF' entry may be undefined.
    */
    PdfDictionary embeddedFilesObject = (PdfDictionary)getBaseDataObject().get(PdfName.EF);
    if(embeddedFilesObject == null)
    {
      embeddedFilesObject = new PdfDictionary();
      getBaseDataObject().put(PdfName.EF,embeddedFilesObject);
    }

    embeddedFilesObject.put(key,value.getBaseObject());
  }

  /**
    @see #getFilename(PdfName)
  */
  private void setFilename(
    PdfName key,
    String value
    )
  {getBaseDataObject().put(key, new PdfString(value));}
  // <private>
  // </interface>
  // </dynamic>
  // </class>
}