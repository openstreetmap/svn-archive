<?php
require_once('../lib/functionsnew.php');
require_once('freemap_functions.php');
session_start();

$conn=pg_connect("dbname=gis user=gis");
$cleaned = clean_input($_REQUEST,'pgsql');

$way=null;

switch($cleaned['action'])
{
case "annotate":
    $q= "SELECT name,amenity,man_made,\"natural\",tourism,place,".
            "AsText(way) FROM ".
            "planet_osm_point WHERE osm_id=$cleaned[id]";
    $result=pg_query($q);
    if($row=pg_fetch_array($result,null,PGSQL_ASSOC))
    {
        $highlevel = get_high_level($row);
        if($highlevel!="unknown")
        {
            $m = array();
            $a = preg_match ("/POINT\((.+)\)/",$row['astext'],$m);
            list($x,$y)= explode(" ",$m[1]);
            $result2=pg_query("SELECT * FROM annotatednodes WHERE ".
                "x BETWEEN $x-50 AND $x+50 AND ".
                "y BETWEEN $y-50 AND $y+50 AND ".
                "type='$type' AND name='$row[name]'");
            if($row2=pg_fetch_array($result,null,PGSQL_ASSOC))
            {
                pg_query("UPDATE annotatednodes SET description=".
                    "'$cleaned[text]' WHERE id=$row2[id]");
            }
            else
            {
                pg_query("INSERT INTO annotatednodes".
                "(x,y,name,type,description) ".
                    "VALUES ($x,$y,'$row[name]',".
                    "'$highlevel','$cleaned[text]')");
            }
        }
    }
    break;
}
?>

