<?php

include_once('tagged.php');
include_once('canonical.php');

class way extends tagged {
  var $midpoint;
  var $nodes;
  var $is_area;

  // --------------------------------------------------
  function way($id, $amended=FALSE) { 
    if (! $amended) { $id = canonical::getuniqueid($id, 'way'); }
    $this->id = $id; 
  }

  // --------------------------------------------------
  function add_node($osmid) { $this->nodes[] = canonical::getuniqueid($osmid, 'node'); }
 
  // --------------------------------------------------
  function set_is_area($key,$value) {
    static $area_tags = array(
                            'building'=>TRUE,
                            'landuse'=>TRUE,
                            'leisure'=>TRUE,
                            'amenity'=>TRUE,
                            'shop'=>TRUE,
                            'tourism'=>TRUE,
                            'historic'=>TRUE,
                            'area'=>TRUE,
                            'military'=>TRUE,
                            'sport'=>TRUE,
                            'natural'=>array('coastline'=>FALSE,
                                             'cliff'=>FALSE),
                            'waterway'=>array('dock'=>TRUE),
                            'railway'=>array('turntable'=>TRUE),
                            'aeroway'=>array('terminal'=>TRUE,
                                             'apron'=>TRUE),
                            'power'=>array('substation'=>TRUE),
                            'man_made'=>array('reservoir_covered'=>TRUE,
                                              'pier'=>TRUE,
                                              'wastewater_plant'=>TRUE),

    );
    if (array_key_exists($key, $area_tags)) {
      $specifically =& $area_tags[$key];
      if ($specifically === TRUE || array_key_exists($value, $specifically)) { 
        $this->is_area = TRUE;
      }
    }   
  }

 // --------------------------------------------------
  function calc_latlong() { 
    if (! $this->is_area) {
      /* The lat/lon of a way is detemrined to be the lat/lon of its middle node */
      if (! $this->get_midpoint()) { return FALSE; }
      if (! $this->midpoint->calc_latlong()) { return FALSE; } 
      $this->lat = $this->midpoint->lat;
      $this->lon = $this->midpoint->lon;
    } else {
      /* The lat/lon of an area is detemrined to be the lat/lon of its bounding box */
      global $db;
      if (empty($this->nodes)) { return FALSE; }
      $minlat =  1000.0; $minlon =  1000.0; 
      $maxlat = -1000.0; $maxlon = -1000.0; 
      foreach ($this->nodes as $nodeid) {
        $node = new node($nodeid, TRUE);
        if ($db->select($node) != 1) { continue; }
        if ($node->lat < $minlat) { $minlat = $node->lat; }
        if ($node->lat > $maxlat) { $maxlat = $node->lat; }
        if ($node->lon < $minlon) { $minlon = $node->lon; }
        if ($node->lon > $maxlon) { $maxlon = $node->lon; }
      }
      if ($minlat > 90.0) { return FALSE; }
      $this->lat = ($maxlat + $minlat) / 2.0;
      $this->lon = ($maxlon + $minlon) / 2.0;
    }
    return TRUE;
  }

  // --------------------------------------------------
  function get_midpoint() {
    global $db;
    if (empty($this->nodes)) { return FALSE; }
    $nodeid = $this->nodes[count($this->nodes)/2];
    $node = new node($nodeid, TRUE);
    if ($db->select($node) != 1) { return FALSE; }
    $this->midpoint = $node;
    return TRUE;
  }

  // --------------------------------------------------
  function insert() {
    global $db, $added;
    // $db->insert($this); - ways not stored

    // and its node references
    if (empty($this->nodes)) { return; }
    $way_nodes = array();
    $way_node = new way_node();
    $way_node->way_id = $this->id;
    foreach ($this->nodes as $nodeid) {
      $way_node->node_id = $nodeid;
      $way_nodes[] = clone $way_node;
    }
    $db->insert($way_nodes);

    $added['way']++;
  }

  // --------------------------------------------------
  function delete() {
    global $db;
    /* ways not stored...
    $way = new way($this->id, TRUE);
    $db->delete($way, 'id');
    */

    $way_node = new way_node();
    $way_node->way_id = $this->id;
    $db->delete($way_node, 'way_id');
  }

  // --------------------------------------------------
  function parent_ids() {
    global $db;
    $parent_ids = array();
    $q = $db->query();
    $relation_way = new relation_way();
    $relation_way->way_id = $this->id;
    $relation = new relation(0, TRUE);
    while ($q->select($relation_way) > 0) { 
      $relation->id = $relation_way->relation_id; 
      $parent_ids[] = $relation->id;
      $relation_parent_ids = $relation->parent_ids();
      if (! empty($relation_parent_ids)) { 
        $parent_ids = array_merge($parent_ids, $relation_parent_ids); 
      }
    }
    $parent_ids = array_unique($parent_ids);
    return $parent_ids;
  }
}

?>
