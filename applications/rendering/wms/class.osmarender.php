<?php

/// @author Iván Sánchez Ortega <ivan@sanchezortega.es>

/**
    OSM WMS ("OpenStreetMap Web Map Service")
    Copyright (C) 2008, Iván Sánchez Ortega

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/    

/// Osmarender class
/// This will pass OSM data to osmarender, get the resulting .svg, rasterize it, return it to the client.


class osmarender /* extends layer */
{

static public function GetCapabilities()
{
// return "
//       <Layer queryable='0' opaque='0'>
//         <Name>wireframe</Name>
//         <Title>OSM Wireframe</Title>
//         <EX_GeographicBoundingBox>
//           <westBoundLongitude>-180</westBoundLongitude>
//           <eastBoundLongitude>180</eastBoundLongitude>
//           <southBoundLatitude>-90</southBoundLatitude>
//           <northBoundLatitude>90</northBoundLatitude>
//         </EX_GeographicBoundingBox>
//         <Style>
//           <Name>Default</Name>
//           <Title>Default</Title>
//         </Style>
//       </Layer>
// ";

// Capabilities for WMS 1.1.0 
return "
      <Layer queryable='0' opaque='0'>
        <Name>osmarender</Name>
        <Title>Osmarender</Title>
	<Style>
          <Name>Default</Name>
          <Title>Default</Title>
        </Style>
      </Layer>
";


// 	<BoundingBox SRS='EPSG:4326' minx='-180' miny='-90' maxx='180' maxy='90' />
// 	<BoundingBox SRS='EPSG:32630' minx='-1050000' miny='3000000' maxx='1150000' maxy='5000000' />


}


static public function GetMap($bbox,$crs,$height,$width,$format)
{

// 	datafactory::get_parsed_data($bbox,$crs,$nodes,$ways,$relations);
	/// TODO: check whether we're using UTM or not, set the factor if needed.
	/// TODO: check osmarender's way of scaling things, to determine the right factor.
	/// TODO: change the osmarender stylesheet depending on the layer's style.
	$factor = 0.00001;

	/// TODO: Generate unique temporal filenames
	$filename_osm = "/tmp/wms_temp.osm";
	$filename_svg = "/tmp/wms_temp.svg"; // osmarender will automatically generate this, no need to pass it on to it
	$filename_png = "/tmp/wms_temp.png";
	@unlink ($filename_osm);
	@unlink ($filename_svg);
	@unlink ($filename_png);


	file_put_contents ( $filename_osm , datafactory::get_data_as_projected_osm($bbox,$crs,$factor));
	
	list ($outer_left,$outer_bottom,$outer_right,$outer_top) = datafactory::get_outerbounds();
	
	// call osmarender to generate the .svg
	/// TODO: migrate to or/p
	chdir ("osmarender6");	$osmarender_output = shell_exec("./osmarender $filename_osm");



	// The SVG starts at 0,0 at the bottom left
	// Get the width and height of the SVG (in SVG units)
	// This may not work if inkscape cannot create some directories - check that /var/www/.gnome2 and /var/www/.inkscape are writable by www-data
	$svg_width  = (float) shell_exec("inkscape -z -W $filename_svg");
	$svg_height = (float) shell_exec("inkscape -z -H $filename_svg");

// 	var_dump($outer_left,$outer_bottom,$outer_right,$outer_top);
// 	var_dump($osmarender_output);



	// Calculate the portion of the SVG to crop out

// var_dump($bbox);
	list($left,$bottom,$right,$top) = explode(',',$bbox);
// 	$width  = $right - $left;
// 	$height = $top - $bottom;
	$outer_width = $outer_right - $outer_left;
	$outer_height = $outer_top - $outer_bottom;
	// inner bbox relative to the outer bbox
	$left   -= $outer_left;
	$right  -= $outer_left;
	$top    -= $outer_bottom;
	$bottom -= $outer_bottom;

	$x_factor = $svg_width  / $outer_width;
	$y_factor = $svg_height / $outer_height;

	// inner bbox relative to the outer bbox, factored
	$left   *= $x_factor;
	$right  *= $x_factor;
	$top    *= $y_factor;
	$bottom *= $y_factor;

// var_dump($left,$bottom,$right,$top);
// var_dump("$outer_left,$outer_bottom,$outer_right,$outer_top");

// var_dump($x_factor,$y_factor);
// var_dump($svg_width,$svg_height);
// var_dump($left,$bottom,$right,$top);



	// Custom background color?
	if (isset($_REQUEST['BGCOLOR']))
		$bgcolor = escapeshellarg($_REQUEST['BGCOLOR']);
	else
		$bgcolor = "#f8f8f8";
// 	$bgcolor = "transparent";

	// Transparency??
	if (isset($_REQUEST['TRANSPARENT']))
		$transparency = " --export-background-opacity=0.0";
	else
		$transparency = " --export-background-opacity=1.0";



	// call inkscape to rasterize the .svg

	exec("inkscape -z -w $width -h $height --export-background=$bgcolor $transparency --export-area=$left:$bottom:$right:$top --export-png=$filename_png $filename_svg ");

// 	echo("inkscape -z -w $width -h $height --export-background=$bgcolor $transparency --export-area=$left:$bottom:$right:$top --export-png=$filename_png $filename_svg ");
// --export-background=$bgcolor 

	fpassthru(fopen($filename_png,'r'));
	
// 	unlink ($filename_osm);
// 	unlink ($filename_svg);
// 	unlink ($filename_png);
	
	
}

}