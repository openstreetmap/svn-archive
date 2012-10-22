


use strict ;
use warnings ;

# mwInteractive.pl

my $sStyle = "mwStandardRules.txt" ;
my $tStyle = "mwTopoRules.txt" ;

my $place = "" ;
my $near = "" ;
my $dist = 50000 ;
my $lonrad = 2 ;
my $latrad = 2 ;
my $scaleset = 10000 ;
my $png = 0 ;
my $pdf = 1 ;
my $outName = "" ;
my $style = "" ;

print "Mapweaver interactive\n\n" ;

while ($place eq "") {
	print "Please enter exact place name:\n" ;
	$place = <STDIN> ;
	print "\n" ;
	chomp $place ;
}

# ---

print "Please enter exact place name of bigger city i.e. in vicinity:\n" ;
$near = <STDIN> ;
print "\n" ;
chomp $near ;

# ---

print "Please enter radius in m for vicinity search (defaults to 50.000):\n" ;
$dist = <STDIN> ;
print "\n" ;
chomp $dist ;

if ($dist eq "") { $dist = 50000 ; }

# ---

print "Please enter radius in km for latitude (defaults to 2km):\n" ;
$latrad = <STDIN> ;
print "\n" ;
chomp $latrad ;
if ($latrad eq "") { $latrad=2 ; }

# ---

print "Please enter radius in km for longitude (defaults to 2km):\n" ;
$lonrad = <STDIN> ;
print "\n" ;
chomp $lonrad ;
if ($lonrad eq "") { $lonrad=2 ; }

# ---

print "Please enter scale of map (i.e. 10000 for 1:10.000):\n" ;
$scaleset = <STDIN> ;
print "\n" ;
chomp $scaleset ;
if ($scaleset eq "") { $scaleset = 10000 ; }

# ---

print "Output map in PDF format yes/no (defaults to yes):\n" ;
$pdf = <STDIN> ;
print "\n" ;
chomp $pdf ;
if (($pdf eq "") or (lc $pdf eq "yes")) { $pdf = 1 ; }

# ---

print "Output map in PNG format yes/no (defaults to no):\n" ;
$png = <STDIN> ;
print "\n" ;
chomp $png ;
if (($png eq "") or (lc $png eq "no")) { $png = 0 ; }
if (lc $png eq "yes") { $png = 1 ; }

# ---

$outName = $place . ".svg" ;

print "Output name (defaults to $outName):\n" ;
$outName = <STDIN> ;
print "\n" ;
chomp $outName ;

if ($outName eq "") { $outName = $place . ".svg" ; }
if (! grep /\.svg$/, $outName) { $outName .= ".svg" ; }

# ---

print "Select map style from list:\n" ;
print "1 - standard rules (default)\n" ;
print "2 - topo rules\n" ;
$style = <STDIN> ;
print "\n" ;
chomp $style ;

if ($style eq "2") { $style = "mwTopoRules.txt" ; }
else { $style = "mwStandardRules.txt" ; }




my $cmd = "perl mw.pl -place=\"$place\" -overpass -style=\"$style\" -out=\"$outName\" -scaleset=$scaleset " ;
if ($near ne "") { $cmd .= "-near=\"$near\" -overpassdistance=$dist " ; }
$cmd .= " -lonrad=$lonrad -latrad=$latrad " ;
if ($png eq "1") { $cmd .= " -png " ; }
if ($pdf eq "1") { $cmd .= " -pdf " ; }

print "call mw.pl: $cmd\n" ;

`$cmd` ;



