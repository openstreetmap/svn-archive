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

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import uk.co.wilson.xml.MinML2;

public class Osm2Csv extends MinML2 {

  PrintWriter nodes, segments, ways, waysegs, waytags;
  int pc = 1; // primitive count
  char section = '.';
  String id, lat, lon, from, to, timestamp;
  int seqid = 0; // sequence - index of segment within way 
  StringBuffer taglist = null;
  boolean doNodes = false;
  boolean doSegments = false;
  boolean doWays = false;
  boolean doWaySegs = false;
  boolean includeTags = false;
  boolean includeTimes = false;

  /**
   * @param filename The <code>planet.osm</code>-format file to source data from.
   * @param control Dictates which output files to generate. A string containing
   *   one or more case-sensitive characters, the presence of each of which
   *   ensures inclusion of a certain output:
   *   <ul>
   *   <li><code>'N'</code> - output nodes to "nodes.csv" in columns 'id,lat,lon'</li>
   *   <li><code>'S'</code> - output segments to "segments.csv" in columns 'id,from,to'</li>
   *   <li><code>'W'</code> - output ways to "ways.csv" in column 'id'</li>
   *   <li><code>'w'</code> - output way segments to "waysegs.csv" in columns 'wayid,segid,seqid'</li>
   *   <li><code>'i'</code> - also output last-modified times as 'timestamp' column</li>
   *   <li><code>'a'</code> - also output tags as 'taglist' column</li>
   *   </ul>
   */
  public Osm2Csv(String filename, String control) 
  {
    if (control == null) control = "";
    try 
    {
      if (contains(control, "N")) doNodes = true;
      if (contains(control, "S")) doSegments = true;
      if (contains(control, "W")) doWays = true;
      if (contains(control, "w")) doWaySegs = true;
      String colOptions = "";
      if (contains(control, "i")) {
        includeTimes = true; colOptions += ",timestamp";
      }
      if (contains(control, "a")) {
        includeTags = true; colOptions += ",taglist";
      }
      if (doNodes) {
        nodes = new PrintWriter(new BufferedWriter(new FileWriter("nodes.csv")));
        nodes.println("id,lat,lon" + colOptions);
        System.out.println("Writing nodes to 'nodes.csv'...");
      }
      if (doSegments) {
        segments = new PrintWriter(new BufferedWriter(new FileWriter("segments.csv")));
        segments.println("id,from,to" + colOptions);
        System.out.println("Writing segments to 'segments.csv'...");
      }
      if (doWays) {
        ways = new PrintWriter(new BufferedWriter(new FileWriter("ways.csv")));
        ways.println("id" + colOptions);
        System.out.println("Writing ways to 'ways.csv'...");
        if (includeTags) {
          waytags = new PrintWriter(new BufferedWriter(new FileWriter("waytags.csv")));
          waytags.println("id,k,v");
          System.out.println("Also writing way tags to 'waytags.csv'...");
        }
      }
      if (doWaySegs) {
        waysegs = new PrintWriter(new BufferedWriter(new FileWriter("waysegs.csv")));
        waysegs.println("wayid,segid,seqid");
        System.out.println("Writing way segments to 'waysegs.csv'...");
      }
      // TODO: accept zipped input, piped input, etc.
      if (doNodes || doSegments || doWays || doWaySegs) {
        if (includeTags) System.out.println("(including tags)");
        if (includeTimes) System.out.println("(including timestamps)");
      }
      else {
        System.out.println("NB: No output defined (any of 'NSWw' included) - parsing input file only.");
      }
      parse(new InputStreamReader(new BufferedInputStream(new FileInputStream(filename))));
    }
    catch (Exception e) 
    {
      e.printStackTrace(); 
    }
    finally {
      if (nodes != null) nodes.close();
      if (segments != null) segments.close();
      if (ways != null) ways.close();
      if (waysegs != null) waysegs.close();
      if (waytags != null) waytags.close();
    }
  }

  public void startElement(final String namespaceURI, 
                           final String localName,
                           final String qName,
                           final Attributes attributes) throws SAXException 
  {
    boolean primitive = false;
    if (includeTags && localName.equals("tag")) {
      if ((doNodes && section == 'n') || (doSegments && section == 's')|| (doWays && section == 'w')) {
        if (taglist == null) {
          taglist = new StringBuffer();
        }
        else {
          taglist.append(";");
        }
        String k = attributes.getValue("k");
        String v = attributes.getValue("v");
        taglist.append(k);
        taglist.append("=");
        taglist.append(v);
        
        if (doWays) { // extra output of way tags to waytags.csv
          waytags.print(id);
          waytags.print(",");
          waytags.print(k);
          waytags.print(",");
          waytags.println(v);
        }
      }
    }
    else if (localName.equals("node")) {
      if (doNodes) {
        id = attributes.getValue("id"); 
        lat = attributes.getValue("lat"); 
        lon = attributes.getValue("lon");
        if (includeTimes) timestamp = attributes.getValue("timestamp");
      }
      section = 'n'; primitive = true;
      pc++;
    }
    else if (localName.equals("segment")) {
      if (doSegments) {
        id = attributes.getValue("id"); 
        from = attributes.getValue("from"); 
        to = attributes.getValue("to");
        if (includeTimes) timestamp = attributes.getValue("timestamp");
      }
      section = 's'; primitive = true;
      pc++;
    }
    else if (localName.equals("way")) {
      id = attributes.getValue("id");
      if (includeTimes) timestamp = attributes.getValue("timestamp");
      seqid = 0;
      section = 'w'; primitive = true;
      pc++;
    }
    else if (doWaySegs && localName.equals("seg")) {
      waysegs.print(id);
      waysegs.print(",");
      waysegs.print(attributes.getValue("id"));
      waysegs.print(",");
      waysegs.println(seqid++);
    }
    if (primitive & pc % 10000 == 0) {
      if (pc % 1000000 == 0) {
        System.out.println(section);
      }
      else {
        System.out.print(section);
      }
    }
  }
  
  public void endElement(final String namespaceURI, 
      final String localName,
      final String qName) throws SAXException {
    PrintWriter pw = null;
    if (doNodes && localName.equals("node")) {
      nodes.print(id);
      nodes.print(","); 
      nodes.print(lat); 
      nodes.print(","); 
      nodes.print(lon);
      pw = nodes;
    }
    else if (doSegments && localName.equals("segment")) {
      segments.print(id);
      segments.print(",");
      segments.print(from);
      segments.print(",");
      segments.print(to);
      pw = segments;
    }
    else if (doWays && localName.equals("way")) {
      ways.print(id);
      pw = ways;
    }
    
    if (pw != null) {
      if (includeTimes) {
        pw.print(",");
        pw.print(timestamp);
      }
      if (includeTags) {
        pw.print(",");
        if (taglist != null) {
          pw.print(delimited(taglist.toString()));
          taglist = null;
        }
      }
      pw.println();
    }
  }

  public void endDocument() throws SAXException {
    System.out.println();
    System.out.println("Import file completed.");
  }

  /**
   * @param args Takes two parameters:
   * <ol>
   * <li>input path - filename of source planet.osm file.</li>
   * <li>control - string denoting which output (@see Osm2Csv()) to generate.</li>
   * </ol>
   */
  public static void main(String args[]) 
  {
    // TODO: also allow specifying output names, omit output if no name given
    // e.g. java org.openstreetmap.util.Osm2Csv planet.osm -n nodes.csv
    //      would only give the nodes
    // TODO: allow verbose output for segments, -v or something
    //      java org.openstreetmap.util.Osm2Csv planet.osm -v -s segments.csv 
    //      would give "id,fromlat,fromlon,tolat,tolon" not just "id,from,to"
    if (args.length == 2) {
      new Osm2Csv(args[0], args[1]);
    }
    else if (args.length == 1) {
      new Osm2Csv(args[0], "NS"); // default = Nodes, Segments (no tags or timestamps)
    }
    else {
      usage();
    }
  }

  private static void usage() {
    System.err.println("parameters: <inputfile> <control>");
    System.err.println("where: <inputfile> is path to source data file in planet.osm format");
    System.err.println("  and: <control> contains case-sensitive characters 'NSWwia' for Nodes, Segments, Ways, way segments, timestamps and tags respectively.");
    System.err.println("e.g. : data/planet.osm Wwa");
    System.err.println("generates Ways and way segments including tags from 'data/planet.osm'");
  }
  
  private String delimited(String raw) {
    if (contains(raw, "\"") || contains(raw, ",")) {
      return "\"" + raw.replaceAll("\"", "\"\"") + "\"";      
    }
    else {
      return raw;
    }
  }
  
  private boolean contains(String searched, String sought) {
	return searched.indexOf(sought) != -1;
  }
}


