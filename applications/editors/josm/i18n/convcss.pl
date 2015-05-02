#! /usr/bin/perl -w

# Written by Dirk Stöcker <openstreetmap@dstoecker.de>
# Public domain, no rights reserved.

use strict;

my $skipmore = 0;
while(my $line = <>)
{
  $skipmore = 1 if $line =~ /meta\[lang/;
  $line =~ s/((?:title|description): +)(.*)(;)/$1tr($2)$3/ if !$skipmore;
  print $line;
}
