#! /usr/bin/perl -w

use strict;

use LWP::UserAgent;

#
#
#
my $URLBASE="http://www.informationfreeway.org/api/0.5";


#
#
#
sub fetch_nodes
{
    my ($datafile, $min_lat, $max_lat, $min_lon, $max_lon) = @_;

    my $URL=$URLBASE . "/node[place=*][bbox=$min_lon,$min_lat,$max_lon,$max_lat]";

    print $URL . "\n";

    my $ua = LWP::UserAgent->new;
    $ua->env_proxy();
    $ua->agent("relationbuilder/0.1");

    my $request = HTTP::Request->new(GET => $URL);
    my $response = $ua->request($request);

    if ($response->is_success)
    {
        open OUT, "> $datafile";
        print OUT $response->content . "\n";
        close (OUT);

        print $response->status_line . "\n";
    }
    else
    {
        print $response->status_line . "\n";
    }
}

# Turkey
fetch_nodes("turkey-places.osm", "35.8", "42.5", "26.0", "45.0");

# Cyprus
