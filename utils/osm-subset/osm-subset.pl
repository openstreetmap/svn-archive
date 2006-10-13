#!/usr/bin/env perl

my ( $minlon, $minlat, $maxlon, $maxlat ) = map $_ + 0.0, splice @ARGV, 0, 4;

my ( %nodes, %segments );

print <<EOF;
<?xml version="1.0"?>
<osm version="0.3" generator="subset.pl">
EOF

my $accum = undef;
my $print = 0;
while (<>) {
    if (/^    /) {
        if ($print) {
            print;
        }
        elsif ( defined $accum ) {
            my ($segid) = m/ id="(\d+)"/;
            if ( $segid && $segments{$segid} ) {
                print $$accum, $_;
                $accum = undef;
                $print = 1;
            }
            else {
                $$accum .= $_;
            }
        }
    }
    else {
        $accum = undef;
        if (m#^  </#) {
            print if $print;
        }
        elsif (/^  /) {
            $print = 0;
            my ($id) = m/ id="(\d+)"/;
            if (/^  <node /) {
                my ( $lat, $lon ) = m/ lat="(.*?)" lon="(.*?)"/;
                if (   $minlon < $lon
                    && $lon < $maxlon
                    && $minlat < $lat
                    && $lat < $maxlat )
                {
                    $nodes{$id} = 1;
                    $print = 1;
                }
            }
            elsif (/<segment /) {
                my ( $from, $to ) = m/ from="(.*?)" to="(.*?)"/;
                if ( $nodes{$from} && $nodes{$to} ) {
                    $segments{$id} = 1;
                    $print = 1;
                }
            }
            elsif ( /<way / || /<area / ) {
                $accum = \"$_";
            }
	    print if $print;
        }
    }
}

print <<EOF;
</osm>
EOF
