#-----------------------------------------------------------------
# Creates a sample rendering for each OpenStreetMap tag
# (uses Osmarender) 
#-----------------------------------------------------------------
# Usage: perl makeSamples.pl
#
# The file sample_requests.txt must have been created by construct.pl
# before running this program
#-----------------------------------------------------------------
# This file is part of Tagwatch
# Tagwatch is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# Tagwatch is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with Tagwatch.  If not, see <http://www.gnu.org/licenses/>.
#---------------------------------------------------------------
use strict;
use LWP::Simple;
my $DataDir = "Output";
my $TempDir = "render";
my $Images = "html/Samples";

my($W,$H) = (150,150);
mkdir $TempDir if ! -d $TempDir;
mkdir $Images if ! -d $Images;
getOsmarender();
my $SampleData = getDataSample();

#CreateSample("amenity","parking"); die; # testing

# Loop through the list of image requests, rendering each one
open(REQUESTS, "<sample_requests.txt") || die("Must have a sample_requests.txt file as input");
while(my $Line = <REQUESTS>){
  if($Line =~ m{^(\w+)\s*=\s*(.*?)\s*$}){
    print STDERR "Creating $1 = $2\n";
    CreateSample($1,$2);
  }
}
close REQUESTS;

# Create a sample rendering of some tag=value pair
sub CreateSample{
  my ($Key, $Value) = @_;

  # Create an OSM file showing this data
  my $Data = $SampleData;
  $Data =~ s{\[tag\]}{$Key}g;
  $Data =~ s{\[value\]}{$Value}g;

  open(OUT, ">$TempDir/data.osm");
  print OUT $Data;
  close OUT;

  # Transform to SVG
  my $Cmd1 = "xsltproc $TempDir/osmarender.xsl $TempDir/map_features.xml > $TempDir/output.svg 2>/dev/null";
  `$Cmd1`;

  # Render to PNG
  my $Filename = sprintf("%s/%s_%s.png", $Images, $Key, $Value);
  my $SvgArea = "60:57:82:86"; # -D option doesn't seem to work!?! 
  my $Cmd2 = sprintf("inkscape --export-area=%s -w %d -h %d --export-png=%s %s 2>/dev/null",
    $SvgArea,
    $W,$H,$Filename, "$TempDir/output.svg");
  `$Cmd2`;
}

sub getOsmarender{
  # Grab the latest copy of osmarender5 + styles from SVN
  mirror("http://svn.openstreetmap.org/applications/rendering/osmarender5/osmarender.xsl", "$TempDir/osmarender.xsl");
  mirror("http://svn.openstreetmap.org/applications/rendering/osmarender5/osm-map-features-z17.xml", "$TempDir/map_features.xml");
}

sub getDataSample{
  # Get a sample OSM data file (choose API0.4 or API0.5 versions
  # depending on whether osmarender supports segments)
  open(IN, "<data_sample_0_4.osm") || die;  
  my $Sample;
  while(my $Line = <IN>){
    $Sample .= $Line;
  }
  close IN;
  return $Sample;
}

