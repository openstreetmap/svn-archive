<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <title>OpenStreetMap: The Free Wiki World Map</title>
        <meta http-equiv="content-type" content="text/html; charset=utf-8" />
        <meta name="Content-Language" content="en-za" />
        <link rel="stylesheet" type="text/css" href="/css/common.css" />
        <link rel="stylesheet" type="text/css" href="/css/main.css" />
        <link rel="shortcut icon" href="/img/favicon.png" />
        <script src="/js/prototype.js" type="text/javascript"></script>
        <script type="text/javascript" src="/ol/OpenLayers.js"></script>
        <script type="text/javascript" src="/js/OpenStreetMap.js"></script>
        <script type="text/javascript" src="/js/util.js"></script>
        <!-- mark <script type="text/javascript" src="/js/markers.js"></script> -->
        <script type="text/javascript">

var map;
var layer_mapnik;
var layer_tah;
// mark var layer_news;
// mark var layer_events;
// mark var layer_local;

function init() {
//    return;

    OpenLayers.Lang.setCode('en');
    var lon = 24.41;
    var lat = -28.87;
    var zoom = 5;

    map = new OpenLayers.Map('map', {
        projection: new OpenLayers.Projection("EPSG:900913"),
        displayProjection: new OpenLayers.Projection("EPSG:4326"),
        controls: [
            new OpenLayers.Control.MouseDefaults(),
            new OpenLayers.Control.LayerSwitcher(),
            new OpenLayers.Control.PanZoomBar()],
        maxExtent:
            new OpenLayers.Bounds(-20037508.34,-20037508.34,
                                    20037508.34, 20037508.34),
        numZoomLevels: 18,
        maxResolution: 156543,
        units: 'meters'
    });

    layer_mapnik = new OpenLayers.Layer.OSM.Mapnik("Mapnik");
    layer_tah = new OpenLayers.Layer.OSM.Osmarender("Osmarender");
    // mark layer_news = new OpenLayers.Layer.Markers("News", { projection: new OpenLayers.Projection("EPSG:4326"), visibility: false });
    // mark layer_events = new OpenLayers.Layer.Markers("Events", { projection: new OpenLayers.Projection("EPSG:4326"), visibility: false });
    // mark layer_local = new OpenLayers.Layer.Markers("Local Groups", { projection: new OpenLayers.Projection("EPSG:4326"), visibility: false });
    // mark map.addLayers([layer_mapnik, layer_tah, layer_news, layer_events, layer_local]);
	map.addLayers([layer_mapnik, layer_tah]);
	
	// mark createMarkers();

    jumpTo(lon, lat, zoom)
}

        </script>
    </head>
    <body onload="init()">

<? include($_SERVER['DOCUMENT_ROOT'].'/menu.inc.php'); ?>

        <div class="left">
            <div class="box" id="title">
                <a href="/"><img src="/img/logo.gif" alt="OpenStreetMap"/></a>
                <p><b>The Free Wiki World Map</b></p>
            </div>
            <div class="box" id="faq">
                <h2><a href="/faq/">Questions and answers</a></h2>
                <ul>
                    <li><a href="/faq/#what_is_osm">What is OpenStreetMap?</a></li>
                    <li><a href="/faq/#can_i_join">How can I join?</a></li>
                    <li><a href="/faq/#use_of_data">How can I use the data?</a></li>
                    <li><a href="/faq/#license">What is with the license?</a></li>
                    <li><a href="/faq/#how_complete">How complete is the data?</a></li>
					
                </ul>
                <p><a href="/faq/">More...</a></p>
            </div>
            <div class="box" id="community">
                <h2><a href="/community/">Community</a></h2>
                <a href="/community/#wiki">Wiki</a> <img src="/img/bullet.gif" width="7" height="9" alt="|"/>
                <a href="/community/#ml">Mailing lists</a> <img src="/img/bullet.gif" width="7" height="9" alt="|"/>
                <a href="/community/#forum">Forums</a> <img src="/img/bullet.gif" width="7" height="9" alt="|"/>
                <a href="/community/#irc">IRC</a> <img src="/img/bullet.gif" width="7" height="9" alt="|"/>
                <a href="/community/#party">Mapping&nbsp;Parties</a><br/>
<!-- South Africa Does not have local groups yet
                <div class="localgroup">
                    <img class="tomap" onclick="showLayer(layer_local);" src="/img/tomapl.png" alt="Auf die Karte" title="Auf die Karte" width="20" height="20"/>
                    <a href="/community/#local">Local groups:</a><br/>
                        <a href="http://wiki.openstreetmap.org/index.php/Braunschweig">BS</a> <img src="/img/bullet.gif" width="7" height="9" alt="|"/>
                        <a href="http://wiki.openstreetmap.org/index.php/DresdnerOSMStammtisch">DD</a> <img src="/img/bullet.gif" width="7" height="9" alt="|"/>
                        <a href="http://wiki.openstreetmap.org/index.php/Hamburger_Mappertreffen">HH</a> <img src="/img/bullet.gif" width="7" height="9" alt="|"/>
                        <a href="http://wiki.openstreetmap.org/index.php/Karlsruhe">KA</a> <img src="/img/bullet.gif" width="7" height="9" alt="|"/>
                        <a href="http://wiki.openstreetmap.org/index.php/München">M</a> <img src="/img/bullet.gif" width="7" height="9" alt="|"/>
                        <a href="http://wiki.openstreetmap.org/index.php/NFE-Treffen">N/FÜ/ER</a> <img src="/img/bullet.gif" width="7" height="9" alt="|"/>

                        <a href="http://wiki.openstreetmap.org/index.php/Murrhardt">WN</a> <img src="/img/bullet.gif" width="7" height="9" alt="|"/>
                        <a href="http://wiki.openstreetmap.org/index.php/Würzburg">WÜ</a>
                </div>
 -->
            </div>
            <div class="box" id="aktionen">
                <h2><a href="/advocacy/">Advocacy</a></h2>
                <p>
                    <a href="/advocacy/#flyer"><img style="padding: 2px 3px 2px 0px;" src="/img/flyer-titel.png" alt=""/></a>
                    OSM promotion leaflets. <a href="/advocacy/#flyer">More...</a>
                </p>
            </div>
        </div>

        <div class="right">
            <div class="box" id="international">
                <h2><a href="http://www.openstreetmap.org/">OSM International</a></h2>
                <a href="http://www.openstreetmap.ca/"><img src="/img/flags/ca.png" alt="Canada" width="24" height="13"/></a>
                <a href="http://www.openstreetmap.ch/"><img src="/img/flags/ch.png" alt="Switzerland" width="24" height="13"/></a>
                <a href="http://www.openstreetmap.de/"><img src="/img/flags/de.png" alt="Germany" width="24" height="13"/></a>
                <a href="http://www.openstreetmap.es/"><img src="/img/flags/es.png" alt="Spain" width="24" height="13"/></a><br/>
                <a href="http://www.openstreetmap.fr/"><img src="/img/flags/fr.png" alt="France" width="24" height="13"/></a>
                <a href="http://www.openstreetmap.jp/"><img src="/img/flags/jp.png" alt="Japan" width="24" height="13"/></a>
                <a href="http://www.openstreetmap.nl/"><img src="/img/flags/nl.png" alt="Netherlands" width="24" height="13"/></a>
                <a href="http://www.openstreetmap.se/"><img src="/img/flags/se.png" alt="Sweden" width="24" height="13"/></a>
				<a href="http://www.openstreetmap.org.za/"><img src="/img/flags/za.png" alt="South Africa" width="24" height="13"/></a>
            </div>
            <div class="box" id="book">
                <h2>OpenStreetMap Book (German)</h2>
                <a href="http://www.openstreetmap.info/"><img src="/img/openstreetmap-buch-cover.png" alt="OpenStreetMap: Die freie Weltkarte nutzen und mitgestalten"/></a>
                <a href="http://www.openstreetmap.info/">www.openstreetmap.info</a>
            </div>
            <div class="box" id="sotm">
                <a href="http://www.stateofthemap.org/"><img src="/img/sotm.png" alt="State of the Map"/></a><br/>
                12 - 13 July 2008<br/>
                Limerick, Ireland
            </div>
            <div class="box" id="showcase">
                <h2><a href="/showcase/">Showcase</a></h2>
                <a href="/showcase/"><img src="/img/showcase/schaufenster-overview.png" alt="" width="180" height="53"/></a><br/>
				A glance into the OpenStreetMap world
            </div>
            <div class="box" id="intro123" style="text-align: center;">
                <a href="/123/"><img src="/img/123.png" alt="OSM step by step" width="180" height="75"/></a>
            </div>
        </div>

        <div class="main">
            <div id="mapborder">
                <div id="map">
                    <img id="border-tl" src="/img/border-tl.gif" alt="" width="10" height="10"/>
                    <img id="border-tr" src="/img/border-tr.gif" alt="" width="10" height="10"/>
                </div>
            </div>
            <div id="marginalia">
                <div id="germany">
                    <a href="#" onclick="return jumpTo(24.41, -28.87, 5)"><img src="/img/southafrica.gif" alt="Zoom into South Africa" width="36" height="40"/></a>
                </div>
                <div id="fullscreen">
                    <a href="/map/"><img src="/img/view-fullscreen.gif" alt="[Larger Map]" width="42" height="40"/></a>
                </div>
                <div id="destinations">
                    <img src="/img/arrow.gif" alt=" " width="7" height="9"/><a href="#" onclick="return jumpTo(18.47, -33.929, 12)">Cape Town</a>
                    <img src="/img/arrow.gif" alt=" " width="7" height="9"/><a href="#" onclick="return jumpTo(27.994, -26.127, 11)">Johannesburg</a>
                    <img src="/img/arrow.gif" alt=" " width="7" height="9"/><a href="#" onclick="return jumpTo(28.223, -25.76, 12)">Pretoria</a> 
                    <img src="/img/arrow.gif" alt=" " width="7" height="9"/><a href="#" onclick="return jumpTo(25.588, -33.96, 12)">Port Elizabeth</a>
                </div>
                <div id="search">
<!--                    <form action="/geocoder/search" method="POST" onsubmit="doSearch()">
                        <input id="query" name="query" type="text" size="30" maxlength="80"/>
                        <input type="submit" value="Suchen"/>
                    </form>-->
                </div>
                <div id="corners">
                    <img id="border-bl" src="/img/border-bl.gif" alt="" width="10" height="10"/>
                    <img id="border-br" src="/img/border-br.gif" alt="" width="10" height="10"/>
                </div>
            </div>
            <table id="content">
                <tr>
                    <td class="tdcontent" id="news">
                        <h2><a href="/news/">News</a> <img style="position: absolute; margin-left: 10px; margin-top: 3px;" onclick="showLayer(layer_news);" src="/img/tomap.png" alt="Show on map" title="Show on map" width="20" height="20"/></h2>
                        <p><b>OpenStreetMap.org.za Relaunch</b><br/>
                        <span style="font-size: small">28. April 2008</span></p>
                        <p>Launch of the new Openstreetmap.org.uk website.</p>
                    </td>
                    <td class="tdcontent" id="events">
                        <h2><a href="/events/">Events</a> <img style="position: absolute; margin-left: 10px; margin-top: 3px;" onclick="showLayer(layer_events);" src="/img/tomap.png" alt="Show on map" title="Show on map" width="20" height="20"/></h2>
                        <p>Forthcoming events ...</p>
                        <table class="event">
                            <tr>
                                <td>12 - 13 July</td>
                                <td>Limerick, Ireland</td>
                                <td><a href="http://www.stateofthemap.org/">State of the Map 2008</a></td>
                            </tr>
                            <tr>
                                <td>29 Sept - 3 Oct</td>
                                <td>Cape Town, South Africa</td>
                                <td><a href="http://conference.osgeo.org/index.php/foss4g/2008">FOSS4G 2008</a></td>
                            </tr>
                        </table>
                        <p>
                            <a href="/events/">Calendar</a><!-- <img src="/img/bullet.gif" width="7" height="9" alt="|"/>
                            <a href="#">Kalender im iCal-Format</a>-->
                        </p>
                    </td>
                </tr>
            </table>
            <div id="attribution">
                <a href="http://creativecommons.org/licenses/by-sa/2.0/"><img src="/img/cc-by-sa.png" alt="[CC-BY-SA]" width="88" height="31"/></a>
				All maps (data) on these pages are from the OpenStreetMap project and are available under 
                <a href="http://creativecommons.org/licenses/by-sa/2.0/">Creative Commons Attribution-ShareAlike 2.0 license.</a>.
                <a href="/faq/#license">More about license...</a>
                || <a href="/impressum/">Impressum</a>
            </div>
        </div>
<script src="http://www.google-analytics.com/urchin.js" type="text/javascript">
</script>
<script type="text/javascript">
_uacct = "UA-288087-5";
urchinTracker();
</script>
    </body>
</html>
