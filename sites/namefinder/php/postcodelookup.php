<?php

class postcodelookup {

  /* This helper class consults google to see if it can find an
     address for a postcode.  The address is then fed back into the
     name search Make an object of this class using
     postcodelookup::postcodelookupfactory: you can then retrieve the
     query to pass on to the name finder using get_query method (which
     will do the lookup) on the postcodelookup object */

  /* http://www.google.com/search?hl=en&q=%22PE27+5JP%22 */

  var $postcode;
  var $googlequery = 'http://www.google.com/search?hl=en&q=';
  var $namefinderquery;

  // --------------------------------------------------
  /* static */ function postcodelookupfactory($prospectivepostcode) {
    $postcodelookup = new postcodelookup();
    if (! preg_match ('/[a-z]{1,2}[0-9]{1,2}[a-z]? [0-9][a-z]{2}/i', $prospectivepostcode)) {
      $postcodelookup->postcode = NULL;
      return $postcodelookup; /* not a postcode */
    }
    $postcodelookup->postcode = $prospectivepostcode;
    $postcodelookup->namefinderquery = NULL;
    return $postcodelookup;
  }

  // --------------------------------------------------
  function get_query(&$query) { 
    if (empty($this->postcode)) { return FALSE; }
    $this->googleme();
    if (is_null($this->namefinderquery)) { return FALSE; }
    $query = $this->namefinderquery;    
    return TRUE;
  }

  // --------------------------------------------------
  /* private */ function match($subject) {
    include_once('named.php');
    static $roadetc = 'road|rd|street|st|lane|ln|place|pl|avenue|ave|crescent|cres|close|cl|way|wy|drive|dr|walk|park|pk|row|hill|parade|pde|terrace|tce|court|ct|mews|grove|rise|fields|meadows|path|green|gn|gardens|gdns|garden|gdn|gate';
    static $namechars = "[a-z \\\\'\\\\.\\\\-]";
    $subject = strip_tags(preg_replace('~\\<br\\ *\\/?\\>~i', ',', $subject));
    $roadsection = " *({$roadetc})";

    /* lat,lon to costrain place checks to uk: just clips northern France, and part of RoI, 
       but it is only to slim down the search */
    $uk = array(49.9,-8.1,61.0,2.0);
    global $db; 

    /* hash of places to avoid repeatedly looking up the same place */
    $triedplaces = array();
    
    for ($i = 0; $i < 2; $i++) {
      $nmatches = preg_match_all ("/[^0-9][0-9]{1,3}[\\, ] *({$namechars}{1,30}{$roadsection})\\.?\\, *({$namechars}{1,})(\\, *({$namechars}{1,}))?(\\, *({$namechars}{1,}))?(\\, *({$namechars}{1,}))?\\,? *{$this->postcode}/i", $subject, $matches, PREG_SET_ORDER);
      
      foreach($matches as $submatches) {
        /* $submatches[1] is the street, $submatches[2] is road, rd
           etc, let's see if $submatches[3] etc is a known place */
        for ($k = 3; $k < 9; $k++) {
          if (! empty($submatches[$k]) && $submatches[$k]{0} != ',') {
            $possibleplace = $submatches[$k];
            if (! array_key_exists($possibleplace, $triedplaces)) {
              $places = named::lookupplaces($possibleplace, $uk, TRUE /* exact match */);
              $triedplaces[$possibleplace] = TRUE;
            }
            if (! empty($places)) { break; }
          }
        }
        if (empty($places)) { continue; }
        $this->namefinderquery = "{$submatches[1]}, {$possibleplace}";

        $db->log("looking up {$this->namefinderquery} for {$this->postcode} ". print_r($submatches,1));
        return TRUE;
      }

      /* nothing helped, so relax the search so it doesn't include things like 'Road', 
         e.g. 47 The Brambles, Somwhereville, SG8 1TX */
      $roadsection = '';
    }

    /* now try it without a number. But to isolate the place from among the four clauses 
       try looking up each prospective place in the place index, and take the clause before 
       it as the street (or sometimes the business name: 
       eg 'University of East Anglia, Norwich, NR4 7TJ' */

    if (preg_match_all ("/({$namechars}{1,})\\, *({$namechars}{1,30})(\\, *({$namechars}{1,30}))?(\\, *({$namechars}{1,30}))?\\,? *{$this->postcode}/i", $subject, $matchesall, PREG_SET_ORDER))
    {
      $db->log("considering: ".print_r($matchesall,1));

      foreach ($matchesall as $matches) {
        for($j = 2; $j < count($matches); $j++) {
          $possibleplace = trim($matches[$j]);
          if (empty ($possibleplace) || $possibleplace{0} == ',') { continue; }
          if (empty($possibleplace)) { continue; }
          if (array_key_exists($possibleplace, $triedplaces)) { continue; }
          $places = named::lookupplaces($possibleplace, $uk, TRUE /* exact match */);
          $triedplaces[$possibleplace] = TRUE;
          if (empty($places)) { continue; }
          $db->log("places found for {$possibleplace}: ".print_r($places,1));
          $address = $matches[$j-2];
          $this->namefinderquery = "{$address}, {$possibleplace}";
          $db->log("looking up {$this->namefinderquery} for {$this->postcode} ". 
                   print_r($matches,1));
          return TRUE;
        }
      }
    }

    return FALSE;
  }

  // --------------------------------------------------
  /* private */ function googleme() {
    $qs = $this->googlequery . urlencode("\"{$this->postcode}\"");
    $googleresult = file_get_contents($qs);
    if ($googleresult === FALSE) { return; }
    if ($this->match($googleresult)) { return; }

    /* try the cached pages if we didn't find it on the search results page */
    preg_match_all ("~\\<a .*href=[\"\']([^\"\']*)[\"\'][^\\>]*\\>cached\\<\\/a\\>~i",
                    $googleresult,
                    $matches, PREG_PATTERN_ORDER);
    $caches =& $matches[1];
    for ($cn = 0; $cn < count($caches); $cn++) {
      $cacheresult = file_get_contents($caches[$cn]);
      if ($cacheresult === FALSE) { return; }
      if ($this->match($cacheresult)) { 
        global $db; $db->log ("found in cache {$cn}: {$caches[$cn]}");
        return;
      }
    }
  }

}

?>