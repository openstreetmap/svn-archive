#!/bin/sh

dst_path=$1

if [ ! -n "$dst_path" ] ; then
    echo "Please specify a Directory to use as Basedirectory"
    echo "Usage:"
    echo "     $0 <working-dir>"
    exit -1 
fi

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
mkdir -p "$dst_path/usr/lib/josm"

# ------------------------------------------------------------------
# Compile the Jar Files 
echo "------------- Compile Josm"
cd core
ant -q clean
ant -q compile || exit -1
cd ..

echo "------------- Compile Josm Plugins"
cd plugins
ant -q clean
ant -q dist|| exit -1
cd ..

# Compile the Josm-ng Files 
echo "------------- Compile Josm-ng"
cd ../josm-ng
    ant -q clean
    ant -q josm-ng-impl.jar || exit -1
cd ../josm


# ------------------------------------------------------------------
echo "------------- Copy Jar Files"

cp ./core/dist/josm-custom.jar $jar_path/josm.jar || exit -1
cp ../josm-ng/dist/josm-ng.jar $jar_path/josm-ng.jar || exit -1

plugin_jars=`find dist -name "*.jar"`
for src_fn in $plugin_jars ; do 
    fn="`basename ${src_fn}`"
    dst_fn="$plugin_dir/$fn"
    echo "cp $src_fn $dst_fn"
    cp "$src_fn" "$dst_fn" || exit -1
    plugin_name=${fn%.jar}
    echo $plugin_name | grep -q -e plastic_laf -e lang && continue
    plugins="$plugins$plugin_name,"
done || exit -1

# remove last empty plugin definition ,
plugins=${plugins%,}

echo "Activated Plugins:"
echo "$plugins"

mkdir -p "$jar_path/speller"
cp ../utils/planet.osm/java/speller/words.cfg "$jar_path/speller/"

# ------------------------------------------------------------------
cp "debian/bin/josm.sh" "$bin_path/josm"
cp "debian/bin/josm-ng.sh" "$bin_path/josm-ng"

sed "s/PLUGIN_LIST/$plugins/;" <debian/bin/preferences >"$jar_path/preferences"
cp nsis/bookmarks "$jar_path/bookmarks"
