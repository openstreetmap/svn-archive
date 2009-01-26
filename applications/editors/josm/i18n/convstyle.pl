#! /usr/bin/perl -w

# Written by Dirk Stöcker <openstreetmap@dstoecker.de>
# Public domain, no rights reserved.

use strict;

# This is a simple conversion and in no way a complete XML parser
# but it works with a default Perl installation

# Print a header to write valid Java code.  No line break,
# so that the input and output line numbers will match.
print "class trans_style { void tr(String s){} void f() {";

while(my $line = <>)
{
  chomp($line);
  if($line =~ /<rules\s+name=(".*?")/)
  {
    print "tr($1); /* mappaint style named $1 */\n";
  }
  elsif($line =~ /colour="([^"]+)#/)
  {
    print "tr(\"$1\"); /* color $1 */\n";
  }
  else
  {
    print "/* $line */\n";
  }
}

print "}}\n";
