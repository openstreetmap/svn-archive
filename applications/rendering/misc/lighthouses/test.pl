use strict;
use Data::Dumper;
use parseFlashSequence;
use renderFlashSequence;
use flashSequenceToImage;
use flashSequenceToGifAnim;
use renderIconLighthouse;
use XML::Simple;

my $OSM = XMLin("lighthouses.osm");

my $OutDir = "html";
mkdir $OutDir if ! -d $OutDir;

# open OUT, ">dumper.txt"; print OUT Dumper($OSM); close OUT;

open HTML, ">$OutDir/index.html";
print HTML "<html><head><title>Irish lighthouses</title></head><body style='background-color:white'><h1>Irish lighthouses in OSM</h1><p><i>Data is CC-BY-SA 2.0</i></p>\n";

my $Cwd = getcwd;
while(my($id, $fields) = each(%{$OSM->{node}}))
{
	my %Tags;
	foreach my $Tag(@{$fields->{tag}})
	{
		$Tags{$Tag->{k}} = $Tag->{v};
	}


	if($Tags{man_made} eq 'lighthouse')
	{	
	my $SVG = renderIconLighthouse(\%Tags);
	open(OUT, ">temp.svg"); print OUT $SVG;close OUT;
	#open(OUT, ">$OutDir/$Tags{name}.txt"); print OUT Dumper(%Tags);close OUT;
	
	printf HTML "<h3>%s</h3>\n", $Tags{name};
	
	my $Name = $Tags{name};
	$Name =~ s{\W}{_}g;
	
	my $BuildingFilename = "building_$Name.png";
	renderSvg("$Cwd/temp.svg", "$Cwd/$OutDir/$BuildingFilename", 120);
	unlink "temp.svg";
	
	my $LightFilename = "lighting_$Name.png";
	my $Sequence = parseFlashSequence($Tags{"lighting:sequence"});
	my $SequenceText = renderFlashSequence($Sequence);
	flashSequenceToImage($SequenceText, "$OutDir/$LightFilename");

        my $GifFilename = "anim_$Name.gif";

        flashSequenceToGifAnim($SequenceText, "$OutDir/$GifFilename");

	printf HTML "<p><img src='$BuildingFilename'></p>";
	printf HTML "<p>Description: $Tags{description}</p>\n";
	printf HTML "<p>Sequence: %s</p>\n", $Tags{"lighting:sequence"};
	
	printf HTML "<p><a href='$GifFilename'>Animation</a></p>";
	printf HTML "<p><img src='$LightFilename'></p>";
	
	}
}

close HTML;



