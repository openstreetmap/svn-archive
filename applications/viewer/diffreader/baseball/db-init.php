<?php 
$filename = "../diffreader/tennis-edits.csv";
$dbFile = "../diffreader/tennis-edits.db";

if (!file_exists($filename)) die('Missing csv file ' . $filename);

if (file_exists($dbFile)) die('DB already exists ' . $dbFile);

print "Converting CSV file $filename into a new Sqlite3 DB";



if (! $db = new SQLite3($dbFile) ) {
   die ("Failed to open new db file");
}
 
$db->exec("CREATE TABLE edits " .
            "(id INT, " .
             "timestamp VARCHAR(40), " .
             "op_type VARCHAR(10), " .
             "element_type VARCHAR(10), " .
             "osm_id BIGINT, " .
             "user_name VARCHAR(255), " .
             "changeset INT, " .
             "PRIMARY KEY (id) );" );
                 


$row = 1;
if (($handle = fopen($filename, "r")) !== FALSE) {
   while (($data = fgetcsv($handle, 1000, ",")) !== FALSE) {
      $timestamp = $data[0];
      $optype    = $data[1];
      $element   = $data[2];
      $user      = $data[3];
      $changeset = $data[4];
      
      $element_bits = explode(":", $element);
      $element_type = $element_bits[0];
      $element_id = $element_bits[1];
      
      $query = "INSERT INTO edits (timestamp, op_type, element_type, osm_id, user_name, changeset) VALUES ('$timestamp', '$optype', '$element_type', $element_id, \"".sqlite_escape_string($user)."\", $changeset)";
      print $query . "\n";
      $db->exec($query); 
      
      //$user_url = urlencode($user);
      //$user_url = str_replace("+", "%20", $user_url);
   }
   fclose($handle);
}