#!/usr/bin/perl 

use strict;
use warnings;
use IO::File;
use Getopt::Long;
use Pod::Usage;

my $project_name="";

my $append=0;
my $prefix="";
my $debug = 0;

$ENV{'LANGUAGE'}="en_US";
$ENV{'LANG'}="en_US";


GetOptions (
    'project_name:s'	=> \$project_name,
    'prefix:s'		=> \$prefix,
    'append'		=> \$append,
    'debug'		=> \$debug,
    ) or pod2usage(1);

unless ( $project_name ) {
    pod2usage(2);
    die "No Projectname defined";
}


my $fi = IO::File->new("svn log . |");

my $fo;
if ( $append )  {
    $fo = IO::File->new(">>debian/changelog");
} else {
    $fo = IO::File->new(">debian/changelog");
}
my ($rev,$user,$date,$lines) = ('','','','');
my ($date_dummy,$date_time,$date_offset,$date_date) = ('','','','');
my $t='@';
my $user2full_name={
    root        => "the Maintainer",
    amillar	=> "Alan Millar",
    andystreet	=> "",
    artem	=> "Artem Pavlenko",
    ben		=> "Ben Robinson",
    bobkare	=> "Knut Arne Bjørndal",
    breki	=> "Igor Brejic",
    brent	=> "",
    bretth	=> "Brett Henderson",
    christofd	=> "Christof Dallermassl",
    damians	=> "",
    danmoore	=> "Dan Moore",
    david	=> "David Earl <david${t}frankieandshadow.com>",
    deelkar	=> "Dirk-Lüder Kreie",
    dennis_de	=> "dennis_de",
    dirkl	=> "Dirk-Lüder Kreie now deelkar",
    dotbaz	=> "Barry Crabtree",
    dshpak	=> "Darryl Shpak",
    enxrah	=> "",
    etienne	=> "Etienne Cherdlu",
    frederik	=> "Frederik Ramm",
    frsantos	=> "Francisco R. Santos <frsantos${t}gmail.com> ",
    gabriel	=> "Gabriel Ebner",
    gslater	=> "Grant Slater",
    guenther	=> "Guenter Maier",
    hakan	=> "Hakan Tandogan",
    harrywood	=> "Harry Wood",
    imi		=> "Immanuel Scholz",
    isortega	=> "Iván Sánchez Ortega",
    jdschmidt	=> "J. D. 'Dutch' Schmidt",
    jeroen	=> "Jeroen Ticheler",
    jochen	=> "Jochen Topf",
    joerg	=> "Joerg Ostertag (Debian Packages) <debian${t}ostertag.name>",
    jonas	=> "",
    jonb	=> "Jon Burgess",
    jrreid	=> "Jason Reid",
    ksharp	=> "Keith Sharp",
    lorenz	=> "Lorenz Kiefner",
    marc	=> "",
    martinvoosterhout	=> "Martijn van Oosterhout",
    matt_gnu	=> "",
    matthewnc	=> "",
    mstrecke	=> "Michael Strecke <MStrecke${t}gmx.de>",
    nick	=> "",
    nickb	=> "Nick Black",
    nickburch	=> "Nick Burch",
    ojw		=> "Almién Oliver White <ojwlists${t}googlemail.com>",
    pere	=> "Petter Reinholdtsen",
    richard	=> "Richard Fairhurst",
    spaetz	=> "Sebastian Spaeth",
    stefanb	=> "Stefan B (Ljubljana, Slovenia)",
    steve	=> "Steve Coast",
    t2000	=> "",
    tabacha	=> "Sven Anders",
    texamus	=> "Artem Dudarev",
    tim		=> "",
    tomhughes	=> "Tom Hughes",
    twalraet	=> "Thomas Walraet",
    tweety	=> "Joerg Ostertag (Debian Packages) <debian${t}ostertag.name>",
    ulf		=> "Ulf Lamping",
    charles	=> "Charles Curley <charlescurley${t}charlescurley.com>",
    cjastram	=> "Christopher Jastram",
    commiter	=> "",
    dse		=> "Guenther Meyer <d.s.e${t}sordidmusic.com>",
    ganter	=> "Fritz Ganter",
    gladiac	=> "Andreas Schneider",
    hamish	=> "Hamish <hamish_b${t}yahoo.com>",
    loom	=> "Christoph Metz <loom${t}mopper.de>",
    pollardd	=> "\"David Pollard\" <david.pollard${t}optusnet.com.au>",
    robstewart	=> "Rob Stewart",
};

my %unknown_users=();
sub print_user_line($$){
    my $user = shift;
    my $date = shift;

    if ( $user ) {
	my $full_name= $user2full_name->{$user};
	if ( ! $full_name ) {
	    $unknown_users{$user}++;
	    $full_name = $user;
	}
	$full_name =~ s/^\((.*)\)$/$1/; #  (no author) --> no author
	if ( $full_name =~ m/\@/ ) {
	    $full_name =~ s/\@/ via the domain /;
	    $full_name =~ s/</(/;
	    $full_name =~ s/>/)/;
	}
	if ( $full_name !~ m/\@/ ) {
	    #my $fake_e_mail=$full_name;
	    #$fake_e_mail=~ s/ /_/g;
	    #$full_name = "$full_name <$fake_e_mail-fake-tmf\@gpsdrive.de>";
	    if ( $project_name =~ "gpsdrive" ) {
		$full_name = "$full_name via GPSdrive discussion list <gpsdrive\@lists.gpsdrivers.org>";
	    } else {
		$full_name = "$full_name via osm-dev List <dev\@openstreetmap.org>";
	    }
	}
	print $fo "\n -- $full_name  $date_date $date_time $date_offset\n\n";
    }
}

my $commitmessage_seen=0;
my $entry_no=0;
while ( my $line = $fi->getline()) {
    if ( $line =~ m/^-+$/) {
	if ( $entry_no && ! $commitmessage_seen ) {
	    print $fo "   * no Commit Message\n";
	    print $fo "\n"
	}
	$entry_no++;
	$commitmessage_seen=0;
	next;
    } elsif ( $line =~ m/^r\d+.*\|.*\|.*line/ ) {
	print_user_line($user,$date);
	($rev,$user,$date,$lines) = split(m/\s*\|\s*/,$line);
	($date_dummy,$date_time,$date_offset,$date_date) = split(m/\s+/,$date,4);
	$date_date =~ s/[\(\)]//g;
	$rev =~ s/^r//;

	print $fo "$project_name (${prefix}$rev) unstable; urgency=low\n\n";
    } elsif ( $line=~ m/^\s*$/ ) {
#	print $fo "\n";	
    } else {
	print $fo "   * $line";
	$commitmessage_seen++;
    }
};
print_user_line($user,$date);

$fi->close();
$fo->close();

if ( $debug ) {
    if ( keys %unknown_users ) {
	warn "Unknown Users:\n\t".
	    join("\n\t",sort keys %unknown_users).
	    "\n";
    }
};

exit 0;

__END__
=head1 NAME

svn_log2debian_changelog.pl - convert svn log to a debian/changelog

=head1 SYNOPSIS

svn_log2debian_changelog.pl tries to fetch the current svn changelog and 
convert it into a valid debian changelog.


svn_log2debian_changelog.pl --project_name="openstreetmap-utils" --prefix="2.10svn" --append

    --prefix=<prefix>
      Prefix is added as a prefix in front of each svn-revision to 
      get the debian revision number.

    --append
         Append to an existing File

    --debug
         also printout which users could not be inserted with there full name



=head1 RESULT

 #############################################
 # Example of the result:

 openstreetmap-utils (7572) unstable; urgency=low

  * Initial Version
  
 -- Joerg Ostertag (Debian Packages) via GPSdrive discussion list <gpsdrive\@lists.gpsdrivers.org>  Fri, 1 Jan 2007 07:05:36 +0100

