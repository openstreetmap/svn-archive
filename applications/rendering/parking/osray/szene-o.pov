global_settings {
   assumed_gamma 1.5
   noise_generator 2
}

box {
   <-0.5, -0.5, -0.5>, <0.5, 0.5, 0.5>
   
   pigment {
      color rgb <1, 1, 0.901961>
   }
   scale <10, 1e-06, 10>
   rotate <0, 0, 0>
   translate y*(-0.01)
}

sphere_sweep { cubic_spline, 9,
  <992.385728-992, 0, 4980.08884-4980>,0.02
  <992.385728-992, 0, 4980.08884-4980>,0.02
  <992.425991-992, 0, 4980.09932-4980>,0.02
  <992.455991-992, 0, 4980.05932-4980>,0.02
  <992.475991-992, 0, 4980.06932-4980>,0.02
  <992.475991-992, 0, 4980.04932-4980>,0.02
  <992.471661-992, 0, 4980.02694-4980>,0.02
  <992.504189-992, 0, 4980.00491-4980>,0.02
  <992.504189-992, 0, 4980.00491-4980>,0.02
  tolerance 0.001
   
   pigment {
      color rgb <1, 1, 1>
   }
   scale <1, 0.1, 1>
   translate y*(0.1)
}
sphere_sweep {
   linear_spline,
   4,
  <992.385728795-993, 0, 4980.08884629-4980>,0.03
  <992.420991286-993, 0, 4980.05932788-4980>,0.03
  <992.471661635-993, 0, 4980.02694497-4980>,0.03
  <992.504189632-993, 0, 4980.00491751-4980>,0.03
   tolerance 0.1
   
   pigment {
      color rgb <1, 1, 1>
   }
   scale <1, 0.1, 1>
}

/*
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
*/

box {
   <0.18226, 0, -0.49487>, <0.5, 0.5, -0.15661>
   
   finish {
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

light_source {
   <4, 5, -4>, rgb <1, 1, 1>
}

camera {
   orthographic
   location <0, 10, -0.5>
   sky <0, 1, 0>
   direction <0, 0, 1>
   right <1.3333, 0, 0>
   up <0, 1, 0>
   look_at <0, 0, 0>
}