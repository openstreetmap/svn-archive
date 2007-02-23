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
 * <p>GENERAL TODO
 * 
 * <ul>
 * <li>Add an undo queue and a save button - ie don't commit changes live?
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import netscape.javascript.JSObject;

import org.openstreetmap.client.Adapter;
import org.openstreetmap.client.CommandManager;
import org.openstreetmap.client.MapData;
import org.openstreetmap.client.ServerCommand;
import org.openstreetmap.client.Tile;
import org.openstreetmap.util.Line;
import org.openstreetmap.util.LineOnlyId;
import org.openstreetmap.util.Node;
import org.openstreetmap.util.OsmPrimitive;
import org.openstreetmap.util.Point;
import org.openstreetmap.util.Releaseable;
import org.openstreetmap.util.Way;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;

/**
 * Main applet, based on processing.org framework (PApplet).
 */
public class OsmApplet extends PApplet implements Releaseable {
  private String copyright = "";

	public Tile tiles;

  /**
   * Object to use to output interact with containing page (via javascript call).
   */
	volatile private JSObject js;
  
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
   * Holds all the nodes, segments and ways.
   */
  final private MapData map = new MapData();
  
  PImage YahooLogo = loadImage("/data/yahoo.png");

	/* image showing GPX tracks - TODO: vector of PImages? one per GPX file? */
	// private PImage gpxImage;

	/**
	 * Width of line segments
	 * TODO: modulate based on scale, and road type
	 */
  float strokeWeight = 11.0f;
  float halfStrokeWeight = strokeWeight / 2;

	/**
	 * For displaying new lines whilst drawing (between start and mouseX/Y)
	 */
	private Line tempLine = new Line(null, null);

	/**
	 * Ids of current selected lines, for creating streets
	 * Type: String (key of a line)
	 */
	public List selectedLine = new ArrayList();
	
  /**
	 * Extra bold highlighted for marking up lines on a way during creation.
	 */
	volatile public String extraHighlightedLine = null;

	/*
	 * current node, for moving nodes - TODO: track this in editmode, and make
	 * node.selected flag
	 */
	Node selectedNode = null;

	/* selected start point when drawing lines */
	volatile Node start = null;

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

  /** Applet still in startup */
  public static final int NOT_READY = 0;
  
  /** Can pan/zoom map but no data to edit as yet */
  public static final int BROWSEABLE = 1;
  
  /** Got OSM data - allow editing */
  public static final int EDITABLE = 2;

	/*
	 * if !ready, a wait cursor is shown and input doesn't do anything 
   * 
   * updated to cope with more states - split ready mode into
   * can-browse and can-edit.
	 */
	private int status = NOT_READY;

	long lastmove;

	boolean moved = true;

	boolean gotGPX = false;

  /** Output of debug statements to HTML page via javascript (see debug()) */
  boolean debugToPage = false;
  
  /** Display abort button for long map downloads **/
  boolean abortable = false;
  
  volatile boolean isShutdown = false;

  /** Set true to cause shutdown of all threads */
  volatile public boolean shutdown = false;

  /** Timeout for map server API calls, miliseconds. */
  public int timeout = 15 * 1000;

  /** Max number of retries in map server API calls. */
  public int retries = 4;

  public void setCopyright(String c) {
    copyright = c;
  }
  
	public void setup() {
    Thread.currentThread().setName("applet_thr");
    
		// for centre lat/lon and scale (degrees per pixel)
		float clat = 51.526447f, clon = -0.14746371f;
		//float clat = -37.526763645918486f, clon = 144.14729439306237f;
		zoom = 15;
    
    int tileThreads = 4; // default thread pool size for image tile downloads

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
      if (param_float_exists("tileThreads"))
        tileThreads = parse_param_int("tileThreads");
      debugToPage = parse_param_boolean("debugToPage");
      abortable = parse_param_boolean("abortable");
      if (param_float_exists("retries"))
        retries = parse_param_int("retries");
      if (param_float_exists("timeout"))
        timeout = (int) (parse_param_float("timeout") * 1000);

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
					zoom = Integer.parseInt(args[i].substring(args[i].indexOf('=')+1));
				}
        if (args[i].startsWith("--windowHeight=")) {
          windowHeight = Integer.parseInt(args[i].substring(args[i].indexOf('=')+1));
        }
        if (args[i].startsWith("--windowWidth=")) {
          windowWidth = Integer.parseInt(args[i].substring(args[i].indexOf('=')+1));
        }
        if (args[i].startsWith("--tileThreads=")) {
          tileThreads = Integer.parseInt(args[i].substring(args[i].indexOf('=')+1));
        }
        if (args[i].startsWith("--abortable")) {
          abortable = true;
        }
        if (args[i].startsWith("--retries=")) {
          retries = Integer.parseInt(args[i].substring(args[i].indexOf('=')+1));
        }
        if (args[i].startsWith("--timeout=")) {
          timeout = (int) (1000 * Float.parseFloat(args[i].substring(args[i].indexOf('=')+1)));
        }
			}
		}

		debug("Got userName: " + userName);
		debug("Got password: " + password);
		debug("Got clon: " + clon);
		debug("Got clat: " + clat);
		debug("Got zoom: " + zoom);
		debug("Got windowHeight: " + windowHeight);
    debug("Got windowWidth: " + windowWidth);
    debug("Got tileThreads: " + tileThreads);
    debug("Got debugToPage: " + debugToPage);
    debug("Got abortable: " + abortable);
    debug("Got retries: " + retries);
    debug("Got timeout: " + timeout);
		debug("--end params--");



		size(windowWidth, windowHeight);
		smooth();

		// this font should have all special characters - open
		// to suggestions for changes though
		font = loadFont("/data/LucidaSansUnicode-11.vlw");

    // try to connect to OSM (before starting mode ui)
    osm = new Adapter(userName, password, this, apiURL);

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
    
    modeManager.setCurrentMode(moveMode); // default to match View pane
		modeManager.draw(); // make modeManager set up things

		tiles = new Tile(this, wmsURL, clat, clon, windowWidth, windowHeight, zoom, tileThreads);
		tiles.start();

		debug(tiles.toString());

		recalcStrokeWeight();

		debug("Selected strokeWeight of " + strokeWeight);

		// register as listener of finished commands (to redraw)
		osm.commandManager.addListener(new CommandManager.Listener(){
			public void commandFinished(ServerCommand command) {
				redraw(); // NB: this will sync on applet - better make sure callback is on event thread
			}
		});

    setStatus(BROWSEABLE);
		noLoop();
		redraw();
	} // setup

	
  /* (non-Javadoc)
   * @see org.openstreetmap.util.Releaseable#release()
   */
  public void release() {
    if (!isShutdown) {
      // debug("attempting shutdown..."); release as many resources as possible
      tiles.release();
      osm.release();
      isShutdown = true;
      exit(); // give 'processing' every chance to shutdown (doesn't seem to though)
    }
  }
  
  /**
   * Applet being shutdown - stop any threads.
   */
  public void destroy() {
    super.destroy();
    release();
  }

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
  
  private boolean parse_param_boolean(String paramName) {
    return ("" + param(paramName)).equals("true");
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

  
  private long lastDrawTime = 0;

  // remember where download abort button rendered
  private int abortButtonX;
  private int abortButtonY;
  private int abortButtonHeight;
  private int abortButtonWidth;

  /**
   * Opacity to render roads at (need to make transparent for certain operations)
   */
  private int opacity = 255;
  
  /**
   * Set default/node road fill opacity.
   * @param opacity From 0 (transparent) to 255 (opaque).
   */
  synchronized public void setOpacity(int opacity) {
    this.opacity = opacity;
  }
  
  /**
   * @return Time last draw completed, or 0 if still drawing.
   */
  synchronized public long getLastDrawTime() {
    return lastDrawTime;
  }
  
	/* (non-Javadoc)
	 * @see processing.core.PApplet#draw()
	 */
  /**
   * NB: Tagged as synchronized since Processing PApplet base code has already
   * obtained lock on this - i.e. be careful with lock ordering.
   * 
   * i.e. draw thread has lock for whole of applet draw cycle.
   */
	synchronized public void draw() {
    lastDrawTime = 0;
    
		tiles.draw();
		try {
      if (abortable && isOverMapGetAbort()) {
        cursor(ARROW);
      }
      else if (modeManager.getCurrentMode() == moveMode) {
        if (!mouseDown && mouseY < buttonHeight + 5 && mouseY > 5
            && mouseX > 5
            && mouseX < 5 + buttonWidth * modeManager.getNumModes()) {
          if (getStatus() != EDITABLE && !tiles.isViewChanged()) {
            cursor(WAIT);
          }
          else {
            cursor(HAND);            
          }
        }
      } else {
        if (getStatus() == EDITABLE) {
          cursor(ARROW);
        } else {
          cursor(WAIT);
        }
      }
			noFill();

			// draw the small black border for every line segment
			strokeWeight(strokeWeight + 2.0f);
			stroke(0);
      
      synchronized (map) { // long sequence of map/iterator use: prevent concurrent edits 
        
  			for (Iterator it = map.linesIterator(); it.hasNext();) {
  				Line line = (Line)it.next();
  				if (line instanceof LineOnlyId)
  					continue;
  				if (line.id == 0)
  					stroke(0, 80);
  				else
  					stroke(0, opacity);
  				line(line.from.coor.x, line.from.coor.y, line.to.coor.x, line.to.coor.y);
  			}
  
  			// draw pending lines (lines that do not belong to a way)
  			stroke(255);
  			for (Iterator it = map.linesIterator(); it.hasNext();) {
          strokeWeight(strokeWeight);
          Line line = (Line)it.next();
  				if (line instanceof LineOnlyId)
  					continue;
  				if (line.ways.isEmpty()) {
  					if (line.id == 0)
  						stroke(200, 255, 200, 80);
  					else
  						stroke(200, 255, 200, opacity);
  					line(line.from.coor.x, line.from.coor.y, line.to.coor.x, line.to.coor.y);
            drawOneWay(line);
  				}
  			}
  
  			// draw all ways
  			for (Iterator it = map.waysIterator(); it.hasNext();) {
  				Way way = (Way)it.next();
  				for (Iterator itw = way.lines.iterator(); itw.hasNext();) {
            strokeWeight(strokeWeight);
  					Line line = (Line)itw.next();
  					if (line instanceof LineOnlyId)
  						continue; // do not draw id-only line segments
  					if (way.id == 0)
  						stroke(255, 80);
  					else
  						stroke(255, opacity);
  					line(line.from.coor.x, line.from.coor.y, line.to.coor.x, line.to.coor.y);
            drawOneWay(line);
  				}
  			}
  			
  			boolean gotOne = false;
  			for (Iterator it = map.linesIterator(); it.hasNext();) {
  				Line line = (Line)it.next();
  				if (line instanceof LineOnlyId)
  					continue;
  				if (modeManager.getCurrentMode() == nameMode && !gotOne) {
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
  				Line l = map.getLine((String)it.next());
  				if (l == null || l instanceof LineOnlyId)
  					continue;
  				if (!l.equals(extraHighlightedLine))
  					line(l.from.coor.x, l.from.coor.y, l.to.coor.x, l.to.coor.y);
  			}
  			if (extraHighlightedLine != null) {
  				Line ehl = map.getLine(extraHighlightedLine);
  				if (ehl != null && !(ehl instanceof LineOnlyId)) {
  					stroke(0, 0, 255, 80);
  					line(ehl.from.coor.x, ehl.from.coor.y, ehl.to.coor.x, ehl.to.coor.y);
  				}
  			}
  
  			// draw nodes
  			noStroke();
  			ellipseMode(CENTER);
  
  			for (Iterator it = map.nodesIterator(); it.hasNext();) {
  				Node node = (Node)it.next();
  				if (modeManager.getCurrentMode() == lineMode && mouseOverPoint(node.coor)) {
  					fill(0xffff0000);
  				} else if (modeManager.getCurrentMode() == nodeMoveMode) {
  					if (node == selectedNode) {
  						fill(0xff00ff00, opacity + 20);
  					} else if (mouseOverPoint(node.coor)) {
  						fill(0xffff0000);
  					} else {
  						fill(0xff000000);
  					}
  				} else if (modeManager.getCurrentMode() == deleteMode) {
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
  
  			for (Iterator it = map.linesIterator(); it.hasNext();) {
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
  			for (Iterator e = map.waysIterator(); e.hasNext();) {
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
      }
      
			// draw all buttons
			modeManager.draw();
			
			// set status text
			EditMode mouseOverMode = null;
			for (int i = 0; i < modeManager.getNumModes(); ++i) {
				if (((EditMode)modeManager.modes.get(i)).isOver()) {
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
      final int HTTP_NOTIFY_HEIGHT = 55; 
			if(osm.commandManager.size() > 0) {
				drawUploadingNotification(HTTP_NOTIFY_HEIGHT);
			}
			
			// If we're downloading data right now, display something
			//  to alert the user to the fact
			if(osm.getDownloadingOSMData()) {
        drawDownloadArea(20); // almost transparent
        drawFetchingDataNotification(HTTP_NOTIFY_HEIGHT);
			}
      else {
        drawDownloadArea(0); // outline where map data runs to
      }

			// finally draw a scale bar TODO scale bar currently unused...
			//draw_scale_bar();

      image(YahooLogo, windowWidth - 90, windowHeight - 40);

      // NB: fixed some text alignment problems / wierdness below
      // by ensuring textAlign() is called appropriately
      int xx = 5;
      int yy = windowHeight - 5;
      Character copyrightSymbol = new Character((char)169);
      textSize(15);
      String txt = copyrightSymbol + " 2006 Yahoo! Inc";
      fill(255);
      textAlign(LEFT);
      text(txt, xx+1,yy+1);
      fill(0);
      text(txt, xx,yy);

      txt = "Imagery " + copyrightSymbol + " 2006" + copyright ;
      xx = windowWidth - 5;
      fill(255);
      textAlign(RIGHT);
      text(txt, xx +1, yy +1);
      fill(0);
      text(txt, xx, yy);

    } catch (NullPointerException npe) {
      npe.printStackTrace();
    }
    finally {
      lastDrawTime = System.currentTimeMillis();
      //debug("OsmApplet.draw() done.");
    }
  } // paint

  private void drawOneWay(Line line) {
    byte oneWay = line.getOneWay();
    if (oneWay == OsmPrimitive.ONEWAY_UNDEFINED || oneWay == OsmPrimitive.ONEWAY_NOT) {
      return;
    }
    pushMatrix();
      // draw triangle some way along segment if oneway
      if (line.getOneWay() == OsmPrimitive.ONEWAY_FORWARDS) {
        translate(line.from.coor.x, line.from.coor.y);
        rotate(line.angle());
      }
      else {
        translate(line.to.coor.x, line.to.coor.y);
        rotate(PI + line.angle());
      }
      // text("oneway>", 0.67f * line.length(), 4);
      translate(0.5f * line.length(), 0);
      strokeWeight(1);
      stroke(50);
      fill(255, 255, 210);
      triangle(0, halfStrokeWeight, 0, -halfStrokeWeight, strokeWeight, 0);
    popMatrix();
  }

  private void drawFetchingDataNotification(final int HTTP_NOTIFY_HEIGHT) {
    // render 'downloading data' message, with abort button
    int xx = 20;
    int yy = windowHeight - HTTP_NOTIFY_HEIGHT;
    textSize(20);
    String txt = "fetching OSM data...";
    fill(0xff0000ff);
    textAlign(LEFT);
    text(txt,xx,yy);
    fill(0x80ffffff);
    text(txt,xx+1,yy+1);

    if (abortable) {
      abortButtonHeight = 20;
      abortButtonX = xx + (int) textWidth(txt);
      txt = "[abort]";
      abortButtonWidth = (int) textWidth(txt);
      abortButtonY = yy - abortButtonHeight + 4;
      pushMatrix();  // button background
        strokeWeight(0);
        fill(255, 255, 255); // almost transparent
        rect(abortButtonX, abortButtonY, abortButtonWidth, abortButtonHeight);
      popMatrix();
      fill(0, 0, 0);
      textAlign(LEFT);
      text(txt,abortButtonX,yy);
    }
  }

  /**
   * @param alpha Opacity value, 0 = transparent, 255 = solid.
   */
  private void drawDownloadArea(float alpha) {
    Point topLeft = tiles.getDataDownloadTopLeft();
    Point bottomRight = tiles.getDataDownloadBottomRight();
    if (topLeft != null && bottomRight != null) {
      topLeft.project(tiles);
      bottomRight.project(tiles);
      pushMatrix();
        strokeWeight(0);
        fill(255, 255, 255, alpha);
        rect(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
      popMatrix();
    }
  }

  private void drawUploadingNotification(final int HTTP_NOTIFY_HEIGHT) {
    pushMatrix();
    
    textSize(20);
    String txt = "uploading...";
    int xx = windowWidth - 20;
    int yy = windowHeight - HTTP_NOTIFY_HEIGHT;
    
    textAlign(RIGHT);
    fill(0xff0000ff);
    text(txt,xx,yy);
    fill(0x80ffffff);
    text(txt,xx+1,yy+1);
    
    popMatrix();
  }

  private boolean isOverMapGetAbort() {
    if (osm.getDownloadingOSMData()) {
      if (mouseX > abortButtonX && mouseY > abortButtonY 
          && mouseX < (abortButtonX + abortButtonWidth) && mouseY < (abortButtonY + abortButtonHeight)) {
        return true;
      }
    }
    return false;
  }
  
  private String getTime(long startTime) {
    return Long.toString((System.currentTimeMillis() - startTime) % 1000);
  }
  
  public void recalcStrokeWeight() {
    // 20m roads, but min 2px width
    strokeWeight = max(20.0f / tiles.metersPerPixel(), 2.0f);
    halfStrokeWeight = strokeWeight / 2;
  }

  public void mouseMoved() {
    if (getStatus() != NOT_READY)
      modeManager.mouseMoved();
  }

  public void mouseDragged() {
    //debug("dragging mouse...");
    if (getStatus() != NOT_READY) {
      modeManager.mouseDragged();
    }
    //debug("mouse dragged.");
  }

  public void mousePressed() {
    if (abortable && isOverMapGetAbort()) {
      osm.abortMapGet();
    }
    mouseDown = true;
    if (getStatus() != NOT_READY)
      modeManager.mousePressed();
  }

  public void mouseReleased() {
    mouseDown = false;
    if (getStatus() != NOT_READY) {
      modeManager.mouseReleased();
    }
  }

  public void keyPressed() {
    // print("keyPressed!");
    if (getStatus() != NOT_READY) {
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

        // TODO currently keys-only for changing resolution - should be made
        // into buttons
        case '{':
          tiles.resolutionUp();
          break;
        case '}':
          tiles.resolutionDown();
          break;

        case '+':
        case '=':
          strokeWeight += 1.0f;
          halfStrokeWeight = strokeWeight / 2;
          redraw();
          break;
        case '-':
        case '_':
          if (strokeWeight >= 2.0f)
            strokeWeight -= 1.0f;
          halfStrokeWeight = strokeWeight / 2;
          redraw();
          break;
      }
      if (modeManager.getCurrentMode() == nameMode) {
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
   * or <code>null</code>, if nothing is in range.
   * 
   * NB: locks <code>map</map>
   */
  public OsmPrimitive getNearest(float x, float y) {
   float minDistanceSq = Float.MAX_VALUE;
    OsmPrimitive min = null;
    synchronized (map) { // see MapData comments
      // first search for nodes
      for (Iterator it = map.nodesIterator(); it.hasNext();) {
        Node n = (Node)it.next();
        float distSq = n.distanceSq(x,y);
        if (distSq < minDistanceSq) {
          minDistanceSq = distSq;
          min = n;
        }
      }
      float nodeRadius = (strokeWeight - 1 ) / 2;
      if (minDistanceSq < nodeRadius*nodeRadius)
        return min; // bias to node so can reliably select in pref to seg

      // search for line segments
      for (Iterator it = map.linesIterator(); it.hasNext();) {
        Line l = (Line)it.next();
        if (l instanceof LineOnlyId)
          continue;
        float c = l.from.distanceSq(l.to.coor.x, l.to.coor.y);
        float a = l.to.distanceSq(x,y);
        float b = l.from.distanceSq(x,y);
        float distSq = a-(a-b+c)*(a-b+c)/4/c;
        if (distSq < 20*20 && distSq < minDistanceSq && a < c+20*20 && b < c+20*20) {
          minDistanceSq = distSq;
          min = l;
        }
      }
      if (minDistanceSq < 20*20)
        return min;
      return null; // nothing within range
    } // sync
  }
  
  /**
   * @return Nearest map primitive to current mouse position.
   */
  public OsmPrimitive getNearest() {
    return getNearest(mouseX, mouseY);
  }

  /**
   * sync = won't reProject while a draw is in progress.
   * 
   * beware calling from inside other sync - deadlock potential
   */
  synchronized public void reProject() {
    map.reProject(tiles);
  }

  // bit crufty - TODO tidy up and move into draw()?
  public void drawPoint(Point p) {
    if (p.projected) {
      ellipseMode(CENTER);
      ellipse(p.x, p.y, strokeWeight - 1, strokeWeight - 1);
    }
  }

  public void updatelinks() {
    jsEval("updatelinks(" + tiles.lon(windowWidth / 2) + "," + tiles.lat(windowHeight / 2) + "," + tiles.getZoom() + ");");
  }

  /**
   * Calls to browser page, evaluates given expression.
   * 
   * @param expr
   */
  void jsEval(String expr) {
    try {
      js.eval(expr);
    }
    catch (Exception e) {
      // NOP
    }
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
			if (attempts > 1) {
              debug("Retrying... attempt " + attempts + " of " + (retries+1) + " to fetch " + url);
            }
			
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				URLConnection conn = url.openConnection();
				conn.setConnectTimeout(connectTimeoutSecs * 1000);
				conn.setReadTimeout(readTimeoutSecs * 1000);
        //debug("Connecting...");
				conn.connect();
				
				byte[] tmp = new byte[2048];
				int read = 0;
				InputStream inp = conn.getInputStream();
        //debug("Reading data....");
				while( (read = inp.read(tmp)) > -1 ) {
					baos.write(tmp, 0, read);
				}
				
				// Save the data, and record it's done
				data = baos.toByteArray();
        //debug("Received data.");
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
    
  /**
   * Output debug to sys out (and optionally to page for testing purposes).
   */
  public void debug(String s) {
    String msg = "[" + Thread.currentThread().getName() + "] " + (System.currentTimeMillis() % 10000) + " " + s;
    System.out.println(msg);
    if (debugToPage && js != null) {
      jsEval("appletDebug('" + msg.replace('\'', '\"') + "');");
    }
  }

  /**
   * Updates applet status, thereby (dis)allowing appropriate functions.
   * 
   * @param status New applet status: one of NOT_READY, BROWSEABLE or EDITABLE
   */
  public void setStatus(int status) {
    this.status = status;
  }

  /**
   * Indicates if applet is ready to be used, and if so, if just for
   * viewing (BROWSEABLE) or if editing is allowed.
   * 
   * @return Applet status: one of NOT_READY, BROWSEABLE or EDITABLE
   */
  public int getStatus() {
    return status;
  }

  /**
   * The low-level map data instance.  
   * 
   * NB: The instance is final and guaranteed to persist - i.e. callers
   * may retain a reference to the map, it will not change (whilst taking 
   * care of possible circular references :) ).
   * 
   * NB: Callers should perform all interactions with the map or its
   * constituents (e.g. iterators, nodes, etc.) from with a sync block, 
   * sync'ed on the map instance.  This is especially true of any map
   * collections, as ConcurrentModification excecptions can occur otherwise.
   * 
   * @return  The applet's singleton map data instance.
   */
  public MapData getMapData() {
    return map;
  }

  /**
   * Updates applet's map 
   * 
   * @param map
   */
  synchronized public void updateMap(MapData map) {
    this.map.updateData(map);
  }
  
  /**
   * Sync'ed updater to ensure updates picked up on draw thread.
   */
  synchronized public void resetTempLine() {
    tempLine.from = null;
    tempLine.to = null;
  }
  
  /**
   * Sync'ed updater to ensure updates picked up on draw thread +
   * prevent concurrent mod.
   */
  synchronized public void clearSelectedLine() {
    selectedLine.clear();
  }
}
