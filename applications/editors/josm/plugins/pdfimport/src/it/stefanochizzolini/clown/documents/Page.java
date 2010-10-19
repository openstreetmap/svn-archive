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

import it.stefanochizzolini.clown.bytes.IBuffer;
import it.stefanochizzolini.clown.documents.contents.Contents;
import it.stefanochizzolini.clown.documents.contents.IContentContext;
import it.stefanochizzolini.clown.documents.contents.Resources;
import it.stefanochizzolini.clown.documents.contents.composition.PrimitiveFilter;
import it.stefanochizzolini.clown.documents.contents.objects.ContentObject;
import it.stefanochizzolini.clown.documents.contents.xObjects.FormXObject;
import it.stefanochizzolini.clown.documents.contents.xObjects.XObject;
import it.stefanochizzolini.clown.documents.interaction.navigation.page.Transition;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.objects.IPdfNumber;
import it.stefanochizzolini.clown.objects.PdfArray;
import it.stefanochizzolini.clown.objects.PdfDataObject;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfInteger;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfReal;
import it.stefanochizzolini.clown.objects.PdfReference;
import it.stefanochizzolini.clown.objects.PdfStream;
import it.stefanochizzolini.clown.objects.Rectangle;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;

/**
  Document page [PDF:1.6:3.6.2].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.0
  @version 0.0.8
*/
public class Page
  extends PdfObjectWrapper<PdfDictionary>
  implements IContentContext
{
  /*
    NOTE: Inheritable attributes are NOT early-collected, as they are NOT part
    of the explicit representation of a page. They are retrieved everytime
    clients call.
  */
  // <class>
  // <classes>
  /**
    Annotations tab order [PDF:1.6:3.6.2].
  */
  public enum TabOrderEnum
  {
    // <class>
    // <static>
    // <fields>
    /**
      Row order.
    */
    Row(PdfName.R),
    /**
      Column order.
    */
    Column(PdfName.C),
    /**
      Structure order.
    */
    Structure(PdfName.S);
    // </fields>

    // <interface>
    // <public>
    /**
      Gets the tab order corresponding to the given value.
    */
    public static TabOrderEnum get(
      PdfName value
      )
    {
      for(TabOrderEnum tabOrder : TabOrderEnum.values())
      {
        if(tabOrder.getCode().equals(value))
          return tabOrder;
      }
      return null;
    }
    // </public>
    // </interface>
    // </static>

    // <dynamic>
    // <fields>
    private final PdfName code;
    // </fields>

    // <constructors>
    private TabOrderEnum(
      PdfName code
      )
    {this.code = code;}
    // </constructors>

    // <interface>
    // <public>
    public PdfName getCode(
      )
    {return code;}
    // </public>
    // </interface>
    // </dynamic>
    // </class>
  }
  // </classes>

  // <static>
  // <interface>
  // <public>
  public static Page wrap(
    PdfReference reference
    )
  {return new Page(reference);}
  // </public>
  // </interface>
  // </static>

  // <dynamic>
  // <constructors>
  /**
    Creates a new page within the given document context, using default resources.
  */
  public Page(
    Document context
    )
  {
    super(
      context.getFile(),
      new PdfDictionary(
        new PdfName[]
        {
          PdfName.Type,
          PdfName.Contents
        },
        new PdfDirectObject[]
        {
          PdfName.Page,
          context.getFile().register(
            new PdfStream()
            )
        }
        )
      );
  }

  /**
    Creates a new page within the given document context, using custom resources.
  */
  public Page(
    Document context,
    Dimension2D size,
    Resources resources
    )
  {
    super(
      context.getFile(),
      new PdfDictionary(
        new PdfName[]
        {
          PdfName.Type,
          PdfName.MediaBox,
          PdfName.Contents,
          PdfName.Resources
        },
        new PdfDirectObject[]
        {
          PdfName.Page,
          new Rectangle(0,0,size.getWidth(),size.getHeight()).getBaseDataObject(),
          context.getFile().register(
            new PdfStream()
            ),
          resources.getBaseObject()
        }
        )
      );
  }

  /**
    For internal use only.
  */
  public Page(
    PdfDirectObject baseObject
    )
  {
    super(
      baseObject,
      null // NO container. NOTE: this is a simplification (the spec [PDF:1.6] doesn't apparently prescribe the use of an indirect object for page dictionary, whilst the general practice is as such. If an exception occur, you'll need to specify the proper container).
      );
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public Page clone(
    Document context
    )
  {
    /*
      NOTE: We cannot just delegate the cloning to the base object, as it would
      involve some unwanted objects like those in 'Parent' and 'Annots' entries that may
      cause infinite loops (due to circular references) and may include exceeding contents
      (due to copy propagations to the whole page-tree which this page belongs to).
      TODO: 'Annots' entry must be finely treated to include any non-circular reference.
    */
    // TODO:IMPL deal with inheritable attributes!!!

    File contextFile = context.getFile();
    PdfDictionary clone = new PdfDictionary(getBaseDataObject().size());
    for(
      Map.Entry<PdfName,PdfDirectObject> entry : getBaseDataObject().entrySet()
      )
    {
      PdfName key = entry.getKey();
      // Is the entry unwanted?
      if(key.equals(PdfName.Parent)
        || key.equals(PdfName.Annots))
        continue;

      // Insert the clone of the entry into the clone of the page dictionary!
      clone.put(
        key,
        (PdfDirectObject)entry.getValue().clone(contextFile)
        );
    }

    return new Page(
      contextFile.getIndirectObjects().add(clone).getReference()
      );
  }

  /**
    Gets the page's behavior in response to trigger events.
  */
  public PageActions getActions(
    )
  {
    PdfDirectObject actionsObject = getBaseDataObject().get(PdfName.AA);
    if(actionsObject == null)
      return null;

    return new PageActions(actionsObject,getContainer());
  }

  /**
    Gets the annotations associated to the page.
  */
  public PageAnnotations getAnnotations(
    )
  {
    PdfDirectObject annotationsObject = getBaseDataObject().get(PdfName.Annots);
    if(annotationsObject == null)
      return null;

    return new PageAnnotations(annotationsObject,getContainer(),this);
  }

  /**
    Gets the page's display duration.
    <p>The page's display duration (also called its advance timing)
    is the maximum length of time, in seconds, that the page is displayed
    during presentations before the viewer application automatically advances
    to the next page.</p>
    <p>By default, the viewer does not advance automatically.</p>
  */
  public double getDuration(
    )
  {
    IPdfNumber durationObject = (IPdfNumber)getBaseDataObject().get(PdfName.Dur);
    if(durationObject == null)
      return 0;

    return durationObject.getNumberValue();
  }

  /**
    Gets the index of the page.
    <h3>Remarks</h3>
    <p>The page index is not an explicit datum, therefore it needs to be
    inferred from the position of the page object inside the page tree,
    requiring a significant amount of computation: invoke it sparingly!</p>
  */
  public int getIndex(
    )
  {
    /*
      NOTE: We'll scan sequentially each page-tree level above this page object
      collecting page counts. At each level we'll scan the kids array from the
      lower-indexed item to the ancestor of this page object at that level.
    */
    PdfReference ancestorKidReference = (PdfReference)getBaseObject();
    PdfReference parentReference = (PdfReference)getBaseDataObject().get(PdfName.Parent);
    PdfDictionary parent = (PdfDictionary)File.resolve(parentReference);
    PdfArray kids = (PdfArray)File.resolve(parent.get(PdfName.Kids));
    int index = 0;
    for(
      int i = 0;
      true;
      i++
      )
    {
      PdfReference kidReference = (PdfReference)kids.get(i);
      // Is the current-level counting complete?
      // NOTE: It's complete when it reaches the ancestor at the current level.
      if(kidReference.equals(ancestorKidReference)) // Ancestor node.
      {
        // Does the current level correspond to the page-tree root node?
        if(!parent.containsKey(PdfName.Parent))
        {
          // We reached the top: counting's finished.
          return index;
        }
        // Set the ancestor at the next level!
        ancestorKidReference = parentReference;
        // Move up one level!
        parentReference = (PdfReference)parent.get(PdfName.Parent);
        parent = (PdfDictionary)File.resolve(parentReference);
        kids = (PdfArray)File.resolve(parent.get(PdfName.Kids));
        i = -1;
      }
      else // Intermediate node.
      {
        PdfDictionary kid = (PdfDictionary)File.resolve(kidReference);
        if(kid.get(PdfName.Type).equals(PdfName.Page))
          index++;
        else
          index += ((PdfInteger)kid.get(PdfName.Count)).getRawValue();
      }
    }
  }

  /**
    Gets the page size.
  */
  public Dimension2D getSize(
    )
  {
    PdfArray box = (PdfArray)File.resolve(
      getInheritableAttribute(PdfName.MediaBox)
      );
    if(box == null)
      return null;

    return new Dimension(
      (int)((IPdfNumber)box.get(2)).getNumberValue(),
      (int)((IPdfNumber)box.get(3)).getNumberValue()
      );
  }

  /**
    Gets the tab order to be used for annotations on the page.
  */
  public TabOrderEnum getTabOrder(
    )
  {return TabOrderEnum.get((PdfName)getBaseDataObject().get(PdfName.Tabs));}

  /**
    Gets the transition effect to be used when displaying the page during presentations.
  */
  public Transition getTransition(
    )
  {
    PdfDirectObject transitionObject = getBaseDataObject().get(PdfName.Trans);
    if(transitionObject == null)
      return null;

    return new Transition(transitionObject,getContainer());
  }

  /**
    @see #getActions()
  */
  public void setActions(
    PageActions value
    )
  {getBaseDataObject().put(PdfName.AA, value.getBaseObject());}

  /**
    @see #getAnnotations()
  */
  public void setAnnotations(
    PageAnnotations value
    )
  {getBaseDataObject().put(PdfName.Annots, value.getBaseObject());}

  /**
    @see #getDuration()
  */
  public void setDuration(
    double value
    )
  {getBaseDataObject().put(PdfName.Dur,new PdfReal(value));}

  /**
    @see #getSize()
  */
  public void setSize(
    Dimension2D value
    )
  {
    /*
      NOTE: When page size is about to be modified, we MUST ensure that the change will affect just
      the mediaBox of this page; so, if such a mediaBox is implicit (inherited), it MUST be cloned
      and explicitly assigned to this page in order to apply changes.
    */
    PdfDictionary dictionary = getBaseDataObject();
    PdfDirectObject entry = dictionary.get(PdfName.MediaBox);
    if(entry == null)
    {
      // Clone the inherited attribute in order to restrict its change to this page's scope only!
      entry = (PdfDirectObject)getInheritableAttribute(PdfName.MediaBox).clone(getFile());
      // Associate the cloned attribute to this page's dictionary!
      dictionary.put(PdfName.MediaBox,entry);
    }

    PdfArray box = (PdfArray)File.resolve(entry);
    ((IPdfNumber)box.get(2)).setNumberValue(value.getWidth());
    ((IPdfNumber)box.get(3)).setNumberValue(value.getHeight());
  }

  /**
    @see #getTabOrder()
  */
  public void setTabOrder(
    TabOrderEnum value
    )
  {getBaseDataObject().put(PdfName.Tabs,value.getCode());}

  /**
    @see #getTransition()
  */
  public void setTransition(
    Transition value
    )
  {getBaseDataObject().put(PdfName.Trans, value.getBaseObject());}

  // <IContentContext>
  public Rectangle2D getBox(
    )
  {
    PdfArray box = (PdfArray)File.resolve(
      getInheritableAttribute(PdfName.MediaBox)
      );

    return new Rectangle2D.Double(
      ((IPdfNumber)box.get(0)).getNumberValue(),
      ((IPdfNumber)box.get(1)).getNumberValue(),
      ((IPdfNumber)box.get(2)).getNumberValue(),
      ((IPdfNumber)box.get(3)).getNumberValue()
      );
  }

  public Contents getContents(
    )
  {
    return new Contents(
      getBaseDataObject().get(PdfName.Contents),
      ((PdfReference)getBaseObject()).getIndirectObject(),
      this
      );
  }

  public Resources getResources(
    )
  {
    return new Resources(
      getInheritableAttribute(PdfName.Resources),
      ((PdfReference)getBaseObject()).getIndirectObject()
      );
  }

  // <IContentEntity>
  /**
    @since 0.0.6
  */
  public ContentObject toInlineObject(
    PrimitiveFilter context
    )
  {throw new NotImplementedException();}

  /**
    @since 0.0.5
  */
  public XObject toXObject(
    Document context
    )
  {
    File contextFile = context.getFile();

    FormXObject form = new FormXObject(context);
    PdfStream formStream = form.getBaseDataObject();

    // Header.
    {
      PdfDictionary formHeader = formStream.getHeader();
      // Bounding box.
      formHeader.put(
        PdfName.BBox,
        (PdfDirectObject)getInheritableAttribute(PdfName.MediaBox).clone(contextFile)
        );
      // Resources.
      {
        PdfDirectObject resourcesObject = getInheritableAttribute(PdfName.Resources);
        formHeader.put(
          PdfName.Resources,
          // Same document?
          /* NOTE: Try to reuse the resource dictionary whenever possible. */
          (context.equals(getDocument()) ?
            resourcesObject
            : (PdfDirectObject)resourcesObject.clone(contextFile))
          );
      }
    }

    // Body (contents).
    {
      IBuffer formBody = formStream.getBody();
      PdfDataObject contentsDataObject = File.resolve(getBaseDataObject().get(PdfName.Contents));
      if(contentsDataObject instanceof PdfStream)
      {formBody.append(((PdfStream)contentsDataObject).getBody());}
      else
      {
        for(PdfDirectObject contentStreamObject : (PdfArray)contentsDataObject)
        {formBody.append(((PdfStream)File.resolve(contentStreamObject)).getBody());}
      }
    }

    return form;
  }
  // </IContentEntity>
  // </IContentContext>
  // </public>

  // <protected>
  protected PdfDirectObject getInheritableAttribute(
    PdfName key
    )
  {
    /*
      NOTE: It moves upward until it finds the inherited attribute.
    */
    PdfDictionary dictionary = getBaseDataObject();
    while(true)
    {
      PdfDirectObject entry = dictionary.get(key);
      if(entry != null)
        return entry;

      dictionary = (PdfDictionary)File.resolve(
        dictionary.get(PdfName.Parent)
        );
      if(dictionary == null)
      {
        // Isn't the page attached to the page tree?
        /* NOTE: This condition is illegal. */
        if(getBaseDataObject().get(PdfName.Parent) == null)
          throw new RuntimeException("Inheritable attributes unreachable: Page objects MUST be inserted into their document's Pages collection before being used.");

        return null;
      }
    }
  }
  // </protected>
  // </interface>
  // </dynamic>
  // </class>
}