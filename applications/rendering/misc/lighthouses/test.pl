%Tags = (
	"building:colour" => 'black',
	"building:bands" => 'yellow',
	"building:top:colour" => 'green',
	"building" => 'tower',
	"building:shape" => 'round',
	"lighting:sequence" => "Fl WR 3s"
        );


use Data::Dumper;
use parseFlashSequence;
use renderFlashSequence;
use flashSequenceToImage;

$Data = parseFlashSequence($Tags{"lighting:sequence"});
#print Dumper($Data);

$Text = renderFlashSequence($Data);

#print "$Text\n\n";
flashSequenceToImage($Text, "lights.png");

use renderIconLighthouse;

#$SVG = renderIconLighthouse(\%Tags);
#print $SVG;

use XML::Simple;
$OSM = XMLin("lighthouses.osm");

open OUT, ">dumper.txt"; print OUT Dumper($OSM); close OUT;
open HTML, ">html/index.html";
print HTML "<table border=1>\n";
my $Dir = getcwd;
while(my($id, $fields) = each(%{$OSM->{node}}))
{
	my %Tags;
	foreach my $Tag(@{$fields->{tag}})
	{
		$Tags{$Tag->{k}} = $Tag->{v};
	}

	printf "%s\n", $Tags{name};

	if($Tags{man_made} eq 'lighthouse')
	{	
	$SVG = renderIconLighthouse(\%Tags);
	open(OUT, ">temp.svg"); print OUT $SVG;close OUT;
	open(OUT, ">html/$Tags{name}.txt"); print OUT Dumper(%Tags);close OUT;
	
	renderSvg("$Dir/temp.svg", "$Dir/html/$Tags{name}.png");
	
	printf HTML "<tr><td><img src='$Tags{name}.png'></td><td>$Tags{name}</td><td>$Tags{description}</td></tr>\n";
	}
}
close HTML;
