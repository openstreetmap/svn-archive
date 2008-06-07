<?php

/* This class represents elements of the table which matches indexed
   alternate canonical forms of search terms and also
   provides services to canonicalise and generalise
   search terms */

class canonical {

  var $canonical;  /* a canonical UTF-8 string, that is punctuation
                      removed, no spaces, diacriticals and ligatures
                      reduced to ascii equivalents */
  var $region;     /* the region number for this canonical string */

  // --------------------------------------------------
  /* constructor */ function canonical($canonical=NULL, $region=NULL) {
    if (! is_null($canonical)) { $this->canonical = $canonical; }
    if (! is_null($region)) { $this->region = $region; }
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
         or 'Newmarket Road [A1304]' 

       alternates: whether to consider  replacing a diacritical 
         character with multiple alternate
         replacements, for example Danish aring character with aa and a
         (we normally only do this on indexing, not on lookup).

       The canonical form also splits each word nto an array of two words 
       at a point between a separalble suffix. For example
       'hauptbahnweg' -> array('hauptbahn','weg') */

    /* The replacements table maps UTF characters (multiple byte keys) to ascii equivalents
       so that characters such as u-umlaut can be matched by u and ue. There are multiple
       tables because some characters have more than one functional replacement 
       (as for u-umlaut). We store multiple canonical forms in the word index, 
       but search only on one (so M<u-umlaut>nchen is stored as 'munchen' and 'muenchen'.

       see http://www.utf8-chartable.de/unicode-utf8-table.pl for a utf-8 list
    */

    static $replacements = NULL;
    if (empty($replacements)) { $replacements = include_once('utf8.inc.php'); }

    static $suffixes = NULL;
    if (empty($suffixes)) { $suffixes = include_once('suffixes.inc.php'); }

    static $languagecodes = NULL;
    if (empty($languagecodes)) { $languagecodes = include_once('languagecodes.inc.php'); }

    /* separate the search terms into words */
    $term = preg_replace('~[ \\-\\/\\:\\;\\=\\|\\,]+~', ' ', 
                         preg_replace('/\\&/', ' and ', $term));
    $term = trim(preg_replace('/ (the|der|das|die|le|la|el|il) /i', ' ', " {$term} "));
    $words = explode(' ', $term);
    $prefix = '';

    $na = $alternates ? count($replacements) : 1;

    $canonicalvariants = array();

    foreach ($words as $word) {
      $word = trim(strtolower($word));
      /* remove apostrophe-s: these are always stored and searched in the singular
         non-possessive so that (the church of, for example) 'St Andrew's', 
         'St Andrews' and 'St Andrew' all match equivalently */
      $pos = mb_strpos($word, "\xe2\x80\x99s", 0, 'UTF-8');
      if ($pos !== FALSE) {
        $apostrophe = mb_strlen($word, 'UTF-8') == $pos + 2;
        if ($apostrophe) { $word = mb_substr($word, 0, -2, 'UTF-8'); }
      } else {
        $apostrophe = strlen($word) > 1 && substr($word, -2) == '\'s';
        if ($apostrophe) { $word = substr($word, 0, -2); }
      }
      if (empty($word)) { continue; }

      if ($word{0} == '[') {
        /* separate clauses within the phrase with |, 
           and remove any language codes following, as in [de ... ] */
        $canonicalvariants[] = '|';
        $word = substr($word, 1);
        if (empty($word)) { continue; }
        if ($alternates && array_key_exists($word, $languagecodes)) { continue; }
      }

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
      
      /* now add the plural form if apostrophe s present */
      if ($apostrophe && $alternates) {
        $nt = count($wordvariants);
        for ($t = 0; $t < $nt; $t++) {
          $wordvariants[] = "{$wordvariants[$t]}s";
        }
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
            $wordvariants[$t] = substr($word, 0, - $ns);
            $addsuffix = $suffix;
            break;
          }
        }
      }

      $canonicalvariants[] = $wordvariants;
      if (isset($addsuffix)) { 
        $canonicalvariants[] = array($addsuffix);
        unset($addsuffix);
      }
    }

    /* now we have an array $canonicalvariants each element of which is an
       aray of alternate possibilities for that original word. We have
       at minumum got rid of any ligatures, diacriticals etc, so we
       don't have to worry about alternatives to strasse and the like
       any more */

    return $canonicalvariants;
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

    $canonicalvariants = canonical::canonical_basic($term, TRUE);
    if (empty($canonicalvariants)) { return $canonicalvariants; }

    static $synonyms = NULL;
    if (is_null($synonyms)) { $synonyms = include_once('synonyms.inc.php'); }

    $nc = count($canonicalvariants);
    for($i = 0; $i < $nc; $i++) {
      $nt = count($canonicalvariants[$i]);
      for($j = 0; $j < $nt; $j++) {
        $wordvariant = $canonicalvariants[$i][$j];
        if (! empty($synonyms[$wordvariant])) {
          if (is_array($synonyms[$wordvariant])) {
            /* special case 'dr'=>'drive' and 'st' => 'street' only when last word, 
               otherwise we get lots of unnecessary 'doctor's and 'saint's */
            if (($wordvariant == 'dr' || $wordvariant == 'st') && $i == $nc-1) {
              $canonicalvariants[$i][] = $synonyms[$wordvariant][0];
            } else {
              $canonicalvariants[$i] = array_merge($canonicalvariants[$i], 
                                                   $synonyms[$wordvariant]);
            }
          } else {
            $canonicalvariants[$i][] = $synonyms[$wordvariant];
          }
        } 
      }
    }

    return $canonicalvariants;
  }

  // --------------------------------------------------
  /* static */ function canonicalise_to_string($canoncalise) {
    $canonicalterms = canonical::canonical_basic($canoncalise);
    $s = '';
    $prefix = '';
    foreach ($canonicalterms as $term) {
      if (is_array($term)) {
        if (empty($term[0])) { continue; }
        $s = "{$s}{$prefix}{$term[0]}";
      } else {
        $s = "{$s}{$prefix}{$term}";
      }
      $prefix = ' ';
    }
    return $s;
  }

  // --------------------------------------------------
  /* static */ function distancerestriction($lat, $lon, $fi) {
    /* This generates a SQL fragment for ORDER BY so that names come back sorted by distance 
       from given latitude and longitude */
    return y_op::oprintf("(pow(%f - {$lat},2) + pow(%f - {$lon},2))", 
                         y_op::field('lat', $fi),
                         y_op::field('lon', $fi));
  }


  // --------------------------------------------------
  /* static */ function getuniqueid($osmid, $type) {
    /* osm ids are only unique within type (node, way, relation), so we make them unique
       overall by inserting in the osm id an extra loworder decimal digit for the type */
    static $types;
    if (! isset($types)) { $types = array_flip(canonical::getosmtypes()); }
    return ($osmid << 2) | (is_int($type) ? $type : $types[$type]);
  }

  // --------------------------------------------------
  /* static */ function getosmid($id, &$type) {
    /* converts from name finder id to osm id; the converse of getuniqueid above */
    static $types;
    if (! isset($types)) { $types = canonical::getosmtypes(); }
    $typeindex = $id & 0x3;
    $type = $types[$typeindex];
    return $id >> 2;
  }

  // --------------------------------------------------
  /* static */ function getosmtypes() {
    static $types = array(1=>'node',2=>'way',3=>'relation');
    return $types;
  }
  
}

?>
