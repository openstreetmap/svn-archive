/*
   Copyright (C) 2004 Stephen Coast (steve@fractalus.com)

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

 */

package org.openstreetmap.applet;

import java.lang.*;
import java.util.*;

import com.bbn.openmap.omGraphics.OMLine;


public class osmStreetSegment extends OMLine
{

  int nUid;


  public osmStreetSegment(
      float lat1,
      float lon1,
      float lat2,
      float lon2,
      int line_type,
      int uid)
  {

    super(lat1, lon1, lat2, lon2, line_type);

    nUid = uid;
    

  } // osmStreetSegment



  public int getUid()
  {
    return nUid;

  } // getUid
  
  


} // osmStreetSegment











