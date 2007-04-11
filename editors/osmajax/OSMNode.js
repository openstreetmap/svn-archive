// OSMNode class 
// Represents an OSM node
// 
// It's a bit hacky putting IDs and tags in a "Geometry", but it's to deal
// with the fact that we don't want to create Features out of nodes which are 
// just points within ways, rather than proper features in their own right.

function OSMNode()
{
		this.nid=null;
		this.point=null;
		this.tags=new Array();

		// TODO - does the JavaScript prototype library do multiple 
		// inheritance? Good to put all the OSM stuff in a class of its own.

		this.toXML = function() { 
			var xml = "<node id='" + (this.nid>0 ? this.nid : 0)  +
						"' lat='" + this.point.y + "' lon='" + this.point.x
						+ "'>";

			for ( k in this.tags) {
				xml += "<tag k='" + k + "' v='" + this.tags[k] + "' />";
			}
			xml += "</node>";	
			return xml;
		}

		this.addTag = function(k,v) {
			this.tags[k] = v;
		}

		this.setID =  function(id) {
			this.nid=id;
		}

		// Is this node a point of interest?
		// If it contains tags other than 'created_by', it is considered so.
		this.isPOI= function() {
			if(this.tags) {
				for(k in this.tags) {
					if (k != 'created_by') {
						return true;
					}
				}
			}
			return false;
		}

		this.upload = function(URL,receiver,callback,info) {
			var realCB = callback.bind(receiver);
			var data = "data=<osm version='0.3'>"+this.toXML()+"</osm>";
			alert('uploading the following XML: to ' + URL + ' ' + data);
			ajaxrequest(URL,'PUT',data,realCB,info);
		}
}
			
