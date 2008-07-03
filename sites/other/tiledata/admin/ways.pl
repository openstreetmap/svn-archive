#!/usr/bin/perl
use XML::Parser;
use Data::Dumper;
use DBI;
use dbpassword;

#print STDERR "way data - ctrl-c to quit in next 10 seconds\n";sleep(10);print STDERR "Running...\n";

# Connect to database
my $db = DBI->connect(getDatabase, getUser, getPass) or die();
my $GetNodeSQL = $db->prepare('select * from nodepos where id=?');

#print getNode(200575), "\n";
#print getNode(200575), "\n";
#exit;

my $Count = 0;
my $inWay = 0;
my $Way;
my %NodeCache;
my $NodeCacheSize = 0;
my $NodeCount = 0;
my ($Hit, $Miss) = (0,0);
my $FoundStart = 0;
my $LineNum = 0;
my $TimeSinceWhine = 0;

$db->prepare("delete from wayloc")->execute();
$db->prepare("delete from waydata")->execute();
$db->prepare("delete from nodedep")->execute();

# List of tags to ignore (not save)
my %Ignore;
foreach my $Word(split(/,\s*/,"created_by, ele, source, time, editor, author, hdop, pdop, sat, speed, fix, course, converted_by"))
{
  $Ignore{$Word} = 1;
}

my $InsertSQL = $db->prepare('INSERT INTO wayloc VALUES (?,?)');
my $WaySQL = $db->prepare('INSERT INTO waydata VALUES (?,?)');
my $NodeDepSQL = $db->prepare('INSERT INTO nodedep VALUES (?,?)');

while(my $Line = <>)
{
  $LineNum++;
  # next if( $LineNum < 840000000);  #all nodes!
  if($Line =~ m{^\s*<way id=["'](\d+)['"]})
  {
    $Way = {id => $1, nodes=>[], tags=>{}, numtags=>0, tiles=>{}};
    $inWay = 1;
    $FoundStart = 1;
  }
  elsif(!$FoundStart)
  {
    if(++$TimeSinceWhine == 1000000)
    {
      printf "ignored %d million lines\n", $LineNum / 1000000;
      $TimeSinceWhine = 0;
    }
    next;
  }
  elsif($Line =~ m{^\s*<nd ref=['"](\d+)['"]})
  {
    my $ID = $1;
    
    my ($ID2,$x,$y,$tile) = getNode($ID);
    if($ID == $ID2)
    {
      push(@{$Way->{nodes}}, [$ID,$x,$y]);
      $Way->{tiles}->{$tile} = 1;
    }
  
  }
  elsif($inWay && $Line =~ m{^\s*<tag k=['"](.*)["'] v=["'](.*)["']\s*/>})
  {
    if(!$Ignore{$1})
      {
      $Way->{tags}->{$1} = $2;
      $Way->{numtags}++;
      }
  }
  elsif($Line =~ m{^\s*</way>})
  {
    print "$Count ways\n" if(($Count++ % 5000) == 0);
    
    if($Way->{numtags} > 0) # Don't store untagged ways
    {
      $ID = $Way->{id};
      $WayAsXml = way2xml($Way);
      
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
printf "Cache hit %d of %d = %1.2f%%\n", 
  $Hit, 
  $Miss, 
  100.0 * $Hit / ($Hit + $Miss);

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
  if(@row = $GetNodeSQL->fetchrow_array())
  {
    $NodeCache{$ID} = [$NodeCount, [@row]];
    #print Dumper($NodeCache{$ID});
    return(@row);
  }   
  
  return(0,0,0,'');
}
