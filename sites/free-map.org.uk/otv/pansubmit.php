<?php
include('connect.php');
include('../lib/functionsnew.php');

if (isset($_FILES["panorama"]))
{

    $cleaned=clean_input($_POST);


    // upload panorama
    $panorama=$_FILES['panorama']['tmp_name'];
    $panorama_name=$_FILES['panorama']['name'];
    $panorama_size=$_FILES['panorama']['size'];
    $panorama_type=$_FILES['panorama']['type'];
    $panorama_error=$_FILES['panorama']['error'];
    
    $msg="";

    if ($panorama_error>0)
    {
        switch($panorama_error)
        {
            case 1: $msg= "exceeded upload max filesize (1MB)"; break;
            case 2: $msg= "exceeded max filesize (1MB)"; break;
            case 3: $msg= "partially uploaded"; break;
            case 4: $msg= "not uploaded"; break;
        }
    }
    else
    {
        mysql_query("INSERT INTO panoramas (lat,lon) VALUES ".
                "($cleaned[lat],$cleaned[lon])") or die(mysql_error());

        $id=mysql_insert_id();

        $upfile = "/home/www-data/uploads/otv/$id.jpg";
        if(is_uploaded_file($panorama))
        {
            $msg= "Uploaded successfully. Your image will need to be ".
                "authorised before it can be viewed.";
            if(!move_uploaded_file($panorama,$upfile))
            {
                $msg= "Could not move file to images directory"; 
                mysql_query("DELETE FROM panoramas where id=$id");
            }
        }
        else
        {
            $msg= "go away cracker";
            mysql_query("DELETE FROM panoramas where id=$id");
        }
    }
    ?>
    <html>
    <head>
    <script type='text/javascript'>
    alert('<?php echo $msg ?>');
    window.location='/otv/index.php';
    </script>
    </head>
    </html>
<?php
}
else
{
?>
<html>
<head>
<title>OpenTrailView</title>
<link rel='stylesheet' type='text/css' href='/css/osv.css' />
<style type='text/css'>
#map
{
	width: 800px;
	height: 300px;
}
#infopanel
{
	width: 800px;
	height: 200px;
	overflow: auto;
}
</style>
<h1>OpenTrailView</h1>
<h2>Please submit your panorama</h2>
<ul>
<li>
It's important to make the centre of the panorama face your direction
of travel; this is needed to align the panorama with the path you're
following, using an OSM map.</li>
<li>To do this when out, turn in the opposite direction to the direction
you're travelling and slightly to the right. Take a series of photos, turning
clockwise, until finally you take a photo which faces in the opposite 
direction to the direction you're travelling and slightly to the left, at
the same angle (but left, rather than right) as your original photo.
Ensure each successive photo overlaps with the previous as the photo stitching
software works by identifying common features.</li>
<li>Also take a GPS waypoint - you'll need that to add a
latitude and longitude for the panorama on the form below.</li>
<li> To actually create a panorama, do the following:
use photo stitching
software, such as the free and open source
<a href='http://hugin.sourceforge.net'>Hugin</a> (commercial photostitchers are
available too) to stitch the photos to make a panorama.</li>
<li>Upload your panorama, here, and wait for it to be authorised.</li>
<li>Finally, align the panorama with the footpath or trail that you took
it from, by rotating the camera icon on the map so that it faces the same
direction as the centre of the panorama.</li>
</ul>
<form method='post' enctype='multipart/form-data' action=''>
<fieldset id='panorama_submit'>
<legend>Please submit your panorama</legend>
<p>
<label for='panorama'>Panorama file:</label>
<input type="file" name="panorama" id="panorama" />
</p>
<p>
<label for='lat'>Latitude:</label>
<input name='lat' id='lat' />
</p>
<p>
<label for='lat'>Longitude:</label>
<input name='lon' id='lon' />
</p>
<input type='hidden' name='MAX_FILE_SIZE' value='1048576' />
<p>
<input type='submit' value='Go!' />
</p>
</fieldset>
</form>
<p><a href='/otv/index.php'>Back to map</a></p>
</body>
</html>

<?php
mysql_close($conn);
}
?>
