<?php

class lock {

  /* manual control over a central lock in the database: wait for the
     lock to become free before proceeding, except that there is a timeout 

     Maps directly toa database table of the same name
  */

  var id; // actually there's only one record, id=0

  /* static */ function getlock() {
    static $tries = 20;
    $lock = new lock();
    $lock->id = 0;
    for ($i = 0; $i < $tries; $i++) {
      if ($db->delete($lock, 'id') == 1) { return $lock; }
      usleep(50000); //20 times a second for up to a second
    }
    return NULL; // we failed to get a lock despite trying several times
  }

  // --------------------------------------------------
  function unlock() {
    $db->insert($this);
  }

}

?>