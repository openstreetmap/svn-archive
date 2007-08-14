<?php
/*+-----------------------------------------------------------------------+
  | Copyright (c) 2006  David Earl                                        |
  | All rights reserved.                                                  |
  |                                                                       |
  | Redistribution and use in source and binary forms, with or without    |
  | modification, are permitted provided that the following conditions    |
  | are met:                                                              |
  |                                                                       |
  | o Redistributions of source code must retain the above copyright      |
  |   notice, this list of conditions and the following disclaimer.       |
  | o Redistributions in binary form must reproduce the above copyright   |
  |   notice, this list of conditions and the following disclaimer in the |
  |   documentation and/or other materials provided with the distribution.|
  | o The names of the authors may not be used to endorse or promote      |
  |   products derived from this software without specific prior written  |
  |   permission.                                                         |
  |                                                                       |
  | THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS   |
  | "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT     |
  | LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR |
  | A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT  |
  | OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, |
  | SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT      |
  | LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, |
  | DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY |
  | THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT   |
  | (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE |
  | OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  |
  |                                                                       |
  +-----------------------------------------------------------------------+
  | Author: David Earl <david@frankieandshadow.com>                       |
  +-----------------------------------------------------------------------+
*/

//  require_once 'PEAR.php';

// further developments: search index support

// ====================================================
class y_db {

  var $version = '2.01';

  /* constructor */ function y_db ($config) {
    $this->db_name     = $config['y_db_name'];
    if (isset($config['y_db_host']))     { $this->db_host =        $config['y_db_host']; }
    $this->db_user     = $config['y_db_user'];
    $this->db_password = $config['y_db_password'];
    if (isset($config['y_db_log']))      { $this->db_log =         $config['y_db_log']; }
    if (isset($config['y_db_backup']))   { $this->db_backup =      $config['y_db_backup']; }
    if (isset($config['y_db_prefix']))   { $this->db_prefix =      $config['y_db_prefix']; }
    if (isset($config['y_db_no_locks'])) { $this->db_no_locks =    $config['y_db_no_locks']; }
    if (isset($config['y_db_always_lock'])) { 
      $this->db_always_lock = $config['y_db_always_lock']; }
    if (isset($config['y_db_debug']))    { $this->db_debug  =      $config['y_db_debug']; }
  }

  // --------------------------------------------------
  var $db_name;
  var $db_host = 'localhost';
  var $db_user;
  var $db_password;
  var $db_log;
  var $db_backup;
  var $db_prefix = '';
  var $db_no_locks = TRUE;
  var $db_always_lock = FALSE;
  var $db_debug = FALSE;

  // --------------------------------------------------
  var $db_serialized = '!SZ!';

  var $tables;
  var $logname;
  var $logfh;
  var $connected = FALSE;
  var $locked = '';
  var $previoushandle = NULL; // for cache of column names in query result
  var $previouscols = NULL;
  var $qualifystart = 0;

  // --------------------------------------------------
  /* public */ function query () {
    return new y_query($this);
  }

  // --------------------------------------------------
  /* public */ function querywrt (&$wrt) {
    $q =& new y_query($this);
    $q->setwrt($wrt);
  }

  // --------------------------------------------------
  /* public */ function select (&$o0)
  {
    $q =& new y_query ($this);
    $n = $q->select($o0);
    $q->_destruct();
    return $n;
  }

  // --------------------------------------------------
  /* public */ function selectjoin (&$o0, &$o1)
  {
    $q =& new y_query ($this);
    $a = array(&$o0, &$o1);
    $n = $q->select($a);
    $q->_destruct();
    return $n;
  }

  // --------------------------------------------------
  /* public */ function info ($oc, $operator) {
    $q =& new y_query ($this);
    $result = $q->info($oc, $operator);
    $q->_destruct();
    return $result;
  }

  // --------------------------------------------------
  /* public */ function delete (&$o, $keys=NULL /* , $keys, ... */) {
    $q =& new y_query ($this);
    $nargs = func_num_args() ;
    if (! is_array($keys)) { $keys = array_slice(func_get_args(), 1, $nargs- 1); }
    $result = $q->delete($o, $keys);
    $q->_destruct();
    return $result;
  }

  // --------------------------------------------------
  /* public */ function update (&$o, $keys /* , $keys ... */) {
    $q =& new y_query ($this);
    if (! is_array($keys)) { 
      $n = func_num_args();
      $keys = array_slice(func_get_args(), 1, $n-1); }
    $result = $q->update($o, $keys);
    $q->_destruct();
    return $result;
  }

  // --------------------------------------------------
  /* public */ function insert (&$o) {
    $numargs = func_num_args();
    if ($numargs > 1) { $oa = func_get_args(); } else { $oa =& $o; }
    $q =& new y_query ($this);
    $result = $q->insert($oa);
    $q->_destruct();
    return $result;
  }

  // --------------------------------------------------
  /* public */ function truncate ($c) { // always a class name
    $q =& new y_query ($this);
    $result = $q->truncate($c);
    $q->_destruct();
    return $result;
  }

  // --------------------------------------------------
  /* public */ function lock ($r='w', $oc=NULL
    /* or any number of objects or class names, or an array of same; 
       if omitted, locks all tables; 
       locks persist until the database is disconnected  */)
  {
    /* LOCK table LOW_PRIORITY WRITE, ect, or READ, ... ; */
    if ($this->db_no_locks) { 
      if ($this->locked != '') { return; /* already locked */ }
      $lockfilename = '/tmp/lock.lock';
      if (! file_exists ($lockfilename)) { fclose(fopen($lockfilename, 'w')); }
      $fp = fopen($lockfilename, 'r');
      if (! flock($fp, LOCK_EX)) { $this->oops("cannot set a lock"); }
      $this->locked = 'w';
      return;
    } 
    $rw = ($r == 'r' ? 'READ' : 'LOW_PRIORITY WRITE');
    if ($this->locked == $r || ($this ->locked == 'w' && $r == 'r')) { return; }
    if (is_null($oc) || is_string($this->locked)) {
      /* if we already locked all tables for read, it is hard to then
         only lock some for write and record it, so do them all */
      if (is_string($this->locked) && ($this->locked == $r || $this->locked == 'w')) { 
        return; // already sufficiently locked
      }
      $qs = 'SHOW TABLES;';
      $handle = $this->run($qs, '-') or $this->oops ("SHOW TABLES failed: ". mysql_error());;
      $numrows = $this->numrows($handle);
      $qs = 'LOCK TABLES ';
      $prefix = '';
      while ($numrows-- > 0) { 
        $row = $this->nextrow($handle); 
        $table =& $row[0];
        if (is_array($this->locked) && isset($this->locked[$table]) && 
            ($this->locked[$table] == $r || $this->locked[$table] == 'w')) {
          continue; // this table already sufficiently locked
        }
        $qs .= "{$prefix}`{$row[0]}` {$rw}";
        $prefix = ', ';
      }
      if ($prefix == '' /* all already locked */) { return; }
      $this->locked = $r;
    } else {
      $numargs = func_num_args();
      if ($numargs == 2 && is_array($oc)) { 
        $os =& $oc;
      } else { 
        $os = func_get_args(); 
        array_splice($os, 0, -1);
      }
      $qs = 'LOCK TABLES ';
      $prefix = '';
      for ($i = 0; $i < count($os); $i++) {
        $omap =& new y_class2db($this, $os[$i]);
        if (isset($this->locked[$omap->tablename]) && 
            ($this->locked[$omap->tablename] == $r || $this->locked[$omap->tablename] == 'w')) {
          continue; // this table already sufficiently locked
        }
        $qs .= "{$prefix}`{$omap->tablename}` {$rw}";
        $prefix = ', ';
      }
      if ($prefix == '' /* all already locked */) { return; }
    }

    $this->run($qs, '-') or $this->oops ("LOCK TABLES failed: ". mysql_error());
  }

  // --------------------------------------------------
  function lockaliases(&$omaps) {
    if ($this->db_no_locks) { return; }
    $qs = 'LOCK TABLES '; 
    $prefix = '';
    for ($i = 0; $i < count($omaps); $i++) {
      $qs .= $prefix . $omaps[$i]->rendertable() . ' READ';
      $prefix = ','; 
    }
    $this->run($qs, 'r');
  }

  // --------------------------------------------------
  function unlockaliases(&$omaps) {
    if ($this->db_no_locks) { return; }
    for ($i = 0; $i < count($omaps); $i++) {
      if (isset($this->locked[$omaps[$i]->tablename])) { 
        $this->locked[$omaps[$i]->tablename] = '-';
      }
    }
    $this->locked = '-';
  }

  // --------------------------------------------------
  /* public */ function tablefromtemplate ($newclass, $templateclass) {
    // todo
  }

  // --------------------------------------------------
  /* protected */ function sqe ($c) { 
    return mysql_escape_string ("{$c}"); 
  }

  // --------------------------------------------------
  /* private */ function connect () {
    // connections are deferred until they are needed
    if (! $this->connected) {
      mysql_connect($this->db_host, $this->db_user, $this->db_password) 
        or $this->oops ('Could not connect : ' . mysql_error());
      mysql_select_db ($this->db_name) 
        or $this->oops ('Could not select database');
      if (isset ($this->db_log) && ! file_exists($this->db_log)) { mkdir($this->db_log); }
      $this->log("CONNECT {$this->version}...");
      $this->connected = TRUE;
    }
  }

  // --------------------------------------------------
  /* protected */ function showtables () {
    if (isset ($this->tables)) { return; }
    $this->tables = array();
    $qs = "SHOW TABLES;";
    $handle = $this->run ($qs, '-');
    $ntables = $this->numrows($handle);
    while ($ntables-- > 0) {
      $tablenames = $this->nextrow($handle);
      $this->tables[$tablenames[0]] = NULL; // values cached later per table - see get_table()
    }
    $this->freeresult($handle);
  }

  // --------------------------------------------------
  /* protected */ function & get_table ($tablename) {
    static $snull = NULL;
    if (! isset ($this->tables)) { $this->showtables(); }
    if (! array_key_exists($tablename, $this->tables)) { return $snull; }
    if (is_null ($this->tables[$tablename])) {
      $qs = "SHOW COLUMNS FROM `{$tablename}`;";
      $handle = $this->run ($qs, '-');
      $ncolumns = $this->numrows($handle);
      $this->tables[$tablename] = array();
      $table =& $this->tables[$tablename];
      while ($ncolumns-- > 0) {
        $row = $this->nextrow($handle); // Field, Type, Null,  Key, Default, Extra  
        $c =& $row[0];
        $table[$c]['type'] = $row[1];
      }
      $this->freeresult($handle);
    }
    return $this->tables[$tablename];
  }

  // --------------------------------------------------
  /* private */ function log ($text) {
    if (isset($this->db_log)) {
      if (! isset ($this->logfh)) {
        $id = session_id();
        if (empty($id)) { 
          $id = 'all';
        } else {
          if (isset($_SESSION['y_db_log'])) {
            $id = $_SESSION['y_db_log'];
          } else {
            $id = date("Y-m-d-H-i-") . $id;
            $_SESSION['y_db_log'] = $id;
          }
        }
        $this->logname = "{$this->db_log}/{$id}.log";
        $this->logfh = fopen ($this->logname, 'a');
      }
      fwrite ($this->logfh, date('Y-m-d:H:i:s')."\n".$text."\n\n");
    }
    if ($this->db_debug) {
      echo htmlspecialchars($text), "<br />\n";
    }
  }

  // --------------------------------------------------
  /* private */ function backup($qs) {
    $lockedname = "{$this->db_backup}/unlocked.lock";
    $unlockedname = "{$this->db_backup}/locked.lock";
    $then = time();
    while (@rename($lockedname, $unlockedname) === FALSE) {
      if (time() - $then >= 2) { $this->oops("waited for lock file more than one second"); }
      usleep(100);
    }
    $filename = "{$this->db_backup}/backup-" . date("Y-m-d") . '.sql';
    if (($fd = @fopen($filename, 'a')) === FALSE) { $this->oops("failed to open {$filename}"); }
    fwrite($fd, $qs);
    fwrite($fd, "\n");
    fclose($fd);
    $this->log("chmod($filename,0666);");
    @chmod($filename,0666); /* may fail if I didn't create it 
                              (cant chmod file with different owner), but that's ok */
    if (@rename($unlockedname, $lockedname) === FALSE) {
      $this->oops("couldn't reinstate backup lock"); 
    }
  }

  // --------------------------------------------------
  /* protected */ function oops($text) {
    $bts = debug_backtrace();
    $btstr = 'OOPS... internal error';
    foreach ($bts as $bt) {
      $btstr .= "\n";
      if (! empty($bt['file'])) { $btstr .= $bt['file'] . ':'; } 
      if (! empty($bt['line'])) { $btstr .= $bt['line']; } 
      $btstr .= ' ---';
    }
    $this->log($text . "\n\n". $btstr);
    if ($this->db_debug) {
      $btstr = print_r($bts, TRUE);
      $btstr = str_replace(' ', '&nbsp;', str_replace("\n", "<br/>\n", htmlspecialchars($btstr)));
      echo $btstr;
    }
    die ("<br /><br />Internal error, sorry.</br />\n");
  }

  // --------------------------------------------------
  /* protected */ function run ($qs, $rw='w', $backup=FALSE) { 
    if ($this->db_always_lock && $rw != '-' /* to prevent recursion in lock or exclude op */) {
      $this->lock($rw);
    }
    $this->log($qs);
    if (! $this->connected) { $this->connect(); }
    if (! ($handle = mysql_query($qs))) {

      if (($e = mysql_error ()) == 'Got error 127 from table handler') {
        // give it another go
        $handle = mysql_query($qs)
        or $this->oops ('internal error: query failed' . mysql_error ());
      }
      if ($handle === FALSE) { 
        $e = mysql_error (); 
        $this->oops ("sql error: {$e}\n"); 
      }
    }
    if ($backup && isset($this->db_backup)) { $this->backup($qs); }
    return $handle;
  }

  // --------------------------------------------------
  function nextrowcol ($handle, &$cols) { 
    // we do it this way rather than by associative array because column names may be 
    // identical in a join (oh why doesn't sql qualify them?)
    $row = mysql_fetch_row ($handle); 
    if ($handle != $this->previoushandle) { 
      $this->previouscols = array();
      $ncols = count($row);
      for ($i = 0; $i < $ncols; $i++) { $this->previouscols[] = mysql_field_name($handle, $i); }
      $this->previoushandle = $handle;
    }
    $cols = $this->previouscols;
    return $row;
  }

  function nextrow ($handle) { return mysql_fetch_row ($handle); }
  function numrows ($handle) { return mysql_num_rows($handle); }
  function setrow ($handle, $rownum) { return mysql_data_seek($handle, $rownum); }
  function affected () { return mysql_affected_rows(); }
  function freeresult ($handle) { mysql_free_result ($handle); }
}

// ====================================================
/* abstract */ class y_op { // produced by where, and, or etc.
  var $op;
  var $args;

  // --------------------------------------------------
  /* static functions for use in where, groupby etc */
  function eq ($f, $vn /* , $v, ... */) { 
    $args = func_get_args();
    return y_op_nary::y_op_nary_expand ('=', 'OR', $args); }
  function ne ($f, $vn /* , $v, ... */) { 
    $args = func_get_args();
    return y_op_nary::y_op_nary_expand ('<>', 'AND', $args); }
  function lt ($f, $v) { return y_op_binary::y_op_binary_fv ('<', $f, $v); }
  function gt ($f, $v) { return y_op_binary::y_op_binary_fv ('>', $f, $v); }
  function le ($f, $v) { return y_op_binary::y_op_binary_fv ('<=', $f, $v); }
  function ge ($f, $v) { return y_op_binary::y_op_binary_fv ('>=', $f, $v); }
  function like ($f, $v) { return y_op_binary::y_op_binary_fv ('LIKE', $f, $v); }
  function isnull ($f) { return new y_op_left ('IS NULL', array(y_op_field::make($f))); }
  function isntnull ($f) { return new y_op_left ('IS NOT NULL', array(y_op_field::make($f))); }
  function between ($f, $v1, $v2) { 
    return new y_op_ternary ('BETWEEN', 'AND', 
      array(y_op_field::make($f), new y_op_value($v1), new y_op_value($v2))); 
  }
  function aand ($yc1 /* , $yc2, ... */) { 
    $args = func_get_args(); 
    if (count($args) == 1 && is_array($args[0])) { $args = $args[0]; } 
    return new y_op_nary ('AND', $args); 
  }
  function oor ($yc1 /* , $yc2, ... */) { 
    $args = func_get_args(); 
    if (count($args) == 1 && is_array($args[0])) { $args = $args[0]; } 
    return new y_op_nary ('OR', $args); 
  }
  function not ($yc) { return new y_op_left ('NOT', array($yc)); }

  function max ($f) { return new y_op_aggregate ('MAX(', array(y_op_field::make($f))); }
  function maxint ($f) { return new y_op_aggregate2 ('MAX(ROUND(', array(y_op_field::make($f))); }
  function min ($f) { return new y_op_aggregate ('MIN(', array(y_op_field::make($f))); }
  function minint ($f) { return new y_op_aggregate2 ('MIN(ROUND(', array(y_op_field::make($f))); }
  function average ($f) { return new y_op_aggregate ('AVG(', array(y_op_field::make($f))); }
  function standard_deviaton ($f) { 
    return new y_op_aggregate ('STD(', array(y_op_field::make($f))); }
  function sum ($f) { return new y_op_aggregate ('SUM(', array(y_op_field::make($f))); }
  function count ($f=NULL) { 
    return new y_op_aggregate ('COUNT(',
                               array(is_null($f) ? new y_op_any_field() : y_op_field::make($f))); 
  }
  function count_distinct ($f) { 
    return new y_op_aggregate ('COUNT(DISTINCT ', array(y_op_field::make($f))); }

  function feq ($f1, $f2) { return y_op_binary::y_op_binary_ff ('=', $f1, $f2); }
  function fne ($f1, $f2) { return y_op_binary::y_op_binary_ff ('<>', $f1, $f2); }
  function flt ($f1, $f2) { return y_op_binary::y_op_binary_ff ('<', $f1, $f2); }
  function fgt ($f1, $f2) { return y_op_binary::y_op_binary_ff ('>', $f1, $f2); }
  function fle ($f1, $f2) { return y_op_binary::y_op_binary_ff ('<=', $f1, $f2); }
  function fge ($f1, $f2) { return y_op_binary::y_op_binary_ff ('>=', $f1, $f2); }

  function field($f, $n=NULL) { return new y_op_field($f, $n); }

  function leftjoin ($yc) { return new y_op_join('LEFT JOIN', array($yc)); }
  function rightjoin ($yc) { return new y_op_join('RIGHT JOIN', array($yc)); }
  function innerjoin () { return new y_op_innerjoin('INNER JOIN', array()); }

  function oprintf($string, $fields) {
    if (func_num_args() > 2) {
      $fields = func_get_args();
      $string = $fields[0];
      array_splice ($fields, 0, 1);
    } else {
      if (! is_array($fields)) { $fields = array($fields); }
    }
    return new y_op_oprintf($string, $fields);
  }

  /* constructor */ function y_op ($op, $args) { $this->op = $op; $this->args = $args; }

  function renderop(&$maps) {
    $prefix = '('; $s = '';
    for ($i = 0; $i < count($this->args); $i++) {
      $a =& $this->args[$i];
      $s .= $prefix . ' ' . $a->renderop($maps);
      $prefix = $this->op;
    }
    $s .= ')';
    return $s;
  }

}

// --------------------------------------------------
class y_op_oprintf extends y_op {
  function y_op_oprintf($string, $args) { parent::y_op($string, $args); }
  function renderop(&$maps) {
    $string = $this->op;
    foreach ($this->args as $field) {
      $map =& $maps[0]->identifymapbyfield($maps, $field, 0);
      $pos = strpos($string, '%f');
      if ($pos === FALSE) { break; }
      $string = substr($string, 0, $pos) . $map->renderfield($field) . substr($string, $pos+2);
    }
    return $string;
  }
}

class y_op_value extends y_op {
  function y_op_value($args) { parent::y_op(NULL, $args); }
  function renderop(&$maps) { return '\'' . y_db::sqe($this->args) . '\' '; }
}

class y_op_field extends y_op {
  var $nmap;
  function y_op_field($args, $nmap=NULL) { parent::y_op(NULL, $args); $this->nmap = $nmap; }
  function renderop(&$maps) { 
    $map =& $maps[0]->identifymapbyfield($maps, $this->args, $this->nmap);
    return $map->renderfield($this->args); 
  }
  /* static */ function make($f) { return is_string($f) ? new y_op_field($f) : $f; }
}

class y_op_class extends y_op {
  function y_op_class($args) { parent::y_op(NULL, $args); }
  function renderop(&$maps) { 
    $map = $maps[0]->identifymapbyclass($maps, $this->args);
    return $map->rendertable(); 
  }
}

class y_op_any_field extends y_op {
  function y_op_any_field() {}
  function renderop(&$maps) { return '*'; }
}

class y_op_left extends y_op {
  function renderop(&$maps) { return '(' . $this->op . $this->args[0]->renderop($maps) .') '; }
}

class y_op_right extends y_op {
  function y_op_right($op, $args) { parent::y_op($op, $args); }
}

class y_op_binary extends y_op {
  function y_op_binary_fv($op, $f, $v) {
    return new y_op_binary ($op, array (y_op_field::make($f), new y_op_value($v)));
  }
  function y_op_binary_ff($op, $f1, $f2) {
    return new y_op_binary ($op, array (y_op_field::make($f1), y_op_field::make($f2)));
  }
}

class y_op_ternary extends y_op {
  var $op2;
  function y_op_ternary($op1, $op2, $args) { parent::y_op($op1, $args); $this->op2 = $op2; }
  function renderop(&$maps) { 
    return '(' . $this->args[0]->renderop($maps) . $this->op . ' ' . $this->args[1]->renderop($maps) . 
           $this->op2 . ' ' . $this->args[2]->renderop($maps) . ') '; 
  }
}

class y_op_nary extends y_op {
  /* static */ function y_op_nary_expand ($op, $junction, $args) {
    // either { f, { v, v, ... } } or { f , v, v, ... }
    $subargs = array();
    $opfield = y_op_field::make($args[0]);
    $values = $args[1];
    if (! is_array($values)) { $values = $args; array_splice($values, 0, 1); }
    for ($i = 0; $i < count($values); $i++) { 
      $subargs[] = new y_op_binary($op, array(&$opfield, new y_op_value($values[$i])));
    } 
    return count($values) == 1 ? $subargs[0] : new y_op_nary($junction, $subargs);
  }
  function renderop(&$maps) { 
    $s = '(';
    $prefix = '';
    for ($i = 0; $i < count($this->args); $i++) {
      $s .= $prefix . $this->args[$i]->renderop($maps);
      $prefix = $this->op;
    }
    return $s . ')';
  }
}

class y_op_aggregate extends y_op {
  function renderop(&$maps) { return $this->op . $this->args[0]->renderop($maps) . ') '; }
}
class y_op_aggregate2 extends y_op {
  function renderop(&$maps) { return $this->op . $this->args[0]->renderop($maps) . ')) '; }
}

class y_op_join extends y_op {
  function withclass (&$o) { $this->args[1] = new y_op_class(get_class($o)); }
  function renderop(&$maps) {
    return $this->op . ' ' . $this->args[1]->renderop($maps) .
      ' ON ' . $this->args[0]->renderop($maps) . ' ';
  }
}

class y_op_innerjoin extends y_op_join {
  function renderop(&$maps) {
    return $this->op . ' ' . $this->args[1]->renderop($maps);
  }
}

// ====================================================
class y_class2db {

  var $db;
  var $o = NULL;
  var $qualified = 0; // whether to prefix field names with table names
  var $classname;
  var $tablename; // including any general prefix
  var $fieldmap;  // class fieldname => database column name
  var $table;

  // --------------------------------------------------
  /* constructor */ function y_class2db (&$db, &$oc) {
    $this->db =& $db;
    if (is_object($oc)) { 
      $this->o =& $oc;
      $this->classname = get_class($this->o);
    } else {
      $this->o = NULL;
      $this->classname = $oc;
    }
    $this->db->showtables(); // only does anything if not already cached
    // what is the table called
    $this->constructtablename();
    // what does it look like
    $this->table =& $this->db->get_table($this->tablename);
    // how do the class fields map to it?
    $this->constructfieldmap();
  }

  // --------------------------------------------------
  /* public */ function get_classname() { return $this->classname; }

  // --------------------------------------------------
  /* public */ function qualify($index) { $this->qualified = ++$this->db->qualifystart; }

  // --------------------------------------------------
  /* private */ function constructtablename () {
    if (is_object ($this->o) && method_exists($this->o, 'y_table')) {
      $this->tablename = $this->db->db_prefix . $this->o->y_table();
      $this->table =& $this->db->get_table($this->tablename);
    } else if (! is_object($this->o) && ! empty($this->o->classname) && 
               (is_array($cm = get_class_methods($this->o->classname))) && 
               in_array('y_table', $cm))
    {
      $this->tablename = $this->db->db_prefix . eval ("return {$this->classname}::y_table();");
      $this->table =& $this->db->get_table($this->tablename);
    } else {
      $classname = $this->classname;
      do {
        $this->tablename = $this->db->db_prefix . $classname;
        $this->table =& $this->db->get_table($this->tablename);
        if (! is_null($this->table)) { break; }
        $classname = get_parent_class ($classname);
      } while ($classname !== FALSE);
    }
    if (is_null ($this->table)) { $this->db->oops("no table found for {$this->classname}"); }
  }

  // --------------------------------------------------
  /* private */ function constructfieldmap () {
    if (is_object($this->o) && method_exists($this->o, 'y_fields')) {
      $fieldmap = $this->o->y_fields();
    } else if (! is_object($this->o) && 
               in_array('y_fields', get_class_methods($this->classname))) 
    {
      $fieldmap = eval("return {$this->classname}::y_fields();");
    } else {
      $classvars = get_class_vars($this->classname);
      foreach ($classvars as $f => $v) { $fieldmap[$f] = $f; }
    }
    foreach ($fieldmap as $f => $c) {
      if (array_key_exists ($c, $this->table)) {
        $this->fieldmap[$f] =  $c;
      }
    }
  }

  // --------------------------------------------------
  /* protected */ function xrefs ($classname) {
    $xrefs = NULL;
    if (is_object($this->o) && method_exists($this->o, 'y_xrefs')) {
      $xrefs = $this->o->y_xrefs($classname);
    } else if (! is_object($this->o) && 
               in_array('y_xrefs', get_class_methods($this->classname))) 
    {
      $xrefs = eval("return {$this->classname}::y_xrefs(\$classname);");
    }
    return $xrefs;
  }

  // --------------------------------------------------
  /* protected */ function is_allfields() {
    return count($this->table) == count($this->fieldmap);
  }

  // --------------------------------------------------
  /* protected */ function is_column($field) {
    return array_key_exists ($field, $this->fieldmap);
  }

  // --------------------------------------------------
  /* protected */ function column_to_field($column) {
    return array_search($column, $this->fieldmap);
  }

  // --------------------------------------------------
  /* field iterator functions, used as
       for($map->field_all(); $map->field_more(); $map->field_next()) {
         $map->field_whatever(...);
       }
  */
  function field_all() { reset($this->fieldmap); }
  function field_more() { return current($this->fieldmap) !== FALSE; }
  function field_next() { next($this->fieldmap); }
  function field_current() { return key($this->fieldmap); }

  // --------------------------------------------------
  function is_set($field=NULL) { 
    if (is_null($field)) { $field = key($this->fieldmap); }
    return isset($this->o->$field);
  }

  // --------------------------------------------------
  function among(&$keys, $field=NULL) { 
    if (is_null($field)) { $field = key($this->fieldmap); }
    return ! is_array($keys) || in_array($field, $keys);
  }

  // --------------------------------------------------
  /* private */ function setfieldtype($field) {
    static $types = array('varchar'=>'string', 'tinyint'=>'integer', 'text'=>'string', 
      'date'=>'integer', 'smallint'=>'integer', 'mediumint'=>'integer', 'int'=>'integer', 
      'bigint'=>'integer', 'float'=>'float', 'double'=>'float', 'decimal'=>'float',
       'datetime'=>'integer', 'timestamp'=>'integer', 'time'=>'integer',
      'year'=>'integer', 'char'=>'string', 'tinyblob'=>'string', 'tinytext'=>'integer', 
      'blob'=>'string', 'mediumblob'=>'string', 'mediumtext'=>'string', 'longblob'=>'string',
      'longtext'=>'string', 'enum'=>'integer', 'set'=>'integer', 'bool'=>'boolean');

    if (is_string($this->o->$field)) {
      $fieldtype = $this->table[$this->fieldmap[$field]]['type']; // from the db column description
      $n = strpos($fieldtype, '(');
      if ($n !== FALSE) { $fieldtype = substr($fieldtype,0,$n); } // remove the size in parentheses
      $fieldtype = $types[$fieldtype]; // look up the php equivalent type
      settype($this->o->$field, $fieldtype);
    }
  }

  // --------------------------------------------------
  function setvalue($value, $field=NULL) {
    if (is_null($field)) { $field = key($this->fieldmap); }
    $lsz = strlen($this->db->db_serialized);
    if (substr($value, 0, $lsz) == $this->db->db_serialized) {
      if ($value{$lsz} == 'O') {
        // it is a serialized object: 'O:n:"nchars-classname":m:{...}'
        $q1 = strpos($value, '"');
        $q2 = strpos($value, '"', $q1+1);
        $classname = substr($value, $q1+1, $q2-$q1-1);
        if (! class_exists($classname)) {
          //load the class either with assistance, or by naming convention
          if (is_object($this->o) && method_exists($this->o, 'y_include')) {
            $this->o->y_include($classname);
          } else if (! is_object($this->o) && 
                     in_array('y_include', get_class_methods($this->classname))) 
          {
            eval("{$this->classname}::y_include(\$classname);");
          } else {
            include_once("{$classname}.php");
          }
        }
      }
      $value = unserialize(substr($value, $lsz));
    }
    $this->o->$field = $value;
    $this->setfieldtype($field);
  }

  // --------------------------------------------------
  function countfields() { return count($this->fieldmap); }

  // --------------------------------------------------
  function get_field() { return key($this->fieldmap); }

  // --------------------------------------------------
  function rendervalue($field=NULL) {
    if (is_null($field)) { $field = key($this->fieldmap); }
    $value =& $this->o->$field;
    if (is_array($value) || is_object($value)) {
      return $this->db->db_serialized . serialize($value);
    }
    return $value;
  }

  // --------------------------------------------------
  /* protected */ function renderfield($field=NULL) {
    if (is_null($field)) { $field = key($this->fieldmap); }
    if ($this->qualified) { $s = "yt{$this->qualified}."; } else { $s = ''; }
    return "{$s}`{$this->fieldmap[$field]}`";
  }

  // --------------------------------------------------
  /* protected */ function rendertable() {
    $s = "`{$this->tablename}`";
    if ($this->qualified > 0) { $s .= " AS yt{$this->qualified} "; }
    return $s;
  }

  // --------------------------------------------------
  /* protected */ function rendertablealias() {
    return $this->qualified ? "yt{$this->qualified}" : "`{$this->tablename}`";
  }

  // --------------------------------------------------
  /* static */ function & identifymapbyfield(&$maps, $field, $nmap=NULL) {
    if (is_null($nmap)) {
      $start = 0; $end = count($maps);
    } else {
      $start = $nmap; $end = $nmap + 1;
    }
    for ($i=$start; $i < $end; $i++) {
      $map =& $maps[$i];
      if ($map->is_column($field)) {
        if (isset($resultmap)) { $this->db->oops ("ambiguous field '{$field}'"); }
        $resultmap =& $map;
      }
    }
    if (! isset($resultmap)) { $this->db->oops ("unknown column for field '{$field}'"); }
    return $resultmap;
  }

  // --------------------------------------------------
  /* static */ function & identifymapbyclass(&$maps, $classname) {
    for ($i=0; $i < count($maps); $i++) {
      $map =& $maps[$i];
      if ($map->classname == $classname) {
        return $map;
      }
    }
    $this->db->oops ("unidentified class '{$classname}'");
  }

}

// ====================================================
class y_query {

  var $db;
  var $handle;
  var $qs;
  var $nrowstotal;
  var $nrowsremaining;
  var $nrowcurrent = 0;
  var $wmap;
  var $omaps;
  var $joins;

  var $aggregates = NULL;
  var $condition = NULL;
  var $orderfield = NULL;
  var $orderdirection = NULL;
  var $limitstart = NULL;
  var $limitcount = NULL;
  var $distinct = FALSE;
  var $groupby = '';
  var $doinggroupby = FALSE;
  var $doinginfo = FALSE;

  // --------------------------------------------------
  /* don't create a y_query directly - use $db->query() */
  /* protected constructor */ function y_query (&$db) {
    $this->db =& $db;
  }

  // --------------------------------------------------
  function setwrt (&$wrt) {
    if (is_object($wrt)) { 
      $this->wmap =& new y_class2db ($this->db, $wrt);
    }
  }

  // --------------------------------------------------
  /* protected */ function _destruct() {
    if (isset($this->handle)) {
      $this->db->freeresult($this->handle);
      unset($this->handle);
    }
  }

  /* private */ function setomaps(&$oc) {
    if (! isset ($this->omaps)) { 
      $this->omaps = array(); 
      $this->omaps[0] =& new y_class2db ($this->db, $oc);
    }
  }

  /* private */ function addomaps(&$o) {
    $map =& new y_class2db ($this->db, $o);
    if (count($this->omaps) > 0 && $this->omaps[0]->qualified > 0) { 
      $map->qualify(count($this->omaps)); 
    }
    $this->omaps[] =& $map;
  }

  // --------------------------------------------------
  /* public */ function where ($yop)
  {
    $this->condition = $yop;
  }

  // --------------------------------------------------
  /* private */ function ordering ($field /* or array of fields/y_op_printfs */, $direction) {
    if (is_null ($this->orderfield)) { 
      $this->orderfield = array(); 
      $this->orderdirection = array(); 
    }
    if (is_array ($field)) {
      foreach ($field as $fieldfield) { 
        $this->orderfield[] = $fieldfield; 
        $this->orderdirection[] = $direction; 
      }
    } else {
      $this->orderfield[] = $field; 
      $this->orderdirection[] = $direction; 
    }
  }

  // --------------------------------------------------
  /* public */ function ascending ($field /* or array of fields or y_op_printf */) {
    if (func_num_args() > 1) { $field = func_get_args(); }
    $this->ordering($field, 1);
  }

  // --------------------------------------------------
  /* public */ function descending ($field /* or array of fields */) {
    if (func_num_args() > 1) { $field = func_get_args(); }
    $this->ordering($field, -1);
  }

  // --------------------------------------------------
  /* public */ function ascendingnumber ($field /* or array of fields */) {
    if (func_num_args() > 1) { $field = func_get_args(); }
    $this->ordering($field, 2);
  }

  // --------------------------------------------------
  /* public */ function descendingnumber ($field /* or array of fields */) {
    if (func_num_args() > 1) { $field = func_get_args(); }
    $this->ordering($field, -2);
  }

  // --------------------------------------------------
  /* public */ function limit ($count, $start=0) {
    $this->limitcount = $count;
    $this->limitstart = $start;
  }

  // --------------------------------------------------
  /* public */ function info ($oc, $yop) {
    /* SELECT operator(field) FROM table WHERE condition (operator such as MAX aka $db->db_max) */
    $this->setaggregates($yop);
    $this->doinginfo = TRUE;
    $this->select($oc);
    if ($this->nrowstotal != 1) { $this->db->oops("{$operator} no result"); }
    $result = $this->db->nextrow($this->handle);
    return $result[0];
  }

  // --------------------------------------------------
  /* public */ function groupby ($fieldops 
    /* either a mixture of fields and yops (for operations), or a single array of same */) 
  {
    /* SELECT field1, ..., MAX(field2), ... FROM table WHERE condition GROUP BY field1, ...
       call select() to get results */
    // just save them until we are ready tomake the SELECT
    if (! is_array($fieldops)) { $fieldops = func_get_args(); }
    $this->setaggregates($fieldops);
    $this->doinggroupby = TRUE;
  }

  // --------------------------------------------------
  /* private */ function setaggregates ($aggregates) {
    if (! is_array($aggregates)) { $aggregates = array($aggregates); }
    if (is_array($this->aggregates)) {
      $this->aggregates = array_merge($this->array_merge, $aggregates);
    } else {
      $this->aggregates = $aggregates;
    }
  }

  // --------------------------------------------------
  /* public */ function selectjoin(&$o0, &$o1) {
    $a = array(&$o0, &$o1);
    return $this->select($a);
  }

  // --------------------------------------------------
  /* public */ function selectjoin3(&$o0, &$o1, &$o2) {
    $a = array(&$o0, &$o1, &$o2);
    return $this->select($a);
  }

  // --------------------------------------------------
  /* public */ function select (&$oa)
  {
    /* note: very important that $o's are retained by reference for
       this to work, hence not using func_get_args; but it does mean
       for multiple operands you have to pass an array in which
       references are retains (array(&$o1,&$o2,...)) */
    if (is_array($oa)) { $o0 =& $oa[0]; } else { $o0 =& $oa; }
    $this->setomaps($o0);
    if (count($oa) > 1 && count($this->omaps) == 1) {
      // for joins only
      $this->omaps[0]->qualify(0);
      $nmap = 1;
      $naliases = count($oa);
      for($i = 1; $i < $naliases; $i++) {
        $oi =& $oa[$i];
        if (is_a($oi, 'y_op_join')) {
          $j = count($this->omaps);
          $this->joins[$j] =& $oi;
          $this->joins[$j]->withclass($oa[$i+1]);
        } else { 
          $this->addomaps($oi); 
        }
      }
    }
    if (! isset ($this->qs)) {
      $this->qs = count($this->omaps) > 1 ? $this->renderselectjoin() : $this->renderselect();
    }
    if (! isset ($this->handle)) {
      if (isset($naliases)) { $this->db->lockaliases(&$this->omaps); }
      $this->handle = $this->db->run($this->qs, 'r');
      $this->nrowstotal = $this->nrowsremaining = $this->db->numrows ($this->handle);
      $this->nrowcurrent = 0;
      if (isset($naliases)) { $this->db->unlockaliases(&$this->omaps); }
    }
    if ($this->doinginfo) { return; }
    if ($this->nrowsremaining > 0) {
      $result = $this->db->nextrowcol($this->handle, $columns);
      $this->assignresult($result, $columns);
      $this->nrowsremaining--;
      $this->nrowcurrent++;
      return $this->nrowsremaining + 1;
    } else {
      return 0;
    }
  }

  // --------------------------------------------------
  /* private */ function assignresult (&$result, &$columns) {
    $rc = 0; $nmap = 0;
    $map =& $this->omaps[0];
    $nfields = $map->countfields();
    $nresults = count($result);
    for ($i = 0; $i < $nresults; $i++) {
      $column =& $columns[$i];
      $value =& $result[$i];
      if (! isset($this->aggregates) && $rc >= $nfields) {
        $rc = 0; $nmap++;
        $map =& $this->omaps[$nmap];
        $nfields = $map->countfields();
      }
      $field = $map->column_to_field($column);
      if ($field === FALSE && isset ($this->aggregates)) {
        if (strpos($column, '(') !== FALSE) { 
          // extract the column name
          preg_match ('/^\s*[A-Z]+\s*\(\s*`([^`]+)`\s*\)\s*$/', $column, $matches);
          $field = $map->column_to_field($matches[1]);
          if ($field !== FALSE) { 
            if (! isset($aggregateresults)) { $aggregateresults = array(); }
            if (! isset($aggregateresults[$field])) {
              $aggregateresults[$field] = $value;
            } else if (is_array($aggregateresults[$field])) {
              $aggregateresults[$field][] = $value;
            } else {
              $aggregateresults[$field] = array($aggregateresults[$field], $value);
            }
          }
        }
      } else {
        $map->setvalue($value, $field);
      }
      $rc++;
    }

    if (isset($aggregateresults)) {
      foreach ($aggregateresults as $field => $value) { $map->setvalue($value, $field); }
    }
  }

  // --------------------------------------------------
  /* public */ function distinct ($f=NULL) {
    if (! is_null($f)) { $this->setaggregates(is_array($f) ? $f : func_get_args()); }
    $this->distinct = TRUE;
  }

  // --------------------------------------------------
  /* public */ function delete ($oc, $keys=NULL /* , $keys, ... */ ) { // -> number of records deleted
    /* DELETE FROM table WHERE condition ; */

    $this->setomaps($oc);
    if (! isset ($this->qs)) {
      if (! is_array($keys) && ! is_null($keys)) {
        $n = func_num_args() - 1;
        $keys = array_slice(func_get_args(), 1, $n); 
      }
      $ws = $this->renderwhere($keys);
      if (empty ($ws)) { $this->db->oops ("delete: no condition - would delete all!"); }
      $this->qs = 'DELETE FROM ' . $this->omaps[0]->rendertable() . " {$ws} ;";
    }
    $this->db->run($this->qs, 'w', TRUE);
    return $this->db->affected();
  }

  // --------------------------------------------------
  /* public */ function update ($o, $keys /* , $keys, ...*/) {
    /* UPDATE table SET col=val, col=val, ... WHERE condition ; */

    $this->setomaps($o);
    if (! isset ($this->qs)) {
      if (! is_array($keys)) { 
        $n = func_num_args() - 1; 
        $keys = array_slice(func_get_args(), 1, $n); 
      }
      $ws = $this->renderwhere($keys);
      if (empty ($ws)) { $this->db->oops ("update: no condition - would update all!"); }
      $this->qs = 'UPDATE ' . $this->omaps[0]->rendertable();
      $prefix = ' SET ';
      $map =& $this->omaps[0];
      for ($map->field_all(); $map->field_more(); $map->field_next()) {
        if ($map->is_set()) {
          $this->qs .= $prefix . $map->renderfield() . "='" . y_db::sqe($map->rendervalue()) . "'";
          $prefix = ', ';
        }
      }
      $this->qs .= " {$ws} ;";
    }
    $this->db->run($this->qs, 'w', TRUE);
    return $this->db->affected();    
  }

  // --------------------------------------------------
  /* public */ function insert (&$o /* or multiple parameters, or array of objects */) {

    $numargs = func_num_args();
    if ($numargs > 1) { 
      $os = func_get_args(); 
    } else if (is_array($o)) { 
      $os =& $o;
    }

    if (! isset($os)) {
      $this->setomaps($o);
      return $this->insertsimple(0);
    } 

    /* if all the more than one object in our hand are all the same
       class and all have the same subset of fields set, we can do it
       in one more efficient operation, so it is worth a check */

    $this->setomaps($os[0]);
    // build a list of used fields in the first object

    $map =& $this->omaps[0];
    for($i = 1; $i < count($os); $i++) {
      $this->addomaps($os[$i]);
      $mapi =& $this->omaps[$i];
      if ($mapi->get_classname() != $map->get_classname()) { break; }
      $map->field_all(); $mapi->field_all();
      while ($map->field_more()) {
        if ($map->is_set() !== $mapi->is_set()) { break 2; }
        $map->field_next(); $mapi->field_next();
      }
    }

    // so were they all the same?
    if ($i == count($os)) {
      return $this->insertcompound();
    }

    // we may have missed some omaps if we bailed the loop above early
    for($i++; $i < count($os); $i++) {
      $this->addomaps($os[$i]);
    }

    // separate inserts
    $count = 0;
    for ($i = 0; $i < count($os); $i++) { $count += $this->insertsimple($i); }
    return $count;
  }

  // --------------------------------------------------
  /* private */ function insertsimple ($i) {
    /* INSERT table SET col=val, col=val, ... ; */

    $map =& $this->omaps[$i];
    $qs = 'INSERT ' . $map->rendertable();
    $prefix = ' SET ';
    for($map->field_all(); $map->field_more(); $map->field_next()) {
      if ($map->is_set()) {
        $qs .= $prefix . $map->renderfield() . "='" . y_db::sqe($map->rendervalue()) . "'";
        $prefix = ', ';
      }
    }
    $qs .= ';';
    $this->db->run($qs, 'w', TRUE);
    return $this->db->affected();
  }

  // --------------------------------------------------
  /* private */ function insertcompound () {
    /* INSERT table (col, col, ...) VALUES (val, val, ...), (val, val, ...)  */

    $map =& $this->omaps[0];
    $qs = 'INSERT ' . $map->rendertable();
    $prefix = '(';
    for($map->field_all(); $map->field_more(); $map->field_next()) {
      if ($map->is_set()) {
        $qs .= $prefix . $map->renderfield();
        $prefix = ',';
      }
    }
    $qs .= ') VALUES ';
    $prefix = '(';
    for ($i = 0; $i < count($this->omaps); $i++) {
      $map =& $this->omaps[$i];
      $qs .= $prefix;
      $prefix = ',(';
      $prefix2 = '';
      for($map->field_all(); $map->field_more(); $map->field_next()) {
        if ($map->is_set()) {
          $qs .= $prefix2 . "'" . y_db::sqe($map->rendervalue()) . "'";
          $prefix2 = ', ';
        }
      }
      $qs .= ")";
    }
    $qs .= ";";
    $this->db->run($qs, 'w', TRUE);
    return $this->db->affected();
  }

  // --------------------------------------------------
  function truncate($c) {
    // TRUCATE <tablename>
    $this->setomaps($c);
    if (! isset ($this->qs)) {
      $this->qs = 'TRUNCATE ' . $this->omaps[0]->rendertable() . ';';
    }
    $this->db->run($this->qs, 'w', TRUE);
    return $this->db->affected();
  }

  // --------------------------------------------------
  /* private */ function renderaggregates () {
    $qs = '';
    $prefix = '';
    $this->groupby = '';
    $numoperands = count($this->aggregates);
    $plain = array();
    for ($i = 0; $i < count($this->aggregates); $i++) {
      $a =& $this->aggregates[$i];
      if (is_a($a, 'y_op')) { 
        $qs .= $prefix . $a->renderop($this->omaps);
      } else {
        $fop = new y_op_field($a);
        $fops = $fop->renderop($this->omaps);
        $qs .= "{$prefix}{$fops}";
        if ($this->doinggroupby) { $this->groupby = "{$prefix}{$fops}"; }
      }
      $prefix = ',';
    }
    return $qs;
  }

  // --------------------------------------------------
  /* private */ function renderselect ()
  {
    /* SELECT [DISTINCT] f1, f2, ... FROM table [WHERE condition] 
              [ORDER BY f1 [DESC], f2 [DESC], ... ]] [LIMIT [offset, ] rows] */

    $qs = 'SELECT ';
    $nmaps = count($this->omaps);
    $map =& $this->omaps[0];

    if ($this->distinct) { $qs .= 'DISTINCT '; }

    // return which fields?
    if (isset($this->aggregates)) {
      $qs .= $this->renderaggregates();
    } else if ($map->is_allfields()) {
      $qs .= '*';
    } else {
      // subset of fields
      $prefix = '';
      for($map->field_all(); $map->field_more(); $map->field_next()) {
        $qs .= $prefix . $map->renderfield();
        $prefix = ','; 
      }
    }

    // from which table?
    $qs .= ' FROM ' . $map->rendertable() . ' ';

    // any 'where' condition?
    $qs .= $this->renderwhere ();

    if (! empty($this->groupby)) { $qs .= "GROUP BY {$this->groupby} "; }

    // any order specified?
    if (is_array($this->orderfield)) {
      $prefix = 'ORDER BY ';
      $norder = count($this->orderfield);
      for ($i = 0; $i < $norder; $i++) {
        if (is_a($this->orderfield[$i], 'y_op')) {
          $qs .= $prefix . $this->orderfield[$i]->renderop($this->omaps);
        } else {
          $qs .= $prefix . $map->renderfield($this->orderfield[$i]);
        }
        if (abs($this->orderdirection[$i]) == 1) { $qs .= ' '; } else { $qs .= '-0 '; }
        if ($this->orderdirection[$i] < 0) { $qs .= 'DESC '; }
        $prefix = ', ';
      }
    }

    // any limit on the number of results?
    if (is_int($this->limitstart) && is_int($this->limitcount)) {
      $qs .= "LIMIT {$this->limitstart}, {$this->limitcount}";
    }

    $qs .= ';';
    return $qs;
  } // end of renderselect


  // --------------------------------------------------
  /* private */ function renderselectjoin ()
  {
    /* SELECT [DISTINCT] a1.f1, a1.f2, ... FROM t1 AS a1 [, t2 AS a2 | 
                LEFT/RIGHT JOIN t2 ON condition]
              [WHERE condition] 
              [ORDER BY a1.f1 [DESC], a1.f2 [DESC], ... ]] [LIMIT [offset, ] rows] */

    $qs = 'SELECT ';

    if ($this->distinct) { $qs .= 'DISTINCT '; }

    // return which fields - always qualified by table name
    if (isset($this->aggregates)) {
      $qs .= $this->renderaggregates();
    } else {
      $prefix = '';
      for ($i = 0; $i < count($this->omaps); $i++) {
        $map =& $this->omaps[$i];
        if ($map->is_allfields()) {
          $qs .= $prefix . $map->rendertablealias() . '.*';
          $prefix = ',';
        } else {
          for($map->field_all(); $map->field_more(); $map->field_next()) {
            $qs .= $prefix . $map->renderfield();
            $prefix = ','; 
          }
        }
      }
    }

    // from which tables/joins?
    $prefix = ' FROM ';
    for ($i = 0; $i < count($this->omaps); $i++) {
      if (isset($this->joins[$i])) {
        $qs .= $this->joins[$i]->renderop($this->omaps);
      } else {
        $qs .= $prefix . $this->omaps[$i]->rendertable();
      }
      $prefix = ',';
    }    

    // any 'where' condition?
    $qs .= $this->renderwhere ();

    if (! empty($this->groupby)) { $qs .= "GROUP BY {$this->groupby} "; }

    // any order specified?
    if (is_array($this->orderfield)) {
      $prefix = 'ORDER BY ';
      $norder = count($this->orderfield);
      for ($i = 0; $i < $norder; $i++) {
        $field =& $this->orderfield[$i];
        if (is_a($field, 'y_op')) { 
          $qs .= $prefix . $field->renderop($this->omaps);
        } else {
          $map =& $this->omaps[0]->identifymapbyfield($this->omaps, $field);
          $qs .= $prefix . $map->renderfield ($field);
        }
        if (abs($this->orderdirection[$i]) == 1) { $qs .= ' '; } else { $qs .= '-0 '; }
        if ($this->orderdirection[$i] < 0) { $qs .= 'DESC '; }
        $prefix = ', ';
      }
    }

    // any limit on the number of results?
    if (is_int($this->limitstart) && is_int($this->limitcount)) {
      $qs .= "LIMIT {$this->limitstart}, {$this->limitcount}";
    }

    $qs .= ';';
    return $qs;
  } // end of renderselect

  // --------------------------------------------------
  /* private */ function renderwhere ($keys=NULL) {
    $ws = '';
    $prefix = 'WHERE ';

    if (! is_null($keys)) {
      /* check the keys (an array of strings) are actually valid -
      otherwise we might end up losing a key and updating more than we
      intended. Only applies to single objects (not joins). */
      if (count($this->omaps) > 1) { $this->db->oops ('where keys dont apply to joins'); }
      $nkeys = count($keys);
      for ($i = 0; $i < $nkeys; $i++) {
        $keys[$i] = trim($keys[$i]);
        if (! $this->omaps[0]->is_column($keys[$i])) {
          $this->db->oops ("where key does not exist: `{$keys[$i]}`");
        }
      }
    }

    // basic fields, limited by keys if necessary
    for($i = 0; $i < count($this->omaps); $i++) {
      $map =& $this->omaps[$i];
      for($map->field_all(); $map->field_more(); $map->field_next()) {
        if (! $map->is_set()) { continue; }
        if (! $map->among($keys)) { continue; }
        $ws .= $prefix . '(' . $map->renderfield() . "='" . y_db::sqe($map->rendervalue()) . "') ";
        $prefix = 'AND ';
      }
    }

    // any explicit conditions arising from 'where' method calls
    if (! empty ($this->condition)) {
      $ws .= $prefix . $this->condition->renderop($this->omaps);
      $prefix = 'AND ';
    }

    if (count($this->omaps) > 1 /* a join */ &&
        ! isset($this->joins) /* no explicit conditions */) 
    {
      // a natural join, on the cross referenced keys in the objects 
      // Do them in pairs, left to right
      for ($i = 1; $i < count($this->omaps); $i++) {
        $mapa =& $this->omaps[$i-1];
        $mapb =& $this->omaps[$i];
        if (! is_null($xrefs = $mapa->xrefs($mapb->get_classname()))) {
          // explicit outward references (a to b)
          foreach ($xrefs as $xfrom => $xto) {
            $ws .= $prefix .'('. $mapa->renderfield ($xfrom) ."=". $mapb->renderfield($xto) .')';
            $prefix = 'AND ';
          }
        } else if (! is_null($xrefs = $mapb->xrefs($mapa->get_classname()))) {
          // explicit inward references
          foreach ($xrefs as $xfrom => $xto) {
            $ws .= $prefix .'('. $mapa->renderfield($xto) ."=". $mapb->renderfield($xfrom) .')';
            $prefix = 'AND ';
          }
        } else {
          // do it by naming convention
          $classnamea = $mapa->get_classname() . '_';
          for($mapb->field_all(); $mapb->field_more(); $mapb->field_next()) {
            $field = $mapb->get_field();
            $keyb = $mapb->get_classname() . '_' . $field;
            if ($mapa->is_column ($keyb)) {
              $ws .= $prefix .'('. $mapa->renderfield($keyb) .'='. $mapb->renderfield() .')';
              $prefix = 'AND ';
            } else if (substr($field, 0, strlen($classnamea)) == $classnamea) {
              $keya = substr ($field, strlen($classnamea));
              if ($mapa->is_column($keya)) {
                $ws .= $prefix .'('. $mapa->renderfield($keya) .'='. $mapb->renderfield() .')';
                $prefix = 'AND ';
              }
            }
          }
        }
      }
    }

    // and any 'wrt' - very similar to the above, but using the value of wrt fields
    if (isset($this->wmap)) {
      $mapo =& $this->omaps[0];
      $mapw =& $this->wmap;
      if (! is_null($xrefs = $mapo->xrefs($mapw->get_classname()))) {
        // explicit outward references
        foreach ($xrefs as $xfrom => $xto) {
          $ws .= $prefix .'('. $mapo->renderfield ($xfrom) . "='" .
            y_db::sqe($mapw->rendervalue($xto))."') ";
          $prefix = 'AND ';
        }
      } else if (! is_null($xrefs = $mapw->xrefs($mapo->get_classname()))) {
        // explicit inward references
        foreach ($xrefs as $xfrom => $xto) {
          $ws .= $prefix .'('. $mapo->renderfield($xto) . "='" .
            y_db::sqe($mapw->rendervalue($xfrom))."') ";
          $prefix = 'AND ';
        }
      } else {
        // do it by naming convention
        $classnameo = $mapo->get_classname() . '_';
        for ($mapw->field_all(); $mapw->field_more(); $mapw->field_next()) {
          if ($mapw->is_set()) {
            $field = $mapw->get_field();
            $keyw = $mapw->get_classname() . '_' . $field;
            if ($mapo->is_column($keyw)) {
              $ws .= $prefix .'('. $mapo->renderfield($keyw) . "='" . 
                    y_db::sqe($mapw->rendervalue()) . "') ";
              $prefix = 'AND ';
            } else if (substr($field, 0, strlen($classnameo)) == $classnameo) {
              $keyo = substr ($field, strlen($classnameo));
              if ($mapo->is_column($keyo)) {
                $ws .= $prefix .'('. $mapo->renderfield($keyo) . "='" . 
                      y_db::sqe($mapw->rendervalue()) . "') ";
                $prefix = 'AND ';
              }
            }
          }
        }
      }
    }
    return $ws;
  }

}

?>