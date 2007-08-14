<?php

/* Helper functions for managing regions. 

   Regions are areas of the earth's surface 1 degree of latitude high
   and 1 degree of longitude wide at the equator (rising to about 7.5
   degrees at 82 deg north) so that (a) there is an integral number of
   regions around each band of latitude (360 at the equator) and (b)
   each is approx 111km wide: rather like map tiles, but the vertical
   edges don't all line up.

   Each region is indexed by a number which is
   HIGH*1000+AROUND. AROUND is the index number of the region starting
   at Greenwich and moving east (so 0 is the first region east of
   Greenwich and at the quator 359 is te first west of Greenwich. Most
   importantly the langitudinal neighbours are +/- 1 modulo the number
   of regions in the band).

   HIGH is the whole number latitude in degrees rounded down to the
   nearest whole number of degrees away from the equator. Thus 0.3 N
   1.5 E will be HIGH=0 and AROUND=1 while 1.3 S 1.5 W will be HIGH=-2
   AROUND=358.

*/

class region {

  var $lat, $lon;

  // --------------------------------------------------
  /* constructor */ function region($lat, $lon) { $this->lat = $lat; $this->lon = $lon; }

  // --------------------------------------------------
  function considerregions() {
    /* for the lat/lon given in this, returns an array of the region
       number containing this and its immediate neighbours. A
       neighbour is any region which can be reached by stepping
       $fraction of a region in the direction of any of the eight
       primary compass points from the given point. In this way, if
       you are bang in the centre of a region, you only get hat
       region, but if you are close to a boundary, you get all the
       nearby regions. Remember that region boundaries are different
       at the next latitude up or down, so the east/west edges don't
       coincide */
    $fraction = 0.35;
    $partregionwidth = 360.0 / $this->countregionsatlat() * $fraction;
    $regionnumbers = array($this->regionnumber());
    $region = new region($this->lat + $fraction, $this->lon - $partregionwidth);
    $region->adjustrange();
    $regionnumbers[] = $region->regionnumber();
    $region = new region($this->lat + $fraction, $this->lon);
    $regionnumbers[] = $region->regionnumber();
    $region = new region($this->lat + $fraction, $this->lon + $partregionwidth);
    $region->adjustrange();
    $regionnumbers[] = $region->regionnumber();
    $region = new region($this->lat, $this->lon - $partregionwidth);
    $region->adjustrange();
    $regionnumbers[] = $region->regionnumber();
    $region = new region($this->lat, $this->lon + $partregionwidth);
    $region->adjustrange();
    $regionnumbers[] = $region->regionnumber();
    $region = new region($this->lat - $fraction, $this->lon - $partregionwidth);
    $region->adjustrange();
    $regionnumbers[] = $region->regionnumber();
    $region = new region($this->lat - $fraction, $this->lon);
    $regionnumbers[] = $region->regionnumber();
    $region = new region($this->lat - $fraction, $this->lon + $partregionwidth);
    $region->adjustrange();
    $regionnumbers[] = $region->regionnumber();
    return array_unique($regionnumbers);
  }

  // --------------------------------------------------
  /* private */ function adjustrange() {
    while ($this->lon >= 360.0) { $this->lon -= 360.0; }
    while ($this->lon < 0.0) { $this->lon += 360.0; }
  }

  // --------------------------------------------------
  /* private */ function regionnumber() {
    return $this->high() * 1000 + $this->around();
  }

  // --------------------------------------------------
  /* private */ function high() { return (int) floor($this->lat); }

  // --------------------------------------------------
  /* private */ function around() { return (int) $this->aroundfloat(); }

  // --------------------------------------------------
  /* private */ function aroundfloat() {
    if ($this->lon < 0) { $this->lon = 360 - $this->lon; }
    return ($this->lon / 360.0) * $this->countregionsatlat();
  }

  // --------------------------------------------------
  /* private */ function countregionsatlat() {
    static $regioncount;

    $high = $this->high();
    if (isset($regioncount[$high])) { return $regioncount[$high]; }

    static $earth_radius = 6336.0; /* km */
    static $approx_region_width = 111.0; /* km */
    $latitudinal_circumference = 2 * M_PI * sin(deg2rad($high+0.5)) * $earth_radius;
    $regioncount[$high] = (int) round($latitudinal_circumference / $approx_region_width);
    return $regioncount[$high];
  }

  // --------------------------------------------------
  /* private */   function fraction ($number) {
    $absnumber = abs($number);
    return $absnumber - floor($absnumber);
  }

}

?>