
function FreemapClient(map,addFeatureURL,georssURL,
                initEasting,initNorthing,initZoom,cvtrtype,du,dt,u,rd,
                ggURL)
{
	var self=this;
    this.osmmarkersLayer = new OpenLayers.Layer.GeoRSS2('Markers',
                        georssURL);
    this.cvtr = new converter(cvtrtype);
    this.addFeatureURL=addFeatureURL;
    this.map=map;
    this.mode = 0;
    this.lastPos = null;
    this.thisPos = null;
    this.dragging = false;
    this.curPopup = null;
    this.featuretypes = new Array();
    this.mapclickpos = null;
    this.dist = 0;
    this.distUnitsId = du;
    this.distTenthsId = dt;
    this.unitsId = u;
    this.ggLayer=null;
    this.walkRouteMode=0;
    this.curPopup = null;
    this.walkrouteMarkersLayer=null;
    this.annotations = new Array();

    // Adds a feature type to the select box for adding a new feature.
    // (corresponds to the featuretypetag in GeoRSS
    this.addFeatureType = function(t,d)
    {
        this.featuretypes[t] = d;
    }

    // Adds an icon for a given feature type
    // Also adds the feature (see function above)
    this.addIcon = function(featuretype,featureTypeDesc,url,size) 
    {
        this.osmmarkersLayer.addIcon(featuretype,url,size);
        this.addFeatureType(featuretype,featureTypeDesc);
    }



    // Add a feature
    // Brings up a popup box allowing the user to add a feature.
    this.addFeature = function()
    {

        if (!this.curPopup)
        {
        var html = " <h3>Please enter details:</h3> "+
        "<label for='ptype'>What is it?</label>  <br/>";

        html += "<select id='ptype' class='textbox'> ";
        for(var featuretype in this.featuretypes)
        {
            html += "<option value='"+featuretype+"'>"+
                        this.featuretypes[featuretype]+"</option>";
        }
        html += "</select> <br/> ";

        html +=
            "<label for='pdescription'>Description or comments</label>  <br/>"+
        "<textarea id='pdescription' class='textbox' ></textarea> <br/> ";


        html += "<input type='button' id='button1' value='Go!' "+
        "<input type='button' value='Cancel' id='button2' /> ";
        
        this.curPopup =   new OpenLayers.Popup('wrp'+i,
                this.map.getLonLatFromViewPortPx(new OpenLayers.Pixel(50,50)),
                new OpenLayers.Size(480,360), html);
        this.curPopup.setBackgroundColor('#ffffc0');
        this.map.addPopup(this.curPopup);
        $('pdescription').style.width="80%";
        $('pdescription').style.height="50%";
        document.getElementById('button1').onclick = this.descSend;
        document.getElementById('button2').onclick = this.removePopup;
        }
    }

    // Send a new feature to the server.
    this.doSend = function (description,type,lat,lon,extra)
    {
        var qstring = "action=add&description="+description+
            "&type="+type+ "&lat="+lat+"&lon="+lon;
        OpenLayers.loadURL(self.addFeatureURL+"?"+qstring, null,
            self,self.addCallback); 
    }


        
    // Callback which runs when a feature has been successfully added.
    this.addCallback = function(xmlHTTP, addData)
    {
        alert('added. '+xmlHTTP.responseText);

        // Request markers in current area - this should make the new one appear
        var newBounds = self.cvtr.customToNormBounds ( self.map.getExtent() );
        self.osmmarkersLayer.load(newBounds);
    }

    this.deleteCallback = function(xmlHTTP,addData)
    {
		if(xmlHTTP.status==200)
        	alert('Delete successful.');
		else
			alert('Could not delete: error code=' + xmlHTTP.status);
    }

    // remove the 'add feature' popup
    this.removePopup = function()
    {
        self.map.removePopup(self.curPopup);
        self.curPopup = null;
    }

    // Reads what the user entered in the popup box for adding a new feature
    // (see this.addFeature above) and calls doSend(), above 
    this.descSend = function()
    {
        //this.removePopup('inputbox');
        var markerIcon = self.osmmarkersLayer.getIcon
            (document.getElementById('ptype').value);

        self.mapclickpos.y  -= markerIcon.size.h/2;

        lonLat = map.getLonLatFromViewPortPx(self.mapclickpos) ;

        var priv = (document.getElementById('visibility')) ? 
                    document.getElementById('visibility').value : 0;
        var normLL = self.cvtr.customToNorm(lonLat);
        
        self.doSend(document.getElementById('pdescription').value,
                    document.getElementById('ptype').value,
                    normLL.lat,normLL.lon,"private="+priv);    

        self.removePopup();
    }

    // For handling mouse up events
    // If mode is 0 (normal mode), load in new markers for the visible area
    this.mouseUpHandler = function(e)
    {
        self.dragging=false;
        var bounds = self.map.getExtent();
        var bl = self.cvtr.customToNorm
                (new OpenLayers.LonLat(bounds.left,bounds.bottom));
        var tr = self.cvtr.customToNorm
                (new OpenLayers.LonLat(bounds.right,bounds.top));

        /* GGKML */
        if(self.mode==0)
        {
            var newBounds = new OpenLayers.Bounds(bl.lon,bl.lat,tr.lon,tr.lat);

            if(self.ggLayer)
            {
                self.ggLayer.load(newBounds);
            }

            if(self.walkrouteMarkersLayer)
            {
                self.walkrouteMarkersLayer.load(newBounds);
            }
            var bounds = self.map.getExtent();

            var bl = self.cvtr.customToNorm
                (new OpenLayers.LonLat(bounds.left,bounds.bottom));
            var tr = self.cvtr.customToNorm
                (new OpenLayers.LonLat(bounds.right,bounds.top));
            var newBounds = new OpenLayers.Bounds(bl.lon,bl.lat,tr.lon,tr.lat);
            self.osmmarkersLayer.load(newBounds);
        }

        self.updateLinks((bl.lon+tr.lon)/2,(bl.lat+tr.lat)/2);

        //self.map.controls[0].defaultMouseUp(e);
        md.defaultMouseUp(e);

        if(e.preventDefault)
            e.preventDefault();
        document.onselectstart = null; 
        return false;
    }

    this.mouseMoveHandler = function(e)
    {
        if (self.dragging && self.mode==3)
        {
            
            thisPos=
                    map.getLonLatFromViewPortPx(map.events.getMousePosition(e));
            if(self.lastPos)
                {
                    var lastPosLL = self.cvtr.customToNorm(self.lastPos);
                    var thisPosLL = self.cvtr.customToNorm(thisPos);
                    self.distance(lastPosLL,thisPosLL);
                }
            self.lastPos = thisPos;
        }

        if(self.mode==0)
            //self.map.controls[0].defaultMouseMove(e);
            md.defaultMouseMove(e);

        if(e.preventDefault)
            e.preventDefault();
        return false;
    }

    this.mouseDownHandler = function(e)
    {
        self.dragging=true;

        //self.map.controls[0].defaultMouseDown(e);
        md.defaultMouseDown(e);
        if(e.preventDefault)
            e.preventDefault();
        return false;
    }

    // Set the mode
    // note that:
    // 0 = normal mode (pan)
    // 1 = add annotations mode
    // 2 = delete annotations mode
    this.setMode = function(m)
    {
        var lastMode = self.mode;
        self.mode=m;

        if(m==4)
        {
            // deactivate map drag - done anyway if mode != 0
            // activate walk route draw tool
            self.drawWRControl.activate();
        }
        else
        {
            $('wrdonebtn').style.visibility= 'hidden';
            this.drawWRControl.deactivate();
        }
        self.walkRouteMode = 0;
        self.curWRID =0;
        lastPos = thisPos = null;
    }

    this.getEventElement = function(e)
    {
        if(!e) e=window.event;
        if(e.srcElement)
            return e.srcElement;
        return e.target;
    }

    // run from the GeoRSS2 layer to convert normal latitudes/longitudes
    // (from the GeoRSS) into the coordinate system used in the map
    this.converterProxy = function(lonlat)
    {
        var a= self.cvtr.normToCustom (lonlat);
        return a;
    }


    // Delete a marker
    // Set up from the GeoRSS2 layer
    this.deleteMarker = function(id)
    {
        //alert('deleting marker: ' + id);    
        OpenLayers.loadURL (self.addFeatureURL +"?action=delete&id="+id,
                    null,self,self.deleteCallback);
    }

    // Set the default icon
    this.setDefaultIcon = function (url,size) 
    {
        this.osmmarkersLayer.setDefaultIcon(url,size);
    }

    this.getDefaultIcon = function()
    {
        return this.osmmarkersLayer.defaultIcon;
    }

	this.isDeleteMode = function()
	{
		return self.mode==2;
	}

    this.osmmarkersLayer.setConverterFunction(this.converterProxy);
    this.osmmarkersLayer.setDeleteTestFunction(this.isDeleteMode);
    this.osmmarkersLayer.setDeleteFunction(this.deleteMarker);



    var initLL = this.cvtr.customToNorm(new OpenLayers.LonLat
            (initEasting,initNorthing));

    if(initZoom)
    {
        this.map.setCenter(this.cvtr.normToCustom
        (new OpenLayers.LonLat(initLL.lon,initLL.lat)), initZoom);
    }
    else
    {
        this.map.setCenter(this.cvtr.normToCustom
        (new OpenLayers.LonLat(initLL.lon,initLL.lat)));

    }

    this.osmmarkersLayer.load(this.cvtr.customToNormBounds(map.getExtent()));

    /////////////////////////////FREEMAPCLIENT//////////////////////////////

    // SETUP VECTOR LAYER START

    this.resetInfoPanel = function()
    {
        var html = "<p><em>Freemap</em> is a project to create free and " +
                   "annotatable maps of the countryside using " +
                   "<a href='http://www.openstreetmap.org'>OpenStreetMap</a>" +
                   " data together with other freely-available data such as " +
                   "NASA SRTM contours and "+
                   "<a href='http://www.geograph.org.uk'>Geograph</a> photos."+
                   "</p> "+
                   "<p>With <em>Freemap</em> "+
                   "you can create and share walking routes and "
                   +"information about path blockages and interesting places "+
                   "you visit.</p>" +
                   "<p><a href='/wordpress'>Development blog</a> <br/>"+
				   "<a href='/downloads/freemap.tar.bz2'>Download Freemap "+
				   "source</a></p>";
        $('infopanel').innerHTML = html;
    }

    this.resetInfoPanel();
    this.newWalkroutePoints = null;

    this.walkrouteLayer = new OpenLayers.Layer.Walkroute
        ("Walk Route Layer",this.cvtr);


    this.drawWRControl = new OpenLayers.Control.DrawWalkroute
                        (this.walkrouteLayer,OpenLayers.Handler.Path);
    map.addControl(this.drawWRControl);


    this.uploadNewWalkroute = function(p)
    {
        self.newWalkroutePoints = p;

        var html = 
        "<h3>Please enter details of the walk route</h3>" +
        "<label for='wrtitle'>Title</label>  <br/>" +
        "<input id='wrtitle' class='textbox' /> <br/> "+
        "<label for='wrdescription'>Description</label>  <br/>"+
        "<textarea id='wrdescription' class='textbox'></textarea><br/>" +
        "Who can see this route?" +
        "<input type='radio' name='wrvisibility' id='wrvisibility_public' "+
        "value='public' "+
        " checked='checked'/>" +
        "Everyone "+
        "<input type='radio' name='wrvisibility' id='wrvisibility_private' "+
        "value='private' />" +
        "Just me<br/>";

        self.curPopup = Dialog(html,self.doUploadNewWalkroute,
                                    self.removePopup, self.map);
    }

    // STUFF FROM WalkRouteLayer
    this.doUploadNewWalkroute= function()
    {
        var url=
            'http://www.free-map.org.uk/freemap/common/walkroute.php?'
            + 'action=add&userid=0';
            

        // points is an array of OpenLayers.Geometry.Point (x,y)
        var lats="", lons="";
        var first=true;
        var p = self.newWalkroutePoints;
        for (var count=0; count<p.length; count++)
        {
            if (!first)
            {
                lats += ",";
                lons += ",";
            }
            else
            {
                first=false;
            }
            var gr = new OpenLayers.LonLat(p[count].x,
                    p[count].y);
            var ll = self.cvtr.customToNorm(gr);

            lons += ll.lon;
            lats += ll.lat; 
        }
        url += "&title=" + $('wrtitle').value;
        url += "&description=" + $('wrdescription').value;
        var visibility = ($('wrvisibility_public').checked) ?
            'public' : 'private';
        url += '&visibility=' + visibility;
        url += "&lats=" + lats + "&lons=" + lons;
        OpenLayers.loadURL(url,null,self,self.walkrouteUploaded);
    }

    this.walkrouteUploaded = function(xmlHTTP)
    {
        if(xmlHTTP.responseText=='0')
        {
            alert("You tried to upload a private route when you "+
                  "weren't logged in! Route not uploaded.");
            self.WRDone();
        }
        else
        {
            alert('walk route added successfully. ID=' + xmlHTTP.responseText);
            self.walkrouteLayer.removeRenderedRoute();
            self.walkrouteLayer.drawRoute(self.newWalkroutePoints);
            self.removePopup();
            self.walkRouteMode = 1;
            $('wrdonebtn').style.visibility = 'visible';
            $('wrdonebtn').onclick = self.WRDone;
            self.curWRID = xmlHTTP.responseText;
            //self.drawControls.line.deactivate();
            self.drawWRControl.deactivate();
        }
    }
    // STUFF FROM walkroutelayer - end

    //this.drawControls['line'].setCallback(this.uploadNewWalkroute);
    this.drawWRControl.setCallback(this.uploadNewWalkroute);

    map.addLayer(this.walkrouteLayer);
    this.map.addLayer ( this.osmmarkersLayer );

    // SETUP VECTOR LAYER END

    


    this.selectUp = function() { alert('selectUp'); }
    this.selectOver  = function() { alert('selectOver'); }

    this.WRDescSend = function()
    {
        if(self.curWRID>0)
        {
            var title=$('wrtitle').value;
            var description=$('wrdescription').value;

            var url = 
            "http://www.free-map.org.uk/freemap/common/walkroute.php?"+
            "action=edit&id=" + self.curWRID + "&description="+description+
            "&title=" + title;
            OpenLayers.loadURL(url,null,self,self.descriptionAdded);
        }
    }

    this.ASend = function()
    {
        var an=$('annotation').value;


        self.mapclickpos.y  -= 16; 
        self.mapclickpos.x  += 16; 

        var lonLat = map.getLonLatFromViewPortPx(self.mapclickpos) ;

        var priv = (document.getElementById('visibility')) ? 
                            document.getElementById('visibility').value : 0;

        var normLL = self.cvtr.customToNorm(lonLat);

        var url = 
            "http://www.free-map.org.uk/freemap/common/walkroute.php?"+
            "action=annotate&id=" + self.curWRID + "&annotation="+an+
            "&lon="+normLL.lon + "&lat=" + normLL.lat;

        OpenLayers.loadURL(url,null,self,self.annotationAdded);
    }

    this.removePopup = function()
    {
        self.map.removePopup(self.curPopup);
        self.curPopup = null;
    }

    this.annotationAdded = function()
    {
        alert('annotation added.');
        self.removePopup();

        var icon = 
            self.walkrouteMarkersLayer.getAnnotationIcon
                (self.annotations.length+1);

        var a = self.mapclickpos;
        var b = self.map.getLonLatFromViewPortPx(a);

        var marker = new OpenLayers.Marker(b,icon); 
        if(self.walkrouteMarkersLayer)
        {
            self.walkrouteMarkersLayer.addMarker(marker);
            self.annotations.push(marker);
        }
    }

    this.showWRDescription = function()
    {
        if(self.walkRouteMode!=1 || self.curPopup!=null)
            return;

        var html = 
        "<label for='wrtitle'>Title</label>  <br/>" +
        "<input id='wrtitle' class='textbox' /> <br/> "+
        "<label for='wrdescription'>Description</label>  <br/>"+
        "<textarea id='wrdescription' class='textbox'></textarea><br/>" ;

        self.curPopup = Dialog(html,self.WRDescSend,self.removePopup,
                                        self.map);
    }

    this.WRDone = function()
    {
        self.walkRouteMode = 0;    
        self.curWRID=0;
        $('wrdonebtn').style.visibility = 'hidden';
        self.walkrouteLayer.removeRenderedRoute();
        while(self.annotations.length>0)
        {
            self.walkrouteMarkersLayer.removeMarker
                (self.annotations[self.annotations.length-1]);
            self.annotations.pop();
        }
        //self.drawControls.line.activate();
        self.drawWRControl.activate();
    }

    this.addAnnotation = function()
    {
        if(self.walkRouteMode!=1 )
            return;

        var html = 
        "<h3>Add walk route annotation</h3>" +
        "<label for='annotation'>Description</label>  <br/>"+
        "<textarea id='annotation' class='textbox'></textarea><br/>";

        self.curPopup =   Dialog(html,self.ASend,self.removePopup,
                                        self.map);
    }

    if(ggURL!=null)
    {
        this.ggLayer = new OpenLayers.Layer.GGKML('Geograph Photo Markers',
                        ggURL);
        this.ggLayer.setConverterFunction(this.converterProxy);
        this.ggLayer.setDefaultIcon
        ('http://www.free-map.org.uk/images/cam.png',
         new OpenLayers.Size(16,16) );
        map.addLayer ( this.ggLayer );
    }

    // Walkroute markers layer
    this.walkrouteMarkersLayer = new OpenLayers.Layer.WalkRouteMarkers
        ('Walk Route Markers',
        'http://www.free-map.org.uk/freemap/common/walkroute.php');
    this.walkrouteMarkersLayer.setConverterFunction(this.converterProxy);
    this.walkrouteMarkersLayer.setDefaultIcon
        ('http://www.free-map.org.uk/images/foot.png',
         new OpenLayers.Size(16,16) );
    this.walkrouteMarkersLayer.addIcon('walkroute_annotation',
        'http://www.free-map.org.uk/images/marker.png',
        new OpenLayers.Size(24,24));
    this.walkrouteManager = new WalkRouteDownloadManager(
                this.walkrouteLayer,this.walkrouteMarkersLayer,$('infopanel'),
                this.resetInfoPanel);
    this.walkrouteMarkersLayer.setManager(this.walkrouteManager);
    map.addLayer(this.walkrouteMarkersLayer);

    



    this.descriptionAdded = function(xmlHTTP)
    {
        alert('description added');
        self.removePopup();
    }



    this.displayDistance = function(dist)
    {
        var intDist=Math.floor(dist%1000), decPt=Math.floor(10*(dist-intDist)), 
        displayedIntDist = (intDist<10) ? "00" : ((intDist<100) ? "0" : ""),
        unitsElem = document.getElementById(this.distUnitsId),
        distTenthsElem = document.getElementById(this.distTenthsId);

        displayedIntDist += intDist;

        unitsElem.replaceChild ( document.createTextNode(displayedIntDist),
                                 unitsElem.childNodes[0] );

        distTenthsElem.replaceChild ( document.createTextNode(decPt),
                                  distTenthsElem.childNodes[0] );
    }

    this.calcDistance = function(pos1,pos2)
    {
        var d =  OpenLayers.Util.distVincenty (pos1,pos2);
        return d;
    }

    this.distance = function(p1,p2)
    {
        var miles = (document.getElementById(this.unitsId).value=="miles");
        this.dist += (this.calcDistance(p1,p2) * (miles ? 0.6214 : 1));
        this.displayDistance(this.dist);
    }


    this.resetDistance = function()
    {
        self.lastPos = null;
        self.dist = 0;
        self.displayDistance(0);
    }


    this.changeUnits = function()
    {
        var miles = (document.getElementById(self.unitsId).value=="miles");
        var factor = (miles) ?  0.6214: 1.6093;
        self.dist *=factor;
        self.displayDistance(self.dist);
    }

    this.updateLinks = function(lon,lat)
    {
        /*
        document.getElementById('mapnikLink').href =
            '/freemap/index.php?mode=mapnik&lat='+lat+'&lon='+lon;
        document.getElementById('npeLink').href =
            '/freemap/index.php?mode=npe&lat='+lat+'&lon='+lon;
        document.getElementById('poiEditorLink').href =
            '/freemap/edit.php?lat='+lat+'&lon='+lon;
        */
    }



    this.mapClick = function(e)
    {
		self.mapclickpos  = map.events.getMousePosition(e);
        if(self.mode==4 && self.walkRouteMode==1)
        {
            // add walk route annotation
            self.mapclickpos = 
                self.map.events.getMousePosition(e);
            self.addAnnotation();
            return false;
        }
        else
        {
        	switch(self.mode)
        	{
            	case 1:
                	self.addFeature();
                	break;
        	}
		}
        if(e.preventDefault)
            e.preventDefault();
        return false;
            return self.mapClick(e);
    }

    this.setupEvents = function()
    {
        this.map.events.register('click',this.map,this.mapClick );

        this.map.events.remove('mousemove');
        this.map.events.remove('mouseup');
        this.map.events.remove('mousedown');

        this.map.events.register('mousedown',this.map,
                this.mouseDownHandler );
        this.map.events.register('mouseup',this.map,
            this.mouseUpHandler );
        this.map.events.register('mousemove',this.map,
            this.mouseMoveHandler );
    }

    this.searchCallback = function(xmlHTTP, addData)
    {
        var latlon = xmlHTTP.responseText.split(",");
        if(latlon[0]!="0" && latlon[1]!="0")
        {
            var normLL = new OpenLayers.LonLat
                (parseFloat(latlon[1]),parseFloat(latlon[0]));
            self.updateLinks(normLL.lon,normLL.lat);
            var prjLL = self.cvtr.normToCustom(normLL);
            map.setCenter(prjLL, self.map.getZoom() );
        }
        else
        {
            alert("That place is not in the database");
        }
    }

    this.placeSearch = function()
    {
        var loc = document.getElementById('search').value;
        OpenLayers.loadURL
            ("http://www.free-map.org.uk/freemap/common/geocoder_ajax.php?" +
            "place="+loc+"&country=uk", null, self,self.searchCallback);
    }


    document.getElementById(rd).onclick = this.resetDistance;
    document.getElementById(u).onchange = this.changeUnits;
    document.getElementById('searchButton').onclick = placeSearch;
}
