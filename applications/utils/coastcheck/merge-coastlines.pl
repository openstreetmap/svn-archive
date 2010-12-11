#!/usr/bin/perl -w

use strict;
use warnings;

use Tree::R;
use Bit::Vector;
use Pod::Usage;

use constant EPSILON => 0.001;
use constant TRACE => 0;

# Grab the filename
my $xml = shift||'';
pod2usage(1) unless $xml;

# Check we can load the file
if($xml eq "-") {
	die("Sorry, reading from stdin is not supported, as we have to make several passes\n");
}
unless( -f $xml) {
	die("Osm file '$xml' could not be found\n");
}

unless( -s $xml ) {
    die " $xml has 0 size\n";
}

# Sub to open xml
sub openXML {
        if( $xml =~ /\.bz2$/ )
        {
          open(XML, "bzcat $xml |") or die($!);
        }
        elsif( $xml =~ /\.gz$/ )
        {
          open(XML, "zcat $xml |") or die($!);
        }
        else
        {
	  open(XML, "<$xml") or die("$!");
        }
	#open(XML, "<:utf8","$xml") or die("$!");
}
# Sub to close xml
sub closeXML {
	close XML;
}

sub processXML {
	my ($nodeH, $wayH, $relH) = @_;
	openXML();
#	$pass++;

	# Process the file, giving tags to the helpers that like them

	# Hold the main line, tags and segs of the tag
	my $main_line;
	my $main_type;
	my $wanted;
	my @tags;
	my @nodes;
	my @rel_ways;
	my @rel_nodes;

	my $startNewTag = sub{
		$wanted = 0;
		@tags = ();
		@nodes = ();
		@rel_ways = ();
		@rel_nodes = ();
	};

	while(my $line = <XML>) {
		if($line =~ /^\s*<node/) {
			$main_line = $line;
			$main_type = "node";
			&$startNewTag();
			unless($line =~ /\/>\s*$/) { next; }
		}
		elsif($line =~ /^\s*\<way/) {
			$main_line = $line;
			$main_type = "way";
			&$startNewTag();
			unless($line =~ /\/>\s*$/) { next; }
		}
		elsif($line =~ /^\s*<relation/) {
			$main_line = $line;
			$main_type = "relation";
			&$startNewTag();
			unless($line =~ /\/>\s*$/) { next; }
		}

		if($line =~ /^\s*\<tag/) {
			my ($name,$value) = ($line =~ /^\s*\<tag k=[\'\"](.*?)[\'\"] v=[\'\"](.*?)[\'\"]/);
			unless($name) { 
				unless($line =~ /k="\s*" v="\s*"/) {
					warn "Invalid line '$line'"; 
				}
				next; 
			}
			my @tag = ($name,$value);
			push @tags, \@tag;
		}
		elsif($line =~ /^\s*\<nd /) {
			my ($ref) = ($line =~ /^\s*\<nd ref=[\'\"](\d+)[\'\"]/);
			unless($main_type eq "way") { warn "Got nd when in $main_type\n"; next; }
			unless($ref) { warn "Invalid line '$line'"; next; }
			push @nodes, $ref;
		}
		elsif($line =~ /^\s*\<member /) {
			my ($type,$ref,$role) = ($line =~ /^\s*\<member type=[\'\"](.*?)[\'\"] ref=[\'\"](\d+)[\'\"] role=[\'\"](.*)[\'\"]/);
			unless($main_type eq "relation") { warn "Got member when in $main_type\n"; next; }
			unless($type && $ref) { warn "Invalid line '$line'"; next; }

			my %m;
			$m{'type'} = $type;
			$m{'ref'} = $ref;
			$m{'role'} = $role;
			if($type eq "node") {
				push @rel_nodes, \%m;
			} elsif($type eq "way") {
				push @rel_ways, \%m;
			} else {
				warn("Got unknown member type '$type' in '$line'"); next;
			}
		}

		# Do the decisions when closing tags - can be self closing
		elsif($line =~ /^\s*<\/?node/) {
			my ($id,$lat,$long) = ($main_line =~ /^\s*<node id=['"](\d+)['"].* lat=['"]?(\-?[\d\.]+)['"]? lon=['"]?(\-?[\d\.]+e?\-?\d*)['"]?/);

			unless($id) { warn "Invalid node line '$main_line'"; next; }
			unless($main_type eq "node") { warn "$main_type ended with $line"; next; }
			if($nodeH) {
				&$nodeH($id,$lat,$long,\@tags,$main_line,$line);
			}
		}
		elsif($line =~ /^\s*\<\/?way/) {
			my ($id) = ($main_line =~ /^\s*\<way id=[\'\"](\d+)[\'\"]/);

			unless($id) { warn "Invalid way line '$main_line'"; next; }
			unless($main_type eq "way") { warn "$main_type ended with $line"; next; }
			if($wayH) {
				&$wayH($id,\@tags,\@nodes,$main_line,$line);
			}
		}
		elsif($line =~ /^\s*<\/?relation/) {
			my ($id) = ($main_line =~ /^\s*\<relation id=[\'\"](\d+)[\'\"]/);

			unless($id) { warn "Invalid relation line '$main_line'"; next; }
			unless($main_type eq "relation") { warn "$main_type ended with $line"; next; }
			if($relH) {
				&$relH($id,\@tags,\@rel_nodes,\@rel_ways,$main_line,$line);
			}
		}
		elsif($line =~ /^\s*\<\?xml/) {
#			if($pass == 1) {
#				print $line;
#			}
		}
		elsif($line =~ /^\s*\<osm /) {
#			if($pass == 1) {
#				print $line;
#			}
		}
		elsif($line =~ /^\s*\<\/osm\>/ ) {
#			if($pass == 3) {
#				print $line;
#			}
		}
		else {
			print STDERR "Unknown line $line\n";
                        exit 1;
		};
	}

	# All done
	closeXML();
}

sub MarkPoint
{
  my $x = shift;
  my $y = shift;
  my $type = shift;
  print "P$type $x $y\n";
}

my $wanted_nodes = Bit::Vector->new( 2000 * 1000 * 1000 );
my(%nodes,%ways);
my $totalways = 0;
my $closed = 0;
my $zero_length = 0;

sub nodeProcessor
{
  my ($id,$lat,$long,$tagsRef,$main_line,$line) = @_;
  if($wanted_nodes->contains($id)) {
    $nodes{$id} = [$lat,$long];
  }
}

sub wayProcessor
{
  my ($id,$tagsRef,$nodesRef,$main_line,$line) = @_;
#  return unless scalar(grep{defined $tags{natural} and $tags{natural} eq "coastline";
  if( scalar(@$nodesRef) <= 1)
  { $zero_length++; return }
  if( $nodesRef->[0] == $nodesRef->[-1] )
  {
    $closed++;
    print "C1 $id\n";
  }
  else
  {
    $wanted_nodes->Bit_On( $nodesRef->[0] );
    $wanted_nodes->Bit_On( $nodesRef->[-1] );
    $ways{$id} = [$nodesRef->[0],$nodesRef->[-1]];
    $totalways++;
    if( $id == TRACE )
    { print STDERR "Found way $id [$nodesRef->[0],$nodesRef->[-1]]\n" }
  }
}

print STDERR "Pass 1: Collecting ways\n";
# This assumes the ways come first, which may not always be the case
processXML( \&nodeProcessor, \&wayProcessor, undef );
print STDERR "$totalways collected, $closed closed, $zero_length zero-length\n";
my $pass = 2;
my $epsilon = EPSILON;
my $ways_remain = $totalways;
my $completed = 0;
my $ways_output = 0;

open TEMP, ">to-merge.txt" or die;

for my $pass (2..4)
{
  print STDERR "Pass ${pass}: Starting with ",scalar(keys %ways)," ways (epsilon=$epsilon)\n";
  print STDERR "Pass ${pass}a: Adding to R-Tree\n";

  my $tree = new Tree::R();
  # Add all begin points to the tree
  for my $way (keys %ways)
  {
    if( not defined $nodes{$ways{$way}[0]} )
    {
      print STDERR "Missing node: $ways{$way}[0](way=$way)\n";
      delete $ways{$way};
      $ways_remain--;
      next;
    }
    if( not defined $nodes{$ways{$way}[1]} )
    {
      print STDERR "Missing node: $ways{$way}[1](way=$way)\n";
      delete $ways{$way};
      $ways_remain--;
      next;
    }
    my @coords = @{$nodes{$ways{$way}[0]}};
    $tree->insert( $way, @coords, @coords );
    if( $way == TRACE )
    { print STDERR "Inserted into r-tree: id $way ($coords[0],$coords[1])\n" }
  }

  my $ways_used = new Bit::Vector( 300_000_000 );
  print STDERR "Pass ${pass}b: Joining ways\n";
  WAY: for my $way (keys %ways)
  {
    if( $way == TRACE )
    { print STDERR "Joining $way used=", $ways_used->contains($way), "\n" }
    next if $ways_used->contains($way);
    for(;;)
    {
      my @coords = @{$nodes{$ways{$way}[1]}};
      my @nearby;
      $tree->query_completely_within_rect( $coords[0] - $epsilon, $coords[1] - $epsilon, $coords[0] + $epsilon, $coords[1] + $epsilon, \@nearby );

      # This is a list of way_ids that start near the end of this way
      my $match = undef;
      my $mindist = 999;
  #    print "Searching near [$coords[0],$coords[1]]\n";
      for my $w (@nearby)
      {
  #      print "Way $way: testing '$w'\n";
        if( $w == TRACE or $way == TRACE )
        { print STDERR "Found way $w used=",$ways_used->contains($w)," way=$way\n" }
        next if $ways_used->contains($w);
        if( $ways{$w}[0] == $ways{$way}[1] )
        { $match = $w; $mindist = 0; last }
        my @coords2 = @{$nodes{$ways{$w}[0]}};
        my $dist = ($coords[0]-$coords2[0])**2 + ($coords[1]-$coords2[1])**2;
        if( $dist < $mindist )
        {
          $mindist = $dist;
          $match = $w;
        }
      }
      # If we loop around to ourselves and we link to no other nodes we bail. We want unclosed single ways to display
      next WAY unless $mindist < 3 * $epsilon * $epsilon;
      next WAY unless defined $match;
      next WAY if $match == $way and scalar(@{$ways{$way}}) == 2 and $ways{$way}[0] != $ways{$way}[1];
      if( $ways{$match}[0] != $ways{$way}[1]  and $mindist < 1e-8 )
      {
        print TEMP "$ways{$way}[1] $ways{$match}[0]\n";
      }
      if( $mindist > 0 )
      {
        MarkPoint( @coords, 2 );
      }
      if( $mindist > 0.05 )
      {
        MarkPoint( @{$nodes{$ways{$mindist}[0]}}, 2 );
      }
      if( $match == $way )
      {
        if( $match == TRACE )
        { print STDERR "Closed way $way" }
        OutputWay( $way, 1 );
        $completed++;
        $ways_remain--;
        $ways_used->Bit_On($way);
        last;
      }
      if( $way == TRACE or $match == TRACE )
      { print STDERR "Appending to $way: $match, now $ways{$way}[0] -> $ways{$match}[1]\n" }
      $ways_used->Bit_On($match);
      $ways_remain--;
      push @{ $ways{$way} }, $match;
      $ways{$way}[1] = $ways{$match}[1];
    }
  }

  print STDERR "Remain: $ways_remain / $totalways (complete $completed)\n";
  print STDERR "Pass ${pass}c: Consolidate remaining\n";
  my %new_ways;
  my $consolidated = 0;
  for my $way (keys %ways)
  {
    if( $way == TRACE )
    { print STDERR "Consolidating $way: ",$ways_used->contains($way),"\n" }
    next if $ways_used->contains($way);
    my @list = Consolidate( \%ways, $way );
    splice @list, 0, 1, $ways{$way}[0], $ways{$way}[1];
    $new_ways{$way} = \@list;
    
  #  print "Consolidated $way: ($list[0] -> $list[1]) $way ",join(" ",@list[2..$#list]),"\n";
    $consolidated++;
  }
  print STDERR "Consolidated: $consolidated\n";
  %ways = %new_ways;
  $epsilon *= 10;
}
print STDERR "Dumping remaining\n";
for my $way (keys %ways)
{
  MarkPoint( @{$nodes{$ways{$way}[0]}}, 1 );
  MarkPoint( @{$nodes{$ways{$way}[1]}}, 1 );
  OutputWay($way, 0);
}
print STDERR "Total ways output: $ways_output\n";
exit 0;

sub Consolidate
{
  my $ways = shift;
  my $way = shift;

  if( $way == TRACE )
  { print STDERR "Consolidate($way)\n" }
  my @res = ($way);
  if( not defined $ways{$way} )
  {
    if( $way == TRACE )
    { print STDERR "Skippping\n" }
    # This can happen after the first pass, then we don't remember the full details of each way anymore
    return @res;
  }
  my @tmp = @{ $ways{$way} };
  if( $way == TRACE )
  { print STDERR "Subways: ", join(" ", @tmp[2..$#tmp]), "\n" }
  for my $i (2..$#tmp)
  {
    push @res, Consolidate($ways, $tmp[$i]);
  }
  if( scalar(grep{$_ == TRACE} @res) )
  { print STDERR "Found ",TRACE," as subway of $way\n" }
  return @res;
}

sub OutputWay
{
  my $way = shift;
  my $complete = shift;
  my @list = Consolidate(\%ways, $way);
  $ways_output += scalar(@list);
  print "",($complete?"C":"I"),scalar(@list)," ",join(" ",@list),"\n" or die "Output error ($!)\n";
}

