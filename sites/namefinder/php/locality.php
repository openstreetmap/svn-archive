<?php

class locality {

  function explodeterms($terms) {
    $terms = str_replace(' near ',',',$terms);
    $terms = explode(',', $terms);
    for ($i = 0; $i < count($terms); $i++) { $terms[$i] = trim($terms[$i]); }
    return $terms;
  }

  function islatlon($term1, $term2, &$pseudoplace) {
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
      $pseudoplace->rank = named::placerank('town'); // hmm
      $pseudoplace->info = 'requested location';
      return TRUE;
    }
    return FALSE;
  }

  function find($lat,$lon) {
    /* find is named or named,place or named,place,isin; near is lat,lon or empty */
    global $db, $config;

    include_once('canon.php');
    include_once('named.php');
    include_once('region.php');

    $db->log("find lat/lon");
    $pseudoplace = new named();
    $pseudoplace->category = 'place';
    $pseudoplace->lat = $lat;
    $pseudoplace->lon = $lon;
    $pseudoplace->name = '';
    $anonid -= 10;
    $pseudoplace->id = $anonid; 
    $pseudoplace->canon = '#;;#';
    $pseudoplace->rank = named::placerank('town'); // hmm
    $pseudoplace->info = 'requested location';

    $hierarchy = array('city'=>2, 'town'=>6, 'suburb'=>6, 'village'=>6, 'hamlet'=>6);
    $numberofresults = array();
    $maxhitsplacetype = '';
    $maxhitsplacecount = 0;

    foreach($hierarchy as $placetype=>$numberof) {
      $places = $pseudoplace->findseveralplace(named::placerank($placetype), $numberof);
      $numberofresults[$placetype] = count($places);
      if ($numberofresults[$placetype] > $maxhitsplacecount) {
        $maxhitsplacecount = $numberofresults[$placetype];
        $maxhitsplacetype = $placetype;
      }
      foreach($places as $place) {
        $place->localdistancefrom($pseudoplace);
      }
    }

    $pseudoplace->assigndescription($nterms > 1);
    $nameds[] = clone $pseudoplace; 


    $toofars = array(0=>10.0, 
                     named::placerank('hamlet')=>8.0,
                     named::placerank('village')=>20.0,
                     named::placerank('suburb')=>20.0,
                     named::placerank('airport')=>20.0,
                     named::placerank('town')=>25.0,
                     named::placerank('city')=>45.0);

    $places = array();
    $nameds = array();
    $nterms = count($terms);

    if ($nterms > 2 && search::islatlon($terms[1], $terms[2], $pseudoplace)) {
      array_splice($terms, 1, 2);
      $nterms -=2;
      $places[] = clone $pseudoplace;
    } else if ($nterms > 1) {

      if (search::islatlon($terms[0], $terms[1], $pseudoplace)) {
      } else {

        $places = array_merge($places, named::lookupplaces($terms[1]));
        if (count($places) == 0) { return "I can't find {$terms[1]}"; }

        // $db->log ("found places " . print_r($places, 1));

        /* cull the possible places according to given qualifying isin */
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

    /* special case for some plural objects: search on churches near ... => church near */
    if ($terms[0] == 'churches') { $terms[0] = 'church'; }
    else if ($terms[0] == 'cities') { $terms[0] = 'city'; }

    $names = canon::canonical_with_synonym($terms[0]);

    if (count($places) > 0) {
      /* select * from names where (region=n0 [or region=n1 or ...]) 
         order by ((lat - latplace)^2 + (lon - lonplace)^2 asc  */
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

        $q->where(y_op::aand($ands));
        $q->ascending(canon::distancerestriction($place->lat, $place->lon));
        $q->limit($config['limit']);

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
      /* no name found near given place (if any): try general search for name: "but I did 
         find one near..." */
      $limit = $config['limit'];
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

    return $nameds;
  }

  // --------------------------------------------------
  function xmlise($find) {
    include_once('postcodelookup.php');
    global $db;
    include_once('options.php');
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

    /* remove comments in square brackets from input */
    $find = trim(preg_replace('/\\[.*\\]/', '', $find));

    /* does it look like a postcode? */
    $postcodelookup = postcodelookup::postcodelookupfactory($find);
    $originalpostcode = $find;
    $postcodified = $postcodelookup->get_query($find);

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
      $thisfind = search::explodeterms($finds[$i]);

      $nameds = search::find($thisfind);

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

      if (is_string($nameds)) {
        if (count($thisfind) == 2) {
          $thisfind = array_merge(array($thisfind[0]), $thisfind);
          $nameds = search::find($thisfind);
        }
      } 

      if (is_string($nameds)) {
        $oxml .= " error='place not found'>\n</searchresults>\n";
        return $oxml;
      }

      if (count($nameds) == 0) {
        if ($postcodified) {
          $oxml .= " error='name not found for postcode'>\n</searchresults>\n";
        } else {
          $oxml .= " error='name not found'>\n</searchresults>\n";
        }
        return $oxml;
      }

      $foundnearplace = ! empty($nameds[0]->place);
      $oxml .= " foundnearplace{$ks}='" . ($foundnearplace ? 'yes' : 'no') . "'";
          
      if ($postcodified) { $oxml .= " postcode='{$originalpostcode}'"; }

      $db->log("result: ".print_r($nameds,1));
      $multinameds[] = $nameds;
    }

    $oxml .= ">\n";
    $xml = '';

    if (count($multinameds) == 1) {
      foreach($nameds as $named) { $xml .= $named->xmlise(); }
    } else {
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
    return $oxml . $xml . "</searchresults>\n";
  }
}


?>
