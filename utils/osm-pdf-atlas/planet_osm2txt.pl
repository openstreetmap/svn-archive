#!/usr/bin/perl

use XML::Parser;
use Getopt::Long;
use strict;
use warnings;
use Storable ();
use IO::File;

my $debug=0;

Getopt::Long::Configure('no_ignore_case');
GetOptions ( 
	     'debug'               => \$debug,      
	     'd'                   => \$debug,      
	     );

my $Filename = shift();

# Temporary data
my (%MainAttr,$Type,%Tags, @WaySegments);
# Stats
my %AllTags;
# Stored data
my (@Nodes, @Segments, @Ways, %Stats);


# -----------------------------------------------------------------------------
# Open Data File
sub data_open($){
    my $file_name = shift;

    if ( -s $file_name ) {
	print STDERR "Opening $file_name" 
	    if $debug;
	my $fh;
	if ( $file_name =~ m/\.gz$/ ) {
	    $fh = IO::File->new("gzip -dc $file_name|")
		or die("cannot open $file_name: $!");
	} elsif ( $file_name =~ m/\.bz2$/ ) {
	    $fh = IO::File->new("bzip2 -dc $file_name|")
		or die("cannot open $file_name: $!");
	} else {
	    $fh = IO::File->new("<$file_name")
		or die("cannot open $file_name: $!");
	}
	return $fh;
    }
    die "cannot Find $file_name";
}

#----------------------------------------------
# Processing stage
#----------------------------------------------

#----------------------------------------------
# Parsing planet.osm File
#----------------------------------------------
print STDERR "Reading and Parsing XML from $Filename\n" if $debug;
my $start_time=time();
my $P = new XML::Parser(Handlers => {Start => \&DoStart, End => \&DoEnd, Char => \&DoChar});
my $fh = data_open($Filename);
eval {
    $P->parse($fh);
};

if ($@) {
    print STDERR "WARNING: Could not parse osm data $Filename\n";
    print STDERR "ERROR: $@\n";
    return;
}
if (not $P) {
    print STDERR "WARNING: Could not parse osm data $Filename\n";
    return;
}
#$P->parsefile($Filename);
printf("Parsing Osm-Data in %.0f sec\n",time()-$start_time )
    if $debug;


printf STDERR "Creating output files\n";

#----------------------------------------------
# Combine way data into segments
#----------------------------------------------
print STDERR "Combine way data into segments\n" if $debug;
if(open(WAYS,">Data/ways.txt")){
    foreach my $Way (@Ways){
	next unless defined $Way;
	my $segments=$Way->{"segments"};
	my @SubSegments = split(/,/, $segments);
	unless ( scalar(@SubSegments) ) {
	    $Stats{"empty ways"}++; 
	    if ( $debug ) {
		printf WAYS "No Segments for Way: Name:%s\n",($Way->{"name"}||'');
	    }
	    next;
	}
	if ( $debug ) {
	    printf WAYS "Way: %s,%s\n", $Way->{"segments"}, ($Way->{"name"}||'');
	}
	$Stats{"untagged ways"}++ 
	    unless scalar( keys (%$Way)); 
	
	if ( $debug) {
	    printf WAYS "Copying keys: %s to segments %s\n",
	    join(",",keys(%$Way)),
	    join(",",@SubSegments);
	}
	
	# Each segment in a way inherits the way's attributes
	foreach my $Segment(@SubSegments){
	    foreach my $Key(keys(%$Way)){
		$Segments[$Segment]{$Key} = $Way->{$Key}
	    }
	}
    }
    close WAYS;
}

#----------------------------------------------
# Main output (segments)
#----------------------------------------------
print STDERR "Writing Segments to Data/osm.txt\n" if $debug;
if(open(OSM, ">Data/osm.txt")){
  foreach my $Segment(@Segments){
      next unless defined $Segment;
      my $From = $Segment->{"from"};
      my $To = $Segment->{"to"};
      unless ( $From && $To ) {
	  $Stats{"segments without endpoints"}++;
	  next;
      }
      unless ( defined($Nodes[$From]) && defined($Nodes[$To]) ) {
	  $Stats{"segments without endpoint nodes defined"}++;
	  next;
      }
      printf OSM "%f,%f,%f,%f,%s,%s,%s\n",
      $Nodes[$From]{"lat"},
      $Nodes[$From]{"lon"},
      $Nodes[$To]{"lat"},
      $Nodes[$To]{"lon"},
      ($Segment->{"class"}||''),
      ($Segment->{"name"}||''),
      ($Segment->{"highway"}||'');
  }
  close OSM;
}

#----------------------------------------------
# Secondary output (named points)
#----------------------------------------------
print STDERR "Writing Points to Data/points.txt\n" if $debug;
if(open(POINTS, ">Data/points.txt")){
  foreach my $Node(@Nodes){
      next unless defined $Node;
    $Stats{"Nodes with zero lat/long"}++ 
	if($Node->{"lat"} == 0 and $Node->{"lon"} == 0);
    
    if($Node->{"name"} || $Node->{"amenity"} || $Node->{"class"}){
	printf POINTS "%f,%f,%s,%s,%s\n",
        $Node->{"lat"},
        $Node->{"lon"},
	($Node->{"name"}||''),
	($Node->{"amenity"}||''),
        ($Node->{"class"}||'');
      }
  }
  close POINTS;
}

#----------------------------------------------
# Statistics output
#----------------------------------------------
print STDERR "Statistics output\n" if $debug;
if(open(STATS, ">Data/stats.txt")){
  foreach(sort {$AllTags{$b} <=> $AllTags{$a}} keys(%AllTags)){
    printf STATS "* %d %s\n", $AllTags{$_}, $_;
  }
  printf STATS "\n\nStats:\n";
  foreach(keys(%Stats)){
    printf STATS "* %d %s\n", $Stats{$_}, $_;
  }
}
printf STDERR "Done\n";
exit;

# Function is called whenever an XML tag is started
#----------------------------------------------
sub DoStart()
{
  my ($Expat, $Name, %Attr) = @_;
  
  if($Name eq "node"){
    undef %Tags;
    %MainAttr = %Attr;
    $Type = "n";
  }
  if($Name eq "segment"){
    undef %Tags;
    %MainAttr = %Attr;
    $Type = "s";
  }
  if($Name eq "way"){
    undef %Tags;
    undef @WaySegments;
    %MainAttr = %Attr;
    $Type = "w";
  }
  if($Name eq "tag"){
    # TODO: protect against id,from,to,lat,long,etc. being used as tags
    $Tags{$Attr{"k"}} = $Attr{"v"};
    $AllTags{$Attr{"k"}}++;
    $Stats{"tags"}++;
  }
  if($Name eq "seg"){
    push(@WaySegments, $Attr{"id"});
  }
}

# Function is called whenever an XML tag is ended
#----------------------------------------------
sub DoEnd(){
  my ($Expat, $Element) = @_;
  if($Element eq "node"){
    my $ID = $MainAttr{"id"};
    $Nodes[$ID]{"lat"} = $MainAttr{"lat"};
    $Nodes[$ID]{"lon"} = $MainAttr{"lon"};
    foreach(keys(%Tags)){
      $Nodes[$ID]{$_} = $Tags{$_};
    }
    $Stats{"named nodes"}++ if($Nodes[$ID]{"name"});
    $Stats{"tagged nodes"}++ if($MainAttr{"tags"});
    $Stats{"nodes"}++;
    #print "Node:".join(",",keys(%Tags))."\n" if(scalar(keys(%Tags))>0);
  }
  if($Element eq "segment"){
    my $ID = $MainAttr{"id"};
    $Segments[$ID]{"from"} = $MainAttr{"from"};
    $Segments[$ID]{"to"} = $MainAttr{"to"};
    foreach(keys(%Tags)){
      $Segments[$ID]{$_} = $Tags{$_};
    }
    $Stats{"tagged segments"}++ if($MainAttr{"tags"});
    $Stats{"segments"}++;
  }
  if($Element eq "way"){
    my $ID = $MainAttr{"id"};
    $Ways[$ID]{"segments"} = join(",",@WaySegments);
    foreach(keys(%Tags)){
      $Ways[$ID]{$_} = $Tags{$_};
    }    
    $Stats{"Ways"}++;
  }
}

# Function is called whenever text is encountered in the XML file
#----------------------------------------------
sub DoChar(){
  my ($Expat, $String) = @_;
}
