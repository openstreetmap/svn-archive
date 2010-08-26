<?php

session_start();

require_once('../lib/gpx.php');
require_once('../lib/functionsnew.php');

if(!isset($_SESSION['gatekeeper']))
    die("Must be logged in!");


    $conn=dbconnect("otv");
if(isset($_FILES["gpx"]))
{
    $pan=array();
    $u = upload_file("gpx","/home/www-data/uploads/otv/gpx");
    $cleaned=clean_input($_POST);
    if($u["file"]!==null)
    {
        $gpx = file($u["file"]);
        if($gpx!==false)
        {
            $gpx = parseGPX($gpx);

            if($gpx!==false)
            {
                // get panoramas from this session
                $q= ("SELECT * FROM panoramas where ".
                "photosession=$cleaned[pssn] ".
                "AND user=$_SESSION[gatekeeper] ORDER BY time");
                $result=mysql_query($q);
                while($row=mysql_fetch_assoc($result))
                    $pan[] = $row;


                for($i=0; $i<count($gpx["trk"])-1; $i++)
                {
                    foreach($pan as $j=>$curpan)
                    {
                        if($curpan["time"]>=$gpx["trk"][$i]["time"] &&
                        $curpan["time"]<=$gpx["trk"][$i+1]["time"])
                        {
                            $t=($curpan["time"]-$gpx["trk"][$i]["time"]) /
                             ($gpx["trk"][$i+1]["time"]-
                             $gpx["trk"][$i]["time"]);

                            $pan[$j]["lat"] = $gpx["trk"][$i]["lat"] + $t*
                            ($gpx["trk"][$i+1]["lat"]-$gpx["trk"][$i]["lat"]);
                            $pan[$j]["lon"] = $gpx["trk"][$i]["lon"] + $t*
                            ($gpx["trk"][$i+1]["lon"]-$gpx["trk"][$i]["lon"]);

                            mysql_query 
                            ("UPDATE panoramas SET lat=".$pan[$j]["lat"].
                            ",lon=".$pan[$j][lon]." WHERE ID=$curpan[ID]");

                            break;
                        }
                    }
                }
                $msg = 'GPX upload ok!';
                // blank out photosession
                $_SESSION['photosession'] = null;
            }
            else
            {
                $msg = 'Error interpreting the GPX file - '.
                        'check that it is a valid GPX file';
            }
        }
        else
        {
            $msg = 'Error reading the GPX file; '.
                    'might not have uploaded correctly'; 
        }
    }
    else
    {
        $msg = $u["error"];
    }

    echo "<html>\n";
    echo "<head>\n";
    echo "<link rel='stylesheet' type='text/css' href='css/osv.css' />\n";
    $successful = 0;
    foreach($pan as $p)
    {
        $lat = ($p["lat"]>=-90 && $p["lat"]<=90) ? $p["lat"]:"Unknown";
        $lon = ($p["lon"]>=-180 && $p["lon"]<=180) ? $p["lon"]:"Unknown";
        if($lat!="Unknown" && $lon!="Unknown")
            $successful++;
    }
    echo "</script>\n";
    echo "</head><body>\n";
    echo "<p>$successful photos were successfully placed using GPX. ";
    echo "If this is not all of them, visit the <a href='photomgr.php'>";
    echo "photo manager</a> page to place the rest.</p>";
    ?>
    <p><a href='index.php'>Map</a> | <a href='psbmt.php'>Upload photos</a></p>
    </body></html>
    <?php
}
else
{
    ?>
    <html>
    <head>
    <script type='text/javascript'>
    function modifyPhotoLink()
    {
        document.getElementById('photolink').value =
            'photomgr.php?pssn='+
            document.getElementById('pssn').value;
    }
    </script>
    <link rel='stylesheet' type='text/css' href='css/osv.css' />
    </head><body>
    <h1>Auto-position photos with GPX</h1>
	<p>You can position any previous set of uploaded photos.</p>
    <div id='gpxsubmit'>
    <form method='post' enctype='multipart/form-data' action=''>
    <p>
    <label for='pssn'>Pick an upload session:</label>
<?php
    echo "<select name='pssn' id='pssn' onchange='modifyPhotoLink()'>";
    $result=mysql_query
        ("SELECT * FROM photosessions WHERE user=$_SESSION[gatekeeper]".
        " ORDER BY t DESC");
    if(mysql_num_rows($result)==0)
    {
        echo "No photo sessions!";
    }
    else
    {
        while($row=mysql_fetch_array($result))
        {
            $result2=mysql_query
            ("SELECT * FROM panoramas WHERE photosession=$row[id]");
            $np=mysql_num_rows($result2);
			$t = date('D d M Y, H:i',strtotime($row['t']));
            echo "<option value='$row[id]'>$t ($np photos)</option>";
        }
        echo "</select>\n";
?>
    <a id='photolink'
    href='photomgr.php?pssn=1'>View photos in this session</a></p>
    <fieldset id='gpx_submit'>
    <legend>Please submit your GPX file</legend>
    <label for='gpx'>GPX file:</label>
    <input type="file" name="gpx" id="gpx" />
    <input type='hidden' name='MAX_FILE_SIZE' value='1048576' />
    <input type='submit' value='Go!' /> 
    </fieldset>
    </form>
    </div>
    <p><a href='index.php'>Map</a> | <a href='psbmt.php'>Upload photos</a></p>
    </body></html>
<?php
    }
    mysql_close($conn);
}
?>
