<?php

session_start();
include('IframeForm.php');
require_once('../lib/functionsnew.php');

class PsbmtForm extends IframeForm
{

    private $id, $displat, $displon;

    function __construct ($file,$errorDiv, $uploadMsg)
    {
        IframeForm::__construct($file,$errorDiv,$uploadMsg);
        if(!isset($_POST['photosubmit']) &&
          !isset($_POST['first']) && !isset($_POST['second']))
        {
            $_SESSION['parentID'] = null;
        }
    }

    function doProcessUpload()
    {

        if(!isset($_POST['photosubmit']))
        {
            return;
        }

        $conn=dbconnect("otv");
        $cleaned=clean_input($_POST);
        $cleaned["lat"] = ($cleaned["lat"]!="") ? $cleaned["lat"]: 999;
        $cleaned["lon"] = ($cleaned["lon"]!="") ? $cleaned["lon"]: 999;
        $isPano = (isset($_POST["isPano"])) ? "true":"false";

        $time=0;
        $q=("INSERT INTO panoramas ".
                        "(lat,lon,time,user,photosession,isPano) ".
                            "VALUES ($cleaned[lat],$cleaned[lon],$time,".
                            "'$_SESSION[gatekeeper]',$_SESSION[photosession],".
                            "$isPano)")
                            or die(mysql_error());
        mysql_query($q) or die(mysql_error());
        $this->id=mysql_insert_id();
        if(!isset($_SESSION['parentID']))
            $_SESSION['parentID'] = $this->id;
      
          $orientation=isset($cleaned['orientation'])?$cleaned['orientation']:0;
        if(! $this->error)
        {
            mysql_query("INSERT INTO photogroups ".
            "(photoID,parentID,orientation) VALUES (".
            $this->id.",$_SESSION[parentID],$orientation)");


            $upfile = "/home/www-data/uploads/otv/".$this->id.".jpg";
            if(!move_uploaded_file($this->filename,$upfile))
            {
                $this->msg= "Could not move file to images directory"; 
                $this->error=true;
                mysql_query("DELETE FROM panoramas where id=".$this->id);
            }
            else // get EXIF lat/lon if present
            {
                $this->msg= "Uploaded $panorama_name successfully."; 
                
                // Note exif_read_data complains about certain non-standard
                // tags. However it doesn't prevent working. So shut it up
                // with @.
                $exif=@exif_read_data($upfile);
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
                         "lon=$cleaned[lon] WHERE ID=".$this->id);
                }
                if(isset($exif['DateTimeOriginal']))
                {
                    $time = strtotime($exif['DateTimeOriginal']);
                    $q= ("UPDATE panoramas SET time=$time WHERE ID=".
                            $this->id);
                    mysql_query($q);
                }
            }

            $this->displat=($cleaned['lat'] <= 90)?$cleaned['lat'] : "Unknown";
            $this->displon=($cleaned['lon'] <= 180)?$cleaned['lon'] : "Unknown";
        }
        mysql_close($conn);
    }

    function doWriteOutput()
    {
        if(!isset($_POST['photosubmit']))
            return;

        ?>
        <script type='text/javascript'>
        var el = pg.getElementById
            ('name_'+<?php echo $_SESSION['parentID']; ?>);
        if(el)
        {
            var existing = el.innerHTML;
            el.innerHTML = existing + "," +
                '<?php echo $_FILES['panorama']['name'] ?>';
        }
        else
        {
            var tbl = pg.getElementById('uploadTable');
            var tr=pg.createElement('tr');
            tbl.appendChild(tr);
            var td1=pg.createElement('td');
            td1.setAttribute('id','name_'+<?php echo $this->id; ?>);
            var txt1=pg.createTextNode
                ('<?php echo $_FILES['panorama']['name'];?>');
            td1.appendChild(txt1);
            tr.appendChild(td1);
            var td2=pg.createElement('td');
            td2.setAttribute('id','lat_'+<?php echo $this->id; ?>);
            var txt2=pg.createTextNode('<?php echo $this->displat; ?>');
            td2.appendChild(txt2);
            tr.appendChild(td2);
            var td3=pg.createElement('td');
            td3.setAttribute('id','lon_'+<?php echo $this->id; ?>);
            var txt3=pg.createTextNode('<?php echo $this->displon; ?>');
            td3.appendChild(txt3);
            tr.appendChild(td3);
        }
        </script>
        <?php
    }

    function doWriteForm()
    {

        if(!isset($_POST['photosubmit']))
        {
            if(isset($_POST["first"]))
                $_SESSION['parentID'] =  null;

            ?>
            <div id='pansubmit'>
            <form method='post' enctype='multipart/form-data' action='' 
            onsubmit='return loadingMsg()'>
            <fieldset id='panorama_submit'>
            <legend>Please submit your photo</legend>
            <label for='panorama'>Photo:</label>
            <input type="file" name="panorama" id="panorama" />  
            <br />
            <?php
            if(isset($_SESSION['parentID']))
            {
                ?>
                <label for='orientation'>Orientation relative to first in
                set:</label>
                <select name='orientation' id='orientation'>
                <option value='-90'>90 degrees clockwise</option>
                <option selected='selected' value='180'>180 degrees</option>
                <option value='90'>90 degrees anticlockwise</option>
                </select>
                <br />
                <?php
            }
            else
            {

                ?>
                <label for='lat'>Latitude:</label>
                <br />
                <input name='lat' id='ifr_lat' class='narrow' value=''/> 
                <br />
                <label for='lat'>Longitude:</label>
                <br />
                <input name='lon' id='ifr_lon' class='narrow' value=''/> 
                <br />
                <?php
            }
        
            ?>
            <label for='isPano'>Is it a panorama?</label>
            <input type='checkbox' name='isPano' />
            <input type='hidden' name='MAX_FILE_SIZE' value='3200000' />
            <input type='submit' value='Upload!' name='photosubmit' />
            </fieldset>
            </form>
            </div>
            <?php
        }
        else
        {
            ?>
            <p>If this photo is part of a set (e.g two linked photos
            from the same position, facing in opposite directions) click
            'Add another photo to set'. If you have just uploaded a single
            photo or panorama, or the last photo in a set, click
            'New photo set'.</p>
            <p>
            <form method='post' action=''>
            <input type='submit'
            value='Add another photo to set' name='second' />
            <input type='submit'
            value='New photo set' name='first' />
            </form>
            </p>
            <?php
        }
    }
}




function to_decimal_degrees($dms)
{
    return $dms[0] + $dms[1]/60.0 + $dms[2]/3600.0;
}




$form = new PsbmtForm("panorama","errors",
    '<img src="ajax-loader.gif" alt="uploading file..." />');
$form->createPage();

?>
