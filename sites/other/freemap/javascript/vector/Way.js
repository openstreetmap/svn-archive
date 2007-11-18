
function Way(id)
{
	this.id=id;
	this.segs=new Array();
	this.tags=new Array();
	this.type="unknown";

	this.addSeg = function(id) {
		this.segs[this.segs.length] = id;
	}

	this.addTag = function(k,v) {
		this.tags[k] = v;
	}

	this.toXML = function()
	{
		var xml = '<osm version="0.4">';
		xml += '<way id="'+this.id+'" >';

		for (tag in tags)
			xml += '<tag k="'+tag+'" v="'+tags[tag]+'" />';

		for (seg in segs)
			xml += '<seg id="'+seg+'" />';

		xml += '</way>';
		xml += '</osm>';
		return xml;
	}	
}

function OSMData()
{
	this.nodes = new Array();
	this.segments = new Array();
	this.ways = new Array();

	this.addNode = function(n) {
		for(var count=0; count<this.nodes.length; count++)
		{
			if(this.nodes[count].id == n.id)
				return;
		}
		this.nodes[this.nodes.length] = n;
	}

	this.addSegment = function(s) {
		for(var count=0; count<this.segments.length; count++)
		{
			if(this.segments[count].id == s.id)
				return;
		}
		this.segments[this.segments.length] = s;
	}

	this.addWay = function(w) {
		for(var count=0; count<this.ways.length; count++)
		{
			if(this.ways[count].id == w.id)
				return;
		}
		this.ways[this.ways.length] = w;
	}

	this.getNodeById = function(id) {
		for(var count=0; count<this.nodes.length; count++)
		{
			if(this.nodes[count].id==id)
				return this.nodes[count];
		}
		return null;
	}

	this.getSegmentById = function(id) {
		for(var count=0; count<this.segments.length; count++)
		{
			if(this.segments[count].id==id)
				return this.segments[count];
		}
		return null;
	}

	this.getNearestNode = function(lon,lat,limit) {
		var nearestNode = null;
		var minDist = limit;
		//for(var count=0; count<this.nodes.length; count++)
		for(nodeId in this.nodes)
		{
			curDist = this.nodes[nodeId].dist(lat,lon);
			if(curDist < minDist)
			{
				minDist = curDist;
				nearestNode = this.nodes[nodeId];
			}
		}
		return nearestNode;
	}

	this.getSegment = function(node1,node2) {
		//for (var count=0; count<this.segments.length; count++)
		for(segmentId in this.segments)
		{
			if((this.segments[segmentId].from==node1 && 
				this.segments[segmentId].to==node2) ||
			   (this.segments[segmentId].from==node2 &&
			    this.segments[segmentId].to==node1) )
			{
				return this.segments[segmentId];
			}
		}
		return null;
	}
		
}
