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

package it.stefanochizzolini.clown.tokens;

/**
  Cross-reference table entry [PDF:1.6:3.4.3].

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @version 0.0.7
*/
public class XRefEntry
{
  // <class>
  // <classes>
  /**
    Cross-reference table entry usage [PDF:1.6:3.4.3].
  */
  public enum UsageEnum
  {
    Undefined,
    Free,
    InUse
  }
  // </classes>

  // <static>
  // <fields>
  /**
    Unreusable generation [PDF:1.6:3.4.3].
  */
  public static final int GenerationUnreusable = 65535;
  // </fields>
  // </static>

  // <dynamic>
  // <fields>
  private int number; // Object number.
  private int generation; // Object generation.
  private int offset; // Indirect-object offset (in-use entry) | Next free-object object number (free entry).
  private UsageEnum usage; // Entry usage.
  // </fields>

  // <constructors>
  public XRefEntry(
    int number,
    int generation,
    int offset,
    UsageEnum usage
    )
  {
    this.number = number;
    this.generation = generation;
    this.offset = offset;
    this.usage = usage;
  }
  // </constructors>

  // <interface>
  // <public>
  /**
    Gets the generation number.
  */
  public int getGeneration(
    )
  {return generation;}

  /**
    Gets the object number.
  */
  public int getNumber(
    )
  {return number;}

  /**
    Gets the byte offset of the object data within the serialized file.
  */
  public int getOffset(
    )
  {return offset;}

  /**
    Gets the usage state.
  */
  public UsageEnum getUsage(
    )
  {return usage;}

  /**
    @see #getUsage()
  */
  public void setUsage(
    UsageEnum value
    )
  {usage = value;}
  // </public>

  // <internal>
  void setGeneration(
    int value
    )
  {generation = value;}

  void setNumber(
    int value
    )
  {number = value;}

  void setOffset(
    int value
    )
  {offset = value;}
  // </internal>
  // </interface>
  // </dynamic>
  // </class>
}