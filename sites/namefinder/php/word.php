<?php

/* Index of names by word. This allows for efficient searching without wildcards, 
   and for variations in names */

include_once('canonical.php');
include_once('named.php');

class word {

  var $word;    /* a canonical word in the index */

  var $ordinal; /* the ordinal position of the word in the canonical name. 
                   e.g. if the canonical  name is 'walter;matthau;avenue' 
                   then 'walter' is word 1, 'matthau' is 2 and so on. */

  var $firstword; /* booleans for whether this word is the first or last in a phrase */
  var $lastword;  /* aids exact macthing */

  var $region;  /* the region number of the record this index term refers to. 
                   Not strictly necessary, but allows entries to be culled by 
                   region before they are joined to the named table during a search */

  var $id;      /* the id of the record this index term refers to in the named table */

  // --------------------------------------------------
  function word($word=null, $ordinal=null, $region=null, $id=null, $firstword=null, $lastword=null)
  {
    if (! is_null($word)) { $this->word = $word; }
    if (! is_null($ordinal)) { $this->ordinal = $ordinal; }
    if (! is_null($region)) { $this->region = $region; }
    if (! is_null($id)) { $this->id = $id; }
    if (! is_null($firstword)) { $this->firstword = $firstword; }
    if (! is_null($lastword)) { $this->lastword = $lastword; }
  }

  // --------------------------------------------------
  /* given an array of either words or arrays of words which are alternatives at each 
     position, construct a partial query. This creates the last term of the join as a named 
     which shares the same id, and it returns a suitable array of objects to query on */
  function whereword(&$joiners, $ws, $exact=FALSE, $regions=NULL) {
    $joiners = array();
    $wsc = count($ws);
    $ands = array();
    for ($i = 0; $i < $wsc; $i++) {
      $joiners[] = new word();
      $ors = array();
      if (is_array($ws[$i])) {
        foreach ($ws[$i] as $term) {
          $ors[] = y_op::eq(y_op::field('word',$i), $term);
        }
      } else {
        // single word
        $ors[] = y_op::eq(y_op::field('word',$i), $ws[$i]);
      }

      $ands[] = count($ors) == 1 ? $ors[0] : y_op::oor($ors);
      $ands[] = y_op::feq(y_op::field('id',$wsc),y_op::field('id',$i));

      if ($i > 0) {
        if (! $exact) {
          $ands[] = y_op::flt(y_op::field('ordinal', $i-1), y_op::field('ordinal',$i));
        } else {
          $ands[] = y_op::oprintf('%f = %f - 1', 
                                  y_op::field('ordinal', $i-1), y_op::field('ordinal',$i));
        }
      }
    }
    if ($exact) {
      // first word of search is first word of result, likewise last
      $ands[] = y_op::eq(y_op::field('firstword', 0), 1);
      $ands[] = y_op::eq(y_op::field('lastword', $i-1), 1);
    }

    if (! empty($regions)) {
      $ors = array();
      foreach ($regions as $region) {
        $ors[] = y_op::eq(y_op::field('region',0), $region);
      }
      $ands[] = count($ors) == 1 ? $ors[0] : y_op::oor($ors);
    }    

    $joiners[] = new named();

    return count($ands) == 1 ? $ands[0] : y_op::aand($ands);
  }

}

?>