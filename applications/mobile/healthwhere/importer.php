#!/usr/bin/env php
<?
$import_log = "/dev/null";
$tmp_database = "healthware.tmp.db";
$database = "healthware.db";
$old_database = "old_healthware.db";
$data_url = "http://www.npemap.org.uk/data/fulllist";
$data_local = "fulllist";
chdir ("/home/russ/vhosts/mappage.org/hw/dev/");

/*
 * Function to call when there is a fatal error
 * Parameters:
 * $log_string is saved in log
 * Returns nothing
*/
function death ($log_string) {
	global $import_log;

	file_put_contents ($import_log, date ("Y-m-d H:i:s") . "\t$log_string\n", FILE_APPEND);
	echo "Fatal error:\n$log_string\n";
	die ();
}

//Download text file
if (file_exists ($data_local))
	if (rename ($data_local, "$data_local.old") === False)
		death ("Error renaming data from $data_local to $data_local.old");
if (copy ($data_url, $data_local) === False)
	death ("Error copying data from $data_url to $data_local");

//Create temporary database
if (file_exists ("$tmp_database"))
	if (unlink ("$tmp_database") === False)
		death ("Error deleting $tmp_database");

$db = sqlite_open ("$tmp_database");
if ($db === False)
	death ("Error opening new SQLite database");
//Create postcode table
$sql = "CREATE TABLE postcodes ('outward','inward','lat','lon','source')";
if (sqlite_exec ($db, $sql, $err) === False)
	death ("Error creating postcodes database table:\n$err");
//Create XAPI cache table
$sql = "CREATE TABLE xapi_cache ('timestamp','latitude','longitude','distance','searchtype','data')";
if (sqlite_exec ($db, $sql, $err) === False)
	death ("Error creating xapi_cache database table:\n$err");
//Create node cache table
$sql = "CREATE TABLE node_cache ('timestamp','nodeid','data')";
if (sqlite_exec ($db, $sql, $err) === False)
	death ("Error creating node_cache database table:\n$err");

//Open CSV
$csv = fopen ($data_local, "r");
if ($csv === False)
	death ("Error opening CSV file");

//Parse CSV, inserting records into database
while ($line = fgetcsv ($csv)) {
	if ($line [0][0] != "#") {
		$sql = "INSERT INTO postcodes ('outward','inward','lat','lon','source') " .
			"VALUES ('{$line [0]}','{$line [1]}','{$line [4]}','{$line [5]}','{$line [8]}')";
		if (sqlite_exec ($db, $sql, $err) === False)
			death ("Error running SQL:\n$sql\n$err");
	}
}
if (fclose ($csv) === False)
	death ("Error closing CSV file");
if (sqlite_close ($db) === False)
	death ("Error closing SQLite database");

//Rotate databases
if (file_exists ($old_database))
	if (unlink ($old_database) === False)
		death ("Error deleting $old_database");
if (rename ($database, $old_database) === False)
	death ("Error renaming $database to $old_database");
if (rename ($tmp_database, $database) === False)
	death ("Error renaming $tmp_database to $database");
if (chmod ($database, 0666) === False)
	death ("Error changing mode of $database");
?>
