/*
 * Copyright (C) 2005 Tom Carden (tom@somethingmodern.com), Steve Coast (steve@asklater.com)
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
 * <li>Prompt for delete yes/no
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
 * <li>Draw streets instead of lines, once streets are computed (still edit lines though)
 * <li>Copious refactoring opportunities in the point/line/latlon classes once it all works
 * <li>use off screen images for picking nodes/lines (use uid as colour?)
 * </ul>
 * 
 * <p>APPLET BUGS:
 * 
 * <ul>
 * <li>Text on vertical lines is strange?
 * </ul>
 * 
 * <p>DONE:
 * 
 * <ul>
 * <li>Allow deletion of nodes/lines 
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

import org.openstreetmap.client.Adapter;
import org.openstreetmap.client.Tile;
import org.openstreetmap.util.Point;
import org.openstreetmap.util.Node;
import org.openstreetmap.util.Line;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Hashtable;

public class OSMApplet extends PApplet {

  Tile tiles;

  private static final int WINDOW_WIDTH = 700;
  private static final int WINDOW_HEIGHT = 500;

  int zoom;
  boolean shiftDown = false;

  int lastmX;
  int lastmY;

  /* set these for testing without needing to log in to the website - for deployment they should be set to null */
  String USERNAME = null;
  String PASSWORD = null;

  /* handles XML-RPC etc */
  public Adapter osm;

  /* converts from lat/lon into screen space */
  //Mercator projection;

  /* collection of OSMNodes (may or may not be projected into screen space) */
  public Hashtable nodes = new Hashtable();
  /* collection of OSMLines */
  public Hashtable lines = new Hashtable();
  /* Integer id -> OSMNode */
  //  Hashtable nodeMap = new Hashtable(); 

  /* image showing GPX tracks - TODO: vector of PImages? one per GPX file? */
  //  PImage gpxImage;

  /* width of line segments - TODO: modulate based on scale, and road type */
  float strokeWeight = 11.0f;

  /* for displaying new lines whilst drawing (between start and mouseX/Y) */
  Line tempLine = new Line(null,null);

  /* current line, for editing street names - 
   * TODO: 
   *   change to array of lines and apply text to all (save as a new street?) 
   *   track this in editmode, and make line.selected flag */
  Line selectedLine;

  /* current node, for moving nodes - TODO: track this in editmode, and make node.selected flag */
  Node selectedNode = null;

  /* selected start point when drawing lines */
  Node start = null;

  /* font for street names - 
   * TODO:
   *   create on the fly? (investigate standard available fonts)
   *   modulate based on scale, and road type? */
  PFont font;

  /* background image - TODO: layers of images from different mapservers? */
//  PImage img = null;

  /* URL for mapserver... will have bbx,width,height appended */
  String wmsURL = "http://www.openstreetmap.org/tile/0.1/wms?map=/usr/lib/cgi-bin/steve/wms.map&service=WMS&WMTVER=1.0.0&REQUEST=map&STYLES=&TRANSPARENT=TRUE&LAYERS=landsat,gpx"; 
  //"http://onearth.jpl.nasa.gov/wms.cgi?request=GetMap&layers=modis,global_mosaic&styles=&srs=EPSG:4326&format=image/jpeg";

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

  long lastmove;
  boolean moved = true;

  public void setup() {

    size(WINDOW_WIDTH, WINDOW_HEIGHT);
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

    modeManager.draw(); // make modeManager set up things

    // for centre lat/lon and scale (degrees per pixel)
    float clat, clon, sc;

    if (online) {
      
      if( param_float_exists("clat") ) {
        clat = parse_param_float("clat");  
      } else {
        clat = 51.526447f;
      }
  
      if( param_float_exists("clon") ) {
        clon = parse_param_float("clon");  
      } else {
        clon = -0.14746371f;
      }

      if( param_float_exists("scale") ) {
        sc = parse_param_float("scale");  
      } else {
        sc = 8.77914943209873e-06f;
      }

      if( param_float_exists("zoom") ) {
        zoom = parse_param_int("zoom");
        sc = 45f * (float)Math.pow(2f, -6 -zoom);
      }
    } else {
      // traditional OSM Regent's Park London default
      clat = 51.526447f;
      clon = -0.14746371f;
      zoom = 15;
      //      sc   = 8.77914943209873e-06f;
      sc = 45f * (float)Math.pow(2f, -6 -zoom);
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

    tiles = new Tile(this, wmsURL, clat, clon, WINDOW_WIDTH, WINDOW_HEIGHT, zoom);
    tiles.start();

    System.out.println(tiles);

    recalcStrokeWeight();

    System.out.println("Selected strokeWeight of " + strokeWeight );

    println("check webpage applet parameters for a user/pass");
    try {
      USERNAME = param("user");
      PASSWORD = param("pass");

      System.out.println("Got user/pass: " + USERNAME + "/" + PASSWORD);
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    if (USERNAME == null && PASSWORD == null) {
      println("check command line arguments for a user/pass");
      try {
        USERNAME = args[0];
        PASSWORD = args[1];
      
        System.out.println("Got user/pass: " + USERNAME + "/" + PASSWORD);
      }
      catch (Exception e2) {
        e2.printStackTrace();
      }
    }

    // try to connect to OSM
    osm = new Adapter(USERNAME,PASSWORD, lines, nodes);

    Thread dataFetcher = new Thread(new Runnable() {

      public void run() {

        osm.getNodesAndLines(tiles.getTopLeft(),tiles.getBotRight(), tiles);

        System.out.println("Got " + nodes.size() + " nodes and " + lines.size() + " lines.");

        ready = true;

        redraw();
      }
    });

    if (osm != null) {
      dataFetcher.start();
    }

    noLoop();
    redraw();
  } // setup

  
  private boolean param_float_exists(String sParamName)
  {
    try
    {
      float foo = Float.parseFloat(param(sParamName));
      return true;
    }
    catch(Exception e)
    {

    }
    return false;
  } // param_float_exists

  private float parse_param_float(String sParamName)
  {
    return Float.parseFloat(param(sParamName));
  } // parse_param_float

  private int parse_param_int(String sParamName)
  {
    return Integer.parseInt(param(sParamName));
  } // parse_param_float

  boolean gotGPX = false;

  public void draw() {

    tiles.draw();
    try{

    if (!ready) {
      cursor(WAIT);
    }
    else {
      cursor(ARROW);
    }

    noFill();
    strokeWeight(strokeWeight+2.0f);
    stroke(0);
    Enumeration e = lines.elements();
    while(e.hasMoreElements()){
      Line line = (Line)e.nextElement();
      //System.out.println("Doing line " + line.a.x + "," + line.a.y + " - " + line.b.x + "," + line.a.y);
      if(line.uid == 0)
      {
        stroke(0,80);
      }
      else
      {
        stroke(0);
      }
      line(line.a.x,line.a.y,line.b.x,line.b.y);
    }
    strokeWeight(strokeWeight);
    stroke(255);
    e = lines.elements();
    while(e.hasMoreElements()){
      Line line = (Line)e.nextElement();
      if(line.uid == 0)
      {
        stroke(255,80);
      }
      else
      {
        stroke(255);
      }

      line(line.a.x,line.a.y,line.b.x,line.b.y);
    }
    boolean gotOne = false;

    e = lines.elements();
    while(e.hasMoreElements()){
      Line line = (Line)e.nextElement();
      if (modeManager.currentMode == nameMode && !gotOne) {
        // highlight first line under mouse
        if (line.mouseOver(mouseX,mouseY,strokeWeight) && line.uid != 0) {
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
      tempLine.b = new Node(mouseX,mouseY,tiles);
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

    e = nodes.elements();
    while(e.hasMoreElements()){
      Node node = (Node)e.nextElement();
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
    textSize(strokeWeight + 4);
    textAlign(CENTER);

    e = lines.elements();
    while(e.hasMoreElements()){
      Line l = (Line)e.nextElement();
      if (l.getName() != null) {
        pushMatrix();
        if (l.a.x <= l.b.x) {
          translate(l.a.x,l.a.y);
          rotate(l.angle());
        }
        else {
          translate(l.b.x,l.b.y);
          rotate(PI+l.angle());      
        }
        text(l.getName(),l.length()/2.0f,4);
        popMatrix();
      }
    }

    // draw all buttons
    modeManager.draw();
    if (online) {
      status("lat: " + tiles.lat(mouseY) + ", lon: " + tiles.lon(mouseX));
    }


    }catch(NullPointerException npe)
    {
      println("caught null exception...");
    }


  }

  public void recalcStrokeWeight()
  {
    strokeWeight = max(0.010f/tiles.kilometersPerPixel(),2.0f); // 10m roads, but min 2px width
  } // recalcStrokeWeight

  public void mouseMoved() {
    if (ready) modeManager.mouseMoved();
  }

  public void mouseDragged() {
    if(shiftDown)
    {
      tiles.drag(lastmX - mouseX, mouseY - lastmY);
      lastmX = mouseX;
      lastmY = mouseY;
    }
    else
    {
      if(ready) 
      {
        modeManager.mouseDragged();
      }
    }
  } // mouseDragged

  public void mousePressed() {
    if(shiftDown)
    {
      lastmX = mouseX;
      lastmY = mouseY;
      return;
    }

    if (ready) modeManager.mousePressed();
  }

  public void mouseReleased() {
    if (ready && !shiftDown && !tiles.viewChanged) modeManager.mouseReleased();
  }

  public void keyPressed() {
    if( key == CODED )
    {
      if(keyCode == SHIFT)
      {
        shiftDown = true;
      }

    }
    if (ready) {
      switch(key) {
        case '[':
          lastmove = System.currentTimeMillis();
          tiles.zoomin();
          break;
        case ']':
          tiles.zoomout();
          break;

        case '+':
        case '=':
          strokeWeight += 1.0f;
          redraw();
          break;
        case '-':
        case '_':
          if (strokeWeight >= 2.0f) strokeWeight -= 1.0f;
          redraw();
          break;
      }
      if (modeManager.currentMode == nameMode) {
        //println(key == CODED);
        //println(java.lang.Character.getNumericValue(key));
        //println("key= \"" + key + "\"");
        //println("keyCode= \"" + keyCode + "\"");
        //println("BACKSPACE= \"" + BACKSPACE + "\"");
        //println("CODED= \"" + CODED + "\"");
        modeManager.keyPressed();
      }
    }
  }

  
  public void keyReleased() {
    if( key == CODED )
    {
      if(keyCode == SHIFT)
      {
        shiftDown = false;
        return;
      }

    }
  } // keyReleased

  // bit crufty - TODO tidy up and move into Point
  public boolean mouseOverPoint(Point p) {
    if (p.projected) {
      return sqrt(sq(p.x-mouseX)+sq(p.y-mouseY)) < strokeWeight; // /2.0f;  so you don't have to be directly on a node for it to light up
    }
    else {
      return false;
    }
  }

  public synchronized void reProject()
  {
    Enumeration e = nodes.elements();
    while(e.hasMoreElements())
    {
      Node n = (Node) e.nextElement();
      n.project(tiles);
    }

  } // reproject


  
  // bit crufty - TODO tidy up and move into draw()?
  public void drawPoint(Point p) {
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
      //System.out.println("draw() START in ModeManager: overButton="+overButton);

      overButton = false;

      pushMatrix();
      translate(x,y);
      for (int i = 0; i < getNumModes(); i++) {
        EditMode mode = getMode(i);
        strokeWeight(1);
        fill(200);
        mode.over = mouseX > x+(i*buttonWidth) && mouseX < buttonWidth+x+(i*buttonWidth) && mouseY < y+buttonHeight && mouseY > y;
        stroke(0);
        fill(mode.over || currentMode == mode ? 255 : 200);
        rect(0,0,buttonWidth,buttonHeight);
        mode.draw();
        overButton = overButton || mode.over;
        translate(buttonWidth,0);
      }
      popMatrix();

      //System.out.println("draw() END in ModeManager: overButton="+overButton);
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
      redraw();
    }
    public void mousePressed() {
      System.out.println("mousePressed in ModeManager with currentMode=" + currentMode + " and overButton=" + overButton);
      if (currentMode != null && !overButton) {
        currentMode.mousePressed();
        redraw();
      }
    }
    public void mouseMoved() {
      if (currentMode != null) {
        currentMode.mouseMoved();
        redraw();
      }
      else
      {
        if(mouseY < buttonHeight && mouseX < (x + getNumModes()*buttonWidth))
        {
          redraw();
        }
      }
    }
    public void mouseDragged() {
      if (currentMode != null) {
        currentMode.mouseDragged();
        redraw();
      }
    }
    public void keyPressed() {
      if (currentMode != null) {
        currentMode.keyPressed();
        redraw();
      }
    }
    public void keyReleased() {
      if (currentMode != null) {
        currentMode.keyReleased();
        redraw();
      }
    }

  }

  class NameMode extends EditMode {
    public void keyPressed() {
      System.out.println("got key " + key + " with keyCode " + keyCode + " and numeric val " + java.lang.Character.getNumericValue(key));
      if (selectedLine != null) {
        if(java.lang.Character.getNumericValue(key) == -1 && keyCode != 32 && keyCode != 222) { // should check for key == CODED but there's a Processing bug 
          if (keyCode == BACKSPACE && selectedLine.getName().length() > 0) {
            selectedLine.setName( selectedLine.getName().substring(0,selectedLine.getName().length()-1) );
            selectedLine.nameChanged = true;
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
          selectedLine.setName(selectedLine.getName() + key);
          selectedLine.nameChanged = true;
        }
      }
    }
    public void mouseReleased() {
      Line previousSelection = selectedLine;
      selectedLine = null;
      Enumeration e = lines.elements();
      while(e.hasMoreElements()){
        Line l = (Line)e.nextElement();
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
      text("A",1+buttonWidth*0.5f,5+(buttonHeight*0.5f));
    }
  } 

  class NodeMode extends EditMode {
    public void mouseReleased() {
      boolean overOne = false; // points can't overlap
      Enumeration e = nodes.elements();
      while(e.hasMoreElements()){
        Node p = (Node)e.nextElement();
        if(mouseOverPoint(p)) {
          overOne = true;
          redraw();
          break;
        }
      }    
      if (!overOne) {
        Node node = new Node(mouseX,mouseY,tiles);
        String tempKey = "temp_" + Math.random();
        if (osm != null) {
          osm.createNode(node, tempKey); 
        }
        nodes.put(tempKey, node);

        println(node);
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
      Enumeration e = nodes.elements();
      while(e.hasMoreElements()){
        Node p = (Node)e.nextElement();
        if(mouseOverPoint(p)) {
          start = p;
          break;
        }
      }    
    }
    public void mouseReleased() {
      boolean gotOne = false;

      Enumeration e = nodes.elements();
      while(e.hasMoreElements()){
        Node p = (Node)e.nextElement();
        if(mouseOverPoint(p)) {
          if (start != null) {
            Line line = new Line(start,p);
            String tempKey = "temp_" + Math.random();
            if (osm != null) {
              osm.createLine(line, tempKey); 
            }
            lines.put(tempKey,line);
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
      Enumeration e = nodes.elements();
      while(e.hasMoreElements()){
        Node p = (Node)e.nextElement();
        if(mouseOverPoint(p)) {
          selectedNode = p;
          println("selected: " + selectedNode);
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
        selectedNode.unproject(tiles);
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
        selectedNode.unproject(tiles);
        osm.moveNode(selectedNode);
        selectedNode = null;
      }
    }
  }


  class DeleteMode extends EditMode {
    public void mouseReleased() {
      boolean gotOne = false;
      Enumeration e = nodes.elements();
      while(e.hasMoreElements()){
        Node p = (Node)e.nextElement();
        if(mouseOverPoint(p) && p.uid != 0) {
          boolean del = true;
          // TODO prompt for delete
          if (del) {
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
        Enumeration ll = lines.elements();
        while(ll.hasMoreElements()){
          Line l = (Line)ll.nextElement();
          if (l.mouseOver(mouseX,mouseY,strokeWeight) && l.uid != 0) {
            boolean del = true;
            // TODO prompt for delete
            if (del) {
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

  static public void main(String args[]) {
    PApplet.main(new String[] { "--present", "--display=1", "org.openstreetmap.processing.OSMApplet" });
  } 

 // OSMApplet
}
