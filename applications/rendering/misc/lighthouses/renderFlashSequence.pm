
sub renderFlashSequence
{
  my $Data = shift();
  my $Text = '';
  
  foreach my $Part(@{$Data})
  {
    $Text .= recreateLightPart($Part);
  }
  return($Text);
}

sub recreateLightPart
{
  my $Data = shift();
  my $Text = '';
  
  return(formatFlash(10,"black")) if($Data->{unlit});
  
  my $Len = LengthOfFlashType($Data->{abbr});
  my $Period = $Data->{period};
  
  my @Reps;
  if( exists($Data->{repeat}))
  {
    @Reps = @{$Data->{repeat}};
    $Debug = "normal repeat";
  }
  elsif($Data->{abbr} eq 'Iso')
  {
    @Reps = (1);
    $Len = $Period / (2.0 * scalar(@{$Data->{colours}}));
    $Debug = "Iso";
  }
  elsif($Data->{abbr} eq 'Oc')
  {
    $Text .= formatFlash(1, "black");
    $Text .= formatFlash($Period - 1, $Data->{colours}[0]);
    return($Text);
  }
  elsif(defined $Data->{morse})
  {
    # Morse code is special case, gets handled separately
    my $Code = morse($Data->{morse});
    my $Time = 0;
    foreach my $DotDash(split(//,$Code))
    {
      my $Pulse = $DotDash eq '-' ? 1.5 : 0.5;
      $Text .= formatFlash($Pulse, $Data->{colours}[0]);
      $Text .= formatFlash(0.5, "black");
      $Time += $Pulse + 0.5;
    }
    if(defined($Period) and ($Time < $Period))
      {
      $Text .= formatFlash($Period - $Time, "black");
      }
    return($Text);
  }
  elsif(! defined $Len)
  {
    return(formatFlash(5,"blue"));
  }
  elsif(defined $Period and defined $Len)
  {
    #$Debug = "Reps from Period";
    #@Reps = ($Period / (2.0 * $Len));
    @Reps = (1);
  }
  else
  {
    $Debug = "Len but not period";
    @Reps = (4);
    $Period = scalar(@Reps) * 2.0 * $Len;
  }
  
  my @Colours = @{$Data->{colours}};
  
  my $Time = 0;
  my $MultiReps = scalar(@Reps) > 1;
  foreach my $Num(@Reps)
  {
    foreach(1..int($Num))
    {
      foreach my $Colour(@Colours)
        {
        $Text .= formatFlash($Len, $Colour);
        $Text .= formatFlash($Len, "black");
        $Time += 2 * $Len;
        }
    }
    if($MultiReps)
    {
        $Text .= formatFlash($Len, "black");
        $Time += $Len;
    }
  }
  if($Time < $Period)
    {
    $Text .= formatFlash($Period - $Time, "black");
    }
    
  return($Text);
}

sub formatFlash
{
  my ($Len, $Colour) = @_;
  
  return sprintf("%s,%s|", $Colour, int($Len * 4));
}

sub LengthOfFlashType
{
  my %Options = (
      'LFl' => 2,
      'Fl' => 1,
      'Q' => 0.5,
      'VQ' => 0.25,
      'Iso' => 5,
      'Occ' => 1
      );
  return($Options{shift()});
}

sub morse
{
  my %Morse = (
  'A'=>'.-',      'G'=>'',      'L'=>'',      'Q'=>'',      'V'=>'',      
  'B'=>'-...',      'H'=>'',      'M'=>'',      'R'=>'',      'W'=>'',      
  'C'=>'-.-.',      'I'=>'',      'N'=>'',      'S'=>'',      'X'=>'',      
  'D'=>'-..',      'J'=>'',      'O'=>'',      'T'=>'',      'Y'=>'',      
  'E'=>'.',      'K'=>'',      'P'=>'',      'U'=>'',      'Z'=>'',      
  'F'=>''); # TODO
  return($Morse{shift()});
}

1