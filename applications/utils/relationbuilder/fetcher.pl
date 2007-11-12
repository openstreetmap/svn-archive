#! /usr/bin/perl -w

use strict;

use LWP::UserAgent;
use XML::Parser;


#
#
#
my $URLBASE="http://www.informationfreeway.org/api/0.5";


#
#
#
sub fetch_place_nodes
{
    my ($datafile, $min_lat, $max_lat, $min_lon, $max_lon) = @_;

    my $URL=$URLBASE . "/node[place=*][bbox=$min_lon,$min_lat,$max_lon,$max_lat]";

    print $URL . "\n";

    # Primitive caching... Just so I can continue working without network access...
    if (! -r $datafile)
    {
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
}


#
#
#
sub create_is_in_hierarchy
{
    my ($datafile) = @_;
    
    my $parser = new XML::Parser (Handlers =>
                                    {
                                        Start => \&is_in_start_handler,
                                        End   => \&is_in_end_handler
                                    }
                                  );

    $parser->parsefile($datafile); # , ProtocolEncoding => 'UTF-16');
}

sub is_in_start_handler
{
    my $expat = shift;
    my $tag = shift;

    print STDERR "START - @{$expat->{Context}} \\\\ '$tag' - (@_)\n";
}

sub is_in_end_handler
{
    my $expat = shift;
    my $tag = shift;

    print STDERR "END - @{$expat->{Context}} // $tag\n";
}

# Turkey
fetch_place_nodes("turkey-places.xml", "35.8", "42.5", "26.0", "45.0");
create_is_in_hierarchy("turkey-places.xml");

# Cyprus
