<?php

/* iframe based pseudo-ajax upload:
   see
   http://ehsun7b.blogspot.com/2007/11/uploading-files-using-ajax_12.html
*/

session_start();
include('../lib/functionsnew.php');


?>
<?php
if (isset($_FILES["panorama"]))
{
   ?>
	<?php
	$conn=dbconnect("otv");
    $cleaned=clean_input($_POST);
	$cleaned["lat"] = ($cleaned["lat"]!="") ? $cleaned["lat"]: 999;
	$cleaned["lon"] = ($cleaned["lon"]!="") ? $cleaned["lon"]: 999;


    // upload panorama
    $panorama=$_FILES['panorama']['tmp_name'];
    $panorama_name=$_FILES['panorama']['name'];
    $panorama_size=$_FILES['panorama']['size'];
    $panorama_type=$_FILES['panorama']['type'];
    $panorama_error=$_FILES['panorama']['error'];
    
    $msg="Successfully uploaded file ". $_FILES['panorama']['name'];
	$error=false;

    if ($panorama_error>0)
    {
		$error=true;
        switch($panorama_error)
        {
            case 1: $msg= "Exceeded upload max filesize (1MB)"; break;
            case 2: $msg= "Exceeded max filesize (1MB)"; break;
            case 3: $msg= "Partially uploaded"; break;
            case 4: $msg= "Nothing uploaded"; break;
        }
    }
    else
    {

		$time=0;
		$q=("INSERT INTO panoramas ".
						"(lat,lon,time,user,photosession) ".
                            "VALUES ($cleaned[lat],$cleaned[lon],$time,".
                            "'$_SESSION[gatekeeper]',$_SESSION[photosession])")
							or die(mysql_error());
		mysql_query($q) or die(mysql_error());
        $id=mysql_insert_id();

        $upfile = "/home/www-data/uploads/otv/$id.jpg";
        if(!is_uploaded_file($panorama))
        {
			$msg="Go away cracker!";
			$error=true;
		}
		else
		{
            if(!move_uploaded_file($panorama,$upfile))
            {
                $msg= "Could not move file to images directory"; 
				$error=true;
            }
            else // get EXIF lat/lon if present
            {
            	$msg= "Uploaded $panorama_name successfully."; 
                $exif=exif_read_data($upfile);
                if(isset($exif['GPSLatitude']) && isset($exif['GPSLongitude']))
                {
                    $cleaned['lat']=to_decimal_degrees($exif['GPSLatitude']);
                    $cleaned['lon']=to_decimal_degrees($exif['GPSLongitude']);
                    if($exif['GPSLatitudeRef']=='S')
                        $cleaned['lat']=-$cleaned['lat'];
                    if($exif['GPSLongitudeRef']=='W')
                        $cleaned['lon']=-$cleaned['lon'];
					mysql_query
						("UPDATE panoramas SET lat=$cleaned[lat],".
						 "lon=$cleaned[lon] WHERE ID=$id");
                }
				if(isset($exif['DateTimeOriginal']))
				{
					$time = strtotime($exif['DateTimeOriginal']);
					$q= ("UPDATE panoramas SET time=$time WHERE ID=$id");
					mysql_query($q);
				}
            }
        }
		if($error==true)
        {

            mysql_query("DELETE FROM panoramas where id=$id");
        }
    }

	$displat = ($cleaned['lat'] <= 90) ? $cleaned['lat'] : "Unknown";
	$displon = ($cleaned['lon'] <= 180) ? $cleaned['lon'] : "Unknown";
	$errHTML = "<strong>$msg</strong>";
	mysql_close($conn);
}
	?>
<html>
<head>
   <script type='text/javascript'>
function loadingMsg()
{
   var pg = parent.content.document;
	pg.getElementById('errors').innerHTML='<img src="ajax-loader.gif" alt="uploading file..." />';
	return true;
}
</script>
<?php
if(isset($_FILES["panorama"]))
{
?>
<script type='text/javascript'>
   var pg = parent.content.document;
	pg.getElementById('errors').innerHTML = '<?php echo $errHTML; ?>';
	<?php
	if($error==false)
	{
		?>
		var tbl = pg.getElementById('uploadTable');
		var tr=pg.createElement('tr');
		tbl.appendChild(tr);
		var td1=pg.createElement('td');
		var txt1=pg.createTextNode('<?php echo $_FILES['panorama']['name'];?>');
		td1.appendChild(txt1);
		tr.appendChild(td1);
		var td2=pg.createElement('td');
		td2.setAttribute('id','lat_'+<?php echo $id; ?>);
		var txt2=pg.createTextNode('<?php echo $displat; ?>');
		td2.appendChild(txt2);
		tr.appendChild(td2);
		var td3=pg.createElement('td');
		td3.setAttribute('id','lon_'+<?php echo $id; ?>);
		var txt3=pg.createTextNode('<?php echo $displon; ?>');
		td3.appendChild(txt3);
		tr.appendChild(td3);
		var td4=pg.createElement('td');
		var txt4=pg.createTextNode('<?php echo $_SESSION['photosession']; ?>');
		td4.appendChild(txt4);
		tr.appendChild(td4);
		<?php
	}
	?>
	</script>
<?php
}
?>
<link rel='stylesheet' type='text/css' href='css/osv.css' />
</head>
<body>
<div id='pansubmit'>
<form method='post' enctype='multipart/form-data' action='' 
onsubmit='return loadingMsg()'>
<fieldset id='panorama_submit'>
<legend>Please submit your panorama</legend>
<label for='panorama'>Panorama:</label>
<input type="file" name="panorama" id="panorama" /> 
<label for='lat'>Latitude:</label>
<input name='lat' id='ifr_lat' class='narrow' /> 
<label for='lat'>Longitude:</label>
<input name='lon' id='ifr_lon' class='narrow'/> 
<input type='hidden' name='MAX_FILE_SIZE' value='2097152' />
<input type='submit' value='Go!' />
</fieldset>
</form>
</div>
</body>
</html>
	<?php

function to_decimal_degrees($dms)
{
    return $dms[0] + $dms[1]/60.0 + $dms[2]/3600.0;
}
?>
