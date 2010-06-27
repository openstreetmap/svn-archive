
#include "colors.inc"
global_settings {
    assumed_gamma 1.5
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
   right <0.781183460015*1038.12040569, 0, 0>
   up <0, 1*1038.12040569*cos(radians(20)), 0> /* this stretches in y to compensate for the rotate below */
   look_at <0, 0, 0>
   rotate <-20,0,0>
   scale <1,1,1>
   translate <1105654.68222,0,6410707.46344>
}

/* ground */
box {
    <1105249.20098, -0.5, 6410188.40323>, <1106060.16347, -0.2, 6411226.52364>
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
        scale <810.96249043,1,1038.12040569>
        translate <1105249.20098,0,6410188.40323>
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
    rotate <-45,-10,0>
    translate <1105654.68222,0,6410707.46344>
}

sphere_sweep { linear_spline, 5,
/* osm_id=13024530 */
  <1105407.7, 0.0, 6411239.19>,2.4
  <1105407.7, 0.0, 6411239.19>,2.4
  <1105300.02, 0.0, 6411203.7>,2.4
  <1105245.16, 0.0, 6411178.08>,2.4
  <1105245.16, 0.0, 6411178.08>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=13024530 */
  <1105407.7, 0.0, 6411239.19>,2.88
  <1105407.7, 0.0, 6411239.19>,2.88
  <1105300.02, 0.0, 6411203.7>,2.88
  <1105245.16, 0.0, 6411178.08>,2.88
  <1105245.16, 0.0, 6411178.08>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=11299275 */
  <1105414.76, 0.0, 6410415.84>,4.0
  <1105414.76, 0.0, 6410415.84>,4.0
  <1105420.86, 0.0, 6410384.27>,4.0
  <1105434.85, 0.0, 6410381.75>,4.0
  <1105434.85, 0.0, 6410381.75>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=11299275 */
  <1105414.76, 0.0, 6410415.84>,4.8
  <1105414.76, 0.0, 6410415.84>,4.8
  <1105420.86, 0.0, 6410384.27>,4.8
  <1105434.85, 0.0, 6410381.75>,4.8
  <1105434.85, 0.0, 6410381.75>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=34680594 */
  <1105478.37, 0.0, 6410424.53>,4.0
  <1105478.37, 0.0, 6410424.53>,4.0
  <1105471.45, 0.0, 6410423.24>,4.0
  <1105450.39, 0.0, 6410418.36>,4.0
  <1105414.76, 0.0, 6410415.84>,4.0
  <1105414.76, 0.0, 6410415.84>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=34680594 */
  <1105478.37, 0.0, 6410424.53>,4.8
  <1105478.37, 0.0, 6410424.53>,4.8
  <1105471.45, 0.0, 6410423.24>,4.8
  <1105450.39, 0.0, 6410418.36>,4.8
  <1105414.76, 0.0, 6410415.84>,4.8
  <1105414.76, 0.0, 6410415.84>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=10213071 */
  <1105432.15, 0.0, 6410309.45>,2.4
  <1105432.15, 0.0, 6410309.45>,2.4
  <1105445.9, 0.0, 6410354.32>,2.4
  <1105449.57, 0.0, 6410378.87>,2.4
  <1105449.57, 0.0, 6410378.87>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=10213071 */
  <1105432.15, 0.0, 6410309.45>,2.88
  <1105432.15, 0.0, 6410309.45>,2.88
  <1105445.9, 0.0, 6410354.32>,2.88
  <1105449.57, 0.0, 6410378.87>,2.88
  <1105449.57, 0.0, 6410378.87>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=10213070 */
  <1105450.39, 0.0, 6410418.36>,4.0
  <1105450.39, 0.0, 6410418.36>,4.0
  <1105449.57, 0.0, 6410378.87>,4.0
  <1105449.57, 0.0, 6410378.87>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=10213070 */
  <1105450.39, 0.0, 6410418.36>,4.8
  <1105450.39, 0.0, 6410418.36>,4.8
  <1105449.57, 0.0, 6410378.87>,4.8
  <1105449.57, 0.0, 6410378.87>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 7,
/* osm_id=28408949 */
  <1105451.82, 0.0, 6410230.89>,4.0
  <1105451.82, 0.0, 6410230.89>,4.0
  <1105466.48, 0.0, 6410146.19>,4.0
  <1105469.21, 0.0, 6410133>,4.0
  <1105480.09, 0.0, 6410084.63>,4.0
  <1105481.55, 0.0, 6410041.09>,4.0
  <1105481.55, 0.0, 6410041.09>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 7,
/* osm_id=28408949 */
  <1105451.82, 0.0, 6410230.89>,4.8
  <1105451.82, 0.0, 6410230.89>,4.8
  <1105466.48, 0.0, 6410146.19>,4.8
  <1105469.21, 0.0, 6410133>,4.8
  <1105480.09, 0.0, 6410084.63>,4.8
  <1105481.55, 0.0, 6410041.09>,4.8
  <1105481.55, 0.0, 6410041.09>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=30214904 */
  <1105464.74, 0.0, 6410506.08>,4.0
  <1105464.74, 0.0, 6410506.08>,4.0
  <1105466.29, 0.0, 6410489.03>,4.0
  <1105466.29, 0.0, 6410489.03>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=30214904 */
  <1105464.74, 0.0, 6410506.08>,4.8
  <1105464.74, 0.0, 6410506.08>,4.8
  <1105466.29, 0.0, 6410489.03>,4.8
  <1105466.29, 0.0, 6410489.03>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=27411432 */
  <1105471.45, 0.0, 6410423.24>,4.0
  <1105471.45, 0.0, 6410423.24>,4.0
  <1105466.29, 0.0, 6410489.03>,4.0
  <1105512.15, 0.0, 6410497.63>,4.0
  <1105512.15, 0.0, 6410497.63>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=27411432 */
  <1105471.45, 0.0, 6410423.24>,4.8
  <1105471.45, 0.0, 6410423.24>,4.8
  <1105466.29, 0.0, 6410489.03>,4.8
  <1105512.15, 0.0, 6410497.63>,4.8
  <1105512.15, 0.0, 6410497.63>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=23034046 */
  <1105865.68, 0.0, 6410772.64>,2.4
  <1105865.68, 0.0, 6410772.64>,2.4
  <1105851.67, 0.0, 6410764.69>,2.4
  <1105811.23, 0.0, 6410760.8>,2.4
  <1105811.23, 0.0, 6410760.8>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=23034046 */
  <1105865.68, 0.0, 6410772.64>,2.88
  <1105865.68, 0.0, 6410772.64>,2.88
  <1105851.67, 0.0, 6410764.69>,2.88
  <1105811.23, 0.0, 6410760.8>,2.88
  <1105811.23, 0.0, 6410760.8>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 7,
/* osm_id=12710971 */
  <1105817.69, 0.0, 6410149.93>,4.0
  <1105817.69, 0.0, 6410149.93>,4.0
  <1105823.36, 0.0, 6410178.55>,4.0
  <1105839.92, 0.0, 6410208.47>,4.0
  <1105867.54, 0.0, 6410248.97>,4.0
  <1105884.22, 0.0, 6410289.43>,4.0
  <1105884.22, 0.0, 6410289.43>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 7,
/* osm_id=12710971 */
  <1105817.69, 0.0, 6410149.93>,4.8
  <1105817.69, 0.0, 6410149.93>,4.8
  <1105823.36, 0.0, 6410178.55>,4.8
  <1105839.92, 0.0, 6410208.47>,4.8
  <1105867.54, 0.0, 6410248.97>,4.8
  <1105884.22, 0.0, 6410289.43>,4.8
  <1105884.22, 0.0, 6410289.43>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=12710997 */
  <1105865.68, 0.0, 6410772.64>,4.0
  <1105865.68, 0.0, 6410772.64>,4.0
  <1105865.98, 0.0, 6410797.87>,4.0
  <1105876.77, 0.0, 6410826.78>,4.0
  <1105890.14, 0.0, 6410852.21>,4.0
  <1105890.14, 0.0, 6410852.21>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=12710997 */
  <1105865.68, 0.0, 6410772.64>,4.8
  <1105865.68, 0.0, 6410772.64>,4.8
  <1105865.98, 0.0, 6410797.87>,4.8
  <1105876.77, 0.0, 6410826.78>,4.8
  <1105890.14, 0.0, 6410852.21>,4.8
  <1105890.14, 0.0, 6410852.21>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=35145975 */
  <1105884.22, 0.0, 6410289.43>,2.4
  <1105884.22, 0.0, 6410289.43>,2.4
  <1105900.69, 0.0, 6410323.09>,2.4
  <1105916.8, 0.0, 6410362.66>,2.4
  <1105916.8, 0.0, 6410362.66>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=35145975 */
  <1105884.22, 0.0, 6410289.43>,2.88
  <1105884.22, 0.0, 6410289.43>,2.88
  <1105900.69, 0.0, 6410323.09>,2.88
  <1105916.8, 0.0, 6410362.66>,2.88
  <1105916.8, 0.0, 6410362.66>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 7,
/* osm_id=12710967 */
  <1105959.21, 0.0, 6410097.78>,4.0
  <1105959.21, 0.0, 6410097.78>,4.0
  <1105955.46, 0.0, 6410142.65>,4.0
  <1105960.53, 0.0, 6410169.81>,4.0
  <1105968.35, 0.0, 6410207.09>,4.0
  <1105981.49, 0.0, 6410250.75>,4.0
  <1105981.49, 0.0, 6410250.75>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 7,
/* osm_id=12710967 */
  <1105959.21, 0.0, 6410097.78>,4.8
  <1105959.21, 0.0, 6410097.78>,4.8
  <1105955.46, 0.0, 6410142.65>,4.8
  <1105960.53, 0.0, 6410169.81>,4.8
  <1105968.35, 0.0, 6410207.09>,4.8
  <1105981.49, 0.0, 6410250.75>,4.8
  <1105981.49, 0.0, 6410250.75>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=35145974 */
  <1105981.49, 0.0, 6410250.75>,2.4
  <1105981.49, 0.0, 6410250.75>,2.4
  <1106019.45, 0.0, 6410355.3>,2.4
  <1106047.98, 0.0, 6410434.48>,2.4
  <1106076.77, 0.0, 6410500.04>,2.4
  <1106076.77, 0.0, 6410500.04>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=35145974 */
  <1105981.49, 0.0, 6410250.75>,2.88
  <1105981.49, 0.0, 6410250.75>,2.88
  <1106019.45, 0.0, 6410355.3>,2.88
  <1106047.98, 0.0, 6410434.48>,2.88
  <1106076.77, 0.0, 6410500.04>,2.88
  <1106076.77, 0.0, 6410500.04>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=12710854 */
  <1105134.31, 0.0, 6410347.76>,1.5
  <1105134.31, 0.0, 6410347.76>,1.5
  <1105192.8, 0.0, 6410358.01>,1.5
  <1105252.7, 0.0, 6410368.49>,1.5
  <1105252.7, 0.0, 6410368.49>,1.5
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=12710854 */
  <1105134.31, 0.0, 6410347.76>,1.8
  <1105134.31, 0.0, 6410347.76>,1.8
  <1105192.8, 0.0, 6410358.01>,1.8
  <1105252.7, 0.0, 6410368.49>,1.8
  <1105252.7, 0.0, 6410368.49>,1.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=31892909 */
  <1105293.38, 0.0, 6410687.47>,1.5
  <1105293.38, 0.0, 6410687.47>,1.5
  <1105290.55, 0.0, 6410628.39>,1.5
  <1105290.55, 0.0, 6410628.39>,1.5
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=31892909 */
  <1105293.38, 0.0, 6410687.47>,1.8
  <1105293.38, 0.0, 6410687.47>,1.8
  <1105290.55, 0.0, 6410628.39>,1.8
  <1105290.55, 0.0, 6410628.39>,1.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=30980476 */
  <1105299.96, 0.0, 6410628.53>,1.5
  <1105299.96, 0.0, 6410628.53>,1.5
  <1105359.6, 0.0, 6410629.57>,1.5
  <1105359.6, 0.0, 6410629.57>,1.5
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=30980476 */
  <1105299.96, 0.0, 6410628.53>,1.8
  <1105299.96, 0.0, 6410628.53>,1.8
  <1105359.6, 0.0, 6410629.57>,1.8
  <1105359.6, 0.0, 6410629.57>,1.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=58570941 */
  <1105391.8, 0.0, 6410347.54>,1.5
  <1105391.8, 0.0, 6410347.54>,1.5
  <1105386.52, 0.0, 6410344.31>,1.5
  <1105386.52, 0.0, 6410344.31>,1.5
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=58570941 */
  <1105391.8, 0.0, 6410347.54>,1.8
  <1105391.8, 0.0, 6410347.54>,1.8
  <1105386.52, 0.0, 6410344.31>,1.8
  <1105386.52, 0.0, 6410344.31>,1.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=58570944 */
  <1105439.03, 0.0, 6410354.8>,1.5
  <1105439.03, 0.0, 6410354.8>,1.5
  <1105420.43, 0.0, 6410356.08>,1.5
  <1105405.13, 0.0, 6410353.64>,1.5
  <1105396.95, 0.0, 6410350.06>,1.5
  <1105396.95, 0.0, 6410350.06>,1.5
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=58570944 */
  <1105439.03, 0.0, 6410354.8>,1.8
  <1105439.03, 0.0, 6410354.8>,1.8
  <1105420.43, 0.0, 6410356.08>,1.8
  <1105405.13, 0.0, 6410353.64>,1.8
  <1105396.95, 0.0, 6410350.06>,1.8
  <1105396.95, 0.0, 6410350.06>,1.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=12711043 */
  <1105445.21, 0.0, 6411109.27>,1.5
  <1105445.21, 0.0, 6411109.27>,1.5
  <1105435.53, 0.0, 6411137.31>,1.5
  <1105419.54, 0.0, 6411187.87>,1.5
  <1105419.54, 0.0, 6411187.87>,1.5
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=12711043 */
  <1105445.21, 0.0, 6411109.27>,1.8
  <1105445.21, 0.0, 6411109.27>,1.8
  <1105435.53, 0.0, 6411137.31>,1.8
  <1105419.54, 0.0, 6411187.87>,1.8
  <1105419.54, 0.0, 6411187.87>,1.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=11299276 */
  <1105434.85, 0.0, 6410381.75>,1.5
  <1105434.85, 0.0, 6410381.75>,1.5
  <1105449.57, 0.0, 6410378.87>,1.5
  <1105449.57, 0.0, 6410378.87>,1.5
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=11299276 */
  <1105434.85, 0.0, 6410381.75>,1.8
  <1105434.85, 0.0, 6410381.75>,1.8
  <1105449.57, 0.0, 6410378.87>,1.8
  <1105449.57, 0.0, 6410378.87>,1.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=58532800 */
  <1105445.9, 0.0, 6410354.32>,1.5
  <1105445.9, 0.0, 6410354.32>,1.5
  <1105444.24, 0.0, 6410354.44>,1.5
  <1105442.06, 0.0, 6410354.58>,1.5
  <1105442.06, 0.0, 6410354.58>,1.5
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=58532800 */
  <1105445.9, 0.0, 6410354.32>,1.8
  <1105445.9, 0.0, 6410354.32>,1.8
  <1105444.24, 0.0, 6410354.44>,1.8
  <1105442.06, 0.0, 6410354.58>,1.8
  <1105442.06, 0.0, 6410354.58>,1.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 11,
/* osm_id=27137049 */
  <1105538.01, 0.0, 6411158.42>,1.5
  <1105538.01, 0.0, 6411158.42>,1.5
  <1105541.5, 0.0, 6411162.11>,1.5
  <1105546.53, 0.0, 6411167.45>,1.5
  <1105550.15, 0.0, 6411176.68>,1.5
  <1105551.06, 0.0, 6411185.91>,1.5
  <1105552.32, 0.0, 6411195.68>,1.5
  <1105552.32, 0.0, 6411203.1>,1.5
  <1105550.83, 0.0, 6411209.82>,1.5
  <1105550.52, 0.0, 6411211.26>,1.5
  <1105550.52, 0.0, 6411211.26>,1.5
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 11,
/* osm_id=27137049 */
  <1105538.01, 0.0, 6411158.42>,1.8
  <1105538.01, 0.0, 6411158.42>,1.8
  <1105541.5, 0.0, 6411162.11>,1.8
  <1105546.53, 0.0, 6411167.45>,1.8
  <1105550.15, 0.0, 6411176.68>,1.8
  <1105551.06, 0.0, 6411185.91>,1.8
  <1105552.32, 0.0, 6411195.68>,1.8
  <1105552.32, 0.0, 6411203.1>,1.8
  <1105550.83, 0.0, 6411209.82>,1.8
  <1105550.52, 0.0, 6411211.26>,1.8
  <1105550.52, 0.0, 6411211.26>,1.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=22662546 */
  <1105558.12, 0.0, 6410568.33>,1.5
  <1105558.12, 0.0, 6410568.33>,1.5
  <1105567.67, 0.0, 6410587.44>,1.5
  <1105567.67, 0.0, 6410587.44>,1.5
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=22662546 */
  <1105558.12, 0.0, 6410568.33>,1.8
  <1105558.12, 0.0, 6410568.33>,1.8
  <1105567.67, 0.0, 6410587.44>,1.8
  <1105567.67, 0.0, 6410587.44>,1.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=27153004 */
  <1105567.67, 0.0, 6410587.44>,1.5
  <1105567.67, 0.0, 6410587.44>,1.5
  <1105590.75, 0.0, 6410601.82>,1.5
  <1105590.75, 0.0, 6410601.82>,1.5
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=27153004 */
  <1105567.67, 0.0, 6410587.44>,1.8
  <1105567.67, 0.0, 6410587.44>,1.8
  <1105590.75, 0.0, 6410601.82>,1.8
  <1105590.75, 0.0, 6410601.82>,1.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=58563358 */
  <1105640.5, 0.0, 6410684.71>,1.5
  <1105640.5, 0.0, 6410684.71>,1.5
  <1105595.08, 0.0, 6410683.64>,1.5
  <1105595.08, 0.0, 6410683.64>,1.5
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=58563358 */
  <1105640.5, 0.0, 6410684.71>,1.8
  <1105640.5, 0.0, 6410684.71>,1.8
  <1105595.08, 0.0, 6410683.64>,1.8
  <1105595.08, 0.0, 6410683.64>,1.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=27152853 */
  <1105670.84, 0.0, 6410770.56>,1.5
  <1105670.84, 0.0, 6410770.56>,1.5
  <1105620.22, 0.0, 6410762.83>,1.5
  <1105620.22, 0.0, 6410762.83>,1.5
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=27152853 */
  <1105670.84, 0.0, 6410770.56>,1.8
  <1105670.84, 0.0, 6410770.56>,1.8
  <1105620.22, 0.0, 6410762.83>,1.8
  <1105620.22, 0.0, 6410762.83>,1.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12710865 */
  <1105707.16, 0.0, 6410773.85>,1.5
  <1105707.16, 0.0, 6410773.85>,1.5
  <1105670.84, 0.0, 6410770.56>,1.5
  <1105670.84, 0.0, 6410770.56>,1.5
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12710865 */
  <1105707.16, 0.0, 6410773.85>,1.8
  <1105707.16, 0.0, 6410773.85>,1.8
  <1105670.84, 0.0, 6410770.56>,1.8
  <1105670.84, 0.0, 6410770.56>,1.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12710992 */
  <1105865.68, 0.0, 6410772.64>,1.5
  <1105865.68, 0.0, 6410772.64>,1.5
  <1105874.73, 0.0, 6410726.05>,1.5
  <1105874.73, 0.0, 6410726.05>,1.5
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12710992 */
  <1105865.68, 0.0, 6410772.64>,1.8
  <1105865.68, 0.0, 6410772.64>,1.8
  <1105874.73, 0.0, 6410726.05>,1.8
  <1105874.73, 0.0, 6410726.05>,1.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 10,
/* osm_id=46188835 */
  <1106037.85, 0.0, 6410053.3>,1.5
  <1106037.85, 0.0, 6410053.3>,1.5
  <1106018.38, 0.0, 6410085.06>,1.5
  <1106008.8, 0.0, 6410105.77>,1.5
  <1106027.71, 0.0, 6410164.36>,1.5
  <1106055.68, 0.0, 6410239.42>,1.5
  <1106093.91, 0.0, 6410341.97>,1.5
  <1106115.41, 0.0, 6410411.48>,1.5
  <1106145.61, 0.0, 6410509.18>,1.5
  <1106145.61, 0.0, 6410509.18>,1.5
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 10,
/* osm_id=46188835 */
  <1106037.85, 0.0, 6410053.3>,1.8
  <1106037.85, 0.0, 6410053.3>,1.8
  <1106018.38, 0.0, 6410085.06>,1.8
  <1106008.8, 0.0, 6410105.77>,1.8
  <1106027.71, 0.0, 6410164.36>,1.8
  <1106055.68, 0.0, 6410239.42>,1.8
  <1106093.91, 0.0, 6410341.97>,1.8
  <1106115.41, 0.0, 6410411.48>,1.8
  <1106145.61, 0.0, 6410509.18>,1.8
  <1106145.61, 0.0, 6410509.18>,1.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=32295317 */
  <1106026.78, 0.0, 6410731.74>,1.5
  <1106026.78, 0.0, 6410731.74>,1.5
  <1106092.59, 0.0, 6410781.84>,1.5
  <1106095.81, 0.0, 6410784.3>,1.5
  <1106095.81, 0.0, 6410784.3>,1.5
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=32295317 */
  <1106026.78, 0.0, 6410731.74>,1.8
  <1106026.78, 0.0, 6410731.74>,1.8
  <1106092.59, 0.0, 6410781.84>,1.8
  <1106095.81, 0.0, 6410784.3>,1.8
  <1106095.81, 0.0, 6410784.3>,1.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 13,
/* osm_id=46188837 */
  <1106055.68, 0.0, 6410239.42>,1.5
  <1106055.68, 0.0, 6410239.42>,1.5
  <1106069.19, 0.0, 6410237.4>,1.5
  <1106093.14, 0.0, 6410244.23>,1.5
  <1106107.01, 0.0, 6410254.01>,1.5
  <1106123.41, 0.0, 6410268.17>,1.5
  <1106139.17, 0.0, 6410269.63>,1.5
  <1106187.08, 0.0, 6410257.42>,1.5
  <1106228.04, 0.0, 6410250.6>,1.5
  <1106253.59, 0.0, 6410249.13>,1.5
  <1106257.37, 0.0, 6410247.16>,1.5
  <1106290.49, 0.0, 6410235.04>,1.5
  <1106290.49, 0.0, 6410235.04>,1.5
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 13,
/* osm_id=46188837 */
  <1106055.68, 0.0, 6410239.42>,1.8
  <1106055.68, 0.0, 6410239.42>,1.8
  <1106069.19, 0.0, 6410237.4>,1.8
  <1106093.14, 0.0, 6410244.23>,1.8
  <1106107.01, 0.0, 6410254.01>,1.8
  <1106123.41, 0.0, 6410268.17>,1.8
  <1106139.17, 0.0, 6410269.63>,1.8
  <1106187.08, 0.0, 6410257.42>,1.8
  <1106228.04, 0.0, 6410250.6>,1.8
  <1106253.59, 0.0, 6410249.13>,1.8
  <1106257.37, 0.0, 6410247.16>,1.8
  <1106290.49, 0.0, 6410235.04>,1.8
  <1106290.49, 0.0, 6410235.04>,1.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=4587363 */
  <1105345.44, 0.0, 6411104.7>,3.0
  <1105345.44, 0.0, 6411104.7>,3.0
  <1105383.56, 0.0, 6410998.05>,3.0
  <1105383.56, 0.0, 6410998.05>,3.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=4587363 */
  <1105345.44, 0.0, 6411104.7>,3.6
  <1105345.44, 0.0, 6411104.7>,3.6
  <1105383.56, 0.0, 6410998.05>,3.6
  <1105383.56, 0.0, 6410998.05>,3.6
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=58570939 */
  <1105411.07, 0.0, 6410325.55>,3.0
  <1105411.07, 0.0, 6410325.55>,3.0
  <1105405.27, 0.0, 6410297.98>,3.0
  <1105434.6, 0.0, 6410290.57>,3.0
  <1105434.6, 0.0, 6410290.57>,3.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=58570939 */
  <1105411.07, 0.0, 6410325.55>,3.6
  <1105411.07, 0.0, 6410325.55>,3.6
  <1105405.27, 0.0, 6410297.98>,3.6
  <1105434.6, 0.0, 6410290.57>,3.6
  <1105434.6, 0.0, 6410290.57>,3.6
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=22662548 */
  <1105422.61, 0.0, 6411010.31>,3.0
  <1105422.61, 0.0, 6411010.31>,3.0
  <1105419.38, 0.0, 6411022.09>,3.0
  <1105419.38, 0.0, 6411022.09>,3.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=22662548 */
  <1105422.61, 0.0, 6411010.31>,3.6
  <1105422.61, 0.0, 6411010.31>,3.6
  <1105419.38, 0.0, 6411022.09>,3.6
  <1105419.38, 0.0, 6411022.09>,3.6
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12710980 */
  <1105564.61, 0.0, 6410235.44>,3.0
  <1105564.61, 0.0, 6410235.44>,3.0
  <1105574.24, 0.0, 6410184.97>,3.0
  <1105574.24, 0.0, 6410184.97>,3.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12710980 */
  <1105564.61, 0.0, 6410235.44>,3.6
  <1105564.61, 0.0, 6410235.44>,3.6
  <1105574.24, 0.0, 6410184.97>,3.6
  <1105574.24, 0.0, 6410184.97>,3.6
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12710861 */
  <1105609.48, 0.0, 6410863.39>,1.8
  <1105609.48, 0.0, 6410863.39>,1.8
  <1105564.71, 0.0, 6410852.66>,1.8
  <1105564.71, 0.0, 6410852.66>,1.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12710861 */
  <1105609.48, 0.0, 6410863.39>,2.16
  <1105609.48, 0.0, 6410863.39>,2.16
  <1105564.71, 0.0, 6410852.66>,2.16
  <1105564.71, 0.0, 6410852.66>,2.16
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12710983 */
  <1105595.68, 0.0, 6410230.97>,3.0
  <1105595.68, 0.0, 6410230.97>,3.0
  <1105601.21, 0.0, 6410190.16>,3.0
  <1105601.21, 0.0, 6410190.16>,3.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12710983 */
  <1105595.68, 0.0, 6410230.97>,3.6
  <1105595.68, 0.0, 6410230.97>,3.6
  <1105601.21, 0.0, 6410190.16>,3.6
  <1105601.21, 0.0, 6410190.16>,3.6
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=28412821 */
  <1105704.11, 0.0, 6411195.6>,3.0
  <1105704.11, 0.0, 6411195.6>,3.0
  <1105728.14, 0.0, 6411211>,3.0
  <1105728.14, 0.0, 6411211>,3.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=28412821 */
  <1105704.11, 0.0, 6411195.6>,3.6
  <1105704.11, 0.0, 6411195.6>,3.6
  <1105728.14, 0.0, 6411211>,3.6
  <1105728.14, 0.0, 6411211>,3.6
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=12710977 */
  <1105976.42, 0.0, 6410561.28>,3.0
  <1105976.42, 0.0, 6410561.28>,3.0
  <1105971.45, 0.0, 6410539.05>,3.0
  <1106000.02, 0.0, 6410528.17>,3.0
  <1106009.23, 0.0, 6410553.07>,3.0
  <1106009.23, 0.0, 6410553.07>,3.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=12710977 */
  <1105976.42, 0.0, 6410561.28>,3.6
  <1105976.42, 0.0, 6410561.28>,3.6
  <1105971.45, 0.0, 6410539.05>,3.6
  <1106000.02, 0.0, 6410528.17>,3.6
  <1106009.23, 0.0, 6410553.07>,3.6
  <1106009.23, 0.0, 6410553.07>,3.6
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=43860425 */
  <1106000.02, 0.0, 6410528.17>,3.0
  <1106000.02, 0.0, 6410528.17>,3.0
  <1105973.77, 0.0, 6410455.8>,3.0
  <1105973.77, 0.0, 6410455.8>,3.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=43860425 */
  <1106000.02, 0.0, 6410528.17>,3.6
  <1106000.02, 0.0, 6410528.17>,3.6
  <1105973.77, 0.0, 6410455.8>,3.6
  <1105973.77, 0.0, 6410455.8>,3.6
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12710954 */
  <1105989.15, 0.0, 6410785.18>,3.0
  <1105989.15, 0.0, 6410785.18>,3.0
  <1106031.68, 0.0, 6410816.79>,3.0
  <1106031.68, 0.0, 6410816.79>,3.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12710954 */
  <1105989.15, 0.0, 6410785.18>,3.6
  <1105989.15, 0.0, 6410785.18>,3.6
  <1106031.68, 0.0, 6410816.79>,3.6
  <1106031.68, 0.0, 6410816.79>,3.6
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12710956 */
  <1106003.45, 0.0, 6410767.11>,3.0
  <1106003.45, 0.0, 6410767.11>,3.0
  <1106053.88, 0.0, 6410803.99>,3.0
  <1106053.88, 0.0, 6410803.99>,3.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12710956 */
  <1106003.45, 0.0, 6410767.11>,3.6
  <1106003.45, 0.0, 6410767.11>,3.6
  <1106053.88, 0.0, 6410803.99>,3.6
  <1106053.88, 0.0, 6410803.99>,3.6
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=49264208 */
  <1106029.47, 0.0, 6410262.58>,3.0
  <1106029.47, 0.0, 6410262.58>,3.0
  <1106004.08, 0.0, 6410188.76>,3.0
  <1106004.08, 0.0, 6410188.76>,3.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=49264208 */
  <1106029.47, 0.0, 6410262.58>,3.6
  <1106029.47, 0.0, 6410262.58>,3.6
  <1106004.08, 0.0, 6410188.76>,3.6
  <1106004.08, 0.0, 6410188.76>,3.6
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12710959 */
  <1106021.14, 0.0, 6410747.17>,3.0
  <1106021.14, 0.0, 6410747.17>,3.0
  <1106072.33, 0.0, 6410787.06>,3.0
  <1106072.33, 0.0, 6410787.06>,3.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12710959 */
  <1106021.14, 0.0, 6410747.17>,3.6
  <1106021.14, 0.0, 6410747.17>,3.6
  <1106072.33, 0.0, 6410787.06>,3.6
  <1106072.33, 0.0, 6410787.06>,3.6
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=49264210 */
  <1106043.61, 0.0, 6410257.08>,3.0
  <1106043.61, 0.0, 6410257.08>,3.0
  <1106029.47, 0.0, 6410262.58>,3.0
  <1106047.78, 0.0, 6410315.14>,3.0
  <1106047.78, 0.0, 6410315.14>,3.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=49264210 */
  <1106043.61, 0.0, 6410257.08>,3.6
  <1106043.61, 0.0, 6410257.08>,3.6
  <1106029.47, 0.0, 6410262.58>,3.6
  <1106047.78, 0.0, 6410315.14>,3.6
  <1106047.78, 0.0, 6410315.14>,3.6
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=59243918 */
  <1106072.63, 0.0, 6411152.54>,3.0
  <1106072.63, 0.0, 6411152.54>,3.0
  <1106036.18, 0.0, 6411186.91>,3.0
  <1106031.55, 0.0, 6411190.54>,3.0
  <1106031.55, 0.0, 6411190.54>,3.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=59243918 */
  <1106072.63, 0.0, 6411152.54>,3.6
  <1106072.63, 0.0, 6411152.54>,3.6
  <1106036.18, 0.0, 6411186.91>,3.6
  <1106031.55, 0.0, 6411190.54>,3.6
  <1106031.55, 0.0, 6411190.54>,3.6
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 13,
/* osm_id=26651367 */
  <1105035.02, 0.0, 6411085.99>,2.4
  <1105035.02, 0.0, 6411085.99>,2.4
  <1105064.91, 0.0, 6411070.07>,2.4
  <1105085.03, 0.0, 6411068.62>,2.4
  <1105202.84, 0.0, 6411109.21>,2.4
  <1105233.29, 0.0, 6411119.7>,2.4
  <1105295.17, 0.0, 6411141.02>,2.4
  <1105372.46, 0.0, 6411167.64>,2.4
  <1105419.54, 0.0, 6411187.87>,2.4
  <1105420.45, 0.0, 6411192.41>,2.4
  <1105418.59, 0.0, 6411198.82>,2.4
  <1105416.05, 0.0, 6411207.57>,2.4
  <1105416.05, 0.0, 6411207.57>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 13,
/* osm_id=26651367 */
  <1105035.02, 0.0, 6411085.99>,2.88
  <1105035.02, 0.0, 6411085.99>,2.88
  <1105064.91, 0.0, 6411070.07>,2.88
  <1105085.03, 0.0, 6411068.62>,2.88
  <1105202.84, 0.0, 6411109.21>,2.88
  <1105233.29, 0.0, 6411119.7>,2.88
  <1105295.17, 0.0, 6411141.02>,2.88
  <1105372.46, 0.0, 6411167.64>,2.88
  <1105419.54, 0.0, 6411187.87>,2.88
  <1105420.45, 0.0, 6411192.41>,2.88
  <1105418.59, 0.0, 6411198.82>,2.88
  <1105416.05, 0.0, 6411207.57>,2.88
  <1105416.05, 0.0, 6411207.57>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 9,
/* osm_id=7834767 */
  <1105416.05, 0.0, 6411207.57>,2.4
  <1105416.05, 0.0, 6411207.57>,2.4
  <1105255.39, 0.0, 6411151.02>,2.4
  <1105230.41, 0.0, 6411142.22>,2.4
  <1105211.25, 0.0, 6411135.48>,2.4
  <1105193.66, 0.0, 6411129.29>,2.4
  <1105064.46, 0.0, 6411083.81>,2.4
  <1105035.02, 0.0, 6411085.99>,2.4
  <1105035.02, 0.0, 6411085.99>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 9,
/* osm_id=7834767 */
  <1105416.05, 0.0, 6411207.57>,2.88
  <1105416.05, 0.0, 6411207.57>,2.88
  <1105255.39, 0.0, 6411151.02>,2.88
  <1105230.41, 0.0, 6411142.22>,2.88
  <1105211.25, 0.0, 6411135.48>,2.88
  <1105193.66, 0.0, 6411129.29>,2.88
  <1105064.46, 0.0, 6411083.81>,2.88
  <1105035.02, 0.0, 6411085.99>,2.88
  <1105035.02, 0.0, 6411085.99>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 11,
/* osm_id=33225135 */
  <1105518.89, 0.0, 6411158.09>,4.0
  <1105518.89, 0.0, 6411158.09>,4.0
  <1105486.64, 0.0, 6411154.02>,4.0
  <1105435.53, 0.0, 6411137.31>,4.0
  <1105409.74, 0.0, 6411128.63>,4.0
  <1105384.67, 0.0, 6411119.34>,4.0
  <1105345.44, 0.0, 6411104.7>,4.0
  <1105242.27, 0.0, 6411069.31>,4.0
  <1105186.43, 0.0, 6411053.22>,4.0
  <1105076.45, 0.0, 6411021.16>,4.0
  <1105076.45, 0.0, 6411021.16>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 11,
/* osm_id=33225135 */
  <1105518.89, 0.0, 6411158.09>,4.8
  <1105518.89, 0.0, 6411158.09>,4.8
  <1105486.64, 0.0, 6411154.02>,4.8
  <1105435.53, 0.0, 6411137.31>,4.8
  <1105409.74, 0.0, 6411128.63>,4.8
  <1105384.67, 0.0, 6411119.34>,4.8
  <1105345.44, 0.0, 6411104.7>,4.8
  <1105242.27, 0.0, 6411069.31>,4.8
  <1105186.43, 0.0, 6411053.22>,4.8
  <1105076.45, 0.0, 6411021.16>,4.8
  <1105076.45, 0.0, 6411021.16>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=12711059 */
  <1105090.4, 0.0, 6410968.77>,2.4
  <1105090.4, 0.0, 6410968.77>,2.4
  <1105140.56, 0.0, 6410973.48>,2.4
  <1105204.21, 0.0, 6410989.17>,2.4
  <1105249.91, 0.0, 6410998.76>,2.4
  <1105249.91, 0.0, 6410998.76>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=12711059 */
  <1105090.4, 0.0, 6410968.77>,2.88
  <1105090.4, 0.0, 6410968.77>,2.88
  <1105140.56, 0.0, 6410973.48>,2.88
  <1105204.21, 0.0, 6410989.17>,2.88
  <1105249.91, 0.0, 6410998.76>,2.88
  <1105249.91, 0.0, 6410998.76>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 11,
/* osm_id=13024522 */
  <1105126.8, 0.0, 6410811.24>,4.0
  <1105126.8, 0.0, 6410811.24>,4.0
  <1105151.44, 0.0, 6410814.63>,4.0
  <1105166.03, 0.0, 6410814.29>,4.0
  <1105196.95, 0.0, 6410825.41>,4.0
  <1105215.71, 0.0, 6410824.38>,4.0
  <1105245.24, 0.0, 6410824.72>,4.0
  <1105261.92, 0.0, 6410825.07>,4.0
  <1105273.68, 0.0, 6410818.69>,4.0
  <1105278.94, 0.0, 6410810.34>,4.0
  <1105278.94, 0.0, 6410810.34>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 11,
/* osm_id=13024522 */
  <1105126.8, 0.0, 6410811.24>,4.8
  <1105126.8, 0.0, 6410811.24>,4.8
  <1105151.44, 0.0, 6410814.63>,4.8
  <1105166.03, 0.0, 6410814.29>,4.8
  <1105196.95, 0.0, 6410825.41>,4.8
  <1105215.71, 0.0, 6410824.38>,4.8
  <1105245.24, 0.0, 6410824.72>,4.8
  <1105261.92, 0.0, 6410825.07>,4.8
  <1105273.68, 0.0, 6410818.69>,4.8
  <1105278.94, 0.0, 6410810.34>,4.8
  <1105278.94, 0.0, 6410810.34>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=11299264 */
  <1105139.1, 0.0, 6410236.54>,4.0
  <1105139.1, 0.0, 6410236.54>,4.0
  <1105177.72, 0.0, 6410230.59>,4.0
  <1105187.8, 0.0, 6410233.39>,4.0
  <1105249.2, 0.0, 6410250.41>,4.0
  <1105249.2, 0.0, 6410250.41>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=11299264 */
  <1105139.1, 0.0, 6410236.54>,4.8
  <1105139.1, 0.0, 6410236.54>,4.8
  <1105177.72, 0.0, 6410230.59>,4.8
  <1105187.8, 0.0, 6410233.39>,4.8
  <1105249.2, 0.0, 6410250.41>,4.8
  <1105249.2, 0.0, 6410250.41>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 8,
/* osm_id=13029694 */
  <1105196.95, 0.0, 6410825.41>,4.0
  <1105196.95, 0.0, 6410825.41>,4.0
  <1105194.86, 0.0, 6410868.85>,4.0
  <1105219.18, 0.0, 6410878.58>,4.0
  <1105227.88, 0.0, 6410884.48>,4.0
  <1105237.61, 0.0, 6410901.51>,4.0
  <1105280.49, 0.0, 6410924.69>,4.0
  <1105280.49, 0.0, 6410924.69>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 8,
/* osm_id=13029694 */
  <1105196.95, 0.0, 6410825.41>,4.8
  <1105196.95, 0.0, 6410825.41>,4.8
  <1105194.86, 0.0, 6410868.85>,4.8
  <1105219.18, 0.0, 6410878.58>,4.8
  <1105227.88, 0.0, 6410884.48>,4.8
  <1105237.61, 0.0, 6410901.51>,4.8
  <1105280.49, 0.0, 6410924.69>,4.8
  <1105280.49, 0.0, 6410924.69>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=12711052 */
  <1105233.29, 0.0, 6411119.7>,4.0
  <1105233.29, 0.0, 6411119.7>,4.0
  <1105242.27, 0.0, 6411069.31>,4.0
  <1105249.91, 0.0, 6410998.76>,4.0
  <1105265.78, 0.0, 6410974.06>,4.0
  <1105265.78, 0.0, 6410974.06>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=12711052 */
  <1105233.29, 0.0, 6411119.7>,4.8
  <1105233.29, 0.0, 6411119.7>,4.8
  <1105242.27, 0.0, 6411069.31>,4.8
  <1105249.91, 0.0, 6410998.76>,4.8
  <1105265.78, 0.0, 6410974.06>,4.8
  <1105265.78, 0.0, 6410974.06>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=13024534 */
  <1105255.39, 0.0, 6411151.02>,4.0
  <1105255.39, 0.0, 6411151.02>,4.0
  <1105245.16, 0.0, 6411178.08>,4.0
  <1105242.35, 0.0, 6411225.16>,4.0
  <1105242.35, 0.0, 6411225.16>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=13024534 */
  <1105255.39, 0.0, 6411151.02>,4.8
  <1105255.39, 0.0, 6411151.02>,4.8
  <1105245.16, 0.0, 6411178.08>,4.8
  <1105242.35, 0.0, 6411225.16>,4.8
  <1105242.35, 0.0, 6411225.16>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 12,
/* osm_id=28870148 */
  <1105249.15, 0.0, 6410451.9>,4.0
  <1105249.15, 0.0, 6410451.9>,4.0
  <1105252.57, 0.0, 6410431.62>,4.0
  <1105252.78, 0.0, 6410407.36>,4.0
  <1105252.75, 0.0, 6410391.77>,4.0
  <1105252.7, 0.0, 6410368.49>,4.0
  <1105246.25, 0.0, 6410291.57>,4.0
  <1105249.2, 0.0, 6410250.41>,4.0
  <1105270.08, 0.0, 6410183.35>,4.0
  <1105285.64, 0.0, 6410128.62>,4.0
  <1105293.17, 0.0, 6410096.82>,4.0
  <1105293.17, 0.0, 6410096.82>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 12,
/* osm_id=28870148 */
  <1105249.15, 0.0, 6410451.9>,4.8
  <1105249.15, 0.0, 6410451.9>,4.8
  <1105252.57, 0.0, 6410431.62>,4.8
  <1105252.78, 0.0, 6410407.36>,4.8
  <1105252.75, 0.0, 6410391.77>,4.8
  <1105252.7, 0.0, 6410368.49>,4.8
  <1105246.25, 0.0, 6410291.57>,4.8
  <1105249.2, 0.0, 6410250.41>,4.8
  <1105270.08, 0.0, 6410183.35>,4.8
  <1105285.64, 0.0, 6410128.62>,4.8
  <1105293.17, 0.0, 6410096.82>,4.8
  <1105293.17, 0.0, 6410096.82>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=11299277 */
  <1105246.25, 0.0, 6410291.57>,4.0
  <1105246.25, 0.0, 6410291.57>,4.0
  <1105256.85, 0.0, 6410290.57>,4.0
  <1105320.75, 0.0, 6410286.31>,4.0
  <1105320.75, 0.0, 6410286.31>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=11299277 */
  <1105246.25, 0.0, 6410291.57>,4.8
  <1105246.25, 0.0, 6410291.57>,4.8
  <1105256.85, 0.0, 6410290.57>,4.8
  <1105320.75, 0.0, 6410286.31>,4.8
  <1105320.75, 0.0, 6410286.31>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=11299270 */
  <1105252.57, 0.0, 6410431.62>,4.0
  <1105252.57, 0.0, 6410431.62>,4.0
  <1105274.82, 0.0, 6410415.84>,4.0
  <1105317.88, 0.0, 6410390.36>,4.0
  <1105335.38, 0.0, 6410379.68>,4.0
  <1105335.38, 0.0, 6410379.68>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=11299270 */
  <1105252.57, 0.0, 6410431.62>,4.8
  <1105252.57, 0.0, 6410431.62>,4.8
  <1105274.82, 0.0, 6410415.84>,4.8
  <1105317.88, 0.0, 6410390.36>,4.8
  <1105335.38, 0.0, 6410379.68>,4.8
  <1105335.38, 0.0, 6410379.68>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12711056 */
  <1105265.78, 0.0, 6410974.06>,4.0
  <1105265.78, 0.0, 6410974.06>,4.0
  <1105280.49, 0.0, 6410924.69>,4.0
  <1105280.49, 0.0, 6410924.69>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12711056 */
  <1105265.78, 0.0, 6410974.06>,4.8
  <1105265.78, 0.0, 6410974.06>,4.8
  <1105280.49, 0.0, 6410924.69>,4.8
  <1105280.49, 0.0, 6410924.69>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 10,
/* osm_id=4568529 */
  <1105265.78, 0.0, 6410974.06>,4.0
  <1105265.78, 0.0, 6410974.06>,4.0
  <1105329.87, 0.0, 6410984.07>,4.0
  <1105369.84, 0.0, 6410994.64>,4.0
  <1105383.56, 0.0, 6410998.05>,4.0
  <1105422.61, 0.0, 6411010.31>,4.0
  <1105461.01, 0.0, 6411021.92>,4.0
  <1105485.47, 0.0, 6411028.37>,4.0
  <1105529.85, 0.0, 6411030.06>,4.0
  <1105529.85, 0.0, 6411030.06>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 10,
/* osm_id=4568529 */
  <1105265.78, 0.0, 6410974.06>,4.8
  <1105265.78, 0.0, 6410974.06>,4.8
  <1105329.87, 0.0, 6410984.07>,4.8
  <1105369.84, 0.0, 6410994.64>,4.8
  <1105383.56, 0.0, 6410998.05>,4.8
  <1105422.61, 0.0, 6411010.31>,4.8
  <1105461.01, 0.0, 6411021.92>,4.8
  <1105485.47, 0.0, 6411028.37>,4.8
  <1105529.85, 0.0, 6411030.06>,4.8
  <1105529.85, 0.0, 6411030.06>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 9,
/* osm_id=31824134 */
  <1105331.33, 0.0, 6411538.55>,4.0
  <1105331.33, 0.0, 6411538.55>,4.0
  <1105336.05, 0.0, 6411521.35>,4.0
  <1105350.65, 0.0, 6411467.43>,4.0
  <1105354.23, 0.0, 6411452.65>,4.0
  <1105407.7, 0.0, 6411239.19>,4.0
  <1105413.49, 0.0, 6411218.14>,4.0
  <1105416.05, 0.0, 6411207.57>,4.0
  <1105416.05, 0.0, 6411207.57>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 9,
/* osm_id=31824134 */
  <1105331.33, 0.0, 6411538.55>,4.8
  <1105331.33, 0.0, 6411538.55>,4.8
  <1105336.05, 0.0, 6411521.35>,4.8
  <1105350.65, 0.0, 6411467.43>,4.8
  <1105354.23, 0.0, 6411452.65>,4.8
  <1105407.7, 0.0, 6411239.19>,4.8
  <1105413.49, 0.0, 6411218.14>,4.8
  <1105416.05, 0.0, 6411207.57>,4.8
  <1105416.05, 0.0, 6411207.57>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=58533363 */
  <1105335.38, 0.0, 6410379.68>,4.0
  <1105335.38, 0.0, 6410379.68>,4.0
  <1105370.27, 0.0, 6410356.63>,4.0
  <1105386.52, 0.0, 6410344.31>,4.0
  <1105386.52, 0.0, 6410344.31>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=58533363 */
  <1105335.38, 0.0, 6410379.68>,4.8
  <1105335.38, 0.0, 6410379.68>,4.8
  <1105370.27, 0.0, 6410356.63>,4.8
  <1105386.52, 0.0, 6410344.31>,4.8
  <1105386.52, 0.0, 6410344.31>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=58534048 */
  <1105350.53, 0.0, 6410294.55>,2.4
  <1105350.53, 0.0, 6410294.55>,2.4
  <1105361.64, 0.0, 6410268.8>,2.4
  <1105361.64, 0.0, 6410268.8>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=58534048 */
  <1105350.53, 0.0, 6410294.55>,2.88
  <1105350.53, 0.0, 6410294.55>,2.88
  <1105361.64, 0.0, 6410268.8>,2.88
  <1105361.64, 0.0, 6410268.8>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=58534047 */
  <1105361.64, 0.0, 6410268.8>,2.4
  <1105361.64, 0.0, 6410268.8>,2.4
  <1105378.2, 0.0, 6410230.42>,2.4
  <1105378.2, 0.0, 6410230.42>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=58534047 */
  <1105361.64, 0.0, 6410268.8>,2.88
  <1105361.64, 0.0, 6410268.8>,2.88
  <1105378.2, 0.0, 6410230.42>,2.88
  <1105378.2, 0.0, 6410230.42>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=58534045 */
  <1105378.2, 0.0, 6410230.42>,2.4
  <1105378.2, 0.0, 6410230.42>,2.4
  <1105383.68, 0.0, 6410217.73>,2.4
  <1105383.68, 0.0, 6410217.73>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=58534045 */
  <1105378.2, 0.0, 6410230.42>,2.88
  <1105378.2, 0.0, 6410230.42>,2.88
  <1105383.68, 0.0, 6410217.73>,2.88
  <1105383.68, 0.0, 6410217.73>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=58534044 */
  <1105383.68, 0.0, 6410217.73>,2.4
  <1105383.68, 0.0, 6410217.73>,2.4
  <1105391.38, 0.0, 6410172.29>,2.4
  <1105409.23, 0.0, 6410122.82>,2.4
  <1105421.85, 0.0, 6410037.76>,2.4
  <1105421.85, 0.0, 6410037.76>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=58534044 */
  <1105383.68, 0.0, 6410217.73>,2.88
  <1105383.68, 0.0, 6410217.73>,2.88
  <1105391.38, 0.0, 6410172.29>,2.88
  <1105409.23, 0.0, 6410122.82>,2.88
  <1105421.85, 0.0, 6410037.76>,2.88
  <1105421.85, 0.0, 6410037.76>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=11299282 */
  <1105451.82, 0.0, 6410230.89>,4.0
  <1105451.82, 0.0, 6410230.89>,4.0
  <1105383.68, 0.0, 6410217.73>,4.0
  <1105383.68, 0.0, 6410217.73>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=11299282 */
  <1105451.82, 0.0, 6410230.89>,4.8
  <1105451.82, 0.0, 6410230.89>,4.8
  <1105383.68, 0.0, 6410217.73>,4.8
  <1105383.68, 0.0, 6410217.73>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=31903542 */
  <1105384.67, 0.0, 6411119.34>,4.0
  <1105384.67, 0.0, 6411119.34>,4.0
  <1105398.68, 0.0, 6411081.33>,4.0
  <1105398.68, 0.0, 6411081.33>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=31903542 */
  <1105384.67, 0.0, 6411119.34>,4.8
  <1105384.67, 0.0, 6411119.34>,4.8
  <1105398.68, 0.0, 6411081.33>,4.8
  <1105398.68, 0.0, 6411081.33>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=58533362 */
  <1105386.52, 0.0, 6410344.31>,4.0
  <1105386.52, 0.0, 6410344.31>,4.0
  <1105411.07, 0.0, 6410325.55>,4.0
  <1105432.15, 0.0, 6410309.45>,4.0
  <1105432.15, 0.0, 6410309.45>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=58533362 */
  <1105386.52, 0.0, 6410344.31>,4.8
  <1105386.52, 0.0, 6410344.31>,4.8
  <1105411.07, 0.0, 6410325.55>,4.8
  <1105432.15, 0.0, 6410309.45>,4.8
  <1105432.15, 0.0, 6410309.45>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=4473086 */
  <1105432.15, 0.0, 6410309.45>,4.0
  <1105432.15, 0.0, 6410309.45>,4.0
  <1105434.6, 0.0, 6410290.57>,4.0
  <1105436.22, 0.0, 6410278.03>,4.0
  <1105451.82, 0.0, 6410230.89>,4.0
  <1105451.82, 0.0, 6410230.89>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=4473086 */
  <1105432.15, 0.0, 6410309.45>,4.8
  <1105432.15, 0.0, 6410309.45>,4.8
  <1105434.6, 0.0, 6410290.57>,4.8
  <1105436.22, 0.0, 6410278.03>,4.8
  <1105451.82, 0.0, 6410230.89>,4.8
  <1105451.82, 0.0, 6410230.89>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=11299268 */
  <1105602.43, 0.0, 6410231.11>,4.0
  <1105602.43, 0.0, 6410231.11>,4.0
  <1105595.68, 0.0, 6410230.97>,4.0
  <1105564.61, 0.0, 6410235.44>,4.0
  <1105451.82, 0.0, 6410230.89>,4.0
  <1105451.82, 0.0, 6410230.89>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=11299268 */
  <1105602.43, 0.0, 6410231.11>,4.8
  <1105602.43, 0.0, 6410231.11>,4.8
  <1105595.68, 0.0, 6410230.97>,4.8
  <1105564.61, 0.0, 6410235.44>,4.8
  <1105451.82, 0.0, 6410230.89>,4.8
  <1105451.82, 0.0, 6410230.89>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=4473085 */
  <1105540.86, 0.0, 6410436.35>,4.0
  <1105540.86, 0.0, 6410436.35>,4.0
  <1105478.37, 0.0, 6410424.53>,4.0
  <1105478.37, 0.0, 6410424.53>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=4473085 */
  <1105540.86, 0.0, 6410436.35>,4.8
  <1105540.86, 0.0, 6410436.35>,4.8
  <1105478.37, 0.0, 6410424.53>,4.8
  <1105478.37, 0.0, 6410424.53>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=27137247 */
  <1105620.22, 0.0, 6410762.83>,4.0
  <1105620.22, 0.0, 6410762.83>,4.0
  <1105561.21, 0.0, 6410760.14>,4.0
  <1105561.21, 0.0, 6410760.14>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=27137247 */
  <1105620.22, 0.0, 6410762.83>,4.8
  <1105620.22, 0.0, 6410762.83>,4.8
  <1105561.21, 0.0, 6410760.14>,4.8
  <1105561.21, 0.0, 6410760.14>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=33225132 */
  <1105575.96, 0.0, 6411171.19>,4.0
  <1105575.96, 0.0, 6411171.19>,4.0
  <1105694.18, 0.0, 6411214.55>,4.0
  <1105721.94, 0.0, 6411229.73>,4.0
  <1105771.42, 0.0, 6411207.76>,4.0
  <1105771.42, 0.0, 6411207.76>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=33225132 */
  <1105575.96, 0.0, 6411171.19>,4.8
  <1105575.96, 0.0, 6411171.19>,4.8
  <1105694.18, 0.0, 6411214.55>,4.8
  <1105721.94, 0.0, 6411229.73>,4.8
  <1105771.42, 0.0, 6411207.76>,4.8
  <1105771.42, 0.0, 6411207.76>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=30278671 */
  <1105740.7, 0.0, 6411011.4>,4.0
  <1105740.7, 0.0, 6411011.4>,4.0
  <1105657.69, 0.0, 6411010.76>,4.0
  <1105583.64, 0.0, 6411010.18>,4.0
  <1105583.64, 0.0, 6411010.18>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=30278671 */
  <1105740.7, 0.0, 6411011.4>,4.8
  <1105740.7, 0.0, 6411011.4>,4.8
  <1105657.69, 0.0, 6411010.76>,4.8
  <1105583.64, 0.0, 6411010.18>,4.8
  <1105583.64, 0.0, 6411010.18>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=37299754 */
  <1105594.11, 0.0, 6410415.39>,4.0
  <1105594.11, 0.0, 6410415.39>,4.0
  <1105593.93, 0.0, 6410483.21>,4.0
  <1105593.93, 0.0, 6410483.21>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=37299754 */
  <1105594.11, 0.0, 6410415.39>,4.8
  <1105594.11, 0.0, 6410415.39>,4.8
  <1105593.93, 0.0, 6410483.21>,4.8
  <1105593.93, 0.0, 6410483.21>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=57837181 */
  <1105600.14, 0.0, 6410298.39>,4.0
  <1105600.14, 0.0, 6410298.39>,4.0
  <1105628.05, 0.0, 6410380.98>,4.0
  <1105629.66, 0.0, 6410400.27>,4.0
  <1105629.66, 0.0, 6410400.27>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=57837181 */
  <1105600.14, 0.0, 6410298.39>,4.8
  <1105600.14, 0.0, 6410298.39>,4.8
  <1105628.05, 0.0, 6410380.98>,4.8
  <1105629.66, 0.0, 6410400.27>,4.8
  <1105629.66, 0.0, 6410400.27>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=10213067 */
  <1105619.89, 0.0, 6410195.38>,4.0
  <1105619.89, 0.0, 6410195.38>,4.0
  <1105613.08, 0.0, 6410209.16>,4.0
  <1105602.43, 0.0, 6410231.11>,4.0
  <1105602.43, 0.0, 6410231.11>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=10213067 */
  <1105619.89, 0.0, 6410195.38>,4.8
  <1105619.89, 0.0, 6410195.38>,4.8
  <1105613.08, 0.0, 6410209.16>,4.8
  <1105602.43, 0.0, 6410231.11>,4.8
  <1105602.43, 0.0, 6410231.11>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=4587366 */
  <1105640.57, 0.0, 6410950.82>,2.4
  <1105640.57, 0.0, 6410950.82>,2.4
  <1105612.41, 0.0, 6410869.42>,2.4
  <1105609.48, 0.0, 6410863.39>,2.4
  <1105609.48, 0.0, 6410863.39>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=4587366 */
  <1105640.57, 0.0, 6410950.82>,2.88
  <1105640.57, 0.0, 6410950.82>,2.88
  <1105612.41, 0.0, 6410869.42>,2.88
  <1105609.48, 0.0, 6410863.39>,2.88
  <1105609.48, 0.0, 6410863.39>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=51195167 */
  <1105668.66, 0.0, 6410880.86>,2.4
  <1105668.66, 0.0, 6410880.86>,2.4
  <1105609.48, 0.0, 6410863.39>,2.4
  <1105609.48, 0.0, 6410863.39>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=51195167 */
  <1105668.66, 0.0, 6410880.86>,2.88
  <1105668.66, 0.0, 6410880.86>,2.88
  <1105609.48, 0.0, 6410863.39>,2.88
  <1105609.48, 0.0, 6410863.39>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 13,
/* osm_id=10213066 */
  <1106006.07, 0.0, 6410099.09>,2.4
  <1106006.07, 0.0, 6410099.09>,2.4
  <1105959.21, 0.0, 6410097.78>,2.4
  <1105914.34, 0.0, 6410099.66>,2.4
  <1105898.63, 0.0, 6410104.53>,2.4
  <1105876.94, 0.0, 6410121.36>,2.4
  <1105845.14, 0.0, 6410138.93>,2.4
  <1105817.69, 0.0, 6410149.93>,2.4
  <1105739.68, 0.0, 6410186.81>,2.4
  <1105716.11, 0.0, 6410193.16>,2.4
  <1105673.1, 0.0, 6410196.16>,2.4
  <1105619.89, 0.0, 6410195.38>,2.4
  <1105619.89, 0.0, 6410195.38>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 13,
/* osm_id=10213066 */
  <1106006.07, 0.0, 6410099.09>,2.88
  <1106006.07, 0.0, 6410099.09>,2.88
  <1105959.21, 0.0, 6410097.78>,2.88
  <1105914.34, 0.0, 6410099.66>,2.88
  <1105898.63, 0.0, 6410104.53>,2.88
  <1105876.94, 0.0, 6410121.36>,2.88
  <1105845.14, 0.0, 6410138.93>,2.88
  <1105817.69, 0.0, 6410149.93>,2.88
  <1105739.68, 0.0, 6410186.81>,2.88
  <1105716.11, 0.0, 6410193.16>,2.88
  <1105673.1, 0.0, 6410196.16>,2.88
  <1105619.89, 0.0, 6410195.38>,2.88
  <1105619.89, 0.0, 6410195.38>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=54266110 */
  <1105641.78, 0.0, 6410410.89>,4.0
  <1105641.78, 0.0, 6410410.89>,4.0
  <1105629.66, 0.0, 6410400.27>,4.0
  <1105629.66, 0.0, 6410400.27>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=54266110 */
  <1105641.78, 0.0, 6410410.89>,4.8
  <1105641.78, 0.0, 6410410.89>,4.8
  <1105629.66, 0.0, 6410400.27>,4.8
  <1105629.66, 0.0, 6410400.27>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=57837180 */
  <1105685.53, 0.0, 6410439.66>,4.0
  <1105685.53, 0.0, 6410439.66>,4.0
  <1105641.78, 0.0, 6410410.89>,4.0
  <1105641.78, 0.0, 6410410.89>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=57837180 */
  <1105685.53, 0.0, 6410439.66>,4.8
  <1105685.53, 0.0, 6410439.66>,4.8
  <1105641.78, 0.0, 6410410.89>,4.8
  <1105641.78, 0.0, 6410410.89>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=28458791 */
  <1105683.8, 0.0, 6410650.26>,4.0
  <1105683.8, 0.0, 6410650.26>,4.0
  <1105712.7, 0.0, 6410635.41>,4.0
  <1105775.02, 0.0, 6410606.13>,4.0
  <1105775.02, 0.0, 6410606.13>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=28458791 */
  <1105683.8, 0.0, 6410650.26>,4.8
  <1105683.8, 0.0, 6410650.26>,4.8
  <1105712.7, 0.0, 6410635.41>,4.8
  <1105775.02, 0.0, 6410606.13>,4.8
  <1105775.02, 0.0, 6410606.13>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=10213069 */
  <1105717.67, 0.0, 6410468.3>,4.0
  <1105717.67, 0.0, 6410468.3>,4.0
  <1105685.53, 0.0, 6410439.66>,4.0
  <1105685.53, 0.0, 6410439.66>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=10213069 */
  <1105717.67, 0.0, 6410468.3>,4.8
  <1105717.67, 0.0, 6410468.3>,4.8
  <1105685.53, 0.0, 6410439.66>,4.8
  <1105685.53, 0.0, 6410439.66>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 7,
/* osm_id=12710999 */
  <1105694.18, 0.0, 6411214.55>,4.0
  <1105694.18, 0.0, 6411214.55>,4.0
  <1105704.11, 0.0, 6411195.6>,4.0
  <1105746.74, 0.0, 6411127.41>,4.0
  <1105776.29, 0.0, 6411065.86>,4.0
  <1105780.22, 0.0, 6411045.1>,4.0
  <1105780.22, 0.0, 6411045.1>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 7,
/* osm_id=12710999 */
  <1105694.18, 0.0, 6411214.55>,4.8
  <1105694.18, 0.0, 6411214.55>,4.8
  <1105704.11, 0.0, 6411195.6>,4.8
  <1105746.74, 0.0, 6411127.41>,4.8
  <1105776.29, 0.0, 6411065.86>,4.8
  <1105780.22, 0.0, 6411045.1>,4.8
  <1105780.22, 0.0, 6411045.1>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=23034045 */
  <1105736.05, 0.0, 6410681.7>,4.0
  <1105736.05, 0.0, 6410681.7>,4.0
  <1105712.7, 0.0, 6410635.41>,4.0
  <1105712.7, 0.0, 6410635.41>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=23034045 */
  <1105736.05, 0.0, 6410681.7>,4.8
  <1105736.05, 0.0, 6410681.7>,4.8
  <1105712.7, 0.0, 6410635.41>,4.8
  <1105712.7, 0.0, 6410635.41>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=7834769 */
  <1105717.07, 0.0, 6410474.87>,4.0
  <1105717.07, 0.0, 6410474.87>,4.0
  <1105717.67, 0.0, 6410468.3>,4.0
  <1105717.67, 0.0, 6410468.3>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=7834769 */
  <1105717.07, 0.0, 6410474.87>,4.8
  <1105717.07, 0.0, 6410474.87>,4.8
  <1105717.67, 0.0, 6410468.3>,4.8
  <1105717.67, 0.0, 6410468.3>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 7,
/* osm_id=12710975 */
  <1105725.1, 0.0, 6410472.25>,2.4
  <1105725.1, 0.0, 6410472.25>,2.4
  <1105771.43, 0.0, 6410434.52>,2.4
  <1105819.9, 0.0, 6410400.72>,2.4
  <1105843.52, 0.0, 6410394.62>,2.4
  <1105911.19, 0.0, 6410393.34>,2.4
  <1105911.19, 0.0, 6410393.34>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 7,
/* osm_id=12710975 */
  <1105725.1, 0.0, 6410472.25>,2.88
  <1105725.1, 0.0, 6410472.25>,2.88
  <1105771.43, 0.0, 6410434.52>,2.88
  <1105819.9, 0.0, 6410400.72>,2.88
  <1105843.52, 0.0, 6410394.62>,2.88
  <1105911.19, 0.0, 6410393.34>,2.88
  <1105911.19, 0.0, 6410393.34>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12711006 */
  <1105791.98, 0.0, 6410933.3>,4.0
  <1105791.98, 0.0, 6410933.3>,4.0
  <1105740.7, 0.0, 6411011.4>,4.0
  <1105740.7, 0.0, 6411011.4>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12711006 */
  <1105791.98, 0.0, 6410933.3>,4.8
  <1105791.98, 0.0, 6410933.3>,4.8
  <1105740.7, 0.0, 6411011.4>,4.8
  <1105740.7, 0.0, 6411011.4>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 8,
/* osm_id=12711009 */
  <1105830.64, 0.0, 6411064.45>,4.0
  <1105830.64, 0.0, 6411064.45>,4.0
  <1105801.49, 0.0, 6411051.93>,4.0
  <1105780.22, 0.0, 6411045.1>,4.0
  <1105771.82, 0.0, 6411031.65>,4.0
  <1105754.58, 0.0, 6411018.09>,4.0
  <1105740.7, 0.0, 6411011.4>,4.0
  <1105740.7, 0.0, 6411011.4>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 8,
/* osm_id=12711009 */
  <1105830.64, 0.0, 6411064.45>,4.8
  <1105830.64, 0.0, 6411064.45>,4.8
  <1105801.49, 0.0, 6411051.93>,4.8
  <1105780.22, 0.0, 6411045.1>,4.8
  <1105771.82, 0.0, 6411031.65>,4.8
  <1105754.58, 0.0, 6411018.09>,4.8
  <1105740.7, 0.0, 6411011.4>,4.8
  <1105740.7, 0.0, 6411011.4>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12711004 */
  <1105746.74, 0.0, 6411127.41>,4.0
  <1105746.74, 0.0, 6411127.41>,4.0
  <1105794.77, 0.0, 6411146.34>,4.0
  <1105794.77, 0.0, 6411146.34>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12711004 */
  <1105746.74, 0.0, 6411127.41>,4.8
  <1105746.74, 0.0, 6411127.41>,4.8
  <1105794.77, 0.0, 6411146.34>,4.8
  <1105794.77, 0.0, 6411146.34>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=6110030 */
  <1105757.47, 0.0, 6410909.53>,4.0
  <1105757.47, 0.0, 6410909.53>,4.0
  <1105786.92, 0.0, 6410905.14>,4.0
  <1105786.92, 0.0, 6410905.14>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=6110030 */
  <1105757.47, 0.0, 6410909.53>,4.8
  <1105757.47, 0.0, 6410909.53>,4.8
  <1105786.92, 0.0, 6410905.14>,4.8
  <1105786.92, 0.0, 6410905.14>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=28808701 */
  <1105791.98, 0.0, 6410933.3>,4.0
  <1105791.98, 0.0, 6410933.3>,4.0
  <1105757.47, 0.0, 6410909.53>,4.0
  <1105757.47, 0.0, 6410909.53>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=28808701 */
  <1105791.98, 0.0, 6410933.3>,4.8
  <1105791.98, 0.0, 6410933.3>,4.8
  <1105757.47, 0.0, 6410909.53>,4.8
  <1105757.47, 0.0, 6410909.53>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=32703964 */
  <1105811.23, 0.0, 6410760.8>,4.0
  <1105811.23, 0.0, 6410760.8>,4.0
  <1105798.29, 0.0, 6410753.47>,4.0
  <1105761.21, 0.0, 6410728.03>,4.0
  <1105761.21, 0.0, 6410728.03>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=32703964 */
  <1105811.23, 0.0, 6410760.8>,4.8
  <1105811.23, 0.0, 6410760.8>,4.8
  <1105798.29, 0.0, 6410753.47>,4.8
  <1105761.21, 0.0, 6410728.03>,4.8
  <1105761.21, 0.0, 6410728.03>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 7,
/* osm_id=28458790 */
  <1105830.64, 0.0, 6411064.45>,4.0
  <1105830.64, 0.0, 6411064.45>,4.0
  <1105794.77, 0.0, 6411146.34>,4.0
  <1105784.38, 0.0, 6411181.02>,4.0
  <1105775.54, 0.0, 6411199.44>,4.0
  <1105771.42, 0.0, 6411207.76>,4.0
  <1105771.42, 0.0, 6411207.76>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 7,
/* osm_id=28458790 */
  <1105830.64, 0.0, 6411064.45>,4.8
  <1105830.64, 0.0, 6411064.45>,4.8
  <1105794.77, 0.0, 6411146.34>,4.8
  <1105784.38, 0.0, 6411181.02>,4.8
  <1105775.54, 0.0, 6411199.44>,4.8
  <1105771.42, 0.0, 6411207.76>,4.8
  <1105771.42, 0.0, 6411207.76>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=9035405 */
  <1105837.75, 0.0, 6410596.18>,4.0
  <1105837.75, 0.0, 6410596.18>,4.0
  <1105795.25, 0.0, 6410595.92>,4.0
  <1105775.02, 0.0, 6410606.13>,4.0
  <1105775.02, 0.0, 6410606.13>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=9035405 */
  <1105837.75, 0.0, 6410596.18>,4.8
  <1105837.75, 0.0, 6410596.18>,4.8
  <1105795.25, 0.0, 6410595.92>,4.8
  <1105775.02, 0.0, 6410606.13>,4.8
  <1105775.02, 0.0, 6410606.13>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 8,
/* osm_id=28808703 */
  <1105786.92, 0.0, 6410905.14>,4.0
  <1105786.92, 0.0, 6410905.14>,4.0
  <1105802.53, 0.0, 6410897.48>,4.0
  <1105827.2, 0.0, 6410886.17>,4.0
  <1105851.62, 0.0, 6410874.96>,4.0
  <1105890.14, 0.0, 6410852.21>,4.0
  <1105945.58, 0.0, 6410819.29>,4.0
  <1105945.58, 0.0, 6410819.29>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 8,
/* osm_id=28808703 */
  <1105786.92, 0.0, 6410905.14>,4.8
  <1105786.92, 0.0, 6410905.14>,4.8
  <1105802.53, 0.0, 6410897.48>,4.8
  <1105827.2, 0.0, 6410886.17>,4.8
  <1105851.62, 0.0, 6410874.96>,4.8
  <1105890.14, 0.0, 6410852.21>,4.8
  <1105945.58, 0.0, 6410819.29>,4.8
  <1105945.58, 0.0, 6410819.29>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=28808702 */
  <1105786.92, 0.0, 6410905.14>,4.0
  <1105786.92, 0.0, 6410905.14>,4.0
  <1105791.98, 0.0, 6410933.3>,4.0
  <1105791.98, 0.0, 6410933.3>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=28808702 */
  <1105786.92, 0.0, 6410905.14>,4.8
  <1105786.92, 0.0, 6410905.14>,4.8
  <1105791.98, 0.0, 6410933.3>,4.8
  <1105791.98, 0.0, 6410933.3>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=28808700 */
  <1105791.98, 0.0, 6410933.3>,4.0
  <1105791.98, 0.0, 6410933.3>,4.0
  <1105854.96, 0.0, 6410972.65>,4.0
  <1105876.06, 0.0, 6410991.17>,4.0
  <1105884.85, 0.0, 6410997.4>,4.0
  <1105884.85, 0.0, 6410997.4>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=28808700 */
  <1105791.98, 0.0, 6410933.3>,4.8
  <1105791.98, 0.0, 6410933.3>,4.8
  <1105854.96, 0.0, 6410972.65>,4.8
  <1105876.06, 0.0, 6410991.17>,4.8
  <1105884.85, 0.0, 6410997.4>,4.8
  <1105884.85, 0.0, 6410997.4>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=6110032 */
  <1105808.61, 0.0, 6411276.44>,2.4
  <1105808.61, 0.0, 6411276.44>,2.4
  <1105819.24, 0.0, 6411245.95>,2.4
  <1105823.91, 0.0, 6411232.59>,2.4
  <1105925.85, 0.0, 6411033.61>,2.4
  <1105925.85, 0.0, 6411033.61>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=6110032 */
  <1105808.61, 0.0, 6411276.44>,2.88
  <1105808.61, 0.0, 6411276.44>,2.88
  <1105819.24, 0.0, 6411245.95>,2.88
  <1105823.91, 0.0, 6411232.59>,2.88
  <1105925.85, 0.0, 6411033.61>,2.88
  <1105925.85, 0.0, 6411033.61>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12711001 */
  <1105830.64, 0.0, 6411064.45>,2.4
  <1105830.64, 0.0, 6411064.45>,2.4
  <1105876.06, 0.0, 6410991.17>,2.4
  <1105876.06, 0.0, 6410991.17>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12711001 */
  <1105830.64, 0.0, 6411064.45>,2.88
  <1105830.64, 0.0, 6411064.45>,2.88
  <1105876.06, 0.0, 6410991.17>,2.88
  <1105876.06, 0.0, 6410991.17>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 9,
/* osm_id=9035403 */
  <1105837.75, 0.0, 6410596.18>,4.0
  <1105837.75, 0.0, 6410596.18>,4.0
  <1105845.73, 0.0, 6410593.78>,4.0
  <1105850, 0.0, 6410592.73>,4.0
  <1105855.23, 0.0, 6410590.66>,4.0
  <1105865.08, 0.0, 6410587.96>,4.0
  <1105885.29, 0.0, 6410581.44>,4.0
  <1105927.25, 0.0, 6410568.18>,4.0
  <1105927.25, 0.0, 6410568.18>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 9,
/* osm_id=9035403 */
  <1105837.75, 0.0, 6410596.18>,4.8
  <1105837.75, 0.0, 6410596.18>,4.8
  <1105845.73, 0.0, 6410593.78>,4.8
  <1105850, 0.0, 6410592.73>,4.8
  <1105855.23, 0.0, 6410590.66>,4.8
  <1105865.08, 0.0, 6410587.96>,4.8
  <1105885.29, 0.0, 6410581.44>,4.8
  <1105927.25, 0.0, 6410568.18>,4.8
  <1105927.25, 0.0, 6410568.18>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=8106716 */
  <1105837.75, 0.0, 6410596.18>,4.0
  <1105837.75, 0.0, 6410596.18>,4.0
  <1105844.07, 0.0, 6410608.92>,4.0
  <1105872.36, 0.0, 6410668.73>,4.0
  <1105871.23, 0.0, 6410713.29>,4.0
  <1105871.23, 0.0, 6410713.29>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=8106716 */
  <1105837.75, 0.0, 6410596.18>,4.8
  <1105837.75, 0.0, 6410596.18>,4.8
  <1105844.07, 0.0, 6410608.92>,4.8
  <1105872.36, 0.0, 6410668.73>,4.8
  <1105871.23, 0.0, 6410713.29>,4.8
  <1105871.23, 0.0, 6410713.29>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 8,
/* osm_id=10213072 */
  <1105871.23, 0.0, 6410713.29>,4.0
  <1105871.23, 0.0, 6410713.29>,4.0
  <1105874.73, 0.0, 6410726.05>,4.0
  <1105887.88, 0.0, 6410744.93>,4.0
  <1105916.16, 0.0, 6410781.53>,4.0
  <1105936.88, 0.0, 6410806.89>,4.0
  <1105945.58, 0.0, 6410819.29>,4.0
  <1105945.58, 0.0, 6410819.29>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 8,
/* osm_id=10213072 */
  <1105871.23, 0.0, 6410713.29>,4.8
  <1105871.23, 0.0, 6410713.29>,4.8
  <1105874.73, 0.0, 6410726.05>,4.8
  <1105887.88, 0.0, 6410744.93>,4.8
  <1105916.16, 0.0, 6410781.53>,4.8
  <1105936.88, 0.0, 6410806.89>,4.8
  <1105945.58, 0.0, 6410819.29>,4.8
  <1105945.58, 0.0, 6410819.29>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=22779407 */
  <1105884.22, 0.0, 6410289.43>,4.0
  <1105884.22, 0.0, 6410289.43>,4.0
  <1105981.49, 0.0, 6410250.75>,4.0
  <1105981.49, 0.0, 6410250.75>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=22779407 */
  <1105884.22, 0.0, 6410289.43>,4.8
  <1105884.22, 0.0, 6410289.43>,4.8
  <1105981.49, 0.0, 6410250.75>,4.8
  <1105981.49, 0.0, 6410250.75>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=28511087 */
  <1105911.19, 0.0, 6410393.34>,2.4
  <1105911.19, 0.0, 6410393.34>,2.4
  <1105920.6, 0.0, 6410392.93>,2.4
  <1105920.6, 0.0, 6410392.93>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=28511087 */
  <1105911.19, 0.0, 6410393.34>,2.88
  <1105911.19, 0.0, 6410393.34>,2.88
  <1105920.6, 0.0, 6410392.93>,2.88
  <1105920.6, 0.0, 6410392.93>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=25859907 */
  <1105916.8, 0.0, 6410362.66>,4.0
  <1105916.8, 0.0, 6410362.66>,4.0
  <1105920.6, 0.0, 6410392.93>,4.0
  <1105920.6, 0.0, 6410392.93>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=25859907 */
  <1105916.8, 0.0, 6410362.66>,4.8
  <1105916.8, 0.0, 6410362.66>,4.8
  <1105920.6, 0.0, 6410392.93>,4.8
  <1105920.6, 0.0, 6410392.93>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 8,
/* osm_id=25859912 */
  <1105920.6, 0.0, 6410392.93>,2.4
  <1105920.6, 0.0, 6410392.93>,2.4
  <1105923.7, 0.0, 6410426.19>,2.4
  <1105917.73, 0.0, 6410480.51>,2.4
  <1105922.32, 0.0, 6410534.36>,2.4
  <1105925.52, 0.0, 6410553.93>,2.4
  <1105927.25, 0.0, 6410568.18>,2.4
  <1105927.25, 0.0, 6410568.18>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 8,
/* osm_id=25859912 */
  <1105920.6, 0.0, 6410392.93>,2.88
  <1105920.6, 0.0, 6410392.93>,2.88
  <1105923.7, 0.0, 6410426.19>,2.88
  <1105917.73, 0.0, 6410480.51>,2.88
  <1105922.32, 0.0, 6410534.36>,2.88
  <1105925.52, 0.0, 6410553.93>,2.88
  <1105927.25, 0.0, 6410568.18>,2.88
  <1105927.25, 0.0, 6410568.18>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=25859904 */
  <1105920.6, 0.0, 6410392.93>,4.0
  <1105920.6, 0.0, 6410392.93>,4.0
  <1106004.38, 0.0, 6410361.63>,4.0
  <1106004.38, 0.0, 6410361.63>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=25859904 */
  <1105920.6, 0.0, 6410392.93>,4.8
  <1105920.6, 0.0, 6410392.93>,4.8
  <1106004.38, 0.0, 6410361.63>,4.8
  <1106004.38, 0.0, 6410361.63>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=25859910 */
  <1105920.6, 0.0, 6410392.93>,4.0
  <1105920.6, 0.0, 6410392.93>,4.0
  <1105960.18, 0.0, 6410402.86>,4.0
  <1105989.4, 0.0, 6410418.55>,4.0
  <1105989.4, 0.0, 6410418.55>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=25859910 */
  <1105920.6, 0.0, 6410392.93>,4.8
  <1105920.6, 0.0, 6410392.93>,4.8
  <1105960.18, 0.0, 6410402.86>,4.8
  <1105989.4, 0.0, 6410418.55>,4.8
  <1105989.4, 0.0, 6410418.55>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=28413144 */
  <1105927.25, 0.0, 6410568.18>,2.4
  <1105927.25, 0.0, 6410568.18>,2.4
  <1105976.42, 0.0, 6410561.28>,2.4
  <1105976.42, 0.0, 6410561.28>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=28413144 */
  <1105927.25, 0.0, 6410568.18>,2.88
  <1105927.25, 0.0, 6410568.18>,2.88
  <1105976.42, 0.0, 6410561.28>,2.88
  <1105976.42, 0.0, 6410561.28>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 7,
/* osm_id=6110036 */
  <1105939.84, 0.0, 6411059.6>,4.0
  <1105939.84, 0.0, 6411059.6>,4.0
  <1106000.07, 0.0, 6411106.37>,4.0
  <1106072.63, 0.0, 6411152.54>,4.0
  <1106127.61, 0.0, 6411187.51>,4.0
  <1106205.65, 0.0, 6411235.16>,4.0
  <1106205.65, 0.0, 6411235.16>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 7,
/* osm_id=6110036 */
  <1105939.84, 0.0, 6411059.6>,4.8
  <1105939.84, 0.0, 6411059.6>,4.8
  <1106000.07, 0.0, 6411106.37>,4.8
  <1106072.63, 0.0, 6411152.54>,4.8
  <1106127.61, 0.0, 6411187.51>,4.8
  <1106205.65, 0.0, 6411235.16>,4.8
  <1106205.65, 0.0, 6411235.16>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 7,
/* osm_id=37652189 */
  <1105945.58, 0.0, 6410819.29>,4.0
  <1105945.58, 0.0, 6410819.29>,4.0
  <1105959.04, 0.0, 6410817.27>,4.0
  <1105968.83, 0.0, 6410818.67>,4.0
  <1105977.94, 0.0, 6410820.6>,4.0
  <1106047.62, 0.0, 6410856.87>,4.0
  <1106047.62, 0.0, 6410856.87>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 7,
/* osm_id=37652189 */
  <1105945.58, 0.0, 6410819.29>,4.8
  <1105945.58, 0.0, 6410819.29>,4.8
  <1105959.04, 0.0, 6410817.27>,4.8
  <1105968.83, 0.0, 6410818.67>,4.8
  <1105977.94, 0.0, 6410820.6>,4.8
  <1106047.62, 0.0, 6410856.87>,4.8
  <1106047.62, 0.0, 6410856.87>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 9,
/* osm_id=12710950 */
  <1105995.21, 0.0, 6410556.85>,4.0
  <1105995.21, 0.0, 6410556.85>,4.0
  <1106026.09, 0.0, 6410720.2>,4.0
  <1106026.78, 0.0, 6410731.74>,4.0
  <1106021.14, 0.0, 6410747.17>,4.0
  <1106003.45, 0.0, 6410767.11>,4.0
  <1105989.15, 0.0, 6410785.18>,4.0
  <1105968.83, 0.0, 6410818.67>,4.0
  <1105968.83, 0.0, 6410818.67>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 9,
/* osm_id=12710950 */
  <1105995.21, 0.0, 6410556.85>,4.8
  <1105995.21, 0.0, 6410556.85>,4.8
  <1106026.09, 0.0, 6410720.2>,4.8
  <1106026.78, 0.0, 6410731.74>,4.8
  <1106021.14, 0.0, 6410747.17>,4.8
  <1106003.45, 0.0, 6410767.11>,4.8
  <1105989.15, 0.0, 6410785.18>,4.8
  <1105968.83, 0.0, 6410818.67>,4.8
  <1105968.83, 0.0, 6410818.67>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 7,
/* osm_id=28413145 */
  <1105976.42, 0.0, 6410561.28>,4.0
  <1105976.42, 0.0, 6410561.28>,4.0
  <1105995.21, 0.0, 6410556.85>,4.0
  <1106009.23, 0.0, 6410553.07>,4.0
  <1106090.34, 0.0, 6410527.46>,4.0
  <1106135.89, 0.0, 6410511.98>,4.0
  <1106135.89, 0.0, 6410511.98>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 7,
/* osm_id=28413145 */
  <1105976.42, 0.0, 6410561.28>,4.8
  <1105976.42, 0.0, 6410561.28>,4.8
  <1105995.21, 0.0, 6410556.85>,4.8
  <1106009.23, 0.0, 6410553.07>,4.8
  <1106090.34, 0.0, 6410527.46>,4.8
  <1106135.89, 0.0, 6410511.98>,4.8
  <1106135.89, 0.0, 6410511.98>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 7,
/* osm_id=12710901 */
  <1106037.97, 0.0, 6411318.97>,4.0
  <1106037.97, 0.0, 6411318.97>,4.0
  <1106053.92, 0.0, 6411307.09>,4.0
  <1106083.41, 0.0, 6411278.63>,4.0
  <1106100.76, 0.0, 6411251.92>,4.0
  <1106127.61, 0.0, 6411187.51>,4.0
  <1106127.61, 0.0, 6411187.51>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 7,
/* osm_id=12710901 */
  <1106037.97, 0.0, 6411318.97>,4.8
  <1106037.97, 0.0, 6411318.97>,4.8
  <1106053.92, 0.0, 6411307.09>,4.8
  <1106083.41, 0.0, 6411278.63>,4.8
  <1106100.76, 0.0, 6411251.92>,4.8
  <1106127.61, 0.0, 6411187.51>,4.8
  <1106127.61, 0.0, 6411187.51>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=11299279 */
  <1105335.38, 0.0, 6410379.68>,2.4
  <1105335.38, 0.0, 6410379.68>,2.4
  <1105320.03, 0.0, 6410349.82>,2.4
  <1105333.67, 0.0, 6410326.85>,2.4
  <1105350.53, 0.0, 6410294.55>,2.4
  <1105350.53, 0.0, 6410294.55>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=11299279 */
  <1105335.38, 0.0, 6410379.68>,2.88
  <1105335.38, 0.0, 6410379.68>,2.88
  <1105320.03, 0.0, 6410349.82>,2.88
  <1105333.67, 0.0, 6410326.85>,2.88
  <1105350.53, 0.0, 6410294.55>,2.88
  <1105350.53, 0.0, 6410294.55>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=51195166 */
  <1105743.28, 0.0, 6410928.26>,2.4
  <1105743.28, 0.0, 6410928.26>,2.4
  <1105728.88, 0.0, 6410936.63>,2.4
  <1105692.57, 0.0, 6410942.97>,2.4
  <1105640.57, 0.0, 6410950.82>,2.4
  <1105640.57, 0.0, 6410950.82>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=51195166 */
  <1105743.28, 0.0, 6410928.26>,2.88
  <1105743.28, 0.0, 6410928.26>,2.88
  <1105728.88, 0.0, 6410936.63>,2.88
  <1105692.57, 0.0, 6410942.97>,2.88
  <1105640.57, 0.0, 6410950.82>,2.88
  <1105640.57, 0.0, 6410950.82>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 8,
/* osm_id=8106714 */
  <1105668.66, 0.0, 6410880.86>,2.4
  <1105668.66, 0.0, 6410880.86>,2.4
  <1105677.12, 0.0, 6410858.63>,2.4
  <1105693.64, 0.0, 6410818.95>,2.4
  <1105707.16, 0.0, 6410773.85>,2.4
  <1105716.29, 0.0, 6410744.76>,2.4
  <1105722.84, 0.0, 6410727.6>,2.4
  <1105722.84, 0.0, 6410727.6>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 8,
/* osm_id=8106714 */
  <1105668.66, 0.0, 6410880.86>,2.88
  <1105668.66, 0.0, 6410880.86>,2.88
  <1105677.12, 0.0, 6410858.63>,2.88
  <1105693.64, 0.0, 6410818.95>,2.88
  <1105707.16, 0.0, 6410773.85>,2.88
  <1105716.29, 0.0, 6410744.76>,2.88
  <1105722.84, 0.0, 6410727.6>,2.88
  <1105722.84, 0.0, 6410727.6>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=57837179 */
  <1105722.84, 0.0, 6410727.6>,2.4
  <1105722.84, 0.0, 6410727.6>,2.4
  <1105727.58, 0.0, 6410708.2>,2.4
  <1105736.05, 0.0, 6410681.7>,2.4
  <1105736.05, 0.0, 6410681.7>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=57837179 */
  <1105722.84, 0.0, 6410727.6>,2.88
  <1105722.84, 0.0, 6410727.6>,2.88
  <1105727.58, 0.0, 6410708.2>,2.88
  <1105736.05, 0.0, 6410681.7>,2.88
  <1105736.05, 0.0, 6410681.7>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 11,
/* osm_id=9035401 */
  <1105609.48, 0.0, 6410863.39>,2.4
  <1105609.48, 0.0, 6410863.39>,2.4
  <1105606.87, 0.0, 6410856.23>,2.4
  <1105620.22, 0.0, 6410762.83>,2.4
  <1105626.01, 0.0, 6410702.17>,2.4
  <1105640.5, 0.0, 6410684.71>,2.4
  <1105646.36, 0.0, 6410677.64>,2.4
  <1105663.84, 0.0, 6410663.24>,2.4
  <1105675.19, 0.0, 6410654.95>,2.4
  <1105683.8, 0.0, 6410650.26>,2.4
  <1105683.8, 0.0, 6410650.26>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 11,
/* osm_id=9035401 */
  <1105609.48, 0.0, 6410863.39>,2.88
  <1105609.48, 0.0, 6410863.39>,2.88
  <1105606.87, 0.0, 6410856.23>,2.88
  <1105620.22, 0.0, 6410762.83>,2.88
  <1105626.01, 0.0, 6410702.17>,2.88
  <1105640.5, 0.0, 6410684.71>,2.88
  <1105646.36, 0.0, 6410677.64>,2.88
  <1105663.84, 0.0, 6410663.24>,2.88
  <1105675.19, 0.0, 6410654.95>,2.88
  <1105683.8, 0.0, 6410650.26>,2.88
  <1105683.8, 0.0, 6410650.26>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=45285258 */
  <1105771.42, 0.0, 6411207.76>,4.0
  <1105771.42, 0.0, 6411207.76>,4.0
  <1105770.94, 0.0, 6411243.83>,4.0
  <1105772.99, 0.0, 6411277.45>,4.0
  <1105772.99, 0.0, 6411277.45>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=45285258 */
  <1105771.42, 0.0, 6411207.76>,4.8
  <1105771.42, 0.0, 6411207.76>,4.8
  <1105770.94, 0.0, 6411243.83>,4.8
  <1105772.99, 0.0, 6411277.45>,4.8
  <1105772.99, 0.0, 6411277.45>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=10213068 */
  <1105629.66, 0.0, 6410400.27>,4.0
  <1105629.66, 0.0, 6410400.27>,4.0
  <1105594.11, 0.0, 6410415.39>,4.0
  <1105561.85, 0.0, 6410428.22>,4.0
  <1105540.86, 0.0, 6410436.35>,4.0
  <1105540.86, 0.0, 6410436.35>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=10213068 */
  <1105629.66, 0.0, 6410400.27>,4.8
  <1105629.66, 0.0, 6410400.27>,4.8
  <1105594.11, 0.0, 6410415.39>,4.8
  <1105561.85, 0.0, 6410428.22>,4.8
  <1105540.86, 0.0, 6410436.35>,4.8
  <1105540.86, 0.0, 6410436.35>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=4473087 */
  <1105618.91, 0.0, 6410042.43>,4.0
  <1105618.91, 0.0, 6410042.43>,4.0
  <1105631.93, 0.0, 6410120.13>,4.0
  <1105633.13, 0.0, 6410154.41>,4.0
  <1105619.89, 0.0, 6410195.38>,4.0
  <1105619.89, 0.0, 6410195.38>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=4473087 */
  <1105618.91, 0.0, 6410042.43>,4.8
  <1105618.91, 0.0, 6410042.43>,4.8
  <1105631.93, 0.0, 6410120.13>,4.8
  <1105633.13, 0.0, 6410154.41>,4.8
  <1105619.89, 0.0, 6410195.38>,4.8
  <1105619.89, 0.0, 6410195.38>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=4568540 */
  <1105757.47, 0.0, 6410909.53>,2.4
  <1105757.47, 0.0, 6410909.53>,2.4
  <1105668.66, 0.0, 6410880.86>,2.4
  <1105668.66, 0.0, 6410880.86>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=4568540 */
  <1105757.47, 0.0, 6410909.53>,2.88
  <1105757.47, 0.0, 6410909.53>,2.88
  <1105668.66, 0.0, 6410880.86>,2.88
  <1105668.66, 0.0, 6410880.86>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=58531773 */
  <1105320.75, 0.0, 6410286.31>,4.0
  <1105320.75, 0.0, 6410286.31>,4.0
  <1105350.53, 0.0, 6410294.55>,4.0
  <1105350.53, 0.0, 6410294.55>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=58531773 */
  <1105320.75, 0.0, 6410286.31>,4.8
  <1105320.75, 0.0, 6410286.31>,4.8
  <1105350.53, 0.0, 6410294.55>,4.8
  <1105350.53, 0.0, 6410294.55>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=4568541 */
  <1105757.47, 0.0, 6410909.53>,2.4
  <1105757.47, 0.0, 6410909.53>,2.4
  <1105743.28, 0.0, 6410928.26>,2.4
  <1105743.28, 0.0, 6410928.26>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=4568541 */
  <1105757.47, 0.0, 6410909.53>,2.88
  <1105757.47, 0.0, 6410909.53>,2.88
  <1105743.28, 0.0, 6410928.26>,2.88
  <1105743.28, 0.0, 6410928.26>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=57837182 */
  <1105602.43, 0.0, 6410231.11>,4.0
  <1105602.43, 0.0, 6410231.11>,4.0
  <1105597.78, 0.0, 6410256.34>,4.0
  <1105600.14, 0.0, 6410298.39>,4.0
  <1105600.14, 0.0, 6410298.39>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=57837182 */
  <1105602.43, 0.0, 6410231.11>,4.8
  <1105602.43, 0.0, 6410231.11>,4.8
  <1105597.78, 0.0, 6410256.34>,4.8
  <1105600.14, 0.0, 6410298.39>,4.8
  <1105600.14, 0.0, 6410298.39>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 10,
/* osm_id=8106712 */
  <1105775.02, 0.0, 6410606.13>,2.4
  <1105775.02, 0.0, 6410606.13>,2.4
  <1105758.78, 0.0, 6410578.09>,2.4
  <1105729.18, 0.0, 6410526.96>,2.4
  <1105714.67, 0.0, 6410490.89>,2.4
  <1105717.07, 0.0, 6410474.87>,2.4
  <1105725.1, 0.0, 6410472.25>,2.4
  <1105771.95, 0.0, 6410555.14>,2.4
  <1105795.25, 0.0, 6410595.92>,2.4
  <1105795.25, 0.0, 6410595.92>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 10,
/* osm_id=8106712 */
  <1105775.02, 0.0, 6410606.13>,2.88
  <1105775.02, 0.0, 6410606.13>,2.88
  <1105758.78, 0.0, 6410578.09>,2.88
  <1105729.18, 0.0, 6410526.96>,2.88
  <1105714.67, 0.0, 6410490.89>,2.88
  <1105717.07, 0.0, 6410474.87>,2.88
  <1105725.1, 0.0, 6410472.25>,2.88
  <1105771.95, 0.0, 6410555.14>,2.88
  <1105795.25, 0.0, 6410595.92>,2.88
  <1105795.25, 0.0, 6410595.92>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 7,
/* osm_id=13029701 */
  <1105255.99, 0.0, 6410598.56>,4.0
  <1105255.99, 0.0, 6410598.56>,4.0
  <1105251.54, 0.0, 6410628.6>,4.0
  <1105241.63, 0.0, 6410686.49>,4.0
  <1105230.97, 0.0, 6410743.66>,4.0
  <1105229.33, 0.0, 6410753.5>,4.0
  <1105229.33, 0.0, 6410753.5>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 7,
/* osm_id=13029701 */
  <1105255.99, 0.0, 6410598.56>,4.8
  <1105255.99, 0.0, 6410598.56>,4.8
  <1105251.54, 0.0, 6410628.6>,4.8
  <1105241.63, 0.0, 6410686.49>,4.8
  <1105230.97, 0.0, 6410743.66>,4.8
  <1105229.33, 0.0, 6410753.5>,4.8
  <1105229.33, 0.0, 6410753.5>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=13024513 */
  <1105241.63, 0.0, 6410686.49>,4.0
  <1105241.63, 0.0, 6410686.49>,4.0
  <1105293.38, 0.0, 6410687.47>,4.0
  <1105293.38, 0.0, 6410687.47>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=13024513 */
  <1105241.63, 0.0, 6410686.49>,4.8
  <1105241.63, 0.0, 6410686.49>,4.8
  <1105293.38, 0.0, 6410687.47>,4.8
  <1105293.38, 0.0, 6410687.47>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=4568539 */
  <1105241.86, 0.0, 6410537.76>,4.0
  <1105241.86, 0.0, 6410537.76>,4.0
  <1105247.78, 0.0, 6410468.11>,4.0
  <1105249.15, 0.0, 6410451.9>,4.0
  <1105249.15, 0.0, 6410451.9>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=4568539 */
  <1105241.86, 0.0, 6410537.76>,4.8
  <1105241.86, 0.0, 6410537.76>,4.8
  <1105247.78, 0.0, 6410468.11>,4.8
  <1105249.15, 0.0, 6410451.9>,4.8
  <1105249.15, 0.0, 6410451.9>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=29157363 */
  <1105241.86, 0.0, 6410537.76>,4.0
  <1105241.86, 0.0, 6410537.76>,4.0
  <1105257.14, 0.0, 6410539.67>,4.0
  <1105281.45, 0.0, 6410541.66>,4.0
  <1105303.97, 0.0, 6410543.48>,4.0
  <1105303.97, 0.0, 6410543.48>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=29157363 */
  <1105241.86, 0.0, 6410537.76>,4.8
  <1105241.86, 0.0, 6410537.76>,4.8
  <1105257.14, 0.0, 6410539.67>,4.8
  <1105281.45, 0.0, 6410541.66>,4.8
  <1105303.97, 0.0, 6410543.48>,4.8
  <1105303.97, 0.0, 6410543.48>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 8,
/* osm_id=28808023 */
  <1105316.59, 0.0, 6410544.79>,4.0
  <1105316.59, 0.0, 6410544.79>,4.0
  <1105316.59, 0.0, 6410524.51>,4.0
  <1105312.85, 0.0, 6410498.84>,4.0
  <1105302.7, 0.0, 6410483.53>,4.0
  <1105287.19, 0.0, 6410476.08>,4.0
  <1105247.78, 0.0, 6410468.11>,4.0
  <1105247.78, 0.0, 6410468.11>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 8,
/* osm_id=28808023 */
  <1105316.59, 0.0, 6410544.79>,4.8
  <1105316.59, 0.0, 6410544.79>,4.8
  <1105316.59, 0.0, 6410524.51>,4.8
  <1105312.85, 0.0, 6410498.84>,4.8
  <1105302.7, 0.0, 6410483.53>,4.8
  <1105287.19, 0.0, 6410476.08>,4.8
  <1105247.78, 0.0, 6410468.11>,4.8
  <1105247.78, 0.0, 6410468.11>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=13012693 */
  <1105257.14, 0.0, 6410539.67>,4.0
  <1105257.14, 0.0, 6410539.67>,4.0
  <1105255.99, 0.0, 6410598.56>,4.0
  <1105255.99, 0.0, 6410598.56>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=13012693 */
  <1105257.14, 0.0, 6410539.67>,4.8
  <1105257.14, 0.0, 6410539.67>,4.8
  <1105255.99, 0.0, 6410598.56>,4.8
  <1105255.99, 0.0, 6410598.56>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=32275246 */
  <1105280.49, 0.0, 6410924.69>,4.0
  <1105280.49, 0.0, 6410924.69>,4.0
  <1105298.4, 0.0, 6410883.86>,4.0
  <1105319.71, 0.0, 6410835.52>,4.0
  <1105319.71, 0.0, 6410835.52>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=32275246 */
  <1105280.49, 0.0, 6410924.69>,4.8
  <1105280.49, 0.0, 6410924.69>,4.8
  <1105298.4, 0.0, 6410883.86>,4.8
  <1105319.71, 0.0, 6410835.52>,4.8
  <1105319.71, 0.0, 6410835.52>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=13012694 */
  <1105303.97, 0.0, 6410543.48>,4.0
  <1105303.97, 0.0, 6410543.48>,4.0
  <1105300.22, 0.0, 6410599.16>,4.0
  <1105300.22, 0.0, 6410599.16>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=13012694 */
  <1105303.97, 0.0, 6410543.48>,4.8
  <1105303.97, 0.0, 6410543.48>,4.8
  <1105300.22, 0.0, 6410599.16>,4.8
  <1105300.22, 0.0, 6410599.16>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 9,
/* osm_id=35419675 */
  <1105303.97, 0.0, 6410543.48>,2.4
  <1105303.97, 0.0, 6410543.48>,2.4
  <1105316.59, 0.0, 6410544.79>,2.4
  <1105362.25, 0.0, 6410548.26>,2.4
  <1105372.76, 0.0, 6410548.26>,2.4
  <1105417.67, 0.0, 6410552.09>,2.4
  <1105484.55, 0.0, 6410559.73>,2.4
  <1105510.34, 0.0, 6410562.59>,2.4
  <1105510.34, 0.0, 6410562.59>,2.4
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 9,
/* osm_id=35419675 */
  <1105303.97, 0.0, 6410543.48>,2.88
  <1105303.97, 0.0, 6410543.48>,2.88
  <1105316.59, 0.0, 6410544.79>,2.88
  <1105362.25, 0.0, 6410548.26>,2.88
  <1105372.76, 0.0, 6410548.26>,2.88
  <1105417.67, 0.0, 6410552.09>,2.88
  <1105484.55, 0.0, 6410559.73>,2.88
  <1105510.34, 0.0, 6410562.59>,2.88
  <1105510.34, 0.0, 6410562.59>,2.88
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*1.2, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=31892910 */
  <1105319.71, 0.0, 6410835.52>,4.0
  <1105319.71, 0.0, 6410835.52>,4.0
  <1105315.48, 0.0, 6410824.72>,4.0
  <1105315.48, 0.0, 6410824.72>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=31892910 */
  <1105319.71, 0.0, 6410835.52>,4.8
  <1105319.71, 0.0, 6410835.52>,4.8
  <1105315.48, 0.0, 6410824.72>,4.8
  <1105315.48, 0.0, 6410824.72>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=13029696 */
  <1105357.02, 0.0, 6410879.99>,4.0
  <1105357.02, 0.0, 6410879.99>,4.0
  <1105386.69, 0.0, 6410932.12>,4.0
  <1105369.84, 0.0, 6410994.64>,4.0
  <1105369.84, 0.0, 6410994.64>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=13029696 */
  <1105357.02, 0.0, 6410879.99>,4.8
  <1105357.02, 0.0, 6410879.99>,4.8
  <1105386.69, 0.0, 6410932.12>,4.8
  <1105369.84, 0.0, 6410994.64>,4.8
  <1105369.84, 0.0, 6410994.64>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 7,
/* osm_id=13024517 */
  <1105357.02, 0.0, 6410879.99>,4.0
  <1105357.02, 0.0, 6410879.99>,4.0
  <1105367.55, 0.0, 6410879.61>,4.0
  <1105391.87, 0.0, 6410881.7>,4.0
  <1105420.37, 0.0, 6410887.62>,4.0
  <1105438.09, 0.0, 6410886.22>,4.0
  <1105438.09, 0.0, 6410886.22>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 7,
/* osm_id=13024517 */
  <1105357.02, 0.0, 6410879.99>,4.8
  <1105357.02, 0.0, 6410879.99>,4.8
  <1105367.55, 0.0, 6410879.61>,4.8
  <1105391.87, 0.0, 6410881.7>,4.8
  <1105420.37, 0.0, 6410887.62>,4.8
  <1105438.09, 0.0, 6410886.22>,4.8
  <1105438.09, 0.0, 6410886.22>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 7,
/* osm_id=25594017 */
  <1105357.78, 0.0, 6410690.61>,4.0
  <1105357.78, 0.0, 6410690.61>,4.0
  <1105359.99, 0.0, 6410661.71>,4.0
  <1105359.6, 0.0, 6410629.57>,4.0
  <1105360, 0.0, 6410596.25>,4.0
  <1105362.25, 0.0, 6410548.26>,4.0
  <1105362.25, 0.0, 6410548.26>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 7,
/* osm_id=25594017 */
  <1105357.78, 0.0, 6410690.61>,4.8
  <1105357.78, 0.0, 6410690.61>,4.8
  <1105359.99, 0.0, 6410661.71>,4.8
  <1105359.6, 0.0, 6410629.57>,4.8
  <1105360, 0.0, 6410596.25>,4.8
  <1105362.25, 0.0, 6410548.26>,4.8
  <1105362.25, 0.0, 6410548.26>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=4575580 */
  <1105509.64, 0.0, 6410663.43>,4.0
  <1105509.64, 0.0, 6410663.43>,4.0
  <1105477.16, 0.0, 6410663.24>,4.0
  <1105408.83, 0.0, 6410662.11>,4.0
  <1105359.99, 0.0, 6410661.71>,4.0
  <1105359.99, 0.0, 6410661.71>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=4575580 */
  <1105509.64, 0.0, 6410663.43>,4.8
  <1105509.64, 0.0, 6410663.43>,4.8
  <1105477.16, 0.0, 6410663.24>,4.8
  <1105408.83, 0.0, 6410662.11>,4.8
  <1105359.99, 0.0, 6410661.71>,4.8
  <1105359.99, 0.0, 6410661.71>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=11299271 */
  <1105393.96, 0.0, 6410501.25>,4.0
  <1105393.96, 0.0, 6410501.25>,4.0
  <1105391.25, 0.0, 6410518.86>,4.0
  <1105376.01, 0.0, 6410517.75>,4.0
  <1105372.76, 0.0, 6410548.26>,4.0
  <1105372.76, 0.0, 6410548.26>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=11299271 */
  <1105393.96, 0.0, 6410501.25>,4.8
  <1105393.96, 0.0, 6410501.25>,4.8
  <1105391.25, 0.0, 6410518.86>,4.8
  <1105376.01, 0.0, 6410517.75>,4.8
  <1105372.76, 0.0, 6410548.26>,4.8
  <1105372.76, 0.0, 6410548.26>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=30332075 */
  <1105376.01, 0.0, 6410517.75>,4.0
  <1105376.01, 0.0, 6410517.75>,4.0
  <1105379.26, 0.0, 6410443.98>,4.0
  <1105379.26, 0.0, 6410443.98>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=30332075 */
  <1105376.01, 0.0, 6410517.75>,4.8
  <1105376.01, 0.0, 6410517.75>,4.8
  <1105379.26, 0.0, 6410443.98>,4.8
  <1105379.26, 0.0, 6410443.98>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=12711062 */
  <1105386.69, 0.0, 6410932.12>,4.0
  <1105386.69, 0.0, 6410932.12>,4.0
  <1105432.56, 0.0, 6410946.73>,4.0
  <1105520.57, 0.0, 6410961.92>,4.0
  <1105520.57, 0.0, 6410961.92>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=12711062 */
  <1105386.69, 0.0, 6410932.12>,4.8
  <1105386.69, 0.0, 6410932.12>,4.8
  <1105432.56, 0.0, 6410946.73>,4.8
  <1105520.57, 0.0, 6410961.92>,4.8
  <1105520.57, 0.0, 6410961.92>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=11299274 */
  <1105414.76, 0.0, 6410415.84>,4.0
  <1105414.76, 0.0, 6410415.84>,4.0
  <1105400.77, 0.0, 6410461.42>,4.0
  <1105393.96, 0.0, 6410501.25>,4.0
  <1105393.96, 0.0, 6410501.25>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=11299274 */
  <1105414.76, 0.0, 6410415.84>,4.8
  <1105414.76, 0.0, 6410415.84>,4.8
  <1105400.77, 0.0, 6410461.42>,4.8
  <1105393.96, 0.0, 6410501.25>,4.8
  <1105393.96, 0.0, 6410501.25>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=11299272 */
  <1105393.96, 0.0, 6410501.25>,4.0
  <1105393.96, 0.0, 6410501.25>,4.0
  <1105424.46, 0.0, 6410502.68>,4.0
  <1105417.67, 0.0, 6410552.09>,4.0
  <1105417.67, 0.0, 6410552.09>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=11299272 */
  <1105393.96, 0.0, 6410501.25>,4.8
  <1105393.96, 0.0, 6410501.25>,4.8
  <1105424.46, 0.0, 6410502.68>,4.8
  <1105417.67, 0.0, 6410552.09>,4.8
  <1105417.67, 0.0, 6410552.09>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=13024508 */
  <1105408.83, 0.0, 6410662.11>,4.0
  <1105408.83, 0.0, 6410662.11>,4.0
  <1105403.94, 0.0, 6410695.06>,4.0
  <1105403.94, 0.0, 6410695.06>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=13024508 */
  <1105408.83, 0.0, 6410662.11>,4.8
  <1105408.83, 0.0, 6410662.11>,4.8
  <1105403.94, 0.0, 6410695.06>,4.8
  <1105403.94, 0.0, 6410695.06>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=26651366 */
  <1105517.12, 0.0, 6411224.81>,4.0
  <1105517.12, 0.0, 6411224.81>,4.0
  <1105500.82, 0.0, 6411219.65>,4.0
  <1105424.19, 0.0, 6411200.94>,4.0
  <1105416.05, 0.0, 6411207.57>,4.0
  <1105416.05, 0.0, 6411207.57>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=26651366 */
  <1105517.12, 0.0, 6411224.81>,4.8
  <1105517.12, 0.0, 6411224.81>,4.8
  <1105500.82, 0.0, 6411219.65>,4.8
  <1105424.19, 0.0, 6411200.94>,4.8
  <1105416.05, 0.0, 6411207.57>,4.8
  <1105416.05, 0.0, 6411207.57>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=6110022 */
  <1105419.54, 0.0, 6411187.87>,4.0
  <1105419.54, 0.0, 6411187.87>,4.0
  <1105427.03, 0.0, 6411189.84>,4.0
  <1105427.03, 0.0, 6411189.84>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=6110022 */
  <1105419.54, 0.0, 6411187.87>,4.8
  <1105419.54, 0.0, 6411187.87>,4.8
  <1105427.03, 0.0, 6411189.84>,4.8
  <1105427.03, 0.0, 6411189.84>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=30214903 */
  <1105424.46, 0.0, 6410502.68>,4.0
  <1105424.46, 0.0, 6410502.68>,4.0
  <1105464.74, 0.0, 6410506.08>,4.0
  <1105464.74, 0.0, 6410506.08>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=30214903 */
  <1105424.46, 0.0, 6410502.68>,4.8
  <1105424.46, 0.0, 6410502.68>,4.8
  <1105464.74, 0.0, 6410506.08>,4.8
  <1105464.74, 0.0, 6410506.08>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 8,
/* osm_id=28595389 */
  <1105461.99, 0.0, 6411107.08>,4.0
  <1105461.99, 0.0, 6411107.08>,4.0
  <1105445.21, 0.0, 6411109.27>,4.0
  <1105437.97, 0.0, 6411109.46>,4.0
  <1105435.88, 0.0, 6411054.81>,4.0
  <1105459.76, 0.0, 6411056>,4.0
  <1105461.01, 0.0, 6411021.92>,4.0
  <1105461.01, 0.0, 6411021.92>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 8,
/* osm_id=28595389 */
  <1105461.99, 0.0, 6411107.08>,4.8
  <1105461.99, 0.0, 6411107.08>,4.8
  <1105445.21, 0.0, 6411109.27>,4.8
  <1105437.97, 0.0, 6411109.46>,4.8
  <1105435.88, 0.0, 6411054.81>,4.8
  <1105459.76, 0.0, 6411056>,4.8
  <1105461.01, 0.0, 6411021.92>,4.8
  <1105461.01, 0.0, 6411021.92>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=28458584 */
  <1105438.09, 0.0, 6410886.22>,4.0
  <1105438.09, 0.0, 6410886.22>,4.0
  <1105455.89, 0.0, 6410836.45>,4.0
  <1105455.89, 0.0, 6410836.45>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=28458584 */
  <1105438.09, 0.0, 6410886.22>,4.8
  <1105438.09, 0.0, 6410886.22>,4.8
  <1105455.89, 0.0, 6410836.45>,4.8
  <1105455.89, 0.0, 6410836.45>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=4568528 */
  <1105534.42, 0.0, 6411089.87>,4.0
  <1105534.42, 0.0, 6411089.87>,4.0
  <1105472.39, 0.0, 6411105.42>,4.0
  <1105461.99, 0.0, 6411107.08>,4.0
  <1105461.99, 0.0, 6411107.08>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=4568528 */
  <1105534.42, 0.0, 6411089.87>,4.8
  <1105534.42, 0.0, 6411089.87>,4.8
  <1105472.39, 0.0, 6411105.42>,4.8
  <1105461.99, 0.0, 6411107.08>,4.8
  <1105461.99, 0.0, 6411107.08>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 8,
/* osm_id=30186921 */
  <1105486.5, 0.0, 6410756.43>,4.0
  <1105486.5, 0.0, 6410756.43>,4.0
  <1105486.36, 0.0, 6410748.81>,4.0
  <1105485.43, 0.0, 6410728.52>,4.0
  <1105477.16, 0.0, 6410663.24>,4.0
  <1105482.93, 0.0, 6410601.77>,4.0
  <1105484.55, 0.0, 6410559.73>,4.0
  <1105484.55, 0.0, 6410559.73>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 8,
/* osm_id=30186921 */
  <1105486.5, 0.0, 6410756.43>,4.8
  <1105486.5, 0.0, 6410756.43>,4.8
  <1105486.36, 0.0, 6410748.81>,4.8
  <1105485.43, 0.0, 6410728.52>,4.8
  <1105477.16, 0.0, 6410663.24>,4.8
  <1105482.93, 0.0, 6410601.77>,4.8
  <1105484.55, 0.0, 6410559.73>,4.8
  <1105484.55, 0.0, 6410559.73>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 7,
/* osm_id=4575533 */
  <1105508.58, 0.0, 6410844.71>,4.0
  <1105508.58, 0.0, 6410844.71>,4.0
  <1105508.49, 0.0, 6410837.05>,4.0
  <1105507.12, 0.0, 6410779.89>,4.0
  <1105507.5, 0.0, 6410757.76>,4.0
  <1105507.55, 0.0, 6410756.24>,4.0
  <1105507.55, 0.0, 6410756.24>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 7,
/* osm_id=4575533 */
  <1105508.58, 0.0, 6410844.71>,4.8
  <1105508.58, 0.0, 6410844.71>,4.8
  <1105508.49, 0.0, 6410837.05>,4.8
  <1105507.12, 0.0, 6410779.89>,4.8
  <1105507.5, 0.0, 6410757.76>,4.8
  <1105507.55, 0.0, 6410756.24>,4.8
  <1105507.55, 0.0, 6410756.24>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 7,
/* osm_id=4575504 */
  <1105507.55, 0.0, 6410756.24>,4.0
  <1105507.55, 0.0, 6410756.24>,4.0
  <1105509.53, 0.0, 6410692.53>,4.0
  <1105509.64, 0.0, 6410663.43>,4.0
  <1105515.62, 0.0, 6410596.94>,4.0
  <1105510.34, 0.0, 6410562.59>,4.0
  <1105510.34, 0.0, 6410562.59>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 7,
/* osm_id=4575504 */
  <1105507.55, 0.0, 6410756.24>,4.8
  <1105507.55, 0.0, 6410756.24>,4.8
  <1105509.53, 0.0, 6410692.53>,4.8
  <1105509.64, 0.0, 6410663.43>,4.8
  <1105515.62, 0.0, 6410596.94>,4.8
  <1105510.34, 0.0, 6410562.59>,4.8
  <1105510.34, 0.0, 6410562.59>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12710868 */
  <1105561.21, 0.0, 6410760.14>,4.0
  <1105561.21, 0.0, 6410760.14>,4.0
  <1105507.55, 0.0, 6410756.24>,4.0
  <1105507.55, 0.0, 6410756.24>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12710868 */
  <1105561.21, 0.0, 6410760.14>,4.8
  <1105561.21, 0.0, 6410760.14>,4.8
  <1105507.55, 0.0, 6410756.24>,4.8
  <1105507.55, 0.0, 6410756.24>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 8,
/* osm_id=6110028 */
  <1105561.44, 0.0, 6410843.97>,4.0
  <1105561.44, 0.0, 6410843.97>,4.0
  <1105559.59, 0.0, 6410857.01>,4.0
  <1105508.95, 0.0, 6410848.59>,4.0
  <1105508.58, 0.0, 6410844.71>,4.0
  <1105508.49, 0.0, 6410837.05>,4.0
  <1105561.44, 0.0, 6410843.97>,4.0
  <1105561.44, 0.0, 6410843.97>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 8,
/* osm_id=6110028 */
  <1105561.44, 0.0, 6410843.97>,4.8
  <1105561.44, 0.0, 6410843.97>,4.8
  <1105559.59, 0.0, 6410857.01>,4.8
  <1105508.95, 0.0, 6410848.59>,4.8
  <1105508.58, 0.0, 6410844.71>,4.8
  <1105508.49, 0.0, 6410837.05>,4.8
  <1105561.44, 0.0, 6410843.97>,4.8
  <1105561.44, 0.0, 6410843.97>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=30278672 */
  <1105526.97, 0.0, 6411013.88>,4.0
  <1105526.97, 0.0, 6411013.88>,4.0
  <1105520.57, 0.0, 6410961.92>,4.0
  <1105508.95, 0.0, 6410848.59>,4.0
  <1105508.58, 0.0, 6410844.71>,4.0
  <1105508.58, 0.0, 6410844.71>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=30278672 */
  <1105526.97, 0.0, 6411013.88>,4.8
  <1105526.97, 0.0, 6411013.88>,4.8
  <1105520.57, 0.0, 6410961.92>,4.8
  <1105508.95, 0.0, 6410848.59>,4.8
  <1105508.58, 0.0, 6410844.71>,4.8
  <1105508.58, 0.0, 6410844.71>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=4575502 */
  <1105510.34, 0.0, 6410562.59>,4.0
  <1105510.34, 0.0, 6410562.59>,4.0
  <1105512.05, 0.0, 6410550.93>,4.0
  <1105522.66, 0.0, 6410499.53>,4.0
  <1105540.86, 0.0, 6410436.35>,4.0
  <1105540.86, 0.0, 6410436.35>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=4575502 */
  <1105510.34, 0.0, 6410562.59>,4.8
  <1105510.34, 0.0, 6410562.59>,4.8
  <1105512.05, 0.0, 6410550.93>,4.8
  <1105522.66, 0.0, 6410499.53>,4.8
  <1105540.86, 0.0, 6410436.35>,4.8
  <1105540.86, 0.0, 6410436.35>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=28441628 */
  <1105510.34, 0.0, 6410562.59>,4.0
  <1105510.34, 0.0, 6410562.59>,4.0
  <1105558.12, 0.0, 6410568.33>,4.0
  <1105573.4, 0.0, 6410569.28>,4.0
  <1105573.4, 0.0, 6410569.28>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=28441628 */
  <1105510.34, 0.0, 6410562.59>,4.8
  <1105510.34, 0.0, 6410562.59>,4.8
  <1105558.12, 0.0, 6410568.33>,4.8
  <1105573.4, 0.0, 6410569.28>,4.8
  <1105573.4, 0.0, 6410569.28>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=27531336 */
  <1105512.15, 0.0, 6410497.63>,4.0
  <1105512.15, 0.0, 6410497.63>,4.0
  <1105522.66, 0.0, 6410499.53>,4.0
  <1105522.66, 0.0, 6410499.53>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=27531336 */
  <1105512.15, 0.0, 6410497.63>,4.8
  <1105512.15, 0.0, 6410497.63>,4.8
  <1105522.66, 0.0, 6410499.53>,4.8
  <1105522.66, 0.0, 6410499.53>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=37656868 */
  <1105525.47, 0.0, 6411203>,4.0
  <1105525.47, 0.0, 6411203>,4.0
  <1105523.03, 0.0, 6411209.48>,4.0
  <1105517.79, 0.0, 6411223.4>,4.0
  <1105517.12, 0.0, 6411224.81>,4.0
  <1105517.12, 0.0, 6411224.81>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=37656868 */
  <1105525.47, 0.0, 6411203>,4.8
  <1105525.47, 0.0, 6411203>,4.8
  <1105523.03, 0.0, 6411209.48>,4.8
  <1105517.79, 0.0, 6411223.4>,4.8
  <1105517.12, 0.0, 6411224.81>,4.8
  <1105517.12, 0.0, 6411224.81>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 8,
/* osm_id=37656870 */
  <1105517.12, 0.0, 6411224.81>,4.0
  <1105517.12, 0.0, 6411224.81>,4.0
  <1105622.72, 0.0, 6411258.23>,4.0
  <1105663.66, 0.0, 6411271.2>,4.0
  <1105702.24, 0.0, 6411283.41>,4.0
  <1105738.55, 0.0, 6411294.9>,4.0
  <1105740.11, 0.0, 6411294.52>,4.0
  <1105740.11, 0.0, 6411294.52>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 8,
/* osm_id=37656870 */
  <1105517.12, 0.0, 6411224.81>,4.8
  <1105517.12, 0.0, 6411224.81>,4.8
  <1105622.72, 0.0, 6411258.23>,4.8
  <1105663.66, 0.0, 6411271.2>,4.8
  <1105702.24, 0.0, 6411283.41>,4.8
  <1105738.55, 0.0, 6411294.9>,4.8
  <1105740.11, 0.0, 6411294.52>,4.8
  <1105740.11, 0.0, 6411294.52>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=4587362 */
  <1105538.01, 0.0, 6411158.42>,4.0
  <1105538.01, 0.0, 6411158.42>,4.0
  <1105518.89, 0.0, 6411158.09>,4.0
  <1105518.89, 0.0, 6411158.09>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=4587362 */
  <1105538.01, 0.0, 6411158.42>,4.8
  <1105538.01, 0.0, 6411158.42>,4.8
  <1105518.89, 0.0, 6411158.09>,4.8
  <1105518.89, 0.0, 6411158.09>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 6,
/* osm_id=28458585 */
  <1105640.57, 0.0, 6410950.82>,4.0
  <1105640.57, 0.0, 6410950.82>,4.0
  <1105591.47, 0.0, 6410958.01>,4.0
  <1105554.23, 0.0, 6410961.54>,4.0
  <1105520.57, 0.0, 6410961.92>,4.0
  <1105520.57, 0.0, 6410961.92>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 6,
/* osm_id=28458585 */
  <1105640.57, 0.0, 6410950.82>,4.8
  <1105640.57, 0.0, 6410950.82>,4.8
  <1105591.47, 0.0, 6410958.01>,4.8
  <1105554.23, 0.0, 6410961.54>,4.8
  <1105520.57, 0.0, 6410961.92>,4.8
  <1105520.57, 0.0, 6410961.92>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 7,
/* osm_id=22913012 */
  <1105525.47, 0.0, 6411203>,4.0
  <1105525.47, 0.0, 6411203>,4.0
  <1105538.01, 0.0, 6411158.42>,4.0
  <1105539.33, 0.0, 6411125.37>,4.0
  <1105534.42, 0.0, 6411089.87>,4.0
  <1105529.85, 0.0, 6411030.06>,4.0
  <1105529.85, 0.0, 6411030.06>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 7,
/* osm_id=22913012 */
  <1105525.47, 0.0, 6411203>,4.8
  <1105525.47, 0.0, 6411203>,4.8
  <1105538.01, 0.0, 6411158.42>,4.8
  <1105539.33, 0.0, 6411125.37>,4.8
  <1105534.42, 0.0, 6411089.87>,4.8
  <1105529.85, 0.0, 6411030.06>,4.8
  <1105529.85, 0.0, 6411030.06>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=42809787 */
  <1105583.64, 0.0, 6411010.18>,4.0
  <1105583.64, 0.0, 6411010.18>,4.0
  <1105573.22, 0.0, 6411010.87>,4.0
  <1105526.97, 0.0, 6411013.88>,4.0
  <1105526.97, 0.0, 6411013.88>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=42809787 */
  <1105583.64, 0.0, 6411010.18>,4.8
  <1105583.64, 0.0, 6411010.18>,4.8
  <1105573.22, 0.0, 6411010.87>,4.8
  <1105526.97, 0.0, 6411013.88>,4.8
  <1105526.97, 0.0, 6411013.88>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=4568527 */
  <1105526.97, 0.0, 6411013.88>,4.0
  <1105526.97, 0.0, 6411013.88>,4.0
  <1105529.85, 0.0, 6411030.06>,4.0
  <1105529.85, 0.0, 6411030.06>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=4568527 */
  <1105526.97, 0.0, 6411013.88>,4.8
  <1105526.97, 0.0, 6411013.88>,4.8
  <1105529.85, 0.0, 6411030.06>,4.8
  <1105529.85, 0.0, 6411030.06>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12654640 */
  <1105538.01, 0.0, 6411158.42>,4.0
  <1105538.01, 0.0, 6411158.42>,4.0
  <1105575.96, 0.0, 6411171.19>,4.0
  <1105575.96, 0.0, 6411171.19>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=12654640 */
  <1105538.01, 0.0, 6411158.42>,4.8
  <1105538.01, 0.0, 6411158.42>,4.8
  <1105575.96, 0.0, 6411171.19>,4.8
  <1105575.96, 0.0, 6411171.19>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=6110025 */
  <1105612.41, 0.0, 6410869.42>,4.0
  <1105612.41, 0.0, 6410869.42>,4.0
  <1105559.59, 0.0, 6410857.01>,4.0
  <1105559.59, 0.0, 6410857.01>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=6110025 */
  <1105612.41, 0.0, 6410869.42>,4.8
  <1105612.41, 0.0, 6410869.42>,4.8
  <1105559.59, 0.0, 6410857.01>,4.8
  <1105559.59, 0.0, 6410857.01>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=27176391 */
  <1105561.44, 0.0, 6410843.97>,4.0
  <1105561.44, 0.0, 6410843.97>,4.0
  <1105606.87, 0.0, 6410856.23>,4.0
  <1105606.87, 0.0, 6410856.23>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=27176391 */
  <1105561.44, 0.0, 6410843.97>,4.8
  <1105561.44, 0.0, 6410843.97>,4.8
  <1105606.87, 0.0, 6410856.23>,4.8
  <1105606.87, 0.0, 6410856.23>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=6110033 */
  <1105884.85, 0.0, 6410997.4>,4.0
  <1105884.85, 0.0, 6410997.4>,4.0
  <1105925.85, 0.0, 6411033.61>,4.0
  <1105925.85, 0.0, 6411033.61>,4.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=6110033 */
  <1105884.85, 0.0, 6410997.4>,4.8
  <1105884.85, 0.0, 6410997.4>,4.8
  <1105925.85, 0.0, 6411033.61>,4.8
  <1105925.85, 0.0, 6411033.61>,4.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=58570943 */
  <1105396.95, 0.0, 6410350.06>,1.5
  <1105396.95, 0.0, 6410350.06>,1.5
  <1105391.8, 0.0, 6410347.54>,1.5
  <1105391.8, 0.0, 6410347.54>,1.5
  tolerance 1
    texture {
        pigment {
            color rgb <0.7,0.8,0.7>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=58570943 */
  <1105396.95, 0.0, 6410350.06>,1.8
  <1105396.95, 0.0, 6410350.06>,1.8
  <1105391.8, 0.0, 6410347.54>,1.8
  <1105391.8, 0.0, 6410347.54>,1.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=58570942 */
  <1105442.06, 0.0, 6410354.58>,1.5
  <1105442.06, 0.0, 6410354.58>,1.5
  <1105439.03, 0.0, 6410354.8>,1.5
  <1105439.03, 0.0, 6410354.8>,1.5
  tolerance 1
    texture {
        pigment {
            color rgb <0.7,0.8,0.7>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=58570942 */
  <1105442.06, 0.0, 6410354.58>,1.8
  <1105442.06, 0.0, 6410354.58>,1.8
  <1105439.03, 0.0, 6410354.8>,1.8
  <1105439.03, 0.0, 6410354.8>,1.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=25859908 */
  <1106004.38, 0.0, 6410361.63>,1.5
  <1106004.38, 0.0, 6410361.63>,1.5
  <1106019.45, 0.0, 6410355.3>,1.5
  <1106019.45, 0.0, 6410355.3>,1.5
  tolerance 1
    texture {
        pigment {
            color rgb <0.7,0.8,0.7>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=25859908 */
  <1106004.38, 0.0, 6410361.63>,1.8
  <1106004.38, 0.0, 6410361.63>,1.8
  <1106019.45, 0.0, 6410355.3>,1.8
  <1106019.45, 0.0, 6410355.3>,1.8
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=58180049 */
  <1105639.64, 0.0, 6410471.16>,2.5
  <1105639.64, 0.0, 6410471.16>,2.5
  <1105641.78, 0.0, 6410410.89>,2.5
  <1105641.78, 0.0, 6410410.89>,2.5
  tolerance 1
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=58180049 */
  <1105639.64, 0.0, 6410471.16>,3.0
  <1105639.64, 0.0, 6410471.16>,3.0
  <1105641.78, 0.0, 6410410.89>,3.0
  <1105641.78, 0.0, 6410410.89>,3.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 5,
/* osm_id=30094857 */
  <1105925.85, 0.0, 6411033.61>,5.0
  <1105925.85, 0.0, 6411033.61>,5.0
  <1106012.73, 0.0, 6410906.14>,5.0
  <1106047.62, 0.0, 6410856.87>,5.0
  <1106047.62, 0.0, 6410856.87>,5.0
  tolerance 1
    texture {
        pigment {
            color rgb <1,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 5,
/* osm_id=30094857 */
  <1105925.85, 0.0, 6411033.61>,6.0
  <1105925.85, 0.0, 6411033.61>,6.0
  <1106012.73, 0.0, 6410906.14>,6.0
  <1106047.62, 0.0, 6410856.87>,6.0
  <1106047.62, 0.0, 6410856.87>,6.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=4482139 */
  <1105939.84, 0.0, 6411059.6>,5.0
  <1105939.84, 0.0, 6411059.6>,5.0
  <1105925.85, 0.0, 6411033.61>,5.0
  <1105925.85, 0.0, 6411033.61>,5.0
  tolerance 1
    texture {
        pigment {
            color rgb <1,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=4482139 */
  <1105939.84, 0.0, 6411059.6>,6.0
  <1105939.84, 0.0, 6411059.6>,6.0
  <1105925.85, 0.0, 6411033.61>,6.0
  <1105925.85, 0.0, 6411033.61>,6.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 8,
/* osm_id=24338192 */
  <1105983.7, 0.0, 6411326.22>,5.0
  <1105983.7, 0.0, 6411326.22>,5.0
  <1105980.73, 0.0, 6411312.9>,5.0
  <1105930.28, 0.0, 6411134.43>,5.0
  <1105933.15, 0.0, 6411105.32>,5.0
  <1105936.94, 0.0, 6411082.17>,5.0
  <1105939.84, 0.0, 6411059.6>,5.0
  <1105939.84, 0.0, 6411059.6>,5.0
  tolerance 1
    texture {
        pigment {
            color rgb <1,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 8,
/* osm_id=24338192 */
  <1105983.7, 0.0, 6411326.22>,6.0
  <1105983.7, 0.0, 6411326.22>,6.0
  <1105980.73, 0.0, 6411312.9>,6.0
  <1105930.28, 0.0, 6411134.43>,6.0
  <1105933.15, 0.0, 6411105.32>,6.0
  <1105936.94, 0.0, 6411082.17>,6.0
  <1105939.84, 0.0, 6411059.6>,6.0
  <1105939.84, 0.0, 6411059.6>,6.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 10,
/* osm_id=4454269 */
  <1106006.07, 0.0, 6410099.09>,5.0
  <1106006.07, 0.0, 6410099.09>,5.0
  <1106004.38, 0.0, 6410125.72>,5.0
  <1106008.47, 0.0, 6410153.6>,5.0
  <1106026.21, 0.0, 6410206.85>,5.0
  <1106043.61, 0.0, 6410257.08>,5.0
  <1106064.67, 0.0, 6410317.88>,5.0
  <1106098.07, 0.0, 6410410.13>,5.0
  <1106100.32, 0.0, 6410416.39>,5.0
  <1106100.32, 0.0, 6410416.39>,5.0
  tolerance 1
    texture {
        pigment {
            color rgb <1,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 10,
/* osm_id=4454269 */
  <1106006.07, 0.0, 6410099.09>,6.0
  <1106006.07, 0.0, 6410099.09>,6.0
  <1106004.38, 0.0, 6410125.72>,6.0
  <1106008.47, 0.0, 6410153.6>,6.0
  <1106026.21, 0.0, 6410206.85>,6.0
  <1106043.61, 0.0, 6410257.08>,6.0
  <1106064.67, 0.0, 6410317.88>,6.0
  <1106098.07, 0.0, 6410410.13>,6.0
  <1106100.32, 0.0, 6410416.39>,6.0
  <1106100.32, 0.0, 6410416.39>,6.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 9,
/* osm_id=30191718 */
  <1106047.62, 0.0, 6410856.87>,5.0
  <1106047.62, 0.0, 6410856.87>,5.0
  <1106078.73, 0.0, 6410809.48>,5.0
  <1106095.81, 0.0, 6410784.3>,5.0
  <1106102.07, 0.0, 6410775.54>,5.0
  <1106146.46, 0.0, 6410722.05>,5.0
  <1106187.71, 0.0, 6410668.35>,5.0
  <1106208.38, 0.0, 6410659.14>,5.0
  <1106208.38, 0.0, 6410659.14>,5.0
  tolerance 1
    texture {
        pigment {
            color rgb <1,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 9,
/* osm_id=30191718 */
  <1106047.62, 0.0, 6410856.87>,6.0
  <1106047.62, 0.0, 6410856.87>,6.0
  <1106078.73, 0.0, 6410809.48>,6.0
  <1106095.81, 0.0, 6410784.3>,6.0
  <1106102.07, 0.0, 6410775.54>,6.0
  <1106146.46, 0.0, 6410722.05>,6.0
  <1106187.71, 0.0, 6410668.35>,6.0
  <1106208.38, 0.0, 6410659.14>,6.0
  <1106208.38, 0.0, 6410659.14>,6.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

sphere_sweep { linear_spline, 4,
/* osm_id=45401885 */
  <1106097.74, 0.0, 6410899.32>,5.0
  <1106097.74, 0.0, 6410899.32>,5.0
  <1106047.62, 0.0, 6410856.87>,5.0
  <1106047.62, 0.0, 6410856.87>,5.0
  tolerance 1
    texture {
        pigment {
            color rgb <1,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
}

sphere_sweep { linear_spline, 4,
/* osm_id=45401885 */
  <1106097.74, 0.0, 6410899.32>,6.0
  <1106097.74, 0.0, 6410899.32>,6.0
  <1106047.62, 0.0, 6410856.87>,6.0
  <1106047.62, 0.0, 6410856.87>,6.0
  tolerance 1
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            /*reflection 0.5*/
        }
    }
    scale <1, 0.05, 1>
    translate <0, -0.05*2.0, 0>
}

prism { linear_spline 0, 0.01, 6,
/* osm_id=12710990 */
  <1105722.84, 6410727.6>,
  <1105738.79, 6410734.93>,
  <1105761.21, 6410728.03>,
  <1105736.05, 6410681.7>,
  <1105727.58, 6410708.2>,
  <1105722.84, 6410727.6>

    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    translate <0, 0.1, 0>
}

prism { linear_spline 0, 0.01, 5,
/* osm_id=15242512 */
  <1105193.84, 6410537.46>,
  <1105245.09, 6410542.03>,
  <1105251, 6410487.99>,
  <1105195.8, 6410483.89>,
  <1105193.84, 6410537.46>

    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    translate <0, 0.1, 0>
}

prism { linear_spline 0, 0.01, 17,
/* osm_id=13012701 */
  <1105225.3, 6410773.23>,
  <1105280.57, 6410776.06>,
  <1105278.94, 6410810.34>,
  <1105315.48, 6410824.72>,
  <1105313.96, 6410794.2>,
  <1105345.14, 6410793.99>,
  <1105370.61, 6410795.49>,
  <1105377.68, 6410800.77>,
  <1105398.62, 6410806.01>,
  <1105397.55, 6410792.41>,
  <1105403.94, 6410695.06>,
  <1105357.78, 6410690.61>,
  <1105293.38, 6410687.47>,
  <1105288.78, 6410746.83>,
  <1105230.97, 6410743.66>,
  <1105229.33, 6410753.5>,
  <1105225.3, 6410773.23>

    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    translate <0, 0.1, 0>
}

prism { linear_spline 0, 0.01, 6,
/* osm_id=13029705 */
  <1105251.54, 6410628.6>,
  <1105290.55, 6410628.39>,
  <1105299.96, 6410628.53>,
  <1105300.22, 6410599.16>,
  <1105255.99, 6410598.56>,
  <1105251.54, 6410628.6>

    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    translate <0, 0.1, 0>
}

prism { linear_spline 0, 0.01, 7,
/* osm_id=12711063 */
  <1105298.4, 6410883.86>,
  <1105326.81, 6410896.29>,
  <1105333.2, 6410874.56>,
  <1105357.02, 6410879.99>,
  <1105360.89, 6410837.48>,
  <1105319.71, 6410835.52>,
  <1105298.4, 6410883.86>

    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    translate <0, 0.1, 0>
}

prism { linear_spline 0, 0.01, 7,
/* osm_id=15259205 */
  <1105372.76, 6410548.26>,
  <1105417.67, 6410552.09>,
  <1105424.46, 6410502.68>,
  <1105393.96, 6410501.25>,
  <1105391.25, 6410518.86>,
  <1105376.01, 6410517.75>,
  <1105372.76, 6410548.26>

    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    translate <0, 0.1, 0>
}

prism { linear_spline 0, 0.01, 13,
/* osm_id=32441620 */
  <1105397.55, 6410792.41>,
  <1105398.62, 6410806.01>,
  <1105403.53, 6410810.25>,
  <1105406.8, 6410822.05>,
  <1105455.89, 6410836.45>,
  <1105508.58, 6410844.71>,
  <1105508.49, 6410837.05>,
  <1105507.12, 6410779.89>,
  <1105507.5, 6410757.76>,
  <1105486.5, 6410756.43>,
  <1105482.92, 6410756.78>,
  <1105478.67, 6410786.23>,
  <1105397.55, 6410792.41>

    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    translate <0, 0.1, 0>
}

prism { linear_spline 0, 0.01, 13,
/* osm_id=32461689 */
  <1105419.9, 6411217.96>,
  <1105573.49, 6411266.11>,
  <1105727.1, 6411314.54>,
  <1105738.55, 6411294.9>,
  <1105739.15, 6411293.88>,
  <1105751.01, 6411272.87>,
  <1105550.52, 6411211.26>,
  <1105525.47, 6411203>,
  <1105430.93, 6411174.45>,
  <1105427.03, 6411189.84>,
  <1105424.41, 6411200.18>,
  <1105424.19, 6411200.94>,
  <1105419.9, 6411217.96>

    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    translate <0, 0.1, 0>
}

prism { linear_spline 0, 0.01, 9,
/* osm_id=22662547 */
  <1105590.75, 6410601.82>,
  <1105591.16, 6410648>,
  <1105663.84, 6410663.24>,
  <1105675.19, 6410654.95>,
  <1105683.8, 6410650.26>,
  <1105662.29, 6410630.12>,
  <1105640.95, 6410618.15>,
  <1105641.27, 6410605.3>,
  <1105590.75, 6410601.82>

    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    translate <0, 0.1, 0>
}

prism { linear_spline 0, 0.01, 6,
/* osm_id=8106713 */
  <1105771.95, 6410555.14>,
  <1105795.25, 6410595.92>,
  <1105837.75, 6410596.18>,
  <1105845.73, 6410593.78>,
  <1105824.56, 6410538.02>,
  <1105771.95, 6410555.14>

    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    translate <0, 0.1, 0>
}

prism { linear_spline  0, 1, 5,
/* osm_id=58533287 */
  <1105243.07, 6410737.28>,
  <1105282.27, 6410740.38>,
  <1105285.82, 6410695.44>,
  <1105246.62, 6410692.34>,
  <1105243.07, 6410737.28>

    texture {
        pigment {
            color rgb <1,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 10.0, 1>
}

prism { linear_spline  0, 1, 40,
/* osm_id=-225641 */
  <1105356.89, 6411478.36>,
  <1105395.6, 6411487.86>,
  <1105397.75, 6411479.88>,
  <1105401.68, 6411465.32>,
  <1105408.76, 6411467.13>,
  <1105410.4, 6411460.86>,
  <1105416.23, 6411462.72>,
  <1105418.27, 6411457.72>,
  <1105431.78, 6411461.67>,
  <1105435.23, 6411449.85>,
  <1105421.97, 6411445.99>,
  <1105422.41, 6411444.47>,
  <1105449.25, 6411351.64>,
  <1105406.91, 6411339.65>,
  <1105413.02, 6411316.77>,
  <1105668.59, 6411393.11>,
  <1105689.74, 6411324.49>,
  <1105699.43, 6411327.61>,
  <1105678.28, 6411395.85>,
  <1105663.39, 6411444.4>,
  <1105661.81, 6411444.06>,
  <1105637.08, 6411516.46>,
  <1105659.83, 6411522.51>,
  <1105723.19, 6411318.85>,
  <1105418.5, 6411224.14>,
  <1105358.1, 6411456.89>,
  <1105361.5, 6411458.01>,
  <1105358.82, 6411469.79>,
  <1105356.89, 6411478.36>,
  <1105455.65, 6411295.04>,
  <1105465.39, 6411262.11>,
  <1105566.63, 6411292.19>,
  <1105663.12, 6411320.56>,
  <1105653.39, 6411353.5>,
  <1105455.65, 6411295.04>,
  <1105428.78, 6411256.49>,
  <1105431.68, 6411246.97>,
  <1105441.48, 6411249.96>,
  <1105438.57, 6411259.48>,
  <1105428.78, 6411256.49>

    texture {
        pigment {
            color rgb <1,0.6,0.6>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 10.0, 1>
}

prism { linear_spline  0, 1, 5,
/* osm_id=40895873 */
  <1105377.86, 6410516.05>,
  <1105389.53, 6410516.94>,
  <1105392.19, 6410496.92>,
  <1105380.22, 6410495.89>,
  <1105377.86, 6410516.05>

    texture {
        pigment {
            color rgb <1,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 10.0, 1>
}

prism { linear_spline  0, 1, 5,
/* osm_id=28412547 */
  <1105402.49, 6410868.94>,
  <1105437.58, 6410877.91>,
  <1105448.88, 6410838.81>,
  <1105408.81, 6410827.83>,
  <1105402.49, 6410868.94>

    texture {
        pigment {
            color rgb <1,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 10.0, 1>
}

prism { linear_spline  0, 1, 8,
/* osm_id=27137052 */
  <1105527.92, 6411202.46>,
  <1105550.83, 6411209.82>,
  <1105573.32, 6411217.05>,
  <1105587.26, 6411177.87>,
  <1105542.48, 6411161.62>,
  <1105541.5, 6411162.11>,
  <1105537.69, 6411164>,
  <1105527.92, 6411202.46>

    texture {
        pigment {
            color rgb <1,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 10.0, 1>
}

prism { linear_spline  0, 1, 18,
/* osm_id=26150815 */
  <1105589.88, 6410587.92>,
  <1105604.44, 6410587.96>,
  <1105604.37, 6410598.51>,
  <1105676.87, 6410599.08>,
  <1105676.61, 6410632.74>,
  <1105711.24, 6410633.02>,
  <1105711.52, 6410598.04>,
  <1105749.45, 6410598.35>,
  <1105749.68, 6410570.09>,
  <1105714.67, 6410569.8>,
  <1105714.97, 6410533.08>,
  <1105705.87, 6410533.02>,
  <1105678.72, 6410532.79>,
  <1105678.5, 6410560.64>,
  <1105604.09, 6410559.92>,
  <1105603.99, 6410571.47>,
  <1105589.93, 6410571.26>,
  <1105589.88, 6410587.92>

    texture {
        pigment {
            color rgb <1,1,0.6>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 65.0, 1>
}

prism { linear_spline  0, 1, 5,
/* osm_id=62148686 */
  <1105589.93, 6410571.26>,
  <1105603.99, 6410571.47>,
  <1105604.09, 6410559.92>,
  <1105589.96, 6410559.93>,
  <1105589.93, 6410571.26>

    texture {
        pigment {
            color rgb <1,1,0.6>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 100.0, 1>
}

prism { linear_spline  0, 1, 5,
/* osm_id=62148687 */
  <1105589.88, 6410587.92>,
  <1105589.88, 6410598.47>,
  <1105604.37, 6410598.51>,
  <1105604.44, 6410587.96>,
  <1105589.88, 6410587.92>

    texture {
        pigment {
            color rgb <1,1,0.6>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 100.0, 1>
}

prism { linear_spline  0, 1, 22,
/* osm_id=-901492 */
  <1105594.84, 6410559.99>,
  <1105604.09, 6410559.92>,
  <1105678.5, 6410560.64>,
  <1105678.72, 6410532.79>,
  <1105705.87, 6410533.02>,
  <1105706.36, 6410471.7>,
  <1105700.81, 6410471.64>,
  <1105639.64, 6410471.16>,
  <1105614.16, 6410470.95>,
  <1105614.35, 6410447.3>,
  <1105621.23, 6410447.35>,
  <1105621.33, 6410435.43>,
  <1105621.37, 6410428.76>,
  <1105595.89, 6410428.55>,
  <1105594.84, 6410559.99>,
  <1105610.63, 6410537.55>,
  <1105611.25, 6410490.39>,
  <1105680.41, 6410491.3>,
  <1105680.15, 6410511.17>,
  <1105667.03, 6410510.99>,
  <1105666.67, 6410538.29>,
  <1105610.63, 6410537.55>

    texture {
        pigment {
            color rgb <1,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 11.0, 1>
}

prism { linear_spline  0, 1, 6,
/* osm_id=27137602 */
  <1105848.11, 6410603.98>,
  <1105878.87, 6410669.85>,
  <1106008.32, 6410656.93>,
  <1105990.96, 6410564>,
  <1105924.28, 6410575.01>,
  <1105848.11, 6410603.98>

    texture {
        pigment {
            color rgb <1,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.2
            /*reflection 0.5*/
        }
    }
    scale <1, 10.0, 1>
}

