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

use strict;
use utf8;
use Encode;
use Getopt::Long;
use Pod::Usage;
use File::Basename;
push(@INC, dirname($0));
require i18nlib;

main();

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
  exit if ($#po < 0);
  my %data = loadfiles(\%lang,@po);
  my $descs = $data{$description};
  my @langs = sort keys %lang;
  addmfdescs($manifest, $descs, @langs);
}
