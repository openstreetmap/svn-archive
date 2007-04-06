
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
						"' from='" + this.nodes[0] + "' to='" +
							this.nodes[1] + "'>";

		for ( k in this.tags) {
				xml += "<tag k='" + k + "' v='" + this.tags[k] + "' />";
		}
		xml += "</segment>";	
		return xml;
	}
}
