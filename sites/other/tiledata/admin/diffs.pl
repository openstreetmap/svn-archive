#!/usr/bin/perl
use XML::Parser;
use Data::Dumper;
use DBI;
use dbpassword;
use latlon_to_relative;
use Data::Dumper;
use strict;

my $db = DBI->connect(getDatabase, getUser, getPass) or die();

my $GetNodeSQL = $db->prepare('select * from nodepos where id=?');
my $InsertNodeSQL = $db->prepare("INSERT INTO nodepos VALUES (?,?,?,?)");
my $UpdateNodeSQL = $db->prepare("UPDATE nodepos SET id=?,x=?,y=?,tile=? where ID=?");
my $DeleteNodeSQL = $db->prepare("DELETE FROM nodepos WHERE ID=?");

my $waylocDeleteSQL = $db->prepare("DELETE FROM wayloc WHERE way=?");
my $waydataDeleteSQL = $db->prepare("DELETE FROM waydata WHERE id=?");
my $nodedepDeleteSQL = $db->prepare("DELETE FROM nodedep WHERE way=?");

my $InsertSQL = $db->prepare('INSERT INTO wayloc VALUES (?,?)');
my $WaySQL = $db->prepare('INSERT INTO waydata VALUES (?,?)');
my $NodeDepSQL = $db->prepare('INSERT INTO nodedep VALUES (?,?)');

my $wayfromnodedepSQL = $db->prepare("SELECT way FROM nodedep WHERE node=?");
my $waydataSQL = $db->prepare("SELECT data FROM waydata WHERE id=?");

my $state = 0;
my %dirtyNode;
my $inWay = 0;
my $Way;
my %Ignore;
my %NodeCache;
my $NodeCacheSize = 0;
my $NodeCount = 0;
my ($Hit, $Miss) = (0,0);


foreach my $Word(split(/,\s*/,"created_by, ele, source, time, editor, author, hdop, pdop, sat, speed, fix, course, converted_by"))
{
  $Ignore{$Word} = 1;
}

while(my $Line = <>)
{
    #print $Line;
    if ($Line =~ m{^\s+<create}) {
	$state = 1;
    } elsif ($Line =~ m{^\s+<modify}) {
	$state = 2;
    } elsif ($Line =~ m{^\s+<delete}) {
	$state = 3;
    } elsif ($Line =~ m{^\s+<node (.*)}) {
	my $Data = $1;
	if($Data =~ m{id="(\d+)" .* lat="(.*?)" lon="(.*?)"}) {
	    my $ID = $1;
	    my ($lat, $lon) = ($2,$3);
	    if($lat > -85 and $lat < 85)
	    {
		my ($x,$y, $tx,$ty) = latlon2relativeXY($lat, $lon);
		if ($state == 1) {
		    #print("create/node $ID $x $y\n");
		    $InsertNodeSQL->execute($ID, $x, $y, sprintf('%05d,%05d', $tx, $ty));
		} elsif ($state == 2) {
		    #print("modify/node $ID $x $y\n");
		    $dirtyNode{$ID} = 1;
		    $UpdateNodeSQL->execute($ID, $x, $y, sprintf('%05d,%05d', $tx, $ty), $ID);
		} elsif ($state == 3) {
		    #print("delete/node $ID\n");
		    $DeleteNodeSQL->execute($ID);
		}
	    }
	}
    } elsif($Line =~ m{^\s*<way id=["'](\d+)['"]}) {
	my $id = $1;
	$Way = {id => $id, nodes=>[], tags=>{}, numtags=>0, tiles=>{}};
	$inWay = 1;
	if ($state == 3) {
	    #print("delete/way $id\n");
	    $waylocDeleteSQL->execute($id);
	    $waydataDeleteSQL->execute($id);
	    $nodedepDeleteSQL->execute($id);
	}
    } elsif($Line =~ m{^\s*<nd ref=['"](\d+)['"]}) {
	my $ID = $1;
	
	my ($ID2,$x,$y,$tile) = getNode($ID);
	if($ID == $ID2)
	{
	    push(@{$Way->{nodes}}, [$ID,$x,$y]);
	    $Way->{tiles}->{$tile} = 1;
	}
    } elsif($inWay && $Line =~ m{^\s*<tag k=['"](.*)["'] v=["'](.*)["']\s*/>}) {
	if(!$Ignore{$1})
	{
	    $Way->{tags}->{$1} = $2;
	    $Way->{numtags}++;
	}
    } elsif($Line =~ m{^\s*</way>}) {
	my $ID = $Way->{id};
	if ($state == 1) {
	    #print("create/way $ID $WayAsXml");
	} elsif ($state == 2) {
	    #print("modify/way $ID $WayAsXml");
	    $waylocDeleteSQL->execute($ID);
	    $waydataDeleteSQL->execute($ID);
	    $nodedepDeleteSQL->execute($ID);
	}
	
	if($Way->{numtags} > 0) # Don't store untagged ways
	{
	    my $WayAsXml = way2xml($Way);
	    
	    if ($state == 1 or $state == 2) {
		die "tried to add node with id=0"
		    if $ID == 0;
		$WaySQL->execute($ID, $WayAsXml);
		foreach my $Node(@{ $Way->{nodes} })
		{
		    $NodeDepSQL->execute(@{ $Node }[0], $ID);
		}
	    
		foreach my $Tile(keys(%{$Way->{tiles}}))
		{
		    $InsertSQL->execute($Way->{id}, $Tile);
		}
	    }
	}
    }
}

# handle dirty ways
{
    my %dirtyWay;
    for my $n (keys(%dirtyNode)) {
	$wayfromnodedepSQL->execute($n);
	while (my @row = $wayfromnodedepSQL->fetchrow_array()) {
	    print("dirty node $n -> dirty way $row[0]\n");
	    $dirtyWay{$row[0]} = 1;
	}
    }
    for my $id (keys(%dirtyWay)) {
	$waydataSQL->execute($id);
	my @row = $waydataSQL->fetchrow_array();
	my $old_waydata = $row[0];
	$waylocDeleteSQL->execute($id);
	$waydataDeleteSQL->execute($id);
	$nodedepDeleteSQL->execute($id);

	$Way = xml2way($old_waydata);
	my $WayAsXml = way2xml($Way);
	print("dirty way $id $old_waydata -> $WayAsXml\n");
	{
	    die "tried to add node with id=0"
		if $id == 0;
	    $WaySQL->execute($id, $WayAsXml);
	    foreach my $Node(@{ $Way->{nodes} })
	    {
		$NodeDepSQL->execute(@{ $Node }[0], $id);
	    }
	    
	    foreach my $Tile(keys(%{$Way->{tiles}}))
	    {
		$InsertSQL->execute($Way->{id}, $Tile);
	    }
	}
    }
}

sub xml2way
{
    my ($xml) = @_;
    my $Way;
    
    foreach my $Line (split("\n", $xml)) {
	if($Line =~ m{^\s*<way id=["'](\d+)['"]}) {
	    my $id = $1;
	    $Way = {id => $id, nodes=>[], tags=>{}, numtags=>0, tiles=>{}};
	} elsif($Line =~ m{^\s*<nd id=[\'"](\d+)['"]}) {
	    my $ID = $1;
	
	    my ($ID2,$x,$y,$tile) = getNode($ID);
	    if($ID == $ID2)
    	    {
	       push(@{$Way->{nodes}}, [$ID,$x,$y]);
	       $Way->{tiles}->{$tile} = 1;
	   }
	} elsif($Line =~ m{^\s*<tag k=['"](.*)["'] v=["'](.*)["']\s*/>}) {
	    if(!$Ignore{$1})
	    {
		$Way->{tags}->{$1} = $2;
		$Way->{numtags}++;
	    }
	}
    }
    return $Way;
}

sub way2xml
{
  my $Way = shift();
  my $Text = sprintf("<way id='%d'>\n", $Way->{id});
  
  foreach my $Node(@{$Way->{nodes}})
  {
    $Text .= sprintf(
      "  <nd id='%d' x='%d' y='%d' />\n", 
      $Node->[0],
      $Node->[1],
      $Node->[2]);
  }
  #while(my($k,$v) = each(%{$Way->{tags}}))
  foreach my $k (sort (keys %{$Way->{tags}}))
  {
    my $v = $Way->{tags}->{$k};
    $Text .= sprintf("  <tag k='%s' v='%s' />\n", $k, $v);
  }
  $Text .= "</way>\n";
}


sub getNode
{
  my $ID = shift();

  if(exists($NodeCache{$ID}))
  {
    $Hit++;
    #print Dumper($NodeCache{$ID});
    
    return(@{$NodeCache{$ID}->[1]});
  }
  
  $Miss++;
  $NodeCount++;
  
  my $MaxSize = 20000;
  if($NodeCacheSize > $MaxSize)
  {
    my $Limit = $NodeCount - ($MaxSize / 2);
    $NodeCacheSize = 0;
    
    while(my($k,$v) = each(%NodeCache))
    {
      if($v->[0] < $Limit)
        {
        delete($NodeCache{$k});
        }
      else
        {
        $NodeCacheSize++;
        }
    }
  }
  
  $NodeCacheSize++;
  
  # Lookup the node
  $GetNodeSQL->execute($ID);
  if(my @row = $GetNodeSQL->fetchrow_array())
  {
    $NodeCache{$ID} = [$NodeCount, [@row]];
    #print Dumper($NodeCache{$ID});
    return(@row);
  }   
  
  return(0,0,0,'');
}
