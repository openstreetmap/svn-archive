#!/usr/bin/perl
# WTFPLv2 [http://sam.zoy.org/wtfpl/]
use strict;
use XML::Parser;
use Geo::OSR;
use Getopt::Std;

our ($opt_p,$opt_b,$opt_r,$opt_w,$opt_l,$opt_t);
getopts("l:p:b:r:w:t:");
unless ($opt_p || $opt_b || $opt_r || $opt_w || $opt_l || $opt_t) {
# Options par defaut, on affiche les limites administratives, le bati,
# les waterway et les riverbank sur la sortie standard
    $opt_b = $opt_r = $opt_w = $opt_t = "-";
}

# Associe chaque nom de fichiers avec son filehandler.
my %files;

sub handle_opts {
    my ($str) = @_;
    if (defined($str) && !defined($files{$str})) {
	my $filehandle;
	open($filehandle,">$str") or die "Impossible d'ouvrir : $!";
	$files{$str} = \$filehandle;
    }
    return $files{$str};
}

# Imprime une chaîne de caractères sur tous les fichiers ouverts
sub print_files {
    my ($str) = @_;
    if (%files) {
	for my $file (keys %files) {
	    print {${$files{$file}}} $str;
	}
    }
    else {
	print STDOUT $str;
    }
}

handle_opts($opt_l);
handle_opts($opt_p);
handle_opts($opt_b);
handle_opts($opt_r);
handle_opts($opt_w);
handle_opts($opt_t);

if (($#ARGV % 5) != 0) {
    print "Usage: svg-parser.pl (-lpbrwt) [IGNF] [[fichier.svg] [bbox] ..]\n";
    exit;
}

my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
my $tag_source = "cadastre-dgi-fr source : Direction Générale des Impôts - Cadastre. Mise à jour : " . ($year + 1900);
my $tag_version = "v0.3";

# Identifie à quoi correspond chaque couleur de remplissage du svg
my %couleurs_remplissage = ("ffffff" => "bbox",
			    # rgb(100%,89.802551%,59.999084%) ffe599
			    "ffe599" => "building_nowall",
			    # rgb(100%,79.998779%,19.999695%) ffcc33
			    "ffcc33" => "building",
			    # fill:rgb(59.606934%,76.470947%,85.488892%) 98c3da
			    "98c3da" => "water",
			    # fill:rgb(10.195923%,47.842407%,67.449951%) 1a7aac
			    "1a7aac" => "riverbank"
    );

# Identifie à quoi correspond chaque objet qui ne peut être identifié
# uniquement grâce à sa couleur de remplissage
# Pour les ways fermés
my %style_closed = (
    "fill:none;stroke-width:0.77;stroke-linecap:butt;stroke-linejoin:miter;stroke:rgb(0%,0%,0%);stroke-opacity:1;stroke-miterlimit:10;" => "parcelle",
    "fill:none;stroke-width:18;stroke-linecap:butt;stroke-linejoin:miter;stroke:rgb(100%,100%,100%);stroke-opacity:1;stroke-miterlimit:10;" => "limite"
    );

# Pour les ways ouverts
my %style_opened = (
    "fill:none;stroke-width:0.77;stroke-linecap:butt;stroke-linejoin:miter;stroke:rgb(0%,0%,0%);stroke-opacity:1;stroke-dasharray:17.72,11.82;stroke-miterlimit:10;" => "trottoir",
    "fill:none;stroke-width:0.77;stroke-linecap:butt;stroke-linejoin:miter;stroke:rgb(0%,0%,0%);stroke-opacity:1;stroke-dasharray:5.9,5.9;stroke-miterlimit:10;" => "trottoir",
    "fill:none;stroke-width:3.55;stroke-linecap:butt;stroke-linejoin:miter;stroke:rgb(0%,0%,0%);stroke-opacity:1;stroke-miterlimit:10;" => "train",
    "fill:none;stroke-width:0.77;stroke-linecap:butt;stroke-linejoin:miter;stroke:rgb(0%,0%,0%);stroke-opacity:1;stroke-dasharray:5.9,11.82;stroke-miterlimit:10;" => "footpath"
    );

my %tags = ("building" =>        " <tag k=\"building\" v=\"yes\"/>\n",
	    "building_nowall" => " <tag k=\"building\" v=\"yes\"/>\n"
	                       . " <tag k=\"wall\" v=\"no\"/>\n",
	    # La couleur ne permet pas de différencier une piscine, d'une mare, d'une fontaine, d'un lac
	    "water" =>           " <tag k=\"natural\" v=\"water\"/>\n",
	    "riverbank" =>       " <tag k=\"waterway\" v=\"riverbank\"/>\n",
	    "parcelle" =>        " <tag k=\"natural\" v=\"land\"/>\n",
	    "limite" =>          " <tag k=\"boundary\" v=\"administrative\"/>\n",
	    "trottoir" =>        " <tag k=\"man_made\" v=\"sidewalk\"/>\n",
	    "train" =>           " <tag k=\"railway\" v=\"rail\"/>\n",
	    "footpath" =>        " <tag k=\"highway\" v=\"footpath\"/>\n"
    );

our @bbox_lbr93;
our @bbox_pts;
# Hash qui à chaque point associe une ref
my %points;
my @ways;
my @relations;
my $refnodes = 0;
my $refways = 0;
my $refrel = 0;

my $source = Geo::OSR::SpatialReference->create ();
my $target = Geo::OSR::SpatialReference->create ();

$source->ImportFromProj4("+init=IGNF:" . shift . " +wktext");
$target->ImportFromEPSG ('4326');

my $transf = new Geo::OSR::CoordinateTransformation ($source, $target);

my $parser = new XML::Parser ( Handlers => {
    Start => \&hdl_start,
    End   => \&hdl_end,
    Default => \&hdl_def
			       });
my $surface = 0;
my $profondeur_groupe = 0;

sub hdl_start {
    my  ($p, $elt, %atts) = @_;
    $profondeur_groupe++ if ($surface == 1 && $elt eq 'g');
    $surface = 1  if ($surface == 0 && $elt eq 'g' && $atts{'id'} eq 'surface0');
    if ($surface && $elt eq 'path' )
    {
	my @m  = get_matrix ($atts{'transform'});
	if ($atts{'style'} =~ m/fill:rgb\(/)
	{
	    my ($rouge,$vert,$bleu) = ($atts{'style'} =~ m/fill:rgb\((\d*\.?\d*)%,(\d*\.?\d*)%,(\d*\.?\d*)%\)/);
	    my $couleur_hexa = (hexa ($rouge)).(hexa ($vert)).(hexa ($bleu));

	    my $type = $couleurs_remplissage{$couleur_hexa};

	    my $s = $atts{'d'};
	    if (defined($type)) {
		my @points = lire_points (\$s,@m);
		if ($#bbox_pts != 3) {
		    if ($type eq "bbox")
		    {
			@bbox_pts = minmax(@points);
		    }
		}
		else
		{
		    if (($#points >= 0) && est_dans_bbox(@points))
		    {
			my $ref = new_rel("multipolygon");
			rel_add_way($ref,new_way($type,\@points)) if ($#points >= 0);
			while ($s =~ m/M (-?\d*\.?\d*) (-?\d*\.?\d*) L/)
			{
			    my @points = lire_points (\$s,@m);
# Le signe de la référence au way indique le sens d'orientation du polygone
			    rel_add_way($ref,(-1+2*(aire_polygone(@points)>0)) * new_way($type,\@points)) if ($#points >= 0);
			}
			$refrel++;
		    }
		}
	    }
	}
	elsif ($#bbox_pts == 3)
	{
	    $atts{'style'} =~ s/^ *//;
	    $atts{'style'} =~ s/ *$//;
	    my $style;
	    if ($atts{'d'} =~ m/Z/) {
		$style = $style_closed{$atts{'style'}};
	    }
	    else {
		$style = $style_opened{$atts{'style'}};
	    }
	    if (defined($style)) {
		my $s = $atts{'d'};
		my @points = lire_points (\$s,@m);
		if (est_dans_bbox(@points)) {
		    my $ref = new_rel("multipolygon");
		    rel_add_way($ref,new_way($style,\@points)) if ($#points >= 0);
		}
	    }
	}
    }
}

sub hdl_end {
    my  ($p, $elt, %atts) = @_;
    if ($surface == 1 && $elt eq 'g') {
	if ($profondeur_groupe == 0) {
	    $surface = 0;
	    @bbox_pts = ();
	}
	else {
	    $profondeur_groupe--;
	}
    }
}

sub hdl_def {}

sub lire_points {
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

# Transforme les coordonnées d'un point pris à la tête d'une chaîne
# pour obtenir des coordonnées en Lambert93
sub transform_point {
    my ($s,@m) = @_;
    my $p;

    ($p->[0],$p->[1]) = $$s =~ m/(-?\d*\.?\d*) (-?\d*\.?\d*) ?/;
    $$s =~ s/(-?\d*\.?\d*) (-?\d*\.?\d*) ?//;

    # Transformations dues à la matrice associées au path
    ($p->[0],$p->[1]) = (
	($m[0]*$p->[0] +  $m[2]*$p->[1] + $m[4]),
	($m[1]*$p->[0] +  $m[3]*$p->[1] + $m[5]));

    if ($#bbox_pts == 3)
    {
	# On "convertit" les coordonnées à partir du référentiel du
	# pdf (en pts) vers du LAMBERT93
	($p->[0],$p->[1]) = (
	    (($p->[0]
	      - $bbox_pts[0]) * ($bbox_lbr93[2]-$bbox_lbr93[0])/($bbox_pts[2]-$bbox_pts[0])
	     + $bbox_lbr93[0]),
	    (($p->[1]
	      - $bbox_pts[1]) * ($bbox_lbr93[1]-$bbox_lbr93[3])/($bbox_pts[3]-$bbox_pts[1])
	     + $bbox_lbr93[3])
	    );
    }
    # Si la bbox n'a pas encore été définie, on retourne les
    # coordonnées brutes, dans le référentiel du pdf
    return $p;
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
	$xmin = $node->[0] if (!defined($xmin) || $node->[0] < $xmin);
	$ymin = $node->[1] if (!defined($ymin) || $node->[1] < $ymin);
	$xmax = $node->[0] if (!defined($xmax) || $node->[0] > $xmax);
	$ymax = $node->[1] if (!defined($ymax) || $node->[1] > $ymax);
    }
    return ($xmin,$ymin,$xmax,$ymax)
}

# Teste si le premier node d'un way se trouve dans la bbox, si il est
# sur un bord, teste le point suivant, si tous les points sont sur des
# bords retourne vrai
sub est_dans_bbox {
    my @nodes = @_;
    for my $node (@nodes) {
	if ($node->[0] >= $bbox_lbr93[0] &&
	    $node->[1] >= $bbox_lbr93[1] &&
	    $node->[0] <= $bbox_lbr93[2] &&
	    $node->[1] <= $bbox_lbr93[3])
	{
	    if (!($node->[0] == $bbox_lbr93[0] ||
		  $node->[1] == $bbox_lbr93[1] ||
		  $node->[0] == $bbox_lbr93[2] ||
		  $node->[1] == $bbox_lbr93[3]))
	    {
		return 1;
	    }
	}
	else
	{
	    return 0;
	}
    }
    return 1;
}

# Retourne l'aire d'un polygone * 2 (peut servir à déterminer le sens)
sub aire_polygone
{
    my (@points) = @_;
    my $somme;

    for my $i (0..$#points) {
	$somme += $points[$i-1][0]*$points[$i][1] - $points[$i][0]*$points[$i-1][1];
    }
    return $somme;
}

# Transforme un pourcentage en sa valeur héxadécimale (de 00 à ff)
sub hexa {
    my ($pourcent) = @_;
    # on arrondi au plus proche car sinon sprintf prend la valeur tronquée
    return sprintf("%02x", int ($pourcent * 255 / 100 + 0.5));
}

sub new_point {
    my ($lat,$lon,$type) = @_;
    my $str = sprintf("%.2f,%.2f",$lon,$lat);
    if (defined($points{$str}))
    {
	push @{$points{$str}{"type"}}, $type;
	return $points{$str}{"ref"};
    }
    else
    {
	$points{$str}{"coord"} = [$lon,$lat];
	$points{$str}{"ref"} = $refnodes;
	$points{$str}{"type"} = [$type];
	return $refnodes++;
    }
}

sub new_way {
    my ($type,$points) = @_;
    my %way;
    $way{"type"} = $type;
    $way{"ref"} = $refways;
    foreach my $node (@$points) {
	my ($lon,$lat) = ($node->[0],$node->[1]);
	push @{$way{"nodes"}}, new_point($lat,$lon,$type);
    }
    $ways[$refways] = \%way;
    return $refways++;
}

sub new_rel {
    my ($type) = @_;
    my %rel;
    $rel{"type"} = $type;
    $rel{"ref"} = $refrel;
    $relations[$refrel] = \%rel;
    return $refrel++;
}

sub rel_add_way {
    my ($ref,@ways) = @_;
    push @{$relations[$ref]{"ways"}},@ways;
}

my @points_imprimes = ();
my @ways_imprimes = ();
sub print_point
{
    my ($lon,$lat,$ref,$file) = @_;
    if (defined($file) && !defined($points_imprimes[$ref]{$file}))
    {
	($lon,$lat) = @{$transf->TransformPoint ($lon,$lat)};
	print {${$files{$file}}} "<node id=\"" , -1-$ref , "\" lat=\"$lat\" lon=\"$lon\"/>\n";
	$points_imprimes[$ref]{$file} = 1;
    }
}

sub print_way
{
    my ($ref_way,$file,$type,$tag) = @_;
    if (defined($file) && !defined($ways_imprimes[$ref_way]{$file})) {
	print {${$files{$file}}} "<way id=\"" , -1-$ref_way , "\">\n";
	foreach my $ref_node (@{$ways[$ref_way]{"nodes"}})
	{
	    print {${$files{$file}}} " <nd ref=\"" , -1-$ref_node , "\"/>\n";
	}
	print {${$files{$file}}} $tags{$type} if $tag;
	print {${$files{$file}}} " <tag k=\"source\" v=\"$tag_source\"/>\n";
	print {${$files{$file}}} " <tag k=\"note:import-bati\" v=\"$tag_version\"/>\n";
	print {${$files{$file}}} "</way>\n";
    }
}

# Traite chaque fichier associé à sa bbox dans l'ordre des arguments
my @bbox_lbr93_glob;
while (@ARGV) {
    my $fichier = shift;
    $bbox_lbr93[0] = shift;
    $bbox_lbr93[1] = shift;
    $bbox_lbr93[2] = shift;
    $bbox_lbr93[3] = shift;

# Calcule la bbox globale
    $bbox_lbr93_glob[0] = $bbox_lbr93[0]
	if (!defined($bbox_lbr93_glob[0]) || $bbox_lbr93[0] < $bbox_lbr93_glob[0]);
    $bbox_lbr93_glob[1] = $bbox_lbr93[1]
	if (!defined($bbox_lbr93_glob[1]) || $bbox_lbr93[1] < $bbox_lbr93_glob[1]);
    $bbox_lbr93_glob[2] = $bbox_lbr93[2]
	if (!defined($bbox_lbr93_glob[2]) || $bbox_lbr93[2] > $bbox_lbr93_glob[2]);
    $bbox_lbr93_glob[3] = $bbox_lbr93[3]
	if (!defined($bbox_lbr93_glob[3]) || $bbox_lbr93[3] > $bbox_lbr93_glob[3]);

    $parser->parsefile($fichier);
}

my @points_imprimes = ();
my @ways_imprimes = ();
# On imprime l'en-tête sur tous les fichiers de sortie
my @bbox_wgs84_glob;
@bbox_wgs84_glob[0,1] = @{$transf->TransformPoint (@bbox_lbr93_glob[0,1])};
@bbox_wgs84_glob[2,3] = @{$transf->TransformPoint (@bbox_lbr93_glob[2,3])};
print_files "<?xml version='1.0' encoding='UTF-8'?>\n";
print_files "<osm version='0.6' generator='plop'>\n";
print_files "<bounds minlat=\"$bbox_wgs84_glob[1]\" minlon=\"$bbox_wgs84_glob[0]\" maxlat=\"$bbox_wgs84_glob[3]\" maxlon=\"$bbox_wgs84_glob[2]\"/>\n";

for my $coord_point (sort keys %points) {
    my ($lon,$lat) = @{$points{$coord_point}{"coord"}};
    my $ref = $points{$coord_point}{"ref"};
    for my $type (@{$points{$coord_point}{"type"}}) {
	print_point($lon,$lat,$ref,$opt_b) if (($type eq "building_nowall" || $type eq "building"));
	print_point($lon,$lat,$ref,$opt_w) if (($type eq "water"));
	print_point($lon,$lat,$ref,$opt_r) if (($type eq "riverbank"));
	print_point($lon,$lat,$ref,$opt_p) if (($type eq "parcelle" || $type eq "trottoir" || $type eq "footpath"));
	print_point($lon,$lat,$ref,$opt_t) if (($type eq "train"));
	print_point($lon,$lat,$ref,$opt_l) if (($type eq "limite"));
    }
}

for my $rel (@relations) {
    if ($#{$rel->{"ways"}} >= 0) {
	my $tag = 1;
	my $file;
	my $first_way = $ways[$rel->{"ways"}[0]];
	my $type = $first_way->{"type"};

	$file = $opt_b if (($type eq "building_nowall" || $type eq "building"));
	$file = $opt_w if (($type eq "water"));
	$file = $opt_r if (($type eq "riverbank"));
	$file = $opt_p if (($type eq "parcelle" || $type eq "trottoir" || $type eq "footpath"));
	$file = $opt_t if (($type eq "train"));
	$file = $opt_l if (($type eq "limite"));

	next unless (defined($file));

	for my $ref_way (@{$rel->{"ways"}}) {
	    my $type = $ways[$ref_way]{"type"};
	    # N'écrit pas le tag pour un inner
	    print_way(abs($ref_way),$file,$type,($ref_way>=0));
	}
	if ($#{$rel->{"ways"}} >= 1) {
	    print {${$files{$file}}} "<relation id=\"" , -1-$rel->{"ref"} , "\">\n";
	    print {${$files{$file}}} " <tag k=\"type\" v=\"" , $rel->{"type"} , "\"/>\n";
	    print {${$files{$file}}} " <member type=\"way\" ref=\"" , -1-$rel->{"ways"}[0] , "\" role=\"outer\"/>\n";
	    my $ways = $rel->{"ways"};
	    foreach my $way (@{$ways}[1..$#{$ways}])
	    {
		# L'orientation du polygone indique si on ajoute ou si on enlève
		my ($role,$ref);
		if ($way > 0) {
		    $role = "outer";
		    $ref = $way;
		}
		else {
		    $role = "inner";
		    $ref = -$way;
		}
		print {${$files{$file}}} " <member type=\"way\" ref=\"" , -1-$ref , "\" role=\"$role\"/>\n";
	    }
	    print {${$files{$file}}} "</relation>\n";
	}
    }
}

# On ferme les balises ouvertes
print_files "</osm>";
