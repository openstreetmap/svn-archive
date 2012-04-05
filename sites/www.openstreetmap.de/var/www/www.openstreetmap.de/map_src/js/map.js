/*
 *  OpenStreetMap.de - Webseite
 *	Copyright (C) Pascal Neis, 2011
 *
 *	This program is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU AFFERO General Public License as published by
 *	the Free Software Foundation; either version 3 of the License, or
 *	any later version.
 *
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU Affero General Public License
 *	along with this program; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *	or see http://www.gnu.org/licenses/agpl.txt.
 */
 
/**
 * Title: map.js
 * Description: init()-funktion to create a map div
 *
 * @author Pascal Neis, pascal@neis-one.org 
 * @version 0.1 2011-10-29
 */

//======================
// OBJECTS
var map;
var markersLayer;
OpenLayers.ImgPath = "./map_src/themes/dark/";
OpenLayers.Lang.setCode('de');

//======================
// FUNCTIONS
/*
 * init()-Function to create a map
 */
function init(){
    map = new OpenLayers.Map("map", {
			controls: [
				new OpenLayers.Control.Navigation(),
			// new OpenLayers.Control.MouseDefaults(),
            // new OpenLayers.Control.MousePosition(),
            // new OpenLayers.Control.ScaleLine()
            	new OpenLayers.Control.Permalink(),
            	new OpenLayers.Control.Attribution()
            ],
			maxExtent:new OpenLayers.Bounds(-20037508.34,-20037508.34, 20037508.34, 20037508.34),
			numZoomLevels: 18,
            maxResolution: 156543,
            units: 'm',
            projection: projmerc,
            displayProjection: proj4326
    } );
    
    //Add LayerSwitcher
    var layerswitcher = new OpenLayers.Control.LayerSwitcher({roundedCornerColor: "#575757"});

	map.addControl(layerswitcher);
	layerswitcher.maximizeControl();
		
	//Add Panzoombar
	var panZoomBar = new OpenLayers.Control.PanZoom({id:'panzoombar',displayClass:'olControlPanZoomBar'})
	map.addControl(panZoomBar);
	document.getElementById('panzoombar').style.left="20px";
	document.getElementById('panzoombar').style.top="20px";

	//Add Layers
    map.addLayers([
       new OpenLayers.Layer.XYZ("OSM deutscher Stil", [
               "http://a.tile.openstreetmap.de/tiles/osmde/${z}/${x}/${y}.png",
               "http://b.tile.openstreetmap.de/tiles/osmde/${z}/${x}/${y}.png",
               "http://c.tile.openstreetmap.de/tiles/osmde/${z}/${x}/${y}.png"],{attribution: '<a href="./germanstyle.html">About style</a>'}),
        new OpenLayers.Layer.OSM.CycleMap("Radfahrkarte (CycleMap)", {attribution:"", keyname: 'cycle'}),
        new OpenLayers.Layer.OSM("&Ouml;PNV-Karte", 
			"http://tile.memomaps.de/tilegen/${z}/${x}/${y}.png", 
			{numZoomLevels: 19,displayInLayerSwitcher:true,buffer:0,attribution:"", keyname: 'oepnvde'}),
        new OpenLayers.Layer.OSM.Mapnik("OSM Standard (Mapnik)", {attribution:"", keyname: 'mapnik'}),
    ]);
    
    //Add a marker layer
    markersLayer = new OpenLayers.Layer.Vector(
              "Markers",{displayInLayerSwitcher:false,
                  styleMap: new OpenLayers.StyleMap({
                      externalGraphic: '${icon}',
                      backgroundXOffset: -10,
                      backgroundYOffset: -25,
                      graphicXOffset: -10,
                      graphicYOffset: -25,
                      graphicZIndex: 10,
                      backgroundGraphicZIndex: 11,
                      pointRadius: 10,
                      graphicWidth: 21,
                      graphicHeight: 25
                  }),
                  rendererOptions: {zIndexing: true}
              }
          );
    map.addLayer(markersLayer);

	//Set Center of the Map
	if (!map.getCenter()){
	    map.setCenter(new OpenLayers.LonLat(10.3,51.3).transform(proj4326,projmerc), 6);
	}
         
    //Add Local Community Overlay
    createMarkers(map);
       
    //Register Events for MapEdit and ReportBug Links
    map.events.register('move', null, mapMoved);
    mapMoved();
    map.events.register('zoomend', null, function() { updateMapKey(false); } );
    
   	//To fix language issues
    //document.getElementsByClassName('baseLbl')[0].innerHTML = "Grundkarte";
    //document.getElementsByClassName('dataLbl')[0].innerHTML = "&Uuml;berlagerungen";
    $('.baseLbl').html('Grundkarten');
    $('.dataLbl').html('&Uuml;berlagerungen');
}