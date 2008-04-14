#!/usr/bin/perl

# simple script that changes attribution by transformin the "user" attributes
# found in the input file.
#
# user=foo becomes user=&#169; foo
# unless "foo" is found in attribution.txt, in which case anything entered there
# will be used.

use strict;
my %mapping;

open(ATT, "attribution.txt") or die;
while(<ATT>)
{
    next if (/^#/);
    chomp;
    my ($old, $new) = split(/=/);
    $mapping{$old} = ($new eq "") ? " " : $new;
}
close(ATT);

while(<>)
{
    chomp;
    while(/^(.*?)\s+user="([^"]+)"(.*)/g)
    {
        print $1;
        printf ' user="%s"', defined($mapping{$2}) ? $mapping{$2} : "&#169; $2";
        $_ = $3;
    }
    print "$_\n";
}
