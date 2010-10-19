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

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.documents.fileSpecs.FileSpec;
import it.stefanochizzolini.clown.documents.interaction.navigation.document.Destination;
import it.stefanochizzolini.clown.objects.PdfAtomicObject;
import it.stefanochizzolini.clown.objects.PdfBoolean;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfIndirectObject;
import it.stefanochizzolini.clown.objects.PdfInteger;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfString;
import it.stefanochizzolini.clown.objects.PdfTextString;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.util.EnumSet;

/**
  'Change the view to a specified destination in a PDF file
  embedded in another PDF file' action [PDF:1.6:8.5.3].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8
  @since 0.0.7
*/
public class GoToEmbedded
  extends GoToNonLocal<Destination>
{
  // <class>
  // <classes>
  /**
    Path information to the target document [PDF:1.6:8.5.3].
  */
  public static class TargetObject
    extends PdfObjectWrapper<PdfDictionary>
  {
    // <class>
    // <classes>
    /**
      Relationship between the target and the current document [PDF:1.6:8.5.3].
    */
    public enum RelationEnum
    {
      // <class>
      // <static>
      // <fields>
      /**
        Parent.
      */
      Parent(PdfName.P),
      /**
        Child.
      */
      Child(PdfName.C);
      // </fields>

      // <interface>
      // <public>
      /**
        Gets the relation corresponding to the given value.
      */
      public static RelationEnum get(
        PdfName value
        )
      {
        for(RelationEnum relation : RelationEnum.values())
        {
          if(relation.getCode().equals(value))
            return relation;
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
      private RelationEnum(
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
    /**
      Creates a new target representing the parent of the document.
    */
    public TargetObject(
      Document context,
      TargetObject target
      )
    {
      this(
        context,
        RelationEnum.Parent,
        null,
        null,
        null,
        target
        );
    }

    /**
      Creates a new target located in the embedded files collection of the document.
    */
    public TargetObject(
      Document context,
      String embeddedFileName,
      TargetObject target
      )
    {
      this(
        context,
        RelationEnum.Child,
        embeddedFileName,
        null,
        null,
        target
        );
    }

    /**
      Creates a new target associated with a file attachment annotation.
    */
    public TargetObject(
      Document context,
      Object annotationPageRef,
      Object annotationRef,
      TargetObject target
      )
    {
      this(
        context,
        RelationEnum.Child,
        null,
        annotationPageRef,
        annotationRef,
        target
        );
    }

    /**
      Creates a new target.
    */
    private TargetObject(
      Document context,
      RelationEnum relation,
      String embeddedFileName,
      Object annotationPageRef,
      Object annotationRef,
      TargetObject target
      )
    {
      super(
        context.getFile(),
        new PdfDictionary()
        );

      setRelation(relation);
      setEmbeddedFileName(embeddedFileName);
      setAnnotationPageRef(annotationPageRef);
      setAnnotationRef(annotationRef);
      setTarget(target);
    }

    private TargetObject(
      PdfDirectObject baseObject,
      PdfIndirectObject container
      )
    {super(baseObject,container);}
    // </constructors>

    // <interface>
    // <public>
    @Override
    public TargetObject clone(
      Document context
      )
    {throw new NotImplementedException();}

    /**
      Gets the page reference to the file attachment annotation.

      @return Either the (zero-based) number of the page in the current document containing the file attachment annotation,
        or the name of a destination in the current document that provides the page number of the file attachment annotation.
    */
    public Object getAnnotationPageRef(
      )
    {
      /*
        NOTE: 'P' entry may be undefined.
      */
      PdfAtomicObject<?> pageRefObject = (PdfAtomicObject<?>)getBaseDataObject().get(PdfName.P);
      if(pageRefObject == null)
        return null;

      return pageRefObject.getValue();
    }

    /**
      Gets the reference to the file attachment annotation.

      @return Either the (zero-based) index of the annotation in the list of annotations
        associated to the page specified by the annotationPageRef property,
        or the name of the annotation.
    */
    public Object getAnnotationRef(
      )
    {
      /*
        NOTE: 'A' entry may be undefined.
      */
      PdfAtomicObject<?> annotationRefObject = (PdfAtomicObject<?>)getBaseDataObject().get(PdfName.A);
      if(annotationRefObject == null)
        return null;

      return annotationRefObject.getValue();
    }

    /**
      Gets the embedded file name.
    */
    public String getEmbeddedFileName(
      )
    {
      /*
        NOTE: 'N' entry may be undefined.
      */
      PdfString fileNameObject = (PdfString)getBaseDataObject().get(PdfName.N);
      if(fileNameObject == null)
        return null;

      return (String)fileNameObject.getValue();
    }

    /**
      Gets the relationship between the target and the current document.
    */
    public RelationEnum getRelation(
      )
    {
      /*
        NOTE: 'R' entry MUST exist.
      */
      return RelationEnum.get((PdfName)getBaseDataObject().get(PdfName.R));
    }

    /**
      Gets a further path information to the target document.
    */
    public TargetObject getTarget(
      )
    {
      /*
        NOTE: 'T' entry may be undefined.
      */
      PdfDirectObject targetObject = getBaseDataObject().get(PdfName.T);
      if(targetObject == null)
        return null;

      return new TargetObject(targetObject,getContainer());
    }

    /**
      @see #getAnnotationPageRef()
    */
    public void setAnnotationPageRef(
      Object value
      )
    {
      if(value == null)
      {getBaseDataObject().remove(PdfName.P);}
      else
      {
        PdfDirectObject pageRefObject;
        if(value instanceof Integer)
        {pageRefObject = new PdfInteger((Integer)value);}
        else if(value instanceof String)
        {pageRefObject = new PdfString((String)value);}
        else
          throw new IllegalArgumentException("Wrong argument type: it MUST be either a page number Integer or a named destination String.");

        getBaseDataObject().put(PdfName.P, pageRefObject);
      }
    }

    /**
      @see #getAnnotationRef()
    */
    public void setAnnotationRef(
      Object value
      )
    {
      if(value == null)
      {getBaseDataObject().remove(PdfName.A);}
      else
      {
        PdfDirectObject annotationRefObject;
        if(value instanceof Integer)
        {annotationRefObject = new PdfInteger((Integer)value);}
        else if(value instanceof String)
        {annotationRefObject = new PdfTextString((String)value);}
        else
          throw new IllegalArgumentException("Wrong argument type: it MUST be either an annotation index Integer or an annotation name String.");

        getBaseDataObject().put(PdfName.A, annotationRefObject);
      }
    }

    /**
      @see #getEmbeddedFileName()
    */
    public void setEmbeddedFileName(
      String value
      )
    {
      if(value == null)
      {getBaseDataObject().remove(PdfName.N);}
      else
      {getBaseDataObject().put(PdfName.N, new PdfString(value));}
    }

    /**
      @see #getRelation()
    */
    public void setRelation(
      RelationEnum value
      )
    {getBaseDataObject().put(PdfName.R, value.getCode());}

    /**
      @see #getTarget()
    */
    public void setTarget(
      TargetObject value
      )
    {
      if(value == null)
      {getBaseDataObject().remove(PdfName.T);}
      else
      {getBaseDataObject().put(PdfName.T, value.getBaseObject());}
    }
    // </public>
    // </interface>
    // </dynamic>
    // </class>
  }
  // </classes>

  // <dynamic>
  // <constructors>
  /**
    Creates a new embedded-goto action within the given document context,
    pointing to a destination within a target document embedded within the same document.
  */
  public GoToEmbedded(
    Document context,
    TargetObject target,
    Destination destination
    )
  {
    this(
      context,
      null,
      target,
      destination
      );
  }

  /**
    Creates a new embedded-goto action within the given document context,
    pointing to a destination within another document.
  */
  public GoToEmbedded(
    Document context,
    FileSpec fileSpec,
    Destination destination
    )
  {
    this(
      context,
      fileSpec,
      null,
      destination
      );
  }

  /**
    Creates a new embedded-goto action within the given document context.
  */
  public GoToEmbedded(
    Document context,
    FileSpec fileSpec,
    TargetObject target,
    Destination destination
    )
  {
    super(
      context,
      PdfName.GoToE,
      fileSpec,
      destination
      );
    setTarget(target);
  }

  GoToEmbedded(
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
  public GoToEmbedded clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the action options.
  */
  public EnumSet<OptionsEnum> getOptions(
    )
  {
    EnumSet<OptionsEnum> options = EnumSet.noneOf(OptionsEnum.class);

    PdfDirectObject optionsObject = getBaseDataObject().get(PdfName.NewWindow);
    if(optionsObject != null
      && ((Boolean)((PdfBoolean)optionsObject).getValue()).booleanValue())
    {options.add(OptionsEnum.NewWindow);}

    return options;
  }

  /**
    Gets the path information to the target document.
  */
  public TargetObject getTarget(
    )
  {
    /*
      NOTE: 'T' entry may be undefined.
    */
    PdfDirectObject targetObject = getBaseDataObject().get(PdfName.T);
    if(targetObject == null)
      return null;

    return new TargetObject(targetObject,getContainer());
  }

  /**
    @see #getOptions()
  */
  public void setOptions(
    EnumSet<OptionsEnum> value
    )
  {
    if(value.contains(OptionsEnum.NewWindow))
    {getBaseDataObject().put(PdfName.NewWindow,PdfBoolean.True);}
    else if(value.contains(OptionsEnum.SameWindow))
    {getBaseDataObject().put(PdfName.NewWindow,PdfBoolean.False);}
    else
    {getBaseDataObject().remove(PdfName.NewWindow);} // NOTE: Forcing the absence of this entry ensures that the viewer application should behave in accordance with the current user preference.
  }

  /**
    @see #getTarget()
  */
  public void setTarget(
    TargetObject value
    )
  {
    if(value == null)
    {getBaseDataObject().remove(PdfName.T);}
    else
    {getBaseDataObject().put(PdfName.T, value.getBaseObject());}
  }
  // </public>
  
  // <protected>
  @Override
  protected Class<Destination> getDestinationClass() {
    return Destination.class;
  }
  // </protected>
  // </interface>
  // </dynamic>
  // </class>
}