
var Way = Class.create({
	initialize: function(way,wayProperties,si)
	{
        this.buffer=si.sendDataToShader(way.vertices);
        this.length=way.vertices.length/3;
        this.colour=
            (wayProperties[way.tags.highway]) ?
            wayProperties[way.tags.highway].colours: [0.5,0.5,0.5,1.0];
        this.width=
            (wayProperties[way.tags.highway]) ?
            wayProperties[way.tags.highway].width: 1.0;
		this.midpoint = $V([way.mpx, 0, way.mpz]);
	},

	render:function(gl,si,d)
	{
        si.doColour (this.colour);

        // simple lines - no variable widths
        // vertices
        si.selectBuffer(this.buffer,si.p_vrtx);

		
	    gl.lineWidth(this.width);

		// need to store the number of vertices in a line
        gl.drawArrays(gl.LINE_STRIP, 0, this.length);
	}
} );
