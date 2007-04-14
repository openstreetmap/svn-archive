<?php
	
if($_SERVER["REMOTE_ADDR"] != $_SERVER["SERVER_ADDR"]){
	print "This page can only be run by the dev server\n";
	exit;
}

include("../connect/connect.php");

header("Content-type: text/plain");
ExportUserlist();
UpdateStats();

function ExportUserlist(){
  QueryInto(
    "select id,name from tiles_users order by id",
    "/home/ojw/public_html/Stats/Data/userlist.txt");
}

function UpdateStats(){
  set_time_limit(10 * 60);
  $Filename = "/home/ojw/public_html/Stats/Data/latest.txt";
  
  if(!QueryInto("select `x`, `y`, `z`, `type`, `size`, unix_timestamp(`date`) as `date`, `user`, `version` from `tiles_meta`", $Filename))
    return(0);
  
  $CompressedFilename = $Filename.".gz";
  
  system("gzip -f $Filename");
  
  if(file_exists($Filename)){
    unlink($Filename);
    print "Textfile still exists after gzip\n";
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
    print mysql_error() . "\n";
    return(0);
    }
  return(1);
}

function termination(){
  return("fields terminated by ',' lines terminated by '\n'");
}

?>