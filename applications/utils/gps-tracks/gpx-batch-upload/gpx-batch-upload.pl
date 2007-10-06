#!/usr/bin/perl

# This Perl script extracts a GPX file from a PostgreSQL database         #
# and uploads it to the OpenStreetMap site via an HTTP POST.              #
#                                                                         #
# The HTTP POST performs a login with password using a cookie,            #
# the cookie is stored in a permanent file and eventually reused.         #
#                                                                         #
# Author:       Niccolo Rigacci <niccolo@rigacci.org>                     #
#                                                                         #
# Version:      2.2     2007-10-05                                        #
#                                                                         #
#   This program is free software; you can redistribute it and/or modify  #
#   it under the terms of the GNU General Public License as published by  #
#   the Free Software Foundation; either version 2 of the License, or     #
#   (at your option) any later version.                                   #
#                                                                         #
#   This program is distributed in the hope that it will be useful,       #
#   but WITHOUT ANY WARRANTY; without even the implied warranty of        #
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         #
#   GNU General Public License for more details.                          #
#                                                                         #
#   You should have received a copy of the GNU General Public License     #
#   along with this program; if not, write to the                         #
#   Free Software Foundation, Inc.,                                       #
#   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             #

use strict;
# Requires Debian package libdbd-pg-perl. See man DBD::Pg
use DBI;
# Requires Debian package libwww-perl.
use LWP::UserAgent;
use HTTP::Request::Common;
# Install the libtimedate-perl Debian package.
use Date::Parse;
use Date::Format;
# Require Debian package perl-modules.
use File::Temp qw/ tempfile tempdir /;
use Getopt::Std;
use vars qw($opt_i $opt_d $opt_e $opt_p $opt_t $opt_u);

# Read database credential from file...
my $DBINFO = '/usr/local/lib/strade/dbinfo';
# ... or define them here.
my $DBHOST;
my $DBNAME;
my $DBUSER;
my $DBPASS;

my $OSM_LOGIN_PAGE   = 'http://www.openstreetmap.org/login.html';
my $OSM_UPLOAD_TRACK = 'http://www.openstreetmap.org/trace/create';

my $TMP_DIR = '/tmp';
my $NAME    = `basename "$0"`;
chomp($NAME);

my $debug = 0;
my $resp;
my $sql;
my $dbh;
my $record;
my $timestamp;
my $tmp_filename;
my $fp;

my $ua;
my $response;
my $hex_string;
my $cookie;
my @stat;
my $original_filename;

#-------------------------------------------------------------------------
# Get the track ID from the command line.
#-------------------------------------------------------------------------
if (! getopts('i:d:e:p:t:u:')) {
    print "Usage: $NAME -i idgpx -e email -p pass -d description -t tags -u public\n\n";
    print "Upload a GPX file extracted from PostgreSQL, to the OpenStreetMap site.\n";
    exit 0;
}

die('Invalid GPX ID')      if (!($opt_i =~ m/^\d+$/));
die('Invalid OSM email')   if (!($opt_e =~ m/^[\@\.0-9A-Za-z_-]{1,50}$/));
die('Invalid OSM pass')    if (!($opt_p =~ m/^.{1,50}$/));
die('Invalid description') if (!($opt_d =~ m/^.{0,255}$/));
die('Invalid tags')        if (!($opt_p =~ m/^.{0,255}$/));
die('Invalid public')      if ($opt_u ne '' and $opt_u ne 'on');

# Get login and password for DB access.
read_db_info();

# Temporary file for cookie.
$hex_string = $opt_e;
$hex_string =~ s/(.)/sprintf('%02X', ord($1))/seg;
$cookie = $TMP_DIR . '/.osm_cookie_' . $hex_string . '.txt';

#-------------------------------------------------------------------------
# Open the database connection and get the track.
#-------------------------------------------------------------------------
print "Getting track ID $opt_i from database...\n";
$dbh = DBI->connect("dbi:Pg:dbname=$DBNAME;host=$DBHOST", $DBUSER, $DBPASS)
    or die $DBI::errstr;

$sql  = 'SELECT id,descr,date_time,osm_upload,xml_text,filename FROM gpxfiles';
$sql .= ' WHERE id = ' . $opt_i;
$sql .= ' AND osm_upload IS NOT TRUE';
$record = $dbh->selectrow_hashref($sql);
die 'Record not found or file already uploaded. Stopped' if (! $record);
# Close the database connection.
$dbh->disconnect;

# Save GPX to a temporary file.
#
# TODO: Avoid to store all the GPX into a variable (RAM).
#
$timestamp = time2str('%Y-%m-%d', str2time($record->{'date_time'}));
($fp, $tmp_filename) = tempfile($timestamp . '.XXXXX', DIR => $TMP_DIR, SUFFIX => '.gpx');
print $fp $record->{'xml_text'};
close($fp);

# Get the original GPX filename, or compose a suitable one.
if (!(defined($record->{'filename'})) or $record->{'filename'} eq '') {
    $original_filename = $timestamp . '.gpx';
} else {
    $original_filename = $record->{'filename'};
}

# Description for OSM is from command line or from database.
$opt_d = $record->{'descr'} if (! defined($opt_d));

print '-' x 50 . "\n";
print 'DB description: '     . $record->{'descr'}            . "\n";
print 'DB timestamp: '       . $record->{'date_time'}        . "\n";
print 'DB XML text lenght: ' . length($record->{'xml_text'}) . "\n";
print '-' x 50 . "\n";
#print "Tempoaray file for cookie: $cookie\n";
#print "Tempoaray file for GPX: $tmp_filename\n";
#print '-' x 50 . "\n";
print "OSM filename: $original_filename\n";
print "OSM description: $opt_d\n";
print "OSM tags: $opt_t\n";
print "OSM public: $opt_u\n";
print '-' x 50 . "\n";

#-------------------------------------------------------------------------
# Begin WEB transactions.
#-------------------------------------------------------------------------
$ua = LWP::UserAgent->new;

#-------------------------------------------------------------------------
# Login to OpenStreetMap Editor.
#-------------------------------------------------------------------------
print "Logging into $OSM_LOGIN_PAGE...\n";

# Remove the saved cookie, if it is too old.
if (-f $cookie) {
    @stat = stat($cookie);
    if ((time() - $stat[9]) > 600) {
        print "Removing old file: $cookie\n";
        unlink($cookie);
    } else {
        print "Using existing cookie from file: $cookie\n";
    }
}

# Save the login cookie into a permanent file.
$ua->cookie_jar({
    file => $cookie,
    autosave => 1,
    ignore_discard => 1,
});

if (! -f $cookie) {
    print "Saving cookie to $cookie\n";
    $response = $ua->request(POST $OSM_LOGIN_PAGE, [
        'user[email]'    => $opt_e,
        'user[password]' => $opt_p,
        commit           => 'Login',
    ]);

    if ($response->status_line ne '302 Found') {
        print $response->content . "\n";
        print "Response: " . $response->status_line . "\n";
        my_die("Login request failed.");
    }
}

#-------------------------------------------------------------------------
# Upload the track.
#-------------------------------------------------------------------------
print "Uploading track to $OSM_UPLOAD_TRACK...\n";
$response = $ua->request(POST $OSM_UPLOAD_TRACK,
    Content_Type => 'form-data',
    Content      => [
        "trace[gpx_file]"    => [
            $tmp_filename,
            $original_filename,
            Content_Type => 'application/octet-stream',
        ],
        "trace[description]" => $opt_d,
        "trace[tagstring]"   => $opt_t,
        "trace[public]"      => $opt_u,
    ]
);

# A success or redirect response is OK.
if (($response->is_redirect) and ($response->content =~ m|/traces/mine|)) {
    print "Status line: " . $response->status_line . "\n";
    print "Upload successful.\n";
} elsif ($response->is_success) {
    print "Upload successful.\n";
} else {
    print "Status line: " . $response->status_line . "\n";
    print "Content:     " . $response->content     . "\n";
    my_die("Upload request failed. Delete login cookie and try again.");
}

#-------------------------------------------------------------------------
# Update the database: set "osm_upload" field to TRUE.
#-------------------------------------------------------------------------
print "Setting osm_upload = TRUE for track $opt_i...\n";
$dbh = DBI->connect("dbi:Pg:dbname=$DBNAME;host=$DBHOST", $DBUSER, $DBPASS)
    or die $DBI::errstr;

$sql  = 'UPDATE gpxfiles';
$sql .= ' SET osm_upload = TRUE';
$sql .= ' WHERE id = ' . $opt_i;
$dbh->do($sql);
# Close the database connection.
$dbh->disconnect;

#-------------------------------------------------------------------------
# Remove temporary file.
#-------------------------------------------------------------------------
unlink($tmp_filename) if (! $debug);

exit(0);

#-------------------------------------------------------------------------
# Print an error message, remove temporary file and die.
#-------------------------------------------------------------------------
sub my_die {
    my $msg = shift;
    print "\n" . $msg . "\n";
    unlink($tmp_filename) if (defined($tmp_filename) and -f $tmp_filename);
    exit(1);
}

#-------------------------------------------------------------------------
# Read database account from file.
#-------------------------------------------------------------------------
sub read_db_info {
    my $fp;
    my $line;
    open ($fp, $DBINFO) or die;
    while ($line = <$fp>) {
        chomp($line);
        if ($line =~ m/^\s*([^#][^=]*)=(.*)/) {
            if    ($1 eq 'DBHOST') { $DBHOST = $2; }
            elsif ($1 eq 'DBNAME') { $DBNAME = $2; }
            elsif ($1 eq 'DBUSER') { $DBUSER = $2; }
            elsif ($1 eq 'DBPASS') { $DBPASS = $2; }
        }
    }
    close($fp);
}

