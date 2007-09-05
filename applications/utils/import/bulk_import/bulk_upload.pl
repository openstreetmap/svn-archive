#!/usr/bin/perl
use warnings;
use strict;

use Getopt::Long;
use Pod::Usage;
use DB_File;
use Time::HiRes qw ( time );   # Get time with floating point seconds

BEGIN {
  unshift @INC, "../../perl_lib";
}

use Geo::OSM::OsmChangeReader;
use Geo::OSM::EntitiesV3;
use Geo::OSM::APIClientV4;

my($input, $username, $password, $api, $help, %additional_tags);
my $force = 0;
my $dry_run = 0;
my $loop = 0;
my $verbose = 0;

Getopt::Long::Configure('no_ignore_case');
Getopt::Long::Configure ("bundling");
GetOptions (
             'i|input=s'          => \$input,
             'u|username=s'       => \$username,
             'p|password=s'       => \$password,
             'a|api=s'            => \$api,
             'h|help'             => \$help,
             'f|force+'           => \$force,
             'n|dry-run'          => \$dry_run,
             'l|loop'             => \$loop,
             't|tags=s%'          => \%additional_tags,
             'v|verbose+'         => \$verbose,
             ) or pod2usage(1);

pod2usage(1) if $help;

#$api ||= "http://www.openstreetmap.org/api/0.4";
if( not defined $api )
{
  die "Must supply the URL of the API (-a) (standard is http://www.openstreetmap.org/api/0.4/)\n";
}

if( not defined $input )
{
  die "Must supply input file name (-i)\n";
}
if( not $dry_run and (not defined $username or not defined $password) )
{
  die "Must supply username and password to upload data\n";
}
my $cache = $input.".cache";
my $dbcache = $input.".dbcache";
my %db_file;

my $quit_request = 0;
$SIG{INT} = sub { $quit_request = 1};

my $OSM = new Geo::OSM::OsmChangeReader(\&process,\&progress);
my $start_time = time();
my $last_time = 0;
my $uploader = new Geo::OSM::APIClient( api => $api, username => $username, password => $password )
    unless $dry_run;

init_cache($cache,$dbcache);

#$OSM->load("data/nld-delete.osm");
my $did_something;
my $delay = 0;   # Delay if get 500 errors...

# On the first iteration, we reset the counters
if( not exists $db_file{loop} or $db_file{loop} eq "0" )
{
  $db_file{loop} = 0;
  $db_file{total} = 0;
  $db_file{count} ||= 0;
}

do {
  $did_something = 0;
  $OSM->load($input);
  $db_file{loop}++;
} while($loop and $did_something == 3);  # We exit if all failed *OR* all succeeded *OR* did nothing

print STDERR  "\n";
exit(1) if $did_something >= 2;  # Exiting because something failed
exit(0);  # Otherwise, nothing to do or everything worked

sub process
{
  my($command, $entity, $attr, $tags, $segs) = @_;
  
  exit 3 if $quit_request;
  
  push @$tags, %additional_tags;

  if( $db_file{loop} == 0 )
  { $db_file{total}++ }
    
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
  my $skipped = resolve_ids( $ent, $command );
  if( $skipped )
  {
    print "Skipped: $command ".$ent->type()." ".$ent->id()."\n"
         if( $verbose and $skipped == 1 );
    return 0;
  }

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
    $db_file{failed}++;
    # Unless force is on, exit on any error
    exit(1) if $force == 0;
    # For force==1, exit on Bad Request, Unauthorized or Internal Server Error
    # These shouldn't happen, but tend to keep happening when they do
    # Note: when you can't connect to server you get error 500
    if( $force == 1 )
    {
      my $code = $uploader->last_error_code();
      exit(2) if $code == 401 or $code == 400 or $code == 500;
    }
    # Force==2 is keep on going, no matter what
    
    if( $uploader->last_error_code() == 500 )
    {
      sleep(2**$delay);
      $delay++;
    }
    $did_something |= 2;
  }
  else
  {
    mark_done( $ent, $command, $id );
    $did_something |= 1;
    $db_file{count}++;
    $delay = int($delay/2);
  }
  return;
}

sub progress
{
  my $count = shift;
  my $perc = shift;
  my $time = time();
  exit 3 if $quit_request;
  
#  print "$time == $last_time or $last_time == $start_time\n";
  return if $time == $last_time or $time == $start_time;
  return if $db_file{total} == 0 or $perc == 0 or $db_file{count} == 0; # Any of these causes problems
  $last_time = $time;  
  
  if( $db_file{loop} != 0 )
  {
    # After the first loop we have the actual count of changes in the file
    $perc = $db_file{count}/$db_file{total};
  }
  else
  {
    # During the first loop, we only have the percentage of the file done to go on
    # Adjust it for work actually done
    $perc = $perc*$db_file{count}/$db_file{total};
  }
  my $remain = (1-$perc)*($last_time - $start_time)/$perc;
  printf STDERR "Loop: %2d Done:%10d/%10d %7.2f%% %3d:%02d:%02d \r", $db_file{loop},
       $db_file{count}, $db_file{total}, $perc*100, int($remain)/3600, int($remain/60)%60, int($remain)%60;
}

sub key
{
  my($key,$command,$id) = @_;
  return substr($key,0,1).substr($command,0,1).$id;
}

sub mark_done
{
  my( $ent, $command, $newid ) = @_;

  return if $dry_run;
  
  my $key = key($ent->type(), $command, $ent->id());
  
  $db_file{$key} = $newid;
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
  my $dbcache = shift;

  # Open the cache file, as DB cache
  tie %db_file, "DB_File", $dbcache, O_CREAT|O_RDWR, 0666, $DB_HASH
    or die "Could not open dbcachefile '$dbcache' ($!)\n";

  # This is backward compatability code, to transfer from old cache format
  # to new. There's no way back.
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
          $db_file{key($type, $command ,$id)}=$status;
        }
      }
      else
      {
        die "Unknown line $. in cache: $_\n";
      }
    }
    unlink $cache;
  }
}

sub resolve_ids
{
  my $ent = shift;
  my $command = shift;
  
  return 0 if $dry_run;
  return 2 if $db_file{key($ent->type, $command, $ent->id)};

  my $incomplete = 0;
  if( $ent->id < 0 )
  {
    if( exists $db_file{key($ent->type, 'create', $ent->id)} )
    {
      $ent->set_id( $db_file{key($ent->type, 'create', $ent->id)} );
    }
    elsif( $command ne "create" )
    { $incomplete = 1 }
  }
  if( $ent->type eq "segment" )
  {
    my $from = $ent->from;
    my $to = $ent->to;
    if( $from < 0 and exists $db_file{key('node', 'create', $ent->from)} )
    {
      $from = $db_file{key('node', 'create', $ent->from)};
    }
    if( $to < 0 and exists $db_file{key('node', 'create', $ent->to)} )
    {
      $to = $db_file{key('node', 'create', $ent->to)};
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
      if( $seg < 0 and exists $db_file{key('segment', 'create', $seg)} )
      {
        $seg = $db_file{key('segment', 'create', $seg)};
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

  -i input.osc                  file read for changes (required)
  -u username                   username for server (required)
  -p password                   password for server (required)
  -a http://server/api/version  server to upload to (required)
                                OSM is: http://www.openstreetmap.org/api/0.4/
  -f                            continue even if server returns an transient error
                                (eg connection timeout)
  -ff                           continue no matter what
  -n                            dry-run (just parse, don't do anything)
  -l                            keep redoing file until nothing more can be done
                                useful if file is not sorted
  -t key=value                  Add the tag key=value to each uploaded object
    
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
