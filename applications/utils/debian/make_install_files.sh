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

jar_path="$dst_path/usr/share/java/$package_name"
mkdir -p "$jar_path"

bin_path="$dst_path/usr/bin"
mkdir -p "$bin_path"


# ------------------------------------------------------------------
# Compile the Jar Files 
cd core
ant build
cd ..
cd plugins
ant build
cd ..


# ------------------------------------------------------------------
# Copy Jar Files

cp ./core/dist/josm-custom.jar $jar_path/josm.jar

dst_dir="$jar_path/plugins"
test -d "$dst_dir" || mkdir -p "$dst_dir"
find core plugins -name "*.jar" | while read src_fn ; do 
    fn="`basename ${src_fn}`"
    dst_fn="$dst_dir/$fn"
    echo "cp $src_fn $dst_fn"
    cp "$src_fn" "$dst_fn"
done

# ------------------------------------------------------------------
cat > "$bin_path/josm" <<EOF
#!/bin/sh
josm_bin="/usr/share/java/$package_name/josm.jar"

# ls -l "\$josm_bin"
# unzip -p    \$josm_bin REVISION | grep "Last Changed"

# proxy=" -Dhttp.proxyHost=gw-squid -Dhttp.proxyPort=8888 "

java -Xmx500m \$proxy -jar "\$josm_bin"  "\$@"

EOF
