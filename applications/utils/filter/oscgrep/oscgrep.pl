#!/usr/bin/perl

# Written by Frederik Ramm <frederik@remote.org>, public domain.
sub HELP_MESSAGE { print <<EOF;
$0 is a simple script that helps you find certain objects in .osc files.

It is called like this:

perl $0 [-a action] [-t type] [-s shapename] [-q] [regex] [file] [file...]

where
  "action" is either create, modify, or delete;
  "type" is either way, node, or relation;
  "regex" is any regular expression 
  "file" is an .osc file (also supports .osc.gz or .osc.bz2)

It lists all elements from the .osc file that match the given action, type, and
regex (regex is applied against full XML content of the object). If -a, -t or
regex are omitted, dumps all actions/all types/all content; if file is omitted,
reads from stdin.

If the -s parameter is specified, then $0 will, in addition to its normal 
output, also write a shape file under the given name which contains all
nodes found. For example, the parameters "-a delete -s myshape user=.fred"
will create a shapefile in myshape.shp that contains all nodes deleted
by users whose name begins with "fred". 

Add -q to suppress normal output.
EOF
exit;
}
sub VERSION_MESSAGE {};

use strict;
use Time::Local;
use Getopt::Std;

my $options = {};
getopts("a:t:s:q", $options);
my $grep;
$grep = shift(@ARGV) unless (-f $ARGV[0]);

my $shp;
if (defined($options->{"s"}))
{
    eval "use Geo::Shapelib qw/:all/; 1; " or die("-s option requires perl module Geo::Shapelib - stopped");
    $shp = new Geo::Shapelib {
        Name => $options->{"s"}, 
        Shapetype => 1,
        FieldNames => ['File','Unixtime','Action','User'],
        FieldTypes => ['String:50','Integer:10','String:10','String:50']
    };

}

push(@ARGV, "-") if (scalar(@ARGV) == 0);

while(my $current_file = shift(@ARGV))
{
    if (($current_file ne "-") && ($current_file !~ /\.osc(\.gz|\.bz2)?$/))
    {
        warn ("file $current_file has unrecognized name (expected: .osc, .osc.gz, .osc.bz2) - ignored");
        next;
    }

    unless (open(F, ($1 eq ".bz2") ? "bzcat $current_file|" : ($1 eq ".gz") ? "zcat $current_file|" : $current_file))
    {
        warn ("cannot read from $current_file - ignored");
        next;
    }

    my $seek;
    my $buffer;
    my $action;
    while(<F>)
    {
        if (/<(modify|create|delete)/)
        {
            $action = $1;
        }
        elsif (/<(node|way|relation).*?(\/)?>/)
        {
            if ($2 eq "/")
            {
                out($1, $action, $current_file, $_);
            }
            else
            {
                $seek = $1;
                $buffer = $_;
            }
        }
        elsif (/<\/$seek>/)
        {
            out($seek, $action, $current_file, $buffer . $_);
        }
        else
        {
            $buffer .= $_;
        }
    }
    close(F);
}
$shp->save() if (defined($shp));

sub out
{
    my ($type, $action, $file, $content) = @_;
    return if (defined($options->{"a"}) && $options->{"a"} ne $action);
    return if (defined($options->{"t"}) && $options->{"t"} ne $type);
    return if (defined($grep) && $content !~ /$grep/);
    print "<$action>\n$content</$action>\n" unless($options->{"q"});

    if (defined($shp) && ($type eq "node"))
    {
        $content =~ /lat=["']([0-9.-]+)["']/;
        my $lat=$1;
        $content =~ /lon=["']([0-9.-]+)["']/;
        my $lon=$1;
        $content =~ /timestamp=["'](\d{4})-(\d\d)-(\d\d)T(\d\d):(\d\d):(\d\d)Z["']/;
        my $uts = timelocal($6,$5,$4,$3,$2-1,$1-1900);
        $content =~ /user=["']([^"']+)["']/;
        my $user=$1;
        push @{$shp->{Shapes}},{ Vertices => [[$lon,$lat,0,0]] };
        $file =~ /.*\/(.{1,50})/;
        push @{$shp->{ShapeRecords}}, [$1, $uts, $action, $user];
    }
}
