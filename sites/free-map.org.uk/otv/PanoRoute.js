
// This has a dependency on OpenLayers which is not good
// but importing Prototype direct seems to be incompatible with
// JSPanoViewer.

var PanoRoute  = OpenLayers.Class.create ();

PanoRoute.prototype = { 
    panoramas : null,

    initialize: function(url,successCb)
    {
        this.panoramas = new Array();
		this.serverURL = url;
		this.successCb = successCb;
		this.geom=null;
    },

	upload: function()
	{
		var fidlist = "";

		for(var count=0;  count<this.panoramas.length; count++)
		{
			if(count)
				fidlist+=",";
			fidlist += this.panoramas[count].fid;
		}


		/*
		var rqst = new Ajax.Request
			( this.serverURL+"?action=add",
				{ method: "post",
				  parameters: "panoIDs="+fidlist,
				  onSuccess: this.uploaded.bind(this),
				  onFailure: this.uploadFailure.bind(this)
				 }
			);
		*/
		OpenLayers.loadURL(this.serverURL+"?action=add&panoIDs="+fidlist,
						null,this,this.uploaded,this.uploadFailure);
	},

	uploaded: function(xmlHTTP)
	{
		alert(xmlHTTP.responseText);
		this.successCb(this.panoramas);
	},

	uploadFailure: function(xmlHTTP)
	{
		alert('failure: ' + xmlHTTP.status);
	}


};
