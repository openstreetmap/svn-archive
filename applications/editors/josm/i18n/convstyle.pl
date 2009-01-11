#! /usr/bin/perl -w

# Written by Dirk Stöcker <openstreetmap@dstoecker.de>
# Public domain, no rights reserved.

use strict;

# This is a simple conversion and in no way a complete XML parser
# but it works with a default Perl installation

while(my $line = <>)
{
  chomp($line);
  if($line =~ /<rules\s+name=(".*?")/)
  {
    print "tr($1) /* mappaint style named $1 */\n";
  }
  elsif($line =~ /colour="([^"]*)#/)
  {
    print "tr(\"$1\") /* color $1 */\n";
  }
  else
  {
    print "/* $line */\n";
  }
}
