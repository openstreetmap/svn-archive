#! /usr/bin/perl -w

# Written by Dirk Stöcker <openstreetmap@dstoecker.de>
# Public domain, no rights reserved.

use strict;

my $item = "";
my $group;
my $combo_n;
my @combo_values;
my $combo_idx;
my $result = 0;
my $comment = 0;

# This is a simple conversion and in no way a complete XML parser
# but it works with a default Perl installation

# Print a header to write valid Java code.  No line break,
# so that the input and output line numbers will match.
print "class trans_preset { void tr(String s){} void f() {";

sub fix($)
{
  my ($val) = @_;
  $val =~ s/'/''/g;
  return $val;
}

while(my $line = <>)
{
  chomp($line);
  if($line =~ /<item\s+name=(".*?")/)
  {
    my $val = fix($1);
    $item = $group ? "$group$val" : $val;
    $item =~ s/""/\//;
    if($line =~ /name_context=(".*?")/)
    {
      print "/* item $item */ trc($1, $val);\n";
    }
    else
    {
      print "/* item $item */ tr($val);\n";
    }
  }
  elsif($line =~ /<group.*\s+name=(".*?")/)
  {
    my $gr = fix($1);
    $group = $group ? "$group$gr" : $gr;
    $group =~ s/\"\"/\//;
    if($line =~ /name_context=(".*?")/)
    {
      print "/* group $group */ trc($1,$gr);\n";
    }
    else
    {
      print "/* group $group */ tr($gr);\n";
    }
  }
  elsif($line =~ /<label.*\s+text=" "/)
  {
    print "/* item $item empty label */\n";
  }
  elsif($line =~ /<label.*\s+text=(".*?")/)
  {
    my $text = fix($1);
    if($line =~ /text_context=(".*?")/)
    {
      print "/* item $item label $text */ trc($1,$text);\n";
    }
    else
    {
      print "/* item $item label $text */ tr($text);\n";
    }
  }
  elsif($line =~ /<text.*\s+text=(".*?")/)
  {
    my $n = fix($1);
    if($line =~ /text_context=(".*?")/)
    {
      print "/* item $item text $n */ trc($1,$n);\n";
    }
    else
    {
      print "/* item $item text $n */ tr($n);\n";
    }
  }
  elsif($line =~ /<check.*\s+text=(".*?")/)
  {
    my $n = fix($1);
    if($line =~ /text_context=(".*?")/)
    {
      print "/* item $item check $n */ trc($1,$n);\n";
    }
    else
    {
      print "/* item $item check $n */ tr($n);\n";
    }
  }
  elsif($line =~ /<role.*\s+text=(".*?")/)
  {
    my $n = fix($1);
    if($line =~ /text_context=(".*?")/)
    {
      print "/* item $item role $n */ trc($1,$n);\n";
    }
    else
    {
      print "/* item $item role $n */ tr($n);\n";
    }
  }
  # first handle display values
  elsif($line =~ /<(combo|multiselect).*\s+text=(".*?").*\s+display_values="(.*?)"/)
  {
    my ($type,$n,$vals) = ($1,fix($2),$3);
    my $sp = ($type eq "combo" ? ",":";");
    $combo_n = $n;
    $combo_idx = 0;
    my $vctx = ($line =~ /values_context=(".*?")/) ? $1 : undef;
    if($line =~ /text_context=(".*?")/)
    {
      print "/* item $item $type $n */ trc($1,$n);";
    }
    else
    {
      print "/* item $item $type $n */ tr($n);";
    }
    $vals =~ s/\\$sp/\x91/g;
    @combo_values = split $sp,$vals;
    for (my $i=0; $i<@combo_values; ++$i)
    {
      my $val = $combo_values[$i];
      $val =~ s/\x91/$sp/g;
      next if $val =~ /^[0-9-]+$/; # search for non-numbers
      $val = fix($val);
      print "/* item $item $type $n display value */" . ($vctx ? " trc($vctx, \"$val\");" : " tr(\"$val\");");
    }
    print "\n";
  }
  elsif($line =~ /<(combo|multiselect).*\s+text=(".*?").*\s+values="(.*?)"/)
  {
    my ($type,$n,$vals) = ($1,fix($2),$3);
    my $sp = ($type eq "combo" ? ",":";");
    $combo_n = $n;
    $combo_idx = 0;
    my $vctx = ($line =~ /values_context=(".*?")/) ? $1 : undef;
    if($line =~ /text_context=(".*?")/)
    {
      print "/* item $item $type $n */ trc($1,$n);";
    }
    else
    {
      print "/* item $item $type $n */ tr($n);";
    }
    @combo_values = split $sp,$vals;
    foreach my $val (@combo_values)
    {
      next if $val =~ /^[0-9-]+$/; # search for non-numbers
      $val = fix($val);
      print "/* item $item $type $n display value */" . ($vctx ? " trc($vctx, \"$val\");" : " tr(\"$val\");");
    }
    print "\n";
  }
  elsif(!$comment && $line =~ /<short_description>(.*?)<\/short_description>/)
  {
    my $n = fix($1);
    print "/* item $item combo $combo_n item \"$combo_values[$combo_idx]\" short description */ tr(\"$n\");\n";
    $combo_idx++;
  }
  elsif($line =~ /<\/group>/)
  {
    $group = 0 if !($group =~ s/(.*\/).*?$//);
    print "\n";
  }
  elsif($line =~ /<\/item>/)
  {
    $item = "";
    print "\n";
  }
  elsif($line =~ /<\/combo/)
  {
    $combo_n = "";
    $combo_idx = 0;
  }
  elsif(!$line)
  {
    print "\n";
  }
  elsif($line =~ /^\s*$/
     || $line =~ /<separator *\/>/
     || $line =~ /<space *\/>/
     || $line =~ /<\/?optional>/
     || $line =~ /<key/
     || $line =~ /<presets/
     || $line =~ /<\/presets/
     || $line =~ /roles/
     || $line =~ /href=/
     || $line =~ /<!--/
     || $line =~ /<\?xml/
     || $line =~ /-->/
     || $comment)
  {
    print "// $line\n";
  }
  else
  {
    print "/* unparsed line $line */\n";
    $result = 20
  }

  # note, these two must be in this order ore oneliners aren't handled
  $comment = 1 if($line =~ /<!--/);
  $comment = 0 if($line =~ /-->/);
}

print "}}\n";
return $result if $result;
