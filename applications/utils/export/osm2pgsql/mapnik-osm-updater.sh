#!/bin/bash

export osm_username="osm"
export database_name="gis"
export planet_dir="/home/$osm_username/osm/planet"
export planet_file="$planet_dir/planet.osm.bz2"
export sql_dump="$planet_dir/planet.osm.sql.bz2"
export osm2pgsql_cmd=`which osm2pgsql`
export log_dir=/var/log
test -x "$osm2pgsql_cmd" || osm2pgsql_cmd="$HOME/svn.openstreetmap.org/applications/utils/export/osm2pgsql/osm2pgsql"

osm_planet_mirror_cmd='../../planet-mirror/planet-mirror.pl'
test -x "$osm_planet_mirror_cmd" || osm_planet_mirror_cmd="$HOME/svn.openstreetmap.org/applications/utils/planet-mirror/planet-mirror.pl"
test -x "$osm_planet_mirror_cmd" || osm_planet_mirror_cmd=`which osm-planet-mirror`

test -n "$1" || help=1
quiet=" -q "
verbose=1

for arg in "$@" ; do
    case $arg in
	--all-planet) #	Do all the creation steps listed below from planet file
	    create_osm_user=1
	    mirror=1
	    check_newer_planet=
	    drop=1
	    create_db=1
	    create_db_user=1
	    grant_all_rights_to_user_osm=1
	    planet_fill=1
	    create_db_users=${create_db_users:-*}
	    grant_db_users=${grant_db_users:-*}
	    ;;

	--all-planet-update) #	Do all the creation steps listed below from planet file with up to date cheching
	    create_osm_user=1
	    mirror=1
	    check_newer_planet=1
	    drop=1
	    create_db=1
	    create_db_user=1
	    grant_all_rights_to_user_osm=1
	    planet_fill=1
	    create_db_users=${create_db_users:-*}
	    grant_db_users=${grant_db_users:-*}
	    ;;

	--all-from-dump) #	Do all the creation steps listed below
    		#	from planet-dump file
		#	!!! all-from-dump is not completely tested yet
	    create_osm_user=1
	    mirror_dump=1
	    drop=1
	    create_db=1
	    create_db_user=1
	    grant_all_rights_to_user_osm=1
	    create_db_users=${create_db_users:-*}
	    fill_from_dump="$sql_dump"
	    grant_db_users=${grant_db_users:-*}
	    ;;

	--all-create) #		Do all the creation steps listed below only no data
		      #	import and no planet mirroring
	    create_osm_user=1
	    drop=1
	    create_db=1
	    create_db_user=1
	    grant_all_rights_to_user_osm=1
	    create_db_users=${create_db_users:-*}
	    grant_db_users=${grant_db_users:-*}
	    ;;

	--create-osm-user) #	create the osm-user needed
		#	This means creating a user 'osm' and his home directory
		#	with useradd, mkdir, chmod and chown
	    create_osm_user=1
	    ;;
	
	--mirror) #		mirror planet File (http://planet.openstreetmap.org/)
	    mirror=1
	    ;;

	--no-mirror) #		do not mirror planet File
	    mirror=
	    ;;

	--check-newer-planet) #	Check if Planet File is newer then stampfile. 
	    #		If yes: Continue
	    check_newer_planet=1
	    ;;

	--drop) #		drop the old Database (gis) and Database-user (osm)
	    drop=1
	    ;;

	--create-db) #		create the database (gis)
	    #		with this command only the database is created, 
	    #		but no tables inside it
	    create_db=1
	    ;;
	
	--create-db-user) #	create the database-user (osm)
	    create_db_user=1
	    ;;
	
	--grant-all2osm-user) #	grant all rights for the database to the DB-User osm
	    grant_all_rights_to_user_osm=1
	    ;;

	--create-db-users=*) #Create a Database user for all users specified.
	    #		To create a db-user for all available system-user
	    #		specify *. (Except root))
	    create_db_users=${arg#*=}
	    ;;
	
	--grant-db-users=*) #	Grant database-users all rights (including write, ...)
	    #		to the gis Database !!! This has to be changed in the
	    #		future, normally only the osm user needs update rights
	    grant_db_users=${arg#*=}
	    ;;

	--planet-fill) #	fill database from planet File
	    planet_fill=1
	    ;;

	--mirror-dump) #	mirror the planet.sql dump File
	    mirror_dump=1
	    ;;

	--no-mirror-dump) #	Do not mirror the planet.sql dump File
	    mirror_dump=
	    ;;

	--fill-from-dump=*) #	fill database from Dump File
	    fill_from_dump=${arg#*=}
	    ;;

	--mapnik-dump=*) #	Dump Content of Mapnik Database to a File (.sql|.sql.bz))
	    postgis_mapnik_dump=${arg#*=}
	    ;;
	
	--db-table-create) #	Create tables in Database with osm2pgsql
	    db_table_create=1
	    ;;

	--count-db) #		Count entries in Database. This is to check
	    # 		if the database really contains entries
	    #		if you set an  empty user with the option osm_username=''
	    #		the current user is used
	    count_db=1
	    ;;

	-h)
	    help=1
	    ;;

	--help)
	    help=1
	    ;;

	-help)
	    help=1
	    ;;

	--debug) #		switch on debugging
	    debug=1
	    verbose=1
	    quiet=""
	    ;;

	-debug)
	    debug=1
	    verbose=1
	    quiet=""
	    ;;
	

	--nv) #			be a little bit less verbose
	    verbose=''
	    ;;

	--planet-dir=*) #	define Directory for Planet-File
	    planet_dir=${arg#*=}
	    planet_file="$planet_dir/planet.osm.bz2"
	    ;;

	--planet-file=*) #	define Planet-File including Directory
	    planet_file=${arg#*=}
	    ;;
	
	--osm-username=*) #	Define username to use for DB creation and planet
	    #		download
	    #		!! You shouldn't use your username or root as the
	    #		!! download and install user. 
	    #		This username is the download and install user.
	    #		The osm-user normally only should have the planet files
	    #		in hishome directory and nothing else. By default 
	    #		the osm-username is 'osm'
	    osm_username=${arg#*=}

	    if [ "$osm_username" = "$USER" ] ; then
		echo 
		echo "!!! Don't use your own login account as the osm_username!!" 1>&2
		echo 
		exit 1
	    fi

	    if [ "$osm_username" = "root" ] ; then
		echo 
		echo "!!! Don't use the root account as the osm_username!!" 1>&2
		echo 
		exit 1
	    fi

	    planet_dir="/home/$osm_username/osm/planet"
	    planet_file="$planet_dir/planet.osm.bz2"
	    ;;
	
	--osm2pgsql-cmd=*) #	The path to the osm2pgsql command
	    #		It can be found at
	    #		svn.openstreetmap.org/applications/utils/export/osm2pgsql/
	    #		and has to be compiled. Alternatively you can install
	    #		the Debian Package openstreetmap-utils
	    osm2pgsql_cmd=${arg#*=}
		if ! [ -x "$osm2pgsql_cmd" ]; then
		    echo "Cannot execute '$osm2pgsql_cmd'" 1>&2
		    exit -1
		fi
		;;

	--database-name=*) #	use this name for the database default is 'gis'
	    database_name=${arg#*=}
	    ;;

	*)
	    echo ""
	    echo "!!!!!!!!! Unknown option $arg"
	    echo ""
	    help=1
	    ;;
    esac
done

if [ -n "$help" ] ; then
    # extract options from case commands above
    options=`grep -E -e esac -e '\s*--.*\).*#' $0 | sed '/esac/,$d;s/.*--/ [--/; s/=\*)/=val]/; s/)[\s ]/]/; s/#.*\s*//; s/[\n/]//g;'`
    options=`for a in $options; do echo -n " $a" ; done`
    echo "$0 $options"
    echo "
!!! Warning: This Script is for now a quick hack to make setting up
!!! Warning: My databases easier. Please check if it really works for you!!
!!! Warning: Especially when using different Database names or username, ...
!!! Warning: not every combination of values except the default is tested.

    This script tries to install the mapnik database.
    For this it first creates a new user osm on the system
    and mirrors the current planet to his home directory.
    Then this planet is imported into the postgis Database from a 
    newly created user named osm

    This script uses sudo. So you either have to have sudo right or you'll 
    have to start the script as root. The users needed will be postgres and osm
    "
    # extract options + description from case commands above
    grep -E  -e esac -e '--.*\).*#' -e '^[\t\s 	]*#' $0 | grep -v /bin/bash | sed '/esac/,$d;s/.*--/  --/;s/=\*)/=val/;s/)//;s/#//;' 
    exit;
fi


if [ -n "$osm_username" ] ; then
    sudo_cmd="sudo -u $osm_username"
else
    sudo_cmd=''
fi

export import_stamp_file=${log_dir}/osm2pgsql_postgis-$database_name.stamp
export import_log=${log_dir}/osm2pgsql_postgis-$database_name.log


if [ -n "$debug" ] ; then
        echo "Planet File: `ls -l $planet_file`"
        echo "Import Stamp : `ls -l $import_stamp_file`"
fi


############################################
# Create a user on the system
############################################
if [ -n "$create_osm_user" ] ; then
    test -n "$verbose" && echo "----- Check if we already have an user '$osm_username'"
    
    if ! id "$osm_username" >/dev/null; then
	echo "create '$osm_username' User"
	useradd "$osm_username"
    fi
    
    mkdir -p "/home/$osm_username/osm/planet"
    # The user itself should be allowed to read/write all his own files
    # in the ~/osm/ Directory
    chown "$osm_username" "/home/$osm_username"
    chown -R "$osm_username" "/home/$osm_username/osm"
    chmod +rwX "/home/$osm_username"
    chmod -R +rwX "/home/$osm_username/osm"

    # Everyone on the system is allowed to read the planet.osm Files
    chmod -R a+rX "/home/$osm_username/osm"
fi


############################################
# Mirror the planet File from planet.openstreetmao.org
############################################
if [ -n "$mirror" ] ; then
    test -n "$verbose" && echo "----- Mirroring planet File"
    if ! [ -x "$osm_planet_mirror_cmd" ]; then
	echo "Cannot execute '$osm_planet_mirror_cmd'" 1>&2
	exit -1
    fi
    if ! $sudo_cmd $osm_planet_mirror_cmd -v -v --planet-dir=$planet_dir ; then 
	echo "Cannot Mirror Planet File" 1>&2
	exit 1
    fi
    if ! [ -s $planet_file ] ; then
        echo "File $planet_file is missing"
        exit -1
    fi


fi

############################################
# Check if Planet File is newer than import Stamp
############################################
if [ -n "$check_newer_planet" ] ; then
    if [ "$planet_file" -nt "$import_stamp_file" ] ; then
	if [ -n "$verbose" ] ; then
	    echo "----- New File needs updating"
            echo "Planet File: `ls -l $planet_file`"
            echo "Import Stamp : `ls -l $import_stamp_file`"
	fi
    else
	echo "Postgis Database already Up To Date"
	echo "`ls -l $import_stamp_file`"
	exit 0
    fi
fi

############################################
# Drop the old Database and Database-user
############################################
if [ -n "$drop" ] ; then
    test -n "$verbose" && echo "----- Drop complete Database '$database_name' and user '$osm_username'"
    echo "CHECKPOINT" | sudo -u postgres psql $quiet
    sudo -u postgres dropdb $quiet -Upostgres   "$database_name"
    sudo -u postgres dropuser $quiet -Upostgres "$osm_username"
fi

############################################
# Create db
############################################
if [ -n "$create_db" ] ; then
    test -n "$verbose" && echo
    test -n "$verbose" && echo "----- Create Database '$database_name'"
    if ! sudo -u postgres createdb -Upostgres  $quiet  -EUTF8 "$database_name"; then
        echo "Creation Failed"
        exit -1
    fi
    if ! sudo -u postgres createlang plpgsql "$database_name"; then
        echo "Creation Failed"
        exit -1
    fi

    if ! sudo -u postgres psql $quiet -Upostgres "$database_name" </usr/share/postgresql-8.2-postgis/lwpostgis.sql ; then
        echo "Creation Failed"
        exit -1
    fi
fi

############################################
# Create db-user
############################################
if [ -n "$create_db_user" ] ; then
    test -n "$verbose" && echo "----- Create Database-user '$osm_username'"
    sudo -u postgres createuser -Upostgres  $quiet -S -D -R "$osm_username"  || exit -1 
fi

if [ -n "$grant_all_rights_to_user_osm" ] ; then
    test -n "$verbose" && echo 
    test -n "$verbose" && echo "----- Grant rights on Database '$database_name' for '$osm_username'"
    (
	echo "GRANT ALL ON SCHEMA PUBLIC TO \"$osm_username\";" 
	echo "GRANT ALL on geometry_columns TO \"$osm_username\";"
	echo "GRANT ALL on spatial_ref_sys TO \"$osm_username\";" 
	echo "GRANT ALL ON SCHEMA PUBLIC TO \"$osm_username\";" 
    ) | sudo -u postgres psql $quiet -Upostgres "$database_name"
fi

############################################
# Create a Database user for all users specified (*) or available on the system. Except root
############################################
if [ -n "$create_db_users" ] ; then

    if [ "$create_db_users" = "*" ] ; then
        echo "Create DB User for every USER"
        create_db_users=''
        for user in `users|grep -v -e root` ; do 
            create_db_users="$create_db_users $user"
        done
    fi

    for user in $create_db_users; do
        sudo -u postgres createuser $quiet -Upostgres --no-superuser --no-createdb --no-createrole "$user"
    done
fi

############################################
# Grant all rights on the gis Database to all system users or selected users in the system
############################################
if [ -n "$grant_db_users" ] ; then

    if [ "$grant_db_users" = "*" ] ; then
        echo "GRANT Rights to every USER"
        grant_db_users=''
        for user in `users` ; do 
	    echo "$user" | grep -q "root" && continue
	    echo " $grant_db_users " | grep -q " $user " && continue
            grant_db_users="$grant_db_users $user"
        done
    fi

    test -n "$verbose" && echo "Granting rights to users: '$grant_db_users'"

    for user in $grant_db_users; do
        echo "Granting all rights to user '$user' for Database '$database_name'"
        (
            echo "GRANT ALL on geometry_columns TO \"$user\";"
            echo "GRANT ALL ON SCHEMA PUBLIC TO \"$user\";"
            echo "GRANT ALL on spatial_ref_sys TO \"$user\";"
            echo "GRANT ALL on TABLE planet_osm_line TO \"$user\";"
            echo "GRANT ALL on TABLE planet_osm_point TO \"$user\";"
            )| sudo -u postgres psql $quiet -Upostgres "$database_name" || true
    done
fi


############################################
# Create Database tables with osm2pgsql
############################################
if [ -n "$db_table_create" ] ; then
    if ! [ -x "$osm2pgsql_cmd" ]; then
	echo "Cannot execute '$osm2pgsql_cmd'" 1>&2
	exit -1
    fi
    echo ""
    echo "--------- Unpack and import $planet_file"
    $sudo_cmd $osm2pgsql_cmd --create "$database_name"
fi

############################################
# Fill Database from planet File
############################################
if [ -n "$planet_fill" ] ; then
    if ! [ -x "$osm2pgsql_cmd" ]; then
	echo "Cannot execute '$osm2pgsql_cmd'" 1>&2
	exit -1
    fi
    echo ""
    echo "--------- Unpack and import $planet_file"
    echo "Import started: `date`" >>"$import_log"
    $sudo_cmd $osm2pgsql_cmd --database "$database_name" $planet_file
    echo "`date`: Import Done: `ls -l $planet_file` import --> $rc" >> "$import_log"
    if [ $? ]; then
	echo "!!!!!!!! ERROR while running '$osm2pgsql_cmd --database "$database_name" $planet_file'"
        echo "Creation with for Database "$database_name" from planet-file '$planet_file' with '$osm2pgsql_cmd' Failed"
        echo "see Logfile for more Information:"
        echo "less $import_log"
	exit -1
    fi
    echo "`date`: `ls -l $planet_file` import --> $rc" >>$import_stamp_file
    touch --reference=$planet_file $import_stamp_file

fi


############################################
# Dump the complete Database
############################################
if [ -n "$postgis_mapnik_dump" ] ; then
	# get Database Content with Dump
    postgis_mapnik_dump_dir=`dirname $postgis_mapnik_dump`
	mkdir -p "$postgis_mapnik_dump_dir"
	case "$postgis_mapnik_dump" in
	    *.bz2)
		$sudo_cmd pg_dump --data-only -U "$osm_username" "$database_name" | bzip2 >"$postgis_mapnik_dump"
		;;
	    *.gz)
		$sudo_cmd pg_dump --data-only -U "$osm_username" "$database_name" | gzip >"$postgis_mapnik_dump"
		;;
	    *)
		$sudo_cmd pg_dump --data-only -U "$osm_username" "$database_name" >"$postgis_mapnik_dump"
		;;
	esac
fi

############################################
# Mirror the planet-dump File from planet.openstreetmap.de
############################################
if [ -n "$mirror_dump" ] ; then
    test -n "$verbose" && echo "----- Mirroring planet-dump File"
    wget -v --mirror http://planet.openstreetmap.de/planet.osm.sql.bz2 \
	--no-directories --directory-prefix=$planet_dir/
fi


############################################
# Fill Database from Dump File
############################################
if [ -n "$fill_from_dump" ] ; then
    echo ""
    echo "--------- Import from Dump '$fill_from_dump'"
	case "$fill_from_dump" in
	    *.bz2)
		test -n "$verbose" && echo "Uncompress File ..."
		bzip2 -dc "$fill_from_dump" | $sudo_cmd psql $quiet "$database_name"
		;;
	    *.gz)
		test -n "$verbose" && echo "Uncompress File ..."
		gzip -dc "$fill_from_dump" | $sudo_cmd psql $quiet "$database_name"
		;;
	    *)
		test -n "$verbose" && echo "Import uncompressed File ..."
		$sudo_cmd psql $quiet "$database_name" <"$fill_from_dump"
		;;
	esac
fi


############################################
# Check number of entries in Database
############################################
if [ -n "$count_db" ] ; then
    echo ""
    echo "--------- Check Number of lines in Database '$database_name'"

    # Get the Table names
    if [ -n "$osm_username" ]; then
	table_owner=" AND tableowner ='$osm_username' ";
    fi
    table_names=`echo "SELECT tablename from pg_catalog.pg_tables where schemaname = 'public' $tableowner;" | \
	$sudo_cmd psql   "$database_name" -h /var/run/postgresql | grep -E -e '^ planet'`

    echo "Counting entries in all Tables (" $table_names ")"
    for table in $table_names; do 
	echo -n "Table $table	= "
	echo "SELECT COUNT(*) from $table;" | \
	    $sudo_cmd psql  gis -h /var/run/postgresql | grep -v -e count -e '------' -e '1 row' | head -1
    done
fi

