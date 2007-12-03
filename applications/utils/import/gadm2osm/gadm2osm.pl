#!/usr/bin/perl


use strict;

use Geo::ShapeFile;
use Data::Dumper;

#
#   Bounding box for Turkey
#
our $min_lat = 35.5;
our $max_lat = 42.2;
our $min_lon = 26.0;
our $max_lon = 45.0;


#
#
#
sub read_shapes
{
    my ($filename) = @_;

    my (%shapes, $key);

    my $shapeFile = new Geo::ShapeFile($filename);
    print $shapeFile->shapes() . " shapes in " . $filename . "\n";

    for (1..$shapeFile->shapes ())
    {
        my %db = $shapeFile->get_dbf_record ($_);

        # if ($db{"ISO"} eq "TUR")
        {
            $key = $db{"ISO"} . "-" . $db{"NAME_1"};
            $shapes{$key} = [] unless (defined $shapes{$key});
            push (@{$shapes{$key}}, $_);
        }
    }

    foreach $key (sort keys %shapes)
    {
        print $key . " : " . Dumper($shapes{$key}) . "\n";
        
        my $filename = "osm/" . $key . ".osm";        

        mkdir("osm", 0755);
        open (OUT, "> $filename");

        # Output header
        print OUT "<?xml version='1.0' encoding='UTF-8'?>\n";
        print OUT "<osm version='0.5' generator='gadm2osm.pl'>\n";

        my $cnt=1;

        my $node_id = -1;

        foreach my $shapeid(@{$shapes{$key}}) 
        {
            my %db = $shapeFile->get_dbf_record($shapeid);
            print $shapeid . " : " . Dumper(\%db) . "\n";

            my $shape = $shapeFile->get_shp_record ($shapeid);
            for(1 .. $shape->num_parts) 
            {
                # shape kann mehrere parts haben, auf keinen fall einfach
                # $shape->getpoints machen!

                my @part = $shape->get_part($_);
                my @polypoints;

                my $node_id_start = $node_id;
                my $node_id_end = $node_id;

                print "Block $cnt has " . @part . " parts\n";

                print OUT "\n";
                printf OUT "<!-- Block %d -->\n", $cnt++;
                
                
                #
                #   Save the nodes
                #
                foreach my $pt(@part)
                {
                    print OUT "<node id='" . $node_id . "' lat='" . $pt->Y() . "' lon='" . $pt->X() . "' />\n";
                    $node_id_end = $node_id;
                    $node_id--;
                }

                #
                #   Now, save the way
                #
                my $way_id = $node_id;
                $node_id--;

                print OUT "\n";
                print OUT "<way id='" . $way_id . "' >\n";

                for (my $node_ref = $node_id_start; $node_ref >= $node_id_end; $node_ref--)
                {
                    print OUT "  <nd ref='" . $node_ref . "' />\n";
                }

                print OUT "  <tag k=\"name\" v=\"Border ???? - ????\" />\n";
                print OUT "  <tag k=\"boundary\" v=\"administrative\" />\n";
                print OUT "  <tag k=\"border_type\" v=\"city\" />\n";
                print OUT "  <tag k=\"admin_level\" v=\"10\" />\n";
                print OUT "  <tag k=\"left:city\" v=\"????\" />\n";
                print OUT "  <tag k=\"right:city\" v=\"????\" />\n";

                print OUT "  <tag k=\"source\" v=\"GADM - http://biogeo.berkeley.edu/gadm/\" />\n";
                print OUT "  <tag k=\"gadm:source\" v=\"" . $db{"ISO"} . " - " . $db{"HASC_1"} . "\" />\n";

                print OUT "</way>\n";
            }
        }

        print OUT "\n";
        print OUT "</osm>";
        close(OUT);
    }
}


#
#
#
sub create_osm_file
{
    
}


#
#   Run that script...
#

# read_shapes("data/world_bnd_m");
# read_shapes("data/world_boundaries_m");
# read_shapes("data/GADM_v0-6");

# read_shapes("data/TUR0");
read_shapes("data/TUR1");

# read_shapes("data/TUR_water_areas_dcw");
# read_shapes("data/TUR_water_lines_dcw");
