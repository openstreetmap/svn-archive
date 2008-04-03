use strict;
use Data::Dumper;
use parseFlashSequence;
use renderFlashSequence;
use flashSequenceToImage;
use renderIconLighthouse;
use XML::Simple;

my $OSM = XMLin("lighthouses.osm");

my $OutDir = "html";
mkdir $OutDir if ! -d $OutDir;

# open OUT, ">dumper.txt"; print OUT Dumper($OSM); close OUT;

open HTML, ">$OutDir/index.html";

my $Cwd = getcwd;
while(my($id, $fields) = each(%{$OSM->{node}}))
{
	my %Tags;
	foreach my $Tag(@{$fields->{tag}})
	{
		$Tags{$Tag->{k}} = $Tag->{v};
	}

	printf HTML "<h3>%s</h3>\n", $Tags{name};

	if($Tags{man_made} eq 'lighthouse')
	{	
	my $SVG = renderIconLighthouse(\%Tags);
	open(OUT, ">temp.svg"); print OUT $SVG;close OUT;
	#open(OUT, ">$OutDir/$Tags{name}.txt"); print OUT Dumper(%Tags);close OUT;
	
	my $BuildingFilename = "building_$Tags{name}.png";
	renderSvg("$Cwd/temp.svg", "$Cwd/$OutDir/$BuildingFilename", 200);
	unlink "temp.svg";
	
	my $LightFilename = "lighting_$Tags{name}.png";
	my $Sequence = parseFlashSequence($Tags{"lighting:sequence"});
	my $SequenceText = renderFlashSequence($Sequence);
	flashSequenceToImage($SequenceText, "$OutDir/$LightFilename");

	printf HTML "<p><img src='$BuildingFilename'></p>";
	printf HTML "<p>Description: $Tags{description}</p>\n";
	printf HTML "<p><img src='$LightFilename'></p>";
	printf HTML "<p>Sequence: %s</p>\n", $Tags{"lighting:sequence"};
	
	}
}

close HTML;



