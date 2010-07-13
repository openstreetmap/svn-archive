
#include "colors.inc"
global_settings {
    assumed_gamma 2.0
    noise_generator 2

    ambient_light rgb <0.1,0.1,0.1>
    radiosity {
        count 30
        error_bound 0.01
        recursion_limit 3
        pretrace_end 0.0005
        brightness 1
    }
}

camera {
   orthographic
   location <0, 10000, 0>
   sky <0, 1, 0>
   direction <0, 0, 1>
   right <0.999999999991*1222.99245235, 0, 0>
   up <0, 1*1222.99245235*cos(radians(20)), 0> /* this stretches in y to compensate for the rotate below */
   look_at <0, 0, 0>
   rotate <-20,0,0>
   scale <1,1,1>
   translate <1106196.67314,0,6410314.93896>
}

/* ground */
box {
    <1105585.17692, -0.5, 6409703.44273>, <1106808.16937, -0.2, 6410926.43519>
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
        scale <1222.99245234,1,1222.99245235>
        translate <1105585.17692,0,6409703.44273>
    }
    
    finish {
        diffuse 0.8
        ambient 0.0
    }

}
/* sky */
sky_sphere {
    pigment {
        gradient y
        color_map {
/*
            [ 0.5 color rgb <0.5,0.6,0.6> ]
            [ 1.0 color rgb <0.5,0.5,0.6> ]
*/
            [ 0.5 color rgb <0.24,0.24,0.24> ]
            [ 1.0 color rgb <0.2,0.2,0.2> ]
        }
        scale 20000
        translate -10
    }
}

/* The Sun */
light_source {
    <0, 1000000,0>,
    rgb <1, 0.9, 0.8>
    area_light <100000, 0, 0>, <0, 0, 100000>, 3, 3
    adaptive 1
    circular
    rotate <45,10,0>
    translate <1106196.67314,0,6410314.93896>
}



/* Sky blue */
light_source {
    <0, 1000000,0>,
    rgb <0.04, 0.04, 0.2>
    area_light <1000000, 0, 0>, <0, 0, 1000000>, 3, 3
    adaptive 1
    circular
    translate <1106196.67314,0,6410314.93896>
}


#declare boundbox = box {
    <0, -1, 0>, <1, 1, 1>
    scale <1345.29169757,50,1345.29169758>
    translate <1105524.0273,0,6409642.29311>
}

#declare texture_highway_primary =
    texture {
        pigment {
            color rgb <1,1,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }
    }

#declare texture_highway_pedestrian =
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }
    }

#declare texture_highway_secondary_link =
    texture {
        pigment {
            color rgb <1,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }
    }

#declare texture_highway_tertiary =
    texture {
        pigment {
            color rgb <1,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }
    }

#declare texture_highway_primary_link =
    texture {
        pigment {
            color rgb <1,1,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }
    }

#declare texture_highway_service =
    texture {
        pigment {
            color rgb <0.8,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }
    }

#declare texture_highway_residential =
    texture {
        pigment {
            color rgb <0.9,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }
    }

#declare texture_highway_motorway_link =
    texture {
        pigment {
            color rgb <1,0,0>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }
    }

#declare texture_highway_cycleway =
    texture {
        pigment {
            color rgb <0.8,0.8,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }
    }

#declare texture_highway_trunk_link =
    texture {
        pigment {
            color rgb <0.9,1,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }
    }

#declare texture_highway_living_street =
    texture {
        pigment {
            color rgb <0.8,0.8,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }
    }

#declare texture_highway_track =
    texture {
        pigment {
            color rgb <0.7,0.7,0.6>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }
    }

#declare texture_highway_motorway =
    texture {
        pigment {
            color rgb <1,0,0>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }
    }

#declare texture_highway_trunk =
    texture {
        pigment {
            color rgb <0.9,1,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }
    }

#declare texture_highway_path =
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }
    }

#declare texture_highway_secondary =
    texture {
        pigment {
            color rgb <1,0.9,0.9>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }
    }

#declare texture_highway_footway =
    texture {
        pigment {
            color rgb <0.8,0.9,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }
    }

#declare texture_highway_unclassified =
    texture {
        pigment {
            color rgb <0.8,0.8,0.8>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }
    }

#declare texture_highway_bus_stop =
    texture {
        pigment {
            color rgb <1,0,0>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }
    }

#declare texture_highway_steps =
    texture {
        pigment {
            color rgb <0.6,0.7,0.6>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }
    }

#declare texture_highway_bus_station =
    texture {
        pigment {
            color rgb <1,0,0>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }
    }

#declare texture_highway_road =
    texture {
        pigment {
            color rgb <1,0,0>
        }
        finish {
            specular 0.5
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }
    }

#declare texture_highway_casing =
    texture {
        pigment {
            color rgb <0.3,0.3,0.3>
        }
        finish {
            specular 0.05
            roughness 0.05
            ambient 0.3
            /*reflection 0.5*/
        }
    }
/* osm_id=6110025 */
object {union { sphere { <1105612.41, 0, 6410869.42>,4.0 }
 sphere { <1105559.59, 0, 6410857.01>,4.0 }
 cylinder { <1105559.59, 0, 6410857.01>,<1105612.41, 0, 6410869.42>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_pedestrian }
}

object {union { sphere { <1105612.41, 0, 6410869.42>,4.8 }
 sphere { <1105559.59, 0, 6410857.01>,4.8 }
 cylinder { <1105559.59, 0, 6410857.01>,<1105612.41, 0, 6410869.42>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=27176391 */
object {union { sphere { <1105561.44, 0, 6410843.97>,4.0 }
 sphere { <1105606.87, 0, 6410856.23>,4.0 }
 cylinder { <1105606.87, 0, 6410856.23>,<1105561.44, 0, 6410843.97>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_pedestrian }
}

object {union { sphere { <1105561.44, 0, 6410843.97>,4.8 }
 sphere { <1105606.87, 0, 6410856.23>,4.8 }
 cylinder { <1105606.87, 0, 6410856.23>,<1105561.44, 0, 6410843.97>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=12710861 */
object {union { sphere { <1105609.48, 0, 6410863.39>,1.8 }
 sphere { <1105564.71, 0, 6410852.66>,1.8 }
 cylinder { <1105564.71, 0, 6410852.66>,<1105609.48, 0, 6410863.39>,1.8 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1105609.48, 0, 6410863.39>,2.16 }
 sphere { <1105564.71, 0, 6410852.66>,2.16 }
 cylinder { <1105564.71, 0, 6410852.66>,<1105609.48, 0, 6410863.39>,2.16 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=12710983 */
object {union { sphere { <1105595.68, 0, 6410230.97>,3.0 }
 sphere { <1105601.21, 0, 6410190.16>,3.0 }
 cylinder { <1105601.21, 0, 6410190.16>,<1105595.68, 0, 6410230.97>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1105595.68, 0, 6410230.97>,3.6 }
 sphere { <1105601.21, 0, 6410190.16>,3.6 }
 cylinder { <1105601.21, 0, 6410190.16>,<1105595.68, 0, 6410230.97>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10152037 */
object {union { sphere { <1105776.97, 0, 6409908.32>,3.0 }
 sphere { <1105792.95, 0, 6409852.02>,3.0 }
 cylinder { <1105792.95, 0, 6409852.02>,<1105776.97, 0, 6409908.32>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1105776.97, 0, 6409908.32>,3.6 }
 sphere { <1105792.95, 0, 6409852.02>,3.6 }
 cylinder { <1105792.95, 0, 6409852.02>,<1105776.97, 0, 6409908.32>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10152039 */
object {union { sphere { <1105818.34, 0, 6409941.41>,3.0 }
 sphere { <1105805.11, 0, 6409953.75>,3.0 }
 cylinder { <1105805.11, 0, 6409953.75>,<1105818.34, 0, 6409941.41>,3.0 }
 sphere { <1105789.91, 0, 6409981.2>,3.0 }
 cylinder { <1105789.91, 0, 6409981.2>,<1105805.11, 0, 6409953.75>,3.0 }
 sphere { <1105793.72, 0, 6410019.66>,3.0 }
 cylinder { <1105793.72, 0, 6410019.66>,<1105789.91, 0, 6409981.2>,3.0 }
 sphere { <1105796.67, 0, 6410051.35>,3.0 }
 cylinder { <1105796.67, 0, 6410051.35>,<1105793.72, 0, 6410019.66>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1105818.34, 0, 6409941.41>,3.6 }
 sphere { <1105805.11, 0, 6409953.75>,3.6 }
 cylinder { <1105805.11, 0, 6409953.75>,<1105818.34, 0, 6409941.41>,3.6 }
 sphere { <1105789.91, 0, 6409981.2>,3.6 }
 cylinder { <1105789.91, 0, 6409981.2>,<1105805.11, 0, 6409953.75>,3.6 }
 sphere { <1105793.72, 0, 6410019.66>,3.6 }
 cylinder { <1105793.72, 0, 6410019.66>,<1105789.91, 0, 6409981.2>,3.6 }
 sphere { <1105796.67, 0, 6410051.35>,3.6 }
 cylinder { <1105796.67, 0, 6410051.35>,<1105793.72, 0, 6410019.66>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10152038 */
object {union { sphere { <1105799.25, 0, 6409926.51>,3.0 }
 sphere { <1105816.44, 0, 6409858.33>,3.0 }
 cylinder { <1105816.44, 0, 6409858.33>,<1105799.25, 0, 6409926.51>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1105799.25, 0, 6409926.51>,3.6 }
 sphere { <1105816.44, 0, 6409858.33>,3.6 }
 cylinder { <1105816.44, 0, 6409858.33>,<1105799.25, 0, 6409926.51>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=32297648 */
object {union { sphere { <1105939.79, 0, 6410014.78>,3.0 }
 sphere { <1105940.97, 0, 6409977.36>,3.0 }
 cylinder { <1105940.97, 0, 6409977.36>,<1105939.79, 0, 6410014.78>,3.0 }
 sphere { <1105931.53, 0, 6409976.81>,3.0 }
 cylinder { <1105931.53, 0, 6409976.81>,<1105940.97, 0, 6409977.36>,3.0 }
 sphere { <1105921.61, 0, 6409976.25>,3.0 }
 cylinder { <1105921.61, 0, 6409976.25>,<1105931.53, 0, 6409976.81>,3.0 }
 sphere { <1105903.06, 0, 6409975.29>,3.0 }
 cylinder { <1105903.06, 0, 6409975.29>,<1105921.61, 0, 6409976.25>,3.0 }
 sphere { <1105896.86, 0, 6409979.77>,3.0 }
 cylinder { <1105896.86, 0, 6409979.77>,<1105903.06, 0, 6409975.29>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1105939.79, 0, 6410014.78>,3.6 }
 sphere { <1105940.97, 0, 6409977.36>,3.6 }
 cylinder { <1105940.97, 0, 6409977.36>,<1105939.79, 0, 6410014.78>,3.6 }
 sphere { <1105931.53, 0, 6409976.81>,3.6 }
 cylinder { <1105931.53, 0, 6409976.81>,<1105940.97, 0, 6409977.36>,3.6 }
 sphere { <1105921.61, 0, 6409976.25>,3.6 }
 cylinder { <1105921.61, 0, 6409976.25>,<1105931.53, 0, 6409976.81>,3.6 }
 sphere { <1105903.06, 0, 6409975.29>,3.6 }
 cylinder { <1105903.06, 0, 6409975.29>,<1105921.61, 0, 6409976.25>,3.6 }
 sphere { <1105896.86, 0, 6409979.77>,3.6 }
 cylinder { <1105896.86, 0, 6409979.77>,<1105903.06, 0, 6409975.29>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=12710977 */
object {union { sphere { <1105976.42, 0, 6410561.28>,3.0 }
 sphere { <1105971.45, 0, 6410539.05>,3.0 }
 cylinder { <1105971.45, 0, 6410539.05>,<1105976.42, 0, 6410561.28>,3.0 }
 sphere { <1106000.02, 0, 6410528.17>,3.0 }
 cylinder { <1106000.02, 0, 6410528.17>,<1105971.45, 0, 6410539.05>,3.0 }
 sphere { <1106009.23, 0, 6410553.07>,3.0 }
 cylinder { <1106009.23, 0, 6410553.07>,<1106000.02, 0, 6410528.17>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1105976.42, 0, 6410561.28>,3.6 }
 sphere { <1105971.45, 0, 6410539.05>,3.6 }
 cylinder { <1105971.45, 0, 6410539.05>,<1105976.42, 0, 6410561.28>,3.6 }
 sphere { <1106000.02, 0, 6410528.17>,3.6 }
 cylinder { <1106000.02, 0, 6410528.17>,<1105971.45, 0, 6410539.05>,3.6 }
 sphere { <1106009.23, 0, 6410553.07>,3.6 }
 cylinder { <1106009.23, 0, 6410553.07>,<1106000.02, 0, 6410528.17>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=43860425 */
object {union { sphere { <1106000.02, 0, 6410528.17>,3.0 }
 sphere { <1105973.77, 0, 6410455.8>,3.0 }
 cylinder { <1105973.77, 0, 6410455.8>,<1106000.02, 0, 6410528.17>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106000.02, 0, 6410528.17>,3.6 }
 sphere { <1105973.77, 0, 6410455.8>,3.6 }
 cylinder { <1105973.77, 0, 6410455.8>,<1106000.02, 0, 6410528.17>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=12710954 */
object {union { sphere { <1105989.15, 0, 6410785.18>,3.0 }
 sphere { <1106031.68, 0, 6410816.79>,3.0 }
 cylinder { <1106031.68, 0, 6410816.79>,<1105989.15, 0, 6410785.18>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1105989.15, 0, 6410785.18>,3.6 }
 sphere { <1106031.68, 0, 6410816.79>,3.6 }
 cylinder { <1106031.68, 0, 6410816.79>,<1105989.15, 0, 6410785.18>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=12710956 */
object {union { sphere { <1106003.45, 0, 6410767.11>,3.0 }
 sphere { <1106053.88, 0, 6410803.99>,3.0 }
 cylinder { <1106053.88, 0, 6410803.99>,<1106003.45, 0, 6410767.11>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106003.45, 0, 6410767.11>,3.6 }
 sphere { <1106053.88, 0, 6410803.99>,3.6 }
 cylinder { <1106053.88, 0, 6410803.99>,<1106003.45, 0, 6410767.11>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=49264208 */
object {union { sphere { <1106029.47, 0, 6410262.58>,3.0 }
 sphere { <1106004.08, 0, 6410188.76>,3.0 }
 cylinder { <1106004.08, 0, 6410188.76>,<1106029.47, 0, 6410262.58>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106029.47, 0, 6410262.58>,3.6 }
 sphere { <1106004.08, 0, 6410188.76>,3.6 }
 cylinder { <1106004.08, 0, 6410188.76>,<1106029.47, 0, 6410262.58>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10152027 */
object {union { sphere { <1106061.37, 0, 6409776.46>,3.0 }
 sphere { <1106013.38, 0, 6409741.89>,3.0 }
 cylinder { <1106013.38, 0, 6409741.89>,<1106061.37, 0, 6409776.46>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106061.37, 0, 6409776.46>,3.6 }
 sphere { <1106013.38, 0, 6409741.89>,3.6 }
 cylinder { <1106013.38, 0, 6409741.89>,<1106061.37, 0, 6409776.46>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=12710959 */
object {union { sphere { <1106021.14, 0, 6410747.17>,3.0 }
 sphere { <1106072.33, 0, 6410787.06>,3.0 }
 cylinder { <1106072.33, 0, 6410787.06>,<1106021.14, 0, 6410747.17>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106021.14, 0, 6410747.17>,3.6 }
 sphere { <1106072.33, 0, 6410787.06>,3.6 }
 cylinder { <1106072.33, 0, 6410787.06>,<1106021.14, 0, 6410747.17>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=49264210 */
object {union { sphere { <1106043.61, 0, 6410257.08>,3.0 }
 sphere { <1106029.47, 0, 6410262.58>,3.0 }
 cylinder { <1106029.47, 0, 6410262.58>,<1106043.61, 0, 6410257.08>,3.0 }
 sphere { <1106047.78, 0, 6410315.14>,3.0 }
 cylinder { <1106047.78, 0, 6410315.14>,<1106029.47, 0, 6410262.58>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106043.61, 0, 6410257.08>,3.6 }
 sphere { <1106029.47, 0, 6410262.58>,3.6 }
 cylinder { <1106029.47, 0, 6410262.58>,<1106043.61, 0, 6410257.08>,3.6 }
 sphere { <1106047.78, 0, 6410315.14>,3.6 }
 cylinder { <1106047.78, 0, 6410315.14>,<1106029.47, 0, 6410262.58>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=36992693 */
object {union { sphere { <1106036.19, 0, 6410046.99>,3.0 }
 sphere { <1106074.18, 0, 6410049.16>,3.0 }
 cylinder { <1106074.18, 0, 6410049.16>,<1106036.19, 0, 6410046.99>,3.0 }
 sphere { <1106112.85, 0, 6410050.74>,3.0 }
 cylinder { <1106112.85, 0, 6410050.74>,<1106074.18, 0, 6410049.16>,3.0 }
 sphere { <1106142.9, 0, 6410051.99>,3.0 }
 cylinder { <1106142.9, 0, 6410051.99>,<1106112.85, 0, 6410050.74>,3.0 }
 sphere { <1106160.23, 0, 6410050.62>,3.0 }
 cylinder { <1106160.23, 0, 6410050.62>,<1106142.9, 0, 6410051.99>,3.0 }
 sphere { <1106167.27, 0, 6410045.18>,3.0 }
 cylinder { <1106167.27, 0, 6410045.18>,<1106160.23, 0, 6410050.62>,3.0 }
 sphere { <1106173.78, 0, 6410026.4>,3.0 }
 cylinder { <1106173.78, 0, 6410026.4>,<1106167.27, 0, 6410045.18>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106036.19, 0, 6410046.99>,3.6 }
 sphere { <1106074.18, 0, 6410049.16>,3.6 }
 cylinder { <1106074.18, 0, 6410049.16>,<1106036.19, 0, 6410046.99>,3.6 }
 sphere { <1106112.85, 0, 6410050.74>,3.6 }
 cylinder { <1106112.85, 0, 6410050.74>,<1106074.18, 0, 6410049.16>,3.6 }
 sphere { <1106142.9, 0, 6410051.99>,3.6 }
 cylinder { <1106142.9, 0, 6410051.99>,<1106112.85, 0, 6410050.74>,3.6 }
 sphere { <1106160.23, 0, 6410050.62>,3.6 }
 cylinder { <1106160.23, 0, 6410050.62>,<1106142.9, 0, 6410051.99>,3.6 }
 sphere { <1106167.27, 0, 6410045.18>,3.6 }
 cylinder { <1106167.27, 0, 6410045.18>,<1106160.23, 0, 6410050.62>,3.6 }
 sphere { <1106173.78, 0, 6410026.4>,3.6 }
 cylinder { <1106173.78, 0, 6410026.4>,<1106167.27, 0, 6410045.18>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59005252 */
object {union { sphere { <1106100.32, 0, 6410416.39>,3.0 }
 sphere { <1106115.41, 0, 6410411.48>,3.0 }
 cylinder { <1106115.41, 0, 6410411.48>,<1106100.32, 0, 6410416.39>,3.0 }
 sphere { <1106119.1, 0, 6410410.27>,3.0 }
 cylinder { <1106119.1, 0, 6410410.27>,<1106115.41, 0, 6410411.48>,3.0 }
 sphere { <1106241.71, 0, 6410372.97>,3.0 }
 cylinder { <1106241.71, 0, 6410372.97>,<1106119.1, 0, 6410410.27>,3.0 }
 sphere { <1106250.43, 0, 6410399.27>,3.0 }
 cylinder { <1106250.43, 0, 6410399.27>,<1106241.71, 0, 6410372.97>,3.0 }
 sphere { <1106256.76, 0, 6410418.12>,3.0 }
 cylinder { <1106256.76, 0, 6410418.12>,<1106250.43, 0, 6410399.27>,3.0 }
 sphere { <1106260.91, 0, 6410430.91>,3.0 }
 cylinder { <1106260.91, 0, 6410430.91>,<1106256.76, 0, 6410418.12>,3.0 }
 sphere { <1106272.51, 0, 6410465.94>,3.0 }
 cylinder { <1106272.51, 0, 6410465.94>,<1106260.91, 0, 6410430.91>,3.0 }
 sphere { <1106276.36, 0, 6410497.22>,3.0 }
 cylinder { <1106276.36, 0, 6410497.22>,<1106272.51, 0, 6410465.94>,3.0 }
 sphere { <1106276.67, 0, 6410507.89>,3.0 }
 cylinder { <1106276.67, 0, 6410507.89>,<1106276.36, 0, 6410497.22>,3.0 }
 sphere { <1106282.35, 0, 6410516.31>,3.0 }
 cylinder { <1106282.35, 0, 6410516.31>,<1106276.67, 0, 6410507.89>,3.0 }
 sphere { <1106298.38, 0, 6410544.03>,3.0 }
 cylinder { <1106298.38, 0, 6410544.03>,<1106282.35, 0, 6410516.31>,3.0 }
 sphere { <1106308.08, 0, 6410573.33>,3.0 }
 cylinder { <1106308.08, 0, 6410573.33>,<1106298.38, 0, 6410544.03>,3.0 }
 sphere { <1106312.56, 0, 6410585.99>,3.0 }
 cylinder { <1106312.56, 0, 6410585.99>,<1106308.08, 0, 6410573.33>,3.0 }
 sphere { <1106317.44, 0, 6410601.58>,3.0 }
 cylinder { <1106317.44, 0, 6410601.58>,<1106312.56, 0, 6410585.99>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106100.32, 0, 6410416.39>,3.6 }
 sphere { <1106115.41, 0, 6410411.48>,3.6 }
 cylinder { <1106115.41, 0, 6410411.48>,<1106100.32, 0, 6410416.39>,3.6 }
 sphere { <1106119.1, 0, 6410410.27>,3.6 }
 cylinder { <1106119.1, 0, 6410410.27>,<1106115.41, 0, 6410411.48>,3.6 }
 sphere { <1106241.71, 0, 6410372.97>,3.6 }
 cylinder { <1106241.71, 0, 6410372.97>,<1106119.1, 0, 6410410.27>,3.6 }
 sphere { <1106250.43, 0, 6410399.27>,3.6 }
 cylinder { <1106250.43, 0, 6410399.27>,<1106241.71, 0, 6410372.97>,3.6 }
 sphere { <1106256.76, 0, 6410418.12>,3.6 }
 cylinder { <1106256.76, 0, 6410418.12>,<1106250.43, 0, 6410399.27>,3.6 }
 sphere { <1106260.91, 0, 6410430.91>,3.6 }
 cylinder { <1106260.91, 0, 6410430.91>,<1106256.76, 0, 6410418.12>,3.6 }
 sphere { <1106272.51, 0, 6410465.94>,3.6 }
 cylinder { <1106272.51, 0, 6410465.94>,<1106260.91, 0, 6410430.91>,3.6 }
 sphere { <1106276.36, 0, 6410497.22>,3.6 }
 cylinder { <1106276.36, 0, 6410497.22>,<1106272.51, 0, 6410465.94>,3.6 }
 sphere { <1106276.67, 0, 6410507.89>,3.6 }
 cylinder { <1106276.67, 0, 6410507.89>,<1106276.36, 0, 6410497.22>,3.6 }
 sphere { <1106282.35, 0, 6410516.31>,3.6 }
 cylinder { <1106282.35, 0, 6410516.31>,<1106276.67, 0, 6410507.89>,3.6 }
 sphere { <1106298.38, 0, 6410544.03>,3.6 }
 cylinder { <1106298.38, 0, 6410544.03>,<1106282.35, 0, 6410516.31>,3.6 }
 sphere { <1106308.08, 0, 6410573.33>,3.6 }
 cylinder { <1106308.08, 0, 6410573.33>,<1106298.38, 0, 6410544.03>,3.6 }
 sphere { <1106312.56, 0, 6410585.99>,3.6 }
 cylinder { <1106312.56, 0, 6410585.99>,<1106308.08, 0, 6410573.33>,3.6 }
 sphere { <1106317.44, 0, 6410601.58>,3.6 }
 cylinder { <1106317.44, 0, 6410601.58>,<1106312.56, 0, 6410585.99>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59005265 */
object {union { sphere { <1106250.43, 0, 6410399.27>,3.0 }
 sphere { <1106139.61, 0, 6410430.74>,3.0 }
 cylinder { <1106139.61, 0, 6410430.74>,<1106250.43, 0, 6410399.27>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106250.43, 0, 6410399.27>,3.6 }
 sphere { <1106139.61, 0, 6410430.74>,3.6 }
 cylinder { <1106139.61, 0, 6410430.74>,<1106250.43, 0, 6410399.27>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=12710948 */
object {union { sphere { <1106251.12, 0, 6410816.41>,3.0 }
 sphere { <1106146.46, 0, 6410722.05>,3.0 }
 cylinder { <1106146.46, 0, 6410722.05>,<1106251.12, 0, 6410816.41>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106251.12, 0, 6410816.41>,3.6 }
 sphere { <1106146.46, 0, 6410722.05>,3.6 }
 cylinder { <1106146.46, 0, 6410722.05>,<1106251.12, 0, 6410816.41>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59005247 */
object {union { sphere { <1106260.91, 0, 6410430.91>,3.0 }
 sphere { <1106148.55, 0, 6410462.95>,3.0 }
 cylinder { <1106148.55, 0, 6410462.95>,<1106260.91, 0, 6410430.91>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106260.91, 0, 6410430.91>,3.6 }
 sphere { <1106148.55, 0, 6410462.95>,3.6 }
 cylinder { <1106148.55, 0, 6410462.95>,<1106260.91, 0, 6410430.91>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10202686 */
object {union { sphere { <1106158.13, 0, 6410497.51>,3.0 }
 sphere { <1106272.51, 0, 6410465.94>,3.0 }
 cylinder { <1106272.51, 0, 6410465.94>,<1106158.13, 0, 6410497.51>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106158.13, 0, 6410497.51>,3.6 }
 sphere { <1106272.51, 0, 6410465.94>,3.6 }
 cylinder { <1106272.51, 0, 6410465.94>,<1106158.13, 0, 6410497.51>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=12710943 */
object {union { sphere { <1106203.73, 0, 6410863.18>,3.0 }
 sphere { <1106251.12, 0, 6410816.41>,3.0 }
 cylinder { <1106251.12, 0, 6410816.41>,<1106203.73, 0, 6410863.18>,3.0 }
 sphere { <1106296.79, 0, 6410787.56>,3.0 }
 cylinder { <1106296.79, 0, 6410787.56>,<1106251.12, 0, 6410816.41>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106203.73, 0, 6410863.18>,3.6 }
 sphere { <1106251.12, 0, 6410816.41>,3.6 }
 cylinder { <1106251.12, 0, 6410816.41>,<1106203.73, 0, 6410863.18>,3.6 }
 sphere { <1106296.79, 0, 6410787.56>,3.6 }
 cylinder { <1106296.79, 0, 6410787.56>,<1106251.12, 0, 6410816.41>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59005147 */
object {union { sphere { <1106208.01, 0, 6410571.47>,3.0 }
 sphere { <1106298.38, 0, 6410544.03>,3.0 }
 cylinder { <1106298.38, 0, 6410544.03>,<1106208.01, 0, 6410571.47>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106208.01, 0, 6410571.47>,3.6 }
 sphere { <1106298.38, 0, 6410544.03>,3.6 }
 cylinder { <1106298.38, 0, 6410544.03>,<1106208.01, 0, 6410571.47>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59005270 */
object {union { sphere { <1106308.08, 0, 6410573.33>,3.0 }
 sphere { <1106219.94, 0, 6410600.11>,3.0 }
 cylinder { <1106219.94, 0, 6410600.11>,<1106308.08, 0, 6410573.33>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106308.08, 0, 6410573.33>,3.6 }
 sphere { <1106219.94, 0, 6410600.11>,3.6 }
 cylinder { <1106219.94, 0, 6410600.11>,<1106308.08, 0, 6410573.33>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59306161 */
object {union { sphere { <1106317.44, 0, 6410601.58>,3.0 }
 sphere { <1106240.06, 0, 6410623.81>,3.0 }
 cylinder { <1106240.06, 0, 6410623.81>,<1106317.44, 0, 6410601.58>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106317.44, 0, 6410601.58>,3.6 }
 sphere { <1106240.06, 0, 6410623.81>,3.6 }
 cylinder { <1106240.06, 0, 6410623.81>,<1106317.44, 0, 6410601.58>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59005237 */
object {union { sphere { <1106256.76, 0, 6410418.12>,3.0 }
 sphere { <1106278.43, 0, 6410409.95>,3.0 }
 cylinder { <1106278.43, 0, 6410409.95>,<1106256.76, 0, 6410418.12>,3.0 }
 sphere { <1106283.1, 0, 6410408.08>,3.0 }
 cylinder { <1106283.1, 0, 6410408.08>,<1106278.43, 0, 6410409.95>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106256.76, 0, 6410418.12>,3.6 }
 sphere { <1106278.43, 0, 6410409.95>,3.6 }
 cylinder { <1106278.43, 0, 6410409.95>,<1106256.76, 0, 6410418.12>,3.6 }
 sphere { <1106283.1, 0, 6410408.08>,3.6 }
 cylinder { <1106283.1, 0, 6410408.08>,<1106278.43, 0, 6410409.95>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59005264 */
object {union { sphere { <1106283.1, 0, 6410408.08>,3.0 }
 sphere { <1106313.19, 0, 6410398.89>,3.0 }
 cylinder { <1106313.19, 0, 6410398.89>,<1106283.1, 0, 6410408.08>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106283.1, 0, 6410408.08>,3.6 }
 sphere { <1106313.19, 0, 6410398.89>,3.6 }
 cylinder { <1106313.19, 0, 6410398.89>,<1106283.1, 0, 6410408.08>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59005234 */
object {union { sphere { <1106312.56, 0, 6410585.99>,3.0 }
 sphere { <1106336.48, 0, 6410578.2>,3.0 }
 cylinder { <1106336.48, 0, 6410578.2>,<1106312.56, 0, 6410585.99>,3.0 }
 sphere { <1106340.08, 0, 6410577.42>,3.0 }
 cylinder { <1106340.08, 0, 6410577.42>,<1106336.48, 0, 6410578.2>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106312.56, 0, 6410585.99>,3.6 }
 sphere { <1106336.48, 0, 6410578.2>,3.6 }
 cylinder { <1106336.48, 0, 6410578.2>,<1106312.56, 0, 6410585.99>,3.6 }
 sphere { <1106340.08, 0, 6410577.42>,3.6 }
 cylinder { <1106340.08, 0, 6410577.42>,<1106336.48, 0, 6410578.2>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59005239 */
object {union { sphere { <1106313.19, 0, 6410398.89>,3.0 }
 sphere { <1106347.66, 0, 6410388.36>,3.0 }
 cylinder { <1106347.66, 0, 6410388.36>,<1106313.19, 0, 6410398.89>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106313.19, 0, 6410398.89>,3.6 }
 sphere { <1106347.66, 0, 6410388.36>,3.6 }
 cylinder { <1106347.66, 0, 6410388.36>,<1106313.19, 0, 6410398.89>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=39986019 */
object {union { sphere { <1106539.18, 0, 6410621.62>,3.0 }
 sphere { <1106472.91, 0, 6410660.62>,3.0 }
 cylinder { <1106472.91, 0, 6410660.62>,<1106539.18, 0, 6410621.62>,3.0 }
 sphere { <1106316.26, 0, 6410698.77>,3.0 }
 cylinder { <1106316.26, 0, 6410698.77>,<1106472.91, 0, 6410660.62>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106539.18, 0, 6410621.62>,3.6 }
 sphere { <1106472.91, 0, 6410660.62>,3.6 }
 cylinder { <1106472.91, 0, 6410660.62>,<1106539.18, 0, 6410621.62>,3.6 }
 sphere { <1106316.26, 0, 6410698.77>,3.6 }
 cylinder { <1106316.26, 0, 6410698.77>,<1106472.91, 0, 6410660.62>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59005255 */
object {union { sphere { <1106340.08, 0, 6410577.42>,3.0 }
 sphere { <1106362.17, 0, 6410570.68>,3.0 }
 cylinder { <1106362.17, 0, 6410570.68>,<1106340.08, 0, 6410577.42>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106340.08, 0, 6410577.42>,3.6 }
 sphere { <1106362.17, 0, 6410570.68>,3.6 }
 cylinder { <1106362.17, 0, 6410570.68>,<1106340.08, 0, 6410577.42>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59005230 */
object {union { sphere { <1106347.66, 0, 6410388.36>,3.0 }
 sphere { <1106370.18, 0, 6410381.48>,3.0 }
 cylinder { <1106370.18, 0, 6410381.48>,<1106347.66, 0, 6410388.36>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106347.66, 0, 6410388.36>,3.6 }
 sphere { <1106370.18, 0, 6410381.48>,3.6 }
 cylinder { <1106370.18, 0, 6410381.48>,<1106347.66, 0, 6410388.36>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59005273 */
object {union { sphere { <1106362.17, 0, 6410570.68>,3.0 }
 sphere { <1106396.73, 0, 6410560.11>,3.0 }
 cylinder { <1106396.73, 0, 6410560.11>,<1106362.17, 0, 6410570.68>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106362.17, 0, 6410570.68>,3.6 }
 sphere { <1106396.73, 0, 6410560.11>,3.6 }
 cylinder { <1106396.73, 0, 6410560.11>,<1106362.17, 0, 6410570.68>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59005235 */
object {union { sphere { <1106370.18, 0, 6410381.48>,3.0 }
 sphere { <1106394.35, 0, 6410374.1>,3.0 }
 cylinder { <1106394.35, 0, 6410374.1>,<1106370.18, 0, 6410381.48>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106370.18, 0, 6410381.48>,3.6 }
 sphere { <1106394.35, 0, 6410374.1>,3.6 }
 cylinder { <1106394.35, 0, 6410374.1>,<1106370.18, 0, 6410381.48>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59005268 */
object {union { sphere { <1106396.73, 0, 6410560.11>,3.0 }
 sphere { <1106422.7, 0, 6410552.17>,3.0 }
 cylinder { <1106422.7, 0, 6410552.17>,<1106396.73, 0, 6410560.11>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106396.73, 0, 6410560.11>,3.6 }
 sphere { <1106422.7, 0, 6410552.17>,3.6 }
 cylinder { <1106422.7, 0, 6410552.17>,<1106396.73, 0, 6410560.11>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59005246 */
object {union { sphere { <1106422.7, 0, 6410552.17>,3.0 }
 sphere { <1106446.07, 0, 6410545.03>,3.0 }
 cylinder { <1106446.07, 0, 6410545.03>,<1106422.7, 0, 6410552.17>,3.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_service }
}

object {union { sphere { <1106422.7, 0, 6410552.17>,3.6 }
 sphere { <1106446.07, 0, 6410545.03>,3.6 }
 cylinder { <1106446.07, 0, 6410545.03>,<1106422.7, 0, 6410552.17>,3.6 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=11299280 */
object {union { sphere { <1105316.69, 0, 6410035.86>,4.0 }
 sphere { <1105320.52, 0, 6410035.86>,4.0 }
 cylinder { <1105320.52, 0, 6410035.86>,<1105316.69, 0, 6410035.86>,4.0 }
 sphere { <1105328.57, 0, 6410037.02>,4.0 }
 cylinder { <1105328.57, 0, 6410037.02>,<1105320.52, 0, 6410035.86>,4.0 }
 sphere { <1105367.83, 0, 6410037>,4.0 }
 cylinder { <1105367.83, 0, 6410037>,<1105328.57, 0, 6410037.02>,4.0 }
 sphere { <1105421.85, 0, 6410037.76>,4.0 }
 cylinder { <1105421.85, 0, 6410037.76>,<1105367.83, 0, 6410037>,4.0 }
 sphere { <1105481.55, 0, 6410041.09>,4.0 }
 cylinder { <1105481.55, 0, 6410041.09>,<1105421.85, 0, 6410037.76>,4.0 }
 sphere { <1105590.35, 0, 6410041.83>,4.0 }
 cylinder { <1105590.35, 0, 6410041.83>,<1105481.55, 0, 6410041.09>,4.0 }
 sphere { <1105618.91, 0, 6410042.43>,4.0 }
 cylinder { <1105618.91, 0, 6410042.43>,<1105590.35, 0, 6410041.83>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105316.69, 0, 6410035.86>,4.8 }
 sphere { <1105320.52, 0, 6410035.86>,4.8 }
 cylinder { <1105320.52, 0, 6410035.86>,<1105316.69, 0, 6410035.86>,4.8 }
 sphere { <1105328.57, 0, 6410037.02>,4.8 }
 cylinder { <1105328.57, 0, 6410037.02>,<1105320.52, 0, 6410035.86>,4.8 }
 sphere { <1105367.83, 0, 6410037>,4.8 }
 cylinder { <1105367.83, 0, 6410037>,<1105328.57, 0, 6410037.02>,4.8 }
 sphere { <1105421.85, 0, 6410037.76>,4.8 }
 cylinder { <1105421.85, 0, 6410037.76>,<1105367.83, 0, 6410037>,4.8 }
 sphere { <1105481.55, 0, 6410041.09>,4.8 }
 cylinder { <1105481.55, 0, 6410041.09>,<1105421.85, 0, 6410037.76>,4.8 }
 sphere { <1105590.35, 0, 6410041.83>,4.8 }
 cylinder { <1105590.35, 0, 6410041.83>,<1105481.55, 0, 6410041.09>,4.8 }
 sphere { <1105618.91, 0, 6410042.43>,4.8 }
 cylinder { <1105618.91, 0, 6410042.43>,<1105590.35, 0, 6410041.83>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10152015 */
object {union { sphere { <1105385.16, 0, 6409964.15>,4.0 }
 sphere { <1105391.67, 0, 6409964.32>,4.0 }
 cylinder { <1105391.67, 0, 6409964.32>,<1105385.16, 0, 6409964.15>,4.0 }
 sphere { <1105491.53, 0, 6409966.29>,4.0 }
 cylinder { <1105491.53, 0, 6409966.29>,<1105391.67, 0, 6409964.32>,4.0 }
 sphere { <1105577.05, 0, 6409962.89>,4.0 }
 cylinder { <1105577.05, 0, 6409962.89>,<1105491.53, 0, 6409966.29>,4.0 }
 sphere { <1105588.55, 0, 6409962.49>,4.0 }
 cylinder { <1105588.55, 0, 6409962.49>,<1105577.05, 0, 6409962.89>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105385.16, 0, 6409964.15>,4.8 }
 sphere { <1105391.67, 0, 6409964.32>,4.8 }
 cylinder { <1105391.67, 0, 6409964.32>,<1105385.16, 0, 6409964.15>,4.8 }
 sphere { <1105491.53, 0, 6409966.29>,4.8 }
 cylinder { <1105491.53, 0, 6409966.29>,<1105391.67, 0, 6409964.32>,4.8 }
 sphere { <1105577.05, 0, 6409962.89>,4.8 }
 cylinder { <1105577.05, 0, 6409962.89>,<1105491.53, 0, 6409966.29>,4.8 }
 sphere { <1105588.55, 0, 6409962.49>,4.8 }
 cylinder { <1105588.55, 0, 6409962.49>,<1105577.05, 0, 6409962.89>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=11299268 */
object {union { sphere { <1105602.43, 0, 6410231.11>,4.0 }
 sphere { <1105595.68, 0, 6410230.97>,4.0 }
 cylinder { <1105595.68, 0, 6410230.97>,<1105602.43, 0, 6410231.11>,4.0 }
 sphere { <1105564.61, 0, 6410235.44>,4.0 }
 cylinder { <1105564.61, 0, 6410235.44>,<1105595.68, 0, 6410230.97>,4.0 }
 sphere { <1105451.82, 0, 6410230.89>,4.0 }
 cylinder { <1105451.82, 0, 6410230.89>,<1105564.61, 0, 6410235.44>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105602.43, 0, 6410231.11>,4.8 }
 sphere { <1105595.68, 0, 6410230.97>,4.8 }
 cylinder { <1105595.68, 0, 6410230.97>,<1105602.43, 0, 6410231.11>,4.8 }
 sphere { <1105564.61, 0, 6410235.44>,4.8 }
 cylinder { <1105564.61, 0, 6410235.44>,<1105595.68, 0, 6410230.97>,4.8 }
 sphere { <1105451.82, 0, 6410230.89>,4.8 }
 cylinder { <1105451.82, 0, 6410230.89>,<1105564.61, 0, 6410235.44>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10152013 */
object {union { sphere { <1105475.54, 0, 6409780.55>,4.0 }
 sphere { <1105490.21, 0, 6409784.19>,4.0 }
 cylinder { <1105490.21, 0, 6409784.19>,<1105475.54, 0, 6409780.55>,4.0 }
 sphere { <1105571.66, 0, 6409803.52>,4.0 }
 cylinder { <1105571.66, 0, 6409803.52>,<1105490.21, 0, 6409784.19>,4.0 }
 sphere { <1105582.65, 0, 6409801.72>,4.0 }
 cylinder { <1105582.65, 0, 6409801.72>,<1105571.66, 0, 6409803.52>,4.0 }
 sphere { <1105613.31, 0, 6409790.81>,4.0 }
 cylinder { <1105613.31, 0, 6409790.81>,<1105582.65, 0, 6409801.72>,4.0 }
 sphere { <1105656.46, 0, 6409783.77>,4.0 }
 cylinder { <1105656.46, 0, 6409783.77>,<1105613.31, 0, 6409790.81>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105475.54, 0, 6409780.55>,4.8 }
 sphere { <1105490.21, 0, 6409784.19>,4.8 }
 cylinder { <1105490.21, 0, 6409784.19>,<1105475.54, 0, 6409780.55>,4.8 }
 sphere { <1105571.66, 0, 6409803.52>,4.8 }
 cylinder { <1105571.66, 0, 6409803.52>,<1105490.21, 0, 6409784.19>,4.8 }
 sphere { <1105582.65, 0, 6409801.72>,4.8 }
 cylinder { <1105582.65, 0, 6409801.72>,<1105571.66, 0, 6409803.52>,4.8 }
 sphere { <1105613.31, 0, 6409790.81>,4.8 }
 cylinder { <1105613.31, 0, 6409790.81>,<1105582.65, 0, 6409801.72>,4.8 }
 sphere { <1105656.46, 0, 6409783.77>,4.8 }
 cylinder { <1105656.46, 0, 6409783.77>,<1105613.31, 0, 6409790.81>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=27137247 */
object {union { sphere { <1105620.22, 0, 6410762.83>,4.0 }
 sphere { <1105561.21, 0, 6410760.14>,4.0 }
 cylinder { <1105561.21, 0, 6410760.14>,<1105620.22, 0, 6410762.83>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105620.22, 0, 6410762.83>,4.8 }
 sphere { <1105561.21, 0, 6410760.14>,4.8 }
 cylinder { <1105561.21, 0, 6410760.14>,<1105620.22, 0, 6410762.83>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10202685 */
object {union { sphere { <1105641.35, 0, 6409856.54>,2.4 }
 sphere { <1105614.37, 0, 6409894.42>,2.4 }
 cylinder { <1105614.37, 0, 6409894.42>,<1105641.35, 0, 6409856.54>,2.4 }
 sphere { <1105588.55, 0, 6409962.49>,2.4 }
 cylinder { <1105588.55, 0, 6409962.49>,<1105614.37, 0, 6409894.42>,2.4 }
 sphere { <1105590.35, 0, 6410041.83>,2.4 }
 cylinder { <1105590.35, 0, 6410041.83>,<1105588.55, 0, 6409962.49>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105641.35, 0, 6409856.54>,2.88 }
 sphere { <1105614.37, 0, 6409894.42>,2.88 }
 cylinder { <1105614.37, 0, 6409894.42>,<1105641.35, 0, 6409856.54>,2.88 }
 sphere { <1105588.55, 0, 6409962.49>,2.88 }
 cylinder { <1105588.55, 0, 6409962.49>,<1105614.37, 0, 6409894.42>,2.88 }
 sphere { <1105590.35, 0, 6410041.83>,2.88 }
 cylinder { <1105590.35, 0, 6410041.83>,<1105588.55, 0, 6409962.49>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=37299754 */
object {union { sphere { <1105594.11, 0, 6410415.39>,4.0 }
 sphere { <1105593.93, 0, 6410483.21>,4.0 }
 cylinder { <1105593.93, 0, 6410483.21>,<1105594.11, 0, 6410415.39>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105594.11, 0, 6410415.39>,4.8 }
 sphere { <1105593.93, 0, 6410483.21>,4.8 }
 cylinder { <1105593.93, 0, 6410483.21>,<1105594.11, 0, 6410415.39>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10152010 */
object {union { sphere { <1105595.51, 0, 6409671.2>,4.0 }
 sphere { <1105656.46, 0, 6409783.77>,4.0 }
 cylinder { <1105656.46, 0, 6409783.77>,<1105595.51, 0, 6409671.2>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105595.51, 0, 6409671.2>,4.8 }
 sphere { <1105656.46, 0, 6409783.77>,4.8 }
 cylinder { <1105656.46, 0, 6409783.77>,<1105595.51, 0, 6409671.2>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=57837181 */
object {union { sphere { <1105600.14, 0, 6410298.39>,4.0 }
 sphere { <1105628.05, 0, 6410380.98>,4.0 }
 cylinder { <1105628.05, 0, 6410380.98>,<1105600.14, 0, 6410298.39>,4.0 }
 sphere { <1105629.66, 0, 6410400.27>,4.0 }
 cylinder { <1105629.66, 0, 6410400.27>,<1105628.05, 0, 6410380.98>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105600.14, 0, 6410298.39>,4.8 }
 sphere { <1105628.05, 0, 6410380.98>,4.8 }
 cylinder { <1105628.05, 0, 6410380.98>,<1105600.14, 0, 6410298.39>,4.8 }
 sphere { <1105629.66, 0, 6410400.27>,4.8 }
 cylinder { <1105629.66, 0, 6410400.27>,<1105628.05, 0, 6410380.98>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10213067 */
object {union { sphere { <1105619.89, 0, 6410195.38>,4.0 }
 sphere { <1105613.08, 0, 6410209.16>,4.0 }
 cylinder { <1105613.08, 0, 6410209.16>,<1105619.89, 0, 6410195.38>,4.0 }
 sphere { <1105602.43, 0, 6410231.11>,4.0 }
 cylinder { <1105602.43, 0, 6410231.11>,<1105613.08, 0, 6410209.16>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105619.89, 0, 6410195.38>,4.8 }
 sphere { <1105613.08, 0, 6410209.16>,4.8 }
 cylinder { <1105613.08, 0, 6410209.16>,<1105619.89, 0, 6410195.38>,4.8 }
 sphere { <1105602.43, 0, 6410231.11>,4.8 }
 cylinder { <1105602.43, 0, 6410231.11>,<1105613.08, 0, 6410209.16>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=4587366 */
object {union { sphere { <1105640.57, 0, 6410950.82>,2.4 }
 sphere { <1105612.41, 0, 6410869.42>,2.4 }
 cylinder { <1105612.41, 0, 6410869.42>,<1105640.57, 0, 6410950.82>,2.4 }
 sphere { <1105609.48, 0, 6410863.39>,2.4 }
 cylinder { <1105609.48, 0, 6410863.39>,<1105612.41, 0, 6410869.42>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105640.57, 0, 6410950.82>,2.88 }
 sphere { <1105612.41, 0, 6410869.42>,2.88 }
 cylinder { <1105612.41, 0, 6410869.42>,<1105640.57, 0, 6410950.82>,2.88 }
 sphere { <1105609.48, 0, 6410863.39>,2.88 }
 cylinder { <1105609.48, 0, 6410863.39>,<1105612.41, 0, 6410869.42>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=51195167 */
object {union { sphere { <1105668.66, 0, 6410880.86>,2.4 }
 sphere { <1105609.48, 0, 6410863.39>,2.4 }
 cylinder { <1105609.48, 0, 6410863.39>,<1105668.66, 0, 6410880.86>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105668.66, 0, 6410880.86>,2.88 }
 sphere { <1105609.48, 0, 6410863.39>,2.88 }
 cylinder { <1105609.48, 0, 6410863.39>,<1105668.66, 0, 6410880.86>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=55199761 */
object {union { sphere { <1105618.91, 0, 6410042.43>,4.0 }
 sphere { <1105714.08, 0, 6410050.64>,4.0 }
 cylinder { <1105714.08, 0, 6410050.64>,<1105618.91, 0, 6410042.43>,4.0 }
 sphere { <1105796.67, 0, 6410051.35>,4.0 }
 cylinder { <1105796.67, 0, 6410051.35>,<1105714.08, 0, 6410050.64>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105618.91, 0, 6410042.43>,4.8 }
 sphere { <1105714.08, 0, 6410050.64>,4.8 }
 cylinder { <1105714.08, 0, 6410050.64>,<1105618.91, 0, 6410042.43>,4.8 }
 sphere { <1105796.67, 0, 6410051.35>,4.8 }
 cylinder { <1105796.67, 0, 6410051.35>,<1105714.08, 0, 6410050.64>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10213066 */
object {union { sphere { <1106006.07, 0, 6410099.09>,2.4 }
 sphere { <1105959.21, 0, 6410097.78>,2.4 }
 cylinder { <1105959.21, 0, 6410097.78>,<1106006.07, 0, 6410099.09>,2.4 }
 sphere { <1105914.34, 0, 6410099.66>,2.4 }
 cylinder { <1105914.34, 0, 6410099.66>,<1105959.21, 0, 6410097.78>,2.4 }
 sphere { <1105898.63, 0, 6410104.53>,2.4 }
 cylinder { <1105898.63, 0, 6410104.53>,<1105914.34, 0, 6410099.66>,2.4 }
 sphere { <1105876.94, 0, 6410121.36>,2.4 }
 cylinder { <1105876.94, 0, 6410121.36>,<1105898.63, 0, 6410104.53>,2.4 }
 sphere { <1105845.14, 0, 6410138.93>,2.4 }
 cylinder { <1105845.14, 0, 6410138.93>,<1105876.94, 0, 6410121.36>,2.4 }
 sphere { <1105817.69, 0, 6410149.93>,2.4 }
 cylinder { <1105817.69, 0, 6410149.93>,<1105845.14, 0, 6410138.93>,2.4 }
 sphere { <1105739.68, 0, 6410186.81>,2.4 }
 cylinder { <1105739.68, 0, 6410186.81>,<1105817.69, 0, 6410149.93>,2.4 }
 sphere { <1105716.11, 0, 6410193.16>,2.4 }
 cylinder { <1105716.11, 0, 6410193.16>,<1105739.68, 0, 6410186.81>,2.4 }
 sphere { <1105673.1, 0, 6410196.16>,2.4 }
 cylinder { <1105673.1, 0, 6410196.16>,<1105716.11, 0, 6410193.16>,2.4 }
 sphere { <1105619.89, 0, 6410195.38>,2.4 }
 cylinder { <1105619.89, 0, 6410195.38>,<1105673.1, 0, 6410196.16>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1106006.07, 0, 6410099.09>,2.88 }
 sphere { <1105959.21, 0, 6410097.78>,2.88 }
 cylinder { <1105959.21, 0, 6410097.78>,<1106006.07, 0, 6410099.09>,2.88 }
 sphere { <1105914.34, 0, 6410099.66>,2.88 }
 cylinder { <1105914.34, 0, 6410099.66>,<1105959.21, 0, 6410097.78>,2.88 }
 sphere { <1105898.63, 0, 6410104.53>,2.88 }
 cylinder { <1105898.63, 0, 6410104.53>,<1105914.34, 0, 6410099.66>,2.88 }
 sphere { <1105876.94, 0, 6410121.36>,2.88 }
 cylinder { <1105876.94, 0, 6410121.36>,<1105898.63, 0, 6410104.53>,2.88 }
 sphere { <1105845.14, 0, 6410138.93>,2.88 }
 cylinder { <1105845.14, 0, 6410138.93>,<1105876.94, 0, 6410121.36>,2.88 }
 sphere { <1105817.69, 0, 6410149.93>,2.88 }
 cylinder { <1105817.69, 0, 6410149.93>,<1105845.14, 0, 6410138.93>,2.88 }
 sphere { <1105739.68, 0, 6410186.81>,2.88 }
 cylinder { <1105739.68, 0, 6410186.81>,<1105817.69, 0, 6410149.93>,2.88 }
 sphere { <1105716.11, 0, 6410193.16>,2.88 }
 cylinder { <1105716.11, 0, 6410193.16>,<1105739.68, 0, 6410186.81>,2.88 }
 sphere { <1105673.1, 0, 6410196.16>,2.88 }
 cylinder { <1105673.1, 0, 6410196.16>,<1105716.11, 0, 6410193.16>,2.88 }
 sphere { <1105619.89, 0, 6410195.38>,2.88 }
 cylinder { <1105619.89, 0, 6410195.38>,<1105673.1, 0, 6410196.16>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=54266110 */
object {union { sphere { <1105641.78, 0, 6410410.89>,4.0 }
 sphere { <1105629.66, 0, 6410400.27>,4.0 }
 cylinder { <1105629.66, 0, 6410400.27>,<1105641.78, 0, 6410410.89>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105641.78, 0, 6410410.89>,4.8 }
 sphere { <1105629.66, 0, 6410400.27>,4.8 }
 cylinder { <1105629.66, 0, 6410400.27>,<1105641.78, 0, 6410410.89>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10202684 */
object {union { sphere { <1105655.5, 0, 6409794.67>,2.4 }
 sphere { <1105653.01, 0, 6409835.33>,2.4 }
 cylinder { <1105653.01, 0, 6409835.33>,<1105655.5, 0, 6409794.67>,2.4 }
 sphere { <1105641.35, 0, 6409856.54>,2.4 }
 cylinder { <1105641.35, 0, 6409856.54>,<1105653.01, 0, 6409835.33>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105655.5, 0, 6409794.67>,2.88 }
 sphere { <1105653.01, 0, 6409835.33>,2.88 }
 cylinder { <1105653.01, 0, 6409835.33>,<1105655.5, 0, 6409794.67>,2.88 }
 sphere { <1105641.35, 0, 6409856.54>,2.88 }
 cylinder { <1105641.35, 0, 6409856.54>,<1105653.01, 0, 6409835.33>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=10152017 */
object {union { sphere { <1105967.28, 0, 6410050.64>,4.0 }
 sphere { <1105946.32, 0, 6410020.29>,4.0 }
 cylinder { <1105946.32, 0, 6410020.29>,<1105967.28, 0, 6410050.64>,4.0 }
 sphere { <1105939.79, 0, 6410014.78>,4.0 }
 cylinder { <1105939.79, 0, 6410014.78>,<1105946.32, 0, 6410020.29>,4.0 }
 sphere { <1105917.08, 0, 6409993.83>,4.0 }
 cylinder { <1105917.08, 0, 6409993.83>,<1105939.79, 0, 6410014.78>,4.0 }
 sphere { <1105896.86, 0, 6409979.77>,4.0 }
 cylinder { <1105896.86, 0, 6409979.77>,<1105917.08, 0, 6409993.83>,4.0 }
 sphere { <1105882.88, 0, 6409971.2>,4.0 }
 cylinder { <1105882.88, 0, 6409971.2>,<1105896.86, 0, 6409979.77>,4.0 }
 sphere { <1105842.06, 0, 6409955.75>,4.0 }
 cylinder { <1105842.06, 0, 6409955.75>,<1105882.88, 0, 6409971.2>,4.0 }
 sphere { <1105818.34, 0, 6409941.41>,4.0 }
 cylinder { <1105818.34, 0, 6409941.41>,<1105842.06, 0, 6409955.75>,4.0 }
 sphere { <1105799.25, 0, 6409926.51>,4.0 }
 cylinder { <1105799.25, 0, 6409926.51>,<1105818.34, 0, 6409941.41>,4.0 }
 sphere { <1105776.97, 0, 6409908.32>,4.0 }
 cylinder { <1105776.97, 0, 6409908.32>,<1105799.25, 0, 6409926.51>,4.0 }
 sphere { <1105760.42, 0, 6409893.42>,4.0 }
 cylinder { <1105760.42, 0, 6409893.42>,<1105776.97, 0, 6409908.32>,4.0 }
 sphere { <1105733.39, 0, 6409880.18>,4.0 }
 cylinder { <1105733.39, 0, 6409880.18>,<1105760.42, 0, 6409893.42>,4.0 }
 sphere { <1105686.84, 0, 6409863.76>,4.0 }
 cylinder { <1105686.84, 0, 6409863.76>,<1105733.39, 0, 6409880.18>,4.0 }
 sphere { <1105641.35, 0, 6409856.54>,4.0 }
 cylinder { <1105641.35, 0, 6409856.54>,<1105686.84, 0, 6409863.76>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105967.28, 0, 6410050.64>,4.8 }
 sphere { <1105946.32, 0, 6410020.29>,4.8 }
 cylinder { <1105946.32, 0, 6410020.29>,<1105967.28, 0, 6410050.64>,4.8 }
 sphere { <1105939.79, 0, 6410014.78>,4.8 }
 cylinder { <1105939.79, 0, 6410014.78>,<1105946.32, 0, 6410020.29>,4.8 }
 sphere { <1105917.08, 0, 6409993.83>,4.8 }
 cylinder { <1105917.08, 0, 6409993.83>,<1105939.79, 0, 6410014.78>,4.8 }
 sphere { <1105896.86, 0, 6409979.77>,4.8 }
 cylinder { <1105896.86, 0, 6409979.77>,<1105917.08, 0, 6409993.83>,4.8 }
 sphere { <1105882.88, 0, 6409971.2>,4.8 }
 cylinder { <1105882.88, 0, 6409971.2>,<1105896.86, 0, 6409979.77>,4.8 }
 sphere { <1105842.06, 0, 6409955.75>,4.8 }
 cylinder { <1105842.06, 0, 6409955.75>,<1105882.88, 0, 6409971.2>,4.8 }
 sphere { <1105818.34, 0, 6409941.41>,4.8 }
 cylinder { <1105818.34, 0, 6409941.41>,<1105842.06, 0, 6409955.75>,4.8 }
 sphere { <1105799.25, 0, 6409926.51>,4.8 }
 cylinder { <1105799.25, 0, 6409926.51>,<1105818.34, 0, 6409941.41>,4.8 }
 sphere { <1105776.97, 0, 6409908.32>,4.8 }
 cylinder { <1105776.97, 0, 6409908.32>,<1105799.25, 0, 6409926.51>,4.8 }
 sphere { <1105760.42, 0, 6409893.42>,4.8 }
 cylinder { <1105760.42, 0, 6409893.42>,<1105776.97, 0, 6409908.32>,4.8 }
 sphere { <1105733.39, 0, 6409880.18>,4.8 }
 cylinder { <1105733.39, 0, 6409880.18>,<1105760.42, 0, 6409893.42>,4.8 }
 sphere { <1105686.84, 0, 6409863.76>,4.8 }
 cylinder { <1105686.84, 0, 6409863.76>,<1105733.39, 0, 6409880.18>,4.8 }
 sphere { <1105641.35, 0, 6409856.54>,4.8 }
 cylinder { <1105641.35, 0, 6409856.54>,<1105686.84, 0, 6409863.76>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=57837180 */
object {union { sphere { <1105685.53, 0, 6410439.66>,4.0 }
 sphere { <1105641.78, 0, 6410410.89>,4.0 }
 cylinder { <1105641.78, 0, 6410410.89>,<1105685.53, 0, 6410439.66>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105685.53, 0, 6410439.66>,4.8 }
 sphere { <1105641.78, 0, 6410410.89>,4.8 }
 cylinder { <1105641.78, 0, 6410410.89>,<1105685.53, 0, 6410439.66>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=47907685 */
object {union { sphere { <1105656.46, 0, 6409783.77>,2.4 }
 sphere { <1105655.5, 0, 6409794.67>,2.4 }
 cylinder { <1105655.5, 0, 6409794.67>,<1105656.46, 0, 6409783.77>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105656.46, 0, 6409783.77>,2.88 }
 sphere { <1105655.5, 0, 6409794.67>,2.88 }
 cylinder { <1105655.5, 0, 6409794.67>,<1105656.46, 0, 6409783.77>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=10152024 */
object {union { sphere { <1105776.97, 0, 6409811.78>,4.0 }
 sphere { <1105688.5, 0, 6409802.43>,4.0 }
 cylinder { <1105688.5, 0, 6409802.43>,<1105776.97, 0, 6409811.78>,4.0 }
 sphere { <1105655.5, 0, 6409794.67>,4.0 }
 cylinder { <1105655.5, 0, 6409794.67>,<1105688.5, 0, 6409802.43>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105776.97, 0, 6409811.78>,4.8 }
 sphere { <1105688.5, 0, 6409802.43>,4.8 }
 cylinder { <1105688.5, 0, 6409802.43>,<1105776.97, 0, 6409811.78>,4.8 }
 sphere { <1105655.5, 0, 6409794.67>,4.8 }
 cylinder { <1105655.5, 0, 6409794.67>,<1105688.5, 0, 6409802.43>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=47907682 */
object {union { sphere { <1105666.91, 0, 6409751.96>,2.4 }
 sphere { <1105656.46, 0, 6409783.77>,2.4 }
 cylinder { <1105656.46, 0, 6409783.77>,<1105666.91, 0, 6409751.96>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105666.91, 0, 6409751.96>,2.88 }
 sphere { <1105656.46, 0, 6409783.77>,2.88 }
 cylinder { <1105656.46, 0, 6409783.77>,<1105666.91, 0, 6409751.96>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=28458791 */
object {union { sphere { <1105683.8, 0, 6410650.26>,4.0 }
 sphere { <1105712.7, 0, 6410635.41>,4.0 }
 cylinder { <1105712.7, 0, 6410635.41>,<1105683.8, 0, 6410650.26>,4.0 }
 sphere { <1105775.02, 0, 6410606.13>,4.0 }
 cylinder { <1105775.02, 0, 6410606.13>,<1105712.7, 0, 6410635.41>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105683.8, 0, 6410650.26>,4.8 }
 sphere { <1105712.7, 0, 6410635.41>,4.8 }
 cylinder { <1105712.7, 0, 6410635.41>,<1105683.8, 0, 6410650.26>,4.8 }
 sphere { <1105775.02, 0, 6410606.13>,4.8 }
 cylinder { <1105775.02, 0, 6410606.13>,<1105712.7, 0, 6410635.41>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10213069 */
object {union { sphere { <1105717.67, 0, 6410468.3>,4.0 }
 sphere { <1105685.53, 0, 6410439.66>,4.0 }
 cylinder { <1105685.53, 0, 6410439.66>,<1105717.67, 0, 6410468.3>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105717.67, 0, 6410468.3>,4.8 }
 sphere { <1105685.53, 0, 6410439.66>,4.8 }
 cylinder { <1105685.53, 0, 6410439.66>,<1105717.67, 0, 6410468.3>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10152020 */
object {union { sphere { <1105688.5, 0, 6409802.43>,4.0 }
 sphere { <1105686.84, 0, 6409863.76>,4.0 }
 cylinder { <1105686.84, 0, 6409863.76>,<1105688.5, 0, 6409802.43>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105688.5, 0, 6409802.43>,4.8 }
 sphere { <1105686.84, 0, 6409863.76>,4.8 }
 cylinder { <1105686.84, 0, 6409863.76>,<1105688.5, 0, 6409802.43>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=23034045 */
object {union { sphere { <1105736.05, 0, 6410681.7>,4.0 }
 sphere { <1105712.7, 0, 6410635.41>,4.0 }
 cylinder { <1105712.7, 0, 6410635.41>,<1105736.05, 0, 6410681.7>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105736.05, 0, 6410681.7>,4.8 }
 sphere { <1105712.7, 0, 6410635.41>,4.8 }
 cylinder { <1105712.7, 0, 6410635.41>,<1105736.05, 0, 6410681.7>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10152018 */
object {union { sphere { <1105714.08, 0, 6410050.64>,4.0 }
 sphere { <1105723.46, 0, 6409989.41>,4.0 }
 cylinder { <1105723.46, 0, 6409989.41>,<1105714.08, 0, 6410050.64>,4.0 }
 sphere { <1105738.9, 0, 6409926.53>,4.0 }
 cylinder { <1105738.9, 0, 6409926.53>,<1105723.46, 0, 6409989.41>,4.0 }
 sphere { <1105760.42, 0, 6409893.42>,4.0 }
 cylinder { <1105760.42, 0, 6409893.42>,<1105738.9, 0, 6409926.53>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105714.08, 0, 6410050.64>,4.8 }
 sphere { <1105723.46, 0, 6409989.41>,4.8 }
 cylinder { <1105723.46, 0, 6409989.41>,<1105714.08, 0, 6410050.64>,4.8 }
 sphere { <1105738.9, 0, 6409926.53>,4.8 }
 cylinder { <1105738.9, 0, 6409926.53>,<1105723.46, 0, 6409989.41>,4.8 }
 sphere { <1105760.42, 0, 6409893.42>,4.8 }
 cylinder { <1105760.42, 0, 6409893.42>,<1105738.9, 0, 6409926.53>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=7834769 */
object {union { sphere { <1105717.07, 0, 6410474.87>,4.0 }
 sphere { <1105717.67, 0, 6410468.3>,4.0 }
 cylinder { <1105717.67, 0, 6410468.3>,<1105717.07, 0, 6410474.87>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105717.07, 0, 6410474.87>,4.8 }
 sphere { <1105717.67, 0, 6410468.3>,4.8 }
 cylinder { <1105717.67, 0, 6410468.3>,<1105717.07, 0, 6410474.87>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=12710975 */
object {union { sphere { <1105725.1, 0, 6410472.25>,2.4 }
 sphere { <1105771.43, 0, 6410434.52>,2.4 }
 cylinder { <1105771.43, 0, 6410434.52>,<1105725.1, 0, 6410472.25>,2.4 }
 sphere { <1105819.9, 0, 6410400.72>,2.4 }
 cylinder { <1105819.9, 0, 6410400.72>,<1105771.43, 0, 6410434.52>,2.4 }
 sphere { <1105843.52, 0, 6410394.62>,2.4 }
 cylinder { <1105843.52, 0, 6410394.62>,<1105819.9, 0, 6410400.72>,2.4 }
 sphere { <1105911.19, 0, 6410393.34>,2.4 }
 cylinder { <1105911.19, 0, 6410393.34>,<1105843.52, 0, 6410394.62>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105725.1, 0, 6410472.25>,2.88 }
 sphere { <1105771.43, 0, 6410434.52>,2.88 }
 cylinder { <1105771.43, 0, 6410434.52>,<1105725.1, 0, 6410472.25>,2.88 }
 sphere { <1105819.9, 0, 6410400.72>,2.88 }
 cylinder { <1105819.9, 0, 6410400.72>,<1105771.43, 0, 6410434.52>,2.88 }
 sphere { <1105843.52, 0, 6410394.62>,2.88 }
 cylinder { <1105843.52, 0, 6410394.62>,<1105819.9, 0, 6410400.72>,2.88 }
 sphere { <1105911.19, 0, 6410393.34>,2.88 }
 cylinder { <1105911.19, 0, 6410393.34>,<1105843.52, 0, 6410394.62>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=4568541 */
object {union { sphere { <1105757.47, 0, 6410909.53>,2.4 }
 sphere { <1105743.28, 0, 6410928.26>,2.4 }
 cylinder { <1105743.28, 0, 6410928.26>,<1105757.47, 0, 6410909.53>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105757.47, 0, 6410909.53>,2.88 }
 sphere { <1105743.28, 0, 6410928.26>,2.88 }
 cylinder { <1105743.28, 0, 6410928.26>,<1105757.47, 0, 6410909.53>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=10152025 */
object {union { sphere { <1105776.97, 0, 6409811.78>,4.0 }
 sphere { <1105783.15, 0, 6409747.46>,4.0 }
 cylinder { <1105783.15, 0, 6409747.46>,<1105776.97, 0, 6409811.78>,4.0 }
 sphere { <1105771.67, 0, 6409695.82>,4.0 }
 cylinder { <1105771.67, 0, 6409695.82>,<1105783.15, 0, 6409747.46>,4.0 }
 sphere { <1105756.56, 0, 6409622.57>,4.0 }
 cylinder { <1105756.56, 0, 6409622.57>,<1105771.67, 0, 6409695.82>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105776.97, 0, 6409811.78>,4.8 }
 sphere { <1105783.15, 0, 6409747.46>,4.8 }
 cylinder { <1105783.15, 0, 6409747.46>,<1105776.97, 0, 6409811.78>,4.8 }
 sphere { <1105771.67, 0, 6409695.82>,4.8 }
 cylinder { <1105771.67, 0, 6409695.82>,<1105783.15, 0, 6409747.46>,4.8 }
 sphere { <1105756.56, 0, 6409622.57>,4.8 }
 cylinder { <1105756.56, 0, 6409622.57>,<1105771.67, 0, 6409695.82>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=6110030 */
object {union { sphere { <1105757.47, 0, 6410909.53>,4.0 }
 sphere { <1105786.92, 0, 6410905.14>,4.0 }
 cylinder { <1105786.92, 0, 6410905.14>,<1105757.47, 0, 6410909.53>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105757.47, 0, 6410909.53>,4.8 }
 sphere { <1105786.92, 0, 6410905.14>,4.8 }
 cylinder { <1105786.92, 0, 6410905.14>,<1105757.47, 0, 6410909.53>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=28808701 */
object {union { sphere { <1105791.98, 0, 6410933.3>,4.0 }
 sphere { <1105757.47, 0, 6410909.53>,4.0 }
 cylinder { <1105757.47, 0, 6410909.53>,<1105791.98, 0, 6410933.3>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105791.98, 0, 6410933.3>,4.8 }
 sphere { <1105757.47, 0, 6410909.53>,4.8 }
 cylinder { <1105757.47, 0, 6410909.53>,<1105791.98, 0, 6410933.3>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10152022 */
object {union { sphere { <1105760.42, 0, 6409893.42>,4.0 }
 sphere { <1105774.21, 0, 6409858.68>,4.0 }
 cylinder { <1105774.21, 0, 6409858.68>,<1105760.42, 0, 6409893.42>,4.0 }
 sphere { <1105775.31, 0, 6409846.54>,4.0 }
 cylinder { <1105775.31, 0, 6409846.54>,<1105774.21, 0, 6409858.68>,4.0 }
 sphere { <1105776.97, 0, 6409811.78>,4.0 }
 cylinder { <1105776.97, 0, 6409811.78>,<1105775.31, 0, 6409846.54>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105760.42, 0, 6409893.42>,4.8 }
 sphere { <1105774.21, 0, 6409858.68>,4.8 }
 cylinder { <1105774.21, 0, 6409858.68>,<1105760.42, 0, 6409893.42>,4.8 }
 sphere { <1105775.31, 0, 6409846.54>,4.8 }
 cylinder { <1105775.31, 0, 6409846.54>,<1105774.21, 0, 6409858.68>,4.8 }
 sphere { <1105776.97, 0, 6409811.78>,4.8 }
 cylinder { <1105776.97, 0, 6409811.78>,<1105775.31, 0, 6409846.54>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=32703964 */
object {union { sphere { <1105811.23, 0, 6410760.8>,4.0 }
 sphere { <1105798.29, 0, 6410753.47>,4.0 }
 cylinder { <1105798.29, 0, 6410753.47>,<1105811.23, 0, 6410760.8>,4.0 }
 sphere { <1105761.21, 0, 6410728.03>,4.0 }
 cylinder { <1105761.21, 0, 6410728.03>,<1105798.29, 0, 6410753.47>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105811.23, 0, 6410760.8>,4.8 }
 sphere { <1105798.29, 0, 6410753.47>,4.8 }
 cylinder { <1105798.29, 0, 6410753.47>,<1105811.23, 0, 6410760.8>,4.8 }
 sphere { <1105761.21, 0, 6410728.03>,4.8 }
 cylinder { <1105761.21, 0, 6410728.03>,<1105798.29, 0, 6410753.47>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=9035405 */
object {union { sphere { <1105837.75, 0, 6410596.18>,4.0 }
 sphere { <1105795.25, 0, 6410595.92>,4.0 }
 cylinder { <1105795.25, 0, 6410595.92>,<1105837.75, 0, 6410596.18>,4.0 }
 sphere { <1105775.02, 0, 6410606.13>,4.0 }
 cylinder { <1105775.02, 0, 6410606.13>,<1105795.25, 0, 6410595.92>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105837.75, 0, 6410596.18>,4.8 }
 sphere { <1105795.25, 0, 6410595.92>,4.8 }
 cylinder { <1105795.25, 0, 6410595.92>,<1105837.75, 0, 6410596.18>,4.8 }
 sphere { <1105775.02, 0, 6410606.13>,4.8 }
 cylinder { <1105775.02, 0, 6410606.13>,<1105795.25, 0, 6410595.92>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10152021 */
object {union { sphere { <1105775.31, 0, 6409846.54>,4.0 }
 sphere { <1105792.95, 0, 6409852.02>,4.0 }
 cylinder { <1105792.95, 0, 6409852.02>,<1105775.31, 0, 6409846.54>,4.0 }
 sphere { <1105816.44, 0, 6409858.33>,4.0 }
 cylinder { <1105816.44, 0, 6409858.33>,<1105792.95, 0, 6409852.02>,4.0 }
 sphere { <1105839.3, 0, 6409864.18>,4.0 }
 cylinder { <1105839.3, 0, 6409864.18>,<1105816.44, 0, 6409858.33>,4.0 }
 sphere { <1105868.11, 0, 6409871.61>,4.0 }
 cylinder { <1105868.11, 0, 6409871.61>,<1105839.3, 0, 6409864.18>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105775.31, 0, 6409846.54>,4.8 }
 sphere { <1105792.95, 0, 6409852.02>,4.8 }
 cylinder { <1105792.95, 0, 6409852.02>,<1105775.31, 0, 6409846.54>,4.8 }
 sphere { <1105816.44, 0, 6409858.33>,4.8 }
 cylinder { <1105816.44, 0, 6409858.33>,<1105792.95, 0, 6409852.02>,4.8 }
 sphere { <1105839.3, 0, 6409864.18>,4.8 }
 cylinder { <1105839.3, 0, 6409864.18>,<1105816.44, 0, 6409858.33>,4.8 }
 sphere { <1105868.11, 0, 6409871.61>,4.8 }
 cylinder { <1105868.11, 0, 6409871.61>,<1105839.3, 0, 6409864.18>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=28808703 */
object {union { sphere { <1105786.92, 0, 6410905.14>,4.0 }
 sphere { <1105802.53, 0, 6410897.48>,4.0 }
 cylinder { <1105802.53, 0, 6410897.48>,<1105786.92, 0, 6410905.14>,4.0 }
 sphere { <1105827.2, 0, 6410886.17>,4.0 }
 cylinder { <1105827.2, 0, 6410886.17>,<1105802.53, 0, 6410897.48>,4.0 }
 sphere { <1105851.62, 0, 6410874.96>,4.0 }
 cylinder { <1105851.62, 0, 6410874.96>,<1105827.2, 0, 6410886.17>,4.0 }
 sphere { <1105890.14, 0, 6410852.21>,4.0 }
 cylinder { <1105890.14, 0, 6410852.21>,<1105851.62, 0, 6410874.96>,4.0 }
 sphere { <1105945.58, 0, 6410819.29>,4.0 }
 cylinder { <1105945.58, 0, 6410819.29>,<1105890.14, 0, 6410852.21>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105786.92, 0, 6410905.14>,4.8 }
 sphere { <1105802.53, 0, 6410897.48>,4.8 }
 cylinder { <1105802.53, 0, 6410897.48>,<1105786.92, 0, 6410905.14>,4.8 }
 sphere { <1105827.2, 0, 6410886.17>,4.8 }
 cylinder { <1105827.2, 0, 6410886.17>,<1105802.53, 0, 6410897.48>,4.8 }
 sphere { <1105851.62, 0, 6410874.96>,4.8 }
 cylinder { <1105851.62, 0, 6410874.96>,<1105827.2, 0, 6410886.17>,4.8 }
 sphere { <1105890.14, 0, 6410852.21>,4.8 }
 cylinder { <1105890.14, 0, 6410852.21>,<1105851.62, 0, 6410874.96>,4.8 }
 sphere { <1105945.58, 0, 6410819.29>,4.8 }
 cylinder { <1105945.58, 0, 6410819.29>,<1105890.14, 0, 6410852.21>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=28808702 */
object {union { sphere { <1105786.92, 0, 6410905.14>,4.0 }
 sphere { <1105791.98, 0, 6410933.3>,4.0 }
 cylinder { <1105791.98, 0, 6410933.3>,<1105786.92, 0, 6410905.14>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105786.92, 0, 6410905.14>,4.8 }
 sphere { <1105791.98, 0, 6410933.3>,4.8 }
 cylinder { <1105791.98, 0, 6410933.3>,<1105786.92, 0, 6410905.14>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=54265631 */
object {union { sphere { <1105796.67, 0, 6410051.35>,4.0 }
 sphere { <1105862.23, 0, 6410051.07>,4.0 }
 cylinder { <1105862.23, 0, 6410051.07>,<1105796.67, 0, 6410051.35>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105796.67, 0, 6410051.35>,4.8 }
 sphere { <1105862.23, 0, 6410051.07>,4.8 }
 cylinder { <1105862.23, 0, 6410051.07>,<1105796.67, 0, 6410051.35>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=23962661 */
object {union { sphere { <1106164.85, 0, 6409707.79>,2.4 }
 sphere { <1106128.07, 0, 6409683.32>,2.4 }
 cylinder { <1106128.07, 0, 6409683.32>,<1106164.85, 0, 6409707.79>,2.4 }
 sphere { <1106072.31, 0, 6409645.11>,2.4 }
 cylinder { <1106072.31, 0, 6409645.11>,<1106128.07, 0, 6409683.32>,2.4 }
 sphere { <1106029.67, 0, 6409615.88>,2.4 }
 cylinder { <1106029.67, 0, 6409615.88>,<1106072.31, 0, 6409645.11>,2.4 }
 sphere { <1105975.29, 0, 6409578.45>,2.4 }
 cylinder { <1105975.29, 0, 6409578.45>,<1106029.67, 0, 6409615.88>,2.4 }
 sphere { <1105949.48, 0, 6409560.98>,2.4 }
 cylinder { <1105949.48, 0, 6409560.98>,<1105975.29, 0, 6409578.45>,2.4 }
 sphere { <1105924.59, 0, 6409544.81>,2.4 }
 cylinder { <1105924.59, 0, 6409544.81>,<1105949.48, 0, 6409560.98>,2.4 }
 sphere { <1105817.34, 0, 6409474.57>,2.4 }
 cylinder { <1105817.34, 0, 6409474.57>,<1105924.59, 0, 6409544.81>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1106164.85, 0, 6409707.79>,2.88 }
 sphere { <1106128.07, 0, 6409683.32>,2.88 }
 cylinder { <1106128.07, 0, 6409683.32>,<1106164.85, 0, 6409707.79>,2.88 }
 sphere { <1106072.31, 0, 6409645.11>,2.88 }
 cylinder { <1106072.31, 0, 6409645.11>,<1106128.07, 0, 6409683.32>,2.88 }
 sphere { <1106029.67, 0, 6409615.88>,2.88 }
 cylinder { <1106029.67, 0, 6409615.88>,<1106072.31, 0, 6409645.11>,2.88 }
 sphere { <1105975.29, 0, 6409578.45>,2.88 }
 cylinder { <1105975.29, 0, 6409578.45>,<1106029.67, 0, 6409615.88>,2.88 }
 sphere { <1105949.48, 0, 6409560.98>,2.88 }
 cylinder { <1105949.48, 0, 6409560.98>,<1105975.29, 0, 6409578.45>,2.88 }
 sphere { <1105924.59, 0, 6409544.81>,2.88 }
 cylinder { <1105924.59, 0, 6409544.81>,<1105949.48, 0, 6409560.98>,2.88 }
 sphere { <1105817.34, 0, 6409474.57>,2.88 }
 cylinder { <1105817.34, 0, 6409474.57>,<1105924.59, 0, 6409544.81>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=10152030 */
object {union { sphere { <1105839.3, 0, 6409864.18>,4.0 }
 sphere { <1105828.27, 0, 6409908.32>,4.0 }
 cylinder { <1105828.27, 0, 6409908.32>,<1105839.3, 0, 6409864.18>,4.0 }
 sphere { <1105818.34, 0, 6409941.41>,4.0 }
 cylinder { <1105818.34, 0, 6409941.41>,<1105828.27, 0, 6409908.32>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105839.3, 0, 6409864.18>,4.8 }
 sphere { <1105828.27, 0, 6409908.32>,4.8 }
 cylinder { <1105828.27, 0, 6409908.32>,<1105839.3, 0, 6409864.18>,4.8 }
 sphere { <1105818.34, 0, 6409941.41>,4.8 }
 cylinder { <1105818.34, 0, 6409941.41>,<1105828.27, 0, 6409908.32>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=9035403 */
object {union { sphere { <1105837.75, 0, 6410596.18>,4.0 }
 sphere { <1105845.73, 0, 6410593.78>,4.0 }
 cylinder { <1105845.73, 0, 6410593.78>,<1105837.75, 0, 6410596.18>,4.0 }
 sphere { <1105850, 0, 6410592.73>,4.0 }
 cylinder { <1105850, 0, 6410592.73>,<1105845.73, 0, 6410593.78>,4.0 }
 sphere { <1105855.23, 0, 6410590.66>,4.0 }
 cylinder { <1105855.23, 0, 6410590.66>,<1105850, 0, 6410592.73>,4.0 }
 sphere { <1105865.08, 0, 6410587.96>,4.0 }
 cylinder { <1105865.08, 0, 6410587.96>,<1105855.23, 0, 6410590.66>,4.0 }
 sphere { <1105885.29, 0, 6410581.44>,4.0 }
 cylinder { <1105885.29, 0, 6410581.44>,<1105865.08, 0, 6410587.96>,4.0 }
 sphere { <1105927.25, 0, 6410568.18>,4.0 }
 cylinder { <1105927.25, 0, 6410568.18>,<1105885.29, 0, 6410581.44>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105837.75, 0, 6410596.18>,4.8 }
 sphere { <1105845.73, 0, 6410593.78>,4.8 }
 cylinder { <1105845.73, 0, 6410593.78>,<1105837.75, 0, 6410596.18>,4.8 }
 sphere { <1105850, 0, 6410592.73>,4.8 }
 cylinder { <1105850, 0, 6410592.73>,<1105845.73, 0, 6410593.78>,4.8 }
 sphere { <1105855.23, 0, 6410590.66>,4.8 }
 cylinder { <1105855.23, 0, 6410590.66>,<1105850, 0, 6410592.73>,4.8 }
 sphere { <1105865.08, 0, 6410587.96>,4.8 }
 cylinder { <1105865.08, 0, 6410587.96>,<1105855.23, 0, 6410590.66>,4.8 }
 sphere { <1105885.29, 0, 6410581.44>,4.8 }
 cylinder { <1105885.29, 0, 6410581.44>,<1105865.08, 0, 6410587.96>,4.8 }
 sphere { <1105927.25, 0, 6410568.18>,4.8 }
 cylinder { <1105927.25, 0, 6410568.18>,<1105885.29, 0, 6410581.44>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=8106716 */
object {union { sphere { <1105837.75, 0, 6410596.18>,4.0 }
 sphere { <1105844.07, 0, 6410608.92>,4.0 }
 cylinder { <1105844.07, 0, 6410608.92>,<1105837.75, 0, 6410596.18>,4.0 }
 sphere { <1105872.36, 0, 6410668.73>,4.0 }
 cylinder { <1105872.36, 0, 6410668.73>,<1105844.07, 0, 6410608.92>,4.0 }
 sphere { <1105871.23, 0, 6410713.29>,4.0 }
 cylinder { <1105871.23, 0, 6410713.29>,<1105872.36, 0, 6410668.73>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105837.75, 0, 6410596.18>,4.8 }
 sphere { <1105844.07, 0, 6410608.92>,4.8 }
 cylinder { <1105844.07, 0, 6410608.92>,<1105837.75, 0, 6410596.18>,4.8 }
 sphere { <1105872.36, 0, 6410668.73>,4.8 }
 cylinder { <1105872.36, 0, 6410668.73>,<1105844.07, 0, 6410608.92>,4.8 }
 sphere { <1105871.23, 0, 6410713.29>,4.8 }
 cylinder { <1105871.23, 0, 6410713.29>,<1105872.36, 0, 6410668.73>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=54265645 */
object {union { sphere { <1105862.23, 0, 6410051.07>,4.0 }
 sphere { <1105904.13, 0, 6410050.9>,4.0 }
 cylinder { <1105904.13, 0, 6410050.9>,<1105862.23, 0, 6410051.07>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105862.23, 0, 6410051.07>,4.8 }
 sphere { <1105904.13, 0, 6410050.9>,4.8 }
 cylinder { <1105904.13, 0, 6410050.9>,<1105862.23, 0, 6410051.07>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10213072 */
object {union { sphere { <1105871.23, 0, 6410713.29>,4.0 }
 sphere { <1105874.73, 0, 6410726.05>,4.0 }
 cylinder { <1105874.73, 0, 6410726.05>,<1105871.23, 0, 6410713.29>,4.0 }
 sphere { <1105887.88, 0, 6410744.93>,4.0 }
 cylinder { <1105887.88, 0, 6410744.93>,<1105874.73, 0, 6410726.05>,4.0 }
 sphere { <1105916.16, 0, 6410781.53>,4.0 }
 cylinder { <1105916.16, 0, 6410781.53>,<1105887.88, 0, 6410744.93>,4.0 }
 sphere { <1105936.88, 0, 6410806.89>,4.0 }
 cylinder { <1105936.88, 0, 6410806.89>,<1105916.16, 0, 6410781.53>,4.0 }
 sphere { <1105945.58, 0, 6410819.29>,4.0 }
 cylinder { <1105945.58, 0, 6410819.29>,<1105936.88, 0, 6410806.89>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105871.23, 0, 6410713.29>,4.8 }
 sphere { <1105874.73, 0, 6410726.05>,4.8 }
 cylinder { <1105874.73, 0, 6410726.05>,<1105871.23, 0, 6410713.29>,4.8 }
 sphere { <1105887.88, 0, 6410744.93>,4.8 }
 cylinder { <1105887.88, 0, 6410744.93>,<1105874.73, 0, 6410726.05>,4.8 }
 sphere { <1105916.16, 0, 6410781.53>,4.8 }
 cylinder { <1105916.16, 0, 6410781.53>,<1105887.88, 0, 6410744.93>,4.8 }
 sphere { <1105936.88, 0, 6410806.89>,4.8 }
 cylinder { <1105936.88, 0, 6410806.89>,<1105916.16, 0, 6410781.53>,4.8 }
 sphere { <1105945.58, 0, 6410819.29>,4.8 }
 cylinder { <1105945.58, 0, 6410819.29>,<1105936.88, 0, 6410806.89>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=22779407 */
object {union { sphere { <1105884.22, 0, 6410289.43>,4.0 }
 sphere { <1105981.49, 0, 6410250.75>,4.0 }
 cylinder { <1105981.49, 0, 6410250.75>,<1105884.22, 0, 6410289.43>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105884.22, 0, 6410289.43>,4.8 }
 sphere { <1105981.49, 0, 6410250.75>,4.8 }
 cylinder { <1105981.49, 0, 6410250.75>,<1105884.22, 0, 6410289.43>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=54265635 */
object {union { sphere { <1105904.13, 0, 6410050.9>,4.0 }
 sphere { <1105967.28, 0, 6410050.64>,4.0 }
 cylinder { <1105967.28, 0, 6410050.64>,<1105904.13, 0, 6410050.9>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105904.13, 0, 6410050.9>,4.8 }
 sphere { <1105967.28, 0, 6410050.64>,4.8 }
 cylinder { <1105967.28, 0, 6410050.64>,<1105904.13, 0, 6410050.9>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=28511087 */
object {union { sphere { <1105911.19, 0, 6410393.34>,2.4 }
 sphere { <1105920.6, 0, 6410392.93>,2.4 }
 cylinder { <1105920.6, 0, 6410392.93>,<1105911.19, 0, 6410393.34>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105911.19, 0, 6410393.34>,2.88 }
 sphere { <1105920.6, 0, 6410392.93>,2.88 }
 cylinder { <1105920.6, 0, 6410392.93>,<1105911.19, 0, 6410393.34>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=25859907 */
object {union { sphere { <1105916.8, 0, 6410362.66>,4.0 }
 sphere { <1105920.6, 0, 6410392.93>,4.0 }
 cylinder { <1105920.6, 0, 6410392.93>,<1105916.8, 0, 6410362.66>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105916.8, 0, 6410362.66>,4.8 }
 sphere { <1105920.6, 0, 6410392.93>,4.8 }
 cylinder { <1105920.6, 0, 6410392.93>,<1105916.8, 0, 6410362.66>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=25859912 */
object {union { sphere { <1105920.6, 0, 6410392.93>,2.4 }
 sphere { <1105923.7, 0, 6410426.19>,2.4 }
 cylinder { <1105923.7, 0, 6410426.19>,<1105920.6, 0, 6410392.93>,2.4 }
 sphere { <1105917.73, 0, 6410480.51>,2.4 }
 cylinder { <1105917.73, 0, 6410480.51>,<1105923.7, 0, 6410426.19>,2.4 }
 sphere { <1105922.32, 0, 6410534.36>,2.4 }
 cylinder { <1105922.32, 0, 6410534.36>,<1105917.73, 0, 6410480.51>,2.4 }
 sphere { <1105925.52, 0, 6410553.93>,2.4 }
 cylinder { <1105925.52, 0, 6410553.93>,<1105922.32, 0, 6410534.36>,2.4 }
 sphere { <1105927.25, 0, 6410568.18>,2.4 }
 cylinder { <1105927.25, 0, 6410568.18>,<1105925.52, 0, 6410553.93>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105920.6, 0, 6410392.93>,2.88 }
 sphere { <1105923.7, 0, 6410426.19>,2.88 }
 cylinder { <1105923.7, 0, 6410426.19>,<1105920.6, 0, 6410392.93>,2.88 }
 sphere { <1105917.73, 0, 6410480.51>,2.88 }
 cylinder { <1105917.73, 0, 6410480.51>,<1105923.7, 0, 6410426.19>,2.88 }
 sphere { <1105922.32, 0, 6410534.36>,2.88 }
 cylinder { <1105922.32, 0, 6410534.36>,<1105917.73, 0, 6410480.51>,2.88 }
 sphere { <1105925.52, 0, 6410553.93>,2.88 }
 cylinder { <1105925.52, 0, 6410553.93>,<1105922.32, 0, 6410534.36>,2.88 }
 sphere { <1105927.25, 0, 6410568.18>,2.88 }
 cylinder { <1105927.25, 0, 6410568.18>,<1105925.52, 0, 6410553.93>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=25859904 */
object {union { sphere { <1105920.6, 0, 6410392.93>,4.0 }
 sphere { <1106004.38, 0, 6410361.63>,4.0 }
 cylinder { <1106004.38, 0, 6410361.63>,<1105920.6, 0, 6410392.93>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105920.6, 0, 6410392.93>,4.8 }
 sphere { <1106004.38, 0, 6410361.63>,4.8 }
 cylinder { <1106004.38, 0, 6410361.63>,<1105920.6, 0, 6410392.93>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=25859910 */
object {union { sphere { <1105920.6, 0, 6410392.93>,4.0 }
 sphere { <1105960.18, 0, 6410402.86>,4.0 }
 cylinder { <1105960.18, 0, 6410402.86>,<1105920.6, 0, 6410392.93>,4.0 }
 sphere { <1105989.4, 0, 6410418.55>,4.0 }
 cylinder { <1105989.4, 0, 6410418.55>,<1105960.18, 0, 6410402.86>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105920.6, 0, 6410392.93>,4.8 }
 sphere { <1105960.18, 0, 6410402.86>,4.8 }
 cylinder { <1105960.18, 0, 6410402.86>,<1105920.6, 0, 6410392.93>,4.8 }
 sphere { <1105989.4, 0, 6410418.55>,4.8 }
 cylinder { <1105989.4, 0, 6410418.55>,<1105960.18, 0, 6410402.86>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=28413144 */
object {union { sphere { <1105927.25, 0, 6410568.18>,2.4 }
 sphere { <1105976.42, 0, 6410561.28>,2.4 }
 cylinder { <1105976.42, 0, 6410561.28>,<1105927.25, 0, 6410568.18>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105927.25, 0, 6410568.18>,2.88 }
 sphere { <1105976.42, 0, 6410561.28>,2.88 }
 cylinder { <1105976.42, 0, 6410561.28>,<1105927.25, 0, 6410568.18>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=37652189 */
object {union { sphere { <1105945.58, 0, 6410819.29>,4.0 }
 sphere { <1105959.04, 0, 6410817.27>,4.0 }
 cylinder { <1105959.04, 0, 6410817.27>,<1105945.58, 0, 6410819.29>,4.0 }
 sphere { <1105968.83, 0, 6410818.67>,4.0 }
 cylinder { <1105968.83, 0, 6410818.67>,<1105959.04, 0, 6410817.27>,4.0 }
 sphere { <1105977.94, 0, 6410820.6>,4.0 }
 cylinder { <1105977.94, 0, 6410820.6>,<1105968.83, 0, 6410818.67>,4.0 }
 sphere { <1106047.62, 0, 6410856.87>,4.0 }
 cylinder { <1106047.62, 0, 6410856.87>,<1105977.94, 0, 6410820.6>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105945.58, 0, 6410819.29>,4.8 }
 sphere { <1105959.04, 0, 6410817.27>,4.8 }
 cylinder { <1105959.04, 0, 6410817.27>,<1105945.58, 0, 6410819.29>,4.8 }
 sphere { <1105968.83, 0, 6410818.67>,4.8 }
 cylinder { <1105968.83, 0, 6410818.67>,<1105959.04, 0, 6410817.27>,4.8 }
 sphere { <1105977.94, 0, 6410820.6>,4.8 }
 cylinder { <1105977.94, 0, 6410820.6>,<1105968.83, 0, 6410818.67>,4.8 }
 sphere { <1106047.62, 0, 6410856.87>,4.8 }
 cylinder { <1106047.62, 0, 6410856.87>,<1105977.94, 0, 6410820.6>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=12710950 */
object {union { sphere { <1105995.21, 0, 6410556.85>,4.0 }
 sphere { <1106026.09, 0, 6410720.2>,4.0 }
 cylinder { <1106026.09, 0, 6410720.2>,<1105995.21, 0, 6410556.85>,4.0 }
 sphere { <1106026.78, 0, 6410731.74>,4.0 }
 cylinder { <1106026.78, 0, 6410731.74>,<1106026.09, 0, 6410720.2>,4.0 }
 sphere { <1106021.14, 0, 6410747.17>,4.0 }
 cylinder { <1106021.14, 0, 6410747.17>,<1106026.78, 0, 6410731.74>,4.0 }
 sphere { <1106003.45, 0, 6410767.11>,4.0 }
 cylinder { <1106003.45, 0, 6410767.11>,<1106021.14, 0, 6410747.17>,4.0 }
 sphere { <1105989.15, 0, 6410785.18>,4.0 }
 cylinder { <1105989.15, 0, 6410785.18>,<1106003.45, 0, 6410767.11>,4.0 }
 sphere { <1105968.83, 0, 6410818.67>,4.0 }
 cylinder { <1105968.83, 0, 6410818.67>,<1105989.15, 0, 6410785.18>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105995.21, 0, 6410556.85>,4.8 }
 sphere { <1106026.09, 0, 6410720.2>,4.8 }
 cylinder { <1106026.09, 0, 6410720.2>,<1105995.21, 0, 6410556.85>,4.8 }
 sphere { <1106026.78, 0, 6410731.74>,4.8 }
 cylinder { <1106026.78, 0, 6410731.74>,<1106026.09, 0, 6410720.2>,4.8 }
 sphere { <1106021.14, 0, 6410747.17>,4.8 }
 cylinder { <1106021.14, 0, 6410747.17>,<1106026.78, 0, 6410731.74>,4.8 }
 sphere { <1106003.45, 0, 6410767.11>,4.8 }
 cylinder { <1106003.45, 0, 6410767.11>,<1106021.14, 0, 6410747.17>,4.8 }
 sphere { <1105989.15, 0, 6410785.18>,4.8 }
 cylinder { <1105989.15, 0, 6410785.18>,<1106003.45, 0, 6410767.11>,4.8 }
 sphere { <1105968.83, 0, 6410818.67>,4.8 }
 cylinder { <1105968.83, 0, 6410818.67>,<1105989.15, 0, 6410785.18>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=28413145 */
object {union { sphere { <1105976.42, 0, 6410561.28>,4.0 }
 sphere { <1105995.21, 0, 6410556.85>,4.0 }
 cylinder { <1105995.21, 0, 6410556.85>,<1105976.42, 0, 6410561.28>,4.0 }
 sphere { <1106009.23, 0, 6410553.07>,4.0 }
 cylinder { <1106009.23, 0, 6410553.07>,<1105995.21, 0, 6410556.85>,4.0 }
 sphere { <1106090.34, 0, 6410527.46>,4.0 }
 cylinder { <1106090.34, 0, 6410527.46>,<1106009.23, 0, 6410553.07>,4.0 }
 sphere { <1106135.89, 0, 6410511.98>,4.0 }
 cylinder { <1106135.89, 0, 6410511.98>,<1106090.34, 0, 6410527.46>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105976.42, 0, 6410561.28>,4.8 }
 sphere { <1105995.21, 0, 6410556.85>,4.8 }
 cylinder { <1105995.21, 0, 6410556.85>,<1105976.42, 0, 6410561.28>,4.8 }
 sphere { <1106009.23, 0, 6410553.07>,4.8 }
 cylinder { <1106009.23, 0, 6410553.07>,<1105995.21, 0, 6410556.85>,4.8 }
 sphere { <1106090.34, 0, 6410527.46>,4.8 }
 cylinder { <1106090.34, 0, 6410527.46>,<1106009.23, 0, 6410553.07>,4.8 }
 sphere { <1106135.89, 0, 6410511.98>,4.8 }
 cylinder { <1106135.89, 0, 6410511.98>,<1106090.34, 0, 6410527.46>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10152032 */
object {union { sphere { <1106025.2, 0, 6409809.57>,4.0 }
 sphere { <1106061.37, 0, 6409776.46>,4.0 }
 cylinder { <1106061.37, 0, 6409776.46>,<1106025.2, 0, 6409809.57>,4.0 }
 sphere { <1106068.28, 0, 6409768.32>,4.0 }
 cylinder { <1106068.28, 0, 6409768.32>,<1106061.37, 0, 6409776.46>,4.0 }
 sphere { <1106090.66, 0, 6409740.67>,4.0 }
 cylinder { <1106090.66, 0, 6409740.67>,<1106068.28, 0, 6409768.32>,4.0 }
 sphere { <1106128.07, 0, 6409683.32>,4.0 }
 cylinder { <1106128.07, 0, 6409683.32>,<1106090.66, 0, 6409740.67>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1106025.2, 0, 6409809.57>,4.8 }
 sphere { <1106061.37, 0, 6409776.46>,4.8 }
 cylinder { <1106061.37, 0, 6409776.46>,<1106025.2, 0, 6409809.57>,4.8 }
 sphere { <1106068.28, 0, 6409768.32>,4.8 }
 cylinder { <1106068.28, 0, 6409768.32>,<1106061.37, 0, 6409776.46>,4.8 }
 sphere { <1106090.66, 0, 6409740.67>,4.8 }
 cylinder { <1106090.66, 0, 6409740.67>,<1106068.28, 0, 6409768.32>,4.8 }
 sphere { <1106128.07, 0, 6409683.32>,4.8 }
 cylinder { <1106128.07, 0, 6409683.32>,<1106090.66, 0, 6409740.67>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=17978041 */
object {union { sphere { <1106076.77, 0, 6410500.04>,4.0 }
 sphere { <1106090.34, 0, 6410527.46>,4.0 }
 cylinder { <1106090.34, 0, 6410527.46>,<1106076.77, 0, 6410500.04>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1106076.77, 0, 6410500.04>,4.8 }
 sphere { <1106090.34, 0, 6410527.46>,4.8 }
 cylinder { <1106090.34, 0, 6410527.46>,<1106076.77, 0, 6410500.04>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10152034 */
object {union { sphere { <1106090.66, 0, 6409740.67>,4.0 }
 sphere { <1106135.61, 0, 6409759.75>,4.0 }
 cylinder { <1106135.61, 0, 6409759.75>,<1106090.66, 0, 6409740.67>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1106090.66, 0, 6409740.67>,4.8 }
 sphere { <1106135.61, 0, 6409759.75>,4.8 }
 cylinder { <1106135.61, 0, 6409759.75>,<1106090.66, 0, 6409740.67>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=12710939 */
object {union { sphere { <1106335.25, 0, 6410966.06>,2.4 }
 sphere { <1106203.73, 0, 6410863.18>,2.4 }
 cylinder { <1106203.73, 0, 6410863.18>,<1106335.25, 0, 6410966.06>,2.4 }
 sphere { <1106102.07, 0, 6410775.54>,2.4 }
 cylinder { <1106102.07, 0, 6410775.54>,<1106203.73, 0, 6410863.18>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1106335.25, 0, 6410966.06>,2.88 }
 sphere { <1106203.73, 0, 6410863.18>,2.88 }
 cylinder { <1106203.73, 0, 6410863.18>,<1106335.25, 0, 6410966.06>,2.88 }
 sphere { <1106102.07, 0, 6410775.54>,2.88 }
 cylinder { <1106102.07, 0, 6410775.54>,<1106203.73, 0, 6410863.18>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=10202687 */
object {union { sphere { <1106336.05, 0, 6409795.65>,4.0 }
 sphere { <1106253.54, 0, 6409753.41>,4.0 }
 cylinder { <1106253.54, 0, 6409753.41>,<1106336.05, 0, 6409795.65>,4.0 }
 sphere { <1106197.62, 0, 6409726.34>,4.0 }
 cylinder { <1106197.62, 0, 6409726.34>,<1106253.54, 0, 6409753.41>,4.0 }
 sphere { <1106164.85, 0, 6409707.79>,4.0 }
 cylinder { <1106164.85, 0, 6409707.79>,<1106197.62, 0, 6409726.34>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1106336.05, 0, 6409795.65>,4.8 }
 sphere { <1106253.54, 0, 6409753.41>,4.8 }
 cylinder { <1106253.54, 0, 6409753.41>,<1106336.05, 0, 6409795.65>,4.8 }
 sphere { <1106197.62, 0, 6409726.34>,4.8 }
 cylinder { <1106197.62, 0, 6409726.34>,<1106253.54, 0, 6409753.41>,4.8 }
 sphere { <1106164.85, 0, 6409707.79>,4.8 }
 cylinder { <1106164.85, 0, 6409707.79>,<1106197.62, 0, 6409726.34>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=6110040 */
object {union { sphere { <1106208.38, 0, 6410659.14>,2.4 }
 sphere { <1106296.79, 0, 6410787.56>,2.4 }
 cylinder { <1106296.79, 0, 6410787.56>,<1106208.38, 0, 6410659.14>,2.4 }
 sphere { <1106317.81, 0, 6410816.86>,2.4 }
 cylinder { <1106317.81, 0, 6410816.86>,<1106296.79, 0, 6410787.56>,2.4 }
 sphere { <1106384.16, 0, 6410895.79>,2.4 }
 cylinder { <1106384.16, 0, 6410895.79>,<1106317.81, 0, 6410816.86>,2.4 }
 sphere { <1106462.92, 0, 6410964.37>,2.4 }
 cylinder { <1106462.92, 0, 6410964.37>,<1106384.16, 0, 6410895.79>,2.4 }
 sphere { <1106493.09, 0, 6410989.96>,2.4 }
 cylinder { <1106493.09, 0, 6410989.96>,<1106462.92, 0, 6410964.37>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1106208.38, 0, 6410659.14>,2.88 }
 sphere { <1106296.79, 0, 6410787.56>,2.88 }
 cylinder { <1106296.79, 0, 6410787.56>,<1106208.38, 0, 6410659.14>,2.88 }
 sphere { <1106317.81, 0, 6410816.86>,2.88 }
 cylinder { <1106317.81, 0, 6410816.86>,<1106296.79, 0, 6410787.56>,2.88 }
 sphere { <1106384.16, 0, 6410895.79>,2.88 }
 cylinder { <1106384.16, 0, 6410895.79>,<1106317.81, 0, 6410816.86>,2.88 }
 sphere { <1106462.92, 0, 6410964.37>,2.88 }
 cylinder { <1106462.92, 0, 6410964.37>,<1106384.16, 0, 6410895.79>,2.88 }
 sphere { <1106493.09, 0, 6410989.96>,2.88 }
 cylinder { <1106493.09, 0, 6410989.96>,<1106462.92, 0, 6410964.37>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=12710937 */
object {union { sphere { <1106384.16, 0, 6410895.79>,4.0 }
 sphere { <1106335.25, 0, 6410966.06>,4.0 }
 cylinder { <1106335.25, 0, 6410966.06>,<1106384.16, 0, 6410895.79>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1106384.16, 0, 6410895.79>,4.8 }
 sphere { <1106335.25, 0, 6410966.06>,4.8 }
 cylinder { <1106335.25, 0, 6410966.06>,<1106384.16, 0, 6410895.79>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=53827831 */
object {union { sphere { <1106654.94, 0, 6409565.95>,4.0 }
 sphere { <1106675.11, 0, 6409581.36>,4.0 }
 cylinder { <1106675.11, 0, 6409581.36>,<1106654.94, 0, 6409565.95>,4.0 }
 sphere { <1106697.33, 0, 6409607.25>,4.0 }
 cylinder { <1106697.33, 0, 6409607.25>,<1106675.11, 0, 6409581.36>,4.0 }
 sphere { <1106718.15, 0, 6409661.94>,4.0 }
 cylinder { <1106718.15, 0, 6409661.94>,<1106697.33, 0, 6409607.25>,4.0 }
 sphere { <1106722.99, 0, 6409707.13>,4.0 }
 cylinder { <1106722.99, 0, 6409707.13>,<1106718.15, 0, 6409661.94>,4.0 }
 sphere { <1106731.35, 0, 6409819.69>,4.0 }
 cylinder { <1106731.35, 0, 6409819.69>,<1106722.99, 0, 6409707.13>,4.0 }
 sphere { <1106734.7, 0, 6409880.71>,4.0 }
 cylinder { <1106734.7, 0, 6409880.71>,<1106731.35, 0, 6409819.69>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1106654.94, 0, 6409565.95>,4.8 }
 sphere { <1106675.11, 0, 6409581.36>,4.8 }
 cylinder { <1106675.11, 0, 6409581.36>,<1106654.94, 0, 6409565.95>,4.8 }
 sphere { <1106697.33, 0, 6409607.25>,4.8 }
 cylinder { <1106697.33, 0, 6409607.25>,<1106675.11, 0, 6409581.36>,4.8 }
 sphere { <1106718.15, 0, 6409661.94>,4.8 }
 cylinder { <1106718.15, 0, 6409661.94>,<1106697.33, 0, 6409607.25>,4.8 }
 sphere { <1106722.99, 0, 6409707.13>,4.8 }
 cylinder { <1106722.99, 0, 6409707.13>,<1106718.15, 0, 6409661.94>,4.8 }
 sphere { <1106731.35, 0, 6409819.69>,4.8 }
 cylinder { <1106731.35, 0, 6409819.69>,<1106722.99, 0, 6409707.13>,4.8 }
 sphere { <1106734.7, 0, 6409880.71>,4.8 }
 cylinder { <1106734.7, 0, 6409880.71>,<1106731.35, 0, 6409819.69>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=5735862 */
object {union { sphere { <1106747.97, 0, 6409907.77>,4.0 }
 sphere { <1106880.48, 0, 6409868.12>,4.0 }
 cylinder { <1106880.48, 0, 6409868.12>,<1106747.97, 0, 6409907.77>,4.0 }
 sphere { <1106890.52, 0, 6409864.92>,4.0 }
 cylinder { <1106890.52, 0, 6409864.92>,<1106880.48, 0, 6409868.12>,4.0 }
 sphere { <1106977.4, 0, 6409846.31>,4.0 }
 cylinder { <1106977.4, 0, 6409846.31>,<1106890.52, 0, 6409864.92>,4.0 }
 sphere { <1107048.64, 0, 6409828.91>,4.0 }
 cylinder { <1107048.64, 0, 6409828.91>,<1106977.4, 0, 6409846.31>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1106747.97, 0, 6409907.77>,4.8 }
 sphere { <1106880.48, 0, 6409868.12>,4.8 }
 cylinder { <1106880.48, 0, 6409868.12>,<1106747.97, 0, 6409907.77>,4.8 }
 sphere { <1106890.52, 0, 6409864.92>,4.8 }
 cylinder { <1106890.52, 0, 6409864.92>,<1106880.48, 0, 6409868.12>,4.8 }
 sphere { <1106977.4, 0, 6409846.31>,4.8 }
 cylinder { <1106977.4, 0, 6409846.31>,<1106890.52, 0, 6409864.92>,4.8 }
 sphere { <1107048.64, 0, 6409828.91>,4.8 }
 cylinder { <1107048.64, 0, 6409828.91>,<1106977.4, 0, 6409846.31>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=50844926 */
object {union { sphere { <1106786.4, 0, 6410583.14>,2.4 }
 sphere { <1106777.57, 0, 6410523.57>,2.4 }
 cylinder { <1106777.57, 0, 6410523.57>,<1106786.4, 0, 6410583.14>,2.4 }
 sphere { <1106775.29, 0, 6410507.94>,2.4 }
 cylinder { <1106775.29, 0, 6410507.94>,<1106777.57, 0, 6410523.57>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1106786.4, 0, 6410583.14>,2.88 }
 sphere { <1106777.57, 0, 6410523.57>,2.88 }
 cylinder { <1106777.57, 0, 6410523.57>,<1106786.4, 0, 6410583.14>,2.88 }
 sphere { <1106775.29, 0, 6410507.94>,2.88 }
 cylinder { <1106775.29, 0, 6410507.94>,<1106777.57, 0, 6410523.57>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=50844925 */
object {union { sphere { <1106822.96, 0, 6410829.81>,2.4 }
 sphere { <1106815.47, 0, 6410779.28>,2.4 }
 cylinder { <1106815.47, 0, 6410779.28>,<1106822.96, 0, 6410829.81>,2.4 }
 sphere { <1106807.01, 0, 6410722.2>,2.4 }
 cylinder { <1106807.01, 0, 6410722.2>,<1106815.47, 0, 6410779.28>,2.4 }
 sphere { <1106803.38, 0, 6410697.7>,2.4 }
 cylinder { <1106803.38, 0, 6410697.7>,<1106807.01, 0, 6410722.2>,2.4 }
 sphere { <1106786.4, 0, 6410583.14>,2.4 }
 cylinder { <1106786.4, 0, 6410583.14>,<1106803.38, 0, 6410697.7>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1106822.96, 0, 6410829.81>,2.88 }
 sphere { <1106815.47, 0, 6410779.28>,2.88 }
 cylinder { <1106815.47, 0, 6410779.28>,<1106822.96, 0, 6410829.81>,2.88 }
 sphere { <1106807.01, 0, 6410722.2>,2.88 }
 cylinder { <1106807.01, 0, 6410722.2>,<1106815.47, 0, 6410779.28>,2.88 }
 sphere { <1106803.38, 0, 6410697.7>,2.88 }
 cylinder { <1106803.38, 0, 6410697.7>,<1106807.01, 0, 6410722.2>,2.88 }
 sphere { <1106786.4, 0, 6410583.14>,2.88 }
 cylinder { <1106786.4, 0, 6410583.14>,<1106803.38, 0, 6410697.7>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=58740408 */
object {union { sphere { <1106680.74, 0, 6410735.48>,4.0 }
 sphere { <1106638.9, 0, 6410701.8>,4.0 }
 cylinder { <1106638.9, 0, 6410701.8>,<1106680.74, 0, 6410735.48>,4.0 }
 sphere { <1106581.79, 0, 6410655.43>,4.0 }
 cylinder { <1106581.79, 0, 6410655.43>,<1106638.9, 0, 6410701.8>,4.0 }
 sphere { <1106539.18, 0, 6410621.62>,4.0 }
 cylinder { <1106539.18, 0, 6410621.62>,<1106581.79, 0, 6410655.43>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1106680.74, 0, 6410735.48>,4.8 }
 sphere { <1106638.9, 0, 6410701.8>,4.8 }
 cylinder { <1106638.9, 0, 6410701.8>,<1106680.74, 0, 6410735.48>,4.8 }
 sphere { <1106581.79, 0, 6410655.43>,4.8 }
 cylinder { <1106581.79, 0, 6410655.43>,<1106638.9, 0, 6410701.8>,4.8 }
 sphere { <1106539.18, 0, 6410621.62>,4.8 }
 cylinder { <1106539.18, 0, 6410621.62>,<1106581.79, 0, 6410655.43>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=58205719 */
object {union { sphere { <1106707.83, 0, 6410757.26>,4.0 }
 sphere { <1106680.74, 0, 6410735.48>,4.0 }
 cylinder { <1106680.74, 0, 6410735.48>,<1106707.83, 0, 6410757.26>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1106707.83, 0, 6410757.26>,4.8 }
 sphere { <1106680.74, 0, 6410735.48>,4.8 }
 cylinder { <1106680.74, 0, 6410735.48>,<1106707.83, 0, 6410757.26>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=8106714 */
object {union { sphere { <1105668.66, 0, 6410880.86>,2.4 }
 sphere { <1105677.12, 0, 6410858.63>,2.4 }
 cylinder { <1105677.12, 0, 6410858.63>,<1105668.66, 0, 6410880.86>,2.4 }
 sphere { <1105693.64, 0, 6410818.95>,2.4 }
 cylinder { <1105693.64, 0, 6410818.95>,<1105677.12, 0, 6410858.63>,2.4 }
 sphere { <1105707.16, 0, 6410773.85>,2.4 }
 cylinder { <1105707.16, 0, 6410773.85>,<1105693.64, 0, 6410818.95>,2.4 }
 sphere { <1105716.29, 0, 6410744.76>,2.4 }
 cylinder { <1105716.29, 0, 6410744.76>,<1105707.16, 0, 6410773.85>,2.4 }
 sphere { <1105722.84, 0, 6410727.6>,2.4 }
 cylinder { <1105722.84, 0, 6410727.6>,<1105716.29, 0, 6410744.76>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105668.66, 0, 6410880.86>,2.88 }
 sphere { <1105677.12, 0, 6410858.63>,2.88 }
 cylinder { <1105677.12, 0, 6410858.63>,<1105668.66, 0, 6410880.86>,2.88 }
 sphere { <1105693.64, 0, 6410818.95>,2.88 }
 cylinder { <1105693.64, 0, 6410818.95>,<1105677.12, 0, 6410858.63>,2.88 }
 sphere { <1105707.16, 0, 6410773.85>,2.88 }
 cylinder { <1105707.16, 0, 6410773.85>,<1105693.64, 0, 6410818.95>,2.88 }
 sphere { <1105716.29, 0, 6410744.76>,2.88 }
 cylinder { <1105716.29, 0, 6410744.76>,<1105707.16, 0, 6410773.85>,2.88 }
 sphere { <1105722.84, 0, 6410727.6>,2.88 }
 cylinder { <1105722.84, 0, 6410727.6>,<1105716.29, 0, 6410744.76>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=57837179 */
object {union { sphere { <1105722.84, 0, 6410727.6>,2.4 }
 sphere { <1105727.58, 0, 6410708.2>,2.4 }
 cylinder { <1105727.58, 0, 6410708.2>,<1105722.84, 0, 6410727.6>,2.4 }
 sphere { <1105736.05, 0, 6410681.7>,2.4 }
 cylinder { <1105736.05, 0, 6410681.7>,<1105727.58, 0, 6410708.2>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105722.84, 0, 6410727.6>,2.88 }
 sphere { <1105727.58, 0, 6410708.2>,2.88 }
 cylinder { <1105727.58, 0, 6410708.2>,<1105722.84, 0, 6410727.6>,2.88 }
 sphere { <1105736.05, 0, 6410681.7>,2.88 }
 cylinder { <1105736.05, 0, 6410681.7>,<1105727.58, 0, 6410708.2>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=58740409 */
object {union { sphere { <1106539.18, 0, 6410621.62>,4.0 }
 sphere { <1106505.63, 0, 6410588.78>,4.0 }
 cylinder { <1106505.63, 0, 6410588.78>,<1106539.18, 0, 6410621.62>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1106539.18, 0, 6410621.62>,4.8 }
 sphere { <1106505.63, 0, 6410588.78>,4.8 }
 cylinder { <1106505.63, 0, 6410588.78>,<1106539.18, 0, 6410621.62>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=9035401 */
object {union { sphere { <1105609.48, 0, 6410863.39>,2.4 }
 sphere { <1105606.87, 0, 6410856.23>,2.4 }
 cylinder { <1105606.87, 0, 6410856.23>,<1105609.48, 0, 6410863.39>,2.4 }
 sphere { <1105620.22, 0, 6410762.83>,2.4 }
 cylinder { <1105620.22, 0, 6410762.83>,<1105606.87, 0, 6410856.23>,2.4 }
 sphere { <1105626.01, 0, 6410702.17>,2.4 }
 cylinder { <1105626.01, 0, 6410702.17>,<1105620.22, 0, 6410762.83>,2.4 }
 sphere { <1105640.5, 0, 6410684.71>,2.4 }
 cylinder { <1105640.5, 0, 6410684.71>,<1105626.01, 0, 6410702.17>,2.4 }
 sphere { <1105646.36, 0, 6410677.64>,2.4 }
 cylinder { <1105646.36, 0, 6410677.64>,<1105640.5, 0, 6410684.71>,2.4 }
 sphere { <1105663.84, 0, 6410663.24>,2.4 }
 cylinder { <1105663.84, 0, 6410663.24>,<1105646.36, 0, 6410677.64>,2.4 }
 sphere { <1105675.19, 0, 6410654.95>,2.4 }
 cylinder { <1105675.19, 0, 6410654.95>,<1105663.84, 0, 6410663.24>,2.4 }
 sphere { <1105683.8, 0, 6410650.26>,2.4 }
 cylinder { <1105683.8, 0, 6410650.26>,<1105675.19, 0, 6410654.95>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105609.48, 0, 6410863.39>,2.88 }
 sphere { <1105606.87, 0, 6410856.23>,2.88 }
 cylinder { <1105606.87, 0, 6410856.23>,<1105609.48, 0, 6410863.39>,2.88 }
 sphere { <1105620.22, 0, 6410762.83>,2.88 }
 cylinder { <1105620.22, 0, 6410762.83>,<1105606.87, 0, 6410856.23>,2.88 }
 sphere { <1105626.01, 0, 6410702.17>,2.88 }
 cylinder { <1105626.01, 0, 6410702.17>,<1105620.22, 0, 6410762.83>,2.88 }
 sphere { <1105640.5, 0, 6410684.71>,2.88 }
 cylinder { <1105640.5, 0, 6410684.71>,<1105626.01, 0, 6410702.17>,2.88 }
 sphere { <1105646.36, 0, 6410677.64>,2.88 }
 cylinder { <1105646.36, 0, 6410677.64>,<1105640.5, 0, 6410684.71>,2.88 }
 sphere { <1105663.84, 0, 6410663.24>,2.88 }
 cylinder { <1105663.84, 0, 6410663.24>,<1105646.36, 0, 6410677.64>,2.88 }
 sphere { <1105675.19, 0, 6410654.95>,2.88 }
 cylinder { <1105675.19, 0, 6410654.95>,<1105663.84, 0, 6410663.24>,2.88 }
 sphere { <1105683.8, 0, 6410650.26>,2.88 }
 cylinder { <1105683.8, 0, 6410650.26>,<1105675.19, 0, 6410654.95>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=54265639 */
object {union { sphere { <1105967.28, 0, 6410050.64>,4.0 }
 sphere { <1105997.89, 0, 6410050.4>,4.0 }
 cylinder { <1105997.89, 0, 6410050.4>,<1105967.28, 0, 6410050.64>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105967.28, 0, 6410050.64>,4.8 }
 sphere { <1105997.89, 0, 6410050.4>,4.8 }
 cylinder { <1105997.89, 0, 6410050.4>,<1105967.28, 0, 6410050.64>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=32295118 */
object {union { sphere { <1105997.89, 0, 6410050.4>,4.0 }
 sphere { <1106035.21, 0, 6410051.45>,4.0 }
 cylinder { <1106035.21, 0, 6410051.45>,<1105997.89, 0, 6410050.4>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105997.89, 0, 6410050.4>,4.8 }
 sphere { <1106035.21, 0, 6410051.45>,4.8 }
 cylinder { <1106035.21, 0, 6410051.45>,<1105997.89, 0, 6410050.4>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10213068 */
object {union { sphere { <1105629.66, 0, 6410400.27>,4.0 }
 sphere { <1105594.11, 0, 6410415.39>,4.0 }
 cylinder { <1105594.11, 0, 6410415.39>,<1105629.66, 0, 6410400.27>,4.0 }
 sphere { <1105561.85, 0, 6410428.22>,4.0 }
 cylinder { <1105561.85, 0, 6410428.22>,<1105594.11, 0, 6410415.39>,4.0 }
 sphere { <1105540.86, 0, 6410436.35>,4.0 }
 cylinder { <1105540.86, 0, 6410436.35>,<1105561.85, 0, 6410428.22>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105629.66, 0, 6410400.27>,4.8 }
 sphere { <1105594.11, 0, 6410415.39>,4.8 }
 cylinder { <1105594.11, 0, 6410415.39>,<1105629.66, 0, 6410400.27>,4.8 }
 sphere { <1105561.85, 0, 6410428.22>,4.8 }
 cylinder { <1105561.85, 0, 6410428.22>,<1105594.11, 0, 6410415.39>,4.8 }
 sphere { <1105540.86, 0, 6410436.35>,4.8 }
 cylinder { <1105540.86, 0, 6410436.35>,<1105561.85, 0, 6410428.22>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=4473087 */
object {union { sphere { <1105618.91, 0, 6410042.43>,4.0 }
 sphere { <1105631.93, 0, 6410120.13>,4.0 }
 cylinder { <1105631.93, 0, 6410120.13>,<1105618.91, 0, 6410042.43>,4.0 }
 sphere { <1105633.13, 0, 6410154.41>,4.0 }
 cylinder { <1105633.13, 0, 6410154.41>,<1105631.93, 0, 6410120.13>,4.0 }
 sphere { <1105619.89, 0, 6410195.38>,4.0 }
 cylinder { <1105619.89, 0, 6410195.38>,<1105633.13, 0, 6410154.41>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105618.91, 0, 6410042.43>,4.8 }
 sphere { <1105631.93, 0, 6410120.13>,4.8 }
 cylinder { <1105631.93, 0, 6410120.13>,<1105618.91, 0, 6410042.43>,4.8 }
 sphere { <1105633.13, 0, 6410154.41>,4.8 }
 cylinder { <1105633.13, 0, 6410154.41>,<1105631.93, 0, 6410120.13>,4.8 }
 sphere { <1105619.89, 0, 6410195.38>,4.8 }
 cylinder { <1105619.89, 0, 6410195.38>,<1105633.13, 0, 6410154.41>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=47907684 */
object {union { sphere { <1105705.93, 0, 6409644.51>,2.4 }
 sphere { <1105683.89, 0, 6409700.06>,2.4 }
 cylinder { <1105683.89, 0, 6409700.06>,<1105705.93, 0, 6409644.51>,2.4 }
 sphere { <1105666.91, 0, 6409751.96>,2.4 }
 cylinder { <1105666.91, 0, 6409751.96>,<1105683.89, 0, 6409700.06>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105705.93, 0, 6409644.51>,2.88 }
 sphere { <1105683.89, 0, 6409700.06>,2.88 }
 cylinder { <1105683.89, 0, 6409700.06>,<1105705.93, 0, 6409644.51>,2.88 }
 sphere { <1105666.91, 0, 6409751.96>,2.88 }
 cylinder { <1105666.91, 0, 6409751.96>,<1105683.89, 0, 6409700.06>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=4568540 */
object {union { sphere { <1105757.47, 0, 6410909.53>,2.4 }
 sphere { <1105668.66, 0, 6410880.86>,2.4 }
 cylinder { <1105668.66, 0, 6410880.86>,<1105757.47, 0, 6410909.53>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105757.47, 0, 6410909.53>,2.88 }
 sphere { <1105668.66, 0, 6410880.86>,2.88 }
 cylinder { <1105668.66, 0, 6410880.86>,<1105757.47, 0, 6410909.53>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=12710930 */
object {union { sphere { <1106493.09, 0, 6410989.96>,4.0 }
 sphere { <1106542.82, 0, 6410942.75>,4.0 }
 cylinder { <1106542.82, 0, 6410942.75>,<1106493.09, 0, 6410989.96>,4.0 }
 sphere { <1106586.76, 0, 6410898.31>,4.0 }
 cylinder { <1106586.76, 0, 6410898.31>,<1106542.82, 0, 6410942.75>,4.0 }
 sphere { <1106630.2, 0, 6410854.37>,4.0 }
 cylinder { <1106630.2, 0, 6410854.37>,<1106586.76, 0, 6410898.31>,4.0 }
 sphere { <1106707.83, 0, 6410757.26>,4.0 }
 cylinder { <1106707.83, 0, 6410757.26>,<1106630.2, 0, 6410854.37>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1106493.09, 0, 6410989.96>,4.8 }
 sphere { <1106542.82, 0, 6410942.75>,4.8 }
 cylinder { <1106542.82, 0, 6410942.75>,<1106493.09, 0, 6410989.96>,4.8 }
 sphere { <1106586.76, 0, 6410898.31>,4.8 }
 cylinder { <1106586.76, 0, 6410898.31>,<1106542.82, 0, 6410942.75>,4.8 }
 sphere { <1106630.2, 0, 6410854.37>,4.8 }
 cylinder { <1106630.2, 0, 6410854.37>,<1106586.76, 0, 6410898.31>,4.8 }
 sphere { <1106707.83, 0, 6410757.26>,4.8 }
 cylinder { <1106707.83, 0, 6410757.26>,<1106630.2, 0, 6410854.37>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=57837182 */
object {union { sphere { <1105602.43, 0, 6410231.11>,4.0 }
 sphere { <1105597.78, 0, 6410256.34>,4.0 }
 cylinder { <1105597.78, 0, 6410256.34>,<1105602.43, 0, 6410231.11>,4.0 }
 sphere { <1105600.14, 0, 6410298.39>,4.0 }
 cylinder { <1105600.14, 0, 6410298.39>,<1105597.78, 0, 6410256.34>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1105602.43, 0, 6410231.11>,4.8 }
 sphere { <1105597.78, 0, 6410256.34>,4.8 }
 cylinder { <1105597.78, 0, 6410256.34>,<1105602.43, 0, 6410231.11>,4.8 }
 sphere { <1105600.14, 0, 6410298.39>,4.8 }
 cylinder { <1105600.14, 0, 6410298.39>,<1105597.78, 0, 6410256.34>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=4996462 */
object {union { sphere { <1106827.64, 0, 6410861.33>,4.0 }
 sphere { <1106807.16, 0, 6410833.41>,4.0 }
 cylinder { <1106807.16, 0, 6410833.41>,<1106827.64, 0, 6410861.33>,4.0 }
 sphere { <1106793.4, 0, 6410822.83>,4.0 }
 cylinder { <1106793.4, 0, 6410822.83>,<1106807.16, 0, 6410833.41>,4.0 }
 sphere { <1106719.95, 0, 6410766.25>,4.0 }
 cylinder { <1106719.95, 0, 6410766.25>,<1106793.4, 0, 6410822.83>,4.0 }
 sphere { <1106707.83, 0, 6410757.26>,4.0 }
 cylinder { <1106707.83, 0, 6410757.26>,<1106719.95, 0, 6410766.25>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1106827.64, 0, 6410861.33>,4.8 }
 sphere { <1106807.16, 0, 6410833.41>,4.8 }
 cylinder { <1106807.16, 0, 6410833.41>,<1106827.64, 0, 6410861.33>,4.8 }
 sphere { <1106793.4, 0, 6410822.83>,4.8 }
 cylinder { <1106793.4, 0, 6410822.83>,<1106807.16, 0, 6410833.41>,4.8 }
 sphere { <1106719.95, 0, 6410766.25>,4.8 }
 cylinder { <1106719.95, 0, 6410766.25>,<1106793.4, 0, 6410822.83>,4.8 }
 sphere { <1106707.83, 0, 6410757.26>,4.8 }
 cylinder { <1106707.83, 0, 6410757.26>,<1106719.95, 0, 6410766.25>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=4996463 */
object {union { sphere { <1106814.3, 0, 6411360.59>,4.0 }
 sphere { <1106815.16, 0, 6411353.21>,4.0 }
 cylinder { <1106815.16, 0, 6411353.21>,<1106814.3, 0, 6411360.59>,4.0 }
 sphere { <1106814.07, 0, 6411345.81>,4.0 }
 cylinder { <1106814.07, 0, 6411345.81>,<1106815.16, 0, 6411353.21>,4.0 }
 sphere { <1106812.61, 0, 6411334.1>,4.0 }
 cylinder { <1106812.61, 0, 6411334.1>,<1106814.07, 0, 6411345.81>,4.0 }
 sphere { <1106811.1, 0, 6411321.04>,4.0 }
 cylinder { <1106811.1, 0, 6411321.04>,<1106812.61, 0, 6411334.1>,4.0 }
 sphere { <1106808.01, 0, 6411284.29>,4.0 }
 cylinder { <1106808.01, 0, 6411284.29>,<1106811.1, 0, 6411321.04>,4.0 }
 sphere { <1106807.1, 0, 6411271.36>,4.0 }
 cylinder { <1106807.1, 0, 6411271.36>,<1106808.01, 0, 6411284.29>,4.0 }
 sphere { <1106807.9, 0, 6411247.7>,4.0 }
 cylinder { <1106807.9, 0, 6411247.7>,<1106807.1, 0, 6411271.36>,4.0 }
 sphere { <1106817.73, 0, 6411195.1>,4.0 }
 cylinder { <1106817.73, 0, 6411195.1>,<1106807.9, 0, 6411247.7>,4.0 }
 sphere { <1106829.22, 0, 6411172.95>,4.0 }
 cylinder { <1106829.22, 0, 6411172.95>,<1106817.73, 0, 6411195.1>,4.0 }
 sphere { <1106838.19, 0, 6411135.31>,4.0 }
 cylinder { <1106838.19, 0, 6411135.31>,<1106829.22, 0, 6411172.95>,4.0 }
 sphere { <1106849.36, 0, 6411098.37>,4.0 }
 cylinder { <1106849.36, 0, 6411098.37>,<1106838.19, 0, 6411135.31>,4.0 }
 sphere { <1106854.67, 0, 6411060.38>,4.0 }
 cylinder { <1106854.67, 0, 6411060.38>,<1106849.36, 0, 6411098.37>,4.0 }
 sphere { <1106848.21, 0, 6410995.84>,4.0 }
 cylinder { <1106848.21, 0, 6410995.84>,<1106854.67, 0, 6411060.38>,4.0 }
 sphere { <1106846.96, 0, 6410983.32>,4.0 }
 cylinder { <1106846.96, 0, 6410983.32>,<1106848.21, 0, 6410995.84>,4.0 }
 sphere { <1106834.8, 0, 6410910.52>,4.0 }
 cylinder { <1106834.8, 0, 6410910.52>,<1106846.96, 0, 6410983.32>,4.0 }
 sphere { <1106831.77, 0, 6410891.6>,4.0 }
 cylinder { <1106831.77, 0, 6410891.6>,<1106834.8, 0, 6410910.52>,4.0 }
 sphere { <1106827.64, 0, 6410861.33>,4.0 }
 cylinder { <1106827.64, 0, 6410861.33>,<1106831.77, 0, 6410891.6>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_residential }
}

object {union { sphere { <1106814.3, 0, 6411360.59>,4.8 }
 sphere { <1106815.16, 0, 6411353.21>,4.8 }
 cylinder { <1106815.16, 0, 6411353.21>,<1106814.3, 0, 6411360.59>,4.8 }
 sphere { <1106814.07, 0, 6411345.81>,4.8 }
 cylinder { <1106814.07, 0, 6411345.81>,<1106815.16, 0, 6411353.21>,4.8 }
 sphere { <1106812.61, 0, 6411334.1>,4.8 }
 cylinder { <1106812.61, 0, 6411334.1>,<1106814.07, 0, 6411345.81>,4.8 }
 sphere { <1106811.1, 0, 6411321.04>,4.8 }
 cylinder { <1106811.1, 0, 6411321.04>,<1106812.61, 0, 6411334.1>,4.8 }
 sphere { <1106808.01, 0, 6411284.29>,4.8 }
 cylinder { <1106808.01, 0, 6411284.29>,<1106811.1, 0, 6411321.04>,4.8 }
 sphere { <1106807.1, 0, 6411271.36>,4.8 }
 cylinder { <1106807.1, 0, 6411271.36>,<1106808.01, 0, 6411284.29>,4.8 }
 sphere { <1106807.9, 0, 6411247.7>,4.8 }
 cylinder { <1106807.9, 0, 6411247.7>,<1106807.1, 0, 6411271.36>,4.8 }
 sphere { <1106817.73, 0, 6411195.1>,4.8 }
 cylinder { <1106817.73, 0, 6411195.1>,<1106807.9, 0, 6411247.7>,4.8 }
 sphere { <1106829.22, 0, 6411172.95>,4.8 }
 cylinder { <1106829.22, 0, 6411172.95>,<1106817.73, 0, 6411195.1>,4.8 }
 sphere { <1106838.19, 0, 6411135.31>,4.8 }
 cylinder { <1106838.19, 0, 6411135.31>,<1106829.22, 0, 6411172.95>,4.8 }
 sphere { <1106849.36, 0, 6411098.37>,4.8 }
 cylinder { <1106849.36, 0, 6411098.37>,<1106838.19, 0, 6411135.31>,4.8 }
 sphere { <1106854.67, 0, 6411060.38>,4.8 }
 cylinder { <1106854.67, 0, 6411060.38>,<1106849.36, 0, 6411098.37>,4.8 }
 sphere { <1106848.21, 0, 6410995.84>,4.8 }
 cylinder { <1106848.21, 0, 6410995.84>,<1106854.67, 0, 6411060.38>,4.8 }
 sphere { <1106846.96, 0, 6410983.32>,4.8 }
 cylinder { <1106846.96, 0, 6410983.32>,<1106848.21, 0, 6410995.84>,4.8 }
 sphere { <1106834.8, 0, 6410910.52>,4.8 }
 cylinder { <1106834.8, 0, 6410910.52>,<1106846.96, 0, 6410983.32>,4.8 }
 sphere { <1106831.77, 0, 6410891.6>,4.8 }
 cylinder { <1106831.77, 0, 6410891.6>,<1106834.8, 0, 6410910.52>,4.8 }
 sphere { <1106827.64, 0, 6410861.33>,4.8 }
 cylinder { <1106827.64, 0, 6410861.33>,<1106831.77, 0, 6410891.6>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=47907681 */
object {union { sphere { <1105756.56, 0, 6409622.57>,4.0 }
 sphere { <1105783.59, 0, 6409625.88>,4.0 }
 cylinder { <1105783.59, 0, 6409625.88>,<1105756.56, 0, 6409622.57>,4.0 }
 sphere { <1105813.93, 0, 6409635.25>,4.0 }
 cylinder { <1105813.93, 0, 6409635.25>,<1105783.59, 0, 6409625.88>,4.0 }
 sphere { <1105850.89, 0, 6409662.83>,4.0 }
 cylinder { <1105850.89, 0, 6409662.83>,<1105813.93, 0, 6409635.25>,4.0 }
 sphere { <1106025.2, 0, 6409809.57>,4.0 }
 cylinder { <1106025.2, 0, 6409809.57>,<1105850.89, 0, 6409662.83>,4.0 }
 sphere { <1106035.13, 0, 6409830.54>,4.0 }
 cylinder { <1106035.13, 0, 6409830.54>,<1106025.2, 0, 6409809.57>,4.0 }
 sphere { <1106039, 0, 6409858.12>,4.0 }
 cylinder { <1106039, 0, 6409858.12>,<1106035.13, 0, 6409830.54>,4.0 }
 sphere { <1106031.83, 0, 6409885.14>,4.0 }
 cylinder { <1106031.83, 0, 6409885.14>,<1106039, 0, 6409858.12>,4.0 }
 sphere { <1106023, 0, 6409935.89>,4.0 }
 cylinder { <1106023, 0, 6409935.89>,<1106031.83, 0, 6409885.14>,4.0 }
 sphere { <1106002.58, 0, 6410002.64>,4.0 }
 cylinder { <1106002.58, 0, 6410002.64>,<1106023, 0, 6409935.89>,4.0 }
 sphere { <1105997.89, 0, 6410050.4>,4.0 }
 cylinder { <1105997.89, 0, 6410050.4>,<1106002.58, 0, 6410002.64>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_living_street }
}

object {union { sphere { <1105756.56, 0, 6409622.57>,4.8 }
 sphere { <1105783.59, 0, 6409625.88>,4.8 }
 cylinder { <1105783.59, 0, 6409625.88>,<1105756.56, 0, 6409622.57>,4.8 }
 sphere { <1105813.93, 0, 6409635.25>,4.8 }
 cylinder { <1105813.93, 0, 6409635.25>,<1105783.59, 0, 6409625.88>,4.8 }
 sphere { <1105850.89, 0, 6409662.83>,4.8 }
 cylinder { <1105850.89, 0, 6409662.83>,<1105813.93, 0, 6409635.25>,4.8 }
 sphere { <1106025.2, 0, 6409809.57>,4.8 }
 cylinder { <1106025.2, 0, 6409809.57>,<1105850.89, 0, 6409662.83>,4.8 }
 sphere { <1106035.13, 0, 6409830.54>,4.8 }
 cylinder { <1106035.13, 0, 6409830.54>,<1106025.2, 0, 6409809.57>,4.8 }
 sphere { <1106039, 0, 6409858.12>,4.8 }
 cylinder { <1106039, 0, 6409858.12>,<1106035.13, 0, 6409830.54>,4.8 }
 sphere { <1106031.83, 0, 6409885.14>,4.8 }
 cylinder { <1106031.83, 0, 6409885.14>,<1106039, 0, 6409858.12>,4.8 }
 sphere { <1106023, 0, 6409935.89>,4.8 }
 cylinder { <1106023, 0, 6409935.89>,<1106031.83, 0, 6409885.14>,4.8 }
 sphere { <1106002.58, 0, 6410002.64>,4.8 }
 cylinder { <1106002.58, 0, 6410002.64>,<1106023, 0, 6409935.89>,4.8 }
 sphere { <1105997.89, 0, 6410050.4>,4.8 }
 cylinder { <1105997.89, 0, 6410050.4>,<1106002.58, 0, 6410002.64>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=23034046 */
object {union { sphere { <1105865.68, 0, 6410772.64>,2.4 }
 sphere { <1105851.67, 0, 6410764.69>,2.4 }
 cylinder { <1105851.67, 0, 6410764.69>,<1105865.68, 0, 6410772.64>,2.4 }
 sphere { <1105811.23, 0, 6410760.8>,2.4 }
 cylinder { <1105811.23, 0, 6410760.8>,<1105851.67, 0, 6410764.69>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_living_street }
}

object {union { sphere { <1105865.68, 0, 6410772.64>,2.88 }
 sphere { <1105851.67, 0, 6410764.69>,2.88 }
 cylinder { <1105851.67, 0, 6410764.69>,<1105865.68, 0, 6410772.64>,2.88 }
 sphere { <1105811.23, 0, 6410760.8>,2.88 }
 cylinder { <1105811.23, 0, 6410760.8>,<1105851.67, 0, 6410764.69>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=12710971 */
object {union { sphere { <1105817.69, 0, 6410149.93>,4.0 }
 sphere { <1105823.36, 0, 6410178.55>,4.0 }
 cylinder { <1105823.36, 0, 6410178.55>,<1105817.69, 0, 6410149.93>,4.0 }
 sphere { <1105839.92, 0, 6410208.47>,4.0 }
 cylinder { <1105839.92, 0, 6410208.47>,<1105823.36, 0, 6410178.55>,4.0 }
 sphere { <1105867.54, 0, 6410248.97>,4.0 }
 cylinder { <1105867.54, 0, 6410248.97>,<1105839.92, 0, 6410208.47>,4.0 }
 sphere { <1105884.22, 0, 6410289.43>,4.0 }
 cylinder { <1105884.22, 0, 6410289.43>,<1105867.54, 0, 6410248.97>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_living_street }
}

object {union { sphere { <1105817.69, 0, 6410149.93>,4.8 }
 sphere { <1105823.36, 0, 6410178.55>,4.8 }
 cylinder { <1105823.36, 0, 6410178.55>,<1105817.69, 0, 6410149.93>,4.8 }
 sphere { <1105839.92, 0, 6410208.47>,4.8 }
 cylinder { <1105839.92, 0, 6410208.47>,<1105823.36, 0, 6410178.55>,4.8 }
 sphere { <1105867.54, 0, 6410248.97>,4.8 }
 cylinder { <1105867.54, 0, 6410248.97>,<1105839.92, 0, 6410208.47>,4.8 }
 sphere { <1105884.22, 0, 6410289.43>,4.8 }
 cylinder { <1105884.22, 0, 6410289.43>,<1105867.54, 0, 6410248.97>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=12710997 */
object {union { sphere { <1105865.68, 0, 6410772.64>,4.0 }
 sphere { <1105865.98, 0, 6410797.87>,4.0 }
 cylinder { <1105865.98, 0, 6410797.87>,<1105865.68, 0, 6410772.64>,4.0 }
 sphere { <1105876.77, 0, 6410826.78>,4.0 }
 cylinder { <1105876.77, 0, 6410826.78>,<1105865.98, 0, 6410797.87>,4.0 }
 sphere { <1105890.14, 0, 6410852.21>,4.0 }
 cylinder { <1105890.14, 0, 6410852.21>,<1105876.77, 0, 6410826.78>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_living_street }
}

object {union { sphere { <1105865.68, 0, 6410772.64>,4.8 }
 sphere { <1105865.98, 0, 6410797.87>,4.8 }
 cylinder { <1105865.98, 0, 6410797.87>,<1105865.68, 0, 6410772.64>,4.8 }
 sphere { <1105876.77, 0, 6410826.78>,4.8 }
 cylinder { <1105876.77, 0, 6410826.78>,<1105865.98, 0, 6410797.87>,4.8 }
 sphere { <1105890.14, 0, 6410852.21>,4.8 }
 cylinder { <1105890.14, 0, 6410852.21>,<1105876.77, 0, 6410826.78>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=35145975 */
object {union { sphere { <1105884.22, 0, 6410289.43>,2.4 }
 sphere { <1105900.69, 0, 6410323.09>,2.4 }
 cylinder { <1105900.69, 0, 6410323.09>,<1105884.22, 0, 6410289.43>,2.4 }
 sphere { <1105916.8, 0, 6410362.66>,2.4 }
 cylinder { <1105916.8, 0, 6410362.66>,<1105900.69, 0, 6410323.09>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_living_street }
}

object {union { sphere { <1105884.22, 0, 6410289.43>,2.88 }
 sphere { <1105900.69, 0, 6410323.09>,2.88 }
 cylinder { <1105900.69, 0, 6410323.09>,<1105884.22, 0, 6410289.43>,2.88 }
 sphere { <1105916.8, 0, 6410362.66>,2.88 }
 cylinder { <1105916.8, 0, 6410362.66>,<1105900.69, 0, 6410323.09>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=12710967 */
object {union { sphere { <1105959.21, 0, 6410097.78>,4.0 }
 sphere { <1105955.46, 0, 6410142.65>,4.0 }
 cylinder { <1105955.46, 0, 6410142.65>,<1105959.21, 0, 6410097.78>,4.0 }
 sphere { <1105960.53, 0, 6410169.81>,4.0 }
 cylinder { <1105960.53, 0, 6410169.81>,<1105955.46, 0, 6410142.65>,4.0 }
 sphere { <1105968.35, 0, 6410207.09>,4.0 }
 cylinder { <1105968.35, 0, 6410207.09>,<1105960.53, 0, 6410169.81>,4.0 }
 sphere { <1105981.49, 0, 6410250.75>,4.0 }
 cylinder { <1105981.49, 0, 6410250.75>,<1105968.35, 0, 6410207.09>,4.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_living_street }
}

object {union { sphere { <1105959.21, 0, 6410097.78>,4.8 }
 sphere { <1105955.46, 0, 6410142.65>,4.8 }
 cylinder { <1105955.46, 0, 6410142.65>,<1105959.21, 0, 6410097.78>,4.8 }
 sphere { <1105960.53, 0, 6410169.81>,4.8 }
 cylinder { <1105960.53, 0, 6410169.81>,<1105955.46, 0, 6410142.65>,4.8 }
 sphere { <1105968.35, 0, 6410207.09>,4.8 }
 cylinder { <1105968.35, 0, 6410207.09>,<1105960.53, 0, 6410169.81>,4.8 }
 sphere { <1105981.49, 0, 6410250.75>,4.8 }
 cylinder { <1105981.49, 0, 6410250.75>,<1105968.35, 0, 6410207.09>,4.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=35145974 */
object {union { sphere { <1105981.49, 0, 6410250.75>,2.4 }
 sphere { <1106019.45, 0, 6410355.3>,2.4 }
 cylinder { <1106019.45, 0, 6410355.3>,<1105981.49, 0, 6410250.75>,2.4 }
 sphere { <1106047.98, 0, 6410434.48>,2.4 }
 cylinder { <1106047.98, 0, 6410434.48>,<1106019.45, 0, 6410355.3>,2.4 }
 sphere { <1106076.77, 0, 6410500.04>,2.4 }
 cylinder { <1106076.77, 0, 6410500.04>,<1106047.98, 0, 6410434.48>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_living_street }
}

object {union { sphere { <1105981.49, 0, 6410250.75>,2.88 }
 sphere { <1106019.45, 0, 6410355.3>,2.88 }
 cylinder { <1106019.45, 0, 6410355.3>,<1105981.49, 0, 6410250.75>,2.88 }
 sphere { <1106047.98, 0, 6410434.48>,2.88 }
 cylinder { <1106047.98, 0, 6410434.48>,<1106019.45, 0, 6410355.3>,2.88 }
 sphere { <1106076.77, 0, 6410500.04>,2.88 }
 cylinder { <1106076.77, 0, 6410500.04>,<1106047.98, 0, 6410434.48>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=58180049 */
object {union { sphere { <1105639.64, 0, 6410471.16>,2.5 }
 sphere { <1105641.78, 0, 6410410.89>,2.5 }
 cylinder { <1105641.78, 0, 6410410.89>,<1105639.64, 0, 6410471.16>,2.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_path }
}

object {union { sphere { <1105639.64, 0, 6410471.16>,3.0 }
 sphere { <1105641.78, 0, 6410410.89>,3.0 }
 cylinder { <1105641.78, 0, 6410410.89>,<1105639.64, 0, 6410471.16>,3.0 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=30094857 */
object {union { sphere { <1105925.85, 0, 6411033.61>,5.0 }
 sphere { <1106012.73, 0, 6410906.14>,5.0 }
 cylinder { <1106012.73, 0, 6410906.14>,<1105925.85, 0, 6411033.61>,5.0 }
 sphere { <1106047.62, 0, 6410856.87>,5.0 }
 cylinder { <1106047.62, 0, 6410856.87>,<1106012.73, 0, 6410906.14>,5.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_secondary }
}

object {union { sphere { <1105925.85, 0, 6411033.61>,6.0 }
 sphere { <1106012.73, 0, 6410906.14>,6.0 }
 cylinder { <1106012.73, 0, 6410906.14>,<1105925.85, 0, 6411033.61>,6.0 }
 sphere { <1106047.62, 0, 6410856.87>,6.0 }
 cylinder { <1106047.62, 0, 6410856.87>,<1106012.73, 0, 6410906.14>,6.0 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=4454269 */
object {union { sphere { <1106006.07, 0, 6410099.09>,5.0 }
 sphere { <1106004.38, 0, 6410125.72>,5.0 }
 cylinder { <1106004.38, 0, 6410125.72>,<1106006.07, 0, 6410099.09>,5.0 }
 sphere { <1106008.47, 0, 6410153.6>,5.0 }
 cylinder { <1106008.47, 0, 6410153.6>,<1106004.38, 0, 6410125.72>,5.0 }
 sphere { <1106026.21, 0, 6410206.85>,5.0 }
 cylinder { <1106026.21, 0, 6410206.85>,<1106008.47, 0, 6410153.6>,5.0 }
 sphere { <1106043.61, 0, 6410257.08>,5.0 }
 cylinder { <1106043.61, 0, 6410257.08>,<1106026.21, 0, 6410206.85>,5.0 }
 sphere { <1106064.67, 0, 6410317.88>,5.0 }
 cylinder { <1106064.67, 0, 6410317.88>,<1106043.61, 0, 6410257.08>,5.0 }
 sphere { <1106098.07, 0, 6410410.13>,5.0 }
 cylinder { <1106098.07, 0, 6410410.13>,<1106064.67, 0, 6410317.88>,5.0 }
 sphere { <1106100.32, 0, 6410416.39>,5.0 }
 cylinder { <1106100.32, 0, 6410416.39>,<1106098.07, 0, 6410410.13>,5.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_secondary }
}

object {union { sphere { <1106006.07, 0, 6410099.09>,6.0 }
 sphere { <1106004.38, 0, 6410125.72>,6.0 }
 cylinder { <1106004.38, 0, 6410125.72>,<1106006.07, 0, 6410099.09>,6.0 }
 sphere { <1106008.47, 0, 6410153.6>,6.0 }
 cylinder { <1106008.47, 0, 6410153.6>,<1106004.38, 0, 6410125.72>,6.0 }
 sphere { <1106026.21, 0, 6410206.85>,6.0 }
 cylinder { <1106026.21, 0, 6410206.85>,<1106008.47, 0, 6410153.6>,6.0 }
 sphere { <1106043.61, 0, 6410257.08>,6.0 }
 cylinder { <1106043.61, 0, 6410257.08>,<1106026.21, 0, 6410206.85>,6.0 }
 sphere { <1106064.67, 0, 6410317.88>,6.0 }
 cylinder { <1106064.67, 0, 6410317.88>,<1106043.61, 0, 6410257.08>,6.0 }
 sphere { <1106098.07, 0, 6410410.13>,6.0 }
 cylinder { <1106098.07, 0, 6410410.13>,<1106064.67, 0, 6410317.88>,6.0 }
 sphere { <1106100.32, 0, 6410416.39>,6.0 }
 cylinder { <1106100.32, 0, 6410416.39>,<1106098.07, 0, 6410410.13>,6.0 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59041363 */
object {union { sphere { <1106135.61, 0, 6409759.75>,5.0 }
 sphere { <1106129.07, 0, 6409777.41>,5.0 }
 cylinder { <1106129.07, 0, 6409777.41>,<1106135.61, 0, 6409759.75>,5.0 }
 sphere { <1106124.69, 0, 6409789.2>,5.0 }
 cylinder { <1106124.69, 0, 6409789.2>,<1106129.07, 0, 6409777.41>,5.0 }
 sphere { <1106093.05, 0, 6409874.26>,5.0 }
 cylinder { <1106093.05, 0, 6409874.26>,<1106124.69, 0, 6409789.2>,5.0 }
 sphere { <1106078.66, 0, 6409940.89>,5.0 }
 cylinder { <1106078.66, 0, 6409940.89>,<1106093.05, 0, 6409874.26>,5.0 }
 sphere { <1106044.32, 0, 6410029.05>,5.0 }
 cylinder { <1106044.32, 0, 6410029.05>,<1106078.66, 0, 6409940.89>,5.0 }
 sphere { <1106036.19, 0, 6410046.99>,5.0 }
 cylinder { <1106036.19, 0, 6410046.99>,<1106044.32, 0, 6410029.05>,5.0 }
 sphere { <1106035.21, 0, 6410051.45>,5.0 }
 cylinder { <1106035.21, 0, 6410051.45>,<1106036.19, 0, 6410046.99>,5.0 }
 sphere { <1106018.89, 0, 6410075.42>,5.0 }
 cylinder { <1106018.89, 0, 6410075.42>,<1106035.21, 0, 6410051.45>,5.0 }
 sphere { <1106006.07, 0, 6410099.09>,5.0 }
 cylinder { <1106006.07, 0, 6410099.09>,<1106018.89, 0, 6410075.42>,5.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_secondary }
}

object {union { sphere { <1106135.61, 0, 6409759.75>,6.0 }
 sphere { <1106129.07, 0, 6409777.41>,6.0 }
 cylinder { <1106129.07, 0, 6409777.41>,<1106135.61, 0, 6409759.75>,6.0 }
 sphere { <1106124.69, 0, 6409789.2>,6.0 }
 cylinder { <1106124.69, 0, 6409789.2>,<1106129.07, 0, 6409777.41>,6.0 }
 sphere { <1106093.05, 0, 6409874.26>,6.0 }
 cylinder { <1106093.05, 0, 6409874.26>,<1106124.69, 0, 6409789.2>,6.0 }
 sphere { <1106078.66, 0, 6409940.89>,6.0 }
 cylinder { <1106078.66, 0, 6409940.89>,<1106093.05, 0, 6409874.26>,6.0 }
 sphere { <1106044.32, 0, 6410029.05>,6.0 }
 cylinder { <1106044.32, 0, 6410029.05>,<1106078.66, 0, 6409940.89>,6.0 }
 sphere { <1106036.19, 0, 6410046.99>,6.0 }
 cylinder { <1106036.19, 0, 6410046.99>,<1106044.32, 0, 6410029.05>,6.0 }
 sphere { <1106035.21, 0, 6410051.45>,6.0 }
 cylinder { <1106035.21, 0, 6410051.45>,<1106036.19, 0, 6410046.99>,6.0 }
 sphere { <1106018.89, 0, 6410075.42>,6.0 }
 cylinder { <1106018.89, 0, 6410075.42>,<1106035.21, 0, 6410051.45>,6.0 }
 sphere { <1106006.07, 0, 6410099.09>,6.0 }
 cylinder { <1106006.07, 0, 6410099.09>,<1106018.89, 0, 6410075.42>,6.0 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=30191718 */
object {union { sphere { <1106047.62, 0, 6410856.87>,5.0 }
 sphere { <1106078.73, 0, 6410809.48>,5.0 }
 cylinder { <1106078.73, 0, 6410809.48>,<1106047.62, 0, 6410856.87>,5.0 }
 sphere { <1106095.81, 0, 6410784.3>,5.0 }
 cylinder { <1106095.81, 0, 6410784.3>,<1106078.73, 0, 6410809.48>,5.0 }
 sphere { <1106102.07, 0, 6410775.54>,5.0 }
 cylinder { <1106102.07, 0, 6410775.54>,<1106095.81, 0, 6410784.3>,5.0 }
 sphere { <1106146.46, 0, 6410722.05>,5.0 }
 cylinder { <1106146.46, 0, 6410722.05>,<1106102.07, 0, 6410775.54>,5.0 }
 sphere { <1106187.71, 0, 6410668.35>,5.0 }
 cylinder { <1106187.71, 0, 6410668.35>,<1106146.46, 0, 6410722.05>,5.0 }
 sphere { <1106208.38, 0, 6410659.14>,5.0 }
 cylinder { <1106208.38, 0, 6410659.14>,<1106187.71, 0, 6410668.35>,5.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_secondary }
}

object {union { sphere { <1106047.62, 0, 6410856.87>,6.0 }
 sphere { <1106078.73, 0, 6410809.48>,6.0 }
 cylinder { <1106078.73, 0, 6410809.48>,<1106047.62, 0, 6410856.87>,6.0 }
 sphere { <1106095.81, 0, 6410784.3>,6.0 }
 cylinder { <1106095.81, 0, 6410784.3>,<1106078.73, 0, 6410809.48>,6.0 }
 sphere { <1106102.07, 0, 6410775.54>,6.0 }
 cylinder { <1106102.07, 0, 6410775.54>,<1106095.81, 0, 6410784.3>,6.0 }
 sphere { <1106146.46, 0, 6410722.05>,6.0 }
 cylinder { <1106146.46, 0, 6410722.05>,<1106102.07, 0, 6410775.54>,6.0 }
 sphere { <1106187.71, 0, 6410668.35>,6.0 }
 cylinder { <1106187.71, 0, 6410668.35>,<1106146.46, 0, 6410722.05>,6.0 }
 sphere { <1106208.38, 0, 6410659.14>,6.0 }
 cylinder { <1106208.38, 0, 6410659.14>,<1106187.71, 0, 6410668.35>,6.0 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=45401885 */
object {union { sphere { <1106097.74, 0, 6410899.32>,5.0 }
 sphere { <1106047.62, 0, 6410856.87>,5.0 }
 cylinder { <1106047.62, 0, 6410856.87>,<1106097.74, 0, 6410899.32>,5.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_secondary }
}

object {union { sphere { <1106097.74, 0, 6410899.32>,6.0 }
 sphere { <1106047.62, 0, 6410856.87>,6.0 }
 cylinder { <1106047.62, 0, 6410856.87>,<1106097.74, 0, 6410899.32>,6.0 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=45770167 */
object {union { sphere { <1106292.09, 0, 6411028.94>,5.0 }
 sphere { <1106181.73, 0, 6410960.11>,5.0 }
 cylinder { <1106181.73, 0, 6410960.11>,<1106292.09, 0, 6411028.94>,5.0 }
 sphere { <1106129.34, 0, 6410922.66>,5.0 }
 cylinder { <1106129.34, 0, 6410922.66>,<1106181.73, 0, 6410960.11>,5.0 }
 sphere { <1106097.74, 0, 6410899.32>,5.0 }
 cylinder { <1106097.74, 0, 6410899.32>,<1106129.34, 0, 6410922.66>,5.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_secondary }
}

object {union { sphere { <1106292.09, 0, 6411028.94>,6.0 }
 sphere { <1106181.73, 0, 6410960.11>,6.0 }
 cylinder { <1106181.73, 0, 6410960.11>,<1106292.09, 0, 6411028.94>,6.0 }
 sphere { <1106129.34, 0, 6410922.66>,6.0 }
 cylinder { <1106129.34, 0, 6410922.66>,<1106181.73, 0, 6410960.11>,6.0 }
 sphere { <1106097.74, 0, 6410899.32>,6.0 }
 cylinder { <1106097.74, 0, 6410899.32>,<1106129.34, 0, 6410922.66>,6.0 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=33775397 */
object {union { sphere { <1106100.32, 0, 6410416.39>,5.0 }
 sphere { <1106124.43, 0, 6410482.04>,5.0 }
 cylinder { <1106124.43, 0, 6410482.04>,<1106100.32, 0, 6410416.39>,5.0 }
 sphere { <1106135.89, 0, 6410511.98>,5.0 }
 cylinder { <1106135.89, 0, 6410511.98>,<1106124.43, 0, 6410482.04>,5.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_secondary }
}

object {union { sphere { <1106100.32, 0, 6410416.39>,6.0 }
 sphere { <1106124.43, 0, 6410482.04>,6.0 }
 cylinder { <1106124.43, 0, 6410482.04>,<1106100.32, 0, 6410416.39>,6.0 }
 sphere { <1106135.89, 0, 6410511.98>,6.0 }
 cylinder { <1106135.89, 0, 6410511.98>,<1106124.43, 0, 6410482.04>,6.0 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=30426298 */
object {union { sphere { <1106164.85, 0, 6409707.79>,5.0 }
 sphere { <1106145.55, 0, 6409741.93>,5.0 }
 cylinder { <1106145.55, 0, 6409741.93>,<1106164.85, 0, 6409707.79>,5.0 }
 sphere { <1106135.61, 0, 6409759.75>,5.0 }
 cylinder { <1106135.61, 0, 6409759.75>,<1106145.55, 0, 6409741.93>,5.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_secondary }
}

object {union { sphere { <1106164.85, 0, 6409707.79>,6.0 }
 sphere { <1106145.55, 0, 6409741.93>,6.0 }
 cylinder { <1106145.55, 0, 6409741.93>,<1106164.85, 0, 6409707.79>,6.0 }
 sphere { <1106135.61, 0, 6409759.75>,6.0 }
 cylinder { <1106135.61, 0, 6409759.75>,<1106145.55, 0, 6409741.93>,6.0 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=37651574 */
object {union { sphere { <1106135.89, 0, 6410511.98>,5.0 }
 sphere { <1106171.68, 0, 6410582.01>,5.0 }
 cylinder { <1106171.68, 0, 6410582.01>,<1106135.89, 0, 6410511.98>,5.0 }
 sphere { <1106182.32, 0, 6410603.6>,5.0 }
 cylinder { <1106182.32, 0, 6410603.6>,<1106171.68, 0, 6410582.01>,5.0 }
 sphere { <1106199.45, 0, 6410638.34>,5.0 }
 cylinder { <1106199.45, 0, 6410638.34>,<1106182.32, 0, 6410603.6>,5.0 }
 sphere { <1106208.38, 0, 6410659.14>,5.0 }
 cylinder { <1106208.38, 0, 6410659.14>,<1106199.45, 0, 6410638.34>,5.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_secondary }
}

object {union { sphere { <1106135.89, 0, 6410511.98>,6.0 }
 sphere { <1106171.68, 0, 6410582.01>,6.0 }
 cylinder { <1106171.68, 0, 6410582.01>,<1106135.89, 0, 6410511.98>,6.0 }
 sphere { <1106182.32, 0, 6410603.6>,6.0 }
 cylinder { <1106182.32, 0, 6410603.6>,<1106171.68, 0, 6410582.01>,6.0 }
 sphere { <1106199.45, 0, 6410638.34>,6.0 }
 cylinder { <1106199.45, 0, 6410638.34>,<1106182.32, 0, 6410603.6>,6.0 }
 sphere { <1106208.38, 0, 6410659.14>,6.0 }
 cylinder { <1106208.38, 0, 6410659.14>,<1106199.45, 0, 6410638.34>,6.0 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=30094855 */
object {union { sphere { <1106844.26, 0, 6410233.18>,5.0 }
 sphere { <1106818.54, 0, 6410209.83>,5.0 }
 cylinder { <1106818.54, 0, 6410209.83>,<1106844.26, 0, 6410233.18>,5.0 }
 sphere { <1106793.71, 0, 6410186.6>,5.0 }
 cylinder { <1106793.71, 0, 6410186.6>,<1106818.54, 0, 6410209.83>,5.0 }
 sphere { <1106771.26, 0, 6410165.19>,5.0 }
 cylinder { <1106771.26, 0, 6410165.19>,<1106793.71, 0, 6410186.6>,5.0 }
 sphere { <1106761.78, 0, 6410113.73>,5.0 }
 cylinder { <1106761.78, 0, 6410113.73>,<1106771.26, 0, 6410165.19>,5.0 }
 sphere { <1106752.83, 0, 6409990.14>,5.0 }
 cylinder { <1106752.83, 0, 6409990.14>,<1106761.78, 0, 6410113.73>,5.0 }
 sphere { <1106749.17, 0, 6409942.68>,5.0 }
 cylinder { <1106749.17, 0, 6409942.68>,<1106752.83, 0, 6409990.14>,5.0 }
 sphere { <1106748.44, 0, 6409926.94>,5.0 }
 cylinder { <1106748.44, 0, 6409926.94>,<1106749.17, 0, 6409942.68>,5.0 }
 sphere { <1106747.97, 0, 6409907.77>,5.0 }
 cylinder { <1106747.97, 0, 6409907.77>,<1106748.44, 0, 6409926.94>,5.0 }
 sphere { <1106739.99, 0, 6409887.16>,5.0 }
 cylinder { <1106739.99, 0, 6409887.16>,<1106747.97, 0, 6409907.77>,5.0 }
 sphere { <1106734.7, 0, 6409880.71>,5.0 }
 cylinder { <1106734.7, 0, 6409880.71>,<1106739.99, 0, 6409887.16>,5.0 }
 sphere { <1106624.62, 0, 6409843.81>,5.0 }
 cylinder { <1106624.62, 0, 6409843.81>,<1106734.7, 0, 6409880.71>,5.0 }
 sphere { <1106599.39, 0, 6409831.71>,5.0 }
 cylinder { <1106599.39, 0, 6409831.71>,<1106624.62, 0, 6409843.81>,5.0 }
 sphere { <1106509.61, 0, 6409788.88>,5.0 }
 cylinder { <1106509.61, 0, 6409788.88>,<1106599.39, 0, 6409831.71>,5.0 }
 sphere { <1106472.49, 0, 6409750.37>,5.0 }
 cylinder { <1106472.49, 0, 6409750.37>,<1106509.61, 0, 6409788.88>,5.0 }
 sphere { <1106414.11, 0, 6409677.59>,5.0 }
 cylinder { <1106414.11, 0, 6409677.59>,<1106472.49, 0, 6409750.37>,5.0 }
 sphere { <1106398.67, 0, 6409671.4>,5.0 }
 cylinder { <1106398.67, 0, 6409671.4>,<1106414.11, 0, 6409677.59>,5.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_secondary }
}

object {union { sphere { <1106844.26, 0, 6410233.18>,6.0 }
 sphere { <1106818.54, 0, 6410209.83>,6.0 }
 cylinder { <1106818.54, 0, 6410209.83>,<1106844.26, 0, 6410233.18>,6.0 }
 sphere { <1106793.71, 0, 6410186.6>,6.0 }
 cylinder { <1106793.71, 0, 6410186.6>,<1106818.54, 0, 6410209.83>,6.0 }
 sphere { <1106771.26, 0, 6410165.19>,6.0 }
 cylinder { <1106771.26, 0, 6410165.19>,<1106793.71, 0, 6410186.6>,6.0 }
 sphere { <1106761.78, 0, 6410113.73>,6.0 }
 cylinder { <1106761.78, 0, 6410113.73>,<1106771.26, 0, 6410165.19>,6.0 }
 sphere { <1106752.83, 0, 6409990.14>,6.0 }
 cylinder { <1106752.83, 0, 6409990.14>,<1106761.78, 0, 6410113.73>,6.0 }
 sphere { <1106749.17, 0, 6409942.68>,6.0 }
 cylinder { <1106749.17, 0, 6409942.68>,<1106752.83, 0, 6409990.14>,6.0 }
 sphere { <1106748.44, 0, 6409926.94>,6.0 }
 cylinder { <1106748.44, 0, 6409926.94>,<1106749.17, 0, 6409942.68>,6.0 }
 sphere { <1106747.97, 0, 6409907.77>,6.0 }
 cylinder { <1106747.97, 0, 6409907.77>,<1106748.44, 0, 6409926.94>,6.0 }
 sphere { <1106739.99, 0, 6409887.16>,6.0 }
 cylinder { <1106739.99, 0, 6409887.16>,<1106747.97, 0, 6409907.77>,6.0 }
 sphere { <1106734.7, 0, 6409880.71>,6.0 }
 cylinder { <1106734.7, 0, 6409880.71>,<1106739.99, 0, 6409887.16>,6.0 }
 sphere { <1106624.62, 0, 6409843.81>,6.0 }
 cylinder { <1106624.62, 0, 6409843.81>,<1106734.7, 0, 6409880.71>,6.0 }
 sphere { <1106599.39, 0, 6409831.71>,6.0 }
 cylinder { <1106599.39, 0, 6409831.71>,<1106624.62, 0, 6409843.81>,6.0 }
 sphere { <1106509.61, 0, 6409788.88>,6.0 }
 cylinder { <1106509.61, 0, 6409788.88>,<1106599.39, 0, 6409831.71>,6.0 }
 sphere { <1106472.49, 0, 6409750.37>,6.0 }
 cylinder { <1106472.49, 0, 6409750.37>,<1106509.61, 0, 6409788.88>,6.0 }
 sphere { <1106414.11, 0, 6409677.59>,6.0 }
 cylinder { <1106414.11, 0, 6409677.59>,<1106472.49, 0, 6409750.37>,6.0 }
 sphere { <1106398.67, 0, 6409671.4>,6.0 }
 cylinder { <1106398.67, 0, 6409671.4>,<1106414.11, 0, 6409677.59>,6.0 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=52454756 */
object {union { sphere { <1106481.04, 0, 6410596.22>,5.0 }
 sphere { <1106497.91, 0, 6410591.85>,5.0 }
 cylinder { <1106497.91, 0, 6410591.85>,<1106481.04, 0, 6410596.22>,5.0 }
 sphere { <1106505.63, 0, 6410588.78>,5.0 }
 cylinder { <1106505.63, 0, 6410588.78>,<1106497.91, 0, 6410591.85>,5.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_secondary }
}

object {union { sphere { <1106481.04, 0, 6410596.22>,6.0 }
 sphere { <1106497.91, 0, 6410591.85>,6.0 }
 cylinder { <1106497.91, 0, 6410591.85>,<1106481.04, 0, 6410596.22>,6.0 }
 sphere { <1106505.63, 0, 6410588.78>,6.0 }
 cylinder { <1106505.63, 0, 6410588.78>,<1106497.91, 0, 6410591.85>,6.0 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=4454268 */
object {union { sphere { <1106255.25, 0, 6409591.45>,5.0 }
 sphere { <1106236.56, 0, 6409602.12>,5.0 }
 cylinder { <1106236.56, 0, 6409602.12>,<1106255.25, 0, 6409591.45>,5.0 }
 sphere { <1106219.47, 0, 6409627.42>,5.0 }
 cylinder { <1106219.47, 0, 6409627.42>,<1106236.56, 0, 6409602.12>,5.0 }
 sphere { <1106193.19, 0, 6409665.01>,5.0 }
 cylinder { <1106193.19, 0, 6409665.01>,<1106219.47, 0, 6409627.42>,5.0 }
 sphere { <1106170.43, 0, 6409697.89>,5.0 }
 cylinder { <1106170.43, 0, 6409697.89>,<1106193.19, 0, 6409665.01>,5.0 }
 sphere { <1106164.85, 0, 6409707.79>,5.0 }
 cylinder { <1106164.85, 0, 6409707.79>,<1106170.43, 0, 6409697.89>,5.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_secondary }
}

object {union { sphere { <1106255.25, 0, 6409591.45>,6.0 }
 sphere { <1106236.56, 0, 6409602.12>,6.0 }
 cylinder { <1106236.56, 0, 6409602.12>,<1106255.25, 0, 6409591.45>,6.0 }
 sphere { <1106219.47, 0, 6409627.42>,6.0 }
 cylinder { <1106219.47, 0, 6409627.42>,<1106236.56, 0, 6409602.12>,6.0 }
 sphere { <1106193.19, 0, 6409665.01>,6.0 }
 cylinder { <1106193.19, 0, 6409665.01>,<1106219.47, 0, 6409627.42>,6.0 }
 sphere { <1106170.43, 0, 6409697.89>,6.0 }
 cylinder { <1106170.43, 0, 6409697.89>,<1106193.19, 0, 6409665.01>,6.0 }
 sphere { <1106164.85, 0, 6409707.79>,6.0 }
 cylinder { <1106164.85, 0, 6409707.79>,<1106170.43, 0, 6409697.89>,6.0 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=30426297 */
object {union { sphere { <1106505.63, 0, 6410588.78>,5.0 }
 sphere { <1106523.34, 0, 6410584.13>,5.0 }
 cylinder { <1106523.34, 0, 6410584.13>,<1106505.63, 0, 6410588.78>,5.0 }
 sphere { <1106526.72, 0, 6410583.01>,5.0 }
 cylinder { <1106526.72, 0, 6410583.01>,<1106523.34, 0, 6410584.13>,5.0 }
 sphere { <1106561.93, 0, 6410571.78>,5.0 }
 cylinder { <1106561.93, 0, 6410571.78>,<1106526.72, 0, 6410583.01>,5.0 }
 sphere { <1106591.49, 0, 6410562.81>,5.0 }
 cylinder { <1106591.49, 0, 6410562.81>,<1106561.93, 0, 6410571.78>,5.0 }
 sphere { <1106681.07, 0, 6410536.1>,5.0 }
 cylinder { <1106681.07, 0, 6410536.1>,<1106591.49, 0, 6410562.81>,5.0 }
 sphere { <1106714.84, 0, 6410525.93>,5.0 }
 cylinder { <1106714.84, 0, 6410525.93>,<1106681.07, 0, 6410536.1>,5.0 }
 sphere { <1106752.89, 0, 6410514.48>,5.0 }
 cylinder { <1106752.89, 0, 6410514.48>,<1106714.84, 0, 6410525.93>,5.0 }
 sphere { <1106775.29, 0, 6410507.94>,5.0 }
 cylinder { <1106775.29, 0, 6410507.94>,<1106752.89, 0, 6410514.48>,5.0 }
 sphere { <1106809.98, 0, 6410497.82>,5.0 }
 cylinder { <1106809.98, 0, 6410497.82>,<1106775.29, 0, 6410507.94>,5.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_secondary }
}

object {union { sphere { <1106505.63, 0, 6410588.78>,6.0 }
 sphere { <1106523.34, 0, 6410584.13>,6.0 }
 cylinder { <1106523.34, 0, 6410584.13>,<1106505.63, 0, 6410588.78>,6.0 }
 sphere { <1106526.72, 0, 6410583.01>,6.0 }
 cylinder { <1106526.72, 0, 6410583.01>,<1106523.34, 0, 6410584.13>,6.0 }
 sphere { <1106561.93, 0, 6410571.78>,6.0 }
 cylinder { <1106561.93, 0, 6410571.78>,<1106526.72, 0, 6410583.01>,6.0 }
 sphere { <1106591.49, 0, 6410562.81>,6.0 }
 cylinder { <1106591.49, 0, 6410562.81>,<1106561.93, 0, 6410571.78>,6.0 }
 sphere { <1106681.07, 0, 6410536.1>,6.0 }
 cylinder { <1106681.07, 0, 6410536.1>,<1106591.49, 0, 6410562.81>,6.0 }
 sphere { <1106714.84, 0, 6410525.93>,6.0 }
 cylinder { <1106714.84, 0, 6410525.93>,<1106681.07, 0, 6410536.1>,6.0 }
 sphere { <1106752.89, 0, 6410514.48>,6.0 }
 cylinder { <1106752.89, 0, 6410514.48>,<1106714.84, 0, 6410525.93>,6.0 }
 sphere { <1106775.29, 0, 6410507.94>,6.0 }
 cylinder { <1106775.29, 0, 6410507.94>,<1106752.89, 0, 6410514.48>,6.0 }
 sphere { <1106809.98, 0, 6410497.82>,6.0 }
 cylinder { <1106809.98, 0, 6410497.82>,<1106775.29, 0, 6410507.94>,6.0 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=4482138 */
object {union { sphere { <1106208.38, 0, 6410659.14>,5.0 }
 sphere { <1106277.7, 0, 6410641.62>,5.0 }
 cylinder { <1106277.7, 0, 6410641.62>,<1106208.38, 0, 6410659.14>,5.0 }
 sphere { <1106351.21, 0, 6410625.05>,5.0 }
 cylinder { <1106351.21, 0, 6410625.05>,<1106277.7, 0, 6410641.62>,5.0 }
 sphere { <1106354.54, 0, 6410624.41>,5.0 }
 cylinder { <1106354.54, 0, 6410624.41>,<1106351.21, 0, 6410625.05>,5.0 }
 sphere { <1106481.04, 0, 6410596.22>,5.0 }
 cylinder { <1106481.04, 0, 6410596.22>,<1106354.54, 0, 6410624.41>,5.0 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_secondary }
}

object {union { sphere { <1106208.38, 0, 6410659.14>,6.0 }
 sphere { <1106277.7, 0, 6410641.62>,6.0 }
 cylinder { <1106277.7, 0, 6410641.62>,<1106208.38, 0, 6410659.14>,6.0 }
 sphere { <1106351.21, 0, 6410625.05>,6.0 }
 cylinder { <1106351.21, 0, 6410625.05>,<1106277.7, 0, 6410641.62>,6.0 }
 sphere { <1106354.54, 0, 6410624.41>,6.0 }
 cylinder { <1106354.54, 0, 6410624.41>,<1106351.21, 0, 6410625.05>,6.0 }
 sphere { <1106481.04, 0, 6410596.22>,6.0 }
 cylinder { <1106481.04, 0, 6410596.22>,<1106354.54, 0, 6410624.41>,6.0 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=27153004 */
object {union { sphere { <1105567.67, 0, 6410587.44>,1.5 }
 sphere { <1105590.75, 0, 6410601.82>,1.5 }
 cylinder { <1105590.75, 0, 6410601.82>,<1105567.67, 0, 6410587.44>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1105567.67, 0, 6410587.44>,1.8 }
 sphere { <1105590.75, 0, 6410601.82>,1.8 }
 cylinder { <1105590.75, 0, 6410601.82>,<1105567.67, 0, 6410587.44>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=58563358 */
object {union { sphere { <1105640.5, 0, 6410684.71>,1.5 }
 sphere { <1105595.08, 0, 6410683.64>,1.5 }
 cylinder { <1105595.08, 0, 6410683.64>,<1105640.5, 0, 6410684.71>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1105640.5, 0, 6410684.71>,1.8 }
 sphere { <1105595.08, 0, 6410683.64>,1.8 }
 cylinder { <1105595.08, 0, 6410683.64>,<1105640.5, 0, 6410684.71>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=27152853 */
object {union { sphere { <1105670.84, 0, 6410770.56>,1.5 }
 sphere { <1105620.22, 0, 6410762.83>,1.5 }
 cylinder { <1105620.22, 0, 6410762.83>,<1105670.84, 0, 6410770.56>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1105670.84, 0, 6410770.56>,1.8 }
 sphere { <1105620.22, 0, 6410762.83>,1.8 }
 cylinder { <1105620.22, 0, 6410762.83>,<1105670.84, 0, 6410770.56>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=12710865 */
object {union { sphere { <1105707.16, 0, 6410773.85>,1.5 }
 sphere { <1105670.84, 0, 6410770.56>,1.5 }
 cylinder { <1105670.84, 0, 6410770.56>,<1105707.16, 0, 6410773.85>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1105707.16, 0, 6410773.85>,1.8 }
 sphere { <1105670.84, 0, 6410770.56>,1.8 }
 cylinder { <1105670.84, 0, 6410770.56>,<1105707.16, 0, 6410773.85>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=12710992 */
object {union { sphere { <1105865.68, 0, 6410772.64>,1.5 }
 sphere { <1105874.73, 0, 6410726.05>,1.5 }
 cylinder { <1105874.73, 0, 6410726.05>,<1105865.68, 0, 6410772.64>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1105865.68, 0, 6410772.64>,1.8 }
 sphere { <1105874.73, 0, 6410726.05>,1.8 }
 cylinder { <1105874.73, 0, 6410726.05>,<1105865.68, 0, 6410772.64>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10152028 */
object {union { sphere { <1105868.11, 0, 6409871.61>,1.5 }
 sphere { <1105900.31, 0, 6409877.33>,1.5 }
 cylinder { <1105900.31, 0, 6409877.33>,<1105868.11, 0, 6409871.61>,1.5 }
 sphere { <1105930, 0, 6409871.23>,1.5 }
 cylinder { <1105930, 0, 6409871.23>,<1105900.31, 0, 6409877.33>,1.5 }
 sphere { <1105947.49, 0, 6409866.35>,1.5 }
 cylinder { <1105947.49, 0, 6409866.35>,<1105930, 0, 6409871.23>,1.5 }
 sphere { <1105981.24, 0, 6409844.38>,1.5 }
 cylinder { <1105981.24, 0, 6409844.38>,<1105947.49, 0, 6409866.35>,1.5 }
 sphere { <1106025.2, 0, 6409809.57>,1.5 }
 cylinder { <1106025.2, 0, 6409809.57>,<1105981.24, 0, 6409844.38>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1105868.11, 0, 6409871.61>,1.8 }
 sphere { <1105900.31, 0, 6409877.33>,1.8 }
 cylinder { <1105900.31, 0, 6409877.33>,<1105868.11, 0, 6409871.61>,1.8 }
 sphere { <1105930, 0, 6409871.23>,1.8 }
 cylinder { <1105930, 0, 6409871.23>,<1105900.31, 0, 6409877.33>,1.8 }
 sphere { <1105947.49, 0, 6409866.35>,1.8 }
 cylinder { <1105947.49, 0, 6409866.35>,<1105930, 0, 6409871.23>,1.8 }
 sphere { <1105981.24, 0, 6409844.38>,1.8 }
 cylinder { <1105981.24, 0, 6409844.38>,<1105947.49, 0, 6409866.35>,1.8 }
 sphere { <1106025.2, 0, 6409809.57>,1.8 }
 cylinder { <1106025.2, 0, 6409809.57>,<1105981.24, 0, 6409844.38>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=32297656 */
object {union { sphere { <1105921.61, 0, 6409976.25>,1.5 }
 sphere { <1105921.88, 0, 6409967.15>,1.5 }
 cylinder { <1105921.88, 0, 6409967.15>,<1105921.61, 0, 6409976.25>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1105921.61, 0, 6409976.25>,1.8 }
 sphere { <1105921.88, 0, 6409967.15>,1.8 }
 cylinder { <1105921.88, 0, 6409967.15>,<1105921.61, 0, 6409976.25>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10152036 */
object {union { sphere { <1105947.49, 0, 6409866.35>,1.5 }
 sphere { <1105950.35, 0, 6409880.11>,1.5 }
 cylinder { <1105950.35, 0, 6409880.11>,<1105947.49, 0, 6409866.35>,1.5 }
 sphere { <1106005.27, 0, 6409881.3>,1.5 }
 cylinder { <1106005.27, 0, 6409881.3>,<1105950.35, 0, 6409880.11>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1105947.49, 0, 6409866.35>,1.8 }
 sphere { <1105950.35, 0, 6409880.11>,1.8 }
 cylinder { <1105950.35, 0, 6409880.11>,<1105947.49, 0, 6409866.35>,1.8 }
 sphere { <1106005.27, 0, 6409881.3>,1.8 }
 cylinder { <1106005.27, 0, 6409881.3>,<1105950.35, 0, 6409880.11>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=46188835 */
object {union { sphere { <1106037.85, 0, 6410053.3>,1.5 }
 sphere { <1106018.38, 0, 6410085.06>,1.5 }
 cylinder { <1106018.38, 0, 6410085.06>,<1106037.85, 0, 6410053.3>,1.5 }
 sphere { <1106008.8, 0, 6410105.77>,1.5 }
 cylinder { <1106008.8, 0, 6410105.77>,<1106018.38, 0, 6410085.06>,1.5 }
 sphere { <1106027.71, 0, 6410164.36>,1.5 }
 cylinder { <1106027.71, 0, 6410164.36>,<1106008.8, 0, 6410105.77>,1.5 }
 sphere { <1106055.68, 0, 6410239.42>,1.5 }
 cylinder { <1106055.68, 0, 6410239.42>,<1106027.71, 0, 6410164.36>,1.5 }
 sphere { <1106093.91, 0, 6410341.97>,1.5 }
 cylinder { <1106093.91, 0, 6410341.97>,<1106055.68, 0, 6410239.42>,1.5 }
 sphere { <1106115.41, 0, 6410411.48>,1.5 }
 cylinder { <1106115.41, 0, 6410411.48>,<1106093.91, 0, 6410341.97>,1.5 }
 sphere { <1106145.61, 0, 6410509.18>,1.5 }
 cylinder { <1106145.61, 0, 6410509.18>,<1106115.41, 0, 6410411.48>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106037.85, 0, 6410053.3>,1.8 }
 sphere { <1106018.38, 0, 6410085.06>,1.8 }
 cylinder { <1106018.38, 0, 6410085.06>,<1106037.85, 0, 6410053.3>,1.8 }
 sphere { <1106008.8, 0, 6410105.77>,1.8 }
 cylinder { <1106008.8, 0, 6410105.77>,<1106018.38, 0, 6410085.06>,1.8 }
 sphere { <1106027.71, 0, 6410164.36>,1.8 }
 cylinder { <1106027.71, 0, 6410164.36>,<1106008.8, 0, 6410105.77>,1.8 }
 sphere { <1106055.68, 0, 6410239.42>,1.8 }
 cylinder { <1106055.68, 0, 6410239.42>,<1106027.71, 0, 6410164.36>,1.8 }
 sphere { <1106093.91, 0, 6410341.97>,1.8 }
 cylinder { <1106093.91, 0, 6410341.97>,<1106055.68, 0, 6410239.42>,1.8 }
 sphere { <1106115.41, 0, 6410411.48>,1.8 }
 cylinder { <1106115.41, 0, 6410411.48>,<1106093.91, 0, 6410341.97>,1.8 }
 sphere { <1106145.61, 0, 6410509.18>,1.8 }
 cylinder { <1106145.61, 0, 6410509.18>,<1106115.41, 0, 6410411.48>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=32295317 */
object {union { sphere { <1106026.78, 0, 6410731.74>,1.5 }
 sphere { <1106092.59, 0, 6410781.84>,1.5 }
 cylinder { <1106092.59, 0, 6410781.84>,<1106026.78, 0, 6410731.74>,1.5 }
 sphere { <1106095.81, 0, 6410784.3>,1.5 }
 cylinder { <1106095.81, 0, 6410784.3>,<1106092.59, 0, 6410781.84>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106026.78, 0, 6410731.74>,1.8 }
 sphere { <1106092.59, 0, 6410781.84>,1.8 }
 cylinder { <1106092.59, 0, 6410781.84>,<1106026.78, 0, 6410731.74>,1.8 }
 sphere { <1106095.81, 0, 6410784.3>,1.8 }
 cylinder { <1106095.81, 0, 6410784.3>,<1106092.59, 0, 6410781.84>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=7836916 */
object {union { sphere { <1106184.76, 0, 6410125.27>,1.5 }
 sphere { <1106116.25, 0, 6410137.81>,1.5 }
 cylinder { <1106116.25, 0, 6410137.81>,<1106184.76, 0, 6410125.27>,1.5 }
 sphere { <1106063.84, 0, 6410125.48>,1.5 }
 cylinder { <1106063.84, 0, 6410125.48>,<1106116.25, 0, 6410137.81>,1.5 }
 sphere { <1106040.6, 0, 6410055.21>,1.5 }
 cylinder { <1106040.6, 0, 6410055.21>,<1106063.84, 0, 6410125.48>,1.5 }
 sphere { <1106037.85, 0, 6410053.3>,1.5 }
 cylinder { <1106037.85, 0, 6410053.3>,<1106040.6, 0, 6410055.21>,1.5 }
 sphere { <1106035.21, 0, 6410051.45>,1.5 }
 cylinder { <1106035.21, 0, 6410051.45>,<1106037.85, 0, 6410053.3>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106184.76, 0, 6410125.27>,1.8 }
 sphere { <1106116.25, 0, 6410137.81>,1.8 }
 cylinder { <1106116.25, 0, 6410137.81>,<1106184.76, 0, 6410125.27>,1.8 }
 sphere { <1106063.84, 0, 6410125.48>,1.8 }
 cylinder { <1106063.84, 0, 6410125.48>,<1106116.25, 0, 6410137.81>,1.8 }
 sphere { <1106040.6, 0, 6410055.21>,1.8 }
 cylinder { <1106040.6, 0, 6410055.21>,<1106063.84, 0, 6410125.48>,1.8 }
 sphere { <1106037.85, 0, 6410053.3>,1.8 }
 cylinder { <1106037.85, 0, 6410053.3>,<1106040.6, 0, 6410055.21>,1.8 }
 sphere { <1106035.21, 0, 6410051.45>,1.8 }
 cylinder { <1106035.21, 0, 6410051.45>,<1106037.85, 0, 6410053.3>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=46188837 */
object {union { sphere { <1106055.68, 0, 6410239.42>,1.5 }
 sphere { <1106069.19, 0, 6410237.4>,1.5 }
 cylinder { <1106069.19, 0, 6410237.4>,<1106055.68, 0, 6410239.42>,1.5 }
 sphere { <1106093.14, 0, 6410244.23>,1.5 }
 cylinder { <1106093.14, 0, 6410244.23>,<1106069.19, 0, 6410237.4>,1.5 }
 sphere { <1106107.01, 0, 6410254.01>,1.5 }
 cylinder { <1106107.01, 0, 6410254.01>,<1106093.14, 0, 6410244.23>,1.5 }
 sphere { <1106123.41, 0, 6410268.17>,1.5 }
 cylinder { <1106123.41, 0, 6410268.17>,<1106107.01, 0, 6410254.01>,1.5 }
 sphere { <1106139.17, 0, 6410269.63>,1.5 }
 cylinder { <1106139.17, 0, 6410269.63>,<1106123.41, 0, 6410268.17>,1.5 }
 sphere { <1106187.08, 0, 6410257.42>,1.5 }
 cylinder { <1106187.08, 0, 6410257.42>,<1106139.17, 0, 6410269.63>,1.5 }
 sphere { <1106228.04, 0, 6410250.6>,1.5 }
 cylinder { <1106228.04, 0, 6410250.6>,<1106187.08, 0, 6410257.42>,1.5 }
 sphere { <1106253.59, 0, 6410249.13>,1.5 }
 cylinder { <1106253.59, 0, 6410249.13>,<1106228.04, 0, 6410250.6>,1.5 }
 sphere { <1106257.37, 0, 6410247.16>,1.5 }
 cylinder { <1106257.37, 0, 6410247.16>,<1106253.59, 0, 6410249.13>,1.5 }
 sphere { <1106290.49, 0, 6410235.04>,1.5 }
 cylinder { <1106290.49, 0, 6410235.04>,<1106257.37, 0, 6410247.16>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106055.68, 0, 6410239.42>,1.8 }
 sphere { <1106069.19, 0, 6410237.4>,1.8 }
 cylinder { <1106069.19, 0, 6410237.4>,<1106055.68, 0, 6410239.42>,1.8 }
 sphere { <1106093.14, 0, 6410244.23>,1.8 }
 cylinder { <1106093.14, 0, 6410244.23>,<1106069.19, 0, 6410237.4>,1.8 }
 sphere { <1106107.01, 0, 6410254.01>,1.8 }
 cylinder { <1106107.01, 0, 6410254.01>,<1106093.14, 0, 6410244.23>,1.8 }
 sphere { <1106123.41, 0, 6410268.17>,1.8 }
 cylinder { <1106123.41, 0, 6410268.17>,<1106107.01, 0, 6410254.01>,1.8 }
 sphere { <1106139.17, 0, 6410269.63>,1.8 }
 cylinder { <1106139.17, 0, 6410269.63>,<1106123.41, 0, 6410268.17>,1.8 }
 sphere { <1106187.08, 0, 6410257.42>,1.8 }
 cylinder { <1106187.08, 0, 6410257.42>,<1106139.17, 0, 6410269.63>,1.8 }
 sphere { <1106228.04, 0, 6410250.6>,1.8 }
 cylinder { <1106228.04, 0, 6410250.6>,<1106187.08, 0, 6410257.42>,1.8 }
 sphere { <1106253.59, 0, 6410249.13>,1.8 }
 cylinder { <1106253.59, 0, 6410249.13>,<1106228.04, 0, 6410250.6>,1.8 }
 sphere { <1106257.37, 0, 6410247.16>,1.8 }
 cylinder { <1106257.37, 0, 6410247.16>,<1106253.59, 0, 6410249.13>,1.8 }
 sphere { <1106290.49, 0, 6410235.04>,1.8 }
 cylinder { <1106290.49, 0, 6410235.04>,<1106257.37, 0, 6410247.16>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=23012280 */
object {union { sphere { <1106084.86, 0, 6409614.07>,1.5 }
 sphere { <1106170.43, 0, 6409697.89>,1.5 }
 cylinder { <1106170.43, 0, 6409697.89>,<1106084.86, 0, 6409614.07>,1.5 }
 sphere { <1106209.62, 0, 6409721.25>,1.5 }
 cylinder { <1106209.62, 0, 6409721.25>,<1106170.43, 0, 6409697.89>,1.5 }
 sphere { <1106348.05, 0, 6409792.64>,1.5 }
 cylinder { <1106348.05, 0, 6409792.64>,<1106209.62, 0, 6409721.25>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106084.86, 0, 6409614.07>,1.8 }
 sphere { <1106170.43, 0, 6409697.89>,1.8 }
 cylinder { <1106170.43, 0, 6409697.89>,<1106084.86, 0, 6409614.07>,1.8 }
 sphere { <1106209.62, 0, 6409721.25>,1.8 }
 cylinder { <1106209.62, 0, 6409721.25>,<1106170.43, 0, 6409697.89>,1.8 }
 sphere { <1106348.05, 0, 6409792.64>,1.8 }
 cylinder { <1106348.05, 0, 6409792.64>,<1106209.62, 0, 6409721.25>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=46188836 */
object {union { sphere { <1106145.61, 0, 6410509.18>,1.5 }
 sphere { <1106212.14, 0, 6410643.33>,1.5 }
 cylinder { <1106212.14, 0, 6410643.33>,<1106145.61, 0, 6410509.18>,1.5 }
 sphere { <1106200.04, 0, 6410653.02>,1.5 }
 cylinder { <1106200.04, 0, 6410653.02>,<1106212.14, 0, 6410643.33>,1.5 }
 sphere { <1106184.36, 0, 6410663.95>,1.5 }
 cylinder { <1106184.36, 0, 6410663.95>,<1106200.04, 0, 6410653.02>,1.5 }
 sphere { <1106092.59, 0, 6410781.84>,1.5 }
 cylinder { <1106092.59, 0, 6410781.84>,<1106184.36, 0, 6410663.95>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106145.61, 0, 6410509.18>,1.8 }
 sphere { <1106212.14, 0, 6410643.33>,1.8 }
 cylinder { <1106212.14, 0, 6410643.33>,<1106145.61, 0, 6410509.18>,1.8 }
 sphere { <1106200.04, 0, 6410653.02>,1.8 }
 cylinder { <1106200.04, 0, 6410653.02>,<1106212.14, 0, 6410643.33>,1.8 }
 sphere { <1106184.36, 0, 6410663.95>,1.8 }
 cylinder { <1106184.36, 0, 6410663.95>,<1106200.04, 0, 6410653.02>,1.8 }
 sphere { <1106092.59, 0, 6410781.84>,1.8 }
 cylinder { <1106092.59, 0, 6410781.84>,<1106184.36, 0, 6410663.95>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=7835347 */
object {union { sphere { <1106341.66, 0, 6410039.3>,1.5 }
 sphere { <1106164.01, 0, 6410103.06>,1.5 }
 cylinder { <1106164.01, 0, 6410103.06>,<1106341.66, 0, 6410039.3>,1.5 }
 sphere { <1106123.87, 0, 6410103.65>,1.5 }
 cylinder { <1106123.87, 0, 6410103.65>,<1106164.01, 0, 6410103.06>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106341.66, 0, 6410039.3>,1.8 }
 sphere { <1106164.01, 0, 6410103.06>,1.8 }
 cylinder { <1106164.01, 0, 6410103.06>,<1106341.66, 0, 6410039.3>,1.8 }
 sphere { <1106123.87, 0, 6410103.65>,1.8 }
 cylinder { <1106123.87, 0, 6410103.65>,<1106164.01, 0, 6410103.06>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=46188838 */
object {union { sphere { <1106276.36, 0, 6410497.22>,1.5 }
 sphere { <1106166.03, 0, 6410518.93>,1.5 }
 cylinder { <1106166.03, 0, 6410518.93>,<1106276.36, 0, 6410497.22>,1.5 }
 sphere { <1106148.15, 0, 6410507.91>,1.5 }
 cylinder { <1106148.15, 0, 6410507.91>,<1106166.03, 0, 6410518.93>,1.5 }
 sphere { <1106145.61, 0, 6410509.18>,1.5 }
 cylinder { <1106145.61, 0, 6410509.18>,<1106148.15, 0, 6410507.91>,1.5 }
 sphere { <1106135.89, 0, 6410511.98>,1.5 }
 cylinder { <1106135.89, 0, 6410511.98>,<1106145.61, 0, 6410509.18>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106276.36, 0, 6410497.22>,1.8 }
 sphere { <1106166.03, 0, 6410518.93>,1.8 }
 cylinder { <1106166.03, 0, 6410518.93>,<1106276.36, 0, 6410497.22>,1.8 }
 sphere { <1106148.15, 0, 6410507.91>,1.8 }
 cylinder { <1106148.15, 0, 6410507.91>,<1106166.03, 0, 6410518.93>,1.8 }
 sphere { <1106145.61, 0, 6410509.18>,1.8 }
 cylinder { <1106145.61, 0, 6410509.18>,<1106148.15, 0, 6410507.91>,1.8 }
 sphere { <1106135.89, 0, 6410511.98>,1.8 }
 cylinder { <1106135.89, 0, 6410511.98>,<1106145.61, 0, 6410509.18>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=23246548 */
object {union { sphere { <1106350.59, 0, 6410070.4>,1.5 }
 sphere { <1106184.76, 0, 6410125.27>,1.5 }
 cylinder { <1106184.76, 0, 6410125.27>,<1106350.59, 0, 6410070.4>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106350.59, 0, 6410070.4>,1.8 }
 sphere { <1106184.76, 0, 6410125.27>,1.8 }
 cylinder { <1106184.76, 0, 6410125.27>,<1106350.59, 0, 6410070.4>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=23246551 */
object {union { sphere { <1106184.76, 0, 6410125.27>,1.5 }
 sphere { <1106190.8, 0, 6410141.58>,1.5 }
 cylinder { <1106190.8, 0, 6410141.58>,<1106184.76, 0, 6410125.27>,1.5 }
 sphere { <1106228.04, 0, 6410250.6>,1.5 }
 cylinder { <1106228.04, 0, 6410250.6>,<1106190.8, 0, 6410141.58>,1.5 }
 sphere { <1106257.8, 0, 6410337.73>,1.5 }
 cylinder { <1106257.8, 0, 6410337.73>,<1106228.04, 0, 6410250.6>,1.5 }
 sphere { <1106270.36, 0, 6410333.4>,1.5 }
 cylinder { <1106270.36, 0, 6410333.4>,<1106257.8, 0, 6410337.73>,1.5 }
 sphere { <1106279.52, 0, 6410361.13>,1.5 }
 cylinder { <1106279.52, 0, 6410361.13>,<1106270.36, 0, 6410333.4>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106184.76, 0, 6410125.27>,1.8 }
 sphere { <1106190.8, 0, 6410141.58>,1.8 }
 cylinder { <1106190.8, 0, 6410141.58>,<1106184.76, 0, 6410125.27>,1.8 }
 sphere { <1106228.04, 0, 6410250.6>,1.8 }
 cylinder { <1106228.04, 0, 6410250.6>,<1106190.8, 0, 6410141.58>,1.8 }
 sphere { <1106257.8, 0, 6410337.73>,1.8 }
 cylinder { <1106257.8, 0, 6410337.73>,<1106228.04, 0, 6410250.6>,1.8 }
 sphere { <1106270.36, 0, 6410333.4>,1.8 }
 cylinder { <1106270.36, 0, 6410333.4>,<1106257.8, 0, 6410337.73>,1.8 }
 sphere { <1106279.52, 0, 6410361.13>,1.8 }
 cylinder { <1106279.52, 0, 6410361.13>,<1106270.36, 0, 6410333.4>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=28408948 */
object {union { sphere { <1106280.63, 0, 6410201.42>,1.5 }
 sphere { <1106190.8, 0, 6410141.58>,1.5 }
 cylinder { <1106190.8, 0, 6410141.58>,<1106280.63, 0, 6410201.42>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106280.63, 0, 6410201.42>,1.8 }
 sphere { <1106190.8, 0, 6410141.58>,1.8 }
 cylinder { <1106190.8, 0, 6410141.58>,<1106280.63, 0, 6410201.42>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10202705 */
object {union { sphere { <1106193.19, 0, 6409665.01>,1.5 }
 sphere { <1106246.25, 0, 6409706.96>,1.5 }
 cylinder { <1106246.25, 0, 6409706.96>,<1106193.19, 0, 6409665.01>,1.5 }
 sphere { <1106270.85, 0, 6409719.53>,1.5 }
 cylinder { <1106270.85, 0, 6409719.53>,<1106246.25, 0, 6409706.96>,1.5 }
 sphere { <1106297.94, 0, 6409731.27>,1.5 }
 cylinder { <1106297.94, 0, 6409731.27>,<1106270.85, 0, 6409719.53>,1.5 }
 sphere { <1106320.42, 0, 6409731.27>,1.5 }
 cylinder { <1106320.42, 0, 6409731.27>,<1106297.94, 0, 6409731.27>,1.5 }
 sphere { <1106343.93, 0, 6409731.27>,1.5 }
 cylinder { <1106343.93, 0, 6409731.27>,<1106320.42, 0, 6409731.27>,1.5 }
 sphere { <1106361.3, 0, 6409738.94>,1.5 }
 cylinder { <1106361.3, 0, 6409738.94>,<1106343.93, 0, 6409731.27>,1.5 }
 sphere { <1106396.05, 0, 6409784.93>,1.5 }
 cylinder { <1106396.05, 0, 6409784.93>,<1106361.3, 0, 6409738.94>,1.5 }
 sphere { <1106418.48, 0, 6409803.6>,1.5 }
 cylinder { <1106418.48, 0, 6409803.6>,<1106396.05, 0, 6409784.93>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106193.19, 0, 6409665.01>,1.8 }
 sphere { <1106246.25, 0, 6409706.96>,1.8 }
 cylinder { <1106246.25, 0, 6409706.96>,<1106193.19, 0, 6409665.01>,1.8 }
 sphere { <1106270.85, 0, 6409719.53>,1.8 }
 cylinder { <1106270.85, 0, 6409719.53>,<1106246.25, 0, 6409706.96>,1.8 }
 sphere { <1106297.94, 0, 6409731.27>,1.8 }
 cylinder { <1106297.94, 0, 6409731.27>,<1106270.85, 0, 6409719.53>,1.8 }
 sphere { <1106320.42, 0, 6409731.27>,1.8 }
 cylinder { <1106320.42, 0, 6409731.27>,<1106297.94, 0, 6409731.27>,1.8 }
 sphere { <1106343.93, 0, 6409731.27>,1.8 }
 cylinder { <1106343.93, 0, 6409731.27>,<1106320.42, 0, 6409731.27>,1.8 }
 sphere { <1106361.3, 0, 6409738.94>,1.8 }
 cylinder { <1106361.3, 0, 6409738.94>,<1106343.93, 0, 6409731.27>,1.8 }
 sphere { <1106396.05, 0, 6409784.93>,1.8 }
 cylinder { <1106396.05, 0, 6409784.93>,<1106361.3, 0, 6409738.94>,1.8 }
 sphere { <1106418.48, 0, 6409803.6>,1.8 }
 cylinder { <1106418.48, 0, 6409803.6>,<1106396.05, 0, 6409784.93>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10202692 */
object {union { sphere { <1106414.11, 0, 6409677.59>,1.5 }
 sphere { <1106374.86, 0, 6409699.54>,1.5 }
 cylinder { <1106374.86, 0, 6409699.54>,<1106414.11, 0, 6409677.59>,1.5 }
 sphere { <1106347.24, 0, 6409689.23>,1.5 }
 cylinder { <1106347.24, 0, 6409689.23>,<1106374.86, 0, 6409699.54>,1.5 }
 sphere { <1106293.24, 0, 6409705.73>,1.5 }
 cylinder { <1106293.24, 0, 6409705.73>,<1106347.24, 0, 6409689.23>,1.5 }
 sphere { <1106246.25, 0, 6409706.96>,1.5 }
 cylinder { <1106246.25, 0, 6409706.96>,<1106293.24, 0, 6409705.73>,1.5 }
 sphere { <1106209.62, 0, 6409721.25>,1.5 }
 cylinder { <1106209.62, 0, 6409721.25>,<1106246.25, 0, 6409706.96>,1.5 }
 sphere { <1106197.62, 0, 6409726.34>,1.5 }
 cylinder { <1106197.62, 0, 6409726.34>,<1106209.62, 0, 6409721.25>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106414.11, 0, 6409677.59>,1.8 }
 sphere { <1106374.86, 0, 6409699.54>,1.8 }
 cylinder { <1106374.86, 0, 6409699.54>,<1106414.11, 0, 6409677.59>,1.8 }
 sphere { <1106347.24, 0, 6409689.23>,1.8 }
 cylinder { <1106347.24, 0, 6409689.23>,<1106374.86, 0, 6409699.54>,1.8 }
 sphere { <1106293.24, 0, 6409705.73>,1.8 }
 cylinder { <1106293.24, 0, 6409705.73>,<1106347.24, 0, 6409689.23>,1.8 }
 sphere { <1106246.25, 0, 6409706.96>,1.8 }
 cylinder { <1106246.25, 0, 6409706.96>,<1106293.24, 0, 6409705.73>,1.8 }
 sphere { <1106209.62, 0, 6409721.25>,1.8 }
 cylinder { <1106209.62, 0, 6409721.25>,<1106246.25, 0, 6409706.96>,1.8 }
 sphere { <1106197.62, 0, 6409726.34>,1.8 }
 cylinder { <1106197.62, 0, 6409726.34>,<1106209.62, 0, 6409721.25>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=17978474 */
object {union { sphere { <1106241.71, 0, 6410372.97>,1.5 }
 sphere { <1106245.26, 0, 6410371.97>,1.5 }
 cylinder { <1106245.26, 0, 6410371.97>,<1106241.71, 0, 6410372.97>,1.5 }
 sphere { <1106264.43, 0, 6410365.84>,1.5 }
 cylinder { <1106264.43, 0, 6410365.84>,<1106245.26, 0, 6410371.97>,1.5 }
 sphere { <1106270.24, 0, 6410364.09>,1.5 }
 cylinder { <1106270.24, 0, 6410364.09>,<1106264.43, 0, 6410365.84>,1.5 }
 sphere { <1106279.52, 0, 6410361.13>,1.5 }
 cylinder { <1106279.52, 0, 6410361.13>,<1106270.24, 0, 6410364.09>,1.5 }
 sphere { <1106424.91, 0, 6410319.02>,1.5 }
 cylinder { <1106424.91, 0, 6410319.02>,<1106279.52, 0, 6410361.13>,1.5 }
 sphere { <1106318.56, 0, 6410229.45>,1.5 }
 cylinder { <1106318.56, 0, 6410229.45>,<1106424.91, 0, 6410319.02>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106241.71, 0, 6410372.97>,1.8 }
 sphere { <1106245.26, 0, 6410371.97>,1.8 }
 cylinder { <1106245.26, 0, 6410371.97>,<1106241.71, 0, 6410372.97>,1.8 }
 sphere { <1106264.43, 0, 6410365.84>,1.8 }
 cylinder { <1106264.43, 0, 6410365.84>,<1106245.26, 0, 6410371.97>,1.8 }
 sphere { <1106270.24, 0, 6410364.09>,1.8 }
 cylinder { <1106270.24, 0, 6410364.09>,<1106264.43, 0, 6410365.84>,1.8 }
 sphere { <1106279.52, 0, 6410361.13>,1.8 }
 cylinder { <1106279.52, 0, 6410361.13>,<1106270.24, 0, 6410364.09>,1.8 }
 sphere { <1106424.91, 0, 6410319.02>,1.8 }
 cylinder { <1106424.91, 0, 6410319.02>,<1106279.52, 0, 6410361.13>,1.8 }
 sphere { <1106318.56, 0, 6410229.45>,1.8 }
 cylinder { <1106318.56, 0, 6410229.45>,<1106424.91, 0, 6410319.02>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59005233 */
object {union { sphere { <1106264.43, 0, 6410365.84>,1.5 }
 sphere { <1106270.27, 0, 6410382.6>,1.5 }
 cylinder { <1106270.27, 0, 6410382.6>,<1106264.43, 0, 6410365.84>,1.5 }
 sphere { <1106278.43, 0, 6410409.95>,1.5 }
 cylinder { <1106278.43, 0, 6410409.95>,<1106270.27, 0, 6410382.6>,1.5 }
 sphere { <1106294.05, 0, 6410461.14>,1.5 }
 cylinder { <1106294.05, 0, 6410461.14>,<1106278.43, 0, 6410409.95>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106264.43, 0, 6410365.84>,1.8 }
 sphere { <1106270.27, 0, 6410382.6>,1.8 }
 cylinder { <1106270.27, 0, 6410382.6>,<1106264.43, 0, 6410365.84>,1.8 }
 sphere { <1106278.43, 0, 6410409.95>,1.8 }
 cylinder { <1106278.43, 0, 6410409.95>,<1106270.27, 0, 6410382.6>,1.8 }
 sphere { <1106294.05, 0, 6410461.14>,1.8 }
 cylinder { <1106294.05, 0, 6410461.14>,<1106278.43, 0, 6410409.95>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=28408947 */
object {union { sphere { <1106290.49, 0, 6410235.04>,1.5 }
 sphere { <1106270.36, 0, 6410333.4>,1.5 }
 cylinder { <1106270.36, 0, 6410333.4>,<1106290.49, 0, 6410235.04>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106290.49, 0, 6410235.04>,1.8 }
 sphere { <1106270.36, 0, 6410333.4>,1.8 }
 cylinder { <1106270.36, 0, 6410333.4>,<1106290.49, 0, 6410235.04>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59007428 */
object {union { sphere { <1106274.79, 0, 6410380.89>,1.5 }
 sphere { <1106270.27, 0, 6410382.6>,1.5 }
 cylinder { <1106270.27, 0, 6410382.6>,<1106274.79, 0, 6410380.89>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106274.79, 0, 6410380.89>,1.8 }
 sphere { <1106270.27, 0, 6410382.6>,1.8 }
 cylinder { <1106270.27, 0, 6410382.6>,<1106274.79, 0, 6410380.89>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59007430 */
object {union { sphere { <1106282.35, 0, 6410516.31>,1.5 }
 sphere { <1106296.87, 0, 6410520.6>,1.5 }
 cylinder { <1106296.87, 0, 6410520.6>,<1106282.35, 0, 6410516.31>,1.5 }
 sphere { <1106309.78, 0, 6410512.79>,1.5 }
 cylinder { <1106309.78, 0, 6410512.79>,<1106296.87, 0, 6410520.6>,1.5 }
 sphere { <1106312.68, 0, 6410497.68>,1.5 }
 cylinder { <1106312.68, 0, 6410497.68>,<1106309.78, 0, 6410512.79>,1.5 }
 sphere { <1106300.54, 0, 6410484.32>,1.5 }
 cylinder { <1106300.54, 0, 6410484.32>,<1106312.68, 0, 6410497.68>,1.5 }
 sphere { <1106285.71, 0, 6410485.46>,1.5 }
 cylinder { <1106285.71, 0, 6410485.46>,<1106300.54, 0, 6410484.32>,1.5 }
 sphere { <1106276.36, 0, 6410497.22>,1.5 }
 cylinder { <1106276.36, 0, 6410497.22>,<1106285.71, 0, 6410485.46>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106282.35, 0, 6410516.31>,1.8 }
 sphere { <1106296.87, 0, 6410520.6>,1.8 }
 cylinder { <1106296.87, 0, 6410520.6>,<1106282.35, 0, 6410516.31>,1.8 }
 sphere { <1106309.78, 0, 6410512.79>,1.8 }
 cylinder { <1106309.78, 0, 6410512.79>,<1106296.87, 0, 6410520.6>,1.8 }
 sphere { <1106312.68, 0, 6410497.68>,1.8 }
 cylinder { <1106312.68, 0, 6410497.68>,<1106309.78, 0, 6410512.79>,1.8 }
 sphere { <1106300.54, 0, 6410484.32>,1.8 }
 cylinder { <1106300.54, 0, 6410484.32>,<1106312.68, 0, 6410497.68>,1.8 }
 sphere { <1106285.71, 0, 6410485.46>,1.8 }
 cylinder { <1106285.71, 0, 6410485.46>,<1106300.54, 0, 6410484.32>,1.8 }
 sphere { <1106276.36, 0, 6410497.22>,1.8 }
 cylinder { <1106276.36, 0, 6410497.22>,<1106285.71, 0, 6410485.46>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=23246550 */
object {union { sphere { <1106277.13, 0, 6410213.33>,1.5 }
 sphere { <1106280.63, 0, 6410201.42>,1.5 }
 cylinder { <1106280.63, 0, 6410201.42>,<1106277.13, 0, 6410213.33>,1.5 }
 sphere { <1106289.23, 0, 6410193.17>,1.5 }
 cylinder { <1106289.23, 0, 6410193.17>,<1106280.63, 0, 6410201.42>,1.5 }
 sphere { <1106296.53, 0, 6410190.57>,1.5 }
 cylinder { <1106296.53, 0, 6410190.57>,<1106289.23, 0, 6410193.17>,1.5 }
 sphere { <1106304.4, 0, 6410190.48>,1.5 }
 cylinder { <1106304.4, 0, 6410190.48>,<1106296.53, 0, 6410190.57>,1.5 }
 sphere { <1106315.29, 0, 6410195.23>,1.5 }
 cylinder { <1106315.29, 0, 6410195.23>,<1106304.4, 0, 6410190.48>,1.5 }
 sphere { <1106322.72, 0, 6410205.28>,1.5 }
 cylinder { <1106322.72, 0, 6410205.28>,<1106315.29, 0, 6410195.23>,1.5 }
 sphere { <1106323.9, 0, 6410218.11>,1.5 }
 cylinder { <1106323.9, 0, 6410218.11>,<1106322.72, 0, 6410205.28>,1.5 }
 sphere { <1106318.56, 0, 6410229.45>,1.5 }
 cylinder { <1106318.56, 0, 6410229.45>,<1106323.9, 0, 6410218.11>,1.5 }
 sphere { <1106311.36, 0, 6410234.83>,1.5 }
 cylinder { <1106311.36, 0, 6410234.83>,<1106318.56, 0, 6410229.45>,1.5 }
 sphere { <1106301.64, 0, 6410237.35>,1.5 }
 cylinder { <1106301.64, 0, 6410237.35>,<1106311.36, 0, 6410234.83>,1.5 }
 sphere { <1106290.49, 0, 6410235.04>,1.5 }
 cylinder { <1106290.49, 0, 6410235.04>,<1106301.64, 0, 6410237.35>,1.5 }
 sphere { <1106284.06, 0, 6410230.49>,1.5 }
 cylinder { <1106284.06, 0, 6410230.49>,<1106290.49, 0, 6410235.04>,1.5 }
 sphere { <1106279.09, 0, 6410223.21>,1.5 }
 cylinder { <1106279.09, 0, 6410223.21>,<1106284.06, 0, 6410230.49>,1.5 }
 sphere { <1106277.13, 0, 6410213.33>,1.5 }
 cylinder { <1106277.13, 0, 6410213.33>,<1106279.09, 0, 6410223.21>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106277.13, 0, 6410213.33>,1.8 }
 sphere { <1106280.63, 0, 6410201.42>,1.8 }
 cylinder { <1106280.63, 0, 6410201.42>,<1106277.13, 0, 6410213.33>,1.8 }
 sphere { <1106289.23, 0, 6410193.17>,1.8 }
 cylinder { <1106289.23, 0, 6410193.17>,<1106280.63, 0, 6410201.42>,1.8 }
 sphere { <1106296.53, 0, 6410190.57>,1.8 }
 cylinder { <1106296.53, 0, 6410190.57>,<1106289.23, 0, 6410193.17>,1.8 }
 sphere { <1106304.4, 0, 6410190.48>,1.8 }
 cylinder { <1106304.4, 0, 6410190.48>,<1106296.53, 0, 6410190.57>,1.8 }
 sphere { <1106315.29, 0, 6410195.23>,1.8 }
 cylinder { <1106315.29, 0, 6410195.23>,<1106304.4, 0, 6410190.48>,1.8 }
 sphere { <1106322.72, 0, 6410205.28>,1.8 }
 cylinder { <1106322.72, 0, 6410205.28>,<1106315.29, 0, 6410195.23>,1.8 }
 sphere { <1106323.9, 0, 6410218.11>,1.8 }
 cylinder { <1106323.9, 0, 6410218.11>,<1106322.72, 0, 6410205.28>,1.8 }
 sphere { <1106318.56, 0, 6410229.45>,1.8 }
 cylinder { <1106318.56, 0, 6410229.45>,<1106323.9, 0, 6410218.11>,1.8 }
 sphere { <1106311.36, 0, 6410234.83>,1.8 }
 cylinder { <1106311.36, 0, 6410234.83>,<1106318.56, 0, 6410229.45>,1.8 }
 sphere { <1106301.64, 0, 6410237.35>,1.8 }
 cylinder { <1106301.64, 0, 6410237.35>,<1106311.36, 0, 6410234.83>,1.8 }
 sphere { <1106290.49, 0, 6410235.04>,1.8 }
 cylinder { <1106290.49, 0, 6410235.04>,<1106301.64, 0, 6410237.35>,1.8 }
 sphere { <1106284.06, 0, 6410230.49>,1.8 }
 cylinder { <1106284.06, 0, 6410230.49>,<1106290.49, 0, 6410235.04>,1.8 }
 sphere { <1106279.09, 0, 6410223.21>,1.8 }
 cylinder { <1106279.09, 0, 6410223.21>,<1106284.06, 0, 6410230.49>,1.8 }
 sphere { <1106277.13, 0, 6410213.33>,1.8 }
 cylinder { <1106277.13, 0, 6410213.33>,<1106279.09, 0, 6410223.21>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59007426 */
object {union { sphere { <1106300.54, 0, 6410484.32>,1.5 }
 sphere { <1106294.05, 0, 6410461.14>,1.5 }
 cylinder { <1106294.05, 0, 6410461.14>,<1106300.54, 0, 6410484.32>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106300.54, 0, 6410484.32>,1.8 }
 sphere { <1106294.05, 0, 6410461.14>,1.8 }
 cylinder { <1106294.05, 0, 6410461.14>,<1106300.54, 0, 6410484.32>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=23246549 */
object {union { sphere { <1106304.4, 0, 6410190.48>,1.5 }
 sphere { <1106353.66, 0, 6410081.27>,1.5 }
 cylinder { <1106353.66, 0, 6410081.27>,<1106304.4, 0, 6410190.48>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106304.4, 0, 6410190.48>,1.8 }
 sphere { <1106353.66, 0, 6410081.27>,1.8 }
 cylinder { <1106353.66, 0, 6410081.27>,<1106304.4, 0, 6410190.48>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59007425 */
object {union { sphere { <1106309.78, 0, 6410512.79>,1.5 }
 sphere { <1106321.97, 0, 6410533.34>,1.5 }
 cylinder { <1106321.97, 0, 6410533.34>,<1106309.78, 0, 6410512.79>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106309.78, 0, 6410512.79>,1.8 }
 sphere { <1106321.97, 0, 6410533.34>,1.8 }
 cylinder { <1106321.97, 0, 6410533.34>,<1106309.78, 0, 6410512.79>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59007427 */
object {union { sphere { <1106312.68, 0, 6410497.68>,1.5 }
 sphere { <1106387.94, 0, 6410474.73>,1.5 }
 cylinder { <1106387.94, 0, 6410474.73>,<1106312.68, 0, 6410497.68>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106312.68, 0, 6410497.68>,1.8 }
 sphere { <1106387.94, 0, 6410474.73>,1.8 }
 cylinder { <1106387.94, 0, 6410474.73>,<1106312.68, 0, 6410497.68>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59005241 */
object {union { sphere { <1106321.97, 0, 6410533.34>,1.5 }
 sphere { <1106336.48, 0, 6410578.2>,1.5 }
 cylinder { <1106336.48, 0, 6410578.2>,<1106321.97, 0, 6410533.34>,1.5 }
 sphere { <1106351.21, 0, 6410625.05>,1.5 }
 cylinder { <1106351.21, 0, 6410625.05>,<1106336.48, 0, 6410578.2>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106321.97, 0, 6410533.34>,1.8 }
 sphere { <1106336.48, 0, 6410578.2>,1.8 }
 cylinder { <1106336.48, 0, 6410578.2>,<1106321.97, 0, 6410533.34>,1.8 }
 sphere { <1106351.21, 0, 6410625.05>,1.8 }
 cylinder { <1106351.21, 0, 6410625.05>,<1106336.48, 0, 6410578.2>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=7835349 */
object {union { sphere { <1106341.66, 0, 6410039.3>,1.5 }
 sphere { <1106323.54, 0, 6410017.48>,1.5 }
 cylinder { <1106323.54, 0, 6410017.48>,<1106341.66, 0, 6410039.3>,1.5 }
 sphere { <1106351.37, 0, 6409998.93>,1.5 }
 cylinder { <1106351.37, 0, 6409998.93>,<1106323.54, 0, 6410017.48>,1.5 }
 sphere { <1106392.19, 0, 6410000.79>,1.5 }
 cylinder { <1106392.19, 0, 6410000.79>,<1106351.37, 0, 6409998.93>,1.5 }
 sphere { <1106448.7, 0, 6409977.25>,1.5 }
 cylinder { <1106448.7, 0, 6409977.25>,<1106392.19, 0, 6410000.79>,1.5 }
 sphere { <1106538.77, 0, 6410002.64>,1.5 }
 cylinder { <1106538.77, 0, 6410002.64>,<1106448.7, 0, 6409977.25>,1.5 }
 sphere { <1106548.04, 0, 6410049.02>,1.5 }
 cylinder { <1106548.04, 0, 6410049.02>,<1106538.77, 0, 6410002.64>,1.5 }
 sphere { <1106436.72, 0, 6410210.44>,1.5 }
 cylinder { <1106436.72, 0, 6410210.44>,<1106548.04, 0, 6410049.02>,1.5 }
 sphere { <1106475.68, 0, 6410290.22>,1.5 }
 cylinder { <1106475.68, 0, 6410290.22>,<1106436.72, 0, 6410210.44>,1.5 }
 sphere { <1106566.6, 0, 6410325.47>,1.5 }
 cylinder { <1106566.6, 0, 6410325.47>,<1106475.68, 0, 6410290.22>,1.5 }
 sphere { <1106653.79, 0, 6410332.88>,1.5 }
 cylinder { <1106653.79, 0, 6410332.88>,<1106566.6, 0, 6410325.47>,1.5 }
 sphere { <1106679.76, 0, 6410405.24>,1.5 }
 cylinder { <1106679.76, 0, 6410405.24>,<1106653.79, 0, 6410332.88>,1.5 }
 sphere { <1106631.53, 0, 6410472.04>,1.5 }
 cylinder { <1106631.53, 0, 6410472.04>,<1106679.76, 0, 6410405.24>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106341.66, 0, 6410039.3>,1.8 }
 sphere { <1106323.54, 0, 6410017.48>,1.8 }
 cylinder { <1106323.54, 0, 6410017.48>,<1106341.66, 0, 6410039.3>,1.8 }
 sphere { <1106351.37, 0, 6409998.93>,1.8 }
 cylinder { <1106351.37, 0, 6409998.93>,<1106323.54, 0, 6410017.48>,1.8 }
 sphere { <1106392.19, 0, 6410000.79>,1.8 }
 cylinder { <1106392.19, 0, 6410000.79>,<1106351.37, 0, 6409998.93>,1.8 }
 sphere { <1106448.7, 0, 6409977.25>,1.8 }
 cylinder { <1106448.7, 0, 6409977.25>,<1106392.19, 0, 6410000.79>,1.8 }
 sphere { <1106538.77, 0, 6410002.64>,1.8 }
 cylinder { <1106538.77, 0, 6410002.64>,<1106448.7, 0, 6409977.25>,1.8 }
 sphere { <1106548.04, 0, 6410049.02>,1.8 }
 cylinder { <1106548.04, 0, 6410049.02>,<1106538.77, 0, 6410002.64>,1.8 }
 sphere { <1106436.72, 0, 6410210.44>,1.8 }
 cylinder { <1106436.72, 0, 6410210.44>,<1106548.04, 0, 6410049.02>,1.8 }
 sphere { <1106475.68, 0, 6410290.22>,1.8 }
 cylinder { <1106475.68, 0, 6410290.22>,<1106436.72, 0, 6410210.44>,1.8 }
 sphere { <1106566.6, 0, 6410325.47>,1.8 }
 cylinder { <1106566.6, 0, 6410325.47>,<1106475.68, 0, 6410290.22>,1.8 }
 sphere { <1106653.79, 0, 6410332.88>,1.8 }
 cylinder { <1106653.79, 0, 6410332.88>,<1106566.6, 0, 6410325.47>,1.8 }
 sphere { <1106679.76, 0, 6410405.24>,1.8 }
 cylinder { <1106679.76, 0, 6410405.24>,<1106653.79, 0, 6410332.88>,1.8 }
 sphere { <1106631.53, 0, 6410472.04>,1.8 }
 cylinder { <1106631.53, 0, 6410472.04>,<1106679.76, 0, 6410405.24>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=4568538 */
object {union { sphere { <1106396.81, 0, 6409850.67>,1.5 }
 sphere { <1106337.81, 0, 6409810.91>,1.5 }
 cylinder { <1106337.81, 0, 6409810.91>,<1106396.81, 0, 6409850.67>,1.5 }
 sphere { <1106336.05, 0, 6409795.65>,1.5 }
 cylinder { <1106336.05, 0, 6409795.65>,<1106337.81, 0, 6409810.91>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106396.81, 0, 6409850.67>,1.8 }
 sphere { <1106337.81, 0, 6409810.91>,1.8 }
 cylinder { <1106337.81, 0, 6409810.91>,<1106396.81, 0, 6409850.67>,1.8 }
 sphere { <1106336.05, 0, 6409795.65>,1.8 }
 cylinder { <1106336.05, 0, 6409795.65>,<1106337.81, 0, 6409810.91>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10202688 */
object {union { sphere { <1106336.05, 0, 6409795.65>,1.5 }
 sphere { <1106341.47, 0, 6409795.55>,1.5 }
 cylinder { <1106341.47, 0, 6409795.55>,<1106336.05, 0, 6409795.65>,1.5 }
 sphere { <1106359.75, 0, 6409809.83>,1.5 }
 cylinder { <1106359.75, 0, 6409809.83>,<1106341.47, 0, 6409795.55>,1.5 }
 sphere { <1106405.67, 0, 6409839.16>,1.5 }
 cylinder { <1106405.67, 0, 6409839.16>,<1106359.75, 0, 6409809.83>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106336.05, 0, 6409795.65>,1.8 }
 sphere { <1106341.47, 0, 6409795.55>,1.8 }
 cylinder { <1106341.47, 0, 6409795.55>,<1106336.05, 0, 6409795.65>,1.8 }
 sphere { <1106359.75, 0, 6409809.83>,1.8 }
 cylinder { <1106359.75, 0, 6409809.83>,<1106341.47, 0, 6409795.55>,1.8 }
 sphere { <1106405.67, 0, 6409839.16>,1.8 }
 cylinder { <1106405.67, 0, 6409839.16>,<1106359.75, 0, 6409809.83>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10202690 */
object {union { sphere { <1106341.47, 0, 6409795.55>,1.5 }
 sphere { <1106348.05, 0, 6409792.64>,1.5 }
 cylinder { <1106348.05, 0, 6409792.64>,<1106341.47, 0, 6409795.55>,1.5 }
 sphere { <1106357.19, 0, 6409793.36>,1.5 }
 cylinder { <1106357.19, 0, 6409793.36>,<1106348.05, 0, 6409792.64>,1.5 }
 sphere { <1106418.48, 0, 6409803.6>,1.5 }
 cylinder { <1106418.48, 0, 6409803.6>,<1106357.19, 0, 6409793.36>,1.5 }
 sphere { <1106445.31, 0, 6409774.38>,1.5 }
 cylinder { <1106445.31, 0, 6409774.38>,<1106418.48, 0, 6409803.6>,1.5 }
 sphere { <1106472.49, 0, 6409750.37>,1.5 }
 cylinder { <1106472.49, 0, 6409750.37>,<1106445.31, 0, 6409774.38>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106341.47, 0, 6409795.55>,1.8 }
 sphere { <1106348.05, 0, 6409792.64>,1.8 }
 cylinder { <1106348.05, 0, 6409792.64>,<1106341.47, 0, 6409795.55>,1.8 }
 sphere { <1106357.19, 0, 6409793.36>,1.8 }
 cylinder { <1106357.19, 0, 6409793.36>,<1106348.05, 0, 6409792.64>,1.8 }
 sphere { <1106418.48, 0, 6409803.6>,1.8 }
 cylinder { <1106418.48, 0, 6409803.6>,<1106357.19, 0, 6409793.36>,1.8 }
 sphere { <1106445.31, 0, 6409774.38>,1.8 }
 cylinder { <1106445.31, 0, 6409774.38>,<1106418.48, 0, 6409803.6>,1.8 }
 sphere { <1106472.49, 0, 6409750.37>,1.8 }
 cylinder { <1106472.49, 0, 6409750.37>,<1106445.31, 0, 6409774.38>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=7835346 */
object {union { sphere { <1106341.66, 0, 6410039.3>,1.5 }
 sphere { <1106350.59, 0, 6410070.4>,1.5 }
 cylinder { <1106350.59, 0, 6410070.4>,<1106341.66, 0, 6410039.3>,1.5 }
 sphere { <1106353.66, 0, 6410081.27>,1.5 }
 cylinder { <1106353.66, 0, 6410081.27>,<1106350.59, 0, 6410070.4>,1.5 }
 sphere { <1106424.91, 0, 6410319.02>,1.5 }
 cylinder { <1106424.91, 0, 6410319.02>,<1106353.66, 0, 6410081.27>,1.5 }
 sphere { <1106494.15, 0, 6410577.8>,1.5 }
 cylinder { <1106494.15, 0, 6410577.8>,<1106424.91, 0, 6410319.02>,1.5 }
 sphere { <1106497.91, 0, 6410591.85>,1.5 }
 cylinder { <1106497.91, 0, 6410591.85>,<1106494.15, 0, 6410577.8>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106341.66, 0, 6410039.3>,1.8 }
 sphere { <1106350.59, 0, 6410070.4>,1.8 }
 cylinder { <1106350.59, 0, 6410070.4>,<1106341.66, 0, 6410039.3>,1.8 }
 sphere { <1106353.66, 0, 6410081.27>,1.8 }
 cylinder { <1106353.66, 0, 6410081.27>,<1106350.59, 0, 6410070.4>,1.8 }
 sphere { <1106424.91, 0, 6410319.02>,1.8 }
 cylinder { <1106424.91, 0, 6410319.02>,<1106353.66, 0, 6410081.27>,1.8 }
 sphere { <1106494.15, 0, 6410577.8>,1.8 }
 cylinder { <1106494.15, 0, 6410577.8>,<1106424.91, 0, 6410319.02>,1.8 }
 sphere { <1106497.91, 0, 6410591.85>,1.8 }
 cylinder { <1106497.91, 0, 6410591.85>,<1106494.15, 0, 6410577.8>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10202691 */
object {union { sphere { <1106401.84, 0, 6409866.71>,1.5 }
 sphere { <1106392.38, 0, 6409884.83>,1.5 }
 cylinder { <1106392.38, 0, 6409884.83>,<1106401.84, 0, 6409866.71>,1.5 }
 sphere { <1106386.69, 0, 6409888.71>,1.5 }
 cylinder { <1106386.69, 0, 6409888.71>,<1106392.38, 0, 6409884.83>,1.5 }
 sphere { <1106377.37, 0, 6409897.76>,1.5 }
 cylinder { <1106377.37, 0, 6409897.76>,<1106386.69, 0, 6409888.71>,1.5 }
 sphere { <1106378.15, 0, 6409921.06>,1.5 }
 cylinder { <1106378.15, 0, 6409921.06>,<1106377.37, 0, 6409897.76>,1.5 }
 sphere { <1106361.32, 0, 6409935.82>,1.5 }
 cylinder { <1106361.32, 0, 6409935.82>,<1106378.15, 0, 6409921.06>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106401.84, 0, 6409866.71>,1.8 }
 sphere { <1106392.38, 0, 6409884.83>,1.8 }
 cylinder { <1106392.38, 0, 6409884.83>,<1106401.84, 0, 6409866.71>,1.8 }
 sphere { <1106386.69, 0, 6409888.71>,1.8 }
 cylinder { <1106386.69, 0, 6409888.71>,<1106392.38, 0, 6409884.83>,1.8 }
 sphere { <1106377.37, 0, 6409897.76>,1.8 }
 cylinder { <1106377.37, 0, 6409897.76>,<1106386.69, 0, 6409888.71>,1.8 }
 sphere { <1106378.15, 0, 6409921.06>,1.8 }
 cylinder { <1106378.15, 0, 6409921.06>,<1106377.37, 0, 6409897.76>,1.8 }
 sphere { <1106361.32, 0, 6409935.82>,1.8 }
 cylinder { <1106361.32, 0, 6409935.82>,<1106378.15, 0, 6409921.06>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10202694 */
object {union { sphere { <1106374.86, 0, 6409699.54>,1.5 }
 sphere { <1106409.89, 0, 6409734.58>,1.5 }
 cylinder { <1106409.89, 0, 6409734.58>,<1106374.86, 0, 6409699.54>,1.5 }
 sphere { <1106445.31, 0, 6409774.38>,1.5 }
 cylinder { <1106445.31, 0, 6409774.38>,<1106409.89, 0, 6409734.58>,1.5 }
 sphere { <1106483.67, 0, 6409809.6>,1.5 }
 cylinder { <1106483.67, 0, 6409809.6>,<1106445.31, 0, 6409774.38>,1.5 }
 sphere { <1106526.95, 0, 6409839.28>,1.5 }
 cylinder { <1106526.95, 0, 6409839.28>,<1106483.67, 0, 6409809.6>,1.5 }
 sphere { <1106592.08, 0, 6409870.19>,1.5 }
 cylinder { <1106592.08, 0, 6409870.19>,<1106526.95, 0, 6409839.28>,1.5 }
 sphere { <1106686.89, 0, 6409891.21>,1.5 }
 cylinder { <1106686.89, 0, 6409891.21>,<1106592.08, 0, 6409870.19>,1.5 }
 sphere { <1106707.86, 0, 6409909.77>,1.5 }
 cylinder { <1106707.86, 0, 6409909.77>,<1106686.89, 0, 6409891.21>,1.5 }
 sphere { <1106724.39, 0, 6409944.8>,1.5 }
 cylinder { <1106724.39, 0, 6409944.8>,<1106707.86, 0, 6409909.77>,1.5 }
 sphere { <1106724.26, 0, 6409971.67>,1.5 }
 cylinder { <1106724.26, 0, 6409971.67>,<1106724.39, 0, 6409944.8>,1.5 }
 sphere { <1106752.83, 0, 6409990.14>,1.5 }
 cylinder { <1106752.83, 0, 6409990.14>,<1106724.26, 0, 6409971.67>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106374.86, 0, 6409699.54>,1.8 }
 sphere { <1106409.89, 0, 6409734.58>,1.8 }
 cylinder { <1106409.89, 0, 6409734.58>,<1106374.86, 0, 6409699.54>,1.8 }
 sphere { <1106445.31, 0, 6409774.38>,1.8 }
 cylinder { <1106445.31, 0, 6409774.38>,<1106409.89, 0, 6409734.58>,1.8 }
 sphere { <1106483.67, 0, 6409809.6>,1.8 }
 cylinder { <1106483.67, 0, 6409809.6>,<1106445.31, 0, 6409774.38>,1.8 }
 sphere { <1106526.95, 0, 6409839.28>,1.8 }
 cylinder { <1106526.95, 0, 6409839.28>,<1106483.67, 0, 6409809.6>,1.8 }
 sphere { <1106592.08, 0, 6409870.19>,1.8 }
 cylinder { <1106592.08, 0, 6409870.19>,<1106526.95, 0, 6409839.28>,1.8 }
 sphere { <1106686.89, 0, 6409891.21>,1.8 }
 cylinder { <1106686.89, 0, 6409891.21>,<1106592.08, 0, 6409870.19>,1.8 }
 sphere { <1106707.86, 0, 6409909.77>,1.8 }
 cylinder { <1106707.86, 0, 6409909.77>,<1106686.89, 0, 6409891.21>,1.8 }
 sphere { <1106724.39, 0, 6409944.8>,1.8 }
 cylinder { <1106724.39, 0, 6409944.8>,<1106707.86, 0, 6409909.77>,1.8 }
 sphere { <1106724.26, 0, 6409971.67>,1.8 }
 cylinder { <1106724.26, 0, 6409971.67>,<1106724.39, 0, 6409944.8>,1.8 }
 sphere { <1106752.83, 0, 6409990.14>,1.8 }
 cylinder { <1106752.83, 0, 6409990.14>,<1106724.26, 0, 6409971.67>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=59007429 */
object {union { sphere { <1106387.94, 0, 6410474.73>,1.5 }
 sphere { <1106389.45, 0, 6410474.42>,1.5 }
 cylinder { <1106389.45, 0, 6410474.42>,<1106387.94, 0, 6410474.73>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106387.94, 0, 6410474.73>,1.8 }
 sphere { <1106389.45, 0, 6410474.42>,1.8 }
 cylinder { <1106389.45, 0, 6410474.42>,<1106387.94, 0, 6410474.73>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10202689 */
object {union { sphere { <1106414.29, 0, 6409871.25>,1.5 }
 sphere { <1106401.84, 0, 6409866.71>,1.5 }
 cylinder { <1106401.84, 0, 6409866.71>,<1106414.29, 0, 6409871.25>,1.5 }
 sphere { <1106396.81, 0, 6409850.67>,1.5 }
 cylinder { <1106396.81, 0, 6409850.67>,<1106401.84, 0, 6409866.71>,1.5 }
 sphere { <1106405.67, 0, 6409839.16>,1.5 }
 cylinder { <1106405.67, 0, 6409839.16>,<1106396.81, 0, 6409850.67>,1.5 }
 sphere { <1106421.42, 0, 6409839.28>,1.5 }
 cylinder { <1106421.42, 0, 6409839.28>,<1106405.67, 0, 6409839.16>,1.5 }
 sphere { <1106429.62, 0, 6409849.12>,1.5 }
 cylinder { <1106429.62, 0, 6409849.12>,<1106421.42, 0, 6409839.28>,1.5 }
 sphere { <1106427.2, 0, 6409864.23>,1.5 }
 cylinder { <1106427.2, 0, 6409864.23>,<1106429.62, 0, 6409849.12>,1.5 }
 sphere { <1106414.29, 0, 6409871.25>,1.5 }
 cylinder { <1106414.29, 0, 6409871.25>,<1106427.2, 0, 6409864.23>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106414.29, 0, 6409871.25>,1.8 }
 sphere { <1106401.84, 0, 6409866.71>,1.8 }
 cylinder { <1106401.84, 0, 6409866.71>,<1106414.29, 0, 6409871.25>,1.8 }
 sphere { <1106396.81, 0, 6409850.67>,1.8 }
 cylinder { <1106396.81, 0, 6409850.67>,<1106401.84, 0, 6409866.71>,1.8 }
 sphere { <1106405.67, 0, 6409839.16>,1.8 }
 cylinder { <1106405.67, 0, 6409839.16>,<1106396.81, 0, 6409850.67>,1.8 }
 sphere { <1106421.42, 0, 6409839.28>,1.8 }
 cylinder { <1106421.42, 0, 6409839.28>,<1106405.67, 0, 6409839.16>,1.8 }
 sphere { <1106429.62, 0, 6409849.12>,1.8 }
 cylinder { <1106429.62, 0, 6409849.12>,<1106421.42, 0, 6409839.28>,1.8 }
 sphere { <1106427.2, 0, 6409864.23>,1.8 }
 cylinder { <1106427.2, 0, 6409864.23>,<1106429.62, 0, 6409849.12>,1.8 }
 sphere { <1106414.29, 0, 6409871.25>,1.8 }
 cylinder { <1106414.29, 0, 6409871.25>,<1106427.2, 0, 6409864.23>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10202698 */
object {union { sphere { <1106418.48, 0, 6409803.6>,1.5 }
 sphere { <1106443.44, 0, 6409819.21>,1.5 }
 cylinder { <1106443.44, 0, 6409819.21>,<1106418.48, 0, 6409803.6>,1.5 }
 sphere { <1106461.78, 0, 6409851.07>,1.5 }
 cylinder { <1106461.78, 0, 6409851.07>,<1106443.44, 0, 6409819.21>,1.5 }
 sphere { <1106478.36, 0, 6409866.33>,1.5 }
 cylinder { <1106478.36, 0, 6409866.33>,<1106461.78, 0, 6409851.07>,1.5 }
 sphere { <1106498.43, 0, 6409877.69>,1.5 }
 cylinder { <1106498.43, 0, 6409877.69>,<1106478.36, 0, 6409866.33>,1.5 }
 sphere { <1106535.09, 0, 6409886.85>,1.5 }
 cylinder { <1106535.09, 0, 6409886.85>,<1106498.43, 0, 6409877.69>,1.5 }
 sphere { <1106586.86, 0, 6409903.2>,1.5 }
 cylinder { <1106586.86, 0, 6409903.2>,<1106535.09, 0, 6409886.85>,1.5 }
 sphere { <1106611.47, 0, 6409908.66>,1.5 }
 cylinder { <1106611.47, 0, 6409908.66>,<1106586.86, 0, 6409903.2>,1.5 }
 sphere { <1106639.4, 0, 6409918.27>,1.5 }
 cylinder { <1106639.4, 0, 6409918.27>,<1106611.47, 0, 6409908.66>,1.5 }
 sphere { <1106661.65, 0, 6409930.92>,1.5 }
 cylinder { <1106661.65, 0, 6409930.92>,<1106639.4, 0, 6409918.27>,1.5 }
 sphere { <1106675.62, 0, 6409924.82>,1.5 }
 cylinder { <1106675.62, 0, 6409924.82>,<1106661.65, 0, 6409930.92>,1.5 }
 sphere { <1106707.86, 0, 6409909.77>,1.5 }
 cylinder { <1106707.86, 0, 6409909.77>,<1106675.62, 0, 6409924.82>,1.5 }
 sphere { <1106734.7, 0, 6409880.71>,1.5 }
 cylinder { <1106734.7, 0, 6409880.71>,<1106707.86, 0, 6409909.77>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106418.48, 0, 6409803.6>,1.8 }
 sphere { <1106443.44, 0, 6409819.21>,1.8 }
 cylinder { <1106443.44, 0, 6409819.21>,<1106418.48, 0, 6409803.6>,1.8 }
 sphere { <1106461.78, 0, 6409851.07>,1.8 }
 cylinder { <1106461.78, 0, 6409851.07>,<1106443.44, 0, 6409819.21>,1.8 }
 sphere { <1106478.36, 0, 6409866.33>,1.8 }
 cylinder { <1106478.36, 0, 6409866.33>,<1106461.78, 0, 6409851.07>,1.8 }
 sphere { <1106498.43, 0, 6409877.69>,1.8 }
 cylinder { <1106498.43, 0, 6409877.69>,<1106478.36, 0, 6409866.33>,1.8 }
 sphere { <1106535.09, 0, 6409886.85>,1.8 }
 cylinder { <1106535.09, 0, 6409886.85>,<1106498.43, 0, 6409877.69>,1.8 }
 sphere { <1106586.86, 0, 6409903.2>,1.8 }
 cylinder { <1106586.86, 0, 6409903.2>,<1106535.09, 0, 6409886.85>,1.8 }
 sphere { <1106611.47, 0, 6409908.66>,1.8 }
 cylinder { <1106611.47, 0, 6409908.66>,<1106586.86, 0, 6409903.2>,1.8 }
 sphere { <1106639.4, 0, 6409918.27>,1.8 }
 cylinder { <1106639.4, 0, 6409918.27>,<1106611.47, 0, 6409908.66>,1.8 }
 sphere { <1106661.65, 0, 6409930.92>,1.8 }
 cylinder { <1106661.65, 0, 6409930.92>,<1106639.4, 0, 6409918.27>,1.8 }
 sphere { <1106675.62, 0, 6409924.82>,1.8 }
 cylinder { <1106675.62, 0, 6409924.82>,<1106661.65, 0, 6409930.92>,1.8 }
 sphere { <1106707.86, 0, 6409909.77>,1.8 }
 cylinder { <1106707.86, 0, 6409909.77>,<1106675.62, 0, 6409924.82>,1.8 }
 sphere { <1106734.7, 0, 6409880.71>,1.8 }
 cylinder { <1106734.7, 0, 6409880.71>,<1106707.86, 0, 6409909.77>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=23021992 */
object {union { sphere { <1106427.2, 0, 6409864.23>,1.5 }
 sphere { <1106453.54, 0, 6409897.99>,1.5 }
 cylinder { <1106453.54, 0, 6409897.99>,<1106427.2, 0, 6409864.23>,1.5 }
 sphere { <1106474.52, 0, 6409912.35>,1.5 }
 cylinder { <1106474.52, 0, 6409912.35>,<1106453.54, 0, 6409897.99>,1.5 }
 sphere { <1106497.04, 0, 6409922.77>,1.5 }
 cylinder { <1106497.04, 0, 6409922.77>,<1106474.52, 0, 6409912.35>,1.5 }
 sphere { <1106535.17, 0, 6409933.56>,1.5 }
 cylinder { <1106535.17, 0, 6409933.56>,<1106497.04, 0, 6409922.77>,1.5 }
 sphere { <1106559.49, 0, 6409942.04>,1.5 }
 cylinder { <1106559.49, 0, 6409942.04>,<1106535.17, 0, 6409933.56>,1.5 }
 sphere { <1106588.31, 0, 6409952.29>,1.5 }
 cylinder { <1106588.31, 0, 6409952.29>,<1106559.49, 0, 6409942.04>,1.5 }
 sphere { <1106604.5, 0, 6409960.77>,1.5 }
 cylinder { <1106604.5, 0, 6409960.77>,<1106588.31, 0, 6409952.29>,1.5 }
 sphere { <1106620.06, 0, 6409975.06>,1.5 }
 cylinder { <1106620.06, 0, 6409975.06>,<1106604.5, 0, 6409960.77>,1.5 }
 sphere { <1106625.47, 0, 6410014.05>,1.5 }
 cylinder { <1106625.47, 0, 6410014.05>,<1106620.06, 0, 6409975.06>,1.5 }
 sphere { <1106623.41, 0, 6410025.17>,1.5 }
 cylinder { <1106623.41, 0, 6410025.17>,<1106625.47, 0, 6410014.05>,1.5 }
 sphere { <1106610.22, 0, 6410053.21>,1.5 }
 cylinder { <1106610.22, 0, 6410053.21>,<1106623.41, 0, 6410025.17>,1.5 }
 sphere { <1106565.7, 0, 6410126.98>,1.5 }
 cylinder { <1106565.7, 0, 6410126.98>,<1106610.22, 0, 6410053.21>,1.5 }
 sphere { <1106565.7, 0, 6410152.53>,1.5 }
 cylinder { <1106565.7, 0, 6410152.53>,<1106565.7, 0, 6410126.98>,1.5 }
 sphere { <1106569.82, 0, 6410175.21>,1.5 }
 cylinder { <1106569.82, 0, 6410175.21>,<1106565.7, 0, 6410152.53>,1.5 }
 sphere { <1106582.19, 0, 6410197.88>,1.5 }
 cylinder { <1106582.19, 0, 6410197.88>,<1106569.82, 0, 6410175.21>,1.5 }
 sphere { <1106601.97, 0, 6410223.85>,1.5 }
 cylinder { <1106601.97, 0, 6410223.85>,<1106582.19, 0, 6410197.88>,1.5 }
 sphere { <1106617.22, 0, 6410239.09>,1.5 }
 cylinder { <1106617.22, 0, 6410239.09>,<1106601.97, 0, 6410223.85>,1.5 }
 sphere { <1106646.08, 0, 6410258.48>,1.5 }
 cylinder { <1106646.08, 0, 6410258.48>,<1106617.22, 0, 6410239.09>,1.5 }
 sphere { <1106684.42, 0, 6410274.13>,1.5 }
 cylinder { <1106684.42, 0, 6410274.13>,<1106646.08, 0, 6410258.48>,1.5 }
 sphere { <1106726.45, 0, 6410293.51>,1.5 }
 cylinder { <1106726.45, 0, 6410293.51>,<1106684.42, 0, 6410274.13>,1.5 }
 sphere { <1106742.54, 0, 6410302.98>,1.5 }
 cylinder { <1106742.54, 0, 6410302.98>,<1106726.45, 0, 6410293.51>,1.5 }
 sphere { <1106756.13, 0, 6410322.36>,1.5 }
 cylinder { <1106756.13, 0, 6410322.36>,<1106742.54, 0, 6410302.98>,1.5 }
 sphere { <1106766.44, 0, 6410349.97>,1.5 }
 cylinder { <1106766.44, 0, 6410349.97>,<1106756.13, 0, 6410322.36>,1.5 }
 sphere { <1106766.44, 0, 6410362.27>,1.5 }
 cylinder { <1106766.44, 0, 6410362.27>,<1106766.44, 0, 6410349.97>,1.5 }
 sphere { <1106746.65, 0, 6410393.25>,1.5 }
 cylinder { <1106746.65, 0, 6410393.25>,<1106766.44, 0, 6410362.27>,1.5 }
 sphere { <1106726.04, 0, 6410427.05>,1.5 }
 cylinder { <1106726.04, 0, 6410427.05>,<1106746.65, 0, 6410393.25>,1.5 }
 sphere { <1106700.49, 0, 6410481.89>,1.5 }
 cylinder { <1106700.49, 0, 6410481.89>,<1106726.04, 0, 6410427.05>,1.5 }
 sphere { <1106681.07, 0, 6410536.1>,1.5 }
 cylinder { <1106681.07, 0, 6410536.1>,<1106700.49, 0, 6410481.89>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106427.2, 0, 6409864.23>,1.8 }
 sphere { <1106453.54, 0, 6409897.99>,1.8 }
 cylinder { <1106453.54, 0, 6409897.99>,<1106427.2, 0, 6409864.23>,1.8 }
 sphere { <1106474.52, 0, 6409912.35>,1.8 }
 cylinder { <1106474.52, 0, 6409912.35>,<1106453.54, 0, 6409897.99>,1.8 }
 sphere { <1106497.04, 0, 6409922.77>,1.8 }
 cylinder { <1106497.04, 0, 6409922.77>,<1106474.52, 0, 6409912.35>,1.8 }
 sphere { <1106535.17, 0, 6409933.56>,1.8 }
 cylinder { <1106535.17, 0, 6409933.56>,<1106497.04, 0, 6409922.77>,1.8 }
 sphere { <1106559.49, 0, 6409942.04>,1.8 }
 cylinder { <1106559.49, 0, 6409942.04>,<1106535.17, 0, 6409933.56>,1.8 }
 sphere { <1106588.31, 0, 6409952.29>,1.8 }
 cylinder { <1106588.31, 0, 6409952.29>,<1106559.49, 0, 6409942.04>,1.8 }
 sphere { <1106604.5, 0, 6409960.77>,1.8 }
 cylinder { <1106604.5, 0, 6409960.77>,<1106588.31, 0, 6409952.29>,1.8 }
 sphere { <1106620.06, 0, 6409975.06>,1.8 }
 cylinder { <1106620.06, 0, 6409975.06>,<1106604.5, 0, 6409960.77>,1.8 }
 sphere { <1106625.47, 0, 6410014.05>,1.8 }
 cylinder { <1106625.47, 0, 6410014.05>,<1106620.06, 0, 6409975.06>,1.8 }
 sphere { <1106623.41, 0, 6410025.17>,1.8 }
 cylinder { <1106623.41, 0, 6410025.17>,<1106625.47, 0, 6410014.05>,1.8 }
 sphere { <1106610.22, 0, 6410053.21>,1.8 }
 cylinder { <1106610.22, 0, 6410053.21>,<1106623.41, 0, 6410025.17>,1.8 }
 sphere { <1106565.7, 0, 6410126.98>,1.8 }
 cylinder { <1106565.7, 0, 6410126.98>,<1106610.22, 0, 6410053.21>,1.8 }
 sphere { <1106565.7, 0, 6410152.53>,1.8 }
 cylinder { <1106565.7, 0, 6410152.53>,<1106565.7, 0, 6410126.98>,1.8 }
 sphere { <1106569.82, 0, 6410175.21>,1.8 }
 cylinder { <1106569.82, 0, 6410175.21>,<1106565.7, 0, 6410152.53>,1.8 }
 sphere { <1106582.19, 0, 6410197.88>,1.8 }
 cylinder { <1106582.19, 0, 6410197.88>,<1106569.82, 0, 6410175.21>,1.8 }
 sphere { <1106601.97, 0, 6410223.85>,1.8 }
 cylinder { <1106601.97, 0, 6410223.85>,<1106582.19, 0, 6410197.88>,1.8 }
 sphere { <1106617.22, 0, 6410239.09>,1.8 }
 cylinder { <1106617.22, 0, 6410239.09>,<1106601.97, 0, 6410223.85>,1.8 }
 sphere { <1106646.08, 0, 6410258.48>,1.8 }
 cylinder { <1106646.08, 0, 6410258.48>,<1106617.22, 0, 6410239.09>,1.8 }
 sphere { <1106684.42, 0, 6410274.13>,1.8 }
 cylinder { <1106684.42, 0, 6410274.13>,<1106646.08, 0, 6410258.48>,1.8 }
 sphere { <1106726.45, 0, 6410293.51>,1.8 }
 cylinder { <1106726.45, 0, 6410293.51>,<1106684.42, 0, 6410274.13>,1.8 }
 sphere { <1106742.54, 0, 6410302.98>,1.8 }
 cylinder { <1106742.54, 0, 6410302.98>,<1106726.45, 0, 6410293.51>,1.8 }
 sphere { <1106756.13, 0, 6410322.36>,1.8 }
 cylinder { <1106756.13, 0, 6410322.36>,<1106742.54, 0, 6410302.98>,1.8 }
 sphere { <1106766.44, 0, 6410349.97>,1.8 }
 cylinder { <1106766.44, 0, 6410349.97>,<1106756.13, 0, 6410322.36>,1.8 }
 sphere { <1106766.44, 0, 6410362.27>,1.8 }
 cylinder { <1106766.44, 0, 6410362.27>,<1106766.44, 0, 6410349.97>,1.8 }
 sphere { <1106746.65, 0, 6410393.25>,1.8 }
 cylinder { <1106746.65, 0, 6410393.25>,<1106766.44, 0, 6410362.27>,1.8 }
 sphere { <1106726.04, 0, 6410427.05>,1.8 }
 cylinder { <1106726.04, 0, 6410427.05>,<1106746.65, 0, 6410393.25>,1.8 }
 sphere { <1106700.49, 0, 6410481.89>,1.8 }
 cylinder { <1106700.49, 0, 6410481.89>,<1106726.04, 0, 6410427.05>,1.8 }
 sphere { <1106681.07, 0, 6410536.1>,1.8 }
 cylinder { <1106681.07, 0, 6410536.1>,<1106700.49, 0, 6410481.89>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10202696 */
object {union { sphere { <1106538.15, 0, 6410191.9>,1.5 }
 sphere { <1106508.91, 0, 6410199.76>,1.5 }
 cylinder { <1106508.91, 0, 6410199.76>,<1106538.15, 0, 6410191.9>,1.5 }
 sphere { <1106528.55, 0, 6410247.77>,1.5 }
 cylinder { <1106528.55, 0, 6410247.77>,<1106508.91, 0, 6410199.76>,1.5 }
 sphere { <1106556.88, 0, 6410240.01>,1.5 }
 cylinder { <1106556.88, 0, 6410240.01>,<1106528.55, 0, 6410247.77>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106538.15, 0, 6410191.9>,1.8 }
 sphere { <1106508.91, 0, 6410199.76>,1.8 }
 cylinder { <1106508.91, 0, 6410199.76>,<1106538.15, 0, 6410191.9>,1.8 }
 sphere { <1106528.55, 0, 6410247.77>,1.8 }
 cylinder { <1106528.55, 0, 6410247.77>,<1106508.91, 0, 6410199.76>,1.8 }
 sphere { <1106556.88, 0, 6410240.01>,1.8 }
 cylinder { <1106556.88, 0, 6410240.01>,<1106528.55, 0, 6410247.77>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=34254226 */
object {union { sphere { <1106637.66, 0, 6410669.18>,1.5 }
 sphere { <1106526.72, 0, 6410583.01>,1.5 }
 cylinder { <1106526.72, 0, 6410583.01>,<1106637.66, 0, 6410669.18>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106637.66, 0, 6410669.18>,1.8 }
 sphere { <1106526.72, 0, 6410583.01>,1.8 }
 cylinder { <1106526.72, 0, 6410583.01>,<1106637.66, 0, 6410669.18>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=51268644 */
object {union { sphere { <1106535.17, 0, 6409933.56>,1.5 }
 sphere { <1106586.86, 0, 6409903.2>,1.5 }
 cylinder { <1106586.86, 0, 6409903.2>,<1106535.17, 0, 6409933.56>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106535.17, 0, 6409933.56>,1.8 }
 sphere { <1106586.86, 0, 6409903.2>,1.8 }
 cylinder { <1106586.86, 0, 6409903.2>,<1106535.17, 0, 6409933.56>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10202695 */
object {union { sphere { <1106565.7, 0, 6410126.98>,1.5 }
 sphere { <1106546.88, 0, 6410155.67>,1.5 }
 cylinder { <1106546.88, 0, 6410155.67>,<1106565.7, 0, 6410126.98>,1.5 }
 sphere { <1106539.46, 0, 6410173.14>,1.5 }
 cylinder { <1106539.46, 0, 6410173.14>,<1106546.88, 0, 6410155.67>,1.5 }
 sphere { <1106538.15, 0, 6410191.9>,1.5 }
 cylinder { <1106538.15, 0, 6410191.9>,<1106539.46, 0, 6410173.14>,1.5 }
 sphere { <1106541.64, 0, 6410211.54>,1.5 }
 cylinder { <1106541.64, 0, 6410211.54>,<1106538.15, 0, 6410191.9>,1.5 }
 sphere { <1106552.55, 0, 6410228.99>,1.5 }
 cylinder { <1106552.55, 0, 6410228.99>,<1106541.64, 0, 6410211.54>,1.5 }
 sphere { <1106556.88, 0, 6410240.01>,1.5 }
 cylinder { <1106556.88, 0, 6410240.01>,<1106552.55, 0, 6410228.99>,1.5 }
 sphere { <1106570.88, 0, 6410258.23>,1.5 }
 cylinder { <1106570.88, 0, 6410258.23>,<1106556.88, 0, 6410240.01>,1.5 }
 sphere { <1106586.15, 0, 6410268.27>,1.5 }
 cylinder { <1106586.15, 0, 6410268.27>,<1106570.88, 0, 6410258.23>,1.5 }
 sphere { <1106613.65, 0, 6410265.22>,1.5 }
 cylinder { <1106613.65, 0, 6410265.22>,<1106586.15, 0, 6410268.27>,1.5 }
 sphere { <1106646.08, 0, 6410258.48>,1.5 }
 cylinder { <1106646.08, 0, 6410258.48>,<1106613.65, 0, 6410265.22>,1.5 }
 sphere { <1106697.44, 0, 6410241.22>,1.5 }
 cylinder { <1106697.44, 0, 6410241.22>,<1106646.08, 0, 6410258.48>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106565.7, 0, 6410126.98>,1.8 }
 sphere { <1106546.88, 0, 6410155.67>,1.8 }
 cylinder { <1106546.88, 0, 6410155.67>,<1106565.7, 0, 6410126.98>,1.8 }
 sphere { <1106539.46, 0, 6410173.14>,1.8 }
 cylinder { <1106539.46, 0, 6410173.14>,<1106546.88, 0, 6410155.67>,1.8 }
 sphere { <1106538.15, 0, 6410191.9>,1.8 }
 cylinder { <1106538.15, 0, 6410191.9>,<1106539.46, 0, 6410173.14>,1.8 }
 sphere { <1106541.64, 0, 6410211.54>,1.8 }
 cylinder { <1106541.64, 0, 6410211.54>,<1106538.15, 0, 6410191.9>,1.8 }
 sphere { <1106552.55, 0, 6410228.99>,1.8 }
 cylinder { <1106552.55, 0, 6410228.99>,<1106541.64, 0, 6410211.54>,1.8 }
 sphere { <1106556.88, 0, 6410240.01>,1.8 }
 cylinder { <1106556.88, 0, 6410240.01>,<1106552.55, 0, 6410228.99>,1.8 }
 sphere { <1106570.88, 0, 6410258.23>,1.8 }
 cylinder { <1106570.88, 0, 6410258.23>,<1106556.88, 0, 6410240.01>,1.8 }
 sphere { <1106586.15, 0, 6410268.27>,1.8 }
 cylinder { <1106586.15, 0, 6410268.27>,<1106570.88, 0, 6410258.23>,1.8 }
 sphere { <1106613.65, 0, 6410265.22>,1.8 }
 cylinder { <1106613.65, 0, 6410265.22>,<1106586.15, 0, 6410268.27>,1.8 }
 sphere { <1106646.08, 0, 6410258.48>,1.8 }
 cylinder { <1106646.08, 0, 6410258.48>,<1106613.65, 0, 6410265.22>,1.8 }
 sphere { <1106697.44, 0, 6410241.22>,1.8 }
 cylinder { <1106697.44, 0, 6410241.22>,<1106646.08, 0, 6410258.48>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10202706 */
object {union { sphere { <1106645.06, 0, 6410069.71>,1.5 }
 sphere { <1106574.9, 0, 6410125.77>,1.5 }
 cylinder { <1106574.9, 0, 6410125.77>,<1106645.06, 0, 6410069.71>,1.5 }
 sphere { <1106565.7, 0, 6410152.53>,1.5 }
 cylinder { <1106565.7, 0, 6410152.53>,<1106574.9, 0, 6410125.77>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106645.06, 0, 6410069.71>,1.8 }
 sphere { <1106574.9, 0, 6410125.77>,1.8 }
 cylinder { <1106574.9, 0, 6410125.77>,<1106645.06, 0, 6410069.71>,1.8 }
 sphere { <1106565.7, 0, 6410152.53>,1.8 }
 cylinder { <1106565.7, 0, 6410152.53>,<1106574.9, 0, 6410125.77>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=35077680 */
object {union { sphere { <1106586.86, 0, 6409903.2>,1.5 }
 sphere { <1106592.08, 0, 6409870.19>,1.5 }
 cylinder { <1106592.08, 0, 6409870.19>,<1106586.86, 0, 6409903.2>,1.5 }
 sphere { <1106599.39, 0, 6409831.71>,1.5 }
 cylinder { <1106599.39, 0, 6409831.71>,<1106592.08, 0, 6409870.19>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106586.86, 0, 6409903.2>,1.8 }
 sphere { <1106592.08, 0, 6409870.19>,1.8 }
 cylinder { <1106592.08, 0, 6409870.19>,<1106586.86, 0, 6409903.2>,1.8 }
 sphere { <1106599.39, 0, 6409831.71>,1.8 }
 cylinder { <1106599.39, 0, 6409831.71>,<1106592.08, 0, 6409870.19>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=34420156 */
object {union { sphere { <1106677.26, 0, 6410624.44>,1.5 }
 sphere { <1106591.49, 0, 6410562.81>,1.5 }
 cylinder { <1106591.49, 0, 6410562.81>,<1106677.26, 0, 6410624.44>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106677.26, 0, 6410624.44>,1.8 }
 sphere { <1106591.49, 0, 6410562.81>,1.8 }
 cylinder { <1106591.49, 0, 6410562.81>,<1106677.26, 0, 6410624.44>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10202697 */
object {union { sphere { <1106613.65, 0, 6410265.22>,1.5 }
 sphere { <1106631.11, 0, 6410277.87>,1.5 }
 cylinder { <1106631.11, 0, 6410277.87>,<1106613.65, 0, 6410265.22>,1.5 }
 sphere { <1106649.43, 0, 6410289.65>,1.5 }
 cylinder { <1106649.43, 0, 6410289.65>,<1106631.11, 0, 6410277.87>,1.5 }
 sphere { <1106679.99, 0, 6410300.57>,1.5 }
 cylinder { <1106679.99, 0, 6410300.57>,<1106649.43, 0, 6410289.65>,1.5 }
 sphere { <1106707.47, 0, 6410307.12>,1.5 }
 cylinder { <1106707.47, 0, 6410307.12>,<1106679.99, 0, 6410300.57>,1.5 }
 sphere { <1106724.06, 0, 6410306.24>,1.5 }
 cylinder { <1106724.06, 0, 6410306.24>,<1106707.47, 0, 6410307.12>,1.5 }
 sphere { <1106742.54, 0, 6410302.98>,1.5 }
 cylinder { <1106742.54, 0, 6410302.98>,<1106724.06, 0, 6410306.24>,1.5 }
 sphere { <1106730.16, 0, 6410255.65>,1.5 }
 cylinder { <1106730.16, 0, 6410255.65>,<1106742.54, 0, 6410302.98>,1.5 }
 sphere { <1106728.82, 0, 6410175.79>,1.5 }
 cylinder { <1106728.82, 0, 6410175.79>,<1106730.16, 0, 6410255.65>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106613.65, 0, 6410265.22>,1.8 }
 sphere { <1106631.11, 0, 6410277.87>,1.8 }
 cylinder { <1106631.11, 0, 6410277.87>,<1106613.65, 0, 6410265.22>,1.8 }
 sphere { <1106649.43, 0, 6410289.65>,1.8 }
 cylinder { <1106649.43, 0, 6410289.65>,<1106631.11, 0, 6410277.87>,1.8 }
 sphere { <1106679.99, 0, 6410300.57>,1.8 }
 cylinder { <1106679.99, 0, 6410300.57>,<1106649.43, 0, 6410289.65>,1.8 }
 sphere { <1106707.47, 0, 6410307.12>,1.8 }
 cylinder { <1106707.47, 0, 6410307.12>,<1106679.99, 0, 6410300.57>,1.8 }
 sphere { <1106724.06, 0, 6410306.24>,1.8 }
 cylinder { <1106724.06, 0, 6410306.24>,<1106707.47, 0, 6410307.12>,1.8 }
 sphere { <1106742.54, 0, 6410302.98>,1.8 }
 cylinder { <1106742.54, 0, 6410302.98>,<1106724.06, 0, 6410306.24>,1.8 }
 sphere { <1106730.16, 0, 6410255.65>,1.8 }
 cylinder { <1106730.16, 0, 6410255.65>,<1106742.54, 0, 6410302.98>,1.8 }
 sphere { <1106728.82, 0, 6410175.79>,1.8 }
 cylinder { <1106728.82, 0, 6410175.79>,<1106730.16, 0, 6410255.65>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=23012286 */
object {union { sphere { <1106620.06, 0, 6409975.06>,1.5 }
 sphere { <1106658.45, 0, 6410042.26>,1.5 }
 cylinder { <1106658.45, 0, 6410042.26>,<1106620.06, 0, 6409975.06>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106620.06, 0, 6409975.06>,1.8 }
 sphere { <1106658.45, 0, 6410042.26>,1.8 }
 cylinder { <1106658.45, 0, 6410042.26>,<1106620.06, 0, 6409975.06>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10202701 */
object {union { sphere { <1106658.45, 0, 6410042.26>,1.5 }
 sphere { <1106645.06, 0, 6410069.71>,1.5 }
 cylinder { <1106645.06, 0, 6410069.71>,<1106658.45, 0, 6410042.26>,1.5 }
 sphere { <1106631.97, 0, 6410103.3>,1.5 }
 cylinder { <1106631.97, 0, 6410103.3>,<1106645.06, 0, 6410069.71>,1.5 }
 sphere { <1106631.97, 0, 6410144.77>,1.5 }
 cylinder { <1106631.97, 0, 6410144.77>,<1106631.97, 0, 6410103.3>,1.5 }
 sphere { <1106641.58, 0, 6410178.81>,1.5 }
 cylinder { <1106641.58, 0, 6410178.81>,<1106631.97, 0, 6410144.77>,1.5 }
 sphere { <1106653.79, 0, 6410212.85>,1.5 }
 cylinder { <1106653.79, 0, 6410212.85>,<1106641.58, 0, 6410178.81>,1.5 }
 sphere { <1106674.74, 0, 6410230.3>,1.5 }
 cylinder { <1106674.74, 0, 6410230.3>,<1106653.79, 0, 6410212.85>,1.5 }
 sphere { <1106697.44, 0, 6410241.22>,1.5 }
 cylinder { <1106697.44, 0, 6410241.22>,<1106674.74, 0, 6410230.3>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106658.45, 0, 6410042.26>,1.8 }
 sphere { <1106645.06, 0, 6410069.71>,1.8 }
 cylinder { <1106645.06, 0, 6410069.71>,<1106658.45, 0, 6410042.26>,1.8 }
 sphere { <1106631.97, 0, 6410103.3>,1.8 }
 cylinder { <1106631.97, 0, 6410103.3>,<1106645.06, 0, 6410069.71>,1.8 }
 sphere { <1106631.97, 0, 6410144.77>,1.8 }
 cylinder { <1106631.97, 0, 6410144.77>,<1106631.97, 0, 6410103.3>,1.8 }
 sphere { <1106641.58, 0, 6410178.81>,1.8 }
 cylinder { <1106641.58, 0, 6410178.81>,<1106631.97, 0, 6410144.77>,1.8 }
 sphere { <1106653.79, 0, 6410212.85>,1.8 }
 cylinder { <1106653.79, 0, 6410212.85>,<1106641.58, 0, 6410178.81>,1.8 }
 sphere { <1106674.74, 0, 6410230.3>,1.8 }
 cylinder { <1106674.74, 0, 6410230.3>,<1106653.79, 0, 6410212.85>,1.8 }
 sphere { <1106697.44, 0, 6410241.22>,1.8 }
 cylinder { <1106697.44, 0, 6410241.22>,<1106674.74, 0, 6410230.3>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=34254224 */
object {union { sphere { <1106700.78, 0, 6410719.5>,1.5 }
 sphere { <1106695.95, 0, 6410715.5>,1.5 }
 cylinder { <1106695.95, 0, 6410715.5>,<1106700.78, 0, 6410719.5>,1.5 }
 sphere { <1106654.48, 0, 6410681.42>,1.5 }
 cylinder { <1106654.48, 0, 6410681.42>,<1106695.95, 0, 6410715.5>,1.5 }
 sphere { <1106648.65, 0, 6410677.56>,1.5 }
 cylinder { <1106648.65, 0, 6410677.56>,<1106654.48, 0, 6410681.42>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106700.78, 0, 6410719.5>,1.8 }
 sphere { <1106695.95, 0, 6410715.5>,1.8 }
 cylinder { <1106695.95, 0, 6410715.5>,<1106700.78, 0, 6410719.5>,1.8 }
 sphere { <1106654.48, 0, 6410681.42>,1.8 }
 cylinder { <1106654.48, 0, 6410681.42>,<1106695.95, 0, 6410715.5>,1.8 }
 sphere { <1106648.65, 0, 6410677.56>,1.8 }
 cylinder { <1106648.65, 0, 6410677.56>,<1106654.48, 0, 6410681.42>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=34420160 */
object {union { sphere { <1106654.48, 0, 6410681.42>,1.5 }
 sphere { <1106679.51, 0, 6410650.57>,1.5 }
 cylinder { <1106679.51, 0, 6410650.57>,<1106654.48, 0, 6410681.42>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106654.48, 0, 6410681.42>,1.8 }
 sphere { <1106679.51, 0, 6410650.57>,1.8 }
 cylinder { <1106679.51, 0, 6410650.57>,<1106654.48, 0, 6410681.42>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10202700 */
object {union { sphere { <1106661.65, 0, 6409930.92>,1.5 }
 sphere { <1106669.08, 0, 6409942.27>,1.5 }
 cylinder { <1106669.08, 0, 6409942.27>,<1106661.65, 0, 6409930.92>,1.5 }
 sphere { <1106673.44, 0, 6409955.37>,1.5 }
 cylinder { <1106673.44, 0, 6409955.37>,<1106669.08, 0, 6409942.27>,1.5 }
 sphere { <1106669.08, 0, 6409972.82>,1.5 }
 cylinder { <1106669.08, 0, 6409972.82>,<1106673.44, 0, 6409955.37>,1.5 }
 sphere { <1106666.89, 0, 6409989.41>,1.5 }
 cylinder { <1106666.89, 0, 6409989.41>,<1106669.08, 0, 6409972.82>,1.5 }
 sphere { <1106665.14, 0, 6410006.43>,1.5 }
 cylinder { <1106665.14, 0, 6410006.43>,<1106666.89, 0, 6409989.41>,1.5 }
 sphere { <1106661.65, 0, 6410023.02>,1.5 }
 cylinder { <1106661.65, 0, 6410023.02>,<1106665.14, 0, 6410006.43>,1.5 }
 sphere { <1106658.45, 0, 6410042.26>,1.5 }
 cylinder { <1106658.45, 0, 6410042.26>,<1106661.65, 0, 6410023.02>,1.5 }
 sphere { <1106682.17, 0, 6410068.4>,1.5 }
 cylinder { <1106682.17, 0, 6410068.4>,<1106658.45, 0, 6410042.26>,1.5 }
 sphere { <1106711.51, 0, 6410085.73>,1.5 }
 cylinder { <1106711.51, 0, 6410085.73>,<1106682.17, 0, 6410068.4>,1.5 }
 sphere { <1106729.1, 0, 6410107.73>,1.5 }
 cylinder { <1106729.1, 0, 6410107.73>,<1106711.51, 0, 6410085.73>,1.5 }
 sphere { <1106731.73, 0, 6410132.98>,1.5 }
 cylinder { <1106731.73, 0, 6410132.98>,<1106729.1, 0, 6410107.73>,1.5 }
 sphere { <1106728.82, 0, 6410175.79>,1.5 }
 cylinder { <1106728.82, 0, 6410175.79>,<1106731.73, 0, 6410132.98>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106661.65, 0, 6409930.92>,1.8 }
 sphere { <1106669.08, 0, 6409942.27>,1.8 }
 cylinder { <1106669.08, 0, 6409942.27>,<1106661.65, 0, 6409930.92>,1.8 }
 sphere { <1106673.44, 0, 6409955.37>,1.8 }
 cylinder { <1106673.44, 0, 6409955.37>,<1106669.08, 0, 6409942.27>,1.8 }
 sphere { <1106669.08, 0, 6409972.82>,1.8 }
 cylinder { <1106669.08, 0, 6409972.82>,<1106673.44, 0, 6409955.37>,1.8 }
 sphere { <1106666.89, 0, 6409989.41>,1.8 }
 cylinder { <1106666.89, 0, 6409989.41>,<1106669.08, 0, 6409972.82>,1.8 }
 sphere { <1106665.14, 0, 6410006.43>,1.8 }
 cylinder { <1106665.14, 0, 6410006.43>,<1106666.89, 0, 6409989.41>,1.8 }
 sphere { <1106661.65, 0, 6410023.02>,1.8 }
 cylinder { <1106661.65, 0, 6410023.02>,<1106665.14, 0, 6410006.43>,1.8 }
 sphere { <1106658.45, 0, 6410042.26>,1.8 }
 cylinder { <1106658.45, 0, 6410042.26>,<1106661.65, 0, 6410023.02>,1.8 }
 sphere { <1106682.17, 0, 6410068.4>,1.8 }
 cylinder { <1106682.17, 0, 6410068.4>,<1106658.45, 0, 6410042.26>,1.8 }
 sphere { <1106711.51, 0, 6410085.73>,1.8 }
 cylinder { <1106711.51, 0, 6410085.73>,<1106682.17, 0, 6410068.4>,1.8 }
 sphere { <1106729.1, 0, 6410107.73>,1.8 }
 cylinder { <1106729.1, 0, 6410107.73>,<1106711.51, 0, 6410085.73>,1.8 }
 sphere { <1106731.73, 0, 6410132.98>,1.8 }
 cylinder { <1106731.73, 0, 6410132.98>,<1106729.1, 0, 6410107.73>,1.8 }
 sphere { <1106728.82, 0, 6410175.79>,1.8 }
 cylinder { <1106728.82, 0, 6410175.79>,<1106731.73, 0, 6410132.98>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10202699 */
object {union { sphere { <1106675.62, 0, 6409924.82>,1.5 }
 sphere { <1106708.09, 0, 6409956.29>,1.5 }
 cylinder { <1106708.09, 0, 6409956.29>,<1106675.62, 0, 6409924.82>,1.5 }
 sphere { <1106724.26, 0, 6409971.67>,1.5 }
 cylinder { <1106724.26, 0, 6409971.67>,<1106708.09, 0, 6409956.29>,1.5 }
 sphere { <1106722.44, 0, 6409999.69>,1.5 }
 cylinder { <1106722.44, 0, 6409999.69>,<1106724.26, 0, 6409971.67>,1.5 }
 sphere { <1106723.57, 0, 6410052.62>,1.5 }
 cylinder { <1106723.57, 0, 6410052.62>,<1106722.44, 0, 6409999.69>,1.5 }
 sphere { <1106729.1, 0, 6410107.73>,1.5 }
 cylinder { <1106729.1, 0, 6410107.73>,<1106723.57, 0, 6410052.62>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106675.62, 0, 6409924.82>,1.8 }
 sphere { <1106708.09, 0, 6409956.29>,1.8 }
 cylinder { <1106708.09, 0, 6409956.29>,<1106675.62, 0, 6409924.82>,1.8 }
 sphere { <1106724.26, 0, 6409971.67>,1.8 }
 cylinder { <1106724.26, 0, 6409971.67>,<1106708.09, 0, 6409956.29>,1.8 }
 sphere { <1106722.44, 0, 6409999.69>,1.8 }
 cylinder { <1106722.44, 0, 6409999.69>,<1106724.26, 0, 6409971.67>,1.8 }
 sphere { <1106723.57, 0, 6410052.62>,1.8 }
 cylinder { <1106723.57, 0, 6410052.62>,<1106722.44, 0, 6409999.69>,1.8 }
 sphere { <1106729.1, 0, 6410107.73>,1.8 }
 cylinder { <1106729.1, 0, 6410107.73>,<1106723.57, 0, 6410052.62>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=34420157 */
object {union { sphere { <1106686.32, 0, 6410642.17>,1.5 }
 sphere { <1106692.07, 0, 6410635.08>,1.5 }
 cylinder { <1106692.07, 0, 6410635.08>,<1106686.32, 0, 6410642.17>,1.5 }
 sphere { <1106700.42, 0, 6410632.5>,1.5 }
 cylinder { <1106700.42, 0, 6410632.5>,<1106692.07, 0, 6410635.08>,1.5 }
 sphere { <1106721.85, 0, 6410640.46>,1.5 }
 cylinder { <1106721.85, 0, 6410640.46>,<1106700.42, 0, 6410632.5>,1.5 }
 sphere { <1106733.61, 0, 6410657.14>,1.5 }
 cylinder { <1106733.61, 0, 6410657.14>,<1106721.85, 0, 6410640.46>,1.5 }
 sphere { <1106734.41, 0, 6410665.56>,1.5 }
 cylinder { <1106734.41, 0, 6410665.56>,<1106733.61, 0, 6410657.14>,1.5 }
 sphere { <1106728.54, 0, 6410673.64>,1.5 }
 cylinder { <1106728.54, 0, 6410673.64>,<1106734.41, 0, 6410665.56>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106686.32, 0, 6410642.17>,1.8 }
 sphere { <1106692.07, 0, 6410635.08>,1.8 }
 cylinder { <1106692.07, 0, 6410635.08>,<1106686.32, 0, 6410642.17>,1.8 }
 sphere { <1106700.42, 0, 6410632.5>,1.8 }
 cylinder { <1106700.42, 0, 6410632.5>,<1106692.07, 0, 6410635.08>,1.8 }
 sphere { <1106721.85, 0, 6410640.46>,1.8 }
 cylinder { <1106721.85, 0, 6410640.46>,<1106700.42, 0, 6410632.5>,1.8 }
 sphere { <1106733.61, 0, 6410657.14>,1.8 }
 cylinder { <1106733.61, 0, 6410657.14>,<1106721.85, 0, 6410640.46>,1.8 }
 sphere { <1106734.41, 0, 6410665.56>,1.8 }
 cylinder { <1106734.41, 0, 6410665.56>,<1106733.61, 0, 6410657.14>,1.8 }
 sphere { <1106728.54, 0, 6410673.64>,1.8 }
 cylinder { <1106728.54, 0, 6410673.64>,<1106734.41, 0, 6410665.56>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=34420161 */
object {union { sphere { <1106695.95, 0, 6410715.5>,1.5 }
 sphere { <1106721.12, 0, 6410683.18>,1.5 }
 cylinder { <1106721.12, 0, 6410683.18>,<1106695.95, 0, 6410715.5>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106695.95, 0, 6410715.5>,1.8 }
 sphere { <1106721.12, 0, 6410683.18>,1.8 }
 cylinder { <1106721.12, 0, 6410683.18>,<1106695.95, 0, 6410715.5>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10202708 */
object {union { sphere { <1106728.82, 0, 6410175.79>,1.5 }
 sphere { <1106711.38, 0, 6410195.36>,1.5 }
 cylinder { <1106711.38, 0, 6410195.36>,<1106728.82, 0, 6410175.79>,1.5 }
 sphere { <1106702.67, 0, 6410222.45>,1.5 }
 cylinder { <1106702.67, 0, 6410222.45>,<1106711.38, 0, 6410195.36>,1.5 }
 sphere { <1106697.44, 0, 6410241.22>,1.5 }
 cylinder { <1106697.44, 0, 6410241.22>,<1106702.67, 0, 6410222.45>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106728.82, 0, 6410175.79>,1.8 }
 sphere { <1106711.38, 0, 6410195.36>,1.8 }
 cylinder { <1106711.38, 0, 6410195.36>,<1106728.82, 0, 6410175.79>,1.8 }
 sphere { <1106702.67, 0, 6410222.45>,1.8 }
 cylinder { <1106702.67, 0, 6410222.45>,<1106711.38, 0, 6410195.36>,1.8 }
 sphere { <1106697.44, 0, 6410241.22>,1.8 }
 cylinder { <1106697.44, 0, 6410241.22>,<1106702.67, 0, 6410222.45>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10202702 */
object {union { sphere { <1106697.44, 0, 6410241.22>,1.5 }
 sphere { <1106730.16, 0, 6410255.65>,1.5 }
 cylinder { <1106730.16, 0, 6410255.65>,<1106697.44, 0, 6410241.22>,1.5 }
 sphere { <1106763.78, 0, 6410269.15>,1.5 }
 cylinder { <1106763.78, 0, 6410269.15>,<1106730.16, 0, 6410255.65>,1.5 }
 sphere { <1106784.71, 0, 6410284.43>,1.5 }
 cylinder { <1106784.71, 0, 6410284.43>,<1106763.78, 0, 6410269.15>,1.5 }
 sphere { <1106814.83, 0, 6410311.88>,1.5 }
 cylinder { <1106814.83, 0, 6410311.88>,<1106784.71, 0, 6410284.43>,1.5 }
 sphere { <1106830.11, 0, 6410334.61>,1.5 }
 cylinder { <1106830.11, 0, 6410334.61>,<1106814.83, 0, 6410311.88>,1.5 }
 sphere { <1106802.18, 0, 6410386.1>,1.5 }
 cylinder { <1106802.18, 0, 6410386.1>,<1106830.11, 0, 6410334.61>,1.5 }
 sphere { <1106796.5, 0, 6410410.1>,1.5 }
 cylinder { <1106796.5, 0, 6410410.1>,<1106802.18, 0, 6410386.1>,1.5 }
 sphere { <1106798.69, 0, 6410423.21>,1.5 }
 cylinder { <1106798.69, 0, 6410423.21>,<1106796.5, 0, 6410410.1>,1.5 }
 sphere { <1106796.11, 0, 6410444.17>,1.5 }
 cylinder { <1106796.11, 0, 6410444.17>,<1106798.69, 0, 6410423.21>,1.5 }
 sphere { <1106787, 0, 6410473.42>,1.5 }
 cylinder { <1106787, 0, 6410473.42>,<1106796.11, 0, 6410444.17>,1.5 }
 sphere { <1106773.27, 0, 6410500.86>,1.5 }
 cylinder { <1106773.27, 0, 6410500.86>,<1106787, 0, 6410473.42>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106697.44, 0, 6410241.22>,1.8 }
 sphere { <1106730.16, 0, 6410255.65>,1.8 }
 cylinder { <1106730.16, 0, 6410255.65>,<1106697.44, 0, 6410241.22>,1.8 }
 sphere { <1106763.78, 0, 6410269.15>,1.8 }
 cylinder { <1106763.78, 0, 6410269.15>,<1106730.16, 0, 6410255.65>,1.8 }
 sphere { <1106784.71, 0, 6410284.43>,1.8 }
 cylinder { <1106784.71, 0, 6410284.43>,<1106763.78, 0, 6410269.15>,1.8 }
 sphere { <1106814.83, 0, 6410311.88>,1.8 }
 cylinder { <1106814.83, 0, 6410311.88>,<1106784.71, 0, 6410284.43>,1.8 }
 sphere { <1106830.11, 0, 6410334.61>,1.8 }
 cylinder { <1106830.11, 0, 6410334.61>,<1106814.83, 0, 6410311.88>,1.8 }
 sphere { <1106802.18, 0, 6410386.1>,1.8 }
 cylinder { <1106802.18, 0, 6410386.1>,<1106830.11, 0, 6410334.61>,1.8 }
 sphere { <1106796.5, 0, 6410410.1>,1.8 }
 cylinder { <1106796.5, 0, 6410410.1>,<1106802.18, 0, 6410386.1>,1.8 }
 sphere { <1106798.69, 0, 6410423.21>,1.8 }
 cylinder { <1106798.69, 0, 6410423.21>,<1106796.5, 0, 6410410.1>,1.8 }
 sphere { <1106796.11, 0, 6410444.17>,1.8 }
 cylinder { <1106796.11, 0, 6410444.17>,<1106798.69, 0, 6410423.21>,1.8 }
 sphere { <1106787, 0, 6410473.42>,1.8 }
 cylinder { <1106787, 0, 6410473.42>,<1106796.11, 0, 6410444.17>,1.8 }
 sphere { <1106773.27, 0, 6410500.86>,1.8 }
 cylinder { <1106773.27, 0, 6410500.86>,<1106787, 0, 6410473.42>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=34254221 */
object {union { sphere { <1106807.16, 0, 6410833.41>,1.5 }
 sphere { <1106796.35, 0, 6410789.44>,1.5 }
 cylinder { <1106796.35, 0, 6410789.44>,<1106807.16, 0, 6410833.41>,1.5 }
 sphere { <1106712.29, 0, 6410728.46>,1.5 }
 cylinder { <1106712.29, 0, 6410728.46>,<1106796.35, 0, 6410789.44>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106807.16, 0, 6410833.41>,1.8 }
 sphere { <1106796.35, 0, 6410789.44>,1.8 }
 cylinder { <1106796.35, 0, 6410789.44>,<1106807.16, 0, 6410833.41>,1.8 }
 sphere { <1106712.29, 0, 6410728.46>,1.8 }
 cylinder { <1106712.29, 0, 6410728.46>,<1106796.35, 0, 6410789.44>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=34420159 */
object {union { sphere { <1106728.63, 0, 6410625.84>,1.5 }
 sphere { <1106777.57, 0, 6410523.57>,1.5 }
 cylinder { <1106777.57, 0, 6410523.57>,<1106728.63, 0, 6410625.84>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106728.63, 0, 6410625.84>,1.8 }
 sphere { <1106777.57, 0, 6410523.57>,1.8 }
 cylinder { <1106777.57, 0, 6410523.57>,<1106728.63, 0, 6410625.84>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10202704 */
object {union { sphere { <1106728.82, 0, 6410175.79>,1.5 }
 sphere { <1106734.38, 0, 6410190.64>,1.5 }
 cylinder { <1106734.38, 0, 6410190.64>,<1106728.82, 0, 6410175.79>,1.5 }
 sphere { <1106751.13, 0, 6410209.14>,1.5 }
 cylinder { <1106751.13, 0, 6410209.14>,<1106734.38, 0, 6410190.64>,1.5 }
 sphere { <1106789.19, 0, 6410231.04>,1.5 }
 cylinder { <1106789.19, 0, 6410231.04>,<1106751.13, 0, 6410209.14>,1.5 }
 sphere { <1106820.94, 0, 6410257.36>,1.5 }
 cylinder { <1106820.94, 0, 6410257.36>,<1106789.19, 0, 6410231.04>,1.5 }
 sphere { <1106846.69, 0, 6410279.15>,1.5 }
 cylinder { <1106846.69, 0, 6410279.15>,<1106820.94, 0, 6410257.36>,1.5 }
 sphere { <1106844.07, 0, 6410305.36>,1.5 }
 cylinder { <1106844.07, 0, 6410305.36>,<1106846.69, 0, 6410279.15>,1.5 }
 sphere { <1106830.11, 0, 6410334.61>,1.5 }
 cylinder { <1106830.11, 0, 6410334.61>,<1106844.07, 0, 6410305.36>,1.5 }
 sphere { <1106853.5, 0, 6410367.94>,1.5 }
 cylinder { <1106853.5, 0, 6410367.94>,<1106830.11, 0, 6410334.61>,1.5 }
 sphere { <1106858.86, 0, 6410399.87>,1.5 }
 cylinder { <1106858.86, 0, 6410399.87>,<1106853.5, 0, 6410367.94>,1.5 }
 sphere { <1106873.01, 0, 6410479.44>,1.5 }
 cylinder { <1106873.01, 0, 6410479.44>,<1106858.86, 0, 6410399.87>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106728.82, 0, 6410175.79>,1.8 }
 sphere { <1106734.38, 0, 6410190.64>,1.8 }
 cylinder { <1106734.38, 0, 6410190.64>,<1106728.82, 0, 6410175.79>,1.8 }
 sphere { <1106751.13, 0, 6410209.14>,1.8 }
 cylinder { <1106751.13, 0, 6410209.14>,<1106734.38, 0, 6410190.64>,1.8 }
 sphere { <1106789.19, 0, 6410231.04>,1.8 }
 cylinder { <1106789.19, 0, 6410231.04>,<1106751.13, 0, 6410209.14>,1.8 }
 sphere { <1106820.94, 0, 6410257.36>,1.8 }
 cylinder { <1106820.94, 0, 6410257.36>,<1106789.19, 0, 6410231.04>,1.8 }
 sphere { <1106846.69, 0, 6410279.15>,1.8 }
 cylinder { <1106846.69, 0, 6410279.15>,<1106820.94, 0, 6410257.36>,1.8 }
 sphere { <1106844.07, 0, 6410305.36>,1.8 }
 cylinder { <1106844.07, 0, 6410305.36>,<1106846.69, 0, 6410279.15>,1.8 }
 sphere { <1106830.11, 0, 6410334.61>,1.8 }
 cylinder { <1106830.11, 0, 6410334.61>,<1106844.07, 0, 6410305.36>,1.8 }
 sphere { <1106853.5, 0, 6410367.94>,1.8 }
 cylinder { <1106853.5, 0, 6410367.94>,<1106830.11, 0, 6410334.61>,1.8 }
 sphere { <1106858.86, 0, 6410399.87>,1.8 }
 cylinder { <1106858.86, 0, 6410399.87>,<1106853.5, 0, 6410367.94>,1.8 }
 sphere { <1106873.01, 0, 6410479.44>,1.8 }
 cylinder { <1106873.01, 0, 6410479.44>,<1106858.86, 0, 6410399.87>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=34420149 */
object {union { sphere { <1106807.01, 0, 6410722.2>,1.5 }
 sphere { <1106745.65, 0, 6410673.64>,1.5 }
 cylinder { <1106745.65, 0, 6410673.64>,<1106807.01, 0, 6410722.2>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106807.01, 0, 6410722.2>,1.8 }
 sphere { <1106745.65, 0, 6410673.64>,1.8 }
 cylinder { <1106745.65, 0, 6410673.64>,<1106807.01, 0, 6410722.2>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=10202703 */
object {union { sphere { <1106855.58, 0, 6410251.91>,1.5 }
 sphere { <1106846.69, 0, 6410279.15>,1.5 }
 cylinder { <1106846.69, 0, 6410279.15>,<1106855.58, 0, 6410251.91>,1.5 }
 sphere { <1106835.78, 0, 6410293.55>,1.5 }
 cylinder { <1106835.78, 0, 6410293.55>,<1106846.69, 0, 6410279.15>,1.5 }
 sphere { <1106814.83, 0, 6410311.88>,1.5 }
 cylinder { <1106814.83, 0, 6410311.88>,<1106835.78, 0, 6410293.55>,1.5 }
 sphere { <1106783.85, 0, 6410352.47>,1.5 }
 cylinder { <1106783.85, 0, 6410352.47>,<1106814.83, 0, 6410311.88>,1.5 }
 sphere { <1106766.44, 0, 6410362.27>,1.5 }
 cylinder { <1106766.44, 0, 6410362.27>,<1106783.85, 0, 6410352.47>,1.5 }
 sphere { <1106758.53, 0, 6410420.1>,1.5 }
 cylinder { <1106758.53, 0, 6410420.1>,<1106766.44, 0, 6410362.27>,1.5 }
 sphere { <1106759.41, 0, 6410461.57>,1.5 }
 cylinder { <1106759.41, 0, 6410461.57>,<1106758.53, 0, 6410420.1>,1.5 }
 sphere { <1106773.27, 0, 6410500.86>,1.5 }
 cylinder { <1106773.27, 0, 6410500.86>,<1106759.41, 0, 6410461.57>,1.5 }
 sphere { <1106775.29, 0, 6410507.94>,1.5 }
 cylinder { <1106775.29, 0, 6410507.94>,<1106773.27, 0, 6410500.86>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106855.58, 0, 6410251.91>,1.8 }
 sphere { <1106846.69, 0, 6410279.15>,1.8 }
 cylinder { <1106846.69, 0, 6410279.15>,<1106855.58, 0, 6410251.91>,1.8 }
 sphere { <1106835.78, 0, 6410293.55>,1.8 }
 cylinder { <1106835.78, 0, 6410293.55>,<1106846.69, 0, 6410279.15>,1.8 }
 sphere { <1106814.83, 0, 6410311.88>,1.8 }
 cylinder { <1106814.83, 0, 6410311.88>,<1106835.78, 0, 6410293.55>,1.8 }
 sphere { <1106783.85, 0, 6410352.47>,1.8 }
 cylinder { <1106783.85, 0, 6410352.47>,<1106814.83, 0, 6410311.88>,1.8 }
 sphere { <1106766.44, 0, 6410362.27>,1.8 }
 cylinder { <1106766.44, 0, 6410362.27>,<1106783.85, 0, 6410352.47>,1.8 }
 sphere { <1106758.53, 0, 6410420.1>,1.8 }
 cylinder { <1106758.53, 0, 6410420.1>,<1106766.44, 0, 6410362.27>,1.8 }
 sphere { <1106759.41, 0, 6410461.57>,1.8 }
 cylinder { <1106759.41, 0, 6410461.57>,<1106758.53, 0, 6410420.1>,1.8 }
 sphere { <1106773.27, 0, 6410500.86>,1.8 }
 cylinder { <1106773.27, 0, 6410500.86>,<1106759.41, 0, 6410461.57>,1.8 }
 sphere { <1106775.29, 0, 6410507.94>,1.8 }
 cylinder { <1106775.29, 0, 6410507.94>,<1106773.27, 0, 6410500.86>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=37994221 */
object {union { sphere { <1106858.86, 0, 6410399.87>,1.5 }
 sphere { <1106798.69, 0, 6410423.21>,1.5 }
 cylinder { <1106798.69, 0, 6410423.21>,<1106858.86, 0, 6410399.87>,1.5 }
 sphere { <1106766.44, 0, 6410362.27>,1.5 }
 cylinder { <1106766.44, 0, 6410362.27>,<1106798.69, 0, 6410423.21>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106858.86, 0, 6410399.87>,1.8 }
 sphere { <1106798.69, 0, 6410423.21>,1.8 }
 cylinder { <1106798.69, 0, 6410423.21>,<1106858.86, 0, 6410399.87>,1.8 }
 sphere { <1106766.44, 0, 6410362.27>,1.8 }
 cylinder { <1106766.44, 0, 6410362.27>,<1106798.69, 0, 6410423.21>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=23021991 */
object {union { sphere { <1106789.19, 0, 6410231.04>,1.5 }
 sphere { <1106818.54, 0, 6410209.83>,1.5 }
 cylinder { <1106818.54, 0, 6410209.83>,<1106789.19, 0, 6410231.04>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106789.19, 0, 6410231.04>,1.8 }
 sphere { <1106818.54, 0, 6410209.83>,1.8 }
 cylinder { <1106818.54, 0, 6410209.83>,<1106789.19, 0, 6410231.04>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=37856508 */
object {union { sphere { <1106893.94, 0, 6410774.77>,1.5 }
 sphere { <1106858.24, 0, 6410777.3>,1.5 }
 cylinder { <1106858.24, 0, 6410777.3>,<1106893.94, 0, 6410774.77>,1.5 }
 sphere { <1106815.47, 0, 6410779.28>,1.5 }
 cylinder { <1106815.47, 0, 6410779.28>,<1106858.24, 0, 6410777.3>,1.5 }
 sphere { <1106796.35, 0, 6410789.44>,1.5 }
 cylinder { <1106796.35, 0, 6410789.44>,<1106815.47, 0, 6410779.28>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106893.94, 0, 6410774.77>,1.8 }
 sphere { <1106858.24, 0, 6410777.3>,1.8 }
 cylinder { <1106858.24, 0, 6410777.3>,<1106893.94, 0, 6410774.77>,1.8 }
 sphere { <1106815.47, 0, 6410779.28>,1.8 }
 cylinder { <1106815.47, 0, 6410779.28>,<1106858.24, 0, 6410777.3>,1.8 }
 sphere { <1106796.35, 0, 6410789.44>,1.8 }
 cylinder { <1106796.35, 0, 6410789.44>,<1106815.47, 0, 6410779.28>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=37856509 */
object {union { sphere { <1106895.37, 0, 6410644.78>,1.5 }
 sphere { <1106883.18, 0, 6410650.69>,1.5 }
 cylinder { <1106883.18, 0, 6410650.69>,<1106895.37, 0, 6410644.78>,1.5 }
 sphere { <1106844.07, 0, 6410669.26>,1.5 }
 cylinder { <1106844.07, 0, 6410669.26>,<1106883.18, 0, 6410650.69>,1.5 }
 sphere { <1106803.38, 0, 6410697.7>,1.5 }
 cylinder { <1106803.38, 0, 6410697.7>,<1106844.07, 0, 6410669.26>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_footway }
}

object {union { sphere { <1106895.37, 0, 6410644.78>,1.8 }
 sphere { <1106883.18, 0, 6410650.69>,1.8 }
 cylinder { <1106883.18, 0, 6410650.69>,<1106895.37, 0, 6410644.78>,1.8 }
 sphere { <1106844.07, 0, 6410669.26>,1.8 }
 cylinder { <1106844.07, 0, 6410669.26>,<1106883.18, 0, 6410650.69>,1.8 }
 sphere { <1106803.38, 0, 6410697.7>,1.8 }
 cylinder { <1106803.38, 0, 6410697.7>,<1106844.07, 0, 6410669.26>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=8106712 */
object {union { sphere { <1105775.02, 0, 6410606.13>,2.4 }
 sphere { <1105758.78, 0, 6410578.09>,2.4 }
 cylinder { <1105758.78, 0, 6410578.09>,<1105775.02, 0, 6410606.13>,2.4 }
 sphere { <1105729.18, 0, 6410526.96>,2.4 }
 cylinder { <1105729.18, 0, 6410526.96>,<1105758.78, 0, 6410578.09>,2.4 }
 sphere { <1105714.67, 0, 6410490.89>,2.4 }
 cylinder { <1105714.67, 0, 6410490.89>,<1105729.18, 0, 6410526.96>,2.4 }
 sphere { <1105717.07, 0, 6410474.87>,2.4 }
 cylinder { <1105717.07, 0, 6410474.87>,<1105714.67, 0, 6410490.89>,2.4 }
 sphere { <1105725.1, 0, 6410472.25>,2.4 }
 cylinder { <1105725.1, 0, 6410472.25>,<1105717.07, 0, 6410474.87>,2.4 }
 sphere { <1105771.95, 0, 6410555.14>,2.4 }
 cylinder { <1105771.95, 0, 6410555.14>,<1105725.1, 0, 6410472.25>,2.4 }
 sphere { <1105795.25, 0, 6410595.92>,2.4 }
 cylinder { <1105795.25, 0, 6410595.92>,<1105771.95, 0, 6410555.14>,2.4 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_unclassified }
}

object {union { sphere { <1105775.02, 0, 6410606.13>,2.88 }
 sphere { <1105758.78, 0, 6410578.09>,2.88 }
 cylinder { <1105758.78, 0, 6410578.09>,<1105775.02, 0, 6410606.13>,2.88 }
 sphere { <1105729.18, 0, 6410526.96>,2.88 }
 cylinder { <1105729.18, 0, 6410526.96>,<1105758.78, 0, 6410578.09>,2.88 }
 sphere { <1105714.67, 0, 6410490.89>,2.88 }
 cylinder { <1105714.67, 0, 6410490.89>,<1105729.18, 0, 6410526.96>,2.88 }
 sphere { <1105717.07, 0, 6410474.87>,2.88 }
 cylinder { <1105717.07, 0, 6410474.87>,<1105714.67, 0, 6410490.89>,2.88 }
 sphere { <1105725.1, 0, 6410472.25>,2.88 }
 cylinder { <1105725.1, 0, 6410472.25>,<1105717.07, 0, 6410474.87>,2.88 }
 sphere { <1105771.95, 0, 6410555.14>,2.88 }
 cylinder { <1105771.95, 0, 6410555.14>,<1105725.1, 0, 6410472.25>,2.88 }
 sphere { <1105795.25, 0, 6410595.92>,2.88 }
 cylinder { <1105795.25, 0, 6410595.92>,<1105771.95, 0, 6410555.14>,2.88 }
}scale <1, 0.05, 1>translate <0, -0.06, 0>texture { texture_highway_casing }
}

/* osm_id=32297659 */
object {union { sphere { <1105931.53, 0, 6409976.81>,1.5 }
 sphere { <1105932.09, 0, 6409967.43>,1.5 }
 cylinder { <1105932.09, 0, 6409967.43>,<1105931.53, 0, 6409976.81>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_steps }
}

object {union { sphere { <1105931.53, 0, 6409976.81>,1.8 }
 sphere { <1105932.09, 0, 6409967.43>,1.8 }
 cylinder { <1105932.09, 0, 6409967.43>,<1105931.53, 0, 6409976.81>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=25859908 */
object {union { sphere { <1106004.38, 0, 6410361.63>,1.5 }
 sphere { <1106019.45, 0, 6410355.3>,1.5 }
 cylinder { <1106019.45, 0, 6410355.3>,<1106004.38, 0, 6410361.63>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_steps }
}

object {union { sphere { <1106004.38, 0, 6410361.63>,1.8 }
 sphere { <1106019.45, 0, 6410355.3>,1.8 }
 cylinder { <1106019.45, 0, 6410355.3>,<1106004.38, 0, 6410361.63>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=34254225 */
object {union { sphere { <1106648.65, 0, 6410677.56>,1.5 }
 sphere { <1106637.66, 0, 6410669.18>,1.5 }
 cylinder { <1106637.66, 0, 6410669.18>,<1106648.65, 0, 6410677.56>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_steps }
}

object {union { sphere { <1106648.65, 0, 6410677.56>,1.8 }
 sphere { <1106637.66, 0, 6410669.18>,1.8 }
 cylinder { <1106637.66, 0, 6410669.18>,<1106648.65, 0, 6410677.56>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=34420150 */
object {union { sphere { <1106638.9, 0, 6410701.8>,1.5 }
 sphere { <1106654.48, 0, 6410681.42>,1.5 }
 cylinder { <1106654.48, 0, 6410681.42>,<1106638.9, 0, 6410701.8>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_steps }
}

object {union { sphere { <1106638.9, 0, 6410701.8>,1.8 }
 sphere { <1106654.48, 0, 6410681.42>,1.8 }
 cylinder { <1106654.48, 0, 6410681.42>,<1106638.9, 0, 6410701.8>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=34420155 */
object {union { sphere { <1106692.07, 0, 6410635.08>,1.5 }
 sphere { <1106677.26, 0, 6410624.44>,1.5 }
 cylinder { <1106677.26, 0, 6410624.44>,<1106692.07, 0, 6410635.08>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_steps }
}

object {union { sphere { <1106692.07, 0, 6410635.08>,1.8 }
 sphere { <1106677.26, 0, 6410624.44>,1.8 }
 cylinder { <1106677.26, 0, 6410624.44>,<1106692.07, 0, 6410635.08>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=34420152 */
object {union { sphere { <1106679.51, 0, 6410650.57>,1.5 }
 sphere { <1106686.32, 0, 6410642.17>,1.5 }
 cylinder { <1106686.32, 0, 6410642.17>,<1106679.51, 0, 6410650.57>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_steps }
}

object {union { sphere { <1106679.51, 0, 6410650.57>,1.8 }
 sphere { <1106686.32, 0, 6410642.17>,1.8 }
 cylinder { <1106686.32, 0, 6410642.17>,<1106679.51, 0, 6410650.57>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=34420151 */
object {union { sphere { <1106680.74, 0, 6410735.48>,1.5 }
 sphere { <1106695.95, 0, 6410715.5>,1.5 }
 cylinder { <1106695.95, 0, 6410715.5>,<1106680.74, 0, 6410735.48>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_steps }
}

object {union { sphere { <1106680.74, 0, 6410735.48>,1.8 }
 sphere { <1106695.95, 0, 6410715.5>,1.8 }
 cylinder { <1106695.95, 0, 6410715.5>,<1106680.74, 0, 6410735.48>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=34254223 */
object {union { sphere { <1106712.29, 0, 6410728.46>,1.5 }
 sphere { <1106700.78, 0, 6410719.5>,1.5 }
 cylinder { <1106700.78, 0, 6410719.5>,<1106712.29, 0, 6410728.46>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_steps }
}

object {union { sphere { <1106712.29, 0, 6410728.46>,1.8 }
 sphere { <1106700.78, 0, 6410719.5>,1.8 }
 cylinder { <1106700.78, 0, 6410719.5>,<1106712.29, 0, 6410728.46>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=34420153 */
object {union { sphere { <1106721.12, 0, 6410683.18>,1.5 }
 sphere { <1106728.54, 0, 6410673.64>,1.5 }
 cylinder { <1106728.54, 0, 6410673.64>,<1106721.12, 0, 6410683.18>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_steps }
}

object {union { sphere { <1106721.12, 0, 6410683.18>,1.8 }
 sphere { <1106728.54, 0, 6410673.64>,1.8 }
 cylinder { <1106728.54, 0, 6410673.64>,<1106721.12, 0, 6410683.18>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=34420158 */
object {union { sphere { <1106721.85, 0, 6410640.46>,1.5 }
 sphere { <1106728.63, 0, 6410625.84>,1.5 }
 cylinder { <1106728.63, 0, 6410625.84>,<1106721.85, 0, 6410640.46>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_steps }
}

object {union { sphere { <1106721.85, 0, 6410640.46>,1.8 }
 sphere { <1106728.63, 0, 6410625.84>,1.8 }
 cylinder { <1106728.63, 0, 6410625.84>,<1106721.85, 0, 6410640.46>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
}

/* osm_id=34420154 */
object {union { sphere { <1106745.65, 0, 6410673.64>,1.5 }
 sphere { <1106734.41, 0, 6410665.56>,1.5 }
 cylinder { <1106734.41, 0, 6410665.56>,<1106745.65, 0, 6410673.64>,1.5 }
}scale <1, 0.05, 1>translate <0, 0.0, 0>texture { texture_highway_steps }
}

object {union { sphere { <1106745.65, 0, 6410673.64>,1.8 }
 sphere { <1106734.41, 0, 6410665.56>,1.8 }
 cylinder { <1106734.41, 0, 6410665.56>,<1106745.65, 0, 6410673.64>,1.8 }
}scale <1, 0.05, 1>translate <0, -0.1, 0>texture { texture_highway_casing }
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

prism { linear_spline 0, 0.01, 47,
/* casing of osm_id=22662547 */
  <1105590.81872076, 6410600.82236407>,
  <1105590.6131496, 6410600.82940827>,
  <1105590.41336845, 6410600.87836355>,
  <1105590.22782989, 6410600.96715865>,
  <1105590.06438389, 6410601.09203671>,
  <1105589.92994571, 6410601.24771427>,
  <1105589.83020331, 6410601.42760474>,
  <1105589.7693767, 6410601.62409711>,
  <1105589.75003941, 6410601.82887795>,
  <1105590.16003941, 6410648.00887795>,
  <1105590.18042951, 6410648.2011011>,
  <1105590.23742175, 6410648.38581001>,
  <1105590.32888658, 6410648.55610294>,
  <1105590.45140638, 6410648.7056168>,
  <1105590.60040313, 6410648.82876495>,
  <1105590.77030949, 6410648.92094588>,
  <1105590.95477683, 6410648.9787152>,
  <1105663.63477683, 6410664.2187152>,
  <1105663.84269029, 6410664.23999638>,
  <1105664.05048624, 6410664.21759682>,
  <1105664.24908796, 6410664.15249495>,
  <1105664.42982035, 6410664.04753449>,
  <1105675.72642145, 6410655.79653686>,
  <1105684.27835183, 6410651.13816828>,
  <1105684.43484416, 6410651.03264021>,
  <1105684.56871952, 6410650.89958603>,
  <1105684.67520846, 6410650.74374595>,
  <1105684.7505172, 6410650.57067193>,
  <1105684.79196279, 6410650.38652989>,
  <1105684.79806869, 6410650.1978801>,
  <1105684.76861737, 6410650.0114434>,
  <1105684.70465807, 6410649.83386178>,
  <1105684.60846939, 6410649.67146177>,
  <1105684.48347816, 6410649.53002904>,
  <1105662.97347816, 6410629.39002904>,
  <1105662.77921306, 6410629.24783569>,
  <1105641.96468923, 6410617.57258498>,
  <1105642.26969007, 6410605.32489501>,
  <1105642.25625901, 6410605.13479359>,
  <1105642.20700808, 6410604.9506923>,
  <1105642.123726, 6410604.7792775>,
  <1105642.0094375, 6410604.62677478>,
  <1105641.86829341, 6410604.4987229>,
  <1105641.70541994, 6410604.39977254>,
  <1105641.52673248, 6410604.33351749>,
  <1105641.33872076, 6410604.30236407>,
  <1105590.81872076, 6410600.82236407>

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
    translate <0, 0.08, 0>
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

prism { linear_spline 0, 0.01, 38,
/* casing of osm_id=8106713 */
  <1105771.64055838, 6410554.18908156>,
  <1105771.4602007, 6410554.26816478>,
  <1105771.29883893, 6410554.38106044>,
  <1105771.16273117, 6410554.52339009>,
  <1105771.0571561, 6410554.68963375>,
  <1105770.98620823, 6410554.87334398>,
  <1105770.95263916, 6410555.06739595>,
  <1105770.95775077, 6410555.26426373>,
  <1105771.00134484, 6410555.45631218>,
  <1105771.08173065, 6410555.63609308>,
  <1105794.38173065, 6410596.41609308>,
  <1105794.50363335, 6410596.58553499>,
  <1105794.65805587, 6410596.725979>,
  <1105794.83826988, 6410596.83130583>,
  <1105795.03642332, 6410596.8969263>,
  <1105795.24388247, 6410596.91998129>,
  <1105837.74388247, 6410597.17998129>,
  <1105837.89252742, 6410597.16979085>,
  <1105838.03800839, 6410597.13762789>,
  <1105846.01800839, 6410594.73762789>,
  <1105846.2070844, 6410594.65885748>,
  <1105846.37614458, 6410594.54321503>,
  <1105846.51809606, 6410594.39555227>,
  <1105846.62698336, 6410594.22206431>,
  <1105846.69823816, 6410594.03002975>,
  <1105846.72887099, 6410593.82750531>,
  <1105846.71759667, 6410593.62298782>,
  <1105846.6648882, 6410593.42505769>,
  <1105825.4948882, 6410537.66505769>,
  <1105825.41086276, 6410537.49461198>,
  <1105825.29611094, 6410537.34313909>,
  <1105825.15477667, 6410537.21610902>,
  <1105824.99196382, 6410537.11810907>,
  <1105824.81355189, 6410537.05267821>,
  <1105824.62598368, 6410537.0221793>,
  <1105824.43603268, 6410537.0277137>,
  <1105824.25055838, 6410537.06908156>,
  <1105771.64055838, 6410554.18908156>

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
    translate <0, 0.08, 0>
}

prism { linear_spline 0, 0.01, 7,
/* osm_id=32297653 */
  <1105904.56, 6409966.37>,
  <1105921.88, 6409967.15>,
  <1105932.09, 6409967.43>,
  <1105944.63, 6409967.17>,
  <1105945.19, 6409925.13>,
  <1105904.87, 6409924.34>,
  <1105904.56, 6409966.37>

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

prism { linear_spline 0, 0.01, 41,
/* casing of osm_id=32297653 */
  <1105903.5600272, 6409966.36262452>,
  <1105903.5769298, 6409966.55322934>,
  <1105903.62982841, 6409966.73712506>,
  <1105903.71678608, 6409966.90757816>,
  <1105903.83461878, 6409967.05834736>,
  <1105903.97901196, 6409967.1839121>,
  <1105904.14467855, 6409967.27967472>,
  <1105904.3255525, 6409967.34212878>,
  <1105904.51501096, 6409967.36898748>,
  <1105921.83501096, 6409968.14898748>,
  <1105921.85258621, 6409968.14962417>,
  <1105932.06258621, 6409968.42962417>,
  <1105932.1107292, 6409968.42978513>,
  <1105944.6507292, 6409968.16978513>,
  <1105944.84122093, 6409968.14743834>,
  <1105945.02394259, 6409968.08913505>,
  <1105945.19217249, 6409967.99702001>,
  <1105945.33972204, 6409967.87448181>,
  <1105945.46116342, 6409967.72602821>,
  <1105945.55202922, 6409967.55712029>,
  <1105945.60897681, 6409967.37397157>,
  <1105945.62991129, 6409967.18331947>,
  <1105946.18991129, 6409925.14331947>,
  <1105946.17343914, 6409924.94876134>,
  <1105946.11947417, 6409924.7611128>,
  <1105946.03007374, 6409924.58752778>,
  <1105945.90864618, 6409924.43462408>,
  <1105945.75982081, 6409924.30823103>,
  <1105945.58927148, 6409924.21316725>,
  <1105945.40350025, 6409924.15305699>,
  <1105945.20958949, 6409924.13019189>,
  <1105904.88958949, 6409923.34019189>,
  <1105904.6926573, 6409923.35585084>,
  <1105904.50264634, 6409923.40991866>,
  <1105904.32697228, 6409923.50028523>,
  <1105904.17249123, 6409923.62342376>,
  <1105904.04523219, 6409923.77452845>,
  <1105903.95016178, 6409923.94770209>,
  <1105903.89099034, 6409924.13618613>,
  <1105903.8700272, 6409924.33262452>,
  <1105903.5600272, 6409966.36262452>

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
    translate <0, 0.08, 0>
}

prism { linear_spline 0, 0.01, 15,
/* osm_id=59005262 */
  <1106146.65, 6410505.03>,
  <1106148.15, 6410507.91>,
  <1106182.33, 6410575.11>,
  <1106302.17, 6410537.88>,
  <1106328.96, 6410625.55>,
  <1106352.98, 6410620.51>,
  <1106325.34, 6410531.96>,
  <1106397.97, 6410508.84>,
  <1106387.94, 6410474.73>,
  <1106376.39, 6410435.5>,
  <1106297.96, 6410459>,
  <1106267.6, 6410357.87>,
  <1106243.52, 6410365.35>,
  <1106276.5, 6410466.88>,
  <1106146.65, 6410505.03>

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

prism { linear_spline 0, 0.01, 80,
/* casing of osm_id=59005262 */
  <1106146.36811371, 6410504.07055218>,
  <1106146.18654151, 6410504.14388137>,
  <1106146.02274091, 6410504.25118934>,
  <1106145.88299297, 6410504.38836131>,
  <1106145.77265639, 6410504.55013732>,
  <1106145.69596212, 6410504.73031396>,
  <1106145.65585103, 6410504.92198226>,
  <1106145.65386122, 6410505.11779257>,
  <1106145.69006899, 6410505.31023642>,
  <1106145.76308592, 6410505.49193442>,
  <1106147.26085785, 6410508.36765654>,
  <1106181.43867119, 6410575.56335742>,
  <1106181.54663798, 6410575.73156572>,
  <1106181.68590076, 6410575.87494194>,
  <1106181.85089586, 6410575.98775806>,
  <1106182.03503158, 6410576.065507>,
  <1106182.23095154, 6410576.10508261>,
  <1106182.43082857, 6410576.10490381>,
  <1106182.62667742, 6410576.06497775>,
  <1106301.50725303, 6410539.13303658>,
  <1106328.00365436, 6410625.84223794>,
  <1106328.07379044, 6410626.01328459>,
  <1106328.17421365, 6410626.16849802>,
  <1106328.30149191, 6410626.30257365>,
  <1106328.45127537, 6410626.41092929>,
  <1106328.61844501, 6410626.48986179>,
  <1106328.79728765, 6410626.53667355>,
  <1106328.98169115, 6410626.54976472>,
  <1106329.16535333, 6410626.5286879>,
  <1106353.18535333, 6410621.4886879>,
  <1106353.38331876, 6410621.42505955>,
  <1106353.56384513, 6410621.32186505>,
  <1106353.7191267, 6410621.18356642>,
  <1106353.84244926, 6410621.01614353>,
  <1106353.92848048, 6410620.82683556>,
  <1106353.97350047, 6410620.62382798>,
  <1106353.97556262, 6410620.4158986>,
  <1106353.93457775, 6410620.21203807>,
  <1106326.59086787, 6410532.61125986>,
  <1106398.27332821, 6410509.79288614>,
  <1106398.4509681, 6410509.71673809>,
  <1106398.61064169, 6410509.60783997>,
  <1106398.74638446, 6410509.47025961>,
  <1106398.85312581, 6410509.30913623>,
  <1106398.92687847, 6410509.13048853>,
  <1106398.96488746, 6410508.94098979>,
  <1106398.96573297, 6410508.74771863>,
  <1106398.92938341, 6410508.55789459>,
  <1106388.89938341, 6410474.44789459>,
  <1106388.89928759, 6410474.44756891>,
  <1106377.34928759, 6410435.21756891>,
  <1106377.2754775, 6410435.03531775>,
  <1106377.16743182, 6410434.87103278>,
  <1106377.02932798, 6410434.73106584>,
  <1106376.86650553, 6410434.62082853>,
  <1106376.68525975, 6410434.54458298>,
  <1106376.49259822, 6410434.50527712>,
  <1106376.29596989, 6410434.50443065>,
  <1106376.10297708, 6410434.54207628>,
  <1106298.63039347, 6410457.75520538>,
  <1106268.55777153, 6410357.58246966>,
  <1106268.48271443, 6410357.40009019>,
  <1106268.37332342, 6410357.23598826>,
  <1106268.23385334, 6410357.09654674>,
  <1106268.06972901, 6410356.98718934>,
  <1106267.88733417, 6410356.9121696>,
  <1106267.69376324, 6410356.87440548>,
  <1106267.4965453, 6410356.87536583>,
  <1106267.30335131, 6410356.91501332>,
  <1106243.22335131, 6410364.39501332>,
  <1106243.04132665, 6410364.47200694>,
  <1106242.87799944, 6410364.58329584>,
  <1106242.73974941, 6410364.72453296>,
  <1106242.63197675, 6410364.89020146>,
  <1106242.55889116, 6410365.07383014>,
  <1106242.52334744, 6410365.26824629>,
  <1106242.52673396, 6410365.46585583>,
  <1106242.56891844, 6410365.65893992>,
  <1106275.2311035, 6410466.21053631>,
  <1106146.36811371, 6410504.07055218>

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
    translate <0, 0.08, 0>
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

prism { linear_spline 0, 0.01, 39,
/* casing of osm_id=12710990 */
  <1105721.86857533, 6410727.36265191>,
  <1105721.84090475, 6410727.55747137>,
  <1105721.85191942, 6410727.75393755>,
  <1105721.90119285, 6410727.94444322>,
  <1105721.98681715, 6410728.12161195>,
  <1105722.10547694, 6410728.27858373>,
  <1105722.25257767, 6410728.40928055>,
  <1105722.42242355, 6410728.50864179>,
  <1105738.37242355, 6410735.83864179>,
  <1105738.54410597, 6410735.89929672>,
  <1105738.72394072, 6410735.9278157>,
  <1105738.90596559, 6410735.92325323>,
  <1105739.08414576, 6410735.88576057>,
  <1105761.50414576, 6410728.98576057>,
  <1105761.68520657, 6410728.90987426>,
  <1105761.84795209, 6410728.80007605>,
  <1105761.98610982, 6410728.66059776>,
  <1105762.09435492, 6410728.49681514>,
  <1105762.16851543, 6410728.31504064>,
  <1105762.20573308, 6410728.12228017>,
  <1105762.20457343, 6410727.92596306>,
  <1105762.16508118, 6410727.73365572>,
  <1105762.08877843, 6410727.55277001>,
  <1105736.92877843, 6410681.22277001>,
  <1105736.81993254, 6410681.06187471>,
  <1105736.68203344, 6410680.92505889>,
  <1105736.52028474, 6410680.81748526>,
  <1105736.34078997, 6410680.74321309>,
  <1105736.15032232, 6410680.70504501>,
  <1105735.95606903, 6410680.70442129>,
  <1105735.76536021, 6410680.74136546>,
  <1105735.58539218, 6410680.81448344>,
  <1105735.42295601, 6410680.92101615>,
  <1105735.28418118, 6410681.0569436>,
  <1105735.1743043, 6410681.21713662>,
  <1105735.09747157, 6410681.39555035>,
  <1105726.62747157, 6410707.89555035>,
  <1105726.60857533, 6410707.96265191>,
  <1105721.86857533, 6410727.36265191>

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
    translate <0, 0.08, 0>
}


#declare texture_building_garages =
    texture {
        pigment {
            color rgb <0.8,0.8,0.8>
        }
        finish {
            diffuse 0.8
            ambient 0
        }
    }

#declare texture_building_yes =
    texture {
        pigment {
            color rgb <1,0.8,0.8>
        }
        finish {
            diffuse 0.8
            ambient 0
        }
    }

#declare texture_building_office =
    texture {
        pigment {
            color rgb <1,0.6,0.6>
        }
        finish {
            diffuse 0.8
            ambient 0
        }
    }

#declare texture_building_apartments =
    texture {
        pigment {
            color rgb <0.8,1,0.6>
        }
        finish {
            diffuse 0.8
            ambient 0
        }
    }

#declare texture_building_place_of_worship =
    texture {
        pigment {
            color rgb <1,1,0.6>
        }
        finish {
            diffuse 0.8
            ambient 0
        }
    }

#declare texture_building_hospital =
    texture {
        pigment {
            color rgb <1,0.6,0.6>
        }
        finish {
            diffuse 0.8
            ambient 0
        }
    }

#declare texture_building_university =
    texture {
        pigment {
            color rgb <0.6,1,0.8>
        }
        finish {
            diffuse 0.8
            ambient 0
        }
    }

#declare texture_building_theatre =
    texture {
        pigment {
            color rgb <1,0.6,1>
        }
        finish {
            diffuse 0.8
            ambient 0
        }
    }

#declare texture_building_parking =
    texture {
        pigment {
            color rgb <0.6,0.6,1>
        }
        finish {
            diffuse 0.8
            ambient 0
        }
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
texture { texture_building_place_of_worship }
    scale <1, 65.0, 1>
}

prism { linear_spline  0, 1, 5,
/* osm_id=62148686 */
  <1105589.93, 6410571.26>,
  <1105603.99, 6410571.47>,
  <1105604.09, 6410559.92>,
  <1105589.96, 6410559.93>,
  <1105589.93, 6410571.26>
texture { texture_building_place_of_worship }
    scale <1, 100.0, 1>
}

prism { linear_spline  0, 1, 5,
/* osm_id=62148687 */
  <1105589.88, 6410587.92>,
  <1105589.88, 6410598.47>,
  <1105604.37, 6410598.51>,
  <1105604.44, 6410587.96>,
  <1105589.88, 6410587.92>
texture { texture_building_place_of_worship }
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
texture { texture_building_yes }
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
texture { texture_building_yes }
    scale <1, 10.0, 1>
}

prism { linear_spline  0, 1, 11,
/* osm_id=24607339 */
  <1105905.02, 6409662.25>,
  <1106017.02, 6409739.77>,
  <1106022.53, 6409730.2>,
  <1106041.74, 6409742.37>,
  <1106077.82, 6409689.28>,
  <1106029.82, 6409656.92>,
  <1106034.44, 6409649.11>,
  <1106003.16, 6409628.83>,
  <1105995.69, 6409636.66>,
  <1105947.34, 6409600.38>,
  <1105905.02, 6409662.25>
texture { texture_building_university }
    scale <1, 10.0, 1>
}

prism { linear_spline  0, 1, 5,
/* osm_id=31251406 */
  <1106081.38, 6410862.47>,
  <1106232.58, 6410976.03>,
  <1106271.1, 6410924.73>,
  <1106119.9, 6410811.17>,
  <1106081.38, 6410862.47>
texture { texture_building_theatre }
    scale <1, 10.0, 1>
}

prism { linear_spline  0, 1, 18,
/* osm_id=-166012 */
  <1106092.39, 6410039.71>,
  <1106145.23, 6410049.71>,
  <1106159.32, 6410044>,
  <1106175.82, 6409961.96>,
  <1106190.39, 6409964.86>,
  <1106190.58, 6409968.81>,
  <1106205.16, 6409977.82>,
  <1106223.3, 6409972.91>,
  <1106252.89, 6409978.74>,
  <1106256.77, 6409960.65>,
  <1106112.43, 6409928.84>,
  <1106094.67, 6410022.29>,
  <1106092.39, 6410039.71>,
  <1106110.84, 6410019.62>,
  <1106122.3, 6409959.3>,
  <1106151.61, 6409964.87>,
  <1106140.15, 6410025.19>,
  <1106110.84, 6410019.62>
texture { texture_building_yes }
    scale <1, 10.0, 1>
}

prism { linear_spline  0, 1, 41,
/* osm_id=-7074 */
  <1106271.4, 6410369.77>,
  <1106274.79, 6410380.89>,
  <1106283.1, 6410408.08>,
  <1106298.36, 6410457.99>,
  <1106377.07, 6410433.93>,
  <1106389.45, 6410474.42>,
  <1106400.24, 6410509.68>,
  <1106326.29, 6410532.29>,
  <1106340.08, 6410577.42>,
  <1106353.24, 6410620.46>,
  <1106491.1, 6410578.33>,
  <1106414.03, 6410326.17>,
  <1106271.4, 6410369.77>,
  <1106418.77, 6410539.33>,
  <1106442.14, 6410532.19>,
  <1106446.07, 6410545.03>,
  <1106449.49, 6410556.23>,
  <1106426.12, 6410563.37>,
  <1106422.7, 6410552.17>,
  <1106418.77, 6410539.33>,
  <1106366.25, 6410368.61>,
  <1106390.42, 6410361.23>,
  <1106394.35, 6410374.1>,
  <1106398.16, 6410386.58>,
  <1106373.99, 6410393.98>,
  <1106370.18, 6410381.48>,
  <1106366.25, 6410368.61>,
  <1106358.19, 6410557.69>,
  <1106392.77, 6410547.12>,
  <1106396.73, 6410560.11>,
  <1106400.02, 6410570.85>,
  <1106365.44, 6410581.42>,
  <1106362.17, 6410570.68>,
  <1106358.19, 6410557.69>,
  <1106309.45, 6410386.65>,
  <1106343.93, 6410376.13>,
  <1106347.66, 6410388.36>,
  <1106351.75, 6410401.72>,
  <1106317.26, 6410412.24>,
  <1106313.19, 6410398.89>,
  <1106309.45, 6410386.65>
texture { texture_building_yes }
    scale <1, 23.0, 1>
}

prism { linear_spline  0, 1, 9,
/* osm_id=39986018 */
  <1106290.93, 6410645.9>,
  <1106300.27, 6410690.23>,
  <1106315.55, 6410687.01>,
  <1106311.53, 6410667.92>,
  <1106338.64, 6410662.21>,
  <1106342.56, 6410680.89>,
  <1106356.3, 6410677.99>,
  <1106347.05, 6410634.08>,
  <1106290.93, 6410645.9>
texture { texture_building_yes }
    scale <1, 10.0, 1>
}

prism { linear_spline  0, 1, 5,
/* osm_id=39986017 */
  <1106351.07, 6410849.04>,
  <1106400.48, 6410893.44>,
  <1106421.15, 6410870.44>,
  <1106371.74, 6410826.03>,
  <1106351.07, 6410849.04>
texture { texture_building_yes }
    scale <1, 10.0, 1>
}

prism { linear_spline  0, 1, 7,
/* osm_id=39986020 */
  <1106357.33, 6410633.38>,
  <1106361.17, 6410650.62>,
  <1106391.33, 6410643.91>,
  <1106397.13, 6410669.99>,
  <1106412.42, 6410666.59>,
  <1106402.78, 6410623.25>,
  <1106357.33, 6410633.38>
texture { texture_building_yes }
    scale <1, 10.0, 1>
}

prism { linear_spline  0, 1, 5,
/* osm_id=27754502 */
  <1106400.93, 6410846.05>,
  <1106432.9, 6410870.75>,
  <1106494.55, 6410790.96>,
  <1106462.58, 6410766.26>,
  <1106400.93, 6410846.05>
texture { texture_building_yes }
    scale <1, 10.0, 1>
}

