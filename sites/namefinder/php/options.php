<?php

class options {

  /* A simple class for retrieving configuration information from the
     database by name. Currently only used for the date of the index

     Maps directly to a database table fo the same class name
  */

  var $id;
  var $name;
  var $value;

  /* static */ function getoption($name) {
    global $db;
    $options = new options();
    $options->name = $name;
    if ($db->select($options) == 1) { return $options->value; }
    return '';
  }
}

?>