use strict;

sub renderIconLighthouse
{
	my $Data = shift();
	my $SVG = loadFile("sample.svg");

	my $Templates = {
		bodycolour => $Data->{'building:colour'},
		lampcolour => $Data->{'building:colour'},
		band1colour => $Data->{'building:colour'},
		band2colour => $Data->{'building:colour'},
		band3colour => $Data->{'building:colour'},
	};
	
	if(exists($Data->{'building:decoration:bands'}))
	{
		$Templates->{band1colour} = 
			$Templates->{band3colour} = $Data->{'building:decoration:bands'};
	}
	if(exists($Data->{'building:decoration:band'}))
	{
		$Templates->{band2colour} = $Data->{'building:decoration:band'};
	}
	if(exists($Data->{'building:lantern:colour'}))
	{
		$Templates->{lampcolour} = $Data->{'building:lantern:colour'};
	}
	
	$SVG = doTemplates($SVG, $Templates);
	
	#saveFile("output.svg", $SVG);
	return($SVG);
}

sub doTemplates
{
	my ($Data, $Templates) = @_;
	while(my($k,$v) = each(%{$Templates}))
	{
		$Data =~ s/{{$k}}/$v/g;
	}
	return($Data);
}
sub loadFile{
	open(IN, "<", shift()) or return '';
	my $Data = join('', <IN>);
	close IN;
	return $Data;
}
sub saveFile{
	open(OUT, ">", shift()) or return;
	print OUT shift();
	close OUT;
}
sub renderSvg
{
	my ($SVG, $PNG, $Size) = @_;
	use Cwd;
	my $Ink = "C:\\program files\\Inkscape\\inkscape.exe";
	my $Cmd = "\"$Ink\" --export-png=\"$PNG\" -D -w $Size -h $Size \"$SVG\"";
	#print $Cmd, "\n";
	`$Cmd`;
	
}
1