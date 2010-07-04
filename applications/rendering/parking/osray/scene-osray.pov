
#include "colors.inc"
global_settings {
    assumed_gamma 2.0
    noise_generator 2
    ambient_light rgb <0.9,0.9,1>
/*
    radiosity {
        count 1000
        error_bound 0.7
        recursion_limit 6
        pretrace_end 0.002
    }
*/
}

camera {
   orthographic
   location <0, 10000, 0>
   sky <0, 1, 0>
   direction <0, 0, 1>
   right <1.0*20037508.3392, 0, 0>
   up <0, 1*20037508.3392*cos(radians(20)), 0> /* this stretches in y to compensate for the rotate below */
   look_at <0, 0, 0>
   rotate <-20,0,0>
   scale <1,1,1>
   translate <10018754.1696,0,-10018754.1696>
}

/* ground */
box {
    <0.0, -0.5, -20037508.3392>, <20037508.3392, -0.2, -7.08115455161e-10>
    texture {
        pigment {
            /* color rgb <1, 1, 1> */
            image_map {
                png "scene-osray-landuse-texture.png"
                map_type 0
                interpolate 2
            }
        }
        rotate <90,0,0>
        scale <20037508.3392,1,20037508.3392>
        translate <0.0,0,-20037508.3392>
    }
    finish {
        specular 0.5
        roughness 0.05
        ambient 0.2
        /*reflection 0.5*/
    }
}
/* sky */
sky_sphere {
    pigment {
        gradient y
        color_map {
            [ 0.5 color CornflowerBlue ]
            [ 1.0 color MidnightBlue ]
        }
        scale 20
        translate -10
    }
}

light_source {
    <0, 1000000,0>,
    rgb <1, 1, 0.9>
    area_light <100000, 0, 0>, <0, 0, 100000>, 8, 8
    adaptive 1
    circular
    rotate <45,10,0>
    translate <10018754.1696,0,-10018754.1696>
}


#declare boundbox = box {
    <0, -1, 0>, <1, 1, 1>
    scale <22041259.1731,50,22041259.1731>
    translate <-1001875.41696,0,-21039383.7562>
}
/* bounding box viz
object { boundbox
    texture {
        pigment {
            color rgb <1, 1, 0>
        }
    }
    finish {
        specular 0.5
        roughness 0.05
        ambient 0.2
        refraction 0.9
    }
} */
