#!/usr/bin/perl
use warnings;
use strict;

use Getopt::Long;
use Pod::Usage;
use Time::HiRes qw ( time );   # Get time with floating point seconds
use POSIX qw(sigaction);

BEGIN {
  unshift @INC, "../../perl_lib";
}

use Geo::OSM::OsmChangeReaderV3;
use Geo::OSM::EntitiesV3;
use Geo::OSM::APIClientV4;

$ENV{TZ} = "UTC";

my($input, $username, $password, $api, $help, %additional_tags);
my $force = 0;
my $dry_run = 0;
my $loop = 0;
my $verbose = 0;
my $extract = 0;

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
             'x|extract+'         => \$extract,
             ) or pod2usage(1);

pod2usage(1) if $help;

#$api ||= "http://www.openstreetmap.org/api/0.4";
if( defined $api and $extract > 0 )
{
  die "Can only supply one of -a and -x\n";
}

#if( $dry_run and $extract > 0 )
#{
#  die "Can't do a dry-run on extraction\n";
#}

if( not defined $api and $extract == 0)
{
  die "Must supply the URL of the API (-a) (standard is http://www.openstreetmap.org/api/0.4/)\n";
}

if( not defined $input )
{
  die "Must supply input file name (-i)\n";
}
if( defined $api and not $dry_run and (not defined $username or not defined $password) )
{
  die "Must supply username and password to upload data\n";
}
my $dbcache = $input.".dbcache";
my $db_file;

my $quit_request = 0;

# This signal stuff is to deal with the fact that LWP treats any kind of
# interruption as a read timeout. so we set the signal to restartable to
# avoid that problem.
{
  my $sa = POSIX::SigAction->new( sub { $quit_request = 1}, undef, &POSIX::SA_RESTART );
  sigaction &POSIX::SIGINT, $sa;
  sigaction &POSIX::SIGQUIT, $sa;
  sigaction &POSIX::SIGHUP, $sa;
}

# Switch to utf8 for extract mode
binmode STDOUT, ":utf8";

my $OSM = init Geo::OSM::OsmChangeReader(\&process,\&progress);
my $start_time = time();
my $last_time = 0;
my $spin_delay = 0;  # Estimates cost of skipping a record because it's done
my $uploader = new Geo::OSM::APIClient( api => $api, username => $username, password => $password )
    unless $dry_run or $extract;

if( not $dry_run )
{
  $db_file = new IdMapper::DB_File( $dbcache );
}
else
{
  $db_file = new IdMapper::Dummy;
}

#$OSM->load("data/nld-delete.osm");
my $did_something;
my $delay = 0;   # Delay if get 500 errors...

# On the first iteration, we reset the counters
if( defined $api and (not exists $db_file->{loop} or $db_file->{loop} eq "0") )
{
  $db_file->{loop} = 0;
  $db_file->{total} = 0;
  $db_file->{count} ||= 0;
}
my $skip_count = 0;  # We track skipped nodes, to stop them screwing the ETA
my $done_count = 0;  # Like the count inside the cache, but only counts this execution

print qq(<?xml version="1.0" encoding="UTF-8"?>\n) if $extract;
print qq(<osm version="0.4" generator="bulk_upload.pl">\n) if $extract;

do {
  $did_something = 0;
  $OSM->load($input);
  $db_file->{loop}++ if defined $api;
} while($loop and $did_something == 3);  # We exit if all failed *OR* all succeeded *OR* did nothing

print qq(</osm>\n) if $extract;
print STDERR  "\n";
exit(1) if $did_something >= 2;  # Exiting because something failed
exit(0);  # Otherwise, nothing to do or everything worked

sub process
{
  my($command, $ent) = @_;
  
  exit 3 if $quit_request;
  
  $ent->add_tags(%additional_tags);

  if( not $extract and $db_file->{loop} == 0 )
  { $db_file->{total}++ }
    
  my $skipped;
  ($ent,$skipped) = resolve_ids( $ent, $command );
  if( $extract > 0 )
  {
    if( $skipped < 2 )    # Not done yet...
    {
      print qq(<$command>\n).$ent->xml.qq(</$command>\n);
    }
    return 0;
  }
  if( $skipped )
  {
    $skip_count++;
    print STDERR "Skipped: $command ".$ent->type()." ".$ent->id()."\n"
         if( $verbose and $skipped == 1 );
    return 0;
  }
  $done_count++;
  
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
    print STDERR get_time(), " Error: ".$uploader->last_error_code()." ".$uploader->last_error_message." ($command ".$ent->type()." ".$ent->id().")\n";
    $db_file->{failed}++;
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
    $db_file->mark_done( $ent, $command, $id );
    $did_something |= 1;
    $db_file->{count}++;
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
  return if $extract;
    
#  print "$time == $last_time or $last_time == $start_time\n";
  return if abs($time - $last_time) < 0.5 or $time == $start_time;
  return if $db_file->{total} == 0 or $perc == 0 or $db_file->{count} == 0; # Any of these causes problems
  $last_time = $time;  
  
  my $remain;
  my $elapsed_time = $time - $start_time;
  
  if( $done_count == 0 and $skip_count > 0 )
  { $spin_delay = $elapsed_time / $skip_count }

  if( $db_file->{loop} != 0 )
  {
    # After the first loop we have the actual count of changes in the file
    $perc = $db_file->{count}/$db_file->{total};
    if( $done_count == 0 )
    { $remain = -1 }
    else
    { $remain = (($db_file->{total}-$db_file->{count})*($elapsed_time-$spin_delay*$skip_count)/$done_count) }
  }
  else
  {
    # During the first loop, we only have the percentage of the file done to go on
    # Adjust it for work actually done
    $perc = $perc*$db_file->{count}/$db_file->{total};
    if( ($done_count+$skip_count) < $db_file->{count} )  # While catching up to previous position, no estimate
    { $remain = -1 }
    else
    # Est time per upload * (est records in file - records done)
    { $remain = (($elapsed_time-$spin_delay*$skip_count)/$done_count) * $db_file->{count} * (1/$perc-1) }
  }
  my $remain_str;
  if( $remain <= 0 )
  { $remain_str = "---:--:--" }
  else
  { $remain_str = sprintf "%3d:%02d:%02d", int($remain)/3600, int($remain/60)%60, int($remain)%60 }
  
  $0 = sprintf "bulk_upload  %s  %7.2f%%  ETA:%s  ", $input, $perc*100, $remain_str;
  printf STDERR "Loop: %2d Done:%10d/%10d %7.2f%%  $remain_str  \r", $db_file->{loop},
       $db_file->{count}, $db_file->{total}, $perc*100;
}

# Returns a new object with the IDs resolved according to the mapper
# Returns 0 if everything could be resolved
# Returns 1 if something could not be resolved
# Returns 2 if this object has already been done
sub resolve_ids
{
  my $ent = shift;
  my $command = shift;
  
  return ($ent,0) if $dry_run;
  return ($ent,2) if $db_file->lookup($ent->type, $command, $ent->id);

  my ($new_ent,$incomplete) = $ent->map($db_file);
  if( $command ne "create" and $new_ent->id < 0 )
  { $incomplete = 0 }
  return ($new_ent, $incomplete);
}

sub get_time
{
  my $time = int(time());
  my @a = gmtime($time);
  sprintf("%4d-%02d-%02d %02d:%02d:%02d UTC", $a[5]+1900, $a[4]+1, $a[3], $a[2], $a[1], $a[0]);
}

package IdMapper::DB_File;
use DB_File;
# This is actually far more than just a IdMapper, it also stores the completion of 

sub new
{
  my $class = shift;
  my $dbcache = shift;

  my %db_file;
  # Open the cache file, as DB cache
  tie %db_file, "DB_File", $dbcache, O_CREAT|O_RDWR, 0666, $DB_HASH
    or die "Could not open dbcachefile '$dbcache' ($!)\n";
    
  return bless \%db_file, $class;
}

sub _key
{
  my($key,$command,$id) = @_;
  return substr($key,0,1).substr($command,0,1).$id;
}

sub mark_done
{
  my $db_file = shift;
  my( $ent, $command, $newid ) = @_;

  my $key = _key($ent->type(), $command, $ent->id());
  
  $db_file->{$key} = $newid;
}

sub lookup
{
  my $db_file = shift;
  my( $type, $command, $id ) = @_;

  my $key = _key($type, $command, $id);
  
  return 1 if exists $db_file->{$key};
  return 0;
}

sub map
{
  my ($db_file,$type,$id) = @_;
  my $key = _key($type,"create",$id);
  if( exists $db_file->{$key} )
  { return ($db_file->{$key},0) }
  return ($id, ($id<0) );
}

package IdMapper::Dummy;
sub new
{
  return bless {}, shift;
}

sub mark_done
{
  return;
}

sub map
{
  return (42,0);
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
