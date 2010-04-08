/*
Code to get co-ordinates from map taken from http://maposmatic.org/ and
copyright (c) 2009 Étienne Loks <etienne.loks_AT_peacefrogsDOTnet>
Other code copyright (c) 2009-2010 Russ Phillips <russ AT phillipsuk DOT org>

This file is part of OSM Error.

OSM Error is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

// If download area is larger than this, Download button will be disabled
var MAX_SIZE = 0.25;
// base URL. Includes trailing slash
var BASE_URL = "http://www.mappage.org/error/"

var map;
var update_lock = 0;
var epsg_display_projection = new OpenLayers.Projection('EPSG:4326');
var epsg_projection = new OpenLayers.Projection('EPSG:900913');

function getUpperLeftLat() { return document.getElementById('lat_upper_left'); }
function getUpperLeftLon() { return document.getElementById('lon_upper_left'); }
function getBottomRightLat() { return document.getElementById('lat_bottom_right'); }
function getBottomRightLon() { return document.getElementById('lon_bottom_right'); }

// Return part of query based on whether item is checked or not
function CheckedOrNot (id) {
	if (document.getElementById(id).checked)
		return id + "=1"
	else
		return id + "=0"
}

// Check size of download area
function CheckDownloadArea (topleft, bottomright) {
	fWidth = Math.abs (topleft.lon.toFixed(4) - bottomright.lon.toFixed(4))
	fHeight = Math.abs (topleft.lat.toFixed(4) - bottomright.lat.toFixed(4))

	oSubmit = document.getElementById ('btnSubmit')
	var sURL = BASE_URL

	if (fWidth * fHeight > MAX_SIZE) {
		oSubmit.disabled = true
		oSubmit.value = "Area too large. Zoom in further"
		document.getElementById('spLink').innerHTML = ""
	}
	else {
		oSubmit.disabled = false
		oSubmit.value = "Download"
		sURL += "error.php?"
		sURL += "lon_upper_left=" + topleft.lon.toFixed(4) + "&amp;"
		sURL += "lat_bottom_right=" + bottomright.lat.toFixed(4) + "&amp;"
		sURL += "lon_bottom_right=" + bottomright.lon.toFixed(4) + "&amp;"
		sURL += "lat_upper_left=" + topleft.lat.toFixed(4) + "&amp;"

		sURL += CheckedOrNot ('ref') + "&amp;"
		sURL += CheckedOrNot ('road') + "&amp;"
		sURL += CheckedOrNot ('name') + "&amp;"
		sURL += CheckedOrNot ('hours') + "&amp;"
		sURL += CheckedOrNot ('source') + "&amp;"
		sURL += CheckedOrNot ('fixme') + "&amp;"
		sURL += CheckedOrNot ('naptan') + "&amp;"
		sURL += CheckedOrNot ('pbref') + "&amp;"

		document.getElementById('spLink').innerHTML = "<p><a href = '" + sURL + "'>Download link</a></p>"
	}
}

/* update form fields on zoom action */
function updateForm ()
{
    if (update_lock)
      return;

    var bounds = map.getExtent();

    var topleft = new OpenLayers.LonLat(bounds.left, bounds.top);
    topleft = topleft.transform(epsg_projection, epsg_display_projection);

    var bottomright = new OpenLayers.LonLat(bounds.right, bounds.bottom);
    bottomright = bottomright.transform(epsg_projection, epsg_display_projection);

    getUpperLeftLat().value = topleft.lat.toFixed(4);
    getUpperLeftLon().value = topleft.lon.toFixed(4);
    getBottomRightLat().value = bottomright.lat.toFixed(4);
    getBottomRightLon().value = bottomright.lon.toFixed(4);

	// Check size of download area
	CheckDownloadArea (topleft, bottomright)
}

/* update map on form field modification */
function updateMap ()
{
    var bounds = new OpenLayers.Bounds(getUpperLeftLon().value,
                                       getUpperLeftLat().value,
                                       getBottomRightLon().value,
                                       getBottomRightLat().value);
    bounds.transform(epsg_display_projection, epsg_projection);

    update_lock = 1;
    map.zoomToExtent(bounds);
    update_lock = 0;

    var topleft = new OpenLayers.LonLat(bounds.left, bounds.top);
    topleft = topleft.transform(epsg_projection, epsg_display_projection);

    var bottomright = new OpenLayers.LonLat(bounds.right, bounds.bottom);
    bottomright = bottomright.transform(epsg_projection, epsg_display_projection);

	// Check size of download area
	CheckDownloadArea (topleft, bottomright)
}

/* main initialisation function */
function init()
{
    map = new OpenLayers.Map ('map', {
        controls:[new OpenLayers.Control.Navigation(),
                  new OpenLayers.Control.PanZoomBar(),
                  new OpenLayers.Control.Attribution()],
        maxExtent: new OpenLayers.Bounds(-20037508.34,-20037508.34,20037508.34,20037508.34),
        numZoomLevels: 18,
        maxResolution: 156543.0399,
        projection: epsg_projection,
        displayProjection: epsg_display_projection
    } );

    layerTilesMapnik = new OpenLayers.Layer.OSM.Mapnik("Mapnik");
    map.addLayer(layerTilesMapnik);

    map.events.register('zoomend', map, updateForm);
    map.events.register('moveend', map, updateForm);
    updateMap();
    updateForm();
}
