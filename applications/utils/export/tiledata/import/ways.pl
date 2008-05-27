#!/usr/bin/perl
use XML::Parser;
use Data::Dumper;
use DBI;
use dbpassword;

print STDERR "way data - ctrl-c to quit in next 10 seconds\n";sleep(10);print STDERR "Running...\n";

# Connect to database
my $db = DBI->connect("dbi:mysql:ojw:localhost:3306", getUser, getPass) or die();

# Parse XML from stdin
my $xml = new XML::Parser(Handlers => {
  Char =>  \&char_handler,
  Start =>  \&start_handler,
  End =>  \&end_handler,
  Default => \&default_handler});

my $Count = 0;
my $inWay;
my $Way;

$db->prepare("delete from wayloc")->execute();
$db->prepare("delete from waydata")->execute();

# List of tags to ignore (not save)
my %Ignore;
foreach my $Word(split(/,\s*/,"created_by, ele, source, time, editor, author, hdop, pdop, sat, speed, fix, course, converted_by"))
{
  $Ignore{$Word} = 1;
}

my $InsertSQL = $db->prepare("INSERT INTO wayloc VALUES (?,?)");
my $WaySQL = $db->prepare("INSERT INTO waydata VALUES (?,?)");

$xml->parse(STDIN);

sub char_handler {
  my($xml, $data) = @_;
}

sub start_handler {
  my($xml, $element, %attr) = @_;
  if($element eq 'way')
  {
    $Way = {id => $attr{id}, nodes=>[], tags=>{}, numtags=>0, tiles=>{}};
    $inWay = 1;
  }
  elsif($element eq 'nd')
  {
    my $ID = $attr{ref};
    
    # Lookup the node
    my $SQL = sprintf('select * from nodepos where id=%d', $ID);
    my $Query = $db->prepare($SQL);
    $Query->execute();
    
    if(@row = $Query->fetchrow_array())
    {
      push(@{$Way->{nodes}}, [@row]);
      $Way->{tiles}->{$row[3]} = 1;
    } 
    
  }
  elsif($inWay && $element eq 'tag')
  {
    if(!$Ignore{$attr{k}})
      {
      $Way->{tags}->{$attr{k}} = $attr{v};
      $Way->{numtags}++;
      }
  }
  
}

sub end_handler {
  my($xml, $element, %attr) = @_;
  if($element eq 'way')
  {
    print "$Count ways\n" if(($Count++ % 5000) == 0);
    
    if($Way->{numtags} > 0) # Don't store untagged ways
    {
      #print Dumper($Way);
      $ID = $Way->{id};
      $WayAsXml = way2xml($Way);
      
      if(0)
      {
        my $Filename = wayFilename($ID);
        #print "Saving $ID to $Filename\n";
        open($fp, ">$Filename") || die("Can't write to $Filename ($!)\n");
        print $fp $WayAsXml;
        close $fp;
      }
      else
      {
        $WaySQL->execute($ID, $WayAsXml);
      }
    
      foreach my $Tile(keys(%{$Way->{tiles}}))
      {
        #my ($tx,$ty) = split(/,/, $Tile);
        
        $InsertSQL->execute($Way->{id}, $Tile);
        
        #print "Saving to $Filename\n";
      }
    }
    #print way2xml($Way);
    #die;
  }
}


sub wayFilename
{
  my $ID = "".shift();
  
  $ID =~ s/(\d{3}?)(?=(\d{1,3})+$)/$1,/g;
  @Parts = split(/,/, $ID);

  makeFilename("tiles/ways", \@Parts, "way.txt")
}

sub makeFilename
{
  my ($Base, $Dirs, $File) = @_;
  my $Filename = $Base;
  foreach my $Part(@{$Dirs})
  {
    $Filename .= sprintf("/%d", $Part);
    mkdir($Filename) if(!-d $Filename);
  }
  return($Filename . "/$File");
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
  while(my($k,$v) = each(%{$Way->{tags}}))
  {
    $Text .= sprintf("  <tag k='%s' v='%s' />\n", $k, $v);
  }
  $Text .= "</way>\n";
}


sub default_handler{}