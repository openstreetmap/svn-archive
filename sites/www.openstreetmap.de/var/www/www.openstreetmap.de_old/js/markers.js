    /* markers.js */

    function createMarkers(map) {			
	proj4326 = new OpenLayers.Projection("EPSG:4326");
        var strategy = new OpenLayers.Strategy.Cluster({distance: 25, threshold: 3});
		var dynStyle = new OpenLayers.Style({label: "${name}",pointerEvents: "visiblePainted", graphicTitle: "${name}",externalGraphic: "${icon}",graphicWidth: "${graphicWidth}",graphicHeight: "${graphicHeight}",graphicYOffset:"${offsetY}",graphicXOffset:"${offsetX}",
						fontColor: "black",fontSize: "10px",fontWeight:"bold",fontFamily: "Courier New, monospace",labelAlign: "lt"},
					{context:{
						name:function(feature){
							 if(feature.cluster) {
								return feature.cluster.length.toString(); //display number
							}
							else return "";
						},
						icon:function(feature){
							return "/img/localgroup.png";
						},
						offsetX:function(feature){
							if(feature.cluster) {
								if(feature.cluster.length>=10) return -7; else return -10; //keep text centered
							}
							else return 10;
						},
						offsetY:function(feature){
							if(feature.cluster) {
								return -20;
							}
							else return -20;
						},
						graphicWidth:function(feature){
							if(feature.cluster) {
									return 22;
								}
								else return 12;
						},
						graphicHeight:function(feature){
							if(feature.cluster) {
								return 22;
							}
							else return 12;
						}
					}
				});
		groups = new OpenLayers.Layer.GML("Lokale Gruppen","osm_user_groups_dach.kml", { projection: proj4326, displayInLayerSwitcher: true, format: OpenLayers.Format.KML,formatOptions: { extractStyles: false, extractAttributes: true },strategies:[strategy],styleMap: new OpenLayers.StyleMap({"default": dynStyle})});
	map.addLayers([groups]);
	//init Interaction
	var select = new OpenLayers.Control.SelectFeature(groups, {clickout: true,multiple: false,onSelect: groupSelect,onUnselect:groupUnselect});
	map.addControl(select);
	select.activate();
}

//create popup content for a POI
	function getPOIContent(data)
	{
		text="<p class='pop_heading'>"+data.name+"<\/p>";
		if(data.photo)
		{
			text+= '<img src="'+data.photo+'" width=300px style="margin-right:20px; margin-bottom:10px"><br>';
		}
		else
		{
			text+= '<img src="/img/nophoto.png" style="margin-right:20px; margin-bottom:10px" title="No photo available"><br>';
		}
		if(data.when) text+="<p class='pop_text'>"+data.when
			else text+="<p class='pop_text'> When: ?";
		if(data.where) text+="<br>Location: "+data.where+"<\/p>";
			else text+="<br>Location: ?<\/p>";
		text+='<a href="'+data.wiki+'"'+" class='pop_link'>Wiki<\/a> ";
		if (data.url) {text+='<a href="'+data.url+'"'+" class='pop_link'>WWW<\/a>";}
		if (data.mail) {text+='<a href="'+data.mail+'"'+" class='pop_link'>Mail<\/a>";}
		return text;
		//return data.toSource();
	}  

//make a popup with infos when POI is selected
	function groupSelect(feature) {
		if (feature.cluster==null)
		{
			poicontent='';
			poicontent=getPOIContent(feature.data);
			feature.popup = new OpenLayers.Popup.FramedCloud(null,
		                        feature.geometry.getBounds().getCenterLonLat(),
		                        new OpenLayers.Size(200,200),
		                        "<div class='popupcontent'>"+poicontent+"<\/div>",
		                        feature.marker,
		                        true);
			map.addPopup(feature.popup);
			//feature.popup.updateSize();
		}
        }

	//close popup
	function groupUnselect(feature) {
	    	if (feature.popup) {
			map.removePopup(feature.popup);
			feature.popup.destroy();
			feature.popup = null;
	    	} 
	}      