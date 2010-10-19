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

package it.stefanochizzolini.clown.documents.interaction.annotations;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.documents.interaction.actions.Action;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
  Annotation actions [PDF:1.6:8.5.2].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.7
*/
public class AnnotationActions
  extends PdfObjectWrapper<PdfDictionary>
  implements Map<PdfName,Action>
{
  // <class>
  // <dynamic>
  // <fields>
  private Annotation parent;
  // </fields>

  // <constructors>
  public AnnotationActions(
    Annotation parent
    )
  {
    super(
      parent.getFile(),
      new PdfDictionary()
      );

    this.parent = parent;
  }

  public AnnotationActions(
    Annotation parent,
    PdfDirectObject baseObject,
    PdfIndirectObject container
    )
  {
    super(baseObject,container);

    this.parent = parent;
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public AnnotationActions clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the action to be performed when the annotation is activated.
  */
  public Action getOnActivate(
    )
  {return parent.getAction();}

  /**
    Gets the action to be performed when the cursor enters the annotation's active area.
  */
  public Action getOnEnter(
    )
  {
    /*
      NOTE: 'E' entry may be undefined.
    */
    PdfDirectObject onEnterObject = getBaseDataObject().get(PdfName.E);
    if(onEnterObject == null)
      return null;

    return Action.wrap(onEnterObject,getContainer());
  }

  /**
    Gets the action to be performed when the cursor exits the annotation's active area.
  */
  public Action getOnExit(
    )
  {
    /*
      NOTE: 'X' entry may be undefined.
    */
    PdfDirectObject onExitObject = getBaseDataObject().get(PdfName.X);
    if(onExitObject == null)
      return null;

    return Action.wrap(onExitObject,getContainer());
  }

  /**
    Gets the action to be performed when the mouse button is pressed
    inside the annotation's active area.
  */
  public Action getOnMouseDown(
    )
  {
    /*
      NOTE: 'D' entry may be undefined.
    */
    PdfDirectObject onMouseDownObject = getBaseDataObject().get(PdfName.D);
    if(onMouseDownObject == null)
      return null;

    return Action.wrap(onMouseDownObject,getContainer());
  }

  /**
    Gets the action to be performed when the mouse button is released
    inside the annotation's active area.
  */
  public Action getOnMouseUp(
    )
  {
    /*
      NOTE: 'U' entry may be undefined.
    */
    PdfDirectObject onMouseUpObject = getBaseDataObject().get(PdfName.U);
    if(onMouseUpObject == null)
      return null;

    return Action.wrap(onMouseUpObject,getContainer());
  }

  /**
    Gets the action to be performed when the page containing the annotation is closed.
  */
  public Action getOnPageClose(
    )
  {
    /*
      NOTE: 'PC' entry may be undefined.
    */
    PdfDirectObject onPageCloseObject = getBaseDataObject().get(PdfName.PC);
    if(onPageCloseObject == null)
      return null;

    return Action.wrap(onPageCloseObject,getContainer());
  }

  /**
    Gets the action to be performed when the page containing the annotation
    is no longer visible in the viewer application's user interface.
  */
  public Action getOnPageInvisible(
    )
  {
    /*
      NOTE: 'PI' entry may be undefined.
    */
    PdfDirectObject onPageInvisibleObject = getBaseDataObject().get(PdfName.PI);
    if(onPageInvisibleObject == null)
      return null;

    return Action.wrap(onPageInvisibleObject,getContainer());
  }

  /**
    Gets the action to be performed when the page containing the annotation is opened.
  */
  public Action getOnPageOpen(
    )
  {
    /*
      NOTE: 'PO' entry may be undefined.
    */
    PdfDirectObject onPageOpenObject = getBaseDataObject().get(PdfName.PO);
    if(onPageOpenObject == null)
      return null;

    return Action.wrap(onPageOpenObject,getContainer());
  }

  /**
    Gets the action to be performed when the page containing the annotation
    becomes visible in the viewer application's user interface.
  */
  public Action getOnPageVisible(
    )
  {
    /*
      NOTE: 'PV' entry may be undefined.
    */
    PdfDirectObject onPageVisibleObject = getBaseDataObject().get(PdfName.PV);
    if(onPageVisibleObject == null)
      return null;

    return Action.wrap(onPageVisibleObject,getContainer());
  }

  /**
    @see #getOnActivate()
  */
  public void setOnActivate(
    Action value
    )
  {parent.setAction(value); parent.update();}

  /**
    @see #getOnEnter()
  */
  public void setOnEnter(
    Action value
    )
  {getBaseDataObject().put(PdfName.E, value.getBaseObject());}

  /**
    @see #getOnExit()
  */
  public void setOnExit(
    Action value
    )
  {getBaseDataObject().put(PdfName.X, value.getBaseObject());}

  /**
    @see #getOnMouseDown()
  */
  public void setOnMouseDown(
    Action value
    )
  {getBaseDataObject().put(PdfName.D, value.getBaseObject());}

  /**
    @see #getOnMouseUp()
  */
  public void setOnMouseUp(
    Action value
    )
  {getBaseDataObject().put(PdfName.U, value.getBaseObject());}

  /**
    @see #getOnPageClose()
  */
  public void setOnPageClose(
    Action value
    )
  {getBaseDataObject().put(PdfName.PC, value.getBaseObject());}

  /**
    @see #getOnPageInvisible()
  */
  public void setOnPageInvisible(
    Action value
    )
  {getBaseDataObject().put(PdfName.PI, value.getBaseObject());}

  /**
    @see #getOnPageOpen()
  */
  public void setOnPageOpen(
    Action value
    )
  {getBaseDataObject().put(PdfName.PO, value.getBaseObject());}

  /**
    @see #getOnPageVisible()
  */
  public void setOnPageVisible(
    Action value
    )
  {getBaseDataObject().put(PdfName.PV, value.getBaseObject());}

  // <Map>
  public void clear(
    )
  {
    getBaseDataObject().clear();
    setOnActivate(null);
  }

  public boolean containsKey(
    Object key
    )
  {
    return getBaseDataObject().containsKey(key)
      || (PdfName.A.equals(key) && parent.getBaseDataObject().containsKey(key));
  }

  public boolean containsValue(
    Object value
    )
  {
    return value != null
      && (getBaseDataObject().containsValue(((Action)value).getBaseObject())
        || (value.equals(getOnActivate())));
  }

  public Set<java.util.Map.Entry<PdfName, Action>> entrySet(
    )
  {throw new NotImplementedException();}

  public Action get(
    Object key
    )
  {
    return Action.wrap(
      getBaseDataObject().get(key),
      getContainer()
      );
  }

  public boolean isEmpty(
    )
  {return getBaseDataObject().isEmpty() && getOnActivate() == null;}

  public Set<PdfName> keySet(
    )
  {
    HashSet<PdfName> keySet = new HashSet<PdfName>(getBaseDataObject().keySet());
    if(parent.getBaseDataObject().containsKey(PdfName.A))
    {keySet.add(PdfName.A);}

    return keySet();
  }

  public Action put(
    PdfName key,
    Action value
    )
  {
    return Action.wrap(
      value == null
        ? getBaseDataObject().remove(key)
        : getBaseDataObject().put(key,value.getBaseObject()),
      getContainer()
      );
  }

  public void putAll(
    Map<? extends PdfName, ? extends Action> entries
    )
  {throw new NotImplementedException();}

  public Action remove(
    Object key
    )
  {
    Action oldValue;
    if(PdfName.A.equals(key))
    {
      oldValue = getOnActivate();
      setOnActivate(null);
    }
    else
    {
      oldValue = Action.wrap(
        getBaseDataObject().remove(key),
        getContainer()
        );
    }
    return oldValue;
  }

  public int size(
    )
  {
    return getBaseDataObject().size()
      + (parent.getBaseDataObject().containsKey(PdfName.A) ? 1 : 0);
  }

  public Collection<Action> values(
    )
  {
    Collection<PdfDirectObject> objects = getBaseDataObject().values();
    Collection<Action> values = new ArrayList<Action>(objects.size());
    for(PdfDirectObject object : objects)
    {values.add(Action.wrap(object,getContainer()));}
    Action action = getOnActivate();
    if(action != null)
    {values.add(action);}

    return values;
  }
  // </Map>
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}