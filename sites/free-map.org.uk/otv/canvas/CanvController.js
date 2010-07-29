var CanvController = Class.create ( {
	initialize: function(se)
	{

		this.pv=new CanvasPanoViewer({
				canvasId: 'photocanvas',
				statusElement: se, 
				hFovDst : 90,
				hFovSrc : 360,
				showStatus : 0,
				wSlice:10 
			} );
		this.fids = new Array();
		this.bearings = new Array();
		this.status = se;
		document.onkeydown = this.keyDown.bind(this);
	},


	load: function(id)
	{
		this.fids = new Array();
		this.bearings = new Array();
		this.pv.loadImage('/otv/panorama/'+id,null);
		new Ajax.Request
			('/otv/route.php?action=getbearings&id=' + id,
				{ onComplete: this.processXML.bind(this) }
			);
	},

	processXML: function(xmlHTTP)
	{
		var xml=xmlHTTP.responseXML;
		var direction=parseInt(xml.getElementsByTagName("direction")[0].firstChild.nodeValue);
		var neighbours = xml.getElementsByTagName("neighbours")[0].
			getElementsByTagName("neighbour");
		for(var i=0; i<neighbours.length; i++)
		{
			this.fids.push
				(neighbours[i].getElementsByTagName("fid")[0].
				 firstChild.nodeValue);
			this.bearings.push
				(neighbours[i].getElementsByTagName("bearing")[0].
				 firstChild.nodeValue);
		}	
		this.pv.dirs=new Array();
		this.pv.panoOrientation=direction;
		var localdir;
		for(var i=0; i<this.bearings.length; i++)
		{
			localdir=this.bearings[i]-direction-180;
			this.pv.dirs[i] = Math.wrap(localdir,0,360);
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
				alert('loading pano ' + this.fids[closestBearing]);
				this.load(this.fids[closestBearing]);
			}
		}
	}

} );
