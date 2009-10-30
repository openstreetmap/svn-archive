<?php 
/* WMS Tile Server

	JP80 29/10/09	- Added HTTP parameter source=OS1|OS7|NPE to choose the map
					- Added auto zoom selection
					- Added easily switchable debug mode from within script (debugging)
					- Added HTTP parameter tileinfo to show zoom level on map (debugging)
					
	Note that JOSM or WMS plugin is quite eager to cache things - can make debugging faults a bit
	confusing. Better to test with a browser directly:
	map.php?source=os1&bbox=-0.6550125,51.7603802,-0.6478145,51.7675781&srs=EPSG:4326&width=500&height=499
*/

try
{
require_once ('generalfuncs.php');
if(!file_exists('defines.php')) throw new Exception("defines.php does not exist or permission denied");
require_once ('defines.php');
require_once ('wmsdrawfuncs.php');

list($width,$height) = GetWindowSize();

require_once ('painter.php');
require_once ('coordtotile.2.php');

$source=strtolower(GetInputVar('source'));

# Set to 1 to display information in browser (instead of drawing an image)
$debug=0;

switch($source)
	{
	case "os1":
		$mapFolder = OS1_MAP_FOLDER;
		$zoom_max = OS1_ZOOM_MAX;
		$zoom_min = OS1_ZOOM_MIN;
		break;
	
	case "os7":
		$mapFolder = OS7_MAP_FOLDER;
		$zoom_max = OS7_ZOOM_MAX;
		$zoom_min = OS7_ZOOM_MIN;
		break;
	
	case "npe":
		$mapFolder = NPE_MAP_FOLDER;
		$zoom_max = NPE_ZOOM_MAX;
		$zoom_min = NPE_ZOOM_MIN;
		break;

	default:
		$mapFolder = DEFAULT_MAP_FOLDER;
		$zoom_max = DEFAULT_ZOOM_MAX;
		$zoom_min = DEFAULT_ZOOM_MIN;
		break;
	}

if(!file_exists($mapFolder)) throw new Exception("Map folder does not exist or permission denied\n".$mapFolder);

//phpinfo(); exit();
//print_r($_SERVER); exit();

SetTimeOutFromInputVar();
$srs = GetInputVar('srs');

/* Live debug info */
if (GetInputVar('tileinfo')==true) $tileinfo=1;

//Catch the arguments used and save to log file
//$logfile = fopen("quicklog.txt","w");
//fwrite($logfile, time()."\n");
//fwrite($logfile, $_SERVER['REQUEST_URI']."\n");

//Get list of tiles that will be needed
$bbox = GetBBox();
$left = RoundDown($bbox[0],0.1);
$bottom = RoundDown($bbox[1],0.1);
$right = RoundUp($bbox[2],0.1);
$top = RoundUp($bbox[3],0.1);

if ($debug) {
	echo $left.' '.$bottom.' '.$right.' '.$top."<br/>\n<br>";
	}


$painter = new GDPainter();
$painter->createImage($width, $height, 0, 0, 0);

//TrySetImageFormat($image,'jpg',1);
//$image->setCompressionQuality(40);

/* 
	TimSC: Check at run time which zoom layers are available on the server

*/
	$zoomMinOnServer = -1;
	$zoomMaxOnServer = -1; 
	$zoomRangeSet = 0;  
	for($zoom = $zoom_max; $zoom > $zoom_min; $zoom--)
	{		
		$zoomFolderName = $mapFolder.'/'.$zoom;
		if ($debug) echo $zoom." ".$zoomFolderName." exists=".file_exists($zoomFolderName)."<br/>\n";
		$exists = file_exists($zoomFolderName);
		if($exists && (!$zoomRangeSet || $zoomMaxOnServer < $zoom)) $zoomMaxOnServer = $zoom;
		if($exists && (!$zoomRangeSet || $zoomMinOnServer > $zoom)) $zoomMinOnServer = $zoom;
		if($exists) $zoomRangeSet = 1;
	}
	if ($debug) echo "Zoom range on server " . $zoomMinOnServer. " to " . $zoomMaxOnServer."<br/>\n";
	$zoom_max = $zoomMaxOnServer;
	$zoom_min = $zoomMinOnServer;

	if($zoomRangeSet == 0) throw new Exception("No zoom folders found in folder:\n".$mapFolder);

/*	JP80: Find best zoom level for chosen area. 
	General idea is to iterate through each zoom level and calculate the projected tile size.
		
	We find the tile that's closest to 256, to avoid stretching or squashing the tile image.

	I'm sure this could be done in a lot less code but I don't fully understand the projection process!
	*/

$zoom_level=$zoom_max;

do 	{

	$zoom_test=17-$zoom_level;
	
	$tileBL = new Tile($bottom,$left,$zoom_test);
	$tileBLX = $tileBL->getTileCoord(); 
	$tileBLX = $tileBLX->x;
	$tileBLY = $tileBL->getTileCoord();
	$tileBLY = $tileBLY->y;

	/* Take the bottom left tile, and work out its size */
	$tile1 = new TileInv($tileBLX,$tileBLY,$zoom_test);
	$tile1Pos = $tile1->getTileLatLong();
	$tile2 = new TileInv($tileBLX+1,$tileBLY+1,$zoom_test);
	$tile2Pos = $tile2->getTileLatLong();

	$tileL = $tile1Pos->x; if($tileL > $tile2Pos->x) $tileL = $tile2Pos->x;
	$tileR = $tile1Pos->x; if($tileR < $tile2Pos->x) $tileR = $tile2Pos->x;
	$tileT = $tile1Pos->y; if($tileT < $tile2Pos->y) $tileT = $tile2Pos->y;
	$tileB = $tile1Pos->y; if($tileB > $tile2Pos->y) $tileB = $tile2Pos->y;	

	$winTL = Project($tileB,$tileL,$srs);
	$winBR = Project($tileT,$tileR,$srs);

	$winHeight = $winTL[1] - $winBR[1];

	$winWidth = $winBR[0] - $winTL[0];
	if ($debug) echo "z".$zoom_level." tiles are ".$winWidth." x ".$winHeight."<br>";
	
	if ($winWidth>TILE_WIDTH) {
		# Check if the previous tile was closer
		if ($winWidth-TILE_WIDTH < $prev_diff) {
			$zoom_level++;
			}
		break;
		}
	
	$prev_diff=$winWidth;
	
	# 
	if ($zoom_level<=$zoom_min || $zoom_level<0) break;
	
	# Move up a zoom level
	$zoom_level--;
	
	} while (1);

if ($debug) echo "..Using z".$zoom_level."<br>";

$zoom_directory = $zoom_level;
$zoom = 17 - $zoom_level;

$tileBL = new Tile($bottom,$left,$zoom);
$tileBLX = $tileBL->getTileCoord(); 
$tileBLX = $tileBLX->x;
$tileBLY = $tileBL->getTileCoord();
$tileBLY = $tileBLY->y;

$tileTR = new Tile($top,$right,$zoom);
$tileTRX = $tileTR->getTileCoord(); 
$tileTRX = $tileTRX->x;
$tileTRY = $tileTR->getTileCoord();
$tileTRY = $tileTRY->y;  

if ($debug) {
	echo "TR  X: ".$tileTRX.", Y: ".$tileTRY.", Z: ".$zoom."<br>\n";
	echo "BL  X: ".$tileBLX.", Y: ".$tileBLY.", Z: ".$zoom."<br>\n";
	}

/* Step through OSM tiles and look for files that exist */
for($i=$tileTRY;$i<=$tileBLY;$i += 1)
{
	for($j=$tileBLX;$j<=$tileTRX;$j += 1)
	{
		$tileName = $zoom_directory.'/'.$j.'/'.$i;

		if ($debug) {
			echo $tileName.'<br/>';
			echo $mapFolder.'/'.$tileName.'.jpg'.'<br/>';
			}

		$mapFilename = null;

		if(file_exists($mapFolder.'/'.$tileName.'.PNG')) $mapFilename = $mapFolder.'/'.$tileName.'.PNG';
		elseif(file_exists($mapFolder.'/'.$tileName.'.png')) $mapFilename = $mapFolder.'/'.$tileName.'.png';
		elseif(file_exists($mapFolder.'/'.$tileName.'.JPG')) $mapFilename = $mapFolder.'/'.$tileName.'.JPG';
		elseif(file_exists($mapFolder.'/'.$tileName.'.jpg')) $mapFilename = $mapFolder.'/'.$tileName.'.jpg';

		if($mapFilename!= NULL)
		{

			if ($debug) echo 'found ' .$tileName.'<br/>';
			/* There is a tile, so load it */
			$painterTile = new GDPainter();
			$painterTile->createFromFile($mapFilename);
			
			/* Is this always 256x256? */
			$height = $painterTile->getImageHeight();
			$width = $painterTile->getImageWidth();
			
			//Get tile lat lon of corners
			//print_r(GetBitmapCoordinate(52.0,-5.0,3));
			//echo $i. ' '.$j."\n";
			
			/* Create tile boundaries and get lat/long */
			$tile1 = new TileInv($j,$i,$zoom);
			$tile1Pos = $tile1->getTileLatLong();
			$tile2 = new TileInv($j+1,$i+1,$zoom);
			$tile2Pos = $tile2->getTileLatLong();
			//print_r($tile1Pos);
			//print_r($tile2Pos);
			$tileL = $tile1Pos->x; if($tileL > $tile2Pos->x) $tileL = $tile2Pos->x;
			$tileR = $tile1Pos->x; if($tileR < $tile2Pos->x) $tileR = $tile2Pos->x;
			$tileT = $tile1Pos->y; if($tileT < $tile2Pos->y) $tileT = $tile2Pos->y;
			$tileB = $tile1Pos->y; if($tileB > $tile2Pos->y) $tileB = $tile2Pos->y;	
			if ($debug) echo $tileL.' '.$tileR.' '.$tileT.' '.$tileB;

			/* Project image square to correct size for tiling */
			$winTL = Project($tileB,$tileL,$srs);
			$winBR = Project($tileT,$tileR,$srs);
			
			$winHeight = $winTL[1] - $winBR[1];
			$winWidth = $winBR[0] - $winTL[0];

			//print_r($winBR);
			//echo $winHeight .' ' .$winWidth. ' ';
			$painter->imageCopyResized($painterTile, $winTL[0], $winBR[1], 0, 0, $winWidth, $winHeight, $width, $height);
			
			if ($tileinfo) {
				$red=imagecolorallocate($painter->im, 255, 0, 0);
				$painter->drawText(50,50,30,"z".(17-$zoom).$addt, $red);
				}
			
		}
	}
}

if ($debug) exit();

header( "Content-Type: image/png" );
$painter->renderImage();
//header( "Content-Type: image/jpeg" );
//$painter->renderImageJpeg();

//$painter->saveToFile("out.jpg");
//echo $image->getImageBlob( );


}
catch (Exception $e) {
	SendImageWithTextMessage('WMS:'.$e->getMessage(),$width, $height);
}


?>

