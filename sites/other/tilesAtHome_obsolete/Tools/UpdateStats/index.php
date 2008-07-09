<?php
include("../../connect/connect.php");

if($_GET["update"] == "update"){
  header("Content-type: text/plain");
  ExportUserlist();
  if(UpdateStats())
    print "OK";
  else
    print "FAIL";
}
else{
  ?>
  <form action="./" method="get">
  <input type="submit" name="update" value="update">
  </form>
  <?php
}

function ExportUserlist(){
  QueryInto(
    "select id,name from tiles_users order by id",
    "/home/ojw/public_html/Stats/Data/userlist.txt");
}

function UpdateStats(){
  set_time_limit(10 * 60);
  $Filename = "/home/ojw/public_html/Stats/Data/latest.txt";
  
  if(!QueryInto("select * from tiles_meta", $Filename))
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
  
  #print $SQL."\n\n"; return(1);
  
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