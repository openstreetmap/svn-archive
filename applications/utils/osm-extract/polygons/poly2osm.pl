#!/usr/bin/perl

# script to convert a polygon file to an OSM file for
# editing in JOSM etc.
# the polygon file is expected to have the structure
# 
# line 1:     symbolic name
# line 2:     id of first polygon
# lines 3-n:  coordinates of first polygon, each line beginning with 
#             whitespace, then having lon and lat of the point in scientific notation
# line n+1:   END
# the lines 2 to n+1 may then be repeated for further polygons, and the last line must be 
# END (so that the file ends with 2 lines having "END").
#
# written by Frederik Ramm <frederik@remote.org>, public domain.

my %nodehash;

# first line
# (employ workaround for polygon files without initial text line)
my $poly_file = <>; chomp($poly_file);
my $workaround = 0;
if ($poly_file =~ /^\d+$/)
{
    $workaround=$poly_file;
    $poly_file="none";
}

my $nodecnt = -1;
my $waycnt = -1;

my $nodes;
my $ways;
my $note = "    <tag k='note' v='created by poly2osm.pl from a polygon file. not for uploading!' />\n";

while(process_poly()) { undef $workaround; };
print "<osm generator='osm2poly.pl' version='0.5'>\n";
print $nodes;
print $ways;
print "</osm>\n";

sub process_poly()
{
    my $poly_id = (defined($workaround)) ? $workaround : <>; chomp($poly_id);
    my $startnode = $nodecnt;
    return 0 if ($poly_id =~ /^END/); # end of file

    $ways .= sprintf("  <way id='%d'>\n    <tag k='polygon_id' v='%d' />\n    <tag k='polygon_file' v='%s' />\n",
        $waycnt--, $poly_id, $poly_file);
    $ways .= $note;

    while($line = <>)
    {
        last if ($line =~ /^END/); # end of poly
        my ($dummy, $x, $y) = split(/\s+/, $line);
        my $existingnode = $nodehash{"$x|$y"};
        if (defined($existingnode))
        {
            $ways .= sprintf("    <nd ref='%d' />\n", $existingnode);
        }
        else
        {
            $nodehash{"$x|$y"} = $nodecnt;
            $ways .= sprintf("    <nd ref='%d' />\n", $nodecnt);
            $nodes .= sprintf("  <node id='%d' lat='%f' lon='%f' />\n", $nodecnt--, $y, $x);
        }
    }
    $ways .= "  </way>\n";
    return 1;
}
