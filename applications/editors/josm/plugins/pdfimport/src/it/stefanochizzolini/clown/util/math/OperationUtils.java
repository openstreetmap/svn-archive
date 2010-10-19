/*
  Copyright 2009 Stefano Chizzolini. http://clown.stefanochizzolini.it

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

package it.stefanochizzolini.clown.util.math;

/**
  Specialized math operations.

  @author Stefano Chizzolini (http://www.stefanochizzolini.it)
  @since 0.0.8
  @version 0.0.8
*/
public final class OperationUtils
{
  // <class>
  // <static>
  /**
    Big-endian comparison.
  */
  public static int compare(
    byte[] data1,
    byte[] data2
    )
  {
    for(
      int index = 0,
        length = data1.length;
      index < length;
      index++
      )
    {
      switch((int)Math.signum((data1[index] & 0xff)-(data2[index] & 0xff)))
      {
        case -1:
          return -1;
        case 1:
          return 1;
      }
    }
    return 0;
  }

  /**
    Big-endian increment.
  */
  public static void increment(
    byte[] data
    )
  {increment(data, data.length-1);}

  /**
    Big-endian increment.
  */
  public static void increment(
    byte[] data,
    int position
    )
  {
    if((data[position] & 0xff) == 255)
    {
      data[position] = 0;
      increment(data, position-1);
    }
    else
    {data[position]++;}
  }
  // </static>
  // </class>
}