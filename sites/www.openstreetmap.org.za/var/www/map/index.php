<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <title>OpenStreetMap: Map</title>
        <meta http-equiv="content-type" content="text/html; charset=utf-8" />
        <meta name="Content-Language" content="en" />
        <link rel="stylesheet" type="text/css" href="/css/common.css" />
        <link rel="stylesheet" type="text/css" href="/css/other.css" />
        <link rel="shortcut icon" href="/img/favicon.png" />
        <script src="/js/prototype.js" type="text/javascript"></script>
        <script type="text/javascript" src="/ol/OpenLayers.js"></script>
        <script type="text/javascript" src="/js/OpenStreetMap.js"></script>
        <script type="text/javascript" src="/js/util.js"></script>
        <script type="text/javascript">

var map;
var layer_mapnik;
var layer_tah;

function init() {
//    return;

    OpenLayers.Lang.setCode('en');
    var lon = 24.41;
    var lat = -28.87;
    var zoom = 6;

    var overviewctrl = new OpenLayers.Control.OverviewMap();
    map = new OpenLayers.Map('map', {
        projection: new OpenLayers.Projection("EPSG:900913"),
        displayProjection: new OpenLayers.Projection("EPSG:4326"),
        controls: [
            new OpenLayers.Control.MouseDefaults(),
            new OpenLayers.Control.LayerSwitcher(),
            new OpenLayers.Control.ScaleLine({ div: $('customScaleLine') }),
            new OpenLayers.Control.MousePosition({ div: $('customMousePosition') }),
            new OpenLayers.Control.Permalink('permalink'),
            overviewctrl,
            new OpenLayers.Control.PanZoomBar()],
        maxExtent:
            new OpenLayers.Bounds(-20037508.34,-20037508.34,
                                   20037508.34, 20037508.34),
        numZoomLevels: 18,
        maxResolution: 156543,
        units: 'm'
    });

    layer_mapnik = new OpenLayers.Layer.OSM.Mapnik("Mapnik");
    layer_tah = new OpenLayers.Layer.OSM.Osmarender("Osmarender");
    map.addLayers([layer_mapnik, layer_tah]);

    overviewctrl.layers[0].getURL = function (bounds) {
        var res = this.map.getResolution();
        var x = Math.round((bounds.left - this.maxExtent.left) / (res * this.tileSize.w));
        var y = Math.round((this.maxExtent.top - bounds.top) / (res * this.tileSize.h));
        var z = this.map.getZoom();
        var limit = Math.pow(2, z);

        if (y < 0 || y >= limit)
        {
            return OpenLayers.Util.OSM.MISSING_TILE_URL;
        }
        else
        {
            x = ((x % limit) + limit) % limit;

            var url = this.url;
            var path = z + "/" + x + "/" + y + ".png";

            if (url instanceof Array)
            {
                url = this.selectUrl(path, url);
            }

            return url + path;
        }
    }
    /* jump to default location if none is set */
    if (map.getCenter() == null) {
        jumpTo(lon, lat, zoom)
    }
}

        </script>
        <style type="text/css">

div#map {
    position: absolute;
    top: 0px;
    bottom: 40px;
    left: 0px;
    right: 0px;
    /* for ie */
 	width:expression((document.documentElement.clientWidth ? document.documentElement.clientWidth : document.body.clientWidth)-200);
 	height:expression((document.documentElement.clientHeight ? document.documentElement.clientHeight : document.body.clientHeight)-50);
    padding: 1px;
}

div#content {
    position: absolute;
    padding: 4px;
    height: 31px;
    bottom: 0px;
    left: 0px;
    right: 0px;
    /* for ie */
 	width:expression((document.documentElement.clientWidth ? document.documentElement.clientWidth : document.body.clientWidth)-200);
 	top:expression((document.documentElement.clientHeight ? document.documentElement.clientHeight : document.body.clientHeight)-48);
    background-image: url('/img/marginaliabg.png');
    border-bottom: 1px solid #bfc0d8;
}

div#customScaleLine {
    position: absolute;
    left: 10px;
    font-size: xx-small;
}

div#customMousePosition {
    position: absolute;
    right: 10px;
    padding: 2px;
    font-size: xx-small;
}

div#customPermalink {
    position: absolute;
    right: 10px;
    padding: 16px 2px;
    font-size: xx-small;
}

div#corners {
    position: absolute;
    top: 34px;
    left: -1px;
    right: 1px;
//    width: 100%;
    width:expression((document.documentElement.clientWidth ? document.documentElement.clientWidth : document.body.clientWidth)-458);
}

img#border-bl {
    position: absolute;
    top: -4px;
    left: 0px;
    z-index: 10000;
}

img#border-br {
    position: absolute;
    top: -4px;
    right: -2px;
    z-index: 10000;
}

div#attribution {
    position: absolute;
    left: 160px;
    right: 160px;
    font-size: xx-small;
}

div#attribution img {
    float: left;
    padding-right: 4px;
}

        </style>
    </head>
    <body onload="init()">

<? include($_SERVER['DOCUMENT_ROOT'].'/menu.inc.php'); ?>

        <div class="left">
            <a href="/"><img src="/img/logolinks.png" alt="OpenStreetMap" width="86" height="400"/></a>
        </div>

        <div class="main" id="main" style="bottom: 20px;">
            <img id="border-tl" src="/img/border-tl.gif" alt="" width="10" height="10"/>
            <img id="border-tr" src="/img/border-tr.gif" alt="" width="10" height="10"/>
            <div id="map"></div>
            <div id="content">
                <div id="customScaleLine"></div>
                <div id="attribution">
	                <a href="http://creativecommons.org/licenses/by-sa/2.0/"><img src="/img/cc-by-sa.png" alt="[CC-BY-SA]" width="88" height="31"/></a>
					All maps (data) on these pages are from the OpenStreetMap project and are available under 
	                <a href="http://creativecommons.org/licenses/by-sa/2.0/">Creative Commons Attribution-ShareAlike 2.0 license.</a>.
	                <a href="/faq/#license">More about license...</a>
                </div>
                <div id="customMousePosition"></div>
                <div id="customPermalink"><a href="" id="permalink">Permalink</a></div>
                <div id="corners">
                    <img id="border-bl" src="/img/border-bl.gif" alt="" width="10" height="10"/>
                    <img id="border-br" src="/img/border-br.gif" alt="" width="10" height="10"/>
                </div>
<? include($_SERVER['DOCUMENT_ROOT'].'/footer.inc.php'); ?>