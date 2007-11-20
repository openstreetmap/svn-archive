function Osmajax(map)
{
    this.map=map;
    this.controls=null;
    this.drawControls=null;
    this.vectorLayer =null;
    this.selectedFeature = null;
    this.loginDlg = null;
    this.loggedIn=false;
    this.navbtn='navbtn';
    this.selbtn='selbtn';
    this.drwbtn='drwbtn';
    this.drwpointbtn='drwpointbtn';
    this.chngbtn='chngbtn';
    this.delbtn='delbtn';
    this.loginbtn='loginbtn';
    this.status='status';
    this.changeFeatureDialog=null;
    var self=this;

    this.statusMsg=function(msg)
    {
        $(self.status).innerHTML = msg;
    }

    this.vectorLayer = new OpenLayers.Layer.OSM("OSM Layer",this.statusMsg);

    this.md = new OpenLayers.Control.MouseDefaults();
    this.md.setMap(map);

    // NEW
    var point = new OpenLayers.Geometry.Point(easting,northing);
    var pointFeature = new OpenLayers.Feature.Vector(point);
    this.vectorLayer.addFeatures(pointFeature);
                
          

    this.selectUp = function (f)
    {
        if(f instanceof OpenLayers.Feature.OSM)
        {
            var item = f.osmitem;
            var msg;
            if(item.type != 'unknown')
                msg = 'You selected a ' + item.type;
            else
                msg = 'You selected an item of unknown type ';
            if(item.tags['name'])
                msg += ' ('+item.tags['name']+')';
            msg += ' with an ID of ' + item.osmid;
            self.statusMsg(msg);
            self.selectedFeature = f; 
        }
    }

    this.load = function()
    {
        if(!(self.controls.select.active))
        {
            var cvtr = new converter("Mercator");
            var bounds = cvtr.customToNormBounds(self.map.getExtent());
            self.statusMsg('Loading...');
            self.vectorLayer.load(bounds);
        }
    }

    this.mouseUpHandler = function(e)
    {
        self.load();
    }

    this.activate = function()
    {
        self.map.events.register('mouseup',map,self.mouseUpHandler);
        self.map.addLayer(self.vectorLayer);
        var cvtr = new converter("Mercator");
        var bounds = cvtr.customToNormBounds(self.map.getExtent());
        self.vectorLayer.load(bounds);
        $(self.navbtn).onclick = this.nav;
        $(self.selbtn).onclick = this.sel;
        $(self.drwbtn).onclick = this.drw;
        $(self.drwpointbtn).onclick = this.drwpoint;
        $(self.chngbtn).onclick =this.chng;
        $(self.delbtn).onclick =this.del;
        $(self.loginbtn).onclick = this.osmLogin;
    }

    this.logoutDone = function(xmlHTTP)
    {
        document.getElementById(self.drwbtn).disabled='disabled';
        document.getElementById(self.drwpointbtn).disabled='disabled';
        document.getElementById(self.chngbtn).disabled='disabled';
        document.getElementById(self.delbtn).disabled='disabled';
        document.getElementById(self.loginbtn).value='OSM Login';
        document.getElementById(self.loginbtn).onclick=self.osmLogin;
        self.loggedIn = false;
    }

    this.setControlIds = function (login,nav,sel,drw,drwpoint,chng,del,stat)
    {
        this.loginbtn=login;
        this.navbtn=nav;
        this.selbtn=sel;
        this.drwbtn=drw;
        this.drwpointbtn=drwpoint;
        this.chngbtn=chng;
        this.delbtn=del;
        this.status=stat;
    }

    this.osmLogout = function()
    {
        var url= 'http://www.free-map.org.uk/freemap/common/osmproxy2.php'+
                '?call=logout';
        OpenLayers.loadURL(url,null,self,self.logoutDone,self.errorHandler);
    }

    this.deactivate = function()
    {
        self.controls.select.deactivate();
        self.drawControls.line.deactivate();
        self.drawControls.point.deactivate();
        self.vectorLayer.destroyFeatures();
        self.map.removeLayer(self.vectorLayer);
        self.osmLogout();
    }
        

    this.setupControls = function()
    {
        this.controls = {
                select: new OpenLayers.Control.SelectFeature
                    (this.vectorLayer,{callbacks:{'up':this.selectUp}})
                        };
        for(var key in this.controls)
        {
            this.map.addControl(this.controls[key]);
        }
        this.controls.select.deactivate();

        this.drawControls = {
                line: new OpenLayers.Control.DrawOSMFeature
                    (this.vectorLayer,OpenLayers.Handler.Path),
                point: new OpenLayers.Control.DrawOSMFeature
                    (this.vectorLayer,OpenLayers.Handler.Point)
            };
        for(var key in this.drawControls)
        {
            this.map.addControl(this.drawControls[key]);
        }
    
        this.drawControls.line.deactivate();
        this.drawControls.point.deactivate();
    }

    this.setupControls();

    this.removeLoginDlg = function()
    {
        if(self.loginDlg)
        {
            self.map.removePopup(self.loginDlg);
            self.loginDlg = null;
        }
    }

    this.errorHandler = function(xmlHTTP)
    {
        if(xmlHTTP.status==401)
        {
            alert('Incorrect OSM login details');
        }
        else
        {
            alert('Error on OSM server, code=' + xmlHTTP.status);
        }
    }

    this.loginProvided = function(xmlHTTP)
    {
        self.loggedIn = true;
        document.getElementById(self.drwbtn).disabled=null;
        document.getElementById(self.drwpointbtn).disabled=null;
        document.getElementById(self.chngbtn).disabled=null;
        document.getElementById(self.delbtn).disabled=null;
        document.getElementById(self.loginbtn).value='OSM Logout';
        document.getElementById(self.loginbtn).onclick=self.osmLogout;
    }

    this.provideLogin = function()
    {
        var url= 'http://www.free-map.org.uk/freemap/common/osmproxy2.php'+
                '?call=login&osmusername='+$('osmusername').value+
                "&osmpassword="+
                $('osmpassword').value;
        self.removeLoginDlg();
        OpenLayers.loadURL(url,null,self,self.loginProvided,
                self.errorHandler);
    }

    this.osmLogin = function()
    {
        var html = "<h3>Please provide your OSM login details</h3>";
        html += "<p>Please note that your OSM username and password will be "+
                "stored on the Freemap server until you log out and will be "+
                "sent to OSM every time you make an edit. Please only "+
                "continue if you're happy with this.</p>";
        html += "Username <input id='osmusername'/><br/>"+
                    "Password <input id='osmpassword' type='password'/><br/>";
        self.loginDlg = Dialog(html,self.provideLogin,
                                self.removeLoginDlg,self.map);
    }

    this.nav=function()
    {
        self.controls.select.deactivate();
        self.drawControls.line.deactivate();
        self.drawControls.point.deactivate();
        document.getElementById(self.navbtn).disabled='disabled';
        document.getElementById(self.selbtn).disabled=null;
        if(self.loggedIn)
        {
            document.getElementById(self.drwbtn).disabled=null;
            document.getElementById(self.drwpointbtn).disabled=null;
        }
        self.selectedFeature=null;
    }
    this.sel=function()
    {
        self.controls.select.activate();
        self.drawControls.line.deactivate();
        self.drawControls.point.deactivate();
        document.getElementById(self.selbtn).disabled='disabled';
        document.getElementById(self.navbtn).disabled=null;
        if(self.loggedIn)
        {
            document.getElementById(self.drwbtn).disabled=null;
            document.getElementById(self.drwpointbtn).disabled=null;
        }
    }

    this.drw=function()
    {
        if(self.loggedIn)
        {
            self.drawControls.line.activate();
            self.drawControls.point.deactivate();
            self.controls.select.deactivate();
            document.getElementById(self.drwbtn).disabled='disabled';
            document.getElementById(self.drwpointbtn).disabled=null;
            document.getElementById(self.navbtn).disabled=null;
            document.getElementById(self.selbtn).disabled=null;
            self.selectedFeature=null;
        }
    }

    this.drwpoint = function()
    {
        if(self.loggedIn)
        {
            self.drawControls.line.deactivate();
            self.drawControls.point.activate();
            self.controls.select.deactivate();
            document.getElementById(self.drwpointbtn).disabled='disabled';
            document.getElementById(self.drwbtn).disabled=null;
            document.getElementById(self.navbtn).disabled=null;
            document.getElementById(self.selbtn).disabled=null;
            self.selectedFeature=null;
        }

    }


    this.removeChangeFeatureDialog = function()
    {
        self.map.removePopup(self.changeFeatureDialog);
        self.changeFeatureDialog = null;
    }

    this.chng=function()
    {
        if(self.loggedIn)
        {
            if(self.selectedFeature) 
            {
                //var newType = prompt('Please enter the new classification');
                self.changeFeatureDialog =
                    new ChangeFeatureDialog(self.selectedFeature,
                                        self.vectorLayer.routeTypes,
                                        self.uploadChanges,
                                        self.removeChangeFeatureDialog,
                                        self.map);
            }
            else
            {
                alert('no selected feature');
            }
        }
    }

    this.del = function()
    {
        if(self.loggedIn)
        {
            if(self.selectedFeature)
            {
                if(self.selectedFeature.osmitem instanceof OpenLayers.OSMWay)
                    self.vectorLayer.deleteWay    (self.selectedFeature);
                else
                    self.vectorLayer.deleteNode    (self.selectedFeature);
            }
            else
            {
                alert('no selected feature');
            }
        }
    }

    this.uploadChanges = function()
    {
        if(self.selectedFeature)
        {

            var apicall = 
                (self.selectedFeature.osmitem instanceof OpenLayers.OSMNode) ?
                    'node' : 'way';

            var URL = 
                     'http://www.free-map.org.uk/freemap/common/osmproxy2.php'
                            + '?call='+apicall+'&id=' + 
                            self.selectedFeature.osmitem.osmid;
        
            alert('uploadChanges(): url=' + URL + 
                        ' XML=' + self.selectedFeature.osmitem.toXML());

            self.selectedFeature.osmitem.upload
                                        ( URL, null, self.refreshStyle,
                                            self.selectedFeature);
            self.removeChangeFeatureDialog();
        }
        else
        {
            alert('no selected feature');
        }
    }

    this.refreshStyle=function(xmlHTTP,w)
    {
        alert('Changes uploaded to server. Response=' + 
                xmlHTTP.responseText);

        if(w.osmitem instanceof OpenLayers.OSMWay)
        {
            var t = w.osmitem.type;
            var colour = self.vectorLayer.routeTypes.getColour(t);
            var width = self.vectorLayer.routeTypes.getWidth(t);
            var style = { fillColor: colour, fillOpacity: 0.4,
                    strokeColor: colour, strokeOpacity: 1,
                    strokeWidth: width };
            w.style=style;
            w.originalStyle=style;
            self.vectorLayer.drawFeature(w,style);
        }
    }
    
    this.setCentre = function(latlon)
    {
        this.vectorLayer.destroy();
        this.vectorLayer=null;
        var cvtr=new converter("Mercator");
        var prjLL = cvtr.normToCustom(latlon);
        this.map.setCenter(prjLL, this.map.getZoom() );
        this.vectorLayer = new OpenLayers.Layer.OSM("OSM Layer",this.statusMsg);
        this.setupControls();
        this.map.addLayer(this.vectorLayer);
        this.load();
    }
}
