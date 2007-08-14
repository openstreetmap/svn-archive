<?php

class search {

  /* The common point of call for doing searches. Call search::xmlise(). */

  // --------------------------------------------------
  /* static */ function xmlise($find, $maxresults) {
    /* Given a search string, returns a string of complete xml describing 
       the matches found for that string 

       find: the search string from the user,
         search ::- nameword [nameword ...] ( ("near" | ",") placeword [placeword ...] 
             ("," isinword )?  )? 
         nameword ::- ( word | lat "," lon)
         placeword ::- ( word | lat "," lon)
         find ::- search ( ":" search )?
         
         The colon form allows great circle distance between two results to be calculated
         Experimentally, the namewords can also form a UK postcode with no qualifications
         Comments are allowed in the string between square brackets

       maxresults; the maximum numberof results to return in the XML

       returns: a string, formatted as XML
    */
    
    include_once('postcodelookup.php');
    global $db;
    include_once('options.php');

    /* oxml is the return string. Put admin stuff in the top level element */
    $oxml = '<' . '?' . 'xml version="1.0" encoding="UTF-8"' . '?'. '>' . "\n";
    $oxml .= "<searchresults find='" . htmlspecialchars($find, ENT_QUOTES, 'UTF-8') . "'";
    $indexdate = options::getoption('indexdate');
    if ($indexdate == '') {
      $oxml .= " error='updating index, temporarily unavailable'>\n</searchresults>\n";
      return $oxml;
    }
    $oxml .= " sourcedate='".
      htmlspecialchars($indexdate, ENT_QUOTES, 'UTF-8')."'";
    $oxml .= " date='".date('Y-m-d H:i:s')."'";

    /* remove comments in square brackets from input - these are comments */
    $find = trim(preg_replace('/\\[.*\\]/', '', $find));

    /* does it look like a postcode? */
    $postcodelookup = postcodelookup::postcodelookupfactory($find);
    $originalpostcode = $find;
    $isapostcode = $postcodelookup->get_query($find);

    $finds = explode(':', $find);
    if (count($finds) > 2) { 
      $oxml .= " error='too many colons'>\n</searchresults>\n";
      return $oxml;
    }
    for($i = 0; $i < count($finds); $i++) { $finds[$i] = trim($finds[$i]); }
    /* no closing > yet: adding find data to header and possibly errors */

    $oxml .= " distancesearch='" . (count($finds) > 1 ? 'yes' : 'no') . "'";

    $near = NULL; // isset($_GET['near']) ? $_GET['near'] : NULL;
    $multinameds = array();

    for($i = 0; $i < count($finds); $i++) {
      /* that is, for each component in a search separated by colons, but often only once... */
      $thisfind = search::explodeterms($finds[$i]);
      
      /* the heart of the search - see below */
      $nameds = search::find($thisfind, $maxresults);

      /* reflect the original search data back in the xml */
      $ks = count($finds) > 1 ? $i+1 : '';
      if (count($thisfind) >= 1) {
        $oxml .= " findname{$ks}='" . htmlspecialchars($thisfind[0], ENT_QUOTES, 'UTF-8')."'";
        if (count($thisfind) >= 2) {
          $oxml .= " findplace{$ks}='" . htmlspecialchars($thisfind[1], ENT_QUOTES, 'UTF-8')."'";
          if (count($thisfind) == 3) {
            $oxml .= " findisin{$ks}='" . htmlspecialchars($thisfind[2], ENT_QUOTES, 'UTF-8')."'";
          }
        }
      }

      /* if find() returned an error message rather than an array of results, try again,
         dropping any qualifying is_in term, because places often don't include them */
      if (is_string($nameds) && ! $isapostcode) {
        if (count($thisfind) == 2) {
          $thisfind = array_merge(array($thisfind[0]), $thisfind);
          $nameds = search::find($thisfind, $maxresults);
        }
      } 

      /* if still nothing, report it */
      if (is_string($nameds)) {
        $oxml .= " error='place not found'>\n</searchresults>\n";
        return $oxml;
      }

      if (count($nameds) == 0) {
        if ($isapostcode) {
          $oxml .= " error='name not found for postcode'>\n</searchresults>\n";
        } else {
          $oxml .= " error='name not found'>\n</searchresults>\n";
        }
        return $oxml;
      }

      /* foundnearplace indicates whether the nearest place was the qualifying one or 
         whether there was another place closer */
      $foundnearplace = ! empty($nameds[0]->place);
      $oxml .= " foundnearplace{$ks}='" . ($foundnearplace ? 'yes' : 'no') . "'";

      /* reflect any postcode requested in the xml */
      if ($isapostcode) { $oxml .= " postcode='{$originalpostcode}'"; }

      /* keep a not of the result for debugging */
      $db->log("result: ".print_r($nameds,1));
      $multinameds[] = $nameds;
    }

    $oxml .= ">\n";
    $xml = '';

    if (count($multinameds) == 1) {
      /* the usual case */
      foreach($nameds as $named) { $xml .= $named->xmlise(); }
    } else {
      /* the colon case: so now compute the great circle distances for each combination 
         of the fist 3 results in each side of the colon  */
      include_once('greatcircle.php');
      $gcs = array();
      for ($i0 = 0; $i0 < min(count($multinameds[0]), 3); $i0++) {
        $output0 = FALSE;
        if ($multinameds[0][$i0]->category != 'place') { break; }
        for ($i1 = 0; $i1 < min(count($multinameds[1]), 3); $i1++) {
          if ($multinameds[1][$i1]->category != 'place') { break; }
          if (! $output0) { 
            $xml .= $multinameds[0][$i0]->xmlise();
            $output0 = TRUE;
          }
          $xml .= $multinameds[1][$i1]->xmlise();
          $gcs[] = new greatcircle($multinameds[0][$i0], $multinameds[1][$i1]);
        }
      }
      for ($i0 = 0; $i0 < min(count($multinameds[0]), 3); $i0++) {
        $output0 = FALSE;
        if ($multinameds[0][$i0]->category == 'place') { continue; }
        for ($i1 = 0; $i1 < min(count($multinameds[1]), 3); $i1++) {
          if ($multinameds[1][$i1]->category == 'place') { continue; }
          if (! $output0) { 
            $xml .= $multinameds[0][$i0]->xmlise();
            $output0 = TRUE;
          }
          $xml .= $multinameds[1][$i1]->xmlise();
          $gcs[] = new greatcircle($multinameds[0][$i0], $multinameds[1][$i1]);
        }
      }
      foreach ($gcs as $gc) { $xml .= $gc->xmlise(); }
    }

    /* and that's it... */
    return $oxml . $xml . "</searchresults>\n";
  }

  // --------------------------------------------------
  /* static */ function find(&$terms, $maxresults) {
    /* Given a search string, returns an array of named's which are the matches 
       for the given search string. Usually this will be called from xmlise rather 
       than directly.

       terms: an array of strings as follows:
         (1) terms[0]: name of something to look for (including references like road numbers 
         and IATA codes, non-native versions of a name (e.g. Londres), or generic things 
         like "school" or "hotels" (singular or plural)

         or

         (2) terms[0] as above, and

         terms[1]: qualifying place name so that terms[0]
         must be found close to (an instance of - there may be more
         than one match) this place.

         or 

         (3) terms[0] and terms[1] as above, and 

         terms[2]: if given, a further qualifying string, which must appear in
         the is_in of the qualyfying place. For example, there are
         multiple Cambridges, so by setting this to UK, the Cambridge
         with UK in its is_in will be used. (Actually there are two
         Cambridges in the UK, so Cambridgeshire might be more
         appropriate in this case)
 
         or

         (4) terms[0] and terms[1] are a lat and lon respectively - just asking 'where am I?'

         or

         (5) terms[0] is a name etc. as case 1, and terms[1] and terms[2] are lat and lon 
         respectively restricting the search to near to that location

         Note that postcode searches are converted toname searches
         before being presented to the find function. find only deals
         with names and lat/lon pairs

       maxresults: the maximum number of results to return.  

       returns: an array of named obects - see class named for details 
         or a string which is an error message
    */

    global $db, $config;

    include_once('canon.php');
    include_once('named.php');
    include_once('region.php');

    /* toofars controls what "nearby" means for a place. For example a
       hamlet is only "near" somewhere if it is within 8km */
    $toofars = array(0=>10.0, 
                     named::placerank('hamlet')=>8.0,
                     named::placerank('village')=>20.0,
                     named::placerank('suburb')=>20.0,
                     named::placerank('airport')=>20.0,
                     named::placerank('town')=>25.0,
                     named::placerank('city')=>45.0);

    $places = array(); /* the places, if any, which should qualifty the search */
    $nameds = array(); /* the result */

    $nterms = count($terms);

    if ($nterms > 2 && search::islatlon($terms[1], $terms[2], $pseudoplace)) {
      /* case 5 above: reduce the lat/lon to a named, one of the places to qualify 
         the search, and remove them from the list */
      array_splice($terms, 1, 2);
      $nterms -=2;
      $places[] = clone $pseudoplace;
    } else if ($nterms > 1) {
      if (search::islatlon($terms[0], $terms[1], $pseudoplace)) {
        /* case 4 above, simply a 'where am i' type of query on
           lat,lon, so the result is the artifical named for that
           lat/lon - but of course we need to get its context later,
           which is the whole point */
        $terms = array();
        $pseudoplace->findnearestplace();
        $pseudoplace->assigndescription($nterms > 1);
        $nameds[] = clone $pseudoplace; 
      } else {
        /* case 2 or 3 above: search is qualified. Find any places of the name given as 
           the second term  */
        $places = array_merge($places, named::lookupplaces($terms[1]));
        if (count($places) == 0) { return "I can't find {$terms[1]}"; }

        // $db->log ("found places " . print_r($places, 1));

        /* cull the possible places according to given qualifying is_in in case 3*/
        $placeisin = $nterms > 2 ? array_slice($terms, 2) : array();
        if (! empty($placeisin)) {
          foreach($placeisin as $isin) {
            $isin = canon::canonical($isin);
            for ($i = 0; $i < count($places); $i++) {
              $sourceisin = canon::canonical($places[$i]->is_in);
              if (strpos($sourceisin, $isin) === FALSE) {
                array_splice($places, $i, 1);
                break 2;
              }
            }
          }
          // $db->log ("places after cull " . print_r($places, 1));
        }

        if (count($places) == 0) { 
          /* nothing left, so say so */
          $isin = '';
          $prefix = '';
          for ($i = 2; $i < count($terms); $i++) { 
            $isin = "{$prefix}{$terms[$i]}";
            $prefix = ', ';
          }
          $unfoundplace = "{$terms[1]} not found";
          if (! empty($isin)) { $unfoundplace .= " in {$isin}"; }
        }
      }
    }

    /* so, we've got so far a list of places,possibly empty, near
       which we must search (which may have come from a lat/lon or a
       name), and maybe a result already from a simple lat/lon for
       which we only require context */


    /* special cases for some plural objects: search on churches near
       ... => church near; and for place like things, limit search
       only to places (rank > 0) rather than including streets named
       'Somewhere Place' etc */
    switch ($terms[0]) {
    case 'churches':
      $terms[0] = 'church'; 
      break;
    case 'cities':
      $terms[0] = 'city'; // and fall through
    case 'towns':
    case 'suburbs':
    case 'villages':
    case 'hamlets':
    case 'places':
      $placesonly = TRUE;
      break;
    }

    /* Work out canonical forms of the first search term (the road name or whatever) to 
       try matching against equivalents in the database. There's more than one because 
       Hinton Road becomes Hinton Rd as well, and so on */

    $names = canon::canonical_with_synonym($terms[0]);

    if (count($places) > 0) {
      /* There are qualifying places. 

         SELECT * FROM named WHERE (region=n0 [or region=n1 or ...]) 
         ORDER BY ((lat - latplace)^2 + (lon - lonplace)^2 asc  */

      foreach ($places as $place) {
        $place->assigncontext(); // nearest more important place(s)

        /* find occurences of the name ordered by distance from the place, 
           for each of the places we found */
        $q = $db->query();
        $ands = array();
        $ands[] = canon::likecanon($names);
        $region = new region($place->lat, $place->lon);
        $regionnumbers = $region->considerregions();
        $regionors = array();
        foreach ($regionnumbers as $regionnumber) {
          $regionors[] = y_op::eq('region', $regionnumber);
        }
        $ands[] = count($regionors) == 1 ? $regionors[0] : y_op::oor($regionors);
        if (! empty($placesonly)) { $ands[] = y_op::gt('rank', 0); }

        $q->where(y_op::aand($ands));
        $q->ascending(canon::distancerestriction($place->lat, $place->lon));
        $q->limit($maxresults);

        $named = new named();
        
        $toofar = empty($toofars[$place->rank]) ? $toofars[0]: $toofars[$place->rank];

        while ($q->select($named) > 0) { 

          $named->place = clone $place;
          $named->place->localdistancefrom($named);

          if ($named->place->distance > $toofar) { break; } // everywhere else is further too

          unset($named->placenearer);
          $named->findnearestplace(/* other than... */ $place);
          if (! empty($named->placenearer) && 
              $named->place->distance < $named->placenearer->distance) 
          {
            unset($named->placenearer);
          }
          $named->assigndescription($nterms > 1);
          $nameds[] = clone $named; 
        }

        // $db->log ("found names near those places " . print_r($nameds, 1));        
      }
    }

    if (count($nameds) == 0) {
      /* Either no qualifying place, or no name found near given place. If there was a
         qualifying place try general search for name anyway: "but I
         did find one near..." 

         In this case we have no place to order by distance from, so
         instead do a partial ordering so that exact matches on the
         name (or one of its abbreviated variants) come first and then
         partial matches. For example, "Fulbourn" would come before
         "Fulbourn Post Office" when searching for "Fulbourn". We do
         this by going round the loop twice, relaxing the exactness
         condition on the second time round */
      $limit = $maxresults;
      $exact = TRUE; 
      for ($i = 0; $i < 2 && $limit > 0; $i++) {
        $q = $db->query();
        $q->limit($limit);
        $condition = canon::likecanon($names, $exact);
        if (! $exact) {
          $condition = y_op::aand($condition, y_op::not(canon::likecanon($names, TRUE)));
        }
        // $condition = y_op::aand($condition, y_op::le('rank',named::placerank('city')));
        $q->where($condition);
        /* prioritise places, and those in order of importance, cities first */
        $q->descending('rank');
        $named= new named();
        while ($q->select($named) > 0) { 
          $namedclone = clone $named;
          if ($namedclone->rank > 0) {
            $namedclone->assigncontext();
            $namedclone->findnearestplace($namedclone, $namedclone->isolatedplaceneighbourranks());
          } else {
            $namedclone->findnearestplace();
          }
          if (isset($unfoundplace)) { $namedclone->place = $unfoundplace; }
          $namedclone->assigndescription($nterms > 1);
          $nameds[] = $namedclone;
        }
        $limit -= count($nameds);
        $exact = FALSE;
      }

      // $db->log ("found names near other places " . print_r($nameds, 1));        
    }

    /* cull duplicate responses. These are usually because it found the name near more than 
       one place which matched the place name criterion */
    $namedsunique = array();
    foreach ($nameds as $named) {
      if (! array_key_exists( $named->id, $namedsunique)) {
        $namedsunique[$named->id] = $named;
      }
    }

    return array_values($namedsunique);
  }

  // --------------------------------------------------
  /* static */ function explodeterms($terms) {
    /* Helper function to expand a complete search string into an array of terms 
       suitable for the find function above */
    $terms = str_replace(' near ',',',$terms);
    $terms = explode(',', $terms);
    for ($i = 0; $i < count($terms); $i++) { $terms[$i] = trim($terms[$i]); }
    return $terms;
  }

  // --------------------------------------------------
  /* private static */ function islatlon($term1, $term2, &$pseudoplace) {
    /* Returns a boolean according to whether term1 and term2 (both
       strings, separate because the comma betwen them caused them to
       be separated), are both decimal numbers, and therefore together
       form a latituelongitude pair. If s, constructs and returns in
       $pseudoplace an anonymous, artificial named which is located
       atthe lat/lon determined */
    static $anonid = 0;
    if (preg_match('/^-?([0-9]+|[0-9]*\\.[0-9]+)$/', $term1) &&
        preg_match('/^-?([0-9]+|[0-9]*\\.[0-9]+)$/', $term2)) {
      $lat = (double)$term1;
      $lon = (double)$term2;
      $pseudoplace = new named();
      $pseudoplace->category = 'place';
      $pseudoplace->lat = $lat;
      $pseudoplace->lon = $lon;
      $pseudoplace->name = '';
      $anonid -= 10;
      $pseudoplace->id = $anonid; 
      $pseudoplace->canon = '#;;#';
      $pseudoplace->rank = named::placerank('city'); // hmm
      $pseudoplace->info = 'requested location';
      return TRUE;
    }
    return FALSE;
  }

}

?>
