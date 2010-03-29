
var Camera = Class.create ( {
    initialize: function (x,y,z,angle)
    {
        this.pos = new Object();
        this.lookat = new Object();
        this.pos.x=x;
        this.pos.y=y;
        this.pos.z=z;
        this.angle = 0.0;
        this.rotate(angle);
    },

    // Rotate the camera by a given angle.
    // Calculate the distance in the x and z directions to the "look-at" 
    // point, if we assume that the "look-at" point is always 1 unit from the
    // camera.
    rotate: function(angle)
    {
        this.angle += angle;
        this.angle %= 360;
        this.dx = Math.cos(this.angle*(Math.PI/180.0));
        this.dz = Math.sin(this.angle*(Math.PI/180.0));
        this.setLookat();
    },

    // calculate a modelview matrix which will generate this camera view.
    // Calls makeLookAt(), a gluLookAt() substitute in glUtils.js.
    getLookatMatrix : function()
    {
        return makeLookAt (this.pos.x,this.pos.y,this.pos.z,
                            this.lookat.x,this.lookat.y,this.lookat.z,
                            0,1,0);
    },

    // Move camera forward by a given amount
    // Note that we have to recalculate the "look-at" point
    forward: function(dist)
    {
        this.pos.x += dist*this.dx;
        this.pos.z -= dist*this.dz;
        this.setLookat();
    },

    up: function(dist)
    {
        this.pos.y += dist;
        this.lookat.y = this.pos.y;
    },

    changeX: function(dist)
    {
        this.pos.x += dist;
        this.setLookat();
    },

    changeZ: function(dist)
    {
        this.pos.z += dist;
        this.setLookat();
    },

    // Recalculate the look-at point
    setLookat: function()
    {
        this.lookat.x = this.pos.x + this.dx;
        this.lookat.y = this.pos.y;
        this.lookat.z = this.pos.z - this.dz;
    }
} );
