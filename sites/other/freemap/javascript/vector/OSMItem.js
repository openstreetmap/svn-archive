
OpenLayers.OSMItem =  OpenLayers.Class.create();

OpenLayers.OSMItem.prototype = {

		tags : null,
		osmid: null,
		type: null,

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

		setType: function(t) { 
			this.type = t; 
		},

		upload: function(URL,receiver,callback,info) {
			var realCB = (receiver===null) ? callback:callback.bind(receiver);
			var data = "method=PUT&data=<osm version='0.3'>"+this.toXML()
				+"</osm>";
//			alert('uploading the following XML: to ' + URL + ' ' + data);
			ajaxrequest(URL,'PUT',data,realCB,info);
		},

		del: function(URL,receiver,callback,info) {
			var realCB = (receiver===null) ? callback:callback.bind(receiver);
			var data = "method=DELETE";
			ajaxrequest(URL,'DELETE',data,realCB,info);
		},

		CLASS_NAME : "OpenLayers.OSMItem"
}
			
