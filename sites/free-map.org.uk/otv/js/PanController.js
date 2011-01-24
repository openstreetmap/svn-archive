var PanController = Class.create ( {
    initialize: function(f,se)
    {

        this.pv=new CanvasPanoViewer({
                canvasId: 'pancanvas',
                statusElement: se, 
                hFovDst : 90,
                hFovSrc : 360,
                showStatus : 0,
                wSlice:10,
                bearing:parseInt(f.attributes.direction),
                imageUrl: '/panorama/'+f.fid
            } );

        this.x=f.geometry.x;
        this.y=f.geometry.y;
        document.onkeydown = this.keyDown.bind(this);
        var parm='action=getAdjacent&id='+f.fid;
        new Ajax.Request
            ('/panorama.php',
                { method:'get',
                    onComplete: this.processResponse.bind(this),
                   parameters: parm } 
            );
    },

    load: function (data)
    {
        var vpBearing=this.pv.getViewportBearing();
        this.x=data.x;
        this.y=data.y;
        this.pv.loadImage('/panorama/'+data.id+'?t='+new Date().getTime(),
			{bearing: parseInt(data.direction)});
        this.pv.setViewportBearing(vpBearing);
        new Ajax.Request
            ('/panorama.php',
                { method:'get',
                    onComplete: this.processResponse.bind(this),
                   parameters: 'action=getAdjacent&id='+data.id}
            );
    },

    processResponse: function(xmlHTTP)
    {
        this.pv.dirs.clear();
        this.adjacents=xmlHTTP.responseText.evalJSON();
        for(var i=0; i<this.adjacents.length; i++)
        {
            this.pv.dirs.push
                (getBearing(this.adjacents[i].x-this.x,
                            this.adjacents[i].y-this.y));
        }
    },

    keyDown: function(e)
    {
        if(!e) e=window.event;
        if(e.keyCode==38)
        {
            var closestBearing = this.pv.getClosestBearing();
            if(closestBearing>=0)
            {
                alert('loading pano ' + this.adjacents[closestBearing].id);
                this.load(this.adjacents[closestBearing]);
            }
        }
    }

} );

function getBearing(dx,dy)
{
    var ang=(-rad2deg(Math.atan2(dy,dx))) + 90;
    return (ang<0 ? ang+360:ang);
}

function rad2deg(r)
{
    return r*(180.0/Math.PI);
}
