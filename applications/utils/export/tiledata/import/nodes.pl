#!/usr/bin/perl
use DBI;
use dbpassword;
use latlon_to_relative;
use strict;

my $db = DBI->connect("dbi:mysql:ojw:localhost:3306", getUser, getPass) or die();

$db->prepare('delete from nodepos')->execute();

my $Count = 0;

while(my $Line = <>)
{
  if($Line =~ m{^\s*<node (.*)})
  {
    my $Data = $1;
    if($Data =~ m{id="(\d+)" lat="(.*?)" lon="(.*?)"})
    {
      my $ID = $1;
      my ($lat, $lon) = ($2,$3);
      $lat = -85 if($lat < -85);
      $lat = 85 if($lat > 85);
      
      my ($x,$y, $tx,$ty) = latlon2relativeXY($lat, $lon);

      #printf("%d, %d -> %d,%d\n", $x, $y, $tx, $ty);
      addPoint($ID,$x,$y, $tx,$ty);
    }
  }
  #die if($Count > 40);
  print "$Count\n" if(($Count % 100000) == 0);
  
  $Count++;
}

sub addPoint
{
  my($ID,$x,$y,$tx,$ty) = @_;
  
  my $SQL = sprintf(
    'insert into nodepos values (%d, %d, %d, \'%05d,%05d\')', 
    $ID, $x, $y, $tx, $ty);
  
  $db->prepare($SQL)->execute();
  
}