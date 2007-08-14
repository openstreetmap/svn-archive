<?php 

class placeindex {

  /* A class mapping directly to the database table of the same name,
     duplicates a subset of the information in class named, both in
     the fields it stores and that only true places 9rank > 0) are
     indexed. This means when we want to seach specifically for aplace
     we can quicklylocate it in this table and then use its id to
     retrieve the full detaqils from the named table, There are very
     many fewer places than nameds, and this makes for a major
     efficiency improvement.

     See named for definitions of fields, they are identical. 
     All fields present in database.
  */

  var $id;
  var $region;
  var $lat;
  var $lon;
  var $rank;    /* By definition, greater than zero only in this table */


  // --------------------------------------------------
  /* static */ function placeindexfromnamed($named) {
    /* A factory for placeindex records, derived from named */
    $placeindex = new placeindex();
    $placeindex->id = $named->id;
    $placeindex->region = $named->region;
    $placeindex->lat = $named->lat;
    $placeindex->lon = $named->lon;
    $placeindex->rank = $named->rank;    
    return $placeindex;
  }

  // --------------------------------------------------
  function findnearbyplaces($rank, $maxresults) {
    /* uses the place index to find the nearest place to this (excluding this, 
       of course). 

       rank: if non-zero only locates places for the particular rank
       given. Can also be an array of ranks in which case we restrict
       the search to any of those ranks

       maxresults: the maximum number of results to return.

       returns an array (possiby empty if no nearby places) of found placeindexes
    */

    global $db;
    include_once('region.php');

    $q = $db->query();

    /* the place sought must be in the same or neighbouring region as this */
    $region = new region($this->lat, $this->lon);
    $regionnumbers = $region->considerregions();
    $conditions = array();
    foreach ($regionnumbers as $regionnumber) { 
      $conditions[] = y_op::eq('region', $regionnumber); 
    }
    $conditions = count($conditions) == 1 ? $conditions[0] : y_op::oor($conditions);

    /* limit the search by rank if necessary */
    if (is_array($rank) || $rank > 0) {
      //not me, and only a place of given rank
      $conditions = y_op::aand(y_op::ne('id', $this->id), 
                              $conditions, 
                              y_op::eq('rank', $rank));
    }

    /* sort the result by increasing distance from this, and limit to max requested */
    $q->where($conditions);
    $q->ascending(canon::distancerestriction($this->lat, $this->lon));
    $q->limit($maxresults);

    $placeindex = new placeindex();
    $nearbyplaces = array();

    while ($q->select($placeindex) > 0) { $nearbyplaces[] = clone $placeindex; }
    return $nearbyplaces;
  }


}

?>
