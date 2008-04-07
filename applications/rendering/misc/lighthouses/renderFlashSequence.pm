#----------------------------------------------------------------------------
# Given a perl structure containing some lighthouse or buoy flash sequence
# (as returned by parseFlashSequence.pm), convert that into a series of
# colour changes which can be rendered, animated, etc.
#
# This program does not generate an image, it returns a string which can
# be converted into an image or animation.
#
# Format of return value:
#
# e.g. "red,1|black,2|green,1|black,10" means :
#  1 time unit of red light
#  then 2 time units of darkness
#  then 1 time units of green light
#  then 10 time units of darkness
#  then repeat
#
# time units are integer, equal to the highest-resolution feature that
# can be represented, and are currently about 0.25 seconds ("very quick")
# (todo: create a function to return this)
#----------------------------------------------------------------------------
# Copyright 2008, Oliver White
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#----------------------------------------------------------------------------

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