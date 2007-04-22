
        var map, controls, drawControls;
		var vectorLayer;
		var selectedFeature;

		function selectUp(f)
		{
			if(f instanceof OpenLayers.Feature.OSM)
			{
				var item = f.osmitem;
				if(item instanceof OpenLayers.OSMWay)
				{
					alert('You selected a ' + item.type + ' with an ID of ' + 
						item.osmid);
				}
				else if (item instanceof OpenLayers.OSMNode)
				{
					alert('You selected a POI. XML=' + item.toXML());
				}

				selectedFeature = f; 
			}
		}

		function selectOver()
		{
			//alert('selectOver');
		}

		function mouseUpHandler(e)
		{
			if(!(controls.select.active))
			{
				//var bounds = map.getExtent();
				var cvtr = new converter("OSGB");
				var bounds = cvtr.customToNormBounds(map.getExtent());
				vectorLayer.load(bounds);
			}
		}

		function setType(t)
		{
			if(selectedFeature)
			{
				var p = vectorLayer.routeTypes.isPolygon(t);
				if((selectedFeature.geometry instanceof 
					OpenLayers.Geometry.LineString && !p ) ||
					(selectedFeature.geometry instanceof 
					OpenLayers.Geometry.Polygon && p ))
				{

					selectedFeature.osmitem.setType(t);
					var newTags = vectorLayer.routeTypes.getTags(t);
					if(newTags)
					{
						for(tag in newTags)
						{
							selectedFeature.osmitem.tags[tag] = newTags[tag];
						}

						// Blank any tags which should no longer be there
						for(tag in selectedFeature.osmitem.tags)
						{
							if(!newTags[tag])
							selectedFeature.osmitem.tags[tag]=null;
						}

						var URL = 
						'http://www.free-map.org.uk/freemap/common/osmproxy2.php'
							+ '?call=way&id=' + selectedFeature.osmitem.osmid;

						selectedFeature.osmitem.upload
										( URL, null, refreshStyle,
											selectedFeature);
					}
					else
					{
						alert('unrecognised feature type');
					}
				}
				else
				{
					alert('Either this is a polygon and you specified a non-'+
					      'polygon type, or this is not a polygon and you ' +
						  'specified a polygon type');
				}
			}
			else
			{
				alert('setType(): no selected feature');
			}
		}

		function refreshStyle(xmlHTTP,w)
		{
			alert('Changes uploaded to server. Response=' + 
				xmlHTTP.responseText);
			var t = w.osmitem.type;
			var colour = vectorLayer.routeTypes.getColour(t);
			var width = vectorLayer.routeTypes.getWidth(t);
			var style = { fillColor: colour, fillOpacity: 0.4,
					strokeColor: colour, strokeOpacity: 1,
					strokeWidth: width };
			w.style=style;
			w.originalStyle=style;
			vectorLayer.drawFeature(w,style);
		}

        function init(){
			/*
            map = new OpenLayers.Map( $('map') );
            var layer = new OpenLayers.Layer.WMS( "OpenLayers WMS", 
                    "http://labs.metacarta.com/wms/vmap0", {layers: 'basic'} );
            map.addLayer(layer);
			*/
            
			/* NPE BEGIN  */
			map = new OpenLayers.Map('map',
			{ maxExtent: new OpenLayers.Bounds (0,0,599999,999999),
	  		maxResolution : 8, 
	  		units: 'meters' }	
			);
	
			var blank = new OpenLayers.Layer.WMS( "None", 
				"http://www.free-map.org.uk/freemap/common/trackpoints.php",
				{buffer:1} );

			var tpts = new OpenLayers.Layer.WMS( "OSM Trackpoints", 
				"http://www.free-map.org.uk/freemap/common/trackpoints.php",
				{'layers': 'trackpoints'} , {buffer:1} );

			var npe = new OpenLayers.Layer.WMS( "New Popular Edition", 
				"http://nick.dev.openstreetmap.org/openpaths/freemap.php",
				{'layers': 'npe'},{buffer:1} );

			map.addLayer(blank);
			map.addLayer(tpts);
			map.addLayer(npe);
			
			map.setCenter(new OpenLayers.LonLat(easting,northing));
			map.addControl(new OpenLayers.Control.LayerSwitcher());

//			map.zoom = 1;

			/*	NPE END */

			
            vectorLayer = new OpenLayers.Layer.OSM("OSM Layer");

            // create a point feature
			// NEW
            var point = new OpenLayers.Geometry.Point(easting,northing);
            var pointFeature = new OpenLayers.Feature.Vector(point);
			vectorLayer.addFeatures(pointFeature);
				
            
            // create a line feature from a list of points
            var pointList = [];
            var newPoint = point;
            for(var p=0; p<5; ++p) {
                newPoint = new OpenLayers.Geometry.Point
					(newPoint.x + Math.random(1),
				 	newPoint.y + Math.random(1));
                pointList.push(newPoint);
            }
            var lineFeature = new OpenLayers.Feature.Vector(
                new OpenLayers.Geometry.LineString(pointList));

			vectorLayer.addFeatures(lineFeature);
            map.addLayer(vectorLayer);

			controls = {
				select: new OpenLayers.Control.SelectFeature
					(vectorLayer,{callbacks:{'up':selectUp,'over':selectOver}})
						};
			for(var key in controls)
			{
				map.addControl(controls[key]);
			}
			controls.select.deactivate();

			drawControls = {
				line: new OpenLayers.Control.DrawOSMFeature
					(vectorLayer,OpenLayers.Handler.Path),
				polygon: new OpenLayers.Control.DrawOSMFeature
					(vectorLayer,OpenLayers.Handler.Polygon)
			};
			for(var key in drawControls)
			{
				map.addControl(drawControls[key]);
			}
			
			drawControls.line.deactivate();
            //vectorLayer.addFeatures([pointFeature, lineFeature]);

            //map.setCenter(new OpenLayers.LonLat(point.x, point.y),15);
			map.events.register('mouseup',map,mouseUpHandler);
			var cvtr = new converter("OSGB");
			//var bounds = map.getExtent();
			var bounds = cvtr.customToNormBounds(map.getExtent());
			vectorLayer.load(bounds);
        }

		function nav()
		{
			controls.select.deactivate();
			drawControls.line.deactivate();
			drawControls.polygon.deactivate();
			document.getElementById('navbtn').disabled='disabled';
			document.getElementById('selbtn').disabled=null;
			document.getElementById('drwbtn').disabled=null;
			document.getElementById('drwpolybtn').disabled=null;
			selectedFeature=null;
		}
		function sel()
		{
			controls.select.activate();
			drawControls.line.deactivate();
			drawControls.polygon.deactivate();
			document.getElementById('selbtn').disabled='disabled';
			document.getElementById('navbtn').disabled=null;
			document.getElementById('drwbtn').disabled=null;
			document.getElementById('drwpolybtn').disabled=null;
		}
		function drw()
		{
			drawControls.line.activate();
			drawControls.polygon.deactivate();
			controls.select.deactivate();
			document.getElementById('drwbtn').disabled='disabled';
			document.getElementById('drwpolybtn').disabled=null;
			document.getElementById('navbtn').disabled=null;
			document.getElementById('selbtn').disabled=null;
			selectedFeature=null;
		}
		function drwpoly()
		{
			drawControls.line.deactivate();
			drawControls.polygon.activate();
			controls.select.deactivate();
			document.getElementById('drwpolybtn').disabled='disabled';
			document.getElementById('drwbtn').disabled=null;
			document.getElementById('navbtn').disabled=null;
			document.getElementById('selbtn').disabled=null;
			selectedFeature=null;
		}

		function chng()
		{
			if(selectedFeature)
			{
				var newType = prompt('Please enter the new classification');
				setType(newType);
			}
			else
			{
				alert('chng(): no selected feature');
			}
		}

		function statusMsg(msg)
		{
			document.getElementById('status').innerHTML = msg;
		}

		function placeSearch()
		{
			var loc = document.getElementById('search').value;
			var country = document.getElementById('country').value;
			ajaxrequest("http://www.free-map.org.uk/freemap/common/geocoder_ajax.php", 
				'POST',"place="+loc+"&country="+country, searchCallback);
		}

		function searchCallback(xmlHTTP, addData)
		{
			var latlon = xmlHTTP.responseText.split(",");
			if(latlon[0]!="0" && latlon[1]!="0")
			{
				var normLL = new OpenLayers.LonLat
					(parseFloat(latlon[1]),parseFloat(latlon[0]));
				map.setCenter(normLL, map.getZoom() );
				//var bounds = map.getExtent();
				var cvtr = new converter("OSGB");
				var bounds = cvtr.customToNormBounds(map.getExtent());
				vectorLayer.load(bounds);
			}
			else
			{
				alert("That place is not in the database");
			}
		}
