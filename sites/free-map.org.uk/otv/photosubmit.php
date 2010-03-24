<?php

include('connect.php');

function delete_photos($id)
{
	mysql_query("DELETE FROM photos where id=$id");
	mysql_query("DELETE FROM photofiles where photoID=$id");
}

if (isset($_FILES["photo1"]))
{
	die("not working atm");

    $cleaned=clean_input($_POST);

	$msg="";
	$err=false;
	$count=1;

	while($err==false && $count<=$cleaned['nfiles'])
	{
		$file = "photo${count}";

    	// upload photo
    	$photo=$_FILES[$file]['tmp_name'];
    	$photo_name=$_FILES[$file]['name'];
    	$photo_size=$_FILES[$file]['size'];
    	$photo_type=$_FILES[$file]['type'];
    	$photo_error=$_FILES[$file]['error'];
    
    	$msg="For photo $count: ";

    	if ($photo_error>0)
    	{
        	switch($photo_error)
        	{
            	case 1: $msg.= "exceeded upload max filesize (1MB)"; break;
            	case 2: $msg.= "exceeded max filesize (1MB)"; break;
            	case 3: $msg.= "partially uploaded"; break;
            	case 4: $msg.= "not uploaded"; break;
        	}
			$err=true;
    	}
    	else
    	{
			if($count==1)
			{
        		mysql_query("INSERT INTO photos (lat,lon) VALUES ".
                "($cleaned[lat],$cleaned[lon])") or die(mysql_error());
        		$id=mysql_insert_id();
			}

			$fname='fe_$id_$count.jpg';
        	mysql_query("INSERT INTO photofiles (photoID,file) VALUES ".
                "($id,'fname')") or die(mysql_error());

        	$upfile = "/home/www-data/uploads/fe/$fname";

        	if(is_uploaded_file($photo))
        	{
            	if(!move_uploaded_file($photo,$upfile))
            	{
                	$msg.= "Could not move file to images directory"; 
					delete_photos($id);
                	mysql_query("DELETE FROM photofiles where photoID=$id");
					$err=true;
            	}
        	}
        	else
        	{
            	$msg.= "go away cracker";
				delete_photos($id);
				$err=true;
        	}
		}
    }

    ?>
    <html>
    <body>
	<p>

	<?php
	if($err==true)
		echo "An error occurred: $msg. ";
	else
		echo "Uploaded successfully.";
	?>

	<a href='index.php'>Back to main page</a>
	</p>
    </body>
    </html>
<?php
}
else
{
?>
<html>
<head>
<title>OpenTrailView</title>
<script type='text/javascript' src='/freemap/javascript/prototype/prototype.js'>
</script>
<script type='text/javascript'>
function photoinputshow()
{
	var phototype=$F('phototype');
	
	var html = 
		{ 'photoset':

		" <label for='photo1'>Photo 1:</label> <input type='file' name='photo1' id='photo1' /> <br /> <label for='photo2'>Photo 2:</label> <input type='file' name='photo2' id='photo2' /> <br /> <label for='photo3'>Photo 3:</label> <input type='file' name='photo3' id='photo3' /> <br /> <label for='photo4'>Photo 4:</label> <input type='file' name='photo4' id='photo4' /> <br />",

		'panorama':

			"  <label for='photo1'>Panorama:</label> <input type='file' name='photo1' id='photo1' /> <br /> "
	};

	$('fileinput').innerHTML = html[phototype];
}

</script>
<link rel='stylesheet' type='text/css' href='/css/osv.css' />
<style type='text/css'>
#panorama_div
{
	visibility: hidden;
}
#photoset_div
{
	visibility: visible;
}
</style>
<body onload='photoinputshow()'>
<p> </p>
<h1>OpenTrailView</h1>
<p>StreetView for the countryside!</p>
<p>An
<a href='http://www.openstreetview.org'>OpenStreetView</a>
related project</p>
<p><strong>NOT WORKING ATM!!!</strong></p>
<form method='post' enctype='multipart/form-data' action=''>
<fieldset id='photo_submit'>
<legend>Please submit your photo</legend>
<p>

<p>
<label for='photo1'>Panorama:</label> 
<input type='file' name='photo1' id='photo1' /> <br /> 
</p>


<p>Please enter latitude and longitude (e.g. from a GPS waypoint
taken at the position of the photo)</p>

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

<?php
mysql_close($conn);
}
?>
