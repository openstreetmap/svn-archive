
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

			var deleteTags = new Array
				('foot','horse','motorcar','bicycle','amenity','power',
				'residence','place','religion','denomination','tourism',
				'man_made','railway','leisure');


			// Blank any tags which should no longer be there - when we 
			// change the type of a highway we might want to blank out old
			// foot, horse tags etc.
			for(tag in this.tags) {
				for(var count=0; count<deleteTags.length; count++)
				{
					if(tag==deleteTags[count])
						this.tags[tag]=null;
				}
			}

			for(tag in newTags) {
				this.tags[tag] = newTags[tag];
			}
		},

		CLASS_NAME : "OpenLayers.OSMItem"
}
			
