<?php
	
if($_SERVER["REMOTE_ADDR"] != $_SERVER["SERVER_ADDR"]){
	print "This page can only be run by the dev server\n";
	exit;
}

include("../connect/connect.php");
include("../lib/log.inc");

header("Content-type: text/plain");
ExportUserlist();
UpdateStats();

function ExportUserlist(){
  QueryInto(
    "select id,name from tiles_users order by id",
    "/home/ojw/public_html/Stats/Data/userlist.txt");
}

function UpdateStats(){
    QueryIntoZip(
      "select `x`, `y`, `z`, `type`, `size`, unix_timestamp(`date`) as `date`, `user`, `version`, `tileset` from `tiles_meta`",
      "/home/ojw/public_html/Stats/Data/latest.txt");
    
    QueryIntoZip(
      "select `x`, `y`, `z`, `layer`, unix_timestamp(`date`) as `date`, `user`, `type` from `tiles_blank`",
      "/home/ojw/public_html/Stats/Data/blank_tiles.txt");
}

function QueryIntoZip($SQL, $Filename){
  set_time_limit(30 * 60);
  
  if(!QueryInto($SQL, $Filename))
    return(0);
  
  $CompressedFilename = $Filename.".gz";
  
  system("gzip -f $Filename");
  
  if(file_exists($Filename)){
    unlink($Filename);
    logMsg("Textfile still exists after gzip\n",2);
    return(0);
  }
  return(1);
}

function QueryInto($SqlSnippet, $Filename){
  if(file_exists($Filename))
    unlink($Filename);

  $SQL = $SqlSnippet . sprintf(" into outfile '%s' %s",
    mysql_escape_string($Filename),
    termination());
  
  $Result = mysql_query($SQL);
  if(mysql_error()){
    logMsg(mysql_error(), 2);
    return(0);
    }
  return(1);
}

function termination(){
  return("fields terminated by ',' lines terminated by '\n'");
}

?>