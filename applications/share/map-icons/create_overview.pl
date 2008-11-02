#!/usr/bin/perl
#####################################################################
#
#  This script handles the XML-Files for the POI-Types in gpsdrive.
#  It has to be run from the data directory.
#  
#  Default actions, when no options are given:
#  - Create overview.html from the XML-File to show all
#    available poi_types and icons.

#use diagnostics;
use strict;
use warnings;

use utf8;
use IO::File;
use File::Find;
use Getopt::Std;
use Pod::Usage;
use File::Basename;
use File::Copy;
use File::Path;
use Data::Dumper;
use XML::Simple;
use Image::Info;
use Cwd;

our ($opt_b,$opt_c, $opt_h, $opt_i, $opt_j, $opt_l, $opt_n, $opt_p, $opt_r,$opt_s,$opt_v, $opt_D, $opt_F, $opt_L, $opt_P,$opt_S) = 0;
getopts('bchijlnprsvF:D:L:P:S:') or $opt_h = 1;
pod2usage( -exitval => '1',  
           -verbose => '1') if $opt_h;

$opt_b ||=0;
my $cwd = cwd;
my $languages = $opt_L || "en,de";
my $base_dir = $opt_D || cwd;
my $file_xml = $opt_F || './icons.xml';
my $i = 0;
my $poi_reserved = 30;
my $poi_type_id_base = $poi_reserved;
my $VERBOSE = $opt_v;
$opt_P ||= "overview";

my @ALL_TYPES = qw(square.big square.small classic.big classic.small svg svg-twotone japan);

my $SVN_STATUS={};
my $SVN_VERSION = '';

my $svn_status2color={
    "_" =>'',
    "?" => "blue",
    "M" => "green",
    "D" => "red",
    "C" => "purple",
    "derived" => "#E5E5E5",
};

sub html_head($);
sub get_svn_status($);
sub update_overview($$);

#####################################################################
#
#  M A I N
#
#
unless (-e $file_xml)
{
  create_xml();	# Create a new XML-File if none exists
}
my $svn_base_dir = $opt_S;
if ( ! $svn_base_dir ){
    $svn_base_dir= $base_dir ;
    $svn_base_dir =~ s,(/build|build/|build),,;
}
get_svn_status($svn_base_dir);

my $rules = XMLin("$file_xml",ForceArray => ['description','title','condition']);
my @rules=@{$rules->{rule}};


for my $lang ( split ( ",", $languages)){
    update_overview($lang,\@rules);	 # update html overview from XML-File
}

exit (0);

##################################################################
# Get the licence from a svg File
# RETURNS: 
#     'PD' for PublicDomain
#     '?'  if unknown
sub get_svg_license($){
    my $icon_file=shift;
    my $icon = XMLin($icon_file,ForceArray => ['description','title','condition']);
    my $license = $icon->{'metadata'}->{'rdf:RDF'}->{'cc:Work'}->{'cc:license'}->{'rdf:resource'};
    #print Dumper(\$license);
    return "?" unless $license;
    return "PD"   if $license eq "http://web.resource.org/cc/PublicDomain";
    return "PD"   if $license =~ m,http://creativecommons.org/licenses/publicdomain,;
    return "CONV" if $license =~ m,Converted from http://svn.openstreetmap.org/applications/share/map-icons,;
    $license =~ s,http://creativecommons.org/licenses/LGPL/?,LGPL-,;
    return $license;
}

sub get_png_license($){
    my $filename = shift;
    my $comment = get_png_comment($filename);
    return "?" unless $comment;
    $comment =~ s/Created with The GIMP//;
    $comment =~ s,Created with Inkscape \(http://www.inkscape.org/\),,;
    $comment =~ s,Generator: Adobe Illustrator 10.0\, SVG Export Plug-In \. SVG Version: [\d\.]+ Build \d+\),,g;
    $comment =~ s/^\s*//g;
    $comment =~ s/\s*$//g;
    $comment =~ s,http://creativecommons.org/licenses/LGPL/?,LGPL-,;
	
    print "Comment($filename): $comment\n" if $VERBOSE && $comment;
    return "PD"   if $comment =~ m/Public.*Domain/i;
    return "CONV" if $comment =~ m,converted from ,;
    return "?" unless $comment;
    return $comment if $comment && $comment =~ m/license/;
}

# Get Comment Field from a PNG
sub get_png_comment($){
    my $filename = shift;
    my ($s1,$s2)=Image::Info::image_info($filename);
    my $comment = $s1->{'Comment'};
}

#####################################################################
#
# Get the "svn status" for all icons Files
#
sub get_svn_status($) {
    my $svn_base_dir = shift;

    return unless $opt_s || $opt_n;

    $SVN_VERSION = `svnversion $svn_base_dir`;
    chomp($SVN_VERSION);
    $SVN_VERSION =~ s/M//;
    my $svn_status = `svn -v status $svn_base_dir`;
    for my $line (split(/[\r\n]+/,$svn_status)) {
	chomp $line;
	$line =~ s/^ /_/;
	$line =~ s,$svn_base_dir/,,;
	my ($status,$rev,$rev_ci,$user,$file) = (split(/\s+/,$line),('')x5);
	if ( $status eq "?" ) {
	    $file = $rev; 
	    $rev ='';
	}
	$SVN_STATUS->{$file}="$status,$rev,$rev_ci,$user";
	#print STDERR "SVN STATUS: $status,$rev,$rev_ci,$user	'$file'\n" if $VERBOSE;
    }
}

sub html_head($){
    my $lang = shift;
    # html 'template'
    my $html_head =
	"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n".
	"  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n".
	"<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" ".
	"xml:lang=\"en\">\n".
	"<head>\n".
	"<meta http-equiv=\"Content-Type\" ".
	"content=\"text/html; charset=utf-8\" />\n".
	"\n".
	"<title>Available POI-Types in gpsdrive</title>\n".
	"<style type=\"text/css\">\n".
	"       table            { width:100%;  background-color:#fff8B2; }\n".
	"	tr               { border-top:5px solid black; }\n".
	"	tr.id            { background-color:#6666ff; color:white; font-weight:bold; }\n".
	"	td.id            { text-align:right; vertical-align:top;}\n".
	"	td.icon          { text-align:center; vertical-align:top;}\n".
	"	td.status        { text-align:left; vertical-align:top;}\n".
	"	td.empty         { text-align:center; height:32px; }\n".
	"	img.square_big   { width:32px; height:32px; }\n".
	"	img.square_small { width:16px; height:16px; }\n".
	"	img.classic      { max-height:32px; }\n".
	"	img.svg          { max-height:32px; }\n".
	"	img.japan        { max-height:32px; }\n".
	"	span.desc        { font:x-small italic condensed }\n".
	"</style>\n".
	"</head>\n";
    $html_head .= "<body>\n";
    
    # Legende
    $html_head .= "<table border=\"1\">\n";
    $html_head .= "<tr>\n";
    if ( 1 ) { # Content with links
	$html_head .= "<td valign=\"top\">\n";
	if ( $lang eq "de" ) {
	    $html_head .= "<h3>Kategorien</h3>\n";
	} else {
	    $html_head .= "<h3>Categories</h3>\n";
	};

	$html_head .= "<font size=\"-2\"><ul>\n";
	#for my $rule (@{$rules}) {
	my %top_categories;
	for my $dir ( sort glob ( "$base_dir/*/*")) {
	    next unless -d $dir;
	    next if  $dir =~ m/CMakeFiles/;
	    my ($cat) = ($dir =~ m/.*\/(.+)(\.svg|\.png)?$/ );
#	    print "$cat\t$dir\n";
	    $top_categories{$cat}++;
	}
	my @top_categories;
	@top_categories = (sort keys %top_categories);
	for my $top_level ( @top_categories ) {
	    $html_head .= "	<li><a href=\"\#$top_level\">$top_level</a></li>\n";
	}
	$html_head .= "</font></ul>\n";
	$html_head .= "</td>\n";
    }

    $html_head .= "\n";
    $html_head .= "<td valign=\"top\">\n";

    # Links to other Versions
    if (0) {
	$html_head .= "\n";
	$html_head .= "<table border=\"1\">\n";
	$html_head .= "<td valign=\"top\">\n";
	$html_head .= "<a href=\"overview.en.html\">Without License Info in English</a><br/>\n";
	$html_head .= "<a href=\"overview.de.html\">Without License Info in German</a><br/>\n";
	$html_head .= "<a href=\"overview_lic.en.html\">With License Info in English</a><br/>\n";
	$html_head .= "<a href=\"overview_lic.de.html\">With License Info in German</a><br/>\n";
	$html_head .= "</td>\n";
	$html_head .= "</table>\n";
    };
    
    if ( $opt_l || $opt_c ) { # Add license Information
	# Legend for Colors
	$html_head .= "<font size=\"-1\">";
	if ( $lang eq "de" ) {
	    $html_head .= "<h3>Lizensen</h3>\n";
	} else {
	    $html_head .= "<h3>Licenses</h3>\n";
	}
	$html_head .= "<table border=\"1\">\n";
	$html_head .= "<tr><td><font size=\"-2\" color=\"lightgreen\" >lic:PD</font></td> <td><font size=\"-2\" >Public Domain License</font></td></tr>\n";
	$html_head .= "<tr><td><font size=\"-2\" color=\"lightgreen\" >lic:LGPL</font></td> <td><font size=\"-2\" >LGPL</font></td></tr>\n";
	$html_head .= "<tr><td><font size=\"-2\" color=\"red\">lic:?</font></td> <td><font size=\"-2\" >No license information available about this icon</font></td></tr>\n";
	$html_head .= "<tr><td><font size=\"-2\" color=\"red\"   >lic:</font></td> <td><font size=\"-2\" >License has no known/predefined category</font></td></tr>\n";
	$html_head .= "</table>\n";
	$html_head .= "</font>";
#	$html_head .= "</td>\n";
	}
    $html_head .= "\n";
    if ( $opt_s ) { # Add SVN Status Information
#	$html_head .= "<td valign=\"top\">\n";
	$html_head .= "<table border=\"1\">\n";
	for my $status ( keys %{$svn_status2color} ) {
	    $html_head .= "<tr><td bgcolor=\"$svn_status2color->{$status}\">SVN Status $status</td></tr>\n";
	}
	$html_head .= "</table>\n";
	}


    $html_head .= "</td>\n";
    $html_head .= "</tr>\n";
    $html_head .= "</table>\n";
    $html_head .= "\n";
    $html_head .= "\n";


    $html_head .= "<table border=\"$opt_b\">\n";
    $html_head .= "  <tr>";
    $html_head .= "    <th>ID</th>" if $opt_j;
    $html_head .= "    <th>Name</th>\n";
    $html_head .= "    <th>Path</th>\n" if $opt_p;
    my $cols_per_icon= 1;
    $cols_per_icon++ if $opt_s || $opt_n;
    $cols_per_icon++ if $opt_l;
    $html_head .= "    <th colspan=\"".($cols_per_icon*scalar(@ALL_TYPES))."\">Icons</th>\n";
    $html_head .= "    <th>Description</th>\n";
    $html_head .= "    <th>OSM Condition</th>\n";
    $html_head .= "  </tr>\n";
    return $html_head;
}

# Header with a list of all types used in one <tr> line
sub all_type_header(){
    my $all_type_header= "<tr>";
    $all_type_header .= "<td></td>" if $opt_j;
    $all_type_header .= " <td></td>";
    $all_type_header .= " <td></td>\n" if $opt_p;
    for my $type ( @ALL_TYPES  ) {
	my $txt=$type;
	$txt=~s/\.$//;
	$txt=~s/\./<br>/;
	$all_type_header .= " <td  valign=\"top\"><font size=\"-3\">$txt</font></td>\n";
	$all_type_header .= " <td><font size=\"-3\">lic</font></td>" if $opt_l;
	$all_type_header .= " <td><font size=\"-3\">svn</font></td>" if $opt_s || $opt_n;
    }
    $all_type_header .= " <td></td>\n";
    $all_type_header .= " <td></td>\n";
    $all_type_header .= " <td></td>\n";
    $all_type_header .= " </tr>\n\n";
    return $all_type_header;
}

#####################################################################
#
#  Update HTML Overview of available Icons and POI-Types
#
#
sub update_overview($$){
    my $lang  = shift || 'en';
    my $rules = shift;
    my $file_html = "$base_dir/${opt_P}.${lang}.html";

    print STDOUT "----- Updating HTML Overview '$file_html' -----\n";
    
    my %out;

    my $ID_SEEN={};
    for my $rule (@{$rules}) {
	#print Dumper(\$rule);
	my $content = '';
	my $names = $rule->{'condition'};
	my ($name) = grep { $_->{k} =~ /^poi|rendering$/ } @{$names};
	$name = $name->{v};
	my $id = $name;
	print "name: '$name'\n" if $VERBOSE;
	if ( ! $name ) {
	    warn "Undefined Name\n";
	    warn Dumper(\$rule);
	    next;
	}
	my $restricted = $rule->{'geoinfo'}->{'restricted'};

	if ( $id && defined($ID_SEEN->{$id}) && "$ID_SEEN->{$id}" ){
	    die "$id was already seen at $ID_SEEN->{$id}. Here in $name\n";
	};
	$ID_SEEN->{$id}=$name;

	if ( $restricted && not $opt_r ){
	    next;
	}

	my $title='';
	for my $t ( @{$rule->{'title'}||[]} ){
	    $title = $t->{content}
	    if $t->{'lang'} eq $lang && $t->{content};
	}
	
	my $descr ='';
	for my $d (@{$rule->{'description'}}) {
	    my $c = $d->{content};
	    if ($d->{'lang'} eq $lang && $c) {
		$descr = '<span class="desc">&nbsp;&nbsp;'.$c.'</span>';
	    }
	}
	
        my $conditions='';
	for my $c (@{$rule->{'condition'}}) {
	    next if $c->{k} eq "poi";
	    $conditions .= "$c->{k}=$c->{v}<br>";
	}

	my $icon = $name;
	my $ind = $name;

	# accentuate base categories
	my $header_line=0;
	if ($id !~ m/\./ || ( $icon !~ m,\.,) )	{
	    $content .= "  <tr><td>&nbsp;</td></tr>\n";
	    $content .=     all_type_header();
	    $content .= "  <tr class=\"id\">\n";
	    $content .= "     <td class=\"id\">$id</td>\n" if $opt_j;
	    $content .= "     <td>&nbsp;<a name=\"$name\">$name</a></td>\n";
	    $header_line++;
	} else {
	    my $level = ($icon =~ tr,\.,/,);
	    my $html_space = '';
	    while ($level)
	    { $html_space .='&nbsp;&nbsp;&nbsp;&nbsp;&rsaquo;&nbsp;'; $level--; };
	    $name =~ s,.*\.,,g;
	    $content .= "<tr>\n";
	    $content .= "    <td class=\"id\">$id</td>" if $opt_j;
	    $content .= "    <td>&nbsp;$html_space$name</td>\n";
	}

	# Add filename+path column
	$content .= "<td><font size=-4>$icon</font></td>\n" 
	    if $opt_p;

	# display all icons
	for my $type ( @ALL_TYPES  ) {
	    my $icon_s = "${type}/$icon.svg";
	    my $icon_p = "${type}/$icon.png";
	    my $icon_t = "${type}-png/${icon}.png";
	    my $class = $type;
	    $class =~ s/\./_/g;

	    my $icon_path_current;
	    if ( -s "$base_dir/$icon_t" ) { $icon_path_current = $icon_t; }
	    else {		$icon_path_current = $icon_p;   };
	    my $icon_path_svn="$svn_base_dir/$icon_path_current";
	    $icon_path_svn =~ s,/([^/]+)\.(...)$,/.svn/text-base/$1.$2.svn-base,;

	    my $svn_bgcolor='';
	    my $status_line = $SVN_STATUS->{$icon_s};
	    $status_line ||= $SVN_STATUS->{$icon_p};
	    $status_line ||= '';
	    my ($status,$rev,$rev_ci,$user,$file) =
		(split(/,/, $status_line),('')x5);
	    print STDERR "svn_status($icon_p): '$status_line'\n" if $VERBOSE;
	    
	    #print STDERR "svn_status($icon_p)[$icon_path_svn]: '$status'\n" if $VERBOSE;
	    if ( $status eq "" ) {
		if ( -s  $icon_path_svn # Im original svn Verzeichnis
		     || -s "$icon_s"
		     || -s "$icon_p"
		     || -s "$icon_t"
		     ) {
		    $svn_bgcolor='';
		} else {
		    $status = "derived";
#		    $svn_bgcolor=' bgcolor="#E5E5E5" ';
		}
	    } 
	    if ( $svn_status2color->{$status}  ) { 
		$svn_bgcolor=" bgcolor=\"$svn_status2color->{$status} \" ";
	    } else {
#		$svn_bgcolor=' bgcolor="red" ';
	    }
	    
	    $content .=  "    <td ";
	    my $empty= ! ( -s "$base_dir/$icon_p" or -s "$base_dir/$icon_s");
	    if ( $empty && ! $status  ){
		$svn_bgcolor='';
	    }
	    if ( $empty ) { # exchange empty or missing icon files with a char for faster display
		$content .=  " class=\"empty\" " unless $header_line;
	    } elsif ( $restricted && not $opt_r ){
		$content .=  " class=\"empty\" " unless $header_line;
	    } else {
		$content .=  " class=\"icon\" " unless $header_line;
	    }

	    # -------------- Add license Information Part 1
	    my $license='';
	    my $lic_color=' ';
	    my $lic_bgcolor=' ';
	    if ( ! $empty && ( $opt_l || $opt_c)  ) {
		if ( -s "$icon_s"  ) {
		    $license = get_svg_license($icon_s);
		} elsif ( -s "$icon_p" ) {
		    $license = get_png_license($icon_p);
		}
		$lic_color='red';
		$lic_color = ''            if $license eq "CONV";
		$lic_color = 'red'         if $license eq "?";
		$lic_color = 'lightgreen'  if $license eq "PD";
		$lic_color = 'lightgreen'  if $license =~ m/^LGPL/;
	    }

	    if ( ! $empty ) {
		if ( $opt_c && $lic_color ) {
		    $content .= " bgcolor=\"$lic_color\" ";
		} else {
		    $content .= $svn_bgcolor;
		}
	    }

	    $content .=  " >";


	    if ( $opt_s && $status ) { # modified icons .... we show old icon too
		$content .= "\n            <img src=\"$icon_path_svn\" /> ==> " 
		    if -s $icon_path_svn && $status =~ "M|D";
	    }
	    if ( $empty ) { # exchange empty or missing icon files with a char for faster display
		$content .=  ".";
	    } elsif ( $restricted && not $opt_r ){
		$content .=   "r";
	    } else {
		if ( -s "$base_dir/$icon_path_current" ){
		    $content .= "     <a href=\"$icon_path_current\" >\n";
		    $content .= "                 <img title=\"$name\" src=\"$icon_path_current\" class=\"$class\" alt=\"$name\" />";
		    $content .= "</a>";
		}
	    }
	    $content .= "</td>\n";



	    # -------------- Add license Information Part 2
	    if ( $opt_l ) {
		$content .= "                      <td>";
		if ( ! $empty  ) {
		    $content .= "<font color=\"$lic_color\" size=\"-2\">$license</font>";
		    print "License($type/$icon): $license\n"
			if $VERBOSE && $license && $license ne "?";
		}
		$content .= "</td>\n";

	    }

	    # ----------- add SVN Status Info 
	    if ($opt_s || $opt_n) {
		$content .= "<td class=\"status\">\n";
		if ( $status ) {
		    # Status Column
		    #$status_line =~ s/guenther/g/;
		    #$status_line =~ s/joerg/j/;
		    #$status_line =~ s/ulf/u/;
		    #$status_line =~ s/$SVN_VERSION//;
		    $content .= "<font size=\"-3\">";
		    $content .= "svn:$status<br>\n" if $opt_s && $status;
		    $content .= "$user<br>\n";
		    $content .= "rev: $rev_ci" if $rev_ci;
		    $content .= "</font>";
		}
		$content .= "</td>\n";
	    }

	}
	$content .= "    <td>$title<br>$descr</td>\n";
	$content .= "    <td><font size=-1>$conditions</font></td>\n";
	$content .= "  </tr>\n";
	$out{$ind} = $content;
    }  

    # create backup of old overview.html

    my $fo = IO::File->new(">$file_html");
    $fo ||die "Cannot write to '$file_html': $!\n";
    $fo->binmode(":utf8");
    print $fo html_head($lang);
    # sorted output
    foreach ( sort keys(%out) )  {
	print $fo $out{$_};
    }

    print $fo "</table>\n";
    if ( $opt_i ) {
	print $fo "<h3>Incomming Directories</h3>\n";
	
	for my $theme ( @ALL_TYPES ) {
	    my $ext = "png";
	    $ext = "svg" if $theme =~ m/svg|japan/;
	    print $fo "<br>\n";
	    print $fo "Incomming for $theme\n";
	    print $fo "<table border=\"1\">\n";
	    print $fo "<tr>\n";
	    my $count=0;
	    print STDERR "glob($theme/incomming/*.$ext)\n";
	    for my $icon ( glob("$theme/incomming/*.$ext" ) ){
		print STDERR "$icon\n" if $VERBOSE;
		my $name = $icon;
		$name =~ s/.*\/incomming\///;
		$name =~ s/\.(svg|png)$//;
		my $icon_t = $icon;
		$icon_t =~ s/\//-png\//;
		$icon_t =~ s/\.svg/\.png/;
		print STDERR "thumb: $icon_t\n" if $VERBOSE;
		$icon_t = $icon unless -s $icon_t;
		my $content = "     <a href=\"$icon_t\" >";
		$content .= "         <img alt=\"$icon\" title=\"$icon\" src=\"$icon_t\" />";
		$content .= "<br/>$name\n";
		$content .= "</a>\n";
		print $fo "    <td>$content</td>";

		if ( $count++ > 5) {
		    $count=0;
		    print $fo "</tr><tr>\n";
		}
	    }
	    print $fo "</tr>\n";
	    print $fo "</table>\n";

	}
    }


    print $fo "\n</body>\n</html>";
    $fo->close();
    return;

}


__END__


=head1 SYNOPSIS
 
create_overview.pl [-h] [-v] [-i] [-r] [-s] [-F XML-FILE] [-D DIR] [-P FILENAME_PREFIX]
 
=head1 OPTIONS
 
=over 2
 
=item B<--h>

Show this help

=item B<-F> XML-FILE

Set file, that holds all the necessary icon and poi_type information.
The default file is 'icons.xml'.

=item B<-D> DIRECTORY

The directory to search for the icons. Default it CWD (./)

=item B<-v>

Enable verbose output

=item B<-i>

Add incomming directory to the end of the 
overview.*.html file.

=item B<-j>

show internal gpsdrive-mysql id in html page

=item B<-l>

Add licence to overview where known

=item B<-c>

Add licence information in form of Background Colors

=item B<-r>

Include restricted icons in overview.html

=item B<-p>

Show path of Filename

=item B<-b>

Add Border to Table

=item B<-s>

 add svn status to overview
    grey is missing in svn
    green is modified
    red is any other condition
 this also shows the old and new icon if it is found in the 
 .svn/ directory

If the working path ends with /build this is truncated, since 
I expect the real svn co one level above.

=item B<-n>

Show the svn revision numbers and user too
This needs option -s to work

=item B<-L language>

Update only this language. Default is en,de

=item B<-P FILENAME-PREFIX>

Use this for the filename Prefix. Default: overview

=item B<-S SVN-BASE>

Use the Directory  SVN-BASE as Base for determining SVN Status

=back
