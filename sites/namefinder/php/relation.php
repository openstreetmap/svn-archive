<?php

include_once('tagged.php');
include_once('canonical.php');

class relation extends tagged {
  var $representativemember;
  var $nodes;
  var $ways;
  var $relations;

  // --------------------------------------------------
  function relation($id, $amended=FALSE) { 
    if (! $amended) { $id = canonical::getuniqueid($id, 'relation'); }
    $this->id = $id; 
  }

  // --------------------------------------------------
  function add_node($osmid) { $this->nodes[] = canonical::getuniqueid($osmid, 'node'); }

  // --------------------------------------------------
  function add_way($osmid) { $this->ways[] = canonical::getuniqueid($osmid, 'way'); }

  // --------------------------------------------------
  function calc_latlong() { 
    /* The lat/lon of a relation is detemrined to be the lat/lon of a
       representative member: its first node if there is one, or if
       not via middle way. In time we might want to refine this to
       take roles and/or the type of relation into account */
    if (! $this->get_representativemember()) { return FALSE; }
    if (! $this->representativemember->calc_latlong()) { return FALSE; } 
    $this->lat = $this->representativemember->lat;
    $this->lon = $this->representativemember->lon;
    return TRUE;
  }

  // --------------------------------------------------
  function get_representativemember() {
    global $db;
    if (empty($this->nodes)) { 
      if (empty($this->ways)) { return FALSE; }
      $wayid = $this->ways[count($this->ways)/2];
      $way_node = new way_node();
      $way_node->way_id = $wayid;
      if ($db->select($way_node) == 0) { return FALSE; }
      $node = new node($way_node->node_id, TRUE);
      if ($db->select($node) == 0) { return FALSE; }
      $this->representativemember = $node;
      return TRUE;
    }
    $nodeid = $this->nodes[0];
    $node = new node($nodeid, TRUE);
    if ($db->select($node) != 1) { return FALSE; }
    $this->representativemember = $node;
    return TRUE;
  }

  // --------------------------------------------------
  function insert() {
    global $db, $added;
    // $db->insert($this); relations themselves not stored

    // and its node, way and relation references
    if (! empty($this->nodes)) { 
      $relation_nodes = array();
      $relation_node = new relation_node();
      $relation_node->relation_id = $this->id;
      foreach ($this->nodes as $nodeid) {
        $relation_node->node_id = $nodeid;
        $relation_nodes[] = clone $relation_node;
      }
      $db->insert($relation_nodes);
    }

    if (! empty($this->ways)) { 
      $relation_ways = array();
      $relation_way = new relation_way();
      $relation_way->relation_id = $this->id;
      foreach ($this->ways as $wayid) {
        $relation_way->way_id = $wayid;
        $relation_ways[] = clone $relation_way;
      }
      $db->insert($relation_ways);
    }

    if (! empty($this->relations)) {
      $relation_relations = array();
      $relation_relation = new relation_relation();
      $relation_relation->relation_id = $this->id;
      foreach ($this->relations as $relationid) {
        $relation_relation->other_relation_id = $relationid;
        $relation_relations[] = clone $relation_relation;
      }
      $db->insert($relation_relations);
    }

    $added['relation']++;
  }

  // --------------------------------------------------
  function delete() {
    global $db;
    /* relations themselves not stored
    $relation = new relation($this->id, TRUE);
    $db->delete($relation, 'id');
    */

    $relation_node = new relation_node();
    $relation_node->relation_id = $this->id;
    $db->delete($relation_node, 'relation_id');

    $relation_way = new relation_way();
    $relation_way->relation_id = $this->id;
    $db->delete($relation_way, 'relation_id');

    $relation_relation = new relation_relation();
    $relation_relation->relation_id = $this->id;
    $db->delete($relation_relation, 'relation_id');
  }

  // --------------------------------------------------
  function parent_ids() {
    /* get parent relations recursively */
    global $db;
    $parent_ids = array();
    $q = $db->query();
    $relation_relation = new relation_relation();
    $relation_relation->other_relation_id = $this->id;
    $relation = new relation(0, TRUE);
    while ($q->select($relation_relation) > 0) { 
      $relation->id = $relation_relation->relation_id; 
      $parent_ids[] = $relation->id;
      /* someone could have created a mutual relationship parentage,
         which would cause a loop if not checked for: */
      if (in_array($relation->id, $parent_ids)) { continue; }
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
