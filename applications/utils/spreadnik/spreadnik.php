#!/usr/bin/php5
<?php

/**

 Spreadnik v0.2

The goal of this script (at least at this version) is to translate a .csv spreadsheet into the mangled XML format that is a mapnik stylesheet.

This will hopefully lead to simplifying the task of customizing mapnik styles: instead of tweaking the stylesheet, tweak the spreadsheet, then run this script, then copy-paste the XML into the stylesheet.


 ----------------------------------------------------------------------------
 "THE BEER-WARE LICENSE":
 <ivan@sanchezortega.es> wrote this file. As long as you retain this notice you
 can do whatever you want with this stuff. If we meet some day, and you think
 this stuff is worth it, you can buy me a beer in return.
 ----------------------------------------------------------------------------

FIXME: the code needs some refactoring. In particular, the n-dimensional array which holds all the casuistic should be replaced by a tree with n depth levels, and intelligently split nodes as needed when adding symbolizer rules.

*/

/// Config: Set this to the directory where the images are stored. If set correctly, this will mean that there is no need to specify the width, height and type of the images for PointSymbolizers.
$symbol_dir = "/home/ivan/mapnik/symbols/";
$symbol_entity = "&symbol_dir;";

// $files = array('highways','stations','symbols','points');
// $files = array('points');
$files = array('boundaries','boundaries-area','roads','labels');

/// TODO: loop through the different CSV files in the directory (when there are more than one) instead of hard-coding the filenames here.



foreach($files as $filename)
{
	// OK, let's parse the CSVs
	// $filename = "highways";
// 	$filename = "stations";
	echo "Processing $filename\n0%...";
	$csvf = fopen("$filename.csv",'r');
	
	
	// $csv_column_names = $raw_csv[0];
	
	$rows = array();
	$filter_values = array();
	$filter_count = 0;
	unset($csv_columns);
	// FIRST PASS: Get the column names from the first row and parse all possible values of the filters.
	while($raw_row = fgetcsv($csvf))
	{
		if (!isset($csv_columns))	// First column
		{
			$csv_columns = array();
			
			// How many columns for filters, rendering rules and zoom factors?
			foreach($raw_row as $index=>$csv_column)
			{
				if ($csv_column == 'z0' ||
				$csv_column == 'z1' ||
				$csv_column == 'z2' ||
				$csv_column == 'z3' ||
				$csv_column == 'z4' ||
				$csv_column == 'z5' ||
				$csv_column == 'z6' ||
				$csv_column == 'z7' ||
				$csv_column == 'z8' ||
				$csv_column == 'z9' ||
				$csv_column == 'z10' ||
				$csv_column == 'z11' ||
				$csv_column == 'z12' ||
				$csv_column == 'z13' ||
				$csv_column == 'z14' ||
				$csv_column == 'z15' ||
				$csv_column == 'z16' ||
				$csv_column == 'z17' ||
				$csv_column == 'z18')
				{
					$csv_columns[$index]['type'] = 'zoom';
					$csv_columns[$index]['name'] = substr($csv_column,1);	// Strip the "z"
				}
				elseif ($csv_column == 'pass')
				{
					$csv_columns[$index]['type'] = 'pass';
					$csv_columns[$index]['name'] = $csv_column;
				}
				elseif ($csv_column == 'symbolizer' )	/// TODO: add textsymbolizer, etc etc etc
				{
					$csv_columns[$index]['type'] = 'symbolizer';
					$csv_columns[$index]['name'] = $csv_column;
				}
				elseif ($csv_column)
				{
					// We'll assume anything else (but an empty column) is a filter.
					$csv_columns[$index]['type'] = 'filter';
					$csv_columns[$index]['name'] = $csv_column;
					$filter_names[$filter_count++] = $csv_column;
					$filter_values[$csv_column]['default'] = true;
				}
			}
	// 		print_r($csv_columns);
		}
		else	// Not first row
		{
			$filters    = array();
			$pass       = null;
			$symbolizer = null;
			$zooms      = array();
			foreach($raw_row as $index=>$cell)
			{
				if ($csv_columns[$index]['type'] == 'filter')
				{
					if (!$cell) $cell = 'all';
					if ($cell == 'no') $cell = '!yes';
					$filters[ $csv_columns[$index]['name'] ] = $cell;
					if ($cell != 'all' && $cell != 'none')
					{
						if (substr($cell,0,1) == '!')
						{
							$cell = substr($cell,1);
						}
						$values = explode(',',$cell);
						foreach($values as $value)
						{
							$filter_values[ $csv_columns[$index]['name'] ][ $value ] = true;
						}
					}
				}
				elseif ($csv_columns[$index]['type'] == 'symbolizer')
				{
					if ($cell)
					$symbolizer = $cell;
				}
				elseif ($csv_columns[$index]['type'] == 'pass')
				{
					if ($cell)
					$pass = $cell;
				}
				elseif ($csv_columns[$index]['type'] == 'zoom')
				{
					if ($cell)
					$zooms[ $csv_columns[$index]['name'] ] = $cell;
				}
			}
			
	// 		echo "$filters, $pass, $symbolizer, $zooms";
			if ($zooms && $filters && $pass && $symbolizer)
			$rows[] = array($filters, $pass, $symbolizer, $zooms);
		}
	}
	
	$filter_values_count = array();
	// Count how many different values are in each filter, for later.
	foreach($filter_values as $filter=>$values)
	{
		foreach($values as $value)
			$filter_values_count[$filter]++;
	}
	
	echo "20%...";
	
// 	print_r($filter_values);
// 	print_r($filter_values_count);
	
	// print_r($filter_values);
	// print_r($rows);
	// die();
	// 
	
	// SECOND PASS: fill the n-dimensional array with the symbolizers and zooms (AKA "unfold the filters")
	$nbox = array();
	
	if (!$define_recursive_fill_nbox)
	{
		function recursive_fill_nbox($filters,$level,&$array,$pass,$symbolizer,$zooms)
		{
			global $filter_count,$filter_names;
			if ($filter_count == $level)
			{
		// 		print_r($zooms);
		// 		foreach($filters[$level] as $option)
		// 		{
					foreach($zooms as $zoom=>$symbol_param)
					{
						$array[$zoom][$pass][$symbolizer] = $symbol_param;
					}
		// 		}
			}
			else
			{
				foreach($filters[$filter_names[$level]] as $option)
				{
					recursive_fill_nbox($filters,$level+1,&$array[$option],$pass,$symbolizer,$zooms);
				}
			}
		}
		$define_recursive_fill_nbox = true;
	}
	// Need to build up alternative filter values array for assigning nodes with 'all'. Basically, swap keys with values.
	$alt_filter_values = array();
	foreach($filter_values as $filter=>$values)
	{
		foreach($values as $value=>$one)
			$alt_filter_values[$filter][] = $value;
	}
	
	// print_r($alt_filter_values);
	
	foreach($rows as $row)
	{
		list($filters, $pass, $symbolizer, $zooms) = $row;
		
		foreach ($filters as $filter=>$values_text)
		{
			$negate_values = false;
			if (substr($values_text,0,1) == '!')
			{
				$negate_values = true;
				$values_text = substr($values_text,1);
			}
			
			if ($values_text == 'all')
				$values = $alt_filter_values[$filter];
	// 			$values = $filter_values[$filter];
			elseif ($values_text == 'none')
				$values = array('');
			else
				$values = explode(',',$values_text);
			
			if ($negate_values)
			{
				$values = array_diff($alt_filter_values[$filter], $values);
	// 			print_r($values);
			}
			
			$choices[$filter] = $values;
		}
	// 	echo "entering fill\n";print_r($choices);
	// 	print_r($zooms);
		
		recursive_fill_nbox($choices,0,&$nbox,$pass,$symbolizer,$zooms);
		
	}
	
	
// 	print_r($nbox);
	// die();
	
	
	// THIRD PASS: refactor the array in order to group the complete symbolizers (pass + symbolizer ruleset + zoom set) shared by several bits of the nbox
	
	echo "40%...";
	$rules = array();
	
	if (!$define_extract_serialized_rules)
	{
		function recursive_extract_serialized_rules($nbox, $level, &$rules, $path)
		{
			global $filter_count;
			if ($level==$filter_count)
			{
				foreach($nbox as $zoom=>$passes)
				{
		// 			echo "$zoom\n";
					foreach($passes as $pass=>$rule)
					{
						$path_copy = $path;
						$item = &$rules[$pass][ serialize($rule) ][$zoom];
						while (!empty($path_copy))
						{
							$item = &$item[ array_shift($path_copy) ];
						}
					}
				}
			}
			else
			{
				foreach($nbox as $path_element=>$child_nbox)
				{
					$newpath = $path;
					array_push($newpath,$path_element);
					recursive_extract_serialized_rules($child_nbox, $level+1, &$rules, $newpath);
		// 			echo ".";
				}
			}
			
		}
		$define_extract_serialized_rules = true;
	}
	
	recursive_extract_serialized_rules($nbox, 0, $rules, array());
	
// 	print_r($rules);
	
	
	// die();
	// 
	
	// FOURTH PASS: merge the filter options sub-arrays into SQL-like filter text snippets. This can be done recursively from the leafs to the root of the rules array (that is, until the zoom depth is reached).
	
	echo "60%...";

	if (!$defined_sql_filter_from_array_filter)
	{
		function sql_filter_from_array_filter($filters,$level)
		{
			global $filter_names,$alt_filter_values,$filter_values_count;
			if (!is_array($filters))
			{
				return '';
			}
			else
			{
				$name_of_current_filter = $filter_names[$level];
				$temp = array();
				foreach($filters as $option=>$subfilters)
				{
		// 			echo "$level $name_of_current_filter $option\n";
					$subfiltertext = sql_filter_from_array_filter($subfilters,$level+1);
					$temp[$subfiltertext][] = $option;
				}
				
		// 		print_r($temp);
				
				$or_pieces = array();
				foreach($temp as $child_sql_chunk=>$level_options)
				{
		// 			echo "" . count($level_options)  . " == " . $filter_values_count[$name_of_current_filter] . "\n" ;
					
					if (count($level_options) == $filter_values_count[$name_of_current_filter])
					{
		// 				echo "$name_of_current_filter yay!!\n";
						return $child_sql_chunk;	// There will be no other child_sql_chunks.
					}
					else
					{
						$or_subpieces = array();
		// 				var_dump($level_options);
						if (array_search('default',$level_options) !== false)
						{
// 							$old_count = count($level_options); $old_options = implode(',',$level_options);
							$negate = 'not ';
							$level_options = array_diff($alt_filter_values[$name_of_current_filter], $level_options);
						}
						else
						{
							$negate = '';
						}
						
		// 				echo $name_of_current_filter."\n"; print_r($alt_filter_values[$name_of_current_filter]); print_r($level_options); echo "--\n";
		
						if (array_search('yes',$level_options) !== false)
						{
							$level_options[] = 'true';
							$level_options[] = '1';
						}
						
						foreach($level_options as $level_option)
						{
// 							if ($level_option == 1) {echo "WTF?!!!\n"; print_r($level_options);}
		
							// Manage options for "lower than" and "greater than". 
							/// TODO: Manage less-or-equal, more-or-equal, and regexps!
							if (substr($level_option,0,1)=='>')
								$or_subpieces[] = "[$name_of_current_filter] > " . (float)substr($level_option,1) ;
							elseif (substr($level_option,0,1)=='<')
								$or_subpieces[] = "[$name_of_current_filter] < " . (float)substr($level_option,1) ;
							else
								$or_subpieces[] = "[$name_of_current_filter] = '$level_option'";
						}
					}
					
// 					if (count($or_subpieces)==0) echo "WTF?! No subpieces! $negate $old_count -> ".count($level_options)." == ".$filter_values_count[$name_of_current_filter]." $name_of_current_filter ($old_options)\n";
// 					if (count($or_subpieces)==1 && !$or_subpieces[0]) echo "WTF?! Subpieces empty!\n";
					
					if ($child_sql_chunk == '')
					{
						$or_pieces[] = "\n" . str_repeat(" ",$level) . "$negate(" .  implode(' or ',$or_subpieces) . ")";
					}
					else
					{
					
						$or_pieces[] = "\n" . str_repeat(" ",$level) . "( $negate(" . implode(' or ',$or_subpieces) . ") and ( $child_sql_chunk )\n" . str_repeat(" ",$level) . ")";
					}
					
				}
				return implode(' or ', $or_pieces);
			}
		}
		$defined_sql_filter_from_array_filter = true;
	}
	
	// Some numbers for the scale factors that mapnik uses in every zoom level.
	$scale = 443744272;	// See "map.getScale()" in any Openlayers with a epsg:900913 map at z0.
	$sqrt2 = sqrt(2);
	for ($i = 0; $i <= 18; $i++)
	{
		$zoom_max_scales[$i] = (int) ($scale * $sqrt2);
		$zoom_min_scales[$i] = (int) ($scale / $sqrt2);
		$zoom_scales[$i]     = $scale;
		$scale /= 2;
	}
	
	
	
	foreach($rules as $pass=>$passrules)
	{
		foreach($passrules as $rule=>$ruleconditions)
		{
			$filterzooms_for_this_rule = array();
			
			foreach($ruleconditions as $zoom=>&$filters)
			{
				$filter_text = sql_filter_from_array_filter($filters,0);
				$filterzooms_for_this_rule[$filter_text][] = $zoom;
			}
			unset($ruleconditions);
			
			// Group adjacent zoom levels into zoom ranges.
			foreach($filterzooms_for_this_rule as $filter_text=>$zooms)
			{
				sort($zooms);	// Just in case.
				$last_zoom = null;
				foreach ($zooms as $zoom)
				{
					if ($last_zoom === null)
					{
						$last_zoom = $zoom;
						$first_zoom = $zoom;
					}
					else
					{
						if ($last_zoom != $zoom-1)
						{
							$min = $zoom_min_scales[$zoom];
							$ruleconditions[$filter_text][] = "$first_zoom-$zoom";
							$max = $zoom_max_scales[$zoom];
						}
						else
						{
							$last_zoom = $zoom;
						}
					}
				}
				$min = $zoom_min_scales[$zoom];
				$ruleconditions[$filter_text][] = "$first_zoom-$zoom";
			}
			$rules[$pass][$rule] = $ruleconditions;
			
		}
	}
	
	
	echo "80%...";
// 	print_r($rules);
	
	
	
	// SIXTH PASS: The rules array has the right structure now, so let's transverse it and spit the XML out.
	
	/// HACK: ensure that the casing is always executed prior to the fill
	ksort($rules);
	
	// Init XML writer stuff
	$xml = new xmlwriter();
	$xml->openMemory();
	$xml->setIndent(true);
	
	
	// Now, just transverse the array and write the stuff inside XML...
	foreach($rules as $pass=>$passrules)
	{
	
		$xml->startElement('Style');
		$xml->writeAttribute('name',"$filename-$pass");
		
		foreach($passrules as $serial_symbolizers=>$filters)
		{
			$symbolizer_parameter_set = unserialize($serial_symbolizers);
			
			// Symbolizer parameter sets need to get grouped into individual symbolizers: PointSymbolizer, LinePatternSymbolizer, PolygonPatternSymbolizer, TextSymbolizer, ShieldSymbolizer, LineSymbolizer, PolygonSymbolizer, BuildingSymbolizer, RasterSymbolizer, and MarkersSymbolizer 
			
			$symbolizers = array();
			$valid_symbolizer = false;
			foreach($symbolizer_parameter_set as $symbolizer_parameter=>$value)
			{
				$pieces = explode('.',$symbolizer_parameter);
				list($symbolizer_type, $symbolizer_param) = $pieces;
	// 			sscanf($symbolizer_type,"line%d",$symbolizer_type_count);
				preg_match('/[0-9]$/',$symbolizer_type,$temp);
				if ($temp[0])
				{
					$symbolizer_count = $temp[0];
					$symbolizer_type = str_replace($symbolizer_count,'',$symbolizer_type);
				}
				else
				{
					$symbolizer_count = 1;
				}
				
				// Fix up commas and dots for known problematic symbolizer parameter, and abbreviated parameters.
				if ($symbolizer_param == 'stroke-width' || $symbolizer_param == 'stroke-opacity'|| $symbolizer_param == 'size')
					$value = str_replace(',','.',$value);
				elseif ($symbolizer_param == 'stroke-dasharray')
					$value = str_replace('.',',',$value);
// 				elseif ($symbolizer_type == 'point' && $symbolizer_param == 'file')
// 					$value = "$symbol_dir$value";
				elseif ($symbolizer_type == 'text' && $symbolizer_param == 'face')
					$symbolizer_param = "face_name";
				
				if ($symbolizer_type == 'line') $symbolizer_type = "LineSymbolizer";
				if ($symbolizer_type == 'poly') $symbolizer_type = "PolygonSymbolizer";
				if ($symbolizer_type == 'text') $symbolizer_type = "TextSymbolizer";
				if ($symbolizer_type == 'point') $symbolizer_type = "PointSymbolizer";
				
				if (($symbolizer_type=='LineSymbolizer'  && $symbolizer_param=='stroke') ||
				($symbolizer_type=='PolygonSymbolizer'  && $symbolizer_param=='fill')   ||
				($symbolizer_type=='TextSymbolizer'  && $symbolizer_param=='face_name'))
					$valid_symbolizer = true;
				if ($symbolizer_type=='PointSymbolizer' && $symbolizer_param=='file')
				{
					$info = getimagesize( "$symbol_dir$value" );
					$value = "$symbol_entity$value";
					$symbolizers[$symbolizer_type][$symbolizer_count]['width']  = $info[0];
					$symbolizers[$symbolizer_type][$symbolizer_count]['height'] = $info[1];
					$mimetype = $info['mime']; $mimetype = explode('/',$mimetype);
					$symbolizers[$symbolizer_type][$symbolizer_count]['type'] = $mimetype[1];
	// 				print_r($info);
					$valid_symbolizer = true;
				}
				
				
				$symbolizers[$symbolizer_type][$symbolizer_count][$symbolizer_param] = $value;
				
				/// TODO: for a PointSymbolizer, get the image info (width/height/format) so these parameters are not needed
// 				echo "$symbolizer_type, $symbolizer_count, $symbolizer_param, $value\n";
				
	// 			$xml->writeComment("$symbolizer_type, $symbolizer_type_count, $symbolizer_param, $value");
			}
			
// 			if (!$valid_symbolizer) echo "No valid symbolizers, skipping rule @ pass $pass\n"; // print_r($filters); print_r($symbolizer_parameter_set);
			
			if ($valid_symbolizer)
			{
				foreach($filters as $filter_text=>$zooms)
				{
					foreach($zooms as $zoomrange)
					{
						
						$twozooms = explode("-",$zoomrange);
						list($maxzoom,$minzoom) = $twozooms;
						$zoom_comment = $maxzoom - $minzoom ? "z$maxzoom - z$minzoom" : "z$maxzoom";
		
						$xml->startElement('Rule');
						
						if ($maxzoom != 0)
						{
							$xml->startElement('MaxScaleDenominator');
							$xml->text($zoom_max_scales[$maxzoom]);
							$xml->endElement();
						}
						if ($minzoom != 18)
						{
							$xml->startElement('MinScaleDenominator');
							$xml->text($zoom_min_scales[$minzoom]);
							$xml->endElement();
						}
						
						$xml->writeComment(" $zoom_comment ");
						$xml->startElement('Filter');
						$xml->text($filter_text);
						$xml->endElement();
						
		// 				$xml->writeComment(var_export($symbolizers,1));
						
						foreach($symbolizers as $symbolizer_type=>$type_symbolizers)
						{
							foreach($type_symbolizers as $symbolizer)	// Skip the symbolizer count. i.e. line2, line3.
							{
								if ($symbolizer_type == 'LineSymbolizer' || $symbolizer_type == 'PolygonSymbolizer')
								{
									$xml->startElement($symbolizer_type);
									
									foreach($symbolizer as $symbolizer_param=>$value)
									{
										$xml->startElement('CssParameter');
										$xml->writeAttribute('name',$symbolizer_param);
										$xml->text($value);
										$xml->endElement();
									}
									$xml->endElement();
								}
								elseif ($symbolizer_type == 'TextSymbolizer' || $symbolizer_type == 'PointSymbolizer')
								{
									$xml->startElement($symbolizer_type);
									
									foreach($symbolizer as $symbolizer_param=>$value)
									{
										$xml->writeAttribute($symbolizer_param,$value);
									}
									$xml->endElement();
								}
								else
									echo "No known symbolizer '$symbolizer_type'\n";
								/// TODO: Add all other symbolizers
							}
						}
						
						
						$xml->endElement();
						
						
					}
				}
			}
		}
		$xml->endElement();
		file_put_contents("$filename-$pass.sty",$xml->outputMemory());

	}
	
	echo "100%\n";
	
}


