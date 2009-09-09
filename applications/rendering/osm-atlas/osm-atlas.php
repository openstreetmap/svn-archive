#!/usr/bin/env php
<?php
/*
 * This file is part of OSM-Atlas, a script to create an atlas from
 * OpenStreetMap (www.openstreetmap.org) data.
 * Copyright 2009 Russell Phillips
*/

// This script works with OSM API v0.6

// Program version
$VERSION = "1.0";
// Page where maps start
$START_PAGE_NUMBER = 2;

//Get config files
require ("inc_config.php");
require ($argv [1]);

// Rotate logs
if (file_exists ($LOG_FILE))
	if (filesize ($LOG_FILE) > 10240) {
		if (file_exists ("$LOG_FILE.old"))
			unlink ("$LOG_FILE.old");
		rename ($LOG_FILE, "$LOG_FILE.old");
	}

/*
 * Function to make a map tile. Arguments:
 * $tLeft: westerly longitude
 * $tRight: easterly longitude
 * $tTop: northerly latitude
 * $tBottom: southerly latitude
 * $tRow
 * $tCol
 * $PageNumber: page number. Used for indexes
*/
function tile ($tLeft, $tRight, $tTop, $tBottom, $tRow, $tCol, $PageNumber) {
	//Define global variables
	global $OSMOSIS_DIR;
	global $DATA_FILE;
	global $OUTPUT_DIR;
	global $OSMARENDER_DIR;
	global $STYLESHEET;
	global $INDEX_STREET_NAMES;
	global $INDEX_PLACE_NAMES;
	global $asStreetNameIndex;
	global $asPlaceNameIndex;
	global $INKSCAPE_DPI;
	global $CurrentProgress;

	// Run osmosis to extract tile area
	Progress ($CurrentProgress, "Running osmosis for tile row=$tRow, col=$tCol");
	chdir ($OSMOSIS_DIR);
	if (!file_exists ($DATA_FILE))
		die_error ("Data file $DATA_FILE does not exist");
	$cmd = "./osmosis --read-xml $DATA_FILE --bounding-box left=$tLeft top=$tTop right=$tRight bottom=$tBottom completeWays=yes --write-xml file=$OUTPUT_DIR/osm-temp-$tRow-$tCol.osm";
	shell_exec ("$cmd 2>/dev/null >/dev/null");
	if (!file_exists ("$OUTPUT_DIR/osm-temp-$tRow-$tCol.osm"))
		die_error ("osmosis output file $OUTPUT_DIR/osm-temp-$tRow-$tCol.osm does not exist\nCommand:\n$cmd");

	// Set bounds of data.osm
	$bounds .= "minlat='$tBottom' minlon='$tLeft' maxlat='$tTop' maxlon='$tRight'";
	Progress ($CurrentProgress, "Setting bounds for tile row=$tRow, col=$tCol");
	chdir ($OUTPUT_DIR);
	$cmd = "sed -i -e \"2 s/^<osm .*$/<osm><bounds $bounds \/>/\" osm-temp-$tRow-$tCol.osm";
	shell_exec ("$cmd 2>/dev/null >/dev/null");

	// Run osmarender
	Progress ($CurrentProgress, "Running osmarender for tile row=$tRow, col=$tCol");
	if (!file_exists ("$OUTPUT_DIR/osm-temp-$tRow-$tCol.osm"))
		die_error ("$OUTPUT_DIR/osm-temp-$tRow-$tCol.osm does not exist");
	if (!copy ("$OUTPUT_DIR/osm-temp-$tRow-$tCol.osm", "$OSMARENDER_DIR/data.osm"))
		die_error ("Error copying $OUTPUT_DIR/osm-temp-$tRow-$tCol.osm to $OSMARENDER_DIR/data.osm");
	chdir ($OSMARENDER_DIR);
	$cmd = "xsltproc osmarender.xsl $STYLESHEET > $OUTPUT_DIR/osm-temp-$tRow-$tCol.svg";
	shell_exec ("$cmd 2>/dev/null");

	// Add names to indexes
	Progress ($CurrentProgress, "Extracting names for index, tile row=$tRow, col=$tCol");
	//Load .osm file into a SimpleXML object
	$xml = simplexml_load_file ("$OUTPUT_DIR/osm-temp-$tRow-$tCol.osm");
	//Street name index
	if ($INDEX_STREET_NAMES == True) {
		//Loop through ways
		foreach ($xml->way as $way) {
			//Initialise $bName & $sName
			$bName = False;
			$sName = "";
			foreach ($way->tag as $tag) {
				//If way is a highway, set $bName to True, get name if found
				if ($tag ['k'] == 'highway')
					$bName = True;
				elseif ($tag ['k'] == 'name')
					$sName = trim ($tag ['v']);
			}
			//If way is a highway *and* it has a name, add to index
			if ($bName == True && $sName != "") {
				if (array_key_exists ($sName, $asStreetNameIndex))
					$asStreetNameIndex [$sName] .= ", $PageNumber";
				else
					$asStreetNameIndex [$sName] = " $PageNumber";
			}
		}
	}
	//Place name index
	if ($INDEX_PLACE_NAMES == True) {
		//Loop through ways
		foreach ($xml->node as $node) {
			//Initialise $bName & $sName
			$bName = False;
			$sName = "";
			foreach ($node->tag as $tag) {
				//If node is a place, set $bName to True, get name if found
				if ($tag ['k'] == 'place')
					$bName = True;
				elseif ($tag ['k'] == 'name')
					$sName = trim ($tag ['v']);
			}
			//If node is a place *and* it has a name, add to index
			if ($bName == True && $sName != "") {
				if (array_key_exists ($sName, $asPlaceNameIndex))
					$asPlaceNameIndex [$sName] .= ", $PageNumber";
				else
					$asPlaceNameIndex [$sName] = " $PageNumber";
			}
		}
	}

	// Run inkscape to create PNG
	Progress ($CurrentProgress, "Running Inkscape to create .png for tile row=$tRow, col=$tCol");
	$cmd = "inkscape --without-gui --file=$OUTPUT_DIR/osm-temp-$tRow-$tCol.svg --export-png=$OUTPUT_DIR/osm-temp-$tRow-$tCol.png --export-dpi=$INKSCAPE_DPI";
	shell_exec ("$cmd 2>/dev/null >/dev/null");

	if (file_exists ("$OSMARENDER_DIR/data.osm"))
		unlink ("$OSMARENDER_DIR/data.osm");
}

/*
 * Function to delete files specified with wildcard
*/
function delfile ($s) {
	global $LOG_FILE;
	foreach (glob ($s) as $f) {
		file_put_contents ($LOG_FILE, "deleting $f\n", FILE_APPEND);
		unlink ($f);
	}
}

/*
 * Function to update progress display
*/
function Progress (&$CurrentProgress, $Description) {
	global $TotalSteps;
	global $LOG_FILE;
	$CurrentProgress++;

	$progress = ceil (($CurrentProgress / $TotalSteps) * 100);
	$progress = str_pad ($progress, 3, " ", STR_PAD_LEFT);
	ncurses_mvaddstr (2, 2, "Progress: $progress%");

	$Description = str_pad ($Description, 60, " ");
	ncurses_mvaddstr (3, 3, $Description);
	ncurses_refresh();

	file_put_contents ($LOG_FILE, "Step $CurrentProgress of $TotalSteps - $Description\n", FILE_APPEND);
}

/*
 * Function to exit with error
*/
function die_error ($ErrorMessage) {
	global $LOG_FILE;
	file_put_contents ($LOG_FILE, "osm-atlas exited with the following error:\n", FILE_APPEND);
	file_put_contents ($LOG_FILE, "\t$ErrorMessage\n", FILE_APPEND);
	ncurses_end();
	die ("$ErrorMessage\n");
}

//Log start
file_put_contents ($LOG_FILE, "\nOSM-Atlas.php started at " . date ("H:i jS M Y") . "\n", FILE_APPEND);
//Check files & directories exist
if (! file_exists ($OUTPUT_DIR))
	die_error ("OUTPUT_DIR ($OUTPUT_DIR) does not exist");
if (! file_exists ($OSMARENDER_DIR))
	die_error ("OSMARENDER_DIR ($OSMARENDER_DIR) does not exist");
if (! file_exists ("$OSMARENDER_DIR/$STYLESHEET"))
	die_error ("STYLESHEET ($STYLESHEET) does not exist");
//Delete .tex file if it already exists
if (file_exists ("$OUTPUT_DIR/OSM_Atlas.tex"))
	if (unlink ("$OUTPUT_DIR/OSM_Atlas.tex") === False)
		die_error ("Unable to delete $OUTPUT_DIR/OSM_Atlas.tex");

//Check data file exists
if (!file_exists ($DATA_FILE))
	die_error ("Data file $DATA_FILE does not exist.");
//Set up ncurses display
$ncurse = ncurses_init();
// let ncurses know we wish to use the whole screen
$fullscreen = ncurses_newwin ( 0, 0, 0, 0);
// draw a border around the whole thing.
ncurses_border(0,0, 0,0, 0,0, 0,0);
// Write text to screen
ncurses_mvaddstr (0, 2, "OSM-Atlas v$VERSION");
// GPL notice
ncurses_mvaddstr (5, 2, "OSM-Atlas Copyright 2009 Russ Phillips");
ncurses_mvaddstr (6, 2, "This program comes with ABSOLUTELY NO WARRANTY.");
ncurses_mvaddstr (7, 2, "This is free software, and you are welcome to redistribute it");
ncurses_mvaddstr (8, 2, "under certain conditions; see the LICENCE.txt file for details.");

// Hide cursor
ncurses_curs_set (0);
ncurses_refresh();

//Calculate number of steps to be done & initialise $CurrentProgress
if ($DELETE_WORKING_FILES)
	$TotalSteps = 4 + ($HORIZ_TILES * $VERT_TILES * 7);
else
	$TotalSteps = 3 + ($HORIZ_TILES * $VERT_TILES * 7);
$CurrentProgress = 0;

//Set up arrays for index of names. Key is name, value is map number
$asStreetNameIndex = array ();
$asPlaceNameIndex = array ();
//Copy logo image
copy ("osm-logo.png", "$OUTPUT_DIR/osm-logo.png");
if (! file_exists ("$OUTPUT_DIR/osm-logo.png"))
	die_error ("osm-logo.png does not exist");

Progress ($CurrentProgress, "Creating LaTeX file headers");
$latexheader = "\\documentclass[a4paper]{article}\n" .
	"\\usepackage[hmargin=3cm,vmargin=3cm]{geometry}\n" .
	"\\usepackage{array}\n" .
	"\\usepackage{graphicx}\n" .
	"\\usepackage{multicol}\n" .
	"\\usepackage{hyperref}\n" .
	"\\hypersetup{%\n" .
	"pdftitle={" . $PDFTITLE . "},%\n" .
	"pdfauthor={" . $PDFAUTHOR. "},%\n" .
	"pdfsubject={" . $PDFSUBJECT. "},%\n" .
	"pdfcreator={OSM-Atlas (wiki.openstreetmap.org/wiki/OSM-Atlas)},%\n" .
	"pdfkeywords={" . $PDFKEYWORDS . "},%\n" .
	"pdfborder = {0 0 0 0}%\n" .
	"}\n" .
	"\\DeclareGraphicsExtensions{.png}\n" .
	"\\begin{document}\n" .
	"\\begin{titlepage}\n" .
	"\\begin{center}\n" .
	"{\\Huge $TITLE}\n" .
	"\\vfill\n" .
	"\\includegraphics{osm-logo}\n\n" .
	"Created using data from OpenStreetMap\n\n" .
	"\\href{http://www.openstreetmap.org/}{www.openstreetmap.org}\n" .
	"\\end{center}\n" .
	"\\end{titlepage}\n";
	file_put_contents ("$OUTPUT_DIR/OSM_Atlas.tex", $latexheader, FILE_APPEND);

// Create tiles
$tilewidth = ($RIGHT - $LEFT) / $HORIZ_TILES;
$tileheight = ($TOP - $BOTTOM) / $VERT_TILES;
$tTop = $TOP;
$tBottom = $tTop - $tileheight;

//Page counter
$PageNumber = $START_PAGE_NUMBER;

//Generate images
for ($tRow = 1; $tRow <= $VERT_TILES; $tRow++) {
	//New row: reset $tLeft & $tRight
	$tLeft = $LEFT;
	$tRight = $tLeft + $tilewidth;
	for ($tCol = 1; $tCol <= $HORIZ_TILES; $tCol++) {
		$tRight = $tLeft + $tilewidth;

		//Generate images etc for this tile
		tile ($tLeft, $tRight, $tTop, $tBottom, $tRow, $tCol, $PageNumber);

		$tLeft += $tilewidth;
		$PageNumber++;
	}
	//End of row: Set new $tTop & $tBottom
	$tTop -= $tileheight;
	$tBottom = $tTop - $tileheight;
}

//Generate LaTeX for overview map
$latex = "\\begin{tabular}{|";
for ($i = 1; $i <= $HORIZ_TILES; $i++)
	$latex .= "c|";
$latex .= "}\n\\hline\n";
$width = 13 / $HORIZ_TILES;
//Page counter
$PageNumber = $START_PAGE_NUMBER;
for ($tRow = 1; $tRow <= $VERT_TILES; $tRow++) {
	for ($tCol = 1; $tCol <= $HORIZ_TILES; $tCol++) {
		Progress ($CurrentProgress, "Generating overview images for tile $tRow-$tCol");
		//Add number to image
		$imsize = getimagesize ("$OUTPUT_DIR/osm-temp" . "-$tRow-$tCol.png");
		$im = @imagecreatefrompng ("$OUTPUT_DIR/osm-temp" . "-$tRow-$tCol.png");
		$bg = imagecolorallocate ($im, 255, 255, 255);
		$textcolor = imagecolorallocate ($im, 0, 0, 0);

		//Get size of text
		$aTxtSize = imagettfbbox ($OV_FONT_SIZE, 0, $OV_FONT, trim ($PageNumber));
		$iTxtWidth = abs ($aTxtSize [2] - $aTxtSize [0]);
		$iTxtHeight = abs ($aTxtSize [1] - $aTxtSize [7]);
		//Get position for text
		$iTxtLeft = ($imsize [0] - $iTxtWidth) / 2;
		//Note that bottom has to be offset by height of image
		$iTxtBottom = (($imsize [1] - $iTxtHeight) / 2) + $iTxtHeight;

		imagettftext($im, $OV_FONT_SIZE, 0, $iTxtLeft, $iTxtBottom, $textcolor, $OV_FONT, $PageNumber);
		imagepng ($im, "$OUTPUT_DIR/osm-temp" . "-ov-$tRow-$tCol.png");
		imagedestroy ($im);
		//Write LaTeX code for the thumbnail
		$latex .= "\\includegraphics[width={$width}cm]{osm-temp-ov-$tRow-$tCol.png}\n";
		$PageNumber++;
	}
	// newline
	$latex .= "\\\\\n";
}
// Add horizontal line at end of table
$latex .= "\\hline\n";
$latex .= "\\end{tabular}\n\\newpage\n";
file_put_contents ("$OUTPUT_DIR/OSM_Atlas.tex", $latex, FILE_APPEND);

//Page counter
$PageNumber = $START_PAGE_NUMBER;
//Create LaTeX for map pages
for ($tRow = 1; $tRow <= $VERT_TILES; $tRow++) {
	for ($tCol = 1; $tCol <= $HORIZ_TILES; $tCol++) {
		Progress ($CurrentProgress, "Generating LaTeX for tile $tRow-$tCol");
		//Calculate N/E/S/W page numbers
		$npage = $PageNumber - $HORIZ_TILES;
		if ($npage < $START_PAGE_NUMBER)
			$npage = "-";
		if ($tCol == $HORIZ_TILES)
			$epage = "-";
		else
			$epage = (string) $PageNumber + 1;
		$spage = $PageNumber + $HORIZ_TILES;
		if ($spage > ($HORIZ_TILES * $VERT_TILES) + ($START_PAGE_NUMBER - 1))
			$spage = "-";
		if ($tCol == 1)
			$wpage = "-";
		else
			$wpage = (string) $PageNumber - 1;
		//Write LaTeX code for table with image & N/E/S/W page numbers
		$latex = "\\begin{tabular}{m{1cm}m{13cm}m{1cm}}\n" .
 			" &\begin{center}{$npage}\end{center}& \\\\\n" .
 			"$wpage &" .
			"\\includegraphics[width=13cm]{osm-temp" . "-$tRow-$tCol.png}&" .
			" $epage \\\\\n" .
 			" &\begin{center}{$spage}\end{center}& \\\\\n" .
			"\\end{tabular}\n" .
			"\\newpage\n";
		file_put_contents ("$OUTPUT_DIR/OSM_Atlas.tex", $latex, FILE_APPEND);
		$PageNumber++;
	}
}

Progress ($CurrentProgress, "Creating index");
if ($INDEX_PLACE_NAMES == True) {
	// Sort index, remove duplicate lines
	natcasesort ($asPlaceNameIndex);
	$asPlaceNameIndex = array_unique ($asPlaceNameIndex);
	/* Write place index to LaTeX file
	 * Each index entry is a seperate paragraph, with first line not indented
	 * and subsequent lines indented. This makes it easier to read entries
	 * that are too long to fit on a single line
	*/
	$latexfile = fopen ("$OUTPUT_DIR/OSM_Atlas.tex", "a");
	fwrite ($latexfile, "\\begin{center}\n" .
		"{\\Large Index of Place Names}\n" .
		"\\end{center}\n" .
		"\\setlength{\columnseprule}{0.5pt}\n" .
		"\\setlength{\columnsep}{20pt}\n" .
		"\\begin{multicols}{4}\n" .
		"\\begin{footnotesize}\n" .
		"\\begin{raggedright}\n");
	$para_indent = "\\hangindent=0.25cm\n\\hangafter=1\n";
	foreach ($asPlaceNameIndex as $name => $page) {
		$line = html_entity_decode ($name);
		//PHP does not recognise &apos; - so do it by hand
		$line = str_replace ("&apos;", "'", $line);
		//Replace & with \\&
		$line = str_replace ("&", "\\&", $line);
		//Remove duplicates of page numbers
		$numbers = explode (",", $page);
		$numbers = array_unique ($numbers);
		//Put page numbers back into a comma-seperated string
		$pages = implode (", ", $numbers);
		fwrite ($latexfile, "$para_indent$name\t$pages\n\n");
	}
	fwrite ($latexfile, "\\end{raggedright}\n" .
		"\\end{footnotesize}\n" .
		"\\end{multicols}\n" .
		"\\newpage\n");
}

if ($INDEX_STREET_NAMES == True) {
	// Sort index
	ksort ($asStreetNameIndex);
	/* Write street index to LaTeX file
	 * Each index entry is a seperate paragraph, with first line not indented
	 * and subsequent lines indented. This makes it easier to read entries
	 * that are too long to fit on a single line
	*/
	$latexfile = fopen ("$OUTPUT_DIR/OSM_Atlas.tex", "a");
	fwrite ($latexfile, "\\begin{center}\n" .
		"{\\Large Index of Street Names}\n" .
		"\\end{center}\n" .
		"\\setlength{\columnseprule}{0.5pt}\n" .
		"\\setlength{\columnsep}{20pt}\n" .
		"\\begin{multicols}{4}\n" .
		"\\begin{footnotesize}\n" .
		"\\begin{raggedright}\n");
	$para_indent = "\\hangindent=0.25cm\n\\hangafter=1\n";
	foreach ($asStreetNameIndex as $name => $page) {
		$name = html_entity_decode ($name);
		//PHP does not recognise &apos; - so do it by hand
		$name = str_replace ("&apos;", "'", $name);
		//Replace & with \\&
		$name = str_replace ("&", "\\&", $name);
		//Remove duplicates of page numbers
		$numbers = explode (",", $page);
		$numbers = array_unique ($numbers);
		//Put page numbers back into a comma-seperated string
		$pages = implode (", ", $numbers);
		fwrite ($latexfile, "$para_indent$name\t$pages\n\n");
	}
	fwrite ($latexfile, "\\end{raggedright}\n" .
		"\\end{footnotesize}\n" .
		"\\end{multicols}\n" .
		"\\newpage\n");
}

fwrite ($latexfile, "\\begin{titlepage}\n" .
	"This atlas was created by OSM-Atlas " .
	"(\\href{http://www.mappage.org/atlas/}{www.mappage.org/atlas}) " .
	"using data from OpenStreetMap. OpenStreetMap " .
	"creates and provides free geographic data such as street maps to " .
	"anyone who wants them. The project was started because most maps " .
	"you think of as free actually have legal or technical restrictions " .
	"on their use, holding back people from using them in creative, productive," .
	" or unexpected ways.\n\n" .
	"OpenStreetMap data, and this atlas, and is licensed under the Creative " .
	"Commons Attribution-Share Alike 2.0 Generic License. To view a " .
	"copy of this license, visit " .
	"\\href{http://creativecommons.org/licenses/by-sa/2.0/}" .
	"{http://creativecommons.org/licenses/by-sa/2.0/} or send a letter " .
	"to: Creative Commons, 171 Second Street, Suite 300, San " .
	"Francisco, California, 94105, USA.\n" .
	"\\begin{center}\n" .
	"\\includegraphics{osm-logo}\n\n" .
	"\\href{http://www.openstreetmap.org/}{www.openstreetmap.org}\n" .
	"\\end{center}\n" .
	"\\end{titlepage}\n" .
	"\\end{document}\n");
fclose ($latexfile);

// Create PDF file
chdir ($OUTPUT_DIR);
Progress ($CurrentProgress, "Running pdflatex to create PDF file");
$cmd = "pdflatex -interaction=batchmode OSM_Atlas.tex";
shell_exec ("$cmd 2>/dev/null >/dev/null");

// Clean up
chdir ($OUTPUT_DIR);
if ($DELETE_WORKING_FILES) {
	//pause for 5 seconds
	Progress ($CurrentProgress, "Deleting working files");
	delfile ("*.osm");
	delfile ("*.svg");
	delfile ("*.png");
	delfile ("*.aux");
	delfile ("*.tex");
	delfile ("*.out");
	//Delete LaTeX log file
	delfile (ereg_replace ("\.tex$", ".log", OSM_Atlas.tex));
}

//Log finish & clean up ncurses
file_put_contents ($LOG_FILE, "Finished at " . date ("H:i jS M Y") . "\n", FILE_APPEND);
ncurses_end();
?>
