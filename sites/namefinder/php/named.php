<?php 

class named {

  /* The main class of the application. A 'named' is an item on the
     map (way,segment or node) which has a name or something like a name
     (a road number reference or airport IATA code for example). Named maps to a 
     database table.

     The class is supplemented by placeindex, which duplicates some of
     the information here so is strictly speaking unnecessary. However
     when qualifying a search by place name is is moreefficient to
     search a small index of place names than a general index of
     everything.  */

  /* --- The following fields are stored in the database --- */
  var $id;       /* a number unique across all nameds, unlike the osm
                    id which is only unique within type. This id is
                    the osm id multiplied by 10 and then the type
                    inserted in the low order decimal digit (1 for
                    node, 2 for segement, 3 for way. It's done this
                    way to make it easy to see the correspondence when
                    seeing the number, and therefore to
                    debug. Conversion functions in canon.php */
  var $region;   /* A number identifying the region of the earth where
                    the point associated with named is. Allows us to
                    limit database searches geographically, and it is
                    this more than anything which makes the
                    application efficient. See region.php for =details
                    of how this number is computed from lat/lon */
  var $lat;      /* in the case of a way, position of one of the nodes
                    along it, determined by the indexing utility */
  var $lon;    
  var $name;     /* the name: but includes alternatives like the road
                    number in square brackets, e.g. "Main Street
                    [A1134]", and language equivalents, e.g. "London
                    [fr:Londres]" */
  var $category; /* The kind of item represented by the named - the
                    tag name of the main tag of the object, such as
                    'highway'or 'amenity'. This is used to ensure that
                    we keep nearby similarly named objects of
                    different kinds -otherwise we cull nearby duplicates */
  var $is_in;    /* A tidied up version of the is_in string coming from the
                    OSM data where supplied (usually for places only) */
  var $rank;     /* A number saying whether a named is a place and if
                    so how important it is: 0 for not a place, 10 for
                    hamlet, 20 for village and so on. The rank is used
                    particularly in determining the most sensible
                    contextual information to supply */
  var $info;     /* desription of the kind of named: basically a
                    version of the value part of the main tag
                    describing named, for example 'school', tidied up
                    for readability - we quote this, as in 'school
                    <name> found...'. Note it is _not_ used in
                    searches like 'schools near <somewhere>' - that
                    comes from comparing with canon above */
  
  /* --- the following fields are not stored in the database --- */
  var $place;    /* Any named we found has a reference to the named
                    which is the place we used as the context of the
                    search,if any. For example, in "Hinton Road,
                    Fulbourn", when named represents Hinton Road,
                    place would be the named for Fulbourn */
  var $distance; /* The distance in km to named referencing this one, if any. In the
                    above example, Fulbourn would say how far it is from its
                    referencing named Hinton Road */
  var $direction; /* Direction in degrees anticlockwise from east (the
                    x axis) _to_ the referencing named */
  var $placenearer; /* A named which is the nearest place to the the
                    named other than place (if one and the same, this
                    is not set). For example, "Hinton Road, Cambridge"
                    will find Fulbourn is the placennearer, but place
                    is what was requested in the search and is
                    Cambridge */
  var $nearesttown1; /* a named which is the nearest town to this named, which 
                    will be a place, if this named is not a town itself */
  var $nearesttown2; /* likewise, nearest city if not a city itself */
  var $description; /* The textual description of the result incuding all its context */
  var $zoom;     /* A suggested zoom level at which the resultmight be
                    displayed, accodng to the kind of named it is:
                    cities for example wil be well zoomed out, but
                    hotels are zoomed all the way in */

  // --------------------------------------------------
  function localdistancefrom($named) {
    /* this is the cartesian distance in a flat projection, so
       approximate and only valid locally. 111.0 is the equivalent of
       one degree of latitude (and one of longitude atthe equator) */
    $dlat = ($this->lat - $named->lat)*111.0;
    $dlon = ($this->lon - $named->lon)*sin(deg2rad(90.0-abs($named->lat)))*111.0;
    $this->distance = sqrt(pow($dlon,2) + pow($dlat,2));
    if (abs($dlon) < 0.0001) {
      $this->direction = $dlat > 0.0 ? 90 : 270;
    } else {
      $this->direction = (int)(round(rad2deg(atan($dlat/$dlon))));
      // gives +90 -> -90
      if ($dlon < 0) { $this->direction -= 180; }
      while ($this->direction < 0) { $this->direction += 360; }
    }
  }

  // --------------------------------------------------
  function approxkm() {
    /* it is more readable to give approximate distances between
       places, especially as the preciselocation of the centre of a town
       etc is unimportant, and a street has lots of possible locations
       and the one we chose to represent it is arbitrary */
    $distance = $this->distance;
    if ($distance < 0.9) { return 0; }
    if ($distance < 35.0) { 
      $distance = (int)floor($distance + 0.5);
    } else if ($distance < 75.0) {
      /* nearest 5km */
      $distance = 5 * (int)floor(($distance + 2.5)/5.0);
    } else {
      /* nearest 10km */
      $distance = 10 * (int)floor(($distance + 5.0)/10.0);
    }
    return $distance;
  }

  // --------------------------------------------------
  function approxdistance() {
    /* converts approxkm into words */
    $distance = $this->approxkm();
    return $distance == 0 ? 'less than 1km' : "about {$distance}km";
  }

  // --------------------------------------------------
  function findplace($otherthanplace=NULL, $rank=0) {
    /* uses the place index to find the nearest place to this (excluding this, 
       of course, if it is a place). Usually used via findnearestplace below.

       otherthanplace: a named which we wish to exclude from the search

       rank: if non-zero only locates places for the particular rank
       given. Can also be an array of ranks in which case we restrict
       the search to any of those ranks

       returns the named for the found place, or null if none found */

    global $db;
    include_once('placeindex.php');
    $placeindex = placeindex::placeindexfromnamed($this);

    /* limit the search to the two nearest places; we only want one,
       but the first may be 'otherthanplace' which we want to exclude,
       and it is more efficient to get two to start with than do
       another search if the a single result happens to be
       otherthanplace */
    $nearbyplaces = $placeindex->findnearbyplaces($rank, 2);

    foreach ($nearbyplaces as $placeindex) {
      if (! empty($otherthanplace) && $placeindex->id == $otherthanplace->id) { continue; }
      /* we found a place (probably nearer to the name we located than the one we were 
         searching for if any) */
      $named = new named();
      $named->id = $placeindex->id;
      if ($db->select($named) != 1) {
        $db->log("didn't find expected named from placeindex " . print_r($placeindex, 1));
        return NULL;
      }
      // $db->log("findplace: ".print_r($named,1));
      return $named;
    }
    return NULL;
  }

  // --------------------------------------------------
  function findseveralplaces($rank, $maxresults) {
    /* Like findplace above, but locates a series of nearby places (excluding 
       this, if it is a place) in order of distance from this.

       rank: if non-zero, limits the search to places of that rank, or
       if an array, any of those ranks given in the array

       maxresults: the maximum nmber of results returned

       returns: an array of nearby nameds for the places matched, which may be 
       empty if none found 
    */

    global $db;
    include_once('placeindex.php');
    $placeindex = placeindex::placeindexfromnamed($this);
    $nearbyplaces = $placeindex->findnearbyplaces($rank, $maxresults);
    foreach ($nearbyplaces as $placeindex) {
      $named = new named();
      $named->id = $placeindex->id;
      if ($db->select($named) != 1) {
        $db->log("didn't find expected named from placeindex " . print_r($placeindex, 1));
        return NULL;
      }
      $namedplaces[] = $named;
    }
    return $namedplaces;
  }

  // --------------------------------------------------
  function findnearestplace($otherthanplace=NULL, $rank=0) {
    /* findplace (q.v. for parameters) does the serious work; this function 
       just updates this with the result */
    $place =& $this->findplace($otherthanplace, $rank);
    if (! empty($place)) {
      $this->placenearer =& $place;
      $this->placenearer->localdistancefrom($this);
      $this->placenearer->assigncontext();
    }
  }

  // --------------------------------------------------
  function assigncontext() {
    /* Sets the nearesttown context for this */
    if ($this->rank == 0) { return; /* not a real place */}

    /* have we seen it before? Rather than look upeverything in the
       database,we keep a cache because the nearest place to A is also
       often the nearest place to to nearby place B */
    static $placecache = array();
    if (! empty($placecache[$this->id])) {
      if (! empty($placecache[$this->id]->nearesttown1)) { 
        $this->nearesttown1 =& $placecache[$this->id]->nearesttown1;
      }
      if (! empty($placecache[$this->id]->nearesttown2)) { 
        $this->nearesttown2 =& $placecache[$this->id]->nearesttown2;
      }
      return;
    }

    /* we want a city if this is a town or city, but for lesser places a 
       town and/or city */
    $ranktown = named::placerank('town');
    $rankcity = named::placerank('city');

    if ($this->rank <= $ranktown) {
      $place =& $this->findplace(NULL, array($ranktown, $rankcity));
      if (! empty($place)) { 
        $place->localdistancefrom($this); 
        if ($place->rank == $ranktown) {
          $this->nearesttown1 =& $place;
          $this->nearesttown2 =& $this->findplace(NULL, $rankcity);
          if (! empty($this->nearesttown2)) { $this->nearesttown2->localdistancefrom($this); }
        } else {
          $this->nearesttown1 =& $place;
        }
      }
    } else if ($this->rank == $rankcity) {
      $this->nearesttown1 =& $this->findplace(NULL, $rankcity);
      if (! empty($this->nearesttown1)) { 
        $this->nearesttown1->localdistancefrom($this); 
      } else {
        /* din't find a city; is there a nearby town? */
        $this->nearesttown1 =& $this->findplace(NULL, $ranktown);
        if (! empty($this->nearesttown1)) { 
          $this->nearesttown1->localdistancefrom($this); 
        }
      }
    }

    $placecache[$this->id] =& $this;
  }

  //--------------------------------------------------
  /* static */ function getplacerankings() {
    /* space allowed for expansion in between existing features */
    static $placerankings = array(
      'hamlet'=> 10, 'village'=>20, 'suburb'=>30, 
      'town'=>50, 'small town' => 50, 
      'city'=>60, 'metropolis' => 70);
    return $placerankings;
  }

  // --------------------------------------------------
  /* static */ function placerank($type) {
    static $placerankings = NULL;
    if (is_null($placerankings)) { $placerankings = named::getplacerankings(); }
    return empty($placerankings[$type]) ? 0 : $placerankings[$type];
  }

  // --------------------------------------------------
  function isolatedplaceneighbourranks() {
    static $placerankings = NULL;
    if (is_null($placerankings)) { $placerankings = named::getplacerankings(); }
    switch ($this->rank) {
    case $placerankings['village']:
    case $placerankings['hamlet']:
    default:
      return array($placerankings['village'], $placerankings['town'], $placerankings['city']);
    case $placerankings['suburb']:
      return array($placerankings['suburb'], $placerankings['town'], $placerankings['city']);
    case $placerankings['town']:
    case $placerankings['city']:
      return array($placerankings['town'], $placerankings['city']);
    }
  }

  // --------------------------------------------------
  function tidyupisin($resetisin=FALSE) {
    /* is_in in osm is a bit untidy, with random spaces or not; just make it a bit tidier 
       returns: the improved string for this's is_in
     */
    static $previousisin = '';
    if ($resetisin) { $previousisin = ''; }
    if (empty($this->is_in)) { return ''; }
    $isin = str_replace(';', ',', $this->is_in);
    $explosion = explode(',', $isin);
    $isin = '';
    $prefix = '';
    for($i = 0; $i < count($explosion); $i++) {
      $term = trim($explosion[$i]);
      if ($term == '') { continue; }
      $isin .= $prefix . strtoupper($term{0}) . substr($term,1);
      $prefix = ', ';
    }
    $isin = preg_replace('/\\,? *capital cities */i', '', $isin);
    if ($isin == $previousisin) { return ', ditto'; }
    $previousisin = $isin;
    return " in {$isin}";
  }

  // --------------------------------------------------
  /* static */ function lookupplaces($name, $latlon=NULL, $exact=FALSE) {
    /* Returns as an array all places (that is, nameds with non-zero
       rank) in the database which canonically match the non-canonical
       name given

       Can be constrained by a lat/lon bounding box - an array of 4 numbers south-west 
       lat,lon and north-east lat,lon, if latlon is non-null
    */
    global $db;
    include_once('canon.php');
    $places = array();
    $q = $db->query();
    $ands = array();
    if (! empty($latlon) && is_array($latlon) && count($latlon) == 4) {
      $ands[] = y_op::gt('lat',$latlon[0]);
      $ands[] = y_op::gt('lon',$latlon[1]);
      $ands[] = y_op::lt('lat',$latlon[2]);
      $ands[] = y_op::lt('lon',$latlon[3]);
    }
    $canonstrings = canon::canonical_with_synonym($name);
    if (empty($canonstrings)) { return $places; /* empty array */ }
    $ands[] = canon::likecanon($canonstrings, $exact);
    $ands[] = y_op::gt('rank',0);
    $q->where(y_op::aand($ands));

    $placecandidate = new named();
    $canon = new canon();
    while ($q->selectjoin($placecandidate, $canon) > 0) {
      $places[] = clone $placecandidate;
    }
    return $places;
  }

  /* ================================================== 
     The following set of functions is used to derive a readable
     description of the location of this in relation to nearby places */

  // --------------------------------------------------
  function describebasic($resetisin=FALSE) {
    /* Builds the basic elements of this's description, returning the
       result as a string resetisin: when TRUE avoids putting 'ditto'
       when is_in is the same as the most recent one, so the first
       time in each result of a search we would need that */
    $info = $this->info;
    if ($info == 'airport; airport') { $info = 'airport'; /* until I can fix it in the index */}
    $infohtml = htmlspecialchars($info, ENT_QUOTES, 'UTF-8');
    $namehtml = htmlspecialchars($this->name, ENT_QUOTES, 'UTF-8');
    $isinhtml = htmlspecialchars($this->info, ENT_QUOTES, 'UTF-8');
    $isinhtml = htmlspecialchars($this->tidyupisin($resetisin), ENT_QUOTES, 'UTF-8');
    return "{$infohtml} &lt;strong&gt;{$namehtml}&lt;/strong&gt;{$isinhtml}";
  }

  // --------------------------------------------------
  function describedistancefrom() {
    /* Converts exact distance and direction in degrees of this to its
       referencing named to an approximate distance and compass point
       (_from_ the referening named), for example "less than 1km east
       of the middle of" or "20km south-west of".  Returns a string */
    $angle = $this->direction;
    if ($angle <= 20 || $angle >= 340) { $direction = 'west'; }
    else if ($angle <= 70) { $direction = 'south-west'; }
    else if ($angle <= 110) { $direction = 'south'; }
    else if ($angle <= 160) { $direction = 'south-east'; }
    else if ($angle <= 200) { $direction = 'east'; }
    else if ($angle <= 250) { $direction = 'north-east'; }
    else if ($angle <= 290) { $direction = 'north'; }
    else { $direction = 'north-west'; }

    $approxdistance = $this->approxdistance();
    return $approxdistance . ' ' . $direction . 
      ($this->distance < 3.0 && $this->name != '' ? ' of middle of' : ' of');
  }

  // --------------------------------------------------
  function describeincontext($resetisin=FALSE) {
    /* Builds the complete contextual string for this from the above building blocks */
    $s = $this->describebasic($resetisin);
    $prefix = ' (which is ';
    $andprefix = ' and ';
    if (isset($this->nearesttown1)) {
      $s .= $prefix . $this->nearesttown1->describedistancefrom() . ' ' . 
        $this->nearesttown1->describebasic();
      $prefix = $andprefix;
    }
    if (isset($this->nearesttown2)) {
      $s .= $prefix . $this->nearesttown2->describedistancefrom() . ' ' . 
        $this->nearesttown2->describebasic();
      $prefix = $andprefix;
    }
    if ($prefix == $andprefix) { $s .= ')'; }
    return $s;
  }

  // --------------------------------------------------
  function contextcontains($place) {
    /* returns a boolean indicating whether the named's place (that
     is, the one the user was using as context to limit the search) is
     the same as either of the nearesttown fields - so we don't need
     to mention it twice in the description */
    if (empty($place)) { return FALSE; }
    if (! empty($this->nearesttown1) && $this->nearesttown1->id == $place->id) { return TRUE; }
    if (! empty($this->nearesttown2) && $this->nearesttown2->id == $place->id) { return TRUE; }
    return FALSE;
  }

  // --------------------------------------------------
  function assigndescription($placerequested) {
    /* Uses the building blocks above to set the description field and also zoom of this. 
       Returns nothing */
    $prefix = '';
    $s = $this->describeincontext(TRUE) . ' found ';
    if (isset($this->placenearer) && 
        ! $this->contextcontains($this->placenearer) && 
       $this->placenearer->id != $this->id) 
    {
      $s .= $this->placenearer->describedistancefrom() . ' ' . 
            $this->placenearer->describeincontext();
      $prefix = ' and ';
    }
    if (is_object($this->place) && 
        ! $this->contextcontains($this->place))
    {
      $s .= $prefix . 
            $this->place->describedistancefrom() . ' ' . 
            $this->place->describeincontext();
    }

    $this->description = $s;

    $zoomlevels = array(
                        'default'=>16,
                        'water'=>13,
                        'school'=>17,
                        'university'=>17,
                        'college'=>17,
                        'cinema'=>17,
                        'theatre'=>17,
                        'hotel'=>17,
                        'parking'=>17,
                        'supermarket'=>17,
                        'hospital'=>17,
                        'doctors'=>17,
                        'pharmacy'=>17,
                        'requested location'=>12,
                        'country'=>5,
                        'pub'=>17,
                        'airport'=>14,
                        'airport; airport'=>14,
                        'city'=>10,
                        'town'=>11,
                        'village'=>13,
                        'hamlet'=>14,
                        'suburb'=>13);
    $this->zoom = $zoomlevels[empty($zoomlevels[$this->info]) ? 'default' : $this->info];
  }

  // --------------------------------------------------
  function getosmid(&$type) { 
    /* Helper function to get the type and id of this */
    include_once('canon.php');
    return canon::getosmid($this->id, $type);
  }

  // --------------------------------------------------
  function xmlise() {
    /* Converts this to an equivalent xml element, which is
       returned. Recurses to deal with subordinate nameds making up
       the context of a search result */
    $xml = '<named';
    $id = $this->getosmid($type);
    $xml .= sprintf(" type='%s' id='%d' lat='%f' lon='%f' name='%s' category='%s' rank='%d' region='%d'",
                  $type, 
                  $id, 
                  $this->lat, 
                  $this->lon, 
                  htmlspecialchars($this->name, ENT_QUOTES, 'UTF-8'),
                  htmlspecialchars($this->category, ENT_QUOTES, 'UTF-8'),
                  $this->rank, 
                  $this->region
    );
    if (! empty($this->is_in)) { 
      $xml .= " is_in='".htmlspecialchars($this->tidyupisin(TRUE), ENT_QUOTES, 'UTF-8')."'"; }
    if (! empty($this->info)) { 
      $xml .= " info='".htmlspecialchars($this->info, ENT_QUOTES, 'UTF-8')."'"; }
    if (! empty($this->distance)) { 
      $xml .= sprintf(" distance='%f' approxdistance='%d'", $this->distance, $this->approxkm());
    }
    if (! empty($this->direction)) { 
      $xml .= sprintf(" direction='%s'", htmlspecialchars($this->direction, ENT_QUOTES, 'UTF-8'));
    }
    if (! empty($this->zoom)) {
      $xml .= " zoom='{$this->zoom}'";
    }
    $xml .= ">\n";
    if (! empty($this->description)) {
      // already htmlspecialchar
      $xml .= "<description>{$this->description}</description>\n";
    }
    if (! empty($this->place)) {
      $xml .= "<place>\n".$this->place->xmlise()."</place>\n";
    }
    if (! empty($this->nearesttown1) || ! empty($this->nearesttown2)) {
      $xml .= "<nearestplaces>\n";
      if (! empty($this->nearesttown1)) { $xml .= $this->nearesttown1->xmlise(); }
      if (! empty($this->nearesttown2)) { $xml .= $this->nearesttown2->xmlise(); }
      $xml .= "</nearestplaces>\n";
    } else if (!empty($this->placenearer)) {
      $xml .= "<nearestplaces>\n" . $this->placenearer->xmlise() . "</nearestplaces>\n";
    }
    $xml .= "</named>\n";
    return $xml;
  }

}

?>
