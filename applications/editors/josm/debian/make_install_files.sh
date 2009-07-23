#!/bin/bash

dst_path=debian/openstreetmap-josm

#test -n "$1" || help=1
quiet=" -q "
verbose=1

do_update_icons=true
do_update_josm=true
do_update_josm_ng=true
do_update_plugins=true
do_remove_jar=true
do_cleanup=true

for arg in "$@" ; do
    case $arg in
	--dest-path=*) # Destination path to install the final *.jar Files
	    dst_path=${arg#*=}
	    ;;
	
	--no-update-icons) # Do not update icons
	    do_update_icons=false
	    ;;

	--no-update-josm) # Do not update Josm
	    do_update_josm=false
	    ;;

	--no-update-josm-ng) # Do not update Josm-NG
	    do_update_josm_ng=false
	    ;;

	--no-update-plugins) # Do not update the plugins
	    do_update_plugins=false
	    ;;

	--no-remove-jar) # Do not remove old jar Files
	    do_remove_jar=false
	    ;;

	--no-clean) # no cleanup before build
	    do_cleanup=false
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

    This script tries to compile and copy all josm Files
    and all the plugins.
    In case a plugin will not compile it is omitted.
    "
    # extract options + description from case commands above
    grep -E  -e esac -e '--.*\).*#' -e '^[\t\s 	]+#' $0 | \
	grep -v /bin/bash | sed '/esac/,$d;s/.*--/  --/;s/=\*)/=val/;s/)//;s/#//;' 
    exit;
fi


# define colors
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
package_name=openstreetmap-josm
dst_path=${dst_path%/}

jar_path="$dst_path/usr/local/share/josm"
mkdir -p "$jar_path"

bin_path="$dst_path/usr/bin"
mkdir -p "$bin_path"

#plugin_dir="$dst_path/usr/local/share/josm/plugins"
plugin_dir="$dst_path/usr/lib/josm/plugins"
mkdir -p "$plugin_dir"

mkdir -p "$dst_path/usr/share/josm" 
#( # map-icons to be symlinked
#    cd  "$dst_path/usr/share/josm" 
#    ln -s ../map-icons/classic.small images
#)

# --------------------------------------------
# Try to update Icons
if $do_update_icons ; then
    echo "Now we try our best ... to get more icons ..."
    find ../../share/map-icons/build/square.small -type f -name "*.png" | while read src_file ; do
	file=${src_file#.*square.small/}
	dst_dir="plugins/mappaint/icons/`dirname $file`"
	mkdir -p $dst_dir
        #echo "File $file"
	cp -u "$src_file" "plugins/mappaint/icons/$file"
    done
fi
mkdir -p "$dst_path/usr/lib/josm"

# ------------------------------------------------------------------
# Remove Old Jar Files in dist/*.jar

$do_cleanup && {
    $do_remove_jar && rm -f dist/*.jar
    $do_remove_jar && rm -f plugins/*/dist/*.jar
    }

# ------------------------------------------------------------------
# Compile the Josm Main File(s)
if $do_update_josm ; then
    echo "------------- Compile Josm"
    cd core
    $do_cleanup && ant clean 2>build.err
    ant dist >>build.log 2>>build.err
    rc=$?
    if [ "$rc" -ne "0" ] ; then 
	cat build.log build.err
	echo "${BG_RED}!!!!!!!!!! ERROR $rc compiling josm core${NORMAL}"
	echo "${BG_RED}!!!!!!!!!! See build.log build.err${NORMAL}"
	exit -1 
    fi
    cd ..
fi

# ------------------------------------------------------------------
# Try to Compile as many Josm Plugins as possible
if $do_update_plugins ; then
    echo "------------- Compile Josm Plugin webkit-image for wmsplugin"
    cd plugins/wmsplugin
    $do_cleanup &&     make clean
    make
    cd ../..
    cp plugins/wmsplugin/webkit-image $bin_path/webkit-image
fi
# ------------------------------------------------------------------
# Try to Compile as many Josm Plugins as possible
if $do_update_plugins ; then
    echo "------------- Compile Josm Plugins"
    compiling_error=''
    compiling_ok=''
    cd plugins
    plugins=`ls */build.xml | sed s,/build.xml,,`
    echo "Plugins(`echo "$plugins"| wc -l`): " $plugins
    for dir in $plugins; do 
	cd $dir
	echo -n -e "----- $dir\r"
	$do_cleanup && {
	    $do_remove_jar && rm -f dist/*.jar
	    $do_remove_jar && rm -f ../../dist/$dir.jar
	    rm -f *.log
	    echo "ant clean" >build.log
	    echo "ant clean" >build.err
	    ant -q clean >>build.log 2>>build.err
	}
	echo "ant dist" >>build.log
	echo "ant dist" >>build.err
	ant -q dist >>build.log 2>>build.err
	rc=$?
	number_of_jar=`(find . -name "*.jar" ;find ../../dist -name "$dir.jar")| grep -v '/lib'| wc -l`
	if [ "$rc" -eq "0" ] ; then 
	    echo "${GREEN}------------------------- compiling $dir successfull${NORMAL} 	( $number_of_jar jar Files)"
	    grep -i -e error -e warn *.log *.err
	    compiling_ok="$compiling_ok $dir"
	else		
	    echo "${BG_RED}!!!!!!!!!! ERROR compiling $dir${NORMAL} 	( $number_of_jar jar Files)"
	    #echo "Details see:"
	    #echo "    `pwd`/build.log"
	    #echo "    `pwd`/build.err"
	    compiling_error="$compiling_error $dir"
	fi
	find . -name "*.jar" | grep -v -e '/lib'

	cd ..
    done
    if [ -n "$compiling_error" ] ; then
	echo "${BG_RED}!!!!!!!!!! ERROR compiling Plugins${NORMAL}"
	echo "Details see:"
	
	err_log_path=''
	for dir in $compiling_error; do 
	    echo "    `pwd`/$dir/build.log"
	    err_log_path="$err_log_path $dir/build.log $dir/build.err"
	done
	zip -q errors.zip $err_log_path
	echo "${RED}Combined ERROR Logfiles are at: `pwd`/errors.zip${NORMAL}"
	echo "${RED}Compiling ERRORs(`echo "$compiling_error"| wc -w`): $compiling_error${NORMAL}"
    fi
    echo "Compiling OK(`echo "$compiling_ok"| wc -w`): $compiling_ok"
    cd ..
fi

# ------------------------------------------------------------------
# Compile the Josm-ng Files 
if $do_update_josm_ng ; then
    echo "------------- Compile Josm-ng"
    cd ../josm-ng
    $do_cleanup && ant -q clean
    ant -q josm-ng-impl.jar  >>build.log 2>>build.err
    rc=$?
    if [ "$rc" -ne "0" ] ; then 
	echo "------------- ERROR Compiling Josm-ng"
	echo "${RED}!!!!!!!!!!!!!!!!! WARNING Josm-NG is not included into the package${NORMAL}"
	#exit -1
    fi
    cd ../josm
fi


# ------------------------------------------------------------------
echo "------------- Copy Jar Files"

cp ./core/dist/josm-custom.jar $jar_path/josm.jar || exit -1
rc=$?
if [ "$rc" -ne "0" ] ; then 
    echo "${RED}------------- ERROR Compiling Josm-ng${NORMAL}"
fi
cp ../josm-ng/dist/josm-ng.jar $jar_path/josm-ng.jar || {
    echo "${RED}!!!!!!!!!!!!!!!!! WARNING Josm-NG is not included into the package${NORMAL}"
    #exit -1
}

# Find all existing plugin-jar files and generate a pluginlist from it
plugin_jars=`find dist -name "*.jar"`
plugins=''
for src_fn in $plugin_jars ; do 
    fn="`basename ${src_fn}`"
    dst_fn="$plugin_dir/$fn"
    echo "cp $src_fn $dst_fn"
    cp "$src_fn" "$dst_fn"
    if [ "$?" -ne "0" ] ; then 
	echo "${RED}------------- ERROR Copying $src_fn ${NORMAL}"
	exit -1
    fi
    plugin_name=${fn%.jar}
    echo $plugin_name | grep -q -e plastic_laf -e lang && continue
    plugins="$plugins$plugin_name,"
done || exit -1

# remove last empty plugin definition ,
plugins=${plugins%,}

echo "Activated Plugins:"
echo "$plugins"

# Copy words.cfg for spelling 
mkdir -p "$jar_path/speller"
cp ../../utils/planet.osm/java/speller/words.cfg "$jar_path/speller/" || {
    echo "!!!!!!!!!! words.cfg is missing"
    exit -1
}


# ------------------------------------------------------------------
cp "debian/bin/josm.sh" "$bin_path/josm" || {
    echo "!!!!!!!!!! josm.sh is missing"
    exit -1
}

cp "debian/bin/josm-ng.sh" "$bin_path/josm-ng" || {
    echo "!!!!!!!!!!!!!!!!! WARNING Josm-NG is not included into the package"
    #exit -1
}

# add plugins to default preferences
sed "s/PLUGIN_LIST/$plugins/;" <debian/bin/preferences >"$jar_path/preferences" || {
    echo "!!!!!!!! WARNING cannot create preferences"
    exit -1
}
    

# Copy default Bookmarks
cp nsis/bookmarks "$jar_path/bookmarks"

exit 0
