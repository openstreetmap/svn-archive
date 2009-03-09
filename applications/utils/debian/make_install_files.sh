#!/bin/bash
# This script replaces a make ; make install for creation of the debian package.
# Maybe you can also use it to install the stuff on your system.
# If you are successfull, please write how to do this here
# PS.: Any improvements/additions to this installer are welcome.

dst_path=$1

if [ ! -n "$dst_path" ] ; then
    echo "Please specify a Directory to use as Basedirectory"
    echo "Usage:"
    echo "     $0 <working-dir>"
    exit -1 
fi

# define Colors
ESC=`echo -e "\033"`
RED="${ESC}[91m"
GREEN="${ESC}[92m"
YELLOW="${ESC}[93m"
BLUE="${ESC}[94m"
MAGENTA="${ESC}[95m"
CYAN="${ESC}[96m"
WHITE="${ESC}[97m"
BG_RED="${ESC}[41m"
BG_GREEN="${ESC}[42m"
BG_YELLOW="${ESC}[43m"
BG_BLUE="${ESC}[44m"
BG_MAGENTA="${ESC}[45m"
BG_CYAN="${ESC}[46m"
BG_WHITE="${ESC}[47m"
BRIGHT="${ESC}[01m"
UNDERLINE="${ESC}[04m"
BLINK="${ESC}[05m"
REVERSE="${ESC}[07m"
NORMAL="${ESC}[0m"

echo "copying Files to '$dst_path'"
package_name=openstreetmap
dst_path=${dst_path%/}
platform=`uname -m`

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


# #######################################################
# Osmosis
# #######################################################
if false; then
    echo "${BLUE}----------> applications/utils/osmosis/trunk Osmosis${NORMAL}"

    cd osmosis

    # Osmosis moves it's Binary arround, so to be sure we do not catch old Versions and don't 
    # recognize it we remove all osmosis.jar Files first
    ( find . -name "osmosis*.jar" | while read file ; do rm "$file" ; done ) || true

    cd trunk

    ant clean >build.log 2>build.err
    ant dist >>build.log 2>>build.err
    if [ "$?" -ne "0" ] ; then
	echo "${RED}!!!!!! ERROR compiling  Osmosis ${NORMAL}"
	exit -1
    fi
    cd ../..

    osmosis_dir="$dst_path/usr/local/share/osmosis/"
    mkdir -p $osmosis_dir
    cp osmosis/trunk/build/binary/osmosis.jar $osmosis_dir
    if [ "$?" -ne "0" ] ; then
	echo "${RED}!!!!!! ERROR cannot find resulting osmosis.jar ${NORMAL}"
	exit -1
    fi

    # copy needed libs
    mkdir -p $osmosis_dir/lib/
    cp osmosis/trunk/lib/default/*.jar $osmosis_dir/lib/
    if [ "$?" -ne "0" ] ; then
	echo "${RED}!!!!!! ERROR cannot copy needed libs for osmosis${NORMAL}"
	exit -1
    fi

    # Osmosis script
    src_fn="osmosis/trunk/bin/osmosis"
    man1_fn="$man1_path/osmosis.1"
    if grep -q -e "--help" "$src_fn"; then
	echo "Create Man Page from Help '$man1_fn'"
	perl $src_fn --help >"$man1_fn"
    else
	echo "!!!! No idea how to create Man Page for $src_fn"
    fi
    mkdir -p $osmosis_dir/bin
    cp $src_fn "$osmosis_dir/bin/osmosis"
    if [ "$?" -ne "0" ] ; then
	echo "${RED}!!!!!! ERROR cannot find resulting osmosis script ${NORMAL}"
	exit -1
    fi

    ln -sf /usr/local/share/osmosis/bin/osmosis $bin_path/osmosis
fi

# ------------------------------------------------------------------
# Utilities written in C

# ------------------------------------------------------------------
# Various libs
for lib in ccoord libosm libimg  ; do 
    allow_error=false
    if [ "$platform" == "x86_64" ] ; then
	if echo $lib | grep -q -e libosm -e ccoord -e libimg ; then
	    allow_error=true
	fi
    fi

    echo "${BLUE}----------> applications/lib/$lib${NORMAL}"
    cd ../lib/$lib

    rm build.log build.err
    if [ -s "configure" ] ;then
	./configure >>build.log 2>>build.err
    fi

    make clean >>build.log 2>>build.err

    make >>build.log 2>>build.err
    if [ "$?" -ne "0" ] ; then
	if $allow_error ; then
	    echo "Ignored Errors in 'applications/lib/$lib' because it does not compile on my debian $platform machine"
	    cd ..
	    continue
	fi
	echo "${RED}!!!!!! ERROR compiling $lib ${NORMAL}"
	echo "Logfile is at `pwd`/build.log build.err"
	exit -1
    fi
    cd ..
    result_lib=$lib/lib${lib}.a
    result_lib=`find . -name "*${lib}*.a"`
    test -s "$result_lib" || result_lib="$lib/${lib}.a"
    if [ ! -s "$result_lib"  ] ; then
	echo "${RED}!!!!!! ERROR compiling $lib no Resulting '$result_lib'${NORMAL}"
	exit -1
    fi
    cp "$result_lib" "../utils/${lib_path}" || exit -1
done
cd ../utils/

# Perl-libs
for lib in Geo-OSM-MapFeatures ; do 
    echo "${BLUE}----------> applications/lib/$lib${NORMAL} (Compile only)"
    cd ../lib/$lib

    rm build.log build.err
    if [ -s "configure" ] ;then
	./configure >>build.log 2>>build.err
    fi

    if [ -S Makefile ] ; then
	make clean >>build.log 2>>build.err
    fi

    if [ -s "Makefile.PL" ] ; then
	perl Makefile.PL >>build.log 2>>build.err
	if [ "$?" -ne "0" ] ; then
	    echo "${RED}!!!!!! ERROR compiling $lib ${NORMAL}"
	    echo "Logfile is at `pwd`/build.log build.err"
	    exit -1
	fi

    fi

    make >>build.log 2>>build.err
    if [ "$?" -ne "0" ] ; then
	echo "${RED}!!!!!! ERROR compiling $lib ${NORMAL}"
	echo "Logfile is at `pwd`/build.log build.err"
	exit -1
    fi

    cd ..

    #!!!!!!!!!!!!!!!!
    # TODO: Copy resulting *.pm and ManPages
    # 
done
cd ../utils/


# ------------------------------------------------------------------
# Importer
for import in `ls import/*/Makefile| sed 's,/Makefile,,;s,import/,,'` ; do 
    allow_error=false
    if echo $import | grep -q  and_import ; then
	allow_error=true
    fi

    echo "${BLUE}----------> applications/utils/import/$import${NORMAL}"
    cd import/$import/

    rm build.log build.err
    if [ -s "configure" ] ;then
	./configure >>build.log 2>>build.err
    fi

    make clean >>build.log 2>>build.err
    make >>build.log 2>>build.err
    if [ "$?" -ne "0" ] ; then
	if $allow_error ; then
	    echo "Ignored 'applications/import/$import' because it does not compile on my debian machine"
	    cd ../..
	    continue
	fi
	echo "${RED}!!!!!! ERROR compiling  import/${import} ${NORMAL}"
	exit -1
    fi
    cd ../..
    cp import/$import/2AND  ${bin_path}/osm2AND
done

# ------------------------------------------------------------------
# Filter
# As soon it compiles here on my debian machine
# i will remove the excludes
for filter in `ls filter/*/Makefile| sed 's,/Makefile,,;s,filter/,,'` ; do 
    allow_error=false
    if echo $filter | grep -q wayclean ; then
	allow_error=true
    fi

    echo "${BLUE}----------> applications/utils/filter/${filter}${NORMAL}"
    cd filter/${filter}  || exit -1

    rm build.log build.err
    if [ -s "configure" ] ;then
	./configure >>build.log 2>>build.err
    fi

    make clean >>build.log 2>>build.err
    make >>build.log 2>>build.err
    if [ "$?" -ne "0" ] ; then
	if $allow_error ; then
	    echo "Ignored 'applications/filter/$filter' because it does not compile on my debian machine"
	    cd ../..
	    continue
	fi
	echo "${RED}!!!!!! ERROR compiling  filter/${filter} ${NORMAL}"
	exit -1
    fi
    cd ../..
    cp filter/${filter}/${filter} ${bin_path}
done

# ------------------------------------------------------------------
# Export
# As soon it compiles here on my debian machine
# i will remove the excludes
for export in `ls export/*/Makefile| sed 's,/Makefile,,;s,export/,,'` ; do 
    allow_error=false
    if echo $export | grep -q -e osmgarminmap -e osm2shp -e osmgoogleearth; then
	allow_error=true
    fi

    echo "${BLUE}----------> applications/utils/export/${export}${NORMAL}"
    cd export/${export}  || exit -1
    
    rm build.log build.err

    if [ -s "configure" ] ;then
	./configure >>build.log 2>>build.err
    fi

    if [ -s "Makefile.$export" ] ;then
	custom_makefile=" -f Makefile.$export"
    else
	custom_makefile=''
    fi

    make $custom_makefile clean >>build.log 2>>build.err
    make $custom_makefile >>build.log 2>>build.err
    if [ "$?" -ne "0" ] ; then
	if $allow_error ; then
	    echo "Ignored 'applications/export/$export' because it does not compile on my debian machine"
	    cd ../..
	    continue
	fi
	echo "${RED}!!!!!! ERROR compiling  export/${export} ${NORMAL}"
	exit -1 
    fi
    if $allow_error ; then
	echo "${GREEN}Even so it sometimes wasn't compiling; 'applications/export/$export' just compiled good on this machine${NORMAL}"
    fi
    cd ../..
    cp export/${export}/${export} ${bin_path}

    case ${export} in
	osm2pgsql) 
	    cp export/osm2pgsql/default.style ${share_path}/default.style
	    if [ "$?" -ne "0" ] ; then
		echo "${RED}!!!!!! ERROR osm2pgsql/default.style no copied ${NORMAL}"
		exit -1
	    fi
	    ;;
    esac
done

# ------------------------------------------------------------------
if true ; then
    echo "${BLUE}----------> applications/utils/color255${NORMAL}"
    cd color255 || exit -1

    rm build.log build.err
    if [ -s "configure" ] ;then
	./configure >>build.log 2>>build.err
    fi

    make clean >>build.log 2>>build.err
    make >>build.log 2>>build.err
    if [ "$?" -ne "0" ] ; then
	echo "${RED}!!!!!! ERROR compiling color255 ${NORMAL}"
	exit -1
    fi
    cd ..
    cp color255/color255 ${bin_path}
fi


# ------------------------------------------------------------------
if true; then
    echo "${BLUE}----------> applications/planet.osm/C/UTF8Sanitizer${NORMAL}${NORMAL}"
    cd planet.osm/C/  || exit -1

    rm build.log build.err
    if [ -s "configure" ] ;then
	./configure >>build.log 2>>build.err
    fi

    make clean >>build.log 2>>build.err
    make >>build.log 2>>build.err
    if [ "$?" -ne "0" ] ; then
	echo "${RED}!!!!!! ERROR compiling color255 ${NORMAL}"
	exit -1
    fi
    cd ../..
    cp planet.osm/C/UTF8Sanitizer ${bin_path} || exit -1 
fi

# ------------------------------------------------------------------
echo "${BLUE}----------> applications/utils/perl_lib Copy Perl libraries${NORMAL}${NORMAL}"
find perl_lib/ -name "*.pm" | while read src_fn ; do 
    dst_fn="$perl_path/${src_fn#perl_lib/}"
    dst_dir=`dirname "$dst_fn"`
    test -d "$dst_dir" || mkdir -p "$dst_dir"
    cp "$src_fn" "$dst_fn"
done

echo "${BLUE}----------> applications/utils Copy Perl Binaries${NORMAL}${NORMAL}"
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
	echo "${RED}WARNING!!! Perl Hash Bang is missing at File '$src_fn'${NORMAL}"
	echo "           I'm not adding this File to the debian Package"
	echo "First Line: `head -1 $src_fn`"
    fi

    # ----- Try to create man Pages
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

# --------------------------------------------
echo "${BLUE}----------> applications/utils Copy Python Binaries${NORMAL}"
find ./ -name "*.py" | while read src_fn ; do 
    dst_fn="$bin_path/${src_fn##*/}"
    dst_fn="${dst_fn/.py}"
    if head -1 "$src_fn" | grep -q -e '^#! */usr/bin/python' -e '^#!/opt/python-2_5/bin/python' -e '^#!/usr/bin/env python'; then
	cp "$src_fn" "$dst_fn"
    else
	if head -1 "$src_fn" | grep -q -e '^#!/opt/python-2_5/bin/python' -e '^#!/usr/bin/env python'; then
	    (echo '#!/usr/bin/python2.5'; cat "$src_fn") >"$dst_fn"
	else
	    echo "${RED}WARNING!!! Python Hash Bang is missing at File '$src_fn'${NORMAL}"
	    echo "           I'm not adding this File to the debian Package"
	    echo "           First Line: `head -1 $src_fn`"
	fi
    fi
done


#########################################################
# Mapnik installation tool
#########################################################
echo "${BLUE}----------> Mapnik${NORMAL}"
cp export/osm2pgsql/mapnik-osm-updater.sh "$bin_path"
cp export/osm2pgsql/900913.sql "$bin_path"
cp export/osm2pgsql/default.style  "$bin_path"


##################################################################
# XXX
# For later:
# Add java tools, but for these a build.xml with a target jar or similar would be best 
