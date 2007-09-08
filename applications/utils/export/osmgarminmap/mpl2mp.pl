#!/usr/bin/perl
#
#  mpl2mp.pl
#

while (<>) {
    if (/Data0/) {
        s/\(([0-9.]+,[0-9.]+)\),\(\1\)/\(\1\)/g;
    }
    print;
}

