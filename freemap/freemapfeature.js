OpenLayers.Feature.FreemapAnnotations = Class.create();
OpenLayers.Feature.FreemapAnnotations.prototype =
	Object.extend (new OpenLayers.Feature(), {

		thePopup: null,
		currentObj : null,
		id: 0,

		initialize: function (layer,lonLat,data) {
			OpenLayers.Feature.prototype.initialize.apply(this,arguments);
			this.createMarker();
			this.createPopup();
			this.layer.addMarker(this.marker);
			this.marker.events.register('click',  this, this.markerClicked);
			this.popup.events.register('click',  this, this.hidePopup);
			currentObj = this;
		},

		destroy: function() {
			if (this.marker != null) {
				this.layer.removeMarker(this.marker);
			}
			OpenLayers.Feature.prototype.destroy.apply(this,arguments);
		},

		markerClicked: function() {
			if(this.layer.mode == 2)
			{
				ajax 
				("http://nick.dev.openstreetmap.org/openlayers/ajaxserver.php",
				 "action=delete&fid="+this.id);
				this.destroy();
			}
			else
			{
				this.showPopup();
			}
		},

		showPopup: function() { 
			//alert('feature was clicked!');  // this works
			this.layer.map.addPopup(this.popup);
			//currentObj.thePopup.draw(); 
		}, 

		hidePopup: function() { 
			//alert('popup was clicked');
			this.layer.map.removePopup(this.popup);
		}
				
} );
