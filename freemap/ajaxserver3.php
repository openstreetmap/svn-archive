<?php
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-06 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################
require_once('defines.php');

// id type
// id point e n pending
session_start();

if( isset($_SESSION['ngatekeeper']) 
    || $_POST['action']=='featurequery' || $_POST['action']=='search')
{
	/*
    $conn=mysql_connect(DB_HOST,DB_USERNAME,DB_PASSWORD);
    mysql_select_db(DB_DBASE);

    $result=mysql_query("select id from usersnew where email='".
                        $_SESSION['ngatekeeper']."'");
    $row=mysql_fetch_array($result);
    $userid=$row['id'];

    $bl['lon']= $_POST['lon'];
    $bl['lat']= $_POST['lat'];
	*/

    if(preg_match("/^feature/",$_POST['action']) ||
			$_POST['action']=='osmupload')
    {
        $xy=explode(",",$_POST['points']);
        
        $p['lon']=$xy[0];
        $p['lat']=$xy[1];
        

        switch($_POST['action'])
        {
            case "feature":

            if(preg_match("/^[0-9A-Za-z- ]+$/",$_POST['type']) &&
                    preg_match("/^[0-9A-Za-z- ]+$/",$_POST['name']) &&
                    preg_match("/^[0-9A-Za-z-\. ]*$/",$_POST['description']))
            {
                $n = $_POST["name"];
                $t = $_POST["type"];
                $d = $_POST["description"];
                mysql_query("insert into featuresnew(name,type,lat,lon,pending,".
                            "description,userid)"                        
                            ."values ('".$n."','".  $t.
                        "',$p[lat],$p[lon],0,'$d',$userid)"); 

                echo "Feature added successfully to database.";
            }
            else
            {
                echo "Your feature type and/or name was invalid. ".
                     "Permitted characters A-Z,spaces,underscores,dash. ";
            }
            
            break;

            // Delete all featuresnew within 5 pixels
            case "featuredel":

//            $range =  5.0 / $map->scale; 
			$range = 0.0000000000001;

            $sql="delete from featuresnew where ".
                        "abs(lat-$p[lat]) <$range and ".
                        "abs(lon-$p[lon])<$range";
            mysql_query($sql);
            echo "Deleted successfully";
            break;

            // Find the nearest feature within 10 pixels
            case "featurequery":
            case "featureupdate":
            
            $range =  10.0 / $map->scale; 
            $sql="select * from featuresnew where abs(lat-$p[lat])<$range ".
                 "and abs(lon-$p[lon])<$range ".
                 "order by sqrt(abs(lat-$p[lat])*abs(lat-$p[lat]) + ".
                 "abs(lon-$p[lon])*abs(lon-$p[lon])) limit 1";
            $result=mysql_query($sql);
            if(mysql_num_rows($result))
            {
                $row=mysql_fetch_array($result);
                if($_POST["action"]=="featurequery")
                {
					$desc = $row['description']==null? "":$row['description'];
                    echo "name=$row[name];desc=$desc;type=$row[type]";
                }
                else if 
					(preg_match("/^[0-9A-Za-z-\., ]*$/",$_POST['description']))
                {
                    mysql_query("update featuresnew set name='".$_POST['name'].
                                "',description='".$_POST['description'].
                                "' where id=$row[id]");
                    echo "Update successful.";
                }
				else
				{
					echo "Invalid format for description.";
				}
            }
			case "osmupload":
				$a=uploadToOSM($_REQUEST["osmapicall"], 
						stripslashes($_REQUEST["osm"]),
						$_REQUEST["ver"]);
				echo (preg_match("/^\d+.*$/", $a)) ? $a: 0;
				break;
		}
	}
	else
	{
		header("HTTP/1.0 404 Not Found");
	}
    //mysql_close($conn);
}
else
{
   	//echo "You need to be logged in to perform this action.";	
	header("HTTP/1.0 401 Unauthorized");
}

function uploadToOSM($osmapicall,$osm,$ver)
{
	$resp=0;
	$t=time();
	$fp = fopen("/var/www-data/tmp$t.osm","w");
	//$fp = tmpfile();
	fwrite($fp,$osm);
	fclose($fp);
	$fp=fopen("/var/www-data/tmp$t.osm","r");
	//fseek($fp,0);
	$url = "http://www.openstreetmap.org/api/$ver/$osmapicall";

	$ch=curl_init ($url);
	curl_setopt($ch,CURLOPT_RETURNTRANSFER,true);
	curl_setopt($ch,CURLOPT_HEADER,false);
	curl_setopt($ch,CURLOPT_USERPWD,OSM_LOGIN);
	curl_setopt($ch,CURLOPT_PUT,true);
	curl_setopt($ch,CURLOPT_INFILE,$fp);
	curl_setopt($ch,CURLOPT_INFILESIZE,filesize("/var/www-data/tmp$t.osm"));
	$resp=curl_exec($ch);
	curl_close($ch);
	
	fclose($fp);
	return $resp;
}
?>
