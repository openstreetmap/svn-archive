#!/usr/bin/perl
#-------------------------------------------------------------
# OpenStreetMap tiles@home
#-----------------------------------------------------------------------------
# Copyright 2008, Matthias Julius
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
#-----------------------------------------------------------------------------

use lib './lib';
use strict;
use TahConf;
use XML::XPath ();
use CSS;
use File::Spec;
use Getopt::Long qw(GetOptions);
use GD;

#---------------------------------

my $option_output = 'simple';
my $number_colors = 256;
GetOptions("simple"   => sub { $option_output = "simple"; }, 
           "wiki"     => sub { $option_output = "wiki"; },
           "palette"  => sub { $option_output = "palette"; },
           "colors=i" => \$number_colors);

# Read the config file
my $Config = TahConf->getConfig();

die "Usage: $0 <layer> [<layer>]" if not $ARGV[0];

my @layers = @ARGV;

my %color_table = (aliceblue            => "#f0f8ff",
                   antiquewhite         => "#faebd7",
                   aqua                 => "#00ffff",
                   aquamarine           => "#7fffd4",
                   azure                => "#f0ffff",
                   beige                => "#f5f5dc",
                   bisque               => "#ffe4c4",
                   black                => "#000000",
                   blanchedalmond       => "#ffebcd",
                   blue                 => "#0000ff",
                   blueviolet           => "#8a2be2",
                   brown                => "#a52a2a",
                   burlywood            => "#deb887",
                   cadetblue            => "#5f9ea0",
                   chartreuse           => "#7fff00",
                   chocolate            => "#d2691e",
                   coral                => "#ff7f50",
                   cornflowerblue       => "#6495ed",
                   cornsilk             => "#fff8dc",
                   crimson              => "#dc143c",
                   cyan                 => "#00ffff",
                   darkblue             => "#00008b",
                   darkcyan             => "#008b8b",
                   darkgoldenrod        => "#b8840b",
                   darkgray             => "#a9a9a9",
                   darkgreen            => "#006400",
                   darkgrey             => "#a9a9a9",
                   darkkhaki            => "#bdb76b",
                   darkmagenta          => "#8b008b",
                   darkolivegreen       => "#556b2f",
                   darkorange           => "#ff8c00",
                   darkorchid           => "#9932cc",
                   darkred              => "#8b0000",
                   darksalmon           => "#e9967a",
                   darkseagreen         => "#8fbc8f",
                   darkslateblue        => "#483d8b",
                   darkslategray        => "#2f4f4f",
                   darkslategrey        => "#2f4f4f",
                   darkturquoise        => "#00ced1",
                   darkviolet           => "#9400d3",
                   deeppink             => "#ff1493",
                   deepskyblue          => "#00bfff",
                   dimgray              => "#696969",
                   dimgrey              => "#696969",
                   dodgerblue           => "#1e90ff",
                   firebrick            => "#b22222",
                   floralwhite          => "#fffff0",
                   forestgreen          => "#228b22",
                   fuchsia              => "#ff00ff",
                   gainsboro            => "#dcdcdc",
                   ghostwhite           => "#f8f8ff",
                   gold                 => "#ffd700",
                   goldenrod            => "#daa520",
                   gray                 => "#808080",
                   grey                 => "#808080",
                   green                => "#008000",
                   greenyellow          => "#adff2f",
                   honeydew             => "#f0fff0",
                   hotpink              => "#ff69b4",
                   indianred            => "#cd5c5c",
                   indigo               => "#4b0082",
                   ivory                => "#fffff0",
                   khaki                => "#f0e68c",
                   lavender             => "#e6e6fa",
                   lavenderblush        => "#fff0f5",
                   lawngreen            => "#7cfc00",
                   lemonchiffon         => "#fffacd",
                   lightblue            => "#add8e6",
                   lightcoral           => "#f08080",
                   lightcyan            => "#e0ffff",
                   lightgoldenrodyellow => "#fafad2",
                   lightgray            => "#d3d3d3",
                   lightgreen           => "#90ee90",
                   lightgrey            => "#d3d3d3",
                   lightpink            => "#ffb6c1",
                   lightsalmon          => "#ffa07a",
                   lightseagreen        => "#20b2aa",
                   lightskyblue         => "#87cefa",
                   lightslategray       => "#778899",
                   lightslategrey       => "#778899",
                   lightsteelblue       => "#b0c4de",
                   lightyellow          => "#ffffe0",
                   lime                 => "#00ff00",
                   limegreen            => "#32cd32",
                   linen                => "#faf0e6",
                   magenta              => "#ff00ff",
                   maroon               => "#800000",
                   mediumaquamarine     => "#66cdaa",
                   mediumblue           => "#0000cd",
                   mediumorchid         => "#ba55d3",
                   mediumpurple         => "#9370db",
                   mediumseagreen       => "#3cb371",
                   mediumslateblue      => "#7b68ee",
                   mediumspringgreen    => "#00fa9a",
                   mediumturquoise      => "#48d1cc",
                   mediumvioletred      => "#c71585",
                   midnightblue         => "#191970",
                   mintcream            => "#f5fffa",
                   mistyrose            => "#ffe4e1",
                   moccasin             => "#ffe4b5",
                   navajowhite          => "#ffdead",
                   navy                 => "#000080",
                   oldlace              => "#fdf5e6",
                   olive                => "#808000",
                   olivedrab            => "#6b8e23",
                   orange               => "#ffa500",
                   orangered            => "#ff4500",
                   orchid               => "#da70d6",
                   palegoldenrod        => "#eee8aa",
                   palegreen            => "#98fb98",
                   paleturquoise        => "#afeeee",
                   palevioletred        => "#db7093",
                   papayawhip           => "#ffefd5",
                   peachpuff            => "#ffdab9",
                   peru                 => "#cd853f",
                   pink                 => "#ffc0cb",
                   plum                 => "#dda0cb",
                   powderblue           => "#b0e0e6",
                   purple               => "#800080",
                   red                  => "#ff0000",
                   rosybrown            => "#bc8f8f",
                   royalblue            => "#4169e1",
                   saddlebrown          => "#8b4513",
                   salmon               => "#fa8072",
                   sandybrown           => "#f4a460",
                   seagreen             => "#2e8b57",
                   seashell             => "#fff5ee",
                   sienna               => "#a0522d",
                   silver               => "#c0c0c0",
                   skyblue              => "#87ceeb",
                   slateblue            => "#6a5acd",
                   slategray            => "#778090",
                   slategrey            => "#778090",
                   snow                 => "#fffffa",
                   springgreen          => "#00ff7f",
                   steelblue            => "#4682b4",
                   tan                  => "#d2b48c",
                   teal                 => "#008080",
                   thistle              => "#d8bfd8",
                   tomato               => "#ff6347",
                   turquoise            => "#40e0d0",
                   violet               => "#ee82ee",
                   wheat                => "#f5deb3",
                   white                => "#ffffff",
                   whitesmoke           => "#f5f5f5",
                   yellow               => "#ffff00",
                   yellowgreen          => "#9acd32");
my %color_table_reverse;
foreach my $key (sort(keys(%color_table))) {
    $color_table_reverse{$color_table{$key}} = $key;
}

my $color;
my %colors;
my %include_files;

foreach my $layer (@layers) {
    my $min_zoom = $Config->get("${layer}_MinZoom");
    my $max_zoom = $Config->get("${layer}_MaxZoom");

    for (my $zoom = $min_zoom; $zoom <= $max_zoom; $zoom++) {
        my $rules_file = $Config->get("${layer}_Rules.$zoom");
        my (undef, $rules_dir, undef) = File::Spec->splitpath($rules_file);
        my $rules = XML::XPath->new(filename => $rules_file);

        my $style_sheets = $rules->find("/rules/defs/style");

        foreach my $style_sheet ($style_sheets->get_nodelist) {
            next if (! $style_sheet->getAttribute("type") eq "text/css");

            my $css = CSS->new;
            $css->read_string($style_sheet->string_value);

            foreach my $style (@{$css->{styles}}) {
                foreach my $properties ($style->properties) {
                    foreach my $property (split(/;/, $properties)) {
                        if ($property =~ /^\s*(fill|stroke):\s*(#[0-9a-f]+|\w+)\s*/i) {
                            next if ($2 eq "none");
                            next if ($2 eq "url");
                            add_color($2) if ($2);
                        }
                    }
                }
            }
        }

        my @svg_elements = $rules->findnodes("/*/*/svg:*");
        find_colors(@svg_elements);

        my @includes = $rules->findnodes("/rules/include");
        foreach my $include (@includes) {
            $include_files{File::Spec->join($rules_dir, $include->getAttribute("ref"))}++;
        }
    }
}

my $symbol_dir = File::Spec->join("osmarender", "symbols");
if (opendir(SYMBOLDIR, $symbol_dir)) {
    my @symbol_files = grep { /\.svg$/i } readdir(SYMBOLDIR);
    closedir(SYMBOLDIR);
    foreach my $symbol_file (@symbol_files) {
        $include_files{File::Spec->join($symbol_dir, $symbol_file)}++;
    }
}

foreach my $include_file (sort(keys(%include_files))) {
    my $include_xml = XML::XPath->new(filename => $include_file);
    my @svg_elements = $include_xml->findnodes("/svg/*");
    find_colors(@svg_elements);
}

if ($option_output eq 'simple') {
    output_simple();
}
elsif ($option_output eq 'wiki') {
    output_wiki();
}
elsif ($option_output eq 'palette') {
    output_palette();
}

#################################################
sub output_simple
{
    my $i = 0;
    foreach my $key (sort_colors()) {
        $i++;
        print "$i - $key" . ((defined($color_table_reverse{$key})) ? " (" . $color_table_reverse{$key} . ")" : "") . ": " . $colors{$key} . "\n";
    }
}

sub output_wiki
{
    my $i = 0;
    foreach my $color (sort_colors()) {
        $i++;
        print "| $i || style='background:$color;' | $color || $color " . ((defined($color_table_reverse{$color})) ? " (" . $color_table_reverse{$color} . ")" : "") . " || " . $colors{$color} . "\n";
        print "|-\n";
    }
}

sub output_palette
{
    my @colors = sort_colors();
    my $image_size = 16;
    my $image = GD::Image->new($image_size, $image_size, 0);

    for (my $y = 0; $y < $image_size; $y++) {
        for (my $x = 0; $x < $image_size; $x++) {
            my $i = $x + $image_size * $y;

            last if (!defined($colors[$i]));

            $colors[$i] =~ /#(..)(..)(..)/i;
            my @rgb = (hex($1), hex($2), hex($3));
            my $index = $image->colorAllocate(@rgb);
            $image->setPixel($x, $y, $index);
            print "$i ($x, $y): ${colors[$i]} - @rgb - $index\n";
        }
    }

    my $png_data = $image->png();

    foreach my $layer (@layers) {
        my $png_file = "palette_$layer.png";

        open(PNGFILE, "> $png_file") or die $!;
        binmode(PNGFILE);
        print PNGFILE $png_data;
    }
}

sub sort_colors
{
    my @colors;
    foreach my $key (sort(keys(%colors))) {
        push(@colors, $key) if ($key =~ /#([0-9a-f]{2})\1\1/i);
    }
    foreach my $key (sort(keys(%colors))) {
        push(@colors, $key) if ($key !~ /#([0-9a-f]{2})\1\1/i);
    }
    return @colors;
}

sub add_color
{
    $color = lc(shift);
    if ($color !~ /^#/) {
        $color = $color_table{$color};
    }
    $colors{$color}++;
}

sub find_colors
{
    my @nodes = @_;
    foreach my $node (@nodes) {
        my $color = $node->getAttribute("fill");
        add_color($color) if ($color and ($color ne "none"));
        $color =  $node->getAttribute("stroke");
        add_color($color) if ($color and ($color ne "none"));

        my @child_nodes = $node->getChildNodes;
        find_colors(@child_nodes) if (@child_nodes);
    }
}
