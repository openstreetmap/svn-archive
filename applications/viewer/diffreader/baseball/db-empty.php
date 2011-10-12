<?php 
$dbFile = "../diffreader/empty-edits.db";

if (!file_exists($dbFile)) die('DB doesnt exist ' . $dbFile);

if (! $db = new SQLite3($dbFile) ) {
   die ("Failed to open new db file");
}
 
$db->exec("DELETE FROM edits;");