var FreemapClient = OpenLayers.Class.create();

FreemapClient.prototype = 
{

    initialize: function(map,addFeatureURL,georssURL,
                initEasting,initNorthing,initZoom,initLoginStatus,
				cvtrtype,du,dt,u,rd)
    {
    this.map=map;
    this.cvtr = new converter(cvtrtype);
    this.cvtrtype=cvtrtype;
    this.setupMarkersLayer(georssURL);
    this.addFeatureURL=addFeatureURL;
    this.mode = 0;
    this.lastPos = null;
    this.thisPos = null;
    this.dragging = false;
    this.mouseDownPressed = false;
    this.curPopup = null;
    this.featuretypes = new Array();
    this.mapclickpos = null;
    this.dist = 0;
    this.distUnitsId = du;
    this.distTenthsId = dt;
    this.unitsId = u;
    this.ggLayer=null;
    this.curPopup = null;
    this.annotations = new Array();
    this.osm_id=0;
    this.osm_type='';
    this.descid='';
    this.wpid='';
    this.existingDescription="";
    this.doOSMLookupOnClick =false;
	this.curMarkerId=null;
	this.curMarkerDescription=null;
	this.isLoggedIn=initLoginStatus;
	this.toPhotos=false;

    this.icons = { 'pub' : 'pub.png', 
                   'car park' : 'carpark.png',
                   'viewpoint' : 'viewpoint.png',
                   'village' : 'place.png',
                   'hamlet' : 'place.png',
                   'town' : 'place.png',
                   'city' : 'place.png',
                   'suburb' : 'place.png',
                   'mast' : 'mast.png',
                   'hill' : 'peak.png',
                   'restaurant' : 'restaurant.png',
                   'farm' : 'farm.png' };

    var initLL = this.cvtr.customToNorm(new OpenLayers.LonLat
            (initEasting,initNorthing));

     var z = (initZoom) ? initZoom: 13;    

    this.defaultInfoHTML = 
                    "<div id='navigation'>"+
                    "<a href='/freemap/index.php'>Map</a> <br/>"+
                    "<a href='#' id='mapkey'>Map key</a> <br/>"+
                    "<a href='#' id='nearby'>Nearby points of interest</a> "+
                    "<br/>"+
                    "<a href='/freemap/about.php'>About/Contact</a> <br/>"+
                    "<a href='/freemap/userguide.php'>User guide</a> <br/>"+
                    "<a href='/freemap/developer.php'>Developer/Technical"+
                    "</a><br/>"+
                    "<a href='/freemap/stats/index.php'>Stats"+
                    "</a><br/>"+
                    "<a href='/wordpress'>Blog"+
                    "</a><br/>"+
                    "<a href='/freemap/other.php>Other stuff"+
                    "</a><br/>"+
                    "</div>";
                    "</div>";
    this.resetInfoPanel();

    this.osmmarkersLayer.setConverterFunction
        (this.converterProxy.bind(this));
    this.osmmarkersLayer.setDeleteTestFunction(this.isDeleteMode.bind(this));
    this.osmmarkersLayer.setDeleteFunction(this.deleteMarker.bind(this));
    this.osmmarkersLayer.setShowMarkerFunction(this.showMarker.bind(this));
    this.osmLookupActive=false;
    this.dialogColour = '#ffffff';



    this.map.setCenter(this.cvtr.normToCustom
        (new OpenLayers.LonLat(initLL.lon,initLL.lat)), z);
	this.updateLinks(initLL.lon,initLL.lat,z);

    this.osmmarkersLayer.load(this.cvtr.customToNormBounds(map.getExtent()));
    




    this.map.addLayer ( this.osmmarkersLayer );
    this.osmmarkersLayer.setVisibility(this.map.getZoom()>=14);

    // SETUP VECTOR LAYER END

    


    document.getElementById(rd).onclick = this.resetDistance.bind(this);
    document.getElementById(u).onchange = this.changeUnits.bind(this);
    document.getElementById('searchButton').onclick = 
                this.placeSearch.bind(this);
    },

    setupMarkersLayer: function(georssURL)
    {
        this.osmmarkersLayer = new OpenLayers.Layer.GeoRSS2('Markers',
                        georssURL);
    },

    setDialogColour : function(colour)
    {
        this.dialogColour = colour;
    },

    addGeographLayer: function(ggURL)
    {
        this.ggLayer = new OpenLayers.Layer.GGKML('Geograph Photo Markers',
                        ggURL);
        this.ggLayer.setConverterFunction(this.converterProxy.bind(this));
        this.ggLayer.setDefaultIcon
            ('http://www.free-map.org.uk/freemap/images/cam.png',
             new OpenLayers.Size(16,16) );
        this.map.addLayer ( this.ggLayer );
        this.ggLayer.setVisibility(this.map.getZoom()>=15);
    },

    // Adds a feature type to the select box for adding a new feature.
    // (corresponds to the featuretypetag in GeoRSS
    addFeatureType: function(t,d)
    {
        this.featuretypes[t] = d;
    },

    // Adds an icon for a given feature type
    // Also adds the feature (see function above)
    addIcon:function(featuretype,featureTypeDesc,url,size) 
    {
        this.osmmarkersLayer.addIcon(featuretype,url,size);
        this.addFeatureType(featuretype,featureTypeDesc);
    },



    // Add a feature
    // Brings up a popup box allowing the user to add a feature.
    addFeature : function()
    {
        var html = " <h3>Please enter details</h3> "+
        "<form><label for='ptype'>What is it?</label>  <br/>";

        html += "<select id='ptype' class='textbox'> ";
        for(var featuretype in this.featuretypes)
        {
            html += "<option value='"+featuretype+"'>"+
                        this.featuretypes[featuretype]+"</option>";
        }
        html += "</select> <br/> ";

		html += "<div id='pdesc'>";
        html +=
            "<label for='pdescription'>Description or comments</label>  <br/>"+
        "<textarea id='pdescription'"+
		"class='textbox' > </textarea><br/></form> ";

		html += "<input type='button' value='Next' id='pok'/>";
		html += "<input type='button' value='Cancel' id='pcancel'/>";
		html += "</div>";

		$('infopanel').innerHTML = html;
		$('pok').onclick = this.descSend.bind(this);
		$('pcancel').onclick = this.resetInfoPanel.bind(this);
		$('ptype').onchange=this.photoTest.bind(this);
    },

	photoTest: function()
	{
		if ($('ptype').value=='photo')
		{
		}
		else
		{
		}
	},

    // Send a new feature to the server.
    doSend : function (description,type,lat,lon,extra)
    {
        $('loading').style.visibility='visible';
        var qstring = "action=add&description="+description+
            "&type="+type+ "&lat="+lat+"&lon="+lon;
        OpenLayers.loadURL(this.addFeatureURL+"?"+qstring, null,
            this,this.addCallback); 
    },


        
    // Callback which runs when a feature has been successfully added.
    addCallback : function(xmlHTTP, addData)
    {
        $('loading').style.visibility='hidden';
        // Request markers in current area - this should make new one appear
        var newBounds = this.cvtr.customToNormBounds ( this.map.getExtent() );
        this.osmmarkersLayer.load(newBounds);
		this.curMarkerId=xmlHTTP.responseText;

		var html = '<p>Added successfully.</p>';
		html += "<input type='button' value='Continue' id='pyes'/>";
		html += "</p>";
		$('infopanel').innerHTML = html;
		$('pyes').onclick = this.resetInfoPanel.bind(this);
    },

    deleteCallback : function(xmlHTTP,addData)
    {
        $('loading').style.visibility='hidden';
        if(xmlHTTP.status==200)
            alert('Delete successful.');
        else
            alert('Could not delete: error code=' + xmlHTTP.status);
    },

    // remove the 'add feature' popup
    removePopup : function()
    {
        if(this.curPopup)
        {
            this.map.removePopup(this.curPopup);
            this.curPopup = null;
        }
    },

    // Reads what the user entered in the popup box for adding a new feature
    // (see this.addFeature above) and calls doSend(), above 
    descSend : function()
    {

        //this.removePopup('inputbox');
        var markerIcon = this.osmmarkersLayer.getIcon
            (document.getElementById('ptype').value);

        this.mapclickpos.y  -= markerIcon.size.h/2;

        lonLat = map.getLonLatFromViewPortPx(this.mapclickpos) ;

        var priv = (document.getElementById('visibility')) ? 
                    document.getElementById('visibility').value : 0;
        var normLL = this.cvtr.customToNorm(lonLat);
        
        this.doSend(document.getElementById('pdescription').value,
                    document.getElementById('ptype').value,
                    normLL.lat,normLL.lon,"private="+priv);    

        //this.removePopup();
		//this.resetInfoPanel();
    },

    // For handling mouse up events
    // If mode is 0 (normal mode), load in new markers for the visible area
    mouseUpHandler : function(e)
    {
        this.mouseDownPressed=false;
        if(!this.dragging && this.mode==0)
        {
            this.doOSMLookupOnClick =true;
        }
        else
        {
            this.doOSMLookupOnClick =false;
            var bounds = this.map.getExtent();
            var bl = this.cvtr.customToNorm
                (new OpenLayers.LonLat(bounds.left,bounds.bottom));
            var tr = this.cvtr.customToNorm
                (new OpenLayers.LonLat(bounds.right,bounds.top));


            /* GGKML */
            if(this.mode==0 && this.dragging)
            {
                var newBounds = 
                    new OpenLayers.Bounds(bl.lon,bl.lat,tr.lon,tr.lat);

                if(this.ggLayer)
                {
                    this.ggLayer.load(newBounds);
                }

                var bounds = this.map.getExtent();

                var bl = this.cvtr.customToNorm
                    (new OpenLayers.LonLat(bounds.left,bounds.bottom));
                var tr = this.cvtr.customToNorm
                    (new OpenLayers.LonLat(bounds.right,bounds.top));
                var newBounds = 
                    new OpenLayers.Bounds(bl.lon,bl.lat,tr.lon,tr.lat);
                this.osmmarkersLayer.load(newBounds);
            }
            this.updateLinks((bl.lon+tr.lon)/2,(bl.lat+tr.lat)/2,
								this.map.getZoom());
        }


        //this.map.controls[0].defaultMouseUp(e);
        md.defaultMouseUp(e);

        if(e.preventDefault)
            e.preventDefault();
        document.onselectstart = null; 
        this.dragging=false;
        return false;
    },

    mouseMoveHandler : function(e)
    {
        if(this.mouseDownPressed)
            this.dragging=true;

        if (this.dragging && this.mode==3)
        {
            
            thisPos=
                    map.getLonLatFromViewPortPx(map.events.getMousePosition(e));
            if(this.lastPos)
                {
                    var lastPosLL = this.cvtr.customToNorm(this.lastPos);
                    var thisPosLL = this.cvtr.customToNorm(thisPos);
                    this.distance(lastPosLL,thisPosLL);
                }
            this.lastPos = thisPos;
        }

        if(this.mode==0)
            //this.map.controls[0].defaultMouseMove(e);
            md.defaultMouseMove(e);

        if(e.preventDefault)
            e.preventDefault();
        return false;
    },

    mouseDownHandler : function(e)
    {
        this.mouseDownPressed=true;

        //this.map.controls[0].defaultMouseDown(e);
        md.defaultMouseDown(e);
        if(e.preventDefault)
            e.preventDefault();
        return false;
    },

    // Set the mode
    // note that:
    // 0 = normal mode (pan)
    // 1 = add annotations mode
    // 2 = delete annotations mode
    setMode : function(m)
    {
        var lastMode = this.mode;
        this.mode=m;

        if(false)
        {
            // deactivate map drag - done anyway if mode != 0
            // activate walk route draw tool
        }
        else
        {
            //$('wrdonebtn').style.visibility= 'hidden';
            this.resetInfoPanel();
        }
        lastPos = thisPos = null;
    },

    getEventElement : function(e)
    {
        if(!e) e=window.event;
        if(e.srcElement)
            return e.srcElement;
        return e.target;
    },

    // run from the GeoRSS2 layer to convert normal latitudes/longitudes
    // (from the GeoRSS) into the coordinate system used in the map
    converterProxy : function(lonlat)
    {
        var a= this.cvtr.normToCustom (lonlat);
        return a;
    },


    // Delete a marker
    // Set up from the GeoRSS2 layer
    deleteMarker : function(id)
    {
        //alert('deleting marker: ' + id);    
        $('loading').style.visibility='visible';
        OpenLayers.loadURL (this.addFeatureURL +"?action=delete&id="+id,
                    null,this,this.deleteCallback);
    },

	showMarker: function (id,description)
	{
		var html = "<p>"+description+"</p>";
		html += "<p>";
		html += "<a href='/freemap/api/markers.php?action=getById"
			+"&id="+id+"'>More...</a> ";
		html += "<a href='#' id='markeredit'>Edit</a> ";
		html += "<a href='#' id='markerclose'>Close</a>";
		var photoLink=false;
		if(photoLink==true)
		{
			html += " <a href='/freemap/api/markers.php?action=getPhoto&" +
					"id="+id+"'>Photo</a>";
		}
		html += "</p>";
		$('infopanel').innerHTML = html;
		$('markeredit').onclick=this.editMarker.bind(this);
		$('markerclose').onclick=this.resetInfoPanel.bind(this);
		this.curMarkerId = id;
		this.curMarkerDescription = description;
	},

	editMarker: function()
	{
		var html="<h3>Edit description</h3>"+
				"<textarea id='editmarker' class='textbox'>"+
				this.curMarkerDescription+"</textarea><br/>";
		html+="<input type='button' value='OK' id='emok'/>";
		html+="<input type='button' value='Cancel' id='emcancel'/>";
		$('infopanel').innerHTML = html;
		$('emok').onclick=this.editMarker2.bind(this);
		$('emcancel').onclick=this.resetInfoPanel.bind(this);
	},

	editMarker2: function()
	{
		var url=
			"http://www.free-map.org.uk/freemap/api/markers.php?"+
			"action=edit&id="+this.curMarkerId+"&description="+
			$('editmarker').value;
        OpenLayers.loadURL(url, null, this,this.editMarker3);
	},

	editMarker3: function(xmlHTTP)
	{
		alert('edited successfully.');
		this.resetInfoPanel();
	},

    // Set the default icon
    setDefaultIcon : function (url,size) 
    {
        this.osmmarkersLayer.setDefaultIcon(url,size);
    },

    getDefaultIcon : function()
    {
        return this.osmmarkersLayer.defaultIcon;
    },

    isDeleteMode : function()
    {
        return this.mode==2;
    },


    /////////////////////////////FREEMAPCLIENT//////////////////////////////

    // SETUP VECTOR LAYER START

    resetInfoPanel : function()
    {
        $('infopanel').innerHTML = this.defaultInfoHTML;
        $('mapkey').onclick=this.showKey.bind(this);
        $('nearby').onclick=this.getNearby.bind(this);
    },

    descriptionAdded : function(xmlHTTP)
    {
        $('loading').style.visibility='hidden';
        alert('description added');
        this.removePopup();
    },



    displayDistance : function(dist)
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
    },

    calcDistance : function(pos1,pos2)
    {
        var d =  OpenLayers.Util.distVincenty (pos1,pos2);
        return d;
    },

    distance : function(p1,p2)
    {
        var miles = (document.getElementById(this.unitsId).value=="miles");
        this.dist += (this.calcDistance(p1,p2) * (miles ? 0.6214 : 1));
        this.displayDistance(this.dist);
    },


    resetDistance : function()
    {
        this.lastPos = null;
        this.dist = 0;
        this.displayDistance(0);
    },


    changeUnits : function()
    {
        var miles = (document.getElementById(this.unitsId).value=="miles");
        var factor = (miles) ?  0.6214: 1.6093;
        this.dist *=factor;
        this.displayDistance(this.dist);
    },

    updateLinks : function(lon,lat,zoom)
    {
        document.cookie = 'lat='+lat;
        document.cookie = 'lon='+lon;
        document.cookie = 'zoom='+zoom;
		$('osmedit').href=
			'http://www.openstreetmap.org/edit.html?lat='+lat+
				'&lon='+lon+'&zoom='+zoom;
    },



    mapClick : function(e)
    {
        if(this.dragging)
            return;

        this.mapclickpos  = map.events.getMousePosition(e);
		if(true)
        {
            switch(this.mode)
            {
                case 1:
                    this.addFeature();
                    break;

                case 0:
                    if(this.doOSMLookupOnClick==true 
                        && this.osmLookupActive==true)
                    {
                        this.osmLookup();    
                        this.doOSMLookupOnClick=false;
                    }
                    break;
            }
        }
        if(e.preventDefault)
            e.preventDefault();
        return false;
    },

    setupEvents : function()
    {
        this.map.events.register('click',this.map,this.mapClick.bind(this) );

        this.map.events.remove('mousemove');
        this.map.events.remove('mouseup');
        this.map.events.remove('mousedown');

        this.map.events.register('mousedown',this.map,
                this.mouseDownHandler.bind(this) );
        this.map.events.register('mouseup',this.map,
            this.mouseUpHandler.bind(this) );
        this.map.events.register('mousemove',this.map,
            this.mouseMoveHandler.bind(this) );

        this.map.events.register('zoomend',this.map,
            this.zoomHandler.bind(this) );
    },

    zoomHandler: function()
    {
        document.cookie = 'zoom='+this.map.getZoom();
        if(this.map.getZoom()>=14)
        {
            this.osmmarkersLayer.setVisibility(true);
            this.ggLayer.setVisibility(this.map.getZoom()>=15);
        }
        else
        {
            this.osmmarkersLayer.setVisibility(false);
            this.ggLayer.setVisibility(false);
        }
        var center = this.cvtr.customToNorm(this.map.getCenter());
        this.updateLinks(center.lon,center.lat,this.map.getZoom());
    },
     
    searchCallback : function(xmlHTTP)
    {
        $('loading').style.visibility='hidden';
        var nodes = xmlHTTP.responseXML.getElementsByTagName('node');
        if(nodes.length>1)
        {
            this.listPOIs(nodes,"locate");
        }
        else if (nodes.length==1)
        {
            var x=nodes[0].getElementsByTagName("x")[0].firstChild.nodeValue;
            var y=nodes[0].getElementsByTagName("y")[0].firstChild.nodeValue;
            var lonLat=new OpenLayers.LonLat(x,y);
            if(this.cvtrtype!="Mercator")
            {
                // convert regular mercator (db format) to latlon 
                var sphmercCvtr = new converter("Mercator");
                lonLat=sphmercCvtr.customToNorm(lonLat);

                // convert latlon to Google Mercator 
                lonLat = this.cvtr.normToCustom(lonLat);
            }
			var z= (this.map.getZoom()>=13) ? this.map.getZoom():14;
            this.map.setCenter(lonLat,z);
			this.resetInfoPanel();
        }
        else
        {
            alert('Sorry, nothing matches your search!');
        }
    },

    setCentre : function(latlon)
    {
        var prjLL = this.cvtr.normToCustom(latlon);
        this.map.setCenter(prjLL, this.map.getZoom() );
    },

    // 15/03/08 this is now searching the OSM database, rather than 
    // using geonames.
    placeSearch : function()
    {
        $('loading').style.visibility='visible';
        var loc = document.getElementById('search').value;
        OpenLayers.loadURL
            ("http://www.free-map.org.uk/freemap/POInotes.php?" +
            "q="+loc+"&action=search", null, this,this.searchCallback);
    },


    osmLookup:function()
    {
        var lonLat = map.getLonLatFromViewPortPx(this.mapclickpos);

        if(this.cvtrtype!="Mercator")
        {
            // convert the map's coord scheme (e.g. Google spherical
            // Mercator) to latlon
            lonLat = this.cvtr.customToNorm(lonLat);

            // convert latlon to non-spherical Mercator
            var sphmercCvtr = new converter("Mercator");

            lonLat = sphmercCvtr.normToCustom(lonLat);
        }
        var action="get";
           url= 
        ('http://www.free-map.org.uk/freemap/POInotes.php?'+ 
            'action='+action+'&n=1&x='+lonLat.lon+'&y='+lonLat.lat);
        $('loading').style.visibility='visible';
        OpenLayers.loadURL(url,
            null,
            this,this.osmLookupCallback); 
    },


    osmLookupCallback : function(xmlHTTP)
    {
        var nodes = xmlHTTP.responseXML.getElementsByTagName('node');
        var ways = xmlHTTP.responseXML.getElementsByTagName('way');
        var features = (ways.length==1) ? ways : nodes;
        this.osm_type = (ways.length==1) ? 'way' : 'node';
        if(features.length==1)
        {
            var osm_id = features[0].getElementsByTagName('osm_id')[0].
                        firstChild.nodeValue;
            var name = 
                (xmlHTTP.responseXML.getElementsByTagName('name').length>0) ?
                    xmlHTTP.responseXML.getElementsByTagName('name')[0].
                        firstChild.nodeValue: "";


			var html="";

			// we're only interested in ways with a highway tag 
			// this is actually done server side but retained here in case
			// of implementation change
            if(this.osm_type=='way' && ways.length==1 &&
				ways[0].getElementsByTagName('highway').length>0)
            {
				var highLevelTypes = 
					{ 'footway' : 'Footpath',
					  'bridleway' : 'Bridleway' ,
					  'byway' : 'Byway',
					  'unclassified' : 'Minor road',
					  'tertiary' : 'Busy minor road',
					  'residential' : 'Residential road',
					  'service' : 'Service road',
					  'track' : 'Track',
					  'secondary' : 'B road',
					  'primary' : 'A road',
					  'trunk' : 'Major A road',
					  'motorway' : 'Motorway',
					  'unsurfaced' : 'Unsurfaced road',
					  'cycleway' : 'Cycle path' };
         		if(true)	
				{

				html += "<h3>"+ highLevelTypes
					[ways[0].getElementsByTagName('highway')[0].
						firstChild.nodeValue]+ "(ID " + osm_id + ")</h3>";
				}
            }

			if(true)
			{
            var description="";
            this.existingDescription="";
            var ad=false;
            if(xmlHTTP.responseXML.getElementsByTagName('description').length)
            {
                this.existingDescription=
                    xmlHTTP.responseXML.getElementsByTagName('description')[0].
                    firstChild.nodeValue;

                description += "<p>"+this.existingDescription+"</p>";
            }
            description+=
                "<a id='adddesc' href='#'>Add or alter description</a>";
            ad=true;
            var image = 
                (xmlHTTP.responseXML.getElementsByTagName('type').length) ?
                    this.icons
                        [xmlHTTP.responseXML.getElementsByTagName('type')[0].
                            firstChild.nodeValue] : null;

            html += (image ? "<img src='/freemap/images/"+image+"'/>" : "") +
            "<strong>" + name + "</strong></p><p>" +
                description + "</p>";

            this.osm_id = osm_id;
			}
			html += "<input type='button' value='OK' id='pok'/>";
		
			$('infopanel').innerHTML = html;
			$('pok').onclick = this.resetInfoPanel.bind(this);
            if($('adddesc'))
                $('adddesc').onclick = this.addOsmDescription.bind(this);
        }
        $('loading').style.visibility='hidden';
    },

    getNearby: function()
    {
        var lonLat = this.map.getCenter();

        if(this.cvtrtype!="Mercator")
        {
            // convert the map's coord scheme (e.g. Google spherical
            // Mercator) to latlon
            lonLat = this.cvtr.customToNorm(lonLat);

            // convert latlon to non-spherical Mercator
            var sphmercCvtr = new converter("Mercator");

            lonLat = sphmercCvtr.normToCustom(lonLat);
        }

        $('loading').style.visibility='visible';

        OpenLayers.loadURL(
            'http://www.free-map.org.uk/freemap/POInotes.php?'+
            'action=get&x='+lonLat.lon+'&y='+lonLat.lat+'&dist=5000',
            null,
            this,this.nearbyCallback); 
    },

    nearbyCallback: function(xmlHTTP)
    {
        var nodes = xmlHTTP.responseXML.getElementsByTagName('node');
        this.listPOIs(nodes,"article");
    },

    listPOIs: function(features,action)
    {
        this.osm_type = 'node';
        var html = "<div id='results'>";
        var link="";
        for(var count=0; count<features.length; count++)
        {
            var osm_id = features[count].getElementsByTagName('osm_id')[0].
                        firstChild.nodeValue;
            var name = 
                (features[count].getElementsByTagName('name').length>0) ?
                    features[count].getElementsByTagName('name')[0].
                        firstChild.nodeValue: "";


            if(name!="")
            {
                var image = 
                (features[count].getElementsByTagName('type').length) ?
                    this.icons
                        [features[count].getElementsByTagName('type')[0].
                            firstChild.nodeValue] : null;

                if(action=="article")
                {
                    link=
                        "<a href='/freemap/POI.php?osm_id="+ osm_id+"'>"+
                        name+"</a>";
                }
                else if (action=="locate")
                {
                    var x= features[count].getElementsByTagName
						("x")[0].firstChild.nodeValue;
                    var y= features[count].getElementsByTagName
						("y")[0].firstChild.nodeValue;

                    var lonLat=new OpenLayers.LonLat(x,y);
					var sphmercCvtr = new converter("Mercator");
					var zoom=(this.map.getZoom()>=13) ?
							this.map.getZoom() : 14;
					lonLat=sphmercCvtr.customToNorm(lonLat);
                    link= "<a href='/freemap/index.php?lat="+
						lonLat.lat+"&lon="+lonLat.lon+
						"&zoom="+zoom+"'>"+name+"</a>";
                }

                html +=
                    (image ? "<img src='/freemap/images/"+image+"'/>" : "") +
                    link + "<br/>";
            }
        }

        html += "<input type='button' value='close' id='close'/></div>";
        $('infopanel').innerHTML = html;
        $('close').onclick=this.resetInfoPanel.bind(this);

        $('loading').style.visibility='hidden';
    },

    addOsmDescription : function()
    {
        this.descid='desc'+(new Date().getTime());
        var html = "<h3>Add or update description</h3>"+
					"<em>(If pub etc, please don't make it " +
                    "slanderous!)</em>"+
                    "<p><textarea id='"+this.descid+
                    "' class='textbox'>"+this.existingDescription+
                    "</textarea></p>" ;
        html+="</p>";
		html += "<input type='button' value='OK' id='pok'/>";
		html += "<input type='button' value='Cancel' id='pcancel'/>";
		$('infopanel').innerHTML = html;
		$('pok').onclick = this.addOsmDescription2.bind(this);
		$('pcancel').onclick = this.resetInfoPanel.bind(this);
    },

    addOsmDescription2 : function(xmlHTTP)
    {
        var action = (this.existingDescription=="") ? "add" : "update";
        var url="http://www.free-map.org.uk/freemap/"+
                            "POInotes.php?action="+action+
                            "&osm_id="+this.osm_id+
                            "&description="+$(this.descid).value;

        url+="&type="+this.osm_type;
        OpenLayers.loadURL(url,
                            null,
                            this,this.resetInfoPanel);
    },

    activateOSMLookup: function()
    {
        this.osmLookupActive=true;
    },

    deactivateOSMLookup: function()
    {
        this.osmLookupActive=false;
    },

    showKey: function()
    {
        var html = 
          "<div id='key'><img src='/freemap/images/fmapkey.png'/>"+
          "<img src='/freemap/images/pub.png' alt='pub'/> Pub<br/>"+
          "<img src='/freemap/images/carpark.png' alt='car park'/>Car park"+
          "<br/>"+
          "<img src='/freemap/images/viewpoint.png' alt='viewpoint'/>Viewpoint"+
          "<br/>"+
          "<img src='/freemap/images/farm.png' alt='farm'/>Farm<br/>"+
          "<img src='/freemap/images/mast.png' alt='mast'/>Mast "+
          "(TV/radio/phone)<br/>"+
          "<img src='/freemap/images/hazardmarker.png' alt='path blockage'/>"+
          "Path blockage/hazard<br/> "+
          "<img src='/freemap/images/infomarker.png' alt='interesting place'/>"+
          "Interesting place<br/> "+
          "<img src='/freemap/images/querymarker.png' alt='path directions'/>"+
          "Path directions<br/>"+
          "<img src='/freemap/images/foot.png' alt='walk route'/>"+
          "Walk route<br/>"+
          "</div>";
        html += "<input type='button' value='close' id='close'/></div>";
        $('infopanel').innerHTML = html;
        $('close').onclick=this.resetInfoPanel.bind(this);
    }
};

