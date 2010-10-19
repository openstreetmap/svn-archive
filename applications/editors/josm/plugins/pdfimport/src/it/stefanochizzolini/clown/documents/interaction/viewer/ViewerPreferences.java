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

package it.stefanochizzolini.clown.documents.interaction.viewer;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.objects.PdfAtomicObject;
import it.stefanochizzolini.clown.objects.PdfBoolean;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.util.NotImplementedException;

/**
  Viewer preferences [PDF:1.6:8.1].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.7
*/
public class ViewerPreferences
  extends PdfObjectWrapper<PdfDictionary>
{
  // <class>
  // <classes>
  /**
    Predominant reading order for text [PDF:1.6:8.1].
  */
  public enum DirectionEnum
  {
    // <class>
    // <static>
    // <fields>
    /**
      Left to right.
    */
    LeftToRight(PdfName.L2R),
    /**
      Right to left.
    */
    RightToLeft(PdfName.R2L);
    // </fields>

    // <interface>
    // <public>
    /**
      Gets the direction corresponding to the given value.
    */
    public static DirectionEnum get(
      PdfName value
      )
    {
      for(DirectionEnum direction : DirectionEnum.values())
      {
        if(direction.getCode().equals(value))
          return direction;
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
    private DirectionEnum(
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

  // <dynamic>
  // <constructors>
  public ViewerPreferences(
    Document context
    )
  {
    super(
      context.getFile(),
      new PdfDictionary()
      );
  }

  /**
    For internal use only.
  */
  public ViewerPreferences(
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
  public ViewerPreferences clone(
    Document context
    )
  {throw new NotImplementedException();}

  public DirectionEnum getDirection(
    )
  {
    /*
      NOTE: 'Direction' entry may be undefined.
    */
    PdfName directionObject = (PdfName)getBaseDataObject().get(PdfName.Direction);
    if(directionObject == null)
      return DirectionEnum.LeftToRight;

    return DirectionEnum.get(directionObject);
  }

  public boolean isCenterWindow(
    )
  {return this.<Boolean,PdfBoolean>getEntry(PdfName.CenterWindow);}

  public boolean isDisplayDocTitle(
    )
  {return this.<Boolean,PdfBoolean>getEntry(PdfName.DisplayDocTitle);}

  public boolean isFitWindow(
    )
  {return this.<Boolean,PdfBoolean>getEntry(PdfName.FitWindow);}

  public boolean isHideMenubar(
    )
  {return this.<Boolean,PdfBoolean>getEntry(PdfName.HideMenubar);}

  public boolean isHideToolbar(
    )
  {return this.<Boolean,PdfBoolean>getEntry(PdfName.HideToolbar);}

  public boolean isHideWindowUI(
    )
  {return this.<Boolean,PdfBoolean>getEntry(PdfName.HideWindowUI);}

  public void setCenterWindow(
    boolean value
    )
  {this.<Boolean,PdfBoolean>setEntry(PdfName.CenterWindow,value,PdfBoolean.class);}

  public void setDirection(
    DirectionEnum value
    )
  {getBaseDataObject().put(PdfName.Direction,value.getCode());}

  public void setDisplayDocTitle(
    boolean value
    )
  {this.<Boolean,PdfBoolean>setEntry(PdfName.DisplayDocTitle,value,PdfBoolean.class);}

  public void setFitWindow(
    boolean value
    )
  {this.<Boolean,PdfBoolean>setEntry(PdfName.FitWindow,value,PdfBoolean.class);}

  public void setHideMenubar(
    boolean value
    )
  {this.<Boolean,PdfBoolean>setEntry(PdfName.HideMenubar,value,PdfBoolean.class);}

  public void setHideToolbar(
    boolean value
    )
  {this.<Boolean,PdfBoolean>setEntry(PdfName.HideToolbar,value,PdfBoolean.class);}

  public void setHideWindowUI(
    boolean value
    )
  {this.<Boolean,PdfBoolean>setEntry(PdfName.HideWindowUI,value,PdfBoolean.class);}
  // </public>

  // <protected>
  @SuppressWarnings("unchecked")
  protected <T,TPdf extends PdfAtomicObject<T>> T getEntry(
    PdfName key
    )
  {
    TPdf entry = (TPdf)File.resolve(getBaseDataObject().get(key));
    if(entry == null)
      return null;

    return (T)entry.getValue();
  }

  @SuppressWarnings("unchecked")
  protected <T,TPdf extends PdfAtomicObject<T>> void setEntry(
    PdfName key,
    T value,
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
      {throw new RuntimeException(e);}
    }

    entry.setValue(value);
  }
  // </protected>
  // </interface>
  // </dynamic>
  // </class>
}