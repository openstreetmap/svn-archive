var Freemap3D = Class.create ({

    initialize: function(x,y,tileZoomLevel)
    {
        this.origx=x;
        this.origz=y;
        this.valid=false;

        // You can't do 2D drawing with a 3D canvas. So we have to layer 2
        // canvases on top of each other.
        this.ctx = $('canvas2').getContext('2d');
        this.ctx.fillStyle = 'white';
        this.ctx.font = '12pt Helvetica';

        this.wgl = WebGLInitialiser.init('canvas1','shader-vs','shader-fs',
                    'wayshader-vs','wayshader-fs'); 
        if(this.wgl!=null)
        {
            this.hExag = 3.0;
            this.osmData = new Object();
            this.srtm = new Object();
            this.tileZoomLevel=tileZoomLevel;
            this.twoToPowerZoom=Math.pow(2,this.tileZoomLevel);
            Freemap3D.baseURL = 'http://www.free-map.org.uk';
            this.camera = new Camera(x, 0.12*this.hExag, y, 90.0);
            this.curXTile = this.metresToTile(x*1000);
            this.curYTile = this.metresToTile(y*1000);
            this.requestReload('osm',this.curXTile,this.curYTile);

            // fetch a load of srtm data to start...
            this.requestReload('srtm',this.curXTile,this.curYTile);

            this.requestReload('srtm',this.curXTile,this.curYTile-1); //N
            this.requestReload('srtm',this.curXTile+1,this.curYTile-1); //NE
            this.requestReload('srtm',this.curXTile+1,this.curYTile); //E

            /*
            this.requestReload('srtm',this.curXTile+1,this.curYTile+1); //SE
            this.requestReload('srtm',this.curXTile,this.curYTile+1); // S
            this.requestReload('srtm',this.curXTile-1,this.curYTile+1); //SW

            this.requestReload('srtm',this.curXTile-1,this.curYTile); //W
            this.requestReload('srtm',this.curXTile-1,this.curYTile-1); //NW
            */

            this.valid=true;
            this.doSRTM=true;
            this.keysPressed = new Object();
            this.wgl.si.srtm.doCalculation(true);
        }
    },

    requestReload: function(layer,tileX,tileY) 
    {
        var format = (layer=='osm') ? 'json' : '';

        if((layer=='osm' && this.osmData[tileY] && this.osmData[tileY][tileX])||
           (layer=='srtm' && this.srtm[tileY] && this.srtm[tileY][tileX]))
        {
            return ;
        }
        else
        {
            $('dbg').innerHTML ='Requesting ' + layer + ' for tile '
                + tileX+' ' +tileY;

			/* upgrading to apache2 messed up .htaccess - TODO find why
            var url = Freemap3D.baseURL + '/' + layer + '/' +
                        this.tileZoomLevel + '/' +
                        tileX + '/' + tileY + '/' + format;
			*/
			
            var url = (layer=='osm') ?
			
					Freemap3D.baseURL + '/freemap/ws/trsvr.php?x=' +
						tileX+ '&y=' + tileY + '&z=' + this.tileZoomLevel +
						'&poi=all&way=all&format=json' :
						
					Freemap3D.baseURL + '/freemap/ws/srtmsvr.php?x=' +
						tileX+ '&y=' + tileY + '&z=' + this.tileZoomLevel;


            // Note, we can supply arbitrary options "layer", "tileX", "tileY"
            // This is so the response can identify what tile it's associated
            // with.
            var rq=new Ajax.Request ( url,
                            { 
                                method: 'get' ,
                                layer:layer,
                                tileX:tileX,
                                tileY:tileY,
                                onSuccess: this.reloadReceived.bind(this),
                                onFailure: this.reloadFailure.bind(this),
                            } );
            if(layer=='osm')
                this.status('loading from URL: ' + url);
        }
    },

    reloadReceived: function(xmlHTTP)
    {
        if(xmlHTTP.request.options.layer=='osm')
        {
        var tileX = xmlHTTP.request.options.tileX;
        var tileY = xmlHTTP.request.options.tileY;
        $('dbg').innerHTML = 'Got OSM data for tile ' + tileX+ ' ' + tileY;

        if(! this.osmData[tileY])
            this.osmData[tileY] = new Object();
        if (! this.osmData[tileY][tileX])
            this.osmData[tileY][tileX] = new OSMData();

        this.osmData[tileY][tileX].json=xmlHTTP.responseText.evalJSON();
        this.status('Response received OK; got ' +
            this.osmData[tileY][tileX].json.poi.length + 
            ' POIs and '+this.osmData[tileY][tileX].json.ways.length+' ways.');

        // add the ways to the way collection
        var str="";
        for(var count=0; count<this.osmData[tileY][tileX].json.ways.length; 
            count++)
        {
            // only add highways for the moment
            // WARNING!!! assumes srtm returns first, though will fail nicely
            // if not - the heights just won't be loaded in
            if(this.osmData[tileY][tileX].json.ways[count].tags.highway)
            {
                
                try
                {
                this.osmData[tileY][tileX].
                    addWay(this.osmData[tileY][tileX].json.
                        ways[count],
                        (this.doSRTM==true ? this.srtm[tileY][tileX]:null));
                str+="Successfully added way : " +
                    this.osmData[tileY][tileX].json.ways[count].tags.osm_id +
                    "<br/>";
                }
                catch (e)
                {
                    alert('failed on way: ' +
                        this.osmData[tileY][tileX].json.ways[count].tags.osm_id+
                        ' error=' + e);
                }
            }
        }
        // has the SRTM data been loaded yet?
        // the height calculation will be done whenever both have been loaded
        if (this.srtm[tileY] && this.srtm[tileY][tileX])
        {
            //alert('srtm before osm... getting heights');
        }
        // add the nodes to the node collection
        for(var count=0; count<this.osmData[tileY][tileX].json.poi.length; 
                count++)
        {
            // only add named pois for the moment
            if(this.osmData[tileY][tileX].json.poi[count].name)
            {
                this.osmData[tileY][tileX].addPOI
                    (this.osmData[tileY][tileX].json.poi[count],
                    (this.doSRTM==true?this.srtm[tileY][tileX]:null));
            }
        }
        str+=this.osmData[tileY][tileX].doAllWayBuffers(this.wgl.si.osm);
        //$('dbg').innerHTML = str;
        }
        else
        {
            try
            {
            var tileX = xmlHTTP.request.options.tileX;
            var tileY = xmlHTTP.request.options.tileY;
            $('dbg').innerHTML = 'Got SRTM data for tile ' + tileX+ ' ' + tileY;

            if(! this.srtm[tileY])
                this.srtm[tileY] = new Object();
            if (! this.srtm[tileY][tileX])
                this.srtm[tileY][tileX] = new Array();
            var srtmJSON = xmlHTTP.responseText.evalJSON();
            for(var count=0; count<srtmJSON.rects.length; count++)
            {
                this.srtm[tileY][tileX][count] = new SRTM(this.hExag);
                this.srtm[tileY][tileX][count].res = 1 / (srtmJSON.npts-1);
                this.srtm[tileY][tileX][count].load(srtmJSON.rects[count]);
                //$('dbg').innerHTML =
                    this.srtm[tileY][tileX][count].generateNormals();
                this.srtm[tileY][tileX][count].getBuffers(this.wgl.si.osm);
            }
            if (this.osmData[tileY] && this.osmData[tileY][tileX])
            {
                /*
                alert('osm before srtm... getting heights');
                this.osmData[tileY][tileX].getHeights
                    (this.srtm[tileY][tileX]);
                    */
            }
            }
            catch(e)
            {
                alert(e);
            }
        }

        //this.status('Added ' + count + ' ways to OSM data');
        this.draw();
    },

    reloadFailure: function(xmlHTTP)
    {
        this.status('Reload failure : http code=' + xmlHTTP.status);
    },

    keyDownEvent: function(e)
    {
        if(!this.valid) return;
        this.keysPressed[e.keyCode] = true;
        //$('dbg').innerHTML = 'keyDown: keycode = ' + e.keyCode;
    },

    keyUpEvent: function(e)
    {
        if(!this.valid) return;
        this.keysPressed[e.keyCode] = false;
        //$('dbg').innerHTML = 'keyUp: keycode = ' + e.keyCode;
    },

    move: function()
    {
        if(this.keysPressed[37])
        {
            this.camera.rotate(10.0);
                     this.draw();
                        this.displayPosition();
        }

        if(this.keysPressed[39])
        {
            this.camera.rotate(-10.0);
                     this.draw();
                    this.displayPosition();
        }

        if(this.keysPressed[38])
        {
             this.camera.forward(0.1);
                     this.draw();
                    this.displayPosition();
                     this.checkNewTile();
        }

        if(this.keysPressed[40])
        {
           this.camera.forward(-0.1);
                     this.draw();
                    this.displayPosition();
                     this.checkNewTile();
        }


        if(this.keysPressed[76])
        {
            this.camera.up (-0.01);
                     this.draw();
                    this.displayPosition();
                     this.checkNewTile();

        }

        if(this.keysPressed[80])
        {
             this.camera.up (0.01);
                     this.draw();
                        this.displayPosition();
                     this.checkNewTile();
        }
    },

    // Check whether we need to load a new tile.
    checkNewTile: function()
    {

        // Get current tile from current metres
        var xTile=this.metresToTile(this.camera.pos.x*1000),
            yTile=this.metresToTile(this.camera.pos.z*1000);


        // If this has changed...
        if(xTile!=this.curXTile || yTile!=this.curYTile)
        {
            this.curXTile=xTile;
            this.curYTile=yTile;
            this.requestReload('osm',xTile,yTile);
            this.requestReload('srtm',xTile,yTile);
        }
    },

    metresToTile: function(m)
    {
         return Math.floor
             ((this.twoToPowerZoom*(m+(Projection.halfW))) / Projection.W);
    },

    status:function(msg)
    {
        $('status').innerHTML=msg;
    },

    displayPosition: function()
    {
        this.status('x=' + this.camera.pos.x.toFixed(2) + 
        ' z=' + this.camera.pos.z.toFixed(2) + ' height='+
        (this.camera.pos.y*1000/this.hExag).toFixed(0)+'m'+
        '[tileX='+this.curXTile+' tileY='+this.curYTile+'] facing=' + 
        (90+(360-(this.camera.angle))) % 360 + " deg");
    },

    draw:function()
    {
        if(this.valid==true)
        {
            this.wgl.gl.clearColor(0.529,0.807,0.922,1.0); // web sky blue
            this.wgl.gl.clearDepth(1.0);
            this.wgl.gl.clear
                (this.wgl.gl.COLOR_BUFFER_BIT | this.wgl.gl.DEPTH_BUFFER_BIT);
            this.wgl.gl.enable(this.wgl.gl.DEPTH_TEST);

            this.wgl.gl.useProgram(this.wgl.si.srtm.shaderProgram);
            var pMtx = makePerspective(45,1.33,0.001,20);
            var mvMtx = this.camera.getLookatMatrix();
            var nMtx = mvMtx.inverse();
            nMtx = nMtx.transpose();
            this.wgl.si.srtm.doMatrices(pMtx,mvMtx,nMtx);


            this.clearCanvas();

            for(ytile in this.srtm)
            {
                for (xtile in this.srtm[ytile])
                {

                    if(this.doSRTM==true)
                    {
                        for(var i=0; i<this.srtm[ytile][xtile].length; i++)
                        {
                            this.srtm[ytile][xtile][i].render
                                (this.wgl.gl,this.wgl.si.srtm);
                        }
                    }
                }
            }

            this.wgl.gl.useProgram(this.wgl.si.osm.shaderProgram);
            this.wgl.si.osm.doMatrices(pMtx,mvMtx,null);
            for(ytile in this.osmData)
            {
                for(xtile in this.osmData[ytile])
                {
                        this.osmData[ytile][xtile].
                            render(this.wgl.gl,this.wgl.si.osm,
                            this.camera.pos.x,this.camera.pos.z);

                        this.osmData[ytile][xtile].drawPOIs
                            (this.ctx,mvMtx,pMtx);
                }
            }
        }
    },

    clearCanvas: function()
    {
        //this.ctx.fillStyle='rgba(135,207,236,0)';
        //this.ctx.fillRect(0,0,640,480);
        $('canvas2').width = $('canvas2').width;
        this.ctx.fillStyle='rgba(255,255,255,1)';
    },

    clearCanvasComplete: function()
    {
        this.ctx.fillStyle='rgba(135,207,236,1)';
        this.ctx.fillRect(0,0,640,480);
        this.ctx.fillStyle='rgba(255,255,255,1)';
    },

    locChange: function()
    {
        alert($F('lon') + ' ' + $F('lat'));
        this.setLonLat($F('lon'),$F('lat'));
    },

    setLonLat: function(lon,lat)
    {
        this.camera.pos.x = Projection.lonToGoogle(lon) / 1000.0;
        this.camera.pos.z = Projection.latToGoogle(lat) / 1000.0;
        this.checkNewTile();
        this.draw();
    }
});

                
function init()
{
    // looks like the coords have to be <=65535 ! So we use km, not m.
    var freemap3D = new Freemap3D(-80.5, -6630.05, 12);
    document.onkeydown = freemap3D.keyDownEvent.bind(freemap3D);
    document.onkeyup = freemap3D.keyUpEvent.bind(freemap3D);
    setInterval(freemap3D.move.bind(freemap3D), 30);
    $('locChange').onclick = freemap3D.locChange.bind(freemap3D);
    freemap3D.draw();
}
