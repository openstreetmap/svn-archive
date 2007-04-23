#! /usr/bin/perl -w


use DBI;


$APT_VERSION="AptNav200701XP810";
$APT_FILE=$ENV{HOME} . "/osm/" . $APT_VERSION . "/apt.dat";
$MYSELF="airport_import " . qw($Revision$);


sub save_airport
{
    return if (!(defined($icao_code)));

    local $tablename = "airport_place_node";

    # print "    $icao_code -> '$airport_name'\n";

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
    # create_airport_node_tag($place_node_id, "icao"); # is already in the master table
    # create_airport_node_tag($place_node_id, "iata", "unknown"); # Where do I get the IATA from?
    # create_airport_node_tag($place_node_id, "name"); # is already in the master table
    # create_airport_node_tag($place_node_id, "is_in", "Asia,Europe,Turkey"); # Ask geonames for that data...
    create_airport_node_tag($place_node_id, "created_by", $MYSELF);
    create_airport_node_tag($place_node_id, "source", $APT_VERSION);
    create_airport_node_tag($place_node_id, "aeroway", "aerodrome");
    create_airport_node_tag($place_node_id, "place", "airport");
    create_airport_node_tag($place_node_id, "type", "civil"); # This is not always right. Where do I get correct data from?
}


sub create_airport_node_tag
{
    my ($parent_id, $key, $value) = @_;

    local $tablename = "airport_place_node_tags";

    my $select_tag_id_sql = "select id from $tablename where parent_id = ? and key = ?";
    my $select_tag_id_sth = $dbh->prepare($select_tag_id_sql);
    $select_tag_id_sth->bind_param(1, $parent_id);
    $select_tag_id_sth->bind_param(2, $key);
    $select_tag_id_sth->execute();
    my @row = $select_tag_id_sth->fetchrow_array;
    if ($#row == -1)
    {
	my $insert_sql = "insert into $tablename (parent_id, key) values (?, ?)";
	my $insert_sth = $dbh->prepare($insert_sql);
	$insert_sth->bind_param(1, $parent_id);
	$insert_sth->bind_param(2, $key);
	$insert_sth->execute();
	$insert_sth->finish();
    }
    $select_tag_id_sth->finish();

    my $update_sql = "update $tablename set value = ? where parent_id = ? and key = ?";
    my $update_sth = $dbh->prepare($update_sql);
    $update_sth->bind_param(1, $value);
    $update_sth->bind_param(2, $parent_id);
    $update_sth->bind_param(3, $key);
    $update_sth->execute();
    $update_sth->finish();
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


#
#
#
$dbh = DBI->connect('dbi:Pg:dbname=osm') || die ("Can't connect to database: " . $DBI::errstr);
read_airports_file();
$dbh->disconnect();

