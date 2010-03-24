<?php
require_once('functionsnew.php');
require_once('freemap_functions.php');

$cleaned = clean_input($_REQUEST);
$conn=pg_connect("dbname=freemap");

$mainpage='/freemap/index.php';

$q=("SELECT osm_id, name,amenity,man_made,\"natural\",tourism,place,".
            "AsText(way) as p ".
            "FROM planet_osm_point WHERE osm_id=$cleaned[osm_id]");
$result=pg_query($q);
$row=pg_fetch_array($result,null,PGSQL_ASSOC);
if(!$row) 
{
    echo "No feature with ID $cleaned[osm_id]!";
    pg_free_result($result);
    pg_close($conn);
    exit;
}
if($row['name']!="")
{
    echo "<h1>$row[name]</h1>\n";
}
else
{    
    $row['name']="This feature";
}

$m = array();
$a = preg_match ("/POINT\((.+)\)/",$row['p'],$m);
list($x,$y)= explode(" ",$m[1]);
?>

<html>
<head>
<title>
<?php
echo "$row[name]\n";
?>
</title>
<link rel='stylesheet' type='text/css' href='/freemap/css/freemap2.css'/>
<style type='text/css'>
#desc_textarea { width:480px; height: 200px; }
#map { width:480px; height: 300px; }
</style>
<script type='text/javascript' 
src='/freemap/javascript/prototype/prototype.js'>
</script>
<script src="http://www.openlayers.org/api/2.5/OpenLayers.js"></script>
<script type='text/javascript' src='/freemap/javascript/lib/get_osm_url.js'>
</script>
<script type='text/javascript' src='/freemap/javascript/lib/converter.js'>
</script>
<script type='text/javascript' src='/freemap/javascript/basicmap.js'>
</script>
<script type='text/javascript'>


function generateEditBox()
{
    var desc = $('description').innerHTML;
    $('desc_container').innerHTML = 
        "<em>Please don't say anything slanderous!</em>"+
		"<textarea id='desc_textarea'>"+desc+"</textarea>";
    $('goButton').value = 'Send!';
    $('goButton').onclick = sendNewDescription;

}

function sendNewDescription()
{
    var req = new Ajax.Request
        ('/freemap/POInotes.php',
            { method : 'GET',
              parameters: 'description=' +
                  $F('desc_textarea') + '&osm_id=' + $('osm_id').value +
				  	"&action=" + $('action').value,
              onComplete: resultsReturned }
        );
}

function resultsReturned(xmlHTTP)
{
    $('desc_container').innerHTML = "<p><strong>Feature " + $('osm_id').value + 
        " updated.</strong></p>"
        +"<div id='description'>"+$F('desc_textarea')+"</div>";
    $('goButton').value = 'Edit';
    $('goButton').onclick = generateEditBox;
}

</script>
</head>

<?php
echo "<body onload='loadMap($x,$y,true)'>\n";

$highlevel = get_high_level($row);
if($highlevel!="unknown")
    echo "<p>$row[name] is a <strong>$highlevel</strong></p>\n";

echo "<div id='map'></div>\n";

$q= ("SELECT * FROM node_descriptions WHERE osm_id='$row[osm_id]'");
$result2=pg_query($q);
$row2=pg_fetch_array($result2,null,PGSQL_ASSOC);

echo "<div id='desc_container'>\n";
echo "<div id='description'>\n";
$action="add";
if($row)
{
	$action="update";
    echo "$row2[description]\n";
}
echo "</div>\n";
echo "</div>\n";
echo "<input type='hidden' id='osm_id' value='$cleaned[osm_id]' />\n";
echo "<input type='hidden' id='action' value='$action' />\n";
//pg_free_result($result2);
pg_close($conn);
?>

<p>
<input type='button' id='goButton' onclick='generateEditBox()' value='Edit' /> 
</p>

<p>
<a href='<?php echo $mainpage;?>'>Back to map</a>
</p>

</body>
</html>
