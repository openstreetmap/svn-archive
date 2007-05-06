

OpenLayers.OSMSegment = OpenLayers.Class.create();

OpenLayers.OSMSegment.prototype = 
	OpenLayers.Class.inherit (OpenLayers.OSMItem, {

	nodes: null,
	way: null,
	
	initialize: function () {
		OpenLayers.OSMItem.prototype.initialize.apply(this,arguments);
		this.nodes = new Array();
	},

	setNodes: function ( from, to) {
		this.nodes[0] = from;
		this.nodes[1] = to;
	},

	toXML : function() {
		var xml = "<segment id='" + (this.osmid>0 ? this.osmid : 0)  +
						"' from='" + this.nodes[0].osmid + "' to='" +
							this.nodes[1].osmid + "'>";
		xml += this.tagsToXML();
		xml += "</segment>";	
		return xml;
	},

	CLASS_NAME : "OpenLayers.OSMSegment"
});
