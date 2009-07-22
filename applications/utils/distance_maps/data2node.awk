BEGIN {
	print "<?xml version='1.0' encoding='UTF-8'?>"
	print "<osm version='0.5' generator='mkcntr'>"
}

{
	print "<node id='-"NR"' lat='"$2"' lon='"$1"'>"
	print "<tag k='ele' v='"$3"' />"
	print "</node>"
}

END {
	print "</osm>"
}
	
