
function OSMSegment()
{
	this.nodes = new Array();
	this.id = null;
	this.tags = new Array();

	this.setNodes = function ( from, to) {
		this.nodes[0] = from;
		this.nodes[1] = to;
	}

	this.toXML = function() {
		var xml = "<segment id='" + (this.id>0 ? this.id : 0)  +
						"' from='" + this.nodes[0].nid + "' to='" +
							this.nodes[1].nid + "'>";

		for ( k in this.tags) {
				xml += "<tag k='" + k + "' v='" + this.tags[k] + "' />";
		}
		xml += "</segment>";	
		return xml;
	}

	this.addTag = function(k,v) {
		this.tags[k] = v;
	}

	this.upload = function(URL,receiver,callback,info) {
		var realCB = callback.bind(receiver);
		var data = "data=<osm version='0.3'>"+this.toXML()+"</osm>";
		alert('uploading the following XML: to ' + URL + ' ' + data);
		ajaxrequest(URL,'PUT',data,realCB,info);
	}
}
