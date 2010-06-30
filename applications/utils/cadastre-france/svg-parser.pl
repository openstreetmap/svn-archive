#!/usr/bin/perl
# WTFPLv2 [http://sam.zoy.org/wtfpl/]
use strict;
use XML::Parser;
use Geo::OSR;

if ($#ARGV != 5) {
    print "Usage: svg-parser.pl [IGNF] [fichier.svg] [bbox]\n";
    exit;
}

my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
my $tag_source = "cadastre-dgi-fr source : Direction Générale des Impôts - Cadastre. Mise à jour : " . ($year + 1900);
my $tag_version = "v0.3";

my %couleurs = ("ffffff" => "bbox",
		# rgb(100%,89.802551%,59.999084%) ffe599
		"ffe599" => "building_nowall ",
		# rgb(100%,79.998779%,19.999695%) ffcc33
		"ffcc33" => "building ",
		# fill:rgb(59.606934%,76.470947%,85.488892%) 98c3da
		"98c3da" => "water ",
		# fill:rgb(10.195923%,47.842407%,67.449951%) 1a7aac
		"1a7aac" => "riverbank "
    );
my %tags = ("building" => " <tag k=\"building\" v=\"yes\"/>\n",
	    "building_nowall" => " <tag k=\"building\" v=\"yes\"/>\n"
                               . " <tag k=\"wall\" v=\"no\"/>\n",
	  # La couleur ne permet pas de différencier une piscine, d'une mare, d'une fontaine, d'un lac
	    "water" => " <tag k=\"natural\" v=\"water\"/>\n",
	    "riverbank" => " <tag k=\"waterway\" v=\"riverbank\"/>\n"
    );


my @bbox_lbr93;
my @bbox_pts;
# Hash qui à chaque point associe une ref
my %points;
my @ways;
my @relations;
my $refnodes = 0;
my $refways = 0;
my $refrel = 0;

my $source = Geo::OSR::SpatialReference->create ();
my $target = Geo::OSR::SpatialReference->create ();

$source->ImportFromProj4("+init=IGNF:" . "$ARGV[0]" . " +wktext");
$target->ImportFromEPSG ('4326');

my $transf = new Geo::OSR::CoordinateTransformation ($source, $target);

my $parser = new XML::Parser ( Handlers => {
    Start => \&hdl_start,
    End   => \&hdl_end,
    Default => \&hdl_def
			       });
my $surface = 0;

sub hdl_start {
    my  ($p, $elt, %atts) = @_;
    @bbox_lbr93 = ($ARGV[2],$ARGV[3],$ARGV[4],$ARGV[5]);
    $surface = 1  if ($surface == 0 && $elt eq 'g' && $atts{'id'} eq 'surface0');
    if ($surface && $elt eq 'path' )
    {
	my @m  = get_matrix ($atts{'transform'});
	if ($atts{'style'} =~ m/fill:rgb\(/)
	{
	    my ($rouge,$vert,$bleu) = ($atts{'style'} =~ m/fill:rgb\((\d*\.?\d*)%,(\d*\.?\d*)%,(\d*\.?\d*)%\)/);
	    my $couleur_hexa = (hexa ($rouge)).(hexa ($vert)).(hexa ($bleu));
	    $relations[$refrel] = $couleurs{$couleur_hexa};

	    my $s = $atts{'d'};
	    if (defined($relations[$refrel])) {
		do {
		    my @points = format_point (\$s,@m);
		    if (!defined(@bbox_pts) && $relations[$refrel] eq "bbox")
		    {
			@bbox_pts = minmax(@points);
		    }
		    elsif (defined(@points))
		    {
			$relations[$refrel] .="$refways ";
			new_way(@points);
		    }
		} while ($s =~ m/M (-?\d*\.?\d*) (-?\d*\.?\d*) L/);
		$refrel++;
	    }
	}
    }
}

sub hdl_end {    my  ($p, $elt, %atts) = @_;
		 $surface = 0  if ($surface == 1 && $elt eq 'g' && $atts{'id'} eq 'surface0');}

sub hdl_def {}

sub format_point {
    my ($s,@m) = @_;
    my @points;
    return unless $$s =~ s/^M //;
    $points[0] = transform_point ($s,@m);
    my $i = 1;
    while ($$s =~ s/^L //)
    {
	$points[$i] = transform_point ($s,@m);
	$i += 1;
    }
    $$s =~ s/^Z //;
    return @points
}

sub transform_point {
    my ($s,@m) = @_;
    my $p;

    ($p->[0],$p->[1]) = $$s =~ m/(-?\d*\.?\d*) (-?\d*\.?\d*) ?/;
    $$s =~ s/(-?\d*\.?\d*) (-?\d*\.?\d*) ?//;

    ($p->[0],$p->[1]) = (($m[0]*$p->[0] +  $m[2]*$p->[1] + $m[4]),($m[1]*$p->[0] +  $m[3]*$p->[1] + $m[5]));

    if (defined(@bbox_pts))
    {
	($p->[0],$p->[1]) = (
	    (($p->[0] - $bbox_pts[0]) * ($bbox_lbr93[2]-$bbox_lbr93[0])/($bbox_pts[2]-$bbox_pts[0]) + $bbox_lbr93[0]),
	    (($p->[1] - $bbox_pts[1]) * ($bbox_lbr93[1]-$bbox_lbr93[3])/($bbox_pts[3]-$bbox_pts[1]) + $bbox_lbr93[3])
	    );
	return ($transf->TransformPoint ($p->[0],$p->[1]));
    }
    else {
	return $p;
    }
}

sub get_matrix {
    my ($s) = @_;
    return split (/,/, $s) if $s =~ s/matrix\((.*)\)/$1/;
    return (1,0,0,1,0,0)
}

sub minmax {
    my @nodes = @_;
    my ($xmin,$ymin,$xmax,$ymax);
    my $node;
    foreach $node (@nodes) {
	$xmin = $node->[0] if ($node->[0] < $xmin);
	$ymin = $node->[1] if ($node->[1] < $ymin);
	$xmax = $node->[0] if ($node->[0] > $xmax);
	$ymax = $node->[1] if ($node->[1] > $ymax);
    }
    return ($xmin,$ymin,$xmax,$ymax)
}

sub hexa {
    my ($pourcent) = @_;
    # on arrondi au plus proche car sinon sprintf prend la valeur tronquée
    return sprintf("%02x", int ($pourcent * 255 / 100 + 0.5));
}

sub new_point {
    my ($lat,$lon) = @_;
    if (!defined($points{"$lon,$lat"}))
    {
	$points{"$lon,$lat"} = $refnodes;
	$refnodes++;
    }
}

sub new_way {
    my (@points) = @_;
    foreach my $node (@points) {
	my ($lon,$lat) = ($node->[0],$node->[1]);
	new_point($lat,$lon);
	$ways[$refways] .= $points{"$lon,$lat"} . " ";
    }
    $refways++;
}

$parser->parsefile($ARGV[1]);

my $bbox_wgs84_min = ($transf->TransformPoint (@bbox_lbr93[0,1]));
my $bbox_wgs84_max = ($transf->TransformPoint (@bbox_lbr93[2,3]));
print "<?xml version='1.0' encoding='UTF-8'?>\n";
print "<osm version='0.6' generator='plop'>\n";
print "<bounds minlat=\"$bbox_wgs84_min->[1]\" minlon=\"$bbox_wgs84_min->[0]\" maxlat=\"$bbox_wgs84_max->[1]\" maxlon=\"$bbox_wgs84_max->[0]\"/>\n";
foreach my $node (keys %points)
{
    my ($lon,$lat) = split (/,/, $node);
    print "<node id=\"" , -1-$points{$node} , "\" lat=\"$lat\" lon=\"$lon\"/>\n";
}

foreach my $i (1..$#relations)
{
    my @relways = (split (/ /,$relations[$i]));
    if ($#relways > 0) {
	foreach my $j (@relways[1..$#relways])
	{
	    print "<way id=\"" , -1-$j , "\">\n";
	    foreach my $node (split (/ /,$ways[$j]))
	    {
		print " <nd ref=\"" , -1-$node , "\"/>\n";
	    }
	    print $tags{$relways[0]} if (($j == $relways[1]) || $relways[0] eq "riverbank");
	    print " <tag k=\"source\" v=\"$tag_source\"/>\n";
	    print " <tag k=\"note:import-bati\" v=\"$tag_version\"/>\n";
	    print "</way>\n";
	}
	if ($#relways > 1)
	{
	    print "<relation id=\"" , -1-$i , "\">\n";
	    print " <tag k=\"type\" v=\"multipolygon\"/>\n";
	    print " <member type=\"way\" ref=\"" , -1-$relways[1] , "\" role=\"outer\"/>\n";
	    # Le lit des cours d'eau a l'air d'être défini par plusieurs outers
	    my $role = ($relways[0] eq "riverbank") ? "outer" : "inner" ;
	    foreach my $way (@relways[2..$#relways])
	    {
		print " <member type=\"way\" ref=\"" , -1-$way , "\" role=\"$role\"/>\n";
	    }
	    print "</relation>\n";
	}
    }
}
print "</osm>";
