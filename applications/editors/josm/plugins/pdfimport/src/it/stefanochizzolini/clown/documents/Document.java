/*
  Copyright 2006-2010 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.documents;

import it.stefanochizzolini.clown.documents.contents.Resources;
import it.stefanochizzolini.clown.documents.interaction.forms.Form;
import it.stefanochizzolini.clown.documents.interaction.navigation.document.Bookmarks;
import it.stefanochizzolini.clown.documents.interaction.navigation.document.Destination;
import it.stefanochizzolini.clown.documents.interaction.viewer.ViewerPreferences;
import it.stefanochizzolini.clown.documents.interchange.metadata.Information;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.objects.IPdfNumber;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfReal;
import it.stefanochizzolini.clown.objects.PdfReference;
import it.stefanochizzolini.clown.objects.PdfString;
import it.stefanochizzolini.clown.objects.Rectangle;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.util.ArrayList;
import java.util.Collection;

/**
  PDF document [PDF:1.6:3.6.1].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
*/
public class Document
  extends PdfObjectWrapper<PdfDictionary>
{
  // <class>
  // <static>
  // <interface>
  // <public>
  @SuppressWarnings("unchecked")
  public static <T extends PdfObjectWrapper<?>> T resolve(
    Class<T> type,
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {
    if(Destination.class.isAssignableFrom(type))
      return (T)Destination.wrap(baseObject,container,null);
    throw new UnsupportedOperationException("Type '" + type.getName() + "' wrapping is not supported.");
  }

  /**
    Forces a named base object to be expressed as its corresponding
    high-level representation.
  */
  public static <T extends PdfObjectWrapper<?>> T resolveName(
    Class<T> type,
    PdfDirectObject namedBaseObject,
    PdfIndirectObject container
    )
  {
    if(namedBaseObject instanceof PdfString) // Named destination.
      return container.getFile().getDocument().getNames().resolve(type,(PdfString)namedBaseObject);
    else // Explicit destination.
      return resolve(type,namedBaseObject,container);
  }
  // </public>
  // </interface>
  // </static>

  // <dynamic>
  // <fields>
  /**
    For internal use only.
  */
  public java.util.Hashtable<PdfReference,Object> cache = new java.util.Hashtable<PdfReference,Object>();
  // </fields>

  // <constructors>
  /**
    For internal use only.
  */
  public Document(
    File context
    )
  {
    super(
      context,
      new PdfDictionary(
        new PdfName[]{PdfName.Type},
        new PdfDirectObject[]{PdfName.Catalog}
        ) // Document catalog [PDF:1.6:3.6.1].
      );

    /*
      NOTE: Here is just a minimal initialization.
      Any further customization is upon client's responsibility.
    */
    // Link the document to the file!
    context.getTrailer().put(PdfName.Root,getBaseObject()); // Attaches the catalog reference to the file trailer.

    // Initialize the pages collection (page-tree root node)!
    /*
      NOTE: The page-tree root node is required [PDF:1.6:3.6.1].
    */
    setPages(new Pages(this));

    // Default page size.
    setPageSize(PageFormat.getSize());

    // Default resources collection.
    setResources(new Resources(this));
  }

  /**
    For internal use only.
  */
  public Document(
    PdfDirectObject baseObject // Catalog.
    )
  {
    super(
      baseObject,
      null // NO container (catalog MUST be an indirect object [PDF:1.6:3.4.4]).
      );
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public Document clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Clones the object within this document context.
  */
  public Object contextualize(
    PdfObjectWrapper<?> object
    )
  {
    if(object.getFile() == getFile())
      return object;

    return object.clone(this);
  }

  /**
    Clones the collection's objects within this document context.
  */
  public Collection<? extends PdfObjectWrapper<?>> contextualize(
    Collection<? extends PdfObjectWrapper<?>> objects
    )
  {
    ArrayList<PdfObjectWrapper<?>> contextualizedObjects = new ArrayList<PdfObjectWrapper<?>>(objects.size());
    for(PdfObjectWrapper<?> object : objects)
    {contextualizedObjects.add((PdfObjectWrapper<?>)contextualize(object));}

    return contextualizedObjects;
  }

  /**
    Drops the object from this document context.
  */
  public void decontextualize(
    PdfObjectWrapper<?> object
    )
  {
    if(object.getFile() != getFile())
      return;

    object.delete();
  }

  /**
    Drops the collection's objects from this document context.
  */
  public void decontextualize(
    Collection<? extends PdfObjectWrapper<?>> objects
    )
  {
    for(PdfObjectWrapper<?> object : objects)
    {decontextualize(object);}
  }

  /**
    Gets the document's behavior in response to trigger events.

    @since 0.0.7
  */
  public DocumentActions getActions(
    )
  {
    PdfDirectObject actionsObject = getBaseDataObject().get(PdfName.AA);
    if(actionsObject == null)
      return null;

    return new DocumentActions(actionsObject,getContainer());
  }

  /**
    Gets the bookmark collection [PDF:1.6:8.2.2].
  */
  public Bookmarks getBookmarks(
    )
  {
    PdfDirectObject bookmarksObject = getBaseDataObject().get(PdfName.Outlines);
    if(bookmarksObject == null)
      return null;

    return new Bookmarks(bookmarksObject);
  }

  /**
    Gets the interactive form (AcroForm) [PDF:1.6:8.6.1].

    @since 0.0.7
  */
  public Form getForm(
    )
  {
    PdfDirectObject formObject = getBaseDataObject().get(PdfName.AcroForm);
    if(formObject == null)
      return null;

    return new Form(formObject,getContainer());
  }

  /**
    Gets the document information dictionary [PDF:1.6:10.2.1].
  */
  public Information getInformation(
    )
  {
    PdfDirectObject informationObject = getFile().getTrailer().get(PdfName.Info);
    if(informationObject == null)
      return null;

    return new Information(informationObject);
  }

  /**
    Gets the name dictionary [PDF:1.6:3.6.3].
  */
  public Names getNames(
    )
  {
    PdfDirectObject namesObject = getBaseDataObject().get(PdfName.Names);
    if(namesObject == null)
      return null;

    return new Names(
      namesObject,
      ((PdfReference)getBaseObject()).getIndirectObject()
      );
  }

  /**
    Gets the page layout to be used when the document is opened.
  */
  public PageLayoutEnum getPageLayout(
    )
  {
    PdfName value = (PdfName)getBaseDataObject().get(PdfName.PageLayout);
    if(value.equals(PdfName.OneColumn))
      return PageLayoutEnum.OneColumn;
    else if(value.equals(PdfName.TwoColumnLeft))
      return PageLayoutEnum.TwoColumns;
    else
      return PageLayoutEnum.SinglePage;
  }

  /**
    Gets the page mode, that is how the document should be displayed when is opened.
  */
  public PageModeEnum getPageMode(
    )
  {
    PdfName value = (PdfName)getBaseDataObject().get(PdfName.PageMode);
    if(value.equals(PdfName.UseOutlines))
      return PageModeEnum.Outlines;
    else if(value.equals(PdfName.UseThumbs))
      return PageModeEnum.Thumbnails;
    else if(value.equals(PdfName.FullScreen))
      return PageModeEnum.FullScreen;
    else
      return PageModeEnum.Simple;
  }

  /**
    Gets the page collection [PDF:1.6:3.6.2].
  */
  public Pages getPages(
    )
  {return new Pages(getBaseDataObject().get(PdfName.Pages)); /* NOTE: Required. */}

  /**
    Gets the default page size [PDF:1.6:3.6.2].
  */
  public Dimension2D getPageSize(
    )
  {
    /*
      NOTE: Due to the contract,
      we cannot force the existence of the default media box at document level.
    */
    PdfArray box = getMediaBox();
    if(box == null)
      return null;

    return new Dimension(
      (int)((IPdfNumber)box.get(2)).getNumberValue(),
      (int)((IPdfNumber)box.get(3)).getNumberValue()
      );
  }

  /**
    Gets the default resource collection [PDF:1.6:3.6.2].
    <h3>Remarks</h3>
    <p>The default resource collection is used as last resort by every page that doesn't reference one
    explicitly (and doesn't reference an intermediate one implicitly).</p>
  */
  public Resources getResources(
    )
  {
    PdfReference pages = (PdfReference)getBaseDataObject().get(PdfName.Pages);
    PdfDirectObject resources = ((PdfDictionary)File.resolve(pages)).get(PdfName.Resources);
    if(resources == null)
      return null;

    return new Resources(
      resources,
      pages.getIndirectObject()
      );
  }

  /**
    Gets the document size, that is the maximum page dimensions across the whole document.
  */
  public Dimension2D getSize(
    )
  {
    int height = 0, width = 0;
    for(Page page : getPages())
    {
      Dimension2D pageSize = page.getSize();
      if(pageSize == null)
        continue;

      height = Math.max(height,(int)pageSize.getHeight());
      width = Math.max(width,(int)pageSize.getWidth());
    }
    return new Dimension(width,height);
  }

  /**
    Gets the version of the PDF specification to which the document conforms [PDF:1.6:3.6.1].
  */
  public String getVersion(
    )
  {
    /*
      NOTE: If the header specifies a later version, or if this entry is absent,
      the document conforms to the version specified in the header.
    */
    String fileVersion = getFile().getVersion(); // Header version.

    /*
      NOTE: 'Version' entry may be undefined.
    */
    PdfName versionObject = (PdfName)getBaseDataObject().get(PdfName.Version);
    if(versionObject == null)
      return fileVersion;

    String version = versionObject.getRawValue();
    if(getFile().getReader() == null) // New file.
      return version;

    String[] fileVersionDigits;
    String[] versionDigits;
    try
    {
      fileVersionDigits = fileVersion.split("\\.");
      versionDigits = version.split("\\.");
    }
    catch(Exception exception)
    {throw new RuntimeException("Version decomposition failed.",exception);}

    try
    {
      if(Integer.parseInt(versionDigits[0]) > Integer.parseInt(fileVersionDigits[0])
        || (Integer.parseInt(versionDigits[0]) == Integer.parseInt(fileVersionDigits[0])
          && Integer.parseInt(versionDigits[1]) > Integer.parseInt(fileVersionDigits[1])))
        return version;

      return fileVersion;
    }
    catch(Exception exception)
    {throw new RuntimeException("Wrong version format.",exception);}
  }

  /**
    Gets the way the document is to be presented [PDF:1.6:8.1].
  */
  public ViewerPreferences getViewerPreferences(
    )
  {
    PdfDirectObject viewerPreferences = getBaseDataObject().get(PdfName.ViewerPreferences);
    if(viewerPreferences == null)
      return null;

    return new ViewerPreferences(
      viewerPreferences,
      ((PdfReference)getBaseObject()).getIndirectObject() //TODO: getContainer()?
      );
  }

  /**
    @see #getActions()
  */
  public void setActions(
    DocumentActions value
    )
  {getBaseDataObject().put(PdfName.AA, value.getBaseObject());}

  /**
    @see #getBookmarks()
  */
  public void setBookmarks(
    Bookmarks value
    )
  {getBaseDataObject().put(PdfName.Outlines,value.getBaseObject());}

  /**
    @see #getForm()
    @since 0.0.7
  */
  public void setForm(
    Form value
    )
  {getBaseDataObject().put(PdfName.AcroForm,value.getBaseObject());}

  /**
    @see #getInformation()
  */
  public void setInformation(
    Information value
    )
  {getFile().getTrailer().put(PdfName.Info,value.getBaseObject());}

  /**
    @see #getNames()
    @since 0.0.4
  */
  public void setNames(
    Names value
    )
  {getBaseDataObject().put(PdfName.Names,value.getBaseObject());}

  /**
    @see #getPageLayout()
  */
  public void setPageLayout(
    PageLayoutEnum value
    )
  {
    switch(value)
    {
      case SinglePage:
        getBaseDataObject().put(PdfName.PageLayout,PdfName.SinglePage);
        break;
      case OneColumn:
        getBaseDataObject().put(PdfName.PageLayout,PdfName.OneColumn);
        break;
      case TwoColumns:
        getBaseDataObject().put(PdfName.PageLayout,PdfName.TwoColumnLeft);
        break;
    }
  }

  /**
    @see #getPageMode()
  */
  public void setPageMode(
    PageModeEnum value
    )
  {
    switch(value)
    {
      case Simple:
        getBaseDataObject().put(PdfName.PageMode,PdfName.UseNone);
        break;
      case Outlines:
        getBaseDataObject().put(PdfName.PageMode,PdfName.UseOutlines);
        break;
      case Thumbnails:
        getBaseDataObject().put(PdfName.PageMode,PdfName.UseThumbs);
        break;
      case FullScreen:
        getBaseDataObject().put(PdfName.PageMode,PdfName.FullScreen);
        break;
    }
  }

  /**
    @see #getPages()
  */
  public void setPages(
    Pages value
    )
  {getBaseDataObject().put(PdfName.Pages,value.getBaseObject());}

  /**
    @see #getPageSize()
  */
  public void setPageSize(
    Dimension2D value
    )
  {
    PdfArray mediaBox = getMediaBox();
    if(mediaBox == null)
    {
      // Create default media box!
      mediaBox = new Rectangle(0,0,0,0).getBaseDataObject();
      // Assign the media box to the document!
      ((PdfDictionary)File.resolve(
        getBaseDataObject().get(PdfName.Pages)
        )).put(PdfName.MediaBox,mediaBox);
    }

    mediaBox.set(2,new PdfReal(value.getWidth()));
    mediaBox.set(3,new PdfReal(value.getHeight()));
  }

  /**
    @see #getResources()
  */
  public void setResources(
    Resources value
    )
  {
    PdfReference pages = (PdfReference)getBaseDataObject().get(PdfName.Pages);
    ((PdfDictionary)File.resolve(pages)).put(
      PdfName.Resources,
      value.getBaseObject()
      );
    value.setContainer(
      pages.getIndirectObject()
      ); // Resources object could be directly inside a container.
  }

  /**
    @see #getVersion()
  */
  public void setVersion(
    String value
    )
  {getBaseDataObject().put(PdfName.Version,new PdfName(value));}

  /**
    @see #getViewerPreferences()
  */
  public void setViewerPreferences(
    ViewerPreferences value
    )
  {
    getBaseDataObject().put(
      PdfName.ViewerPreferences,
      value.getBaseObject()
      );
    value.setContainer(
      ((PdfReference)getBaseObject()).getIndirectObject()
      ); // ViewerPreferences object could be directly inside a container.
  }
  // </public>

  // <private>
  /**
    Gets the default media box.
  */
  private PdfArray getMediaBox(
    )
  {
    /*
      NOTE: Document media box MUST be associated with the page-tree root node
      in order to be inheritable by all the pages.
    */
    return (PdfArray)((PdfDictionary)getBaseDataObject().resolve(PdfName.Pages)).resolve(PdfName.MediaBox);
  }
  // </private>
  // </interface>
  // </dynamic>
  // </class>
}