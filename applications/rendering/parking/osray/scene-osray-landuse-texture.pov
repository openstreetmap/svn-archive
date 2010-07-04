
global_settings {
    assumed_gamma 2.0
    noise_generator 2
}

camera {
   orthographic
   location <0, 10000, 0>
   sky <0, 1, 0>
   direction <0, 0, 1>
   right <1.0*20037508.3392, 0, 0>
   up <0, 1*20037508.3392*cos(radians(10)), 0> /* this stretches in y to compensate for the rotate below */
   look_at <0, 0, 0>
   rotate <-10,0,0>
   scale <1,1,1>
   translate <10018754.1696,0,-10018754.1696>
}

/* ground */
box {
    <0.0, -0.5, -20037508.3392>, <20037508.3392, -0.0, -7.08115455161e-10>
    pigment {
        color rgb <1, 1, 1>
    }
    finish {
        ambient 1
    }
}
