<?php
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-06 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################
require_once('defines.php');
//header("Content-type: text/xml");

#globals
$suppliedKeyvals = null;
$inDoc=$inRule=$inCondition=$inStyle=$inGpsmapcode=$inGpstype=false;
$curConditions=null;
$curRule=null;
$rules = array();
#end globals

function readStyleRules($rulefile)
{
	global $rules;

	$parser = xml_parser_create();
	xml_set_element_handler($parser,"on_start_element_rules",
				"on_end_element_rules");
	xml_set_character_data_handler($parser,"on_characters_rules");

	$fp = fopen($rulefile,"r");
	while($line=fread($fp,4096))
	{
		if(!xml_parse($parser,$line))
		{
			fclose($fp);
			return false;
		}
	}

	fclose($fp);
	xml_parser_free($parser);
	return $rules; 
}

#NB the PHP expat library reads in all tags as capitals - even if they're
#lower case!!!
function on_start_element_rules($parser,$element,$attrs)
{
	global $inDoc, $inRule, $curConditions, $inCondition, $curRule,
			$inStyle, $inGpsmapcode, $inGpstype;

	if($element=="RULESET")
	{
		$inDoc = true;
	}
	elseif($inDoc)
	{
		if($element=="RULE")
		{
			$inRule=true;
			$curRule = array();
		}
		elseif($element=="CONDITION" && $inRule)
		{
			$inCondition=true;
			if(isset($attrs["K"]) && isset($attrs["V"]))
				$curRule["conditions"][$attrs["K"]]=$attrs["V"];
		}
		elseif($element=="STYLE" && $inRule)
		{
			$inStyle=true;
			foreach($attrs as $name => $value)
				$curRule[strtolower($name)] = $value;
		}
		elseif($element=="GPSMAPCODE" && $inRule)
			$inGpsmapcode = true;
		elseif($element=="GPSTYPE" && $inRule)
			$inGpstype = true;
	}
}

function on_end_element_rules($parser,$element)
{
	global $inDoc, $inRule, $curConditions, $inCondition, $curRule,
			$inStyle, $rules, $inGpsmapcode, $inGpstype;

	if($element=="RULESET")
		$inDoc = false;
	elseif($inDoc && $element=="RULE")
	{
		$inRule = false;
		$rules[] = $curRule;
	}
	elseif($inRule && $element=="CONDITION")
		$inCondition = false;
	elseif($inRule && $element=="STYLE")
		$inStyle = false;
	elseif($element=="GPSMAPCODE" && $inRule)
		$inGpsmapcode = false;
	elseif($element=="GPSTYPE" && $inRule)
		$inGpstype = false;
}

function on_characters_rules($parser, $characters)
{
	global $curRule, $inGpsmapcode, $inGpstype;
	if($inGpsmapcode)
		$curRule["gpsmapcode"] = $characters;
	elseif($inGpstype)
		$curRule["gpstype"] = $characters;
}

function getStyle($rules,$suppliedKeyvals)
{
	$prevHit = 0;
	$style=array();
	// defaults
	$style["colour"] = "220,220,220";
	$style["width"] = 1;

	foreach($rules as $curRule)
	{
		$hit = 0;

		// Go through all supplied Keyvals
		foreach($suppliedKeyvals as $k=>$v)
		{
			// If the current ruleset includes the current key...
			if(isset($curRule["conditions"][$k]))
			{
				// If key and value match, increase no. of hits
				if($curRule["conditions"][$k]==$v)
				{
					$hit++;
				}
				// If values are different, we definitely don't want this rule
				else
				{
					$hit=0;
					break;
				}
			}
		}
		// If more rule "hits" this time, this is seen as greater specificity,
		// so take on this rule
		if($hit > $prevHit)
		{
			$style=array();
			foreach($curRule as $k=>$v)
				if($k!="conditions")
					$style[$k] = $v; 
			$prevHit = $hit;
		}
	}

	/*
	if(!isset($style["dash"]) && !isset($style["casing"]))
		$style["casing"] = "0,0,0";
	*/

	return $style;
}

?>
