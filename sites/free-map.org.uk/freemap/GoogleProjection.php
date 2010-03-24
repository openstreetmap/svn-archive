<?php

// Note that this is based on GoogleProjection.cpp from the Freemap rendering
// code, which in turn was based on the Python tile generation script 
// generate_tiles.py from OSM.

function minmax($a,$b,$c)
{
	$a = max($a,$b);
	$a = min($a,$c);
	return $a;
}

class GoogleProjection
{
   var  $Bc,$Cc,$zc,$Ac;

   function GoogleProjection($levels=18)
   {
   		$this->Bc =array();
   		$this->Cc =array();
   		$this->zc =array();
   		$this->Ac =array();

        $c = 256;
        for ($d=0; $d<$levels; $d++) 
		{
            $e = $c/2;
            $this->Bc[] = $c/360.0;
            $this->Cc[] = ($c/(2 * M_PI));
            $this->zc[] = $e;
            $this->Ac[] = $c;
            $c *= 2;
		}
	}
                
	function fromLLtoPixel($lon,$lat,$zoom)
	{
		$d = $this->zc[$zoom];
		$e = round($d + $lon * $this->Bc[$zoom]);
		$f = minmax(sin((M_PI/180.0) * $lat),-0.9999,0.9999);
		$g = round($d + 0.5*log((1+$f)/(1-$f))*-$this->Cc[$zoom]);
		return array($e,$g);
	}

	function fromPixelToLL($x,$y,$zoom)
	{
		$e = $this->zc[$zoom];
		$f = ($x - $e)/$this->Bc[$zoom];
		$g = ($y - $e)/-$this->Cc[$zoom];
		$h = (180.0/M_PI) * ( 2 * atan(exp($g)) - 0.5 * M_PI);
		return array($f,$h);
	}
}

/*
$goog = new GoogleProjection(18);
$xy=$goog->fromLLToPixel(-0.72,51.05,13);
echo floor($xy['x'] / 256) . " " . floor($xy['y']/256). "<br />";
$latlon = $goog->fromPixelToLL(4079*256,2740*256,13);
$latlon2 = $goog->fromPixelToLL(4079*256,2741*256,13);
print_r($latlon);
print_r($latlon2);
*/
?>
