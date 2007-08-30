#!/usr/bin/perl
use warnings;
use strict;

use Getopt::Long;
use Pod::Usage;

BEGIN {
  unshift @INC, "../../perl_lib";
}

use Geo::OSM::OsmChangeReader;
use Geo::OSM::EntitiesV3;
use Geo::OSM::APIClientV4;

my($input, $username, $password, $api, $cache, $help);
my $force = 0;
my $dry_run = 0;
my $loop = 0;

Getopt::Long::Configure('no_ignore_case');
GetOptions (
             'i|input=s'          => \$input,
             'u|username=s'       => \$username,
             'p|password=s'       => \$password,
             'a|api=s'            => \$api,
             'c|cache=s'          => \$cache,
             'h|help'             => \$help,
             'f|force+'           => \$force,
             'n|dry-run'          => \$dry_run,
             'l|loop'             => \$loop,
             ) or pod2usage(1);

pod2usage(1) if $help;

#$api ||= "http://openstreetmap.gryph.de/api/0.5/";
$api ||= "http://www.openstreetmap.org/api/0.4";

if( not $dry_run and (not defined $username or not defined $password) )
{
  die "Must supply username and password to upload data\n";
}
$cache ||= $input.".cache";

my $OSM = new Geo::OSM::OsmChangeReader(\&process,\&progress);
my $start_time = time();
my $last_time = 0;
my $uploader = new Geo::OSM::APIClient( api => $api, username => $username, password => $password )
    unless $dry_run;

init_cache($cache);

#$OSM->load("data/nld-delete.osm");
my $did_something;

do {
  $did_something = 0;
  $OSM->load($input);
} while($loop and $did_something);

use Data::Dumper;
sub process
{
  my($command, $entity, $attr, $tags, $segs) = @_;
  
  my $ent;
  if( $entity eq "node" )
  {
    $ent = new Geo::OSM::Node( $attr, $tags );
  }
  if( $entity eq "segment" )
  {
    $ent = new Geo::OSM::Segment( $attr, $tags );
  }
  if( $entity eq "way" )
  {
    $ent = new Geo::OSM::Way( $attr, $tags, $segs );
  }
#  print Dumper($ent);
#  print $ent->xml;
#  return if $dry_run;
  return if resolve_ids( $ent, $command );

  my $id;
  if( not $dry_run )
  {
    if( $command eq "create" )
    {
      $id = $uploader->create( $ent );
    }
    elsif( $command eq "modify" )
    {
      $id = $uploader->modify( $ent );
    }
    elsif( $command eq "delete" )
    {
      $id = $uploader->delete( $ent );
    }
  }
  else
  {
    $id = 42;
  }

  if( not defined $id )
  {
    print "Error: ".$uploader->last_error_code()." ".$uploader->last_error_message." ($command ".$ent->type()." ".$ent->id().")\n";
    # Unless force is on, exit on any error
    exit if $force == 0;
    # For force==1, exit on Bad Request, Unauthorized or Interal Server Error
    # These shouldn't happen, but tend to keep happening when they do
    # Note: when you can't connect to server you get error 500
    if( $force == 1 )
    {
      my $code = $uploader->last_error_code();
      exit if $code == 401 or $code == 400 or $code == 500;
    }
    # Force==2 is keep on going, no matter what
  }
  else
  {
    mark_done( $ent, $command, $id );
    $did_something = 1;
  }
  
#  my $id = $uploader->upload($ent);
}

sub progress
{
  my $count = shift;
  my $perc = shift;
  my $time = time();
#  print "$time == $last_time or $last_time == $start_time\n";
  return if $time == $last_time or $time == $start_time;
  
  $last_time = $time;  
  my $remain = (1-$perc)*($last_time - $start_time)/$perc;
  printf STDERR "%10d %7.2f%% %3d:%02d:%02d \r", $count, $perc*100, int($remain)/3600, int($remain/60)%60, int($remain)%60;
}

#my %resolved;
my %done;
my $cache_fh;

sub mark_done
{
  my( $ent, $command, $newid ) = @_;

  my $line = substr($ent->type,0,1).substr($command,0,1)." ".$ent->id()." ";
  if( not defined $newid )
  {
    $line .= "!"
  }
  elsif( $ent->id < 0 and $newid > 0 )
  {
    $line .= $newid;
  }
  else
  {
    $line .= "0";
  }
  print $cache_fh $line,"\n"    unless $dry_run;
  
  $done{$ent->type}{$command}{$ent->id} = $newid;
}

# Cache has the following format
# n=node, s=segment, w=way
# c=create, m=modify, d=delete
# nc -1  3988283      -- create succeed, newid given
# sm 3498283 0        -- modify segment done
# wd 273743 !         -- delete way failed
sub init_cache
{
  my $cache = shift;
  if( open my $fh, "<", $cache )  # Check readable first
  {
    my %types = qw( n node s segment w way );
    my %commands = qw( c create m modify d delete );
    while(<$fh>)
    {
      if( /^([nsw])([cmd]) (-?\d+) (\d+|!)$/ )
      {
        my $type = $types{$1};
        my $command = $commands{$2};
        my $id = $3;
        my $status = $4;
        
        if( $status ne "!" )
        {
          $done{$type}{$command}{$id}=1;
        }
      }
      else
      {
        die "Unknown line $. in cache: $_\n";
      }
    }
  }
  open $cache_fh, ">>", $cache or die "Couldn't open cache for append ($!)\n";
  $cache_fh->autoflush(1);
}

sub resolve_ids
{
  my $ent = shift;
  my $command = shift;
  
  return 1 if $done{$ent->type}{$command}{$ent->id};

  my $incomplete = 0;
  if( $ent->id < 0 )
  {
    if( exists $done{$ent->type}{create}{$ent->id} )
    {
      $ent->set_id( $done{$ent->type}{create}{$ent->id} );
    }
    elsif( $command ne "create" )
    { $incomplete = 1 }
  }
  if( $ent->type eq "segment" )
  {
    my $from = $ent->from;
    my $to = $ent->to;
    if( $from < 0 and exists $done{node}{create}{$ent->from} )
    {
      $from = $done{node}{create}{$ent->from};
    }
    if( $to < 0 and exists $done{node}{create}{$ent->to} )
    {
      $to = $done{node}{create}{$ent->to};
    }
    $ent->set_fromto( $from, $to );
    if( $from < 0 or $to < 0 )
    { $incomplete = 1 }
  }
  if( $ent->type eq "way" )
  {
    my $segs = $ent->segs;
    my @newsegs;
    for my $seg (@$segs)
    {
      if( $seg < 0 and exists $done{segment}{create}{$seg} )
      {
        $seg = $done{segment}{create}{$seg};
      }
      push @newsegs, $seg;
      if( $seg < 0 )
      { $incomplete = 1 }
    }
    $ent->set_segs( \@newsegs );
  }
  return $incomplete;
}

=head1 NAME

B<bulk_upload.pl>

=head1 DESCRIPTION

Reads osmChange files and upload the changes to the server via the API

=head1 SYNOPSIS

B<Common usages:>

B<bulk_upload.pl> -i input.osc -u username -p password [-a http://server/api/version] [-c cachefile]

  -i input.osc                  file read for changes
  -u username                   username for server
  -p password                   password for server
  -a http://server/api/version  server to uplaod to
                                default: http://www.openstreetmap.org/api/0.4/
  -c cachefile                  the name of the cachefile
                                default: <input.osc>.cache
  -f                            continue even if server returns an transient error
                                (eg connection timeout)
  -ff                           continue no matter what
  -n                            dry-run (just parse, don't do anything)
  -l                            keep redoing file until nothing more can be done
                                useful if file is not sorted
    
The cachefile is a file that tracks the usage of placeholders and what has
been uploaded already, allowing aborted uploads to continue. Do not use an
out of date cache and don't modify the source file between upload attempts!

=head1 AUTHOR

Martijn van Oosterhout <kleptog@svana.org>

=head1 COPYRIGHT

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

=head1 SEE ALSO

http://www.openstreetmap.org/

=cut
