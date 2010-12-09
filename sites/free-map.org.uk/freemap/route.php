<?php
require_once('../lib/functionsnew.php');
require_once('freemap_functions.php');

header("Content-type: text/xml");

$cleaned = clean_input ($_GET);
$wayids = explode(",", $cleaned['ways']);

if(count($wayids) < 2)
{
    header("HTTP/1.1 400 Bad Request");
    exit;
}
$conn=pg_connect("dbname=gis user=gis");

echo "<route>\n";

for($i=0; $i<count($wayids); $i++)
{
    $way = get_annotated_way($wayids[$i]);
    $start_wp = $way['points'][0];
    $end_wp = $way['points'][count($way['points'])-1];

    if($i>0)
    {    
        // possibly need to reverse the first way, but only know this once
        // we have the second way
        if($i==1)
        {
            // first way wrongway round
            if($start_wp==$firstway_start_wp || $end_wp==$firstway_start_wp)
            {
                annotated_way_to_xml($firstway,true);
                $start_rp = $firstway_end_wp;
                $end_rp = $firstway_start_wp;
            }
            else
            {
                annotated_way_to_xml($firstway);
                $start_rp = $firstway_start_wp;
                $end_rp = $firstway_end_wp;
            }
        }

        // way is the "right way round" - the first point is the same as the
        // last point (routewise not waywise) of the last way
        if ($start_wp == $end_rp)
        {
            annotated_way_to_xml($way);

            // routewise points same as waywise points 
            $start_rp = $start_wp;
            $end_rp = $end_wp;
        }
        else
        {
            annotated_way_to_xml($way,true);

            // routewise points opposite to waywise points 
            $start_rp = $end_wp;
            $end_rp = $start_wp;
        }
    }
    // Do nothing for first way except save it
    else
    {
        $firstway = $way;
        $firstway_start_wp=$start_wp;
        $firstway_end_wp = $end_wp;
    }
}

echo "</route>\n";
pg_close($conn);

?>
