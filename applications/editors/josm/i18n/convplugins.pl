#! /usr/bin/perl -w

# Written by Dirk Stöcker <openstreetmap@dstoecker.de>
# Public domain, no rights reserved.

use strict;

# This is a simple conversion and in no way a complete XML parser
# but it works with a default Perl installation

# Print a header to write valid Java code.
print "class trans_plugins { void tr(String s){} void f() {\n";

foreach my $arg (@ARGV)
{
  foreach my $name (glob $arg)
  {
    my $printed = 0;
    die "Can't open $name." if !(open FILE,"<",$name);
    my $plugin = $name;
    while(my $line = <FILE>)
    {
      chomp($line);
      if($line =~ /name=\"[Pp]lugin.[Dd]escription\" +value=\"(.*)\"/)
      {
        $printed = 1;
        print "/* Plugin $plugin */\ntr(\"$1\");\n" if($plugin ne "myPluginName");
      }
      elsif($line =~ /project name=\"(.*?)\"/)
      {
        $plugin = $1;
      }
    }
    close FILE;
    print "/* File $name had no data */\n" if(!$printed);
  }
}

print "}}\n";
