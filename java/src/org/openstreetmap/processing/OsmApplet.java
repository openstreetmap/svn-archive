/*
 * Copyright (C) 2005 
 * Tom Carden (tom@somethingmodern.com)
 * Steve Coast (steve@asklater.com)
 * Immanuel Scholz (immanuel.scholz@gmx.de)
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

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Math;
import java.lang.Character;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import netscape.javascript.JSObject;

import org.openstreetmap.client.Adapter;
import org.openstreetmap.client.CommandManager;
import org.openstreetmap.client.ServerCommand;
import org.openstreetmap.client.Tile;
import org.openstreetmap.util.Line;
import org.openstreetmap.util.LineOnlyId;
import org.openstreetmap.util.Node;
import org.openstreetmap.util.OsmPrimitive;
import org.openstreetmap.util.Point;
import org.openstreetmap.util.Way;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;

public class OsmApplet extends PApplet {
  private String copyright = "";

	public Tile tiles;

	private JSObject js;

	/**
	 * Current zoom level
	 */
	private int zoom = 15;

	private int windowHeight = 500;
	private int windowWidth = 700;
	
	/**
	 * Whether the left mouse button is pressed down.
	 */
	private boolean mouseDown = false;

	/**
	 * The username given as an applet parameter. To set this in test environments,
	 * pass user=(here your email) as parameter to the applet runner.
	 */
	private String userName = null;
	/**
	 * The password in cleartext given as an applet parameter. 
	 * To set this in test environments, set pass=(here your password) as parameter 
	 * to the applet runner. Do not encode the password (cleartext only).
	 */
	private String password = null;

	/**
	 * Handles most communication with the server.
	 */
	public Adapter osm;

	/**
	 * Map of OSMNodes (may or may not be projected into screen space).
	 * Type: String -> Node
	 */
	public Map nodes = new Hashtable();

	/**
	 * Collection of OSMLines
	 * Type: String -> Line
	 */
	public Map lines = new Hashtable();
	
	/**
	 * Collection of OSM ways
	 * Type: String -> Way
	 */
	public Map ways = new Hashtable();


  PImage YahooLogo = loadImage("/data/yahoo.png");

	/* image showing GPX tracks - TODO: vector of PImages? one per GPX file? */
	// private PImage gpxImage;

	/**
	 * Width of line segments
	 * TODO: modulate based on scale, and road type
	 */
	float strokeWeight = 11.0f;

	/**
	 * For displaying new lines whilst drawing (between start and mouseX/Y)
	 */
	Line tempLine = new Line(null, null);

	/**
	 * Ids of current selected lines, for creating streets
	 * Type: String (key of a line)
	 */
	public List selectedLine = new ArrayList();
	/**
	 * Extra bold highlighted for marking up lines on a way during creation.
	 */
	public String extraHighlightedLine = null;

	/*
	 * current node, for moving nodes - TODO: track this in editmode, and make
	 * node.selected flag
	 */
	Node selectedNode = null;

	/* selected start point when drawing lines */
	Node start = null;

	/*
	 * font for street names - TODO: create on the fly? (investigate standard
	 * available fonts) modulate based on scale, and road type?
	 */
	PFont font;

	/* background image - TODO: layers of images from different mapservers? */
	// PImage img = null;
	/* URL for mapserver... will have bbx,width,height appended */
	String wmsURL = "http://www.openstreetmap.org/tile/0.2/gpx?;http://www.openstreetmap.org/api/wms/0.2/landsat/?request=GetMap&layers=modis,global_mosaic&styles=&srs=EPSG:4326&FORMAT=image/jpeg";

	// "http://onearth.jpl.nasa.gov/wms.cgi?request=GetMap&layers=modis,global_mosaic&styles=&srs=EPSG:4326&format=image/jpeg";

	String apiURL = "http://www.openstreetmap.org/api/0.3/";

	/* modes - input is passed to the current mode, assigned by node manager */
	ModeManager modeManager;

	EditMode nodeMode = new NodeMode(this);
	EditMode lineMode = new LineMode(this);
	EditMode wayMode = new WayMode(this);
	EditMode nameMode = new PropertiesMode(this);
	EditMode nodeMoveMode = new NodeMoveMode(this);
	EditMode deleteMode = new DeleteMode(this);
	EditMode moveMode = new MoveMode(this);
	EditMode zoomoutMode = new ZoomOutMode(this);
	EditMode zoominMode = new ZoomInMode(this);

	/*
	 * if !ready, a wait cursor is shown and input doesn't do anything TODO:
	 * disable button mouseover highlighting when !ready
	 */
	boolean ready = false;

	long lastmove;

	boolean moved = true;

	boolean gotGPX = false;

  public void setCopyright(String c) {
    copyright = c;
  }
  
	public void setup() {


		// for centre lat/lon and scale (degrees per pixel)
		float clat = 51.526447f, clon = -0.14746371f;
		//float clat = -37.526763645918486f, clon = 144.14729439306237f;
		zoom = 15;

		if (online) {
			if (param_float_exists("windowHeight"))
				windowHeight = parse_param_int("windowHeight");
			if (param_float_exists("windowWidth"))
				windowWidth = parse_param_int("windowWidth");
			if (param_float_exists("clat"))
				clat = parse_param_float("clat");
			if (param_float_exists("clon"))
				clon = parse_param_float("clon");
			if (param_float_exists("zoom"))
				zoom = parse_param_int("zoom");

			try {
				String wmsURLfromParam = param("wmsurl");
				if (wmsURLfromParam != null && !wmsURLfromParam.equals("")) {
					wmsURL = wmsURLfromParam;
					if (wmsURL.indexOf("http://") < 0)
						wmsURL = "http://" + wmsURL;
				}
			} catch (Exception e) {
				println(e.toString());
				e.printStackTrace();
			}

			try {
				String apiURLfromParam = param("apiurl");
				if (apiURLfromParam != null) {
					if (!apiURLfromParam.equals("")) {
						apiURL = apiURLfromParam;
						if (apiURL.indexOf("http://") < 0) {
							apiURL = "http://" + apiURL;
						}
					}
				}
			} catch (Exception e) {
				println(e.toString());
				e.printStackTrace();
			}

			js = JSObject.getWindow(this);
		}

		println("check webpage applet parameters for a user/pass");
		try {
			userName = param("user");
			password = param("pass");
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (args != null) {
			println("Parsing command line arguments");
			for (int i = 0; i < args.length; ++i) {
				if (args[i].startsWith("--user=")) {
					userName = args[i].substring(args[i].indexOf('=')+1);
				}
				if (args[i].startsWith("--pass=")) {
					password = args[i].substring(args[i].indexOf('=')+1);
				}
				if (args[i].startsWith("--clon=")) {
					clon = Float.parseFloat(args[i].substring(args[i].indexOf('=')+1));
				}
				if (args[i].startsWith("--clat=")) {
					clat = Float.parseFloat(args[i].substring(args[i].indexOf('=')+1));
				}
				if (args[i].startsWith("--zoom=")) {
					clat = Integer.parseInt(args[i].substring(args[i].indexOf('=')+1));
				}
			}
		}

		System.out.println("Got userName: " + userName);
		System.out.println("Got password: " + password);
		System.out.println("Got clon: " + clon);
		System.out.println("Got clat: " + clat);
		System.out.println("Got zoom: " + zoom);
		System.out.println("Got windowHeight: " + windowHeight);
		System.out.println("Got windowWidth: " + windowWidth);
		System.out.println("--end params--");



		size(windowWidth, windowHeight);
		smooth();

		// this font should have all special characters - open
		// to suggestions for changes though
		font = loadFont("/data/LucidaSansUnicode-11.vlw");

		// initialise node manager and add buttons in desired order
		modeManager = new ModeManager(this);
		modeManager.addMode(moveMode);
		modeManager.addMode(nodeMode);
		modeManager.addMode(lineMode);
		modeManager.addMode(wayMode);
		modeManager.addMode(nameMode);
		modeManager.addMode(nodeMoveMode);
		modeManager.addMode(deleteMode);
		modeManager.addMode(zoominMode);
		modeManager.addMode(zoomoutMode);

		modeManager.draw(); // make modeManager set up things


		tiles = new Tile(this, wmsURL, clat, clon, windowWidth, windowHeight, zoom);
		tiles.start();

		System.out.println(tiles);

		recalcStrokeWeight();

		System.out.println("Selected strokeWeight of " + strokeWeight);

		// try to connect to OSM
		osm = new Adapter(userName, password, this, apiURL);

		// register as listener of finished commands (to redraw)
		osm.commandManager.addListener(new CommandManager.Listener(){
			public void commandFinished(ServerCommand command) {
				redraw();
			}
		});

		Thread dataFetcher = new Thread(new Runnable() {

			public void run() {

				osm.getNodesLinesWays(tiles.getTopLeft(), tiles.getBottomRight(), tiles);

				System.out.println("Got " + nodes.size() + " nodes and "
						+ lines.size() + " lines.");

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

	
	private boolean param_float_exists(String paramName) {
		try {
			Float.parseFloat(param(paramName));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private float parse_param_float(String paramName) {
		return Float.parseFloat(param(paramName));
	}

	private int parse_param_int(String paramName) {
		return Integer.parseInt(param(paramName));
	}

	private void draw_scale_bar() {
		double factor[] = { 1.0f, 2.5f, 5.0f };
		int exponent = 0;
		int used_factor = 0;
		double remains = 1.0f;
		int i;
		int min_length = getWidth() / 6;
		int bar_length;
		int dist_bottom = 70;
		int dist_right = 20;
		int ending_bar_length = getHeight() / 30;

		/* Find the nearest factor */
		for (i = 0; i < factor.length; i++) {
			double rest;
			double log_value;

      // log_10(x) == log_e(x) / log_e(10)
      // log_e(10) == 2.30259
      
			log_value = Math.log(tiles.metersPerPixel()
					       * min_length / factor[i])  / 2.30259;
      
			if ((rest = log_value - Math.floor(log_value)) < remains) {
				remains = rest;
				used_factor = i;
				exponent = (int)Math.floor(log_value);
			}
		}

		/* Calculate the exact bar length */
		bar_length = (int) (factor[used_factor] * Math.pow(10.0, exponent)
				    / tiles.metersPerPixel());

		fill(0);
		strokeWeight(2);
		textSize(10);
		pushMatrix();
		/* Horizonthal bar */
		line(getWidth() - bar_length - dist_right, getHeight() - dist_bottom,
		     getWidth() - dist_right, getHeight() - dist_bottom);
		/* Left ending bar */
		line(getWidth() - bar_length - dist_right, getHeight() - dist_bottom + ending_bar_length / 2,
		     getWidth() - bar_length - dist_right, getHeight() - dist_bottom - ending_bar_length / 2);
		/* Right ending bar */
		line(getWidth() - dist_right, getHeight() - dist_bottom + ending_bar_length / 2,
		     getWidth() - dist_right, getHeight() - dist_bottom - ending_bar_length / 2);

		/* Print the numeric scale value */
		String meters = "" + Math.round(factor[used_factor]
				     * Math.pow(10.0, exponent)) + "m";
		translate(getWidth() - dist_right - bar_length + (bar_length - textWidth(meters))/2,
			  getHeight() - dist_bottom + 5);
		text(meters);
		popMatrix();
	}

	public void draw() {
		tiles.draw();
		try {
			if (modeManager.currentMode == moveMode) {
				if (!mouseDown && mouseY < buttonHeight + 5 && mouseY > 5
						&& mouseX > 5
						&& mouseX < 5 + buttonWidth * modeManager.getNumModes()) {
					if (ready && !tiles.viewChanged) {
						cursor(ARROW);
					} else {
						cursor(WAIT);
					}
				} else {
					cursor(MOVE);
				}
			} else {
				if (!ready) {
					cursor(WAIT);
				} else {
					cursor(ARROW);
				}
			}
			noFill();

			// We need to have a cache of all the things we want to draw
			// Otherwise, another Thread might edit the list while we're
			//  in the middle of drawing it, and we'll crash out with a
			//  java.util.ConcurrentModificationException
			
			ArrayList allNodes = new ArrayList(nodes.values());
			ArrayList allLines = new ArrayList(lines.values());
			ArrayList allWays = new ArrayList(ways.values());
			

			// draw the small black border for every line segment
			strokeWeight(strokeWeight + 2.0f);
			stroke(0);
			for (Iterator it = allLines.iterator(); it.hasNext();) {
				Line line = (Line)it.next();
				if (line instanceof LineOnlyId)
					continue;
				if (line.id == 0)
					stroke(0, 80);
				else
					stroke(0);
				line(line.from.coor.x, line.from.coor.y, line.to.coor.x, line.to.coor.y);
			}

			// draw pending lines (lines that do not belong to a way)
			strokeWeight(strokeWeight);
			stroke(255);
			for (Iterator it = allLines.iterator(); it.hasNext();) {
				Line line = (Line)it.next();
				if (line instanceof LineOnlyId)
					continue;
				if (line.ways.isEmpty()) {
					if (line.id == 0)
						stroke(200, 255, 200, 80);
					else
						stroke(200, 255, 200);
					line(line.from.coor.x, line.from.coor.y, line.to.coor.x, line.to.coor.y);
				}
			}

			// draw all ways
			for (Iterator it = allWays.iterator(); it.hasNext();) {
				Way way = (Way)it.next();
				for (Iterator itw = way.lines.iterator(); itw.hasNext();) {
					Line line = (Line)itw.next();
					if (line instanceof LineOnlyId)
						continue; // do not draw id-only line segments
					if (way.id == 0)
						stroke(255, 80);
					else
						stroke(255);
					line(line.from.coor.x, line.from.coor.y, line.to.coor.x, line.to.coor.y);
				}
			}
			
			boolean gotOne = false;
			for (Iterator it = allLines.iterator(); it.hasNext();) {
				Line line = (Line)it.next();
				if (line instanceof LineOnlyId)
					continue;
				if (modeManager.currentMode == nameMode && !gotOne) {
					// highlight first line under mouse
					if (line.mouseOver(mouseX, mouseY, strokeWeight)
							&& line.id != 0) {
						strokeWeight(strokeWeight);
						stroke(0xffffff80);
						line(line.from.coor.x, line.from.coor.y, line.to.coor.x, line.to.coor.y);
						gotOne = true;
					}
				}
			}

			// draw temp line

			if (start != null) {
				tempLine.from = start;
				tempLine.to = new Node(mouseX, mouseY, tiles);
				stroke(0, 80);
				strokeWeight(strokeWeight + 2);
				line(tempLine.from.coor.x, tempLine.from.coor.y, tempLine.to.coor.x, tempLine.to.coor.y);
				stroke(255, 80);
				strokeWeight(strokeWeight);
				line(tempLine.from.coor.x, tempLine.from.coor.y, tempLine.to.coor.x, tempLine.to.coor.y);
			}
			// draw selected line
			stroke(255, 0, 0, 80);
			strokeWeight(strokeWeight);
			for (Iterator it = selectedLine.iterator(); it.hasNext();) {
				Line l = (Line)lines.get(it.next());
				if (l == null || l instanceof LineOnlyId)
					continue;
				if (!l.equals(extraHighlightedLine))
					line(l.from.coor.x, l.from.coor.y, l.to.coor.x, l.to.coor.y);
			}
			if (extraHighlightedLine != null) {
				Line ehl = (Line)lines.get(extraHighlightedLine);
				if (ehl != null && ehl instanceof LineOnlyId) {
					stroke(0, 0, 255, 80);
					line(ehl.from.coor.x, ehl.from.coor.y, ehl.to.coor.x, ehl.to.coor.y);
				}
			}


			// draw nodes
			noStroke();
			ellipseMode(CENTER);

			for (Iterator it = allNodes.iterator(); it.hasNext();) {
				Node node = (Node)it.next();
				if (modeManager.currentMode == lineMode && mouseOverPoint(node.coor)) {
					fill(0xffff0000);
				} else if (modeManager.currentMode == nodeMoveMode) {
					if (node == selectedNode) {
						fill(0xff00ff00);
					} else if (mouseOverPoint(node.coor)) {
						fill(0xffff0000);
					} else {
						fill(0xff000000);
					}
				} else if (modeManager.currentMode == deleteMode) {
					if (mouseOverPoint(node.coor)) {
						fill(0xffff0000);
					} else {
						fill(0xff000000);
					}
				} else if (node == tempLine.from || node == tempLine.to) {
					fill(0xff000000);
				} else if (node.lines.size() > 0) {
					fill(0xffaaaaaa);
				} else if(node.id == 0) {
					fill(0xbbccccff);
				} else {
					fill(0xff000080);
				}
				drawPoint(node.coor);
			}

			// draw way and segment names
			fill(0);
			textFont(font);
			textSize(strokeWeight + 4);
			textAlign(CENTER);

			for (Iterator it = allLines.iterator(); it.hasNext();) {
				Line l = (Line)it.next();
				if (l instanceof LineOnlyId)
					continue;
				if (l.getName() != null) {
					pushMatrix();
					if (l.from.coor.x <= l.to.coor.x) {
						translate(l.from.coor.x, l.from.coor.y);
						rotate(l.angle());
					} else {
						translate(l.to.coor.x, l.to.coor.y);
						rotate(PI + l.angle());
					}
					text(l.getName(), l.length() / 2.0f, 4);
					popMatrix();
				}
			}
			for (Iterator e = ways.values().iterator(); e.hasNext();) {
				Way w = (Way)e.next();
				if (w.getName() != null) {
					Line l = w.getNameLineSegment();
					if (l == null || l instanceof LineOnlyId)
						continue;
					pushMatrix();
					if (l.from.coor.x <= l.to.coor.x) {
						translate(l.from.coor.x, l.from.coor.y);
						rotate(l.angle());
					} else {
						translate(l.to.coor.x, l.to.coor.y);
						rotate(PI + l.angle());
					}
					text(w.getName(), l.length() / 2.0f, 4);
					popMatrix();
				}
			}

			// draw all buttons
			modeManager.draw();
			
			// set status text
			EditMode mouseOverMode = null;
			for (int i = 0; i < modeManager.getNumModes(); ++i) {
				if (((EditMode)modeManager.modes.get(i)).over) {
					mouseOverMode = (EditMode)modeManager.modes.get(i);
					break;
				}
			}
			if (mouseOverMode != null) {
				status(mouseOverMode.getDescription());
			} else if (online) {
				status("lat: " + tiles.lat(mouseY) + ", lon: "
						+ tiles.lon(mouseX));
			}

			// Draw command queue message
			// If the yahoo icon changes again, make sure this stays above it 
			if(osm.commandManager.size() > 0) {
				pushMatrix();
				
				textSize(25);
				String txt = "uploading...";
				int xx = windowWidth - (int)textWidth(txt);
				int yy = windowHeight - 100;
				
				fill(0xff0000ff);
				text(txt,xx,yy);
				fill(0x80ffffff);
				text(txt,xx+1,yy+1);
				
				popMatrix();
			}
			
			// If we're downloading data right now, display something
			//  to alert the user to the fact
			if(osm.getDownloadingOSMData()) {
				textSize(25);
				String txt = "fetching OSM data..."; 
				int xx = 150;
				int yy = windowHeight - 50;
				
				fill(0xff0000ff);
				text(txt,xx,yy);
				fill(0x80ffffff);
				text(txt,xx+1,yy+1);
			}

			// finally draw a scale bar
//			draw_scale_bar();


      image(YahooLogo, windowWidth - 100, windowHeight - 40);

      int xx = 55;
      int yy = windowHeight - 5;
      Character copyrightSymbol = new Character((char)169);
      textSize(15);
      String txt = copyrightSymbol + " 2006 Yahoo! Inc";
      fill(255);
      text(txt, xx+1,yy+1);
      fill(0);
      text(txt, xx,yy);

      txt = "Imagery " + copyrightSymbol + " 2006" + copyright ;
//      print(txt + "___________");
      xx = windowWidth - (int)textWidth(txt) + 30;
      yy = windowHeight - 5;
      fill(255);
      text(txt, xx +1, yy +1);
      fill(0);
      text(txt, xx, yy);

    } catch (NullPointerException npe) {
      npe.printStackTrace();
    }
  } // paint

  public void recalcStrokeWeight() {
    // 20m roads, but min 2px width
    strokeWeight = max(20.0f / tiles.metersPerPixel(), 2.0f);
  }

  public void mouseMoved() {
    if (ready)
      modeManager.mouseMoved();
  }

  public void mouseDragged() {
    if (ready) {
      modeManager.mouseDragged();
    }
  }

  public void mousePressed() {
    mouseDown = true;
    if (ready)
      modeManager.mousePressed();
  }

  public void mouseReleased() {
    mouseDown = false;
    if (ready) {
      if (!tiles.viewChanged)
        modeManager.mouseReleased();
    }
  }

  public void keyPressed() {
    // print("keyPressed!");
    if (ready) {
      switch (key) {
        case '[':
          lastmove = System.currentTimeMillis();
          tiles.zoomin();
          updatelinks();
          break;
        case ']':
          tiles.zoomout();
          updatelinks();
          break;

        case '+':
        case '=':
          strokeWeight += 1.0f;
          redraw();
          break;
        case '-':
        case '_':
          if (strokeWeight >= 2.0f)
            strokeWeight -= 1.0f;
          redraw();
          break;
      }
      if (modeManager.currentMode == nameMode) {
        // println(key == CODED);
        // println(java.lang.Character.getNumericValue(key));
        // println("key= \"" + key + "\"");
        // println("keyCode= \"" + keyCode + "\"");
        // println("BACKSPACE= \"" + BACKSPACE + "\"");
        // println("CODED= \"" + CODED + "\"");
        modeManager.keyPressed();
      }
    }
    key = 0; // catch when key = escape otherwise processing dies
  }

  // bit crufty - TODO tidy up and move into Point
  public boolean mouseOverPoint(Point p) {
    if (p.projected) {
      // /2.0f; so you don't have to be directly on a node for it to light up
      return sq(p.x - mouseX) + sq(p.y - mouseY) < (strokeWeight*strokeWeight); 
    }
    return false;
  }

  /**
   * Get the node or line segment that is nearest to the given coordinates 
   * or <code>null</code>, if nothing is in range
   */
  public OsmPrimitive getNearest(float x, float y) {
    float minDistanceSq = Float.MAX_VALUE;
    OsmPrimitive min = null;
    // first search for nodes
    for (Iterator it = nodes.values().iterator(); it.hasNext();) {
      Node n = (Node)it.next();
      float distSq = n.distanceSq(x,y);
      if (distSq < minDistanceSq) {
        minDistanceSq = distSq;
        min = n;
      }
    }
    if (minDistanceSq < 20)
      return min;
    minDistanceSq = Float.MAX_VALUE;
    // search for line segments
    for (Iterator it = lines.values().iterator(); it.hasNext();) {
      Line l = (Line)it.next();
      if (l instanceof LineOnlyId)
        continue;
      float c = l.from.distanceSq(l.to.coor.x, l.to.coor.y);
      float a = l.to.distanceSq(x,y);
      float b = l.from.distanceSq(x,y);
      float distSq = a-(a-b+c)*(a-b+c)/4/c;
      if (distSq < 20 && distSq < minDistanceSq && a < c+20 && b < c+20) {
        minDistanceSq = distSq;
        min = l;
      }
    }
    if (minDistanceSq < 20)
      return min;
    return null; // nothing within range
  }

  public synchronized void reProject() {
    for (Iterator it = nodes.values().iterator(); it.hasNext();)
      ((Node)it.next()).coor.project(tiles);
  }

  // bit crufty - TODO tidy up and move into draw()?
  public void drawPoint(Point p) {
    if (p.projected) {
      ellipseMode(CENTER);
      ellipse(p.x, p.y, strokeWeight - 1, strokeWeight - 1);
    }
  }

  public void updatelinks() {
    js.eval("updatelinks(" + tiles.lon(windowWidth / 2) + "," + tiles.lat(windowHeight / 2) + "," + tiles.getZoom() + ")");
  }


  ///////////////////////////// BUTTON STUFF //////////////////////////////////

  float buttonWidth = 15.0f;
  float buttonHeight = 15.0f;

  // TODO PFont buttonFont; // for tool-tips

  static public void main(String args[]) {
    String[] params = new String[args.length+1];
    params[0] = "org.openstreetmap.processing.OsmApplet";
    System.arraycopy(args, 0, params, 1, args.length);
    PApplet.main(params);
  }

  
	/**
	 * Our own version of loadImage, which takes account of and handles
	 *  Network timeouts properly
	 */
	public PImage loadImage(String file) {
		if(file.startsWith("http://")) {
			// Do our own, special handling
			try {
				return loadImageWithTimeoutAndRetry(
						new URL(file),
						5,
						10,
						10
				);
			} catch(MalformedURLException e) {
				System.err.println("Skipping invalid image URL " + e);
				return null;
			}
		} else {
			// Our parent's version should be fine
			return super.loadImage(file);
		}
	}
  
	/**
	 * Fetches the Image from the URL, with the various options for retrying,
	 *  timing out etc
	 */
	public PImage loadImageWithTimeoutAndRetry(URL url, int retries, int connectTimeoutSecs, int readTimeoutSecs) {
		byte[] data = null;
		boolean worked = false;
		int attempts = 0;
		
		while(!worked && (attempts <= retries)) {
			attempts++;
			System.out.println("Making attempt " + attempts + " of " + (retries+1) + " to fetch " + url);
			
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				URLConnection conn = url.openConnection();
				conn.setConnectTimeout(connectTimeoutSecs * 1000);
				conn.setReadTimeout(readTimeoutSecs * 1000);
				conn.connect();
				
				byte[] tmp = new byte[2048];
				int read = 0;
				InputStream inp = conn.getInputStream();
				while( (read = inp.read(tmp)) > -1 ) {
					baos.write(tmp, 0, read);
				}
				
				// Save the data, and record it's done
				data = baos.toByteArray();
				worked = true;
			} catch(IOException e) {
				System.err.println("Error fetching " + url + " - " + e);
			}
		}
		
		if(data == null || data.length == 0) {
			// Give up, return null
			return null;
		}
		
		// Create an image from the data
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		try {
			Image img = ImageIO.read(bais);
			return new PImage(img);
		} catch(IOException e) {
			// Should never happen, but never mind
			return null;
		}
	}
}
