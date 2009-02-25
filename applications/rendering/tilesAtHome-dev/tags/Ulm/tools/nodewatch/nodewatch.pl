#!/usr/bin/perl -w
#---------------------------------------------------------------------
# Martijn van Oosterhout
# http://lists.openstreetmap.org/pipermail/dev/2006-December/002706.html
#---------------------------------------------------------------------
use Math::Trig;
use POSIX qw(floor);
use LWP::Simple;

# With this on it won't actually put in any requests
use constant TESTING => 0;

use constant PI => 4 * atan2 1, 1;

# File to store info about when a tile was last requested
use constant CACHEFILE => '/tmp/tilecache.dat';
# Request URL
use constant REQUEST_URL => 'http://dev.openstreetmap.org/~ojw/NeedRender/?x=%d&y=%d&src=rss&priority=1';
# URL of RSS feed
use constant FEED_URL => 'http://www.openstreetmap.org/feeds/nodes.rss';

use strict;
my %hash;
my $str = get( FEED_URL );
if( not defined $str )
{
  die "Failed to download feed: ".FEED_URL."\n";
}

while( $str =~ m,<item>(.*?)</item>,g )
{
  my $part = $1;
  my($lat,$lon) = (undef, undef);

  if( $part =~ m,<geo:lat>-?([0-9.]+)</geo:lat>, )
  {
    $lat = $1;
  }
  if( $part =~ m,<geo:lon>-?([0-9.]+)</geo:lon>, )
  {
    $lon = $1;
  }

  next unless defined $lat and defined $lon;

  # Taken from http://almien.co.uk/OSM/Tools/Coord/source.php
  my $PX = ($lon + 180) / 360; 
  my $PY = Lat2Y($lat);

  my $X = POSIX::floor($PX*4096);
  my $Y = POSIX::floor($PY*4096);

  my $id = $X + ($Y<<12);
#  print "($lat,$lon) ($X,$Y) ($id)\n";
  $hash{$id}++;
}

my @changed = keys %hash;

# In theiry we're done, but we add a little extra smarts to not request the
# same tile twice within 48 hours.

my $day = int( time() / 86400 );
if( ! -e CACHEFILE )
{
  open( CACHE, "+>", CACHEFILE ) or die "Failed to create request cache '".CACHEFILE."' ($!)\n";
}
else
{
  open( CACHE, "+<", CACHEFILE ) or die "Failed to open request cache '".CACHEFILE."' ($!)\n";
}

for my $id (@changed)
{
  my $buf;
  seek CACHE, $id*2, 0;
  read CACHE, $buf, 2;
  $buf .= "\0\0";
  my $oldday = unpack "n", $buf;

  next if $oldday > $day-2;

  # Only mark done if request succeeded
  if( defined Request($id) )
  {
    seek CACHE, $id*2, 0;
    $buf = pack "n", $day;
    print CACHE $buf;
  }
}

exit;

sub Request
{
  my $id = shift;
  my $x = $id & 0xFFF;
  my $y = $id >> 12;

  my $url = sprintf REQUEST_URL, $x, $y;

  print "Get: $url\n";
  return undef if TESTING;

  my $res = get($url);
  if( not defined $res )
  {
    warn "Request failed: $url\n";
  }
  return $res;
}

sub Lat2Y
{
  my $Lat = shift;
  my $LimitY = ProjectF(85.0511);
  my $Y = ProjectF($Lat);
  
  my $PY = ($LimitY - $Y) / (2 * $LimitY);
  return($PY);
}

sub ProjectF
{
  my $Lat = shift;
  $Lat = deg2rad($Lat);
  my $Y = log(tan($Lat) + (1/cos($Lat)));
  return($Y);
}

