
        var map, controls, drawControls;
		var vectorLayer;
		var selectedFeature;
		var fpopup=null;
		var allControls;

		function selectUp(f)
		{
			if(f instanceof OpenLayers.Feature.OSM)
			{
				var nstr = (f.osmitem.tags['name']) ? 
						f.osmitem.tags['name']+", " : "";
				statusMsg('You selected ' + nstr + 'a ' + f.osmitem.type + 
				' (ID ' + f.osmitem.osmid + ')');
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
				var cvtr = new converter("OSGB");
				var bounds = cvtr.customToNormBounds(map.getExtent());
				vectorLayer.load(bounds);
			}
		}

		function setType(n,t)
		{
			if(selectedFeature)
			{
				var featureclass = vectorLayer.routeTypes.getFeatureClass(t);
				var call=(featureclass=="point") ? "node":"way";
				if((selectedFeature.geometry instanceof 
					OpenLayers.Geometry.LineString && featureclass=="line" ) ||
					(selectedFeature.geometry instanceof 
					OpenLayers.Geometry.Polygon && featureclass=="polygon" ) ||
					(selectedFeature.geometry instanceof 
					OpenLayers.Geometry.Point && featureclass=="point" ) )
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
					}

					selectedFeature.osmitem.tags['name'] = n;

					var URL = 
					'http://www.free-map.org.uk/freemap/common/osmproxy2.php'
					+ '?call='+call+'&id=' + selectedFeature.osmitem.osmid;
					
					selectedFeature.osmitem.upload
										( URL, null, refreshStyle,
											selectedFeature);
				}
				else
				{
					alert('Type mismatch');
				}
			}
			else
			{
				alert('setType(): no selected feature');
			}
		}

		function refreshStyle(xmlHTTP,w)
		{
			statusMsg('Changes uploaded to server. Response=' + 
				xmlHTTP.responseText);
			if(w.osmitem instanceof OpenLayers.OSMWay)
			{
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
		}

        function init(){
            
			/* NPE BEGIN  */
			map = new OpenLayers.Map('map',
			{ maxExtent: new OpenLayers.Bounds (0,0,599999,999999),
	  		maxResolution : 8, 
	  		units: 'meters' }	
			);
	
			var npe = new OpenLayers.Layer.WMS( "New Popular Edition", 
				"http://nick.dev.openstreetmap.org/openpaths/freemap.php",
				{'layers': 'npe'},{buffer:1} );

			var blank = new OpenLayers.Layer.WMS( "None", 
				"http://www.free-map.org.uk/freemap/common/trackpoints.php",
				{buffer:1} );

			/*
			var tpts = new OpenLayers.Layer.WMS( "OSM Trackpoints", 
				"http://www.free-map.org.uk/freemap/common/tp.php",
				{'layers': 'trackpoints'} , {buffer:1} );
			*/



			map.addLayer(npe);
			map.addLayer(blank);
			//map.addLayer(tpts);
			
			map.setCenter(new OpenLayers.LonLat(easting,northing));
			map.addControl(new OpenLayers.Control.LayerSwitcher());

			//map.zoom = 1;

			/*	NPE END */

			
            vectorLayer = new OpenLayers.Layer.OSM("OSM Layer");

            // create a point feature
			// NEW
			/* DEMO STUFF
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
			DEMO STUFF END */
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
				point: new OpenLayers.Control.DrawOSMFeature
					(vectorLayer,OpenLayers.Handler.Point),
				polygon: new OpenLayers.Control.DrawOSMFeature
					(vectorLayer,OpenLayers.Handler.Polygon)
			};
			for(var key in drawControls)
			{
				map.addControl(drawControls[key]);
			}
			
			drawControls.point.deactivate();

			allControls = new Array();
			allControls['select'] = controls['select'];
			allControls['draw'] = drawControls['point'];
			allControls['polygon'] = drawControls['polygon'];

            //vectorLayer.addFeatures([pointFeature, lineFeature]);

            //map.setCenter(new OpenLayers.LonLat(point.x, point.y),15);
			map.events.register('mouseup',map,mouseUpHandler);
			var cvtr = new converter("OSGB");
			//var bounds = map.getExtent();
			var bounds = cvtr.customToNormBounds(map.getExtent());
			vectorLayer.load(bounds);

			document.getElementById('searchButton').onclick = placeSearch;
        }

		function setEditMode(mode)
		{
			var modes = new Array("navigate","select","draw","polygon");
			for(var count=0; count<modes.size; count++)
			{
				document.getElementById("mode_"+modes[count]).
					style.backgroundColor = 
					(modes[count]==mode) ? '#8080ff':'#000080';

				if (allControls[modes[count]])
				{
					if(modes[count]==mode)
						allControls[modes[count]].activate();
					else
						allControls[modes[count]].deactivate();
				}
			}

			if(mode!="select")
				selectedFeature=null;
		}

			void changeFeature()
			{
					var f = 
					(selectedFeature.osmitem instanceof OpenLayers.OSMWay)?
						new Array("--Select--","wood","lake","heath",
									"crag","scree","golf course"):
						new Array("--Select--","village","town","pub","church",
									"viewpoint","peak","mast","hamlet",
									"farm","country house");

					var html = "<h3>Please enter details</h3>";
					var val = (selectedFeature.osmitem.tags['name'] ) ?
					selectedFeature.osmitem.tags['name']  : "";
					html+=
					"<label for='fname'>Name</label><br/>"+
					"<input id='fname' class='textbox' value='"+val+"'/><br/>" +
					"<label for='ftype'>Type</label><br/>"+
					"<select id='ftype'>";

					for(var count=0; count<f.length; count++)
					{
						html += "<option";
						if(f[count]==selectedFeature.osmitem.type)
							html+=" selected='selected'";
						html += ">"+f[count]+"</option>";
					}
					html += "</select><br/>";
					html += "<input type='button' id='fbutton1' value='Go!'/>" +
						"<input type='button' value='Cancel' id='fbutton2'/>";
					fpopup = new OpenLayers.Popup('fpopup',
						map.getLonLatFromViewPortPx	
							(new OpenLayers.Pixel(50,50)),
						new OpenLayers.Size(480,360),html);
					fpopup.setBackgroundColor('#ffffc0');
					map.addPopup(fpopup);
					document.getElementById('fbutton1').onclick=fc;
					document.getElementById('fbutton2').onclick=fh;
				}
				else
				{
					alert('no selected feature');
				}
				break;
		}

		function fc()
		{
			var t = (document.getElementById('ftype').value=="--Select--")?
					"" : document.getElementById('ftype').value;
			setType(document.getElementById('fname').value,t);
			fh();
		}

		function fh()
		{
			map.removePopup(fpopup);
			fpopup = null;
		}

		function statusMsg(msg)
		{
			document.getElementById('status').innerHTML = msg;
		}

		function placeSearch()
		{
			var loc = document.getElementById('search').value;
			var country = document.getElementById('country').value;
			ajaxrequest
				("http://www.free-map.org.uk/freemap/common/geocoder_ajax.php", 
				'POST',"place="+loc+"&country="+country, searchCallback);
		}

		function searchCallback(xmlHTTP, addData)
		{
			var latlon = xmlHTTP.responseText.split(",");
			if(latlon[0]!="0" && latlon[1]!="0")
			{
				var normLL = new OpenLayers.LonLat
					(parseFloat(latlon[1]),parseFloat(latlon[0]));
				var cvtr = new converter("OSGB");
				var convLL  = cvtr.normToCustom(normLL);
				map.setCenter(convLL);
				//var bounds = map.getExtent();
				var bounds = cvtr.customToNormBounds(map.getExtent());
				vectorLayer.load(bounds);
			}
			else
			{
				alert("That place is not in the database");
			}
		}

		function test()
		{
			alert('zoom is : ' +  map.zoom);
			for(var a in map.baseLayer.resolutions)
			{
				alert('key=' + a + ' value=' + map.baseLayer.resolutions[a]);
			}
		}
