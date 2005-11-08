/*
 * Copyright (C) 2005 Tom Carden (tom@somethingmodern.com)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *  
 */

package org.openstreetmap.util;
import java.util.StringTokenizer;
import java.util.Vector;

// minimal representation of OpenStreetMap line (node id -> node id, with uid)
public class Line {

  public Node a,b;
  private String name="";
  private String tags="";
  public long uid;
  public boolean nameChanged = false;

  public Line(Node a, Node b) {
    this(a,b,0,"");
  }
    
  public Line(Node a, Node b, long uid, String sTags) {
    if (a != null && b != null) {
      this.a=a; this.b=b;
    }
    if (a != null) {
      a.lines.addElement(this);
    }
    if (b != null) {
      b.lines.addElement(this);
    }
    this.uid=uid;
    splitTags(sTags);
  }
  
  public void reverse() {
    Node temp = a;
    a = b;
    b = temp;
  }
  
  // screen angle, if projected
  public float angle() {
    return (float)Math.atan2(b.y-a.y,b.x-a.x);
  }
  
  // pixel length, if projected
  public float length() {
    // TODO check != 0
    return a.distance(b);
  }

  // pixel distance, if projected  
  public float distance(Node c) {
    return distance(c.x,c.y);
  }

  // pixel distance, if projected  
  public float distance(float x, float y) {
    // project x/y onto line a->b
    // first find parameter (how far along a->b are we?
    float u = ( ((x-a.x)*(b.x-a.x)) + ((y-a.y)*(b.y-a.y)) ) / ((b.y-a.y)*(b.y-a.y)+(b.x-a.x)*(b.x-a.x));
    float d = 0.0f;
    if(u <= 0.0f) {
      d = a.distance(x,y);
    }
    else if (u >= 1.0f) {
      d = b.distance(x,y);    
    }
    else {
      // project x/y onto line a->b
      float px = a.x + (u * (b.x-a.x));
      float py = a.y + (u * (b.y-a.y));
      d = (float)Math.sqrt((x-px)*(x-px)+(y-py)*(y-py));
    }
    return d;
  }
  
  public boolean mouseOver(float mouseX, float mouseY, float strokeWeight) {
    return distance(mouseX,mouseY) < strokeWeight/2.0;
  } // mouseOver

  
  public String toString()
  {
    return "[Line " + uid + " from " + a + " to " + b + "]";

  } // toString

  public String getTags()
  {
    if( tags.equals(" ") ||  tags.equals("; ") )
    {
      return "name=" + name;
    }
    else
    {
      return "name=" + name + "; " + tags;
    }

  } // getTags

  public synchronized void setName(String sName)
  {
    name = sName;
  }

  public synchronized String getName()
  {
    return name;
  } // getName

  private void splitTags(String sTags)
  {
    StringTokenizer st = new StringTokenizer(sTags, ";");

    while( st.hasMoreTokens() )
    {
      String t = st.nextToken();
      t = t.trim();
      if(t.startsWith("name="))
      {
        this.name = t.substring(5);
      }
      else
      {
        this.tags = this.tags + t + "; ";
      }
    }

  } // splitTags
 
} // Line

