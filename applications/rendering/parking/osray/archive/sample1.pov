  camera {
    orthographic
    up <0, 5, 0>
    right <5, 0, 0>
    location <0, 0, -25>
    look_at <0, 0, 0>
  }
  plane {
    -z, 0
    pigment { rgb <1/255, 0, 0.5> }
    finish { ambient 1 }
  }
  box {
    <-2.3, -1.8, -0.2>, <2.3, 1.8, -0.2>
    pigment { rgb <0/255, 0, 1> }
    finish { ambient 1 }
  }
  box {
    <-1.95, -1.3, -0.4>, <1.95, 1.3, -0.3>
    pigment { rgb <2/255, 0.5, 0.5> }
    finish { ambient 1 }
  }
  text {
    ttf "crystal.ttf", "The vision", 0.1, 0
    scale <0.7, 1, 1>
    translate <-1.8, 0.25, -0.5>
    pigment { rgb <3/255, 1, 1> }
    finish { ambient 1 }
  }
  text {
    ttf "crystal.ttf", "Persists!", 0.1, 0
    scale <0.7, 1, 1>
    translate <-1.5, -1, -0.5>
    pigment { rgb <3/255, 1, 1> }
    finish { ambient 1 }
  }