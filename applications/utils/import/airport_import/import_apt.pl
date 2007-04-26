#! /usr/bin/perl -w


use DBI;


$FEET_TO_METER = 0.3048;

$APT_VERSION="AptNav200701XP810";
$APT_FILE=$ENV{HOME} . "/osm/" . $APT_VERSION . "/apt.dat";
$MYSELF="airport_import " . qw($Revision$);


sub save_airport
{
    return if (!(defined($icao_code)));

    local $tablename = "airport_place_node";

    $sql = "select id from $tablename where icao = ?";
    $select_place_id_sth = $dbh->prepare($sql);
    $select_place_id_sth->bind_param(1, $icao_code);
    $select_place_id_sth->execute();

    @row = $select_place_id_sth->fetchrow_array;
    if ($#row == -1)
    {
	$sql = "insert into $tablename (icao) values (?)";
	$insert_sth = $dbh->prepare($sql);
	$insert_sth->bind_param(1, $icao_code);
	$insert_sth->execute();

	$insert_sth->finish();
    }

    $select_place_id_sth->finish();

    $sql = "select id from $tablename where icao = ?";
    $select_place_id_sth = $dbh->prepare($sql);
    $select_place_id_sth->bind_param(1, $icao_code);
    $select_place_id_sth->execute();
    @row = $select_place_id_sth->fetchrow_array;
    if ($#row > -1)
    {
	$place_node_id = $row[0];
	print "Place ID: '$place_node_id', '$icao_code', '$airport_name'\n";
    }

    #
    #  The Airport node itself
    #

    $min_lat =  999;
    $min_lon =  999;

    $max_lat = -999;
    $max_lon = -999;

    $sum_lat = 0;
    $sum_lon = 0;

    foreach (@ways)
    {
	@foo = split(/\s+/);

	$center_lat = $foo[1];
	$center_lon = $foo[2];

	$sum_lat = $sum_lat + $center_lat;
	$sum_lon = $sum_lon + $center_lon;

	$min_lat = $center_lat if ($center_lat < $min_lat);
	$min_lon = $center_lon if ($center_lon < $min_lon);

	$max_lat = $center_lat if ($max_lat < $center_lat);
	$max_lon = $center_lon if ($max_lon < $center_lon);

	create_way($place_node_id, $_);
    }

    $lat = $sum_lat / ( $#ways + 1 );
    $lon = $sum_lon / ( $#ways + 1 );

    $sql = "update $tablename set lat = ?, lon = ?, min_lat = ?, min_lon = ?, max_lat = ?, max_lon = ?, name = ? where id = ?";
    $update_place_sth = $dbh->prepare($sql);
    $update_place_sth->bind_param(1, $lat);
    $update_place_sth->bind_param(2, $lon);
    $update_place_sth->bind_param(3, $min_lat);
    $update_place_sth->bind_param(4, $min_lon);
    $update_place_sth->bind_param(5, $max_lat);
    $update_place_sth->bind_param(6, $max_lon);
    $update_place_sth->bind_param(7, $airport_name);
    $update_place_sth->bind_param(8, $place_node_id);
    $update_place_sth->execute();
    $update_place_sth->finish();


    #
    #   Tags of the airport node
    #
    # create_tag($place_node_id, "P", "icao"); # is already in the master table
    # create_tag($place_node_id, "P", "iata", "unknown"); # Where do I get the IATA from?
    # create_tag($place_node_id, "P", "name"); # is already in the master table
    # create_tag($place_node_id, "P", "is_in", "Asia,Europe,Turkey"); # Ask geonames for that data...
    create_tag($place_node_id, "P", "is_in", $is_in->{$icao_code}) if defined($is_in->{$icao_code});
    create_tag($place_node_id, "P", "created_by", $MYSELF);
    create_tag($place_node_id, "P", "source", $APT_VERSION);
    create_tag($place_node_id, "P", "aeroway", "aerodrome");
    create_tag($place_node_id, "P", "place", "airport");
    create_tag($place_node_id, "P", "type", "civil"); # This is not always right. Where do I get correct data from?
}


sub create_tag
{
    my ($parent_id, $type, $key, $value) = @_;

    local $tablename = "airport_tags";

    my $select_tag_id_sql = "select id from $tablename where parent_id = ? and type = ? and key = ?";
    my $select_tag_id_sth = $dbh->prepare($select_tag_id_sql);
    $select_tag_id_sth->bind_param(1, $parent_id);
    $select_tag_id_sth->bind_param(2, $type);
    $select_tag_id_sth->bind_param(3, $key);
    $select_tag_id_sth->execute();
    my @row = $select_tag_id_sth->fetchrow_array;
    if ($#row == -1)
    {
	my $insert_sql = "insert into $tablename (parent_id, type, key) values (?, ?, ?)";
	my $insert_sth = $dbh->prepare($insert_sql);
	$insert_sth->bind_param(1, $parent_id);
	$insert_sth->bind_param(2, $type);
	$insert_sth->bind_param(3, $key);
	$insert_sth->execute();
	$insert_sth->finish();
    }
    $select_tag_id_sth->finish();

    my $update_sql = "update $tablename set value = ? where parent_id = ? and type = ? and key = ?";
    my $update_sth = $dbh->prepare($update_sql);
    $update_sth->bind_param(1, $value);
    $update_sth->bind_param(2, $parent_id);
    $update_sth->bind_param(3, $type);
    $update_sth->bind_param(4, $key);
    $update_sth->execute();
    $update_sth->finish();
}


sub create_node
{
    my ($parent_id, $type, $lon, $lat) = @_;

    local $tablename = "airport_nodes";

    my $select_tag_id_sql = "select id from $tablename where parent_id = ? and type = ?";
    my $select_tag_id_sth = $dbh->prepare($select_tag_id_sql);
    $select_tag_id_sth->bind_param(1, $parent_id);
    $select_tag_id_sth->bind_param(2, $type);
    $select_tag_id_sth->execute();
    my @row = $select_tag_id_sth->fetchrow_array;
    if ($#row == -1)
    {
	my $insert_sql = "insert into $tablename (parent_id, type) values (?, ?)";
	my $insert_sth = $dbh->prepare($insert_sql);
	$insert_sth->bind_param(1, $parent_id);
	$insert_sth->bind_param(2, $type);
	$insert_sth->execute();
	$insert_sth->finish();
    }
    $select_tag_id_sth->finish();

    my $update_sql = "update $tablename set lon = ?, lat = ? where parent_id = ? and type = ?";
    my $update_sth = $dbh->prepare($update_sql);
    $update_sth->bind_param(1, $lon);
    $update_sth->bind_param(2, $lat);
    $update_sth->bind_param(3, $parent_id);
    $update_sth->bind_param(4, $type);
    $update_sth->execute();
    $update_sth->finish();
}


sub create_way
{
    my ($parent_id, $wayline) = @_;

    local $tablename = "airport_ways";

    local $way_id;

    local ($x, $center_lat, $center_lon, $name, $heading, $length, $x, $width) = split(/\s+/, $wayline);

    my $select_way_id_sql = "select id from $tablename where center_lon = ? and center_lat = ?";
    my $select_way_id_sth = $dbh->prepare($select_way_id_sql);
    $select_way_id_sth->bind_param(1, $center_lon);
    $select_way_id_sth->bind_param(2, $center_lat);
    $select_way_id_sth->execute();
    my @row = $select_way_id_sth->fetchrow_array;
    if ($#row == -1)
    {
	my $insert_way_sql = "insert into $tablename (parent_id, center_lon, center_lat) values (?, ?, ?)";
	my $insert_way_sth = $dbh->prepare($insert_way_sql);
	$insert_way_sth->bind_param(1, $parent_id);
	$insert_way_sth->bind_param(2, $center_lon);
	$insert_way_sth->bind_param(3, $center_lat);
	$insert_way_sth->execute();
	$insert_way_sth->finish();

	$select_way_id_sth->execute();
	@row = $select_way_id_sth->fetchrow_array;
    }
    $select_way_id_sth->finish();

    $way_id = $row[0];

    my $update_way_sql = "update $tablename set runway_number = ?, heading = ?, length = ?, width = ? where parent_id = ? and center_lon = ? and center_lat = ?";
    my $update_way_sth = $dbh->prepare($update_way_sql);
    $update_way_sth->bind_param(1, $name);
    $update_way_sth->bind_param(2, $heading);
    $update_way_sth->bind_param(3, $length * $FEET_TO_METER);
    $update_way_sth->bind_param(4, $width  * $FEET_TO_METER);
    $update_way_sth->bind_param(5, $parent_id);
    $update_way_sth->bind_param(6, $center_lon);
    $update_way_sth->bind_param(7, $center_lat);
    $update_way_sth->execute();
    $update_way_sth->finish();

    create_node($way_id, "C", $center_lon, $center_lat);

    my ($lon_s, $lat_s) = moveTo($center_lon, $center_lat,  $heading, $length / 2);
    create_node($way_id, "S", $lon_s, $lat_s);

    my ($lon_e, $lat_e) = moveTo($center_lon, $center_lat, -$heading, $length / 2);
    create_node($way_id, "E", $lon_e, $lat_e);
}


sub moveTo
{
    my ($lat_from, $lon_from, $distance, $heading) = @_;
    local ($lon, $lat, $dlon);

    $lat_from = $lat_from / 100;
    $lon_from = $lon_from / 100;
    $heading  = $heading  / 360;

    #
    # $lat = asin(sin($lat_from) * cos($distance) + cos($lat_from) * sin($distance) * cos($heading))
    # $dlon = atan2(sin($heading) * sin($distance) * cos($lat_from) , cos($distance) - sin($lat_from) * sin($lat))
    # $lon = mod($lon_from - $dlon + $PI , 2 *  $PI ) - $PI
    #

    $lon = -999;
    $lat = -999;

}


sub read_airports_file
{
    open (APT, "< $APT_FILE") || die ("Can't open $APT_FILE: $!\n");

    while (<APT>)
    {
	chomp;

	if (/^1\s+/)
	{
	    # New airport starting, dump the (previous) valid airport
	    if ($#ways > -1)
	    {
		save_airport();
	    }
	    else
	    {
		print "No ways for airport $icao_code / $airport_name\n";
	    }

	    # Airport Header
	    @foo = split(/\s+/);

	    # print $#foo . " - '" . join("', '", @foo) . "'\n";

	    $icao_code = $foo[4];
	    $airport_name = join(" ", @foo[ 5 .. $#foo ]);
	    $airport_name = $utf8_names->{$icao_code} if (defined($utf8_names->{$icao_code}));

	    @ways = ();

	    # print "    $icao_code -> '$airport_name'\n";
	}
	elsif (/^10\s+/)
	{
	    # Runways and Taxiways
	    @foo = split(/\s+/);

	    # print $#foo . " - '" . join("', '", @foo) . "'\n";

	    $center_lat = $foo[1];
	    $center_lon = $foo[2];

	    push (@ways, $_);
	}
    }
    
    close (APT);
}


sub read_utf8_airport_names
{
    open (UTF8, "< airportnames.utf8");
    while (<UTF8>)
    {
	chomp();
	next if (/^\#/);

	@foo = split(/\t+/, $_);
	if ($#foo == 2)
	{
	    # print "'" . $foo[0] . "' - '" . $foo[1] . "' - '" . $foo[2] . "'\n";
	    $icao_code = $foo[0];

	    $is_in->{$icao_code} = $foo[1] if (!($foo[1] =~ /^\?/));
	    $utf8_names->{$icao_code} = $foo[2];
	}
    }
    close (UTF8);
}


#
#
#
$dbh = DBI->connect('dbi:Pg:dbname=osm') || die ("Can't connect to database: " . $DBI::errstr);

$dbh->do("SET client_encoding to UNICODE");

undef %is_in;
undef %utf8_names;

read_utf8_airport_names();
read_airports_file();

$dbh->disconnect();

