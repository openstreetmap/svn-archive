
var SRTM = Class.create ( { 
initialize: function(hExag) 
	{
		this.hExag = hExag;
	},

	load: function (json)
    {
        this.lon=json.lon;
        this.lat=json.lat;
        this.box = json.box;
        this.points = new Array();
        this.vertexIndices = new Array();
        var htidx = 0;
         // everything needs to be in km, not m
    
        for(var row=this.box[1]; row<=this.box[3]; row++)
        {
            for(var col=this.box[0]; col<=this.box[2]; col++)
            {
                this.points.push
                    (Projection.lonToGoogle(this.lon+this.res*col)*0.001);
                this.points.push(json.heights[htidx++] * 0.001 * this.hExag); 
                this.points.push(Projection.latToGoogle((this.lat+1)-
					this.res*row) *0.001);
            }
        }
    },

	info:function()
	{
		return 'box=' + this.box + ' npoints=' + this.points.length +
			' res=' + this.res;
	},
        
    getHeight : function(lon,lat,e,n)
    {
		var str = "";
        var h, htop, hbottom;
        var lonidx =Math.floor( ((lon-this.lon) / this.res) - this.box[0]),
            latidx = Math.floor((((this.lat+1)-lat) / this.res) - this.box[1]),
            width = (this.box[2]-this.box[0])+1,
            height = (this.box[3]-this.box[1])+1;
        if(lonidx>=0 && latidx>=0 && lonidx<width-1 && latidx<height-1)
        {
            var x1,x2,y1,y2;
            var x3,x4,y3,y4;
            var h1,h2,h3,h4;
    
            h1=this.points[((latidx*width+lonidx)*3)+1];
            h2=this.points[((latidx*width+lonidx+1)*3)+1];
            h3=this.points[((latidx*width+lonidx+width)*3)+1];
            h4=this.points[((latidx*width+lonidx+width+1)*3)+1];
    
            x1=this.points[(latidx*width+lonidx)*3];
            x2=this.points[(latidx*width+lonidx+1)*3];
            x3=this.points[(latidx*width+lonidx+width)*3];
            x4=this.points[(latidx*width+lonidx+width+1)*3];
            
            y1=this.points[((latidx*width+lonidx)*3)+2];
            y2=this.points[((latidx*width+lonidx+1)*3)+2];
            y3=this.points[((latidx*width+lonidx+width)*3)+2];
            y4=this.points[((latidx*width+lonidx+width+1)*3)+2];
			
			str += "Hts = " + h1 + " " + h2 + " " + h3 + " " + h4;
    
            var ptop = new Object(); 
            ptop.e = e;
            var p = (ptop.e - x1) / (x2-x1);
            ptop.n = (1-p)*y1 + p*y2;
     
            var pbottom = new Object();
            pbottom.e = e;
            var p2 = (pbottom.e - x3) / (x4-x3);
            pbottom.n = (1-p2)*y3 + p2*y4;
            
            htop = this.points[((latidx*width+lonidx)*3)+1] * (1-p) +
               this.points[((latidx*width+lonidx+1)*3)+1] * p;
            hbottom = this.points[((latidx*width+lonidx+width)*3)+1] 
                        * (1-p2) +
                  this.points[((latidx*width+lonidx+width+1)*3)+1] * p2;
    
    
            var p3 = (n - ptop.n) / (pbottom.n - ptop.n);
    
            h = htop*(1-p3) + hbottom*p3;
    
            return [h,str];
        }
        return [-1,str];
    },
    
    getBuffers: function(si)
    {
		try
		{
        var i=0;
        this.vertexIndexBuffers = new Array();
        for(var row=this.box[1]; row<this.box[3]; row++)
        {
            var vx = new Array();
            for(var col=this.box[0]; col<=this.box[2]; col++)
            {
                vx.push(i);    
                vx.push(i+((this.box[2]-this.box[0])+1));    
                i++;
            }
            this.vertexIndexBuffers.push(si.sendVertexIndicesToShader(vx));
        }
        this.buffer=si.sendDataToShader(this.points);
		this.normalBuffer = si.sendDataToShader(this.normals);
		}
		catch(e)
		{
			alert(e);
		}
    },
    
    render: function(gl,si)
    {


		try
		{
		var col = [ 0.0, 1.0, 0.0, 1.0 ];
        // select vertex buffer
        si.selectBuffer(this.buffer,si.p_vrtx);
		si.selectBuffer(this.normalBuffer,si.p_nrml);
    
		//si.doHExag(this.hExag);

		si.doColour (col);
		si.doCalculation(true);
    
        for(var count=0; count<this.vertexIndexBuffers.length; count++)
        {
            // select vertex index buffer
            si.selectVertexIndexBuffer(this.vertexIndexBuffers[count]);
    
            gl.drawElements(gl.TRIANGLE_STRIP, 
                    ((this.box[2]-this.box[0])+1)*2,
                    gl.UNSIGNED_SHORT, 0);
        }
		}
		catch(e)
		{
			alert(e);
		}
    },

	generateNormals: function()
	{
	try
	{
	var str="";
    var nrows=(this.box[3]-this.box[1])+1;
    var ncols=(this.box[2]-this.box[0])+1;
	this.normals = new Array(); 
    var i=0;
    var up,down,left,right;
	var n=new Array();
    for(var row=0; row<nrows; row++)
    {
        for(var col=0; col<ncols; col++)
        {

            if(row<nrows-1)
            {
                down = $V([ 
                this.points[(i+ncols)*3]-this.points[i*3],
                this.points[(i+ncols)*3 + 1]-this.points[i*3 + 1],
                this.points[(i+ncols)*3 + 2]-this.points[i*3 + 2]
						]);
            }
            if(row>0)
            {
                up = $V([
                this.points[(i-ncols)*3]-this.points[i*3],
                this.points[(i-ncols)*3 + 1]-this.points[i*3 + 1],
                this.points[(i-ncols)*3 + 2]-this.points[i*3 + 2]
						]);
            }
            if(col<ncols-1)
            {
                right = $V([
                this.points[(i+1)*3]-this.points[i*3],
                this.points[(i+1)*3 + 1]-this.points[i*3 + 1],
                this.points[(i+1)*3 + 2]-this.points[i*3 + 2]
						]);
            }
            if(col>0)
            {
                left = $V([ 
                this.points[(i-1)*3]-this.points[i*3],
                this.points[(i-1)*3 + 1]-this.points[i*3 + 1],
                this.points[(i-1)*3 + 2]-this.points[i*3 + 2]
						]);
            }

            if(row<nrows-1 && col<ncols-1)
                n[0] = down.cross(right).toUnitVector();
			else
				n[0] = $V([0.0, 0.0, 0.0]);
            if(row>0 && col<ncols-1)
                n[1] = right.cross(up).toUnitVector();
			else
				n[1] = $V([0.0, 0.0, 0.0]);
            if(row>0 && col>0)
                n[2] = up.cross(left).toUnitVector();
			else
				n[2] = $V([0.0, 0.0, 0.0]);
            if(row<nrows-1 && col>0)
                n[3] = left.cross(down).toUnitVector();
			else
				n[3] = $V([0.0, 0.0, 0.0]);
            
			 var normal = $V([
             n[0].elements[0]+n[1].elements[0]+
			 n[2].elements[0]+n[3].elements[0],
             n[0].elements[1]+n[1].elements[1]+
			 n[2].elements[1]+n[3].elements[1],
             n[0].elements[0]+n[1].elements[2]+
			 n[2].elements[2]+n[3].elements[2] 
			 			]).toUnitVector();
			 this.normals[i*3] = normal.elements[0];
			 this.normals[i*3+1] = normal.elements[1];
			 this.normals[i*3+2] = normal.elements[2];
			 str += 'normal: ' + this.normals[i*3].toFixed(2)+ " "
			 	+this.normals[i*3+1].toFixed(2)+" "+
				this.normals[i*3+2].toFixed(2)+"<br/>";
             i++;
        }    
    }
		return str;
	}
	catch(e)
	{
		alert(e);
	}
	}
});
