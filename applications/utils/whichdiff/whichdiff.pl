#!/usr/bin/perl

use LWP::UserAgent;
use strict;
use warnings;

my $verbose = 0;
my $tofind = $ARGV[0];
if ($tofind eq "-v") { $verbose = 1; $tofind= $ARGV[1]; }

if (!$tofind)
{
    print "usage: perl whichdiff.pl nodeid\n";
    print "finds minutely status file to use if your max node is the given id\n";
    exit;
}

my $ua = LWP::UserAgent->new;

my $topdirs = [];
my $checked = {};
my $basedir = "http://planet.openstreetmap.org/replication/minute/";

my $getcount = 0;
my $getlimit = 500;

my $latest = $ua->get($basedir."/state.txt");
die $latest->status_line unless($latest->is_success);
my $cnt = $latest->content;
$cnt =~ m!sequenceNumber=(\d+)! or die ("cannot read latest sequence number");
my $latestdiff = sprintf("%03d/%03d/%03d.osc.gz", $1/1000000, ($1%1000000)/1000, ($1%1000));
my $latestnode = firstnode($latestdiff);

printf "first node created in latest .osc.gz file is $latestnode\n" if ($verbose);

if (($latestnode - $tofind) > 100000000)
{
    printf(STDERR "current OSM node IDs are around $latestnode and you are looking for $tofind\n");
    printf(STDERR "that is too long ago.\n");
    exit;
}

my $root = $ua->get($basedir);
die $root->status_line unless($root->is_success);

$cnt = $root->content;

while($cnt =~ m!<a href="(\d+)/">(\d+)/</a>!gm)
{
    push(@$topdirs, $1);
    printf("topdir $1\n") if ($verbose);
}

my $subdirs = [];
while(my $td = pop(@$topdirs))
{
    printf("get $basedir/$td\n") if ($verbose);
    my $subdir = $ua->get("$basedir/$td/");
    die unless($subdir->is_success);
    $cnt = $subdir->content;
    while($cnt =~ m!<a href="(\d+)/">(\d+)/</a>!gm)
    {
        push(@$subdirs, $td."/".$1);
    }
}

my @a = sort(@$subdirs);

while(my $sd = pop(@a))
{
    printf("pop $sd\n") if ($verbose);
    my $files = [];
    my $index = $ua->get("$basedir/$sd/");
    die unless($index->is_success);
    $cnt = $index->content;

    while($cnt =~ m!<a href="(\d+\.osc\.gz)">(\d+)\.osc\.gz</a>!gm)
    {
        unshift(@$files, $sd."/".$1);
    }
    die unless (scalar @$files);

    my $first = 0;
    my $firstfirstnode = firstnode($files->[$first]);
    next if ($firstfirstnode == 0 || $firstfirstnode > $tofind);
    my $last = scalar(@$files) -1;
    my $lastfirstnode = firstnode($files->[$last]);

    found($files->[$last]) if ($lastfirstnode <= $tofind && $lastfirstnode > 0);

    while(1)
    {
        my $mid = ($first + $last) / 2;
        my $midfirstnode = firstnode($files->[$mid]);
        if ($midfirstnode > $tofind)
        {
            $last = $mid;
            $lastfirstnode = $midfirstnode;
        }
        elsif ($midfirstnode < $tofind)
        {
            if ($checked->{$files->[$mid+1]})
            {
                found($files->[$mid]);
            }
            $first = $mid;
            $firstfirstnode = $midfirstnode;
        }
        elsif ($midfirstnode == $tofind)
        {
            found($files->[$mid]);
        }
    }
}

sub firstnode
{
    my ($file) = shift;
    die("made over $getcount requests and didn't find anything") if ($getcount++>$getlimit);
    $checked->{$file} = 1;
    printf("check $basedir/$file\n") if ($verbose);
    open (I, "wget -qO- $basedir/$file | zcat |") or die;
    my $cr = 0;
    my $id = 0;
    while(<I>)
    {
        if (m!<create>!)
        {
            $cr=1;
        } 
        elsif ($cr) 
        {
            if (m!</create>!)
            {
                $cr=0;
            }
            elsif (m!<node id="(\d+)"!)
            {
                $id = $1;
                last;
            }
        }
    }
    die("oh dear, no nodes created in diff $basedir/$file, OSM is doomed") if ($id==0);
    print "firstnode $file = $id\n" if ($verbose);
    return $id;
}

sub found
{
    my ($file) = shift;
    print "node $tofind found in file $file\n" if ($verbose);
    $file =~ /(.*).osc.gz/;
    my @components = split(/\//, $1);
    my $decrement_which = scalar(@components) - 1;
    my $decrement_howmuch = 2;
    DEC:
    {
        $components[$decrement_which] -= $decrement_howmuch;
        if ($components[$decrement_which] < 0)
        {
            $components[$decrement_which--] += 1000;
            $decrement_howmuch = 1;
            redo DEC;
        }
    }
    $file = join("/", @components).".state.txt";
    print "therefore, use status file $file:\n" if ($verbose);
    my $r = $ua->get($basedir.$file);
    print($r->content);
    exit;
}

