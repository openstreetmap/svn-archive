
var WebGLInitialiser = new Object();

WebGLInitialiser.getGL = function(canvas)
{
    var gl = null;

    try
    {
        // This would appear to get a 3D drawing context in Gecko
        gl=canvas.getContext("experimental-webgl");
    }
    catch(e) { }
    if(!gl)
    {
        try
        {
            // or in webkit
            gl = canvas.getContext("webkit-3d");
        }
        catch(e) { alert('this browser does not support WebGL'); }
    }

    return gl;
};

WebGLInitialiser.getShader = function(gl,id)
{
    var shader = null;
    var script = $(id);
    if(script)
    {
        var src = "";
        var k = script.firstChild;
        while(k)
        {
            if (k.nodeType == Node.TEXT_NODE)
                src += k.textContent;
            k = k.nextSibling;
        }
    
        if (script.type=="x-shader/x-fragment")
            shader = gl.createShader(gl.FRAGMENT_SHADER);
        else if (script.type=="x-shader/x-vertex")
            shader = gl.createShader(gl.VERTEX_SHADER);
        else
        {
            alert('Unrecognised shader type');
            return null;
        }

        gl.shaderSource(shader,src);
        gl.compileShader(shader);
        if (!gl.getShaderParameter(shader,gl.COMPILE_STATUS))
        {
            alert('shader compile fail:' + gl.getShaderInfoLog(shader));
            return null;
        }
    }
    return shader;
};

WebGLInitialiser.init = function(canvasID,vertShaderID,fragShaderID,
                                    wayVrtxShaderID,wayFragShaderID)
{
    var canvas = $(canvasID);
    var wgl = new Object();

    if((wgl.gl=WebGLInitialiser.getGL(canvas)))
    {
        var fragShader = WebGLInitialiser.getShader(wgl.gl,fragShaderID);
        var vrtxShader = WebGLInitialiser.getShader(wgl.gl,vertShaderID);
        var wayFragShader = WebGLInitialiser.getShader(wgl.gl,wayFragShaderID);
        var wayVrtxShader = WebGLInitialiser.getShader(wgl.gl,wayVrtxShaderID);

        if (fragShader && vrtxShader && wayFragShader && wayVrtxShader)    
        {
            var shaderProgram = wgl.gl.createProgram();
            wgl.gl.attachShader(shaderProgram,vrtxShader);
            wgl.gl.attachShader(shaderProgram,fragShader);
            wgl.gl.linkProgram(shaderProgram);
            var shaderProgram2 = wgl.gl.createProgram();
            wgl.gl.attachShader(shaderProgram2,wayVrtxShader);
            wgl.gl.attachShader(shaderProgram2,wayFragShader);
            wgl.gl.linkProgram(shaderProgram2);
            if (!wgl.gl.getProgramParameter(shaderProgram,    
                wgl.gl.LINK_STATUS))
            {
                alert('shader link fail:' +
                        wgl.gl.getProgramInfoLog(shaderProgram));
            }
            else if (!wgl.gl.getProgramParameter(shaderProgram2,    
                wgl.gl.LINK_STATUS))
            {
                alert('shader2 link fail:' +
                        wgl.gl.getProgramInfoLog(shaderProgram2));
            }
            else
            {
                wgl.gl.useProgram(shaderProgram);
                //wgl.gl.useProgram(shaderProgram2);
                wgl.si = new Object();
                wgl.si.srtm=
                    new ShaderInterface(wgl.gl,
                                        shaderProgram,'aVertex','uColour',
                                            'upMtx','umvMtx','uDoCalculation',
                                            'aNormal','unMtx');
                wgl.si.osm=
                    new ShaderInterface(wgl.gl,
                                        shaderProgram2,'aVertex2','uColour2',
                                            'upMtx2','umvMtx2',null,
                                            null,null);
                return wgl;
            }
        }
    }
    return null;
};


