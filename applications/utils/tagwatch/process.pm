#-----------------------------------------------------------------
# Parses an OpenStreetMap XML file looking for tags, and counting
# how often each one is used
#-----------------------------------------------------------------
# Will create an ./Output/ directory and fill it with text files
# describing the tags used in data.osm
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
use MediaWiki;

sub process{
	my ($osmfile, $outputdir) = @_;

	my $Dir = $outputdir;
	mkdir $Dir if(!-d $Dir);

	my %IgnoreTags = IgnoreTags();   # List of tag keys to ignore
	my $Tagtype = '-';               # What object the parser is in
	my %Tags;
	my %Values;
	my %Usage;

	my @tempcombi;
	my %Combinations;
	my @IgnoreValues = IgnoreValues();   # List of values to ignore
	
	my %watchedKeys;

	my $c = MediaWiki->new;
	    $c->setup({'wiki' => {
	   'host' => 'wiki.openstreetmap.org',
	   'path' => ''}});

	foreach my $Line(split(/\n/, $c->text("Tagwatch/Watchlist"))){
		if($Line =~ m{\* (\w+)}){ 
			$watchedKeys{$1}  = 1; }
		}

	open(OSMFILE, $osmfile) || die("Could not open osm file!");

	while(my $Line = <OSMFILE>){
		if($Line =~ m{<tag k=["'](.*?)["'] v=["'](.*?)["']\s*/>}){
			# Tag within an object
			my ($Name, $Value) = ($1, $2);
			if($Value ne ''){
				if(!$IgnoreTags{$Name}){      # Ignored tags
					$Tags{$Name}++;
					$Values{$Name}->{$Value}++;
					$Usage{$Name}->{$Value}->{$Tagtype}++;
					
					my $tempTagName;

					foreach my $regex(@IgnoreValues) {
						if($Name =~ m{$regex}i) {
							$tempTagName = "$Name=*";
							last;
						} else {
							$tempTagName = "$Name=$Value";
						}
					}
					push(@tempcombi,$tempTagName);
				}
			}
		}
	
		elsif($Line =~ m{<(node|segment|way) (.*)}){
			# Beginning of an object
			@tempcombi = 0;
			shift @tempcombi;
			$Tagtype = substr($1,0,1);
		}
		elsif($Line =~ m{<seg id=["'](\d+)["']\s*/>}){
			# Segment within a way
		}
		elsif($Line =~ m{</(node|segment|way)}){
			# End of an item
			$Tagtype = '-';
	
			foreach my $tc (@tempcombi) {
				foreach my $tc2 (@tempcombi) {
					if($tc ne $tc2) {
						$Combinations{$tc}->{$tc2}++; }
				}
			}
		}
	}
	
	
	foreach my $c1(keys %Combinations){
		# build combipages only for keys that are on the watchlist
		my @split = split(/=/, $c1);
		
		if($watchedKeys{$split[0]} eq 1) {
			open(COMBI, ">$Dir/combi_$c1.txt");
	
			foreach my $c2(keys(%{$Combinations{$c1}})){
				printf COMBI "%d %s\n", $Combinations{$c1}->{$c2},  $c2;
			}
		
			close COMBI; 
		}
	}
	
	open(OUT, ">$Dir/tags.txt");
	foreach my $Tag(keys %Tags){
		printf OUT "%d %s\n", $Tags{$Tag}, $Tag;
	
		open(TAG, ">$Dir/tag_$Tag.txt");
	
		foreach my $Value(keys(%{$Values{$Tag}})){
			printf TAG "%d %d %d %s\n", $Values{$Tag}->{$Value}, $Usage{$Tag}->{$Value}->{'n'}, $Usage{$Tag}->{$Value}->{'w'}, $Value;
		}
	
		close TAG; 
	}
	close OUT;
}

# Create a list of tags to ignore
# TODO: put this on a wiki page?
sub IgnoreTags{
  my %Ignore;
  foreach my $Tag(
    'lat','lon','tagtype','id',  # Reserved words (all objects)
    'created_by', # Not relevant for rendering
    'ele',        # GPS metadata
    '',           # Tags without a name
    'from',       # Reserved word (segment)
    'to',         # Reserved word (segment)
    'visible',    # OSM internal metadata
    'timestamp',  # OSM internal metadata
    'user',       # OSM internal metadata
    #'source',     # Not relevant for rendering
    'polyline',   # Reserved word (way)
    'time',       # GPS metadata?
    'editor',     # Not relevant for rendering
    'author',     # Not relevant for rendering
    'hdop',       # GPS metadata
    'pdop',       # GPS metadata
    'sat',        # GPS metadata
    'speed',      # GPS metadata
    'fix',        # GPS metadata
    'course',     # GPS metadata
    'class',      # depreciated
    'converted_by', # Some program
    ){
    $Ignore{$Tag} = 1;
  }
  return(%Ignore);
}

sub IgnoreValues{
  my @Ignore = (
    "name",
    "^ref\$",
    "^ref:",
    "_ref\$",
    "operator",
    "est_width",
    "^source",
    "^AND",
    "openGeoDB",
    "opengeodb:",
    "code_INSEE",
    "housenr_",
    "is_in",
    "icao",
    "iata",
    "comment",
    "postal_code",
    "population",
    "image",
    "wikipedia",
    "^FIXME\$",
    "^todo\$",
    "^note\$",,
    "^notes\$",
    "lanes",
    "bb:name",
    "width",
    "^gns:",
    "code_departement",
    "place_numbers",
    "house_numbers",
    "external-ID=AND",
    "description",
    "address"
    );
  return(@Ignore);
}

1;