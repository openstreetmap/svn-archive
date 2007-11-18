
        var map, controls, drawControls;
        var osmLayer;
        var selectedFeature, currentSelectedFeature;
        var fpopup=null;
        var allControls;
        var cvtr;
        var initZoom;
        var editMode;
        var mouseIsDown = false, dragged = false;
        var isIn = null;
        var md;
          var recentDrag = false;

        function init(){
           
    
            var base;

            currentSelectedFeature = null;

            if (basemap=="npe")
            {
                cvtr = new converter("OSGB");

                /* NPE BEGIN  */
                map = new OpenLayers.Map('map',
                { maxExtent: new OpenLayers.Bounds (0,0,699999,999999),
                  maxResolution : 8, 
                  units: 'meters' }    
                );

                base = new OpenLayers.Layer.WMS( "New Popular Edition", 
                "http://nick.dev.openstreetmap.org/openpaths/freemap.php",
                {'layers': 'npe'},{buffer:1} );

                
            }
            else if (basemap=="osm")
            {
                cvtr = new converter("GOOG");

                map = new OpenLayers.Map('map',
                { maxExtent: new OpenLayers.Bounds (-20037508.34,
                -20037508.34,20037508.34,20037508.34),
                   numZoomLevels: 19, maxResolution:156543,    
                  units: 'meters',projection:'EPSG:41001' }    
                );

                base = new OpenLayers.Layer.TMS( "tiles@home", 
                "http://dev.openstreetmap.org/~ojw/Tiles/tile.php/",
                {'type': 'png', getURL:get_osm_url} );

                initZoom=14;
            }
            else if (basemap=="landsat")
            {
                cvtr = new converter("");
                map=new OpenLayers.Map('map');
                base =  new OpenLayers.Layer.WMS( "Landsat", 
                "http://www.free-map.org.uk/freemap/common/landsat.php");
                initZoom=14;
            }
            else
            {
                // main freemap maps
                cvtr = new converter("Mercator");

                map = new OpenLayers.Map('map',
                { maxExtent: new OpenLayers.Bounds
                    (-700000,6500000,200000,8100000),
                resolutions: [10],
                tileSize: new OpenLayers.Size(500,500),
                        units: 'meters' }    
                          );

                base = new OpenLayers.Layer.WMS( "Freemap/Mapnik", 
                        "http://www.free-map.org.uk/cgi-bin/render",
                            {buffer:1} );
            }

            map.addLayer(base);
            //map.addLayer(tpts);
            
            map.setCenter(new OpenLayers.LonLat(easting,northing),initZoom);
            map.addControl(new OpenLayers.Control.LayerSwitcher());

            //map.zoom = 1;

            /*    NPE END */

            
            osmLayer = new OpenLayers.Layer.OSMMarkers("OSM Layer");
            osmLayer.basemap = basemap;

            // create a point feature
            // NEW

            map.addLayer(osmLayer);

            md = new OpenLayers.Control.MouseDefaults();
            md.setMap(map);

            /*
            controls = {
                select: new OpenLayers.Control.SelectFeature
                    (osmLayer,{callbacks:{'up':selectUp,'over':selectOver}})
                        };
            for(var key in controls)
            {
                map.addControl(controls[key]);
            }
            controls.select.deactivate();

            drawControls = {
                point: new OpenLayers.Control.DrawOSMFeature
                    (osmLayer,OpenLayers.Handler.Point),
                polygon: new OpenLayers.Control.DrawOSMFeature
                    (osmLayer,OpenLayers.Handler.Polygon)
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
            */

            map.events.remove('mousemove');
            map.events.remove('mouseup');
            map.events.remove('mousedown');

            map.events.register('mouseup',map,mouseUpHandler);
            map.events.register('mousedown',map,mouseDownHandler);
            map.events.register('mousemove',map,mouseMoveHandler);

            map.events.register('click',map,mouseClickHandler);

            var bounds = cvtr.customToNormBounds(map.getExtent());
            osmLayer.load(bounds);
            var bl = new OpenLayers.LonLat(bounds.left,bounds.bottom);
            var tr = new OpenLayers.LonLat(bounds.right,bounds.top);
            updateLinks((bl.lon+tr.lon)/2,(bl.lat+tr.lat)/2);
            updateLinks((bl.lon+tr.lon)/2,(bl.lat+tr.lat)/2);

            document.getElementById('searchButton').onclick = placeSearch;
            document.getElementById('goButton').onclick = goToLocation; 
            //setEditMode("navigate");
        }

        // from informationfreeway.org
        function get_osm_url (bounds) {
            var res = this.map.getResolution();
            var x = Math.round 
                ((bounds.left - this.maxExtent.left) / (res * this.tileSize.w));
            var y = Math.round 
                ((this.maxExtent.top - bounds.top) / (res * this.tileSize.h));
            var z = this.map.getZoom();
            var path = z + "/" + x + "/" + y + "." + this.type; 
            var url = this.url;
            if (url instanceof Array) {
                url = this.selectUrl(path, url);
                                                                                        }
                                                                                        return url + path;
                                                                                        }
                                                                                        
        function selectUp(f)
        {
            if(f instanceof OpenLayers.Feature.OSM)
            {
                var t = osmLayer.routeTypes.getType(f.osmitem.tags);
                var nstr = (f.osmitem.tags['name']) ? 
                        f.osmitem.tags['name']+", " : "";
                statusMsg('You selected ' + nstr + 'a ' + t + 
                ' (ID ' + f.osmitem.osmid + ')');
                selectedFeature = f; 
            }
        }

        function selectOver()
        {
            //alert('selectOver');
        }

        function mouseDownHandler(e)
        {
               recentDrag = false;
            statusMsg("MOUSEDOWN");
            mouseIsDown = true;
            md.defaultMouseDown(e);
            if(e.preventDefault)
                e.preventDefault();
            return false;
        }

        function mouseMoveHandler(e)
        {
            if(mouseIsDown)
            {
                statusMsg("MOUSEDRAG");
                dragged=true;    
            }
               else
               {
                var mapclickpos=map.events.getMousePosition(e);
                var lonLat = cvtr.customToNorm
                         (map.getLonLatFromViewPortPx(mapclickpos));
                showPosition(lonLat);
               }
            md.defaultMouseMove(e);
            if(e.preventDefault)
                e.preventDefault();
            return false;
        }

        function mouseUpHandler(e)
        {
            if(dragged==true)//editMode=="navigate")
            {
                var bounds = cvtr.customToNormBounds(map.getExtent());
                osmLayer.load(bounds);
                var bl = new OpenLayers.LonLat(bounds.left,bounds.bottom);
                var tr = new OpenLayers.LonLat(bounds.right,bounds.top);
                updateLinks((bl.lon+tr.lon)/2,(bl.lat+tr.lat)/2);
                //showPosition(cvtr.customToNorm(map.getCenter()));
                    recentDrag = true;
            }

            dragged=false;
            mouseIsDown=false;
            md.defaultMouseUp(e);
            if(e.preventDefault)
                e.preventDefault();
            return false;
        }

        function mouseClickHandler(e)
        {
            if(!recentDrag)//editMode=="draw")
            {
                var mapclickpos=map.events.getMousePosition(e);
                var lonLat = map.getLonLatFromViewPortPx(mapclickpos);
                var id = osmLayer.nextNodeId--;
                osmLayer.nodes[id]=new OpenLayers.OSMNode();
                osmLayer.nodes[id].geometry = 
                    new OpenLayers.Geometry.Point(lonLat.lon, lonLat.lat);
                osmLayer.nodes[id].tags['created_by'] = 'POI Editor';
                if(osmLayer.basemap=='npe' || osmLayer.basemap=='landsat')
                    osmLayer.nodes[id].tags['source'] = osmLayer.basemap;
                osmLayer.nodes[id].osmid = id;
                osmLayer.uploadNewNode(osmLayer.nodes[id]);
            }
        }


        function uploadChangesWithTagCheck()
        {
            if(currentSelectedFeature)
            {
                var t = osmLayer.routeTypes.getType
                    (currentSelectedFeature.osmitem.tags);
                var featureclass = osmLayer.routeTypes.getFeatureClass(t);
                /*
                if(featureclass===null||(selectedFeature.geometry instanceof 
                    OpenLayers.Geometry.LineString && featureclass=="line" ) ||
                    (selectedFeature.geometry instanceof 
                    OpenLayers.Geometry.Polygon && featureclass=="polygon" ) ||
                    (selectedFeature.geometry instanceof 
                    OpenLayers.Geometry.Point && featureclass=="point" ) )
                */
                if(true)
                {
                    uploadChanges();
                }
                else
                {
                    alert('Type mismatch - not uploading');
                }
            }
            else
            {
                alert('no selected feature');
            }
        }

        function uploadChanges()
        {
            var call= "node";
            /*
                    (currentSelectedFeature.osmitem instanceof 
                    OpenLayers.OSMNode) ? "node":"way";
            */
            var URL = 
                'http://www.free-map.org.uk/freemap/common/osmproxy2.php'
                + '?call='+call+'&id=' + currentSelectedFeature.osmitem.osmid;
                    
            currentSelectedFeature.osmitem.upload
                ( URL, null, refreshStyle, currentSelectedFeature);

            fh();
        }

        function refreshStyle(xmlHTTP,w)
        {
               if(xmlHTTP.responseText.substring(0,5)=="ERROR")
               {
                    alert("Error sending changes to server: " +
                           xmlHTTP.responseText.substring(6) + 
                           " Please try again.");
               }
               else
               {
                 statusMsg('Changes uploaded to server successfully.');
                 /*
                 if(w.osmitem instanceof OpenLayers.OSMWay)
                 {
                var t = osmLayer.routeTypes.getType(w.osmitem.tags);
                var colour = osmLayer.routeTypes.getColour(t);
                var width = osmLayer.routeTypes.getWidth(t);
                var style = { fillColor: colour, fillOpacity: 0.4,
                    strokeColor: colour, strokeOpacity: 1,
                    strokeWidth: width };
                w.style=style;
                w.originalStyle=style;
                osmLayer.drawFeature(w,style);
                 }
                 */
               }
        }


        function setEditMode(mode)
        {
            var modes = new Array("navigate","draw","edit");
            editMode = mode;
            for(var count=0; count<modes.length; count++)
            {
                document.getElementById("mode_"+modes[count]).
                    style.backgroundColor = 
                    (modes[count]==mode) ? '#8080ff':'#000080';

                /*
                if (allControls[modes[count]])
                {
                    if(modes[count]==mode)
                        allControls[modes[count]].activate();
                    else
                        allControls[modes[count]].deactivate();
                }
                */
            }

            if(mode!="select")
                selectedFeature=null;
        }

            function changeFeature(selFeature)
            {
                if(selFeature)
                {
                    currentSelectedFeature=selFeature;
                    var t = osmLayer.routeTypes.getType
                        (currentSelectedFeature.osmitem.tags);
                    var f = 
                    (currentSelectedFeature.osmitem instanceof 
                        OpenLayers.OSMWay)?
                            osmLayer.routeTypes.getTypes('polygon') : 
                            osmLayer.routeTypes.getTypes('point');

                    var html = "<h3>Please enter details</h3>";
                    var val = (currentSelectedFeature.osmitem.tags['name'] ) ?
                    currentSelectedFeature.osmitem.tags['name']  : "";
                    html+=
                    "<label for='fname'>Name</label><br/>"+
                    "<input id='fname' class='textbox' value=\""+
                            val+"\"/><br/>" +
                    "<label for='ftype'>Type</label><br/>"+
                    "<select id='ftype'>" +
                    "<option>--Select--</option>";

                    for(var count=0; count<f.length; count++)
                    {
                        html += "<option";
                        if(f[count]==t)
                            html+=" selected='selected'";
                        html += ">"+f[count]+"</option>";
                    }
                    html += "</select><br/>";
                    html += "<h4>Full tags:</h4>";
                    html += "<p><select id='tagkey'></select>";
                    html += "<input id='tagvalue'/>";
                    html += "<input type='button' id='ubtn' value='Update!'/>";
                    html += "<input type='button' id='delt' value='Delete'/>";
                    html += "<br/><input type='button' id='addt' ";
                    html += "value='Add tag'/></p>";
                    html += "<input type='button' id='fbutton1' value='Go!'/>" +
                        "<input type='button' value='Cancel' id='fbutton2'/>";
                    fpopup = new OpenLayers.Popup('fpopup',
                        map.getLonLatFromViewPortPx    
                            (new OpenLayers.Pixel(50,50)),
                        new OpenLayers.Size(480,360),html);
                    fpopup.setBackgroundColor('#ffffc0');
                    fpopup.setOpacity(0.6);
                    map.addPopup(fpopup);
                    document.getElementById('fbutton1').onclick=
                            uploadChangesWithTagCheck;
                    document.getElementById('fbutton2').onclick=fh;
                    document.getElementById('fname').onblur=updateNameTag;
                    document.getElementById('ubtn').onclick=
                        updateSelectedItemTags;
                    document.getElementById('addt').onclick= addTag;
                    document.getElementById('delt').onclick= deleteTag;
                    document.getElementById('tagkey').onchange=
                        tagSelectChange;
                    document.getElementById('ftype').onchange= typeChange;    
                    populateFullTags();
                }
                else
                {
                    alert('no selected feature');
                }
        }

        function typeChange()
        {
            var t = (document.getElementById('ftype').value=="--Select--")?
                    "" : document.getElementById('ftype').value;
            var newTags = osmLayer.routeTypes.getTags(t);
            currentSelectedFeature.osmitem.updateTags(newTags);
            if(currentSelectedFeature.osmitem.isPlace() && isIn &&
                !currentSelectedFeature.osmitem.tags['is_in'] )
            {
                currentSelectedFeature.osmitem.tags['is_in'] = isIn;
            }
            populateFullTags();
        }

        function fh()
        {
            map.removePopup(fpopup);
               //document.getElementById('editbox').style.visibility= 'hidden';
            fpopup = null;
            currentSelectedFeature = null;
        }

        function populateFullTags()
        {
               var selectbox = document.getElementById('tagkey');

            while(selectbox.options.length > 0)
                selectbox.options[0] = null;

            for(var tag in currentSelectedFeature.osmitem.tags)
            {
                if(currentSelectedFeature.osmitem.tags[tag])
                {
                    selectbox.options[selectbox.options.length]= 
                            new Option(tag,tag);
                }
            }
            document.getElementById('tagvalue').value =     
                currentSelectedFeature.osmitem.tags
                    [document.getElementById('tagkey').value];
        }

        function tagSelectChange()
        {
            document.getElementById('tagvalue').value =     
                currentSelectedFeature.osmitem.tags
                    [document.getElementById('tagkey').value];
        }

        function updateSelectedItemTags()
        {
            if(document.getElementById('tagkey').value=='is_in')
                isIn = document.getElementById('tagvalue').value; 
            currentSelectedFeature.osmitem.tags
                [document.getElementById('tagkey').value] = 
                    document.getElementById('tagvalue').value;
            if (document.getElementById('tagkey').value=="name")
            {
                document.getElementById().value = 
                    document.getElementById('tagvalue').value;
            }
        }

        function addTag()
        {
            var key = prompt("Tag:");
            var value = prompt("Value:");
            if(key=='is_in')
                isIn = value;
            currentSelectedFeature.osmitem.tags[key] = value;
            populateFullTags();
        }

        function deleteTag()
        {
            currentSelectedFeature.osmitem.tags
                [document.getElementById('tagkey').value] = null;
            populateFullTags();
            
        }

        function updateNameTag()
        {
            currentSelectedFeature.osmitem.tags['name'] = 
                document.getElementById('fname').value;
            populateFullTags();
        }

        function statusMsg(msg)
        {
            document.getElementById('status').innerHTML = msg;
        }

        function showPosition(position)
        {
            document.getElementById('position').innerHTML = 
                "Pos (" + position.lat.toFixed(3) + "," + 
                        position.lon.toFixed(3) + ")";
        }

        function placeSearch()
        {
            var loc = document.getElementById('search').value;
            var country = document.getElementById('country').value;
            ajaxrequest
                ("http://www.free-map.org.uk/freemap/common/geocoder_ajax.php", 
                'POST',"place="+loc+"&country="+country, searchCallback);
        }

		function goToLocation()
		{
			var LL = new OpenLayers.LonLat
						(document.getElementById('inpLon').value,
						 document.getElementById('inpLat').value);
			reposition(LL);
		}

        function searchCallback(xmlHTTP, addData)
        {
            var latlon = xmlHTTP.responseText.split(",");
            if(latlon[0]!="0" && latlon[1]!="0")
            {
                var normLL = new OpenLayers.LonLat
                    (parseFloat(latlon[1]),parseFloat(latlon[0]));
                    reposition(normLL);
            }
            else
            {
                alert("That place is not in the database");
            }
        }

          function reposition(normLL)
          {
               map.setCenter(cvtr.normToCustom(normLL));
               var bounds = cvtr.customToNormBounds(map.getExtent());
               osmLayer.load(bounds);
          }

        function test()
        {
            alert('zoom is : ' +  map.zoom);
            for(var a in map.baseLayer.resolutions)
            {
                alert('key=' + a + ' value=' + map.baseLayer.resolutions[a]);
            }
        }

        function testlayer()
        {
            alert(map.baseLayer.name);
        }

    function updateLinks (lon,lat)
    {
        document.getElementById('base_npe').href =
            '/freemap/edit.php?basemap=npe&lat='+lat+'&lon='+lon;
        document.getElementById('base_freemap').href =
            '/freemap/edit.php?basemap=freemap&lat='+lat+'&lon='+lon;
        document.getElementById('base_osm').href =
            '/freemap/edit.php?basemap=osm&lat='+lat+'&lon='+lon;
        document.getElementById('base_landsat').href =
            '/freemap/edit.php?basemap=landsat&lat='+lat+'&lon='+lon;
    }
