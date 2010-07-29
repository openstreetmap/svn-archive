<?php
session_start();

function psbmt()
{
    if(isset($_SESSION['gatekeeper']))
    {
        $_SESSION['photosession'] = newsession();
        ?>
        <div id='pansubmit_all'>
        <?php
        display_map(320,320);
        ?>
        <div id='psbmt0'>
        <h2>Submit a series of photos</h2>
        <p>
        Upload each photo or panorama in your trip, one after the
        other. Then, if you also recorded a GPX trace
        while out, you can select "Auto-position with GPX", below, to 
        automatically position each photo or panorama. If not, just click 
        "Finish trip".</p>

        <iframe src='psbmt.php' id='iframe1'></iframe>
        </div>
        </div>
        <div id='errors'></div>
        <h3>Uploaded photos or panoramas for this trip</h3>
        <div id='uptbl'>
        <table id='uploadTable'>
        <tr>
        <th>Photo filename</th>
        <th>Latitude</th>
        <th>Longitude</th>
        </tr>
        </table>
        </div>
        <div>
        <input type='button' value='Auto-position with GPX' id='apbtn'
        onclick='document.getElementById("iframe1").src="gpxupload.php";
        document.getElementById("apbtn").style.visibility="hidden"' />
        <input type='button' value='Finish trip' 
        onclick='window.location="index.php"' />
        </div>

        <?php
    }
    else
    {
        echo "<div id='pansubmit_all'>Need to be logged in to upload photos".
        " <a href='user.php?action=login&redirect=index.php".
        "'>Login now!</a></div>";
    }
}

function newsession()
{
    $q= "SELECT MAX(photosession) AS pssn FROM panoramas ".
                            "WHERE user=$_SESSION[gatekeeper]";
    $result=mysql_query($q) or die(mysql_error());
    $row=mysql_fetch_assoc($result);
    return ($row['pssn']==null) ? 1:$row['pssn']+1;
}

function display_map($w,$h)
{
    echo "<div style='width:${w}px; height:${h}px' id='map'></div>";
}

?>
