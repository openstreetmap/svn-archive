#!/usr/bin/perl
use DBI;
use dbpassword;
use latlon_to_relative;
use strict;

open LOG, ">log.txt" || die("Can't open logfile\n");

print STDERR "This will delete existing nodes - ctrl-c to quit in next 10 seconds\n";sleep(10);print STDERR "Running...\n";

my $db = DBI->connect(getDatabase, getUser, getPass) or die();

$db->prepare('delete from nodepos')->execute();

my $Count = 0;

my $SQL = $db->prepare("INSERT INTO nodepos VALUES (?,?,?,?)");

while(my $Line = <>)
{
  if($Line =~ m{^\s*<node (.*)})
  {
    my $Data = $1;
    if($Data =~ m{id="(\d+)" .*lat="(.*?)" lon="(.*?)"})
    {
      my $ID = $1;
      my ($lat, $lon) = ($2,$3);
      if($lat > -85 and $lat < 85)
      {
        my ($x,$y, $tx,$ty) = latlon2relativeXY($lat, $lon);
        $SQL->execute($ID, $x, $y, sprintf('%05d,%05d', $tx, $ty));
      }
    }
  }
  #die if($Count > 40);
  if(($Count % 100000) == 0)
  {
    my $num = "$Count";
    $num =~ s/(\d{1,3}?)(?=(\d{3})+$)/$1,/g;
    my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
    printf LOG "%02d:%02d:%02d %s nodes\n", $hour, $min, $sec, $num;
  }
  
  $Count++;
}

