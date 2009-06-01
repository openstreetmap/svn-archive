#!/usr/bin/env perl
use feature ':5.10';
use strict;
use warnings;
use GD;

=head1 NAME

generate-heatmap.pl - Make a 2^12 x 2^12 pixel heatmap of the world by looking at the size of z12 tiles

=cut

my $from = 0;
my $to   = 2**12-1;

# Create a new image with each pixel = one z12 tile
my $im = GD::Image->new(2**12, 2**12);

my $file = shift;
say STDERR "Parsing tile size file `$file'...";
my ($hash, $min, $max) = parse_tile_size($file);
say STDERR "Done parsing tile size";

my $hash_num = scalar keys %$hash;
my $median = ((sort { $a <=> $b } values %$hash)[int($hash_num/2)]);

# say STDERR "Min = $min";
# say STDERR "Max = $max";
# say STDERR "Average = " . (int sum(values %$hash)/$hash_num);
# say STDERR "Median = " . ((sort { $a <=> $b } values %$hash)[int($hash_num/2)]);
# say STDERR "Num keys = $hash_num";

my $color;
for my $x ($from .. $to)
{
    say STDERR "Processing $x/" . 2**12 if $x % 100 == 0;

    my $x_key = sprintf "%04d", $x;

    for my $y ($from .. $to)
    {
        my $y_key = sprintf "%04d", $y;
        my $key = $x_key . ',' . $y_key;
        if (exists $hash->{$key}) {
            my $size = $hash->{$key};
            my @rgb = heatmap(warp_size_for_heatmap($size));
            $color = $im->colorResolve(@rgb);
            $im->setPixel($x, $y, $color);
        } else {
            # Black background
            $im->setPixel($x, $y, $im->colorResolve(0,0,0));
        }
    }
}

# make sure we are writing to a binary stream
binmode STDOUT;

# Convert the image to PNG and print it on standard output
print $im->png;

exit 0;

sub parse_tile_size
{
    my ($file) = @_;

    my (%hash, $smallest, $biggest);

    my $do_size = sub {
        my $size = shift;

        if (not defined $biggest or $size > $biggest) {
            $biggest = $size;
        }
        if (not defined $smallest or $size < $smallest) {
            $smallest = $size;
        }
    };

    open my $fh, "<", $file or die "Can't open file $file: $!";
    while (my $line = <$fh>)
    {
        chomp $line;
        my ($tile, $size) = split /\s+/, $line;
        $do_size->($size);
        $hash{$tile} = $size;
    }
    close $fh;

    return (\%hash, $smallest, $biggest);
}

sub warp_size_for_heatmap
{
    my $size = shift;

    # FIXME: This results in a very lousy distribution, the map is all
    # blue
    my $n = $size / ($median * 32);

    $n = 0.95 if $n >= 1;

    return $n;
}

# From http://google.com/codesearch/p?hl=en#v85D9_xn8lk/viewcvs.py/gmod/graphbrowse/cgi/graphbrowse%3Frev%3D1.10&q=heat map rgb lang:perl
sub heatmap {
    my($v)=@_;

    die "Heatmap input out of range: $v" if $v < 0 || $v > 1;

    my @rgb;

    for my $offset (-0.25,0,0.25) {
        my $x = $v + $offset;
        my $y;
        if ($x <= .125) {
            $y = 0;
        } elsif ($x <= .375) {
            $y = ($x-.125)/.25;
        } elsif ($x <= .625) {
            $y = 1;
        } elsif ($x <= .875) {
            $y = (.875 - $x )/.25;
        } else {
            $y = 0;
        }
        push @rgb => $y * 255;
    }
    map { int } @rgb;
}
