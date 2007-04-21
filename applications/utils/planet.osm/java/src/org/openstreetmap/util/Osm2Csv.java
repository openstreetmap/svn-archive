/*
 * Osm2Csv.java - convert osm format to csv files
 *     
 * Copyright (C) 2006 Tom Carden (tom@tom-carden.co.uk)
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

import uk.org.xml.sax.*;
import org.xml.sax.*;
import uk.co.wilson.xml.*;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class Osm2Csv extends MinML2 {

  PrintWriter nodes,segments;

  public Osm2Csv(String filename) 
  {
    try 
    {
      nodes = new PrintWriter(new BufferedWriter(new FileWriter("nodes.csv")));
      nodes.println("id,lat,lon");
      segments = new PrintWriter(new BufferedWriter(new FileWriter("segments.csv")));
      segments.println("id,from,to");
      // TODO: accept zipped input, piped input, etc.
      parse(new InputStreamReader(new BufferedInputStream(new FileInputStream(filename)), 1024));
      nodes.close();
      segments.close();
    }
    catch (Exception e) 
    {
      e.printStackTrace(); 
    }
  }

  public void startElement(final String namespaceURI, 
                           final String localName,
                           final String qName,
                           final Attributes attributes) throws SAXException 
  {
    if (localName.equals("node")) {
      nodes.println(attributes.getValue("id") + "," + 
                    attributes.getValue("lat") + "," + 
                    attributes.getValue("lon"));
    }
    else if (localName.equals("segment")) {
      segments.println(attributes.getValue("id") + "," + 
                       attributes.getValue("from") + "," + 
                       attributes.getValue("to"));
    }
    // TODO: parse tags too
  }
  
  public static void main(String args[]) 
  {
    // TODO: also allow specifying output names, omit output if no name given
    // e.g. java org.openstreetmap.util.Osm2Csv planet.osm -n nodes.csv
    //      would only give the nodes
    // TODO: allow verbose output for segments, -v or something
    //      java org.openstreetmap.util.Osm2Csv planet.osm -v -s segments.csv 
    //      would give "id,fromlat,fromlon,tolat,tolon" not just "id,from,to" 
    if (args.length > 0) 
    {
      new Osm2Csv(args[0]);
    }
    else 
    {
      System.err.println("no input file specified");
    }
  }

}


