<?php

include_once('tagged.php');
include_once('canonical.php');

class node extends tagged {

  /* constructor */ function node($id, $amended=FALSE) { 
    if (! $amended) { $id = canonical::getuniqueid($id, 'node'); }
    $this->id = $id;
  }  

  // --------------------------------------------------
  function set_latlon($lat, $lon) {
    $this->lat = $lat;
    $this->lon = $lon;
  }

  // --------------------------------------------------
  /* a node has its own natural lat/lon so no calculation needed in this subclass */
  function calc_latlong() { return TRUE; }

  // --------------------------------------------------
  function insert() {
    global $db, $added;
    $db->insert($this);
    $added['node']++;
  }

  // --------------------------------------------------
  function delete() {
    global $db;
    $node = new node($this->id, TRUE);
    $db->delete($node, 'id');
  }

  // --------------------------------------------------
  function parent_ids() {
    global $db;
    $parent_ids = array();

    // parent ways
    $q = $db->query();
    $way_node = new way_node();
    $way_node->node_id = $this->id;
    $way = new way(0, TRUE);
    while ($q->select($way_node) > 0) { 
      $way->id = $way_node->way_id; 
      $parent_ids[] = $way->id;
      $way_parent_ids = $way->parent_ids();
      if (! empty($way_parent_ids)) { 
        $parent_ids = array_merge($parent_ids, $way_parent_ids); 
      }
    }

    // and then parent relations
    $q = $db->query();
    $relation_node = new relation_node();
    $relation_node->node_id = $this->id;
    $relation = new relation(0, TRUE);
    while ($q->select($relation_node) > 0) { 
      $relation->id = $relation_node->relation_id; 
      $parent_ids[] = $relation->id;
      $relation_parent_ids = $relation->parent_ids();
      if (! empty($relation_parent_ids)) { 
        $parent_ids = array_merge($parent_ids, $relation_parent_ids); 
      }
    }

    // finally, consolidate
    $parent_ids = array_unique($parent_ids);
    return $parent_ids;
  }
}

?>
