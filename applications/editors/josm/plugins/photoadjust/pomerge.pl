#! /usr/bin/perl -w
###
### pomerge.pl - Run msgmerge with the files in the po directory and
###     remove untranslated strings.

use strict;
use File::Spec::Functions;

### Directory with PO files that are to be merged.
my $podir = "po";
### Path to POT file.
my $potfile = catfile($podir, "photoadjust.pot");

foreach my $pofile (split("\n", `find $podir -name "*.po"`)) {
  ### Merge translation with template.
  my $cmd = "msgmerge --quiet --update --backup=none $pofile $potfile";
  system $cmd;

  ### Get rid of all unneeded translations.  Fuzzy translations are
  ### removed too.  msgattrib will not write an output file if nothing
  ### is left.  We move the original file and delete it afterwards to
  ### preserve only languages with translations.
  my $potmp = $pofile . ".tmp";
  rename $pofile, $potmp;
  $cmd = "msgattrib --output-file=$pofile --translated --no-fuzzy --no-obsolete $potmp";
  system $cmd;
  unlink $potmp;
  if (-z $pofile) {
    ### The PO file might be empty if there are no translated strings.
    unlink $pofile;
  }
  if (-e $pofile . "~") {
    ### Remove the backup copy.
    unlink $pofile . "~";
  }
}
