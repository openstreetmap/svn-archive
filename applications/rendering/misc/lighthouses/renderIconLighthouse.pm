use strict;
#----------------------------------------------------------------------------
# Create an SVG icon which aims to represent a lighthouse node in
# OpenStreetMap, with realistic building colours where known.
#
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

#---------------------------------------------------------------
# Convert an openstreetmap lighthouse node to a SVG icon
# 
# Parameters: pointer to hash of OSM tags from the node
#
# Returns: SVG (a complete file, not a snippet)
#---------------------------------------------------------------
sub renderIconLighthouse
{
  my $Data = shift();

  # By default, a lighthouse is white
  if(!exists($Data->{'building:colour'}))
  {
    $Data->{'building:colour'} = 'white';
  }

  # list the templates we have in the SVG file
  # and default them all to the building colour
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
  
  # Apply those templates to the SVG file
  my $SVG = loadFile("sample.svg");
  $SVG = doTemplates($SVG, $Templates);
  return($SVG);
}

#---------------------------------------------------------------
# Very basic templating code, to allow the SVG template to be
# modified.  Replace this with a real template library (preferably
# an SVG-compatible one, i.e can be edited in inkscape) for greater
# use
# 
# Parameters: 
#  * Template text
#  * Hash of template name/value pairs
#---------------------------------------------------------------
sub doTemplates
{
  my ($Data, $Templates) = @_;
  while(my($k,$v) = each(%{$Templates}))
  {
    $Data =~ s/{{$k}}/$v/g;
  }
  return($Data);
}

#---------------------------------------------------------------
# Load a file from disk
#---------------------------------------------------------------
sub loadFile{
  open(IN, "<", shift()) or return '';
  my $Data = join('', <IN>);
  close IN;
  return $Data;
}

#---------------------------------------------------------------
# Save a file to disk
#---------------------------------------------------------------
sub saveFile{
  open(OUT, ">", shift()) or return;
  print OUT shift();
  close OUT;
}

#---------------------------------------------------------------
# For testing, render an SVG file to a PNG
# 
# Parameters: SVG input filename, PNG output filename, pixel width
#---------------------------------------------------------------
sub renderSvg
{
  my ($SVG, $PNG, $Size) = @_;
  use Cwd;
  my $Ink = "inkscape";
  my $Cmd = "\"$Ink\" --export-png=\"$PNG\" -D -w $Size -h $Size \"$SVG\"";
  #print $Cmd, "\n";
  `$Cmd`;	
}
1