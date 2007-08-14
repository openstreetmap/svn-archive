<?php

/* Every html request includes this file, to give access to everything else */

include_once ('.config.php'); // => $config
set_include_path (get_include_path().':'.$config['includephp']);

if (! function_exists('mb_strlen')) {
  include_once('mb.php');
}

include_once('classysql/classysql.php');
$db =& new y_db($config);

// $db->log("VISITING {$_SERVER['REQUEST_URI']} from {$ra} {$rh} user {$rn}");

// set up custom error handling 
function eh($errno, $errstr, $errfile, $errline) {
  if ($errno != 2048 && error_reporting() != 0) {
    global $state, $db, $me, $config;
    $id = session_id(); if (empty($id)) { $id = 'all'; }
    $emailsent = ' (email NOT sent)';

    if (! empty($config['webmaster'])) {
      $logname = "{$config['cataloguehomepage']}/log.html?logname=" . urlencode($db->logname);
      mail ($config['webmaster'], "{$config['cataloguehomepage']} internal error", 
            "Internal error detected:\nsee log {$logname}\n",
            "From: {$config['webmaster']}\r\n");
      $emailsent = ' (email sent)';
    }
    $loglink = '';
    if (isset($me) && $me->privileged('debug')) {
      echo "<div style='margin-left: 100px; margin-top: 100px;'><a href='log.html'>log file &raquo;</a></div>";
    }
    $db->oops("{$errno} {$errstr}{$emailsent}");
  }
}
// set_error_handler('eh');

function jumpto($page='index.html') {
  header("Location: {$page}");
  exit();
}

?>