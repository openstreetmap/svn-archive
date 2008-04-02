
sub parseFlashSequence
{
  my $Text = shift();
  #print STDERR "$Text -->\n";
  my $Orig = $Text;
  my $Data;
  my @Parts = split(/\s+\+\s*/, $Text);
  
  foreach my $Part(@Parts)
  {
    push(@{$Data}, parseCharacter2($Part));
  }
  return($Data);
}

sub parseCharacter2
{
  my $Text = shift();
  
  my $Data;
  
  if($Text =~ s{UNLIT}{}i)
  {
    $Data->{unlit} = 1;
    return($Data);
  }
  if($Text =~ s{^(Dir)\s*}{})
  {
    $Data->{dir} = 1;
  }
  if($Text =~ s{Mo\s*\(\s*(\w+)\s*\)\s*}{}i)
  {
    $Data->{morse} = $1;
  }
  if($Text =~ s{^(L?Fl|V?Q|Iso|Occ?)}{})
  {
    my %Abbr = (
      'LFl' => 'Long flash',
      'Fl' => 'Flash',
      'Q' => 'Quick',
      'VQ' => 'Very quick',
      'Iso' => 'Isophase',
      'Occ' => 'Occulted');
    
    $Data->{abbr} = $1;
    $Data->{type} = $Abbr{$1};
  }
  if($Text =~ s{([0-9.]+)\s*s$}{}i)
  {
    $Data->{period} = $1;
  }
  if($Text =~ s{^\s*\(([0-9+]+)\s*\)}{})
  {
    @{$Data->{repeat}} = split(/\+/,$1);
  }
  if($Text =~ s{^\s*([WYRG]+)}{})
  {
    $Data->{colour_abbr} = $1;
  }
  else
  {
    $Data->{colour_abbr} = 'W';
  }
  my %Cols = (
    'W' => 'white',
    'Y' => 'yellow',
    'R' => 'red',
    'G' => 'green');
  foreach my $Letter(split(//,$Data->{colour_abbr}))
  {
    push(@{$Data->{colours}}, $Cols{$Letter});
  }
  
  $Data->{unconverted} = $Text if($Text =~ m{\S});
  
  return($Data);
}

1
