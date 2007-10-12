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
echo "Compile Josm"
cd core
ant compile || exit -1
cd ..

echo "Compile Josm Plugins"
cd plugins
ant build || exit -1
cd ..


# ------------------------------------------------------------------
# Copy Jar Files

cp ./core/dist/josm-custom.jar $jar_path/josm.jar || exit -1

plugin_jars=`find plugins -name "*.jar"`
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

#mkdir -p "$jar_path/speller"
#cp ../utils/planet.osm/java/speller/words.cfg "$jar_path/speller/"

# Maybe this has to be removed, since it is inside the plugin?
#cp plugins/mappaint/styles/osmfeatures/elemstyles.xml "$jar_path/elemstyles.xml"
#mkdir -p "$jar_path/plugins/mappaint/standard"
#cp plugins/mappaint/styles/osmfeatures/elemstyles.xml "$jar_path/plugins/mappaint/standard/elemstyles.xml"
# ------------------------------------------------------------------
cat > "$bin_path/josm" <<EOF
#!/bin/sh
josm_dir="/usr/local/share/josm"
josm_bin="\$josm_dir/josm.jar"

test -d ~/.josm/plugins/ || mkdir -p ~/.josm/plugins/
#for dir in mappaint osmarender validator tways-0.1; do
for dir in ${plugins//,/ } ; do
    test -d ~/.josm/plugins/\$dir || mkdir -p ~/.josm/plugins/\$dir
done
test -d ~/.josm/plugins/mappaint/standard || mkdir -p ~/.josm/plugins/mappaint/standard

if ! [ -s ~/.josm/preferences ]; then
     echo "Installing Preferences File"
     cp "\$josm_dir/preferences"  ~/.josm/preferences
fi

if ! [ -s ~/.josm/bookmarks ]; then
     echo "Installing Bookmarks File"
     cp "\$josm_dir/bookmarks"  ~/.josm/bookmarks
fi

if ! [ -s ~/.josm/plugins/mappaint/standard/elemstyles.xml ]; then
#     echo "Installing Elemstyles File"
#     cp "\$josm_dir/elemstyles.xml"  ~/.josm/plugins/mappaint/standard/elemstyles.xml
      true
fi

# ls -l "\$josm_bin"
# unzip -p    \$josm_bin REVISION | grep "Last Changed"

# proxy=" -Dhttp.proxyHost=gw-squid -Dhttp.proxyPort=8888 "

java -Djosm.resource=/usr/share/map-icons/square.small \
     -Xmx500m \
     \$proxy \
     -jar "\$josm_bin"\
     "\$@"

EOF


cat > "$jar_path/preferences" <<EOF
download.gps=false
download.newlayer=false
download.osm=true
download.tab=1
lakewalker.python=/usr/bin/python
layerlist.visible=true
osm-server.url=http://www.openstreetmap.org/api
plugins=$plugins
projection=org.openstreetmap.josm.data.projection.Epsg4326
propertiesdialog.visible=true
propertiesdialog.visible=true
propertiesdialog.visible=true
toolbar=download;upload;|;new;open;save;exportgpx;|;undo;redo;|;preference
validator.SpellCheck.checkKeys=true
validator.SpellCheck.checkKeysBeforeUpload=true
validator.SpellCheck.checkValues=true
validator.SpellCheck.checkValuesBeforeUpload=true
validator.SpellCheck.sources=/usr/local/share/josm/speller/words.cfg
ywms.firefox=firefox
ywms.port=8000
EOF

cat > "$jar_path/bookmarks" <<EOF
Muenchen+,47.983424415942416,11.402620097655612,48.36334800308583,12.002250823113542
Muenchen-,48.05109190794662,11.447878885677385,48.246831966462025,11.703938333879364
Kirchheim,48.14904045814527,11.728348604380155,48.18983784113904,11.79273346326812
Muc_Altstadtring,48.125724515280666,11.553433712891074,48.15107325612488,11.596158188775085
Mainz,49.58,8.14,49.6,8.16
Erlangen,49.53530551899356,10.893663089997254,49.64013443292672,11.07554098888691
Ingolstadt,48.615608086215175,11.232933428311759,48.893652866507985,11.728832483590338
EOF
