#!/bin/sh

dst_path=$1

if [ ! -n "$dst_path" ] ; then
    echo "Please specify a Directory to use as Basedirectory"
    echo "Usage:"
    echo "     $0 <working-dir>"
    exit -1 
fi

echo "copying Files to '$dst_path'"
package_name=openstreetmap
dst_path=${dst_path%/}

perl_path="$dst_path/usr/share/perl5"
bin_path="$dst_path/usr/bin"
lib_path="$dst_path/usr/lib"
share_path="$dst_path/usr/share/$package_name"
man1_path="$dst_path/usr/man/man1"
mkdir -p "$perl_path"
mkdir -p "$bin_path"
mkdir -p "$lib_path"
mkdir -p "$share_path"
mkdir -p "$man1_path"


# ------------------------------------------------------------------
# Utilities written in C

echo ""
echo "---> import/and2osm"
(
    cd import/and2osm/
    make clean
    make || exit -1
    ) || exit -1
cp import/and2osm/2AND  ${bin_path}/osm2AND

#if false ; then
# tweety@pack:~/svn.openstreetmap.org/applications/lib/libosm$ make
# g++ -g -I/usr/local/include -I../ccoord   -c -o Components.o Components.cpp
# Components.cpp:27:29: error: libshp/shapefil.h: No such file or directory
# so 
#    libosm libimg
# have to be added later to the loop
for lib in ccoord ; do 
	(
	    echo ""
	    echo "---> lib/$lib"
	    cd ../lib/$lib
	#    for a in *.cpp ; do perl -p -i -e 's,libshp/shapefil.h,shapefil.h,g' $a; done
	    make clean
	    make || exit -1
	    ) || exit -1

	cp ../lib/$lib/lib${lib}.a ${lib_path}
    done

# As soon as libosm compiles here on my debian machine
if false ; then
    echo ""
    echo "---> filter/wayclean"
    (
	cd filter/wayclean
	make clean
	make || exit -1
	) || exit -1
    cp filter/wayclean/wayclean ${bin_path}

    echo ""
    echo "---> osm2shp"
    ( 
	cd export/osm2shp
	make clean
	make || exit -1
	) || exit -1
    cp export/osm2shp/osm2shp ${bin_path}
fi
# of later

echo ""
echo "---> color255"
(
    cd color255
    make clean
    make || exit -1
) || exit -1
cp color255/color255 ${bin_path}

echo ""
echo "---> osm2pqsql"
(
    cd export/osm2pgsql
    make clean
    make || exit -1
    ) || exit -1 
cp export/osm2pgsql/osm2pgsql ${bin_path}

echo ""
echo "---> UTF8Sanitizer"
(
    cd planet.osm/C/
    make clean
    make || exit -1
) || exit -1
cp planet.osm/C/UTF8sanitizer ${bin_path}


# ------------------------------------------------------------------
# Copy Perl libraries
find perl_lib/ -name "*.pm" | while read src_fn ; do 
    dst_fn="$perl_path/${src_fn#perl_lib/}"
    dst_dir="`dirname "$dst_fn"`"
    test -d "$dst_dir" || mkdir -p "$dst_dir"
    cp "$src_fn" "$dst_fn"
done

# Copy Perl Binaries
find ./ -name "*.pl" | while read src_fn ; do 
    dst_fn="$bin_path/${src_fn##*/}"
    filename="`basename $src_fn`"
    dst_fn="${dst_fn/.pl}"
    if ! echo $dst_fn | grep -e osm ; then
	dst_fn="`dirname ${dst_fn}`/osm-`basename ${dst_fn}`"
    fi
    man1_fn="$man1_path/${filename%.pl}.1"
    if head -1 "$src_fn" | grep -q -e '^#! */usr/bin/perl' ; then
	cp "$src_fn" "$dst_fn"
    else
	echo "WARNING!!! Perl Hash Bang is missing at File '$src_fn'"
	echo "           I'm not adding this File to the debian Package"
	echo "First Line: `head -1 "$src_fn"`"
    fi



    if perldoc "$src_fn" >/dev/null 2>&1 ; then
	echo "Create Man Page from pod '$man1_fn'"
	pod2man $src_fn >"$man1_fn"
    else
	if grep -q -e "--man" "$src_fn"; then
	    echo "Create Man Page '$man1_fn'"
	    perl $src_fn --man >"$man1_fn"
	else
	    if grep -q -e "--help" "$src_fn"; then
		echo "Create Man Page from Help '$man1_fn'"
		perl $src_fn --help >"$man1_fn"
	    else
		echo "!!!! No idea how to create Man Page for $src_fn"
	    fi
	fi
    fi
done

# Copy Python Binaries
find ./ -name "*.py" | while read src_fn ; do 
    dst_fn="$bin_path/${src_fn##*/}"
    dst_fn="${dst_fn/.py}"
    if head -1 "$src_fn" | grep -q -e '^#! */usr/bin/python' -e '^#!/usr/bin/env python'; then
	cp "$src_fn" "$dst_fn"
    else
	echo "WARNING!!! Python Hash Bang is missing at File '$src_fn'"
	echo "           I'm not adding this File to the debian Package"
	echo "           First Line: `head -1 "$src_fn"`"
    fi
done




# XXX
# For later:
# Add java tools, but for these a build.xml with a target jar or similar would be best 


# #######################################################
# Osmosis
# #######################################################
cd osmosis
ant dist_binary
cd ..
mkdir -p $dst_path/usr/local/share/osmosis/
cp ./osmosis/dist/result/osmosis.jar $dst_path/usr/local/share/osmosis/


cp debian/osmosis.sh "$bin_path/osmosis"


#########################################################
# Mapnik installation tool
#########################################################
cp export/osm2pgsql/mapnik-osm-updater.sh "$bin_path"
