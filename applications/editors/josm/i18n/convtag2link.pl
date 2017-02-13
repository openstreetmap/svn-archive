#! /usr/bin/perl -w

# Written by Dirk Stöcker <openstreetmap@dstoecker.de>
# Public domain, no rights reserved.

use strict;

my $item;
my $src = "";
my $country = "";
my $comment = 0;

# This is a simple conversion and in no way a complete XML parser
# but it works with a default Perl installation

# Print a header to write valid Java code.  No line break,
# so that the input and output line numbers will match.
print "class trans_tag2link { void tr(String s){} void f() {";

while(my $line = <>)
{
  chomp($line);
  print "tr(\"---DUMMY-MARKER---\"); ";
  if($line =~ /<link name="([^"]+)" /)
  {
    print "tr(\"$1\") /* src $src country code $country */\n";
  }
  elsif($line =~ /^$/)
  {
    print "\n";
  }
  else
  {
    if($line =~ /<src name="([^"]+)"  country-code="([^"]+)"/)
    {
      $src = $1; $country = $2;
    }
    elsif($line =~ /<\/src/)
    {
      $src = ""; $country = ""
    }
    print "/* $line */\n";
  }
}

print "}}\n";
