#! /usr/bin/perl -w
###
### diff_lang.pl - Show differences of a JOSM language file.
###
### Syntax examples:
###   diff_lang.pl -u -L "data/en.lang (revision 12345)"
###	-L "data/en.lang (working copy)"
###	.svn/pristine/42/423b7dfb6a014d6c0d3ec3ce96a10e2df665266a.svn-base
###	data/en.lang
###   diff_lang.pl --tkdiff -L "data/en.lang (revision 12345)"
###	-L "data/en.lang (working copy)"
###	.svn/pristine/42/423b7dfb6a014d6c0d3ec3ce96a10e2df665266a.svn-base
###	data/en.lang

use strict;
use utf8;
use File::Basename;
use File::Temp qw/ tempfile /;
push(@INC, dirname($0));
require i18nlib;

my @diffArgs;
my @files;
my $tkdiff = 0;
#print "Arguments: ", join(" ", @ARGV), "\n";
for (my $idx = 0; $idx <= $#ARGV; $idx++) {
  my $arg = $ARGV[$idx];
  if ($arg eq "-u") {
    push(@diffArgs, $arg);
  }
  elsif ($arg eq "-L") {
    push(@diffArgs, $arg, $ARGV[++$idx]);
  }
  elsif ($arg eq "--tkdiff") {
    $tkdiff = 1;
  }
  elsif ($arg =~ m/^-/) {
    #die "Unknown option '$arg'.\n";
    ### Assume that diff knows this option and that it has no argument.
    push(@diffArgs, $arg);
  }
  else {
    push(@files, $arg);
  }
  #print "$idx: $ARGV[$idx]\n";
}

if ($#files != 1) {
  print "Files:\n", join("\n", @files), "\n";
  die sprintf("Expected two file arguments, got %d arguments.\n", $#files + 1);
}

my $separator = "\n\n";
my @strings = loadLangFile($files[0]);
my ($fh1, $tmpfile1) = tempfile();
#binmode($fh1, ":utf8");
print $fh1 join($separator, @strings), "\n";
close($fh1);
@strings = loadLangFile($files[1]);
my ($fh2, $tmpfile2) = tempfile();
#binmode($fh2, ":utf8");
print $fh2 join($separator, @strings), "\n";
close($fh2);

push(@diffArgs, $tmpfile1, $tmpfile2);
my @diffCmd;
if ($tkdiff) {
  push(@diffCmd, "tkdiff");
}
else {
  push(@diffCmd, "diff");
}
push(@diffCmd, @diffArgs);
#print "Command: ", join(" ", @diffCmd), "\n";
system(@diffCmd);
