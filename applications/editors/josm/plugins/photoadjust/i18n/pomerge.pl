#! /usr/bin/perl -w

#####################################################################
### http://www.perl.com/doc/manual/html/utils/pod2man.html
### http://search.cpan.org/dist/perl/pod/perlpod.pod

=head1 NAME

pomerge.pl - Run msgmerge with the files in the po directory and
    remove untranslated strings.

=head1 SYNOPSIS

B<poimport.pl> [B<--help>] [B<--man>] [B<--podir> I<po>]
    [B<--project> I<project>]

=head1 DESCRIPTION

This script merges the translated strings with the template POT file
(msgmerge).  It then removes all untranslated strings (msgattrib).
The script works with all PO files in the po directory.

=head1 OPTIONS

=over 4

=item B<--help>

Prints a brief help message and exits.

=item B<--man>

Prints the manual page and exits.

=item B<--podir>

Directory with PO files that are to be merged.  Default is F<po>.

=item B<--project>

Project or plugin name.  If the name of the template is F<plugin.pot>,
then this name is I<plugin>.  Default is I<josm>.

=back

=cut
#####################################################################

use strict;
use File::Spec::Functions;
use Getopt::Long;
use Pod::Usage;

my $podir = "po";               ### Directory with PO files that are merged.
my $project = "josm";           ### Project/plugin name.
my $showhelp = 0;               ### Show help screen.
my $showman = 0;                ### Show manual page of this script.

GetOptions('help|?|h'  => \$showhelp,
           'man'       => \$showman,
           'podir=s'   => \$podir,
           'project=s' => \$project,
          ) or pod2usage(2);

pod2usage(1) if $showhelp;
pod2usage(-exitstatus => 0, -verbose => 2) if $showman;

### Path to POT file.
my $potfile = catfile($podir, $project . ".pot");

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
