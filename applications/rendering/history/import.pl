#!/usr/bin/perl

use DBI;
use strict;

my $planet = int($ARGV[0]);
if ($planet<1)
{
    die "please specify planet filedate on cmd line";
}

my $dsn="DBI:mysql:host=localhost:database=osmhistory";
my $dbh = DBI->connect($dsn) or die;

$dbh->do(<<EOF) or die;
create table if not exists planets
(
    filedate integer,
    importdate integer,
    comment varchar(255)
);
EOF

$dbh->do(<<EOF) or die;
create table if not exists nodes
(
    id integer,
    lat integer,
    lon integer,
    bucket integer,
    fromdate integer,
    todate integer,
    key(id),
    key(bucket)
);
EOF

my $query = "select max(filedate) from planets";
my $sth = $dbh->prepare($query) or die;
my $lastfiledate;
$sth->execute() or die;
if (my $row = $sth->fetchrow_arrayref)
{
    $lastfiledate = $row->[0];
}

if ($lastfiledate >= $planet)
{
    printf "last planet in db is %06d, refusing to process %06d\n",
        $lastfiledate, $planet;
    exit;
}

my $uquery = "update nodes set todate=? where id=? and todate=?";
my $usth = $dbh->prepare($uquery) or die;
my $iquery = "insert into nodes (id,lat,lon,bucket,fromdate,todate) values(?,?,?,?,?,?)";
my $isth = $dbh->prepare($iquery) or die;

printf "";
$| = 1;
printf "running...";

my $c = 1;
my $i;
my $u;

while(<STDIN>)
{
    last if (/<segment/);
    if (/^\s*<node id=.(\d+).+lat=.([0-9.Ee-]+).+lon=.([0-9.-Ee-]+)./)
    {
        my ($id, $lat, $lon) = ($1, $2, $3);
        my $bucket = (int($lat*2)+180) * 720 + int($lon*2) + 360;
        my $do_insert=1;
        if (defined($lastfiledate))
        {
            if ($usth->execute($planet, $id, $lastfiledate) > 0)
            {
                $u++;
                $do_insert=0;
            }
        }
        if ($do_insert)
        {
            $i++;
            $isth->execute($id,int($lat*10000),int($lon*10000),$bucket,$planet,$planet) or die;
        }

        if ($c++ % 1000 == 0)
        {
            printf "\r%dk nodes processed...", $c/1000;
        }
    }
}

printf "\rdone - %d nodes processed, %d inserts, %d updates\n", $c, $i, $u;

$query = "insert into planets (filedate,importdate) values($planet,".time().")";
$sth = $dbh->prepare($query) or die;
$sth->execute() or die;

