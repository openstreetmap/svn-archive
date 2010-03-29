var DownloadedTiles = Class.create ( {
	initialize: function()
	{
		this.xs = new Array();
		this.ys = new Array();
		this.layers = new Array();
	},

	add: function(layer,x,y)
	{
		this.xs.push(x);
		this.ys.push(y);
		this.layers.push(layer);
	},

	isDownloaded: function (layer,x,y)
	{
		for(var count=0; count<this.xs.length; count++)
		{
			if(this.xs[count]==x && this.ys[count]==y &&
				this.layers[count]==layer)
			{
				return true;
			}
		}
		return false;
	}
} );

