<?php

# Constants for the UK gridref system
define ('N0',  -100000);
define('E0', 400000);
define( 'F0', 0.9996012717);
define ('PHI0',    49.0);
define ('LAMBDA0', -2.0);
define('A',        6377563.396);
define('B',        6356256.910);

// NOTICE OF AUTHORSHIP: These are PHP versions of C functions from 
// the LGPL JEEPS library, of which author and licence info follows....

/********************************************************************
** @source JEEPS arithmetic/conversion functions
**
** @author Copyright (C) 1999 Alan Bleasby
** @version 1.0 
** @modified Dec 28 1999 Alan Bleasby. First version
** @@
** 
** This library is free software; you can redistribute it and/or
** modify it under the terms of the GNU Library General Public
** License as published by the Free Software Foundation; either
** version 2 of the License, or (at your option) any later version.
** 
** This library is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
** Library General Public License for more details.
** 
** You should have received a copy of the GNU Library General Public
** License along with this library; if not, write to the
** Free Software Foundation, Inc., 59 Temple Place - Suite 330,
** Boston, MA  02111-1307, USA.
********************************************************************/


// ll_to_gr()
// Slightly modified version of:
/* @func GPS_Math_Airy1830LatLonToNGEN **************************************
**
** Convert Airy 1830 datum latitude and longitude to UK Ordnance Survey
** National Grid Eastings and Northings
**
** @param [r] phi [double] WGS84 latitude     (deg)
** @param [r] lambda [double] WGS84 longitude (deg)
** @param [w] E [double *] NG easting (metres)
** @param [w] N [double *] NG northing (metres)
**
** @return [void]
*/

// Modifications:
// Altered to take advantage of PHP associative arrays.

function ll_to_gr ( $pos )
{
    $gridref = GPS_Math_LatLon_To_EN($pos['lat'],$pos['long'],N0,E0,PHI0,
										LAMBDA0,F0, A,B);

    return $gridref;
}

/* @func  GPS_Math_LatLon_To_EN **********************************
**
** Convert latitude and longitude to eastings and northings
** Standard Gauss-Kruger Transverse Mercator
**
** @param [w] E [double *] easting (metres)
** @param [w] N [double *] northing (metres)
** @param [r] phi [double] latitude (deg)
** @param [r] lambda [double] longitude (deg)
** @param [r] N0 [double] true northing origin (metres)
** @param [r] E0 [double] true easting  origin (metres)
** @param [r] phi0 [double] true latitude origin (deg)
** @param [r] lambda0 [double] true longitude origin (deg)
** @param [r] F0 [double] scale factor on central meridian
** @param [r] a [double] semi-major axis (metres)
** @param [r] b [double] semi-minor axis (metres)
**
** @return [void]
************************************************************************/
function GPS_Math_LatLon_To_EN($phi, $lambda, $N0, $E0, $phi0,
								$lambda0, $F0, $a, $b)
{
	// Replaced these deg/rad conversions (originally done with a function)
	// with simple multiplication
    $phi0    *= (M_PI/180.0); 
    $lambda0 *= (M_PI/180.0); 
    $phi     *= (M_PI/180.0); 
    $lambda  *= (M_PI/180.0); 
    
    $esq = (($a*$a)-($b*$b)) / ($a*$a);
    $n   = ($a-$b) / ($a+$b);
    
    $tmp  = 1.0 - ($esq * sin($phi) * sin($phi));
    $nu   = $a * $F0 * pow($tmp,-0.5);
    $rho  = $a * $F0 * (1.0 - $esq) * pow($tmp,-1.5);
    $etasq = ($nu / $rho) - 1.0;

    $fdf   = 5.0 / 4.0;
    $tmp   = 1.0 + $n + ($fdf * $n * $n) + ($fdf * $n * $n * $n);
    $tmp  *= ($phi - $phi0);
    $tmp2  = 3.0*$n + 3.0*$n*$n + (21.0/8.0)*$n*$n*$n;
    $tmp2 *= (sin($phi-$phi0) * cos($phi+$phi0));
    $tmp  -= $tmp2;

    $fde   = (15.0 / 8.0);
    $tmp2  = (($fde*$n*$n) + ($fde*$n*$n*$n)) * sin(2.0 * ($phi-$phi0));
    $tmp2 *= cos(2.0 * ($phi+$phi0));
    $tmp  += $tmp2;
    
    $tmp2  = (35.0/24.0) * $n * $n * $n;
    $tmp2 *= sin(3.0 * ($phi-$phi0));
    $tmp2 *= cos(3.0 * ($phi+$phi0));
    $tmp  -= $tmp2;

    $M     = $b * $F0 * $tmp;
    $I     = $M + $N0;
    $II    = ($nu / 2.0) * sin($phi) * cos($phi);
    $III   = ($nu / 24.0) * sin($phi) * cos($phi) * cos($phi) * cos($phi);
    $III  *= (5.0 - (tan($phi) * tan($phi)) + (9.0 * $etasq));
    $IIIA  = ($nu / 720.0) * sin($phi) * pow(cos($phi),5.0);
    $IIIA *= (61.0 - (58.0*tan($phi)*tan($phi)) +
	     pow(tan($phi),4.0));
    $IV    = $nu * cos($phi);

    $tmp   = pow(cos($phi),3.0);
    $tmp  *= (($nu/$rho) - tan($phi) * tan($phi));
    $V     = ($nu/6.0) * $tmp;

    $tmp   = 5.0 - (18.0 * tan($phi) * tan($phi));
    $tmp  += tan($phi)*tan($phi)*tan($phi)*tan($phi) + (14.0 * $etasq);
    $tmp  -= (58.0 * tan($phi) * tan($phi) * $etasq);
    $tmp2  = cos($phi)*cos($phi)*cos($phi)*cos($phi)*cos($phi) * $tmp;
    $VI    = ($nu / 120.0) * $tmp2;
    
    $gridref['n'] = $I + $II*($lambda-$lambda0)*($lambda-$lambda0) +
	     $III*pow(($lambda-$lambda0),4.0) +
	     $IIIA*pow(($lambda-$lambda0),6.0);

    $gridref['e']=$E0 + $IV*($lambda-$lambda0)+$V*pow(($lambda-$lambda0),3.0) +
	 $VI * pow(($lambda-$lambda0),5.0);

    return $gridref;
}

// gr_to_ll()
// Slightly modified version of:
/* @func GPS_Math_NGENToAiry1830LatLon **************************************
**
** Convert  to UK Ordnance Survey National Grid Eastings and Northings to
** Airy 1830 datum latitude and longitude
**
** @param [r] E [double] NG easting (metres)
** @param [r] N [double] NG northing (metres)
** @param [w] phi [double *] Airy latitude     (deg)
** @param [w] lambda [double *] Airy longitude (deg)
**
** @return [void]
************************************************************************/

// Modifications:
// Altered to take advantage of PHP associative arrays.
function gr_to_ll($gridref)
{
    $point = GPS_Math_EN_To_LatLon($gridref['e'],$gridref['n'],
									N0,E0,PHI0,LAMBDA0,F0,A,B);
    
    return $point;
}

/* @func  GPS_Math_EN_To_LatLon **************************************
**
** Convert Eastings and Northings to latitude and longitude
**
** @param [w] E [double] NG easting (metres)
** @param [w] N [double] NG northing (metres)
** @param [r] phi [double *] Airy latitude     (deg)
** @param [r] lambda [double *] Airy longitude (deg)
** @param [r] N0 [double] true northing origin (metres)
** @param [r] E0 [double] true easting  origin (metres)
** @param [r] phi0 [double] true latitude origin (deg)
** @param [r] lambda0 [double] true longitude origin (deg)
** @param [r] F0 [double] scale factor on central meridian
** @param [r] a [double] semi-major axis (metres)
** @param [r] b [double] semi-minor axis (metres)
**
** @return [void]
************************************************************************/
function GPS_Math_EN_To_LatLon($E,  $N, 
			   $N0, $E0,
			   $phi0, $lambda0,
			   $F0, $a, $b)
{
    $nphi=0.0;
    
	// Replaced these deg/rad conversions (originally done with a function)
	// with simple multiplication
    $phi0    *= (M_PI/180.0); 
    $lambda0 *= (M_PI/180.0); 

    $n     = ($a-$b) / ($a+$b);
    $fdf   = 5.0 / 4.0;
    $fde   = (15.0 / 8.0);

    $esq = (($a*$a)-($b*$b)) / ($a*$a);


    $phix = (($N-$N0)/($a*$F0)) + $phi0;
    
    $tmp  = 1.0 - ($esq * sin($phix) * sin($phix));
    $nu   = $a * $F0 * pow($tmp,-0.5);
    $rho  = $a * $F0 * (1.0 - $esq) * pow($tmp,-1.5);
    $etasq = ($nu / $rho) - 1.0;

    $M = -1e20;

	
	for($c=1; $c<=10; $c++)
    //while($N-$N0-$M > 0.000001)
    {
	$nphi = $phix;
	
	$tmp   = 1.0 + $n + ($fdf * $n * $n) + ($fdf * $n * $n * $n);
	$tmp  *= ($nphi - $phi0);
	$tmp2  = 3.0*$n + 3.0*$n*$n +
	        (21.0/8.0)*$n*$n*$n;
	$tmp2 *= (sin($nphi-$phi0) * cos($nphi+$phi0));
	$tmp  -= $tmp2;


	$tmp2  = (($fde*$n*$n)+($fde*$n*$n*$n))*sin(2.0*($nphi-$phi0));
	$tmp2 *= cos(2.0 * ($nphi+$phi0));
	$tmp  += $tmp2;
    
	$tmp2  = (35.0/24.0) * $n * $n * $n;
	$tmp2 *= sin(3.0 * ($nphi-$phi0));
	$tmp2 *= cos(3.0 * ($nphi+$phi0));
	$tmp  -= $tmp2;

	$M     = $b * $F0 * $tmp;

	if($N-$N0-$M > 0.000001)
	    $phix = (($N-$N0-$M)/($a*$F0)) + $nphi;
    }
    

    $VII  = tan($nphi) / (2.0 * $rho * $nu);

    $tmp  = 5.0 + 3.0 * tan($nphi) * tan($nphi) + $etasq;
    $tmp -= 9.0 * tan($nphi) * tan($nphi) * $etasq;
    $VIII = (tan($nphi)*$tmp) / (24.0 * $rho * $nu*$nu*$nu);

    $tmp  = 61.0 + 90.0 * tan($nphi) * tan($nphi);
    $tmp += 45.0 * pow(tan($nphi),4.0);
    $IX   = tan($nphi) / (720.0 * $rho * pow($nu,5.0)) * $tmp;

    $X    = 1.0 / (cos($nphi) * $nu);

    $tmp  = ($nu / $rho) + 2.0 * tan($nphi) * tan($nphi);
    $XI   = (1.0 / (cos($nphi) * 6.0 * $nu*$nu*$nu)) * $tmp;

    $tmp  = 5.0 + 28.0 * tan($nphi)*tan($nphi);
    $tmp += 24.0 * pow(tan($nphi),4.0);
    $XII  = (1.0 / (120.0 * pow($nu,5.0) * cos($nphi)))
	   * $tmp;

    $tmp  = 61.0 + 662.0 * tan($nphi) * tan($nphi);
    $tmp += 1320.0 * pow(tan($nphi),4.0);
    $tmp += 720.0  * pow(tan($nphi),6.0);
    $XIIA = (1.0 / (cos($nphi) * 5040.0 * pow($nu,7.0)))
	   * $tmp;

    $point['lat'] = $nphi - $VII*pow(($E-$E0),2.0) + $VIII*pow(($E-$E0),4.0) -
	   $IX*pow(($E-$E0),6.0);
    
    $point['long'] = $lambda0 + $X*($E-$E0) - $XI*pow(($E-$E0),3.0) +
	      $XII*pow(($E-$E0),5.0) - $XIIA*pow(($E-$E0),7.0);

	// Replaced these rad/deg conversions (originally done with a function)
	// with simple multiplication
    $point['lat']   *= (180.0/M_PI); 
    $point['long'] *= (180.0/M_PI); 

    return $point;
}
