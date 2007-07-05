#!/usr/bin/perl

use LWP;
use Geo::Proj4;
use GD;
use DBI;
use Data::Dumper;
use strict;

my $ua = LWP::UserAgent->new();
my $dsn="DBI:mysql:host=localhost:database=osmhistory";
my $dbh = DBI->connect($dsn) or die;
my $destpath = "/var/www/webspace/gryph.de/openstreetmap.gryph.de/history/images/";
my $thumbpath = "/var/www/webspace/gryph.de/openstreetmap.gryph.de/history/thumbnails/";

my $colormap = { 
    "0" => [ 255, 0, 0 ], # red
    "1" => [ 255, 255, 0 ], # yellow
    "2" => [ 255, 255, 255 ], # white
    "3" => [ 0, 0, 255 ], # blue
};

my $query = <<EOF;
select * 
from     jobs 
where    status='waiting'
order by date_entered asc
limit    1
EOF

my $sth = $dbh->prepare($query) or die;
my $sth_planet = $dbh->prepare("select filedate from planets") or die;

my $sth_begin = $dbh->prepare(<<EOF) or die;
update jobs 
set status='processing', 
date_started=? 
where id=?
EOF

my $sth_failure = $dbh->prepare(<<EOF) or die;
update jobs 
set status='failed', 
errmsg=?, 
date_finished=? 
where id=?
EOF

my $sth_success = $dbh->prepare(<<EOF) or die;
update jobs 
set status='finished', 
date_finished=?,
width=?,
height=?,
max_nodes=?,
filename=?,
filesize=?,
num_frames=?
where id=?
EOF

while(1)
{
    if (-f "/tmp/hqrun.terminate")
    {
        print "/tmp/hqrun.terminate exists, aborting\n";
        exit;
    }
    $sth->execute();
    if (my $row = $sth->fetchrow_hashref)
    {
        my $input = {};
        foreach my $key(keys(%$row))
        {
            $input->{$key} = $row->{$key};
        }

        my @filedates = get_filedates();
        my @usedates;
        my $used = {};

        my $lastdate = defined($input->{"todate"}) ? $input->{"todate"} : $filedates[$#filedates];
        my $firstdate = defined($input->{"fromdate"}) ? $input->{"fromdate"} : $filedates[0];
        $lastdate = $lastdate + $input->{"frequency"};
        while(1)
        {
            my $want = todays($lastdate) - $input->{"frequency"};
            my $bestdiff = 9999999;
            my $thisdate;
            foreach my $date(@filedates)
            {
                next if ($used->{$date});
                next if ($date < $firstdate);
                next if ($date > $lastdate);
                my $diff = abs($want-todays($date));
                if ($diff<$bestdiff)
                {
                    $bestdiff = $diff;
                    $thisdate = $date;
                }
            }
            last if ($bestdiff == 9999999);
            unshift(@usedates, $thisdate);
            $used->{$thisdate} = 1;
            $lastdate = $thisdate;
        }

        $input->{"filedates"} = \@usedates;
        $sth_begin->execute(time(), $input->{"id"});
        my $output = doit($input);
        if (ref($output) ne "HASH")
        {
            # print "fail: $output\n";
            $sth_failure->execute($output, time(), $input->{"id"});
        }
        else
        {
            my $filename = $destpath . $input->{"id"} . ".gif";
            system "mv ".$output->{"filename"}." ".$filename;
            my $filesize = -s $filename;
            system "mv ".$output->{"thumbname"}." ".$thumbpath.$input->{"id"} . ".gif";
            $sth_success->execute(time(), $output->{"width"}, $output->{"height"}, $output->{"max_nodes"}, $input->{"id"}.".gif", $filesize, scalar(@usedates)+1, $input->{"id"});
        }
    }
    else
    {
        sleep 30;
    }
}

sub doit
{
    my $input = shift;
    my $output = {};
    my @tmpfiles;
    my $outfile = "/tmp/hqrun.out.$$.gif";
    my $thumbfile = "/tmp/hqrun.thumb.$$.gif";

    my $proj;
    if ($input->{"projection"} == 1)
    {
        $proj = Geo::Proj4->new(proj => "latlong")
            or return "parameter error: ".Geo::Proj4->error;
    }
    elsif ($input->{"projection"} == 2)
    {
        $proj = Geo::Proj4->new(proj => "merc", ellps => "WGS84")
            or return "parameter error: ".Geo::Proj4->error;
    }
    else
    {
        return "invalid 'projection' parameter: ".$input->{"projection"};
    }

    return "minlat > maxlat" if ($input->{'minlat'} > $input->{'maxlat'});
    return "minlon > maxlon" if ($input->{'minlon'} > $input->{'maxlon'});
    return "lat range too large (max 90deg)" if ($input->{'maxlat'} - $input->{'minlat'} > 90);
    return "lon range too large (max 120deg)" if ($input->{'maxlon'} - $input->{'minlon'} > 120);

    return "bad minlat" if ($input->{'minlat'} > 90 or $input->{"minlat"} < -90);
    return "bad maxlat" if ($input->{'maxlat'} > 90 or $input->{"maxlat"} < -90);
    return "bad minlon" if ($input->{'minlon'} > 180 or $input->{"minlon"} < -180);
    return "bad maxlon" if ($input->{'maxlon'} > 180 or $input->{"maxlon"} < -180);

    return "width too large (max 1600)" if ($input->{'width'} > 1600);
    return "height too large (max 1200)" if ($input->{'height'} > 1200);

    my ($min_e, $min_n) = $proj->forward($input->{'minlat'}, $input->{'minlon'});
    my ($max_e, $max_n) = $proj->forward($input->{'maxlat'}, $input->{'maxlon'});

    my ($width_e, $height_n) = ($max_e - $min_e, $max_n - $min_n);

    return "width is 0" if ($width_e == 0);
    return "height is 0" if ($height_n == 0);

    my $scale;
    my $height;
    my $width;

    if ($input->{"width"} >0)
    {
        $width = $input->{"width"};
        $scale = $width_e / $width; 
        $height = int($height_n / $scale + .5);
    }
    else
    {
        $height = $input->{"height"};
        $scale = $height_n / $height; 
        $width = int($width_e / $scale + .5);
    }

    $output->{"width"} = $width;

    my $statusheight = 35;
    $output->{"height"} = $height + $statusheight;

    # initialize images
    my $images = {};
    $images->{0}->{"img"} = new GD::Image($width, $height + $statusheight, 0);
    for (my $i = 0; $i < scalar(@{$input->{"filedates"}}); $i++)
    {
        my $key = $input->{"filedates"}->[$i];
        $images->{$key}->{"img"} = new GD::Image($width, $height + $statusheight, 0);
        my ($r, $g, $b) = @{$colormap->{$input->{"color"}}};
        $images->{$key}->{"col"} = $images->{$key}->{"img"}->colorAllocate($r, $g, $b);
        $images->{$key}->{"txt"} = $images->{$key}->{"img"}->colorAllocate(255,255,255);
        $images->{$key}->{"bgr"} = $images->{$key}->{"img"}->colorAllocate(100,100,100);
    }

    my @tmp = sort keys(%$images);
    my $minfd = $tmp[1]; 
    my $maxfd = $tmp[$#tmp];
    my $request;

    if ($input->{"bgimage"} == 1)
    {
        $request = "http://labs.metacarta.com/wms/vmap0?service=WMS&request=GetMap&srs=EPSG:4326&format=image/jpeg&version=1.1.1&layers=basic";
    }
    elsif ($input->{"bgimage"} == 2)
    {
        $request = "http://onearth.jpl.nasa.gov/wms.cgi?request=GetMap&layers=global_mosaic&styles=&srs=EPSG:4326&format=image/jpeg";
    }
    my $image;

    if ($request ne "")
    {
        $request = $request . sprintf("&bbox=%f,%f,%f,%f&width=%d&height=%d", 
                $input->{"minlon"}, $input->{"minlat"}, 
                $input->{"maxlon"}, $input->{"maxlat"}, $width, $height);

        # print "GET $request\n";
        my $response = $ua->get($request);
        # print "response: ".$response->status_line."\n";
        if (!$response->is_success)
        {
            return "WMS server error: ".$response->status_line;
        }
        unless ($image = GD::Image->newFromJpegData($response->content, 0))
        {
            return "WMS server error: not returned a valid image";
        }
    }
    else
    {
        $image = GD::Image->new($width, $height + $statusheight, 0);
        my $bgr = $image->colorAllocate(100,100,100);
        $image->filledRectangle(0, 0, $width, $height + $statusheight, $bgr);
    }

    foreach my $key(keys %$images)
    {
        $images->{$key}->{"img"}->copy($image, 0, 0, 0, 0, $width, $height);
    }

    my @buckets;

    for (my $lat = int($input->{"minlat"}*2); $lat <= int($input->{"maxlat"}*2)+1; $lat++)
    {
        for (my $lon = int($input->{"minlon"}*2); $lon <= int($input->{"maxlon"}*2)+1; $lon++)
        {
            push(@buckets, ($lat+180)*720+$lon+360);
        }
    }

    my $buckets = join(",", @buckets);
    my $mila = int($input->{"minlat"} * 10000);
    my $milo = int($input->{"minlon"} * 10000);
    my $mala = int($input->{"maxlat"} * 10000);
    my $malo = int($input->{"maxlon"} * 10000);

    my $query =<<EOF;
select lat, lon, fromdate, todate
from   nodes
where  bucket in ($buckets)
and    fromdate <= $maxfd
and    todate >= $minfd
and    lat >= $mila
and    lat <= $mala
and    lon >= $milo
and    lon <= $malo;
EOF

    #print "$query\n";
    my $sth = $dbh->prepare($query) or return $dbh->errstr;
    $sth->execute() or return $dbh->errstr;

    my ($x, $y);
    my $pixel = $input->{"pixel"};

    while (my $row = $sth->fetchrow_arrayref)
    {
        my ($x, $y) = $proj->forward($row->[0] / 10000, $row->[1] / 10000);
        $x = int(($x-$min_e)/$scale);
        $y = $height - int(($y-$min_n)/$scale);
        foreach my $key(keys(%$images))
        {
            if ($row->[2] <= $key && $row->[3] >= $key)
            {
                if ($pixel == 0)
                {
                    $images->{$key}->{"img"}->setPixel($x, $y, $images->{$key}->{"col"});
                }
                elsif ($pixel == 1)
                {
                    $images->{$key}->{"img"}->rectangle($x, $y, $x+1, $y+1, $images->{$key}->{"col"});
                }
                elsif ($pixel == 2)
                {
                    $images->{$key}->{"img"}->rectangle($x-1, $y-1, $x+1, $y+1, $images->{$key}->{"col"});
                }
                else
                {
                    $images->{$key}->{"img"}->filledEllipse($x, $y, 5, 5, $images->{$key}->{"col"});
                }
                $images->{$key}->{"cnt"}++;
            }
        }
    }

    my $maxnodes = 0;
    foreach my $key(keys %$images)
    {
        $maxnodes = $images->{$key}->{"cnt"} if ($images->{$key}->{"cnt"} > $maxnodes);
    }
    $output->{"max_nodes"} = $maxnodes;
    my $digitspace = 7 * length($maxnodes);
    my $finalframe; 

    foreach my $key(sort keys %$images)
    {
        my $i = $images->{$key}->{"img"};
        $finalframe = $i;
        my $d = sprintf "20%02d-%02d-%02d", $key/10000, ($key%10000)/100, $key%100;
        my $c = $images->{$key}->{"cnt"};

        my $scaleleft = todays($minfd);
        my $scaleright = todays($maxfd);
        my $barwidth = ($width - 120 - $digitspace);
        my $barscale = $barwidth / ($scaleright - $scaleleft);
        my $thisbar = (todays($key)-$scaleleft)*$barscale;
#printf "scale left $scaleleft right $scaleright this date %d gives %d\n", todays($key),$thisbar;
        $i->filledRectangle(0,$height+1,$width,$height+$statusheight,$images->{$key}->{"bgr"});
        $i->filledRectangle(110+$digitspace,$height+11,$thisbar+110+$digitspace,$height+20, $images->{$key}->{"txt"});

        $i->string(gdMediumBoldFont, 10, $height + 10, "$d ($c)", $images->{$key}->{"txt"});
#add_frame_data($i);
#    $gifdata .= $i->gifanimadd(undef, 0, 0, 100, 1, $prev);
#    $prev = $i;
        my $tmp = sprintf("/tmp/hqrun.tmp.$$.frame-%06d.gif", $key);
        open(BLA, ">$tmp") or return $!;
        print(BLA $i->gif());
        close(BLA);
        push(@tmpfiles, $tmp);
    }

    my $loop = ($input->{"loopflag"}) ? "--loopcount" : "";
    system "gifsicle --colors 256 $loop --delay ".$input->{"delay"}." -O2 ".join(" ", @tmpfiles)." > $outfile";
    foreach my $tmp (@tmpfiles) { unlink $tmp; }
    $output->{"filename"} = $outfile;

    my $tw = 160;
    my $th = 160 / $width * ($height);
    my $thumb = GD::Image->new($tw, $th, 0); 
    $thumb->copyResampled($finalframe, 0, 0, 0, 0, $tw, $th, $width, $height);
    open(BLA, ">$thumbfile") or return $!;
    print(BLA $thumb->gif());
    close(BLA);
    $output->{"thumbname"} = $thumbfile;
    
    return $output;
}

sub todays
{
    my $d = shift;
    return 365 * int($d/10000)+30*int(($d%10000)/100)+$d%100;
}

sub get_filedates
{
    my @ret;
    $sth_planet->execute();
    while(my $row = $sth_planet->fetchrow_arrayref())
    {
        push(@ret, $row->[0]);
    }
    return @ret;
}
