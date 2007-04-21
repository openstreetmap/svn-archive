#!/usr/bin/perl
#
# This Perl script extracts a GPX file from a PostgreSQL database
# and uploads it to the OpenStreetMap site via an HTTP POST.
#
# The HTTP POST performs a login with password using a cookie,
# the cookie is stored in a permanent file and eventually reused.
#
# The cookie become invalid if - in the meantime - the same userid
# logs-in via another user agent (browser).
#
# Author:       Niccolo Rigacci <niccolo@rigacci.org>
#
# Version:      1.0     2006-06-06

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

# Database account.
my $DBHOST = '127.0.0.1';
my $DBNAME = 'strade';
my $DBUSER = 'psql_login';
my $DBPASS = 'psql_secret';

# OpenStreetMap account.
my $OSM_USERID = 'osm_login';
my $OSM_PASSWD = 'osm_secret';

# Some TAGs appended to trak upload.
my $OSM_TRACK_TAGS   = 'Italy';
my $OSM_TRACK_PUBLIC = 'on';

my $OSM_LOGIN_PAGE   = 'http://www.openstreetmap.org/login.html';
my $OSM_UPLOAD_TRACK = 'http://www.openstreetmap.org/traces/mine';

my $debug = 0;
my $resp;
my $sql;
my $id_gpxfile;
my $dbh;
my $record;
my $timestamp;
my $tmp_filename;
my $fp;

my $ua;
my $response;
my $cookie = $ENV{HOME} . '/.osm_cookie.txt';
my @stat;

#-------------------------------------------------------------------------
# Get the track ID from the command line.
#-------------------------------------------------------------------------
$id_gpxfile = $ARGV[0];
if (!($id_gpxfile =~ m/^\d+$/)) {
    print "Usage: upload-gpxfile-to-osm <id_gpxfile>\n";
    print "Upload a GPX file extracted from PostgreSQL, to the OpenStreetMap  site.\n";
    exit;
}

#-------------------------------------------------------------------------
# Open the database connection and get the track.
#-------------------------------------------------------------------------
print "Getting track ID $id_gpxfile from database...\n";
$dbh = DBI->connect("dbi:Pg:dbname=$DBNAME;host=$DBHOST", $DBUSER, $DBPASS)
    or die $DBI::errstr;

$sql  = 'SELECT id,descr,date_time,osm_upload,xml_text FROM gpxfiles';
$sql .= ' WHERE id = ' . $id_gpxfile;
$sql .= ' AND osm_upload IS NOT TRUE';
$record = $dbh->selectrow_hashref($sql);
die 'Record not found or file already uploaded. Stopped' if (! $record);
# Close the database connection.
$dbh->disconnect;

print '-' x 50 . "\n";
print 'GPX description: ' . $record->{'descr'}            . "\n";
print 'Timestamp: '       . $record->{'date_time'}        . "\n";
print 'XML text lenght: ' . length($record->{'xml_text'}) . "\n";
print '-' x 50 . "\n";

print "Upload this file to OpenStreetMap? [N/y] ";
$resp = <STDIN>;
chomp($resp);
exit if ($resp ne 'y' and $resp ne 'Y');

# Save GPX to a temporary file.
#
# TODO: Avoid to store all the GPX into a variable (RAM).
#
$timestamp = time2str('%Y-%m-%d', str2time($record->{'date_time'}));
($fp, $tmp_filename) = tempfile($timestamp . '.XXXXX', SUFFIX => '.gpx');
print $fp $record->{'xml_text'};
close($fp);

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
        email  => $OSM_USERID,
        pass   => $OSM_PASSWD,
        action => 'login',
    ]);

    if ($response->status_line ne '302 Found') {
        print $response->content . "\n";
        print "Response: " . $response->status_line . "\n";
        die "Login request failed. Stopped";
    }
}

#-------------------------------------------------------------------------
# Upload the track.
#-------------------------------------------------------------------------
print "Uploading track to $OSM_UPLOAD_TRACK...\n";
$response = $ua->request(POST $OSM_UPLOAD_TRACK,
    Content_Type => 'form-data',
    Content      => [
        gpxfile      => [
            $tmp_filename,
            "${timestamp}.gpx",
            Content_Type => 'application/octet-stream',
        ],
        description  => $record->{'descr'},
        tags         => $OSM_TRACK_TAGS,
        public       => $OSM_TRACK_PUBLIC,
    ]
);

if (! $response->is_success) {
    print $response->content . "\n";
    print "Response: " . $response->status_line . "\n";
    die "Upload request failed. Stopped";
} else {
    if (!($response->content =~ m/$OSM_USERID/)) {
        die "Upload failed? Delete login cookie and try again. Stopped";
    } else {
        print "Upload successful.\n";
    }
}

#-------------------------------------------------------------------------
# Update the database: set "osm_upload" field to TRUE.
#-------------------------------------------------------------------------
print "Setting osm_upload = TRUE for track $id_gpxfile...\n";
$dbh = DBI->connect("dbi:Pg:dbname=$DBNAME;host=$DBHOST", $DBUSER, $DBPASS)
    or die $DBI::errstr;

$sql  = 'UPDATE gpxfiles';
$sql .= ' SET osm_upload = TRUE';
$sql .= ' WHERE id = ' . $id_gpxfile;
$dbh->do($sql);
# Close the database connection.
$dbh->disconnect;

#-------------------------------------------------------------------------
# Remove temporary file.
#-------------------------------------------------------------------------
unlink($tmp_filename) if (! $debug);

