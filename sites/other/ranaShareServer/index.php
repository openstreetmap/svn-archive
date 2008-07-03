<?php
#---------------------------------------------------------------------------
# Server for mobile devices to share geographic data
#
# URL: http://dev.openstreetmap.org/~ojw/pos/
#---------------------------------------------------------------------------
# Copyright 2008, Oliver White
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#---------------------------------------------------------------------------
include("../../conn/connect.php");

if(!array_key_exists("A", $_REQUEST))
{
  addForms();
  exit;
}


header("Content-type: text/plain");
switch($_REQUEST['A'])
{
case 'newusr':
  addUser($_REQUEST['P']);
  break;
case 'newgrp':
  addGroup($_REQUEST['ID'],$_REQUEST['P'], $_REQUEST['N'], $_REQUEST['RP'], $_REQUEST['WP'], $_REQUEST['DT']);
  break;
case 'nick':
  setNickname($_REQUEST['ID'],$_REQUEST['P'], $_REQUEST['G'], $_REQUEST['WP'], $_REQUEST['N']);
  break;
case 'pos':
  addReport($_REQUEST['ID'], $_REQUEST['P'], $_REQUEST['G'], $_REQUEST['WP'], $_REQUEST['LAT'], $_REQUEST['LON']);
  break;
case 'get':
  listGroup($_REQUEST['G'], $_REQUEST['RP'], 'text');
  break;
case 'getfmt':
  listGroup($_REQUEST['G'], $_REQUEST['RP'], $_REQUEST['FMT']);
  break;
case 'grpnam':
  groupName($_REQUEST['G'], $_REQUEST['RP']);
  break;
case 'usrnam':
  userName($_REQUEST['ID'], $_REQUEST['G'], $_REQUEST['RP']);
  break;
  
default:
  print "Unrecognised action.\nSupported:newusr,newgrp,nick,pos,get,getfmt,grpnam,usrnam";
  break;
}


function addForms()
{
  addForm("Create user", "newusr", 'P=desired pin');
  addForm("Create group", "newgrp", 'ID=your ID, P=your PIN, N=group name, RP=desired PIN to see group, WP=desired PIN to publish to group, DT=unused');
  addForm("Report position", "pos", 'ID=your ID, P=your PIN, G=group, WP=group publish PIN, LAT=latitude, LON=longitude');
  addForm("Download group", "get", 'G=group, RP=group view PIN');
  addForm("Set nickname", "nick", "ID=your ID, P=your PIN, G=group, WP=group publish PIN, N=desired nickname");
  addForm("Get group name", "grpnam", "G=group, RP=group view PIN");
  addForm("Get username", "usrnam", "ID=user number, G=group, RP=group view PIN");
}

function addForm($Title, $Action, $Fields)
{
  print "<h2>$Title</h2><form action=./ method=get>\n";
  print "<input type=hidden name=A value=$Action /></p>\n";
  foreach(explode(", ", $Fields) as $Field)
  {
    list($Name,$Desc) = explode("=", $Field);
    print "<p>$Desc: <input type=text name=$Name /></p>\n";
  }
  print "<p><input type=submit value=OK /></p>\n";
  print "</form>\n\n";
}

function addReport($User, $PIN, $Group, $WritePw, $Lat, $Lon)
{
  if(!testUser($User, $PIN))
    showErr('supply ID= for user and P= for PIN');
  if(!testGroupWrite($Group, $WritePw))
    showErr('supply G= for group and WP= for group write PIN');
    
  $SQL = sprintf(
    "replace into pos_reports (`user`, `group`, `lat`, `lon`) values (%d, %d, %f, %f);",
    $User,
    $Group,
    $Lat,
    $Lon);
    
  mysql_query($SQL);
  checkErr();
  printf("OK\n");
}

function setNickname($User, $PIN, $Group, $WritePw, $Nickname)
{
  if(!testUser($User, $PIN))
    showErr('supply ID= for user and P= for PIN');
  if(!testGroupWrite($Group, $WritePw))
    showErr('supply G= for group and WP= for group write PIN');

  $Nickname = preg_replace("/[^A-za-z0-9 _]+/","_", $Nickname);
  
  $SQL = sprintf("replace into pos_text (`user`,`group`,`type`,`text`) values(%d,%d,%d,'%s')",
    $User,
    $Group,
    1,
    mysql_escape_string($Nickname));
  
  mysql_query($SQL);
  checkErr();
  printf("OK\n");
  
}

function groupName($Group, $ReadPw)
{
  if(!testGroupRead($Group, $ReadPw))
    showErr('supply G= for group and RP= for group read PIN');
  $Result = mysql_query(sprintf("select name from pos_groups where `id`=%d;", $Group));
  checkErr();
  $Fields = mysql_fetch_row($Result);
  print("OK: " . $Fields[0]);
  
}
  
function userName($User, $Group, $ReadPw, $Plain = false)
{
  if(!testGroupRead($Group, $ReadPw))
    showErr('supply G= for group and RP= for group read PIN');
  $Result = mysql_query(sprintf("select text from pos_text where `user`=%d and `group`=%d and `type`=%d;", $User, $Group, 1));
  checkErr();
  $Fields = mysql_fetch_row($Result);
  if($Plain)
    return(mysql_num_rows($Result) > 0 ? $Fields[0] : 'anon');
  else
    print("OK: " . $Fields[0]);
}

function listGroup($Group, $ReadPw, $Format)
{
  if(!testGroupRead($Group, $ReadPw))
    showErr('supply G= for group and RP= for group read PIN');
  $Result = mysql_query(sprintf("select user,lat,lon from pos_reports where `group`=%d;", $Group));
  checkErr();

    switch($Format)
    {
      case "gpx":
        printf("<?xml version=\"1.0\"?>\n<gpx version=\"1.0\" creator=\"ranaShareServer\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/0\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n");
        break;
      default:
        break;
    }


  while($Fields = mysql_fetch_row($Result))
  {
    list($User, $Lat, $Lon) = $Fields;

    $Username = userName($User, $Group, $ReadPw, true);
    switch($Format)
    {
      case "gpx":
        printf("<wpt lat=\"%1.6f\" lon=\"%1.6f\">\n  <name>%s</name>\n</wpt>\n", $Lat, $Lon, $Username);
        break;
      default:
        printf("%s,%1.6f,%1.6f\n", $Username, $Lat, $Lon);
        break;
    }
  }

    switch($Format)
    {
      case "gpx":
        printf("</gpx>");
        break;
      default:
        break;
    }

}

function addGroup($User, $PIN, $GroupName, $ReadPw, $WritePw, $Timeout)
{
  if(!testUser($User, $PIN))
    showErr('supply ID= for user and P= for PIN');

  $GroupName = preg_replace("/[^A-za-z0-9 _]+/","_", $GroupName);
  
  $SQL = sprintf(
    "insert into pos_groups (`owner`, `name`, `pin_read`, `pin_write`, `timeout`) values (%d, '%s', %d, %d, %1.2f);",
    $User,
    mysql_escape_string($GroupName),
    $ReadPw,
    $WritePw,
    $Timeout);
    
  mysql_query($SQL);
  checkErr();
  $ID = mysql_insert_id();
  printf("OK: %d\n", $ID);
    
}

function testGroupWrite($Group, $WritePw)
{
  $Result = mysql_query(sprintf("select * from pos_groups where `id`=%d and `pin_write`=%d limit 1;", $Group, $WritePw));
  checkErr();
  return(mysql_num_rows($Result) == 1);
}

function testGroupRead($Group, $ReadPw)
{
  $Result = mysql_query(sprintf("select * from pos_groups where `id`=%d and (`pin_read`=%d or `pin_read`=0) limit 1;", $Group, $ReadPw));
  checkErr();
  return(mysql_num_rows($Result) == 1);
}

function testUser($User, $PIN)
{
  $Result = mysql_query(sprintf("select * from pos_users where `id`=%d and `pin`=%d limit 1;", $User, $PIN));
  checkErr();
  return(mysql_num_rows($Result) == 1);
}
function addUser($pin)
{
  if(!$pin)
    showErr('supply P= for PIN');

  $SQL = sprintf("insert into pos_users (`pin`) values ('%d');", $pin);
  mysql_query($SQL);
  checkErr();
  $ID = mysql_insert_id();
  printf("OK: %d\n", $ID);
}

function checkErr()
{
  if(mysql_errno())
    showErr(mysql_error());
}
function showErr($Text)
{
  print "<pre>$Text</pre>\n";
  exit;
}

?>