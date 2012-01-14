/*
 *  OpenStreetMap.de - Webseite
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
 * Title: utils.js
 * Description: Some utility functions
 *
 * @version 0.1 2011-10-29
 */
 
	var projmerc = new OpenLayers.Projection("EPSG:900913");
	var proj4326 = new OpenLayers.Projection("EPSG:4326");
	
	function parseParams(handler) {
	    var perma = location.search.substr(1);
	    if (perma != '') {
	        paras = perma.split('&');
	        for (var i = 0; i < paras.length; i++) {
	            var p = paras[i].split('=');
	            handler(p[0], p[1]);
	        }
	    }
	}
	
	function jumpTo(lon, lat, zoom) {
	    var lonlat = new OpenLayers.LonLat(lon, lat);
	    lonlat.transform(proj4326, projmerc);
	    map.setCenter(lonlat, zoom);
	    return false;
	}
	
	function makeIcon(url, x, y) {
	    var size = new OpenLayers.Size(x, y);
	    var offset = new OpenLayers.Pixel(-(size.w/2), -(size.h/2));
	    return new OpenLayers.Icon(url, size, offset);
	}
	
	var icon = makeIcon('img/localgroup.png', 16, 16);
	
	function addMarker(layer, lon, lat, popupContentHTML) {
	    var lonlat = new OpenLayers.LonLat(lon, lat);
	    lonlat.transform(proj4326, projmerc);
	    var feature = new OpenLayers.Feature(layer, lonlat); 
	    feature.closeBox = true;
	    feature.popupClass = OpenLayers.Class(OpenLayers.Popup.FramedCloud, {minSize: new OpenLayers.Size(300, 180) } );
	    feature.data.popupContentHTML = popupContentHTML;
	    feature.data.overflow = "hidden";
	            
	    var marker = new OpenLayers.Marker(lonlat, icon.clone());
	    marker.feature = feature;
	
	    var markerClick = function(evt) {
	        if (this.popup == null) {
	            this.popup = this.createPopup(this.closeBox);
	            map.addPopup(this.popup);
	            this.popup.show();
	        } else {
	            this.popup.toggle();
	        }
	        OpenLayers.Event.stop(evt);
	    };
	    marker.events.register("mousedown", feature, markerClick);
	
	    layer.addMarker(marker);
	}
	
	function setMarker() {
	    if (marker != null) {
	        layer_marker.removeMarker(marker);
	        marker.destroy();
	    }
	    var size = new OpenLayers.Size(21, 25);
	    var offset = new OpenLayers.Pixel(-size.w/2-1, -size.h-1);
	    var icon = new OpenLayers.Icon('/img/marker.png', size, offset);
	    var lonlat = new OpenLayers.LonLat(mlon, mlat);
	    marker = new OpenLayers.Marker(lonlat.transform(proj4326, projmerc), icon);
	    layer_marker.addMarker(marker);
	}
	
	function updateMapKey(force) {
		var html = document.getElementById('information').innerHTML;
		if (force || html.startsWith("Legende")) {
			var layer = map.baseLayer.keyname;
			var zoom = map.getZoom();
			var html = 'Legende:<br><div id="mapkey_area"><iframe src="http://www.openstreetmap.org/key?layer=' + layer + '&zoom=' + zoom + '"/></div>';
			document.getElementById('information').innerHTML = html;
			openSlide('slider');
		}
	}

	function mapMoved() {
	    var lonlat = map.getCenter().clone();
	    lonlat.transform(projmerc, proj4326);
	    var pos = '?lon=' + lonlat.lon.toFixed(5) + '&lat=' + lonlat.lat.toFixed(5) + '&zoom=' + map.getZoom();
    	
	    document.getElementById('editMap').innerHTML = '<a class="btn success" href="http://www.openstreetmap.org/edit'+pos+'" target="_blank">Karte bearbeiten!</a>';
	    document.getElementById('errorMap').innerHTML = '<a class="btn danger" href="http://www.openstreetbugs.org'+pos+'" target="_blank">Fehler melden!</a>';
	}
	