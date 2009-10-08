<?php 

$width = 300;
$height = 300;

try
{
require_once ('defines.php');
require_once ('generalfuncs.php');
require_once ('wmsdrawfuncs.php');
list($width,$height) = GetWindowSize();

if(!file_exists(MAP_FOLDER))
{
	throw new Exception("MAP_FOLDER does not exist or permission denied");
}

require_once ('painter.php');
require_once ('coordtotile.2.php');

//phpinfo(); exit();
//print_r($_SERVER); exit();

SetTimeOutFromInputVar();
$srs = GetInputVar('srs');

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
//echo $left.' '.$bottom.' '.$right.' '.$top."<br/>\n";

$painter = new GDPainter();
$painter->createImage($width, $height,0,0,0);

//TrySetImageFormat($image,'jpg',1);
//$image->setCompressionQuality(40);

//Get bounds of tiles we need to render
$zoom = 3;

$tileTR = new Tile($top,$right,$zoom);
$tileBL = new Tile($bottom,$left,$zoom);

$tileTRX = $tileTR->getTileCoord(); 
$tileTRX = $tileTRX->x;
$tileTRY = $tileTR->getTileCoord();
$tileTRY = $tileTRY->y;  

$tileBLX = $tileBL->getTileCoord(); 
$tileBLX = $tileBLX->x;
$tileBLY = $tileBL->getTileCoord();
$tileBLY = $tileBLY->y;
//echo "X: ".$tileTRX.", Y: ".$tileTRY.", Z: ".$zoom."<br>\n";
//echo "X: ".$tileBLX.", Y: ".$tileBLY.", Z: ".$zoom."<br>\n";

for($i=$tileTRY;$i<$tileBLY;$i += 1)
{
	for($j=$tileBLX;$j<$tileTRX;$j+=1)
	{
		$tileName = $j.'/'.$i;

		//echo $tileName.'<br/>';
		//echo MAP_FOLDER.'/'.$tileName.'.jpg'.'<br/>';

		$mapFilename = null;
		if(file_exists(MAP_FOLDER.'/'.$tileName.'.PNG')) $mapFilename = MAP_FOLDER.'/'.$tileName.'.PNG';
		if(file_exists(MAP_FOLDER.'/'.$tileName.'.png')) $mapFilename = MAP_FOLDER.'/'.$tileName.'.png';
		if(file_exists(MAP_FOLDER.'/'.$tileName.'.JPG')) $mapFilename = MAP_FOLDER.'/'.$tileName.'.JPG';
		if(file_exists(MAP_FOLDER.'/'.$tileName.'.jpg')) $mapFilename = MAP_FOLDER.'/'.$tileName.'.jpg';

		if($mapFilename!= NULL)
		{
			//echo 'found ' .$tileName.'<br/>';

			$painterTile = new GDPainter();
			$painterTile->createFromFile($mapFilename);
			
			$height = $painterTile->getImageHeight();
			$width = $painterTile->getImageWidth();

			//Get tile lat lon of corners
			//print_r(GetBitmapCoordinate(52.0,-5.0,3));
			//echo $i. ' '.$j."\n";
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
			//echo $tileL.' '.$tileR.' '.$tileT.' '.$tileB;

			$winTL = Project($tileB,$tileL,$srs);
			$winBR = Project($tileT,$tileR,$srs);
			$winHeight = $winTL[1] - $winBR[1];
			$winWidth = $winBR[0] - $winTL[0];
			//print_r($winTL);
			//print_r($winBR);
			//echo $winHeight .' ' .$winWidth. ' ';
			$painter->imageCopyResized($painterTile, $winTL[0],$winBR[1] ,0 ,0 ,$winWidth,$winHeight,$width,$height);
			
		}
	}
}

//exit();

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

