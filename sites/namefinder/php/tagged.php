<?php

include_once('named.php');

class tagged {

  /* tagged is the superclass of node, way and relation.

     It represents the class of things in the planet file which have
     tags. Its main purpose is to digest those tags picking out the
     ones that look interesting for our index.
  */

  var $id;
  var $tags;
  var $lat, $lon;

  /* the following are not saved in the database */
  var $is_interesting = FALSE;
  var $named = NULL;

  // --------------------------------------------------
  function add_tag($key, $value) { 
    static $interesting_tags = array('name' => TRUE,
                                     'ref' => TRUE,
                                     'amenity' => array('post_office' => TRUE,
                                                        'fuel' => TRUE,
                                                        'supermarket' => TRUE,
                                                        'pharmacy' => TRUE,
                                                        'hospital' => TRUE,
                                                        'police' => TRUE,
                                                        'fire_station' => TRUE,
                                                        'bus_station' => TRUE,
                                                        'atm' => TRUE,
                                                        'bank' => TRUE,
                                                        'place_of_worship' => TRUE,
                                                        'school' => TRUE,
                                                        'college' => TRUE,
                                                        'university' => TRUE,
                                                        'cinema' => TRUE,
                                                        'theatre' => TRUE),
                                     'tourism' => array('hotel' => TRUE,
                                                        'motel' => TRUE,
                                                        'hostel' => TRUE,
                                                        'guest_house' => TRUE,
                                                        'camp_site' => TRUE,
                                                        'caravan_site' => TRUE),
                                     'historic' => array('castle' => TRUE,
                                                         'monument' => TRUE,
                                                         'museum' => TRUE,
                                                         'ruins' => TRUE),
                                     'railway' => array('station' => TRUE),
                                     'iata' => TRUE,
                                     'icao' => TRUE,
                                     'place_name' => TRUE,
    );

    static $very_uninteresting_tags = array('created_by'=>TRUE,
                                            'source'=>TRUE,
                                            'source:ref'=>TRUE,
                                            'source:name'=>TRUE,
                                            'source_name'=>TRUE,
                                            'source_ref'=>TRUE);
    if (array_key_exists($key, $very_uninteresting_tags)) { return; }

    $this->tags[$key] = $value; 

    if (array_key_exists($key, $interesting_tags)) {
      $specifically =& $interesting_tags[$key];
      if ($specifically === TRUE || array_key_exists($value, $specifically)) { 
        $this->is_interesting = TRUE;
      }
    }   

    $this->set_is_area($key, $value);
  }

  // --------------------------------------------------
  function set_is_area($key, $value) { return; } // overridden for ways

  // --------------------------------------------------
  function ok_latlon() {
    /* there was a preponderance at one time for dud objects to end up
       in mid-Atlantic, which we can safely ignore */
    return isset($this->lat) && ($this->lat != 0.0 || $this->lon != 0.0);
  }

  // --------------------------------------------------
  function interesting_name($deleting=FALSE) {

    /* This is the key indexing function. It chooses an item (node,
       way, relation) with tags sufficiently interesting to go into the
       index. This is anything with a name, certain classes of object
       which we might search for by class ("pubs near Cambridge") and
       proxy-names like road numbers ('ref') and IATA airport codes */

    if (empty($this->tags) || ! $this->is_interesting) { return; }

    global $db;

    if (! $deleting) {
      /* we only need to work out the lat/lon if we are adding or replacing the object */
      if (! $this->calc_latlong()) { return; }
    }

    /* It has interest, and it has location, so make a named object to
       consider it further. Build a name for it which is a
       composite of the ruename, and the various other
       possibilities like ref, and non-native names (we usually
       put these in square brackets) */

    $named = new named();
    $namestring = '';

    if (! empty($this->tags['name'])) { 
      $namestring .= $this->tags['name']; 
      $canonicalise = $namestring;
    } else if (! empty($this->tags['place_name'])) { 
      $namestring .= $this->tags['place_name']; 
    }
    
    foreach (array('ref', 'iata', 'icao', 'old_name','loc_name','alt_name') as $refkey) {
      if (! empty($this->tags[$refkey])) { 
        if (empty($namestring)) {
          $namestring = $this->tags[$refkey];
        } else {
          $namestring .= ' [' . $this->tags[$refkey] .']';
        }
      }
    }
    foreach ($this->tags as $key=>$value) {
      if (substr($key,0,5) == 'name:') {
        $namestring .= ' ['.substr($key,5).':'.$value.']';
      }
    }

    /* and then the other properties of named... */
    $region = new region($this->lat, $this->lon);
    $named->region = $region->regionnumber();
    $named->id = $this->id;
    $named->name = $namestring;
    $named->canonical = canonical::canonicalise_to_string($namestring);
    $named->lat = $this->lat;
    $named->lon = $this->lon;
    $named->info = '';
    $named->category = '';
    $named->rank = 0;
    $named->is_in = '';

    /* now construct a useful description of the class of the item, so
       we can say, for example, "school St Bede's found ...". This is
       closely related to the tag name of the main tag of the item,
       but sometimes we need to construct it from more than one, and
       remove non-linguistic things like underscores */
    
    $prefix = '';
    $isplace = FALSE;
    if (is_a($this, 'relation')) {
      foreach ($this->tags as $key=>$value) {
        switch ($key) {
        case 'type':
          $named->info = str_replace('_', ' ', $value);
          break;
        }
      }
    } else /* way or node */ {
      foreach ($this->tags as $key=>$value) {
        switch ($key) {
        case 'type':
          if (is_a($this, 'relation')) {
            $named->info = str_replace('_', ' ', $value);
          }
          break;
        case 'highway':
          $named->category = $key;
          switch ($value) {
          case 'trunk':
          case 'primary':
          case 'secondary':
          case 'service':
          case 'unclassified':
            $residential = (! empty($this->tags['abutters']) && 
                            $this->tags['abutters'] == 'residential') ? 'residential ' : '';
            $named->info .= "{$prefix}{$residential}{$value} road"; 
            break;
          case 'track':
            $residential = (! empty($this->tags['abutters']) && 
                            $this->tags['abutters'] == 'residential') ? 'residential ' : '';
            $named->info .= "{$prefix}{$residential}{$value}"; 
            break;
          case 'trunk_link':
          case 'primary_link':
          case 'motorway_link':
            $named->info .= "{$prefix}link road"; 
            break;
          case 'tertiary':
            $named->info .= "{$prefix}road"; 
            break;
          case 'residential':
            $named->info .= "{$prefix}street"; 
            break;
          case 'cycleway':
          case 'bridleway':
          case 'footway':
          case 'footpath':
            $named->info .= "{$prefix}{$value}"; 
            break;
          default:
            $value = str_replace('_', ' ', $value);
            $named->info .= "{$prefix}{$value}"; 
          }
          $prefix = '; ';
          break;
        case 'amenity':
          $named->category = $key;
          switch ($value) {
          case 'fast_food':
            $named->info .= "{$prefix}take-away"; 
            $prefix = '; ';
            break;
          case 'place_of_worship':
            $powtype = 'place of worship';
            if (! empty($this->tags['religion'])) {
              switch($this->tags['religion']) {
              case 'christian':
              case 'church_of_england':
              case 'catholic':
              case 'anglican':
              case 'methodist':
              case 'baptist':
                $powtype = 'church';
                break;
              case 'moslem':
              case 'muslim':
              case 'islam':
                $powtype = 'mosque';
                break;
              }
            } else if (! empty($this->tags['denomination'])) {
              switch($this->tags['denomination']) {
              case 'christian':
              case 'church_of_england':
              case 'catholic':
              case 'anglican':
              case 'methodist':
              case 'baptist':
                $powtype = 'church';
                break;
              case 'moslem':
              case 'muslim':
              case 'islam':
                $powtype = 'mosque';
                break;
              }
            }
            if (! empty($powtype) && strpos($named->info, $powtype) === FALSE) { 
              $named->info .= "{$prefix}{$powtype}"; 
              $prefix = '; ';
            }
            $named->category = $key;
            break;
          default:
            $value = str_replace('_', ' ', $value);
            if (! empty($value) && strpos($named->info, $value) === FALSE) { 
              $named->info .= "{$prefix}{$value}";
              $prefix = '; ';
            }
            break;
          }
          break;
        case 'landuse':
          $named->category = $key;
          switch ($value) {
          case 'farm':
            if (! empty($value) && strpos($named->info, $value) === FALSE) { 
              $named->info .= "{$prefix}{$value}"; 
              $prefix = '; ';
            }
            break;
          default:
            if (! empty($value) && strpos($named->info, $value) === FALSE) { 
              $named->info .= "{$prefix}{$value} area"; 
              $prefix = '; ';
            }
            break;
          }
          break;
        case 'railway':        
        case 'aeroway':        
        case 'man_made':
        case 'military':
        case 'tourism':
        case 'waterway':
        case 'leisure':
        case 'shop':
        case 'tourism':
        case 'historic':
        case 'natural':
        case 'sport':
          $value = str_replace('_', ' ', $value);
          if (! empty($value) && strpos($named->info, $value) === FALSE) { 
            $named->info .= "{$prefix}{$value}";
            $prefix = '; ';
          }
          $named->category = $key;
          break;
        case 'building':
          $value = str_replace('_', ' ', $value);
          if (! empty($value) && strpos($named->info, $value) === FALSE) { 
            $named->info .= $value == 'yes' ? $key : "{$prefix}{$value}";
            $prefix = '; ';
          }
          $named->category = $key;
          break;
        case 'place':
          $value = str_replace('_', ' ', $value);
          if (! empty($value) && strpos($named->info, $value) === FALSE) { 
            $named->info .= "{$prefix}{$value}";
            $named->rank = named::placerank($value);
            $named->category = $key;
            $prefix = '; ';
          }
          break;        
        case 'is_in':
          $named->is_in = $value;
          break;
        case 'religion':
        case 'denomination':
          break;
        }
      }
    }

    $this->named =& $named;
  }
}

?>
