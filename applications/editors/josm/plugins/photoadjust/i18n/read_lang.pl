#! /usr/bin/perl -w

#####################################################################
### http://www.perl.com/doc/manual/html/utils/pod2man.html
### http://search.cpan.org/dist/perl/pod/perlpod.pod

=head1 NAME

read_lang.pl - Read a binary JOSM language file.

=head1 SYNOPSIS

B<read_lang.pl> [B<--help>] [B<--man>] [B<--output> I<outfile>] I<lang.lang>

=head1 DESCRIPTION

Read a binary JOSM language file and print the strings.

=head1 OPTIONS

=over 4

=item B<--help>

Prints a brief help message and exits.

=item B<--man>

Prints the manual page and exits.

=item B<--output>, B<-o>

Write the strings into the specified file.  Default is to print the string to
standard output.

=item B<--statistics>, B<-s>

Print statistics about the language file instead of the strings.

=back

=cut
#####################################################################

use strict;
use File::Basename;
use Getopt::Long;
use Pod::Usage;
push(@INC, dirname($0));
require i18nlib;

my $showhelp = 0;               ### Show help screen.
my $showman = 0;                ### Show manual page of this script.
my $outfile;			### Name of output file.
my $langfile;			### Name of language file.
my $statistics = 0;		### Print statistics.
my $separator = "\n\n";		### String separator.

GetOptions('help|?|h'   => \$showhelp,
           'man'        => \$showman,
           'output|o=s' => \$outfile,
           'statistics|s' => \$statistics,
          ) or pod2usage(2);

pod2usage(1) if $showhelp;
pod2usage(-exitstatus => 0, -verbose => 2) if $showman;

### Check for arguments.  The only supported argument is the language file.
if ($#ARGV == 0) {
  $langfile = $ARGV[0];
}
elsif ($#ARGV > 0) {
  die "This script accepts only one argument.\n";
}

die "Please specify a language file.\n" unless ($langfile);

my @strings = loadLangFile($langfile);
if ($statistics) {
  my $empty = 0;
  my $sametrans = 0;
  foreach my $string (@strings) {
    if ($string eq "") {
      $empty++;
    }
    elsif ($string eq "(see original string)") {
      $sametrans++;
    }
  }
  printf("Total strings: %d\n", $#strings + 1);
  print "Strings without translation: $empty\n";
  print "Strings with equal translation: $sametrans\n";
}
else {
  if ($outfile) {
    open(my $outFd, ">", $outfile) or die "Cannot open output file $outfile: $!";
    print $outFd join($separator, @strings), "\n";
    close($outFd);
  }
  else {
    print join($separator, @strings), "\n";
  }
}
