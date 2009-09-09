<?php
/*
 * This file is part of OSM-Atlas, a script to create an atlas from
 * OpenStreetMap (www.openstreetmap.org) data.
 * Copyright 2009 Russell Phillips
*/

/*
 * Global configuration file for OSM-Atlas.php. Settings in this file
 * may be over-ridden by settings in the location-specific file
*/

//DPI for Inkscape export
$INKSCAPE_DPI = 900;

// bounding box
$LEFT = 0;
$RIGHT = 0;
$TOP = 0;
$BOTTOM = 0;

//Number of tiles
$HORIZ_TILES = 5;
$VERT_TILES = 3;

// Stylesheet to use
$STYLESHEET = "osm-map-features-z17.xml";
//Delete working files? Default is True, but setting this to False can
//be useful to help with debugging, or if you wish to manually
//edit the .tex file
$DELETE_WORKING_FILES = True;

//Title
$TITLE = "Atlas";

//PDF metadata
$PDFTITLE = "Atlas";
$PDFAUTHOR = "OSM-Atlas";
$PDFSUBJECT = "Atlas";
$PDFKEYWORDS = "openstreetmap,atlas";

//TTF font to use for numbers on overview map
$OV_FONT = "/usr/share/fonts/truetype/freefont/FreeSerif.ttf";
$OV_FONT_SIZE = 20;

//What to include in the index
$INDEX_STREET_NAMES = True;
$INDEX_PLACE_NAMES = True;

//Directories & files. Do not include trailing / when specifying directories
//Directory containing osmarender
$OSMARENDER_DIR = "/home/user/osmarender";
//Directory containing osmosis
$OSMOSIS_DIR = "/home/user/osmosis-0.30/bin/";
// Directory where all output files (working files and final PDF) will be placed
$OUTPUT_DIR = "/home/user";

// Data file (.osm file - see http://wiki.openstreetmap.org/wiki/Planet.osm for downloads)
$DATA_FILE="/home/user/planet.osm";
//Log file
$LOG_FILE = "/home/user/atlas.log";
?>
