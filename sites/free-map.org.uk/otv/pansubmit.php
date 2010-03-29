<?php
session_start();
include('connect.php');
include('../lib/functionsnew.php');

if(!isset($_SESSION['gatekeeper']))
{
	header("Location: login.php?redirect=/otv/index.php");
}
else if (isset($_FILES["panorama"]))
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
        mysql_query("INSERT INTO panoramas (lat,lon,user) VALUES ($cleaned[lat],$cleaned[lon],'$_SESSION[gatekeeper]')") or die(mysql_error());

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
	echo "You must upload a file!";
}
mysql_close($conn);
?>
