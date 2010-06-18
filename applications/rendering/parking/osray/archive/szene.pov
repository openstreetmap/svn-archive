global_settings {
   assumed_gamma 1.5
   noise_generator 2
   radiosity {
      count 1000
      error_bound 0.7
      recursion_limit 6
      pretrace_end 0.002
   }
}

box {
   <-50000, -0.5, -50000>, <50000, -0.0, 50000>
   
   pigment {
      color rgb <1, 1, 0.901961>
   }
   /*scale <10, 1e-06, 10>
   rotate <0, 0, 0>
   translate y*(-0.01)*/
}


/*992.498 4978.816,993.955 4980.02*/
sphere_sweep { cubic_spline, 10,
/* bbox */
  <992.498, 0, 4978.816>,0.01
  <992.498, 0, 4978.816>,0.01
  <992.498, 0, 4980.02>,0.01
  <992.498, 0, 4980.02>,0.01
  <993.955, 0, 4980.02>,0.01
  <993.955, 0, 4980.02>,0.01
  <993.955, 0, 4978.816>,0.01
  <993.955, 0, 4978.816>,0.01
  <992.498, 0, 4978.816>,0.01
  <992.498, 0, 4978.816>,0.01
  tolerance 0.001
   
   pigment {
      color rgb <0.7,0.7,1>
   }
   scale <1*1.0, 0.1*1.0, 1*1.0>
}

#include "pov_highways.pov"

sphere_sweep {
   cubic_spline,
   4,
   <0, 0, -2.0023>,0.1
   <0, 0, 0>,0.1
   <1, 0, 0>,0.1
   <1, 0, 1>,0.1
   tolerance 0.1
   
   pigment {
      color rgb <1, 1, 1>
   }
   scale <1, 0.1, 1>
}

sphere_sweep {
   cubic_spline,
   4,
   <0, 0, -2.0023>,0.12
   <0, 0, 0>,0.12
   <1, 0, 0>,0.12
   <1, 0, 1>,0.12
   tolerance 0.1
   
   pigment {
      color rgb <0.345098, 0.345098, 0.345098>
   }
   scale <1, 0.1, 1>
   translate y*(-0.01)
}

sphere_sweep {
   linear_spline,
   4,
   <0, 0, -2.0023>,0.1
   <0, 0, 0>,0.1
   <1, 0, 0>,0.1
   <1, 0, 1>,0.1
   tolerance 0.1
   
   pigment {
      color rgb <1, 1, 1>
   }
   scale <1, 0.1, 1>
   rotate y*90
}

sphere_sweep {
   linear_spline,
   4,
   <0, 0, -2.0023>,0.12
   <0, 0, 0>,0.12
   <1, 0, 0>,0.12
   <1, 0, 1>,0.12
   tolerance 0.1
   
   pigment {
      color rgb <0.345098, 0.345098, 0.345098>
   }
   scale <1, 0.1, 1>
   translate y*(-0.01)
   rotate y*90
}

box {
   <0.18226, 0, -0.49487>, <0.5, 0.5, -0.15661>
   
   finish {
      //*PMName Putz
      diffuse 0.6
      brilliance 1
      
      reflection {
         rgb <0, 0, 0>
      }
   }
   pigment {
      color rgb <1, 0, 0>
   }
   scale 1
   rotate <0, 0, 0>
   translate <0, 0, 0>
}

sphere {
   <-0.137002, 0.194382, 0.30642>, 0.161653
   
   finish {
      diffuse 0.6
      brilliance 1
      phong 0
      phong_size 40
      conserve_energy
      
      reflection {
         rgb <0.823529, 0.823529, 0.823529>
      }
   }
   
   pigment {
      color rgb <1, 1, 1>
   }
   scale 1
   rotate <0, 0, 0>
   translate <0, 0, 0>
}


/*
light_source {
   <4, 50000, -4>, rgb <1, 1, 1>
}
light_source {
   <1000, 10000, 4900>, rgb <1, 1, 1>
}

camera {
   orthographic
   /*location <0, 10, -0.5>*/
   location <993.1, 100, 4979.5-50>
   sky <0, 1, 0>
   direction <0, 0, 1>
   right <1.3333*1.3, 0, 0>
   up <0, 1*1.3*0.8, 0>
   look_at <993.1, 0, 4979.5>
}
*/