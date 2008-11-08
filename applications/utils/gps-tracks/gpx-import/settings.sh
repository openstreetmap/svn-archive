#!/bin/sh

# This script configures the environment to use the OSM db
# so that the gpx-import program can find it.

setting () {
  S_N=GPX_$1
  shift
  eval "${S_N}='$*'"
  export ${S_N}
}

# General settings
setting SLEEP_TIME 1

# Paths (can be relative from invocation path if appropriate)
setting PATH_TRACES /home/osm/traces
setting PATH_IMAGES /home/osm/images
setting PATH_TEMPLATES templates/

# MySQL connection
setting MYSQL_HOST localhost
setting MYSQL_USER openstreetmap
setting MYSQL_DB openstreetmap
setting MYSQL_PASS openstreetmap

# Logging, pidfiles etc
# If you comment out the LOGFILE then it will log to stdout
setting LOG_FILE /home/osm/gpx-import.log
# If you comment out the PIDFILE then it will not daemonise
setting PID_FILE /home/osm/gpx-import.pid

# Optional debug statements
#setting INTERPOLATE_STDOUT 1

CMD=$1
shift

case "$CMD" in
    start)
	if test "x$GPX_PID_FILE" = "x"; then
	    exec "$@"
	else
	    "$@"
	    $0 check
	fi
	;;
    stop)
	if test -r $GPX_PID_FILE; then
	    PID=$(cat $GPX_PID_FILE)
	    if test "x$PID" != "x"; then
		if kill -0 $PID; then
		    kill -TERM $PID
		    for TRY in $(seq 1 10); do
			sleep 1
			if ! kill -0 $PID; then
			    echo "GPX daemon killed"
			    rm -f $GPX_PID_FILE
			    exit 0
			else
			    echo "Still running?"
			fi
		    done
		    echo "GPX daemon still running?"
		    exit 1
		else
		    echo "GPX daemon is not running, pid ?= $PID"
		    exit 1
		fi
	    else
		echo "GPX daemon pidfile is empty"
		exit 1
	    fi
	else
	    echo "GPX daemon pidfile is missing"
	    exit 1
	fi
	;;
    rotated)
	if test -r $GPX_PID_FILE; then
	    PID=$(cat $GPX_PID_FILE)
	    if test "x$PID" != "x"; then
		if kill -0 $PID; then
		    kill -HUP $PID
		    echo "GPX daemon sent HUP"
		    sleep 0.5
		    $0 check
		else
		    echo "GPX daemon is not running, pid ?= $PID"
		fi
	    else
		echo "GPX daemon pidfile is empty"
	    fi
	else
	    echo "GPX daemon pidfile is missing"
	fi
	;;
    check)
	if test -r $GPX_PID_FILE; then
	    PID=$(cat $GPX_PID_FILE)
	    if test "x$PID" != "x"; then
		if kill -0 $PID; then
		    echo "GPX daemon is running, pid = $PID"
		else
		    echo "GPX daemon is not running, pid ?= $PID"
		    exit 1
		fi
	    else
		echo "GPX daemon pidfile is empty"
		exit 1
	    fi
	else
	    echo "GPX daemon pidfile is missing"
	    exit 1
	fi
	;;
    *)
	echo "usage: $0 [start|stop|rotated|check] path/to/gpx-import"
	;;
esac
