
// Class to represent a variable thickness line; 
// precalculates an array of vertices given a width in OpenGL units.
var Line = Class.create ( {
    initialize: function(vertices,width)
    {
		this.points = [];
		for (var count=0; count<(vertices.length/3)-1; count++)
		{
			var dx =   vertices[count*3+3] - vertices[count*3],
				dz = vertices[count*3+5] - vertices[count*3+2];
			var vec = $V ( [dx, 0, dz] ); 

			var vlength = vec.modulus();

			// perpendicular dx and dz, unsigned
			// this will always give the point in +90 (anticlockwise) direction
			var dxperp = (dz*(width/2)) / vlength,
				dzperp = -(dx*(width/2)) / vlength;

			// xyz of one side (anticlockwise)
			this.points.push(vertices[count*3]+dxperp);
			this.points.push(vertices[count*3+1]); //y doesn't change
			this.points.push(vertices[count*3+2]+dzperp);


			// xyz of the other (clockwise)
			this.points.push(vertices[count*3]-dxperp);
			this.points.push(vertices[count*3+1]); //y doesn't change
			this.points.push(vertices[count*3+2]-dzperp);
		}

		// do end point
		this.points.push(vertices[count*3]+dxperp);
		this.points.push(vertices[count*3+1]); //y doesn't change
		this.points.push(vertices[count*3+2]+dzperp);

		this.points.push(vertices[count*3]-dxperp);
		this.points.push(vertices[count*3+1]); //y doesn't change
		this.points.push(vertices[count*3+2]-dzperp);

        // Vertex indices; see learningwebgl.com, lesson 4
        this.vertexIndices = [];
	 	for(var count=0; count<((vertices.length/3)*2)-2; count+=2)
	 	{
			this.vertexIndices.push(count);
		 	this.vertexIndices.push(count+1);
		 	this.vertexIndices.push(count+2);
		 	this.vertexIndices.push(count+1);
		 	this.vertexIndices.push(count+3);
		 	this.vertexIndices.push(count+2);
	 	}
    }
} );
