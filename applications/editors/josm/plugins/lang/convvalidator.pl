#! /usr/bin/perl -w

# Written by Dirk Stöcker <openstreetmap@dstoecker.de>
# Public domain, no rights reserved.

use strict;

my $item;
my $comment = 0;

# This is a simple conversion and in no way a complete XML parser
# but it works with a default Perl installation

while(my $line = <>)
{
  chomp($line);
  if($line =~ /^#(.*)$/)
  {
    print "//$1\n";
  }
  elsif($line =~ /^$/)
  {
    print "\n";
  }
  elsif($line =~ /(.*) *# *(.*) *$/)
  {
    print "tr(\"$2\") // $1\n";
  }
  else
  {
    print "/* $line */\n";
  }
}
