#! /usr/bin/perl -w
# fileformat documentation: JOSM I18n.java function load()

use utf8;
use strict;
use open qw/:std :encoding(utf8)/;
use Term::ReadKey;
use Encode;

my $waswarn = 0;
my $lang_pattern = '([a-z]{2}_[A-Z]{2}|[a-z]{2,3}|[a-z]{2}\@[a-z]+)';
my $lang_pattern_file = '([a-z]{2}_[A-Z]{2}|[a-z]{2,3}|[a-z]{2}-[a-z]+)';

main();

sub getdate
{
  my @t=gmtime();
  return sprintf("%04d-%02d-%02d %02d:%02d+0000",
  1900+$t[5],$t[4]+1,$t[3],$t[2],$t[1]);
}

sub loadpot($)
{
  my ($file) = @_;
  my %all = ();
  my %keys = ();
  die "Could not open file $file." if(!open FILE,"<:utf8",$file);
  my %postate = (last => "", type => "");
  my $linenum = 0;
  print "Reading file $file\n";
  while(<FILE>)
  {
    ++$linenum;
    my $fn = "$file:$linenum";
    chomp;
    if($_ =~ /^#/ || !$_)
    {
      checkpo(\%postate, \%all, "pot", "line $linenum in $file", \%keys, 1, undef);
      $postate{fuzzy} = 1 if ($_ =~ /fuzzy/);
    }
    elsif($_ =~ /^"(.*)"$/) {$postate{last} .= $1;}
    elsif($_ =~ /^(msg.+) "(.*)"$/)
    {
      my ($n, $d) = ($1, $2);
      my $new = !${postate}{fuzzy} && (($n eq "msgid" && $postate{type} ne "msgctxt") || ($n eq "msgctxt"));
      checkpo(\%postate, \%all, "pot", "line $linenum in $file", \%keys, $new, undef);
      $postate{last} = $d;
      $postate{type} = $n;
      $postate{src} = $fn if $new;
    }
    else
    {
      die "Strange line $linenum in $file: $_.";
    }
  }
  checkpo(\%postate, \%all, "pot", "line $linenum in $file", \%keys, 1, undef);
  close(FILE);
  return \%all;
}

sub loadfiles($$@)
{
  my $desc;
  my %all = ();
  my %keys = ();
  my ($lang,$use,@files) = @_;
  foreach my $file (@files)
  {
    die "Could not open file $file." if(!open FILE,"<:utf8",$file);

    if($file =~ /\/$lang_pattern\.po$/)
    {
      my $l = $1;
      ++$lang->{$l};
      my %postate = (last => "", type => "");
      my $linenum = 0;
      print "Reading file $file (lang $l)\n";
      while(<FILE>)
      {
        ++$linenum;
        my $fn = "$file:$linenum";
        chomp;
        if($_ =~ /^#/ || !$_)
        {
          checkpo(\%postate, \%all, $l, "line $linenum in $file", \%keys, 1, $use);
          $postate{fuzzy} = 1 if ($_ =~ /fuzzy/);
        }
        elsif($_ =~ /^"(.*)"$/) {$postate{last} .= $1;}
        elsif($_ =~ /^(msg.+) "(.*)"$/)
        {
          my ($n, $d) = ($1, $2);
          my $new = !${postate}{fuzzy} && (($n eq "msgid" && $postate{type} ne "msgctxt") || ($n eq "msgctxt"));
          checkpo(\%postate, \%all, $l, "line $linenum in $file", \%keys, $new, $use);
          $postate{last} = $d;
          $postate{type} = $n;
          $postate{src} = $fn if $new;
        }
        else
        {
          die "Strange line $linenum in $file: $_.";
        }
      }
      checkpo(\%postate, \%all, $l, "line $linenum in $file", \%keys, 1, $use);
    }
    else
    {
      die "File format not supported for file $file.";
    }
    close(FILE);
  }
  return %all;
}

my $alwayspo = 0;
my $alwaysup = 0;
my $noask = 0;
my %conflicts;
sub copystring($$$$$$$)
{
  my ($data, $en, $l, $str, $txt, $context, $ispo) = @_;

  $en = "___${context}___$en" if $context;

  if(exists($data->{$en}{$l}) && $data->{$en}{$l} ne $str)
  {
    return if !$str;
    if($l =~ /^_/)
    {
      $data->{$en}{$l} .= ";$str" if !($data->{$en}{$l} =~ /$str/);
    }
    elsif(!$data->{$en}{$l})
    {
      $data->{$en}{$l} = $str;
    }
    else
    {
      my $f = $data->{$en}{_file} || "";
      $f = ($f ? "$f;".$data->{$en}{"_src.$l"} : $data->{$en}{"_src.$l"}) if $data->{$en}{"_src.$l"};
      my $isotherpo = ($f =~ /\.po\:/);
      my $pomode = ($ispo && !$isotherpo) || (!$ispo && $isotherpo);

      my $mis = "String mismatch for '$en' **$str** ($txt) != **$data->{$en}{$l}** ($f)\n";
      my $replace = 0;

      if(($conflicts{$l}{$str} || "") eq $data->{$en}{$l}) {}
      elsif($pomode && $alwaysup) { $replace=$isotherpo; }
      elsif($pomode && $alwayspo) { $replace=$ispo; }
      elsif($noask) { print $mis; ++$waswarn; }
      else
      {
        ReadMode 4; # Turn off controls keys
        my $arg = "(l)eft, (r)ight";
        $arg .= ", (p)o, (u)pstream[ts/mat], all p(o), all up(s)tream" if $pomode;
        $arg .= ", e(x)it: ";
        print "$mis$arg";
        while((my $c = getc()))
        {
          if($c eq "l") { $replace=1; }
          elsif($c eq "r") {}
          elsif($c eq "p" && $pomode) { $replace=$ispo; }
          elsif($c eq "u" && $pomode) { $replace=$isotherpo; }
          elsif($c eq "o" && $pomode) { $alwayspo = 1; $replace=$ispo; }
          elsif($c eq "s" && $pomode) { $alwaysup = 1; $replace=$isotherpo; }
          elsif($c eq "x") { $noask = 1; ++$waswarn; }
          else { print "\n$arg"; next; }
          last;
        }
        print("\n");
        ReadMode 0; # Turn on controls keys
      }
      if(!$noask)
      {
        if($replace)
        {
          $data->{$en}{$l} = $str;
          $conflicts{$l}{$data->{$en}{$l}} = $str;
        }
        else
        {
          $conflicts{$l}{$str} = $data->{$en}{$l};
        }
      }
    }
  }
  else
  {
    $data->{$en}{$l} = $str;
  }
}

# Check a current state for new data
#
# @param postate Pointer to current status hash
# @param data    Pointer to final data array
# @param l       current language
# @param txt     output text in case of error, usually file and line number
# @param keys    pointer to hash for info keys extracted from the first msgid "" entry
# @param new     whether a data set is finish or not yet complete
# @param use     hash to strings to use or undef for all strings
#
sub checkpo($$$$$$$)
{
  my ($postate, $data, $l, $txt, $keys, $new, $use) = @_;

  if($postate->{type} eq "msgid") {$postate->{msgid} = $postate->{last};}
  elsif($postate->{type} eq "msgid_plural") {$postate->{msgid_1} = $postate->{last};}
  elsif($postate->{type} =~ /^msgstr(\[0\])?$/) {$postate->{msgstr} = $postate->{last};}
  elsif($postate->{type} =~ /^msgstr\[(.+)\]$/) {$postate->{"msgstr_$1"} = $postate->{last};}
  elsif($postate->{type} eq "msgctxt") {$postate->{context} = $postate->{last};}
  elsif($postate->{type}) { die "Strange type $postate->{type} found\n" }

  if($new)
  {
    my $en = $postate->{context} ?  "___$postate->{context}___$postate->{msgid}" : $postate->{msgid};
    if((!$postate->{fuzzy}) && ($l eq "pot" || $postate->{msgstr}) && $postate->{msgid}
    && (!$use || $use->{$en}))
    {
      copystring($data, $postate->{msgid}, $l, $postate->{msgstr},$txt,$postate->{context}, 1);
      if(!$use || $use->{$en}{"en.1"})
      {
        for(my $i = 1; exists($postate->{"msgstr_$i"}); ++$i)
        { copystring($data, $postate->{msgid}, "$l.$i", $postate->{"msgstr_$i"},$txt,$postate->{context}, 1); }
        if($postate->{msgid_1})
        { copystring($data, $postate->{msgid}, "en.1", $postate->{msgid_1},$txt,$postate->{context}, 1); }
      }
      copystring($data, $postate->{msgid}, "_src.$l", $postate->{src},$txt,$postate->{context}, 1);
    }
    elsif($postate->{msgstr} && !$postate->{msgid})
    {
      my %k = ($postate->{msgstr} =~ /(.+?): +(.+?)\\n/g);
      # take the first one!
      for $a (sort keys %k)
      {
        $keys->{$l}{$a} = $k{$a} if !$keys->{$l}{$a};
      }
    }
    foreach my $k (keys %{$postate})
    {
      delete $postate->{$k};
    }
    $postate->{type} = $postate->{last} = "";
  }
}

sub makestring($)
{
  my ($str) = @_;
  $str =~ s/\\"/"/g;
  $str =~ s/\\\\/\\/g;
  $str =~ s/\\n/\n/g;
  $str = encode("utf8", $str);
  return $str;
}

sub checkstring
{
  my ($la, $tr, $en, $cnt, $en1, $eq) = @_;
  $tr = makestring($tr);
  $en = makestring($en);
  $cnt = $cnt || 0;
  $en1 = makestring($en1) if defined($en1);
  my $error = 0;

  # Test one - are there single quotes which don't occur twice
  my $v = $tr;
  $v =~ s/''//g; # replace all twice occuring single quotes
  $v =~ s/'[{}]'//g; # replace all bracketquoting single quotes
  if($v =~ /'/)#&& $la ne "en")
  {
    warn "JAVA translation issue for language $la: Mismatching single quotes:\nTranslated text: ".decode("utf8",$tr)."\nOriginal text: ".decode("utf8",$en)."\n";
    $error = 1;
  }
  if($tr =~ /<!\[CDATA\[/)#&& $la ne "en")
  {
    warn "JAVA translation issue for language $la: CDATA in string:\nTranslated text: ".decode("utf8",$tr)."\nOriginal text: ".decode("utf8",$en)."\n";
    $error = 1;
  }
  # Test two - check if there are {..} which should not be
  my @fmt = ();
  my $fmt;
  my $fmte;
  my $fmte1 = "";
  my $trt = $tr; $trt =~ s/'[{}]'//g;
  while($trt =~ /\{(.*?)\}/g) {push @fmt,$1};
  while($trt =~ /\%([a-z]+)\%/g) {push @fmt,$1};
  $fmt = join("_", sort @fmt); @fmt = ();
  my $ent = $en; $ent =~ s/'[{}]'//g;
  while($ent =~ /\{(.*?)\}/g) {push @fmt,$1};
  while($ent =~ /\%([a-z]+)\%/g) {push @fmt,$1};
  $fmte = join("_", sort @fmt); @fmt = ();
  if($en1)
  {
     my $en1t = $en1; $en1t =~ s/'[{}]'//g;
     while($en1t =~ /\{(.*?)\}/g) {push @fmt,$1}; $fmte1 = join("_", sort @fmt);
  }
  if($fmt ne $fmte && $fmt ne $fmte1)
  {
    if(!($fmte eq '0' && $fmt eq "" && $cnt == 1)) # Don't warn when a single value is left for first multi-translation
    {
      warn "JAVA translation issue for language $la ($cnt): Mismatching format entries:\nTranslated text: ".decode("utf8",$tr)."\nOriginal text: ".decode("utf8",$en)."\n";
      $error = 1;
    }
  }

  #$tr = "" if($error && $la ne "en");
  return pack("n",65534) if $eq;

  return pack("n",length($tr)).$tr;
}

sub createlang($@)
{
  my ($data, @files) = @_;
  my $maxlen = 0;
  foreach my $file (@files)
  {
    my $len = length($file);
    $maxlen = $len if $len > $maxlen;
  }
  my $maxcount = keys(%{$data});
  foreach my $file (@files)
  {
    my $la;
    my $cnt = 0;
    if($file =~ /^(?:.*\/)?$lang_pattern_file\.lang$/)
    {
      $la = $1;
      $la =~ s/-/\@/;
    }
    else
    {
      die "Language for file $file unknown.";
    }
    die "Could not open outfile $file\n" if !open FILE,">:raw",$file;

    foreach my $en (sort keys %{$data})
    {
      next if $data->{$en}{"en.1"};
      my $val;
      my $eq;
      if($la eq "en")
      {
        ++$cnt;
        $val = $en;
        $val =~ s/^___(.*)___/_:$1\n/;
      }
      else
      {
        my $ennoctx = $en;
        $ennoctx =~ s/^___(.*)___//;
        $val = (exists($data->{$en}{$la})) ? $data->{$en}{$la} : "";
        ++$cnt if $val;
        if($ennoctx eq $val)
        {
          $val = ""; $eq = 1;
        }
      }
      print FILE checkstring($la, $val, $en, undef, undef, $eq);
    }
    print FILE pack "n",0xFFFF;
    foreach my $en (sort keys %{$data})
    {
      next if !$data->{$en}{"en.1"};
      my $num;
      for($num = 1; exists($data->{$en}{"$la.$num"}); ++$num)
      { }
      my $val;
      my $eq = 0;
      if($la eq "en")
      {
        ++$cnt;
        $val = $en;
        $val =~ s/^___(.*)___/_:$1\n/;
      }
      else
      {
        $val = (exists($data->{$en}{$la})) ? $data->{$en}{$la} : "";
        --$num if(!$val);
        ++$cnt if $val;
        if($num == 2)
        {
          my $ennoctx = $en;
          $ennoctx =~ s/^___(.*)___//;
          if($val eq $ennoctx && $data->{$en}{"$la.1"} eq $data->{$en}{"en.1"})
          {
            $num = 0;
            $eq = 1;
          }
        }
      }

      print FILE pack "C",$eq ? 0xFE : $num;
      if($num)
      {
        print FILE checkstring($la, $val, $en, 1, $data->{$en}{"en.1"});
        for($num = 1; exists($data->{$en}{"$la.$num"}); ++$num)
        {
          print FILE checkstring($la, $data->{$en}{"$la.$num"}, $en, $num+1, $data->{$en}{"en.1"});
        }
      }
    }
    close FILE;
    if(!$cnt)
    {
      unlink $file;
      printf "Skipped file %-${maxlen}s: Contained 0 strings out of %5d.\n",$file,$maxcount;
    }
    else
    {
      printf "Created file %-${maxlen}s: Added %5d strings out of %5d (%5.1f%%).\n",$file,$cnt,$maxcount,,$cnt*100.0/$maxcount-5e-2;
    }
  }
}

sub main
{
  my %lang;
  my @po;
  my $potfile;
  my $basename = "./";
  foreach my $arg (@ARGV)
  {
    next if $arg !~ /^--/;
    if($arg =~ /^--basedir=(.+)$/)
    {
      $basename = $1;
    }
    elsif($arg =~ /^--potfile=(.+)$/)
    {
      $potfile = $1;
    }
    else
    {
      die "Unknown argument $arg.";
    }
  }
  $basename .= "/" if !($basename =~ /[\/\\:]$/);
  foreach my $arg (@ARGV)
  {
    next if $arg =~ /^--/;
    foreach my $f (glob $arg)
    {
      if($f =~ /\*/) { printf "Skipping $f\n"; }
      elsif($f =~ /\.po$/) { push(@po, $f); }
      else { die "unknown file extension."; }
    }
  }
  my %data = loadfiles(\%lang,$potfile ? loadpot($potfile) : undef, @po);

  my @clang;
  foreach my $la (sort keys %lang)
  {
    $la =~ s/\@/-/;
    push(@clang, "${basename}$la.lang");
  }
  push(@clang, "${basename}en.lang");
  die "There have been warning. No output.\n" if $waswarn;

  createlang(\%data, @clang);
}
