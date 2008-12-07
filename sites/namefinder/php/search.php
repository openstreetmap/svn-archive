<?php

include_once('word.php');

class search {

  /* The common point of call for doing searches. Call search::xmlise(). */

  // --------------------------------------------------
  /* static */ function xmlise($find, $maxresults, $anyoccurenceifnotlocal) {
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

    $closedfile = "{$config['installdir']}/closed.xml";
    if (file_exists($closedfile)) {
      return file_get_contents($closedfile);
    }

    /* oxml is the return string. Put admin stuff in the top level element */
    $oxml = '<' . '?' . 'xml version="1.0" encoding="UTF-8"' . '?'. '>' . "\n";
    $oxml .= "<searchresults find='" . htmlspecialchars($find, ENT_QUOTES, 'UTF-8') . "'";
    $indexdate = options::getoption('indexdate');
    if ($indexdate == '') {
      $oxml .= " error='index temporarily unavailable'>\n</searchresults>\n";
      return $oxml;
    }
    $oxml .= " sourcedate='".
      htmlspecialchars($indexdate, ENT_QUOTES, 'UTF-8')."'";
    $oxml .= " date='".date('Y-m-d H:i:s')."'";

    /* make a note of the query */
    include_once('querylog.php');
    querylog::log($find);

    /* remove comments in square brackets from input - these are comments */
    $find = trim(preg_replace('/\\[.*\\]/', '', $find));

    /* is there actually something to search on? */ 
    if ($find == '') { 
      $oxml .= " error='nothing given to search for'>\n</searchresults>\n"; 
      return $oxml; 
    } 

    /* does it look like a postcode? */
    $postcodelookup = postcodelookup::postcodelookupfactory($find);
    if (! empty($postcodelookup)) {
      $find = $postcodelookup->namefinderquery;
      if (! $postcodelookup->prefixonly) { $maxresults = 1; }
    }

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
      $nameds = search::find($thisfind, $maxresults, $postcodelookup, $anyoccurenceifnotlocal);

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
      if (is_string($nameds) && empty($postcodelookup)) {
        if (count($thisfind) == 2) {
          $thisfind = array_merge(array($thisfind[0]), $thisfind);
          $nameds = search::find($thisfind, $maxresults, FALSE, $anyoccurenceifnotlocal, TRUE);
        }
      } 

      /* if still nothing, report it */
      if (is_string($nameds)) {
        $oxml .= " error='place not found'>\n</searchresults>\n";
        return $oxml;
      }

      if (count($nameds) == 0) {
        if (! empty($postcodelookup)) {
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
      if (! empty($postcodelookup)) { $oxml .= " postcode='{$postcodelookup->postcode}'"; }

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
  /* static */ function find(&$terms, $maxresults, $postcodelookup, 
                             $anyoccurenceifnotlocal=FALSE, $recursive=FALSE) 
  {
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

       place names may also be postcode prefixes in the UK (e.g. CB21)
       or place and postcode (London EC1A), in which case we'll
       crosscheck.

       maxresults: the maximum number of results to return.  

       returns: an array of named obects - see class named for details 
         or a string which is an error message
    */

    global $db, $config;

    include_once('canonical.php');
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
      $doinglatlonqualifier= TRUE;
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

        /* allow for the place being qualified by a postcode either with or 
           without a comma separator (and also below, and arbitrary is_in term) */
        $postcodeappendage = postcodelookup::postcodelookupfactory($terms[1], TRUE);
        if (! empty($postcodeappendage)) {
          if (empty($postcodeappendage->textbefore)) {
            /* the place term is only a postcode - use a pseudo place instead */
            $places = array(named::pseudonamedfrompostcode($postcodeappendage));
            unset($postcodeappendage);
          } else {
            /* otherwise it stands as an is_in term even though there's no comma */
            $places = array_merge($places, named::lookupplaces($postcodeappendage->textbefore, 
                                                               NULL, TRUE));
          }
        } else {
          if (! empty($terms[2])) {
            $postcodeappendage = postcodelookup::postcodelookupfactory($terms[2]);
            /* which, if set, is like an is_in qualifier */
          }
          $places = array_merge($places, named::lookupplaces($terms[1], NULL, TRUE));
        }

        if (count($places) == 0) { return "I can't find {$terms[1]}"; }

        // $db->log ("found places " . print_r($places, 1));

        /* cull the possible places according to given qualifying is_in in case 3, or 
           by a postcode or postcode area */
        if (empty($postcodeappendage)) {
          $placeisin = $nterms > 2 ? array_slice($terms, 2) : array();
          if (! empty($placeisin)) {
            foreach($placeisin as $isin) {
              $isinstrings = explode(' ', canonical::canonicalise_to_string($isin));
              for ($i = 0; $i < count($places); $i++) {
                $sourceisinstrings = 
                  explode(' ', canonical::canonicalise_to_string($places[$i]->is_in));
                $found = FALSE;
                foreach($isinstrings as $isin) {
                  foreach ($sourceisinstrings as $sourceisin) {
                    if (strpos($sourceisin, $isin) !== FALSE) {
                      $found = TRUE;
                      break 2;
                    }
                  }
                }
                if (! $found) {
                  array_splice($places, $i, 1);
                  $i--;
                }
              }
            }
            // $db->log ("places after cull " . print_r($places, 1));
          }
        } else {
          /* cull the places to be within a reasonable distance of the postcode prefix centroid */
          include_once('placeindex.php');
          $postcodeplace = named::pseudonamedfrompostcode($postcodeappendage);
          include_once('region.php');
          $region = new region($postcodeplace->lat, $postcodeplace->lon);
          $considerregions = $region->considerregions();
          for($i = 0; $i < count($places) /* which varies! */; $i++) {
            if (in_array($places[$i]->region, $considerregions)) {
              /* the biggest postcode area (in Caithness) is approx 120km in diameter, 
               so we need to be within 60km or so of the place for it to qualify */ 
              $tempnamed = clone $places[$i];
              if ($tempnamed->localdistancefrom($postcodeplace) < 60.0) { continue; }
            }
            array_splice($places, $i, 1);
            $i--; // because we'll increase it again in the for
          }          
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


    /* special cases for place like things: limit search
       only to places (rank > 0) rather than including streets named
       'Somewhere Place' etc */

    switch ($terms[0]) {
    case 'cities':
      $terms[0] = 'city';
      $placesonly = y_op::eq(y_op::field('rank',0), named::placerank('city'));
      break;
    case 'towns':
      $placesonly = y_op::eq(y_op::field('rank',0), named::placerank('town'));
      break;
    case 'suburbs':
      $placesonly = y_op::eq(y_op::field('rank',0), named::placerank('suburb'));
      break;
    case 'villages':
      $placesonly = y_op::eq(y_op::field('rank',0), named::placerank('village'));
      break;
    case 'hamlets':
      $placesonly = y_op::eq(y_op::field('rank',0), named::placerank('hamlet'));
      break;
    case 'places':
      $placesonly = y_op::gt(y_op::field('rank',0), 0);
      break;
    }

    /* Work out canonical forms of the first search term (the road name or whatever) to 
       try matching against equivalents in the database. There's more than one because 
       Hinton Road becomes Hinton Rd as well, and so on */

    $canonterms = canonical::canonical_basic($terms[0]);
    if (count($canonterms) > 4) { array_splice($canonterms, 4); }

    $ctn = count($canonterms)-1;
    if ($ctn > 0) {
      if (count($canonterms[0]) == 1 && preg_match('/^[1-9][0-9]*$/', $canonterms[0][0])) {
        /* remove numbers at the beginning on the basis someone probably
         typed a street address, such as "31 Hinton Road" */
        array_splice($canonterms,0,1); 
      } else if (count($canonterms[$ctn]) == 1 && 
                 preg_match('/^[1-9][0-9]*$/', $canonterms[$ctn][0])) 
      {
        /* ditto European style addresses with the number at the end, as in "Via Meloria 14" */
        array_splice($canonterms, $ctn, 1); 
      }
    }

    if (count($places) > 0) {
      /* There are qualifying places. 

         SELECT * FROM named WHERE (region=n0 [or region=n1 or ...]) 
         ORDER BY ((lat - latplace)^2 + (lon - lonplace)^2 asc  */

      foreach ($places as $place) {
        $place->assigncontext(); // nearest more important place(s)
        /* find occurences of the name ordered by distance from the place, 
           for each of the places we found */
        $region = new region($place->lat, $place->lon);
        $regionnumbers = $region->considerregions();

        $q = $db->query();
        if (! isset($placesonly)) {
          $q->where(word::whereword($joiners, $canonterms, FALSE, $regionnumbers));
        } else {
          $joiners = array(new placeindex(), new named());
          $ors = array();
          foreach ($regionnumbers as $regionnumber) { 
            $ors[] = y_op::eq(y_op::field('region',0), $regionnumber); 
          }
          $ands = array($placesonly,
                        count($ors) == 1 ? $ors[0] : y_op::oor($ors),
                        y_op::feq(y_op::field('id',0),y_op::field('id',1)));
          $q->where(y_op::aand($ands));
        }
        $q->ascending(canonical::distancerestriction($place->lat, $place->lon, count($joiners)-1));
        $q->limit($maxresults);
        // $q->groupby(y_op::field('id',count($joiners)-1));

        $toofar = empty($toofars[$place->rank]) ? $toofars[0]: $toofars[$place->rank];
        while ($q->select($joiners) > 0) { 
          $named = $joiners[count($joiners) - 1];
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

    if (! $recursive && $nterms > 1) {
      /* look for place qualified by is_in (do it this way because
         there are places e.g called "England" where you may or may
         not find the first term) */
      $qualified_terms = array_merge(array($terms[0]), $terms);
      $qualified_nameds = search::find($qualified_terms, $maxresults,
                                      $postcodelookup, $anyoccurenceifnotlocal, TRUE);
      if (! is_string($qualified_nameds)) {
        $nameds = array_merge($qualified_nameds, $nameds);
      }
    }

    if (count($nameds) == 0 && (count($places) == 0 || $anyoccurenceifnotlocal) && 
        empty($doinglatlonqualifier)) 
    {
      /* Either no qualifying place, or no name found near given place
         (and we asked to search more widely). If there was a
         qualifying place try general search for name anyway: "but I
         did find one near..."

         In this case we have no place to order by distance from, so
         instead do a partial ordering so that exact matches on the
         name (or one of its abbreviated variants) come first and then
         partial matches. For example, "Fulbourn" would come before
         "Fulbourn Post Office" when searching for "Fulbourn". We do
         this by going round the loop twice, relaxing the exactness
         condition on the second time round 

         ... Well, that's what I used to do. In the interests of
         eifficiency, however, for now just do inexact matches. We'll
         still get places first, but a search for Bury will include
         Bury St Edmunds whereas before that would have been well down
         the list, after all the other Burys */
      $limit = $maxresults;
      $exact = FALSE; // TRUE;
      for ($i = 0; $i < 1 /* 2 */ && $limit > 0; $i++) {
        $q = $db->query();
        $q->where(word::whereword($joiners, $canonterms, $exact));
        $q->limit($limit);
        // $condition = y_op::aand($condition, y_op::le('rank',named::placerank('city')));
        /* prioritise places, and those in order of importance, cities first */
        $q->descending('rank');
        // $q->groupby(y_op::field('id',count($joiners)-1));

        while ($q->select($joiners) > 0) { 
          // $db->log(print_r($joiners,1));
          // $db->log(print_r($named,1));
          $named = $joiners[count($joiners) - 1];
          $namedclone = clone $named;
          if ($namedclone->rank > 0) {
            $namedclone->assigncontext();
            $namedclone->findnearestplace($namedclone, $namedclone->isolatedplaceneighbourranks());
          } else {
            $namedclone->findnearestplace();
          }
          // if (isset($unfoundplace)) { $namedclone->place = $unfoundplace; }
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
      $pseudoplace->rank = named::placerank('city'); // hmm
      $pseudoplace->info = 'requested location';
      return TRUE;
    }
    return FALSE;
  }

}

?>
