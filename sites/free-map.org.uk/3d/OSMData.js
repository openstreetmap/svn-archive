
var OSMData = Class.create ( {

    initialize: function()
    {
        this.ways=new Object();
        this.pois = new Object();
        this.json = null;
        this.wayProperties = {
           'motorway': { width:8, colours: [ 1.0, 1.0, 1.0, 1.0 ]},
            'trunk': { width:5, colours:[ 1.0, 1.0, 1.0, 1.0 ] },
            'primary': { width:5, colours:[ 1.0, 1.0, 1.0, 1.0 ] },
            'secondary': { width:4, colours: [1.0, 1.0, 1.0, 1.0 ] },
            'tertiary': {width:4,colours:[1.0, 1.0, 1.0, 1.0 ]},
            'unclassified': {width:3, colours:[1.0, 1.0, 1.0, 1.0 ]},
            'residential': { width:2,colours:[1.0, 1.0, 1.0, 1.0 ]},
            'service': { width:2,colours:[1.0, 1.0, 1.0, 1.0 ]},
            'path': { width:1,colours:[ 1.0, 1.0, 0.0, 1.0 ]},
            'footway': { width:1,colours:[ 1.0, 1.0, 0.0, 1.0 ]},
            'cycleway': { width:2,colours:[ 1.0, 1.0, 1.0, 1.0 ]},
            'bridleway' : { width:2,colours: [ 1.0, 0.75, 0.0, 1.0 ]},
            'byway' : { width:2,colours:[ 1.0, 0.0, 0.0, 1.0 ]},
            'track' : { width:2,colours:[ 1.0, 0.5, 0.0, 1.0] }
                            };
    },

    addPOI: function(poi,srtm)
    {
        this.pois[poi.osm_id] = new Object();
        for(tag in poi)
        {
            if(tag!='osm_id' && tag!='x' && tag!='y')
                this.pois[poi.osm_id][tag] = poi[tag];
        }
        var e= Projection.lonToGoogle(poi.x)/1000.0;
        var n= Projection.latToGoogle(poi.y)/1000.0;
        this.pois[poi.osm_id].x=e;
        this.pois[poi.osm_id].z=n;
        this.pois[poi.osm_id].y=0;
        if(srtm)
        {
        for(var srtmtile=0; srtmtile<srtm.length; srtmtile++)
        {
            var a = srtm[srtmtile].getHeight(poi.x,poi.y,e,n);
            this.pois[poi.osm_id].y = a[0] + 0.005*srtm[srtmtile].hExag;
        }
        }
    },

    addWay: function(way,srtm)
    {
        way.mpx=0.0; 
        way.mpz=0.0;
        var str="";
        way.vertices = new Array(); 
        for(var count=0; count<way.points.length; count++)
        {
            var e=Projection.lonToGoogle(way.points[count][0])/1000.0;
            var n=Projection.latToGoogle(way.points[count][1])/1000.0;

            if(srtm)
            {
            for(var srtmtile=0; srtmtile<srtm.length; srtmtile++)
            {
                var a= srtm[srtmtile].getHeight
                        (way.points[count][0],
                            way.points[count][1],
                            e,n);
                if(a[0]>=0)
                {
                    way.vertices.push(e);
                    way.vertices.push(a[0]+0.005*srtm[srtmtile].hExag);
                    way.vertices.push(n);
                    str+=a[1];
                    way.mpx+=e;
                    way.mpz+=n;
                }
            }
            }
            else
            {
                way.vertices.push(e);
                way.vertices.push(0);
                way.vertices.push(n);
                    way.mpx+=e;
                    way.mpz+=n;
            }
        }    
        //this.doWayBuffers (way,si);
        way.mpx /=(way.vertices.length/3);
        way.mpz /=(way.vertices.length/3);
        return str;
    },

    doAllWayBuffers: function(si)
    {
        var str="";
        for(var way=0;way<this.json.ways.length; way++)
            str+=this.doWayBuffers(this.json.ways[way],si);
        return str;
    },

    doWayBuffers: function(way,si)
    {
        var str="";

        // reject all non-highways
        if (!way.tags.highway)
            return;

        try
        {
            this.ways[way.tags.osm_id] = new Way(way,this.wayProperties,si);
            str+="Successfully did buffer for : "  + way.tags.osm_id + "<br/>";
        }
        catch (e)
        {
            str+="ERROR for way " + way.tags.osm_id+ " : " + e + "<br />";
        }
        return str;
    },

    getHeights: function (srtm)
    {
    },


    render: function(gl,si,e,n)
    {
        var camVec = $V([e,0,n]), d;

        for(var count in this.ways)
        {
			d=camVec.distanceFrom(this.ways[count].midpoint);
            if(d<=3.0)
            {
                this.ways[count].render(gl,si,d);
            }
        }
    },

    drawPOIs: function(ctx2d,mvMtx,pMtx)
    {
        var screenWidth = 640, screenHeight = 480;
        for(var id in this.pois)
        {
            if(this.pois[id].name && 
                !(this.pois[id].highway && this.pois[id].highway=='bus_stop'))
            {
                var eyecoords = mvMtx.x
                    ($V([this.pois[id].x,this.pois[id].y,this.pois[id].z,1.0]));
                var clip  = pMtx.x(eyecoords);
                var ndc = 
                    [ clip.elements[0] / clip.elements[3],
                     clip.elements[1] / clip.elements[3],
                     clip.elements[2] / clip.elements[3] ];
                var screenX = (ndc[0] + 1.0) * (screenWidth/2);
                var screenY = (1.0 - ndc[1]) * (screenHeight/2);

                if(clip.elements[2]>=0.0 && clip.elements[2]<=3.0)
                {
                    ctx2d.font = 12-(2*Math.round(clip.elements[2]))+
                        'pt Helvetica';
                    ctx2d.fillText(this.pois[id].name,
                        screenX,screenY);
                }
            }
        }
    }
} );
