<?php
    
function getLayers()
{
  return(Array(
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
    "aerial photos" => Array(
      "tiles" => "http://oam1.hypercube.telascience.org/tiles/1.0.0/openaerialmap-900913/",
      "description"=>"Aerial photographs",
      "update"=>"As new data becomes available",
      "license"=>"PD",
      "source"=>"OpenAerialMap",
      "format"=>"jpeg",
      "hosted" =>"telascience",
      "slippy"=>"http://openaerialmap.org/"
      ),
    "captionless" => Array(
      "tiles" =>"http://tah.openstreetmap.org/Tiles/captionless/",
      "description"=>"osmarender, without the text",
      "update"=>"within a few hours of map data changing",
      "license"=>"CC-BY-SA 2.0",
      "source"=>"OpenStreetMap",
      "hosted"=>"OpenStreetMap foundation dev server",
      "format"=>"png",
      "slippy"=>"http://informationfreeway.org/"
      ),
    "slovakia" => Array(
      "tiles" =>"http://www.freemap.sk/layers/allinone/?/",
      "description"=>"Slovakian localisation of OSM",
      "update"=>"",
      "license"=>"CC-BY-SA 2.0",
      "source"=>"OpenStreetMap",
      "hosted"=>"OpenStreetMap Slovakia",
      "format"=>"png",
      "slippy"=>"http://www.freemap.sk/"
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
      "tiles" =>"http://tah.openstreetmap.org/Tiles/maplint.php/",
      "description"=>"Debug information for OSM",
      "update"=>"within a few hours of map data changing",
      "license"=>"CC-BY-SA 2.0",
      "source"=>"OpenStreetMap",
      "hosted"=>"OpenStreetMap foundation dev server",
      "format"=>"png",
      "slippy"=>"http://informationfreeway.org/"
      )
    ));
}
