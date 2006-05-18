#!/usr/bin/perl

use lib '/home/mikel/perl';
use DBI;
use GD;
use Math::Trig;
use CGI;

$cgi = new CGI;
$bbox = $cgi->param("BBOX") || $cgi->param("bbox") || '-2.0641200,52.37683465,-1.76921,52.864987';
$width = $cgi->param("WIDTH") || $cgi->param("width") || 256;
$height = $cgi->param("HEIGHT") || $cgi->param("height") || 128;

print STDERR $bbox . $width . $height . "\n";

@bbox = split(",",$bbox);

$lon1 = $bbox[0];
$lat2 = $bbox[1];
$lon2 = $bbox[2];
$lat1 = $bbox[3];

if ($width > 256 || $height > 256 || (($lon2 - $lon1) * ($lat1 - $lat2)) > 0.0035) {
	print STDERR "too big\n";
	$im = new GD::Image($width,$height);
	$white = $im->colorAllocate(255,255,255);
	$im->transparent($white);
	print "Content-type: image/png\n\n";
	print $im->png;
	exit;
}

print STDERR "not too big\n";

$pi = 3.1415926535;
$dpp = ($lon2 - $lon1) / $width;

$clon = ($lon2 + $lon1) / 2;
$clat = ($lat2 + $lat1) / 2;
$dlon = $width / 2 * $dpp;
$dlat = $height / 2 * $dpp * cos($clat *  $pi / 180);

$tx = $clon - $dlon;
$ty = log(tan($pi/4 + (($clat - $dlat) * $pi / 180 / 2)));

$bx = $clon + $dlon;
$by = log(tan($pi/4 + (($clat + $dlat) * $pi / 180 / 2)));


$dbh = DBI->connect("dbi:mysql:openstreetmap;128.40.59.181;","openstreetmap","openstreetmap");

$sql = "select id, latitude, longitude, visible, tags from (select * from (select nodes.id, nodes.latitude, nodes.longitude, nodes.visible, nodes.tags from nodes, nodes as a where a.latitude > $lat2  and a.latitude < $lat1  and a.longitude > $lon1 and a.longitude < $lon2 and nodes.id = a.id order by nodes.timestamp desc) as b group by id) as c where visible = true and latitude > $lat2 and latitude < $lat1  and longitude > $lon1 and longitude < $lon2";
$sth = $dbh->prepare($sql);
$sth->execute;
while (@row = $sth->fetchrow_array) {
	$nodeslat{ $row[0] } = $row[1];
	$nodeslon{ $row[0] } = $row[2];
	$nodesvis{ $row[0] } = $row[3];
	if (! $clause) {
		$clause .= $row[0];
	} else {
		$clause .= ',' . $row[0];
	}
}

$sql = "SELECT segment.id, segment.node_a, segment.node_b FROM ( select * from (SELECT * FROM segments where node_a IN ($clause) OR node_b IN ($clause) ORDER BY timestamp DESC) as a group by id) as segment where visible = true";
#print $sql;
$sth = $dbh->prepare($sql);
$sth->execute;


$im = new GD::Image($width,$height);
$red = $im->colorAllocate(255,0,0);
$white = $im->colorAllocate(255,255,255);
$black = $im->colorAllocate(0,0,0);

$im->fill(0,0,$red);
$im->transparent($red);

#$brush = new GD:Image(3,1);
#$brush_white = $brush->colorAllocate(255,255,255);
#$brush_black = $bruch->colorAllocate(0,0,0);
#$brush->setPixel(0,0,$brush_black);
#$brush->setPixel(1,0,$brush_white);
#$brush->setPixel(2,0,$brush_black);

#$im->setBrush($brush);

while (@row = $sth->fetchrow_array) {
	$nodea = $row[1];
	$nodeb = $row[2];
     
     	if ($nodesvis{ $nodea } == 1 && $nodesvis{ $nodeb } == 1) {
		$x1 = ($nodeslon{ $nodea } - $tx) / ($bx - $tx) * $width;
		$tmpy = log(tan($pi/4 + (($nodeslat{ $nodea }) * $pi / 180 / 2)));
		$y1 = ($height - $height * ($tmpy - $ty) /  ($by - $ty) );
		

		$x2 = ($nodeslon{ $nodeb } - $tx) / ($bx - $tx) * $width;
		$tmpy = log(tan($pi/4 + (($nodeslat{ $nodeb }) * $pi / 180 / 2)));
		$y2 = ($height - $height * ($tmpy - $ty) /  ($by - $ty) );

		$im->setThickness(3);
		$im->line($x1,$y1,$x2,$y2,$black);		
		$im->setThickness(1);
		$im->line($x1,$y1,$x2,$y2,$white);		
		#print $nodeslon{ $nodea } . ',' . $nodeslat{ $nodea } . ',' . $nodeslon{ $nodeb } . ',' . $nodeslat{ $nodeb } . "\n";
		#print $nodeslon{ $nodea } . ',' . $nodeslat{ $nodea } . ',' . $nodeslon{ $nodeb } . ',' . $nodeslat{ $nodeb } . "\n";
		#print "$x1,$y1,$x2,$y2\n";
	}
}

print "Content-type: image/png\n\n";
print $im->png;
