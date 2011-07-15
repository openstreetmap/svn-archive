#! /usr/bin/perl -w

use utf8;
use encoding "utf8";
use Term::ReadKey;
use Encode;

my $waswarn = 0;
my $maxcount = 0;

main();

sub getdate
{
  my @t=gmtime();
  return sprintf("%04d-%02d-%02d %02d:%02d+0000",
  1900+$t[5],$t[4]+1,$t[3],$t[2],$t[1]);
}

sub loadfiles($@)
{
  my $desc;
  my $all;
  my ($lang,@files) = @_;
  foreach my $file (@files)
  {
    die "Could not open file $file." if(!open FILE,"<:utf8",$file);
    my $linenum = 0;

    my $cnt = -1; # don't count translators info
    if($file =~ /\/(.._..)\.po$/ || $file =~ /\/(...?)\.po$/)
    {
      my $l = $1;
      ++$lang->{$l};
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
          checkpo(\%postate, \%all, $l, "line $linenum in $file", $keys, 1);
          $postate{fuzzy} = 1 if ($_ =~ /fuzzy/);
        }
        elsif($_ =~ /^"(.*)"$/) {$postate{last} .= $1;}
        elsif($_ =~ /^(msg.+) "(.*)"$/)
        {
          my ($n, $d) = ($1, $2);
          ++$cnt if $n eq "msgid";
          my $new = !${postate}{fuzzy} && (($n eq "msgid" && $postate{type} ne "msgctxt") || ($n eq "msgctxt"));
          checkpo(\%postate, \%all, $l, "line $linenum in $file", $keys, $new);
          $postate{last} = $d;
          $postate{type} = $n;
          $postate{src} = $fn if $new;
        }
        else
        {
          die "Strange line $linenum in $file: $_.";
        }
      }
      checkpo(\%postate, \%all, $l, "line $linenum in $file", $keys, 1);
    }
    else
    {
      die "File format not supported for file $file.";
    }
    $maxcount = $cnt if $cnt > $maxcount;
    close(FILE);
  }
  return %all;
}

my $alwayspo = 0;
my $alwaysup = 0;
my $noask = 0;
my $conflicts;
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

sub checkpo($$$$$$)
{
  my ($postate, $data, $l, $txt, $keys, $new) = @_;

  if($postate->{type} eq "msgid") {$postate->{msgid} = $postate->{last};}
  elsif($postate->{type} eq "msgid_plural") {$postate->{msgid_1} = $postate->{last};}
  elsif($postate->{type} =~ /^msgstr(\[0\])?$/) {$postate->{msgstr} = $postate->{last};}
  elsif($postate->{type} =~ /^msgstr\[(.+)\]$/) {$postate->{"msgstr_$1"} = $postate->{last};}
  elsif($postate->{type} eq "msgctxt") {$postate->{context} = $postate->{last};}
  elsif($postate->{type}) { die "Strange type $postate->{type} found\n" }

  if($new)
  {
    if((!$postate->{fuzzy}) && $postate->{msgstr} && $postate->{msgid})
    {
      copystring($data, $postate->{msgid}, $l, $postate->{msgstr},$txt,$postate->{context}, 1);
      for($i = 1; exists($postate->{"msgstr_$i"}); ++$i)
      { copystring($data, $postate->{msgid}, "$l.$i", $postate->{"msgstr_$i"},$txt,$postate->{context}, 1); }
      if($postate->{msgid_1})
      { copystring($data, $postate->{msgid}, "en.1", $postate->{msgid_1},$txt,$postate->{context}, 1); }
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
  my ($la, $tr, $en, $cnt, $en1) = @_;
  $tr = makestring($tr);
  $en = makestring($en);
  $en1 = makestring($en1) if defined($en1);
  my $error = 0;

  # Test one - are there single quotes which don't occur twice
  my $v = $tr;
  $v =~ s/''//g; # replace all twice occuring single quotes
  if($v =~ /'/)
  {
    warn "JAVA translation issue for language $la: Mismatching single quotes:\nTranslated text: $tr\n";
    $error = 1;
  }
  # Test two - check if there are {..} which should not be
  my @fmt = ();
  my $fmt;
  my $fmte;
  my $fmte1 = "";
  while($tr =~ /\{(.*?)\}/g) {push @fmt,$1}; $fmt = join("_", sort @fmt); @fmt = ();
  while($en =~ /\{(.*?)\}/g) {push @fmt,$1}; $fmte = join("_", sort @fmt); @fmt = ();
  if($en1) {while($en1 =~ /\{(.*?)\}/g) {push @fmt,$1}; $fmte1 = join("_", sort @fmt);}
  if($fmt ne $fmte && $fmt ne $fmte1)
  {
    if(!($fmte eq '0' && $fmt eq "" && $cnt == 1)) # Don't warn when a single value is left for first multi-translation
    {
      $cnt == $cnt || 0;
      warn "JAVA translation issue for language $la ($cnt): Mismatching format entries:\nTranslated text: $tr\nOriginal text: $en\n";
      $error = 1;
    }
  }

  #$tr = "" if($error && $la ne "en");

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
  foreach my $file (@files)
  {
    my $la;
    my $cnt = 0;
    if($file =~ /[-_](.._..)\.lang$/ || $file =~ /^(?:.*\/)?(.._..)\.lang$/ ||
    $file =~ /[-_](...?)\.lang$/ || $file =~ /^(?:.*\/)?(...?)\.lang$/)
    {
      $la = $1;
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
        $val = "" if($ennoctx eq $val);
      }
      print FILE checkstring($la, $val, $en);
    }
    print FILE pack "n",0xFFFF;
    foreach my $en (sort keys %{$data})
    {
      next if !$data->{$en}{"en.1"};
      my $num;
      for($num = 1; exists($data->{$en}{"$la.$num"}); ++$num)
      { }
      my $val;
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
          $num = 0 if $val eq $ennoctx && $data->{$en}{"$la.1"} eq $data->{$en}{"en.1"};
        }
      }

      print FILE pack "C",$num;
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
      printf "Created file %-${maxlen}s: Added %5d strings out of %5d (%5.1f%%).\n",$file,$cnt,$maxcount,,$cnt*100.0/$maxcount;
    }
  }
}

sub main
{
  my %lang;
  my @po;
  my $basename = shift @ARGV;
  foreach my $arg (@ARGV)
  {
    foreach my $f (glob $arg)
    {
      if($f =~ /\*/) { printf "Skipping $f\n"; }
      elsif($f =~ /\.po$/) { push(@po, $f); }
      else { die "unknown file extension."; }
    }
  }
  my %data = loadfiles(\%lang,@po);
  my @clang;
  foreach my $la (sort keys %lang)
  {
    push(@clang, "${basename}$la.lang");
  }
  push(@clang, "${basename}en.lang");
  die "There have been warning. No output.\n" if $waswarn;

  createlang(\%data, @clang);
}
