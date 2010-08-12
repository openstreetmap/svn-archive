<?php

session_start();

require_once('../lib/gpx.php');
require_once('../lib/functionsnew.php');

if(!isset($_SESSION['photosession']))
    die("No photos for this session!");

if(!isset($_SESSION['gatekeeper']))
    die("Must be logged in!");


    $conn=dbconnect("otv");
if(isset($_FILES["gpx"]))
{
    $pan=array();
    $u = upload_file("gpx","/home/www-data/uploads/otv/gpx");
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
                "photosession=$_SESSION[photosession] ".
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
    foreach($pan as $p)
    {
        $lat = ($p["lat"]>=-90 && $p["lat"]<=90) ? $p["lat"]:"Unknown";
        $lon = ($p["lon"]>=-180 && $p["lon"]<=180) ? $p["lon"]:"Unknown";
        echo "pg.getElementById('lat_${p[ID]}').firstChild.nodeValue='$lat';\n";
        echo "pg.getElementById('lon_${p[ID]}').firstChild.nodeValue='$lon';\n";
    }
    echo "</script>\n";
    echo "</head><body>\n";
    echo "$msg";
    ?>
    <a href='index.php'>Back to index page</a>
    </body></html>
    <?php
}
else
{
    echo "<html><body>";
	echo "<h1>Auto-position photos with GPX</h1>";
        $q= ("SELECT * FROM panoramas where ".
                "photosession=$_SESSION[photosession] ".
                "AND user=$_SESSION[gatekeeper] ORDER BY time");
        $result=mysql_query($q);
    echo "<p>".mysql_num_rows($result)." photos will be positioned.</p>";
?>
    <div id='gpxsubmit'>
    <form method='post' enctype='multipart/form-data' action=''>
    <fieldset id='gpx_submit'>
    <legend>Please submit your gpx file</legend>
    <label for='gpx'>gpx file:</label>
    <input type="file" name="gpx" id="gpx" />
    <input type='hidden' name='MAX_FILE_SIZE' value='1048576' />
    <input type='submit' value='Go!' /> 
    </fieldset>
    </form>
    </div>
    </body></html>
<?php
    mysql_close($conn);
}
?>
