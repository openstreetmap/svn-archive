#-----------------------------------------------------------------
# Creates a sample rendering for each OpenStreetMap tag
# (uses Osmarender) 
#-----------------------------------------------------------------
# The file sample_requests.txt must have been created by
# constructHTMLStats.pm before running this program
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

sub renderExamples 
{
	my (%Config) = @_;

	my $OutputFolder = "$Config{'output_folder'}/samples";
	mkdir $OutputFolder if ! -d $OutputFolder;
	mkdir $Config{'osmarender_folder'} if ! -d $Config{'osmarender_folder'};
	
	# downloads osmarender 6 files
	getOsmarender($Config{'osmarender_folder'});

	my $SampleData = getDataSample($Config{'osmr_example_file'});

	#CreateSample("amenity","parking"); die; # testing

	# Loop through the list of image requests, rendering each one
	open(REQUESTS, "<$Config{'main_folder'}/sample_requests.txt") || die("Must have a sample_requests.txt file as input");
	while(my $Line = <REQUESTS>)
	{
		if($Line =~ m{^(\w+)\s*=\s*(.*?)\s*$})
		{
			print STDERR "\tCreating $1 = $2\n";
			CreateSample($1,$2,$OutputFolder,$Config{'osmarender_folder'},$Config{'sample_width'},$Config{'sample_height'},$SampleData);
		}
	}
	close REQUESTS;
}

#--------------------------------------------------------------------------
# Create a sample rendering of some tag=value pair
#--------------------------------------------------------------------------
sub CreateSample
{
	my ($Key, $Value, $OutputFolder, $OSMRDir,$sample_w,$sample_h,$SampleData) = @_;

	# Create an OSM file showing this data
	my $Data = $SampleData;
	$Data =~ s{\[tag\]}{$Key}g;
	$Data =~ s{\[value\]}{$Value}g;

	open(OUT, ">$OSMRDir/data.osm");
	print OUT $Data;
	close OUT;

	# Transform to SVG
	my $Cmd1 = "xsltproc $OSMRDir/osmarender.xsl $OSMRDir/map_features.xml > $OSMRDir/output.svg 2>/dev/null";
	`$Cmd1`;

	# Render to PNG
	my $Filename = sprintf("%s/%s_%s.png", $OutputFolder, $Key, $Value);
	my $SvgArea = "55:52:87:91"; # -D option doesn't seem to work!?! 
	my $Cmd2 = sprintf("inkscape --export-area=%s -w %d -h %d '--export-png=%s' %s 2>/dev/null",
		$SvgArea,
		$sample_w,$sample_h,$Filename, "$OSMRDir/output.svg");
	`$Cmd2`;
}

#--------------------------------------------------------------------------
# returns all Key groups that can be found on the Map Feature page
# Grab the latest copy of osmarender6 + styles from SVN
# under restrictions or properties.
#--------------------------------------------------------------------------
sub getOsmarender
{
	my($OSMRDir) = @_;
	mirror("http://svn.openstreetmap.org/applications/rendering/osmarender6/osmarender.xsl", "$OSMRDir/osmarender.xsl");
	mirror("http://svn.openstreetmap.org/applications/rendering/osmarender6/osm-map-features-z17.xml", "$OSMRDir/map_features.xml");
}

#--------------------------------------------------------------------------
# Get a sample OSM data file (API0.5 version)
#--------------------------------------------------------------------------
sub getDataSample
{
	my($osm_example) = @_;

	open(IN, "<$osm_example") || die;  
	my $Sample;
	while(my $Line = <IN>)
	{
		$Sample .= $Line;
	}
	close IN;
	return $Sample;
}

1;