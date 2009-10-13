<?php
/*
 * This file is part of OSM-Atlas, a script to create an atlas from
 * OpenStreetMap (www.openstreetmap.org) data.
 * Copyright 2009 Russell Phillips
*/

/*
 * Sample configuration file for OSM-Atlas.php. The values in this
 * file are reasonable values for a street atlas
*/

// bounding box
$LEFT = 0;
$RIGHT = 0;
$TOP = 0;
$BOTTOM = 0;

//Number of tiles
$HORIZ_TILES = 5;
$VERT_TILES = 3;

//TTF font to use for numbers on overview map
$OV_FONT = "/usr/share/fonts/truetype/freefont/FreeSerif.ttf";
$OV_FONT_SIZE = 20;

//Directory containing osmarender. Do not include trailing /
$OSMARENDER_DIR = "/home/user/osmarender";
//Directory containing osmosis. Do not include trailing /
$OSMOSIS_DIR = "/home/user/osmosis-0.30/bin";
// Directory where all output files (working files and final PDF) will be placed. Do not include trailing /
$OUTPUT_DIR = "/home/user";

// Data file (.osm file - see http://wiki.openstreetmap.org/wiki/Planet.osm for downloads)
$DATA_FILE="/home/user/planet.osm";

//Title
$TITLE = "Street Atlas";

//What to include in the index
$INDEX_STREET_NAMES = True;
$INDEX_PLACE_NAMES = False;

//Log file
$LOG_FILE = "/dev/null";

//DPI for Inkscape export
$INKSCAPE_DPI = 900;

// Stylesheet to use for main map pages
$STYLESHEET = "osm-map-features-z17.xml";
// Stylesheet to use for thumbnails in overview map
$STYLESHEET_THUMBS = "osm-map-features-z12.xml";
//Delete working files? Default is True, but setting this to False can
//be useful to help with debugging, or if you wish to manually
//edit the .tex file
$DELETE_WORKING_FILES = True;

//PDF metadata
$PDFTITLE = "Street Atlas";
$PDFAUTHOR = "OSM-Atlas";
$PDFSUBJECT = "Street Atlas";
$PDFKEYWORDS = "openstreetmap,atlas,street atlas";
?>
