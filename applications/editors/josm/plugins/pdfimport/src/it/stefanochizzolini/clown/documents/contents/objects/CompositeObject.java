/*
  Copyright 2007-2010 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

import it.stefanochizzolini.clown.bytes.IOutputStream;
import it.stefanochizzolini.clown.documents.contents.ContentScanner;

import java.util.ArrayList;
import java.util.List;

/**
  Composite object. It is made up of multiple content objects.

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.4
  @version 0.0.8
*/
public abstract class CompositeObject
  extends ContentObject
{
  // <class>
  // <dynamic>
  // <fields>
  protected List<ContentObject> objects;
  // </fields>

  // <constructors>
  protected CompositeObject(
    )
  {this.objects = new ArrayList<ContentObject>(2);}

  protected CompositeObject(
    ContentObject object
    )
  {
    this();    
    objects.add(object);
  }

  protected CompositeObject(
    ContentObject... objects
    )
  {
    this();
    for(ContentObject object : objects)
    {this.objects.add(object);}
  }

  protected CompositeObject(
    List<ContentObject> objects
    )
  {this.objects = objects;}
  // </constructors>

  // <interface>
  // <public>
  @Override
  public void applyTo(
    ContentScanner.GraphicsState state
    )
  {
    // Update the current level's graphics state according to the current object's final graphics state!
    ContentScanner childLevel = state.getScanner().getChildLevel();
    childLevel.moveEnd(); // Forces the current object to its final graphics state.
    childLevel.getState().copyTo(state); // Copies the current object's final graphics state to the current level's.
  }

  /**
    Gets the list of inner objects.
  */
  public List<ContentObject> getObjects(
    )
  {return objects;}
  
  @Override
  public String toString(
    )
  {
    return "{"
      + objects.toString()
      + "}";
  }
  
  @Override
  public void writeTo(
    IOutputStream stream
    )
  {
    for(ContentObject object : objects)
    {object.writeTo(stream);}
  }
  // </public>
  // </interface>
  // </dynamic>
  // </class>
}