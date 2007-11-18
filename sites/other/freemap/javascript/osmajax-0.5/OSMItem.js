
OpenLayers.OSMItem =  OpenLayers.Class.create();

OpenLayers.OSMItem.prototype = {

		tags : null,
		osmid: null,
		type : null,

		initialize : function() {
			this.tags = new Array();
		},
		addTag: function (k,v) {
			this.tags[k] = v;
		},

		toXML: function() {
			return "";
		},

		tagsToXML: function() {
			var str = "";
			for ( k in this.tags) {
				if (this.tags[k]!==null) {
					str += "<tag k='" + k + "' v='" + this.tags[k] + "' />";
				}
			}
			return str;
		},

		upload: function(URL,receiver,callback,info) {
			var realCB = (receiver===null) ? callback:callback.bind(receiver);
			var data = "method=PUT&data=<osm version='0.5'>"+this.toXML()
				+"</osm>";
			ajaxrequest(URL,data,realCB,info);
		},

		del: function(URL,receiver,callback,info) {
			var realCB = (receiver===null) ? callback:callback.bind(receiver);
			var data = "method=DELETE";
			ajaxrequest(URL,data,realCB,info);
		},

		setType: function(t) { 
			this.type = t; 
		},

		updateTags: function(newTags) {
			for(tag in newTags) {
				this.tags[tag] = newTags[tag];
			}

			// Blank any tags which should no longer be there - when we 
			// change the type of a highway we might want to blank out old
			// foot, horse tags etc.
			for(tag in this.tags) {
				if( (tag == 'foot' || tag == 'horse' || 
					tag == 'motorcar' || tag == 'bicycle') && !newTags[tag]) {
					this.tags[tag]=null;
				}
			}
		},

		CLASS_NAME : "OpenLayers.OSMItem"
}
			
