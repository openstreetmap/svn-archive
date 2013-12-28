#! /usr/bin/perl -w

#####################################################################
### http://www.perl.com/doc/manual/html/utils/pod2man.html
### http://search.cpan.org/dist/perl/pod/perlpod.pod

=head1 NAME

mftrans.pl - Add the translations of the plugin description to
    the manifest.

=head1 SYNOPSIS

B<poimport.pl> [B<--help>] [B<--man>] [B<--manifest> I<MANIFEST>]
    B<--description> I<"Plugin description."> I<po/*.po> ...

=head1 DESCRIPTION

Read the translations of the plugin description from the specified PO
files and add them to the manifest.  Option B<--description> is
mandatory.  PO files are expected as arguments.

=head1 OPTIONS

=over 4

=item B<--help>

Prints a brief help message and exits.

=item B<--man>

Prints the manual page and exits.

=item B<--manifest>

Manifest file the translations are added to.  Default is F<MANIFEST>.

=item B<--description>

Plugin description.  The same string that is specified as
C<plugin.description> in file F<build.xml>.

=back

=cut
#####################################################################

### This file is based on i18n/i18n.pl.  The functions loadfiles(),
### copystring(), checkpo() and variables used by those functions are
### a one-to-one copy.

use utf8;
#use encoding "utf8";
binmode STDERR, ":encoding(utf8)";
use Term::ReadKey;
use Encode;
use Getopt::Long;
use Pod::Usage;

my $waswarn = 0;
my $maxcount = 0;

main();

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

### Add translations of plugin description to manifest.  We write an
### ant build file and call ant to do that.  This way ant will take
### care of the manifest format details.
sub addmfdescs($@)
{
  my ($manifest, $descs, @langs) = @_;
  my $buildfile = "build-descs.xml";
  open FILE,">",$buildfile or die "Could not open file $buildfile: $!";
  binmode FILE, ":encoding(utf8)";
  print FILE <<EOT;
<?xml version="1.0" encoding="utf-8"?>
<project name="photoadjust" default="descs" basedir=".">
  <target name="descs">
    <manifest file="$manifest" mode="update">
EOT
  foreach my $la (@langs) {
    if (exists(${$descs}{$la})) {
      my $trans = ${$descs}{$la};
      print FILE "      <attribute name=\"", $la,
        "_Plugin-Description\" value=\"", $trans, "\"/>\n";
    }
  }
  print FILE <<EOT;
    </manifest>
  </target>
</project>
EOT
  close FILE;
  system "ant -buildfile $buildfile";
  unlink $buildfile;
}

sub main
{
  my $manifest = "MANIFEST";            ### Manifest file.
  my $description = "No description.";  ### Plugin description.
  my $showhelp = 0;                     ### Show help screen.
  my $showman = 0;                      ### Show manual page of this script.

  GetOptions('help|?|h'      => \$showhelp,
             'man'           => \$showman,
             'manifest=s'    => \$manifest,
             'description=s' => \$description,
            ) or pod2usage(2);

  pod2usage(1) if $showhelp;
  pod2usage(-exitstatus => 0, -verbose => 2) if $showman;

  my %lang;
  my @po;
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
  my $descs = $data{$description};
  my @langs = sort keys %lang;
  addmfdescs($manifest, $descs, @langs);
}
