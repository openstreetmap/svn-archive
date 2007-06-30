#!/usr/bin/perl
use LWP::UserAgent;
use strict;

# OSM downloader
#
# for those moments when you want more than you can get!
#
# splits an area into stripes, downloads them, recombines results
# number of stripes currently hardcoded (see num_slices below)
#
# written by Frederik Ramm <frederik@remote.org>, re-using code
# contributed to tilesGen.pl by F.R.
#
# public domain

my $request = $ARGV[0];
my $num_slices = 5;

if ($request !~ m!http://([^/]+)/api/([0-9.]+)/map\?bbox=([0-9.]+),([0-9.]+),([0-9.]+),([0-9.]+)!)
{
    print STDERR <<EOF;
OSM download utility

Partitions an area into slices, downloads them, and patches them together
again. Warning: The resulting file does not have the usual "nodes, then
segments, then ways" order.

usage (example):
perl $0 http://www.openstreetmap.org/api/0.4/map?bbox=1.0,40.0,2.0,39.0
(takes the same syntax you'd use for wget etc. - tip: if you've tried
to download the area using JOSM, then you'll find that string in the 
console output!)

EOF
    exit;
}
my ($server, $version, $W, $S, $E, $N) = ($1, $2, $3, $4, $5, $6);

my $existing = {};
my $copyheader = 1;
my $tmpfile = "osmdownload.$$.tmp";
my $ua = LWP::UserAgent->new(keep_alive => 1, timeout => 300);
$ua->agent("tilesAtHome");
$ua->env_proxy();

my $slice=(($E-$W)/$num_slices); 

for (my $i = 0 ; $i<$num_slices ; $i++) 
{
    my $url = sprintf("http://%s/api/%s/map?bbox=%f,%f,%f,%f",
       $server, $version, ($W+($slice*$i)), $S, ($W+($slice*($i+1))), $N);
    print STDERR $url . "... ";
    my $response = $ua->get($url, ":content_file" => $tmpfile);
    print STDERR $response->status_line . "\n";
    if (!$response->is_success())
    {
        unlink $tmpfile;
        die;
    }
    merge($tmpfile);
}
unlink($tmpfile);
print "</osm>\n";
        
sub merge()
{
    my $source = shift;
    open(SOURCE, $source);
    my $copy = 0;
    if ($copyheader)
    {
        $copy = 1;
        $copyheader = 0;
    }
    while(<SOURCE>)
    {
        if ($copy)
        {
            last if (/^\s*<\/osm>/);
            if (/^\s*<(node|segment|way) id="(\d+)".*(.)>/)
            {
                my ($what, $id, $slash) = ($1, $2, $3);
                my $key = substr($what, 0, 1) . $id;
                if (defined($existing->{$key}))
                {
                    # object exists already. skip!
                    next if ($slash eq "/");
                    while(<SOURCE>)
                    {
                        last if (/^\s*<\/$what>/);
                    }
                    next;
                }
                else
                {
                    # object didn't exist, note
                    $existing->{$key} = 1;
                }
            }
            print;
        }
        else
        {
            $copy = 1 if (/^\s*<osm /);
        }
    }
    close(SOURCE);
}
