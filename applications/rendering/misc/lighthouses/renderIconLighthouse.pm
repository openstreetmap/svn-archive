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
	
	if(exists($Data->{'building:decoration:multiple_bands'}))
	{
		$Templates->{band1colour} = 
			$Templates->{band3colour} = $Data->{'building:decoration:multiple_bands'};
	}
	if(exists($Data->{'building:decoration:single_band'}))
	{
		$Templates->{band2colour} = $Data->{'building:decoration:single_band'};
	}
	if(exists($Data->{'lighting:colour'}))
	{
		$Templates->{lampcolour} = $Data->{'lighting:colour'};
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
	my ($SVG, $PNG) = @_;
	use Cwd;
	my $Ink = "C:\\program files\\Inkscape\\inkscape.exe";
	my $Cmd = "\"$Ink\" --export-png=\"$PNG\" -D -w 400 -h 400 \"$SVG\"";
	#print $Cmd, "\n";
	`$Cmd`;
	
}
1