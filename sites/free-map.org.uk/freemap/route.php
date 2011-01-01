<?php

require_once('../lib/functionsnew.php');
require_once('freemap_functions.php');
require_once('Walkroute.php');

session_start();

$conn=pg_connect("dbname=gis user=gis");
$cleaned = clean_input($_REQUEST,'pgsql');

switch($cleaned['action'])
{
  case 'get':

    if($cleaned['source']=='db')
    {
        $wr=new Walkroute((int)$cleaned['id']);
    }
    elseif(isset($cleaned['serialised']))
    {
        $wr=new Walkroute($cleaned['serialised'],true);
    }
    else
    {
        $wr=new Walkroute($cleaned['route']);
    }

    switch($cleaned['format'])
    {
      case 'xml':
        header("Content-Type: text/xml");
        header("Content-Disposition: attachment; filename=route.xml");
          $wr->to_xml();
        break;
      case 'html':
        $wr->to_html();
        break;
      case 'htmlpage':
          ?>
        <html>
        <head>
        <link rel='stylesheet' 
        type='text/css' href='/freemap/css/freemap2.css '/>
        <title>Your walk route</title>
        </head>
        <body>
        <?php
        $wr->to_html();
        ?>
        <p><a href='/freemap/index.php'>Back to map</a></p>
        </body>
        </html>
        <?php
        break;
      case 'pdf':
          //header("Content-type: application/pdf");
        $wr->to_pdf();
        break;
      case 'png':
          header("Content-type: image/png");
          $wr->to_png(); 
          break;
    }
    break;


  case 'add':
    if(isset($_SESSION['gatekeeper']))
    {
        $q=("INSERT INTO routes (userid,route) VALUES ".
        "('$_SESSION[gatekeeper]',".
        "GeomFromText('LINESTRING($cleaned[route])',900913))");
        echo $q;
        pg_query($q);
    }
    else
    {
        header("HTTP/1.1 401 Unauthorized");
    }    
    break;
}

pg_close($conn);

?>
