<?

 // By Christopher Schmidt, crschmidt@crschmidt.net
 // Completely untested, mostly because I don't have a db
 // that supports 4.1's geometry extensions.

 // Sat Nov 27 15:39:04 GMT 2004
 // Fixes by steve@fractalus.com, matt amos 
?>

<html>
<head>
<title>OpenStreetMap</title>
</head>
<body>

Enter a text note below:<br><Br>

<FORM method="GET" action="<?=$PHP_SELF?>">
<INPUT type="text" name="txt" size="30" maxlength="255">
<INPUT type="submit" value="Add text note!">
</form>
<?

$txt = $_GET['txt'];
$x = 0;
$y = 0;
$timestamp = 0;

$conn = mysql_connect("localhost:/tmp/mysql.sock","openstreetmap","");
  mysql_select_db("openstreetmap");
  if( $txt )
  {
    mysql_query("insert into tempNotes values ('".$txt."',NOW())");

  }

  $results = mysql_query("select x(g) as x,y(g) as y,timestamp from tempPoints order by timestamp desc limit 1");

  $rs = mysql_fetch_array($results);
  $x = $rs['x'];
  $y = $rs['y'];
  $timestamp = $rs[timestamp];

?>
<br>
Last recorded point was:

<table>
<tr>
  <td align="right">
    <b>Lat:</b>
  </td>
  <td>
    <?=$x?>
  </td>
<tr>
  <td align="right">
    <b>Lon:</b>
  </td>
  <td>
    <?=$y?>
  </td>
<tr>
  <td align="right">
    <b>Time:</b>
  </td>
  <td>
    <?=date("l dS of F Y h:i:s A",$timestamp);?>
  </td>
</tr>
</table>
</body>
</html>
