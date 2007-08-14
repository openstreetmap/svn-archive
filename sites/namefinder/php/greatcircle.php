<?php 

include_once('named.php');

class greatcircle {

  /* This class provides helper functions for computing great circle distances */

  var $fromid;
  var $toid;
  var $distance;

  // --------------------------------------------------
  /* static */ function earth_radius() {
    return 6372.795;
  }

  // --------------------------------------------------
  /* constructor */ function greatcircle($named1, $named2) {
    /* Calculates the great circle distance between two objects of class named - 
       see http://en.wikipedia.org/wiki/Great-circle_distance */
    $lat1 = deg2rad($named1->lat); $lat2 = deg2rad($named2->lat);
    $lon1 = deg2rad($named1->lon); $lon2 = deg2rad($named2->lon);
    $dlon = $lon2 - $lon1;
    $this->distance =
      greatcircle::earth_radius() *
      atan2(
            sqrt(
                 pow(cos($lat2) * sin($dlon), 2) +
                 pow(cos($lat1) * sin($lat2) - sin($lat1) * cos($lat2) * cos($dlon), 2)
            )
            ,
            sin($lat1) * sin($lat2) + 
            cos($lat1) * cos($lat2) * cos($dlon)
      );
    $this->fromid = $named1->id;
    $this->toid = $named2->id;
  }

  // --------------------------------------------------
  function xmlise() {
    $fromid = canon::getosmid($this->fromid, $fromtype);
    $toid = canon::getosmid($this->toid, $totype);
    return sprintf(" <distance fromtype='%s' from='%d' totype='%s' to='%d'>%0.1f</distance>\n",
                   $fromtype, $fromid, $totype, $toid, $this->distance);
  }
}

?>
