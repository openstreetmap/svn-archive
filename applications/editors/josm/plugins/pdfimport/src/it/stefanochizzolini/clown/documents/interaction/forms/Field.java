/*
  Copyright 2008 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.documents.interaction.forms;

import it.stefanochizzolini.clown.documents.Document;
import it.stefanochizzolini.clown.documents.interaction.annotations.Widget;
import it.stefanochizzolini.clown.files.File;
import it.stefanochizzolini.clown.objects.PdfAtomicObject;
import it.stefanochizzolini.clown.objects.PdfDictionary;
import it.stefanochizzolini.clown.objects.PdfDirectObject;
import it.stefanochizzolini.clown.objects.PdfInteger;
import it.stefanochizzolini.clown.objects.PdfName;
import it.stefanochizzolini.clown.objects.PdfObjectWrapper;
import it.stefanochizzolini.clown.objects.PdfReference;
import it.stefanochizzolini.clown.objects.PdfString;
import it.stefanochizzolini.clown.objects.PdfTextString;
import it.stefanochizzolini.clown.util.NotImplementedException;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
  Interactive form field [PDF:1.6:8.6.2].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.8.1
  @since 0.0.7
*/
public abstract class Field
  extends PdfObjectWrapper<PdfDictionary>
{
  /*
    NOTE: Inheritable attributes are NOT early-collected, as they are NOT part
    of the explicit representation of a page. They are retrieved everytime
    clients call.
  */
  // <class>
  // <classes>
  /**
    Field flags [PDF:1.6:8.6.2].
  */
  public enum FlagsEnum
  {
    // <class>
    // <static>
    // <fields>
    /**
      The user may not change the value of the field.
    */
    ReadOnly(0x1),
    /**
      The field must have a value at the time it is exported by a submit-form action.
    */
    Required(0x2),
    /**
      The field must not be exported by a submit-form action.
    */
    NoExport(0x4),
    /**
      (Text fields only) The field can contain multiple lines of text.
    */
    Multiline(0x1000),
    /**
      (Text fields only) The field is intended for entering a secure password
      that should not be echoed visibly to the screen.
    */
    Password(0x2000),
    /**
      (Radio buttons only) Exactly one radio button must be selected at all times.
    */
    NoToggleToOff(0x4000),
    /**
      (Button fields only) The field is a set of radio buttons (otherwise, a check box).
      This flag is meaningful only if the Pushbutton flag isn't selected.
    */
    Radio(0x8000),
    /**
      (Button fields only) The field is a pushbutton that does not retain a permanent value.
    */
    Pushbutton(0x10000),
    /**
      (Choice fields only) The field is a combo box (otherwise, a list box).
    */
    Combo(0x20000),
    /**
      (Choice fields only) The combo box includes an editable text box as well as a dropdown list
      (otherwise, it includes only a drop-down list).
    */
    Edit(0x40000),
    /**
      (Choice fields only) The field's option items should be sorted alphabetically.
    */
    Sort(0x80000),
    /**
      (Text fields only) Text entered in the field represents the pathname of a file
      whose contents are to be submitted as the value of the field.
    */
    FileSelect(0x100000),
    /**
      (Choice fields only) More than one of the field's option items may be selected simultaneously.
    */
    MultiSelect(0x200000),
    /**
      (Choice and text fields only) Text entered in the field is not spell-checked.
    */
    DoNotSpellCheck(0x400000),
    /**
      (Text fields only) The field does not scroll to accommodate more text
      than fits within its annotation rectangle.
      Once the field is full, no further text is accepted.
    */
    DoNotScroll(0x800000),
    /**
      (Text fields only) The field is automatically divided into as many equally spaced positions,
      or combs, as the value of MaxLen, and the text is laid out into those combs.
    */
    Comb(0x1000000),
    /**
      (Text fields only) The value of the field should be represented as a rich text string.
    */
    RichText(0x2000000),
    /**
      (Button fields only) A group of radio buttons within a radio button field that use
      the same value for the on state will turn on and off in unison
      (otherwise, the buttons are mutually exclusive).
    */
    RadiosInUnison(0x2000000),
    /**
      (Choice fields only) The new value is committed as soon as a selection is made with the pointing device.
    */
    CommitOnSelChange(0x4000000);
    // </fields>

    // <interface>
    // <public>
    /**
      Converts an enumeration set into its corresponding bit mask representation.
    */
    public static int toInt(
      EnumSet<FlagsEnum> flags
      )
    {
      int flagsMask = 0;
      for(FlagsEnum flag : flags)
      {flagsMask |= flag.getCode();}

      return flagsMask;
    }

    /**
      Converts a bit mask into its corresponding enumeration representation.
    */
    public static EnumSet<FlagsEnum> toEnumSet(
      int flagsMask
      )
    {
      EnumSet<FlagsEnum> flags = EnumSet.noneOf(FlagsEnum.class);
      for(FlagsEnum flag : FlagsEnum.values())
      {
        if((flagsMask & flag.getCode()) > 0)
        {flags.add(flag);}
      }

      return flags;
    }
    // </public>
    // </interface>
    // </static>

    // <dynamic>
    // <fields>
    /**
      <h3>Remarks</h3>
      <p>Bitwise code MUST be explicitly distinct from the ordinal position of the enum constant
      as they don't coincide.</p>
    */
    private final int code;
    // </fields>

    // <constructors>
    private FlagsEnum(
      int code
      )
    {this.code = code;}
    // </constructors>

    // <interface>
    // <public>
    public int getCode(
      )
    {return code;}
    // </public>
    // </interface>
    // </dynamic>
    // </class>
  }
  // </classes>

  // <static>
  // <fields>
  // </fields>

  // <interface>
  // <public>
  /**
    Wraps a field reference into a field object.

    @param reference Reference to a field object.
    @return Field object associated to the reference.
  */
  public static final Field wrap(
    PdfReference reference
    )
  {
    /*
      NOTE: This is a factory method for any field-derived object.
    */
    if(reference == null)
      return null;

    PdfDictionary dataObject = (PdfDictionary)reference.getDataObject();
    PdfName fieldType = (PdfName)dataObject.get(PdfName.FT);
    PdfInteger fieldFlags = (PdfInteger)dataObject.get(PdfName.Ff);
    int fieldFlagsValue = (fieldFlags == null ? 0 : fieldFlags.getRawValue());
    if(fieldType.equals(PdfName.Btn)) // Button.
    {
      if((fieldFlagsValue & FlagsEnum.Pushbutton.getCode()) > 0) // Pushbutton.
        return new PushButton(reference);
      if((fieldFlagsValue & FlagsEnum.Radio.getCode()) > 0) // Radio.
        return new RadioButton(reference);
      // Check box.
      return new CheckBox(reference);
    }
    if(fieldType.equals(PdfName.Tx)) // Text.
      return new TextField(reference);
    if(fieldType.equals(PdfName.Ch)) // Choice.
    {
      if((fieldFlagsValue & FlagsEnum.Combo.getCode()) > 0) // Combo box.
        return new ComboBox(reference);
      // List box.
      return new ListBox(reference);
    }
    if(fieldType.equals(PdfName.Sig)) // Signature.
      return new SignatureField(reference);
    // Unknown.
    return null;
  }
  // </public>
  // </interface>
  // </static>

  // <dynamic>
  // <constructors>
  /**
    Creates a new field within the given document context.
  */
  protected Field(
    PdfName fieldType,
    String name,
    Widget widget
    )
  {
    this(widget.getBaseObject());

    PdfDictionary baseDataObject = getBaseDataObject();
    baseDataObject.put(PdfName.FT, fieldType);
    baseDataObject.put(PdfName.T, new PdfTextString(name));
  }

  protected Field(
    PdfDirectObject baseObject
    )
  {
    super(
      baseObject,
      null // NO container (baseObject MUST be an indirect reference).
      );
  }
  // </constructors>

  // <interface>
  // <public>
  @Override
  public Field clone(
    Document context
    )
  {throw new NotImplementedException();}

  /**
    Gets the field's behavior in response to trigger events.
  */
  public FieldActions getActions(
    )
  {
    PdfDirectObject actionsObject = getBaseDataObject().get(PdfName.AA);
    if(actionsObject == null)
      return null;

    return new FieldActions(actionsObject,getContainer());
  }

  /**
    Gets the field default value.
  */
  public Object getDefaultValue(
    )
  {
    PdfAtomicObject<?> defaultValueObject = (PdfAtomicObject<?>)File.resolve(
      getInheritableAttribute(PdfName.DV)
      );
    if(defaultValueObject == null)
      return null;

    return defaultValueObject.getValue();
  }

  /**
    Gets the field flags.
  */
  public EnumSet<FlagsEnum> getFlags(
    )
  {
    PdfInteger flagsObject = (PdfInteger)File.resolve(
      getInheritableAttribute(PdfName.Ff)
      );
    if(flagsObject == null)
      return EnumSet.noneOf(FlagsEnum.class);

    return FlagsEnum.toEnumSet(flagsObject.getRawValue());
  }

  /**
    Gets the widget annotations that are associated with this field.
  */
  public FieldWidgets getWidgets(
    )
  {
    /*
      NOTE: Terminal fields MUST be associated at least to one widget annotation.
      If there is only one associated widget annotation and its contents
      have been merged into the field dictionary, 'Kids' entry MUST be omitted.
    */
    PdfDirectObject widgetsObject = getBaseDataObject().get(PdfName.Kids);
    if(widgetsObject == null) // Merged annotation.
      return new FieldWidgets(getBaseObject(), null, this);
    else // Annotation array.
    	return new FieldWidgets(widgetsObject, getContainer(), this);
  }

  /**
    Gets the fully-qualified field name.
  */
  public String getFullName(
    )
  {
    List<String> partialNames = new ArrayList<String>();
    PdfDictionary parent = getBaseDataObject();
    while(parent != null)
    {
      partialNames.add((String)((PdfTextString)parent.get(PdfName.T)).getValue());

      parent = (PdfDictionary)File.resolve(parent.get(PdfName.Parent));
    }

    StringBuilder buffer = new StringBuilder();
    for(
      int index = partialNames.size() - 1;
      index >= 0;
      index--
      )
    {
      buffer.append(partialNames.get(index));
      if(index > 0)
      {buffer.append(".");}
    }

    return buffer.toString();
  }

  /**
    Gets the partial field name.
  */
  public String getName(
    )
  {return (String)((PdfTextString)getBaseDataObject().get(PdfName.T)).getValue();}

  /**
    Gets the field value.
  */
  public Object getValue(
    )
  {
    PdfAtomicObject<?> valueObject = (PdfAtomicObject<?>)File.resolve(
      getInheritableAttribute(PdfName.V)
      );
    if(valueObject == null)
      return null;

    return valueObject.getValue();
  }

  /**
    Gets whether the field is exported by a submit-form action.
  */
  public boolean isExportable(
    )
  {return !getFlags().contains(FlagsEnum.NoExport);}

  /**
    Gets whether the user may not change the value of the field.
  */
  public boolean isReadOnly(
    )
  {return getFlags().contains(FlagsEnum.ReadOnly);}

  /**
    Gets whether the field must have a value at the time
    it is exported by a submit-form action.
  */
  public boolean isRequired(
    )
  {return getFlags().contains(FlagsEnum.Required);}

  /**
    @see #getActions()
  */
  public void setActions(
    FieldActions value
    )
  {getBaseDataObject().put(PdfName.AA,value.getBaseObject());}

  /**
    @see #isExportable()
  */
  public void setExportable(
    boolean value
    )
  {
    EnumSet<FlagsEnum> flags = getFlags();
    if(value)
    {flags.remove(FlagsEnum.NoExport);}
    else
    {flags.add(FlagsEnum.NoExport);}
    setFlags(flags);
  }

  /**
    @see #getFlags()
  */
  public void setFlags(
    EnumSet<FlagsEnum> value
    )
  {
    /*
      NOTE: As flags may be inherited from a parent field dictionary,
      we MUST ensure that the change will affect just this one;
      so, if such flags are implicit (inherited), they MUST be cloned
      and explicitly assigned to this field in order to apply changes.
    */
    PdfDictionary baseDataObject = getBaseDataObject();
    PdfInteger entry = (PdfInteger)baseDataObject.get(PdfName.Ff);
    if(entry == null) // Implicit flags.
    {
      // Clone the inherited attribute in order to restrict its change to this field's scope only!
      entry = (PdfInteger)getInheritableAttribute(PdfName.Ff).clone(getFile());
      // Associate the cloned attribute to this field's dictionary!
      baseDataObject.put(PdfName.Ff,entry);
    }

    entry.setRawValue(FlagsEnum.toInt(value));
  }

  /**
    @see #getName()
  */
  public void setName(
    String value
    )
  {getBaseDataObject().put(PdfName.T, new PdfTextString(value));}

  /**
    @see #isReadOnly()
  */
  public void setReadOnly(
    boolean value
    )
  {
    EnumSet<FlagsEnum> flags = getFlags();
    if(value)
    {flags.add(FlagsEnum.ReadOnly);}
    else
    {flags.remove(FlagsEnum.ReadOnly);}
    setFlags(flags);
  }

  /**
    @see #isRequired()
  */
  public void setRequired(
    boolean value
    )
  {
    EnumSet<FlagsEnum> flags = getFlags();
    if(value)
    {flags.add(FlagsEnum.Required);}
    else
    {flags.remove(FlagsEnum.Required);}
    setFlags(flags);
  }

  /**
    @see #getValue()
  */
  public void setValue(
    Object value
    )
  {getBaseDataObject().put(PdfName.V, new PdfString((String)value));}
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
        if(key.equals(PdfName.Ff))
          return new PdfInteger(0);

        return null;
      }
    }
  }
  // </protected>
  // </interface>
  // </dynamic>
  // </class>
}