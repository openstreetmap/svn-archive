<?php
    
function getLayers()
{
  $Layers = Array(
    "mapnik"=>Array(
      "tiles"=>"http://tile.openstreetmap.org/",
      "description"=>"Roadmap rendering of OSM",
      "update"=>"weekly, on wednesdays",
      "license"=>"CC-BY-SA 2.0",
      "source"=>"OpenStreetMap",
      "hosted"=>"OpenStreetMap foundation",
      "format"=>"png",
      "slippy"=>"http://openstreetmap.org/"
      ),
    "osmarender" => Array(
      "tiles" =>"http://tah.openstreetmap.org/Tiles/tile",
      "description"=>"Distributed rendering project for OSM",
      "update"=>"within a few hours of map data changing",
      "license"=>"CC-BY-SA 2.0",
      "source"=>"OpenStreetMap",
      "hosted"=>"OpenStreetMap foundation dev server",
      "format"=>"png",
      "slippy"=>"http://openstreetmap.org/"
      ),
    "cycle" => Array(
      "tiles" => "http://a.andy.sandbox.cloudmade.com/tiles/cycle",
      # was "http://www.thunderflames.org/tiles/cycle/",
      "description"=>"Cycle routes and the national cycling network (NCN)",
      "update"=>"",
      "license"=>"CC-BY-SA 2.0",
      "source"=>"OpenStreetMap",
      "hosted"=>"thunderflames.org",
      "format"=>"png",
      "slippy"=>"http://www.gravitystorm.co.uk/osm/"
      ),
    "skiing" => Array(
      "tiles" =>"http://openpistemap.org/tiles/contours/",
      "description"=>"Skiing maps of the Alps",
      "update"=>"",
      "license"=>"CC-BY-SA 2.0",
      "source"=>"OpenStreetMap",
      "hosted"=>"OpenPisteMap",
      "format"=>"png",
      "slippy"=>"http://openpistemap.org/"
      ),
    "maplint" => Array(
      "tiles" =>"http://tah.openstreetmap.org/Tiles/maplint/",
      "description"=>"Debug information for OSM",
      "update"=>"within a few hours of map data changing",
      "license"=>"CC-BY-SA 2.0",
      "source"=>"OpenStreetMap",
      "hosted"=>"OpenStreetMap foundation dev server",
      "format"=>"png",
      "slippy"=>"http://informationfreeway.org/"
      )
    );

  # optionally include cloudmade tiles
  # requires api key and probably some sort of usage agreement
  if(1) 
    {
    foreach(array(1,2,999,5, 998) as $CloudmadeStyleNum)
      {
      $Layers[sprintf("cloudmade_%d", $CloudmadeStyleNum)] = Array(
	"tiles" => sprintf("http://tile.cloudmade.com/8460df1eb000505e92f1e907e4b426e6/%d/256/", $CloudmadeStyleNum),
	"description"=>sprintf("Style %d on Cloudmade's style-editor", $CloudmadeStyleNum),
	"update"=>"As new data becomes available",
	"license"=>"CC-BY-SA 2.0",
	"source"=>"CloudMade",
	"format"=>"png",
	"hosted" =>"cloudmade",
	"slippy"=>"http://maps.cloudmade.com/"
	);
      }
    }

  # OpenAerialMap - project no longer running, waiting for replacement source
  # of freely-licensed satellite/aerial images
  if(0)
    $Layers["aerial photos"] = Array(
      "tiles" => "http://oam1.hypercube.telascience.org/tiles/1.0.0/openaerialmap-900913/",
      "description"=>"Aerial photographs",
      "update"=>"As new data becomes available",
      "license"=>"PD",
      "source"=>"OpenAerialMap",
      "format"=>"jpeg",
      "hosted" =>"telascience",
      "slippy"=>"http://openaerialmap.org/"
      );
  
  # TODO: detect if lat/lon is in slovakia before offering this one?
  if(0)
    $Layers["slovakia"] = Array(
      "tiles" =>"http://www.freemap.sk/layers/allinone/?/",
      "description"=>"Slovakian localisation of OSM",
      "update"=>"",
      "license"=>"CC-BY-SA 2.0",
      "source"=>"OpenStreetMap",
      "hosted"=>"OpenStreetMap Slovakia",
      "format"=>"png",
      "slippy"=>"http://www.freemap.sk/"
      );

  return($Layers);
}
