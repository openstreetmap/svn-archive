<?php
require_once('../lib/functionsnew.php');
require_once('otv_funcs.php');
require_once('../common/defines.php');

// partly based on example from Andrew Valums' site

session_start();
$conn = pg_connect("dbname=gis user=gis");

//header ("Content-type: application/json");

function writeFile($outfileDir,$outfileName)
{
    if(isset($_GET['qqfile']))
    {
        $input=fopen("php://input","r");


        // todo check extension
        $fp = fopen("$outfileDir/$outfileName","w");

        if($fp!==false)
        {
            while($line=fread($input,1024))
                fwrite($fp,$line);
            fclose($fp);
            return true;
        }
        else
        {
            return "Unable to open file on server"; 
        }
    }
    else if (isset($_FILES['qqfile']))
    {
        $status=upload_file("qqfile",$outfileDir,$outfileName);
        return ($status["error"]) ? $status["error"]: true;
    }
    return "No file uploaded";
}

function toobig($limit)
{
    if(isset($_GET['qqfile']))
    {
        $headers=apache_request_headers();
        return  ((int)$headers['Content-Length']) >= $limit;
    }
    else
    {
        return $_FILES['qqfile']['size'] >= $limit;
    }
}

if (!isset($_SESSION['gatekeeper']))
{
    $resp = array ("success" => false, "error" => "not logged in");
}
else if(isset($_GET['qqfile']) || isset($_FILES['qqfile']))
{
    $resp = array ("success" => true);
    $maxMB = 3;
    $filename = (isset($_GET['qqfile'])) ? $_GET['qqfile']:
                    $_FILES['qqfile']['name'];
    if(!toobig($maxMB*1024*1024))
    {
        $pathinfo = pathinfo($filename);
        if(strtolower($pathinfo['extension']) != 'jpg')
        {
            $resp = array ("success" => false,
                        "error" => "$filename: not a JPEG");
        }    
        else
        {

			if(!isset($_SESSION['photosession']))
				$_SESSION['photosession'] = newsession();


            pg_query("INSERT INTO panoramas ".
                "(authorised,direction,userid,photosession) VALUES ".
                "(0,0,$_SESSION[gatekeeper],$_SESSION[photosession]".
				")");
			$result=pg_query("SELECT currval('panoramas_id_seq') AS panid");
			$row=pg_fetch_array($result,null,PGSQL_ASSOC);
            $id = $row['panid']; 

            $outdir = OTV_UPLOADS; 
             $outfile= "$id.jpg";

            if(writeFile($outdir,$outfile)===true)
            {
				$str = "";
                $sz = getimagesize("$outdir/$outfile");


                // Note exif_read_data complains about certain non-standard
                // tags. However it doesn't prevent working.
                $exif=@exif_read_data("$outdir/$outfile");
                if(isset($exif['GPSLatitude']) && isset($exif['GPSLongitude']))
                {
                    $lat=to_decimal_degrees($exif['GPSLatitude']);
                    $lon=to_decimal_degrees($exif['GPSLongitude']);
                    if($exif['GPSLatitudeRef']=='S')
                        $lat = -$lat;
                    if($exif['GPSLongitudeRef']=='W')
                        $lon = -$lon;
					$x = lon_to_sphmerc($lon);
					$y = lat_to_sphmerc($lat);
                    mysql_query ("UPDATE panoramas SET ".
						"xy=GeomFromText('POINT($x $y)',900913) ".
						"WHERE id=$id");
                }
                if(isset($exif['DateTimeOriginal']))
                {
					$str .= "DateTimeOriginal = ".$exif['DateTimeOriginal'];
                    $time = strtotime($exif['DateTimeOriginal']);
                    $q= ("UPDATE panoramas SET time=$time WHERE id=$id");
                    pg_query($q);
                }

                $resp = array ("success" => true,"id" => $id);
            }
            else
            {
                pg_query("DELETE FROM panoramas WHERE id=$id");
                $resp = array ("success" => false,
                        "error" => "Error saving file $filename on server");
            }
        }
    }
    else
    {
        $resp = array ("success" => false,
                        "error" => "File $filename too big, limit ${maxMB} MB");
    }
}
else
{
    $resp = array ("success" => false,
                "error" => "no uploaded file");
}

echo json_encode($resp);
pg_close($conn);

function to_decimal_degrees($dms)
{
    return frac_to_dec($dms[0]) + frac_to_dec($dms[1])/60.0 + 
			frac_to_dec($dms[2])/3600.0;
}

function frac_to_dec($frac)
{
	$components = explode("/", $frac);
	return (is_array($components) && count($components)==2) ? 
		$components[0]/$components[1] : $frac;
}
?>
