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

/**
 * 
 * <p>TODO BEFORE RELEASE
 * 
 * <ul>
 * <li>Save line segment names back to OSM
 * <li>Stop hard-coding my username and password (just use params)
 * <li>Report useful errors, somehow.
 * <li>Implement a redirect or something so we can load NASA satellite data in the applet 
 * <li>Prompt for delete yes/no
 * <li>Make deployment scripts, compile with command line, etc.
 * 
 * <p>GENERAL TODO
 * 
 * <ul>
 * <li>Add an undo queue and a save button - ie don't commit changes live?
 * <li>Implement panning and zooming without reloading the applet (and recenter the projection and retransform the nodes) 
 * <li>Test inverse Mercator accuracy
 * <li>Allow show/hide for GPX tracks (right-click popup menu?)
 * <li>Colour lines according to age/recent edits 
 * <li>Draw street names, not line names, and respect curvy streets
 * <li>Draw continuous street chains as curves?
 * <li>Allow selection of multiple adjacent lines and apply same name to all of them, and then call mergeSegments (and mergeSegments should deal with this)
 * <li>Allow varying applet size 
 * <li>Allow cut and paste for street names 
 * <li>Test non-ascii characters in street names (may need a font change)
 * <li>Package standalone full screen app for demos
 * <li>Revalidate token after x minutes (9?)
 * <li>Draw streets instead of lines, once streets are computed (still edit lines though)
 * <li>Copious refactoring opportunities in the point/line/latlon classes once it all works
 * <li>use off screen images for picking nodes/lines (use uid as colour?)
 * </ul>
 * 
 * <p>APPLET BUGS:
 * 
 * <ul>
 * <li>Login prompt sucks. Seems broken at the moment. Do a nicer one with AWT myself instead of stealing a bad one.
 * <li>Remote images aren't shown due to security things
 * <li>CPU usage is obnoxious - lots of layer-caching optimisation to do once the interaction is right
 * <li>Text on vertical lines is strange
 * </ul>
 * 
 * <p>OSM/XML-RPC show-stoppers:
 * 
 * <ul>
 * <li>GPS points returned are limited to 50000.  Maybe we need to pre-render the images / put them into MapServer and cache them with Squid?
 * <li>Mopping up "off-screen" nodes to complete lines is hella-slow.  Need a getLines(lat,lon,lat,lon) to call first, or at least a getNodes(uids).
 * <li>No deleteLine or getStreets
 * </ul>
 * 
 * <p>DONE:
 * 
 * <ul>
 * <li>Allow deletion of nodes/lines 
 * <li>Log in to OSM, or use a given token
 * <li>Load and display nodes and lines using Mercator transform
 * <li>Eliminate depency on OpenMap
 * <li>Load NASA images
 * <li>Click a line and type directly into it
 * <li>Click to add nodes
 * <li>Click and drag to add lines
 * <li>Fetch GPX track and render them to images for display 
 * <li>Fetch existing street (segment) names from OpenStreetMap (e.g. look at Manhattan Data) 
 * <li>Implement reverse Mercator transform (x/y pixels to lat/lon) 
 * <li>Fix line/point intersection algorithm so lines aren't infinitely long 
 * <li>Allow moving of nodes 
 * <li>Save modifications back to OpenStreetMap
 * <li>Improve the buttons / optimise the different "modes" 
 * <li>Change print(ln)s to print(ln)s and tell print(ln) to use status() if online
 * <li>Fix status/println to use browser status bar
 * <li>GPL everything
 * <li>Choose line width and node size based on scale 
 * </ul>
 * 
 **/

package org.openstreetmap.processing; 
 
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PFont;

import org.openstreetmap.processing.util.OSMAdapter; 
import org.openstreetmap.processing.util.OSMPoint; 
import org.openstreetmap.processing.util.OSMNode; 
import org.openstreetmap.processing.util.OSMLine; 
import org.openstreetmap.processing.util.OSMMercator; 

import java.util.Vector;
import java.util.Iterator;
import java.util.Hashtable;

public class OSMApplet extends PApplet {

  /* set these for testing without needing to log in to the website - for deployment they should be set to null */
  String USERNAME = null;
  String PASSWORD = null;

  /* handles XML-RPC etc */
  OSMAdapter osm;

  /* converts from lat/lon into screen space */
  OSMMercator projection;

  /* collection of OSMNodes (may or may not be projected into screen space) */
  Vector nodes = new Vector();
  /* collection of OSMLines */
  Vector lines = new Vector();
  /* Integer id -> OSMNode */
  Hashtable nodeMap = new Hashtable(); 

  /* image showing GPX tracks - TODO: vector of PImages? one per GPX file? */
//  PImage gpxImage;

  /* width of line segments - TODO: modulate based on scale, and road type */
  float strokeWeight = 11.0f;

  /* for displaying new lines whilst drawing (between start and mouseX/Y) */
  OSMLine tempLine = new OSMLine(null,null);

  /* current line, for editing street names - 
   * TODO: 
   *   change to array of lines and apply text to all (save as a new street?) 
   *   track this in editmode, and make line.selected flag */
  OSMLine selectedLine;

  /* current node, for moving nodes - TODO: track this in editmode, and make node.selected flag */
  OSMNode selectedNode = null;

  /* selected start point when drawing lines */
  OSMNode start = null;

  /* font for street names - 
   * TODO:
   *   create on the fly? (investigate standard available fonts)
   *   modulate based on scale, and road type? */
  PFont font;

  /* background image - TODO: layers of images from different mapservers? */
  PImage img = null;

  /* URL for mapserver... will have bbx,width,height appended */
  String wmsURL = "http://onearth.jpl.nasa.gov/wms.cgi?request=GetMap&layers=modis,global_mosaic&styles=&srs=EPSG:4326&format=image/jpeg";

  /* modes - input is passed to the current mode, assigned by node manager */
  ModeManager modeManager;
  EditMode nodeMode     = new NodeMode();
  EditMode lineMode     = new LineMode();
  EditMode nameMode     = new NameMode();
  EditMode nodeMoveMode = new NodeMoveMode();
  EditMode deleteMode   = new DeleteMode();

  /* if !ready, a wait cursor is shown and input doesn't do anything 
     TODO: disable button mouseover highlighting when !ready */
  boolean ready = false;

  /* for revalidating token with OSM server */
  int validCount = 1;


  public void setup() {

    size(700,500);
    smooth();

    // this font should have all special characters - open to suggestions for changes though
    font = loadFont("LucidaSansUnicode-11.vlw");

    // initialise node manager and add buttons in desired order
    modeManager = new ModeManager();
    modeManager.addMode(nodeMode);
    modeManager.addMode(lineMode);
    modeManager.addMode(nameMode);
    modeManager.addMode(nodeMoveMode);
    modeManager.addMode(deleteMode);

    // for centre lat/lon and scale (degrees per pixel)
    float clat, clon, sc;

    if (online) {
      try {
        clat = Float.parseFloat(param("clat"));
        clon = Float.parseFloat(param("clon"));
      }
      catch (Exception e) {
        println(e.toString());
        e.printStackTrace();
        // traditional OSM Regent's Park London default
        clat = 51.526447f;
        clon = -0.14746371f;
      }
      try {
        sc   = Float.parseFloat(param("scale"));
      }
      catch (Exception e) {
        println(e.toString());
        e.printStackTrace();
        sc   = 0.0001f;
      }
    }
    else {
    
      // traditional OSM Regent's Park London default
      clat = 51.526447f;
      clon = -0.14746371f;
      sc   = 0.0001f;

      // Manhattan for testing street names
      //clat = 40.7621;
      //clon = -73.983765;
      //sc   = 0.0003;

      // slightly empty bit of London, for live testing...
      //clat = 51.53681622214006;
      //clon = -0.11829704333333334;
      //sc  = 6.666666666666667E-5;

      // Grenoble?
      //clat = 45.186; 
      //clon = 5.733;
      //sc = 0.0003;
    
    }

    if (online) {
      try {
        String wmsURLfromParam = param("wmsurl");
        if (wmsURLfromParam != null) {
          if (!wmsURLfromParam.equals("")) {
            wmsURL = wmsURLfromParam;
            if (wmsURL.indexOf("http://") < 0) {
              wmsURL = "http://" + wmsURL;
            }
          }
        }
      }
      catch (Exception e) {
        println(e.toString());
        e.printStackTrace();
      }
    }

    // initialise projection at given centre and scale
    // TODO - consider fixing scale to be one of N pre-determined values  
    //      - or allow "zoom level" as well as scale
    projection = new OSMMercator(clat,clon,sc,width,height);

    strokeWeight = max((float)(0.005f/projection.kilometersPerPixel()),1.0f); // 5m roads, but min 1px width

    OSMPoint tl = projection.getTopLeft();
    OSMPoint br = projection.getBottomRight();

    if (wmsURL.indexOf("?") > 0) {
      wmsURL += "&bbox="+tl.lon+","+br.lat+","+br.lon+","+tl.lat+"&width="+width+"&height="+height;
    }
    else {
      wmsURL += "?bbox="+tl.lon+","+br.lat+","+br.lon+","+tl.lat+"&width="+width+"&height="+height;
    }

    Thread imageFetcher = new Thread(new Runnable() {
      public void run() {
        try {
          print("loading: " + wmsURL);
          img = loadImage(wmsURL);
          if (img == null || img.width == 0 || height == 0) {
            throw new Exception("bad image from: " + wmsURL);
          }
        }
        catch (Exception e) {
          img = null;
          e.printStackTrace();
        }
      }
    }
    );
    imageFetcher.start();

    String token = null;

    // check webpage applet parameters for a token
    try {
      token = param("token");
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    // check webpage applet parameters for a user/pass
    try {
      USERNAME = param("user");
      PASSWORD = param("pass");
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    // try to connect to OSM
    osm = new OSMAdapter(lines,nodes,token,USERNAME,PASSWORD);

    Thread dataFetcher = new Thread(new Runnable() {

      public void run() {

        print("getting nodes... ");
        Vector osmNodes = osm.getNodes(projection.getTopLeft(),projection.getBottomRight());
        for (int i = 0; i < osmNodes.size(); i++) {
          if (i%max(1,osmNodes.size()/100) == 0) print("#"); 
          OSMNode node = (OSMNode)osmNodes.elementAt(i);
          node.project(projection);
          nodes.addElement(node);
          nodeMap.put(new Integer(node.uid),node);
        }
        println("got " + nodes.size() + " nodes, getting lines...");

        Vector osmLines = osm.getLines(osm.getIDs(osmNodes));
        Vector incompleteLines = new Vector();
        Vector nodesToFetch = new Vector();
        print("parsing lines");
        for (int i = 0; i < osmLines.size(); i++) {
          if (i%max(1,osmLines.size()/100) == 0) print("#"); 
          Vector linedata = (Vector)osmLines.elementAt(i);
          //println(linedata);
          Integer uid = (Integer)linedata.elementAt(0);
          Integer a = (Integer)linedata.elementAt(1);
          Integer b = (Integer)linedata.elementAt(2);
          String name = (String)linedata.elementAt(3);
          OSMNode pa = (OSMNode)nodeMap.get(a);
          OSMNode pb = (OSMNode)nodeMap.get(b);
          if (pa != null && pb != null) {
            OSMLine osmline = new OSMLine(pa,pb,uid.intValue());
            osmline.name = name;
            lines.addElement(osmline);
          }
          else {
            if (pa == null) {
              if (a != null) {
                nodesToFetch.addElement(a);
              }
              else {
                println("a is null for line " + uid);
              }
            }
            if (pb == null) {
              if (b != null) {
                nodesToFetch.addElement(b);
              }
              else {
                println("b is null for line " + uid);
              }
            }
            incompleteLines.addElement(linedata);
          }
        }
        println(lines.size() + " lines (" + incompleteLines.size() + " incomplete lines, " + nodesToFetch.size() + " nodes to fetch)");

        int nodesSizeBeforeFetch = nodes.size();
        for (int i = 0; i < nodesToFetch.size(); i++) {
          Integer id = (Integer)nodesToFetch.elementAt(i);
          OSMNode node = osm.getNode(id);
          if (node != null) {
            node.project(projection);
            nodes.addElement(node);
            nodeMap.put(new Integer(node.uid),node);
          }
          else {
            println("problem fetching node " + i + " with id " + id);
          }
        }
        println(nodes.size()-nodesSizeBeforeFetch + " extra nodes fetched, completing incomplete lines...");

        int linesSizeBeforeFetch = lines.size();
        for (int i = 0; i < incompleteLines.size(); i++) {
          Vector linedata = (Vector)incompleteLines.elementAt(i);
          Integer uid = (Integer)linedata.elementAt(0);
          Integer a = (Integer)linedata.elementAt(1);
          Integer b = (Integer)linedata.elementAt(2);
          String name = (String)linedata.elementAt(3);
          OSMNode pa = (OSMNode)nodeMap.get(a);
          OSMNode pb = (OSMNode)nodeMap.get(b);
          OSMLine line = new OSMLine(pa,pb,uid.intValue());
          line.name = name;
          lines.addElement(line);
        }
        println(lines.size()-linesSizeBeforeFetch + " extra lines completed");

        // could be heavy for lots of lines - perhap start in another thread?
        // or automate on the server and use getStreets like Steve originally planned?
        //      mergeSegments();

        ready = true;

/*
  
   below grabs gpx points but thats in the image now

   
        print("getting gpx points... ");
        Vector osmPoints = osm.getPoints(projection.getTopLeft(),projection.getBottomRight());
        println(osmPoints.size() + " points received");
        print("plotting points: ");
        gpxImage = new PImage(width,height);
        gpxImage.loadPixels();
        for (int i = 0; i < gpxImage.pixels.length; i++) {
          gpxImage.pixels[i] = 0xffffffff;
        }
        for (int i = 0; i < osmPoints.size(); i++) {
          if (i%max(1,osmPoints.size()/100) == 0) print("#"); 
          OSMPoint osmpoint = (OSMPoint)osmPoints.elementAt(i);
          osmpoint.project(projection);
          gpxImage.set((int)osmpoint.x,(int)osmpoint.y,0xffff0000);
        }
        gpxImage.updatePixels();
        osmPoints.clear();
        osmPoints = null;
        println("done drawing gpx points");

        gotGPX = true;
*/
      }

    }
    );

    if (osm != null) {
      dataFetcher.start();
    }

  }

  boolean gotGPX = false;

  public void draw() {

    background(200);

    if (!ready) {
      cursor(WAIT);
    }
    else {
      cursor(ARROW);
    }

    // draw background satellite image
    if (img != null) {
      image(img,0,0);
    }

    /*
    // draw GPX tracks image
    if (gotGPX && gpxImage != null) {
      blend(gpxImage,0,0,width,height,0,0,width,height,DARKEST);
      //    image(tracks,0,0);
    }
*/
    noFill();
    strokeWeight(strokeWeight+2.0f);
    stroke(0);
    for (int i = 0; i < lines.size(); i++) {
      OSMLine line = (OSMLine)lines.elementAt(i);
      line(line.a.x,line.a.y,line.b.x,line.b.y);
    }
    strokeWeight(strokeWeight);
    stroke(255);
    for (int i = 0; i < lines.size(); i++) {
      OSMLine line = (OSMLine)lines.elementAt(i);
      line(line.a.x,line.a.y,line.b.x,line.b.y);
    }
    boolean gotOne = false;
    for (int i = 0; i < lines.size(); i++) {
      OSMLine line = (OSMLine)lines.elementAt(i);
      if (modeManager.currentMode == nameMode && !gotOne) {
        // highlight first line under mouse
        if (line.mouseOver(mouseX,mouseY,strokeWeight)) {
          strokeWeight(strokeWeight);
          stroke(0xffffff80);
          line(line.a.x,line.a.y,line.b.x,line.b.y);
          gotOne = true;
        }
      }
    }

    // draw temp line
    if (start != null) {
      tempLine.a = start;
      tempLine.b = new OSMNode(mouseX,mouseY,projection);
      stroke(0,80);
      strokeWeight(strokeWeight+2);
      line(tempLine.a.x,tempLine.a.y,tempLine.b.x,tempLine.b.y);
      stroke(255,80);
      strokeWeight(strokeWeight);
      line(tempLine.a.x,tempLine.a.y,tempLine.b.x,tempLine.b.y);
    }

    // draw selected line
    stroke(255,0,0,80);
    strokeWeight(strokeWeight);
    if (selectedLine != null) {
      line(selectedLine.a.x,selectedLine.a.y,selectedLine.b.x,selectedLine.b.y);
    }

    // draw nodes
    noStroke();
    ellipseMode(CENTER);
    for (int i = 0; i < nodes.size(); i++) {
      OSMNode node = (OSMNode)nodes.elementAt(i);
      if (modeManager.currentMode == lineMode && mouseOverPoint(node)) {
        fill(0xffff0000);
      }
      else if (modeManager.currentMode == nodeMoveMode) {
        if (node == selectedNode) {
          fill(0xff00ff00);    
        }
        else if (mouseOverPoint(node)) {
          fill(0xffff0000);
        }
        else {
          fill(0xff000000);
        }
      }
      else if (modeManager.currentMode == deleteMode) {
        if (mouseOverPoint(node)) {
          fill(0xffff0000);
        }
        else {
          fill(0xff000000);
        }
      }
      else if(node == tempLine.a || node == tempLine.b) {
        fill(0xff000000);
      }
      else if (node.lines.size() > 0) {
        fill(0xffffffff);
      }
      else {
        fill(0xff000000);
      }
      drawPoint(node);
    }

    // draw street segment names
    fill(0);
    textFont(font);
    textSize(strokeWeight);
    textAlign(CENTER);
    for (int i = 0; i < lines.size(); i++) {
      OSMLine l = (OSMLine)lines.elementAt(i);
      if (l.name != null) {
        pushMatrix();
        if (l.a.x <= l.b.x) {
          translate(l.a.x,l.a.y);
          rotate(l.angle());
        }
        else {
          translate(l.b.x,l.b.y);
          rotate(PI+l.angle());      
        }
        text(l.name,l.length()/2.0f,4);
        popMatrix();
      }
    }

    // draw all buttons
    modeManager.draw();

    if (millis() > validCount * 9 * 60 * 1000) {
      osm.revalidateToken();
      validCount++;
    }

    if (online) {
      status("lat: " + projection.lat(mouseY) + ", lon: " + projection.lon(mouseX));
    }

  }

  public void mouseMoved() {
    if (ready) modeManager.mouseMoved();
  }

  public void mouseDragged() {
    if (ready) modeManager.mouseDragged();
  }

  public void mousePressed() {
    if (ready) modeManager.mousePressed();
  }

  public void mouseReleased() {
    if (ready) modeManager.mouseReleased();
  }

  public void keyPressed() {
    if (ready) {
      modeManager.keyPressed();
      switch(key) {
        case '+':
        case '=':
          strokeWeight += 1.0f;
          break;
        case '-':
        case '_':
          if (strokeWeight >= 2.0f) strokeWeight -= 1.0f;
          break;
      }
    }
  }

  // bit crufty - TODO tidy up and move into OSMPoint
  public boolean mouseOverPoint(OSMPoint p) {
    if (p.projected) {
      return sqrt(sq(p.x-mouseX)+sq(p.y-mouseY)) < strokeWeight/2.0f;
    }
    else {
      return false;
    }
  }

  // bit crufty - TODO tidy up and move into draw()?
  public void drawPoint(OSMPoint p) {
    if (p.projected) {
      ellipseMode(CENTER);
      ellipse(p.x,p.y,strokeWeight-1,strokeWeight-1);
    }
  }

  ////////////////////////////////////// BUTTON STUFF ////////////////////////////////////////////



  float buttonWidth = 15.0f;
  float buttonHeight = 15.0f;
  // TODO PFont buttonFont; // for tool-tips

  class EditMode {
    boolean over = false;

    public void mouseReleased() {}
    public void mousePressed() {}
    public void mouseMoved() {}
    public void mouseDragged() {}
    public void keyPressed() {}
    public void keyReleased() {}
    public void draw() {}
    public void set() {}
    public void unset() {}
  }


  class ModeManager {

    Vector modes;
    boolean overButton;
    EditMode currentMode;
    int x,y;

    ModeManager() {
      modes = new Vector();
      overButton = false;  
      x = 5;
      y = 5;
    }

    public void addMode(EditMode mode) {
      modes.addElement(mode);
    }
    public EditMode getMode(int i) {
      return (EditMode)modes.elementAt(i);
    }
    public int getNumModes() {
      return modes.size();
    }

    public void draw() {

      overButton = false;

      pushMatrix();
      translate(x,y);
      for (int i = 0; i < getNumModes(); i++) {
        EditMode mode = getMode(i);
        strokeWeight(1);
        fill(200);
        mode.over = mouseX > x+(i*buttonWidth) && mouseX < buttonWidth+x+(i*buttonWidth) && mouseY < y+buttonHeight && mouseY > y;
        stroke(mode.over || currentMode == mode ? 255 : 0);
        rect(0,0,buttonWidth,buttonHeight);
        mode.draw();
        overButton = overButton || mode.over;
        translate(buttonWidth,0);
      }
      popMatrix();

    }

    public void mouseReleased() {
      for (int i = 0; i < getNumModes(); i++) {
        EditMode mode = getMode(i);
        if (mode.over) {
          if (currentMode != null) {
            currentMode.unset();
          }
          currentMode = mode;
          currentMode.set();
          break;
        }
      }
      if (currentMode != null && !overButton) {
        currentMode.mouseReleased();
      }
    }
    public void mousePressed() {
      if (currentMode != null && !overButton) {
        currentMode.mousePressed();
      }
    }
    public void mouseMoved() {
      if (currentMode != null) {
        currentMode.mouseMoved();
      }
    }
    public void mouseDragged() {
      if (currentMode != null) {
        currentMode.mouseDragged();
      }
    }
    public void keyPressed() {
      if (currentMode != null) {
        currentMode.keyPressed();
      }
    }
    public void keyReleased() {
      if (currentMode != null) {
        currentMode.keyReleased();
      }
    }

  }

  class NameMode extends EditMode {
    public void keyPressed() {
      if (selectedLine != null) {
        if(key == CODED) { 
          if (keyCode == BACKSPACE) {
            if (selectedLine.name.length() > 0) {
              selectedLine.name = selectedLine.name.substring(0,selectedLine.name.length()-1);
              selectedLine.nameChanged = true;
            }
          }
          else if (keyCode == ENTER) {
            if (selectedLine.nameChanged) {
              if (osm != null) {
                osm.updateLineName(selectedLine);
              }
            }
            selectedLine = null;
          }
        }
        else {
          selectedLine.name += key;
          selectedLine.nameChanged = true;
        }
      }
    }
    public void mouseReleased() {
      OSMLine previousSelection = selectedLine;
      selectedLine = null;
      for (int i = 0; i < lines.size(); i++) {
        OSMLine l = (OSMLine)lines.elementAt(i);
        if (l.mouseOver(mouseX,mouseY,strokeWeight)) {
          selectedLine = l;
          break;
        }
      }
      if (previousSelection != null && previousSelection != selectedLine) {
        if (previousSelection.nameChanged) {
          if (osm != null) {
            osm.updateLineName(previousSelection);
          }
        }
        selectedLine = null;
      }
    }
    public void draw() {
      fill(0);
      textFont(font);
      textSize(11);
      textAlign(CENTER);
      text("A",buttonWidth*0.5f,5+(buttonHeight*0.5f));
    }
  } 

  class NodeMode extends EditMode {
    public void mouseReleased() {
      boolean overOne = false; // points can't overlap
      for (int i = 0; i < nodes.size(); i++) {
        OSMNode p = (OSMNode)nodes.elementAt(i);
        if(mouseOverPoint(p)) {
          overOne = true;
          break;
        }
      }    
      if (!overOne) {
        OSMNode node = new OSMNode(mouseX,mouseY,projection); 
        if (osm != null) {
          osm.createNode(node); 
        }
        nodes.addElement(node);
      }
    }
    public void draw() {
      fill(0);
      noStroke();
      ellipseMode(CENTER);
      ellipse(buttonWidth/2.0f,buttonHeight/2.0f,5,5);
    }
  }


  class LineMode extends EditMode {
    public void mousePressed() {
      for (int i = 0; i < nodes.size(); i++) {
        OSMNode p = (OSMNode)nodes.elementAt(i);
        if(mouseOverPoint(p)) {
          start = p;
          break;
        }
      }    
    }
    public void mouseReleased() {
      boolean gotOne = false;
      for (int i = 0; i < nodes.size(); i++) {
        OSMNode p = (OSMNode)nodes.elementAt(i);
        if(mouseOverPoint(p)) {
          if (start != null) {
            OSMLine line = new OSMLine(start,p);
            if (osm != null) {
              osm.createLine(line); 
              // TODO assign ID, asynchronously?
            }
            lines.addElement(line);
          }
          gotOne = true;
          break;
        }
      }
      start = null;
      tempLine.a = null;
      tempLine.b = null;
    }
    public void draw() {
      noFill();
      stroke(0);
      strokeWeight(5);
      line(2,2,buttonWidth-2,buttonHeight-2);
      stroke(255);
      strokeWeight(4);
      line(2,2,buttonWidth-2,buttonHeight-2);
    }
  }


  class NodeMoveMode extends EditMode {
    float lastOffsetX = 0.0f;
    float lastOffsetY = 0.0f;
    public void mousePressed() {
      println("nousePressed in node move mode");
      for (int i = 0; i < nodes.size(); i++) {
        OSMNode p = (OSMNode)nodes.elementAt(i);
        if(mouseOverPoint(p)) {
          selectedNode = p;
          //println("selected: " + selectedNode);
          lastOffsetX = selectedNode.x - mouseX;
          lastOffsetY = selectedNode.y - mouseY;
          break;
        }
      }
      println("selected: " + selectedNode);
    }
    public void mouseDragged() {
      if (selectedNode != null) {
        selectedNode.x = mouseX + lastOffsetX;
        selectedNode.y = mouseY + lastOffsetY;
        //println("node moved:" + selectedNode.x + " " + selectedNode.y);
      }
      else {
        println("no selectedNode");
      }
    }
    public void mouseReleased() {
      if (selectedNode != null) {
        selectedNode.unproject(projection);
        osm.moveNode(selectedNode); 
        selectedNode = null;
      }
      else {
        println("no selectedNode on mouse release");
      }
    }
    public void draw() {
      stroke(0);
      noFill();
      line(buttonWidth/2.0f,buttonHeight*0.2f,buttonWidth/2.0f,buttonHeight*0.8f);
      line(buttonWidth*0.2f,buttonHeight/2.0f,buttonWidth*0.8f,buttonHeight/2.0f);
    }
    public void unset() {
      if (selectedNode != null) {
        selectedNode.unproject(projection);
        osm.moveNode(selectedNode);
        selectedNode = null;
      }
    }
  }


  class DeleteMode extends EditMode {
    public void mouseReleased() {
      boolean gotOne = false;
      for (int i = 0; i < nodes.size(); i++) {
        OSMNode p = (OSMNode)nodes.elementAt(i);
        if(mouseOverPoint(p)) {
          boolean delete = true;
          // TODO prompt for delete
          if (delete) {
            println("deleting " + p);
            osm.deleteNode(p);
          }
          else {
            println("not deleting " + p);
          }
          gotOne = true;
          break;
        }
      }
      if (!gotOne) {
        for (int i = 0; i < lines.size(); i++) {
          OSMLine l = (OSMLine)lines.elementAt(i);
          if (l.mouseOver(mouseX,mouseY,strokeWeight)) {
            boolean delete = true;
            // TODO prompt for delete
            if (delete) {
              println("deleting " + l);
              osm.deleteLine(l);
            }
            else {
              println("not deleting " + l);
            }
            break;
          }
        }
      }
    }
    public void draw() {
      stroke(0);
      noFill();
      line(buttonWidth*0.2f,buttonHeight*0.2f,buttonWidth*0.8f,buttonHeight*0.8f);
      line(buttonWidth*0.8f,buttonHeight*0.2f,buttonWidth*0.2f,buttonHeight*0.8f);
    }
  }

  /////////////////////////////////////// END BUTTON STUFF ///////////////////////////////////////


} // OSMApplet

