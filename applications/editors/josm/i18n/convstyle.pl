#! /usr/bin/perl -w

# Written by Dirk Stöcker <openstreetmap@dstoecker.de>
# Public domain, no rights reserved.

use strict;

my $rule_cond; # cumulated conditions from current rule

# This is a simple conversion and in no way a complete XML parser
# but it works with a default Perl installation

# Print a header to write valid Java code.  No line break,
# so that the input and output line numbers will match.
print "class trans_style { void tr(String s){} void f() {";

while(my $line = <>)
{
  chomp($line);
  print "tr(\"\"); ";
  if($line =~ /<rules\s+name=(".*?")/)
  {
    print "/* mappaint style named $1 */ tr($1);\n";
  }
  elsif($line =~ /<rule\s*>/)
  {
    $rule_cond = "";
    print "/* $line */\n";
  }
  elsif($line =~ /<condition.*\s+k="([^"]*)"/)
  {
    my $cond_k = $1; # according to schema, k is always present
    my $cond_v = ($line =~ /\s+v="([^"]*)"/) ? $1 : "";
    my $cond_b = ($line =~ /\s+b="([^"]*)"/) ? $1 : "";
    print STDERR "$0: Found both v=\"$cond_v\" and b=\"$cond_b\" for k=\"$cond_k\" at line $.\n" if ($cond_v && $cond_b);
    my $cond = ($cond_v || $cond_b) ? "$cond_k=$cond_v$cond_b" : "$cond_k"; # v and b shouldn't appear both
    if ($rule_cond)
    {
      $rule_cond .= ", " . $cond;
    }
    else
    {
      $rule_cond = $cond;
    }
    print "/* $line */\n";
  }
  elsif($line =~ /colour="([^"]+)#/)
  {
    if ($line =~ /\s+colour="([^"]+)#/)
    {
      my $c = $1;
      my $co = $1;
      $c =~ s/[^a-z0-9]+/./g;
      print "/* color $co (applied for \"$rule_cond\") */ tr(\"$c\");";
    }
    if ($line =~ /\s+dashedcolour="([^"]+)#/)
    {
      my $c = $1;
      my $co = $1;
      $c =~ s/[^a-z0-9]+/./g;
      print "/* color $co (applied for \"$rule_cond\") */ tr(\"$c\");";
    }
    print "\n";
  }
  else
  {
    print "/* $line */\n";
  }
}

print "}}\n";
