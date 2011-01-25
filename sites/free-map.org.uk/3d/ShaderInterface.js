
// 20/01/11 changed WebGLFloatArray to Float32Array
// see: http://learningwebgl.com/blog/?p=2566

var ShaderInterface = Class.create ( {
    initialize: function(gl,shaderProgram,vertexVarName,colourVarName,
                        perspMtxName,mvMtxName, doCalculationName,
						normalName,nMtxName)
    {
		try
		{
        this.gl=gl;
		this.shaderProgram=shaderProgram;
		if(vertexVarName)
		{
        	this.p_vrtx=this.gl.getAttribLocation(shaderProgram,vertexVarName);
			//alert(vertexVarName + ' ' + this.p_vrtx);
        	this.gl.enableVertexAttribArray(this.p_vrtx);
		}

		// uniform colouring
		if(colourVarName)
			this.p_uCol=this.gl.getUniformLocation(shaderProgram,colourVarName);

		if(normalName)
		{
			this.p_nrml = this.gl.getAttribLocation(shaderProgram,normalName);
        	this.gl.enableVertexAttribArray(this.p_nrml);
		}

        this.p_upMtx = this.gl.getUniformLocation(shaderProgram,perspMtxName);
        this.p_umvMtx = this.gl.getUniformLocation(shaderProgram,mvMtxName);

		if(doCalculationName)
		{
			this.p_uDoCalculation=this.gl.getUniformLocation
			(shaderProgram,doCalculationName);

		}

		if(nMtxName)
		{
			this.p_unMtx =this.gl.getUniformLocation(shaderProgram,nMtxName);
		}

		/*
		alert('vertex=' + this.p_vrtx + ' normal='+this.p_nrml +
			' ucol=' + this.p_uCol+' pmtx=' + this.p_upMtx + 
			' mvmtx=' + this.p_umvMtx +  
			' nmtx=' + this.p_unMtx   + ' docalualtion='
			+this.p_uDoCalculation);
		*/
        this.vertexAttribPointers = new Array();
		}
		catch(e) { alert('ERROR: ' + e);}
    },

    sendDataToShader: function(data)
    {
		try
		{
        var buffer1= this.gl.createBuffer();
        this.gl.bindBuffer(this.gl.ARRAY_BUFFER,buffer1);
        this.gl.bufferData
            (this.gl.ARRAY_BUFFER,new Float32Array(data),
            this.gl.STATIC_DRAW);
        return buffer1;
		}
		catch(e)
		{
			alert(e);
		}
    },

    sendVertexIndicesToShader: function(indices)
    {
        var buffer=this.gl.createBuffer();
        this.gl.bindBuffer(this.gl.ELEMENT_ARRAY_BUFFER,buffer);
        this.gl.bufferData(this.gl.ELEMENT_ARRAY_BUFFER,
            new Uint16Array(indices),
            this.gl.STATIC_DRAW);
        return buffer;
    },

    doMatrices: function(pMtx,mvMtx,nMtx)
    {
        this.gl.uniformMatrix4fv
            (this.p_upMtx,false,new Float32Array(pMtx.flatten()));
        this.gl.uniformMatrix4fv
            (this.p_umvMtx,false,new Float32Array(mvMtx.flatten()));
		if(nMtx!=null)
		{
        	this.gl.uniformMatrix4fv
            (this.p_unMtx,false,new Float32Array(nMtx.flatten()));
		}
    },

	doColour: function(colour)
	{
		this.gl.uniform4fv
			(this.p_uCol,new Float32Array(colour));
	},

	doCalculation: function(calculation)
	{
		try
		{
		this.gl.uniform1i(this.p_uDoCalculation,calculation);
		}
		catch(e)
		{
		alert(e);
		}
	},

	doHExag: function(hExag)
	{
		this.gl.uniform1f(this.p_uhExag,hExag);
	},

    selectBuffer: function(bufferIn,shadervarIn)
    {
        this.gl.bindBuffer(this.gl.ARRAY_BUFFER,bufferIn);
        this.gl.vertexAttribPointer
                (shadervarIn,3,this.gl.FLOAT,false,0,0);
    },

    selectVertexIndexBuffer: function(bufferIn)
    {
        this.gl.bindBuffer(this.gl.ELEMENT_ARRAY_BUFFER,bufferIn);
    }
});
