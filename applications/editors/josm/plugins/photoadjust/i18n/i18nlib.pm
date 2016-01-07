### i18nlib.pm - Perl library based on i18n/i18n.pl.

use utf8;
binmode STDERR, ":encoding(utf8)";
use Term::ReadKey;
use Encode;

my $waswarn = 0;
my $maxcount = 0;

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
    if($file =~ /\/(.._..(@.+)?)\.po$/ || $file =~ /\/(...?(@.+)?)\.po$/)
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

### Load a JOSM language file.  The format is two byte string length, then the
### string.  Length 0x0000 means the string was not in the PO file, i.e. there
### is no translation (there are no empty strings).  Length 0xfffe (65534)
### means the string is the same as the original string.  Length 0xffff ends
### the string list.
###
### Args:
###     filename (str): Path to language file.
###
### Returns:
###     array: List of decoded strings.
sub loadLangFile {
  my ($filename) = @_;
  my @strings;
  open(my $langFd, $filename) or die "Cannot open file $filename: $!";
  binmode($langFd);
  my $buffer;
  my $length;
  while (1) {
    read($langFd, $buffer, 2);
    $length = unpack("n", $buffer);
    die "Unable to read string length" unless ($buffer);
    if ($length == 0) {
      push(@strings, "");
    }
    elsif ($length == 0xfffe) {
      push(@strings, "(see original string)");
    }
    elsif ($length == 0xffff) {
      last;
    }
    else {
      read($langFd, $buffer, $length);
      push(@strings, $buffer);
    }
  }
  close($langFd);
  return @strings;
}

1;
