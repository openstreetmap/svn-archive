#! /usr/bin/perl -w

use strict;

use LWP::UserAgent;
use LWP::Debug qw (+);
use XML::Parser;

use open "utf8";


#
#
#
my $URLBASE="http://www.informationfreeway.org/api/0.5";

my $last_id = -1;
my $place_name = "unknown";
my $place_type = "unknown";
my $place_is_in = "unknown";
my $node_line = "";
my @tags = ();


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
        my $ua = LWP::UserAgent->new(keep_alive=>1);
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

    if ($tag eq "node")
    {
        for my $i ( 0 ... $#_)
        {
            # print STDERR " ----- $i : '" . $_[$i] . "' -----\n";
            
            my $key = $_[$i];

            if (($i % 2) == 0)
            {
                $node_line .= " " . $key . "=";

                if ($key eq "id")
                {
                    $last_id = $_[$i + 1];
                }
            }
            else            
            {
                $node_line .= "'" . $key . "'";
            }            
        }
    }
    elsif ($tag eq "tag")
    {
        my $key = $_[1];
        my $val = $_[3];
        
        # print STDERR " --- key = '" . $key . "', val = '" . $val . "'\n";
        if ($key eq "place")
        {
            $place_type = $val;
        }
        elsif ($key eq "name")
        {
            $place_name = $val;
        }
        elsif ($key eq "is_in")
        {
            $place_is_in = $val;
        }
        
        push (@tags, "    <tag k='" . $key . "' v='" . $val . "'/>");
    }
    else
    {
        print STDERR "START - @{$expat->{Context}} \\\\ '$tag' - (@_)\n";
    }
}

sub is_in_end_handler
{
    my $expat = shift;
    my $tag = shift;

    if ($tag eq "node")
    {
        utf8::downgrade($place_type);
        utf8::downgrade($place_name, 1);
        utf8::downgrade($place_is_in, 1);

        chomp($place_type);

        mkdir ("data", "0755");
        mkdir ("data/" . $place_type, "0755");
        my $filename = "data/" . $place_type . "/place-" . $place_type . "-" . $place_is_in  . "-" . $place_name . "-" . $last_id . ".osm";
        # my $filename = "data/" . "place-" . $place_type . "-" . $place_name . "-" . $last_id . ".osm";

        open OUT, "> $filename" || die ("Can't open $filename to write: $!\n");
        binmode(OUT, ":utf8");
        print OUT "<?xml version='1.0' encoding='UTF-8'?>\n";
        print OUT "<osm version='0.5' generator='relationbuilder'>\n";
        print OUT "  <node " . $node_line . ">\n";
        
        foreach my $line (@tags)
        {
            print OUT $line . "\n";
        }
        
        # print OUT "    <tag k='is_in' v='Asia, Turkey' />\n";
        # print OUT "    <tag k='place' v='cityX' />\n";
        # print OUT "    <tag k='name' v='Erzurum' />\n";
        print OUT "  </node>\n";
        print OUT "</osm>\n";

        close (OUT);

        $last_id = -1;
        $node_line = "";

        $place_name = "unknown";
        $place_type = "unknown";
        $place_is_in = "unknown";

        @tags = ();
    }
    elsif ($tag eq "tag")
    {
    }
    else
    {
        print STDERR "END - @{$expat->{Context}} // $tag\n";
    }
}

# Turkey
fetch_place_nodes("turkey-places.xml", "35.8", "42.5", "26.0", "45.0");
create_is_in_hierarchy("turkey-places.xml");

# Cyprus
