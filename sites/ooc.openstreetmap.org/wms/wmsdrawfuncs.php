<?php

require_once('wmsfuncs.php');

function DrawLine(&$image, $lat1, $lon1, $lat2, $lon2)
{
	$polylineArg = array();
	list($polylineArg[0],$polylineArg[1])= Project($lat1,$lon1);
	list($polylineArg[2],$polylineArg[3])= Project($lat2,$lon2);

	$imagickDraw = new ImagickDraw();
	$imagickDraw->line($polylineArg[0],$polylineArg[1],$polylineArg[2],$polylineArg[3]);
	//print_r($polylineArg);
	$image->drawImage( $imagickDraw );

	//$image->ployline( $polylineArg );
}

function DrawLonLine(&$image,$lon)
{
	list ($left, $bottom, $right, $top) = GetBBox();
	DrawLine($image,$top, $lon,$bottom, $lon);	
}

function DrawLatLine(&$image,$lat)
{
	list ($left, $bottom, $right, $top) = GetBBox();
	DrawLine($image,$lat, $left,$lat, $right);
}

function DrawGrid(&$image, $step)
{
	list ($left, $bottom, $right, $top) = GetBBox();

	$leftRound = RoundUp($left,$step);
	$rightRound = RoundDown($right,$step);
	for($i=$leftRound;$i<=$rightRound;$i += $step)
	{
		DrawLonLine($image,$i);
	}

	$bottomRound = RoundUp($bottom,$step);
	$topRound = RoundDown($top,$step);
	//echo $bottomRound.' ' .$topRound;
	for($i=$bottomRound;$i<=$topRound;$i += $step)
	{
		DrawLatLine($image,$i);
	}
}

function GetCalcAndDrawMapFragment(&$outputBuffer,&$sourceMap,$sourcePoints,$outputPoints)
{
	//Get map fragment
	$margin = 1;
	$resource2=GetSubImage($sourceMap,$sourcePoints[0][0],$sourcePoints[0][1],
		$sourcePoints[1][0],$sourcePoints[1][1],
		$sourcePoints[2][0],$sourcePoints[2][1],$margin);

	//Calculate affine transform
	$p = array($sourcePoints[0],$sourcePoints[1],$sourcePoints[2]);
	$q = array($outputPoints[0],$outputPoints[1],$outputPoints[2]);
	require_once('affinefit.php');
	$trans = AffineFit($p,$q);
	//global $file; fwrite($file, serialize($trans));

	//Draw map fragment to output buffer with affine transform
	$ImagickDraw = new ImagickDraw();
	$ImagickDraw->setFillColor(new ImagickPixel('yellow'));
	$affine=array();
	$affine['sx'] = $trans['A'][0][0];
	$affine['sy'] = $trans['A'][1][1];
	$affine['rx'] = $trans['A'][0][1];
	$affine['ry'] = $trans['A'][1][0];
	$affine['tx'] = $trans['t'][0];
	$affine['ty'] = $trans['t'][1];
	/*$ImagickDraw->point( $waypoint1[0] ,$waypoint1[1] );
	$ImagickDraw->point( $waypoint2[0] ,$waypoint2[1] );
	$ImagickDraw->point( $waypoint3[0] ,$waypoint3[1] );*/

	//$ImagickDraw->translate(-160.0,0.0);


	$ImagickDraw->affine($affine);
//print_r($affine);exit();
	$ImagickDraw->composite(imagick::COMPOSITE_OVER,0,0,-1,-1,$resource2);
	$outputBuffer->drawImage( $ImagickDraw );

}

function DrawNativeSquare(&$outputBuffer,&$sourceMap,$sourcePoints,$outputPoints)
{
	//Draw map fragment to output buffer with affine transform
	$ImagickDraw = new ImagickDraw();

	//print_r($outputPoints);

	$winCoordXMin = min($outputPoints[0][0],$outputPoints[1][0],$outputPoints[2][0],$outputPoints[3][0]);
	$winCoordXMax = max($outputPoints[0][0],$outputPoints[1][0],$outputPoints[2][0],$outputPoints[3][0]);
	$winCoordYMin = min($outputPoints[0][1],$outputPoints[1][1],$outputPoints[2][1],$outputPoints[3][1]);
	$winCoordYMax = max($outputPoints[0][1],$outputPoints[1][1],$outputPoints[2][1],$outputPoints[3][1]);
	//echo $winCoordYMin.' '.$winCoordYMax."<br/>\n";

	$winWidth = $winCoordXMax - $winCoordXMin;
	$winHeight = $winCoordYMax - $winCoordYMin;
	//echo $winWidth.' '.$winHeight."<br/>\n";
	
	$height = $sourceMap->GetImageHeight();
	$width = $sourceMap->GetImageWidth();

	$ImagickDraw->translate($winCoordXMin,$winCoordYMin);
	$ImagickDraw->scale($winWidth/$width, $winHeight/$height);

	$ImagickDraw->composite(imagick::COMPOSITE_OVER,0,0,-1,-1,$sourceMap);
	$outputBuffer->drawImage( $ImagickDraw );

}

function DrawNativeQuads(&$outputBuffer,&$sourceMap,$quads)
{
	//print_r(GetWindowSize()); echo'<br/>';
	//print_r(GetBBox());echo'<br/>';

	foreach ($quads as $key => $quad)
	{
		$screenDst = array();
		foreach ($quad[1] as $key => $value)
		{
			//print_r($value); print_r(Project($value[0], $value[1]));echo'<br/>';
			
			array_push($screenDst, Project($value[0], $value[1]));
			
		}
		//print_r($screenDst);

		DrawNativeSquare($outputBuffer,$sourceMap,
			array($quad[0][0],$quad[0][1],$quad[0][2],$quad[0][3]),
			array($screenDst[0],$screenDst[1],$screenDst[2],$screenDst[3]));
	}
}

?>
