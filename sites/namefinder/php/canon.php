<?php

/* This class represents elements of the table which matches indexed
   alternate canonical forms of search terms and also
   provides services to canonicalise and generalise
   search terms  */

class canon {

  var $named_id; /* a number unique across all nameds, unlike the osm id
                    which is only unique within type. This id is the osm
                    id multiplied by 10 and then the type inserted in the
                    low order decimal digit (1 for node, 4 for relation,
                    3 for way. It's done this way to make it easy to see
                    the correspondence when seeing the number, and
                    therefore to debug. Conversion functions below */

  var $canon;    /* a canonical UTF-8 string, that is punctuation
                    removed, no spaces, diacriticals and ligatures
                    reduced to ascii equivalents, and each constituent
                    word surrounded y semicolons to help in pattern
                    matching.Note that means former spaces are always two
                    semicolons */

  // --------------------------------------------------
  /* constructor */ function canon($id=NULL, $canon=NULL) {
    if (! is_null($id)) { $this->named_id = $id; }
    if (! is_null($canon)) { $this->canon = $canon; }
  }

  // --------------------------------------------------
  /* static */ function canonfactory($id, $canonvariants) {
    /* converts an array of alternate canonical strings for an object with id $id 
       into an array of canon objects */
    $canons = array();
    foreach ($canonvariants as $canonstring) { $canons[] = new canon($id, $canonstring); }
    return $canons;
  }

  // --------------------------------------------------
  /* static */ function canonical_basic($term, $alternates=FALSE) {

    /* produces an array of "words" where each word is itself an array
       of alternate forms of that word according to replacements for
       utf8 diacriticals and the like. A word is canonical - that is
       uses a single form of diacritical and ligature equivalents,
       lower case, punctuation free and so on so that when a similar
       name is converted on search, it can also be converted to
       canonical form to compare with the canonical index.

       term: a name being indexed or sought, for example 'Ben Nevis'
         or 'Newmarket Road [A1304]' alternates: whether to consider
         replacing a diacritical character with multiple alternate
         replacements, for example Danish aring character with aa and a
         (we normally only do this on indexing, not on lookup).

       The canonical form also splits each word nto an array of two words 
       at a point between a separalble suffix. For example
       'haupbahnweg' -> array('hauptbahn','weg') */

    /* The replacements table maps UTF characters (multiple byte keys) to ascii equivalents
       so that characters such as u-umlaut can be matched by u and ue. There are multiple
       tables because some characters have more than one functional replacement 
       (as for u-umlaut). We store multiple canonical forms, but search only on one (so 
       M<u-umlaut>nchen is stored as '#;munchen;#;muenchen;#' which means munchen or 
       muenchen as search strings will match one or the ther, and M<u-umlaut>nchen as 
       a search string will match the first.

       see http://www.utf8-chartable.de/unicode-utf8-table.pl for a utf-8 list
    */

    static $replacements = NULL;
    if (empty($replacements)) { $replacements = include_once('utf8.inc.php'); }

    static $suffixes = NULL;
    if (empty($suffixes)) { $suffixes = include_once('suffixes.inc.php'); }

    /* separate the search terms into words */
    $term = preg_replace('~[ \\-\\/\\:\\;\\=\\|]+~', ' ', str_replace('&', ' and ', $term));
    $words = explode(' ', $term);
    $prefix = '';

    $na = $alternates ? count($replacements) : 1;

    $canonvariants = array();

    foreach ($words as $word) {
      /* remove apostrophe-s: these are always stored and searched in the singular
         non-possessive so that (the church of, for example) 'St Andrew's', 
         'St Andrews' and 'St Andrew' all match equivalently */
      $word = preg_replace('~\\\'s$~', '', trim(strtolower($word)));
      if (empty($word)) { continue; }

      $l = mb_strlen($word, 'UTF-8');
      $s = '';
      $letters = array();
      for ($i = 0; $i < $l; $i++) {
        /* replace listed UTF-8 characters with their ascii
           equivalents. For search words we only replace from the main
           replacement table (hence $na = 1), but so that we get
           alternatives to search for, we replace from all the tables
           in turn (falling back to the main table if not in the
           alternates */
        $c = mb_substr($word, $i, 1, 'UTF-8');
        /* try each replacements table */
        for($alt = 0; $alt < $na; $alt++) {
          $replacement =& $replacements[$alt];
          if (array_key_exists($c, $replacement)) {
            $letters[$i][] = $replacement[$c];
          } else if ($alt == 0) {
            $letters[$i][] = $c;
          }
        }
      }

      /* so now we have an array of each letter in its several alternate forms. 
         Build an array of alternate combinations */

      $wordvariants = array('');
      foreach ($letters as $lettervariants) {
        $newwordvariants = array();
        foreach ($wordvariants as $variant) {
          foreach ($lettervariants as $lettercombination) {
            $newwordvariants[] = $variant . $lettercombination;
          }
        }
        $wordvariants = $newwordvariants;
      }
      
      /* $wordvariants now contains an array of possible variants on
         the word under consideration. If this ends in a concatenated
         suffix like 'strasse', split it - e.g. 'hauptbahnstrasse' ->
         'hauptbahn strasse' - so we'll always look for, abbreviate and
         deabbrevaite, and find, the separated variety */

      $nt = count($wordvariants);
      for ($t = 0; $t < $nt; $t++) {
        $word = $wordvariants[$t];
        foreach ($suffixes as $suffix) {
          $ns = strlen($suffix);
          if (strlen($word) > $ns && substr($word, - $ns) == $suffix) {
            $wordvariants[$t] = array(substr($word, 0, - $ns), $suffix);
          }
        }
      }

      $canonvariants[] = $wordvariants;
    }

    /* now we have an array $canonvariants each element of which is an
       aray of alternate possibilities for that original word. We have
       at minumum got rid of any ligatures, diacriticals etc, so we
       don't have to worry about alternatives to strasse and the like
       any more */

    return $canonvariants;
  }

  // --------------------------------------------------
  /* private static */ function canonical_normalise($canonvariants) {
    /* convert an array of word variations (each variant might be a
       simple string or an array of two strings arising from a split off
       suffix) into an array of alternate strings, each word surrounded
       by semicolons. For example 
         array(array('munchen','muenchen'),
               array('platz','pl')) ->
         array(';munchen;;platz;', 
               ';muenchen;;platz;', 
               ';munchen;;pl;',
               ';muenchen;;pl;');
               
    */
    
    $canonstrings = array('');
    foreach ($canonvariants as $wordvariants) {
      $newcanonstrings = array();
      foreach ($canonstrings as $canonstring) {
        foreach ($wordvariants as $wordvariant) {
          if (is_array($wordvariant)) {
            $newcanonstrings[] = $canonstring . ";{$wordvariant[0]};;{$wordvariant[1]};";
          } else {
            $newcanonstrings[] = $canonstring . ";{$wordvariant};";
          }
        }
      }
      $canonstrings = $newcanonstrings;
    }

    return $canonstrings;
  }

  // --------------------------------------------------
  /* static */ function canonical($term, $alternates=FALSE) {
    return canon::canonical_normalise(canon::canonical_basic($term, $alternates));
  }

  // --------------------------------------------------
  /* static */ function canonical_with_synonym($term) {
    /* expand the array of variants to include cononicalise 
       the term as above, but also create multiple
       canonical strings where each has a variation in common
       abbreviations (road for rd etc, and vice-versa, and singnular
       for plural - that's particularly important for church names and
       similar, where we want to match "St John's" with "St John" or
       "St Johns" (simple canonicalisation will have removed the
       apostrophe, so the plural to singular also acts as possessive
       to non-possessive */

    $canonvariants = canon::canonical_basic($term);
    if (empty($canonvariants)) { return $canonvariants; }

    static $synonyms = NULL;
    if (is_null($synonyms)) { $synonyms = include_once('synonyms.inc.php'); }

    $nc = count($canonvariants);
    for($i = 0; $i < $nc; $i++) {
      $nt = count($canonvariants[$i]);
      for($j = 0; $j < $nt; $j++) {
        $wordvariant = $canonvariants[$i][$j];
        if (is_array($wordvariant)) {
          /* only ever an array of two elements, e.g. 'essen','plein' arising from 'essenplein' */
          if (! empty($synonyms[$wordvariant[1]])) {
            if (is_array($synonyms[$wordvariant[1]])) {
              foreach ($synonyms[$wordvariant[1]] as $synonym) {
                $canonvariants[$i][] = array($wordvariant[0], $synonym);
              }
            } else {
              $canonvariants[$i][] = array($wordvariant[0], $synonyms[$wordvariant[1]]);
            }
            if (substr($wordvariant[0], -1) == 's') {
              $withouts = substr($wordvariant[0],0,-1);
              $canonvariants[$i][] = array($withouts, $wordvariant[1]);
              if (is_array($synonyms[$wordvariant[1]])) {
                foreach ($synonyms[$wordvariant[1]] as $synonym) {
                  $canonvariants[$i][] = array($withouts, $synonym);
                }
              } else {
                $canonvariants[$i][] = array($withouts, $synonyms[$wordvariant[1]]);
              }
            }
          } else if (substr($wordvariant[0], -1) == 's') {
            $canonvariants[$i][] = array(substr($wordvariant[0],0,-1), $wordvariant[1]);
          }
        } else if (! empty($synonyms[$wordvariant])) {
          if (is_array($synonyms[$wordvariant])) {
            $canonvariants[$i] = array_merge($canonvariants[$i], $synonyms[$wordvariant]);
          } else {
            $canonvariants[$i][] = $synonyms[$wordvariant];
          }
        } if (substr($wordvariant, -1) == 's') {
          /* apply singular form too, only for 's' not 'es' or other peculiarities */
          $canonvariants[$i][] = substr($wordvariant, 0, -1);
        }
      }
    }

    return canon::canonical_normalise($canonvariants);
  }

  // --------------------------------------------------
  /* static */ function likecanon($canonstrings, $exact=FALSE) {
    /* generates a SQL fragment which compares canonical indexes with
    given (canonical) name.  exact is a boolean which will mean the
    match has to be exactly word for word (though each word may still
    have accented variants); when false the index need only contain
    all the words in 'name' in the same order to match, though there may be other 
    words before, after or in between. For example, 'Hinton Road' canonicalises 
    to ';hinton;;road;'. We may have ';hinton;road;' 
    and say ';cherry;;hinton;;road;' in the index. Exact match catches only the first, 
    non-exact both */

    $ors = array();
    if ($exact) {
      foreach ($canonstrings as $canonstring) {
        $ors[] = y_op::eq('canon', $canonstring);
      }
    } else {
      foreach ($canonstrings as $canonstring) {
        $ors[] = y_op::like('canon', '%'.str_replace(';;', ';%;', $canonstring).'%');
      }
    }
    return count($ors) == 1 ? $ors[0] : y_op::oor($ors);
  }

  // --------------------------------------------------
  /* static */ function distancerestriction($lat, $lon) {
    /* This generates a SQL fragment for ORDER BY so that names come back sorted by distance 
       from given latitude and longitude */
    return y_op::oprintf("(pow(%f - {$lat},2) + pow(%f - {$lon},2))", 'lat', 'lon');
  }


  // --------------------------------------------------
  /* static */ function getuniqueid($osmid, $type) {
    /* osm ids are only unique within type (node, way, relation), so we make them unique
       overall by inserting in the osm id an extra loworder decimal digit for the type */
    static $types;
    if (! isset($types)) { $types = array_flip(canon::getosmtypes()); }
    return 10 *$osmid + $types[$type];
  }

  // --------------------------------------------------
  /* static */ function getosmid($id, &$type) {
    /* converts from name finder id to osm id; the converse of getuniqueid above */
    static $types;
    if (! isset($types)) { $types = canon::getosmtypes(); }
    $typeindex = $id % 10;
    $type = $types[$typeindex];
    return (int)floor(($id/10));
  }

  // --------------------------------------------------
  /* static */ function getosmtypes() {
    static $types = array(1=>'node',3=>'way',4=>'relation');
    return $types;
  }
  
}

?>
