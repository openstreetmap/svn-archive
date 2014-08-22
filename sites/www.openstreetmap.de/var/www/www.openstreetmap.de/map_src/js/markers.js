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
 * Title: markers.js
 * Description: functions to create the 'local community' overlay
 *
 * @version 0.1 2011-10-29
 */

    function createMarkers(map) {			
        
		var dynStyle = new OpenLayers.Style({label: "${name}",pointerEvents: "visiblePainted", graphicTitle: "${name}",externalGraphic: "${icon}",graphicWidth: "${graphicWidth}",graphicHeight: "${graphicHeight}",graphicYOffset:"${offsetY}",graphicXOffset:"${offsetX}", fontColor: "black",fontSize: "11px",fontWeight:"bold",fontFamily: "Courier New, monospace",labelAlign: "lt"},
					{context:{
						name:function(feature){
							 if(feature.cluster && feature.cluster.length > 1) {
								return feature.cluster.length.toString(); //display number
							}
							else return "";
						},
						icon:function(feature){
							return "img/localgroup.png";
						},
						offsetX:function(feature){
							if(feature.cluster && feature.cluster.length > 1) {
								if(feature.cluster.length>=10) return -7; else return -10; //keep text centered
							}
							else return 0;//10;
						},
						offsetY:function(feature){
							if(feature.cluster && feature.cluster.length > 1) {
								return -20;
							}
							else return 0;//-20;
						},
						graphicWidth:function(feature){
							if(feature.cluster && feature.cluster.length > 1) {
									return 22;
							}
							else return 12;
						},
						graphicHeight:function(feature){
							if(feature.cluster && feature.cluster.length > 1) {
								return 22;
							}
							else return 12;
						}
					}
				});
	groups = new OpenLayers.Layer.Vector("Lokale Gruppen", { projection: proj4326,
            strategies: [new OpenLayers.Strategy.Fixed(),
                        new OpenLayers.Strategy.Cluster()],
            protocol: new OpenLayers.Protocol.HTTP({
                url: "map_src/osm_user_groups_DACH.kml",
                format: new OpenLayers.Format.KML({ extractStyles: false, extractAttributes: true })
            }),
            styleMap: new OpenLayers.StyleMap({"default": dynStyle})
        });
	//groups.visibility=false;
	map.addLayers([groups]);

	
	//init Interaction
	sel = new OpenLayers.Control.SelectFeature(groups);
		groups.events.on({
			"featureselected": onFeatureSelect,
			"featureunselected": onFeatureUnselect
		});
	map.addControl(sel);
	sel.activate();
}

//create popup content for a POI
	function getPOIContent(data)
	{
		text="<p class='pop_heading'>"+data.name+"<\/p>";
		if(data.photo)
		{
			text+= '<img src="'+data.photo+'" width="300px" style="margin-right:20px; margin-bottom:10px"><br>';
		}
		else
		{
			text+= '<img src="./map_src/img/nophoto.png" width="150px" style="margin-right:20px; margin-bottom:10px" title="No photo available"><br>';
		}
		if(data.when) text+="<p class='pop_text'>When: "+data.when
		else text+="<p class='pop_text'> When: ?";
		if(data.where) text+="<br>Location: "+data.where+"<\/p>";
		else text+="<br>Location: ?<\/p>";
		text+='<a href="'+data.wiki+'"'+" class='pop_link'>Wiki<\/a> ";
		if (data.url) {text+='<a href="'+data.url+'"'+" class='pop_link'>WWW<\/a>";}
		if (data.mail) {text+='<a href="'+data.mail+'"'+" class='pop_link'>Mail<\/a>";}
		return text;
		//return data.toSource();
	}  

	function onPopupClose(evt) {
		sel.unselectAll();
	}
	function onFeatureSelect(event) {
		var feature = event.feature;
		var content = getPOIContent(event.feature.cluster[0].data);
		popup = new OpenLayers.Popup.FramedCloud("popup", 
			feature.geometry.getBounds().getCenterLonLat(),
			null,
			"<div class='popupcontent'>"+content+"<\/div>",
			null,
			true,
			onPopupClose
		);
		feature.popup = popup;
		feature.popup.minSize = new OpenLayers.Size(280,250);
		map.addPopup(popup);
	}
	function onFeatureUnselect(event) {
		var feature = event.feature;
		if(feature.popup) {
			map.removePopup(feature.popup);
			feature.popup.destroy();
			delete feature.popup;
		}
	}
