<?php
require_once('../lib/functionsnew.php');
require_once('index_funcs.php');

// partly based on example from Andrew Valums' site

session_start();

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
    $maxMB = 3;
    $filename = (isset($_GET['qqfile'])) ? $_GET['qqfile']:
                    $_FILES['qqfile']['name'];
    if(!toobig($maxMB*1024*1024))
    {
        $pathinfo = pathinfo($filename);
        if($pathinfo['extension'] != 'jpg')
        {
            $resp = array ("success" => false,
                        "error" => "$filename: not a JPEG");
        }    
        else
        {
            $conn = dbconnect("otv");

            if(!isset($_SESSION['photosession']))
                $_SESSION['photosession'] = newsession();

            mysql_query("INSERT INTO panoramas ".
                "(authorised,user,photosession) VALUES ".
                "(0,$_SESSION[gatekeeper],$_SESSION[photosession])");
            $id = mysql_insert_id();

            $outdir = "/home/www-data/uploads/otv";
             $outfile= "$id.jpg";

            if(writeFile($outdir,$outfile)===true)
            {
                // Note exif_read_data complains about certain non-standard
                // tags. However it doesn't prevent working.
                $exif=@exif_read_data($outfile);
                if(isset($exif['GPSLatitude']) && isset($exif['GPSLongitude']))
                {
                    $lat=to_decimal_degrees($exif['GPSLatitude']);
                    $lon=to_decimal_degrees($exif['GPSLongitude']);
                    if($exif['GPSLatitudeRef']=='S')
                        $lat = -$lat;
                    if($exif['GPSLongitudeRef']=='W')
                        $lon = -$lon;
                    mysql_query ("UPDATE panoramas SET lat=$lat,".
                         "lon=$lon WHERE ID=$id");
                }
                if(isset($exif['DateTimeOriginal']))
                {
                    $time = strtotime($exif['DateTimeOriginal']);
                    $q= ("UPDATE panoramas SET time=$time WHERE ID=$id");
                    mysql_query($q);
                }

                $resp = array ("success" => true,"id" => $id);
            }
            else
            {
                mysql_query("DELETE FROM panoramas WHERE id=$id");
                $resp = array ("success" => false,
                        "error" => "Error saving file $filename on server");
            }
            mysql_close($conn);
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

function to_decimal_degrees($dms)
{
    return $dms[0] + $dms[1]/60.0 + $dms[2]/3600.0;
}
?>
