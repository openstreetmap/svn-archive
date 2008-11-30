<?php

class querylog {
  var $id;
  var $date;
  var $query;

  function log($query) {
    $ql = new querylog();
    $ql->query = $query;
    $ql->date = time();
    /* id autonumbered */
    global $db;
    $db->insert($ql);
  }
}

?>